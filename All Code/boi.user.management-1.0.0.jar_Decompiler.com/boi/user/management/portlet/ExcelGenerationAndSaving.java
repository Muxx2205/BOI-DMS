package boi.user.management.portlet;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelGenerationAndSaving {
   private static final Log _log = LogFactoryUtil.getLog(ExcelGenerationAndSaving.class);
   private static final String[] COLUMNS = new String[]{"ADID", "USER-CREATION", "ORGANIZATION-MAPPING", "ORGANIZATION-ROLE-MAPPING", "TEMPLATE-ROLE-MAPPING", "CUSTOM-ORG-MAPPING", "ERROR"};

   public static void createAndSaveExcelFile(List<Map<String, Object>> dataList, String baseFileName, long folderId, long groupId, long userId, ServiceContext serviceContext) throws PortalException, IOException {
      Workbook workbook = new XSSFWorkbook();

      try {
         Sheet sheet = workbook.createSheet("User Management Data");
         createHeaderRow(workbook, sheet);
         createDataRows(workbook, sheet, dataList);

         for(int i = 0; i < COLUMNS.length; ++i) {
            sheet.autoSizeColumn(i);
         }

         String timestamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
         String fileName = baseFileName + "_" + timestamp + ".xlsx";
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         byte[] excelBytes;
         try {
            workbook.write(baos);
            excelBytes = baos.toByteArray();
         } catch (Throwable var19) {
            try {
               baos.close();
            } catch (Throwable var18) {
               var19.addSuppressed(var18);
            }

            throw var19;
         }

         baos.close();
         saveToDocumentsAndMedia(folderId, excelBytes, fileName, serviceContext);
      } catch (Throwable var20) {
         try {
            workbook.close();
         } catch (Throwable var17) {
            var20.addSuppressed(var17);
         }

         throw var20;
      }

      workbook.close();
   }

   private static void createHeaderRow(Workbook workbook, Sheet sheet) {
      Row headerRow = sheet.createRow(0);
      CellStyle headerStyle = createHeaderStyle(workbook);

      for(int i = 0; i < COLUMNS.length; ++i) {
         Cell cell = headerRow.createCell(i);
         cell.setCellValue(COLUMNS[i]);
         cell.setCellStyle(headerStyle);
      }

   }

   private static CellStyle createHeaderStyle(Workbook workbook) {
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setColor(IndexedColors.WHITE.getIndex());
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);
      headerStyle.setBorderBottom(BorderStyle.THIN);
      headerStyle.setBorderTop(BorderStyle.THIN);
      headerStyle.setBorderLeft(BorderStyle.THIN);
      headerStyle.setBorderRight(BorderStyle.THIN);
      return headerStyle;
   }

   private static void createDataRows(Workbook workbook, Sheet sheet, List<Map<String, Object>> dataList) {
      CellStyle dataStyle = createDataStyle(workbook);
      CellStyle errorStyle = createErrorStyle(workbook);
      int rowNum = 1;

      for(Map<String, Object> data : dataList) {
         Row row = sheet.createRow(rowNum++);

         for(int i = 0; i < COLUMNS.length; ++i) {
            String columnName = COLUMNS[i];
            Cell cell = row.createCell(i);
            Object value = data.get(columnName);
            if (value != null) {
               if (value instanceof List) {
                  cell.setCellValue(formatListAsString((List)value));
               } else {
                  cell.setCellValue(value.toString());
               }
            } else {
               cell.setCellValue("");
            }

            if ("ERROR".equals(columnName) && value != null && !value.toString().isEmpty()) {
               cell.setCellStyle(errorStyle);
            } else {
               cell.setCellStyle(dataStyle);
            }
         }
      }

   }

   private static CellStyle createDataStyle(Workbook workbook) {
      CellStyle dataStyle = workbook.createCellStyle();
      dataStyle.setBorderBottom(BorderStyle.THIN);
      dataStyle.setBorderTop(BorderStyle.THIN);
      dataStyle.setBorderLeft(BorderStyle.THIN);
      dataStyle.setBorderRight(BorderStyle.THIN);
      dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
      dataStyle.setWrapText(true);
      return dataStyle;
   }

   private static CellStyle createErrorStyle(Workbook workbook) {
      CellStyle errorStyle = workbook.createCellStyle();
      Font errorFont = workbook.createFont();
      errorFont.setColor(IndexedColors.RED.getIndex());
      errorStyle.setFont(errorFont);
      errorStyle.setBorderBottom(BorderStyle.THIN);
      errorStyle.setBorderTop(BorderStyle.THIN);
      errorStyle.setBorderLeft(BorderStyle.THIN);
      errorStyle.setBorderRight(BorderStyle.THIN);
      errorStyle.setVerticalAlignment(VerticalAlignment.TOP);
      errorStyle.setWrapText(true);
      return errorStyle;
   }

   private static String formatListAsString(List<?> list) {
      if (list != null && !list.isEmpty()) {
         StringBuilder sb = new StringBuilder();

         for(Object item : list) {
            if (item != null) {
               sb.append("• ").append(item.toString()).append("\n");
            }
         }

         return sb.toString().trim();
      } else {
         return "";
      }
   }

   private static FileEntry saveToDocumentsAndMedia(long folderId, byte[] fileContent, String fileName, ServiceContext context) {
      File tempFile = null;

      try {
         _log.info("=========== [saveToDocumentsAndMedia] START =============");
         DLFolder folder = DLFolderLocalServiceUtil.getFolder(folderId);
         tempFile = File.createTempFile("ABHI", "_" + fileName);
         Files.write(tempFile.toPath(), fileContent, new OpenOption[0]);
         _log.info("Temporary file created at: " + tempFile.getAbsolutePath());
         _log.info("=== [saveToDocumentsAndMedia] SUCCESS ===");
      } catch (Exception e) {
         _log.error("Error in saveToDocumentsAndMedia(): " + e.getMessage(), e);
      } finally {
         _log.info("=== [saveToDocumentsAndMedia] END ===");
      }

      return null;
   }

   public static Map<String, Object> createUserDataMap(String adid, String userCreationStatus, String organizationMappingStatus, String organizationRoleMappingStatus, String templateRoleMappingStatus, String customOrgMappingStatus, List<String> errors) {
      Map<String, Object> userData = new HashMap();
      userData.put("ADID", adid);
      userData.put("USER-CREATION", userCreationStatus);
      userData.put("ORGANIZATION-MAPPING", organizationMappingStatus);
      userData.put("ORGANIZATION-ROLE-MAPPING", organizationRoleMappingStatus);
      userData.put("TEMPLATE-ROLE-MAPPING", templateRoleMappingStatus);
      userData.put("CUSTOM-ORG-MAPPING", customOrgMappingStatus);
      userData.put("ERROR", errors);
      return userData;
   }
}
