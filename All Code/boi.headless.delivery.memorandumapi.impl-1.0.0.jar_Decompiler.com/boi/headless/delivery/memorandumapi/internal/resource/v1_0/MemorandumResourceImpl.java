package boi.headless.delivery.memorandumapi.internal.resource.v1_0;

import boi.headless.delivery.memorandum.configuration.ConfigurationUtil;
import boi.headless.delivery.memorandum.util.FileUtil;
import boi.headless.delivery.memorandum.util.MemoCommon;
import boi.headless.delivery.memorandum.util.MemoManager;
import boi.headless.delivery.memorandumapi.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.memorandumapi.dto.v1_0.MemorandumResponse;
import boi.headless.delivery.memorandumapi.resource.v1_0.MemorandumResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   properties = {"OSGI-INF/liferay/rest/v1_0/memorandum.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {MemorandumResource.class}
)
public class MemorandumResourceImpl extends BaseMemorandumResourceImpl {
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";
   private MemoManager _manager = null;
   private MemoCommon memoCommon = null;
   private DLAppService dlAppService;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   private Log log = LogFactoryUtil.getLog(MemorandumResourceImpl.class);

   public BOIHeadlessResponse approveOrRejectMemo(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
         jsonObject = this.memoCommon.approveOrRejectMemo(this.contextHttpServletRequest, this.contextUser, this.dlAppService, multipartBody);
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

   public BOIHeadlessResponse getMemoList(String search, Filter filter, Pagination pagination, Sort[] sorts) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
         String userFilterQuery = this.getUserFilterQuery();
         Sort defaultSort = SortFactoryUtil.create("modifiedDate", 3, true);
         if (sorts == null || sorts.length == 0) {
            sorts = new Sort[]{defaultSort};
         }

         Page<ObjectEntry> listData = this.memoCommon.getListData(search, filter, pagination, sorts, userFilterQuery, this.contextUser, this._manager, this.getDTOConverterContext((Long)null));
         JSONObject filterPageCustom = this.memoCommon.filterPageCustom(listData);
         return MemorandumResponse.builder().status(Status.OK).data(filterPageCustom).build();
      } catch (Exception e) {
         jsonObject.put("Error", "Failed to fetch memo details: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse GetMemoPreData() throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
         JSONObject memoPreData = this.memoCommon.getMemoPreData(this._manager, this.getDTOConverterContext((Long)null));
         return MemorandumResponse.builder().status(Status.OK).data(memoPreData).build();
      } catch (Exception e) {
         jsonObject.put("Error", "Failed to fetch memo details: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse getMemo(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
         jsonObject = this.memoCommon.getMemoDetailWithLogs((long)id, this.contextUser, this._manager, this.getDTOConverterContext((Long)null));
         return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
      } catch (Exception e) {
         jsonObject.put("Error", "Failed to fetch memo details: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse addMemoAsDraft(MultipartBody multipartBody) {
      this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map<String, Serializable> values = new HashMap();
      String referenceCode = null;
      List<FileEntry> files = new ArrayList();

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (entryId != 0L) {
            objectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
            if (objectEntry == null) {
               jsonObject.put("Error", "Object entry not found for entryId: " + entryId);
               return MemorandumResponse.builder().status(Status.NOT_FOUND).data(jsonObject).build();
            }

            values = new HashMap(objectEntry.getValues());
            referenceCode = objectEntry.getExternalReferenceCode();
         }

         if (referenceCode == null) {
            String var10000 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            referenceCode = "MEMO_" + var10000 + "_" + UUID.randomUUID().toString().substring(0, 8);
         }

         FileUtil fileUtil = new FileUtil(multipartBody, "MEMORANDUM");
         long defaultFolderId = ConfigurationUtil.getDefaultFolderId();
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
               List<FileEntry> var21 = fileUtil.uploadFile(defaultFolderId, jsonObject, (String)null, validateFile);
               if (!var21.isEmpty()) {
                  FileEntry fileEntry = (FileEntry)var21.get(0);
                  String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                  values.put("memoAttachment", downloadURL);
               }
            }

            com.liferay.object.model.ObjectEntry savedDraft = this.memoCommon.saveAsDraft(this.contextHttpServletRequest, values, ConfigurationUtil.getMemoObjectDefinitionId(), this.contextUser, referenceCode);
            jsonObject.put("entryId", savedDraft.getObjectEntryId());
            jsonObject.put("message", "Draft saved successfully");
            return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
         }
      } catch (ValidationException ve) {
         jsonObject.put("Error", ve.getMessage());
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
      } catch (Exception e) {
         this.log.error("Error in addMemoAsDraft", e);

         for(FileEntry file : files) {
            try {
               DLAppServiceUtil.deleteFileEntry(file.getFileEntryId());
            } catch (Exception ex) {
               this.log.error("File rollback failed", ex);
            }
         }

         jsonObject.put("Error", "Internal server error");
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   public BOIHeadlessResponse addMemoAsExport(MultipartBody multipartBody) {
      this.initMemoComponents(this.contextUser, this._dtoConverterRegistry, this._objectEntryManagerRegistry);
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map<String, Serializable> values = new HashMap();
      String referenceCode = null;
      List<FileEntry> files = new ArrayList();

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long entryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         com.liferay.object.model.ObjectEntry objectEntry = null;
         if (entryId != 0L) {
            objectEntry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
            if (objectEntry == null) {
               jsonObject.put("Error", "Object entry not found for entryId: " + entryId);
               return MemorandumResponse.builder().status(Status.NOT_FOUND).data(jsonObject).build();
            }

            values = objectEntry.getValues();
            referenceCode = objectEntry.getExternalReferenceCode();
         }

         FileUtil fileUtil = new FileUtil(multipartBody, "MEMORANDUM");
         long defaultFolderId = ConfigurationUtil.getDefaultFolderId();
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
               List<FileEntry> var21 = fileUtil.uploadFile(defaultFolderId, jsonObject, (String)null, validateFile);
               if (var21.size() > 0) {
                  FileEntry fileEntry = (FileEntry)var21.get(0);
                  String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                  values.put("memoAttachment", downloadURL);
               }
            }

            com.liferay.object.model.ObjectEntry savedExport = this.memoCommon.saveAsExport(this.contextHttpServletRequest, values, ConfigurationUtil.getMemoObjectDefinitionId(), this.contextUser, referenceCode);
            jsonObject.put("entryId", savedExport.getObjectEntryId());
            jsonObject.put("referenceCode", savedExport.getExternalReferenceCode());
            jsonObject.put("message", "Export saved successfully");
            return MemorandumResponse.builder().status(Status.OK).data(jsonObject).build();
         }
      } catch (ValidationException ve) {
         jsonObject.put("Error", ve.getMessage());
         return MemorandumResponse.builder().status(Status.BAD_REQUEST).data(jsonObject).build();
      } catch (Exception e) {
         for(FileEntry file : files) {
            try {
               DLAppServiceUtil.deleteFileEntry(file.getFileEntryId());
            } catch (Exception ex) {
               this.log.error("File rollback failed", ex);
            }
         }

         jsonObject.put("Error", "Internal server error: " + e.getMessage());
         return MemorandumResponse.builder().status(Status.INTERNAL_SERVER_ERROR).data(jsonObject).build();
      }
   }

   private String getUserFilterQuery() {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status", (String)null);
      boolean onlyShowApproved = this._manager.onlyShowApproved();
      if (onlyShowApproved) {
         queryList.add("currentStatus eq 'Approved'");
      } else {
         if (Validator.isNotNull(status) && !status.isBlank()) {
            queryList.add("currentStatus eq '" + status + "'");
         }

         boolean isMaker = this._manager.isMaker();
         if (!isMaker) {
            queryList.add("currentStatus ne 'Draft'");
            queryList.add("currentStatus ne 'Denied'");
         }
      }

      this.addHierarchyFilter(queryList);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      return queryResult;
   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + (String)isoRange.get("toDate");
         queryList.add(dateFilter);
      }

   }

   private Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
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

   private void addHierarchyFilter(List<String> queryList) {
      if (!Validator.isBlank(this._manager.getBranchName())) {
         queryList.add("branch eq '" + this._manager.getBranchName() + "'");
      } else if (!Validator.isBlank(this._manager.getZoneName())) {
         queryList.add("zone eq '" + this._manager.getZoneName() + "'");
      } else if (!Validator.isBlank(this._manager.getNbgName())) {
         queryList.add("nBG eq '" + this._manager.getNbgName() + "'");
      } else {
         if (!Validator.isBlank(this._manager.getHoName())) {
            queryList.add("ho eq '" + this._manager.getHoName() + "'");
         }

      }
   }

   private void initMemoComponents(User contextUser, DTOConverterRegistry _dtoConverterRegistry, ObjectEntryManagerRegistry _objectEntryManagerRegistry) {
      if (Validator.isNull(this._manager)) {
         this._manager = (new MemoManager.Builder(contextUser, _dtoConverterRegistry, _objectEntryManagerRegistry)).build();
      }

      if (Validator.isNull(this.memoCommon)) {
         this.memoCommon = new MemoCommon();
      }

   }

   private DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
      return new DefaultDTOConverterContext(this.contextAcceptLanguage.isAcceptAllLanguages(), (Map)null, this._dtoConverterRegistry, this.contextHttpServletRequest, objectEntryId, this.contextAcceptLanguage.getPreferredLocale(), this.contextUriInfo, this.contextUser);
   }
}
