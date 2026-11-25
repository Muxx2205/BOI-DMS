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
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
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

@Component(
    immediate = true,
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.BulkUpdateAccountNoBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)

public class BulkUpdateAccountNoBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(BulkUpdateAccountNoBackgroundTaskExecutor.class);

    // Blueprint ERC - using same as your working search
    private static final String TOTAL_COUNT_BLUEPRINT_ERC = "PMSBYSearch";
    
    // Blueprint parameter names

    private static final class Params {
        static final String URN = "uRN";
        static final String CONSENT_RECIVED = "consentRecived";
        static final String STAGE = "stage";
        static final String ACCOUNT_NUMBER = "accountNumber";
        private Params() {}
    }

    // Performance configuration
    private static final int PARALLEL_THREADS = 20;
    private static final int BATCH_SIZE = 500;
    private static final int LOG_INTERVAL = 5000;
    private static final int ES_SEARCH_SIZE = 1000;

    private ExecutorService executorService;

    // Statistics
    private final AtomicInteger totalRecordsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalUpdated = new AtomicInteger(0);
    private final AtomicInteger totalNotFound = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicInteger totalNoChange = new AtomicInteger(0);
    private final AtomicLong totalTimeSpent = new AtomicLong(0);
    private int totalRecordsCount = 0;

    // Update report storage
