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
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.searcher.SearchRequest;
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
import java.util.HashSet;
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

@Component(immediate = true, property = {
		"background.task.executor.class.name=pmsby.bulk.delete.background.task.BulkURNDeleteBackgroundTaskExecutor" }, service = BackgroundTaskExecutor.class)
public class BulkURNDeleteBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

	private static final Log _log = LogFactoryUtil.getLog(BulkURNDeleteBackgroundTaskExecutor.class);

	private static final class Params {
		static final String URN = "uRN";
		static final String STAGE = "stage";
		static final String ENTRY_ID = "entryId";
		private Params() {}
	}

	// Performance configuration
	private static final int PARALLEL_THREADS = 5;
	private static final int BATCH_SIZE = 100; // URNs per batch
	private static final int MAX_RETRIES = 3;
	private static final int ES_SEARCH_SIZE = 5000;

	private ExecutorService executorService;

	// Statistics
	private final AtomicInteger totalURNsProcessed = new AtomicInteger(0);
	private final AtomicInteger totalDuplicatesDeleted = new AtomicInteger(0);
	private final AtomicInteger totalFoundInDB = new AtomicInteger(0);
	private final AtomicInteger totalNotFoundInDB = new AtomicInteger(0);
	private final AtomicInteger totalErrors = new AtomicInteger(0);
	private final AtomicLong totalTimeSpent = new AtomicLong(0);

	// Deletion report storage
	private final Map<String, DeletionInfo> deletionSummary = new ConcurrentHashMap<>();

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private Queries _queries;

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
		long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"), 268219L);

		_log.info("==========================================================");
		_log.info("🚀 DUPLICATE CLEANUP STARTED");
		_log.info("==========================================================");
		_log.info("CSV File: " + fileName);
		_log.info("Object Definition ID: " + objectDefinitionId);
		_log.info("Company ID: " + companyId);

		User user = _userLocalService.getUser(userId);
		_log.info("Started by: " + user.getEmailAddress() + " (ID: " + userId + ")");

		File csvFile = new File(csvFilePath);
		if (!csvFile.exists()) {
			_log.error("CSV file not found: " + csvFilePath);
			return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, "CSV file not found");
		}

		_log.info("CSV File size: " + csvFile.length() + " bytes");
		_log.info("CSV File path: " + csvFile.getAbsolutePath());

		long startTime = System.currentTimeMillis();
		executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);

		try {
			// Step 1: Read unique URNs from CSV
			_log.info("Step 1: Reading URNs from CSV...");
			List<String> uniqueURNs = readUniqueURNsFromCSV(csvFile);
			_log.info("Found " + uniqueURNs.size() + " unique URNs in CSV");

			if (uniqueURNs.isEmpty()) {
				_log.warn("No valid URNs found in CSV file!");
				return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);
			}

			// Display first 10 URNs for debugging
			_log.info("Sample URNs from CSV (first 10):");
			for (int i = 0; i < Math.min(uniqueURNs.size(), 10); i++) {
				_log.info("  " + (i + 1) + ". '" + uniqueURNs.get(i) + "'");
			}

			// Step 2: Verify if these URNs exist in the database
			_log.info("Step 2: Verifying URNs in database...");
			verifyURNsInDatabase(uniqueURNs, companyId, objectDefinitionId);

			// Step 3: Process duplicates in batches
			_log.info("Step 3: Processing duplicates in batches...");
			processDuplicatesInBatches(uniqueURNs, companyId, objectDefinitionId);

			long totalTime = System.currentTimeMillis() - startTime;

			// Final summary
			_log.info("==========================================================");
			_log.info("✅ DUPLICATE CLEANUP COMPLETED");
			_log.info("==========================================================");
			_log.info("URNs processed: " + totalURNsProcessed.get());
			_log.info("URNs found in DB: " + totalFoundInDB.get());
			_log.info("URNs NOT found in DB: " + totalNotFoundInDB.get());
			_log.info("Duplicate records deleted: " + totalDuplicatesDeleted.get());
			_log.info("Errors: " + totalErrors.get());
			_log.info(String.format("Total time: %d min %d sec", totalTime / 60000, (totalTime % 60000) / 1000));

			// Generate report
			generateDeletionReport(csvFile.getParent(), fileName);

			csvFile.delete();

			return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_SUCCESSFUL);

		} catch (Exception e) {
			_log.error("FATAL ERROR during duplicate cleanup", e);
			return new BackgroundTaskResult(BackgroundTaskConstants.STATUS_FAILED, e.getMessage());
		} finally {
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
				try {
					if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
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
	 * Read unique URNs from CSV file
	 */
	private List<String> readUniqueURNsFromCSV(File csvFile) throws Exception {
		Set<String> uniqueURNs = new HashSet<>();
		int lineNumber = 0;
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(csvFile)))) {
			String line;
			boolean isFirstLine = true;
			int columnIndex = -1;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				String[] columns = line.split(",");

				if (isFirstLine) {
					for (int i = 0; i < columns.length; i++) {
						String header = columns[i].trim();
						if ("URN".equalsIgnoreCase(header)) {
							columnIndex = i;
							_log.info("Found URN column at index: " + i);
							break;
						}
					}
					if (columnIndex == -1) {
						_log.warn("URN column not found. Available headers: " + String.join(", ", columns));
						throw new Exception("URN column 'URN' not found in CSV. Available columns: " + String.join(", ", columns));
					}
					isFirstLine = false;
					continue;
				}

				if (columnIndex < columns.length) {
					String urn = columns[columnIndex].trim();
					if (!urn.isEmpty()) {
						uniqueURNs.add(urn);
					}
				}
			}
		}
		
		_log.info("Read " + lineNumber + " lines from CSV, found " + uniqueURNs.size() + " unique URNs");
		return new ArrayList<>(uniqueURNs);
	}

	/**
	 * Verify if URNs exist in the database using Elasticsearch
	 */
	private void verifyURNsInDatabase(List<String> uniqueURNs, long companyId, long objectDefinitionId) {
		int batchSize = 50;
		int found = 0;
		int notFound = 0;
		
		for (int i = 0; i < uniqueURNs.size(); i += batchSize) {
			int end = Math.min(i + batchSize, uniqueURNs.size());
			List<String> batch = uniqueURNs.subList(i, end);
			
			try {
				BooleanQuery orQuery = _queries.booleanQuery();
				for (String urn : batch) {
					orQuery.addShouldQueryClauses(_queries.term("uRN.keyword", urn));
				}
				
				BooleanQuery finalQuery = _queries.booleanQuery();
				finalQuery.addMustQueryClauses(orQuery);
				finalQuery.addMustQueryClauses(_queries.term("objectDefinitionId", objectDefinitionId));
				
				SearchRequest searchRequest = _searchRequestBuilderFactory.builder()
					.emptySearchEnabled(true)
					.query(finalQuery)
					.size(ES_SEARCH_SIZE)
					.build();
				
				SearchResponse response = _searcher.search(searchRequest);
				SearchHits hits = response.getSearchHits();
				
				Set<String> foundURNs = new HashSet<>();
				if (hits != null && hits.getSearchHits() != null) {
					for (SearchHit hit : hits.getSearchHits()) {
						Document doc = hit.getDocument();
						String urn = doc.getString("uRN");
						if (urn != null) {
							foundURNs.add(urn);
						}
					}
				}
				
				found += foundURNs.size();
				notFound += (batch.size() - foundURNs.size());
				
				_log.info("Verification batch: " + foundURNs.size() + " found, " + (batch.size() - foundURNs.size()) + " not found");
				
			} catch (Exception e) {
				_log.error("Error verifying URNs batch", e);
			}
		}
		
		_log.info("Verification complete: " + found + " URNs found in database, " + notFound + " URNs NOT found");
		
		if (found == 0) {
			_log.error("⚠️ CRITICAL: No URNs from CSV were found in the database!");
			_log.error("Please check:");
			_log.error("  1. The URN values in CSV match exactly with database values");
			_log.error("  2. The field name in Elasticsearch is 'uRN' (case sensitive)");
			_log.error("  3. The object definition ID is correct: " + objectDefinitionId);
			_log.error("  4. The records exist in the database and are indexed in Elasticsearch");
		}
	}

	/**
	 * Process duplicates in batches
	 */
	private void processDuplicatesInBatches(List<String> uniqueURNs, long companyId, long objectDefinitionId) {
		int totalBatches = (int) Math.ceil(uniqueURNs.size() / (double) BATCH_SIZE);
		_log.info("Total batches: " + totalBatches + " (batch size: " + BATCH_SIZE + ")");
		
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		
		for (int i = 0; i < uniqueURNs.size(); i += BATCH_SIZE) {
			int end = Math.min(i + BATCH_SIZE, uniqueURNs.size());
			List<String> batch = uniqueURNs.subList(i, end);
			final int batchNumber = (i / BATCH_SIZE) + 1;
			
			futures.add(CompletableFuture.runAsync(() -> 
				processBatch(batch, batchNumber, totalBatches, companyId, objectDefinitionId), executorService));
		}
		
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	}

	/**
	 * Process a single batch of URNs
	 */
	private void processBatch(List<String> batch, int batchNumber, int totalBatches, 
			long companyId, long objectDefinitionId) {
		
		_log.info(String.format("Batch %d/%d started with %d URNs", batchNumber, totalBatches, batch.size()));
		
		int batchDeleted = 0;
		int batchErrors = 0;
		int batchFound = 0;
		long batchStartTime = System.currentTimeMillis();
		
		try {
			// Build OR query for all URNs in this batch
			BooleanQuery orQuery = _queries.booleanQuery();
			for (String urn : batch) {
				orQuery.addShouldQueryClauses(_queries.term("uRN.keyword", urn));
			}
			
			// Add object definition filter
			BooleanQuery finalQuery = _queries.booleanQuery();
			finalQuery.addMustQueryClauses(orQuery);
			finalQuery.addMustQueryClauses(_queries.term("objectDefinitionId", objectDefinitionId));
			finalQuery.addMustQueryClauses(_queries.term("status", 0));
			
			// Execute search
			SearchRequest searchRequest = _searchRequestBuilderFactory.builder()
				.emptySearchEnabled(true)
				.query(finalQuery)
				.size(ES_SEARCH_SIZE)
				.build();
			
			SearchResponse searchResponse = _searcher.search(searchRequest);
			SearchHits searchHits = searchResponse.getSearchHits();
			
			if (searchHits != null && searchHits.getSearchHits() != null) {
				_log.info(String.format("Batch %d: ES returned %d hits", batchNumber, searchHits.getTotalHits()));
				
				// Group results by URN
				Map<String, List<ObjectEntry>> entriesByURN = new HashMap<>();
				
				for (SearchHit hit : searchHits.getSearchHits()) {
					Document document = hit.getDocument();
					if (document != null) {
						long entryId = GetterUtil.getLong(document.getLong("entryClassPK"));
						String urn = document.getString("uRN");
						
						if (entryId > 0 && urn != null) {
							ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryId);
							if (entry != null) {
								entriesByURN.computeIfAbsent(urn, k -> new ArrayList<>()).add(entry);
								batchFound++;
							}
						}
					}
				}
				
				totalFoundInDB.addAndGet(batchFound);
				totalNotFoundInDB.addAndGet(batch.size() - entriesByURN.size());
				
				_log.info(String.format("Batch %d: Found %d URNs in DB with %d total records", 
					batchNumber, entriesByURN.size(), batchFound));
				
				// Process duplicates for each URN
				for (Map.Entry<String, List<ObjectEntry>> entry : entriesByURN.entrySet()) {
					String urn = entry.getKey();
					List<ObjectEntry> entries = entry.getValue();
					
					_log.info(String.format("URN '%s' has %d records in database", urn, entries.size()));
					
					if (entries.size() > 1) {
						// Sort by stage (ascending) then by create date (ascending)
						entries.sort((e1, e2) -> {
							int stage1 = GetterUtil.getInteger(e1.getValues().get(Params.STAGE), 0);
							int stage2 = GetterUtil.getInteger(e2.getValues().get(Params.STAGE), 0);
							if (stage1 != stage2) {
								return Integer.compare(stage1, stage2);
							}
							return e1.getCreateDate().compareTo(e2.getCreateDate());
						});
						
						ObjectEntry keptEntry = entries.get(0);
						_log.info(String.format("URN '%s': Keeping ID: %d (stage: %d, created: %s), will delete %d duplicates", 
							urn, keptEntry.getObjectEntryId(), 
							GetterUtil.getInteger(keptEntry.getValues().get(Params.STAGE), 0),
							keptEntry.getCreateDate(), entries.size() - 1));
						
						for (int i = 1; i < entries.size(); i++) {
							ObjectEntry duplicate = entries.get(i);
							boolean deleted = deleteWithRetry(duplicate.getObjectEntryId());
							
							if (deleted) {
								batchDeleted++;
								totalDuplicatesDeleted.incrementAndGet();
								
								DeletionInfo info = deletionSummary.getOrDefault(urn, new DeletionInfo());
								info.keptEntryId = keptEntry.getObjectEntryId();
								info.deletedEntryIds.add(duplicate.getObjectEntryId());
								deletionSummary.put(urn, info);
								
								_log.info(String.format("✅ Deleted duplicate ID: %d for URN: %s", 
									duplicate.getObjectEntryId(), urn));
							} else {
								batchErrors++;
								totalErrors.incrementAndGet();
								_log.error(String.format("❌ Failed to delete duplicate ID: %d for URN: %s", 
									duplicate.getObjectEntryId(), urn));
							}
						}
					} else if (entries.size() == 1) {
						_log.debug(String.format("URN '%s' has only 1 record - no duplicates to delete", urn));
					}
				}
			} else {
				_log.warn(String.format("Batch %d: No results returned from Elasticsearch", batchNumber));
				totalNotFoundInDB.addAndGet(batch.size());
			}
			
		} catch (Exception e) {
			_log.error("Error processing batch " + batchNumber, e);
			batchErrors = batch.size();
			totalErrors.addAndGet(batchErrors);
		}
		
		totalURNsProcessed.addAndGet(batch.size());
		
		long batchTime = System.currentTimeMillis() - batchStartTime;
		_log.info(String.format("Batch %d/%d completed in %d ms | Found: %d | Deleted: %d | Errors: %d", 
			batchNumber, totalBatches, batchTime, batchFound, batchDeleted, batchErrors));
	}

	/**
	 * Delete with retry logic
	 */
	private boolean deleteWithRetry(long entryId) {
		for (int retry = 0; retry < MAX_RETRIES; retry++) {
			try {
				ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryId);
				if (entry != null) {
					_objectEntryLocalService.deleteObjectEntry(entryId);
					return true;
				}
				return false;
			} catch (PortalException e) {
				if (retry == MAX_RETRIES - 1) {
					_log.error("Failed to delete ID " + entryId + " after " + MAX_RETRIES + " attempts", e);
				} else {
					try {
						Thread.sleep(100 * (retry + 1));
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generate deletion report CSV
	 */
	private void generateDeletionReport(String tempDir, String originalFileName) {
		String reportPath = tempDir + "/deletion_report_" + System.currentTimeMillis() + ".csv";
		
		try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(reportPath))) {
			writer.write("URN,Kept Record ID,Deleted Record IDs,Number of Duplicates Deleted");
			writer.newLine();
			
			for (Map.Entry<String, DeletionInfo> entry : deletionSummary.entrySet()) {
				String urn = entry.getKey();
				DeletionInfo info = entry.getValue();
				String deletedIds = String.join(";", info.deletedEntryIds.stream()
						.map(String::valueOf).collect(java.util.stream.Collectors.toList()));
				
				writer.write(String.format("%s,%d,\"%s\",%d", urn, info.keptEntryId, deletedIds, info.deletedEntryIds.size()));
				writer.newLine();
			}
			
			_log.info("Deletion report generated: " + reportPath);
			
		} catch (Exception e) {
			_log.error("Failed to generate deletion report", e);
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
	 * Inner class to store deletion information
	 */
	private static class DeletionInfo {
		long keptEntryId;
		List<Long> deletedEntryIds = new ArrayList<>();
	}
}