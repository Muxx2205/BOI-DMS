package com.liferay.object.delete.portlet;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoTask;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   property = {"dispatch.task.executor.name=ObjectEntryDeleteSchedularJob", "dispatch.task.executor.type=ObjectEntryDeleteSchedularJob"},
   service = {DispatchTaskExecutor.class}
)
public class ObjectEntryDeleteSchedularJob extends BaseDispatchTaskExecutor {
   private static final Log log = LogFactoryUtil.getLog(ObjectEntryDeleteSchedularJob.class);
   @Reference
   private KaleoTaskInstanceTokenLocalService _kaleoTaskInstanceTokenLocalService;
   private BOIWorkflowConfiguation boiWorkflowConfiguation;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   @Reference
   private WorkflowTaskManager _workflowTaskManager;
   @Reference
   private WorkflowInstanceManager _workflowInstanceManager;
   @Reference
   private WorkflowInstanceLinkLocalService _workflowInstanceLinkLocalService;

   public String getName() {
      return "ObjectEntryDeleteSchedularJob";
   }

   public void doExecute(DispatchTrigger dispatchTrigger, DispatchTaskExecutorOutput dispatchTaskExecutorOutput) throws Exception {
      User user = UserLocalServiceUtil.getUser(20124L);
      this.printUserAssignments(user.getCompanyId(), user.getUserId());
   }

   public void printUserAssignments(long companyId, long userId) throws Exception {
      List<WorkflowTask> tasks = this._workflowTaskManager.getWorkflowTasksByUser(companyId, userId, false, -1, -1, (OrderByComparator)null);
      log.info("tasks recieved " + tasks.size());

      for(WorkflowTask task : tasks) {
         long workflowInstanceId = task.getWorkflowInstanceId();
         WorkflowInstance instance = this._workflowInstanceManager.getWorkflowInstance(companyId, workflowInstanceId);
         Map<String, Serializable> context = instance.getWorkflowContext();
         User adminUser = UserLocalServiceUtil.getUser(420111L);
         long groupId = Long.parseLong((String)context.get("groupId"));
         String className = (String)context.get("entryClassName");
         long classPK = Long.parseLong((String)context.get("entryClassPK"));
         WorkflowInstanceLink link = this._workflowInstanceLinkLocalService.fetchWorkflowInstanceLink(companyId, groupId, className, classPK);
         long kaleoTaskInstanceTokenId = task.getWorkflowTaskId();
         KaleoTaskInstanceToken taskInstanceToken = this._kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(kaleoTaskInstanceTokenId);
         KaleoTask kaleoTask = taskInstanceToken.getKaleoTask();
         KaleoNode kaleoNode = kaleoTask.getKaleoNode();
         log.info("");
         log.info("");
         log.info("");
         log.info("WorkflowInstanceId: " + task.getWorkflowInstanceId());
         log.info("ClassName: " + link.getClassName());
         log.info("ClassPK: " + link.getClassPK());
         log.info("Task Name: " + kaleoTask.getName());
         log.info("Node Name: " + kaleoNode.getName());
         log.info("Node Type: " + kaleoNode.getType());
         log.info("");
         log.info("");
         log.info("");
         if (link != null && link.getClassName().equalsIgnoreCase("com.liferay.object.model.ObjectDefinition#B7W4") && kaleoNode.getName().equalsIgnoreCase("a96d8a6b-809c-48bc-b803-86d3ec3a5667")) {
            log.info("");
            log.info("");
            log.info(" ");
            log.info("ClassName: " + link.getClassName());
            log.info("ClassPK: " + link.getClassPK());
            List<User> users = this.getMessageFromScript(companyId, link.getClassPK(), link.getClassName(), "ZONE", "Zone Checker");
            if (Validator.isNotNull(users)) {
               log.info("users founds : " + users.size());
               users.forEach((user) -> {
                  try {
                     this._workflowTaskManager.assignWorkflowTaskToUser(companyId, adminUser.getUserId(), task.getWorkflowTaskId(), user.getUserId(), "Re-Assignments", (Date)null, (Map)null);
                     log.info("users : " + user.getScreenName());
                  } catch (PortalException e) {
                     log.info("error " + e.getMessage());
                  }

               });
            }

            log.info("");
            log.info("");
            log.info("");
         }

         log.info("-------------------");
      }

   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
   }

