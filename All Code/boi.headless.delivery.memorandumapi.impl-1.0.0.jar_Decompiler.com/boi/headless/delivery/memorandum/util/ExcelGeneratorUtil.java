package boi.headless.delivery.memorandum.util;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.osgi.service.component.annotations.Reference;

public class ExcelGeneratorUtil {
   JSONObject jsonObject = null;
   @Reference
   private DLAppService _dlAppService;
   private SXSSFWorkbook workbook;
   private Sheet sheet;
   private static final Log logger = LogFactoryUtil.getLog(ExcelGeneratorUtil.class);

   public String createExcelForObjectEntry(long groupId, long companyId, DLFolder folder, long userId, ServiceContext serviceContext, Collection<ObjectEntry> entries, String fileName) {
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

   private void grantFolderPermissions(DLFolder folder) throws PortalException {
      long companyId = folder.getCompanyId();
      long roleId = RoleLocalServiceUtil.getRole(companyId, "User").getRoleId();
      Map<Long, String[]> rolePermissions = new HashMap();
      rolePermissions.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_DOCUMENT"});
      ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, DLFolder.class.getName(), 4, String.valueOf(folder.getFolderId()), rolePermissions);
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
}
