
package com.boi.headless.rest.common.handler;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, configurationPid = {
		"com.boi.headless.rest.common.application.BOIWorkflowConfiguation" }, service = UserHandler.class)
public class UserHandler {

	private static final Log log = LogFactoryUtil.getLog(UserHandler.class);

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);

	}

	private BOIWorkflowConfiguation boiWorkflowConfiguation;
	
	private void logStepTime(String stepName, long last) {
		long now = System.currentTimeMillis();
		log.info(stepName + " execution time: " + (now - last) + " ms");
		// update the last reference
		this.last = now;
	}

	private long last;

	public List<User> getMessageFromScript(Map<String, Serializable> workflowContext, String organizationType,
			String roleName) {
		log.debug("Request Recieved .... for custom workflow handler");
	    long start = System.currentTimeMillis();
	    long last = start;
	    logStepTime("Call Started...  " , last);
		if (!hasRequiredWorkflowContextKeys(workflowContext)) {
			return Collections.emptyList();
		}
		 logStepTime("HasRequiredWorkflowContextKeys " , last);
		long companyId = parseLong(workflowContext.get("companyId"));
		long classPk = parseLong(workflowContext.get("entryClassPK"));
		String entryClassName = String.valueOf(workflowContext.get("entryClassName"));
		long userId = parseLong(workflowContext.get("userId"));

		log.debug("companyId: " + companyId + " classPk: " + classPk + " entryClassName: " + entryClassName
				+ " userId: " + userId);

		try {
			logStepTime("Script Started " , last);
			ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
			Map<String, Serializable> values = objectEntry.getValues();

			ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil
					.getObjectDefinition(objectEntry.getObjectDefinitionId());
			if (!objectDefinition.getClassName().equalsIgnoreCase(entryClassName)) {
				log.debug("objectDefinition.getClassName().equalsIgnoreCase(entryClassName) true"  );
				return Collections.emptyList();
			}
			log.debug(" organizationType  "+organizationType);
			Organization organization = resolveOrganization(organizationType, values, companyId);
			
			if (Validator.isNull(organization)) {
				log.debug(" organizationType Not Found!! ");
				return Collections.emptyList();
			}

			log.debug("organization: " + organization.getName());

			String departmentRole = getDepartmentRoleId(boiWorkflowConfiguation,
					objectDefinition.getObjectDefinitionId());
			List<User> userFoundInOrganisation = new ArrayList<>();
			long[] organisationUserId = OrganizationLocalServiceUtil
					.getUserPrimaryKeys(organization.getOrganizationId());
			if (Validator.isNotNull(organisationUserId)) {
				log.debug("Total Users founds in same organisations " + organisationUserId.length);
				/*
				 * String[] rolesToCheck = new String[2]; rolesToCheck[0] = roleName;
				 * rolesToCheck[1] = departmentRole;
				 */
				for (long l : organisationUserId) {
					User user1 = UserLocalServiceUtil.getUser(l);
				//	boolean result = RoleLocalServiceUtil.hasUserRoles(user1.getUserId(), companyId, rolesToCheck,true);
					boolean hasRoleA = RoleLocalServiceUtil.hasUserRole(user1.getUserId(), companyId, roleName, true);
					boolean hasRoleB = RoleLocalServiceUtil.hasUserRole(user1.getUserId(), companyId, departmentRole, true);

					boolean hasBoth = hasRoleA && hasRoleB;

					if (hasBoth) {
						userFoundInOrganisation.add(user1);
					}
				}
				log.debug("Based on role user found "+roleName+" : "+departmentRole+" : " + userFoundInOrganisation.size());
			}
			if(Validator.isNotNull(userFoundInOrganisation) && userFoundInOrganisation.size()==0) {
				User user1 = UserLocalServiceUtil.getUserByScreenName(companyId, "test");
				userFoundInOrganisation.add(user1);
			}
			logStepTime("DB Queries Ended with 100% excution " , last);
			//log.debug("sleep for 10min ");
			//Thread.sleep(600020);
		//	log.debug("awak--up ");
			
			if(userFoundInOrganisation.size()>0) {
				values.put("customStatusByHandler", ("Pending with "+roleName).toLowerCase());
				updateData(objectEntry, values);
			}
			
			
			return userFoundInOrganisation;
			
			/*
			 * String filter = buildFilter(systemClassName,
			 * organization.getOrganizationId());
			 * 
			 * log.debug("statusQuery: " + filter);
			 * 
			 * Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page =
			 * getUserDeparments(filter, user); return getObjectToUserList(page, companyId,
			 * roleName);
			 */
		} catch (Exception e) {
			log.error("Error processing workflow context: ", e);
		}
		logStepTime("Script Ended with Exception " , last);
		return new ArrayList<>();
	}
	
	
	
	
	public com.liferay.object.model.ObjectEntry updateData(ObjectEntry objectEntry,Map<String, Serializable> values) throws Exception {

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(objectEntry.getCompanyId());
		serviceContext.setUserId(objectEntry.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(
			    null,
			    0L,
			    objectEntry.getUserId(),
			    objectEntry.getObjectDefinitionId(),
			    0L,
			    values,
			    serviceContext
			);
		
		return ObjectEntryLocalServiceUtil.getObjectEntry(objectEntry.getObjectEntryId());
	}
	
	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;
	
	
	
	
	public static String getDepartmentRoleId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {
		
	    if (classId == boiWorkflowConfiguation.ConsentObjectDefinitionId()) {
	    	return "RRB Consent";
	    }

	    return null;
	}
	
	
	private boolean hasRequiredWorkflowContextKeys(Map<String, Serializable> context) {
	    return context.containsKey("companyId") &&
	           context.containsKey("entryClassName") &&
	           context.containsKey("entryClassPK");
	}

	private long parseLong(Object value) {
	    return Long.parseLong(String.valueOf(value));
	}

	private Organization resolveOrganization(String orgType, Map<String, Serializable> values, long companyId) {
	    if (orgType.equalsIgnoreCase(BOIDMSConstants.BRANCH) && values.containsKey("branchCode")) {
	        String branchName = String.valueOf(values.get("branchCode"));
	        log.debug("branchName: " + branchName);
	        return getOrganization(branchName, companyId);
	    }

	    if (orgType.equalsIgnoreCase(BOIDMSConstants.REGIONAL_OFFICE) && values.containsKey("REGIONAL OFFICE")) {
	        String zoneName = String.valueOf(values.get("REGIONAL OFFICE"));
	        return getOrganization(zoneName, companyId);
	    }

	    if (orgType.equalsIgnoreCase(BOIDMSConstants.HO)) {
	        return getOrganization("BOI HO", companyId);
	    }
	    if (orgType.equalsIgnoreCase("Organization")) {
	        return getOrganization("BOI", companyId);
	    }

	    return null;
	}

	 
 

	private Organization getOrganization(String organizationName, long companyId) {
		try {
			return OrganizationLocalServiceUtil.getOrganization(companyId, organizationName);
		} catch (PortalException e) {
			log.error("error... " + e.getMessage());
			return null;
		}

	}

	 

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
}