   public List<User> getMessageFromScript(long companyId, long classPk, String entryClassName, String organizationType, String roleName) {
      try {
         com.liferay.object.model.ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
         Map<String, Serializable> values = objectEntry.getValues();
         long userId = objectEntry.getUserId();
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
         if (!objectDefinition.getClassName().equalsIgnoreCase(entryClassName)) {
            return Collections.emptyList();
         } else {
            Organization organization = this.resolveOrganization(organizationType, values, companyId);
            if (Validator.isNull(organization)) {
               return Collections.emptyList();
            } else {
               String systemClassName = BOIDMSConstants.getDepartmentNameId(this.boiWorkflowConfiguation, objectDefinition.getObjectDefinitionId());
               log.debug("systemClassName: " + systemClassName);
               String departmentRole = getDepartmentRoleId(this.boiWorkflowConfiguation, objectDefinition.getObjectDefinitionId());
               List<User> userFoundInOrganisation = new ArrayList();
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

   private boolean hasRequiredWorkflowContextKeys(Map<String, Serializable> context) {
      return context.containsKey("companyId") && context.containsKey("entryClassName") && context.containsKey("entryClassPK");
   }

   private long parseLong(Object value) {
      return Long.parseLong(String.valueOf(value));
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

   private String buildFilter(String systemClassName, long organizationId) {
      List<String> filters = new ArrayList();
      filters.add("departmentId eq '" + systemClassName + "'");
      filters.add("organizationId eq " + organizationId);
      return (String)filters.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
   }

   private Role getRoleBasedType(String roleType, long companyId) {
      try {
         if (roleType.equalsIgnoreCase("Branch Checker")) {
            return RoleLocalServiceUtil.getRole(companyId, "Branch Checker");
         } else if (roleType.equalsIgnoreCase("Branch Maker")) {
            return RoleLocalServiceUtil.getRole(companyId, "Branch Maker");
         } else if (roleType.equalsIgnoreCase("Zone Maker")) {
            return RoleLocalServiceUtil.getRole(companyId, "Zone Maker");
         } else if (roleType.equalsIgnoreCase("Zone Checker")) {
            return RoleLocalServiceUtil.getRole(companyId, "Zone Checker");
         } else {
            return roleType.equalsIgnoreCase("HO Checker") ? RoleLocalServiceUtil.getRole(companyId, "HO Checker") : null;
         }
      } catch (Exception e) {
         log.error("error... " + e.getMessage());
         return null;
      }
   }

   private List<User> getObjectToUserList(Page<ObjectEntry> page, long companyId, String roleType) {
      List<User> users = new ArrayList();
      Role role = this.getRoleBasedType(roleType, companyId);
      page.getItems().forEach((x) -> {
         try {
            Map<String, Object> map = x.getProperties();
            if (map.containsKey("userScreenName")) {
               String userScreenName = String.valueOf(map.get("userScreenName"));
               User user = UserLocalServiceUtil.getUserByScreenName(companyId, userScreenName);
               boolean isValid = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
               if (isValid) {
                  users.add(UserLocalServiceUtil.getUserByScreenName(companyId, userScreenName));
               }
            }
         } catch (Exception e) {
            log.error("error... " + e.getMessage());
         }

      });
      if (users.isEmpty()) {
         try {
            User var7 = UserLocalServiceUtil.getUserByScreenName(companyId, "test");
         } catch (PortalException e) {
            log.error(e);
         }
      }

      users.forEach((u) -> {
      });
      return users;
   }

   private Organization getOrganization(String organizationName, long companyId) {
      try {
         return OrganizationLocalServiceUtil.getOrganization(companyId, organizationName);
      } catch (PortalException e) {
         log.error("error... " + e.getMessage());
         return null;
      }
   }

   private Page<ObjectEntry> getUserDeparments(String query, User user) {
      try {
         ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil.getObjectDefinition(this.boiWorkflowConfiguation.UserDepartmentObjectDefinitionId());
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(userDepartmentObject.getStorageType());
         Page<ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject, userDepartmentObject.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);
         return page;
      } catch (Exception e) {
         log.error(e);
         return null;
      }
   }

   static {
      System.out.println("com.liferay.object.delete.portlet.ObjectEntryDeleteSchedularJob loaded..");
   }
}
