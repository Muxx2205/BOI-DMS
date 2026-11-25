package pmsby.bulk.delete.background.task;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.FinacleCSVConsentSyncBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)
public class FinacleCSVConsentSyncBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(FinacleCSVConsentSyncBackgroundTaskExecutor.class);

    // ----------------------------------------------------------------------
    //  Configuration – adjust as needed
    // ----------------------------------------------------------------------
    private static final String FINACLE_URL = "https://psms.starfina.boicorp.net:35500/FISERVLET/fihttp";
    private static final String BATCH_API_URL = "https://stardms.bankofindia.co.in/o/c/pmsbypmjjyconsentforms/batch";
    private static final long SITE_ID = 290702L;
    private static final String AUTH_USERNAME = "boidmsadmin";
    private static final String AUTH_PASSWORD = "boi321vspl";
    private static final long OBJECT_DEFINITION_ID = 62979176L; // adjust if needed

    private static final int CSV_BATCH_SIZE = 500;   // rows to read at once
    private static final int UPDATE_BATCH_SIZE = 500; // records per Batch API call
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int READ_TIMEOUT = 60000;

    // Statistics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalSuccess = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicInteger totalNotFound = new AtomicInteger(0);

    private long companyId;
    private long userId;
    private long groupId;
    private String urnColumn;
    private String customerIdColumn;
    private String accountNumberColumn;
    private String bimaTypeColumn;
    private String delimiter;

    private BufferedWriter reportWriter;
    private String reportFilePath;

    // Batch accumulator (same as your existing)
    private final List<Map<String, Object>> pendingBatchData = new ArrayList<>();
    private int pendingBatchCount = 0;

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

 @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContext = backgroundTask.getTaskContextMap();
        String csvPath = GetterUtil.getString(taskContext.get("csvFilePath"));
        companyId = GetterUtil.getLong(taskContext.get("companyId"));
        groupId = GetterUtil.getLong(taskContext.get("groupId"), 0L);
        userId = GetterUtil.getLong(taskContext.get("userId"));
        urnColumn = GetterUtil.getString(taskContext.get("urnColumn"), "URN");
        customerIdColumn = GetterUtil.getString(taskContext.get("customerIdColumn"), "customerId");
        accountNumberColumn = GetterUtil.getString(taskContext.get("accountNumberColumn"), "accountNumber");
        bimaTypeColumn = GetterUtil.getString(taskContext.get("bimaTypeColumn"), "bimaType");
        delimiter = GetterUtil.getString(taskContext.get("delimiter"), ",");

        User user = _userLocalService.getUser(userId);
        _log.info("==========================================================");
        _log.info("Finacle CSV Consent Sync started by " + user.getEmailAddress());
        _log.info("CSV file: " + csvPath);
        _log.info("==========================================================");

        // Prepare report file
        initReportFile();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(csvPath)))) {
            String line;
            String[] headers = null;
            boolean firstLine = true;
            List<Map<String, String>> batchRows = new ArrayList<>(CSV_BATCH_SIZE);

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    headers = line.split(delimiter);
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(delimiter);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                batchRows.add(row);

                if (batchRows.size() >= CSV_BATCH_SIZE) {
                    processBatch(batchRows);
                    batchRows.clear();
                }
            }
            if (!batchRows.isEmpty()) {
                processBatch(batchRows);
            }
            // Final flush
            flushBatchUpdate();
        } catch (Exception e) {
            _log.error("CSV processing error", e);
        } finally {
            closeReportWriter();
        }

        _log.info("==========================================================");
        _log.info("CSV Consent Sync completed.");
        _log.info("Processed: " + totalProcessed.get() +
                  " Success: " + totalSuccess.get() +
                  " Failed: " + totalFailed.get() +
                  " Not Found: " + totalNotFound.get() +
                  " Errors: " + totalErrors.get());
        _log.info("Report saved: " + reportFilePath);
        _log.info("==========================================================");
        return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
    }

    private void processBatch(List<Map<String, String>> rows) {
        for (Map<String, String> row : rows) {
            try {
                String urn = row.get(urnColumn);
                if (Validator.isNull(urn)) {
                    logToReport(urn, "SKIPPED", "URN missing", "", "");
                    totalErrors.incrementAndGet();
                    totalProcessed.incrementAndGet();
                    continue;
                }

                // Find ObjectEntry by URN
                ObjectEntry entry = findObjectEntryByURN(urn);
                if (entry == null) {
                    logToReport(urn, "NOT_FOUND", "URN not found in database", "", "");
                    totalNotFound.incrementAndGet();
                    totalProcessed.incrementAndGet();
                    continue;
                }

                // Get values from CSV or fallback to existing object values
                String customerId = row.get(customerIdColumn);
                if (Validator.isNull(customerId)) {
                    customerId = (String) entry.getValues().get("customerId");
                }
                String accountNumber = row.get(accountNumberColumn);
                if (Validator.isNull(accountNumber)) {
                    accountNumber = (String) entry.getValues().get("accountNumber");
                }
                String bimaType = row.get(bimaTypeColumn);
                if (Validator.isNull(bimaType)) {
                    bimaType = (String) entry.getValues().get("typeOfInsurance");
                }
                Date modifiedDate = entry.getModifiedDate();

                // Build Finacle request
                String xmlRequest = buildFinacleRequest(urn, customerId, accountNumber, bimaType, modifiedDate);
                String responseXml = callFinacleApi(xmlRequest);
                if (responseXml != null) {
                    BusinessResponse business = parseBusinessResponse(responseXml);
                    if (business.isSuccess) {
                        addToBatchUpdate(entry, 200, "SUCCESS", business.message);
                        totalSuccess.incrementAndGet();
                        logToReport(urn, "SUCCESS", "200", "SUCCESS", business.message);
                    } else {
                        addToBatchUpdate(entry, 500, "FAILED", business.message);
                        totalFailed.incrementAndGet();
                        logToReport(urn, "FAILED", "500", "FAILED", business.message);
                    }
                } else {
                    addToBatchUpdate(entry, 500, "FAILED", "Network error – no response");
                    totalErrors.incrementAndGet();
                    logToReport(urn, "FAILED", "500", "FAILED", "Network error");
                }
                totalProcessed.incrementAndGet();
            } catch (Exception e) {
                _log.error("Error processing row", e);
                totalErrors.incrementAndGet();
                totalProcessed.incrementAndGet();
                logToReport(row.get(urnColumn), "ERROR", "", "", e.getMessage());
            }
        }
    }

   // ----------------------------------------------------------------------
    //  Object lookup by URN
    // ----------------------------------------------------------------------
    private ObjectEntry findObjectEntryByURN(String urn) {
        try {
            DynamicQuery dq = _objectEntryLocalService.dynamicQuery();
            dq.add(PropertyFactoryUtil.forName("objectDefinitionId").eq(OBJECT_DEFINITION_ID));
            dq.add(PropertyFactoryUtil.forName("uRN").eq(urn));
            List<ObjectEntry> entries = _objectEntryLocalService.dynamicQuery(dq);
            return entries.isEmpty() ? null : entries.get(0);
        } catch (Exception e) {
            _log.error("Error finding URN: " + urn, e);
            return null;
        }
    }

    // ----------------------------------------------------------------------
    //  Batch update accumulator (reusing your existing logic)
    // ----------------------------------------------------------------------
    private void addToBatchUpdate(ObjectEntry entry, int statusCode, String status, String message) {
        Map<String, Serializable> existing = entry.getValues();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", entry.getObjectEntryId());
        for (Map.Entry<String, Serializable> f : existing.entrySet()) {
            if (!f.getKey().startsWith("_")) updateData.put(f.getKey(), f.getValue());
        }
        updateData.put("finacleStatusCode", String.valueOf(statusCode));
        updateData.put("finacleStatus", status);
        if (message != null && !message.isEmpty()) updateData.put("finacleErrorMessage", message);
        pendingBatchData.add(updateData);
        pendingBatchCount++;
        if (pendingBatchCount >= UPDATE_BATCH_SIZE) flushBatchUpdate();
    }

    private void flushBatchUpdate() {
        if (pendingBatchCount == 0) return;
        _log.info("Flushing " + pendingBatchCount + " records to Liferay Batch API");
        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < pendingBatchData.size(); i++) {
                Map<String, Object> rec = pendingBatchData.get(i);
                json.append("{");
                int f = 0;
                for (Map.Entry<String, Object> e : rec.entrySet()) {
                    if (f++ > 0) json.append(",");
                    String key = e.getKey();
                    if ("finacleStatusCode".equals(key)) key = "finacleStatusCode_x";
                    else if ("finacleStatus".equals(key)) key = "finacleStatus_x";
                    else if ("finacleErrorMessage".equals(key)) key = "finacleErrorMessage_x";
                    json.append("\"").append(escapeJson(key)).append("\":");
                    Object v = e.getValue();
                    if (v == null) json.append("null");
                    else if (v instanceof Number) json.append(v);
                    else json.append("\"").append(escapeJson(v.toString())).append("\"");
                }
                json.append("}");
                if (i < pendingBatchData.size() - 1) json.append(",");
            }
            json.append("]");
            String jsonBody = json.toString();

            java.net.URL url = new java.net.URL(BATCH_API_URL + "?siteId=" + SITE_ID);
            javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) url.openConnection();
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
            sc.init(null, new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            }, new java.security.SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier((hostname, session) -> true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            String auth = AUTH_USERNAME + ":" + AUTH_PASSWORD;
            conn.setRequestProperty("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes()));
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }
            int responseCode = conn.getResponseCode();
            _log.info("Batch API response code: " + responseCode);
            if (responseCode == 202 || responseCode == 200) {
                _log.info("Batch API accepted " + pendingBatchCount + " records");
                pendingBatchData.clear();
                pendingBatchCount = 0;
            } else {
                _log.warn("Batch API returned " + responseCode + ", falling back to individual updates");
                fallbackIndividualUpdates();
            }
            conn.disconnect();
        } catch (Exception e) {
            _log.error("Batch API error, falling back to individual updates", e);
            fallbackIndividualUpdates();
        }
    }

