package boi.headless.delivery.memorandumapi.internal.resource.v1_0;

import boi.headless.delivery.memorandum.configuration.PAIMSConfiguration;
import boi.headless.delivery.memorandum.constant.MemorandumConstants;
import boi.headless.delivery.memorandum.util.FileUtil;
import boi.headless.delivery.memorandum.util.PAIMSCommon;
import boi.headless.delivery.memorandumapi.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.memorandumapi.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.memorandumapi.dto.v1_0.MemorandumResponse;
import boi.headless.delivery.memorandumapi.resource.v1_0.PAIMSConsentResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.memorandum.configuration.PAIMSConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/paims-consent.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {PAIMSConsentResource.class}
)
public class PAIMSConsentResourceImpl extends BasePAIMSConsentResourceImpl {
   private Set<String> statusList = new LinkedHashSet<String>() {
      private static final long serialVersionUID = 1L;

      {
         this.add("pending with branch maker");
         this.add("pending at branch checker");
         this.add("approved");
      }
   };
   private Map<String, String> fileUpload = MemorandumConstants.getObjectFileNames("PAIMS");
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private long workflowObjectDefinitionId = 0L;
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";
   Log _log = LogFactoryUtil.getLog(this.getClass());
   private PAIMSConfiguration paimsConfiguration;
   private DLAppService dlAppService;
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

