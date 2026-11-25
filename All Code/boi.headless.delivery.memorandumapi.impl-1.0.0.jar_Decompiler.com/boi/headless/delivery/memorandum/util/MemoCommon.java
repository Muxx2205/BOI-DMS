package boi.headless.delivery.memorandum.util;

import boi.headless.delivery.memorandum.configuration.ConfigurationUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Reference;

public class MemoCommon {
   private List<String> recommenders;
   private List<String> approvers;
   @Reference
   private ObjectDefinitionLocalService _objectDefinitionLocalService;
   @Reference
   private ObjectEntryLocalService _objectEntryLocalService;
   private Log log = LogFactoryUtil.getLog(MemoCommon.class);

   public JSONObject getMemoDetailWithLogs(long entryId, User user, MemoManager _manager, DefaultDTOConverterContext defaultDTOConverterContext) throws Exception {
      Log log = LogFactoryUtil.getLog(this.getClass());
      log.info("--------- Starting getMemoDetailWithLogs ---------");
      log.info("Requested Entry ID: " + entryId);
      long var10001 = user.getUserId();
      log.info("Requested By User: " + var10001 + " (" + user.getScreenName() + ")");
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         log.info("Fetching ObjectEntry for entryId: " + entryId);
         com.liferay.object.model.ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
         if (entry == null) {
            log.error("Memo entry not found for ID: " + entryId);
            response.put("errors", "Memo entry not found for ID: " + entryId);
            return response;
         }

         log.info("ObjectEntry found. Preparing memo JSON...");
         JSONObject memoJson = JSONFactoryUtil.createJSONObject();
         Map<String, Serializable> entryValues = entry.getValues();
         log.info("Total fields in entry: " + entryValues.size());
         entryValues.forEach((key, val) -> {
            log.info("Processing field: " + key + " = " + val);
            memoJson.put(key, val != null ? String.valueOf(val) : "");
         });
         log.info("All memo fields added. Fetching workflow logs...");

         try {
            this.addWorkflowLogs(memoJson, entryId, _manager, defaultDTOConverterContext);
            log.info("Workflow logs added successfully.");
         } catch (Exception var12) {
         }

         this.addCustomActions(memoJson, entry, user);
         response.put("memoData", memoJson);
         log.info("Final JSON response generated successfully.");
         log.info("--------- Completed getMemoDetailWithLogs ---------");
      } catch (Exception e) {
         log.error("Error in getMemoDetailWithLogs for entryId: " + entryId, e);
         response.put("errors", e.getMessage());
      }

