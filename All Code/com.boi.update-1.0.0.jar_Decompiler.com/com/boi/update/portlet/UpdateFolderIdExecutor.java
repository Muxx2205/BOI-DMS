package com.boi.update.portlet;

import com.boi.update.configuration.ConfigurationHelper;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusMessageTranslator;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"background.task.executor.class.name=com.boi.update.portlet.UpdateFolderIdExecutor"},
   service = {BackgroundTaskExecutor.class}
)
public class UpdateFolderIdExecutor extends BaseBackgroundTaskExecutor {
   private static final Log _log = LogFactoryUtil.getLog(UpdateFolderIdExecutor.class);

   public UpdateFolderIdExecutor() {
      this.setBackgroundTaskStatusMessageTranslator((BackgroundTaskStatusMessageTranslator)null);
   }

   public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      String base64Content = (String)taskContextMap.get("fileContent");
      byte[] fileContent = Base64.getDecoder().decode(base64Content);
      String fileName = (String)taskContextMap.get("fileName");
      ServiceContext serviceContext = (ServiceContext)taskContextMap.get("serviceContext");
      File file = null;
      boolean useTempFile = false;
      if (fileContent != null) {
         file = File.createTempFile("excel_upload_", "_" + fileName);
         Files.write(file.toPath(), fileContent, new OpenOption[0]);
         useTempFile = true;
         _log.info("------------------------------------------------------------");
         _log.info("Processing Excel file from byte array: " + fileName);
      } else {
         String filePath = (String)taskContextMap.get("filePath");
         file = new File(filePath);
         if (!file.exists()) {
            _log.info("File not found: " + filePath);
            return new BackgroundTaskResult(2);
         }
      }

      label276: {
         BackgroundTaskResult var50;
         try {
            InputStream inputStream;
            label280: {
               inputStream = new FileInputStream(file);

               try {
                  label281: {
                     Workbook workbook = new XSSFWorkbook(inputStream);

                     label268: {
                        try {
                           Sheet sheet = workbook.getSheetAt(0);
                           Iterator<Row> rowIterator = sheet.iterator();
                           if (!rowIterator.hasNext()) {
                              _log.info("Excel sheet is empty");
                              var50 = new BackgroundTaskResult(3);
                              break label268;
                           }

                           Row headerRow = (Row)rowIterator.next();
                           int lastColumn = headerRow.getLastCellNum();
                           String[] columnNames = new String[lastColumn];

                           for(int i = 0; i < lastColumn; ++i) {
                              columnNames[i] = headerRow.getCell(i) != null ? headerRow.getCell(i).toString().trim() : "Column" + i;
                           }

                           _log.info("Columns " + columnNames.toString());
                           Map<Long, Long> orgDeptFolderMap = new HashMap();
                           String DPTFOLDERNAME = ConfigurationHelper.getDepartmentName();

                           while(rowIterator.hasNext()) {
                              Row row = (Row)rowIterator.next();
                              long ORGANIZATIONID = this.parseLongSafe(this.getCellValue(row, columnNames, "ORGANIZATIONID"));
                              long DPTFOLDERID = this.parseLongSafe(this.getCellValue(row, columnNames, "DPTFOLDERID"));
                              if (ORGANIZATIONID != 0L) {
                                 orgDeptFolderMap.put(ORGANIZATIONID, DPTFOLDERID);
                              }
                           }

                           _log.info("orgDeptFolderMap : " + orgDeptFolderMap.size());
                           Map<String, String> templateNames = new HashMap();
                           templateNames.put("UPI Insurance", "UPI_Insurance");
                           templateNames.put("Finance Ploe", "Finance_Ploe");
                           templateNames.put("Digital Banking Department", "Digital_Banking_Department");
                           templateNames.put("DBD Mandate Cancellation", "DBD_Mandate_Cancellation");
                           templateNames.put("Account Opening", "Account_Opening");
                           templateNames.put("CPD Insurance", "CPD_Insurance_Claim");
                           templateNames.put("FBD", "FBD");
                           templateNames.put("FBD FE BO", "FBD_FE_BO");
                           templateNames.put("FE BO NON Financial", "FE_BO_NON_Financial");
                           String matchedTemplate = (String)templateNames.get(DPTFOLDERNAME.trim());
                           if (!Validator.isBlank(matchedTemplate) || Validator.isNotNull(matchedTemplate)) {
                              List<UserOrganizationRecord> recordsByOrganizationId = getRecordsByOrganizationId(0L, 287853L, matchedTemplate);
                              _log.info("recordsByOrganizationId : " + recordsByOrganizationId.size());
                              if (recordsByOrganizationId != null || recordsByOrganizationId.size() > 0) {
                                 for(UserOrganizationRecord organizationRecord : recordsByOrganizationId) {
                                    long orgId = organizationRecord.getOrganizationId();
                                    if (orgDeptFolderMap.containsKey(orgId) && organizationRecord.getDepartmentId().equalsIgnoreCase(matchedTemplate)) {
                                       long entryId = organizationRecord.getObjectEntryId();
                                       long newFolderId = (Long)orgDeptFolderMap.get(orgId);
                                       ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(entryId);
                                       Map<String, Serializable> values = objectEntry.getValues();
                                       values.put("folderId", newFolderId);
                                       ObjectEntry updatedEntry = ObjectEntryLocalServiceUtil.updateObjectEntry(objectEntry.getUserId(), entryId, values, serviceContext);
                                       _log.info("Updated .. " + updatedEntry.getObjectEntryId());
                                       _log.info(String.format("%s -> OrganizationId: %s |  NewFolderId: %s | OldFolderId: %s", matchedTemplate, orgId, newFolderId, organizationRecord.getFolderId()));
                                    }
                                 }
                              }
                           }
                        } catch (Throwable var45) {
                           try {
                              workbook.close();
                           } catch (Throwable var44) {
                              var45.addSuppressed(var44);
                           }

                           throw var45;
                        }

                        workbook.close();
                        break label281;
                     }

                     workbook.close();
                     break label280;
                  }
               } catch (Throwable var46) {
                  try {
                     inputStream.close();
                  } catch (Throwable var43) {
                     var46.addSuppressed(var43);
                  }

                  throw var46;
               }

               inputStream.close();
               break label276;
            }

            inputStream.close();
         } finally {
            if (useTempFile && file != null && file.exists()) {
               try {
                  file.delete();
                  _log.info("Temporary file cleaned up: " + file.getName());
               } catch (Exception e) {
                  _log.warn("Failed to delete temporary file: " + file.getName(), e);
               }
            }

         }

         return var50;
      }

