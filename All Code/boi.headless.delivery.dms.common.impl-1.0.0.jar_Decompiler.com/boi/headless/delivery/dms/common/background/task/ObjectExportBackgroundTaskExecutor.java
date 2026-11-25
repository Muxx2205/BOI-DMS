package boi.headless.delivery.dms.common.background.task;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.UriInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"boi.headless.delivery.dms.common.background.task.DMSCommonAPIConfiguation"},
   property = {"background.task.executor.class.name=boi.headless.delivery.dms.common.background.task.ObjectExportBackgroundTaskExecutor"},
   service = {BackgroundTaskExecutor.class}
)
public class ObjectExportBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {
   JSONObject jsonObject = null;
   private DMSCommonAPIConfiguation dmsCommonAPIConfiguation;
   @Reference
   private DLAppService _dlAppService;
   private SXSSFWorkbook workbook;
   private Sheet sheet;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   private static final Log logger = LogFactoryUtil.getLog(ObjectExportBackgroundTaskExecutor.class);

   public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      long companyId = GetterUtil.getLong(taskContextMap.get("companyId"), 0L);
      long userid = GetterUtil.getLong(taskContextMap.get("userId"), 0L);
      String screenName = GetterUtil.getString(taskContextMap.get("screenName"), "default");
      String templateName = GetterUtil.getString(taskContextMap.get("templateName"), "default");
      String fileName = GetterUtil.getString(taskContextMap.get("fileName"), "default");
      long objectDefinitionId = this.getObjectDefinitionIdByTemplateName(templateName);
      long folderId = this.dmsCommonAPIConfiguation.DMSDefualtFolderId();
      String statusQuery = GetterUtil.getString(taskContextMap.get("statusQuery"), "KuchBHi");
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
      User user = UserLocalServiceUtil.getUser(userid);
      DLFolder parentFolder = DLFolderLocalServiceUtil.fetchDLFolder(folderId);
      if (parentFolder == null) {
         logger.info("Failed due to get parent folder .");
         return BackgroundTaskResult.SUCCESS;
      } else {
         DLFolder templateFolder = this.getOrCreateFolder(user.getUserId(), parentFolder.getGroupId(), folderId, "Report - " + templateName);
         if (templateFolder == null) {
            logger.info("Failed due to folder creation for template .");
            return BackgroundTaskResult.SUCCESS;
         } else {
            DLFolder dlFolder = this.getOrCreateYearMonthDayFolder(user.getUserId(), templateFolder);
            if (dlFolder == null) {
               logger.info("Failed due to folder creation for month and year .");
               return BackgroundTaskResult.SUCCESS;
            } else {
               Collection<ObjectEntry> entries = this.getEntries(objectDefinition, user, statusQuery);
               if (Validator.isNotNull(entries)) {
                  this.jsonObject = this.getColumns(objectDefinitionId);
                  ServiceContext serviceContext = (ServiceContext)taskContextMap.get("serviceContext");
                  String url = this.createExcelForObjectEntry(user.getGroupId(), companyId, dlFolder, userid, serviceContext, entries, templateName, fileName);
                  if (url != null) {
                     logger.info("Report saved to document library: " + url);
                  }
               }

               logger.info("companyId " + companyId);
               logger.info("userid " + userid);
               logger.info("screenName " + screenName);
               logger.info("objectDefinitionId " + objectDefinitionId);
               logger.info("folderId " + folderId);
               logger.info("statusQuery " + statusQuery);
               return BackgroundTaskResult.SUCCESS;
            }
         }
      }
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return this;
   }

   private Collection<ObjectEntry> getEntries(ObjectDefinition objectDefinition, User user, String filter) {
      logger.info("getEntries() started");
      logger.info("ObjectDefinitionId: " + objectDefinition.getObjectDefinitionId());
      logger.info("CompanyId: " + objectDefinition.getCompanyId());
      logger.info("Filter: " + filter);

      try {
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, objectDefinition.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         Sort sort = SortFactoryUtil.create("createDate", 3, false);
         Sort[] sorts = new Sort[]{sort};
         logger.debug("Sort configured on createdDate ASC");
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, dtoConverterContext, filter, Pagination.of(-1, -1), (String)null, sorts);
         Collection<ObjectEntry> items = page.getItems();
         int var10001 = items != null ? items.size() : 0;
         logger.info("getEntries() completed. Total items fetched: " + var10001);
         return items;
      } catch (Exception e) {
         logger.error("Error occurred in getEntries()", e);
         return null;
      }
   }

   public File writeBytesToTempFile(byte[] bytes, String prefix, String suffix) throws IOException {
      File tempFile = File.createTempFile(prefix, suffix);
      FileOutputStream fos = new FileOutputStream(tempFile);

      try {
         fos.write(bytes);
      } catch (Throwable var9) {
         try {
            fos.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      fos.close();
      return tempFile;
   }

   private DLFolder getOrCreateYearMonthDayFolder(long userId, DLFolder parentFolder) throws PortalException {
      LocalDate now = LocalDate.now();
      String yearFolderName = String.valueOf(now.getYear());
      DLFolder yearFolder = this.getOrCreateFolder(userId, parentFolder.getGroupId(), parentFolder.getFolderId(), yearFolderName);
      if (yearFolder == null) {
         return yearFolder;
      } else {
         String monthFolderName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
         DLFolder monthFolder = this.getOrCreateFolder(userId, yearFolder.getGroupId(), yearFolder.getFolderId(), monthFolderName);
         if (monthFolder == null) {
            return yearFolder;
         } else {
            String dayFolderName = String.valueOf(now.getDayOfMonth());
            DLFolder dayFolder = this.getOrCreateFolder(userId, monthFolder.getGroupId(), monthFolder.getFolderId(), dayFolderName);
            return dayFolder;
         }
      }
   }

   private DLFolder getOrCreateFolder(long userId, long groupId, long parentFolderId, String folderName) throws PortalException {
      DLFolder folder = DLFolderLocalServiceUtil.fetchFolder(groupId, parentFolderId, folderName);
      if (folder == null) {
         try {
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setAddGroupPermissions(true);
            serviceContext.setAddGuestPermissions(true);
            folder = DLFolderLocalServiceUtil.addFolder((String)null, userId, groupId, groupId, false, parentFolderId, folderName, "Created By Abhishek Sharma: " + folderName, false, serviceContext);
            logger.info("Created folder: " + folderName);
            this.grantFolderPermissions(folder);
         } catch (Exception e) {
            logger.info(e.getMessage());
         }
      }

      return folder;
   }

   private void grantFolderPermissions(DLFolder folder) throws PortalException {
      long companyId = folder.getCompanyId();
      long roleId = RoleLocalServiceUtil.getRole(companyId, "User").getRoleId();
      Map<Long, String[]> rolePermissions = new HashMap();
      rolePermissions.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_DOCUMENT"});
      ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, DLFolder.class.getName(), 4, String.valueOf(folder.getFolderId()), rolePermissions);
   }

   private String createExcelForObjectEntry(long groupId, long companyId, DLFolder folder, long userId, ServiceContext serviceContext, Collection<ObjectEntry> entries, String templateName, String fileName) {
      try {
         byte[] bytes = this.createDownloadExcel(entries);
         File file = this.writeBytesToTempFile(bytes, fileName, ".xlsx");
         FileEntry fileEntry = this._dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(fileName), fileName, " ", " ", file, serviceContext);
         this.grantGuestViewPermissionsIfNeeded(companyId, fileEntry);
         logger.info("Generated download URL: {}");
         return "";
      } catch (Exception ex) {
         Log var10000 = logger;
         String var10001 = ex.getMessage();
         var10000.error("Error generating Excel and creating file entry: {}" + var10001 + ex);
         return "";
      }
   }

   private void grantGuestViewPermissionsIfNeeded(long companyId, FileEntry fileEntry) throws PortalException {
      long roleId = RoleLocalServiceUtil.getRole(companyId, "User").getRoleId();
      String fileEntryIdStr = Long.toString(fileEntry.getFileEntryId());
      boolean hasPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(companyId, DLFileEntry.class.getName(), 4, fileEntryIdStr, roleId, "VIEW");
      if (!hasPermission) {
         Map<Long, String[]> rolePermissions = new HashMap();
         rolePermissions.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, DLFileEntry.class.getName(), 4, fileEntryIdStr, rolePermissions);
         logger.info("Set view/download permissions for Guest role on file entry: {}" + fileEntryIdStr);
      }

   }

   public byte[] createDownloadExcel(Collection<ObjectEntry> entries) {
      this.workbook = new SXSSFWorkbook(50000);
      this.sheet = this.workbook.createSheet("Sheet");
      logger.info("Craeting headre");
      this.fillHeader(this.sheet);
      logger.info("created header");
      this.fillOthersData(entries, this.sheet);
      logger.info("data populated");
      byte[] outArray = null;

      try {
         ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();

         try {
            this.workbook.write(outByteStream);
            outArray = outByteStream.toByteArray();
            this.workbook.dispose();
            this.workbook.close();
         } catch (Throwable var7) {
            try {
               outByteStream.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         outByteStream.close();
      } catch (IOException e) {
         logger.error(e);
      }

      return outArray;
   }

   private void fillHeader(Sheet sh) {
      String[] columns = new String[this.jsonObject.length() + 1];
      Iterator<String> keys = this.jsonObject.keys();
      int index = 1;

      String key;
      for(columns[0] = "S.No"; keys.hasNext(); columns[index++] = this.jsonObject.getString(key)) {
         key = (String)keys.next();
      }

      Row row = null;
      Cell cell = null;

      for(int rownum = 0; rownum < 1; ++rownum) {
         row = sh.createRow(rownum);

         for(int cellnum = 0; cellnum < columns.length; ++cellnum) {
            cell = row.createCell(cellnum);
            cell.setCellValue(columns[cellnum]);
            sh.setColumnWidth(cellnum, 4000);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setLocked(true);
            cell.setCellStyle(cellStyle);
         }
      }

   }

   private void fillOthersData(Collection<ObjectEntry> entries, Sheet sh) {
      int rownum = 1;
      Row row = null;
      int cellCounter = -1;
      int rowCounter = 0;
      Cell cell = null;

      for(ObjectEntry entry : entries) {
         ++rowCounter;
         row = sh.createRow(rowCounter);
         cellCounter = -1;
         ++cellCounter;
         cell = row.createCell(cellCounter);
         cell.setCellValue((double)rownum);

         for(String key : this.jsonObject.keySet()) {
            String value = "";
            if (key.contains("status")) {
               value = String.valueOf(entry.getStatus().getLabel());
            } else if (key.contains("createDate")) {
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               value = String.valueOf(sdf.format(entry.getDateCreated()));
            } else if (key.contains("modifiedDate")) {
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               value = String.valueOf(sdf.format(entry.getDateModified()));
            } else {
               value = String.valueOf(entry.getPropertyValue(key));
            }

            ++cellCounter;
            cell = row.createCell(cellCounter);
            cell.setCellValue(value);
         }

         ++rownum;
      }

   }

   private long getObjectDefinitionIdByTemplateName(String templateName) {
      if (Validator.isNull(templateName)) {
         return 0L;
      } else {
         switch (templateName) {
            case "BoardLevelMeeting":
               return this.dmsCommonAPIConfiguation.BoardLevelMeetingObjectDefinitionId();
            case "CDAD":
               return this.dmsCommonAPIConfiguation.CDADObjectDefinitionId();
            case "InspectionAuditACE":
               return this.dmsCommonAPIConfiguation.InspectionAuditACEObjectDefinitionId();
            case "DataWarehouse":
               return this.dmsCommonAPIConfiguation.DataWareHouseObjectDefinitionId();
            case "EPlatform":
               return this.dmsCommonAPIConfiguation.EPlateFormObjectDefinitionId();
            case "ECRM":
               return this.dmsCommonAPIConfiguation.ECRMFormObjectDefinitionId();
            case "KCCJansamarth":
               return this.dmsCommonAPIConfiguation.KCCJansamarthFormObjectDefinitionId();
            case "UTOPIA":
               return this.dmsCommonAPIConfiguation.UTOPIAFormObjectDefinitionId();
            case "ElectricalWiring":
               return this.dmsCommonAPIConfiguation.ElectricalWiringObjectDefinitionId();
            case "Finastra":
               return this.dmsCommonAPIConfiguation.FinastraObjectDefinitionId();
            case "FinastraOther":
               return this.dmsCommonAPIConfiguation.FinastraOtherObjectDefinitionId();
            case "HORD":
               return this.dmsCommonAPIConfiguation.HORDObjectDefinitionId();
            case "HOSecurityDepartment":
               return this.dmsCommonAPIConfiguation.HOSecurityDepartmentObjectDefinitionId();
            case "InspectionAuditACB":
               return this.dmsCommonAPIConfiguation.InspectionAuditACBObjectDefinitionId();
            case "InspectionAuditCAAppointment":
               return this.dmsCommonAPIConfiguation.InspectionAuditCAAppointmentObjectDefinitionId();
            case "InspectionAuditMisc":
               return this.dmsCommonAPIConfiguation.InspectionAuditMiscObjectDefinitionId();
            case "InspectionAuditOtherReport":
               return this.dmsCommonAPIConfiguation.InspectionAuditOthReportObjectDefinitionId();
            case "InspectionAuditReports":
               return this.dmsCommonAPIConfiguation.InspectionAuditRepotsObjectDefinitionId();
            case "InspectionForeignAudit":
               return this.dmsCommonAPIConfiguation.InspectionForeignAuditObjectDefinitionId();
            case "InternalLayoutMap":
               return this.dmsCommonAPIConfiguation.InternalLayoutMapObjectDefinitionId();
            case "InternationalDepartment":
               return this.dmsCommonAPIConfiguation.InternationalDepObjectDefinitionId();
            case "BranchDigitation":
               return this.dmsCommonAPIConfiguation.BranchDigitationObjectDefinitionId();
            case "ProjectManagementList":
               return this.dmsCommonAPIConfiguation.ProjectManagementListObjectDefinitionId();
            case "CustomerAccount":
               return this.dmsCommonAPIConfiguation.CustomerAccountObjectDefinitionId();
            case "UPIInsurance":
               return this.dmsCommonAPIConfiguation.UPIInsuranceObjectDefinitionId();
            case "CPDInsurance":
               return this.dmsCommonAPIConfiguation.CPDInsuranceObjectDefinitionId();
            case "DBDepartment":
               return this.dmsCommonAPIConfiguation.DBDepartmentObjectDefinitionId();
            case "DBDMandate":
               return this.dmsCommonAPIConfiguation.DBDMandateObjectDefinitionId();
            case "FBD":
               return this.dmsCommonAPIConfiguation.FBDObjectDefinitionId();
            case "FEBONONFinancial":
               return this.dmsCommonAPIConfiguation.FEBONONFinancialObjectDefinitionId();
            case "FinancePloe":
               return this.dmsCommonAPIConfiguation.FinancePloeObjectDefinitionId();
            case "FBDFRBO":
               return this.dmsCommonAPIConfiguation.FBDFRBOObjectDefinitionId();
            default:
               return 0L;
         }
      }
   }

   private JSONObject getColumns(long definitionId) {
      logger.info("Searching columns for definitionId :: " + definitionId);
      Map<Long, String> columnsMap = new HashMap();
      columnsMap.put(this.dmsCommonAPIConfiguation.BoardLevelMeetingObjectDefinitionId(), this.dmsCommonAPIConfiguation.BoardLevelMeetingObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.CDADObjectDefinitionId(), this.dmsCommonAPIConfiguation.CDADObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.HORDObjectDefinitionId(), this.dmsCommonAPIConfiguation.HORecruitmentDivObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditACBObjectDefinitionId(), this.dmsCommonAPIConfiguation.InsceptionAuditACBObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditACEObjectDefinitionId(), this.dmsCommonAPIConfiguation.InsceptionAuditACEObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditCAAppointmentObjectDefinitionId(), this.dmsCommonAPIConfiguation.InspectionAuditCAAppointObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditMiscObjectDefinitionId(), this.dmsCommonAPIConfiguation.InspectionAuditMiscObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditRepotsObjectDefinitionId(), this.dmsCommonAPIConfiguation.InspectionAuditReportsObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionAuditOthReportObjectDefinitionId(), this.dmsCommonAPIConfiguation.InspectionAuditReportsObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InspectionForeignAuditObjectDefinitionId(), this.dmsCommonAPIConfiguation.InspectionForeignAuditObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.HOSecurityDepartmentObjectDefinitionId(), this.dmsCommonAPIConfiguation.HOSecurityDepObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InternationalDepObjectDefinitionId(), this.dmsCommonAPIConfiguation.InternationalDepartmentObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.DBDepartmentObjectDefinitionId(), this.dmsCommonAPIConfiguation.DBDepartmentObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.DataWareHouseObjectDefinitionId(), this.dmsCommonAPIConfiguation.DataWarehouseObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.InternalLayoutMapObjectDefinitionId(), this.dmsCommonAPIConfiguation.InternalLayoutMapObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.ProjectManagementListObjectDefinitionId(), this.dmsCommonAPIConfiguation.ProjectManageListObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.BranchDigitationObjectDefinitionId(), this.dmsCommonAPIConfiguation.ProjectManageListObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.ElectricalWiringObjectDefinitionId(), this.dmsCommonAPIConfiguation.ElectricalWiringDiagramObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.CustomerAccountObjectDefinitionId(), this.dmsCommonAPIConfiguation.CustomerAccountObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.UPIInsuranceObjectDefinitionId(), this.dmsCommonAPIConfiguation.UPIInsuranceObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.CPDInsuranceObjectDefinitionId(), this.dmsCommonAPIConfiguation.CPDInsuranceObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.DBDMandateObjectDefinitionId(), this.dmsCommonAPIConfiguation.DBDMandateObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.FBDObjectDefinitionId(), this.dmsCommonAPIConfiguation.FBDObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.FEBONONFinancialObjectDefinitionId(), this.dmsCommonAPIConfiguation.FEBONONFinancialObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.FinancePloeObjectDefinitionId(), this.dmsCommonAPIConfiguation.FinancePloeObjectColumns());
      columnsMap.put(this.dmsCommonAPIConfiguation.FBDFRBOObjectDefinitionId(), this.dmsCommonAPIConfiguation.FBDFOBOObjectColumns());
      String jsonData = (String)columnsMap.get(definitionId);
      logger.info("Search Result empty :: " + Validator.isBlank(jsonData));
      if (Validator.isBlank(jsonData)) {
         return null;
      } else {
         try {
            return JSONFactoryUtil.createJSONObject(jsonData);
         } catch (JSONException e) {
            logger.error("Invalid JSON for ObjectDefinitionId :: " + definitionId, e);
            return null;
         }
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.dmsCommonAPIConfiguation = (DMSCommonAPIConfiguation)ConfigurableUtil.createConfigurable(DMSCommonAPIConfiguation.class, properties);
   }
}
