package pmsby.bulk.delete.background.task;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import pmsby.bulk.delete.util.CSVReaderUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.BulkDeleteBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)
public class BulkDeleteBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(BulkDeleteBackgroundTaskExecutor.class);
    
    // ================ OPTIMIZED FOR 2.5 LAKH RECORDS ================
    private static final int BATCH_SIZE = 5000; // Process 5000 records per batch
    private static final int PARALLEL_THREADS = 5; // 5 parallel threads
    private static final int LOG_INTERVAL = 10000; // Log every 10k records
    private static final long BATCH_DELAY_MS = 50; // 50ms delay between batches
    private static final int MAX_RETRIES = 3; // Max retries for failed deletions
    
    // Executor service - will be initialized in execute method
    private ExecutorService executorService;
    
    // Statistics
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicInteger totalDeleted = new AtomicInteger(0);
    private final AtomicInteger totalNotFound = new AtomicInteger(0);
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicLong totalTimeSpent = new AtomicLong(0);

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private UserLocalService _userLocalService;
    
    @Reference
    private CSVReaderUtil _csvReaderUtil;

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
        
        String csvFilePath = GetterUtil.getString(taskContextMap.get("csvFilePath"));
        String fileName = GetterUtil.getString(taskContextMap.get("fileName"));
        String idColumnName = GetterUtil.getString(taskContextMap.get("idColumnName"), "c_pmsbyPmjjyConsentFormId_");
        String delimiter = GetterUtil.getString(taskContextMap.get("delimiter"), ",");
        long userId = GetterUtil.getLong(taskContextMap.get("userId"));

        _log.info("==========================================================");
        _log.info("BULK DELETION STARTED for: " + fileName);
        _log.info("==========================================================");
        _log.info("ID Column: " + idColumnName);
        _log.info("Delimiter: " + delimiter);
        _log.info("Batch Size: " + BATCH_SIZE);
        _log.info("Parallel Threads: " + PARALLEL_THREADS);

        User user = _userLocalService.getUser(userId);
        _log.info("Started by: " + user.getEmailAddress() + " (ID: " + userId + ")");
        
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            _log.error("CSV file not found: " + csvFilePath);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "CSV file not found");
        }

        long startTime = System.currentTimeMillis();
        int totalIds = 0;

        // Initialize executor service HERE, not as static final
        executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

        try {
            // Read IDs from CSV
            _log.info("Reading IDs from CSV file...");
            List<String> idStrings = _csvReaderUtil.readIdsFromCSV(csvFile, idColumnName, delimiter);
            
            if (idStrings.isEmpty()) {
                _log.info("No IDs found in CSV");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }
            
            // Convert String IDs to Long and remove duplicates
            _log.info("Converting and deduplicating IDs...");
            Set<Long> uniqueIds = new HashSet<>();
            int invalidCount = 0;
            
            for (String idStr : idStrings) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    uniqueIds.add(id);
                } catch (NumberFormatException e) {
                    invalidCount++;
                    if (invalidCount <= 10) {
                        _log.warn("Invalid ID format: '" + idStr + "'");
                    }
                }
            }
            
            List<Long> entryIds = new ArrayList<>(uniqueIds);
            totalIds = entryIds.size();
            
            _log.info(String.format("Summary: Total in CSV: %d, Invalid: %d, Unique Valid IDs: %d", 
                idStrings.size(), invalidCount, totalIds));
            
            if (totalIds == 0) {
                _log.info("No valid IDs found in CSV");
                return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
            }
            
            // Show sample IDs
            _log.info("Sample IDs (first 10):");
            for (int i = 0; i < Math.min(entryIds.size(), 10); i++) {
                _log.info("  " + (i+1) + ": " + entryIds.get(i));
            }
            
            // Validate that IDs exist in database (optional - can be skipped for speed)
            _log.info("Validating IDs against database...");
