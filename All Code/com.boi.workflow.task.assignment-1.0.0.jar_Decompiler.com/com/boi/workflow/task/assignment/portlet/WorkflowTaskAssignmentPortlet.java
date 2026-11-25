package com.boi.workflow.task.assignment.portlet;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=WorkflowTaskAssignment", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_boi_workflow_task_assignment_WorkflowTaskAssignmentPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class WorkflowTaskAssignmentPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(WorkflowTaskAssignmentPortlet.class);
   long enteredUserId = 0L;
   String enteredScreenName = "";
   String maker = "";
   String checker = "";
   long organizationId = 0L;
   long newMakerId = 0L;
   String newMakerScreenName = "";
   long newCheckerId = 0L;
   String newCheckerScreenName = "";
   JSONObject json = JSONFactoryUtil.createJSONObject();

   public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      String screenName = ParamUtil.getString(resourceRequest, "screenName");
      String direction = ParamUtil.getString(resourceRequest, "direction");
      String currentRole = ParamUtil.getString(resourceRequest, "currentRole");
      this.maker = currentRole + " Maker";
      this.checker = currentRole + " Checker";
      this.json.put("0", "new");
      this.json.put("1", "---------------User Details---------------");
      this.json.put("2", "User :- " + screenName);

      try {
         User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), screenName);
         this.json.put("3", "User Found Successfully");
         this.enteredUserId = user.getUserId();
         this.enteredScreenName = user.getScreenName();
         List<String> roleNames = new ArrayList();

         for(Role role : user.getRoles()) {
            String roleName = role.getName();
            if (!"User".equalsIgnoreCase(roleName)) {
               roleNames.add(roleName);
            }
         }

         this.json.put("4", "User Roles :- " + roleNames);
         String branch = "";
         String zone = "";
         String nbg = "";
         long branchId = 0L;
         long zoneId = 0L;
         long nbgId = 0L;
         List<Organization> orgs = user.getOrganizations();
         if (!orgs.isEmpty()) {
            Organization currentOrg = (Organization)orgs.get(0);
            if ("Branch".equalsIgnoreCase(currentOrg.getType())) {
               branch = currentOrg.getName();
               branchId = currentOrg.getOrganizationId();
            } else if ("Zone".equalsIgnoreCase(currentOrg.getType())) {
               zone = currentOrg.getName();
               zoneId = currentOrg.getOrganizationId();
            } else if ("NBG".equalsIgnoreCase(currentOrg.getType())) {
               nbg = currentOrg.getName();
               nbgId = currentOrg.getOrganizationId();
            }

            Organization parent;
            for(long parentId = currentOrg.getParentOrganizationId(); parentId > 0L; parentId = parent.getParentOrganizationId()) {
               parent = OrganizationLocalServiceUtil.getOrganization(parentId);
               if ("Branch".equalsIgnoreCase(parent.getType()) && branch.isEmpty()) {
                  branch = parent.getName();
                  branchId = parent.getOrganizationId();
               } else if ("Zone".equalsIgnoreCase(parent.getType()) && zone.isEmpty()) {
                  zone = parent.getName();
                  zoneId = parent.getOrganizationId();
               } else if ("NBG".equalsIgnoreCase(parent.getType()) && nbg.isEmpty()) {
                  nbg = parent.getName();
                  nbgId = parent.getOrganizationId();
               }
            }
         }

         if (branchId > 0L) {
            this.organizationId = branchId;
         } else if (zoneId > 0L) {
            this.organizationId = zoneId;
         }

         this.json.put("5", "User Branch :- " + branch);
         User checkers = this.getCheckers(this.organizationId, this.enteredScreenName);
         if (direction.equalsIgnoreCase("MakerToChecker")) {
            this.newMakerId = this.enteredUserId;
            this.newMakerScreenName = this.enteredScreenName;
            this.newCheckerId = checkers.getUserId();
            this.newCheckerScreenName = checkers.getScreenName();
         } else if (direction.equalsIgnoreCase("CheckerToMaker")) {
            this.newMakerId = checkers.getUserId();
            this.newMakerScreenName = checkers.getScreenName();
            this.newCheckerId = this.enteredUserId;
            this.newCheckerScreenName = this.enteredScreenName;
         }

         List<WorkflowTask> pendingTasksForUser = this.getPendingTasksForUser(this.newCheckerId, checkers.getCompanyId());
         this.json.put("8", "---------------Workflow Assignment---------------");
         String var10002 = this.newCheckerScreenName;
         this.json.put("9", var10002 + " having total Pending Task  : " + pendingTasksForUser.size());
         this.json.put("10", "Assignee Task to User   :- " + this.newMakerScreenName);
         JSONObject var10000 = this.json;
         int var35 = pendingTasksForUser.size();
         var10000.put("11", "Total Assigning task " + var35 + " to user  : " + this.newMakerScreenName);

         for(WorkflowTask task : pendingTasksForUser) {
            this.assignTaskToTargetUser(task.getWorkflowTaskId(), this.newMakerId);
         }

         this.json.put("12", "All Task Assigned to  " + this.newMakerScreenName);
         List<WorkflowTask> pendingTasksForUser1 = this.getPendingTasksForUser(this.newMakerId, checkers.getCompanyId());
         var10000 = this.json;
         var35 = pendingTasksForUser1.size();
         var10000.put("13", "Total Assigned task " + var35 + " to user  : " + this.newMakerScreenName);
         this.json.put("14", " Action SuccessFully Done .. ");
      } catch (Exception e) {
         _log.error("User fetch error: ", e);
         this.json.put("error", "User not found with screen name: " + screenName);
      }

      try {
         resourceResponse.setContentType("application/json");
         resourceResponse.getWriter().write(this.json.toString());
         _log.info("JSON response sent successfully");
      } catch (IOException e) {
         throw new PortletException("Failed to write JSON response", e);
      }
   }

   public User getCheckers(long organizationId, String enteredScreenName) {
      List<User> branchCheckers = new ArrayList();

      try {
         for(User user : UserLocalServiceUtil.getOrganizationUsers(organizationId)) {
            for(Role role : user.getRoles()) {
               if (role.getName().equalsIgnoreCase(this.checker) && this.hasMatchingObjectEntry(user.getGroupId(), user.getScreenName(), organizationId, enteredScreenName)) {
                  branchCheckers.add(user);
                  return (User)branchCheckers.get(0);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return (User)branchCheckers.get(0);
   }

   public List<WorkflowTask> getPendingTasksForUser(long userId, long companyId) {
      try {
         return WorkflowTaskManagerUtil.getWorkflowTasksByUser(companyId, userId, false, -1, -1, (OrderByComparator)null);
      } catch (Exception e) {
         e.printStackTrace();
         return Collections.emptyList();
      }
   }

   public boolean hasMatchingObjectEntry(long groupId, String userScreenName, long organizationId, String enteredScreenName) {
      try {
         String departmentId = "";
         List<ObjectEntry> entries = ObjectEntryLocalServiceUtil.getObjectEntries(0L, 224295L, -1, -1);

         for(ObjectEntry entry : entries) {
            try {
               Map<String, Serializable> values = ObjectEntryLocalServiceUtil.getValues(entry);
               if (enteredScreenName.equals(values.get("userScreenName")) && Long.valueOf(organizationId).equals(values.get("organizationId"))) {
                  departmentId = (String)values.get("departmentId");
                  this.json.put("6", "User Department :- " + departmentId);
                  break;
               }
            } catch (PortalException var13) {
            }
         }

         for(ObjectEntry entry : entries) {
            try {
               Map<String, Serializable> values = ObjectEntryLocalServiceUtil.getValues(entry);
               if (userScreenName.equals(values.get("userScreenName")) && departmentId.equals(values.get("departmentId")) && Long.valueOf(organizationId).equals(values.get("organizationId"))) {
                  this.json.put("7", "User Department Checker/Maker :- " + userScreenName);
                  return true;
               }
            } catch (PortalException e) {
               _log.error("Error retrieving values for entry ID: " + entry.getObjectEntryId(), e);
            }
         }

         return false;
      } catch (Exception e) {
         _log.error("Unexpected error while checking object entries", e);
         return false;
      }
   }

   public void assignTaskToTargetUser(long workflowTaskId, long makerUserId) {
      Date dueDate = null;
      Map<String, Serializable> workflowContext = new HashMap();

      try {
         WorkflowTaskManagerUtil.assignWorkflowTaskToUser(CompanyThreadLocal.getCompanyId(), 20124L, workflowTaskId, makerUserId, "", dueDate, workflowContext);
      } catch (PortalException e) {
         e.printStackTrace();
      }

   }
}
