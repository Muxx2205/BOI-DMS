package boi.headless.delivery.memorandum.background.task;

import boi.headless.delivery.memorandum.configuration.PSRSConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.UriInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"boi.headless.delivery.memorandum.configuration.PSRSConfiguration"},
   property = {"background.task.executor.class.name=boi.headless.delivery.memorandum.background.task.PDFReportGeneratorEngine"},
   service = {BackgroundTaskExecutor.class}
)
public class PDFReportGeneratorEngine extends BaseBackgroundTaskExecutor {
   private static final Log _log = LogFactoryUtil.getLog(PDFReportGeneratorEngine.class);
   private static final String USER_ROLE_NAME = "User";
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
   private volatile PSRSConfiguration psrsConfiguration;
   private Workbook workbook;
   private Sheet sheet;
   @Reference
   private DLAppService _dlAppService;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.psrsConfiguration = (PSRSConfiguration)ConfigurableUtil.createConfigurable(PSRSConfiguration.class, properties);
   }

   public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      if (taskContextMap != null && !taskContextMap.isEmpty()) {
         long userId = GetterUtil.getLong(taskContextMap.get("UserId"), 0L);
         User user = UserLocalServiceUtil.getUser(userId);
         if (user == null) {
            _log.error("User not found in task context");
            return BackgroundTaskResult.SUCCESS;
         } else {
            String filterQuery = GetterUtil.getString(taskContextMap.get("Query"), "");
            String searchQuery = GetterUtil.getString(taskContextMap.get("Search"));
            String reportName = GetterUtil.getString(taskContextMap.get("ReportName"), "default");
            String fromDate = GetterUtil.getString(taskContextMap.get("pdfFromDate"));
            String toDate = GetterUtil.getString(taskContextMap.get("pdfToDate"));
            ServiceContext serviceContext = (ServiceContext)taskContextMap.get("serviceContext");
            String branchName = GetterUtil.getString(taskContextMap.get("branchName"), "");
            String branchCode = GetterUtil.getString(taskContextMap.get("branchCode"), "");

            try {
               ReportType reportType = PDFReportGeneratorEngine.ReportType.valueOf(reportName);
               Map<String, String> headers = this.getHeaders(reportType);
               if (headers == null || headers.isEmpty()) {
                  _log.error("No headers configured for report: " + reportName);
                  return BackgroundTaskResult.SUCCESS;
               }

               long objectDefId = this.getObjectDefinitionId(reportType);
               if (objectDefId <= 0L) {
                  _log.error("Invalid object definition ID for report: " + reportName);
                  return BackgroundTaskResult.SUCCESS;
               }

               ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.fetchObjectDefinition(objectDefId);
               if (objectDefinition == null) {
                  _log.error("Object definition not found for ID: " + objectDefId);
                  return BackgroundTaskResult.SUCCESS;
               }

               List<ObjectEntry> entries = this.getEntries(objectDefinition, user, filterQuery, searchQuery);
               if (entries == null || entries.isEmpty()) {
                  _log.warn("No entries found for report: " + reportName);
                  entries = new ArrayList();
               }

               byte[] excelBytes = this.generateExcel(reportType, entries, headers, branchName, branchCode, fromDate, toDate);
               if (excelBytes != null && excelBytes.length > 0) {
                  _log.info("Excel generated successfully for report: " + reportName + " with " + entries.size() + " entries. Excel size: " + excelBytes.length + " bytes");
                  long psrsDefaultFolderId = this.psrsConfiguration.PSRSFolderId();
                  if (psrsDefaultFolderId > 0L && serviceContext != null) {
                     String url = this.saveReportToDocumentLibrary(serviceContext.getScopeGroupId(), user.getCompanyId(), psrsDefaultFolderId, user.getUserId(), serviceContext, excelBytes, reportName);
                     if (url != null) {
                        _log.info("Report saved to document library: " + url);
                        taskContextMap.put("fileURL", url);
                     }
                  }
               } else {
                  _log.error("Failed to generate Excel for report: " + reportName);
                  taskContextMap.put("fileURL", "Failed to generate Excel for report");
               }
            } catch (IllegalArgumentException e) {
               _log.error("Invalid report name: " + reportName, e);
            } catch (Exception e) {
               _log.error("Error generating Excel report: " + e.getMessage(), e);
            }

            return BackgroundTaskResult.SUCCESS;
         }
      } else {
         _log.error("Task context map is null or empty");
         return BackgroundTaskResult.SUCCESS;
      }
   }

   private Map<String, String> getHeaders(ReportType type) throws JsonProcessingException {
      String jsonString = this.getConfigJsonForType(type);
      if (Validator.isNull(jsonString)) {
         _log.error("No column configuration found for report type: " + type);
         return new LinkedHashMap();
      } else {
         ObjectMapper objectMapper = new ObjectMapper();
         return (Map)objectMapper.readValue(jsonString, new TypeReference<LinkedHashMap<String, String>>() {
         });
      }
   }

   private String getConfigJsonForType(ReportType type) {
      switch (type) {
         case m1:
            return this.psrsConfiguration.M1ObjectColumns();
         case m2:
            return this.psrsConfiguration.M2ObjectColumns();
         case m3:
            return this.psrsConfiguration.M3ObjectColumns();
         case m4:
            return this.psrsConfiguration.M4ObjectColumns();
         case m5:
            return this.psrsConfiguration.M5ObjectColumns();
         case m6:
            return this.psrsConfiguration.M6ObjectColumns();
         case m7:
            return this.psrsConfiguration.M7ObjectColumns();
         case m8:
            return this.psrsConfiguration.M8ObjectColumns();
         default:
            return "";
      }
   }

   private long getObjectDefinitionId(ReportType type) {
      switch (type) {
         case m1:
            return this.psrsConfiguration.M1Id();
         case m2:
            return this.psrsConfiguration.M2Id();
         case m3:
            return this.psrsConfiguration.M3Id();
         case m4:
            return this.psrsConfiguration.M4Id();
         case m5:
            return this.psrsConfiguration.M5Id();
         case m6:
            return this.psrsConfiguration.M6Id();
         case m7:
            return this.psrsConfiguration.M7Id();
         case m8:
            return this.psrsConfiguration.M8Id();
         default:
            return 0L;
      }
   }

   private byte[] generateExcel(ReportType type, List<ObjectEntry> dataList, Map<String, String> headers, String branchName, String branchCode, String fromDate, String toDate) throws Exception {
      this.workbook = new SXSSFWorkbook(50000);
      this.sheet = this.workbook.createSheet("PSRS Report");
      _log.info("Creating Excel header");
      this.createHeaderSection(type, branchName, branchCode, fromDate, toDate);
      this.createDataTable(headers, dataList);
      byte[] outArray = null;

      try {
         ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();

         try {
            this.workbook.write(outByteStream);
            outArray = outByteStream.toByteArray();
            this.workbook.close();
         } catch (Throwable var13) {
            try {
               outByteStream.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }

            throw var13;
         }

         outByteStream.close();
         return outArray;
      } catch (Exception e) {
         _log.error("Error generating Excel: " + e.getMessage(), e);
         throw e;
      }
   }

   private void createHeaderSection(ReportType type, String branchName, String branchCode, String fromDate, String toDate) {
      int rowNum = 0;
      Row titleRow = this.sheet.createRow(rowNum++);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("BANK OF INDIA");
      CellStyle titleStyle = this.workbook.createCellStyle();
      Font titleFont = this.workbook.createFont();
      titleFont.setBold(true);
      titleFont.setFontHeightInPoints((short)16);
      titleStyle.setFont(titleFont);
      titleStyle.setAlignment(HorizontalAlignment.CENTER);
      titleCell.setCellStyle(titleStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
      Row branchRow = this.sheet.createRow(rowNum++);
      Cell branchCell = branchRow.createCell(0);
      String var33 = this.getSafeString(branchName);
      branchCell.setCellValue("BRANCH: " + var33);
      CellStyle normalStyle = this.workbook.createCellStyle();
      normalStyle.setAlignment(HorizontalAlignment.CENTER);
      branchCell.setCellStyle(normalStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));
      Row branchCodeRow = this.sheet.createRow(rowNum++);
      Cell branchCodeCell = branchCodeRow.createCell(0);
      var33 = this.getSafeString(branchCode);
      branchCodeCell.setCellValue("BRANCH CODE: " + var33);
      branchCodeCell.setCellStyle(normalStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
      Row generatedRow = this.sheet.createRow(rowNum++);
      Cell generatedCell = generatedRow.createCell(0);
      LocalDate var37 = LocalDate.now();
      generatedCell.setCellValue("Generated On: " + var37.format(DATE_FORMATTER));
      generatedCell.setCellStyle(normalStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));
      Row periodRow = this.sheet.createRow(rowNum++);
      Cell periodCell = periodRow.createCell(0);
      String var39 = this.getSafeString(fromDate);
      periodCell.setCellValue("Report Period: " + var39 + " TO " + this.getSafeString(toDate));
      periodCell.setCellStyle(normalStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));
      this.sheet.createRow(rowNum++);
      Row reportTypeRow = this.sheet.createRow(rowNum++);
      Cell reportTypeCell = reportTypeRow.createCell(0);
      reportTypeCell.setCellValue("PSRS REPORT - " + type.name());
      CellStyle reportTypeStyle = this.workbook.createCellStyle();
      Font reportTypeFont = this.workbook.createFont();
      reportTypeFont.setBold(true);
      reportTypeFont.setFontHeightInPoints((short)14);
      reportTypeStyle.setFont(reportTypeFont);
      reportTypeStyle.setAlignment(HorizontalAlignment.CENTER);
      reportTypeCell.setCellStyle(reportTypeStyle);
      this.sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 5));
      this.sheet.createRow(rowNum++);
   }

   private void createDataTable(Map<String, String> headers, List<ObjectEntry> dataList) {
      int startRow = 8;
      int cellNum = 0;
      Row headerRow = this.sheet.createRow(startRow);
      CellStyle headerStyle = this.workbook.createCellStyle();
      Font headerFont = this.workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);
      headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
      headerStyle.setWrapText(true);
      Cell snoHeader = headerRow.createCell(cellNum++);
      snoHeader.setCellValue("S.No");
      snoHeader.setCellStyle(headerStyle);

      for(String header : headers.values()) {
         Cell headerCell = headerRow.createCell(cellNum++);
         headerCell.setCellValue(header);
         headerCell.setCellStyle(headerStyle);
         this.sheet.setColumnWidth(cellNum - 1, 4000);
      }

      if (dataList != null && !dataList.isEmpty()) {
         Font dataFont = this.workbook.createFont();
         dataFont.setFontHeightInPoints((short)9);
         CellStyle dataStyle = this.workbook.createCellStyle();
         dataStyle.setFont(dataFont);
         dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
         dataStyle.setWrapText(true);
         int rowNum = startRow + 1;
         int serialNo = 1;

         for(ObjectEntry objectEntry : dataList) {
            Row dataRow = this.sheet.createRow(rowNum++);
            cellNum = 0;
            Map<String, Object> properties = objectEntry.getProperties();
            if (properties != null) {
               Cell snoCell = dataRow.createCell(cellNum++);
               snoCell.setCellValue((double)(serialNo++));
               snoCell.setCellStyle(dataStyle);

               for(String fieldName : headers.keySet()) {
                  Cell dataCell = dataRow.createCell(cellNum++);
                  Object value = properties.get(fieldName);
                  dataCell.setCellValue(this.formatCellValue(value));
                  dataCell.setCellStyle(dataStyle);
               }
            }
         }
      } else {
         Row noDataRow = this.sheet.createRow(startRow + 1);
         Cell noDataCell = noDataRow.createCell(0);
         noDataCell.setCellValue("No records found for the selected period");
         CellStyle noDataStyle = this.workbook.createCellStyle();
         noDataStyle.setAlignment(HorizontalAlignment.CENTER);
         noDataCell.setCellStyle(noDataStyle);
         this.sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, 0, headers.size()));
      }

   }

   private String formatCellValue(Object value) {
      if (value == null) {
         return "";
      } else if (value instanceof Date) {
         SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
         return sdf.format((Date)value);
      } else {
         return value instanceof Number ? String.format("%,.2f", ((Number)value).doubleValue()) : value.toString();
      }
   }

   private String getSafeString(String value) {
      return value != null && !value.isEmpty() ? value : "N/A";
   }

   private List<ObjectEntry> getEntries(ObjectDefinition objectDefinition, User user, String filter, String search) {
      List<ObjectEntry> result = new ArrayList();
      if (objectDefinition != null && user != null) {
         _log.info("Fetching entries - ObjectDefinitionId: " + objectDefinition.getObjectDefinitionId() + ", Filter: " + filter + ", Search: " + search);

         try {
            DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, objectDefinition.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
            ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
            if (objectEntryManager == null) {
               _log.error("No ObjectEntryManager found for storage type: " + objectDefinition.getStorageType());
               return result;
            }

            Sort sort = SortFactoryUtil.create("createDate", 3, false);
            Sort[] sorts = new Sort[]{sort};
            Page<ObjectEntry> page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, (String)null, (Aggregation)null, dtoConverterContext, filter, Pagination.of(1, 10000), search, sorts);
            if (page != null) {
               Collection<ObjectEntry> items = page.getItems();
               if (items != null) {
                  result.addAll(items);
               }

               _log.info("Successfully fetched " + result.size() + " entries");
            }
         } catch (Exception e) {
            _log.error("Error occurred in getEntries()", e);
         }

         return result;
      } else {
         _log.error("ObjectDefinition or User is null");
         return result;
      }
   }

   private String saveReportToDocumentLibrary(long groupId, long companyId, long folderId, long userId, ServiceContext serviceContext, byte[] excelBytes, String reportName) {
      if (excelBytes != null && excelBytes.length != 0) {
         File tempFile = null;

         FileVersion fileVersion;
         try {
            DLFolder dlFolder = DLFolderLocalServiceUtil.getFolder(folderId);
            DLFolder psrsFolder = this.createORGetFolder(dlFolder.getRepositoryId(), dlFolder.getFolderId(), dlFolder.getGroupId(), "PSRS Exported File");
            _log.info("psrsFolder = " + psrsFolder != null);
            DLFolder moduleFolder = this.createORGetFolder(psrsFolder.getRepositoryId(), psrsFolder.getFolderId(), psrsFolder.getGroupId(), reportName);
            _log.info("moduleFolder = " + moduleFolder != null);
            String fileName = this.generateFileName(reportName);
            tempFile = File.createTempFile("psrs_", ".xlsx");
            FileOutputStream fos = new FileOutputStream(tempFile);

            try {
               fos.write(excelBytes);
               fos.flush();
            } catch (Throwable var27) {
               try {
                  fos.close();
               } catch (Throwable var26) {
                  var27.addSuppressed(var26);
               }

               throw var27;
            }

            fos.close();
            FileEntry fileEntry = this._dlAppService.addFileEntry(dlFolder.getRepositoryId(), dlFolder.getFolderId(), fileName, MimeTypesUtil.getContentType(fileName), fileName, "PSRS Report - " + reportName, "Generated on " + LocalDate.now().format(DATE_FORMATTER), tempFile, serviceContext);
            if (fileEntry != null) {
               this.grantViewPermissions(companyId, fileEntry);
               fileVersion = fileEntry.getFileVersion();
               String url = DLURLHelperUtil.getDownloadURL(fileEntry, fileVersion, (ThemeDisplay)null, "");
               _log.info("Generated download URL: " + url);
               String var20 = url;
               return var20;
            }

            _log.error("Failed to create file entry");
            fileVersion = null;
         } catch (Exception ex) {
            _log.error("Error saving Excel to document library: " + ex.getMessage(), ex);
            DLFolder psrsFolder = null;
            return psrsFolder;
         } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
               _log.warn("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }

         }

         return fileVersion;
      } else {
         _log.error("Cannot save empty Excel to document library");
         return null;
      }
   }

   private void grantViewPermissions(long companyId, FileEntry fileEntry) {
      if (fileEntry != null) {
         try {
            Role userRole = RoleLocalServiceUtil.getRole(companyId, "User");
            if (userRole == null) {
               return;
            }

            long roleId = userRole.getRoleId();
            String fileEntryIdStr = Long.toString(fileEntry.getFileEntryId());
            boolean hasPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(companyId, DLFileEntry.class.getName(), 4, fileEntryIdStr, roleId, "VIEW");
            if (!hasPermission) {
               Map<Long, String[]> rolePermissions = new HashMap();
               rolePermissions.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
               ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, DLFileEntry.class.getName(), 4, fileEntryIdStr, rolePermissions);
               _log.info("Set view/download permissions for User role on file entry: " + fileEntryIdStr);
            }
         } catch (PortalException e) {
            _log.error("Failed to grant view permissions: " + e.getMessage(), e);
         }

      }
   }

   private DLFolder createORGetFolder(long repositoryId, long folderId, long groupId, String folderName) {
      if (Validator.isBlank(folderName)) {
         return null;
      } else {
         DLFolder folder = DLFolderLocalServiceUtil.fetchFolder(repositoryId, folderId, folderName);
         if (folder != null) {
            return folder;
         } else {
            try {
               ServiceContext context = ServiceContextThreadLocal.getServiceContext();
               context.setScopeGroupId(groupId);
               DLFolder newFolder = DLFolderLocalServiceUtil.addFolder((String)null, context.getUserId(), groupId, repositoryId, false, folderId, folderName, "", false, context);
               this.getPermission(newFolder);
               _log.info("Folder created: " + folderName);
               return newFolder;
            } catch (Exception e) {
               _log.error("Failed to create folder: " + folderName, e);
               return null;
            }
         }
      }
   }

   private boolean getPermission(DLFolder folder) {
      long roleId = this.getUserRole(folder);
      HashMap<Long, String[]> rolesForFolder = new HashMap();
      rolesForFolder.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_SUBFOLDER", "ADD_DOCUMENT"});

      try {
         _log.info("checking permission " + roleId);
         boolean isTrue = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), roleId, "ADD_DOCUMENT");
         if (!isTrue) {
            return this.addPermission(folder, roleId, rolesForFolder);
         }
      } catch (PortalException e) {
         e.printStackTrace();
      }

      return false;
   }

   private long getUserRole(DLFolder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
      } catch (PortalException e) {
         e.printStackTrace();
         return 0L;
      }
   }

   private boolean addPermission(DLFolder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {
      try {
         _log.info("Adding permission " + roleId);
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), rolesForFolder);
         return true;
      } catch (PortalException e) {
         e.printStackTrace();
         return false;
      }
   }

   private String generateFileName(String reportName) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
      String dateTime = sdf.format(new Date());
      return "PSRS_Report_" + reportName + "_" + dateTime + ".xlsx";
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return this;
   }

   private static enum ReportType {
      m1,
      m2,
      m3,
      m4,
      m5,
      m6,
      m7,
      m8;
   }
}
