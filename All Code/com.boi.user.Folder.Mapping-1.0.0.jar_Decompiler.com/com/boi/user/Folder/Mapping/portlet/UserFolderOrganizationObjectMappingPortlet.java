package com.boi.user.Folder.Mapping.portlet;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=UserFolderOrganizationObjectMapping", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_boi_user_Folder_Mapping_UserFolderOrganizationObjectMappingPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class UserFolderOrganizationObjectMappingPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(UserFolderOrganizationObjectMappingPortlet.class);

   public void uploadExcel(ActionRequest actionRequest, ActionResponse actionResponse) {
      int successCount = 0;
      int errorCount = 0;
      List<String> errorMessages = new ArrayList();

      try {
         _log.info("=== Starting Excel upload process ===");
         UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);
         File file = uploadPortletRequest.getFile("excelFile");
         String fileName = uploadPortletRequest.getFileName("excelFile");
         _log.info("Uploaded file: " + fileName);
         if (file == null || file.length() == 0L) {
            _log.error("File is missing or empty.");
            actionRequest.setAttribute("errorMessage", "Please select a valid Excel file.");
         } else {
            InputStream inputStream = new FileInputStream(file);

            label144: {
               try {
                  Workbook workbook = new XSSFWorkbook(inputStream);

                  label116: {
                     try {
                        _log.info("Workbook loaded successfully.");
                        Sheet sheet = workbook.getSheetAt(0);
                        Iterator<Row> rowIterator = sheet.iterator();
                        if (rowIterator.hasNext()) {
                           Row headerRow = (Row)rowIterator.next();
                           int lastColumn = headerRow.getLastCellNum();
                           String[] columnNames = new String[lastColumn];

                           for(int i = 0; i < lastColumn; ++i) {
                              columnNames[i] = headerRow.getCell(i) != null ? headerRow.getCell(i).toString().trim() : "Column" + i;
                           }

                           _log.info("Header columns found: " + String.join(", ", columnNames));
                           _log.info("Processing total data rows: " + sheet.getLastRowNum());
                           int currentRowNumber = 1;

                           while(true) {
                              if (!rowIterator.hasNext()) {
                                 break label116;
                              }

                              Row row = (Row)rowIterator.next();
                              ++currentRowNumber;
                              if (this.isRowEmpty(row)) {
                                 _log.warn("Skipping empty row: " + currentRowNumber);
                              } else {
                                 _log.info("=== Processing row " + currentRowNumber + " ===");

                                 try {
                                    String organizationName = this.getCellValue(row, columnNames, "ZONE / LCB / RV").trim();
                                    String folderName = this.getCellValue(row, columnNames, "Folder Name").trim();
                                    String userScreenName = this.getCellValue(row, columnNames, "MMS ID OF CONCERNED OFFICER").trim();
                                    long parentFolderId = this.parseLongSafe(this.getCellValue(row, columnNames, "PARENTFOLDERID"));
                                    _log.info("Extracted values -> Organization: " + organizationName + ", Folder: " + folderName + ", User: " + userScreenName);
                                    if (Validator.isNull(folderName)) {
                                       ++errorCount;
                                       errorMessages.add("Row " + currentRowNumber + ": Folder Name is required");
                                       _log.warn("Row " + currentRowNumber + ": Missing Folder Name, skipping.");
                                    } else {
                                       boolean result = this.processUserFolderMapping(organizationName, folderName, userScreenName, actionRequest, parentFolderId);
                                       if (result) {
                                          ++successCount;
                                          _log.info(" Row " + currentRowNumber + " processed successfully.");
                                       } else {
                                          ++errorCount;
                                          errorMessages.add("Row " + currentRowNumber + ": Failed to process mapping");
                                          _log.warn(" Row " + currentRowNumber + " failed to process.");
                                       }

                                       _log.info("=== Row " + currentRowNumber + " completed ===");
                                       _log.info("------------------------------------------------------------");
                                    }
                                 } catch (Exception e) {
                                    ++errorCount;
                                    errorMessages.add("Row " + currentRowNumber + ": " + e.getMessage());
                                    _log.error("Error processing row " + currentRowNumber, e);
                                 }
                              }
                           }
                        }

                        _log.warn("Excel sheet is empty.");
                        actionRequest.setAttribute("errorMessage", "Excel sheet is empty");
                     } catch (Throwable var27) {
                        try {
                           workbook.close();
                        } catch (Throwable var25) {
                           var27.addSuppressed(var25);
                        }

                        throw var27;
                     }

                     workbook.close();
                     break label144;
                  }

                  workbook.close();
               } catch (Throwable var28) {
                  try {
                     inputStream.close();
                  } catch (Throwable var24) {
                     var28.addSuppressed(var24);
                  }

                  throw var28;
               }

               inputStream.close();
               _log.info("=== Excel processing completed ===");
               _log.info(" Success: " + successCount + ",  Errors: " + errorCount);
               if (successCount > 0) {
                  actionRequest.setAttribute("successMessage", "Successfully processed " + successCount + " mappings. " + (errorCount > 0 ? errorCount + " errors occurred." : ""));
               }

               if (errorCount > 0) {
                  actionRequest.setAttribute("errorDetails", errorMessages);
               }

               return;
            }

            inputStream.close();
         }
      } catch (Exception e) {
         _log.error("Error uploading file: " + e.getMessage(), e);
         actionRequest.setAttribute("errorMessage", "Error uploading file: " + e.getMessage());
      }
   }

   private boolean processUserFolderMapping(String organizationName, String folderName, String userScreenName, ActionRequest actionRequest, long parentFolderId) {
      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
         long companyId = themeDisplay.getCompanyId();
         User user = null;
         if (Validator.isNotNull(userScreenName)) {
            try {
               user = UserLocalServiceUtil.getUserByScreenName(companyId, userScreenName);
            } catch (Exception var13) {
               _log.info("User not found with screen name: " + userScreenName);
            }

            if (user != null) {
               DLFolder folder = this.getFolder(user.getGroupId(), folderName, parentFolderId);
               if (folder != null) {
                  addUserOrganizationMaster(serviceContext, 13814050L, folder.getFolderId(), folder.getName(), organizationName, userScreenName, folder.getParentFolderId());
               }
            }
         }

         return true;
      } catch (Exception e) {
         _log.error("Error processing organization-folder mapping: " + e.getMessage(), e);
         return false;
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

   private boolean isRowEmpty(Row row) {
      if (row == null) {
         return true;
      } else if (row.getLastCellNum() <= 0) {
         return true;
      } else {
         for(int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); ++cellNum) {
            if (row.getCell(cellNum) != null && row.getCell(cellNum).getCellType() != CellType.BLANK) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean addUserOrganizationMaster(ServiceContext serviceContext, long objectDefinitionId, long folderId, String folderName, String organizationName, String userScreenName, long parentFolderId) {
      try {
         if (Validator.isNotNull(userScreenName) && folderId != 0L) {
            Map<String, Serializable> values = new HashMap();
            values.put("organization", organizationName);
            values.put("folderName", folderName);
            values.put("userScreenName", userScreenName);
            values.put("isActive", true);
            values.put("folderId", folderId);
            values.put("parentFolderId", parentFolderId);
            ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(serviceContext.getUserId(), 0L, objectDefinitionId, values, serviceContext);
            _log.info("Inserted ObjectEntry: " + objectEntry.getObjectEntryId());
            return true;
         } else {
            return false;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
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

   private DLFolder getFolder(long groupId, String folderName, long parentFolderId) {
      DLFolder folder = null;

      try {
         DLFolder fetchDLFolder = DLFolderLocalServiceUtil.fetchDLFolder(parentFolderId);
         _log.info(" fetchDLFolder : " + fetchDLFolder.getFolderId());
         List<DLFolder> dlFolders = DLFolderLocalServiceUtil.getFolders(fetchDLFolder.getGroupId(), parentFolderId);
         if (dlFolders.isEmpty()) {
            return folder;
         }

         for(DLFolder dlFolder : dlFolders) {
            if (dlFolder.getName().trim().equalsIgnoreCase(folderName.trim())) {
               folder = dlFolder;
               _log.info(" found folder by name: ");
               break;
            }
         }
      } catch (Exception e) {
         _log.error("Error finding folder by name: " + folderName, e);
      }

      return folder;
   }
}
