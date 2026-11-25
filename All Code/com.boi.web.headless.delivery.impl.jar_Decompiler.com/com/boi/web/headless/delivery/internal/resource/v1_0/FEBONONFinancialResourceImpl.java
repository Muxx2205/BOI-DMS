package com.boi.web.headless.delivery.internal.resource.v1_0;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.application.CustomWorkflowLogs;
import com.boi.headless.rest.common.application.FileHandler;
import com.boi.headless.rest.common.application.ObjectConverter;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.handler.AssigmentUsers;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.rest.common.validators.RequestValidator;
import com.boi.web.headless.delivery.dto.v1_0.BOIHeadlessResponse;
import com.boi.web.headless.delivery.dto.v1_0.CustomBOIHeadlessResponse;
import com.boi.web.headless.delivery.dto.v1_0.CustomBOIHeadlessResponseAsList;
import com.boi.web.headless.delivery.resource.v1_0.FEBONONFinancialResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTransition;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/febonon-financial.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FEBONONFinancialResource.class}
)
public class FEBONONFinancialResourceImpl extends BaseFEBONONFinancialResourceImpl {
   private Set<String> statusList = new LinkedHashSet<String>() {
      private static final long serialVersionUID = 1L;

      {
         this.add("pending with branch maker");
         this.add("pending with ho checker");
         this.add("approved");
      }
   };
   @Reference
   private WorkflowInstanceManager _workflowInstanceManager;
   private Map<String, String> fileUpdload = BOIDMSConstants.getObjectFileNames("FE_BO_NON_Financial");
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private long workflowObjectDefinitionId = 0L;
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";
   private BOIWorkflowConfiguation boiWorkflowConfiguation;
   @Reference
   private DDMExpressionFactory _ddmExpressionFactory;
   @Reference
   private ObjectFieldSettingLocalService _objectFieldSettingLocalService;
   @Reference
   private ObjectFieldLocalService _objectFieldLocalService;
   @Reference
   private DLAppService _dlAppService;
   private static final Log log = LogFactoryUtil.getLog(FEBONONFinancialResourceImpl.class);
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   @Reference
   private ObjectDefinitionLocalService _objectDefinitionLocalService;
   @Reference
   private ObjectEntryLocalService _objectEntryLocalService;
   @Reference
   private UserLocalService _userLocalService;
   @Reference
   private Portal _portal;
   @Reference
   private WorkflowTaskManager _workflowTaskManager;

   public BOIHeadlessResponse getFEBONONFinancials(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      return this.listData(search, filter, pagination, sorts);
   }

   public BOIHeadlessResponse getFEBONONFinancial(@NotNull Integer id) throws Exception {
      return this.getSingleEntry(id);
   }

