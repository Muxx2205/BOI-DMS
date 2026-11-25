package com.boi.liferay.dms.assignment.portlet;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserModel;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalServiceUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   property = {"javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "mvc.command.name=saveIt"},
   service = {MVCActionCommand.class}
)
public class AssignmentUserAction implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(AssignmentUserAction.class);
   @Reference
   private WorkflowTaskManager _workflowTaskManager;
   private Map<String, String> nodeNamesWithRole = new HashMap<String, String>() {
      {
         this.put("104cbe98-1425-41fa-945a-6cbba9308abe", "Branch Checker");
         this.put("9ff5ec94-e7a3-4451-8b5d-7d8f14e87c45", " Branch Maker");
         this.put("8fad257c-ff91-4046-a4fd-adf44945fd23", "Branch Maker");
         this.put("7864e92b-c385-4f96-8369-f6096970d513", "Zone Checker");
         this.put("e7f00783-b869-4a82-ba78-0d34ac45c434", "HO Checker");
         this.put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "HO Checker");
         this.put("139b0ffe-cd24-4721-ad72-15d2830309d4", "Branch Maker");
         this.put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "Zone Checker");
         this.put("139b0ffe-cd24-4721-ad72-15d2830309d4", "Branch Maker");
         this.put("zoneMaker", "Zone Maker");
         this.put("Branch Maker", "Branch Maker");
         this.put("branchChecker", "Branch Checker");
         this.put("zoneChecker", "Zone Checker");
         this.put("b8a45e52-9b60-43fe-8e36-45ec4d68717f", "HO Checker");
         this.put("477c9839-1483-41d4-830e-9c61b25a7514", "Branch Maker");
         this.put("6c991a85-64af-4ad1-89a5-3b919f7e4ab4", "Branch Checker");
      }
   };
   private Map<String, String> nodeNamesWithOrgType = new HashMap<String, String>() {
      {
         this.put("104cbe98-1425-41fa-945a-6cbba9308abe", "BRANCH");
         this.put("9ff5ec94-e7a3-4451-8b5d-7d8f14e87c45", "BRANCH");
         this.put("8fad257c-ff91-4046-a4fd-adf44945fd23", "BRANCH");
         this.put("7864e92b-c385-4f96-8369-f6096970d513", "ZONE");
         this.put("e7f00783-b869-4a82-ba78-0d34ac45c434", "HO");
         this.put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "HO");
         this.put("139b0ffe-cd24-4721-ad72-15d2830309d4", "BRANCH");
         this.put("a96d8a6b-809c-48bc-b803-86d3ec3a5667", "ZONE");
         this.put("139b0ffe-cd24-4721-ad72-15d2830309d4", "BRANCH");
         this.put("zoneMaker", "ZONE");
         this.put("resubmit", "BRANCH");
         this.put("branchChecker", "BRANCH");
         this.put("zoneChecker", "ZONE");
         this.put("b8a45e52-9b60-43fe-8e36-45ec4d68717f", "HO");
         this.put("477c9839-1483-41d4-830e-9c61b25a7514", "BRANCH");
         this.put("6c991a85-64af-4ad1-89a5-3b919f7e4ab4", "BRANCH");
      }
   };
   private BOIWorkflowConfiguation boiWorkflowConfiguation;
   private DTOConverterRegistry _dtoConverterRegistry;
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

   private void assignmentFor(User fromUser, User toUser) {
      try {
         for(WorkflowTask task : this._workflowTaskManager.getWorkflowTasksByUser(fromUser.getCompanyId(), fromUser.getUserId(), false, 0, 50, (OrderByComparator)null)) {
            if (!task.isCompleted()) {
               log.info("isAssignedToSingleUser Tasks " + task.isAssignedToSingleUser());
               log.info("getAssigneeUserId Tasks " + task.getAssigneeUserId());
               log.info("getType Tasks " + task.getType());
               log.info("isAsynchronous Tasks " + task.isAsynchronous());
               log.info("getUserName Tasks " + task.getUserName());
               KaleoTaskInstanceToken kaleoTaskInstanceToken = KaleoTaskInstanceTokenLocalServiceUtil.getKaleoTaskInstanceToken(task.getWorkflowTaskId());
               WorkflowInstance workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(fromUser.getCompanyId(), task.getWorkflowInstanceId());
               Map<String, Serializable> workflowContext = workflowInstance.getWorkflowContext();
               ServiceContext serviceContext = (ServiceContext)workflowContext.get("serviceContext");
               KaleoTaskInstanceTokenLocalServiceUtil.assignKaleoTaskInstanceToken(kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(), "com.liferay.portal.kernel.model.User", toUser.getUserId(), workflowContext, serviceContext);
               log.info(" Assignment Done ");
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String classPks = ParamUtil.getString(actionRequest, "classPk");
      Long[] classPKLongArray = (Long[])Arrays.stream(classPks.split(",")).map(String::trim).map(Long::parseLong).toArray((x$0) -> new Long[x$0]);

      for(Long long1 : classPKLongArray) {
         try {
            this.assignmentForAllUsers(long1);
         } catch (Exception e1) {
            log.info(e1.getMessage());
         }
      }

      SessionMessages.add(actionRequest, "requestProcessed");
      return false;
   }

   public boolean processAction1(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String fromUser = ParamUtil.getString(actionRequest, "fromUser");
      long classPk = ParamUtil.getLong(actionRequest, "classPk");
      String assignmentId = ParamUtil.getString(actionRequest, "assignmentId");
      log.info("organizationId " + classPk);
      log.info("assignmentId " + assignmentId);

      try {
         com.liferay.object.model.ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
         User user = UserLocalServiceUtil.getUser(420111L);
         User userAssignment = UserLocalServiceUtil.getUserByScreenName(user.getCompanyId(), assignmentId);
         Log var10000 = log;
         long var10001 = user.getUserId();
         var10000.info("Admin User " + var10001 + " classNameId " + objectDefinition.getClassName());
         WorkflowInstanceLink workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil.getWorkflowInstanceLink(user.getCompanyId(), 20121L, objectDefinition.getClassName(), classPk);
         long workflowInstanceId = workflowInstanceLink.getWorkflowInstanceId();
         Long userId = null;
         Boolean completed = Boolean.FALSE;
         int start = 0;
         int end = Integer.MAX_VALUE;
         OrderByComparator<WorkflowTask> orderByComparator = null;
         log.info("Fetching Tasks ");
         List<WorkflowTask> tasks = WorkflowTaskManagerUtil.getWorkflowTasksByWorkflowInstance(user.getCompanyId(), userId, workflowInstanceId, completed, start, end, orderByComparator);
         ServiceContext serviceContext = ServiceContextFactory.getInstance(actionRequest);
         log.info("tasks list found " + tasks.size());

         for(WorkflowTask task : tasks) {
            if (!task.isCompleted()) {
               log.info("isAssignedToSingleUser Tasks " + task.isAssignedToSingleUser());
               log.info("getAssigneeUserId Tasks " + task.getAssigneeUserId());
               log.info("getType Tasks " + task.getType());
               log.info("isAsynchronous Tasks " + task.isAsynchronous());
               log.info("getUserName Tasks " + task.getUserName());
               KaleoTaskInstanceToken kaleoTaskInstanceToken = KaleoTaskInstanceTokenLocalServiceUtil.getKaleoTaskInstanceToken(task.getWorkflowTaskId());
               WorkflowInstance workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(user.getCompanyId(), workflowInstanceId);
               Map<String, Serializable> workflowContext = workflowInstance.getWorkflowContext();
               KaleoTaskInstanceTokenLocalServiceUtil.assignKaleoTaskInstanceToken(kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(), "com.liferay.portal.kernel.model.User", userAssignment.getUserId(), workflowContext, serviceContext);
               log.info(" Assignment Done ");
            }
         }
      } catch (Exception e) {
         log.info("Error " + e.getMessage());
         e.printStackTrace();
      }

      return false;
   }

   private void assignmentForAllUsers(long classPk) throws Exception {
      com.liferay.object.model.ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
      String className = objectDefinition.getClassName();
      WorkflowInstanceLink workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil.getWorkflowInstanceLink(objectEntry.getCompanyId(), 20121L, className, classPk);
      long workflowInstanceId = workflowInstanceLink.getWorkflowInstanceId();
      KaleoInstance kaleoInstance = KaleoInstanceLocalServiceUtil.fetchKaleoInstance(workflowInstanceLink.getWorkflowInstanceId());
      if (kaleoInstance == null) {
         throw new PortalException("No workflow instance found for classPK=" + objectEntry.getObjectEntryId());
      } else {
         log.info("kaleoInstance" + objectEntry.getObjectEntryId());
         DynamicQuery dynamicQuery = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery();
         dynamicQuery.add(RestrictionsFactoryUtil.eq("kaleoInstanceId", workflowInstanceId));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("classPK", objectEntry.getObjectEntryId()));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("className", objectDefinition.getClassName()));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", false));
         List<KaleoTaskInstanceToken> allTokens = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery(dynamicQuery);
         if (allTokens.isEmpty()) {
            throw new PortalException("No active task tokens found for this instance.");
         } else {
            log.info("allTokens " + allTokens.size());
            KaleoTaskInstanceToken activeToken = (KaleoTaskInstanceToken)allTokens.get(0);
            List<KaleoTaskAssignmentInstance> assignments = KaleoTaskAssignmentInstanceLocalServiceUtil.getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
            String nodeName = activeToken.getKaleoInstanceToken().getCurrentKaleoNodeName();
            if (assignments.isEmpty()) {
               throw new PortalException(" assignments is blank please check it ");
            } else {
               log.info("assignments " + assignments.size());
               if (this.nodeNamesWithRole.containsKey(nodeName) && this.nodeNamesWithOrgType.containsKey(nodeName)) {
                  WorkflowInstance workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(activeToken.getCompanyId(), workflowInstanceId);
                  Map<String, Serializable> workflowContext = workflowInstance.getWorkflowContext();
                  ServiceContext serviceContext1 = (ServiceContext)workflowContext.get("serviceContext");
                  String roleForUsers = (String)this.nodeNamesWithRole.get(nodeName);
                  String orgForUsers = (String)this.nodeNamesWithOrgType.get(nodeName);
                  log.info("RoleForUsers " + roleForUsers + " orgForUsers " + orgForUsers);
                  List<User> desiredUserIds = this.getMessageFromScript(classPk, orgForUsers, roleForUsers);
                  log.info("users " + desiredUserIds.isEmpty());

                  for(User user : desiredUserIds) {
                     boolean alreadyAssigned = assignments.stream().anyMatch((a) -> a.getAssigneeClassPK() == user.getUserId());
                     Log var10000 = log;
                     String var10001 = user.getScreenName();
                     var10000.info(var10001 + " isAssigned " + alreadyAssigned);
                     if (!alreadyAssigned) {
                        KaleoTaskAssignmentInstance newAssignmentInstance = KaleoTaskAssignmentInstanceLocalServiceUtil.addKaleoTaskAssignmentInstance(activeToken.getGroupId(), activeToken, User.class.getName(), user.getUserId(), serviceContext1);
                        Indexer<KaleoTaskAssignmentInstance> indexer1 = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskAssignmentInstance.class);
                        indexer1.reindex(newAssignmentInstance);
                        Indexer<KaleoTaskInstanceToken> indexer = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskInstanceToken.class);
                        indexer.reindex(activeToken);
                        log.info("Assignment Done with " + user.getScreenName());
                     }
                  }

                  log.info("Removing Assigments");
                  List<KaleoTaskAssignmentInstance> assignmentss = KaleoTaskAssignmentInstanceLocalServiceUtil.getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
                  if (!assignmentss.isEmpty()) {
                     log.info("Size of assginment is " + assignmentss.size());
                  }

                  Set<Long> desiredUserIdSet = (Set)desiredUserIds.stream().map(UserModel::getUserId).collect(Collectors.toSet());

                  for(KaleoTaskAssignmentInstance assignment : assignmentss) {
                     if (!desiredUserIdSet.contains(assignment.getAssigneeClassPK())) {
                        KaleoTaskAssignmentInstanceLocalServiceUtil.deleteKaleoTaskAssignmentInstance(assignment);
                        log.info("Removed  Assigments from " + assignment.getAssigneeClassPK());
                        Indexer<KaleoTaskAssignmentInstance> indexer1 = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskAssignmentInstance.class);
                        indexer1.reindex(assignment);
                        Indexer<KaleoTaskInstanceToken> indexer = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskInstanceToken.class);
                        indexer.reindex(activeToken);
                     }
                  }

               } else {
                  throw new PortalException("NodeNames WithRole missing or Node Names With Org Type");
               }
            }
         }
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
   }

   public List<User> getMessageFromScript(long classPk, String organizationType, String roleName) {
      try {
         com.liferay.object.model.ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
         Map<String, Serializable> values = objectEntry.getValues();
         long companyId = objectEntry.getCompanyId();
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
         Organization organization = this.resolveOrganization(organizationType, values, companyId);
         log.info("organization " + organization.getName());
         if (Validator.isNull(organization)) {
            return Collections.emptyList();
         } else {
            String departmentRole = getDepartmentRoleId(this.boiWorkflowConfiguation, objectDefinition.getObjectDefinitionId());
            List<User> userFoundInOrganisation = new ArrayList();
            log.info("departmentRole " + departmentRole + " roleName " + roleName);
            long[] organisationUserId = OrganizationLocalServiceUtil.getUserPrimaryKeys(organization.getOrganizationId());
            if (Validator.isNotNull(organisationUserId)) {
               log.debug("Total Users founds in same organisations " + organisationUserId.length);

               for(long l : organisationUserId) {
                  User user1 = UserLocalServiceUtil.getUser(l);
                  boolean hasRoleA = RoleLocalServiceUtil.hasUserRole(user1.getUserId(), companyId, roleName, true);
                  boolean hasRoleB = RoleLocalServiceUtil.hasUserRole(user1.getUserId(), companyId, departmentRole, true);
                  boolean hasBoth = hasRoleA && hasRoleB;
                  if (hasBoth) {
                     userFoundInOrganisation.add(user1);
                  }
               }

               log.debug("Based on role user found " + roleName + " : " + departmentRole + " : " + userFoundInOrganisation.size());
            }

            if (Validator.isNotNull(userFoundInOrganisation) && userFoundInOrganisation.size() == 0) {
               User user1 = UserLocalServiceUtil.getUserByScreenName(companyId, "test");
               userFoundInOrganisation.add(user1);
            }

            return userFoundInOrganisation;
         }
      } catch (Exception e) {
         log.error("Error processing workflow context: ", e);
         return new ArrayList();
      }
   }

   public static String getDepartmentRoleId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {
      if (classId == boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId()) {
         return "CPD Insurance";
      } else if (classId == boiWorkflowConfiguation.DBDMandateObjectDefinitionId()) {
         return "DBD Mandate Cancellation";
      } else if (classId == boiWorkflowConfiguation.DBDepartmentObjectDefinitionId()) {
         return "Digital Banking Department";
      } else if (classId == boiWorkflowConfiguation.CustomerAccountObjectDefinitionId()) {
         return "Account Opening";
      } else if (classId == boiWorkflowConfiguation.FBDFRBOObjectDefinitionId()) {
         return "FBD FE BO";
      } else if (classId == boiWorkflowConfiguation.FBDObjectDefinitionId()) {
         return "FBD";
      } else if (classId == boiWorkflowConfiguation.FinancePloeObjectDefinitionId()) {
         return "Finance Ploe";
      } else if (classId == boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId()) {
         return "UPI Insurance";
      } else {
         return classId == boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId() ? "FE BO NON Financial" : null;
      }
   }

   private Organization resolveOrganization(String orgType, Map<String, Serializable> values, long companyId) {
      if (orgType.equalsIgnoreCase("BRANCH") && values.containsKey("branchCode")) {
         String branchName = String.valueOf(values.get("branchCode"));
         return this.getOrganization(branchName, companyId);
      } else if (orgType.equalsIgnoreCase("ZONE") && values.containsKey("zone")) {
         String zoneName = String.valueOf(values.get("zone"));
         return this.getOrganization(zoneName, companyId);
      } else {
         return orgType.equalsIgnoreCase("HO") ? this.getOrganization("BOI HO", companyId) : null;
      }
   }

   private Organization getOrganization(String organizationName, long companyId) {
      try {
         return OrganizationLocalServiceUtil.getOrganization(companyId, organizationName);
      } catch (PortalException e) {
         log.error("error... " + e.getMessage());
         return null;
      }
   }

   private Page<ObjectEntry> getUserDeparments(User user, ObjectDefinition objectDefinition, String branchName) {
      try {
         ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil.getObjectDefinition(this.boiWorkflowConfiguation.UserDepartmentObjectDefinitionId());
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         branchName = "branchCode eq '" + branchName + "' and status eq 1";
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(userDepartmentObject.getStorageType());
         Page<ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject, userDepartmentObject.getScope(), (Aggregation)null, dtoConverterContext, branchName, Pagination.of(-1, 50), (String)null, (Sort[])null);
         log.debug("List from user deaprtment " + page.getTotalCount());
         return page;
      } catch (Exception e) {
         log.error(e.getMessage());
         return null;
      }
   }
}
