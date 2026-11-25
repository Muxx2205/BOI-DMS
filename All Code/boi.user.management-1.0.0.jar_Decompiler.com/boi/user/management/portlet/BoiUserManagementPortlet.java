package boi.user.management.portlet;

import boi.user.management.backgroundTask.ExcelUploadBackgroundTaskExecutor;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=BoiUserManagement", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=boi_user_management_BoiUserManagementPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class BoiUserManagementPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(BoiUserManagementPortlet.class);
   private DLAppService dlAppService;

   public void uploadExcel(ActionRequest actionRequest, ActionResponse actionResponse) {
      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         ServiceContext serviceContext = ServiceContextFactory.getInstance(this.getClass().getName(), actionRequest);
         UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
         File[] uploadedFiles = uploadRequest.getFiles("excelFiles");
         String[] fileNames = uploadRequest.getFileNames("excelFiles");
         Object var10001 = uploadedFiles != null ? uploadedFiles.length : "null";
         _log.info("Uploaded files array: " + var10001);
         String var26 = fileNames != null ? Arrays.toString(fileNames) : "null";
         _log.info("File names array: " + var26);
         if (uploadedFiles == null || uploadedFiles.length == 0 || uploadedFiles[0] == null) {
            actionRequest.setAttribute("errorMessage", "No files selected for upload.");
            return;
         }

         int successCount = 0;
         StringBuilder processedFiles = new StringBuilder();
         long backgroundTaskId = 0L;

         for(int i = 0; i < uploadedFiles.length; ++i) {
            File uploadedFile = uploadedFiles[i];
            String fileName = fileNames != null && i < fileNames.length ? fileNames[i] : "unknown_file_" + i;
            if (uploadedFile != null && uploadedFile.exists() && uploadedFile.length() != 0L) {
               if (fileName != null && !fileName.toLowerCase().endsWith(".xlsx")) {
                  _log.info("Skipping non-Excel file: " + fileName);
               } else {
                  try {
                     byte[] fileContent = Files.readAllBytes(uploadedFile.toPath());
                     String base64Content = Base64.getEncoder().encodeToString(fileContent);
                     Map<String, Serializable> taskContextMap = new HashMap();
                     taskContextMap.put("fileContent", base64Content);
                     taskContextMap.put("fileName", fileName);
                     taskContextMap.put("companyId", themeDisplay.getCompanyId());
                     taskContextMap.put("groupId", themeDisplay.getScopeGroupId());
                     taskContextMap.put("userId", themeDisplay.getUserId());
                     taskContextMap.put("serviceContext", serviceContext);
                     String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                     BackgroundTask addBackgroundTask = BackgroundTaskManagerUtil.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), "ExcelUpload_" + fileName + "_" + timestamp, ExcelUploadBackgroundTaskExecutor.class.getName(), taskContextMap, serviceContext);
                     backgroundTaskId = addBackgroundTask.getBackgroundTaskId();
                     ++successCount;
                     processedFiles.append(fileName).append(", ");
                     _log.info("Successfully queued file for processing: " + fileName + " (Size: " + uploadedFile.length() + " bytes)");
                  } catch (Exception e) {
                     _log.info("Error processing file " + fileName + ": " + e.getMessage());
                  }
               }
            } else {
               _log.info("Skipping invalid file at index " + i + ": " + fileName);
            }
         }

         JSONObject response = JSONFactoryUtil.createJSONObject();
         if (successCount > 0) {
            String filesList = processedFiles.toString();
            if (filesList.endsWith(", ")) {
               filesList = filesList.substring(0, filesList.length() - 2);
            }

            String message = "Successfully uploaded " + successCount + " Excel file(s) for processing: " + filesList;
            response.put("success", true);
            response.put("message", message);
            response.put("successCount", successCount);
            response.put("filesList", filesList);
            response.put("backgroundTaskId", backgroundTaskId);
         } else {
            response.put("success", false);
            response.put("message", "No valid Excel files found to upload. Please ensure you select .xlsx files.");
         }

         this.writeJSON(actionRequest, actionResponse, response);
      } catch (Exception e) {
         String errorMsg = "Error uploading files: " + e.getMessage();
         actionRequest.setAttribute("errorMessage", errorMsg);
         e.printStackTrace();
      }

   }

   public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
      String resourceId = resourceRequest.getResourceID();
      _log.info("serveResource called with resourceId: " + resourceId);
      if ("getProgress".equals(resourceId)) {
         _log.info("Handling getProgress resource request");
         this.getProgress(resourceRequest, resourceResponse);
      } else if ("getRoleCount".equals(resourceId)) {
         this.getRoleCount(resourceRequest, resourceResponse);
      } else if ("getUserRoles".equals(resourceId)) {
         this.getUserRoles(resourceRequest, resourceResponse);
      } else if ("assignRole".equals(resourceId)) {
         this.assignRole(resourceRequest, resourceResponse);
      } else {
         _log.info("Delegating to super.serveResource for resourceId: " + resourceId);
         super.serveResource(resourceRequest, resourceResponse);
      }

   }

   public void getProgress(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      long backgroundTaskId = ParamUtil.getLong(resourceRequest, "backgroundTaskId");
      _log.info("getProgress called for backgroundTaskId: " + backgroundTaskId);

      try {
         com.liferay.portal.background.task.model.BackgroundTask backgroundTask = BackgroundTaskLocalServiceUtil.getBackgroundTask(backgroundTaskId);
         Log var10000 = _log;
         long var10001 = backgroundTask.getBackgroundTaskId();
         var10000.info("Retrieved background task: " + var10001 + ", status: " + backgroundTask.getStatus() + ", statusLabel: " + BackgroundTaskConstants.getStatusLabel(backgroundTask.getStatus()));
         JSONObject response = JSONFactoryUtil.createJSONObject();
         response.put("status", backgroundTask.getStatus());
         response.put("statusLabel", BackgroundTaskConstants.getStatusLabel(backgroundTask.getStatus()));
         Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
         _log.info("Task context map keys: " + taskContextMap.keySet());
         int progress = 0;
         String statusText = "Processing...";
         int processed = 0;
         int total = 0;
         int successCount = 0;
         int failedCount = 0;
         if (taskContextMap.containsKey("1progress")) {
            Object progObj = taskContextMap.get("1progress");
            _log.info("Found 1progress in context map. Value: " + progObj + ", Type: " + (progObj != null ? progObj.getClass().getSimpleName() : "null"));
            if (progObj instanceof Number) {
               progress = ((Number)progObj).intValue();
               _log.info("Parsed progress as Number: " + progress);
            } else if (progObj instanceof String) {
               try {
                  progress = Integer.parseInt((String)progObj);
                  _log.info("Parsed progress as String: " + progress);
               } catch (NumberFormatException var21) {
                  _log.warn("Failed to parse progress from String: " + progObj + ", defaulting to 0");
                  progress = 0;
               }
            }
         } else {
            _log.info("1progress key not found in context map");
         }

         if (taskContextMap.containsKey("1statusText")) {
            statusText = String.valueOf(taskContextMap.get("1statusText"));
            _log.info("Found 1statusText: " + statusText);
         } else {
            _log.info("1statusText key not found, using default: " + statusText);
         }

         if (taskContextMap.containsKey("1processed")) {
            Object procObj = taskContextMap.get("1processed");
            _log.info("Found 1processed in context map. Value: " + procObj + ", Type: " + (procObj != null ? procObj.getClass().getSimpleName() : "null"));
            if (procObj instanceof Number) {
               processed = ((Number)procObj).intValue();
               _log.info("Parsed processed as Number: " + processed);
            } else if (procObj instanceof String) {
               try {
                  processed = Integer.parseInt((String)procObj);
                  _log.info("Parsed processed as String: " + processed);
               } catch (NumberFormatException var20) {
                  _log.warn("Failed to parse processed from String: " + procObj + ", defaulting to 0");
                  processed = 0;
               }
            }
         } else {
            _log.info("1processed key not found in context map");
         }

         if (taskContextMap.containsKey("1total")) {
            Object totalObj = taskContextMap.get("1total");
            _log.info("Found 1total in context map. Value: " + totalObj + ", Type: " + (totalObj != null ? totalObj.getClass().getSimpleName() : "null"));
            if (totalObj instanceof Number) {
               total = ((Number)totalObj).intValue();
               _log.info("Parsed total as Number: " + total);
            } else if (totalObj instanceof String) {
               try {
                  total = Integer.parseInt((String)totalObj);
                  _log.info("Parsed total as String: " + total);
               } catch (NumberFormatException var19) {
                  _log.warn("Failed to parse total from String: " + totalObj + ", defaulting to 0");
                  total = 0;
               }
            }
         } else {
            _log.info("1total key not found in context map");
         }

         if (taskContextMap.containsKey("1successCount")) {
            Object scObj = taskContextMap.get("1successCount");
            _log.info("Found 1successCount in context map. Value: " + scObj + ", Type: " + (scObj != null ? scObj.getClass().getSimpleName() : "null"));
            if (scObj instanceof Number) {
               successCount = ((Number)scObj).intValue();
               _log.info("Parsed successCount as Number: " + successCount);
            } else if (scObj instanceof String) {
               try {
                  successCount = Integer.parseInt((String)scObj);
                  _log.info("Parsed successCount as String: " + successCount);
               } catch (NumberFormatException var18) {
                  _log.warn("Failed to parse successCount from String: " + scObj + ", defaulting to 0");
                  successCount = 0;
               }
            }
         } else {
            _log.info("1successCount key not found in context map");
         }

         if (taskContextMap.containsKey("1failedCount")) {
            Object fcObj = taskContextMap.get("1failedCount");
            _log.info("Found 1failedCount in context map. Value: " + fcObj + ", Type: " + (fcObj != null ? fcObj.getClass().getSimpleName() : "null"));
            if (fcObj instanceof Number) {
               failedCount = ((Number)fcObj).intValue();
               _log.info("Parsed failedCount as Number: " + failedCount);
            } else if (fcObj instanceof String) {
               try {
                  failedCount = Integer.parseInt((String)fcObj);
                  _log.info("Parsed failedCount as String: " + failedCount);
               } catch (NumberFormatException var17) {
                  _log.warn("Failed to parse failedCount from String: " + fcObj + ", defaulting to 0");
                  failedCount = 0;
               }
            }
         } else {
            _log.info("1failedCount key not found in context map");
         }

         response.put("progress", progress);
         response.put("statusText", statusText);
         response.put("processed", processed);
         response.put("total", total);
         response.put("successCount", successCount);
         response.put("failedCount", failedCount);
         _log.info("Progress response values - progress: " + progress + ", statusText: " + statusText + ", processed: " + processed + ", total: " + total + ", successCount: " + successCount + ", failedCount: " + failedCount);
         if (backgroundTask.getStatus() != 3 && backgroundTask.getStatus() != 2) {
            _log.info("Background task still in progress. Status: " + backgroundTask.getStatus());
         } else {
            var10000 = _log;
            int var31 = backgroundTask.getStatus();
            var10000.info("Background task completed with status: " + var31);
            Object successMap = taskContextMap.get("1successMap");
            Object failedMap = taskContextMap.get("1failedMap");
            _log.info("Success map present: " + (successMap != null));
            _log.info("Failed map present: " + (failedMap != null));
            if (successMap != null && successMap instanceof List) {
               List<?> successList = (List)successMap;
               _log.info("Success map is a List with size: " + successList.size());
               response.put("successList", JSONFactoryUtil.createJSONArray(JSONFactoryUtil.looseSerialize(successMap)));
               _log.info("Success list added to response");
            }

            if (failedMap != null && failedMap instanceof List) {
               List<?> failedList = (List)failedMap;
               _log.info("Failed map is a List with size: " + failedList.size());
               response.put("failedList", JSONFactoryUtil.createJSONArray(JSONFactoryUtil.looseSerialize(failedMap)));
               _log.info("Failed list added to response");
            }
         }

         this.writeJSON(resourceRequest, resourceResponse, response);
         _log.info("JSON response written successfully for backgroundTaskId: " + backgroundTaskId);
      } catch (Exception e) {
         _log.error("Error getting progress for background task: " + backgroundTaskId, e);
         JSONObject errorResponse = JSONFactoryUtil.createJSONObject();
         errorResponse.put("error", "Failed to get progress: " + e.getMessage());
         this.writeJSON(resourceRequest, resourceResponse, errorResponse);
         _log.info("Error response written for backgroundTaskId: " + backgroundTaskId);
      }

   }

   private void writeJSON(ResourceRequest resourceRequest, ResourceResponse resourceResponse, JSONObject jsonObject) {
      try {
         String json = jsonObject.toString();
         resourceResponse.setContentType("application/json");
         resourceResponse.getWriter().write(json);
      } catch (Exception e) {
         _log.error("Error writing JSON response", e);
      }

   }

   public void getUserRoles(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      String adid = ParamUtil.getString(resourceRequest, "adid");
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         long companyId = PortalUtil.getCompanyId(resourceRequest);
         User user = UserLocalServiceUtil.fetchUserByScreenName(companyId, adid);
         if (user != null) {
            response.put("success", true);
            response.put("adid", user.getScreenName());
            response.put("name", user.getFullName());
            response.put("email", user.getEmailAddress());
            List<Role> userRoles = user.getRoles();
            String roleType = this.determineRoleType(userRoles);
            response.put("roleType", roleType);
         } else {
            response.put("success", false);
            response.put("message", "User not found with ADID: " + adid);
         }
      } catch (Exception e) {
         _log.error("Error getting user roles", e);
         response.put("success", false);
         response.put("message", e.getMessage());
      }

      this.writeJSON(resourceRequest, resourceResponse, response);
   }

   private String determineRoleType(List<Role> userRoles) {
      boolean hasPSRS = false;
      boolean hasBranchMaker = false;
      boolean hasBranchChecker = false;
      boolean hasZoneMaker = false;
      boolean hasZoneChecker = false;
      boolean hasRADZM = false;
      boolean hasNotingAuthority = false;

      for(Role role : userRoles) {
         String roleName = role.getName();
         if (roleName.equals("PSRS")) {
            hasPSRS = true;
         } else if (roleName.equals("PSRS Branch Maker")) {
            hasBranchMaker = true;
         } else if (roleName.equals("PSRS Branch Checker")) {
            hasBranchChecker = true;
         } else if (roleName.equals("PSRS Zone Maker")) {
            hasZoneMaker = true;
         } else if (roleName.equals("PSRS Zone Checker")) {
            hasZoneChecker = true;
         } else if (roleName.equals("PSRS RA DZM")) {
            hasRADZM = true;
         } else if (roleName.equals("PSRS Noting Authority")) {
            hasNotingAuthority = true;
         }
      }

      if (hasBranchChecker && hasBranchMaker && hasPSRS) {
         return "Branch Checker";
      } else if (hasBranchMaker && hasPSRS) {
         return "Branch Maker";
      } else if (hasZoneChecker && hasZoneMaker && hasPSRS) {
         return "Zone Checker";
      } else if (hasZoneMaker && hasPSRS) {
         return "Zone Maker";
      } else if (hasRADZM && hasPSRS) {
         return "Recommending Authority (RA DZM)";
      } else if (hasNotingAuthority && hasPSRS) {
         return "Noting Authority";
      } else if (hasPSRS) {
         return "PSRS Base Role";
      } else {
         return "No PSRS Role Assigned";
      }
   }

   public void assignRole(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      String adid = ParamUtil.getString(resourceRequest, "adid");
      String roleType = ParamUtil.getString(resourceRequest, "roleType");
      JSONObject response = JSONFactoryUtil.createJSONObject();
      _log.info("Assign role request - ADID: {}, RoleType: {}" + adid + roleType);

      try {
         if (Validator.isNull(adid)) {
            response.put("success", false);
            response.put("message", "ADID is required");
            this.writeJSON(resourceRequest, resourceResponse, response);
            return;
         }

         if (Validator.isNull(roleType)) {
            response.put("success", false);
            response.put("message", "Role type is required");
            this.writeJSON(resourceRequest, resourceResponse, response);
            return;
         }

         long companyId = PortalUtil.getCompanyId(resourceRequest);
         User user = UserLocalServiceUtil.fetchUserByScreenName(companyId, adid);
         if (user == null) {
            _log.warn("User not found with ADID: {}" + adid);
            response.put("success", false);
            response.put("message", "User not found with ADID: " + adid);
            this.writeJSON(resourceRequest, resourceResponse, response);
            return;
         }

         Log var10000 = _log;
         long var10001 = user.getUserId();
         var10000.info("User found - ID: {}, Name: {}, ADID: {}" + var10001 + user.getFullName() + user.getScreenName());
         List<Role> rolesToAssign = this.getRolesForType(companyId, roleType);
         if (rolesToAssign.isEmpty()) {
            _log.warn("No roles found for roleType: {}" + roleType);
            response.put("success", false);
            response.put("message", "Invalid role type: " + roleType);
            this.writeJSON(resourceRequest, resourceResponse, response);
            return;
         }

         List<Role> existingUserRoles = user.getRoles();
         List<String> removedRoles = new ArrayList();

         for(Role role : existingUserRoles) {
            if (role.getName().startsWith("PSRS")) {
               RoleLocalServiceUtil.deleteUserRole(user.getUserId(), role.getRoleId());
               removedRoles.add(role.getName());
               var10000 = _log;
               String var22 = role.getName();
               var10000.info("Removed role: {} from user: {}" + var22 + user.getScreenName());
            }
         }

         List<String> assignedRoles = new ArrayList();

         for(Role role : rolesToAssign) {
            if (role != null) {
               RoleLocalServiceUtil.addUserRole(user.getUserId(), role.getRoleId());
               assignedRoles.add(role.getName());
               var10000 = _log;
               String var23 = role.getName();
               var10000.info("Assigned role: {} to user: {}" + var23 + user.getScreenName());
            }
         }

         String message = String.format("Roles assigned successfully to %s (ADID: %s). Removed: %d roles. Assigned: %s", user.getFullName(), user.getScreenName(), removedRoles.size(), String.join(", ", assignedRoles));
         var10000 = _log;
         String var24 = user.getScreenName();
         var10000.info("Role assignment completed - User: {}, Assigned: {}" + var24 + assignedRoles);
         response.put("success", true);
         response.put("message", message);
         response.put("assignedRoles", JSONFactoryUtil.createJSONArray(assignedRoles));
         response.put("removedRoles", JSONFactoryUtil.createJSONArray(removedRoles));
         response.put("userName", user.getFullName());
         response.put("adid", user.getScreenName());
      } catch (Exception e) {
         _log.error("Error assigning role to ADID: {} - {}" + adid + e.getMessage() + e);
         response.put("success", false);
         response.put("message", "Error assigning role: " + e.getMessage());
      }

      this.writeJSON(resourceRequest, resourceResponse, response);
   }

   private List<Role> getRolesForType(long companyId, String roleType) {
      List<Role> roles = new ArrayList();

      try {
         _log.info("Getting roles for type: {} in company: {}" + roleType + companyId);
         Role psrsRole = null;

         try {
            psrsRole = RoleLocalServiceUtil.getRole(companyId, "PSRS");
            if (psrsRole == null) {
               _log.error("PSRS role not found in company: {}" + companyId);
               return roles;
            }

            roles.add(psrsRole);
         } catch (Exception e) {
            _log.error("PSRS role not found in company: {}" + companyId, e);
            return roles;
         }

         switch (roleType) {
            case "branch_maker":
               Role branchMaker = RoleLocalServiceUtil.getRole(companyId, "PSRS Branch Maker");
               if (branchMaker != null) {
                  roles.add(branchMaker);
                  _log.info("Added PSRS Branch Maker role");
               } else {
                  _log.warn("PSRS Branch Maker role not found");
               }
               break;
            case "branch_checker":
               Role branchMakerRole = RoleLocalServiceUtil.getRole(companyId, "PSRS Branch Maker");
               Role branchCheckerRole = RoleLocalServiceUtil.getRole(companyId, "PSRS Branch Checker");
               if (branchMakerRole != null) {
                  roles.add(branchMakerRole);
                  _log.info("Added PSRS Branch Maker role");
               } else {
                  _log.warn("PSRS Branch Maker role not found");
               }

               if (branchCheckerRole != null) {
                  roles.add(branchCheckerRole);
                  _log.info("Added PSRS Branch Checker role");
               } else {
                  _log.warn("PSRS Branch Checker role not found");
               }
               break;
            case "zone_maker":
               Role zoneMaker = RoleLocalServiceUtil.getRole(companyId, "PSRS Zone Maker");
               if (zoneMaker != null) {
                  roles.add(zoneMaker);
                  _log.info("Added PSRS Zone Maker role");
               } else {
                  _log.warn("PSRS Zone Maker role not found");
               }
               break;
            case "zone_checker":
               Role zoneMakerRole = RoleLocalServiceUtil.getRole(companyId, "PSRS Zone Maker");
               Role zoneCheckerRole = RoleLocalServiceUtil.getRole(companyId, "PSRS Zone Checker");
               if (zoneMakerRole != null) {
                  roles.add(zoneMakerRole);
                  _log.info("Added PSRS Zone Maker role");
               } else {
                  _log.warn("PSRS Zone Maker role not found");
               }

               if (zoneCheckerRole != null) {
                  roles.add(zoneCheckerRole);
                  _log.info("Added PSRS Zone Checker role");
               } else {
                  _log.warn("PSRS Zone Checker role not found");
               }
               break;
            case "ra_dzm":
               Role raDzmRole = RoleLocalServiceUtil.getRole(companyId, "PSRS RA DZM");
               if (raDzmRole != null) {
                  roles.add(raDzmRole);
                  _log.info("Added PSRS RA DZM role");
               } else {
                  _log.warn("PSRS RA DZM role not found");
               }
               break;
            case "noting_authority":
               Role notingAuthorityRole = RoleLocalServiceUtil.getRole(companyId, "PSRS Noting Authority");
               if (notingAuthorityRole != null) {
                  roles.add(notingAuthorityRole);
                  _log.info("Added PSRS Noting Authority role");
               } else {
                  _log.warn("PSRS Noting Authority role not found");
               }
               break;
            default:
               _log.warn("Unknown role type: {}" + roleType);
               return new ArrayList();
         }

         _log.info("Total roles to assign: {}" + roles.size());
         return roles;
      } catch (Exception e) {
         _log.error("Error getting roles for type: " + roleType, e);
         return new ArrayList();
      }
   }

   public void getRoleCount(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         long companyId = PortalUtil.getCompanyId(resourceRequest);
         Map<String, Integer> roleCounts = new HashMap();
         Map<String, List<String>> roleADIDs = new HashMap();
         String[] roleNames = new String[]{"PSRS", "PSRS Branch Maker", "PSRS Branch Checker", "PSRS Zone Maker", "PSRS Zone Checker", "PSRS RA DZM", "PSRS Noting Authority"};

         for(String roleName : roleNames) {
            try {
               Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
               List<User> usersWithRole = UserLocalServiceUtil.getRoleUsers(role.getRoleId());
               int count = usersWithRole.size();
               List<String> adids = new ArrayList();

               for(User user : usersWithRole) {
                  adids.add(user.getScreenName());
               }

               String key = roleName.replace(" ", "_");
               roleCounts.put(key, count);
               roleADIDs.put(key, adids);
               _log.info("Role: {} - Count: {}" + roleName + count);
            } catch (Exception e) {
               _log.warn("Error processing role: {}" + roleName, e);
               roleCounts.put(roleName.replace(" ", "_"), 0);
               roleADIDs.put(roleName.replace(" ", "_"), new ArrayList());
            }
         }

         response.put("success", true);
         response.put("counts", JSONFactoryUtil.createJSONObject(roleCounts));
         response.put("adids", JSONFactoryUtil.createJSONObject(roleADIDs));
      } catch (Exception e) {
         _log.error("Error getting role counts", e);
         response.put("success", false);
         response.put("message", e.getMessage());
      }

      this.writeJSON(resourceRequest, resourceResponse, response);
   }
}
