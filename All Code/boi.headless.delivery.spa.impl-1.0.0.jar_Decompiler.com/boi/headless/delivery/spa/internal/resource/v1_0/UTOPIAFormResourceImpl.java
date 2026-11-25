package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.common.RequestValidator;
import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.spa.resource.v1_0.UTOPIAFormResource;
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
import com.liferay.portal.kernel.model.User;
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.spa.application.BOISPAConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/utopia-form.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {UTOPIAFormResource.class}
)
public class UTOPIAFormResourceImpl extends BaseUTOPIAFormResourceImpl {
   private static final String ROLE_UTOPIA = "UTOPIA";
   private static final String INVALID_FOLDER_CHARS_REGEX = ".*[\\\\/:*?\"<>|!@#$%^&()=+\\[\\]{};,'`~\\s].*";
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(UTOPIAFormResourceImpl.class);
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

   private boolean checkAuthType() {
      String authHeader = this.contextHttpServletRequest.getHeader("Authorization");
      return Validator.isNotNull(authHeader) && authHeader.startsWith("Bearer ");
   }

   public BOIHeadlessResponse searchFolder(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("message", "Authentication failed!");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasUTOPIARole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            return this.listData(search, filter, pagination, sorts);
         }
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse searchDocument(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("message", "Authentication failed!");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasUTOPIARole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            return this.listDocument(search, pagination, sorts);
         }
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse getFolder(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("message", "Authentication failed!");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasUTOPIARole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            return this.getSingleEntry(id);
         }
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse addFolder(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("message", "Authentication failed!");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasUTOPIARole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
            return this.addFolder(apiRequestHandler, multipartBody, 1);
         }
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse addDocument(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("message", "Authentication failed!");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasUTOPIARole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
            return this.addDocument(apiRequestHandler, multipartBody, 1);
         }
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Page<ObjectEntry> page = null;
      new LinkedList();
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      String query = search;
      search = null;
      log.info("query result is " + query);
      page = objectEntryManager.getObjectEntries(this.contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   private BOIHeadlessResponse listDocument(String search, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Page<ObjectEntry> page = null;
      String filterString = search;
      search = null;
      page = objectEntryManager.getObjectEntries(this.contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), filterString, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   private BOIHeadlessResponse getSingleEntry(Number id) {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.getObjectEntry(id.longValue());
      if (Validator.isNotNull(objectEntry) && objectEntry.getObjectDefinitionId() == objectDefinition.getObjectDefinitionId()) {
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject jsonObject = apiRequestHandler.convertEntryToArray(values);
         return new CustomBOIHeadlessResponse(jsonObject);
      } else {
         return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject());
      }
   }

   public static boolean isValidFolderName(String folderName) {
      return folderName != null && !folderName.matches(".*[\\\\/:*?\"<>|!@#$%^&()=+\\[\\]{};,'`~\\s].*");
   }

   private BOIHeadlessResponse addFolder(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!isValidFolderName(ParamUtil.getString(this.contextHttpServletRequest, "folderName"))) {
            jsonObject.put("Error", "Invalid folder name. Only letters, numbers, '-', '_', and spaces allowed; must start with letter, number, '-' or '_'");
            jsonObject.put("ResponseCode", "301");
            return new CustomBOIHeadlessResponse(jsonObject);
         }

         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleAddFolderRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, this.defaultFolderId);
         if (Validator.isNotNull(objectEntry)) {
            jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
            jsonObject.put("values", objectEntry.getValues());
         }
      } catch (Exception e) {
         String msg = e.getMessage();
         if (Validator.isNotNull(msg) && msg.contains("ResponseCode")) {
            String code = msg.replaceAll(".*ResponseCode\\s*=\\s*(\\d+).*", "$1");
            String message = msg.replaceAll(".*Message\\s*=\\s*", "");
            jsonObject.put("ResponseCode", code);
            jsonObject.put("Error", message);
         } else {
            jsonObject.put("Error", msg);
         }
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "UTOPIA")).validateRequestFiles();
   }

   private BOIHeadlessResponse addDocument(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         Map<String, String> fileUpdload = new HashMap();
         fileUpdload.put("imageData", "imageData");
         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleProdAddDocumentRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, fileUpdload);
         if (Validator.isNotNull(objectEntry)) {
            jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
            jsonObject.put("values", objectEntry.getValues());
         }
      } catch (Exception e) {
         String msg = e.getMessage();
         if (Validator.isNotNull(msg) && msg.contains("ResponseCode")) {
            String code = msg.replaceAll(".*ResponseCode\\s*=\\s*(\\d+).*", "$1");
            String message = msg.replaceAll(".*Message\\s*=\\s*", "");
            jsonObject.put("ResponseCode", code);
            jsonObject.put("Error", message);
         } else {
            jsonObject.put("Error", msg);
         }
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   private boolean hasUTOPIARole(User user) {
      return user != null && user.getRoles().stream().anyMatch((role) -> "UTOPIA".equalsIgnoreCase(role.getName()));
   }

   private APIRequestHandler getAPIRequestHandler() {
      log.info("APIRequestHandler Created for UTOPIA");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this.objectFieldLocalService, this.dlAppService, this.contextUriInfo, this.dtoConverterRegistry, this.objectEntryManagerRegistry, this.objectDefinitionLocalService, this.objectEntryLocalService, this.workflowTaskManager, this.boiSpaConfiguation, "UTOPIA", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
      this.defaultFolderId = this.boiSpaConfiguation.SPADefualtFolderId();
      this.objectDefinitionId = this.boiSpaConfiguation.UTOPIAFormObjectDefinitionId();
   }
}
