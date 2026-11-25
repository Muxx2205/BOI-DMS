package com.boi.headless.rest.common.application;

import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CustomWorkflowLogs {

	private User user;

	public CustomWorkflowLogs(User user) {
		this.user = user;
	}

	public boolean createWorkflowEntryLogs(com.liferay.object.model.ObjectEntry objectEntry, String status,
			long objectDefinitionId) {

		Map<String, Serializable> values = new HashMap<>();
		values.put("attachment", StringPool.BLANK);
		values.put("customAction",
				"Mark as " + (objectEntry.getStatus() == WorkflowConstants.STATUS_DRAFT ? "Draft" : "Export"));
		values.put("objectEntryId", objectEntry.getObjectEntryId());

		values.put("remarks", "Created");
		values.put("transitionName", "Created");
		values.put("workflowTaskId", 0l);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		try {
			// FIX: Correct parameter order
			// addOrUpdateObjectEntry(externalReferenceCode, groupId, userId, objectDefinitionId, objectEntryFolderId, values, serviceContext)
			ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
					null,                    // externalReferenceCode
					0L,                      // groupId
					user.getUserId(),        // userId
					objectDefinitionId,      // objectDefinitionId
					0L,                      // objectEntryFolderId
					values,                  // values
					serviceContext);         // serviceContext
			return true;
		} catch (PortalException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createWorkflowEntryLogs(long objectEntryId, String action, String remarks, long objectDefinitionId) {

		Map<String, Serializable> values = new HashMap<>();
		values.put("attachment", StringPool.BLANK);
		values.put("customAction", action);
		values.put("objectEntryId", objectEntryId);

		values.put("remarks", remarks);
		values.put("transitionName", "Created");
		values.put("workflowTaskId", 0l);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		try {
			// FIX: Correct parameter order
			ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
					null,                    // externalReferenceCode
					0L,                      // groupId
					user.getUserId(),        // userId
					objectDefinitionId,      // objectDefinitionId
					0L,                      // objectEntryFolderId
					values,                  // values
					serviceContext);         // serviceContext
			return true;
		} catch (PortalException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Account Opening

	public boolean createAccountWorkflowEntryLogs(com.liferay.object.model.ObjectEntry objectEntry, String customAction,
			int currentStatus,String remarks, long objectDefinitionId) {

		Map<String, Serializable> values = new HashMap<>();
		values.put("attachment", StringPool.BLANK);
		values.put("customAction", customAction);
		values.put("currentStatus", currentStatus);
		values.put("objectEntryId", objectEntry.getObjectEntryId());

		values.put("remarks",  Validator.isBlank(remarks)?"Resubmit":remarks);
		values.put("transitionName", "Created");
		values.put("workflowTaskId", 0l);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		try {
			// FIX: Correct parameter order
			ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
					null,                    // externalReferenceCode
					0L,                      // groupId
					user.getUserId(),        // userId
					objectDefinitionId,      // objectDefinitionId
					0L,                      // objectEntryFolderId
					values,                  // values
					serviceContext);         // serviceContext
			return true;
		} catch (PortalException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean createAccountWorkflowEntryLogs(long objectEntryId, String action, String remarks,
			String currentStatus, long objectDefinitionId) {

		Map<String, Serializable> values = new HashMap<>();
		values.put("attachment", StringPool.BLANK);
		values.put("customAction", action);
		values.put("currentStatus", currentStatus);
		values.put("cuAction", action);
		values.put("objectEntryId", objectEntryId);

		values.put("remarks", remarks);
		values.put("transitionName", "Created");
		values.put("workflowTaskId", 0l);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		try {
			// FIX: Correct parameter order
			ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
					null,                    // externalReferenceCode
					0L,                      // groupId
					user.getUserId(),        // userId
					objectDefinitionId,      // objectDefinitionId
					0L,                      // objectEntryFolderId
					values,                  // values
					serviceContext);         // serviceContext
			return true;
		} catch (PortalException e) {
			e.printStackTrace();
		}
		return false;
	}

}