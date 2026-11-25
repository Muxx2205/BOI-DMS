package boi.background.track.portlet;

import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=BoiBackgroundTrack", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=boi_background_track_BoiBackgroundTrackPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class BoiBackgroundTrackPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(BoiBackgroundTrackPortlet.class);
   private static final int MAX_RESULTS = 1000;
   private static final String[] ALLOWED_FILTER_TYPES = new String[]{"startsWith", "endsWith", "contains", "equalsIgnoreCase", "exact"};

   public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
      resourceResponse.setProperty("Cache-Control", "no-cache, no-store, must-revalidate");
      String resourceId = resourceRequest.getResourceID();

      try {
         if (!this.hasPermission(resourceRequest)) {
            this.sendErrorResponse(resourceRequest, resourceResponse, "You don't have permission to access this resource", 403);
            return;
         }

         if ("filterTasks".equals(resourceId)) {
            this.filterBackgroundTasks(resourceRequest, resourceResponse);
         } else if ("deleteTask".equals(resourceId)) {
            this.deleteTask(resourceRequest, resourceResponse);
         } else {
            super.serveResource(resourceRequest, resourceResponse);
         }
      } catch (Exception e) {
         _log.error("Error in serveResource: " + e.getMessage(), e);
         this.sendErrorResponse(resourceRequest, resourceResponse, "Internal server error: " + e.getMessage(), 500);
      }

   }

   private boolean hasPermission(ResourceRequest resourceRequest) {
      try {
         HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(resourceRequest);
         PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(PortalUtil.getUser(httpRequest));
         return permissionChecker.isCompanyAdmin() || permissionChecker.isOmniadmin() || permissionChecker.hasPermission(0L, "com.liferay.portal.background.task", 0L, "VIEW");
      } catch (Exception e) {
         _log.error("Error checking permission: " + e.getMessage());
         return false;
      }
   }

   public void filterBackgroundTasks(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      long startTime = System.currentTimeMillis();
      String filterType = ParamUtil.getString(resourceRequest, "filterType");
      String filterText = ParamUtil.getString(resourceRequest, "filterText");
      if (!this.isValidFilterType(filterType)) {
         this.sendErrorResponse(resourceRequest, resourceResponse, "Invalid filter type", 400);
      } else if (!Validator.isNull(filterText) && filterText.length() <= 100) {
         JSONObject response = JSONFactoryUtil.createJSONObject();
         JSONArray tasksArray = JSONFactoryUtil.createJSONArray();

         try {
            int start = ParamUtil.getInteger(resourceRequest, "start", -1);
            int end = ParamUtil.getInteger(resourceRequest, "end", -1);
            List<BackgroundTask> tasks;
            if (start >= 0 && end > start) {
               tasks = BackgroundTaskLocalServiceUtil.getBackgroundTasks(start, end);
            } else {
               tasks = BackgroundTaskLocalServiceUtil.getBackgroundTasks(-1, -1);
            }

            if (tasks.size() > 1000) {
               tasks = tasks.subList(0, 1000);
               response.put("truncated", true);
               response.put("totalCount", tasks.size());
            }

            for(BackgroundTask task : (List)tasks.parallelStream().filter((taskx) -> {
               try {
                  String name = taskx.getName();
                  return this.matchFilter(name, filterType, filterText);
               } catch (Exception e) {
                  _log.warn("Error filtering task: " + e.getMessage());
                  return false;
               }
            }).collect(Collectors.toList())) {
               JSONObject obj = this.createTaskJSON(task);
               tasksArray.put(obj);
            }

            response.put("success", true);
            response.put("tasks", tasksArray);
            response.put("count", tasksArray.length());
            response.put("processingTime", System.currentTimeMillis() - startTime);
         } catch (Exception e) {
            _log.error("Error filtering background tasks: " + e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve tasks: " + e.getMessage());
         }

         this.writeJSON(resourceRequest, resourceResponse, response);
      } else {
         this.sendErrorResponse(resourceRequest, resourceResponse, "Filter text cannot be empty and must be less than 100 characters", 400);
      }
   }

   private JSONObject createTaskJSON(BackgroundTask task) {
      JSONObject obj = JSONFactoryUtil.createJSONObject();

      try {
         obj.put("taskId", task.getBackgroundTaskId());
         obj.put("name", this.sanitizeString(task.getName()));
         obj.put("status", BackgroundTaskConstants.getStatusLabel(task.getStatus()));
         obj.put("statusCode", task.getStatus());
         obj.put("createDate", task.getCreateDate() != null ? task.getCreateDate().getTime() : null);
         obj.put("completionDate", task.getCompletionDate() != null ? task.getCompletionDate().getTime() : null);
         Map<String, Serializable> contextMap = task.getTaskContextMap();
         JSONObject contextJson = JSONFactoryUtil.createJSONObject();
         if (contextMap != null && !contextMap.isEmpty()) {
            int count = 0;

            for(Map.Entry<String, Serializable> entry : contextMap.entrySet()) {
               if (count++ >= 50) {
                  break;
               }

               String key = this.sanitizeString((String)entry.getKey());
               String value = entry.getValue() != null ? this.sanitizeString(String.valueOf(entry.getValue())) : "null";
               if (value.length() > 500) {
                  value = value.substring(0, 500) + "...";
               }

               contextJson.put(key, value);
            }
         }

         obj.put("contextMap", contextJson);
      } catch (Exception e) {
         Log var10000 = _log;
         long var10001 = task.getBackgroundTaskId();
         var10000.warn("Error creating task JSON for task " + var10001 + ": " + e.getMessage());
         obj.put("error", "Error serializing task data");
      }

      return obj;
   }

   private String sanitizeString(String input) {
      return input == null ? "" : input.replaceAll("[<>\"'&]", "");
   }

   private boolean isValidFilterType(String type) {
      for(String allowed : ALLOWED_FILTER_TYPES) {
         if (allowed.equals(type)) {
            return true;
         }
      }

      return false;
   }

   private boolean matchFilter(String name, String type, String text) {
      if (!Validator.isNull(name) && !Validator.isNull(text)) {
         String lowerName = name.toLowerCase();
         String lowerText = text.toLowerCase();
         switch (type) {
            case "startsWith":
               return name.startsWith(text);
            case "endsWith":
               return name.endsWith(text);
            case "contains":
               return name.contains(text);
            case "equalsIgnoreCase":
               return name.equalsIgnoreCase(text);
            case "exact":
               return name.equals(text);
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public void deleteTask(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
      long taskId = ParamUtil.getLong(resourceRequest, "taskId");
      if (taskId <= 0L) {
         this.sendErrorResponse(resourceRequest, resourceResponse, "Invalid task ID", 400);
      } else {
         JSONObject response = JSONFactoryUtil.createJSONObject();

         try {
            BackgroundTask task = BackgroundTaskLocalServiceUtil.fetchBackgroundTask(taskId);
            if (task == null) {
               response.put("success", false);
               response.put("message", "Task not found with ID: " + taskId);
               this.writeJSON(resourceRequest, resourceResponse, response);
               return;
            }

            _log.info("Deleting background task: " + taskId + " - " + task.getName());
            BackgroundTaskLocalServiceUtil.deleteBackgroundTask(taskId);
            response.put("success", true);
            response.put("message", "Task deleted successfully");
            response.put("taskId", taskId);
         } catch (Exception e) {
            _log.error("Error deleting task " + taskId + ": " + e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete task: " + e.getMessage());
         }

         this.writeJSON(resourceRequest, resourceResponse, response);
      }
   }

   private void sendErrorResponse(ResourceRequest request, ResourceResponse response, String message, int status) throws IOException {
      JSONObject errorResponse = JSONFactoryUtil.createJSONObject();
      errorResponse.put("success", false);
      errorResponse.put("message", message);
      errorResponse.put("status", status);
      response.setProperty("portlet.http-status-code", String.valueOf(status));
      this.writeJSON(request, response, errorResponse);
   }
}