//    private final List<UpdateInfo> updateSummary = new ArrayList<>();
    
    private final ConcurrentLinkedQueue<UpdateInfo> updateSummary = new ConcurrentLinkedQueue<>();


    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;

    @Reference
    private Searcher _searcher;

    @Reference
    private SearchRequestBuilderFactory _searchRequestBuilderFactory;

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();

        String csvFilePath = GetterUtil.getString(taskContextMap.get("csvFilePath"));
        String fileName = GetterUtil.getString(taskContextMap.get("fileName"));
        long userId = GetterUtil.getLong(taskContextMap.get("userId"));
        long companyId = GetterUtil.getLong(taskContextMap.get("companyId"));
        String urnColumnName = GetterUtil.getString(taskContextMap.get("urnColumnName"), "URN");
        String accountNoColumnName = GetterUtil.getString(taskContextMap.get("accountNoColumnName"), "accountNumber");
        String delimiter = GetterUtil.getString(taskContextMap.get("delimiter"), ",");

        _log.info("==========================================================");
        _log.info("BULK UPDATE ACCOUNT NUMBER STARTED");
        _log.info("==========================================================");
        _log.info("CSV File: " + fileName);
        _log.info("URN Column: " + urnColumnName);
        _log.info("Account No Column: " + accountNoColumnName);
        _log.info("Company ID: " + companyId);
        _log.info("Blueprint ERC: " + TOTAL_COUNT_BLUEPRINT_ERC);

        User user = _userLocalService.getUser(userId);
        _log.info("Started by: " + user.getEmailAddress());

        File csvFile = new File(csvFilePath);
        
        if (!csvFile.exists()) {
            _log.error("CSV file not found: " + csvFilePath);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "CSV file not found");
        }

        long startTime = System.currentTimeMillis();
        executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

        try {
            // Step 1: Read URN and Account Number pairs from CSV
            _log.info("Step 1: Reading CSV file...");
            List<UpdateRecord> updateRecords = readUpdateRecordsFromCSV(csvFile, urnColumnName, accountNoColumnName, delimiter);
            totalRecordsCount = updateRecords.size();
            
            _log.info("Found " + totalRecordsCount + " unique records to process");

            if (updateRecords.isEmpty()) {
                _log.warn("No valid records found in CSV file!");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }

            // Display sample records
            _log.info("Sample records (first 10):");
            for (int i = 0; i < Math.min(updateRecords.size(), 10); i++) {
                UpdateRecord record = updateRecords.get(i);
                _log.info("  " + (i + 1) + ". URN: '" + record.getUrn() + "', New Account: '" + record.getNewAccountNumber() + "'");
            }

            // Step 2: Process updates in batches using blueprint search
            _log.info("Step 2: Processing updates using blueprint search...");
            processUpdatesInBatches(updateRecords, companyId, userId);

            long totalTime = System.currentTimeMillis() - startTime;

            // Final summary
            _log.info("==========================================================");
            _log.info("BULK UPDATE COMPLETED");
            _log.info("==========================================================");
            _log.info("Total records: " + totalRecordsProcessed.get());
            _log.info("Updated: " + totalUpdated.get());
            _log.info("No change: " + totalNoChange.get());
            _log.info("Not found: " + totalNotFound.get());
            _log.info("Errors: " + totalErrors.get());
            _log.info(String.format("Total time: %d min %d sec", totalTime / 60000, (totalTime % 60000) / 1000));

            // Generate report
            generateUpdateReport(csvFile.getParent(), fileName);
            csvFile.delete();

            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

        } catch (Exception e) {
            _log.error("FATAL ERROR during bulk update", e);
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
     * Read URN and Account Number pairs from CSV file
     */
    private List<UpdateRecord> readUpdateRecordsFromCSV(File csvFile, String urnColumnName, 
            String accountNoColumnName, String delimiter) throws Exception {
        
        List<UpdateRecord> records = new ArrayList<>();
        Map<String, UpdateRecord> uniqueRecords = new HashMap<>();
        int lineNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)))) {
            String line;
            boolean isFirstLine = true;
            int urnColumnIndex = -1;
            int accountNoColumnIndex = -1;
            int consentRecivedColumnIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] columns = line.split(delimiter);
                
                if (isFirstLine) {
                    for (int i = 0; i < columns.length; i++) {
                        String header = columns[i].trim();
                        if (urnColumnName.equalsIgnoreCase(header)) {
                            urnColumnIndex = i;
                            _log.info("Found URN column at index: " + i);
                        } else if (accountNoColumnName.equalsIgnoreCase(header)) {
                            accountNoColumnIndex = i;
                            _log.info("Found Account Number column at index: " + i);
                        } else if("consentRecieved".equals(header)) {
                        	consentRecivedColumnIndex=i;
                        }
                    }
                    
                    if (urnColumnIndex == -1) {
                        throw new Exception("URN column '" + urnColumnName + "' not found. Headers: " + String.join(", ", columns));
                    }
                    if (accountNoColumnIndex == -1) {
                        throw new Exception("Account Number column '" + accountNoColumnName + "' not found. Headers: " + String.join(", ", columns));
                    }
                    
                    isFirstLine = false;
                    continue;
                }
                
                if (columns.length > Math.max(urnColumnIndex, accountNoColumnIndex)) {
                    String urn = columns[urnColumnIndex].trim();
//                    String accountNumber = columns[accountNoColumnIndex].trim();
//                    String consentRecieved =  columns[consentRecivedColumnIndex].trim();
                    
                        uniqueRecords.put(urn, new UpdateRecord(urn, "accountNumber","true"));
                }
            }
        }
        
        records.addAll(uniqueRecords.values());
        _log.info("Read " + records.size() + " unique records from " + lineNumber + " lines");
        return records;
    }

    /**
     * Process updates in batches
     */
    private void processUpdatesInBatches(List<UpdateRecord> updateRecords, long companyId, 
             long userId) {
        int totalBatches = (int) Math.ceil(updateRecords.size() / (double) BATCH_SIZE);
        _log.info("Total batches: " + totalBatches + " (batch size: " + BATCH_SIZE + ")");
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < updateRecords.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, updateRecords.size());
            List<UpdateRecord> batch = updateRecords.subList(i, end);
            final int batchNumber = (i / BATCH_SIZE) + 1;
            
            futures.add(CompletableFuture.runAsync(() -> 
                processBatch(batch, batchNumber, totalBatches, companyId, userId), executorService));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Process a single batch using BLUEPRINT SEARCH (matching your working pattern)
     */
    private void processBatch(List<UpdateRecord> batch, int batchNumber, int totalBatches, 
            long companyId, long userId) {
        
        _log.info(String.format("Batch %d/%d started with %d records", batchNumber, totalBatches, batch.size()));
        
        
        batch.parallelStream().forEach(record -> {
            try {
                
                // Find entry by URN using Blueprint search
                ObjectEntry entry = findEntryByBlueprint(record.getUrn(), companyId);
                
                if (entry != null) {
                    String oldAccountNumber = GetterUtil.getString(entry.getValues().get(Params.ACCOUNT_NUMBER));
                    
                    // Update account number
                    Map<String, Serializable> values = entry.getValues();
                    values.put(Params.CONSENT_RECIVED, "true");
                    values.put(Params.STAGE, "4");
//                    values.put(Params.ACCOUNT_NUMBER, record.getNewAccountNumber());
                    
                    ServiceContext serviceContext = new ServiceContext();
                    serviceContext.setCompanyId(companyId);
                    serviceContext.setUserId(userId);
                    
                    _objectEntryLocalService.addOrUpdateObjectEntry(
                        entry.getExternalReferenceCode(), userId, 0l, entry.getObjectDefinitionId(), values, serviceContext);
                    
                    totalUpdated.incrementAndGet();
                    
                    
                        UpdateInfo info = new UpdateInfo();
                        info.urn = record.getUrn();
                        info.entryId = entry.getObjectEntryId();
                        info.oldAccountNumber = oldAccountNumber;
                        info.newAccountNumber = record.getNewAccountNumber();
                        info.status = "SUCCESS";
                        updateSummary.add(info);
                    
                } else {
                    totalNotFound.incrementAndGet();
                    
                        UpdateInfo info = new UpdateInfo();
                        info.urn = record.getUrn();
                        info.status = "NOT_FOUND";
                        updateSummary.add(info);
                    
                }
                
                totalRecordsProcessed.incrementAndGet();
                
            } catch (Exception e) {
                totalErrors.incrementAndGet();
                _log.error("Error updating URN: " + record.getUrn(), e);
            }
        });
        
    }

    /**
     * Find entry by URN using BLUEPRINT SEARCH (same pattern as your working dashboard)
     */
    private ObjectEntry findEntryByBlueprint(String urn, long companyId) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Create search request builder
            SearchRequestBuilder searchRequestBuilder = _searchRequestBuilderFactory.builder();
            
            // Set basic properties
            searchRequestBuilder.companyId(companyId);
            searchRequestBuilder.emptySearchEnabled(false);
            searchRequestBuilder.from(0);
            searchRequestBuilder.size(10);
            searchRequestBuilder.fetchSource(true);
            
            // Add query string for URN
            String queryString = "\"" + urn + "\"";
            searchRequestBuilder.queryString(queryString);
            
            // Configure search context with blueprint attributes
            searchRequestBuilder.withSearchContext(searchContext -> {
                searchContext.setAttribute("search.experiences.blueprint.external.reference.code", TOTAL_COUNT_BLUEPRINT_ERC);
                searchContext.setAttribute("search.experiences.scope.group.id", "0");
                searchContext.setAttribute("search.empty.search", "false");
                searchContext.setStart(0);
                searchContext.setCompanyId(companyId);
                searchContext.setEnd(10);
                searchContext.setGroupIds(new long[] { 0 });
                
//                // Pass URN parameter to blueprint
//                if (Validator.isNotNull(urn)) {
//                    searchContext.setAttribute(BLUEPRINT_PARAM_URN, urn);
//                }
            });
            
            // Build and execute search
            SearchRequest searchRequest = searchRequestBuilder.build();
            SearchResponse searchResponse = _searcher.search(searchRequest);
            
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (searchResponse != null && searchResponse.getTotalHits() > 0) {
                
                // Get the first entry (lowest stage)
                var searchHits = searchResponse.getSearchHits();
                if (searchHits != null && searchHits.getSearchHits() != null && !searchHits.getSearchHits().isEmpty()) {
                    var hit = searchHits.getSearchHits().get(0);
                    long entryId = GetterUtil.getLong(hit.getDocument().getLong("entryClassPK"));
                    if (entryId > 0) {
                        return _objectEntryLocalService.fetchObjectEntry(entryId);
                    }
                }
            } else {
                _log.info(String.format("Blueprint search for URN '%s' returned no results in %d ms", urn, elapsed));
            }
            
        } catch (Exception e) {
            _log.error("Error in blueprint search for URN: " + urn, e);
        }
        
        return null;
    }

    /**
     * Generate update report CSV
     */
    private void generateUpdateReport(String tempDir, String originalFileName) {
        String reportPath = tempDir + "/update_report_" + System.currentTimeMillis() + ".csv";
        
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(reportPath))) {
            writer.write("URN,Entry ID,Old Account Number,New Account Number,Status,Error Message");
            writer.newLine();
            
            for (UpdateInfo info : updateSummary) {
                writer.write(String.format("\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"",
                    info.urn != null ? info.urn : "",
                    info.entryId,
                    info.oldAccountNumber != null ? info.oldAccountNumber : "",
                    info.newAccountNumber != null ? info.newAccountNumber : "",
                    info.status,
                    info.errorMessage != null ? info.errorMessage.replace("\"", "'") : ""));
                writer.newLine();
            }
            
            _log.info("Update report generated: " + reportPath);
            _log.info("Report summary: Total=" + updateSummary.size() + 
                ", Success=" + totalUpdated.get() + 
                ", No Change=" + totalNoChange.get() +
                ", Not Found=" + totalNotFound.get() + 
                ", Errors=" + totalErrors.get());
            
        } catch (Exception e) {
            _log.error("Failed to generate update report", e);
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
     * Inner class to store update record
     */
    private static class UpdateRecord {
        private String urn;
        private String newAccountNumber;
        private String consentRecived;
        
        public UpdateRecord(String urn, String newAccountNumber,String consentRecived) {
            this.urn = urn;
            this.newAccountNumber = newAccountNumber;
            this.consentRecived = consentRecived;
        }
        
        public String getUrn() { return urn; }
        public String getNewAccountNumber() { return newAccountNumber; }
        public String getConsentRecived() { return consentRecived; }
    }
    
    /**
     * Inner class to store update information for report
     */
    private static class UpdateInfo {
        String urn;
        long entryId;
        String oldAccountNumber;
        String newAccountNumber;
        String status;
        String errorMessage;
    }
}