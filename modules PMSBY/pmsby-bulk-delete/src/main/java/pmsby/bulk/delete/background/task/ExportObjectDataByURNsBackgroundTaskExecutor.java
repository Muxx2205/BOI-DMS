package pmsby.bulk.delete.background.task;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.ExportObjectDataByURNsBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)
public class ExportObjectDataByURNsBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(ExportObjectDataByURNsBackgroundTaskExecutor.class);

    // Blueprint configuration
    private static final String TOTAL_COUNT_BLUEPRINT_ERC = "PMSBYSearch";
    
    // Performance configuration
    private static final int PARALLEL_THREADS = 5;
    private static final int BATCH_SIZE = 100;
    private static final int LOG_INTERVAL = 100;
    private static final int ES_SEARCH_SIZE = 10;

    private ExecutorService executorService;

    // Statistics
    private final AtomicInteger totalRecordsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalFound = new AtomicInteger(0);
    private final AtomicInteger totalNotFound = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicLong totalTimeSpent = new AtomicLong(0);
    private int totalRecordsCount = 0;

    // Export data storage
    private final List<ExportRecord> exportData = new ArrayList<>();
    private volatile Set<String> allFieldNames = ConcurrentHashMap.newKeySet();
    
    // Field mapping for common fields
    private static final Map<String, String> FIELD_DISPLAY_NAMES = new HashMap<>();
    static {
        FIELD_DISPLAY_NAMES.put("objectEntryId", "Object Entry ID");
        FIELD_DISPLAY_NAMES.put("createDate", "Create Date");
        FIELD_DISPLAY_NAMES.put("modifiedDate", "Modified Date");
        FIELD_DISPLAY_NAMES.put("status", "Status");
        FIELD_DISPLAY_NAMES.put("stage", "Stage");
        FIELD_DISPLAY_NAMES.put("accountNumber", "Account Number");
        FIELD_DISPLAY_NAMES.put("externalReferenceCode", "External Reference Code");
    }

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
        long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"));
        String urnColumnName = GetterUtil.getString(taskContextMap.get("urnColumnName"), "URN");
        String delimiter = GetterUtil.getString(taskContextMap.get("delimiter"), ",");
        String exportFileName = GetterUtil.getString(taskContextMap.get("exportFileName"), "");
        boolean exportAllFields = GetterUtil.getBoolean(taskContextMap.get("exportAllFields"), true);
        String selectedFieldsStr = GetterUtil.getString(taskContextMap.get("selectedFields"), "");
        
        Set<String> selectedFields = Set.of(selectedFieldsStr.split(","));
        
        _log.info("==========================================================");
        _log.info("📥 EXPORT OBJECT DATA BY URNS STARTED");
        _log.info("==========================================================");
        _log.info("Input CSV File: " + fileName);
        _log.info("URN Column: " + urnColumnName);
        _log.info("Object Definition ID: " + objectDefinitionId);
        _log.info("Company ID: " + companyId);
        _log.info("Export All Fields: " + exportAllFields);
        _log.info("Selected Fields: " + selectedFieldsStr);
        _log.info("Blueprint ERC: " + TOTAL_COUNT_BLUEPRINT_ERC);

        File csvFile = new File(csvFilePath);
        
        if (!csvFile.exists()) {
            _log.error("CSV file not found: " + csvFilePath);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "CSV file not found");
        }

        long startTime = System.currentTimeMillis();
        executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

        try {
            // Step 1: Read URNs from CSV
            _log.info("Step 1: Reading URNs from CSV file...");
            List<String> urns = readURNsFromCSV(csvFile, urnColumnName, delimiter);
            totalRecordsCount = urns.size();
            
            _log.info("Found " + totalRecordsCount + " unique URNs to export");

            if (urns.isEmpty()) {
                _log.warn("No valid URNs found in CSV file!");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }

            // Display sample URNs
            _log.info("Sample URNs (first 10):");
            for (int i = 0; i < Math.min(urns.size(), 10); i++) {
                _log.info("  " + (i + 1) + ". " + urns.get(i));
            }

            // Step 2: Process exports in batches
            _log.info("Step 2: Fetching object data for URNs...");
            processExportsInBatches(urns, companyId, objectDefinitionId, exportAllFields, selectedFields);

            long totalTime = System.currentTimeMillis() - startTime;

            // Final summary
            _log.info("==========================================================");
            _log.info("✅ EXPORT COMPLETED");
            _log.info("==========================================================");
            _log.info("Total URNs: " + totalRecordsProcessed.get());
            _log.info("Found: " + totalFound.get());
            _log.info("Not found: " + totalNotFound.get());
            _log.info("Errors: " + totalErrors.get());
            _log.info(String.format("Total time: %d min %d sec", totalTime / 60000, (totalTime % 60000) / 1000));
            _log.info("Total fields in export: " + allFieldNames.size());

            // Step 3: Generate export CSV
            _log.info("Step 3: Generating export CSV file...");
            generateExportCSV(csvFile.getParent(), fileName, exportFileName, delimiter, exportAllFields);
            
            // Clean up input file
            csvFile.delete();

            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

        } catch (Exception e) {
            _log.error("FATAL ERROR during export", e);
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
     * Read URNs from CSV file
     */
    private List<String> readURNsFromCSV(File csvFile, String urnColumnName, String delimiter) throws Exception {
        List<String> urns = new ArrayList<>();
        Map<String, Boolean> uniqueURNs = new HashMap<>();
        int lineNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)))) {
            String line;
            boolean isFirstLine = true;
            int urnColumnIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] columns = line.split(delimiter);
                
                if (isFirstLine) {
                    // Find URN column
                    for (int i = 0; i < columns.length; i++) {
                        String header = columns[i].trim();
                        if (urnColumnName.equalsIgnoreCase(header)) {
                            urnColumnIndex = i;
                            _log.info("Found URN column '" + header + "' at index: " + i);
                            break;
                        }
                    }
                    
                    if (urnColumnIndex == -1) {
                        throw new Exception("URN column '" + urnColumnName + "' not found. Available headers: " + String.join(", ", columns));
                    }
                    
                    isFirstLine = false;
                    continue;
                }
                
                if (columns.length > urnColumnIndex) {
                    String urn = columns[urnColumnIndex].trim();
                    if (!urn.isEmpty() && !uniqueURNs.containsKey(urn)) {
                        uniqueURNs.put(urn, true);
                        urns.add(urn);
                    }
                }
            }
        }
        
        _log.info("Read " + urns.size() + " unique URNs from " + (lineNumber - 1) + " data rows");
        return urns;
    }

    /**
     * Process exports in batches
     */
    private void processExportsInBatches(List<String> urns, long companyId, long objectDefinitionId, 
            boolean exportAllFields, Set<String> selectedFields) {
        int totalBatches = (int) Math.ceil(urns.size() / (double) BATCH_SIZE);
        _log.info("Total batches: " + totalBatches + " (batch size: " + BATCH_SIZE + ")");
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < urns.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, urns.size());
            List<String> batch = urns.subList(i, end);
            final int batchNumber = (i / BATCH_SIZE) + 1;
            
            futures.add(CompletableFuture.runAsync(() -> 
                processBatch(batch, batchNumber, totalBatches, companyId, objectDefinitionId, 
                    exportAllFields, selectedFields), executorService));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Process a single batch
     */
    private void processBatch(List<String> batch, int batchNumber, int totalBatches, 
            long companyId, long objectDefinitionId, boolean exportAllFields, Set<String> selectedFields) {
        
        _log.info(String.format("Batch %d/%d started with %d URNs", batchNumber, totalBatches, batch.size()));
        
        int batchFound = 0;
        int batchNotFound = 0;
        int batchErrors = 0;
        long batchStartTime = System.currentTimeMillis();
        
        for (String urn : batch) {
            long recordStartTime = System.currentTimeMillis();
            
            try {
                // Find entry by URN using Blueprint search
                ObjectEntry entry = findEntryByBlueprint(urn, companyId);
                
                if (entry != null) {
                    batchFound++;
                    totalFound.incrementAndGet();
                    
                    // Extract all field values from the object entry
                    Map<String, String> fieldValues = extractFieldValues(entry, exportAllFields, selectedFields);
                    
                    // Add to export data
                    synchronized (exportData) {
                        ExportRecord record = new ExportRecord();
                        record.urn = urn;
                        record.entryId = entry.getObjectEntryId();
                        record.fieldValues = fieldValues;
                        record.status = "FOUND";
                        exportData.add(record);
                        
                        // Track all field names for CSV header
                        allFieldNames.addAll(fieldValues.keySet());
                    }
                    
                    _log.info(String.format("✅ Found: %s | Entry ID: %d | Fields: %d", 
                        urn, entry.getObjectEntryId(), fieldValues.size()));
                } else {
                    batchNotFound++;
                    totalNotFound.incrementAndGet();
                    
                    synchronized (exportData) {
                        ExportRecord record = new ExportRecord();
                        record.urn = urn;
                        record.status = "NOT_FOUND";
                        exportData.add(record);
                    }
                    
                    if (batchNotFound <= 10) {
                        _log.warn("❌ URN not found: " + urn);
                    }
                }
                
            } catch (Exception e) {
                _log.error("Error exporting URN: " + urn, e);
                batchErrors++;
                totalErrors.incrementAndGet();
                
                synchronized (exportData) {
                    ExportRecord record = new ExportRecord();
                    record.urn = urn;
                    record.status = "ERROR";
                    record.errorMessage = e.getMessage();
                    exportData.add(record);
                }
            }
            
            totalTimeSpent.addAndGet(System.currentTimeMillis() - recordStartTime);
            int processed = totalRecordsProcessed.incrementAndGet();
            
            if (processed % LOG_INTERVAL == 0) {
                logProgress(processed);
            }
        }
        
        long batchTime = System.currentTimeMillis() - batchStartTime;
        _log.info(String.format("Batch %d/%d completed in %d ms | ✅ Found: %d | ❌ Not Found: %d | ⚠️ Errors: %d", 
            batchNumber, totalBatches, batchTime, batchFound, batchNotFound, batchErrors));
    }

    /**
     * Find entry by URN using Blueprint search
     */
    private ObjectEntry findEntryByBlueprint(String urn, long companyId) {
        try {
            long startTime = System.currentTimeMillis();
            
            SearchRequestBuilder searchRequestBuilder = _searchRequestBuilderFactory.builder();
            
            searchRequestBuilder.companyId(companyId);
            searchRequestBuilder.emptySearchEnabled(false);
            searchRequestBuilder.from(0);
            searchRequestBuilder.size(ES_SEARCH_SIZE);
            searchRequestBuilder.fetchSource(true);
            
            // Add query string for URN
            String queryString = "\"" + urn + "\"";
            searchRequestBuilder.queryString(queryString);
            
            searchRequestBuilder.withSearchContext(searchContext -> {
                searchContext.setAttribute("search.experiences.blueprint.external.reference.code", TOTAL_COUNT_BLUEPRINT_ERC);
                searchContext.setAttribute("search.experiences.scope.group.id", "0");
                searchContext.setAttribute("search.empty.search", "false");
                searchContext.setStart(0);
                searchContext.setCompanyId(companyId);
                searchContext.setEnd(ES_SEARCH_SIZE);
                searchContext.setGroupIds(new long[] { 0 });
            });
            
            SearchRequest searchRequest = searchRequestBuilder.build();
            SearchResponse searchResponse = _searcher.search(searchRequest);
            
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (searchResponse != null && searchResponse.getTotalHits() > 0) {
                _log.info(String.format("Blueprint search for URN '%s' returned %d hits in %d ms", 
                    urn, searchResponse.getTotalHits(), elapsed));
                
                var searchHits = searchResponse.getSearchHits();
                if (searchHits != null && searchHits.getSearchHits() != null && !searchHits.getSearchHits().isEmpty()) {
                    // Find the entry with lowest stage
                    ObjectEntry bestEntry = null;
                    int lowestStage = Integer.MAX_VALUE;
                    
                    for (var hit : searchHits.getSearchHits()) {
                        long entryId = GetterUtil.getLong(hit.getDocument().getLong("entryClassPK"));
                        if (entryId > 0) {
                            ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryId);
                            if (entry != null) {
                                int stage = GetterUtil.getInteger(entry.getValues().get("stage"), Integer.MAX_VALUE);
                                if (stage < lowestStage) {
                                    lowestStage = stage;
                                    bestEntry = entry;
                                }
                            }
                        }
                    }
                    
                    if (bestEntry != null) {
                        return bestEntry;
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
     * Extract field values from ObjectEntry
     */
    private Map<String, String> extractFieldValues(ObjectEntry entry, boolean exportAllFields, Set<String> selectedFields) {
        Map<String, String> fieldValues = new LinkedHashMap<>();
        
        Map<String, Serializable> values = entry.getValues();
        
        if (exportAllFields) {
            // Export all fields from the object
            for (Map.Entry<String, Serializable> field : values.entrySet()) {
                String displayName = FIELD_DISPLAY_NAMES.getOrDefault(field.getKey(), field.getKey());
                String value = field.getValue() != null ? field.getValue().toString() : "";
                fieldValues.put(displayName, value);
            }
            
            // Add standard fields
            if (!fieldValues.containsKey("Object Entry ID")) {
                fieldValues.put("Object Entry ID", String.valueOf(entry.getObjectEntryId()));
            }
            if (!fieldValues.containsKey("Create Date") && entry.getCreateDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                fieldValues.put("Create Date", sdf.format(entry.getCreateDate()));
            }
            if (!fieldValues.containsKey("Modified Date") && entry.getModifiedDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                fieldValues.put("Modified Date", sdf.format(entry.getModifiedDate()));
            }
            if (!fieldValues.containsKey("External Reference Code") && Validator.isNotNull(entry.getExternalReferenceCode())) {
                fieldValues.put("External Reference Code", entry.getExternalReferenceCode());
            }
        } else {
            // Export only selected fields
            for (String field : selectedFields) {
                String displayName = FIELD_DISPLAY_NAMES.getOrDefault(field, field);
                String value = "";
                
                if ("objectEntryId".equals(field)) {
                    value = String.valueOf(entry.getObjectEntryId());
                } else if ("createDate".equals(field) && entry.getCreateDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = sdf.format(entry.getCreateDate());
                } else if ("modifiedDate".equals(field) && entry.getModifiedDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = sdf.format(entry.getModifiedDate());
                } else if ("externalReferenceCode".equals(field)) {
                    value = entry.getExternalReferenceCode();
                } else {
                    Serializable fieldValue = values.get(field);
                    if (fieldValue != null) {
                        value = fieldValue.toString();
                    }
                }
                
                fieldValues.put(displayName, value);
            }
        }
        
        return fieldValues;
    }

    /**
     * Generate export CSV file
     */
    private void generateExportCSV(String tempDir, String originalFileName, String customFileName, 
            String delimiter, boolean exportAllFields) {
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String exportFileName = Validator.isNotNull(customFileName) ? 
            customFileName + "_" + timestamp + ".csv" : 
            "export_by_urns_" + timestamp + ".csv";
        
        String exportPath = tempDir + "/" + exportFileName;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath))) {
            // Prepare headers
            List<String> headers = new ArrayList<>();
            headers.add("URN");
            headers.add("Status");
            
            if (exportAllFields) {
                // Add all field names that were found
                List<String> sortedFields = new ArrayList<>(allFieldNames);
                java.util.Collections.sort(sortedFields);
                headers.addAll(sortedFields);
            } else {
                headers.add("Object Entry ID");
                headers.add("Error Message");
            }
            
            headers.add("Entry ID");
            
            // Write header
            writer.write(String.join(delimiter, headers));
            writer.newLine();
            
            // Write data rows
            for (ExportRecord record : exportData) {
                List<String> row = new ArrayList<>();
                row.add(escapeCSV(record.urn, delimiter));
                row.add(record.status);
                
                if (exportAllFields && record.fieldValues != null) {
                    for (String fieldName : headers) {
                        if (!fieldName.equals("URN") && !fieldName.equals("Status") && !fieldName.equals("Entry ID")) {
                            String value = record.fieldValues.getOrDefault(fieldName, "");
                            row.add(escapeCSV(value, delimiter));
                        }
                    }
                } else if (!exportAllFields) {
                    if ("FOUND".equals(record.status) && record.fieldValues != null) {
                        row.add(escapeCSV(record.fieldValues.getOrDefault("Object Entry ID", ""), delimiter));
                        row.add("");
                    } else {
                        row.add("");
                        row.add(escapeCSV(record.errorMessage != null ? record.errorMessage : "", delimiter));
                    }
                }
                
                row.add(String.valueOf(record.entryId));
                
                writer.write(String.join(delimiter, row));
                writer.newLine();
            }
            
            _log.info("📊 Export file generated: " + exportPath);
            _log.info("Export summary: Total=" + exportData.size() + 
                ", Found=" + totalFound.get() + 
                ", Not Found=" + totalNotFound.get() + 
                ", Errors=" + totalErrors.get());
            
        } catch (Exception e) {
            _log.error("Failed to generate export CSV", e);
        }
    }
    
    /**
     * Escape CSV values to handle delimiters and quotes
     */
    private String escapeCSV(String value, String delimiter) {
        if (value == null) return "";
        
        if (value.contains(delimiter) || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }

    /**
     * Log progress
     */
    private void logProgress(int processed) {
        double percent = (processed * 100.0) / totalRecordsCount;
        double avgTime = totalTimeSpent.get() / (double) processed;
        
        _log.info(String.format(
            "📈 PROGRESS: %d/%d (%.1f%%) | ✅ Found: %d | ❌ Not Found: %d | ⚠️ Errors: %d | ⏱️ Avg: %.2f ms",
            processed, totalRecordsCount, percent, 
            totalFound.get(), totalNotFound.get(), totalErrors.get(), avgTime
        ));
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
     * Inner class to store export record
     */
    private static class ExportRecord {
        String urn;
        long entryId;
        Map<String, String> fieldValues;
        String status;
        String errorMessage;
    }
}