private void fallbackIndividualUpdates() {
        _log.warn("Falling back to individual updates for " + pendingBatchCount + " records");
        ServiceContext sc = new ServiceContext();
        sc.setCompanyId(companyId);
        sc.setUserId(userId);
        for (Map<String, Object> rec : pendingBatchData) {
            try {
                long id = (Long) rec.get("id");
                String code = (String) rec.get("finacleStatusCode");
                String status = (String) rec.get("finacleStatus");
                String msg = (String) rec.get("finacleErrorMessage");
                ObjectEntry entry = _objectEntryLocalService.getObjectEntry(id);
                Map<String, Serializable> vals = entry.getValues();
                vals.put("finacleStatusCode", code);
                vals.put("finacleStatus", status);
                if (msg != null) vals.put("finacleErrorMessage", msg);
                _objectEntryLocalService.updateObjectEntry(userId, id, vals, sc);
            } catch (Exception e) {
                _log.error("Individual update error", e);
            }
        }
        pendingBatchData.clear();
        pendingBatchCount = 0;
    }

    // ----------------------------------------------------------------------
    //  Finacle API (reused from your working code)
    // ----------------------------------------------------------------------
    private String callFinacleApi(String xmlRequest) throws Exception {
        java.net.URL url = new java.net.URL(FINACLE_URL);
        javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection) url.openConnection();
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
        sc.init(null, new javax.net.ssl.TrustManager[]{
            new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        }, new java.security.SecureRandom());
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("SOAPAction", "executeFinacleScript");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(xmlRequest.getBytes("UTF-8"));
        }
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"))) {
            String line; while ((line = br.readLine()) != null) response.append(line);
        }
        conn.disconnect();
        return response.toString();
    }

    private BusinessResponse parseBusinessResponse(String xml) {
        BusinessResponse resp = new BusinessResponse();
        resp.isSuccess = false;
        resp.message = "Unknown";
        if (xml == null) return resp;
        try {
            Pattern host = Pattern.compile("<HostTransaction>.*?<Status>(.*?)</Status>.*?</HostTransaction>", Pattern.DOTALL);
            Matcher m = host.matcher(xml);
            if (m.find() && "SUCCESS".equalsIgnoreCase(m.group(1))) {
                Pattern consent = Pattern.compile("<GETCONSENT>.*?<Status>(.*?)</Status>.*?</GETCONSENT>", Pattern.DOTALL);
                Matcher cm = consent.matcher(xml);
                resp.isSuccess = cm.find() ? "Success".equalsIgnoreCase(cm.group(1)) : true;
                Pattern err = Pattern.compile("<errorMessage>(.*?)</errorMessage>");
                Matcher em = err.matcher(xml);
                resp.message = em.find() ? em.group(1) : (resp.isSuccess ? "Success" : "Failed");
            }
        } catch (Exception e) {
            _log.error("Error parsing response", e);
        }
        return resp;
    }

