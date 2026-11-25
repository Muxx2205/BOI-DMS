package boi.user.management.backgroundTask;

import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskStatusMessageTranslator;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"background.task.executor.class.name=boi.user.management.backgroundTask.ExcelUploadBackgroundTaskExecutor"},
   service = {BackgroundTaskExecutor.class}
)
public class ExcelUploadBackgroundTaskExecutor extends BaseBackgroundTaskExecutor {
   private static final Log _log = LogFactoryUtil.getLog(ExcelUploadBackgroundTaskExecutor.class);

   public ExcelUploadBackgroundTaskExecutor() {
      this.setBackgroundTaskStatusMessageTranslator((BackgroundTaskStatusMessageTranslator)null);
   }

   public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
      List<Map<String, Serializable>> successList = new ArrayList();
      List<Map<String, Serializable>> failedList = new ArrayList();
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      List<String> notFoundADID = new ArrayList();
      String base64Content = (String)taskContextMap.get("fileContent");
      byte[] fileContent = Base64.getDecoder().decode(base64Content);
      String fileName = (String)taskContextMap.get("fileName");
      long companyId = ((Number)taskContextMap.get("companyId")).longValue();
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

      label425: {
         BackgroundTaskResult var69;
         try {
            InputStream inputStream;
            label429: {
               inputStream = new FileInputStream(file);

               try {
                  label430: {
                     Workbook workbook = new XSSFWorkbook(inputStream);

                     label417: {
                        try {
                           Sheet sheet = workbook.getSheetAt(0);
                           Iterator<Row> rowIterator = sheet.iterator();
                           int totalRows = sheet.getLastRowNum();
                           int processed = 0;
                           if (!rowIterator.hasNext()) {
                              _log.info("Excel sheet is empty");
                              var69 = new BackgroundTaskResult(3);
                              break label417;
                           }

                           Row headerRow = (Row)rowIterator.next();
                           int lastColumn = headerRow.getLastCellNum();
                           String[] columnNames = new String[lastColumn];

                           for(int i = 0; i < lastColumn; ++i) {
                              columnNames[i] = headerRow.getCell(i) != null ? headerRow.getCell(i).toString().trim() : "Column" + i;
                           }

                           Role psrsRole = RoleLocalServiceUtil.getRole(companyId, "PSRS");
                           Role psrsBranchMaker = RoleLocalServiceUtil.getRole(companyId, "PSRS Branch Maker");
                           Role psrsBranchChecker = RoleLocalServiceUtil.getRole(companyId, "PSRS Branch Checker");
                           Role psrsZoneMaker = RoleLocalServiceUtil.getRole(companyId, "PSRS Zone Maker");
                           Role psrsZoneChecker = RoleLocalServiceUtil.getRole(companyId, "PSRS Zone Checker");
                           Role psrsRADZm = RoleLocalServiceUtil.getRole(companyId, "PSRS RA DZM");
                           Role psrsNotingAuthority = RoleLocalServiceUtil.getRole(companyId, "PSRS Noting Authority");
                           String screenName = "";

                           while(rowIterator.hasNext()) {
                              Row row = (Row)rowIterator.next();
                              String roleType = null;
                              String orgType = null;

                              try {
                                 ++processed;
                                 int progress = (int)((float)processed * 100.0F / (float)totalRows);
                                 Map<String, Serializable> ctx = backgroundTask.getTaskContextMap();
                                 ctx.put("1progress", progress);
                                 ctx.put("1processed", processed);
                                 ctx.put("1total", totalRows);
                                 ctx.put("1statusText", "Processing Excel...");
                                 String var71 = this.getCellValueByPartialHeaderMatch(row, columnNames, "ADID");
                                 roleType = this.getCellValueByPartialHeaderMatch(row, columnNames, "ROLE(Maker/Checker)");
                                 orgType = this.getCellValueByPartialHeaderMatch(row, columnNames, "Organisation Type(Branch/Zone/FGMO/HO)");
                                 _log.info("----------------------------------------------------------------------------");
                                 screenName = Validator.isNotNull(var71) ? var71.trim() : "";
                                 User user = UserLocalServiceUtil.fetchUserByScreenName(companyId, screenName);
                                 if (user == null) {
                                    _log.info("screenName Not Found : " + screenName);
                                    notFoundADID.add(screenName);
                                    Map<String, Serializable> fail = new HashMap();
                                    fail.put("ADID", screenName);
                                    fail.put("orgType", orgType);
                                    fail.put("roleType", roleType);
                                    fail.put("status", "FAILED");
                                    fail.put("reason", "User not found");
                                    failedList.add(fail);
                                 } else {
                                    List<String> missingFields = new ArrayList();
                                    if (Validator.isNull(screenName)) {
                                       missingFields.add("screenName (ADID)");
                                    }

                                    if (Validator.isNull(orgType)) {
                                       missingFields.add("orgType");
                                    }

                                    if (Validator.isNull(roleType)) {
                                       missingFields.add("roleType");
                                    }

                                    if (!missingFields.isEmpty()) {
                                       Map<String, Serializable> fail = new HashMap();
                                       fail.put("ADID", screenName);
                                       fail.put("orgType", orgType);
                                       fail.put("roleType", roleType);
                                       fail.put("status", "FAILED");
                                       fail.put("reason", "Missing required field(s): " + String.join(", ", missingFields));
                                       if (!missingFields.isEmpty()) {
                                          failedList.add(fail);
                                          continue;
                                       }
                                    }

                                    if (user != null && missingFields.isEmpty()) {
                                       _log.info("screenName Found : " + screenName);
                                       List<Role> userRoles = user.getRoles();
                                       List<String> psrsRoleNames = Arrays.asList("PSRS Branch Maker", "PSRS Branch Checker", "PSRS Zone Maker", "PSRS Zone Checker", "PSRS RA DZM", "PSRS Noting Authority", "PSRS");

                                       for(Role role : userRoles) {
                                          if (psrsRoleNames.contains(role.getName())) {
                                             RoleLocalServiceUtil.deleteUserRole(user.getUserId(), role.getRoleId());
                                             _log.info("Removed Role: " + role.getName());
                                          }
                                       }

                                       List<String> rolesAdded = new ArrayList();
                                       Set<String> allowedRoles = new HashSet(Arrays.asList("maker", "checker", "zone maker", "zone checker", "ra", "noting"));
                                       String roleType2 = this.getRoleType(roleType, orgType);
                                       if (!allowedRoles.contains(roleType2)) {
                                          Map<String, Serializable> fail = new HashMap();
                                          fail.put("ADID", screenName);
                                          fail.put("orgType", orgType);
                                          fail.put("roleType", roleType);
                                          fail.put("status", "FAILED");
                                          fail.put("reason", " Organisation Type(Branch/Zone/FGMO/HO) : " + orgType + ", ROLE(Maker/Checker) : " + roleType);
                                          failedList.add(fail);
                                       } else {
                                          switch (roleType2) {
                                             case "maker":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsBranchMaker.getRoleId());
                                                rolesAdded.add("PSRS Branch Maker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             case "checker":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsBranchMaker.getRoleId());
                                                rolesAdded.add("PSRS Branch Maker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsBranchChecker.getRoleId());
                                                rolesAdded.add("PSRS Branch Checker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             case "zone maker":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsZoneMaker.getRoleId());
                                                rolesAdded.add("PSRS Zone Maker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             case "zone checker":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsZoneMaker.getRoleId());
                                                rolesAdded.add("PSRS Zone Maker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsZoneChecker.getRoleId());
                                                rolesAdded.add("PSRS Zone Checker");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             case "ra":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRADZm.getRoleId());
                                                rolesAdded.add("PSRS RA DZM");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             case "noting":
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsNotingAuthority.getRoleId());
                                                rolesAdded.add("PSRS Noting Authority");
                                                RoleLocalServiceUtil.addUserRole(user.getUserId(), psrsRole.getRoleId());
                                                rolesAdded.add("PSRS");
                                                break;
                                             default:
                                                _log.warn("Invalid roleType: " + roleType);
                                          }

                                          List<String> roles = (List)user.getRoles().stream().map(RoleModel::getName).collect(Collectors.toList());
                                          Map<String, Serializable> success = new HashMap();
                                          success.put("ADID", screenName);
                                          success.put("status", "SUCCESS");
                                          success.put("orgType", orgType);
                                          success.put("roleType", roleType);
                                          String rolesString = String.join(", ", rolesAdded);
                                          success.put("role", rolesString);
                                          successList.add(success);
                                          Log var10000 = _log;
                                          String var10001 = user.getScreenName();
                                          var10000.info("User = " + var10001 + ". Roles After Update = " + roles);
                                       }
                                    }
                                 }
                              } catch (Exception e) {
                                 _log.error(e.getMessage());
                                 Map<String, Serializable> fail = new HashMap();
                                 fail.put("ADID", screenName);
                                 fail.put("orgType", orgType);
                                 fail.put("roleType", roleType);
                                 fail.put("status", "FAILED");
                                 fail.put("reason", e.getMessage());
                                 failedList.add(fail);
                              }
                           }
                        } catch (Throwable var64) {
                           try {
                              workbook.close();
                           } catch (Throwable var62) {
                              var64.addSuppressed(var62);
                           }

                           throw var64;
                        }

                        workbook.close();
                        break label430;
                     }

                     workbook.close();
                     break label429;
                  }
               } catch (Throwable var65) {
                  try {
                     inputStream.close();
                  } catch (Throwable var61) {
                     var65.addSuppressed(var61);
                  }

                  throw var65;
               }

               inputStream.close();
               break label425;
            }

            inputStream.close();
         } finally {
            try {
               _log.info("ALL Not Found Users = " + notFoundADID);
               _log.info("SUCCESS COUNT = " + successList.size());
               _log.info("FAILED COUNT = " + failedList.size());
               Map<String, Serializable> ctx = backgroundTask.getTaskContextMap();
               ctx.put("1progress", 100);
               ctx.put("1statusText", "Completed");
               ctx.put("1successCount", successList.size());
               ctx.put("1failedCount", failedList.size());
               ctx.put("1successMap", successList);
               ctx.put("1failedMap", failedList);
            } catch (Exception e) {
               _log.info(e.getMessage());
            }

            if (useTempFile && file != null && file.exists()) {
               try {
                  file.delete();
                  _log.info("Temporary file cleaned up: " + file.getName());
               } catch (Exception e) {
                  _log.warn("Failed to delete temporary file: " + file.getName(), e);
               }
            }

         }

         return var69;
      }

      _log.info("BackgroundTaskResult Success");
      return new BackgroundTaskResult(3);
   }

   private String getRoleType(String roleType, String orgType) {
      if (roleType != null && !roleType.trim().isEmpty()) {
         String r = roleType.trim().toLowerCase().replace("-", "").replace("_", "").replaceAll("\\s+", "");
         String o = orgType == null ? "" : orgType.trim().toLowerCase();
         r = r.replace("cheker", "checker").replace("chaker", "checker");
         boolean isBranch = o.contains("branch");
         boolean isZone = o.contains("zone");
         if (isBranch) {
            if (r.contains("maker")) {
               return "maker";
            }

            if (r.contains("checker")) {
               return "checker";
            }
         }

         if (isZone) {
            if (r.contains("maker")) {
               return "zone maker";
            }

            if (r.contains("checker1") || r.contains("checker 1")) {
               return "ra";
            }

            if (r.contains("checker2") || r.contains("checker 2")) {
               return "noting";
            }

            if (r.contains("checker")) {
               return "zone checker";
            }
         }

         return roleType.toLowerCase();
      } else {
         return "";
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

   private String getCellValueByPartialHeaderMatch(Row row, String[] columnNames, String columnName) {
      if (row != null && columnNames != null && columnName != null) {
         for(int i = 0; i < columnNames.length; ++i) {
            String header = columnNames[i];
            if (header != null && header.toLowerCase().contains(columnName.toLowerCase())) {
               return row.getCell(i) != null ? row.getCell(i).toString().trim() : "";
            }
         }

         return "";
      } else {
         return "";
      }
   }

   public static String extractNumber(String input) {
      if (input != null && !input.isEmpty()) {
         String[] nums = input.split("\\D+");
         String max = null;

         for(String n : nums) {
            if (!n.isEmpty() && (max == null || Long.parseLong(n) > Long.parseLong(max))) {
               max = n;
            }
         }

         if (max != null && max.length() > 4) {
            max = max.substring(0, 4);
         }

         System.out.println("org " + input + " : " + max);
         return max;
      } else {
         return null;
      }
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return new ExcelUploadBackgroundTaskExecutor();
   }
}
