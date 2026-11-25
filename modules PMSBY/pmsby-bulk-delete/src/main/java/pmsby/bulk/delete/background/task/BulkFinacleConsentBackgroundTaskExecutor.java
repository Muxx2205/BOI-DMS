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
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.TermQueryImpl;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import pmsby.bulk.delete.service.FinacleConsentService;
import pmsby.bulk.delete.service.FinacleConsentService.FinacleResponse;
import pmsby.bulk.delete.util.CSVReaderUtil;

/**
 * Background task for bulk sending consent to Finacle API
 * Processes approved records (stage=3) from CSV or from database
 */
@Component(
    immediate = true,
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.BulkFinacleConsentBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)
public class BulkFinacleConsentBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(BulkFinacleConsentBackgroundTaskExecutor.class);

    // Performance configuration
    private static final int PARALLEL_THREADS = 10;
    private static final int BATCH_SIZE = 100;
    private static final int LOG_INTERVAL = 1000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 5000;

    // Stage value for approved records
    private static final int APPROVED_STAGE = 3;

    private ExecutorService executorService;

    // Statistics
    private final AtomicInteger totalRecordsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalSuccess = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicInteger totalRetried = new AtomicInteger(0);
    private final AtomicLong totalTimeSpent = new AtomicLong(0);
    private int totalRecordsCount = 0;

    // Storage for results
    private final ConcurrentLinkedQueue<ConsentResult> results = new ConcurrentLinkedQueue<>();

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;

    @Reference
    private FinacleConsentService _finacleConsentService;
    
    @Reference
    private CSVReaderUtil _csvReaderUtil;

    @Override
    public BackgroundTaskExecutor clone() {
        BulkFinacleConsentBackgroundTaskExecutor executor = new BulkFinacleConsentBackgroundTaskExecutor();
        executor._objectEntryLocalService = this._objectEntryLocalService;
        executor._userLocalService = this._userLocalService;
        executor._finacleConsentService = this._finacleConsentService;
        executor._csvReaderUtil = this._csvReaderUtil;
        return executor;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();

        String csvFilePath = GetterUtil.getString(taskContextMap.get("csvFilePath"));
        String fileName = GetterUtil.getString(taskContextMap.get("fileName"));
        long userId = GetterUtil.getLong(taskContextMap.get("userId"));
        long companyId = GetterUtil.getLong(taskContextMap.get("companyId"));
        String mode = GetterUtil.getString(taskContextMap.get("mode"), "APPROVED_ONLY");
        
        // Column names for CSV mode
        String urnColumnName = GetterUtil.getString(taskContextMap.get("urnColumnName"), "URN");
        String customerIdColumnName = GetterUtil.getString(taskContextMap.get("customerIdColumnName"), "customerId");
        String debitAccountColumnName = GetterUtil.getString(taskContextMap.get("debitAccountColumnName"), "debitAccount");
        String bimaTypeColumnName = GetterUtil.getString(taskContextMap.get("bimaTypeColumnName"), "bimaType");
        String delimiter = GetterUtil.getString(taskContextMap.get("delimiter"), ",");

        _log.info("==========================================================");
        _log.info("BULK FINACLE CONSENT SYNC STARTED");
        _log.info("==========================================================");
        _log.info("Mode: " + mode);
        _log.info("Company ID: " + companyId);
        _log.info("User ID: " + userId);
        _log.info("Approved Stage Value: " + APPROVED_STAGE);

        User user = _userLocalService.getUser(userId);
        _log.info("Started by: " + user.getEmailAddress());

        long startTime = System.currentTimeMillis();
        executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

        try {
            List<ConsentRecord> recordsToProcess = new ArrayList<>();

            if ("CSV".equalsIgnoreCase(mode) && csvFilePath != null) {
                // Mode 1: Read from CSV file using CSVReaderUtil
                File csvFile = new File(csvFilePath);
                if (!csvFile.exists()) {
                    _log.error("CSV file not found: " + csvFilePath);
                    return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "CSV file not found");
                }
                _log.info("Reading records from CSV file: " + fileName);
                recordsToProcess = readRecordsFromCSVUsingUtil(csvFile, urnColumnName, 
                    customerIdColumnName, debitAccountColumnName, bimaTypeColumnName, delimiter);
            } else {
                // Mode 2: Fetch approved records from database (stage = 3)
                _log.info("Fetching approved records (stage=" + APPROVED_STAGE + ") from database...");
                recordsToProcess = fetchApprovedRecordsFromDB(companyId, userId);
            }

            totalRecordsCount = recordsToProcess.size();
            _log.info("Total records to process: " + totalRecordsCount);

            if (recordsToProcess.isEmpty()) {
                _log.warn("No records found to process!");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }

            // Display sample records
            _log.info("Sample records (first 10):");
            for (int i = 0; i < Math.min(recordsToProcess.size(), 10); i++) {
                ConsentRecord record = recordsToProcess.get(i);
                _log.info("  " + (i + 1) + ". URN: '" + record.urn + 
                         "', Customer ID: '" + record.customerId + 
                         "', Debit Account: '" + record.debitAccount + "'");
            }

            // Process records in batches
            _log.info("Processing records with " + PARALLEL_THREADS + " parallel threads...");
            processRecordsInBatches(recordsToProcess, companyId, userId);

            long totalTime = System.currentTimeMillis() - startTime;

            // Final summary
            _log.info("==========================================================");
            _log.info("BULK FINACLE CONSENT SYNC COMPLETED");
            _log.info("==========================================================");
            _log.info("Total records: " + totalRecordsProcessed.get());
            _log.info("Success: " + totalSuccess.get());
            _log.info("Failed: " + totalFailed.get());
            _log.info("Errors: " + totalErrors.get());
            _log.info("Retries: " + totalRetried.get());
            _log.info(String.format("Total time: %d min %d sec", totalTime / 60000, (totalTime % 60000) / 1000));

            // Generate report
            generateReport(csvFilePath != null ? new File(csvFilePath).getParent() : System.getProperty("java.io.tmpdir"), 
                          fileName != null ? fileName : "finacle_consent_report");

            if (csvFilePath != null) {
                new File(csvFilePath).delete();
            }

            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

        } catch (Exception e) {
            _log.error("FATAL ERROR during bulk Finacle consent sync", e);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, e.getMessage());
        } finally {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    executorService.awaitTermination(10, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Read consent records from CSV file using CSVReaderUtil
     */
    private List<ConsentRecord> readRecordsFromCSVUsingUtil(File csvFile, String urnColumnName,
            String customerIdColumnName, String debitAccountColumnName, 
            String bimaTypeColumnName, String delimiter) throws Exception {

        List<ConsentRecord> records = new ArrayList<>();
        Map<String, ConsentRecord> uniqueRecords = new HashMap<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            int urnColumnIndex = -1;
            int customerIdColumnIndex = -1;
            int debitAccountColumnIndex = -1;
            int bimaTypeColumnIndex = -1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Handle BOM for UTF-8 files
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                
                String[] columns = parseCSVLine(line, delimiter);

                if (isFirstLine) {
                    // Parse headers
                    for (int i = 0; i < columns.length; i++) {
                        String header = columns[i].trim();
                        if (urnColumnName.equalsIgnoreCase(header)) {
                            urnColumnIndex = i;
                        } else if (customerIdColumnName.equalsIgnoreCase(header)) {
                            customerIdColumnIndex = i;
                        } else if (debitAccountColumnName.equalsIgnoreCase(header)) {
                            debitAccountColumnIndex = i;
                        } else if (bimaTypeColumnName.equalsIgnoreCase(header)) {
                            bimaTypeColumnIndex = i;
                        }
                    }

                    // Validate required columns
                    if (urnColumnIndex == -1) {
                        throw new Exception("URN column '" + urnColumnName + "' not found");
                    }
                    if (customerIdColumnIndex == -1) {
                        throw new Exception("Customer ID column '" + customerIdColumnName + "' not found");
                    }
                    if (debitAccountColumnIndex == -1) {
                        throw new Exception("Debit Account column '" + debitAccountColumnName + "' not found");
                    }

                    isFirstLine = false;
                    continue;
                }

                if (columns.length > Math.max(Math.max(urnColumnIndex, customerIdColumnIndex), 
                                               Math.max(debitAccountColumnIndex, bimaTypeColumnIndex))) {
                    String urn = columns[urnColumnIndex].trim();
                    String customerId = columns[customerIdColumnIndex].trim();
                    String debitAccount = columns[debitAccountColumnIndex].trim();
                    String bimaType = (bimaTypeColumnIndex != -1 && columns.length > bimaTypeColumnIndex) 
                                      ? columns[bimaTypeColumnIndex].trim() : "JBY";

                    // Remove quotes if present
                    urn = removeQuotes(urn);
                    customerId = removeQuotes(customerId);
                    debitAccount = removeQuotes(debitAccount);
                    bimaType = removeQuotes(bimaType);

                    if (Validator.isNotNull(urn) && Validator.isNotNull(customerId) && Validator.isNotNull(debitAccount)) {
                        uniqueRecords.putIfAbsent(urn, new ConsentRecord(urn, customerId, debitAccount, bimaType));
                    }
                }
            }
        }

        records.addAll(uniqueRecords.values());
        _log.info("Read " + records.size() + " unique records from " + lineNumber + " lines");
        return records;
    }
    
    /**
     * Parse a CSV line handling quotes properly
     */
    private String[] parseCSVLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        String delim = delimiter;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delim.charAt(0) && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        
        result.add(field.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Remove quotes from string if present
     */
    private String removeQuotes(String value) {
        if (value == null) return null;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Fetch approved records from database using pagination
     */
    private List<ConsentRecord> fetchApprovedRecordsFromDB(long companyId, long userId) throws Exception {
        List<ConsentRecord> records = new ArrayList<>();
        
        try {
            int start = 0;
            int end = 1000;
            boolean hasMore = true;
            
            _log.info("Fetching object entries in batches of 1000...");
            
            while (hasMore) {
                // Fetch object entries (you may need to filter by object definition ID)
                List<ObjectEntry> entries = _objectEntryLocalService.getObjectEntries(start, end);
                
                if (entries == null || entries.isEmpty()) {
                    hasMore = false;
                    break;
                }
                
                _log.info("Processing batch from " + start + " to " + (start + entries.size()));
                
                for (ObjectEntry entry : entries) {
                    Map<String, Serializable> values = entry.getValues();
                    
                    // Check stage = 3 (approved)
                    int stage = GetterUtil.getInteger(values.get("stage"));
                    
                    // Check if already sent successfully
                    String finacleStatus = GetterUtil.getString(values.get("finacleStatus"));
                    
                    if (stage == APPROVED_STAGE && !"SUCCESS".equalsIgnoreCase(finacleStatus)) {
                        String urn = GetterUtil.getString(values.get("urn"));
                        String customerId = GetterUtil.getString(values.get("customerId"));
                        String debitAccount = GetterUtil.getString(values.get("debitAccount"));
                        String bimaType = GetterUtil.getString(values.get("bimaType"), "JBY");
                        
                        if (Validator.isNotNull(urn) && Validator.isNotNull(customerId) && Validator.isNotNull(debitAccount)) {
                            ConsentRecord record = new ConsentRecord(urn, customerId, debitAccount, bimaType);
                            record.objectEntryId = entry.getObjectEntryId();
                            record.existingValues = values;
                            records.add(record);
                        }
                    }
                }
                
                // Move to next batch
                start = start + end;
                
                // If we got less than end, we've reached the end
                if (entries.size() < end) {
                    hasMore = false;
                }
                
                // Log progress
                if (records.size() % 10000 == 0) {
                    _log.info("Found " + records.size() + " approved records so far...");
                }
            }
            
        } catch (Exception e) {
            _log.error("Error fetching approved records from database", e);
            throw e;
        }
        
        _log.info("Fetched " + records.size() + " pending approved records from database");
        return records;
    }

    /**
     * Process records in batches
     */
    private void processRecordsInBatches(List<ConsentRecord> records, long companyId, long userId) {
        int totalBatches = (int) Math.ceil(records.size() / (double) BATCH_SIZE);
        _log.info("Total batches: " + totalBatches + " (batch size: " + BATCH_SIZE + ")");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < records.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, records.size());
            List<ConsentRecord> batch = records.subList(i, end);
            final int batchNumber = (i / BATCH_SIZE) + 1;

            futures.add(CompletableFuture.runAsync(() -> 
                processBatch(batch, batchNumber, totalBatches, companyId, userId), executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Process a single batch
     */
    private void processBatch(List<ConsentRecord> batch, int batchNumber, int totalBatches, 
                              long companyId, long userId) {

        _log.info(String.format("Batch %d/%d started with %d records", batchNumber, totalBatches, batch.size()));

        batch.parallelStream().forEach(record -> {
            processSingleRecord(record, companyId, userId);
            totalRecordsProcessed.incrementAndGet();
            
            if (totalRecordsProcessed.get() % LOG_INTERVAL == 0) {
                _log.info(String.format("Progress: %d/%d records processed (Success: %d, Failed: %d, Errors: %d)",
                    totalRecordsProcessed.get(), totalRecordsCount, totalSuccess.get(), 
                    totalFailed.get(), totalErrors.get()));
            }
        });
    }

    /**
     * Process single record with retry logic
     */
    private void processSingleRecord(ConsentRecord record, long companyId, long userId) {
        int retryCount = 0;
        boolean success = false;
        FinacleResponse finacleResponse = null;
        
        while (!success && retryCount < MAX_RETRY_COUNT) {
            try {
                finacleResponse = _finacleConsentService.sendConsent(
                    record.urn, record.customerId, record.debitAccount, 
                    record.bimaType, "Y" // consentTaken = Y
                );
                
                if (finacleResponse.isSuccess()) {
                    success = true;
                    totalSuccess.incrementAndGet();
                    _log.info("SUCCESS: Consent sent for URN: " + record.urn + 
                             " - Response: " + finacleResponse.getMessage());
                } else {
                    totalFailed.incrementAndGet();
                    _log.warn("FAILED: Consent for URN: " + record.urn + 
                             " - Status: " + finacleResponse.getStatus() + 
                             " - Message: " + finacleResponse.getMessage());
                }
                
                // Update object entry with response
                updateObjectEntry(record, finacleResponse, retryCount, userId, companyId, success);
                
            } catch (Exception e) {
                _log.error("Error processing URN: " + record.urn + " (Retry: " + (retryCount + 1) + ")", e);
                
                if (retryCount >= MAX_RETRY_COUNT - 1) {
                    totalErrors.incrementAndGet();
                    // Create error response
                    finacleResponse = new FinacleResponse();
                    finacleResponse.setSuccess(false);
                    finacleResponse.setStatus("ERROR");
                    finacleResponse.setMessage("Exception after " + MAX_RETRY_COUNT + " retries: " + e.getMessage());
                    
                    try {
                        updateObjectEntry(record, finacleResponse, retryCount, userId, companyId, false);
                    } catch (Exception updateEx) {
                        _log.error("Failed to update object entry for URN: " + record.urn, updateEx);
                    }
                }
            }
            
            if (!success && retryCount < MAX_RETRY_COUNT - 1) {
                totalRetried.incrementAndGet();
                try {
                    _log.info("Retrying URN: " + record.urn + " in " + RETRY_DELAY_MS + "ms (Attempt " + 
                             (retryCount + 2) + "/" + MAX_RETRY_COUNT + ")");
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            retryCount++;
        }
        
        // Store result for report
        ConsentResult result = new ConsentResult();
        result.urn = record.urn;
        result.customerId = record.customerId;
        result.status = success ? "SUCCESS" : "FAILED";
        result.message = finacleResponse != null ? finacleResponse.getMessage() : "Unknown error";
        result.retryCount = retryCount;
        results.add(result);
    }

    /**
     * Update object entry with Finacle response
     */
    private void updateObjectEntry(ConsentRecord record, FinacleResponse response, 
                                   int retryCount, long userId, long companyId, boolean success) 
            throws Exception {
        
        ObjectEntry entry = null;
        
        if (record.objectEntryId > 0) {
            // Update existing object entry
            entry = _objectEntryLocalService.fetchObjectEntry(record.objectEntryId);
        } else {
            // Try to find by URN
            entry = findObjectEntryByURN(record.urn, companyId);
        }
        
        if (entry == null) {
            _log.warn("No object entry found for URN: " + record.urn);
            return;
        }
        
        Map<String, Serializable> values = entry.getValues();
        
        // Update Finacle fields
        values.put("finacleStatus", response.getStatus());
        values.put("finacleMessage", response.getMessage());
        values.put("finacleRequestId", response.getRequestId());
        values.put("finacleResponseDate", new Date());
        
        int currentRetryCount = GetterUtil.getInteger(values.get("finacleRetryCount"));
        values.put("finacleRetryCount", currentRetryCount + retryCount + 1);
        
        if (success) {
            values.put("finacleSentDate", new Date());
        }
        
        // Also update other fields if they exist in record and are valid
        if (Validator.isNotNull(record.customerId) && !"0".equals(record.customerId)) {
            values.put("customerId", record.customerId);
        }
        if (Validator.isNotNull(record.debitAccount)) {
            values.put("debitAccount", record.debitAccount);
        }
        
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setCompanyId(companyId);
        serviceContext.setUserId(userId);
        
        _objectEntryLocalService.updateObjectEntry(
            entry.getObjectEntryId(), userId, values, serviceContext);
        _log.debug("Updated object entry " + entry.getObjectEntryId() + " for URN: " + record.urn);
    }

    /**
     * Find object entry by URN using search
     */
    private ObjectEntry findObjectEntryByURN(String urn, long companyId) {
        try {
            // Create SearchContext properly
            SearchContext searchContext = new SearchContext();
            searchContext.setCompanyId(companyId);
            searchContext.setStart(0);
            searchContext.setEnd(10);
            
            // Build query for URN
            BooleanQuery booleanQuery = new BooleanQueryImpl();
            booleanQuery.add(new TermQueryImpl("urn", urn), BooleanClauseOccur.MUST);
            
            // Get indexer for ObjectEntry
            Indexer<ObjectEntry> indexer = IndexerRegistryUtil.getIndexer(ObjectEntry.class);
            
            if (indexer != null) {
                // Execute search with BooleanQuery
                Hits hits = indexer.search(searchContext);
                
                if (hits.getLength() > 0) {
                    long entryId = GetterUtil.getLong(hits.getDocs()[0].get(Field.ENTRY_CLASS_PK));
                    return _objectEntryLocalService.fetchObjectEntry(entryId);
                }
            }
        } catch (Exception e) {
            _log.error("Error finding object entry by URN: " + urn, e);
        }
        return null;
    }

    /**
     * Generate CSV report
     */
    private void generateReport(String tempDir, String fileName) {
        String reportPath = tempDir + "/finacle_consent_report_" + System.currentTimeMillis() + ".csv";
        
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.FileWriter(reportPath, StandardCharsets.UTF_8))) {
            writer.write("\uFEFFURN,Customer ID,Status,Message,Retry Count"); // Add BOM for Excel
            writer.newLine();
            
            for (ConsentResult result : results) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d",
                    result.urn != null ? result.urn : "",
                    result.customerId != null ? result.customerId : "",
                    result.status != null ? result.status : "",
                    result.message != null ? result.message.replace("\"", "'") : "",
                    result.retryCount));
                writer.newLine();
            }
            
            _log.info("Report generated: " + reportPath);
            _log.info("Report summary: Total=" + results.size() + 
                ", Success=" + totalSuccess.get() + 
                ", Failed=" + totalFailed.get() + 
                ", Errors=" + totalErrors.get());
            
        } catch (Exception e) {
            _log.error("Failed to generate report", e);
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

    /**
     * Inner class to store consent record
     */
    private static class ConsentRecord {
        String urn;
        String customerId;
        String debitAccount;
        String bimaType;
        long objectEntryId;
        Map<String, Serializable> existingValues;
        
        ConsentRecord(String urn, String customerId, String debitAccount, String bimaType) {
            this.urn = urn;
            this.customerId = customerId;
            this.debitAccount = debitAccount;
            this.bimaType = bimaType;
        }
    }
    
    /**
     * Inner class to store consent result
     */
    private static class ConsentResult {
        String urn;
        String customerId;
        String status;
        String message;
        int retryCount;
    }
}