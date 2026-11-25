package boi.headless.delivery.memorandum.util;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class PSRSWorkflow {
   private static final String ROLE_BRANCH_MAKER = "psrs branch maker";
   private static final String ROLE_BRANCH_CHECKER = "psrs branch checker";
   private static final String ROLE_ZONE_MAKER = "psrs zone maker";
   private static final String ROLE_ZONE_CHECKER = "psrs zone checker";
   private static final String ROLE_RA_DZM = "psrs ra dzm";
   private static final String ROLE_NOTING_AUTHORITY = "psrs noting authority";
   private static final String STATUS_RESUBMIT = "Pending for Observations Noted";
   private static final String PROPOSED_LIMIT = "proposedLimitRs";
   private long objectEntryId = 0L;
   private User user = null;
   private Company company = null;
   private String boiStatusAndCurrent = "collST";
   private String boiCurrent = "boiCurrent";
   private String boiStatus = "boiStatus";
   private String currentWorkflowRole = "";
   private long wrkTskId = 0L;
   private String actionKey = "";
   private HttpServletRequest contextHttpServletRequest = null;
   private static final Map<String, Map<String, String>> processFlow = new LinkedHashMap();
   public static final Map<String, String[]> stageFlow;
   public static final Map<String, String> statusMap;
   private final List<Long> roleIds = new ArrayList();
   private String currentRoleName = "";
   Log _log = LogFactoryUtil.getLog(this.getClass());

   public PSRSWorkflow(HttpServletRequest contextHttpServletRequest, User user, Company company) {
      this.objectEntryId = ParamUtil.getLong(contextHttpServletRequest, "entryId");
      this.user = user;
      this.company = company;
      this.actionKey = ParamUtil.getString(contextHttpServletRequest, "action");
      this.wrkTskId = ParamUtil.getLong(contextHttpServletRequest, "wrkTsk");
      this.contextHttpServletRequest = contextHttpServletRequest;
   }

   private boolean isDoubleRoles() {
      this.roleIds.clear();
      this.currentRoleName = "";

      try {
         List<Role> roles = this.user.getRoles();
         Map<String, Long> roleMap = new HashMap();

         for(Role role : roles) {
            roleMap.put(role.getName().toLowerCase().trim(), role.getRoleId());
         }

         if (this.hasBoth(roleMap, "psrs branch maker", "psrs branch checker")) {
            this.roleIds.add((Long)roleMap.get("psrs branch maker"));
            this.roleIds.add((Long)roleMap.get("psrs branch checker"));
            this.currentRoleName = "psrs branch checker";
            return true;
         }

         if (this.hasBoth(roleMap, "psrs zone maker", "psrs zone checker")) {
            this.roleIds.add((Long)roleMap.get("psrs zone maker"));
            this.roleIds.add((Long)roleMap.get("psrs zone checker"));
            this.currentRoleName = "psrs zone checker";
            return true;
         }

         if (roleMap.containsKey("psrs ra dzm")) {
            this.currentRoleName = "psrs ra dzm";
         } else if (roleMap.containsKey("psrs noting authority")) {
            this.currentRoleName = "psrs noting authority";
         } else if (roleMap.containsKey("psrs branch maker")) {
            this.currentRoleName = "psrs branch maker";
         } else if (roleMap.containsKey("psrs branch checker")) {
            this.currentRoleName = "psrs branch checker";
         } else if (roleMap.containsKey("psrs zone maker")) {
            this.currentRoleName = "psrs zone maker";
         } else if (roleMap.containsKey("psrs zone checker")) {
            this.currentRoleName = "psrs zone checker";
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return false;
   }

   private boolean hasBoth(Map<String, Long> map, String r1, String r2) {
      return map.containsKey(r1) && map.containsKey(r2);
   }

   private <T> T getObjectFieldValue(long objectEntryId, String fieldKey, Class<T> type) {
      try {
         ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(objectEntryId);
         Map<String, Serializable> values = objectEntry.getValues();
         Serializable value = (Serializable)values.get(fieldKey);
         if (value == null) {
            return null;
         }

         if (type == String.class) {
            return (T)type.cast(String.valueOf(value));
         }

         if (type == Long.class) {
            return (T)type.cast(Long.parseLong(String.valueOf(value)));
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return null;
   }

   public String getTransitionNode() {
      try {
         this._log.info("Fetching object field value for boiCurrent");
         String objectFieldValue = (String)this.getObjectFieldValue(this.objectEntryId, this.boiStatusAndCurrent, String.class);
         String[] data = (String[])stageFlow.get(objectFieldValue);
         String value = "";
         String status = "";
         if (data != null) {
            value = data[0];
            status = data[1];
            this._log.info("Role: " + value);
            this._log.info("Status: " + status);
         }

         this._log.info("Value received: " + value);
         if (value == null || status == null) {
            return null;
         }

         this.currentWorkflowRole = value.trim();
         boolean isDouble = this.isDoubleRoles();
         this._log.info("isDoubleRoles: " + isDouble);
         if (!isDouble) {
            this._log.info("Entered SINGLE role block");
            if ("Pending for Observations Noted".equalsIgnoreCase(status) && this.currentRoleName.equalsIgnoreCase("psrs branch maker")) {
               this._log.info("Condition matched: RESUBMIT + BRANCH_MAKER");
               Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
               String result = nextMap != null ? (String)nextMap.get("resubmit") : null;
               this._log.info("Returning node: " + result);
               return result;
            }

            if ("psrs zone maker".equalsIgnoreCase(value.trim()) && this.currentRoleName.equalsIgnoreCase("psrs zone maker")) {
               Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
               String nextNode = this.actionKey.equalsIgnoreCase("complete") ? "finalapproved" : this.actionKey;
               String result = nextMap != null ? (String)nextMap.get(nextNode) : null;
               this._log.info("Returning node: " + result);
               return result;
            }

            this._log.info("Node null, using actionKey");
            Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
            this._log.info("Map Size: " + nextMap.size());
            String result = nextMap != null ? (String)nextMap.get(this.actionKey) : null;
            this._log.info("Returning node: " + result);
            return result;
         }

         this._log.info("Entered DOUBLE role block");
         if ("Pending for Observations Noted".equalsIgnoreCase(status) && this.currentRoleName.equalsIgnoreCase("psrs branch checker")) {
            this._log.info("Condition matched: RESUBMIT + BRANCH_CHECKER");
            Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
            String result = nextMap != null ? (String)nextMap.get("bypassresubmit") : null;
            this._log.info("Returning node: " + result);
            return result;
         }

         if ("psrs branch maker".equalsIgnoreCase(value.trim()) && this.currentRoleName.equalsIgnoreCase("psrs branch checker")) {
            this._log.info("Condition matched: BRANCH_MAKER -> BRANCH_CHECKER");
            String node = this.checkLimitAndReturnNode(this.objectEntryId, this.currentRoleName, value.trim());
            this._log.info("Branch maker node result: " + node);
            if (Validator.isNull(node)) {
               this._log.info("Condition matched: BRANCH_MAKER -> BRANCH_CHECKER");
               Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
               String result = nextMap != null ? (String)nextMap.get("bypass") : null;
               this._log.info("Returning node: " + result);
               return result;
            }

            return node;
         }

         if ("psrs zone maker".equalsIgnoreCase(value.trim()) && this.currentRoleName.equalsIgnoreCase("psrs zone checker")) {
            String changedRoleName = this.currentRoleName;
            String nextNode = "bypass";
            if (this.actionKey.equalsIgnoreCase("complete")) {
               changedRoleName = "psrs zone maker";
               nextNode = "finalapproved";
            }

            Map<String, String> nextMap = (Map)processFlow.get(changedRoleName);
            String result = nextMap != null ? (String)nextMap.get(nextNode) : null;
            this._log.info("Returning node: " + result);
            return result;
         }

         if ("psrs zone checker".equalsIgnoreCase(value.trim()) && this.currentRoleName.equalsIgnoreCase("psrs zone checker")) {
            Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
            String nextNode = this.actionKey.equalsIgnoreCase("complete") ? "finalapproved" : this.actionKey;
            String result = nextMap != null ? (String)nextMap.get(nextNode) : null;
            this._log.info("Returning node: " + result);
            return result;
         }

         if ("psrs branch checker".equalsIgnoreCase(value.trim()) && this.currentRoleName.equalsIgnoreCase("psrs branch checker")) {
            String node = this.checkLimitAndReturnNode(this.objectEntryId, this.currentRoleName, value.trim());
            if (Validator.isNull(node)) {
               this._log.info("Condition matched: BRANCH_MAKER -> BRANCH_CHECKER");
               Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
               String result = nextMap != null ? (String)nextMap.get(this.actionKey) : null;
               this._log.info("Returning node: " + result);
               return result;
            }

            return node;
         }

         if (this.currentRoleName.equalsIgnoreCase(value.trim())) {
            this._log.info("Current role equals value");
            Map<String, String> nextMap = (Map)processFlow.get(this.currentRoleName);
            String result = nextMap != null ? (String)nextMap.get(this.actionKey) : null;
            this._log.info("Returning node: " + result);
            return result;
         }
      } catch (Exception e) {
         this._log.error("Exception in getTransitionNode()", e);
      }

      this._log.info("getTransitionNode() END returning null");
      return null;
   }

   private String checkLimitAndReturnNode(long objectEntryId, String currentRoleName, String value) {
      this._log.info("checkLimitAndReturnNode() START");
      this._log.info("objectEntryId: " + objectEntryId + ", currentRoleName: " + currentRoleName + ", value: " + value);

      try {
         boolean roleMatch = currentRoleName.equalsIgnoreCase("psrs branch checker") && (value.equalsIgnoreCase("psrs branch checker") || value.equalsIgnoreCase("psrs branch maker"));
         this._log.info("Role match condition: " + roleMatch);
         if (roleMatch) {
            this._log.info("Fetching proposed limit");
            String proposedLimitStr = (String)this.getObjectFieldValue(objectEntryId, "proposedLimitRs", String.class);
            this._log.info("Proposed limit string: " + proposedLimitStr);
            if (Validator.isNotNull(proposedLimitStr)) {
               this._log.info("Parsing proposed limit to double");
               double proposedLimitValue = Double.parseDouble(proposedLimitStr);
               this._log.info("Proposed limit value: " + proposedLimitValue);
               if (proposedLimitValue < (double)500000.0F) {
                  this._log.info("Limit < 5 Lakh condition matched");
                  Map<String, String> nextMap = (Map)processFlow.get("psrs branch checker");
                  String fiveLakhLimitNode = value.equalsIgnoreCase("psrs branch checker") ? "branchChecker5LakhLimit" : "branchMaker5LakhLimit";
                  String result = nextMap != null ? (String)nextMap.get(fiveLakhLimitNode) : null;
                  this._log.info("Returning node: " + result);
                  return result;
               }

               this._log.info("Limit >= 5 Lakh, skipping bypass");
            } else {
               this._log.info("Proposed limit is null or empty");
            }
         } else {
            this._log.info("Role condition not matched");
         }
      } catch (Exception e) {
         this._log.error("Exception in checkZoneMakerLimitAndReturnNode()", e);
      }

      this._log.info("checkZoneMakerLimitAndReturnNode() END returning null");
      return null;
   }

   public long getRoleId() {
      try {
         return RoleLocalServiceUtil.getRole(this.company.getCompanyId(), this.currentWorkflowRole).getRoleId();
      } catch (PortalException e) {
         e.printStackTrace();
         return 0L;
      }
   }

   public WorkflowTask getWorkFlowTaskId() {
      this._log.info("Entering getWorkFlowTaskId");
      long companyId = this.company.getCompanyId();
      long userId = this.user.getUserId();
      WorkflowTask workflowTask = null;
      List<WorkflowTask> workflowTasks = null;
      this._log.info("CompanyId: " + companyId + ", UserId: " + userId + ", ObjectEntryId: " + this.objectEntryId);

      try {
         workflowTasks = WorkflowTaskManagerUtil.getWorkflowTasksByUserRoles(companyId, userId, false, -1, -1, (OrderByComparator)null);
         this._log.info("Workflow tasks fetched count: " + workflowTasks.size());

         for(WorkflowTask task : workflowTasks) {
            Map<String, Serializable> context = task.getOptionalAttributes();
            Object pk = context.get("entryClassPK");
            Long entryClassPK = pk != null ? (pk instanceof Long ? (Long)pk : Long.valueOf(pk.toString())) : null;
            if (entryClassPK != null && entryClassPK.equals(this.objectEntryId)) {
               workflowTask = task;
               this._log.info("Matching WorkflowTask found. TaskId: " + task.getWorkflowTaskId());
               break;
            }
         }

         if (workflowTask == null) {
            this._log.info("No matching WorkflowTask found for ObjectEntryId: " + this.objectEntryId);
         }
      } catch (WorkflowException e) {
         this._log.error("Error while fetching workflow tasks", e);
      }

      this._log.info("Exiting getWorkFlowTaskId");
      return workflowTask;
   }

   static {
      Map<String, String> branchMaker = new LinkedHashMap();
      branchMaker.put("approve", "psrs2BranchChecker");
      branchMaker.put("resubmit", "psrs2BranchMakerResubmit");
      processFlow.put("psrs branch maker", branchMaker);
      Map<String, String> branchChecker = new LinkedHashMap();
      branchChecker.put("bypass", "psrs2ByPassZoneMaker");
      branchChecker.put("branchMaker5LakhLimit", "psrs2Limit5LakhApproved");
      branchChecker.put("branchChecker5LakhLimit", "psrs2Limit5LakhCheckerApproved");
      branchChecker.put("approve", "psrs2ZoneMaker");
      branchChecker.put("bypassresubmit", "psrs2ByPassResubmit");
      processFlow.put("psrs branch checker", branchChecker);
      Map<String, String> zoneMaker = new LinkedHashMap();
      zoneMaker.put("finalapproved", "psrs2ZoneMakerFinalApproved");
      zoneMaker.put("approve", "psrs2ZoneChecker");
      processFlow.put("psrs zone maker", zoneMaker);
      Map<String, String> zoneChecker = new LinkedHashMap();
      zoneChecker.put("reject", "psrs2ZoneCheckerDenied");
      zoneChecker.put("bypass", "psrs2ByPassToZoneCheker1");
      zoneChecker.put("approve", "psrs2RA-DZM");
      zoneChecker.put("finalapproved", "psrs2ZoneCheckerFinalAproved");
      processFlow.put("psrs zone checker", zoneChecker);
      Map<String, String> psrsRADZM = new LinkedHashMap();
      psrsRADZM.put("reject", "psrs2RA-DZMDenied");
      psrsRADZM.put("approve", "psrs2NotingAuthority");
      processFlow.put("psrs ra dzm", psrsRADZM);
      Map<String, String> notingAuthority = new LinkedHashMap();
      notingAuthority.put("Noted & Closed", "psrs2NotedAndClosed");
      notingAuthority.put("Noted with Observations", "Psrs2ObervationNoted");
      notingAuthority.put("reject", "Psrs2NotingAuthorityReject");
      processFlow.put("psrs noting authority", notingAuthority);
      stageFlow = new HashMap();
      stageFlow.put("-3", new String[]{"PSRS Branch Maker", "Pending for Observations Noted"});
      stageFlow.put("-2", new String[]{"PSRS Branch Maker", "Pending at Branch Maker"});
      stageFlow.put("-1", new String[]{"PSRS Branch Checker", "Pending at Branch Checker"});
      stageFlow.put("0", new String[]{"PSRS Zone Maker", "Pending at Zone Maker"});
      stageFlow.put("1", new String[]{"PSRS Zone Checker", "Pending at Zone Checker"});
      stageFlow.put("2", new String[]{"PSRS RA DZM", "Pending at Recommending Authority"});
      stageFlow.put("3", new String[]{"PSRS Noting Authority", "Pending at Noting Authority"});
      stageFlow.put("4", new String[]{" ", "Approved at Branch Level"});
      stageFlow.put("5", new String[]{" ", "Approved at Zone Level"});
      stageFlow.put("6", new String[]{" ", "Approved"});
      statusMap = new HashMap();
      statusMap.put("psrs2BranchChecker", "-1");
      statusMap.put("psrs2BranchMakerResubmit", "-1");
      statusMap.put("psrs2ByPassZoneMaker", "0");
      statusMap.put("psrs2Limit5LakhApproved", "4");
      statusMap.put("psrs2Limit5LakhCheckerApproved", "4");
      statusMap.put("psrs2ZoneMaker", "0");
      statusMap.put("psrs2ByPassResubmit", "0");
      statusMap.put("psrs2ZoneMakerFinalApproved", "5");
      statusMap.put("psrs2ZoneChecker", "1");
      statusMap.put("psrs2ZoneCheckerDenied", "0");
      statusMap.put("psrs2ByPassToZoneCheker1", "2");
      statusMap.put("psrs2RA-DZM", "2");
      statusMap.put("psrs2ZoneCheckerFinalAproved", "5");
      statusMap.put("psrs2RA-DZMDenied", "1");
      statusMap.put("psrs2NotingAuthority", "3");
      statusMap.put("psrs2NotedAndClosed", "6");
      statusMap.put("Psrs2ObervationNoted", "-3");
      statusMap.put("Psrs2NotingAuthorityReject", "2");
   }
}
