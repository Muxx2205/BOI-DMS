package com.boi.web.headless.delivery.internal.resource.v1_0;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.application.CustomWorkflowLogs;
import com.boi.headless.rest.common.application.FileHandler;
import com.boi.headless.rest.common.application.ObjectConverter;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.mail.BOIEmail;
import com.boi.headless.rest.common.mail.MailConfiguration;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.rest.common.validators.RequestValidator;
import com.boi.web.headless.delivery.dto.v1_0.BOIHeadlessResponse;
import com.boi.web.headless.delivery.dto.v1_0.CustomBOIHeadlessResponse;
import com.boi.web.headless.delivery.dto.v1_0.CustomFBDFEBOBOIHeadlessResponseAsList;
import com.boi.web.headless.delivery.resource.v1_0.FBDFEBOResource;
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
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation", "com.boi.headless.rest.common.mail.MailConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/fbdfebo.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FBDFEBOResource.class}
)
public class FBDFEBOResourceImpl extends BaseFBDFEBOResourceImpl {
   private String roleGandhiNager = "Gandhi Nagar";
   private String roleSwiftcell = "Swift Cell";
   private String stage = "stage";
   private String typeOfRemitence = "typeOfRemittance";
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";
   private Map<String, Boolean> isGoToSwiftcell = new HashMap<String, Boolean>() {
      private static final long serialVersionUID = 1L;

      {
         this.put("OR_Outward Remittance", true);
         this.put("EN_Export Bills Non Dispatch", false);
         this.put("ED_Export Bills Dispatch", false);
         this.put("IP_Import Bills Payment", true);
         this.put("IDPMS(Bill of Entry)", false);
         this.put("IRE_Inward Remittance Export", false);
         this.put("IRN_Inward Remittance Non Export", false);
         this.put("IB_ Import Bill Collections (LC/BGBC/FIBCB)", true);
         this.put("EL_Export LC Advising", true);
         this.put("LC_Letter of Credit", true);
         this.put("BG_Bank Guarantee", true);
         this.put("EX_Export_EDPMS", false);
      }
   };
   @Reference
   private WorkflowInstanceManager _workflowInstanceManager;
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private long workflowObjectDefinitionId = 0L;
   private Map<String, String> fileUpdload = BOIDMSConstants.getObjectFileNames("FBD_FE_BO");
   private MailConfiguration mailConfiguration;
   private BOIWorkflowConfiguation boiWorkflowConfiguation;
   @Reference
   private DDMExpressionFactory _ddmExpressionFactory;
   @Reference
   private ObjectFieldSettingLocalService _objectFieldSettingLocalService;
   @Reference
   private ObjectFieldLocalService _objectFieldLocalService;
   @Reference
   private DLAppService _dlAppService;
   private static final Log log = LogFactoryUtil.getLog(FBDFEBOResourceImpl.class);
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

