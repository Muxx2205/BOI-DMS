package boi.headless.delivery.memorandumapi.internal.resource.v1_0;

import boi.headless.delivery.memorandum.configuration.PSRSConfiguration;
import boi.headless.delivery.memorandum.util.FileUtil;
import boi.headless.delivery.memorandum.util.PSRSCommon;
import boi.headless.delivery.memorandum.util.PSRSWorkflow;
import boi.headless.delivery.memorandumapi.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.memorandumapi.dto.v1_0.CustomResponseAsList;
import boi.headless.delivery.memorandumapi.dto.v1_0.MemorandumResponse;
import boi.headless.delivery.memorandumapi.resource.v1_0.PSRSDocumentManagementResource;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.memorandum.configuration.PSRSConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/psrs-document-management.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {PSRSDocumentManagementResource.class}
)
public class PSRSDocumentManagementResourceImpl extends BasePSRSDocumentManagementResourceImpl {
   List<String> statusFilterWorkflow = List.of("Pending for Observations Noted", "Pending at Branch Maker", "Pending at Branch Checker", "Pending at Zone Maker", "Pending at Zone Checker", "Pending at Recommending Authority", "Pending at Noting Authority", "Approved at Branch Level", "Approved at Zone Level", "Approved");
   private PSRSConfiguration psrsConfiguration;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   Log _log = LogFactoryUtil.getLog(this.getClass());

   public BOIHeadlessResponse addPSRSExcelData(@NotNull String reportType, Object[] objects) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         return this.addExcelData(reportType, objects);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse GetExcelPreData() throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         return this.excelPreData();
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse getPSRSList(@NotNull String reportType, String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
         sorts = new Sort[]{sort};
         return this.listData(reportType, search, filter, pagination, sorts);
      } catch (Exception e) {
         jsonObject.put("error : ", e.getMessage());
         return MemorandumResponse.builder().status(Status.NOT_ACCEPTABLE).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse ExportToPDF(String search, Filter filter, Pagination pagination, Sort[] sorts) {
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();

      try {
         responseJson.put("success", "Background Task Triggered Successfully");
      } catch (Exception e) {
         this._log.error("Error triggering PDF export background task", e);
         responseJson.put("Error", e.getMessage());
      }

      return MemorandumResponse.builder().status(Status.OK).data(responseJson).build();
   }

   public BOIHeadlessResponse addDocument(MultipartBody multipartBody) throws Exception {
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();
      JSONObject uploadErrors = JSONFactoryUtil.createJSONObject();
      FileUtil fileUtil = new FileUtil(multipartBody, "PSRS");
      long uploadFolderId = this.psrsConfiguration.headOfficeFolderId();
      long psrsFolderId = this.psrsConfiguration.PSRSFolderId();
      long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "id", 0L);
      long fileEntryId = ParamUtil.getLong(this.contextHttpServletRequest, "fileId", 0L);
      if (fileEntryId != 0L) {
         try {
            DLFileEntryLocalServiceUtil.deleteDLFileEntry(fileEntryId);
         } catch (Exception e) {
            this._log.info(e.getMessage());
         }
      }

      String templateName = ParamUtil.getString(this.contextHttpServletRequest, "moduleName");
      if (!Arrays.asList("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8").contains(templateName.toLowerCase())) {
         responseJson.put("error", "TemplateName is not proper.");
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
      } else if (entryId == 0L) {
         responseJson.put("error", "entryId is required");
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
      } else {
         Map<String, BinaryFile> validateFile = fileUtil.validateFile(uploadErrors);
         if (MapUtil.isEmpty(validateFile)) {
            responseJson.put("errors", uploadErrors);
            return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
         } else {
            long folderId = fileUtil.getPSRSFolder(uploadFolderId, psrsFolderId, templateName, this.contextUser);
            List<FileEntry> workflowFileEntries = fileUtil.uploadFile(folderId, uploadErrors, (String)null, validateFile);
            if (workflowFileEntries != null && !workflowFileEntries.isEmpty()) {
               ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
               if (!ListUtil.isNull(workflowFileEntries) && !ListUtil.isEmpty(workflowFileEntries)) {
                  FileEntry fileEntry = (FileEntry)workflowFileEntries.get(0);
                  String downloadUrl = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                  responseJson.put("contentUrl", downloadUrl);
                  responseJson.put("fileName", fileEntry.getFileName());
                  responseJson.put("id", fileEntry.getFileEntryId());
                  responseJson.put("title", fileEntry.getTitle());
                  responseJson.put("message", "Files uploaded successfully");
                  return MemorandumResponse.builder().status(Status.OK).data(responseJson).build();
               } else {
                  responseJson.put("message", "Files uploaded failed");
                  return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
               }
            } else {
               responseJson.put("errors", uploadErrors);
               return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
            }
         }
      }
   }

