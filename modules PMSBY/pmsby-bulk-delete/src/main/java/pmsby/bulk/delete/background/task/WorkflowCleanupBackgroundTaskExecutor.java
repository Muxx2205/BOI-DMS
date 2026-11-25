package pmsby.bulk.delete.background.task;

import com.liferay.portal.background.task.service.BackgroundTaskLocalService;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = {
        "background.task.executor.class.name=pmsby.bulk.delete.background.task.WorkflowCleanupBackgroundTaskExecutor"
    },
    service = BackgroundTaskExecutor.class
)
public class WorkflowCleanupBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {

    @Override
    public BackgroundTaskExecutor clone() {
        return this;
    }

    @Override
    public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
        Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
        
        long companyId = GetterUtil.getLong(taskContextMap.get("companyId"));
//        long groupId = GetterUtil.getLong(taskContextMap.get("groupId"));
        long siteId = GetterUtil.getLong(taskContextMap.get("siteId"));
        long classNameId = GetterUtil.getLong(taskContextMap.get("classNameId"));
        int batchSize = GetterUtil.getInteger(taskContextMap.get("batchSize"), 1000);
        
        _log.info("Company ID: " + companyId + ", classNameId: "+classNameId+ ", Group ID: " + siteId + ", Batch Size: " + batchSize);
        
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failureCount = new AtomicLong(0);
        
        try {
            // Get classNameId for the object
            
            
            // Get total count using dynamic query
            DynamicQuery countQuery = _workflowInstanceLinkLocalService.dynamicQuery();
            countQuery.add(RestrictionsFactoryUtil.eq("companyId", companyId));
            countQuery.add(RestrictionsFactoryUtil.eq("groupId", siteId));
            countQuery.add(RestrictionsFactoryUtil.eq("classNameId", classNameId));
            
            long totalCount = _workflowInstanceLinkLocalService.dynamicQueryCount(countQuery);
            
            _log.info("Total workflow links to delete: " + totalCount);
            
            if (totalCount == 0) {
                _log.info("No workflow links found to delete");
                return BackgroundTaskResult.SUCCESS;
            }
            
            // Process in batches
            int processed = 0;
            int start = 0;
            
            while (processed < totalCount) {
                // Get batch of workflow links using dynamic query
                DynamicQuery query = _workflowInstanceLinkLocalService.dynamicQuery();
                query.add(RestrictionsFactoryUtil.eq("companyId", companyId));
                query.add(RestrictionsFactoryUtil.eq("groupId", siteId));
                query.add(RestrictionsFactoryUtil.eq("classNameId", classNameId));
                query.setLimit(start, start + batchSize);
                
                List<WorkflowInstanceLink> workflowLinks = 
                    _workflowInstanceLinkLocalService.dynamicQuery(query);
                
                if (workflowLinks.isEmpty()) {
                    break;
                }
                
                // Process this batch in a new transaction
                try {
                    TransactionInvokerUtil.invoke(
                        _transactionConfig,
                        () -> {
                            for (WorkflowInstanceLink link : workflowLinks) {
                                try {
                                    _workflowInstanceLinkLocalService.deleteWorkflowInstanceLink(link);
                                    successCount.incrementAndGet();
                                } catch (Exception e) {
                                    _log.error("Failed to delete workflow link: " + 
                                        link.getWorkflowInstanceLinkId(), e);
                                    failureCount.incrementAndGet();
                                }
                            }
                            return null;
                        }
                    );
                } catch (Throwable e) {
                    _log.error("Batch transaction failed", e);
                    failureCount.addAndGet(workflowLinks.size());
                }
                
                processed += workflowLinks.size();
                start += batchSize;
                
                _log.info(String.format("Progress: %d/%d (Success: %d, Failed: %d)", 
                    processed, totalCount, successCount.get(), failureCount.get()));
                
                // Update background task status (simplified - just update the task context)
                try {
                    backgroundTask.setTaskContextMap(taskContextMap);
                } catch (Exception e) {
                    _log.debug("Could not update background task status", e);
                }
                
                // Small pause to reduce database load
                Thread.sleep(500);
            }
            
            _log.info(String.format("Cleanup completed! Total: %d, Success: %d, Failed: %d", 
                totalCount, successCount.get(), failureCount.get()));
            
            return BackgroundTaskResult.SUCCESS;
            
        } catch (Exception e) {
            _log.error("Fatal error during workflow cleanup", e);
            throw e;
        }
    }
    
    @Override
    public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
        return null;
    }
    
    @Override
    public boolean isSerial() {
        return true; // Run one at a time to avoid database overload
    }
    
    private static final Log _log = LogFactoryUtil.getLog(
        WorkflowCleanupBackgroundTaskExecutor.class);
    
    private static final TransactionConfig _transactionConfig =
        TransactionConfig.Factory.create(
            Propagation.REQUIRES_NEW, new Class<?>[] {Exception.class});
    
    @Reference
    private WorkflowInstanceLinkLocalService _workflowInstanceLinkLocalService;
    
    @Reference
    private ClassNameLocalService _classNameLocalService;
    
    @Reference
    private CompanyLocalService _companyLocalService;
}