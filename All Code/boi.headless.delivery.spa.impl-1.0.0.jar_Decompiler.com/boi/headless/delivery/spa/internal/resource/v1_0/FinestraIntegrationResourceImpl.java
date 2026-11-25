package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.common.RequestValidator;
import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.spa.resource.v1_0.FinestraIntegrationResource;
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
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.spa.application.BOISPAConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/finestra-integration.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FinestraIntegrationResource.class}
)
public class FinestraIntegrationResourceImpl extends BaseFinestraIntegrationResourceImpl {
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(EWDResourceImpl.class);
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

   public BOIHeadlessResponse searchFinastraDoc(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      return this.listData(search, filter, pagination, sorts);
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      String query = search;
      search = null;
      log.info("query result is " + query);
      Page<ObjectEntry> page = null;
      page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   public BOIHeadlessResponse addFinastraDoc(MultipartBody multipartBody) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      return this.addDocumentWithStatus(apiRequestHandler, multipartBody);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "FINASTRA")).validateRequestFiles();
   }

   private BOIHeadlessResponse addDocumentWithStatus(APIRequestHandler apiRequestHandler, MultipartBody multipartBody) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         Map<String, String> fileUpdload = new HashMap();
         fileUpdload.put("documentNameDetailed", "documentNameDetailed");
         String referenceNumber = ParamUtil.getString(this.contextHttpServletRequest, "referenceNumber", (String)null);
         ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
         ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         String query = "referenceNumber eq '" + referenceNumber + "'";
         if (Validator.isBlank(referenceNumber)) {
            throw new Exception("Reference Number is mandatory");
         }

         log.info("APIRequestHandler Created for " + query);
         Page<ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, Pagination.of(-1, -1), (String)null, (Sort[])null);
         List<ObjectEntry> allEntries = (List)page.getItems();
         if (Validator.isNull(allEntries) || allEntries.isEmpty()) {
            throw new Exception("Invalid referenceNumber");
         }

         Log var10000 = log;
         int var10001 = allEntries.size();
         var10000.info("APIRequestHandler Created for " + var10001);
         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleFinastraAddDoc(this.contextHttpServletRequest, multipartBody, this.objectDefinitionId, fileUpdload, ((ObjectEntry)allEntries.get(0)).getId());
         log.info("APIRequestHandler Created for " + (objectEntry == null));
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
      log.info("APIRequestHandler Created for FINASTRA");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this.objectFieldLocalService, this.dlAppService, this.contextUriInfo, this.dtoConverterRegistry, this.objectEntryManagerRegistry, this.objectDefinitionLocalService, this.objectEntryLocalService, this.workflowTaskManager, this.boiSpaConfiguation, "FINASTRA", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
      this.defaultFolderId = this.boiSpaConfiguation.SPADefualtFolderId();
      this.objectDefinitionId = this.boiSpaConfiguation.FinastraObjectDefinitionId();
   }
}