      _log.info("BackgroundTaskResult Success");
      return new BackgroundTaskResult(3);
   }

   private long parseLongSafe(String value) {
      if (value != null && !value.trim().isEmpty()) {
         try {
            return (long)Double.parseDouble(value.trim());
         } catch (NumberFormatException e) {
            _log.info("value " + e.getMessage());
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public static List<UserOrganizationRecord> getRecordsByOrganizationId(long groupId, long objectDefinitionId, String departmentId) {
      List<UserOrganizationRecord> records = new ArrayList();
      int start = 0;
      int batchSize = 5000;

      while(true) {
         List<ObjectEntry> entries = ObjectEntryLocalServiceUtil.getObjectEntries(groupId, objectDefinitionId, start, start + batchSize);
         if (entries.isEmpty()) {
            return records;
         }

         for(ObjectEntry entry : entries) {
            Map<String, Serializable> values = entry.getValues();
            if (values != null && values.get("departmentId") != null) {
               String dID = ((Serializable)values.get("departmentId")).toString();
               if (departmentId.equalsIgnoreCase(dID)) {
                  String screenName = values.get("userScreenName") != null ? ((Serializable)values.get("userScreenName")).toString() : "";
                  String deId = values.get("departmentId") != null ? ((Serializable)values.get("departmentId")).toString() : "";
                  long folderId = values.get("folderId") != null ? Long.parseLong(((Serializable)values.get("folderId")).toString()) : 0L;
                  long organizationId = values.get("organizationId") != null ? Long.parseLong(((Serializable)values.get("organizationId")).toString()) : 0L;
                  records.add(new UserOrganizationRecord(entry.getObjectEntryId(), folderId, deId, organizationId, screenName));
               }
            }
         }

         start += batchSize;
      }
   }

   private String getCellValue(Row row, String[] columnNames, String columnName) {
      for(int i = 0; i < columnNames.length; ++i) {
         if (columnNames[i].equalsIgnoreCase(columnName)) {
            return row.getCell(i) != null ? row.getCell(i).toString().trim() : "";
         }
      }

      return "";
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return new UpdateFolderIdExecutor();
   }

   public static class UserOrganizationRecord {
      private long folderId;
      private long objectEntryId;
      private String departmentId;
      private long organizationId;
      private String userScreenName;

      public UserOrganizationRecord(long objectEntryId, long folderId, String departmentId, long organizationId, String userScreenName) {
         this.folderId = folderId;
         this.departmentId = departmentId;
         this.organizationId = organizationId;
         this.userScreenName = userScreenName;
         this.objectEntryId = objectEntryId;
      }

      public long getFolderId() {
         return this.folderId;
      }

      public String getDepartmentId() {
         return this.departmentId;
      }

      public long getOrganizationId() {
         return this.organizationId;
      }

      public String getUserScreenName() {
         return this.userScreenName;
      }

      public long getObjectEntryId() {
         return this.objectEntryId;
      }

      public String toString() {
         return "UserOrganizationRecord [folderId=" + this.folderId + ", objectEntryId=" + this.objectEntryId + ", departmentId=" + this.departmentId + ", organizationId=" + this.organizationId + ", userScreenName=" + this.userScreenName + "]";
      }
   }
}
