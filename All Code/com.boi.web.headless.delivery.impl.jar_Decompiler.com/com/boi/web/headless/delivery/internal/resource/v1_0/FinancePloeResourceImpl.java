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
import com.boi.web.headless.delivery.resource.v1_0.FinancePloeResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
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
import com.liferay.portal.kernel.json.JSONException;
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
   properties = {"OSGI-INF/liferay/rest/v1_0/finance-ploe.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FinancePloeResource.class}
)
public class FinancePloeResourceImpl extends BaseFinancePloeResourceImpl {
   private static final Log log = LogFactoryUtil.getLog(FinancePloeResourceImpl.class);
   private Set<String> statusList = new LinkedHashSet<String>() {
      private static final long serialVersionUID = 1L;

      {
         this.add("pending with branch maker");
         this.add("pending with zone checker");
         this.add("approved");
      }
   };
   private long last;
   @Reference
   private WorkflowInstanceManager _workflowInstanceManager;
   private Map<String, String> fileUpdload = BOIDMSConstants.getObjectFileNames("Finance_Ploe");
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

   public BOIHeadlessResponse getFinancePloes(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
         sorts = new Sort[]{sort};
         return this.listData(search, filter, pagination, sorts);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
      }
   }

   public BOIHeadlessResponse getFinancePloe(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         return this.getSingleEntry(id);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
      }
   }

   public BOIHeadlessResponse addFinancePloeAsDraft(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.logStepTime("Call Started... addFinancePloeAsDraft ", this.last);
         APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
         this.fileValidation(multipartBody);
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (entryId != 0L) {
            objectEntry = apiRequestHandler.getObjectEntry(entryId);
         }

         objectEntry = this.saveAsDraft(this.contextHttpServletRequest, multipartBody, 2, this.objectDefinitionId, this.defaultFolderId, this.fileUpdload, apiRequestHandler, objectEntry);
         (new CustomWorkflowLogs(this.contextUser)).createAccountWorkflowEntryLogs(objectEntry, "Mark As Draft", 0, "Created", this.workflowObjectDefinitionId);
         jsonObject.put("entryId", objectEntry.getObjectEntryId());
         Map<String, Serializable> values = objectEntry.getValues();
         log.debug("entryId " + objectEntry.getObjectEntryId());
         if (values.containsKey("urn")) {
            log.debug("urn " + values.get("urn"));
            jsonObject.put("urn", values.get("urn"));
         }

         this.logStepTime("Call End... addFinancePloeAsDraft ", this.last);
         return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
      } catch (Exception e) {
         jsonObject.put("error", e.getMessage());
         e.printStackTrace();
         log.error(e);
         return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
      }
   }

   public BOIHeadlessResponse addFinancePloeAsExport(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
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
            Map<String, Serializable> values = objectEntry.getValues();
            if (values.containsKey("urn")) {
               jsonObject.put("urn", values.get("urn"));
            }

            return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
         }
      } catch (Exception e) {
         jsonObject.put("error", e.getMessage());
         e.printStackTrace();
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
         return new CustomBOIHeadlessResponseAsList(page, isMaker, url, this.statusList, "FINANCE_PLOE");
      } else {
         return new CustomBOIHeadlessResponseAsList(page, isMaker, "", this.statusList, "FINANCE_PLOE");
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
         if (status.equalsIgnoreCase("approved")) {
            queryList.add("(collST eq 2 or (customStatusByHandler eq 'pending with zone checker' and status eq 0 and collST eq 0))");
         } else {
            int statusCode = this.getStatusCode(status);
            if (statusCode != -1) {
               queryList.add("status eq " + statusCode);
            } else if (status.equalsIgnoreCase("pending with zone checker")) {
               queryList.add("customStatusByHandler eq 'pending with zone checker'");
               queryList.add("(status ne 0 or collST ne 0)");
            } else {
               queryList.add("customStatusByHandler eq '" + status.toLowerCase() + "'");
            }
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
         this._getCustomWorkFlowActionList(apiRequestHandler, objectDefinition, objectEntry, jsonObject);
         jsonObject.put("entryId", objectEntry.getObjectEntryId());
         apiRequestHandler.addWorkflowLogs(jsonObject, objectEntry.getObjectEntryId(), this.workflowObjectDefinitionId);
         return new CustomBOIHeadlessResponse(jsonObject, Status.ACCEPTED);
      } else {
         return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject(), Status.NOT_ACCEPTABLE);
      }
   }

   private void _getCustomWorkFlowActionList(APIRequestHandler apiRequestHandler, ObjectDefinition objectDefinition, com.liferay.object.model.ObjectEntry objectEntry, JSONObject jsonObject) {
      log.info("_getWorkFlowActionList started");
      UserHierarchy userHierarchy = apiRequestHandler.getUserHierarchy();
      String customStatusByHandler = String.valueOf(objectEntry.getValues().get("customStatusByHandler"));
      int collST = objectEntry.getValues().get("collST") != null ? (Integer)objectEntry.getValues().get("collST") : 10;
      boolean branchMaker = userHierarchy.isMaker();
      boolean zoneChecker = userHierarchy.isZoneChecker();
      if ("pending with zone checker".equalsIgnoreCase(customStatusByHandler) && objectEntry.isApproved() && collST == 0) {
         log.info("Skipping action creation because entry already approved by zone checker");
      } else {
         JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
         if (!"pending with zone checker".equalsIgnoreCase(customStatusByHandler) && collST != 1) {
            if (!"pending with branch maker".equalsIgnoreCase(customStatusByHandler) && collST != -1) {
               log.info("No workflow condition matched");
            } else {
               log.info("Matched zone maker workflow");
               if (branchMaker) {
                  log.info("User is zone maker. Adding Approve and Reject actions");
                  jsonArray.put(this.createAction("Resubmit", "zoneChecker", 1234L));
               } else {
                  log.info("User is not zone maker");
               }
            }
         } else {
            log.info("Matched zone checker workflow");
            if (zoneChecker) {
               log.info("User is zone checker. Adding Approve and Reject actions");
               jsonArray.put(this.createAction("Approve", "approved", 1234L));
               jsonArray.put(this.createAction("Reject", "branchMaker", 1234L));
               jsonArray.put(this.createAction("Save", "Save", 1234L));
            } else {
               log.info("User is not zone checker");
            }
         }

         log.info("Final actions jsonArray: " + jsonArray.toString());
         jsonObject.put("actions", jsonArray);
         log.info("_getWorkFlowActionList completed");
      }
   }

   private JSONObject createAction(String actionName, String transitionId, long workflowTaskId) {
      JSONObject actionJson = JSONFactoryUtil.createJSONObject();
      actionJson.put("actionName", actionName);
      actionJson.put("transitionId", transitionId);
      actionJson.put("workflowTaskId", workflowTaskId);
      return actionJson;
   }

   private void _getWorkFlowActionList(APIRequestHandler apiRequestHandler, ObjectDefinition objectDefinition, com.liferay.object.model.ObjectEntry objectEntry, JSONObject jsonObject) {
      jsonObject.put("entryId", objectEntry.getObjectEntryId());
      log.debug("objectEntry _getWorkFlowActionList " + objectEntry.getObjectEntryId());
      List<WorkflowTask> list = (List)apiRequestHandler.getWorkflowTasks(objectDefinition, this.contextUser, objectEntry.getObjectEntryId()).stream().sorted((w1, w2) -> w2.getCreateDate().compareTo(w1.getCreateDate())).collect(Collectors.toList());
      log.debug("list " + list.isEmpty());
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
         if (apiRequestHandler.getUserHierarchy().isZoneChecker() && jsonArray.length() > 0) {
            JSONObject saveTrans = JSONFactoryUtil.createJSONObject();
            saveTrans.put("actionName", "Save");
            saveTrans.put("transitionId", "Save");
            saveTrans.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(saveTrans);
         }

         jsonObject.put("actions", jsonArray);
      }

   }

   private void logStepTime(String stepName, long last) {
      long now = System.currentTimeMillis();
      log.debug(stepName + " execution time: " + (now - last) + " ms");
      this.last = now;
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
            if (values.containsKey("urn")) {
               uniqueId = String.valueOf(values.get("urn"));
               if (Validator.isBlank(uniqueId)) {
                  uniqueId = apiRequestHandler.getUserHierarchy().getURN();
                  log.debug("uniqueId " + uniqueId);
                  values.put("urn", uniqueId);
               }
            }

            apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
            values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            values.put("collST", 0);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
         } else {
            Map<String, Serializable> values = new HashMap();
            Map<String, Serializable> var19 = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            String uniqueId = null;
            uniqueId = apiRequestHandler.getUserHierarchy().getURN();
            log.debug("uniqueId " + uniqueId);
            var19.put("urn", uniqueId);
            var19.put("collST", 0);
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
         boolean isZoneChecker = apiRequestHandler.getUserHierarchy().isZoneChecker();
         boolean isDenied = false;
         int currentStatus = objectEntry.getStatus();
         if (isBranchMaker && (currentStatus == 2 || currentStatus == 4 || (Integer)((Serializable)objectEntry.getValues().getOrDefault("collST", 10)) == -1)) {
            Map<String, Serializable> values = objectEntry.getValues();
            String referanceCode = objectEntry.getExternalReferenceCode();
            values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
            if ((Integer)((Serializable)objectEntry.getValues().getOrDefault("collST", 10)) == -1 || currentStatus == 4) {
               isDenied = true;
               String uniqueId = null;
               this.updateFromToValues(values);
               if (values.containsKey("urn")) {
                  uniqueId = String.valueOf(values.get("urn"));
               }

               apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
            }

            values.put("collST", 1);
            values.put("customStatusByHandler", "pending with zone checker");
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
            if (currentStatus == 2) {
               (new CustomWorkflowLogs(this.contextUser)).createAccountWorkflowEntryLogs(objectEntry, "Mark As Export", 1, "Created", this.workflowObjectDefinitionId);
            } else if (isDenied) {
               String comment = ParamUtil.getString(httpServletRequest, "remarks", "");
               (new CustomWorkflowLogs(this.contextUser)).createAccountWorkflowEntryLogs(objectEntry, "ReSubmit", 1, comment, this.workflowObjectDefinitionId);
            }
         }

         String transitionId = ParamUtil.getString(httpServletRequest, "transitionId");
         if (isZoneChecker && transitionId.equalsIgnoreCase("Save")) {
            String remarks = ParamUtil.getString(httpServletRequest, "remarks", (String)null);
            if (Validator.isBlank(remarks)) {
               return objectEntry;
            } else {
               Map<String, Serializable> values = objectEntry.getValues();
               String referanceCode = objectEntry.getExternalReferenceCode();
               values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
               objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
               (new CustomWorkflowLogs(this.contextUser)).createWorkflowEntryLogs(objectEntry.getObjectEntryId(), transitionId, remarks, this.workflowObjectDefinitionId);
               return objectEntry;
            }
         } else {
            if (isZoneChecker) {
               String customAction = "";
               int cusCurrentStatus = 10;
               String customHandler = "";
               if (transitionId.equalsIgnoreCase("approved")) {
                  customAction = "Approve";
                  cusCurrentStatus = 2;
                  customHandler = "approved";
               } else if (transitionId.equalsIgnoreCase("branchMaker")) {
                  cusCurrentStatus = -1;
                  customAction = "Reject";
                  customHandler = "pending with branch maker";
               }

               Map<String, Serializable> values = objectEntry.getValues();
               String referanceCode = objectEntry.getExternalReferenceCode();
               values.put("collST", cusCurrentStatus);
               values.put("customStatusByHandler", customHandler);
               objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
               this.completeWorkflowLogs(httpServletRequest, apiRequestHandler, multipartBody, objectEntry, transitionId, customAction, cusCurrentStatus);
            }

            return objectEntry;
         }
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

   private void updateFromToValues(Map<String, Serializable> objectValues) {
      for(ObjectField objectField : this._objectFieldLocalService.getObjectFields(this.objectDefinitionId)) {
         String fieldName = objectField.getName();
         if (fieldName.endsWith("Detailed")) {
            String from = ParamUtil.getString(this.contextHttpServletRequest, fieldName + "From", "");
            String to = ParamUtil.getString(this.contextHttpServletRequest, fieldName + "To", "");
            Object jsonValue = objectValues.get(fieldName);
            log.debug("Existing JSON value for " + fieldName + ": " + jsonValue);
            if (jsonValue != null && !String.valueOf(jsonValue).isBlank()) {
               try {
                  JSONObject jsonObject = JSONFactoryUtil.createJSONObject(String.valueOf(jsonValue));
                  jsonObject.put(fieldName + "From", from);
                  jsonObject.put(fieldName + "To", to);
                  objectValues.put(fieldName, jsonObject.toString());
                  log.debug("After update for " + fieldName + ": " + jsonObject.toString());
               } catch (JSONException e) {
                  log.error("Error while updating field: " + fieldName, e);
               }
            } else {
               log.debug("Skipping update because JSON is null/blank for field: " + fieldName);
            }
         }
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

   public void completeWorkflowLogs(HttpServletRequest httpServletRequest, APIRequestHandler apiRequestHandler, MultipartBody multipartBody, com.liferay.object.model.ObjectEntry entrys) {
      long workflowTaskId = ParamUtil.getLong(httpServletRequest, "workflowTaskId", 0L);
      log.debug(this.workflowObjectDefinitionId + " workflow Task Id " + workflowTaskId);
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
                  log.debug("User " + user.getScreenName());
                  Folder folderCreated = this.getAPIRequestHandlerForUploader(user).getUserHierarchy().getUploadFolder();
                  log.debug("folderCreated " + folderCreated.getFolderId());
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

   public void completeWorkflowLogs(HttpServletRequest httpServletRequest, APIRequestHandler apiRequestHandler, MultipartBody multipartBody, com.liferay.object.model.ObjectEntry entrys, String transitionId, String customAction, int currentStatus) {
      try {
         String comment = ParamUtil.getString(httpServletRequest, "remarks");
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

         values.put("customAction", customAction);
         values.put("currentStatus", currentStatus);
         values.put("objectEntryId", entrys.getObjectEntryId());
         values.put("remarks", comment);
         values.put("transitionName", transitionId);
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(this.contextUser.getCompanyId());
         serviceContext.setUserId(this.contextUser.getUserId());
         serviceContext.setWorkflowAction(1);
         ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, this.contextUser.getUserId(), 0L, this.workflowObjectDefinitionId, values, serviceContext);
      } catch (Exception e) {
         log.error(e);
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
      log.debug("APIRequestHandler Created for Finance_Ploe");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this._objectFieldLocalService, this._dlAppService, this.contextUriInfo, this._dtoConverterRegistry, this._objectEntryManagerRegistry, this._objectDefinitionLocalService, this._objectEntryLocalService, this._workflowTaskManager, this.boiWorkflowConfiguation, "Finance_Ploe", this.defaultFolderId);
   }

   private APIRequestHandler getAPIRequestHandlerForUploader(User user) {
      return new APIRequestHandler(user, this.contextHttpServletRequest, this.contextAcceptLanguage, this._objectFieldLocalService, this._dlAppService, this.contextUriInfo, this._dtoConverterRegistry, this._objectEntryManagerRegistry, this._objectDefinitionLocalService, this._objectEntryLocalService, this._workflowTaskManager, this.boiWorkflowConfiguation, "Account_Opening", this.defaultFolderId);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "Finance_Ploe")).validateRequestFiles();
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
      this.defaultFolderId = this.boiWorkflowConfiguation.HODefualtFolderId();
      this.objectDefinitionId = this.boiWorkflowConfiguation.FinancePloeObjectDefinitionId();
      this.workflowObjectDefinitionId = this.boiWorkflowConfiguation.CustomWorkflowLogsObjectDefinitionId();
   }
}
