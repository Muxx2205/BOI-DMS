package pmsby.bulk.delete.dispatch;

import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskConstants;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = {
        "dispatch.task.executor.name=finacle-consent-Dispatcher",
        "dispatch.task.executor.type=finacle-consent-Dispatcher"
    },
    service = DispatchTaskExecutor.class
)
public class FinacleDispatchExecutor extends BaseDispatchTaskExecutor {

    private static final Log _log = LogFactoryUtil.getLog(FinacleDispatchExecutor.class);

    @Reference
    private BackgroundTaskManager _backgroundTaskManager;

    @Reference
    private UserLocalService _userLocalService;

    @Reference
    private Portal _portal;

    @Override
    public String getName() {
        return "finacle-consent-Dispatcher";
    }

    @Override
    public void doExecute(
            DispatchTrigger dispatchTrigger,
            DispatchTaskExecutorOutput output) throws Exception {

        long companyId = dispatchTrigger.getCompanyId();
        String triggerName = dispatchTrigger.getName();

        _log.info("==========================================================");
        _log.info("Dispatch trigger FIRED: " + triggerName);
        _log.info("Time: " + new java.util.Date());
        _log.info("Company ID: " + companyId);
        _log.info("==========================================================");

        // Use company ID as groupId (0 is often valid for company-scoped tasks)
        long groupId = 0;
        _log.info("Using groupId: " + groupId);

        User adminUser;
        try {
            adminUser = _userLocalService.getUserByEmailAddress(companyId, "dmsl2.support@bankofindia.co.in");
            if (adminUser == null) {
                _log.error("Admin user not found for email: dmsl2.support@bankofindia.co.in");
                output.setError("Admin user not found");
                return;
            }
        } catch (Exception e) {
            _log.error("Failed to get admin user", e);
            output.setError("User lookup error: " + e.getMessage());
            return;
        }

        long userId = adminUser.getUserId();
        _log.info("Resolved user ID: " + userId);

        // Check for existing background task
        List<BackgroundTask> existingTasks;
        try {
            existingTasks = _backgroundTaskManager.getBackgroundTasks(
                    groupId,
                    "pmsby.bulk.delete.background.task.FinacleConsentTask",
                    BackgroundTaskConstants.STATUS_IN_PROGRESS);
            if (existingTasks != null && !existingTasks.isEmpty()) {
                _log.info("Background task already running. Skipping.");
                output.setOutput("Task already running");
                return;
            }
        } catch (Exception e) {
            _log.error("Error checking existing tasks", e);
            output.setError("Error checking tasks: " + e.getMessage());
            return;
        }

        // Prepare task context
        Map<String, Serializable> taskContext = new HashMap<>();
        taskContext.put("companyId", companyId);
        taskContext.put("groupId", groupId);
        taskContext.put("userId", userId);
        taskContext.put("objectDefinitionId", 62979176L);
        taskContext.put("stage", 3);   // or 0 for testing

        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setCompanyId(companyId);
        serviceContext.setUserId(userId);
        serviceContext.setScopeGroupId(groupId);

        String taskName = "Finacle Consent Sync - " + triggerName + " - " +
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        try {
            _log.info("Adding background task with name: " + taskName);
            BackgroundTask task = _backgroundTaskManager.addBackgroundTask(
                    userId, groupId, taskName,
                    "pmsby.bulk.delete.background.task.FinacleConsentTask",
                    taskContext, serviceContext);
            _log.info("Background task created successfully with ID: " + (task != null ? task.getBackgroundTaskId() : "null"));
            output.setOutput("Task started with ID: " + (task != null ? task.getBackgroundTaskId() : "unknown"));
        } catch (Exception e) {
            _log.error("Failed to add background task", e);
            output.setError("Error adding task: " + e.getMessage());
            throw e;
        }
    }
}