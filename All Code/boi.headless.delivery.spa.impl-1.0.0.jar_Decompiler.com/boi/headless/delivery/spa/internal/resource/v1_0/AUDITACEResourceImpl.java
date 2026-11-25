package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.common.RequestValidator;
import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.spa.internal.common.SharedData;
import boi.headless.delivery.spa.resource.v1_0.AUDITACEResource;
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
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTransition;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.Serializable;
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
   properties = {"OSGI-INF/liferay/rest/v1_0/auditace.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {AUDITACEResource.class}
)
public class AUDITACEResourceImpl extends BaseAUDITACEResourceImpl {
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(AUDITACEResourceImpl.class);
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

   public BOIHeadlessResponse getDwaDtl(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      SharedData.getHello();
      return this.listData(search, filter, pagination, sorts);
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      List<String> queryList = new LinkedList();
      String query = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      log.info("query result is " + query);
      Page<ObjectEntry> page = null;
      page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
      boolean isExport = ParamUtil.get(this.contextHttpServletRequest, "export", false);
      if (isExport) {
         apiRequestHandler.exportToExcelData(query, this.objectDefinitionId, this.defaultFolderId);
      }

      String url = apiRequestHandler.getTaskComplatedURL(this.objectDefinitionId);
      log.info("url : " + url);
      return new CustomBOIHeadlessResponseAsList(page, url);
   }

   public BOIHeadlessResponse getDwa(@NotNull Integer id) throws Exception {
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

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "INSPECTION_AUDIT_ACE")).validateRequestFiles();
   }

   public BOIHeadlessResponse addDWAAsDraft(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      return this.saveAsStatus(apiRequestHandler, multipartBody, 2);
   }

   public BOIHeadlessResponse addDWAAsExport(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      return this.saveAsStatus(apiRequestHandler, multipartBody, 1);
   }

   private BOIHeadlessResponse saveAsStatus(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         Map<String, String> fileUpdload = new HashMap();
         fileUpdload.put("document", "document");
         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleAuditAceRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, fileUpdload, this.defaultFolderId);
         if (Validator.isNotNull(objectEntry)) {
            jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
            jsonObject.put("values", objectEntry.getValues());
            jsonObject.put("entryId", objectEntry.getObjectEntryId());
         }
      } catch (Exception e) {
         e.printStackTrace();
         jsonObject.put("Error ", e.getMessage());
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   private APIRequestHandler getAPIRequestHandler() {
      log.info("APIRequestHandler Created for INSPECTION_AUDIT_ACE");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this.objectFieldLocalService, this.dlAppService, this.contextUriInfo, this.dtoConverterRegistry, this.objectEntryManagerRegistry, this.objectDefinitionLocalService, this.objectEntryLocalService, this.workflowTaskManager, this.boiSpaConfiguation, "INSPECTION_AUDIT_ACE", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
      this.defaultFolderId = this.boiSpaConfiguation.SPADefualtFolderId();
      this.objectDefinitionId = this.boiSpaConfiguation.InspectionAuditACEObjectDefinitionId();
   }
}
