package pmsby.bulk.delete.portlet;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import pmsby.bulk.delete.background.task.BulkDeleteBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.BulkFinacleConsentBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.BulkURNDeleteBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.BulkUpdateAccountNoBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.ExportObjectDataByURNsBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.FinacleConsentSyncBackgroundTaskExecutor;
import pmsby.bulk.delete.background.task.WorkflowCleanupBackgroundTaskExecutor;
import pmsby.bulk.delete.constants.PmsbyBulkDeletePortletKeys;

/**
 * @author shehn
 */
@Component(
	property = {
		"com.liferay.portlet.display-category=category.object",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=PmsbyBulkDelete",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + PmsbyBulkDeletePortletKeys.PMSBYBULKDELETE,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class PmsbyBulkDeletePortlet extends MVCPortlet {

	private static final Log _log = LogFactoryUtil.getLog(PmsbyBulkDeletePortlet.class);

	@Reference
	private BackgroundTaskManager _backgroundTaskManager;

	@ProcessAction(name = "startBulkDelete")
	public void startBulkDelete(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {

		try {
			ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			// Get the uploaded file
			UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);

			File csvFile = uploadPortletRequest.getFile("csvFile");
			String fileName = uploadPortletRequest.getFileName("csvFile");
			String description = ParamUtil.getString(actionRequest, "description");
			String idColumnName = ParamUtil.getString(actionRequest, "idColumnName", "objectEntryId");
			String delimiter = ParamUtil.getString(actionRequest, "delimiter", ",");

			if (csvFile == null || !csvFile.exists()) {
				actionResponse.setRenderParameter("error", "Please upload a valid CSV file");
				return;
			}

			_log.info("File uploaded: " + fileName + ", Size: " + csvFile.length() + " bytes");

			// Prepare task context
			Map<String, Serializable> taskContextMap = HashMapBuilder
					.<String, Serializable>put("csvFilePath", csvFile.getAbsolutePath()).put("fileName", fileName)
					.put("description", description).put("idColumnName", idColumnName).put("delimiter", delimiter)
					.put("companyId", themeDisplay.getCompanyId()).put("userId", themeDisplay.getUserId()).build();

			// Create service context
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			if (serviceContext == null) {
				serviceContext = new ServiceContext();
				serviceContext.setCompanyId(themeDisplay.getCompanyId());
				serviceContext.setUserId(themeDisplay.getUserId());
				serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
			}

			// Create background task
			String taskName = "Delete by IDs - " + fileName + " (" + description + ")";

			_backgroundTaskManager.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), taskName,
					BulkDeleteBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);

			_log.info("Background task created: " + taskName);

			actionResponse.setRenderParameter("success", "File uploaded successfully! Processing " + fileName
					+ " in background. Check Background Tasks for progress.");

		} catch (Exception e) {
			_log.error("Error creating background task", e);
			actionResponse.setRenderParameter("error", "Failed to start deletion: " + e.getMessage());
		}
	}

	@ProcessAction(name = "startBulkWorkflowDelete")
	public void startBulkWorkflowDelete(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {
		String taskName = "Delete by workflow - ";

		String classNameId = ParamUtil.getString(actionRequest, "classNameId", "classNameId");
		String siteId = ParamUtil.getString(actionRequest, "siteId", "siteId");
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
		if (serviceContext == null) {
			serviceContext = new ServiceContext();
			serviceContext.setCompanyId(themeDisplay.getCompanyId());
			serviceContext.setUserId(themeDisplay.getUserId());
			serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
		}

		Map<String, Serializable> taskContextMap = HashMapBuilder.<String, Serializable>put(

				"companyId", themeDisplay.getCompanyId()).put("userId", themeDisplay.getUserId())
				.put("classNameId", classNameId).put("siteId", siteId).build();

		try {
			_backgroundTaskManager.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), taskName,
					WorkflowCleanupBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);
		} catch (PortalException e) {

			e.printStackTrace();
		}
	}

	@ProcessAction(name = "startURNBulkDelete")
	public void startURNBulkDelete(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortletException {

		try {
			ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

			// Get the uploaded file
			UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);

			File csvFile = uploadPortletRequest.getFile("csvURNFile");
			String fileName = uploadPortletRequest.getFileName("csvURNFile");
			String description = ParamUtil.getString(actionRequest, "description");

			if (csvFile == null || !csvFile.exists()) {
				actionResponse.setRenderParameter("error", "Please upload a valid CSV file");
				return;
			}

			_log.info("File uploaded: " + fileName + ", Size: " + csvFile.length() + " bytes");

			// Prepare task context
			Map<String, Serializable> taskContextMap = HashMapBuilder
					.<String, Serializable>put("csvFilePath", csvFile.getAbsolutePath()).put("fileName", fileName)
					.put("description", description).put("companyId", themeDisplay.getCompanyId())
					.put("userId", themeDisplay.getUserId()).build();

			// Create service context
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			if (serviceContext == null) {
				serviceContext = new ServiceContext();
				serviceContext.setCompanyId(themeDisplay.getCompanyId());
				serviceContext.setUserId(themeDisplay.getUserId());
				serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
			}

			// Create background task
			String taskName = "Delete by IDs - " + fileName + " (" + description + ")";

			_backgroundTaskManager.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), taskName,
					BulkURNDeleteBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);

			_log.info("Background task created: " + taskName);

			actionResponse.setRenderParameter("success", "File uploaded successfully! Processing " + fileName
					+ " in background. Check Background Tasks for progress.");

		} catch (Exception e) {
			_log.error("Error creating background task", e);
			actionResponse.setRenderParameter("error", "Failed to start deletion: " + e.getMessage());
		}
	}

	public void startBulkUpdateAccountNo(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		try {
			UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);

			ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
			// Get the uploaded file
			File file = uploadRequest.getFile("csvUpdateFile");
			String fileName = uploadRequest.getFileName("csvURNFile");
			if (file == null || !file.getName().endsWith(".csv")) {
				actionResponse.setRenderParameter("error", "Please upload a valid CSV file");
				return;
			}

			// Get parameters
			String urnColumnName = ParamUtil.getString(actionRequest, "urnColumnName", "URN");
			String accountNoColumnName = ParamUtil.getString(actionRequest, "accountNoColumnName", "accountNumber");
			String delimiter = ParamUtil.getString(actionRequest, "delimiter", ",");
			String description = ParamUtil.getString(actionRequest, "description", "");

			// Create service context
			ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
			if (serviceContext == null) {
				serviceContext = new ServiceContext();
				serviceContext.setCompanyId(themeDisplay.getCompanyId());
				serviceContext.setUserId(themeDisplay.getUserId());
				serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
			}
			Map<String, Serializable> taskContextMap = HashMapBuilder
					.<String, Serializable>put("csvFilePath", file.getAbsolutePath()).put("fileName", fileName)
					.put("description", description).put("urnColumnName", urnColumnName)
					.put("accountNoColumnName", accountNoColumnName).put("delimiter", delimiter)
					.put("companyId", themeDisplay.getCompanyId()).put("userId", themeDisplay.getUserId()).build();

			// Create background task
			String taskName = "Delete by IDs - " + fileName + " (" + description + ")";

			_backgroundTaskManager.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), taskName,
					BulkUpdateAccountNoBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);

			// Add background task

			actionResponse.setRenderParameter("success", "File uploaded successfully! Processing " + fileName
					+ " in background. Check Background Tasks for progress.");

		} catch (Exception e) {
			_log.error("Error starting bulk update", e);
			actionResponse.setRenderParameter("error", "Failed to start update: " + e.getMessage());
		}
	}

	
	@ProcessAction(name = "exportObjectDataByURNs")
	public void exportObjectDataByURNs(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
	    try {
	        UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
	        ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
	        
	        // Get the uploaded file
	        File file = uploadRequest.getFile("exportCSVFile");
	        String fileName = uploadRequest.getFileName("exportCSVFile");
	        
	        if (file == null || !file.getName().endsWith(".csv")) {
	            actionResponse.setRenderParameter("error", "Please upload a valid CSV file");
	            return;
	        }
	        
	        // Get parameters
	        String urnColumnName = ParamUtil.getString(actionRequest, "urnColumnName", "URN");
	        String delimiter = ParamUtil.getString(actionRequest, "exportDelimiter", ",");
	        String exportFileName = ParamUtil.getString(actionRequest, "exportFileName", "");
	        String description = ParamUtil.getString(actionRequest, "exportDescription", "");
	        String[] selectedFields = ParamUtil.getParameterValues(actionRequest, "exportFields");
	        
	        // Check if "Export All Fields" is selected (no specific fields selected)
	        boolean exportAllFields = (selectedFields == null || selectedFields.length == 0);
	        
	        // Create service context
	        ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
	        if (serviceContext == null) {
	            serviceContext = new ServiceContext();
	            serviceContext.setCompanyId(themeDisplay.getCompanyId());
	            serviceContext.setUserId(themeDisplay.getUserId());
	            serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
	        }
	        
	        // Build task context map
	        Map<String, Serializable> taskContextMap = HashMapBuilder
	                .<String, Serializable>put("csvFilePath", file.getAbsolutePath())
	                .put("fileName", fileName)
	                .put("description", description)
	                .put("urnColumnName", urnColumnName)
	                .put("delimiter", delimiter)
	                .put("exportFileName", exportFileName)
	                .put("exportAllFields", exportAllFields)
	                .put("selectedFields", selectedFields != null ? String.join(",", selectedFields) : "")
	                .put("companyId", themeDisplay.getCompanyId())
	                .put("userId", themeDisplay.getUserId())
	                .put("objectDefinitionId", getObjectDefinitionId()) // Add your object definition ID
	                .build();
	        
	        // Create background task
	        String taskName = "Export by URNs - " + fileName + " (" + description + ")";
	        
	        _backgroundTaskManager.addBackgroundTask(
	            themeDisplay.getUserId(), 
	            themeDisplay.getScopeGroupId(), 
	            taskName,
	            ExportObjectDataByURNsBackgroundTaskExecutor.class.getName(), 
	            taskContextMap, 
	            serviceContext
	        );
	        
	        actionResponse.setRenderParameter("success", 
	            "File uploaded successfully! Processing " + fileName + 
	            " in background. You will be notified when the export is ready for download."
	        );
	        
	    } catch (Exception e) {
	        _log.error("Error starting export by URNs", e);
	        actionResponse.setRenderParameter("error", "Failed to start export: " + e.getMessage());
	    }
	}
	
	@ProcessAction(name = "startFinacleConsentSync")
	public void startFinacleConsentSync(ActionRequest actionRequest, ActionResponse actionResponse) {
	    try {
	        ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
	        
	        String insuranceType = ParamUtil.getString(actionRequest, "insuranceType", "JBY");
	        long objectDefinitionId = ParamUtil.getLong(actionRequest, "objectDefinitionId", 395809L);
	        
	        Map<String, Serializable> taskContextMap = new HashMap<>();
	        taskContextMap.put("companyId", themeDisplay.getCompanyId());
	        taskContextMap.put("groupId", themeDisplay.getScopeGroupId());
	        taskContextMap.put("userId", themeDisplay.getUserId());
	        taskContextMap.put("insuranceType", insuranceType);
	        taskContextMap.put("objectDefinitionId", objectDefinitionId);
	        
	        ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
	        if (serviceContext == null) {
	            serviceContext = new ServiceContext();
	            serviceContext.setCompanyId(themeDisplay.getCompanyId());
	            serviceContext.setUserId(themeDisplay.getUserId());
	            serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
	        }
	        
	        String taskName = "Finacle Consent Sync - " + insuranceType;
	        
	        _backgroundTaskManager.addBackgroundTask(
	            themeDisplay.getUserId(),
	            themeDisplay.getScopeGroupId(),
	            taskName,
	            FinacleConsentSyncBackgroundTaskExecutor.class.getName(),
	            taskContextMap,
	            serviceContext
	        );
	        
	        actionResponse.setRenderParameter("success", "Finacle Consent Sync started for " + insuranceType + "! Check Control Panel → System → Background Tasks for progress.");
	        
	    } catch (Exception e) {
	        _log.error("Error starting Finacle consent sync", e);
	        actionResponse.setRenderParameter("error", "Failed to start: " + e.getMessage());
	    }
	}

	private long getObjectDefinitionId() {
	    // Return your object definition ID
	    // You can fetch it dynamically or hardcode
	    return 268219L; // Replace with your actual object definition ID
	}
}