      return response;
   }

   public JSONObject approveOrRejectMemo(HttpServletRequest request, User user, DLAppService dlAppService, MultipartBody multipartBody) {
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         long objectEntryId = ParamUtil.getLong(request, "entryId", 0L);
         String action = ParamUtil.getString(request, "action", "");
         String description = ParamUtil.getString(request, "description", "");
         com.liferay.object.model.ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(objectEntryId);
         if (entry == null) {
            return response.put("status", "error").put("message", "Object entry not found");
         }

         String currentAssigneeId = (String)entry.getValues().get("currentAssignedUser");
         if (!user.getScreenName().equalsIgnoreCase(currentAssigneeId)) {
            return response.put("status", "error").put("message", "User not authorized");
         }

         String fileURL = "";
         BinaryFile binaryFile = multipartBody.getBinaryFile("file");
         if (binaryFile != null) {
            JSONObject uploadErrors = JSONFactoryUtil.createJSONObject();
            FileUtil fileUtil = new FileUtil(multipartBody, "MEMORANDUM");
            long uploadFolderId = ConfigurationUtil.getDefaultFolderId();
            Map<String, BinaryFile> validateFile = fileUtil.validateFile(uploadErrors);
            if (MapUtil.isEmpty(validateFile)) {
               return uploadErrors;
            }

            List<FileEntry> workflowFileEntries = fileUtil.uploadFile(uploadFolderId, uploadErrors, (String)null, validateFile);
            FileEntry workflowFileEntry = workflowFileEntries != null && !workflowFileEntries.isEmpty() ? (FileEntry)workflowFileEntries.get(0) : null;
            if (uploadErrors.has("errors")) {
               return uploadErrors;
            }

            if (workflowFileEntry != null) {
               ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
               fileURL = DLURLHelperUtil.getDownloadURL(workflowFileEntry, workflowFileEntry.getFileVersion(), themeDisplay, "");
            }
         }

         String recommender = (String)entry.getValues().get("recommender");
         String approver = (String)entry.getValues().get("approver");
         String remarks = ParamUtil.getString(request, "remarks", "");
         this.init(recommender, approver);
         Map<String, Serializable> values = entry.getValues();
         if (action.equalsIgnoreCase("approve")) {
            String nextAssigneeId = this.next(currentAssigneeId);
            if (Validator.isNotNull(nextAssigneeId)) {
               if (nextAssigneeId.equalsIgnoreCase("Approved")) {
                  values.put("currentStatus", "Approved");
                  values.put("currentAssignedUser", "");
               } else {
                  values.put("currentAssignedUser", nextAssigneeId);
                  values.put("currentStatus", "Pending at " + nextAssigneeId);
                  response.put("assignee", nextAssigneeId);
               }
            }
         } else {
            if (!action.equalsIgnoreCase("reject")) {
               return response.put("status", "error").put("message", "Invalid action (approve/reject required)");
            }

            String previousAssigneeId = this.previous(currentAssigneeId);
            if (Validator.isNotNull(previousAssigneeId)) {
               if (previousAssigneeId.equalsIgnoreCase("Denied")) {
                  values.put("currentStatus", "Denied");
                  values.put("currentAssignedUser", "");
               } else {
                  values.put("currentAssignedUser", previousAssigneeId);
                  values.put("currentStatus", "Pending at " + previousAssigneeId);
                  response.put("assignee", previousAssigneeId);
               }
            }
         }

         values.put("description", description);
         entry.setValues(values);
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(user.getCompanyId());
         serviceContext.setUserId(entry.getUserId());
         serviceContext.setWorkflowAction(1);
         ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(entry.getExternalReferenceCode(), entry.getUserId(), 0L, entry.getObjectDefinitionId(), values, serviceContext);
         this.createWorkflowEntry(user, entry, action, remarks, fileURL);
         response.put("status", "success");
         response.put("action", action);
         response.put("remarks", remarks);
         response.put("entryId", objectEntryId);
      } catch (Exception e) {
         response.put("status", "error").put("message", e.getMessage());
         e.printStackTrace();
      }

      return response;
   }

   public JSONObject filterPageCustom(Page<ObjectEntry> page) {
      JSONObject result = JSONFactoryUtil.createJSONObject();
      JSONArray items = JSONFactoryUtil.createJSONArray();
      if (page != null && page.getItems() != null) {
         for(ObjectEntry entry : page.getItems()) {
            Map<String, Object> props = entry.getProperties();
            JSONObject obj = JSONFactoryUtil.createJSONObject(props);
            obj.put("id", entry.getId());
            obj.put("date", entry.getDateCreated());
            obj.put("modifiedDate", entry.getDateModified());
            obj.put("status", entry.getPropertyValue("currentStatus"));
            items.put(obj);
         }

         result.put("totalCount", items.length());
         result.put("items", items);
         return result;
      } else {
         result.put("totalCount", 0);
         result.put("items", items);
         return result;
      }
   }

   public com.liferay.object.model.ObjectEntry saveAsDraft(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId, User user, String referanceCode) {
      try {
         Map<String, Serializable> objectData = this.getObjectData(request, values, objectDefinitionId);
         return this.updateData(user, objectData, 2, referanceCode, objectDefinitionId, "Save as Draft");
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public com.liferay.object.model.ObjectEntry saveAsExport(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId, User user, String referanceCode) {
      try {
         Map<String, Serializable> objectData = this.getObjectData(request, values, objectDefinitionId);
         return this.updateData(user, objectData, 1, referanceCode, objectDefinitionId, "Save as Export");
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public com.liferay.object.model.ObjectEntry updateData(User user, Map<String, Serializable> values, int status, String referanceCode, long objectDefinitionId, String logStatus) throws Exception {
      ServiceContext serviceContext = new ServiceContext();
      serviceContext.setCompanyId(user.getCompanyId());
      serviceContext.setUserId(user.getUserId());
      serviceContext.setWorkflowAction(status);
      if (status == 1) {
         String recommenderSeq = (String)values.get("recommender");
         String approverSeq = (String)values.get("approver");
         this.init(recommenderSeq, approverSeq);
         String assigneeUserId = this.getFirst();
         if (assigneeUserId != null && !assigneeUserId.isBlank()) {
            values.put("currentAssignedUser", assigneeUserId);
         }

         values.put("currentStatus", "Pending at " + assigneeUserId);
      } else if (status == 2) {
         values.put("currentStatus", "Draft");
      }

      com.liferay.object.model.ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(referanceCode, user.getUserId(), 0L, objectDefinitionId, values, serviceContext);
      if (Validator.isNotNull(objectEntry)) {
         this.createWorkflowEntry(user, objectEntry, logStatus, logStatus, (String)null);
      }

      this.log.info("updateData method completed for ObjectDefinitionId: " + objectDefinitionId);
      return objectEntry;
   }

   private com.liferay.object.model.ObjectEntry createWorkflowEntry(User user, com.liferay.object.model.ObjectEntry objectEntry, String logStatus, String remarks, String fileURL) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(user.getCompanyId());
         serviceContext.setUserId(user.getUserId());
         Map<String, Serializable> workflowLogs = new HashMap();
         workflowLogs.put("memorandumId", objectEntry.getObjectEntryId());
         workflowLogs.put("customAction", logStatus);
         workflowLogs.put("actionBy", user.getScreenName());
         workflowLogs.put("createdDate", new Date());
         workflowLogs.put("remarks", remarks);
         if (Validator.isNotNull(fileURL)) {
            workflowLogs.put("file", fileURL);
         }

         this.log.info("Workflow logs prepared: " + workflowLogs);
         com.liferay.object.model.ObjectEntry workflowEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, user.getUserId(), 0L, ConfigurationUtil.getMemoWorkflowObjectDefinitionId(), workflowLogs, serviceContext);
         Object var10001 = workflowEntry != null ? workflowEntry.getObjectEntryId() : "null";
         this.log.info("Workflow entry created/updated: " + var10001);
         return workflowEntry;
      } catch (Exception var9) {
         return null;
      }
   }

   public Map<String, Serializable> getObjectData(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId) {
      this.log.info("Starting getObjectData for objectDefinitionId: " + objectDefinitionId);

      try {
         List<ObjectField> fields = ObjectFieldLocalServiceUtil.getObjectFields(objectDefinitionId);
         Log var10000 = this.log;
         int var10001 = fields.size();
         var10000.info("Retrieved " + var10001 + " fields for ObjectDefinitionId: " + objectDefinitionId);

         for(ObjectField field : fields) {
            String fieldName = field.getName();
            String dbType = field.getDBType();
            this.log.info("Processing field: " + fieldName + ", DBType: " + dbType);

            try {
               if (!"String".equalsIgnoreCase(dbType) && !"Clob".equalsIgnoreCase(dbType) && !"RichText".equalsIgnoreCase(dbType)) {
                  if ("Date".equalsIgnoreCase(dbType)) {
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                     sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                     Date defaultValue = new Date();
                     Date value = ParamUtil.getDate(request, fieldName, sdf, defaultValue);
                     this.log.info("ParamUtil date value for " + fieldName + ": " + value);
                     if (value.compareTo(defaultValue) == 0) {
                        value = (Date)this.getDefaultData(fieldName, values, (Serializable)null);
                        this.log.info("Default date value used for " + fieldName + ": " + value);
                     }

                     values.put(fieldName, value);
                     this.log.info("Added date field to values map: " + fieldName + " = " + value);
                  } else if ("Long".equalsIgnoreCase(dbType)) {
                     long value = ParamUtil.getLong(request, fieldName, 0L);
                     this.log.info("ParamUtil long value for " + fieldName + ": " + value);
                     if (value == 0L) {
                        value = (Long)this.getDefaultData(fieldName, values, 0L);
                        this.log.info("Default long value used for " + fieldName + ": " + value);
                     }

                     values.put(fieldName, value);
                     this.log.info("Added long field to values map: " + fieldName + " = " + value);
                  } else if ("Integer".equalsIgnoreCase(dbType)) {
                     int value = ParamUtil.getInteger(request, fieldName, 0);
                     this.log.info("ParamUtil integer value for " + fieldName + ": " + value);
                     if (value == 0) {
                        value = (Integer)this.getDefaultData(fieldName, values, 0);
                        this.log.info("Default integer value used for " + fieldName + ": " + value);
                     }

                     values.put(fieldName, value);
                     this.log.info("Added integer field to values map: " + fieldName + " = " + value);
                  } else {
                     this.log.warn("Unsupported DBType for field " + fieldName + ": " + dbType);
                  }
               } else {
                  String value = ParamUtil.getString(request, fieldName, "");
                  this.log.info("ParamUtil value for " + fieldName + ": " + value);
                  if ("".equalsIgnoreCase(value)) {
                     value = (String)this.getDefaultData(fieldName, values, "");
                     this.log.info("Default value used for " + fieldName + ": " + value);
                  }

                  values.put(fieldName, value);
                  this.log.info("Added field to values map: " + fieldName + " = " + value);
               }
            } catch (Exception e) {
               this.log.error("Error processing field " + fieldName, e);
            }
         }
      } catch (Exception e) {
         this.log.error("Error retrieving object fields for objectDefinitionId " + objectDefinitionId, e);
      }

      this.log.info("Completed getObjectData. Final values map: " + values);
      return values;
   }

   private Serializable getDefaultData(String fieldName, Map<String, Serializable> values, Serializable serializable) {
      return values.containsKey(fieldName) ? (Serializable)values.get(fieldName) : serializable;
   }

   public JSONObject getMemoPreData(MemoManager organizationManager, DefaultDTOConverterContext defaultDTOConverterContext) {
      JSONObject json = JSONFactoryUtil.createJSONObject();
      if (organizationManager == null) {
         json.put("success", false);
         json.put("message", "OrganizationManager is null");
         return json;
      } else {
         if (organizationManager.getBranchName() != null) {
            json.put("BRANCH", organizationManager.getBranchName());
         }

         if (organizationManager.getZoneName() != null) {
            json.put("ZONE", organizationManager.getZoneName());
            this.log.info("zone = " + organizationManager.getZoneName());
         }

         if (organizationManager.getNbgName() != null) {
            json.put("NBG", organizationManager.getNbgName());
            this.log.info("nbg = " + organizationManager.getNbgName());
         }

         if (organizationManager.getHoName() != null) {
            json.put("HO", organizationManager.getHoName());
            this.log.info("ho = " + organizationManager.getHoName());
         }

         Map<String, String> approver = new HashMap();
         Map<String, String> recommender = new HashMap();
         String buildOrgHierarchyQuery = this.buildOrgHierarchyQuery(organizationManager);
         this.log.info("buildOrgHierarchyQuery = " + buildOrgHierarchyQuery);
         this.log.info("objectId = " + ConfigurationUtil.getMemoUserMappingObjectDefinitionId());
         if (Validator.isNotNull(buildOrgHierarchyQuery)) {
            try {
               Page<ObjectEntry> listData = this.listData((String)null, (Filter)null, Pagination.of(-1, -1), (Sort[])null, ConfigurationUtil.getMemoUserMappingObjectDefinitionId(), buildOrgHierarchyQuery, organizationManager.getUser(), defaultDTOConverterContext, organizationManager.get_objectEntryManagerRegistry());
               this.log.info("listData = " + listData);
               if (listData != null && listData.getItems() != null) {
                  for(ObjectEntry entry : listData.getItems()) {
                     this.log.info("entry = " + entry);
                     String role = (String)entry.getPropertyValue("role");
                     String screenName = (String)entry.getPropertyValue("userScreenName");
                     String designation = (String)entry.getPropertyValue("designation");
                     boolean isActive = (Boolean)entry.getPropertyValue("active");
                     if (isActive) {
                        if (organizationManager.getUser().getScreenName().equalsIgnoreCase(screenName)) {
                           json.put("Designation", designation);
                        }

                        if ("RECOMMENDER".equalsIgnoreCase(role)) {
                           recommender.put(screenName, screenName);
                        } else if ("APPROVER".equalsIgnoreCase(role)) {
                           approver.put(screenName, screenName);
                        }
                     }
                  }

                  json.put("recommenders", recommender);
                  json.put("approvers", approver);
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return json;
      }
   }

   private Page<ObjectEntry> listData(String search, Filter filter, Pagination pagination, Sort[] sorts, long objectDefinitionId, String query, User user, DefaultDTOConverterContext dtoConverterContext, ObjectEntryManagerRegistry _objectEntryManagerRegistry) throws Exception {
      Page<ObjectEntry> page = null;

      try {
         this.log.info("Starting listData() method");
         this.log.info("objectDefinitionId: " + objectDefinitionId + ", query: " + query + ", user: " + user);
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
         if (objectDefinition == null) {
            this.log.error("ObjectDefinition not found for objectDefinitionId: " + objectDefinitionId);
            return page;
         }

         this.log.info("Retrieved ObjectDefinition: " + objectDefinition.getName());
         this.log.info("Created DTOConverterContext with objectDefinitionId: " + objectDefinition.getObjectDefinitionId());
         ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         this.log.info("Retrieved ObjectEntryManager: " + objectEntryManager);
         page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, dtoConverterContext, query, pagination, search, sorts);
         if (page != null) {
            this.log.info("Fetched " + page.getTotalCount() + " ObjectEntries.");
         } else {
            this.log.warn("No ObjectEntries found.");
         }
      } catch (Exception e) {
         this.log.error("Error occurred in listData method for objectDefinitionId: " + objectDefinitionId, e);
         e.printStackTrace();
      }

      return page;
   }

   public Page<ObjectEntry> getListData(String search, Filter filter, Pagination pagination, Sort[] sorts, String query, User user, MemoManager _manager, DefaultDTOConverterContext defaultDTOConverterContext) throws Exception {
      return this.listData(search, filter, pagination, sorts, _manager.getObjectDefinitionId(), query, user, defaultDTOConverterContext, _manager.get_objectEntryManagerRegistry());
   }

   private String buildOrgHierarchyQuery(MemoManager organizationManager) {
      List<String> filters = new ArrayList();
      if (organizationManager == null) {
         return null;
      } else {
         if (organizationManager.getBranchName() != null) {
            filters.add("orgType eq '" + organizationManager.getBranchName() + "'");
         } else if (organizationManager.getZoneName() != null) {
            filters.add("orgType eq '" + organizationManager.getZoneName() + "'");
         } else if (organizationManager.getNbgName() != null) {
            filters.add("orgType eq '" + organizationManager.getNbgName() + "'");
         } else if (organizationManager.getHoName() != null) {
            filters.add("orgType eq '" + organizationManager.getHoName() + "'");
         }

         return String.join(" and ", filters);
      }
   }

   private void addWorkflowLogs(JSONObject jsonObject, long objectEntryId, MemoManager _manager, DefaultDTOConverterContext defaultDTOConverterContext) throws Exception {
      this.log.info("Fetching workflow logs for memorandumId: " + objectEntryId);
      ObjectDefinition workflowlogs = ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(ConfigurationUtil.getMemoWorkflowObjectDefinitionId());
      if (workflowlogs == null) {
         this.log.error("WorkflowLogs ObjectDefinition NOT found!");
         jsonObject.put("logs", JSONFactoryUtil.createJSONArray());
      } else {
         this.log.info("WorkflowLogs ObjectDefinition loaded: " + workflowlogs.getName());
         ObjectEntryManager objectEntryManager = _manager.get_objectEntryManagerRegistry().getObjectEntryManager(workflowlogs.getStorageType());
         String query = "memorandumId eq " + objectEntryId;
         this.log.info("OData Query for logs: " + query);
         Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
         Sort[] sorts = new Sort[]{sort};
         this.log.info("Calling objectEntryManager.getObjectEntries()...");
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(workflowlogs.getCompanyId(), workflowlogs, workflowlogs.getScope(), (Aggregation)null, defaultDTOConverterContext, query, Pagination.of(-1, -1), (String)null, sorts);
         this.log.info("Workflow logs fetched. Count = " + page.getItems().size());
         jsonObject.put("logs", this.getArray(page));
         this.log.info("Workflow logs successfully added to response JSON.");
      }
   }

   private void addCustomActions(JSONObject jsonObject, com.liferay.object.model.ObjectEntry entry, User user) {
      String assignedUser = (String)entry.getValues().get("currentAssignedUser");
      if (assignedUser != null && assignedUser.equalsIgnoreCase(user.getScreenName())) {
         JSONArray buttons = JSONFactoryUtil.createJSONArray();
         buttons.put("Approve");
         buttons.put("Reject");
         jsonObject.put("actions", buttons);
      }

   }

   private JSONArray getArray(Page<ObjectEntry> page) {
      JSONArray items = JSONFactoryUtil.createJSONArray();
      if (Validator.isNotNull(page)) {
         page.getItems().forEach((x) -> {
            JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
            x.getProperties().forEach((c, d) -> itemObjects.put(c, d));
            itemObjects.put("action", x.getPropertyValue("action"));
            itemObjects.put("actionBy", x.getPropertyValue("actionBy"));
            itemObjects.put("remarks", x.getPropertyValue("remarks"));
            itemObjects.put("dateModified", x.getDateModified());
            itemObjects.put("dateCreated", x.getDateCreated());
            itemObjects.put("id", x.getPropertyValue("memoId"));
            items.put(itemObjects);
         });
      }

      return items;
   }

   private void init(String recommenderSeq, String approverSeq) {
      this.recommenders = recommenderSeq != null && !recommenderSeq.isEmpty() ? Arrays.asList(recommenderSeq.split("\\s*,\\s*")) : Arrays.asList();
      this.approvers = approverSeq != null && !approverSeq.isEmpty() ? Arrays.asList(approverSeq.split("\\s*,\\s*")) : Arrays.asList();
   }

   private String next(String current) {
      if (this.recommenders.contains(current)) {
         int index = this.recommenders.indexOf(current);
         if (index + 1 < this.recommenders.size()) {
            return (String)this.recommenders.get(index + 1);
         } else {
            return this.approvers.isEmpty() ? "Approved" : (String)this.approvers.get(0);
         }
      } else if (this.approvers.contains(current)) {
         int index = this.approvers.indexOf(current);
         return index + 1 < this.approvers.size() ? (String)this.approvers.get(index + 1) : "Approved";
      } else {
         return null;
      }
   }

   private String previous(String current) {
      if (this.approvers.contains(current)) {
         int index = this.approvers.indexOf(current);
         if (index - 1 >= 0) {
            return (String)this.approvers.get(index - 1);
         } else {
            return this.recommenders.isEmpty() ? "Denied" : (String)this.recommenders.get(this.recommenders.size() - 1);
         }
      } else if (this.recommenders.contains(current)) {
         int index = this.recommenders.indexOf(current);
         return index - 1 >= 0 ? (String)this.recommenders.get(index - 1) : "Denied";
      } else {
         return null;
      }
   }

   private String getFirst() {
      if (!this.recommenders.isEmpty()) {
         return (String)this.recommenders.get(0);
      } else {
         return !this.approvers.isEmpty() ? (String)this.approvers.get(0) : null;
      }
   }

   private String getLast() {
      if (!this.approvers.isEmpty()) {
         return (String)this.approvers.get(this.approvers.size() - 1);
      } else {
         return !this.recommenders.isEmpty() ? (String)this.recommenders.get(this.recommenders.size() - 1) : null;
      }
   }
}
