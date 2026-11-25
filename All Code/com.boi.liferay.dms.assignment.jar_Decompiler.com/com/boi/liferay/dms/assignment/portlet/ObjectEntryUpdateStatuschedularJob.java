package com.boi.liferay.dms.assignment.portlet;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
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
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   immediate = true,
   configurationPid = {"com.boi.liferay.dms.assignment.portlet.RolesAllowToAssignment"},
   property = {"dispatch.task.executor.name=ObjectEntryUpdateStatuschedularJob", "dispatch.task.executor.type=ObjectEntryUpdateStatuschedularJob"},
   service = {DispatchTaskExecutor.class}
)
public class ObjectEntryUpdateStatuschedularJob extends BaseDispatchTaskExecutor {
   private static final Log log = LogFactoryUtil.getLog(ObjectEntryUpdateStatuschedularJob.class);
   private static final List<Long> UAT_DEFINITIONS = Arrays.asList(148467L, 148824L, 149243L, 149440L, 149610L, 181212L, 182190L, 108846L);
   private static final List<Long> PROD_DEFINITIONS = Arrays.asList(288141L);
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
         this.put("resubmit", "Branch Maker");
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
   private RolesAllowToAssignment allowToAssignment;
   private BOIWorkflowConfiguation boiWorkflowConfiguation;

   public String getName() {
      return "ObjectEntryUpdateStatuschedularJob";
   }

   public void doExecute(DispatchTrigger dispatchTrigger, DispatchTaskExecutorOutput dispatchTaskExecutorOutput) throws Exception {
      List<Long> uatDefinitionList = this.allowToAssignment.isSyncProdStatus() ? new ArrayList(PROD_DEFINITIONS) : new ArrayList(UAT_DEFINITIONS);
      ObjectDefinition objectDefinition = null;

      for(long l : uatDefinitionList) {
         objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(l);
         List<ObjectEntry> entries = ObjectEntryLocalServiceUtil.getObjectEntries(0L, objectDefinition.getObjectDefinitionId(), 4, -1, -1);
         if (!entries.isEmpty()) {
            Log var10000 = log;
            String var10001 = objectDefinition.getLabel(Locale.getDefault());
            var10000.info(var10001 + " entries " + entries.size());

            for(ObjectEntry objectEntry : entries) {
               try {
                  this.assignmentForAllUsers(objectEntry);
               } catch (Exception e) {
                  log.info(e.getMessage());
               }
            }
         } else {
            log.info(objectDefinition.getLabel(Locale.getDefault()) + " have not entries");
         }
      }

   }

   private void assignmentForAllUsers(ObjectEntry objectEntry) throws Exception {
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
      String className = objectDefinition.getClassName();
      WorkflowInstanceLink workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil.getWorkflowInstanceLink(objectEntry.getCompanyId(), 20121L, className, objectEntry.getObjectEntryId());
      long workflowInstanceId = workflowInstanceLink.getWorkflowInstanceId();
      KaleoInstance kaleoInstance = KaleoInstanceLocalServiceUtil.fetchKaleoInstance(workflowInstanceLink.getWorkflowInstanceId());
      if (kaleoInstance == null) {
         throw new PortalException("No workflow instance found for classPK=" + objectEntry.getObjectEntryId());
      } else {
         DynamicQuery dynamicQuery = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery();
         dynamicQuery.add(RestrictionsFactoryUtil.eq("kaleoInstanceId", workflowInstanceId));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("classPK", objectEntry.getObjectEntryId()));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("className", objectDefinition.getClassName()));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", false));
         List<KaleoTaskInstanceToken> allTokens = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery(dynamicQuery);
         if (allTokens.isEmpty()) {
            throw new PortalException("No active task tokens found for this instance." + objectEntry.getObjectEntryId());
         } else {
            KaleoTaskInstanceToken activeToken = (KaleoTaskInstanceToken)allTokens.get(0);
            List<KaleoTaskAssignmentInstance> assignments = KaleoTaskAssignmentInstanceLocalServiceUtil.getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
            String nodeName = activeToken.getKaleoInstanceToken().getCurrentKaleoNodeName();
            if (assignments.isEmpty()) {
               throw new PortalException(" assignments is blank please check it ");
            } else if (this.nodeNamesWithRole.containsKey(nodeName) && this.nodeNamesWithOrgType.containsKey(nodeName)) {
               String roleForUsers = (String)this.nodeNamesWithRole.get(nodeName);
               log.info("RoleForUsers " + roleForUsers);
               Map<String, Serializable> values = objectEntry.getValues();
               if (values.containsKey("customStatusByHandler")) {
                  String customStatusByHandler = (String)values.get("customStatusByHandler");
                  if (Validator.isBlank(customStatusByHandler)) {
                     values.put("customStatusByHandler", ("Pending with " + roleForUsers).toLowerCase());
                     ServiceContext serviceContext = new ServiceContext();
                     serviceContext.setCompanyId(objectEntry.getCompanyId());
                     serviceContext.setUserId(objectEntry.getUserId());
                     serviceContext.setModifiedDate(objectEntry.getModifiedDate());
                     objectEntry.setModifiedDate(objectEntry.getModifiedDate());
                     ObjectEntryLocalServiceUtil.updateObjectEntry(objectEntry.getUserId(), objectEntry.getObjectEntryId(), values, serviceContext);
                     log.info(objectEntry.getObjectEntryId() + " values has been updated ");
                  } else {
                     log.info("customStatusByHandler have some value");
                  }
               } else {
                  log.info(objectEntry.getObjectEntryId() + " values have not customStatusByHandler");
               }

            } else {
               throw new PortalException("NodeNames WithRole missing or Node Names With Org Type " + objectEntry.getObjectEntryId());
            }
         }
      }
   }

   public List<User> getMessageFromScript(long classPk, String organizationType, String roleName) {
      try {
         ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
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

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
      this.allowToAssignment = (RolesAllowToAssignment)ConfigurableUtil.createConfigurable(RolesAllowToAssignment.class, properties);
   }
}
