package com.boi.headless.rest.common.handler;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalServiceUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AssigmentUsers {
	/*
	 * private static final Log log = LogFactoryUtil.getLog(AssigmentUsers.class);
	 * 
	 * public AssigmentUsers(BOIWorkflowConfiguation boiWorkflowConfiguation) {
	 * this.boiWorkflowConfiguation = boiWorkflowConfiguation; }
	 * 
	 * public void assignmentForAllUsers(ObjectEntry objectEntry, ObjectDefinition
	 * objectDefinition, APIRequestHandler apiRequestHandler) throws Exception {
	 * 
	 * String className = objectDefinition.getClassName(); WorkflowInstanceLink
	 * workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil
	 * .getWorkflowInstanceLink(objectEntry.getCompanyId(), 20121l, className,
	 * objectEntry.getObjectEntryId()); long workflowInstanceId =
	 * workflowInstanceLink.getWorkflowInstanceId(); KaleoInstance kaleoInstance =
	 * KaleoInstanceLocalServiceUtil
	 * .fetchKaleoInstance(workflowInstanceLink.getWorkflowInstanceId()); if
	 * (kaleoInstance == null) { throw new
	 * PortalException("No workflow instance found for classPK=" +
	 * objectEntry.getObjectEntryId()); }
	 * 
	 * log.info("kaleoInstance" + objectEntry.getObjectEntryId());
	 * 
	 * DynamicQuery dynamicQuery =
	 * KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery();
	 * 
	 * dynamicQuery.add(RestrictionsFactoryUtil.eq("kaleoInstanceId",
	 * workflowInstanceId));
	 * 
	 * dynamicQuery.add(RestrictionsFactoryUtil.eq("classPK",
	 * objectEntry.getObjectEntryId()));
	 * dynamicQuery.add(RestrictionsFactoryUtil.eq("className",
	 * objectDefinition.getClassName()));
	 * dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", false));
	 * List<KaleoTaskInstanceToken> allTokens =
	 * KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery(dynamicQuery); if
	 * (allTokens.isEmpty()) { throw new
	 * PortalException("No active task tokens found for this instance."); }
	 * log.info("allTokens " + allTokens.size());
	 * 
	 * KaleoTaskInstanceToken activeToken = allTokens.get(0); String nodeName =
	 * activeToken.getKaleoInstanceToken().getCurrentKaleoNodeName(); String
	 * roleForUsers = nodeNamesWithRole.get(nodeName); String orgForUsers =
	 * nodeNamesWithOrgType.get(nodeName);
	 * 
	 * boolean userHasData =
	 * UserLocalServiceUtil.hasRoleUser(objectEntry.getCompanyId(), roleForUsers,
	 * apiRequestHandler.getUser().getUserId(), true); if (!userHasData) { throw new
	 * PortalException("Not permitted for assignments "); }
	 * 
	 * 
	 * List<KaleoTaskAssignmentInstance> assignments =
	 * KaleoTaskAssignmentInstanceLocalServiceUtil
	 * .getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
	 * 
	 * if (assignments.isEmpty()) { throw new
	 * PortalException(" assignments is blank please check it "); }
	 * log.info("assignments " + assignments.size());
	 * 
	 * if ((!nodeNamesWithRole.containsKey(nodeName)) ||
	 * (!nodeNamesWithOrgType.containsKey(nodeName))) { throw new
	 * PortalException("NodeNames WithRole missing or Node Names With Org Type"); }
	 * 
	 * 
	 * WorkflowInstance workflowInstance =
	 * WorkflowInstanceManagerUtil.getWorkflowInstance(activeToken.getCompanyId(),
	 * workflowInstanceId);
	 * 
	 * Map<String, Serializable> workflowContext =
	 * workflowInstance.getWorkflowContext();
	 * 
	 * ServiceContext serviceContext1 = (ServiceContext)
	 * workflowContext.get("serviceContext");
	 * 
	 * log.info("RoleForUsers " + roleForUsers + " orgForUsers " + orgForUsers);
	 * List<User> desiredUserIds = getMessageFromScript(objectEntry,
	 * objectDefinition, orgForUsers, roleForUsers); log.info("users " +
	 * desiredUserIds.isEmpty()); for (User user : desiredUserIds) { boolean
	 * alreadyAssigned = assignments.stream().anyMatch(a -> a.getAssigneeClassPK()
	 * == user.getUserId()); log.info(user.getScreenName() + " isAssigned " +
	 * alreadyAssigned); if (!alreadyAssigned) {
	 * 
	 * KaleoTaskAssignmentInstance newAssignmentInstance =
	 * KaleoTaskAssignmentInstanceLocalServiceUtil
	 * .addKaleoTaskAssignmentInstance(activeToken.getGroupId(), activeToken,
	 * User.class.getName(), user.getUserId(), serviceContext1);
	 * Indexer<KaleoTaskAssignmentInstance> indexer1 = IndexerRegistryUtil
	 * .nullSafeGetIndexer(KaleoTaskAssignmentInstance.class);
	 * indexer1.reindex(newAssignmentInstance); Indexer<KaleoTaskInstanceToken>
	 * indexer = IndexerRegistryUtil
	 * .nullSafeGetIndexer(KaleoTaskInstanceToken.class);
	 * indexer.reindex(activeToken); log.info("Assignment Done with " +
	 * user.getScreenName()); } }
	 * 
	 * log.info("Removing Assigments");
	 * 
	 * List<KaleoTaskAssignmentInstance> assignmentss =
	 * KaleoTaskAssignmentInstanceLocalServiceUtil
	 * .getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
	 * 
	 * if (!assignmentss.isEmpty()) { log.info("Size of assginment is " +
	 * assignmentss.size()); }
	 * 
	 * Set<Long> desiredUserIdSet =
	 * desiredUserIds.stream().map(User::getUserId).collect(Collectors.toSet());
	 * 
	 * for (KaleoTaskAssignmentInstance assignment : assignmentss) { if
	 * (!desiredUserIdSet.contains(assignment.getAssigneeClassPK())) {
	 * KaleoTaskAssignmentInstanceLocalServiceUtil.deleteKaleoTaskAssignmentInstance
	 * (assignment); log.info("Removed  Assigments from " +
	 * assignment.getAssigneeClassPK()); Indexer<KaleoTaskAssignmentInstance>
	 * indexer1 = IndexerRegistryUtil
	 * .nullSafeGetIndexer(KaleoTaskAssignmentInstance.class);
	 * indexer1.reindex(assignment); Indexer<KaleoTaskInstanceToken> indexer =
	 * IndexerRegistryUtil .nullSafeGetIndexer(KaleoTaskInstanceToken.class);
	 * indexer.reindex(activeToken); } }
	 * 
	 * }
	 * 
	 * private Map<String, String> nodeNamesWithRole = new HashMap<String, String>()
	 * { { // DBD Workflow // put("38d02f3f-85b6-41f2-8c96-d8bf8f9d9eff","Start");
	 * // put("e136fa00-89a2-431f-8bb0-3f43474ac33b","End");
	 * put("104cbe98-1425-41fa-945a-6cbba9308abe", "Branch Checker");
	 * put("9ff5ec94-e7a3-4451-8b5d-7d8f14e87c45", " Branch Maker");
	 * 
	 * // Common Insurance Workflow put("8fad257c-ff91-4046-a4fd-adf44945fd23",
	 * "Branch Maker"); // put("d07b5a1f-ccb4-4f1c-b4ee-64b9008d0971","End");
	 * put("7864e92b-c385-4f96-8369-f6096970d513", "Zone Checker"); //
	 * put("8460c7f6-fcd6-48ce-ab6b-72b211c2a8b3","Start");
	 * put("e7f00783-b869-4a82-ba78-0d34ac45c434", "HO Checker");
	 * 
	 * // Common Workflow // put("d7990efa-c2a9-477a-8ea8-e04e79cae618","End");
	 * put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "HO Checker");
	 * put("139b0ffe-cd24-4721-ad72-15d2830309d4", "Branch Maker"); //
	 * put("d73580fc-4a37-4b2a-84eb-cf713a2ac93d","Start");
	 * 
	 * // Finance Ploe Workflow //
	 * put("d7990efa-c2a9-477a-8ea8-e04e79cae618","End");
	 * put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "Zone Checker");
	 * put("139b0ffe-cd24-4721-ad72-15d2830309d4", "Branch Maker"); //
	 * put("d73580fc-4a37-4b2a-84eb-cf713a2ac93d","Start");
	 * 
	 * // Account Opening put("zoneMaker", "Zone Maker"); put("Branch Maker",
	 * "Branch Maker"); put("resubmit", "Branch Maker"); put("branchChecker",
	 * "Branch Checker"); // put("38e17e9c-07aa-4b19-99fa-14278cdec133","End");
	 * put("zoneChecker", "Zone Checker"); //
	 * put("91145f0f-adfc-4f79-b593-62cefd873879","Start");
	 * 
	 * // FBD Workflow put("b8a45e52-9b60-43fe-8e36-45ec4d68717f", "HO Checker");
	 * put("477c9839-1483-41d4-830e-9c61b25a7514", "Branch Maker");
	 * put("6c991a85-64af-4ad1-89a5-3b919f7e4ab4", "Branch Checker"); //
	 * put("d69c24ae-0382-47f7-b4ab-4c397a645379 ", "Start"); //
	 * put("b5c311dd-4bb9-41c2-ae86-7e1441e27c1f","End");
	 * 
	 * } };
	 * 
	 * private Map<String, String> nodeNamesWithOrgType = new HashMap<String,
	 * String>() { { // DBD Workflow //
	 * put("38d02f3f-85b6-41f2-8c96-d8bf8f9d9eff","Start"); //
	 * put("e136fa00-89a2-431f-8bb0-3f43474ac33b","End");
	 * put("104cbe98-1425-41fa-945a-6cbba9308abe", "BRANCH");
	 * put("9ff5ec94-e7a3-4451-8b5d-7d8f14e87c45", "BRANCH");
	 * 
	 * // Common Insurance Workflow put("8fad257c-ff91-4046-a4fd-adf44945fd23",
	 * "BRANCH"); // put("d07b5a1f-ccb4-4f1c-b4ee-64b9008d0971","End");
	 * put("7864e92b-c385-4f96-8369-f6096970d513", "ZONE"); //
	 * put("8460c7f6-fcd6-48ce-ab6b-72b211c2a8b3","Start");
	 * put("e7f00783-b869-4a82-ba78-0d34ac45c434", "HO");
	 * 
	 * // Common Workflow // put("d7990efa-c2a9-477a-8ea8-e04e79cae618","End");
	 * put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "HO");
	 * put("139b0ffe-cd24-4721-ad72-15d2830309d4", "BRANCH"); //
	 * put("d73580fc-4a37-4b2a-84eb-cf713a2ac93d","Start");
	 * 
	 * // Finance Ploe Workflow //
	 * put("d7990efa-c2a9-477a-8ea8-e04e79cae618","End");
	 * put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "ZONE");
	 * put("139b0ffe-cd24-4721-ad72-15d2830309d4", "BRANCH"); //
	 * put("d73580fc-4a37-4b2a-84eb-cf713a2ac93d","Start");
	 * 
	 * // Account Opening put("zoneMaker", "ZONE"); put("resubmit", "BRANCH");
	 * put("branchChecker", "BRANCH"); //
	 * put("38e17e9c-07aa-4b19-99fa-14278cdec133","End"); put("zoneChecker",
	 * "ZONE"); // put("91145f0f-adfc-4f79-b593-62cefd873879","Start");
	 * 
	 * // FBD Workflow put("b8a45e52-9b60-43fe-8e36-45ec4d68717f", "HO");
	 * put("477c9839-1483-41d4-830e-9c61b25a7514", "BRANCH");
	 * put("6c991a85-64af-4ad1-89a5-3b919f7e4ab4", "BRANCH"); //
	 * put("d69c24ae-0382-47f7-b4ab-4c397a645379 ", "Start"); //
	 * put("b5c311dd-4bb9-41c2-ae86-7e1441e27c1f","End"); } }; private
	 * BOIWorkflowConfiguation boiWorkflowConfiguation;
	 * 
	 * public List<User> getMessageFromScript(ObjectEntry objectEntry,
	 * ObjectDefinition objectDefinition, String organizationType, String roleName)
	 * { // //log.debug("Request Recieved .... for custom workflow handler");
	 * 
	 * // //log.debug("companyId:"+ companyId + " classPk: " + classPk + " //
	 * entryClassName: " + entryClassName // );
	 * 
	 * try {
	 * 
	 * Map<String, Serializable> values = objectEntry.getValues(); long companyId =
	 * objectEntry.getCompanyId();
	 * 
	 * Organization organization = resolveOrganization(organizationType, values,
	 * companyId); log.info("organization " + organization.getName()); if
	 * (Validator.isNull(organization)) {
	 * 
	 * return Collections.emptyList(); }
	 * 
	 * String departmentRole = getDepartmentRoleId(boiWorkflowConfiguation,
	 * objectDefinition.getObjectDefinitionId()); List<User> userFoundInOrganisation
	 * = new ArrayList<>(); log.info("departmentRole " + departmentRole +
	 * " roleName " + roleName); long[] organisationUserId =
	 * OrganizationLocalServiceUtil
	 * .getUserPrimaryKeys(organization.getOrganizationId()); if
	 * (Validator.isNotNull(organisationUserId)) {
	 * log.debug("Total Users founds in same organisations " +
	 * organisationUserId.length);
	 * 
	 * for (long l : organisationUserId) { User user1 =
	 * UserLocalServiceUtil.getUser(l); boolean hasRoleA =
	 * RoleLocalServiceUtil.hasUserRole(user1.getUserId(), companyId, roleName,
	 * true); boolean hasRoleB = RoleLocalServiceUtil.hasUserRole(user1.getUserId(),
	 * companyId, departmentRole, true);
	 * 
	 * boolean hasBoth = hasRoleA && hasRoleB;
	 * 
	 * if (hasBoth) { userFoundInOrganisation.add(user1); } }
	 * log.debug("Based on role user found " + roleName + " : " + departmentRole +
	 * " : " + userFoundInOrganisation.size()); } if
	 * (Validator.isNotNull(userFoundInOrganisation) &&
	 * userFoundInOrganisation.size() == 0) { User user1 =
	 * UserLocalServiceUtil.getUserByScreenName(companyId, "test");
	 * userFoundInOrganisation.add(user1); } return userFoundInOrganisation;
	 * 
	 * 
	 * User user = UserLocalServiceUtil.getUser(userId); String filter =
	 * buildFilter(systemClassName, organization.getOrganizationId());
	 * 
	 * //// log.debug("statusQuery: " + filter);
	 * 
	 * Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page =
	 * getUserDeparments(filter, user); return getObjectToUserList(page, companyId,
	 * roleName);
	 * 
	 * } catch (Exception e) { log.error("Error processing workflow context: ", e);
	 * }
	 * 
	 * return new ArrayList<>(); }
	 * 
	 * public static String getDepartmentRoleId(BOIWorkflowConfiguation
	 * boiWorkflowConfiguation, long classId) {
	 * 
	 * if (classId == boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId()) {
	 * return "CPD Insurance"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.DBDMandateObjectDefinitionId()) {
	 * return "DBD Mandate Cancellation"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.DBDepartmentObjectDefinitionId()) {
	 * return "Digital Banking Department"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.CustomerAccountObjectDefinitionId()) {
	 * return "Account Opening"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.FBDFRBOObjectDefinitionId()) { return
	 * "FBD FE BO"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.FBDObjectDefinitionId()) { return
	 * "FBD"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.FinancePloeObjectDefinitionId()) {
	 * return "Finance Ploe"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId()) {
	 * return "UPI Insurance"; }
	 * 
	 * if (classId == boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId())
	 * { return "FE BO NON Financial"; }
	 * 
	 * return null; }
	 * 
	 * private Organization resolveOrganization(String orgType, Map<String,
	 * Serializable> values, long companyId) { if
	 * (orgType.equalsIgnoreCase(BOIDMSConstants.BRANCH) &&
	 * values.containsKey("branchCode")) { String branchName =
	 * String.valueOf(values.get("branchCode")); // //log.debug("branchName: " +
	 * branchName); return getOrganization(branchName, companyId); }
	 * 
	 * if (orgType.equalsIgnoreCase(BOIDMSConstants.ZONE) &&
	 * values.containsKey("zone")) { String zoneName =
	 * String.valueOf(values.get("zone")); return getOrganization(zoneName,
	 * companyId); }
	 * 
	 * if (orgType.equalsIgnoreCase(BOIDMSConstants.HO)) { return
	 * getOrganization("BOI HO", companyId); }
	 * 
	 * return null; }
	 * 
	 * private Organization getOrganization(String organizationName, long companyId)
	 * { try { return OrganizationLocalServiceUtil.getOrganization(companyId,
	 * organizationName); } catch (PortalException e) { log.error("error... " +
	 * e.getMessage()); return null; }
	 * 
	 * }
	 */
}
