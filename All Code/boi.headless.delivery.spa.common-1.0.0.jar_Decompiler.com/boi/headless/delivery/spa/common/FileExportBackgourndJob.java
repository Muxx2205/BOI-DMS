package boi.headless.delivery.spa.common;

import com.boi.headless.rest.common.spa.application.BOISPAConfiguation;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
   configurationPid = {"com.boi.headless.rest.common.spa.application.BOISPAConfiguation"},
   property = {"background.task.executor.class.name=boi.headless.delivery.spa.common.FileExportBackgourndJob"},
   service = {BackgroundTaskExecutor.class}
)
public class FileExportBackgourndJob extends BaseBackgroundTaskExecutor {
   JSONObject jsonObject = null;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private DLAppService _dlAppService;
   private SXSSFWorkbook workbook;
   private Sheet sheet;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   private static final Log logger = LogFactoryUtil.getLog(FileExportBackgourndJob.class);

   public BackgroundTaskResult execute(com.liferay.portal.kernel.backgroundtask.BackgroundTask backgroundTask) throws Exception {
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      long companyId = GetterUtil.getLong(taskContextMap.get("companyId"), 0L);
      long userid = GetterUtil.getLong(taskContextMap.get("userId"), 0L);
      String screenName = GetterUtil.getString(taskContextMap.get("screenName"), "default");
      long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"), 0L);
      long folderId = GetterUtil.getLong(taskContextMap.get("folderId"), 0L);
      String statusQuery = GetterUtil.getString(taskContextMap.get("statusQuery"), "Stattatatta");
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
      User user = UserLocalServiceUtil.getUser(userid);
      Collection<ObjectEntry> entries = this.getEntries(objectDefinition, user, statusQuery);
      if (Validator.isNotNull(entries)) {
         this.deleteOldJob(userid, user.getGroupId());
         this.jsonObject = this.getColumns(objectDefinitionId);
         ServiceContext serviceContext = (ServiceContext)taskContextMap.get("serviceContext");
         String url = this.createExcelForObjectEntry(user.getGroupId(), companyId, folderId, userid, serviceContext, entries);
         taskContextMap.put("fileURL", url);
      }

      logger.info("companyId " + companyId);
      logger.info("userid " + userid);
      logger.info("screenName " + screenName);
      logger.info("objectDefinitionId " + objectDefinitionId);
      logger.info("folderId " + folderId);
      logger.info("statusQuery " + statusQuery);
      return BackgroundTaskResult.SUCCESS;
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(com.liferay.portal.kernel.backgroundtask.BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return this;
   }

   private void deleteOldJob(long userid, long groupId) {
      try {
         DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
         dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", userid));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", groupId));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", true));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("servletContextNames", "FileExportBackgourndJob"));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("name", "com.boi.headless.rest.common.application.FileExportBackgourndJob"));
         List<BackgroundTask> backgroundTasks = BackgroundTaskLocalServiceUtil.dynamicQuery(dynamicQuery);
         logger.info("Old data ***************** " + backgroundTasks.size());

         for(BackgroundTask backgroundTask : backgroundTasks) {
            try {
               BackgroundTaskLocalServiceUtil.deleteBackgroundTask(backgroundTask.getBackgroundTaskId());
            } catch (PortalException var10) {
               logger.error("Export Entry not able to delete " + backgroundTask.getBackgroundTaskId());
            }
         }
      } catch (Exception e) {
         logger.info("Exception " + e.getMessage());
      }

   }

   private Collection<ObjectEntry> getEntries(ObjectDefinition objectDefinition, User user, String filter) {
      try {
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, objectDefinition.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         Page<ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, dtoConverterContext, filter, Pagination.of(-1, -1), (String)null, (Sort[])null);
         return page.getItems();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   private FileEntry getfileEntry(long groupId, long folderId) {
      try {
         return DLAppLocalServiceUtil.getFileEntryByFileName(groupId, folderId, "Report-1.xlsx");
      } catch (Exception var6) {
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

   private String createExcelForObjectEntry(long groupId, long companyId, long folderId, long userId, ServiceContext serviceContext, Collection<ObjectEntry> entries) {
      try {
         byte[] bytes = this.createDownloadExcel(entries);
         DLFolder dlFolder = DLFolderLocalServiceUtil.getDLFolder(folderId);
         this.deleteExistingFileIfExists(groupId, folderId);
         File file = this.writeBytesToTempFile(bytes, "Report-1", ".xlsx");
         FileEntry fileEntry = this._dlAppService.addFileEntry(dlFolder.getRepositoryId(), dlFolder.getFolderId(), "Report-1.xlsx", MimeTypesUtil.getContentType("Report-1.xlsx"), "Report-1.xlsx", " ", " ", file, serviceContext);
         this.grantGuestViewPermissionsIfNeeded(companyId, fileEntry);
         FileVersion fileVersion = fileEntry.getFileVersion();
         String url = DLURLHelperUtil.getDownloadURL(fileEntry, fileVersion, (ThemeDisplay)null, "");
         logger.info("Generated download URL: {}" + url);
         return url;
      } catch (Exception ex) {
         Log var10000 = logger;
         String var10001 = ex.getMessage();
         var10000.error("Error generating Excel and creating file entry: {}" + var10001 + ex);
         return "";
      }
   }

   private void deleteExistingFileIfExists(long groupId, long folderId) throws PortalException {
      FileEntry existingFile = this.getfileEntry(groupId, folderId);
      if (Validator.isNotNull(existingFile)) {
         logger.info("Deleting existing file from folderId: {}" + folderId);
         DLAppLocalServiceUtil.deleteFileEntry(existingFile.getFileEntryId());
      }

   }

   private void grantGuestViewPermissionsIfNeeded(long companyId, FileEntry fileEntry) throws PortalException {
      long roleId = RoleLocalServiceUtil.getRole(companyId, "Guest").getRoleId();
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

   private JSONObject getColumns(long definitionId) {
      String jsonData = "";
      logger.info("Searching columns.." + definitionId);
      if (definitionId == this.boiSpaConfiguation.BoardLevelMeetingObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.BoardLevelMeetingObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.CDADObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.CDADObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.HORDObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.HORecruitmentDivObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditOthReportObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InspectionAuditReportsObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditACBObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InsceptionAuditACBObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InternalLayoutMapObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InternalLayoutMapObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InternationalDepObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InternationalDepartmentObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.ProjectManagementListObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.ProjectManageListObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionForeignAuditObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InspectionForeignAuditObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.DataWareHouseObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.DataWarehouseObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.ElectricalWiringObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.ElectricalWiringDiagramObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.HOSecurityDepartmentObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.HOSecurityDepObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditACEObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InsceptionAuditACEObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditCAAppointmentObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InspectionAuditCAAppointObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditMiscObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InspectionAuditMiscObjectColumns();
      }

      if (definitionId == this.boiSpaConfiguation.InspectionAuditRepotsObjectDefinitionId()) {
         jsonData = this.boiSpaConfiguation.InspectionAuditReportsObjectColumns();
      }

      logger.info("Search Result " + Validator.isBlank(jsonData));
      if (!Validator.isBlank(jsonData)) {
         try {
            return JSONFactoryUtil.createJSONObject(jsonData);
         } catch (JSONException e) {
            e.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
   }
}