   public MemorandumResponse approveOrRejectPSRS(MultipartBody multipartBody) {
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();

      try {
         PSRSWorkflow psrsWorkflow = new PSRSWorkflow(this.contextHttpServletRequest, this.contextUser, this.contextCompany);
         long objectEntryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId");
         String action = ParamUtil.getString(this.contextHttpServletRequest, "action");
         String description = ParamUtil.getString(this.contextHttpServletRequest, "description");
         this._log.info("Received parameters: entryId=" + objectEntryId + ", action=" + action + ", description=" + description);
         long companyId = this.contextCompany.getCompanyId();
         long userId = this.contextUser.getUserId();
         if (objectEntryId <= 0L) {
            this._log.warn("Invalid entryId: " + objectEntryId);
            responseJson.put("message", "Invalid entryId");
            return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
         } else if (!"resubmit".equalsIgnoreCase(action) && !"approve".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action) && !"complete".equalsIgnoreCase(action) && !"Noted & Closed".equalsIgnoreCase(action) && !"Noted with Observations".equalsIgnoreCase(action)) {
            responseJson.put("message", "Action must be approve or reject");
            return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
         } else {
            String actionMapped = psrsWorkflow.getTransitionNode();
            this._log.info("Mapped action: " + actionMapped);
            if (!Validator.isNull(actionMapped) && !actionMapped.isEmpty()) {
               try {
                  ServiceContext serviceContext = new ServiceContext();
                  String collST = (String)PSRSWorkflow.statusMap.get(actionMapped);
                  String[] currentFlow = (String[])PSRSWorkflow.stageFlow.get(collST);
                  if (currentFlow == null) {
                     throw new IllegalArgumentException("Invalid stageFlow mapping for: " + collST);
                  } else {
                     Map<String, Serializable> values = HashMapBuilder.put("boiStatus", currentFlow[1]).put("collST", collST).put("boiCurrent", currentFlow[0]).build();
                     ObjectEntryLocalServiceUtil.updateObjectEntry(userId, objectEntryId, values, serviceContext);
                     responseJson.put("message", "Workflow " + action + " successful");
                     this._log.info("Returning success response for entryId=" + objectEntryId);
                     return MemorandumResponse.builder().status(Status.OK).data(responseJson).build();
                  }
               } catch (Exception e) {
                  this._log.error("WorkflowException while processing entryId=" + ParamUtil.getLong(this.contextHttpServletRequest, "entryId"), e);
                  responseJson.put("error", "Workflow error");
                  responseJson.put("details", e.getMessage());
                  return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(responseJson).build();
               }
            } else {
               this._log.warn("Invalid or unmapped workflow action: " + action);
               responseJson.put("message", "Invalid workflow action: " + action);
               return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(responseJson).build();
            }
         }
      } catch (Exception e) {
         this._log.error("Unexpected exception while processing entryId=" + ParamUtil.getLong(this.contextHttpServletRequest, "entryId"), e);
         responseJson.put("error", "Unexpected error");
         responseJson.put("details", e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(responseJson).build();
      }
   }