   public BOIHeadlessResponse approveOrRejectPaims(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         PAIMSCommon _common = new PAIMSCommon(this.contextHttpServletRequest, this.contextUser, this.paimsConfiguration, "");
         jsonObject = _common.approveOrRejectPAIMS(this.dlAppService, multipartBody);
         if (jsonObject.has("errors") && jsonObject.getJSONArray("errors") != null) {
            jsonObject.put("Error", jsonObject.getJSONArray("errors"));
            return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
         } else if (jsonObject.has("status") && "error".equalsIgnoreCase(String.valueOf(jsonObject.get("status")))) {
            jsonObject.put("Error", jsonObject.get("message"));
            return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
         } else {
            return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse addPaimsAsExport(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map<String, Serializable> values = new HashMap();
      String referenceCode = null;
      List<FileEntry> files = new ArrayList();

      try {
         PAIMSCommon _common = new PAIMSCommon(this.contextHttpServletRequest, this.contextUser, this.paimsConfiguration, "");
         ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (_common.isPAIMS && _common.isBranchMaker) {
            if (entryId != 0L) {
               objectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
               if (objectEntry == null) {
                  jsonObject.put("Error", "Object entry not found for entryId: " + entryId);
                  return MemorandumResponse.builder().status(Status.NOT_FOUND).data(jsonObject).build();
               }

               values = objectEntry.getValues();
               referenceCode = objectEntry.getExternalReferenceCode();
            }

            FileUtil fileUtil = new FileUtil(multipartBody, "PAIMS");
            boolean isFilePresent = true;
            BinaryFile binaryFile = multipartBody.getBinaryFile("file");
            if (Validator.isNull(binaryFile) || Validator.isBlank(binaryFile.getFileName())) {
               isFilePresent = false;
            }

            Map<String, BinaryFile> validateFile = fileUtil.validateFile(jsonObject);
            if (isFilePresent && MapUtil.isEmpty(validateFile)) {
               return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
            } else {
               if (isFilePresent) {
                  List<FileEntry> var20 = fileUtil.uploadFile(this.defaultFolderId, jsonObject, (String)null, validateFile);
                  if (var20.size() > 0) {
                     FileEntry fileEntry = (FileEntry)var20.get(0);
                     String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                     values.put("paimsAttachment", downloadURL);
                  }
               }

               com.liferay.object.model.ObjectEntry savedExport = _common.saveAsExport(this.contextHttpServletRequest, values, this.objectDefinitionId, referenceCode);
               jsonObject.put("entryId", savedExport.getObjectEntryId());
               jsonObject.put("referenceCode", savedExport.getExternalReferenceCode());
               jsonObject.put("message", "Export saved successfully");
               return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
            }
         } else {
            jsonObject.put("Error", "You are not authorized to access PAIMS services.");
            return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
         }
      } catch (ValidationException ve) {
         jsonObject.put("Error", ve.getMessage());
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
      } catch (Exception e) {
         for(FileEntry file : files) {
            try {
               DLAppServiceUtil.deleteFileEntry(file.getFileEntryId());
            } catch (Exception ex) {
               this._log.error("File rollback failed", ex);
            }
         }

         jsonObject.put("Error", "Internal server error: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse addPaimsAsDraft(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map<String, Serializable> values = new HashMap();
      String referenceCode = null;
      List<FileEntry> files = new ArrayList();

      try {
         PAIMSCommon _common = new PAIMSCommon(this.contextHttpServletRequest, this.contextUser, this.paimsConfiguration, "");
         ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (_common.isPAIMS && _common.isBranchMaker) {
            if (entryId != 0L) {
               objectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
               if (objectEntry == null) {
                  jsonObject.put("Error", "Object entry not found for entryId: " + entryId);
                  return MemorandumResponse.builder().status(Status.NOT_FOUND).data(jsonObject).build();
               }

               values = new HashMap(objectEntry.getValues());
               referenceCode = objectEntry.getExternalReferenceCode();
            }

            boolean isFilePresent = true;
            BinaryFile binaryFile = multipartBody.getBinaryFile("file");
            if (Validator.isNull(binaryFile) || Validator.isBlank(binaryFile.getFileName())) {
               isFilePresent = false;
            }

            if (isFilePresent) {
               FileUtil fileUtil = new FileUtil(multipartBody, "PAIMS");
               Map<String, BinaryFile> validateFile = fileUtil.validateFile(jsonObject);
               if (MapUtil.isEmpty(validateFile)) {
                  return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
               }

               List<FileEntry> var20 = fileUtil.uploadFile(this.defaultFolderId, jsonObject, (String)null, validateFile);
               if (!var20.isEmpty()) {
                  FileEntry fileEntry = (FileEntry)var20.get(0);
                  String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                  values.put("paimsAttachment", downloadURL);
               }
            }

            com.liferay.object.model.ObjectEntry savedDraft = _common.saveAsDraft(this.contextHttpServletRequest, values, this.objectDefinitionId, referenceCode);
            jsonObject.put("entryId", savedDraft.getObjectEntryId());
            jsonObject.put("message", "Draft saved successfully");
            return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
         } else {
            jsonObject.put("Error", "You are not authorized to access PAIMS services.");
            return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
         }
      } catch (ValidationException ve) {
         jsonObject.put("Error", ve.getMessage());
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
      } catch (Exception var19) {
         for(FileEntry file : files) {
            try {
               DLAppServiceUtil.deleteFileEntry(file.getFileEntryId());
            } catch (Exception var17) {
            }
         }

         jsonObject.put("Error", "Internal server error");
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse getPaims(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         return this.getPAIMSEntry((long)id);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse getPaimsList(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
         sorts = new Sort[]{sort};
         return this.listData(search, filter, pagination, sorts);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse getPAIMSEntry(long entryId) {
      JSONObject paimsDetailJson = JSONFactoryUtil.createJSONObject();

      try {
         PAIMSCommon _common = new PAIMSCommon(this.contextHttpServletRequest, this.contextUser, this.paimsConfiguration);
         if (!_common.isPAIMS) {
            paimsDetailJson.put("Error", "You are not authorized to access PAIMS services.");
            return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(paimsDetailJson).build();
         } else {
            paimsDetailJson = _common.getPAIMSDetailWithActions(entryId);
            if (!paimsDetailJson.has("errors") || Validator.isNull(paimsDetailJson.opt("errors"))) {
               this.addWorkflowLogs(paimsDetailJson, entryId);
            }

            return MemorandumResponse.builder().status(Status.OK).data(paimsDetailJson).build();
         }
      } catch (Exception e) {
         paimsDetailJson.put("Error", "Failed to fetch paims details: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(paimsDetailJson).build();
      }
   }

   private void addWorkflowLogs(JSONObject jsonObject, long objectEntryId) throws Exception {
      ObjectDefinition workflowlogs = ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(this.workflowObjectDefinitionId);
      if (workflowlogs == null) {
         jsonObject.put("logs", JSONFactoryUtil.createJSONArray());
      } else {
         String query = "paimsId eq " + objectEntryId;
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(workflowlogs.getStorageType());
         Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
         Sort[] sorts = new Sort[]{sort};
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(workflowlogs.getCompanyId(), workflowlogs, workflowlogs.getScope(), (Aggregation)null, this.getDTOConverterContext((Long)null), query, Pagination.of(-1, -1), (String)null, sorts);
         jsonObject.put("logs", this.getArray(page));
      }
   }

   private JSONArray getArray(Page<ObjectEntry> page) {
      JSONArray items = JSONFactoryUtil.createJSONArray();
      if (Validator.isNotNull(page)) {
         page.getItems().forEach((x) -> {
            JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
            x.getProperties().forEach((c, d) -> itemObjects.put(c, d));
            itemObjects.put("customAction", x.getPropertyValue("customAction"));
            itemObjects.put("actionBy", x.getPropertyValue("actionBy"));
            itemObjects.put("remarks", x.getPropertyValue("remarks"));
            itemObjects.put("dateModified", x.getDateModified());
            itemObjects.put("dateCreated", x.getDateCreated());
            itemObjects.put("id", x.getPropertyValue("paimsId"));
            items.put(itemObjects);
         });
      }

      return items;
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      PAIMSCommon _common = new PAIMSCommon(this.contextHttpServletRequest, this.contextUser, this.paimsConfiguration);
      this._log.info(_common.toString());
      if (!_common.isPAIMS) {
         JSONObject paimsDetailJson = JSONFactoryUtil.createJSONObject();
         paimsDetailJson.put("Error", "You are not authorized to access PAIMS services.");
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(paimsDetailJson).build();
      } else {
         boolean isHOChecker = _common.isHOChecker;
         String userFilterQuery = isHOChecker ? "" : _common.getUserFilterQuery();
         this._log.info(userFilterQuery);
         boolean isBranchMaker = _common.isBranchMaker;
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(this.objectDefinitionId);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         Page<ObjectEntry> page = null;
         if (!isHOChecker && Validator.isBlank(userFilterQuery)) {
            return new CustomBOIHeadlessResponseAsList(page, isBranchMaker, "", this.statusList);
         } else {
            page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, this.getDTOConverterContext((Long)null), userFilterQuery, pagination, search, sorts);
            this._log.info(page.getTotalCount());
            return new CustomBOIHeadlessResponseAsList(page, isBranchMaker, "", this.statusList);
         }
      }
   }

   private DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
      return new DefaultDTOConverterContext(this.contextAcceptLanguage.isAcceptAllLanguages(), (Map)null, this._dtoConverterRegistry, this.contextHttpServletRequest, objectEntryId, this.contextAcceptLanguage.getPreferredLocale(), this.contextUriInfo, this.contextUser);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.paimsConfiguration = (PAIMSConfiguration)ConfigurableUtil.createConfigurable(PAIMSConfiguration.class, properties);
      this.defaultFolderId = this.paimsConfiguration.PAIMSDefaultFolderId();
      this.objectDefinitionId = this.paimsConfiguration.PAIMSObjectDefinitionId();
      this.workflowObjectDefinitionId = this.paimsConfiguration.PAIMSWorkflowLogsDefinitionId();
   }
}
