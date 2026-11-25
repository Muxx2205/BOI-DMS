package pmsby.bulk.delete.background.task;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
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
import com.liferay.portal.kernel.util.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.FinacleConsentSyncBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)

public class FinacleConsentSyncBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(FinacleConsentSyncBackgroundTaskExecutor.class);

    private static final String FINACLE_URL = "https://dcfinxuw01.boicorp.net:32061/FISERVLET/fihttp";
    private static final int BATCH_SIZE = 5000;
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int READ_TIMEOUT = 60000;

    private final AtomicInteger totalRecordsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalSuccess = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private long totalRecordsCount = 0;

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;

    // Disable SSL for UAT
    static {
        disableSSLVerification();
    }
    
    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();

        long userId = GetterUtil.getLong(taskContextMap.get("userId"));
        long companyId = GetterUtil.getLong(taskContextMap.get("companyId"));
        long groupId = GetterUtil.getLong(taskContextMap.get("groupId"));
        String insuranceType = GetterUtil.getString(taskContextMap.get("insuranceType"), "JBY");
        long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"), 395809L);

        _log.info("==========================================================");
        _log.info("FINACLE CONSENT SYNC STARTED");
        _log.info("Endpoint: " + FINACLE_URL);
        _log.info("Insurance Type: " + insuranceType);
        _log.info("Object Definition ID: " + objectDefinitionId);
        _log.info("Group ID: " + groupId);
        _log.info("Batch Size: " + BATCH_SIZE);
        _log.info("==========================================================");
        _log.info("CONDITIONS (ALL MUST BE TRUE):");
        _log.info("  1. Stage = 3");
        _log.info("  2. FinacleStatus IS NULL OR FinacleStatusCode != 200");
        _log.info("==========================================================");

        User user = _userLocalService.getUser(userId);
        _log.info("Started by: " + user.getEmailAddress());

        // Test API connectivity first
        _log.info("Testing API connectivity...");
        boolean apiWorking = testApiConnectivity();
        if (!apiWorking) {
            _log.error("API connectivity test failed!");
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "API connectivity test failed");
        }
        _log.info("API connectivity test PASSED!");

        long startTime = System.currentTimeMillis();

        try {
            // Get all eligible records
            List<ObjectEntry> eligibleRecords = getEligibleRecords(objectDefinitionId, groupId, insuranceType);
            
            totalRecordsCount = eligibleRecords.size();
            _log.info("==========================================================");
            _log.info("Found " + totalRecordsCount + " eligible records for " + insuranceType);
            _log.info("==========================================================");
            
            if (totalRecordsCount == 0) {
                _log.info("No records to process");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }

            // Process in batches
            int batchNumber = 0;
            for (int i = 0; i < eligibleRecords.size(); i += BATCH_SIZE) {
                batchNumber++;
                int endIdx = Math.min(i + BATCH_SIZE, eligibleRecords.size());
                List<ObjectEntry> batch = eligibleRecords.subList(i, endIdx);
                
                _log.info("==========================================================");
                _log.info("BATCH " + batchNumber + ": Processing " + batch.size() + " records");
                _log.info("==========================================================");
                
                for (ObjectEntry entry : batch) {
                    processSingleRecord(entry, userId, companyId);
                }
                
                _log.info("Batch " + batchNumber + " completed. Progress: " + totalRecordsProcessed.get() + "/" + totalRecordsCount);
                
                if (i + BATCH_SIZE < eligibleRecords.size()) {
                    Thread.sleep(2000);
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            _log.info("==========================================================");
            _log.info("FINACLE CONSENT SYNC COMPLETED");
            _log.info("Insurance Type: " + insuranceType);
            _log.info("Total Processed: " + totalRecordsProcessed.get());
            _log.info("Successful: " + totalSuccess.get());
            _log.info("Failed: " + totalFailed.get());
            _log.info("Errors: " + totalErrors.get());
            _log.info("Total Time: " + (totalTime / 1000) + " seconds");
            _log.info("==========================================================");

            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

        } catch (Exception e) {
            _log.error("Fatal error", e);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, e.getMessage());
        }
    }

    private boolean testApiConnectivity() {
        try {
            String testUrn = "TEST-URN-" + System.currentTimeMillis();
            String testCustomerId = "TEST001";
            String testAccountNumber = "TEST123";
            String testInsuranceType = "JBY";
            
            String xmlRequest = buildFinacleRequest(testUrn, testCustomerId, testAccountNumber, testInsuranceType, new Date());
            
            _log.info("=== TEST REQUEST XML ===");
            _log.info(xmlRequest);
            _log.info("=======================");
            
            URL url = new URL(FINACLE_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("SOAPAction", "executeFinacleScript");
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(xmlRequest.getBytes("UTF-8"));
            }
            
            int responseCode = connection.getResponseCode();
            
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(responseCode == 200 ? connection.getInputStream() : connection.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            
            _log.info("=== TEST RESPONSE ===");
            _log.info("HTTP Status: " + responseCode);
            _log.info("Response Body: " + responseBody.toString());
            _log.info("====================");
            
            connection.disconnect();
            
            return responseCode == 200;
            
        } catch (Exception e) {
            _log.error("API test failed", e);
            return false;
        }
    }

    private List<ObjectEntry> getEligibleRecords(long objectDefinitionId, long groupId, String insuranceType) {
        List<ObjectEntry> results = new ArrayList<>();
        
        try {
            // Fetch all records for this object definition
            List<ObjectEntry> allEntries = _objectEntryLocalService.getObjectEntries(
                groupId, objectDefinitionId, 0, Integer.MAX_VALUE);
            
            _log.info("Total records in object: " + allEntries.size());
            
            if (allEntries.isEmpty()) {
                _log.warn("No records found for Object Definition ID: " + objectDefinitionId);
                return results;
            }
            
            int stage3Count = 0;
            int matchingInsuranceCount = 0;
            
            for (ObjectEntry entry : allEntries) {
                Map<String, Serializable> values = entry.getValues();
                
                int stage = GetterUtil.getInteger(values.get("stage"));
                String typeOfInsurance = GetterUtil.getString(values.get("typeOfInsurance"));
                String finacleStatus = GetterUtil.getString(values.get("finacleStatus"));
                String finacleStatusCode = GetterUtil.getString(values.get("finacleStatusCode"));
                
                if (stage == 3 && stage3Count < 5) {
                    _log.info("Sample Stage=3 record - URN: " + values.get("uRN") + 
                              ", Insurance: " + typeOfInsurance +
                              ", FinacleStatus: " + finacleStatus +
                              ", FinacleCode: " + finacleStatusCode);
                    stage3Count++;
                }
                
                if (stage == 3 && insuranceType.equals(typeOfInsurance)) {
                    matchingInsuranceCount++;
                    if (Validator.isNull(finacleStatus) || !"200".equals(finacleStatusCode)) {
                        results.add(entry);
                    }
                }
            }
            
            _log.info("Records with Stage=3: " + stage3Count);
            _log.info("Records with Stage=3 AND Insurance=" + insuranceType + ": " + matchingInsuranceCount);
            _log.info("Eligible records (need sync): " + results.size());
            
        } catch (Exception e) {
            _log.error("Error getting eligible records", e);
        }
        
        return results;
    }

    private void processSingleRecord(ObjectEntry entry, long userId, long companyId) {
        try {
            Map<String, Serializable> values = entry.getValues();
            String urn = (String) values.get("uRN");
            
            if (Validator.isNull(urn)) {
                _log.warn("Skipping - URN is null");
                totalErrors.incrementAndGet();
                totalRecordsProcessed.incrementAndGet();
                return;
            }
            
            String customerId = (String) values.get("customerId");
            String accountNumber = (String) values.get("accountNumber");
            String insuranceType = (String) values.get("typeOfInsurance");
            Date modifiedDate = entry.getModifiedDate();
            
            _log.info("==========================================================");
            _log.info("[" + (totalRecordsProcessed.get() + 1) + "/" + totalRecordsCount + "] Processing: " + urn);
            _log.info("Customer ID: " + customerId);
            _log.info("Account Number: " + accountNumber);
            _log.info("Insurance Type: " + insuranceType);
            _log.info("Modified Date: " + modifiedDate);
            
            String xmlRequest = buildFinacleRequest(urn, customerId, accountNumber, insuranceType, modifiedDate);
            
            // Log the complete request XML
            _log.info("=== REQUEST XML ===");
            _log.info(xmlRequest);
            _log.info("==================");
            
            URL url = new URL(FINACLE_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("SOAPAction", "executeFinacleScript");
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            long requestStartTime = System.currentTimeMillis();
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(xmlRequest.getBytes("UTF-8"));
            }
            
            int httpResponseCode = connection.getResponseCode();
            
            long requestEndTime = System.currentTimeMillis();
            long responseTime = requestEndTime - requestStartTime;
            
            // Read response body
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(httpResponseCode == 200 ? connection.getInputStream() : connection.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            
            String responseXml = responseBody.toString();
            
            // Log complete response
            _log.info("=== RESPONSE ===");
            _log.info("HTTP Status Code: " + httpResponseCode);
            _log.info("Response Time: " + responseTime + " ms");
            _log.info("Response XML: " + responseXml);
            _log.info("===============");
            
            connection.disconnect();
            
            // Parse response to check business status
            boolean businessSuccess = false;
            String businessMessage = "";
            
            if (responseXml.contains("<Status>SUCCESS</Status>")) {
                businessSuccess = true;
                businessMessage = "HostTransaction SUCCESS";
            }
            if (responseXml.contains("<Status>Success</Status>")) {
                businessSuccess = true;
                businessMessage = "GETCONSENT Success";
            }
            
            // Extract error message if any
            int errorStart = responseXml.indexOf("<errorMessage>");
            int errorEnd = responseXml.indexOf("</errorMessage>");
            if (errorStart > 0 && errorEnd > errorStart) {
                businessMessage = responseXml.substring(errorStart + 13, errorEnd);
            }
            
            _log.info("Business Status: " + (businessSuccess ? "SUCCESS" : "FAILED"));
            _log.info("Business Message: " + businessMessage);
            
            updateRecord(entry, httpResponseCode, userId, companyId);
            
            if (httpResponseCode == 200 && businessSuccess) {
                totalSuccess.incrementAndGet();
                _log.info("✅ SUCCESS: " + urn + " - Finacle updated successfully");
            } else {
                totalFailed.incrementAndGet();
                _log.warn("❌ FAILED: " + urn + " - HTTP: " + httpResponseCode + ", Message: " + businessMessage);
            }
            
            totalRecordsProcessed.incrementAndGet();
            _log.info("==========================================================");
            
        } catch (Exception e) {
            totalErrors.incrementAndGet();
            _log.error("Error processing record", e);
        }
    }

    private String buildFinacleRequest(String urn, String customerId, String accountNumber, String insuranceType, Date modifiedDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        
        String consentDate = dateFormat.format(modifiedDate);
        String messageDateTime = dateTimeFormat.format(new Date());
        String requestUUID = "Req_" + dateFormat.format(new Date()) + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<FIXML xmlns=\"http://www.finacle.com/fixml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.finacle.com/fixml executeFinacleScript.xsd\">\n" +
            "<Header>\n" +
            "<RequestHeader>\n" +
            "<MessageKey>\n" +
            "<RequestUUID>" + requestUUID + "</RequestUUID>\n" +
            "<ServiceRequestId>executeFinacleScript</ServiceRequestId>\n" +
            "<ServiceRequestVersion>10.2</ServiceRequestVersion>\n" +
            "<ChannelId>COR</ChannelId>\n" +
            "<LanguageId/>\n" +
            "</MessageKey>\n" +
            "<RequestMessageInfo>\n" +
            "<BankId/>\n" +
            "<TimeZone/>\n" +
            "<EntityId/>\n" +
            "<EntityType/>\n" +
            "<ArmCorrelationId/>\n" +
            "<MessageDateTime>" + messageDateTime + "</MessageDateTime>\n" +
            "</RequestMessageInfo>\n" +
            "<Security>\n" +
            "<Token>\n" +
            "<PasswordToken>\n" +
            "<UserId/>\n" +
            "<Password/>\n" +
            "</PasswordToken>\n" +
            "</Token>\n" +
            "<FICertToken/>\n" +
            "<RealUserLoginSessionId/>\n" +
            "<RealUser/>\n" +
            "<RealUserPwd/>\n" +
            "<SSOTransferToken/>\n" +
            "</Security>\n" +
            "</RequestHeader>\n" +
            "</Header>\n" +
            "<Body>\n" +
            "<executeFinacleScriptRequest>\n" +
            "<ExecuteFinacleScriptInputVO>\n" +
            "<requestId>FI_Inbound.scr</requestId>\n" +
            "</ExecuteFinacleScriptInputVO>\n" +
            "<executeFinacleScript_CustomData>\n" +
            "<Identifier>JBYSBYCONSENT</Identifier>\n" +
            "<customerId>" + (customerId != null ? customerId : "") + "</customerId>\n" +
            "<debitAccount>" + (accountNumber != null ? accountNumber : "") + "</debitAccount>\n" +
            "<urn>" + urn + "</urn>\n" +
            "<bimaType>" + (insuranceType != null ? insuranceType : "JBY") + "</bimaType>\n" +
            "<consentTaken>Y</consentTaken>\n" +
            "<consentTime>" + consentDate + "</consentTime>\n" +
            "<autoRenewalConsent>Y</autoRenewalConsent>\n" +
            "<autoRenewalConsentTime>" + consentDate + "</autoRenewalConsentTime>\n" +
            "<channelId>DMS</channelId>\n" +
            "</executeFinacleScript_CustomData>\n" +
            "</executeFinacleScriptRequest>\n" +
            "</Body>\n" +
            "</FIXML>";
    }

    private void updateRecord(ObjectEntry entry, int statusCode, long userId, long companyId) {
        try {
            Map<String, Serializable> values = entry.getValues();
            String urn = (String) values.get("uRN");
            
            int oldStatusCode = GetterUtil.getInteger(values.get("finacleStatusCode"));
            String oldStatus = GetterUtil.getString(values.get("finacleStatus"));
            
            _log.info("Updating record - URN: " + urn);
            _log.info("  Old Status: " + (Validator.isNull(oldStatus) ? "NULL" : oldStatus));
            _log.info("  Old Status Code: " + (oldStatusCode == 0 ? "NULL" : oldStatusCode));
            _log.info("  New Status Code: " + statusCode);
            _log.info("  New Status: " + (statusCode == 200 ? "SUCCESS" : "FAILED"));
            
            values.put("finacleStatusCode", statusCode);
            
            if (statusCode == 200) {
                values.put("finacleStatus", "SUCCESS");
            } else {
                values.put("finacleStatus", "FAILED");
            }
            
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setCompanyId(companyId);
            serviceContext.setUserId(userId);
            
            _objectEntryLocalService.updateObjectEntry(
                userId, entry.getObjectEntryId(), values, serviceContext);
            
            _log.info("Record updated successfully for URN: " + urn);
            
        } catch (Exception e) {
            _log.error("Error updating record for URN: " + getUrn(entry), e);
        }
    }

    private String getUrn(ObjectEntry entry) {
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
}