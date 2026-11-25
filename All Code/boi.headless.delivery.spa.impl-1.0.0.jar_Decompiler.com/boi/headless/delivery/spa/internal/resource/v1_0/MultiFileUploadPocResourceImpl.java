package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.common.RequestValidator;
import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.spa.resource.v1_0.MultiFileUploadPocResource;
import com.boi.headless.rest.common.spa.application.APIRequestHandler;
import com.boi.headless.rest.common.spa.application.BOISPAConfiguation;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTransition;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.spa.application.BOISPAConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/multi-file-upload-poc.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {MultiFileUploadPocResource.class}
)
public class MultiFileUploadPocResourceImpl extends BaseMultiFileUploadPocResourceImpl {
   private long defaultFolderId = 0L;
   private long branchDigitilizationDefaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(MultiFileUploadPocResourceImpl.class);
   @Reference
   private DTOConverterRegistry dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;
   @Reference
   private ObjectDefinitionLocalService objectDefinitionLocalService;
   @Reference
   private ObjectEntryLocalService objectEntryLocalService;
   @Reference
   private WorkflowTaskManager workflowTaskManager;

   public BOIHeadlessResponse searchBranchDoc(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      return this.listData(search, filter, pagination, sorts);
   }

   public BOIHeadlessResponse addData(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      this.fileValidation(multipartBody);
      return this.exportAll(apiRequestHandler, multipartBody, 1);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "BRANCH_DIGITIATION")).validateRequestFiles();
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      String query = this.getFilterQuery();
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      log.info("query result is " + query);
      Page<ObjectEntry> page = null;
      page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   private BOIHeadlessResponse exportAll(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      APIRequestHandler.BranchDigitalResponse response = apiRequestHandler.handleBranchDigitalRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, this.branchDigitilizationDefaultFolderId);
      if (response.isSuccess()) {
         if (!response.getEntries().isEmpty()) {
            jsonObject = this.convertObjectEntriesToJSON(response.getEntries());
         } else {
            jsonObject.put("message", "No records processed, but no errors reported.");
         }
      } else {
         jsonObject.put("success", false);
         jsonObject.put("message", response.getMessage());
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   public BOIHeadlessResponse getRecord(@NotNull Integer id) {
      return this.getSingleEntry(id);
   }

   private BOIHeadlessResponse getSingleEntry(Number id) {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.getObjectEntry(id.longValue());
      if (Validator.isNotNull(objectEntry) && objectEntry.getObjectDefinitionId() == objectDefinition.getObjectDefinitionId()) {
         List<WorkflowTask> list = (List)apiRequestHandler.getWorkflowTasks(objectDefinition, this.contextUser, objectEntry.getObjectEntryId()).stream().sorted((w1, w2) -> w2.getCreateDate().compareTo(w1.getCreateDate())).collect(Collectors.toList());
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject jsonObject = apiRequestHandler.convertEntryToArray(values);
         jsonObject.put("entryId", objectEntry.getObjectEntryId());
         if (Validator.isNotNull(list) && !list.isEmpty()) {
            WorkflowTask workflow = (WorkflowTask)list.get(0);
            List<WorkflowTransition> actions = apiRequestHandler.getWorkflowTransitions(((WorkflowTask)list.get(0)).getWorkflowTaskId());
            JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
            actions.forEach((action) -> {
               JSONObject actionJson = JSONFactoryUtil.createJSONObject();
               actionJson.put("name", action.getLabel(Locale.getDefault()));
               actionJson.put("id", action.getName());
               actionJson.put("taskId", workflow.getWorkflowTaskId());
               jsonArray.put(actionJson);
            });
            jsonObject.put("Actions", jsonArray);
         }

         return new CustomBOIHeadlessResponse(jsonObject);
      } else {
         return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject());
      }
   }

   public JSONObject convertObjectEntriesToJSON(List<com.liferay.object.model.ObjectEntry> objectEntries) {
      JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

      for(com.liferay.object.model.ObjectEntry objectEntry : objectEntries) {
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

         for(Map.Entry<String, Serializable> entry : values.entrySet()) {
            jsonObject.put((String)entry.getKey(), String.valueOf(entry.getValue()));
         }

         jsonArray.put(jsonObject);
      }

      JSONObject result = JSONFactoryUtil.createJSONObject();
      result.put("success", true);
      result.put("entries", jsonArray);
      return result;
   }

   public String getFilterQuery() {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      log.debug("Final queryResult:  " + queryResult);
      return queryResult;
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

   private APIRequestHandler getAPIRequestHandler() {
      log.info("APIRequestHandler Created for BRANCH_DIGITIATION");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this.objectFieldLocalService, this.dlAppService, this.contextUriInfo, this.dtoConverterRegistry, this.objectEntryManagerRegistry, this.objectDefinitionLocalService, this.objectEntryLocalService, this.workflowTaskManager, this.boiSpaConfiguation, "BRANCH_DIGITIATION", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
      this.defaultFolderId = this.boiSpaConfiguation.SPADefualtFolderId();
      this.objectDefinitionId = this.boiSpaConfiguation.BranchDigitationObjectDefinitionId();
      this.branchDigitilizationDefaultFolderId = this.boiSpaConfiguation.BranchDigitizationDefaultFolderId();
   }
}