private String buildFinacleRequest(String urn, String custId, String acct, String insType, Date modDate) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String consentDate = df.format(modDate);
        String msgDateTime = dtf.format(new Date());
        String reqUUID = "Req_" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 6);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<FIXML xmlns=\"http://www.finacle.com/fixml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.finacle.com/fixml executeFinacleScript.xsd\">\n" +
            "<Header><RequestHeader><MessageKey><RequestUUID>" + reqUUID + "</RequestUUID>\n" +
            "<ServiceRequestId>executeFinacleScript</ServiceRequestId><ServiceRequestVersion>10.2</ServiceRequestVersion>\n" +
            "<ChannelId>DMS</ChannelId><LanguageId/></MessageKey>\n" +
            "<RequestMessageInfo><MessageDateTime>" + msgDateTime + "</MessageDateTime></RequestMessageInfo>\n" +
            "<Security><Token><PasswordToken><UserId/><Password/></PasswordToken></Token></Security>\n" +
            "</RequestHeader></Header>\n" +
            "<Body><executeFinacleScriptRequest><ExecuteFinacleScriptInputVO><requestId>FI_Inbound.scr</requestId></ExecuteFinacleScriptInputVO>\n" +
            "<executeFinacleScript_CustomData><Identifier>JBYSBYCONSENT</Identifier>\n" +
            "<customerId>" + (custId != null ? custId : "") + "</customerId>\n" +
            "<debitAccount>" + (acct != null ? acct : "") + "</debitAccount>\n" +
            "<urn>" + urn + "</urn><bimaType>" + (insType != null ? insType : "JBY") + "</bimaType>\n" +
            "<consentTaken>Y</consentTaken><consentTime>" + consentDate + "</consentTime>\n" +
            "<autoRenewalConsent>Y</autoRenewalConsent><autoRenewalConsentTime>" + consentDate + "</autoRenewalConsentTime>\n" +
            "<channelId>DMS</channelId></executeFinacleScript_CustomData>\n" +
            "</executeFinacleScriptRequest></Body></FIXML>";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ----------------------------------------------------------------------
    //  Report utilities
    // ----------------------------------------------------------------------
    private void initReportFile() throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        reportFilePath = System.getProperty("java.io.tmpdir") + "/finacle_csv_consent_sync_" + timestamp + ".csv";
        Path path = Paths.get(reportFilePath);
        Files.createDirectories(path.getParent());
        reportWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        reportWriter.write("URN,Status,finacleStatusCode,finacleStatus,finacleErrorMessage");
        reportWriter.newLine();
        reportWriter.flush();
        _log.info("CSV report created: " + reportFilePath);
    }

    private void logToReport(String urn, String status, String code, String finacleStatus, String message) {
        if (reportWriter == null) return;
        try {
            reportWriter.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    escapeCsv(urn), escapeCsv(status), escapeCsv(code), escapeCsv(finacleStatus), escapeCsv(message)));
            reportWriter.newLine();
            reportWriter.flush();
        } catch (Exception e) {
            _log.error("Error writing to report", e);
        }
    }

    private void closeReportWriter() {
        try {
            if (reportWriter != null) reportWriter.close();
        } catch (Exception e) {
            _log.error("Error closing report", e);
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    private static class BusinessResponse {
        boolean isSuccess;
        String message;
    }

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
		// TODO Auto-generated method stub
		return null;
	}
}