   public BOIHeadlessResponse getRequestFBDFEBO(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         return this.getSingleEntry(id);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject, Status.NOT_ACCEPTABLE);
      }
   }

   public BOIHeadlessResponse addFBDFEBOAsExport(MultipartBody multipartBody) {
      return this.authorizeAndExecute(multipartBody, 1, true, false);
   }

   public BOIHeadlessResponse addFBDFEBOAsDraft(MultipartBody multipartBody) {
      return this.authorizeAndExecute(multipartBody, 2, false, true);
   }

   private BOIHeadlessResponse authorizeAndExecute(MultipartBody multipartBody, int workflowStatus, boolean requireExistingEntry, boolean requireMaker) {
      JSONObject json = JSONFactoryUtil.createJSONObject();

      try {
         APIRequestHandler apiHandler = this.getAPIRequestHandler();
         apiHandler.checkPermission("FBD FE BO");
         if (requireMaker && !apiHandler.getUserHierarchy().isMaker()) {
            json.put("error", "Access Denied!");
            return new CustomBOIHeadlessResponse(json, Status.NOT_ACCEPTABLE);
         } else {
            return this.processFBDFEBO(multipartBody, workflowStatus, requireExistingEntry, apiHandler);
         }
      } catch (Exception e) {
         json.put("error", e.getMessage());
         return new CustomBOIHeadlessResponse(json, Status.NOT_ACCEPTABLE);
      }
   }

   private BOIHeadlessResponse processFBDFEBO(MultipartBody multipartBody, int workflowStatus, boolean requireExistingEntry, APIRequestHandler apiHandler) {
      JSONObject json = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId");
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (requireExistingEntry && entryId == 0L) {
            json.put("error", "entry missing");
            return new CustomBOIHeadlessResponse(json, Status.NOT_ACCEPTABLE);
         } else {
            if (entryId > 0L) {
               objectEntry = apiHandler.getObjectEntry(entryId);
            }

            objectEntry = this.handleRequest(this.contextHttpServletRequest, multipartBody, workflowStatus, this.objectDefinitionId, this.defaultFolderId, this.fileUpdload, apiHandler, objectEntry);
            json.put("entryId", objectEntry.getObjectEntryId());
            return new CustomBOIHeadlessResponse(json, Status.ACCEPTED);
         }
      } catch (Exception e) {
         log.error(e, e);
         json.put("error", e.getMessage());
         return new CustomBOIHeadlessResponse(json, Status.NOT_ACCEPTABLE);
      }
   }

   public com.liferay.object.model.ObjectEntry handleRequest(HttpServletRequest request, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload, APIRequestHandler apiHandler, com.liferay.object.model.ObjectEntry existingEntry) throws Exception {
      String transitionId = ParamUtil.getString(request, "transitionId");
      Map<String, Serializable> values = existingEntry != null ? new HashMap(existingEntry.getValues()) : new HashMap();
      String referenceCode = existingEntry != null ? existingEntry.getExternalReferenceCode() : null;
      boolean isNew = existingEntry != null && existingEntry.getStatus() == 2 && status == 1;
      boolean isDraftOrResubmit = existingEntry != null && (existingEntry.getStatus() == 2 || "ReSubmit".equalsIgnoreCase(transitionId));
      if (isNew || isDraftOrResubmit) {
         values = ObjectConverter.getDefaultData(request, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
         this.handleFileUpload(apiHandler, request, multipartBody, values, filesValueUpload, objectDefinitionId);
      }

      boolean notAllClear = false;
      if (isNew && !this.isAllClear(values)) {
         status = 2;
         notAllClear = true;
      } else if (isNew) {
         values.put("stage", 2);
      }

      this.applyWorkflowTransition(request, apiHandler, existingEntry, values, transitionId, objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiHandler.updateData(this.contextUser, values, status, referenceCode, objectDefinitionId);
      this.completeWorkflowLogs(request, apiHandler, multipartBody, objectEntry);
      if (notAllClear) {
         throw new Exception("File is mandaroty for Export transaction");
      } else {
         return objectEntry;
      }
   }

   private boolean isAllClear(Map<String, Serializable> values) {
      Map<String, String> fileUploadMap = BOIDMSConstants.getObjectFileNames("FBD_FE_BO");
      Stream var10000 = fileUploadMap.values().stream();
      Objects.requireNonNull(values);
      var10000 = var10000.filter(values::containsKey);
      Objects.requireNonNull(values);
      return var10000.map(values::get).allMatch((value) -> Validator.isNotNull(value));
   }

   private void applyWorkflowTransition(HttpServletRequest request, APIRequestHandler apiHandler, com.liferay.object.model.ObjectEntry objectEntry, Map<String, Serializable> values, String transitionId, long objectDefinitionId) throws Exception {
      long workflowTaskId = ParamUtil.getLong(request, "workflowTaskId");
      if (!Validator.isNull(objectEntry)) {
         if (workflowTaskId == objectEntry.getObjectEntryId()) {
            if ("Reject".equalsIgnoreCase(transitionId)) {
               values.put("stage", 1);
            } else if ("Approve".equalsIgnoreCase(transitionId)) {
               if (this.isGandhiNagarUser()) {
                  String remitType = this.getTypeRemittee(values);
                  values.put("stage", Boolean.TRUE.equals(this.isGoToSwiftcell.get(remitType)) ? 3 : 5);
               } else if (this.isSwiftCellUser()) {
                  values.put("stage", 4);
               }

            } else {
               if ("Save".equalsIgnoreCase(transitionId)) {
                  values.put("transactionID", ParamUtil.getString(request, "transactionID"));
                  values.put("decision", ParamUtil.getString(request, "decision"));
               }

               if ("ReSubmit".equalsIgnoreCase(transitionId) && apiHandler.getUserHierarchy().isMaker()) {
                  values.put("stage", 2);
               }

            }
         }
      }
   }

   private void handleFileUpload(APIRequestHandler apiHandler, HttpServletRequest request, MultipartBody multipartBody, Map<String, Serializable> values, Map<String, String> filesValueUpload, long objectDefinitionId) throws Exception {
      String uniqueId = values.containsKey("referenceNumber") ? String.valueOf(values.get("referenceNumber")) : null;
      apiHandler.updaloadFile(values, filesValueUpload, multipartBody, request, objectDefinitionId, uniqueId, false);
   }

   public BOIHeadlessResponse getRequestFBDFEBOs(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      if (Validator.isNull(sorts)) {
         sorts = new Sort[]{SortFactoryUtil.create("modifiedDate", 3, true)};
      }

      return this.listData(search, filter, pagination, sorts);
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiHandler = this.getAPIRequestHandler();
      UserHierarchy hierarchy = apiHandler.getUserHierarchy();
      ObjectDefinition objectDefinition = apiHandler.getObjectDefinition(this.objectDefinitionId);
      String query = this.buildQuery(apiHandler);
      if (Validator.isBlank(query)) {
         return this.emptyResponse(hierarchy);
      } else {
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
         String exportUrl = this.resolveExportUrl(apiHandler, query);
         return new CustomFBDFEBOBOIHeadlessResponseAsList(page, hierarchy.isMaker(), exportUrl, this.isGandhiNagarUser(), this.isSwiftCellUser());
      }
   }

   private String buildQuery(APIRequestHandler apiHandler) {
      UserHierarchy hierarchy = apiHandler.getUserHierarchy();
      List<String> filters = new ArrayList();
      this.addHierarchyFilter(filters, hierarchy);
      this.addStatusAndStageFilter(filters, hierarchy);
      this.addDecisionFilter(filters, hierarchy);
      this.addDateFilter(filters);
      this.addTypeOfRemittanceFilter(filters);
      String query = String.join(" and ", filters);
      log.info("Final Query: " + query);
      return !hierarchy.isHOChecker() && !this.containsHierarchyFilters(query) ? "" : query;
   }

   private void addHierarchyFilter(List<String> filters, UserHierarchy hierarchy) {
      if (Validator.isNotNull(hierarchy.getBranchName())) {
         filters.add("branchCode eq '" + hierarchy.getBranchName() + "'");
      } else if (Validator.isNotNull(hierarchy.getZoneName())) {
         filters.add("zone eq '" + hierarchy.getZoneName() + "'");
      } else {
         if (Validator.isNotNull(hierarchy.getNbgName())) {
            filters.add("nBGCode eq '" + hierarchy.getNbgName() + "'");
         }

      }
   }

   private void addStatusAndStageFilter(List<String> filters, UserHierarchy hierarchy) {
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status");
      if (Validator.isNull(status)) {
         this.applyDefaultRoleFilter(filters, hierarchy);
      } else {
         this.applyExplicitStatusFilter(filters, status, hierarchy);
      }
   }

   private void applyDefaultRoleFilter(List<String> filters, UserHierarchy hierarchy) {
      if (!hierarchy.isMaker()) {
         filters.add("status ne 2");
      }

      if (hierarchy.isGandhiNagarUser()) {
         filters.add("stage in (2,5,4,3)");
      } else if (hierarchy.isSwiftCellUser()) {
         filters.add("stage in (3,4)");
      }

   }

   private void applyExplicitStatusFilter(List<String> filters, String status, UserHierarchy hierarchy) {
      switch (status.toLowerCase()) {
         case "draft":
            if (hierarchy.isMaker()) {
               filters.add("status eq 2");
            }
            break;
         case "denied":
            filters.add("stage eq 1");
            filters.add("status eq 0");
            break;
         case "pending with febo":
            filters.add("stage eq 2");
            filters.add("status eq 0");
            break;
         case "pending with swift cell":
            filters.add("stage eq 3");
            filters.add("status eq 0");
            break;
         case "approved":
            if (this.isGandhiNagarUser()) {
               filters.add("stage in (4,5)");
            }

            if (this.isSwiftCellUser()) {
               filters.add("stage eq 5");
            }

            if (hierarchy.isMaker()) {
               filters.add("stage in (4,5)");
            }

            filters.add("status eq 0");
      }

   }

   private void addDecisionFilter(List<String> filters, UserHierarchy hierarchy) {
      String decision = ParamUtil.getString(this.contextHttpServletRequest, "decision");
      if (Validator.isNotNull(decision) && "FBD_FE_BO".equalsIgnoreCase(hierarchy.getDepartmentId())) {
         filters.add("decision eq '" + decision + "'");
      }

   }

   private void addDateFilter(List<String> filters) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date from = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      Date to = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      if (from != null && to != null) {
         Map<String, String> range = this.convertToISO8601Range(sdf.format(from), sdf.format(to));
         String var10001 = (String)range.get("fromDate");
         filters.add("dateModified ge " + var10001 + " and dateModified le " + (String)range.get("toDate"));
      }
   }

   private void addTypeOfRemittanceFilter(List<String> filters) {
      String typeOfRemittance = ParamUtil.getString(this.contextHttpServletRequest, "typeOfRemittance");
      if (Validator.isNotNull(typeOfRemittance)) {
         filters.add("typeOfRemittance eq '" + typeOfRemittance + "'");
      }

   }

   private Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
      LocalDate fromDate = LocalDate.parse(fromDateStr);
      LocalDate toDate = LocalDate.parse(toDateStr);
      ZonedDateTime from = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime to = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("fromDate", DateTimeFormatter.ISO_INSTANT.format(from));
      result.put("toDate", DateTimeFormatter.ISO_INSTANT.format(to));
      return result;
   }

   private boolean containsHierarchyFilters(String query) {
      return query.contains("branchCode") || query.contains("zone") || query.contains("nBGCode");
   }

   private String resolveExportUrl(APIRequestHandler apiHandler, String query) {
      if (!apiHandler.isHaveExportToRole()) {
         return "";
      } else {
         boolean export = ParamUtil.getBoolean(this.contextHttpServletRequest, "export");
         if (export) {
            apiHandler.exportToExcelData(query, this.objectDefinitionId, this.defaultFolderId);
            return "in-progress";
         } else {
            return apiHandler.getTaskComplatedURL(this.objectDefinitionId);
         }
      }
   }

   private BOIHeadlessResponse emptyResponse(UserHierarchy hierarchy) {
      return new CustomFBDFEBOBOIHeadlessResponseAsList((Page)null, hierarchy.isMaker(), "", this.isGandhiNagarUser(), this.isSwiftCellUser());
   }

   private BOIHeadlessResponse getSingleEntry(Number id) throws PortalException {
      APIRequestHandler apiHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiHandler.getObjectDefinition(this.objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiHandler.getObjectEntry(id.longValue());
      if (!this.isValidEntry(objectEntry, objectDefinition)) {
         return this.invalidResponse();
      } else {
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject json = this.convertEntryToJson(values);
         this.enrichWithWorkflowData(apiHandler, objectEntry, json, values);
         return new CustomBOIHeadlessResponse(json, Status.ACCEPTED);
      }
   }

   private boolean isValidEntry(com.liferay.object.model.ObjectEntry objectEntry, ObjectDefinition objectDefinition) {
      return Validator.isNotNull(objectEntry) && objectEntry.getObjectDefinitionId() == objectDefinition.getObjectDefinitionId();
   }

   private void enrichWithWorkflowData(APIRequestHandler apiHandler, com.liferay.object.model.ObjectEntry objectEntry, JSONObject json, Map<String, Serializable> values) {
      json.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
      int stage = this.getStageValue(values);
      if (this.shouldPopulateStageData(stage)) {
         this.getValue(values, objectEntry, json, stage);
      }

      json.put("entryId", objectEntry.getObjectEntryId());
      apiHandler.addWorkflowLogs(json, objectEntry.getObjectEntryId(), this.workflowObjectDefinitionId);
   }

   private boolean shouldPopulateStageData(int stage) {
      return stage != 0 && stage != 4 && stage != 5;
   }

   public JSONObject convertEntryToJson(Map<String, Serializable> values) {
      JSONObject json = JSONFactoryUtil.createJSONObject();
      if (Validator.isNull(values)) {
         return json;
      } else {
         values.forEach((key, value) -> {
            if (key.endsWith("Detailed")) {
               this.handleDetailedEntry(json, key, value);
            } else {
               json.put(key, value);
            }

         });
         return json;
      }
   }

   private BOIHeadlessResponse invalidResponse() {
      return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject(), Status.NOT_ACCEPTABLE);
   }

   private int getStageValue(Map<String, Serializable> values) {
      try {
         if (values.containsKey(this.stage)) {
            int stage = Integer.parseInt(String.valueOf(values.get(this.stage)));
            return stage;
         } else {
            return 9;
         }
      } catch (Exception var3) {
         return 2;
      }
   }

   private boolean isGandhiNagarUser() {
      try {
         Role role = RoleLocalServiceUtil.getRole(this.contextCompany.getCompanyId(), this.roleGandhiNager);
         return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.contextUser.getUserId());
      } catch (PortalException var3) {
         return false;
      }
   }

   private boolean isSwiftCellUser() {
      try {
         Role role = RoleLocalServiceUtil.getRole(this.contextCompany.getCompanyId(), this.roleSwiftcell);
         return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.contextUser.getUserId());
      } catch (PortalException var3) {
         return false;
      }
   }

   private void getValue(Map<String, Serializable> values, com.liferay.object.model.ObjectEntry objectEntry, JSONObject jsonObject, int stage) {
      JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
      if (objectEntry.getStatus() != 2) {
         if (stage == 1 && !this.isGandhiNagarUser() && !this.isSwiftCellUser()) {
            JSONObject actionJson = JSONFactoryUtil.createJSONObject();
            actionJson.put("actionName", "ReSubmit");
            actionJson.put("transitionId", "ReSubmit");
            actionJson.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(actionJson);
            jsonObject.put("actions", jsonArray);
         }

         if (stage == 2 && this.isGandhiNagarUser() && objectEntry.getStatus() != 4) {
            JSONObject approve = JSONFactoryUtil.createJSONObject();
            approve.put("actionName", "Approve");
            approve.put("transitionId", "Approve");
            approve.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(approve);
            JSONObject reject = JSONFactoryUtil.createJSONObject();
            reject.put("actionName", "Reject");
            reject.put("transitionId", "Reject");
            reject.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(reject);
            JSONObject saveTrans = JSONFactoryUtil.createJSONObject();
            saveTrans.put("actionName", "Save");
            saveTrans.put("transitionId", "Save");
            saveTrans.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(saveTrans);
            jsonObject.put("actions", jsonArray);
         }

         if (stage == 3 && this.isSwiftCellUser() && objectEntry.getStatus() != 4) {
            JSONObject approve = JSONFactoryUtil.createJSONObject();
            approve.put("actionName", "Approve");
            approve.put("transitionId", "Approve");
            approve.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(approve);
            JSONObject reject = JSONFactoryUtil.createJSONObject();
            reject.put("actionName", "Reject");
            reject.put("transitionId", "Reject");
            reject.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(reject);
            JSONObject saveTrans = JSONFactoryUtil.createJSONObject();
            saveTrans.put("actionName", "Save");
            saveTrans.put("transitionId", "Save");
            saveTrans.put("workflowTaskId", objectEntry.getObjectEntryId());
            jsonArray.put(saveTrans);
            jsonObject.put("actions", jsonArray);
         }

      }
   }

   private String getTypeRemittee(Map<String, Serializable> values) {
      try {
         if (values.containsKey(this.typeOfRemitence)) {
            String typeOfRemitence = String.valueOf(values.get(this.typeOfRemitence));
            return typeOfRemitence;
         } else {
            return null;
         }
      } catch (Exception var3) {
         return null;
      }
   }

   private void handleDetailedEntry(JSONObject jsonArray, String key, Serializable value) {
      try {
         String originalString = (String)value;
         String jsonData = originalString.trim();
         if (jsonData.startsWith("{")) {
            JSONObject detailedObject = JSONFactoryUtil.createJSONObject(jsonData);
            if (Validator.isNotNull(detailedObject) && detailedObject.length() > 0) {
               this.addAllKeysToJson(jsonArray, detailedObject);
            }
         } else if (jsonData.startsWith("[")) {
            JSONArray sourceArray = JSONFactoryUtil.createJSONArray(jsonData);
            this.mergeJsonArray(jsonArray, sourceArray);
         } else {
            jsonArray.put(key, value);
         }
      } catch (JSONException e) {
         log.error(e);
      }

   }

   private void mergeJsonArray(JSONObject target, JSONArray sourceArray) {
      for(int i = 0; i < sourceArray.length(); ++i) {
         JSONObject source = sourceArray.getJSONObject(i);

         for(String key : source.keySet()) {
            String newKey = key;

            for(int counter = 1; target.has(newKey); ++counter) {
               newKey = key + counter;
            }

            target.put(newKey, source.get(key));
         }
      }

   }

   private void addAllKeysToJson(JSONObject target, JSONObject source) {
      for(String sourceKey : source.keySet()) {
         target.put(sourceKey, source.get(sourceKey));
      }

   }

   public com.liferay.object.model.ObjectEntry handeeleRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload, APIRequestHandler apiRequestHandler, com.liferay.object.model.ObjectEntry objectEntry) throws Exception {
      String transitionId = ParamUtil.getString(httpServletRequest, "transitionId", "blank");
      String referanceCode = null;
      Map<String, Serializable> values = new HashMap();
      com.liferay.object.model.ObjectEntry oldObjectEntry = objectEntry;
      if (Validator.isNotNull(objectEntry)) {
         values = objectEntry.getValues();
         referanceCode = objectEntry.getExternalReferenceCode();
      }

      if (Validator.isNotNull(objectEntry) && (objectEntry.getStatus() == 2 || transitionId.equalsIgnoreCase("ReSubmit"))) {
         values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
      }

      if (Validator.isNull(objectEntry)) {
         values = ObjectConverter.getDefaultData(httpServletRequest, values, objectDefinitionId, this._objectFieldLocalService, (List)null);
         values.put("stage", 1);
      }

      objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
      String uniqueId = null;
      if (values.containsKey("referenceNumber")) {
         uniqueId = String.valueOf(values.get("referenceNumber"));
      }

      String typeOfRemitence = this.getTypeRemittee(values);
      if (Validator.isNotNull(oldObjectEntry) && oldObjectEntry.getStatus() == 2 && objectEntry.getStatus() == 0) {
         values.put("stage", 2);
      }

      if (Validator.isNotNull(oldObjectEntry) && (oldObjectEntry.getStatus() == 2 || transitionId.equalsIgnoreCase("ReSubmit"))) {
         apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
      }

      if (Validator.isNull(objectEntry)) {
         apiRequestHandler.updaloadFile(values, filesValueUpload, multipartBody, httpServletRequest, objectDefinitionId, uniqueId, false);
      }

      if (Validator.isBlank(referanceCode)) {
         referanceCode = objectEntry.getExternalReferenceCode();
      }

      objectEntry = apiRequestHandler.updateData(this.contextUser, values, status, referanceCode, objectDefinitionId);
      if (status == 1 && Validator.isNotNull(objectEntry)) {
         log.debug("Updateding logs..  ");
         long workflowTaskId = ParamUtil.getLong(httpServletRequest, "workflowTaskId", 0L);
         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("Reject") && this.isGandhiNagarUser()) {
            values.put("stage", 1);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("Reject") && this.isSwiftCellUser()) {
            values.put("stage", 1);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("Approve") && this.isGandhiNagarUser()) {
            if (this.isGoToSwiftcell.containsKey(typeOfRemitence) && (Boolean)this.isGoToSwiftcell.get(typeOfRemitence)) {
               values.put("stage", 3);
            } else {
               values.put("stage", 5);
            }

            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("Save") && (this.isGandhiNagarUser() || this.isSwiftCellUser())) {
            String transactionID = ParamUtil.getString(httpServletRequest, "transactionID", "");
            String decision = ParamUtil.getString(httpServletRequest, "decision", (String)null);
            if (Validator.isNotNull(decision)) {
               values.put("decision", decision);
            }

            values.put("transactionID", transactionID);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("Approve") && this.isSwiftCellUser()) {
            values.put("stage", 4);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         boolean isMaker = apiRequestHandler.getUserHierarchy().isMaker();
         if (workflowTaskId == objectEntry.getObjectEntryId() && transitionId.equalsIgnoreCase("ReSubmit") && isMaker) {
            values.put("stage", 2);
            objectEntry = apiRequestHandler.updateData(this.contextUser, values, 0, referanceCode, objectDefinitionId);
         }

         this.completeWorkflowLogs(httpServletRequest, apiRequestHandler, multipartBody, objectEntry);
         log.debug("Updateding logs..  ");
      }

      return objectEntry;
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

   public void completeWorkflowLogs(HttpServletRequest request, APIRequestHandler apiRequestHandler, MultipartBody multipartBody, com.liferay.object.model.ObjectEntry objectEntry) {
      long workflowTaskId = ParamUtil.getLong(request, "workflowTaskId");
      if (workflowTaskId == 0L) {
         this.createExportLog(objectEntry);
      } else {
         try {
            String transitionId = ParamUtil.getString(request, "transitionId");
            String comment = this.resolveComment(request, transitionId);
            Map<String, Serializable> values = new HashMap();
            this.attachFileIfPresent(apiRequestHandler, multipartBody, values);
            this.populateWorkflowValues(values, objectEntry, workflowTaskId, transitionId, comment);
            this.persistWorkflowLog(values);
            if ("Reject".equalsIgnoreCase(transitionId)) {
               this.sendRejectNotification(objectEntry, comment);
            }
         } catch (Exception e) {
            log.error("Error while completing workflow logs", e);
         }

      }
   }

   private void createExportLog(com.liferay.object.model.ObjectEntry objectEntry) {
      (new CustomWorkflowLogs(this.contextUser)).createWorkflowEntryLogs(objectEntry, "Export", this.workflowObjectDefinitionId);
   }

   private String resolveComment(HttpServletRequest request, String transitionId) {
      String comment = ParamUtil.getString(request, "remarks");
      String decision = ParamUtil.getString(request, "decision");
      if (!Validator.isBlank(comment)) {
         return comment;
      } else if ("Save".equalsIgnoreCase(transitionId)) {
         return Validator.isNotNull(decision) ? "Decision changed with " + decision : "Save action performed";
      } else {
         return "";
      }
   }

   private void attachFileIfPresent(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, Map<String, Serializable> values) throws Exception {
      BinaryFile binaryFile = multipartBody.getBinaryFile("attachments");
      if (!Validator.isNull(binaryFile)) {
         File file = apiRequestHandler.createFileFromInputStream(binaryFile.getInputStream(), binaryFile.getFileName());
         Folder uploadFolder = apiRequestHandler.getUserHierarchy().getUploadFolder();
         if (!Validator.isNull(uploadFolder)) {
            FileHandler fileHandler = new FileHandler(this._dlAppService);
            FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(uploadFolder, file, apiRequestHandler.appendTimestamp(binaryFile.getFileName(), (String)null, false));
            values.put("attachment", this.getFileEntryJson(fileEntry));
         }
      }
   }

   private void populateWorkflowValues(Map<String, Serializable> values, com.liferay.object.model.ObjectEntry objectEntry, long workflowTaskId, String transitionId, String comment) {
      values.put("customAction", transitionId);
      values.put("transitionName", transitionId);
      values.put("remarks", comment);
      values.put("workflowTaskId", workflowTaskId);
      values.put("objectEntryId", objectEntry.getObjectEntryId());
   }

   private void persistWorkflowLog(Map<String, Serializable> values) throws PortalException {
      ServiceContext serviceContext = new ServiceContext();
      serviceContext.setCompanyId(this.contextUser.getCompanyId());
      serviceContext.setUserId(this.contextUser.getUserId());
      serviceContext.setWorkflowAction(1);
      ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, this.contextUser.getUserId(), 0L, this.workflowObjectDefinitionId, values, serviceContext);
   }

   private void sendRejectNotification(com.liferay.object.model.ObjectEntry objectEntry, String comment) {
      try {
         Map<String, Object> requestBody = Map.of("REF_NO", String.valueOf(objectEntry.getValues().get("referenceNumber")), "REMARKS", Validator.isNotNull(comment) ? comment : "", "USERID", objectEntry.getUserId(), "BRANCHCODE", String.valueOf(objectEntry.getValues().get("branchCode")).split("-")[0]);
         (new BOIEmail(this.mailConfiguration, this._dtoConverterRegistry, this._objectEntryManagerRegistry)).sendMail(2, requestBody);
      } catch (Exception e) {
         log.error("Failed to send reject email notification", e);
      }

   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "FBD_FE_BO")).validateRequestFiles();
   }

   private APIRequestHandler getAPIRequestHandler() {
      log.debug("APIRequestHandler Created for FBD_FE_BO");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this._objectFieldLocalService, this._dlAppService, this.contextUriInfo, this._dtoConverterRegistry, this._objectEntryManagerRegistry, this._objectDefinitionLocalService, this._objectEntryLocalService, this._workflowTaskManager, this.boiWorkflowConfiguation, "FBD_FE_BO", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
      this.mailConfiguration = (MailConfiguration)ConfigurableUtil.createConfigurable(MailConfiguration.class, properties);
      this.defaultFolderId = this.boiWorkflowConfiguation.HODefualtFolderId();
      this.objectDefinitionId = this.boiWorkflowConfiguation.FBDFRBOObjectDefinitionId();
      this.workflowObjectDefinitionId = this.boiWorkflowConfiguation.CustomWorkflowLogsObjectDefinitionId();
   }
}