   public BOIHeadlessResponse addFEBONONFinancialAsDraft(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (entryId != 0L) {
            objectEntry = apiRequestHandler.getObjectEntry(entryId);
         }

         objectEntry = this.saveAsDraft(this.contextHttpServletRequest, multipartBody, 2, this.objectDefinitionId, this.defaultFolderId, this.fileUpdload, apiRequestHandler, objectEntry);
         (new CustomWorkflowLogs(this.contextUser)).createWorkflowEntryLogs(objectEntry, "Draft", this.workflowObjectDefinitionId);
         jsonObject.put("entryId", objectEntry.getObjectEntryId());
         return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         log.error(e);
         return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
      }
   }

   public BOIHeadlessResponse addFEBONONFinancialAsExport(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (entryId == 0L) {
            jsonObject.put("entryId", entryId);
            return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
         }

         objectEntry = apiRequestHandler.getObjectEntry(entryId);
         if (objectEntry.getObjectDefinitionId() == this.objectDefinitionId) {
            objectEntry = this.saveAsExport(this.contextHttpServletRequest, multipartBody, 1, this.objectDefinitionId, this.defaultFolderId, this.fileUpdload, apiRequestHandler, objectEntry);
            jsonObject.put("entryId", objectEntry.getObjectEntryId());
            return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         log.error(e);
      }

      return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      boolean isMaker = apiRequestHandler.getUserHierarchy().isMaker();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      String query = this.getUserFilterQuery(apiRequestHandler);
      Page<ObjectEntry> page = null;
      if (!Validator.isBlank(query)) {
         page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
         boolean isExport = ParamUtil.get(this.contextHttpServletRequest, "export", false);
         String url = "not-available";
         if (apiRequestHandler.isHaveExportToRole()) {
            if (isExport) {
               apiRequestHandler.exportToExcelData(query, this.objectDefinitionId, this.defaultFolderId);
               url = "in-progress";
            } else {
               url = apiRequestHandler.getTaskComplatedURL(this.objectDefinitionId);
            }
         }

         log.debug("url : " + url);
         return new CustomBOIHeadlessResponseAsList(page, isMaker, url, this.statusList);
      } else {
         return new CustomBOIHeadlessResponseAsList(page, isMaker, "", this.statusList);
      }
   }

   public String getUserFilterQuery(APIRequestHandler apiRequestHandler) {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status", (String)null);
      this.addHierarchyFilter(queryList, apiRequestHandler.getUserHierarchy());
      this.addStatusFilter(queryList, status, apiRequestHandler.getUserHierarchy());
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      if (!this.containsHierarchyFilters(queryResult) && !apiRequestHandler.getUserHierarchy().isHOChecker()) {
         log.debug("Final queryResult:  Blank");
         return "";
      } else {
         log.debug("Final queryResult:  " + queryResult);
         return queryResult;
      }
   }

   private int getStatusCode(String status) {
      return WorkflowConstants.getLabelStatus(status);
   }

   private void addStatusFilter(List<String> queryList, String status, UserHierarchy userHierarchy) {
      log.debug("Request required status " + status);
      if (!userHierarchy.isMaker() && Validator.isNull(status)) {
         queryList.add("status ne 2");
      }

      if (Validator.isNotNull(status)) {
         int statusCode = this.getStatusCode(status);
         if (statusCode != -1) {
            queryList.add("status eq " + statusCode);
         } else {
            queryList.add("customStatusByHandler eq '" + status.toLowerCase() + "'");
            queryList.add("status ne 0");
         }
      }

   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + (String)isoRange.get("toDate");
         log.debug("Request required dateFilter  " + dateFilter);
         queryList.add(dateFilter);
      }

   }

   public Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      LocalDate fromDate = LocalDate.parse(fromDateStr);
      LocalDate toDate = LocalDate.parse(toDateStr);
      ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime toDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("fromDate", isoFormatter.format(fromDateTime));
      result.put("toDate", isoFormatter.format(toDateTime));
      return result;
   }

   private boolean containsHierarchyFilters(String queryResult) {
      return queryResult.contains("branchCode") || queryResult.contains("nBGCode") || queryResult.contains("zone");
   }

   private void addHierarchyFilter(List<String> queryList, UserHierarchy userHierarchy) {
      if (!Validator.isBlank(userHierarchy.getBranchName())) {
         queryList.add("branchCode eq '" + userHierarchy.getBranchName() + "'");
      } else if (!Validator.isBlank(userHierarchy.getZoneName())) {
         queryList.add("zone eq '" + userHierarchy.getZoneName() + "'");
      } else {
         if (!Validator.isBlank(userHierarchy.getNbgName())) {
            queryList.add("nBGCode eq '" + userHierarchy.getNbgName() + "'");
         }

      }
   }

   private BOIHeadlessResponse getSingleEntry(Number id) throws PortalException {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.getObjectEntry(id.longValue());
      if (Validator.isNotNull(objectEntry) && objectEntry.getObjectDefinitionId() == objectDefinition.getObjectDefinitionId()) {
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject jsonObject = apiRequestHandler.convertEntryToArray(values);
         jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
         this._getWorkFlowActionList(apiRequestHandler, objectDefinition, objectEntry, jsonObject);
         jsonObject.put("entryId", objectEntry.getObjectEntryId());
         apiRequestHandler.addWorkflowLogs(jsonObject, objectEntry.getObjectEntryId(), this.workflowObjectDefinitionId);
         return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
      } else {
         return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject(), Status.NOT_ACCEPTABLE);
      }
   }

   private void _getWorkFlowActionList(APIRequestHandler apiRequestHandler, ObjectDefinition objectDefinition, com.liferay.object.model.ObjectEntry objectEntry, JSONObject jsonObject) {
      List<WorkflowTask> list = (List)apiRequestHandler.getWorkflowTasks(objectDefinition, this.contextUser, objectEntry.getObjectEntryId()).stream().sorted((w1, w2) -> w2.getCreateDate().compareTo(w1.getCreateDate())).collect(Collectors.toList());
      if ((Validator.isNull(list) || list.isEmpty()) && this.boiWorkflowConfiguation.selfAssignmentIsAllow()) {
         try {
            (new AssigmentUsers(this.boiWorkflowConfiguation)).assignmentForAllUsers(objectEntry, objectDefinition, apiRequestHandler);
            list = (List)apiRequestHandler.getWorkflowTasks(objectDefinition, this.contextUser, objectEntry.getObjectEntryId()).stream().sorted((w1, w2) -> w2.getCreateDate().compareTo(w1.getCreateDate())).collect(Collectors.toList());
         } catch (Exception e) {
            log.error("While Assigment Getting Exception " + e.getMessage());
         }
      }

      if (Validator.isNotNull(list) && list.size() > 0) {
         WorkflowTask workflow = (WorkflowTask)list.get(0);
         List<WorkflowTransition> actions = apiRequestHandler.getWorkflowTransitions(((WorkflowTask)list.get(0)).getWorkflowTaskId());
         JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
         actions.forEach((action) -> {
            JSONObject actionJson = JSONFactoryUtil.createJSONObject();
            actionJson.put("actionName", action.getLabel(Locale.getDefault()));
            actionJson.put("transitionId", action.getName());
            actionJson.put("workflowTaskId", workflow.getWorkflowTaskId());
            jsonArray.put(actionJson);
         });
         jsonObject.put("actions", jsonArray);
      }

   }

   private com.liferay.object.model.ObjectEntry saveAsDraft(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload, APIRequestHandler apiRequestHandler, com.liferay.object.model.ObjectEntry objectEntry) throws Exception {
      String referanceCode = null;
      boolean isBranchMaker = apiRequestHandler.getUserHierarchy().isMaker();
      if (!isBranchMaker) {
         throw new Exception("State is not for Draft and role is miss-matched");
      } else {
         if (Validator.isNotNull(objectEntry)) {
            Map<String, Serializable> values = objectEntry.getValues();
            referanceCode = objectEntry.getExternalReferenceCode();
            String uniqueId = null;
            if (values.containsKey("serialNo")) {
               uniqueId = String.valueOf(values.get("serialNo"));
            }

            apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
            values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
         } else {
            Map<String, Serializable> values = new HashMap();
            Map<String, Serializable> var19 = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            String uniqueId = null;
            if (var19.containsKey("serialNo")) {
               uniqueId = String.valueOf(var19.get("serialNo"));
            }

            apiRequestHandler.updaloadFile(var19, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
            objectEntry = apiRequestHandler.updateData(this.contextUser, var19, status, referanceCode, objectDefinitionId);
         }

         return objectEntry;
      }
   }

   private com.liferay.object.model.ObjectEntry saveAsExport(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload, APIRequestHandler apiRequestHandler, com.liferay.object.model.ObjectEntry objectEntry) throws Exception {
      if (Validator.isNull(objectEntry)) {
         throw new Exception("Object entry is null");
      } else {
         boolean isBranchMaker = apiRequestHandler.getUserHierarchy().isMaker();
         boolean isHoChecker = apiRequestHandler.getUserHierarchy().isHOChecker();
         int currentStatus = objectEntry.getStatus();
         if (isBranchMaker && (currentStatus == 2 || currentStatus == 4)) {
            Map<String, Serializable> values = objectEntry.getValues();
            String referanceCode = objectEntry.getExternalReferenceCode();
            values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            if (currentStatus == 4) {
               String uniqueId = null;
               if (values.containsKey("serialNo")) {
                  uniqueId = String.valueOf(values.get("serialNo"));
               }

               apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
            }

            objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
            if (currentStatus == 2) {
               (new CustomWorkflowLogs(this.contextUser)).createWorkflowEntryLogs(objectEntry, "Export", this.workflowObjectDefinitionId);
            } else {
               this.completeWorkflowLogs(httpServletRequest, apiRequestHandler, multipartBody, objectEntry, "Mark as export");
            }
         }

         if (isHoChecker) {
            this.completeWorkflowLogs(httpServletRequest, apiRequestHandler, multipartBody, objectEntry, "Mark as export");
         }

         return objectEntry;
      }
   }

   public long getClassPKFromTask(WorkflowTask task, long companyId) {
      try {
         WorkflowInstance instance = this._workflowInstanceManager.getWorkflowInstance(companyId, task.getWorkflowInstanceId());
         Map<String, Serializable> workflowContext = instance.getWorkflowContext();
         return GetterUtil.getLong(workflowContext.get("entryClassPK"));
      } catch (WorkflowException var6) {
         return 0L;
      }
   }

   private String getFileEntryJson(FileEntry entry) {
      try {
         String downloadURL = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), (ThemeDisplay)null, (String)null);
         JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
         jsonObject.put("URL", downloadURL);
         jsonObject.put("fileId", entry.getFileEntryId());
         jsonObject.put("fileName", entry.getFileName());
         jsonObject.put("fileExtension", entry.getExtension());
         return jsonObject.toJSONString();
      } catch (PortalException var4) {
         return String.valueOf(entry.getFileEntryId());
      }
   }

   private WorkflowTransition getWorkflowTransitions(long workflowTaskId, String name) {
      try {
         for(WorkflowTransition workflowTransition : WorkflowTaskManagerUtil.getWorkflowTaskWorkflowTransitions(workflowTaskId)) {
            if (workflowTransition.getName().equalsIgnoreCase(name)) {
               return workflowTransition;
            }
         }
      } catch (WorkflowException e) {
         log.error(e);
      }

      return null;
   }

   public void completeWorkflowLogs(HttpServletRequest httpServletRequest, APIRequestHandler apiRequestHandler, MultipartBody multipartBody, com.liferay.object.model.ObjectEntry entrys, String action) {
      long workflowTaskId = ParamUtil.getLong(httpServletRequest, "workflowTaskId", 0L);
      log.debug(this.workflowObjectDefinitionId + " workflowTaskId " + workflowTaskId);
      if (workflowTaskId != 0L) {
         try {
            String transitionId = ParamUtil.getString(httpServletRequest, "transitionId");
            WorkflowTransition workflowTransition = this.getWorkflowTransitions(workflowTaskId, transitionId);
            String comment = ParamUtil.getString(httpServletRequest, "remarks");
            Map<String, Serializable> workflowContext = null;
            WorkflowTask workflowTask = this.completeWorkflowTask(workflowTaskId, transitionId, comment, workflowContext);
            if (Validator.isNotNull(workflowTask)) {
               Map<String, Serializable> values = new HashMap();
               BinaryFile binaryFile = multipartBody.getBinaryFile("attachments");
               if (Validator.isNotNull(binaryFile)) {
                  String fileName = binaryFile.getFileName();
                  File file = apiRequestHandler.createFileFromInputStream(binaryFile.getInputStream(), fileName);
                  User user = UserLocalServiceUtil.getUser(entrys.getUserId());
                  Folder folderCreated = this.getAPIRequestHandlerForUploader(user).getUserHierarchy().getUploadFolder();
                  if (Validator.isNotNull(folderCreated)) {
                     FileHandler fileHandler = new FileHandler(this._dlAppService);
                     FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folderCreated, file, apiRequestHandler.appendTimestamp(fileName, (String)null, false));
                     values.put("attachment", this.getFileEntryJson(fileEntry));
                  }
               }

               values.put("customAction", workflowTransition.getLabel(Locale.getDefault()));
               values.put("objectEntryId", entrys.getObjectEntryId());
               values.put("remarks", comment);
               values.put("transitionName", workflowTask.getName());
               values.put("workflowTaskId", workflowTask.getWorkflowTaskId());
               ServiceContext serviceContext = new ServiceContext();
               serviceContext.setCompanyId(this.contextUser.getCompanyId());
               serviceContext.setUserId(this.contextUser.getUserId());
               serviceContext.setWorkflowAction(1);
               ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, this.contextUser.getUserId(), 0L, this.workflowObjectDefinitionId, values, serviceContext);
            }
         } catch (Exception e) {
            log.error(e);
         }

      }
   }

   private WorkflowTask completeWorkflowTask(long workflowTaskId, String name, String comment, Map<String, Serializable> workflowContext) {
      try {
         this._workflowTaskManager.assignWorkflowTaskToUser(this.contextCompany.getCompanyId(), this.contextUser.getUserId(), workflowTaskId, this.contextUser.getUserId(), "Auto Self Assigned", (Date)null, (Map)null);
         return this._workflowTaskManager.completeWorkflowTask(this.contextCompany.getCompanyId(), this.contextUser.getUserId(), workflowTaskId, name, comment, workflowContext);
      } catch (PortalException e) {
         log.error(e);
         return null;
      }
   }

   private APIRequestHandler getAPIRequestHandler() {
      log.debug("APIRequestHandler Created for FE_BO_NON_Financial");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this._objectFieldLocalService, this._dlAppService, this.contextUriInfo, this._dtoConverterRegistry, this._objectEntryManagerRegistry, this._objectDefinitionLocalService, this._objectEntryLocalService, this._workflowTaskManager, this.boiWorkflowConfiguation, "FE_BO_NON_Financial", this.defaultFolderId);
   }

   private APIRequestHandler getAPIRequestHandlerForUploader(User user) {
      return new APIRequestHandler(user, this.contextHttpServletRequest, this.contextAcceptLanguage, this._objectFieldLocalService, this._dlAppService, this.contextUriInfo, this._dtoConverterRegistry, this._objectEntryManagerRegistry, this._objectDefinitionLocalService, this._objectEntryLocalService, this._workflowTaskManager, this.boiWorkflowConfiguation, "FE_BO_NON_Financial", this.defaultFolderId);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "FE_BO_NON_Financial")).validateRequestFiles();
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
      this.defaultFolderId = this.boiWorkflowConfiguation.HODefualtFolderId();
      this.objectDefinitionId = this.boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId();
      this.workflowObjectDefinitionId = this.boiWorkflowConfiguation.CustomWorkflowLogsObjectDefinitionId();
   }
}