   private BOIHeadlessResponse listData(String reportType, String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONArray items = JSONFactoryUtil.createJSONArray();
      boolean canExportToPDF = false;
      boolean isRemark = false;
      boolean isComplianceRemarks = false;
      boolean isObservation = false;
      String isCertificate = "";
      PSRSCommon _common = new PSRSCommon(this.contextHttpServletRequest, this.contextUser, this.psrsConfiguration, reportType);
      boolean hoChecker = _common.isHoChecker();
      String userFilterQuery = _common.getUserFilterQuery();
      long objectDefinitionId = _common.getObjectDefinitionId();
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
      ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Page<ObjectEntry> page = null;
      if (!hoChecker && Validator.isBlank(userFilterQuery)) {
         return new CustomResponseAsList(page, items, "", canExportToPDF, isRemark, isObservation, isCertificate, this.psrsConfiguration.CommonHideColumns(), _common.getOrderColumns(), (List)null, "", (List)null, (List)null);
      } else {
         page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, this.getDTOConverterContext((Long)null), userFilterQuery, pagination, search, sorts);
         this._log.info("List :- " + page.getTotalCount());
         _common.buildItemJsonArrayMain(page, items);
         if (_common.isMonthlyCheckListReport()) {
            return new CustomResponseAsList(page, items, "", canExportToPDF, isRemark, isObservation, isCertificate, this.psrsConfiguration.CommonHideColumns(), _common.getOrderColumns(), (List)null, "", (List)null, (List)null);
         } else {
            boolean isExport = ParamUtil.get(this.contextHttpServletRequest, "export", false);
            String url = "not-available";
            this._log.info("isExport :- " + isExport);
            if (_common.isHaveExportToPDFRole()) {
               canExportToPDF = true;
               if (isExport) {
                  _common.exportToPDF(search, userFilterQuery, _common.getReportName());
                  url = "in-progress";
               } else {
                  url = _common.getTaskCompletedURL(objectDefinitionId, _common.getReportName());
               }
            }

            if (_common.isBranchMaker || _common.isBranchChecker) {
               isRemark = true;
               isCertificate = _common.isBranchChecker ? "true" : "";
               this._log.info("Current Role :- Branch");
            }

            List<String> zoneFilter = null;
            List<String> branchFilter = null;
            if (_common.isZoneMaker || _common.isZoneChecker) {
               isObservation = true;
               isCertificate = "false";
               this._log.info("Current Role :- Zone");
            }

            if (_common.isZoneMaker || _common.isZoneChecker || hoChecker || _common.isRADZM || _common.isNotingAuthority) {
               zoneFilter = _common.zoneFilter();
               branchFilter = ListUtil.isEmpty(zoneFilter) ? Arrays.asList("Head Office") : _common.branchFilter();
            }

            List<String> statusFilter = _common.isWorkflowReport() ? this.statusFilterWorkflow : Collections.emptyList();
            this._log.info("url :- " + url);
            return new CustomResponseAsList(page, items, url, canExportToPDF, isRemark, isObservation, isCertificate, this.psrsConfiguration.CommonHideColumns(), _common.getOrderColumns(), statusFilter, _common.getDateFilter(), branchFilter, zoneFilter);
         }
      }
   }

   private MemorandumResponse excelPreData() throws JSONException {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      jsonObject.put("M1-STATEMENT OF TEMPORARY OVERDRAFT BUSINESS", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M1ObjectColumns()));
      jsonObject.put("M2-STATEMENT OF OVERLIMIT BUSINESS", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M2ObjectColumns()));
      jsonObject.put("M3-STATEMENT OF TOD/TOL/BCP/CP ON CASUAL BASIS GRANTED", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M3ObjectColumns()));
      jsonObject.put("M4-REPORTING OF NEW ADVANCES SANCTIONED", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M4ObjectColumns()));
      jsonObject.put("M5-MONTHLY STATEMENT OF FRESH DISBURSEMENT", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M5ObjectColumns()));
      jsonObject.put("M6-MONTHLY STATEMENT OF REVIEW OF ACS WITH SAME LIMITS", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M6ObjectColumns()));
      jsonObject.put("M7-MONTHLY STATEMENT OF REVIEW OF ACS WITH ADDITIONAL LIMITS", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M7ObjectColumns()));
      jsonObject.put("M8-MONTHLY STATEMENT OF REVIEW OF ACS WITH REDUCTION IN LIMITS", JSONFactoryUtil.createJSONObject(this.psrsConfiguration.M8ObjectColumns()));
      return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
   }

   private MemorandumResponse addExcelData(String reportType, Object[] object) {
      boolean hasRole = this.contextUser.getRoles().stream().anyMatch((x) -> "PSRS Data Upload".equalsIgnoreCase(x.getName()));
      if (!hasRole) {
         JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
         jsonObject.put("error", "Access Denied .Please Contact Support Team.");
         jsonObject.put("row", "0");
         return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
      } else {
         PSRSCommon _common = new PSRSCommon(this.contextHttpServletRequest, this.contextUser, this.psrsConfiguration, reportType, false);

         try {
            int start = 0;
            int delta = 100;
            List<Organization> branchOrg = new ArrayList();
            List<Organization> zoneOrg = new ArrayList();

            List<Organization> organizations;
            do {
               organizations = OrganizationLocalServiceUtil.getOrganizations(start, start + delta);

               for(Organization org : organizations) {
                  String type = org.getType();
                  if (type != null) {
                     if ("BRANCH".equalsIgnoreCase(type)) {
                        branchOrg.add(org);
                     } else if ("ZONE".equalsIgnoreCase(type)) {
                        zoneOrg.add(org);
                     }
                  }
               }

               start += delta;
            } while(!organizations.isEmpty());

            JSONObject jsonObject = _common.validateAndAddData(reportType, object, zoneOrg, branchOrg);
            return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
         } catch (Exception e) {
            this._log.error("Error while processing Excel data", e);
            JSONObject errorResponse = JSONFactoryUtil.createJSONObject();
            errorResponse.put("error", e.getMessage());
            return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(errorResponse).build();
         }
      }
   }

   private DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
      return new DefaultDTOConverterContext(this.contextAcceptLanguage.isAcceptAllLanguages(), (Map)null, this._dtoConverterRegistry, this.contextHttpServletRequest, objectEntryId, this.contextAcceptLanguage.getPreferredLocale(), this.contextUriInfo, this.contextUser);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.psrsConfiguration = (PSRSConfiguration)ConfigurableUtil.createConfigurable(PSRSConfiguration.class, properties);
   }
}
