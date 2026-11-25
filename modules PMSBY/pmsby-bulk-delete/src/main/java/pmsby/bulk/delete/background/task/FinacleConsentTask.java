package pmsby.bulk.delete.background.task;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.InfrastructureUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    configurationPid = "pmsby.bulk.delete.background.task.FinacleConfiguration",
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.FinacleConsentTask"
    },
    service = BackgroundTaskExecutor.class
)
public class FinacleConsentTask extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(FinacleConsentTask.class);

    // Configuration fields
    private volatile int recordsPerThread = 2000;
    private volatile int parallelThreads = 4;
    private volatile Set<Integer> stages = new HashSet<>(Arrays.asList(2, 3));
    private volatile int eveningStartHour = 12;
    private volatile int eveningStartMinute = 30;
    private volatile int eveningEndHour = 17;
    private volatile int eveningEndMinute = 30;
    private volatile int morningStartHour = 23;
    private volatile int morningStartMinute = 30;
    private volatile int morningEndHour = 3;
    private volatile int morningEndMinute = 30;
    private volatile int connectionTimeout = 60000;
    private volatile int readTimeout = 60000;
    private volatile int maxConsecutiveErrors = 500;
    private volatile String finacleURL = "https://psms.starfina.boicorp.net:35500/FISERVLET/fihttp";
    private volatile String mainTable = "O_46102212172422_PMSBYPMJJYCONSENTFORM";
    private volatile String extensionTable = "O_46102212172422_PMSBYPMJJYCONSENTFORM_X";

    // Statistics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalSuccess = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicInteger consecutiveErrors = new AtomicInteger(0);

    private final AtomicInteger jbySuccess = new AtomicInteger(0);
    private final AtomicInteger jbyFailed = new AtomicInteger(0);
    private final AtomicInteger sbySuccess = new AtomicInteger(0);
    private final AtomicInteger sbyFailed = new AtomicInteger(0);

    private long companyId = 0;
    private long userId = 0;

    // CSV report
    private BufferedWriter reportWriter = null;
    private String reportFilePath = null;

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;

    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        FinacleConfiguration config = ConfigurableUtil.createConfigurable(FinacleConfiguration.class, properties);
        recordsPerThread = config.batchSize();
        parallelThreads = config.parallelThreads();
        
        // Parse stages
        stages.clear();
        String stagesStr = config.stages();
        if (stagesStr != null && !stagesStr.isEmpty()) {
            for (String s : stagesStr.split(",")) {
                try {
                    stages.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    _log.error("Invalid stage value: " + s);
                }
            }
        }
        if (stages.isEmpty()) {
            stages.add(2);
            stages.add(3);
        }
        
        eveningStartHour = config.eveningStartHour();
        eveningStartMinute = config.eveningStartMinute();
        eveningEndHour = config.eveningEndHour();
        eveningEndMinute = config.eveningEndMinute();
        morningStartHour = config.morningStartHour();
        morningStartMinute = config.morningStartMinute();
        morningEndHour = config.morningEndHour();
        morningEndMinute = config.morningEndMinute();
        connectionTimeout = config.connectiontimeout();
        readTimeout = config.readTimeout();
        maxConsecutiveErrors = config.maxErrors();
        finacleURL = config.finacleURL();
        mainTable = config.mainTable();
        extensionTable = config.extensionTable();

        _log.info("==========================================================");
        _log.info("FinacleConsentTask configuration updated:");
        _log.info("  recordsPerThread: " + recordsPerThread);
        _log.info("  parallelThreads: " + parallelThreads);
        _log.info("  stages: " + stages);
        _log.info("  TOTAL records per run: " + (recordsPerThread * parallelThreads));
        _log.info("  eveningWindow: " + eveningStartHour + ":" + eveningStartMinute + "-" + eveningEndHour + ":" + eveningEndMinute);
        _log.info("  morningWindow: " + morningStartHour + ":" + morningStartMinute + "-" + morningEndHour + ":" + morningEndMinute);
        _log.info("==========================================================");
    }

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContext = backgroundTask.getTaskContextMap();
        companyId = GetterUtil.getLong(taskContext.get("companyId"));
        userId = GetterUtil.getLong(taskContext.get("userId"));

        int totalRecordsToProcess = recordsPerThread * parallelThreads;

        _log.info("==========================================================");
        _log.info("FINACLE CONSENT SYNC STARTED");
        _log.info("Stages to process: " + stages);
        _log.info("Current UTC time: " + getCurrentUTCTime());
        _log.info("Records per thread: " + recordsPerThread);
        _log.info("Parallel threads: " + parallelThreads);
        _log.info("TOTAL records to process: " + totalRecordsToProcess);
        _log.info("==========================================================");

        // Time window check
        if (!isWithinTimeWindow()) {
            _log.info("Outside allowed time window – exiting immediately.");
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
        }
        _log.info("Within allowed time window - processing will continue");

        User user = _userLocalService.getUser(userId);
        _log.info("Started by: " + user.getEmailAddress());

        initReportFile();
        _log.info("CSV report file: " + reportFilePath);

        // Test Finacle connectivity
        if (!testFinacleConnectivity()) {
            _log.error("Finacle API unreachable – aborting task.");
            closeReportWriter();
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "Finacle API unreachable");
        }

        // Get total eligible count
        long totalEligible = getTotalEligibleCount();
        _log.info("Total eligible records in database (stages " + stages + "): " + totalEligible);
        
        if (totalEligible == 0) {
            _log.info("No eligible records to process.");
            closeReportWriter();
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
        }

        // Create thread pool and process
        ExecutorService executorService = Executors.newFixedThreadPool(parallelThreads);
        List<RecordProcessor> processors = new ArrayList<>();
        
        for (int threadId = 0; threadId < parallelThreads; threadId++) {
            int offset = threadId * recordsPerThread;
            
            if (offset >= totalEligible) {
                _log.info("Thread " + threadId + " skipped - offset " + offset + " exceeds total eligible records " + totalEligible);
                break;
            }
            
            int limit = recordsPerThread;
            if (offset + limit > totalEligible) {
                limit = (int)(totalEligible - offset);
            }
            
            _log.info("Thread " + threadId + " will fetch " + limit + " records starting from offset " + offset);
            
            List<ObjectEntry> threadRecords = fetchEligibleRecordsBatch(offset, limit);
            
            if (!threadRecords.isEmpty()) {
                RecordProcessor processor = new RecordProcessor(threadRecords, threadId);
                processors.add(processor);
                processor.start();
             
            }
        }
        
        // Shutdown and wait for completion
        executorService.shutdown();
        try {
            boolean finished = executorService.awaitTermination(30, TimeUnit.MINUTES);
            if (!finished) {
                _log.warn("Timeout waiting for threads to complete, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            _log.error("Thread pool interrupted", e);
            executorService.shutdownNow();
        }
        
             closeReportWriter();
        return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
    }

    // Build stage condition for SQL
    private String buildStageCondition() {
        if (stages.size() == 1) {
            return "t2.STAGE_ = " + stages.iterator().next();
        }
        StringBuilder sb = new StringBuilder("t2.STAGE_ IN (");
        boolean first = true;
        for (int stage : stages) {
            if (!first) sb.append(", ");
            sb.append(stage);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    // Record Processor for parallel execution
    private class RecordProcessor extends Thread {
        private final List<ObjectEntry> records;
        private final int threadId;
        private int successCount = 0;
        private int failedCount = 0;
        private int errorCount = 0;
        private int jbyS = 0;
        private int jbyF = 0;
        private int sbyS = 0;
        private int sbyF = 0;
        
        public RecordProcessor(List<ObjectEntry> records, int threadId) {
            this.records = records;
            this.threadId = threadId;
        }
        
        @Override
        public void run() {
            String threadName = "Thread-" + threadId;
            _log.info(threadName + " starting to process " + records.size() + " records");
            
            for (int i = 0; i < records.size(); i++) {
                ObjectEntry entry = records.get(i);
                try {
                    Map<String, Serializable> values = entry.getValues();
                    String urn = (String) values.get("uRN");
                    String customerId = (String) values.get("customerId");
                    
                    if (Validator.isNull(urn)) {
                        _log.warn(threadName + " - Skipping record with null URN");
                        errorCount++;
                        logToReportSafe("Unknown", "ERROR", "URN is null");
                        continue;
                    }

                    String accountNumber = (String) values.get("accountNumber");
                    String insuranceType = GetterUtil.getString(values.get("typeOfInsurance"));
                    Date modifiedDate = entry.getModifiedDate();

                    if (i % 100 == 0 && i > 0) {
                        _log.info(threadName + " - Progress: " + i + "/" + records.size() + " records processed");
                    }

                    String xmlRequest = buildFinacleRequest(urn, customerId, accountNumber, insuranceType, modifiedDate);
                    String responseXml = callFinacleApi(xmlRequest, urn);

                    if (responseXml != null) {
                        BusinessResponse businessResponse = parseBusinessResponse(responseXml);
                        if (businessResponse.isSuccess) {
                            updateRecord(entry, 200, "SUCCESS", businessResponse.message);
                            successCount++;
                            if ("JBY".equals(insuranceType)) jbyS++;
                            else if ("SBY".equals(insuranceType)) sbyS++;
                            _log.debug(threadName + " - SUCCESS: " + urn);
                            logToReportSafe(urn, "SUCCESS", businessResponse.message);
                        } else {
                            updateRecord(entry, 500, "FAILED", businessResponse.message);
                            failedCount++;
                            if ("JBY".equals(insuranceType)) jbyF++;
                            else if ("SBY".equals(insuranceType)) sbyF++;
                            _log.warn(threadName + " - FAILED: " + urn + " - " + businessResponse.message);
                            logToReportSafe(urn, "FAILED", businessResponse.message);
                        }
                    } else {
                        updateRecord(entry, 500, "FAILED", "Network error – no response");
                        errorCount++;
                        if ("JBY".equals(insuranceType)) jbyF++;
                        else if ("SBY".equals(insuranceType)) sbyF++;
                        _log.error(threadName + " - Network error for URN: " + urn);
                        logToReportSafe(urn, "ERROR", "Network error – no response");
                    }
                } catch (Exception e) {
                    _log.error("Error processing record", e);
                    errorCount++;
                    logToReportSafe(getUrnFromEntry(entry), "ERROR", "Exception: " + e.getMessage());
                }
            }
            
            _log.info(threadName + " completed - Success: " + successCount + 
                      ", Failed: " + failedCount + ", Errors: " + errorCount);
        }
        // Database update
        private void updateRecord(ObjectEntry entry, int statusCode, String status, String errorMessage) {
            try {
                Map<String, Serializable> values = entry.getValues();
                String urn = (String) values.get("uRN");
                values.put("finacleStatusCode", statusCode);
                values.put("finacleStatus", status);
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    values.put("finacleErrorMessage", errorMessage);
                }
                ServiceContext serviceContext = new ServiceContext();
                serviceContext.setCompanyId(companyId);
                serviceContext.setUserId(userId);
                _objectEntryLocalService.updateObjectEntry(userId, entry.getObjectEntryId(), values, serviceContext);
                _log.debug("Updated record for URN: " + urn);
            } catch (Exception e) {
                _log.error("Failed to update record for URN: " + getUrnFromEntry(entry), e);
            }
        }
        

        private String callFinacleApi(String xmlRequest, String urn) throws Exception {
            URL url = new URL(finacleURL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            }, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("SOAPAction", "executeFinacleScript");
            connection.setDoOutput(true);
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(xmlRequest.getBytes("UTF-8"));
            }

            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? connection.getInputStream() : connection.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }
            connection.disconnect();
            return response.toString();
        }

        private BusinessResponse parseBusinessResponse(String xml) {
            BusinessResponse resp = new BusinessResponse();
            resp.isSuccess = false;
            resp.message = "Unknown";
            if (xml == null) return resp;
            try {
                Pattern hostPattern = Pattern.compile("<HostTransaction>.*?<Status>(.*?)</Status>.*?</HostTransaction>", Pattern.DOTALL);
                Matcher m = hostPattern.matcher(xml);
                if (m.find() && "SUCCESS".equalsIgnoreCase(m.group(1))) {
                    Pattern consentPattern = Pattern.compile("<GETCONSENT>.*?<Status>(.*?)</Status>.*?</GETCONSENT>", Pattern.DOTALL);
                    Matcher cm = consentPattern.matcher(xml);
                    resp.isSuccess = cm.find() ? "Success".equalsIgnoreCase(cm.group(1)) : true;
                    Pattern errPattern = Pattern.compile("<errorMessage>(.*?)</errorMessage>");
                    Matcher em = errPattern.matcher(xml);
                    resp.message = em.find() ? em.group(1) : (resp.isSuccess ? "Success" : "Failed");
                }
            } catch (Exception e) {
                _log.error("Error parsing business response", e);
            }
            return resp;
        }

        private String buildFinacleRequest(String urn, String custId, String acct, String insType, Date modDate) {
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String consentDate = df.format(modDate);
            String msgDateTime = dtf.format(new Date());
            String reqUUID = "Req_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
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

        private String getUrnFromEntry(ObjectEntry entry) {
            try {
                return (String) entry.getValues().get("uRN");
            } catch (Exception e) {
                return "Unknown";
            }
        }


        public void aggregateStatistics() {
            totalSuccess.addAndGet(successCount);
            totalFailed.addAndGet(failedCount);
            totalErrors.addAndGet(errorCount);
            totalProcessed.addAndGet(successCount + failedCount + errorCount);
            jbySuccess.addAndGet(jbyS);
            jbyFailed.addAndGet(jbyF);
            sbySuccess.addAndGet(sbyS);
            sbyFailed.addAndGet(sbyF);
            
            if (successCount > 0 || failedCount > 0) {
                consecutiveErrors.set(0);
            } else if (errorCount > 0) {
                consecutiveErrors.incrementAndGet();
            }
        }
    }

    // Thread-safe CSV logging
    private synchronized void logToReportSafe(String urn, String status, String message) {
        logToReport(urn, status, message);
    }

    // Time window logic
    private boolean isWithinTimeWindow() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        int eveningStart = eveningStartHour * 60 + eveningStartMinute;
        int eveningEnd = eveningEndHour * 60 + eveningEndMinute;
        boolean inEvening = currentMinutes >= eveningStart && currentMinutes < eveningEnd;

        int morningStart = morningStartHour * 60 + morningStartMinute;
        int morningEnd = morningEndHour * 60 + morningEndMinute;
        boolean inMorning;
        if (morningStart < morningEnd) {
            inMorning = currentMinutes >= morningStart && currentMinutes < morningEnd;
        } else {
            inMorning = currentMinutes >= morningStart || currentMinutes < morningEnd;
        }

        return inEvening || inMorning;
    }

    private String getCurrentUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // CSV report helpers
    private void initReportFile() throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        reportFilePath = System.getProperty("java.io.tmpdir") + "/finacle_consent_sync_" + timestamp + ".csv";
        Path path = Paths.get(reportFilePath);
        Files.createDirectories(path.getParent());
        reportWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        reportWriter.write("URN,Status,Message");
        reportWriter.newLine();
        reportWriter.flush();
    }

    private void logToReport(String urn, String status, String message) {
        if (reportWriter == null) return;
        try {
            synchronized (reportWriter) {
                reportWriter.write(String.format("\"%s\",\"%s\",\"%s\"",
                        escapeCsv(urn), escapeCsv(status), escapeCsv(message)));
                reportWriter.newLine();
                reportWriter.flush();
            }
        } catch (Exception e) {
            _log.error("Error writing to CSV report", e);
        }
    }

    private void closeReportWriter() {
        try {
            if (reportWriter != null) {
                reportWriter.close();
                _log.info("CSV report saved: " + reportFilePath);
            }
        } catch (Exception e) {
            _log.error("Error closing CSV report", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    // Database count and fetch
    private long getTotalEligibleCount() {
        long count = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            DataSource ds = InfrastructureUtil.getDataSource();
            conn = ds.getConnection();
            String stageCondition = buildStageCondition();
            String sql = "SELECT COUNT(t1.c_pmsbypmjjyconsentformid_) FROM " + mainTable + " t1 " +
                         "INNER JOIN " + extensionTable + " t2 " +
                         "ON t1.c_pmsbypmjjyconsentformid_ = t2.c_pmsbypmjjyconsentformid_ " +
                         "WHERE " + stageCondition + " AND (t2.FINACLESTATUS_ IS NULL OR t2.FINACLESTATUSCODE_ IS NULL)";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (Exception e) {
            _log.error("Error counting eligible records", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return count;
    }

    private List<ObjectEntry> fetchEligibleRecordsBatch(int offset, int limit) {
        List<ObjectEntry> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            DataSource ds = InfrastructureUtil.getDataSource();
            conn = ds.getConnection();
            String stageCondition = buildStageCondition();
            String sql = "SELECT t1.c_pmsbypmjjyconsentformid_ FROM " + mainTable + " t1 " +
                         "INNER JOIN " + extensionTable + " t2 " +
                         "ON t1.c_pmsbypmjjyconsentformid_ = t2.c_pmsbypmjjyconsentformid_ " +
                         "WHERE " + stageCondition + " AND (t2.FINACLESTATUS_ IS NULL OR t2.FINACLESTATUSCODE_ IS NULL) " +
                         "ORDER BY t1.c_pmsbypmjjyconsentformid_ ASC " +
                         "OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            List<Long> entryIds = new ArrayList<>();
            while (rs.next()) {
                entryIds.add(rs.getLong(1));
            }
            for (Long id : entryIds) {
                ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(id);
                if (entry != null) results.add(entry);
            }
            _log.debug("Fetched " + results.size() + " records at offset " + offset);
        } catch (Exception e) {
            _log.error("Error fetching eligible records batch at offset " + offset, e);
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return results;
    }

    // Database update
    private void updateRecord2(ObjectEntry entry, int statusCode, String status, String errorMessage) {
        try {
            Map<String, Serializable> values = entry.getValues();
            String urn = (String) values.get("uRN");
            values.put("finacleStatusCode", statusCode);
            values.put("finacleStatus", status);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                values.put("finacleErrorMessage", errorMessage);
            }
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setCompanyId(companyId);
            serviceContext.setUserId(userId);
            _objectEntryLocalService.updateObjectEntry(userId, entry.getObjectEntryId(), values, serviceContext);
            _log.debug("Updated record for URN: " + urn);
        } catch (Exception e) {
            _log.error("Failed to update record for URN: ", e);
        }
    }

    // Finacle API
    private boolean testFinacleConnectivity() {
        try {
            String xmlRequest = buildFinacleRequest2("TEST-URN", "TEST001", "TEST123", "JBY", new Date());
            String response = callFinacleApi2(xmlRequest, "TEST");
            return response != null;
        } catch (Exception e) {
            _log.error("Connectivity test failed", e);
            return false;
        }
    }
    

    private String callFinacleApi2(String xmlRequest, String urn) throws Exception {
        URL url = new URL(finacleURL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        }, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        connection.setRequestProperty("SOAPAction", "executeFinacleScript");
        connection.setDoOutput(true);
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(xmlRequest.getBytes("UTF-8"));
        }

        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) response.append(line);
        }
        connection.disconnect();
        return response.toString();
    }

    private BusinessResponse parseBusinessResponse2(String xml) {
        BusinessResponse resp = new BusinessResponse();
        resp.isSuccess = false;
        resp.message = "Unknown";
        if (xml == null) return resp;
        try {
            Pattern hostPattern = Pattern.compile("<HostTransaction>.*?<Status>(.*?)</Status>.*?</HostTransaction>", Pattern.DOTALL);
            Matcher m = hostPattern.matcher(xml);
            if (m.find() && "SUCCESS".equalsIgnoreCase(m.group(1))) {
                Pattern consentPattern = Pattern.compile("<GETCONSENT>.*?<Status>(.*?)</Status>.*?</GETCONSENT>", Pattern.DOTALL);
                Matcher cm = consentPattern.matcher(xml);
                resp.isSuccess = cm.find() ? "Success".equalsIgnoreCase(cm.group(1)) : true;
                Pattern errPattern = Pattern.compile("<errorMessage>(.*?)</errorMessage>");
                Matcher em = errPattern.matcher(xml);
                resp.message = em.find() ? em.group(1) : (resp.isSuccess ? "Success" : "Failed");
            }
        } catch (Exception e) {
            _log.error("Error parsing business response", e);
        }
        return resp;
    }

    private String buildFinacleRequest2(String urn, String custId, String acct, String insType, Date modDate) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String consentDate = df.format(modDate);
        String msgDateTime = dtf.format(new Date());
        String reqUUID = "Req_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
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

    private String getUrnFromEntry(ObjectEntry entry) {
        try {
            return (String) entry.getValues().get("uRN");
        } catch (Exception e) {
            return "Unknown";
        }
    }


    @Override
    public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
        return null;
    }

    @Override
    public int getIsolationLevel() {
        return BackgroundTaskConstants.ISOLATION_LEVEL_NOT_ISOLATED;
    }

    private static class BusinessResponse {
        boolean isSuccess;
        String message;
    }
}