//            int existingCount = 0;
//            int sampleSize = Math.min(100, entryIds.size());
//            for (int i = 0; i < sampleSize; i++) {
//                ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryIds.get(i));
//                if (entry != null) {
//                    existingCount++;
//                }
//            }
//            _log.info(String.format("Sample validation: %d/%d IDs exist in database (%.1f%%)", 
//                existingCount, sampleSize, (existingCount * 100.0 / sampleSize)));
//            
//            // Process deletion in optimized batches
//            _log.info("Starting optimized batch deletion...");
//            _log.info(String.format("Total batches: %d (batch size: %d, threads: %d)", 
//                (int) Math.ceil(totalIds / (double) BATCH_SIZE), BATCH_SIZE, PARALLEL_THREADS));
//            
            processDeletionBatches(entryIds);
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Final summary
            _log.info("==========================================================");
            _log.info("BULK DELETION COMPLETED");
            _log.info("==========================================================");
            _log.info("Total IDs processed: " + totalIds);
            _log.info("Successfully deleted: " + totalDeleted.get());
            _log.info("Not found in database: " + totalNotFound.get());
            _log.info("Errors: " + totalErrors.get());
            _log.info(String.format("Total time: %d min %d sec", 
                totalTime / 60000, (totalTime % 60000) / 1000));
            
            if (totalDeleted.get() > 0) {
                double avgTime = totalTimeSpent.get() / (double) totalDeleted.get();
                _log.info(String.format("Average time per deletion: %.2f ms", avgTime));
                _log.info(String.format("Speed: %.2f records/sec", 
                    totalDeleted.get() / (totalTime / 1000.0)));
            }
            
            if (totalNotFound.get() > 0) {
                _log.info("Note: " + totalNotFound.get() + " IDs were not found in database");
            }
            
            if (totalErrors.get() > 0) {
                _log.info(totalErrors.get() + " deletions failed - check logs above");
            }
            
            _log.info("==========================================================");
            
            // Cleanup - try multiple times
            boolean deleted = false;
            for (int i = 0; i < 3 && !deleted; i++) {
                deleted = csvFile.delete();
                if (!deleted && i < 2) {
                    Thread.sleep(500);
                }
            }
            _log.info("Temporary file deleted: " + deleted);
            
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

        } catch (Exception e) {
            _log.error("FATAL ERROR during deletion", e);
            return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, e.getMessage());
        } finally {
            // Shutdown executor service properly after all tasks are done
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Process deletion in optimized batches with parallel threads
     */
    private void processDeletionBatches(List<Long> entryIds) {
        int totalBatches = (int) Math.ceil(entryIds.size() / (double) BATCH_SIZE);
        
        // Split into batches
        List<List<Long>> batches = new ArrayList<>();
        for (int i = 0; i < entryIds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entryIds.size());
            batches.add(entryIds.subList(i, end));
        }
        
        _log.info("Created " + batches.size() + " batches for processing");
        
        // Submit all batches to executor
        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            List<Long> batch = batches.get(batchIndex);
            final int currentBatch = batchIndex + 1;
            final int totalBatchesCount = batches.size();
            
            executorService.submit(() -> processSingleBatch(batch, currentBatch, totalBatchesCount));
            
            // Small delay between submissions to prevent overwhelming
            try {
                Thread.sleep(BATCH_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // IMPORTANT: Do NOT shutdown here - let the executor finish
        // The shutdown will happen in the finally block of execute() method
    }

    /**
     * Process a single batch of IDs
     */
    private void processSingleBatch(List<Long> batch, int batchNumber, int totalBatches) {
        _log.info(String.format("Batch %d/%d started with %d records", 
            batchNumber, totalBatches, batch.size()));
        
        int batchDeleted = 0;
        int batchNotFound = 0;
        int batchErrors = 0;
        long batchStartTime = System.currentTimeMillis();
        
        for (Long entryId : batch) {
            long itemStartTime = System.currentTimeMillis();
            boolean processed = false;
            int retryCount = 0;
            
            while (!processed && retryCount < MAX_RETRIES) {
                try {
                    // Check if entry exists
                    ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryId);
                    
                    if (entry != null) {
                        _objectEntryLocalService.deleteObjectEntry(entryId);
                        totalDeleted.incrementAndGet();
                        batchDeleted++;
                        processed = true;
                    } else {
                        totalNotFound.incrementAndGet();
                        batchNotFound++;
                        processed = true;
                    }
                    
                } catch (PortalException e) {
                    retryCount++;
                    if (retryCount >= MAX_RETRIES) {
                        _log.error("Failed to delete ID " + entryId + " after " + MAX_RETRIES + " attempts", e);
                        totalErrors.incrementAndGet();
                        batchErrors++;
                        processed = true;
                    } else {
                        _log.warn("Retry " + retryCount + "/" + MAX_RETRIES + " for ID: " + entryId);
                        try {
                            Thread.sleep(100 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            long itemTime = System.currentTimeMillis() - itemStartTime;
            totalTimeSpent.addAndGet(itemTime);
            
            // Update progress counter
            int processedData = totalProcessed.incrementAndGet();
            
            // Log progress at intervals
            if (processedData % LOG_INTERVAL == 0) {
                logProgress(processedData);
            }
        }
        
        long batchTime = System.currentTimeMillis() - batchStartTime;
        _log.info(String.format(
            "Batch %d/%d completed in %d ms | Deleted: %d | Not Found: %d | Errors: %d", 
            batchNumber, totalBatches, batchTime, batchDeleted, batchNotFound, batchErrors));
    }

    /**
     * Log overall progress
     */
    private void logProgress(int processed) {
        double percent = (processed * 100.0) / (totalProcessed.get() + (totalNotFound.get() + totalErrors.get()));
        double avgTime = totalTimeSpent.get() / (double) processed;
        
        _log.info(String.format(
            "PROGRESS: %d records processed (%.1f%%) | Deleted: %d | Not Found: %d | Errors: %d | Avg: %.2f ms",
            processed, percent, totalDeleted.get(), totalNotFound.get(), totalErrors.get(), avgTime
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
}