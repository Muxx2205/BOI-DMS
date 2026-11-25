package boi.document.management.system.mvcResourceCommand;

import boi.document.management.system.portlet.BoiDocumentManagementSystemPortlet;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=getSubFoldersAndDocs"},
   service = {MVCResourceCommand.class}
)
public class GetSubFoldersAndDocsMVCResourceCommand extends BaseMVCResourceCommand {
   @Reference
   private DLAppLocalService _dlAppLocalService;
   private boolean isMappedValueCalled = false;
   private static final Log _log = LogFactoryUtil.getLog(GetSubFoldersAndDocsMVCResourceCommand.class);
   private static Map<String, String> folderNameMap = new HashMap();
   private boolean isZoneLevel = false;
   private boolean isBranchLevel = false;
   private boolean isBranchUnderLevel = false;
   private boolean isNBGLevel = false;
   private boolean isboardSecretariat = false;

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long folderId = ParamUtil.getLong(resourceRequest, "folderId");
      int folderCur = ParamUtil.getInteger(resourceRequest, "folderCur", 1);
      int folderDelta = ParamUtil.getInteger(resourceRequest, "folderDelta", 10);
      int docCur = ParamUtil.getInteger(resourceRequest, "docCur", 1);
      int docDelta = ParamUtil.getInteger(resourceRequest, "docDelta", 20);
      JSONObject result = JSONFactoryUtil.createJSONObject();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      this.isZoneLevel = false;
      this.isBranchLevel = false;
      this.isBranchUnderLevel = false;
      this.isNBGLevel = false;

      try {
         List<Role> roles = BoiDocumentManagementSystemPortlet.getRoles();
         List<DLFolder> allFolders = new ArrayList();
         List<DLFolder> allSubFolders = new ArrayList();
         if (folderId > 0L) {
            allSubFolders = DLFolderLocalServiceUtil.getFolders(themeDisplay.getScopeGroupId(), folderId);
         }

         long nbgCount = allSubFolders.stream().filter((folderx) -> folderx.getName().toUpperCase().trim().startsWith("NBG")).count();
         if (nbgCount > 5L) {
            List<String> nbgFolder = Arrays.asList(BoiDocumentManagementSystemPortlet.nbgFoldersName().split("\\s*,\\s*"));
            List<DLFolder> filteredFolders = (List)allSubFolders.stream().filter((folderx) -> nbgFolder.stream().anyMatch((name) -> name.equalsIgnoreCase(folderx.getName()))).collect(Collectors.toList());
            if (filteredFolders.size() > 1) {
               allFolders = filteredFolders;
               this.isNBGLevel = true;
            }
         }

         boolean isHavingHideDocPrmsn = BoiDocumentManagementSystemPortlet.isHavingDocHidePermission();
         _log.info("isHavingHideDocPrmsn = " + isHavingHideDocPrmsn);
         if (isHavingHideDocPrmsn && allFolders.isEmpty() && !allSubFolders.isEmpty()) {
            Set<String> nbgFolder = (Set)Arrays.stream(BoiDocumentManagementSystemPortlet.nbgFoldersName().split("\\s*,\\s*")).collect(Collectors.toSet());
            this.isAt3Level(folderId, nbgFolder);
            _log.info("isZoneLevel = " + this.isZoneLevel);
            _log.info("isBranchLevel = " + this.isBranchLevel);
            _log.info("isBranchUnderLevel = " + this.isBranchUnderLevel);
            _log.info("isHavingHideDocPrmsn = " + isHavingHideDocPrmsn);
            _log.info("isNBGLevel = " + this.isNBGLevel);
            if (this.isZoneLevel) {
               Set<String> hideSet = (Set)Arrays.stream(BoiDocumentManagementSystemPortlet.api3LevelHideFolder().split("\\s*,\\s*")).map(String::toUpperCase).collect(Collectors.toSet());
               allFolders = (List)allSubFolders.stream().filter((folderx) -> {
                  String name = folderx.getName().trim().toUpperCase();
                  return !hideSet.contains(name) && !name.endsWith("OLD");
               }).collect(Collectors.toList());
            } else if (this.isBranchLevel) {
               allFolders = (List)allSubFolders.stream().filter((folderx) -> folderx.getName() != null && this.startsWith4Digits(folderx.getName().trim())).collect(Collectors.toList());
            }
         }

         if (allFolders.isEmpty()) {
            allFolders = (List)allSubFolders.stream().filter((folderx) -> roles.stream().anyMatch((role) -> folderx.getName().trim().equalsIgnoreCase(this.getMappedValue(role.getName())))).collect(Collectors.toList());
         }

         if (allFolders.isEmpty()) {
            allFolders = allSubFolders;
         }

         int totalFolders = allFolders.size();
         int folderStart = (folderCur - 1) * folderDelta;
         int folderEnd = Math.min(folderStart + folderDelta, totalFolders);
         List<DLFolder> subFolders = ListUtil.subList(allFolders, folderStart, folderEnd);
         dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
         JSONArray subFoldersArray = JSONFactoryUtil.createJSONArray();

         for(DLFolder folder : subFolders) {
            JSONObject folderJson = JSONFactoryUtil.createJSONObject();
            folderJson.put("id", folder.getFolderId());
            folderJson.put("name", folder.getName());
            folderJson.put("createdBy", this.getScreenName(folder.getUserId()));
            folderJson.put("createDate", dateFormat.format(folder.getCreateDate()));
            folderJson.put("modifiedDate", dateFormat.format(folder.getModifiedDate()));
            subFoldersArray.put(folderJson);
         }

         result.put("subFolders", subFoldersArray);
         result.put("totalFolders", totalFolders);
         result.put("folderCur", folderCur);
         result.put("folderDelta", folderDelta);
         _log.info("subFoldersArray = " + subFoldersArray);
         if (!isHavingHideDocPrmsn || !this.isBranchLevel && !this.isZoneLevel && !this.isNBGLevel && !this.isBranchUnderLevel) {
            int docStart = (docCur - 1) * docDelta;
            int docEnd = docStart + docDelta;
            new ArrayList();
            List<DLFileEntry> fileEntries = new ArrayList();
            int totalDocs = DLFileEntryLocalServiceUtil.getFileEntriesCount(themeDisplay.getScopeGroupId(), folderId);
            if (folderId > 0L) {
               List<DLFileEntry> allDocs = DLFileEntryLocalServiceUtil.getFileEntries(themeDisplay.getScopeGroupId(), folderId, -1, -1, (OrderByComparator)null);
               List<DLFileEntry> sortedFileEntries = this.sortFileEntries(allDocs, this.isboardSecretariat);
               int start = Math.max(0, docStart);
               int end = Math.min(docEnd, totalDocs);
               fileEntries = start < end ? sortedFileEntries.subList(start, end) : Collections.emptyList();
            }

            if (totalDocs > 0) {
               DLFolder folder = DLFolderLocalServiceUtil.getFolder(folderId);
               boolean isBoardSecretariatExist = Arrays.stream(folder.getTreePath().split("/")).filter((id) -> !id.isEmpty()).anyMatch((id) -> id.equals(String.valueOf(BoiDocumentManagementSystemPortlet.getBoardSectraitFolderId())));
               if (isBoardSecretariatExist) {
                  this.isboardSecretariat = true;
               }
            }

            JSONArray docsArray = JSONFactoryUtil.createJSONArray();

            for(DLFileEntry file : fileEntries) {
               try {
                  JSONObject docJson = JSONFactoryUtil.createJSONObject();
                  FileEntry fileEntry = this._dlAppLocalService.getFileEntry(file.getFileEntryId());
                  String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, "");
                  dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                  docJson.put("id", fileEntry.getFileEntryId());
                  docJson.put("url", downloadURL);
                  docJson.put("name", this.isboardSecretariat ? this.renameForBoardSecretrait(fileEntry.getTitle()) : fileEntry.getTitle());
                  docJson.put("extension", fileEntry.getExtension());
                  docJson.put("size", fileEntry.getSize());
                  docJson.put("createdBy", this.getScreenName(fileEntry.getUserId()));
                  docJson.put("createDate", dateFormat.format(fileEntry.getCreateDate()));
                  docJson.put("modifiedDate", dateFormat.format(fileEntry.getModifiedDate()));
                  docsArray.put(docJson);
               } catch (Exception e) {
                  Log var10000 = _log;
                  long var10001 = file.getFileEntryId();
                  var10000.warn("Error processing file entry: " + var10001 + ", error: " + e.getMessage());
               }
            }

            result.put("documents", docsArray);
            result.put("totalDocs", totalDocs);
            result.put("docCur", docCur);
            result.put("docDelta", docDelta);
            result.put("success", true);
         } else {
            result.put("documents", JSONFactoryUtil.createJSONArray());
            result.put("totalDocs", 0);
            result.put("success", true);
         }
      } catch (Exception e) {
         _log.error("Error fetching folder data: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", e.getMessage());
         result.put("subFolders", JSONFactoryUtil.createJSONArray());
         result.put("totalFolders", 0);
         result.put("documents", JSONFactoryUtil.createJSONArray());
         result.put("totalDocs", 0);
      }

      folderNameMap.clear();
      this.isMappedValueCalled = false;
      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }

   private String getMappedValue(String key) {
      if (MapUtil.isEmpty(folderNameMap) && !this.isMappedValueCalled) {
         this.parseFolderNames();
         this.isMappedValueCalled = true;
      }

      return (String)folderNameMap.getOrDefault(key, "12345");
   }

   private String getScreenName(long userId) {
      try {
         User user = UserLocalServiceUtil.getUser(userId);
         return user.getScreenName();
      } catch (Exception var4) {
         return "";
      }
   }

   private List<DLFileEntry> sortFileEntries(List<DLFileEntry> allDocs, boolean isBoardSecretariat) {
      List<DLFileEntry> fileEntries = new ArrayList(allDocs);
      Collections.sort(fileEntries, (f1, f2) -> {
         String name1 = isBoardSecretariat ? this.renameForBoardSecretrait(f1.getTitle()) : f1.getTitle();
         String name2 = isBoardSecretariat ? this.renameForBoardSecretrait(f2.getTitle()) : f2.getTitle();
         return this.naturalCompare(name1, name2);
      });
      return fileEntries;
   }

   private String renameForBoardSecretrait(String fileName) {
      if (fileName != null && !fileName.isBlank()) {
         try {
            int pdfIndex = fileName.toLowerCase().indexOf(".pdf");
            if (pdfIndex > -1) {
               fileName = fileName.substring(0, pdfIndex);
            }

            fileName = fileName.replaceFirst("^\\d{4,}[-_]", "");
         } catch (Exception e) {
            _log.warn("Unable to extract name from: " + fileName, e);
         }

         return fileName;
      } else {
         return fileName;
      }
   }

   private Map<String, String> parseFolderNames() {
      String workflowFolderNameMapping = BoiDocumentManagementSystemPortlet.workflowFolderNameMapping();
      String combinedMapping = "";
      if (workflowFolderNameMapping != null && !workflowFolderNameMapping.isEmpty()) {
         combinedMapping = workflowFolderNameMapping;
      }

      if (combinedMapping.isEmpty()) {
         return folderNameMap;
      } else {
         String[] entries = combinedMapping.split(",");

         for(String entry : entries) {
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length == 2) {
               String key = keyValue[0].trim().replace("\"", "");
               String value = keyValue[1].trim().replace("\"", "");
               folderNameMap.put(key, value);
            }
         }

         return folderNameMap;
      }
   }

   private void isAt3Level(long folderId, Set<String> nbgSet) {
      this.isZoneLevel = false;
      this.isBranchLevel = false;
      this.isBranchUnderLevel = false;
      if (folderId != 0L) {
         try {
            DLFolder folder = DLFolderLocalServiceUtil.fetchDLFolder(folderId);
            if (folder == null) {
               return;
            }

            String folderName = folder.getName().trim();
            if (this.startsWith4Digits(folderName)) {
               this.isBranchUnderLevel = true;
               return;
            }

            if (nbgSet.contains(folderName)) {
               this.isZoneLevel = true;
               return;
            }

            DLFolder parent = folder.getParentFolder();
            if (parent == null) {
               return;
            }

            String parentName = parent.getName().trim();
            if (nbgSet.contains(parentName)) {
               this.isBranchLevel = true;
               return;
            }
         } catch (Exception e) {
            _log.info(e.getMessage());
            e.printStackTrace();
         }

      }
   }

   private int naturalCompare(String s1, String s2) {
      s1 = s1.replaceAll("[^a-zA-Z0-9]", "");
      s2 = s2.replaceAll("[^a-zA-Z0-9]", "");
      Pattern pattern = Pattern.compile("(\\d+|\\D+)");
      Matcher m1 = pattern.matcher(s1);
      Matcher m2 = pattern.matcher(s2);

      while(m1.find() && m2.find()) {
         String part1 = m1.group();
         String part2 = m2.group();
         if (part1.matches("\\d+") && part2.matches("\\d+")) {
            int num1 = Integer.parseInt(part1);
            int num2 = Integer.parseInt(part2);
            if (num1 != num2) {
               int result = Integer.compare(num1, num2);
               return result;
            }
         } else {
            int result = part1.compareToIgnoreCase(part2);
            if (result != 0) {
               return result;
            }
         }
      }

      int finalResult = Integer.compare(s1.length(), s2.length());
      return finalResult;
   }

   private boolean startsWith4Digits(String name) {
      if (name == null) {
         return false;
      } else {
         name = name.trim();
         if (name.length() < 6) {
            return false;
         } else {
            for(int i = 0; i < 4; ++i) {
               if (!Character.isDigit(name.charAt(i))) {
                  return false;
               }
            }

            int index = 4;
            if (name.charAt(index) != '-') {
               return false;
            } else {
               ++index;

               while(index < name.length() && name.charAt(index) == ' ') {
                  ++index;
               }

               if (index >= name.length()) {
                  return false;
               } else {
                  return !Character.isDigit(name.charAt(index));
               }
            }
         }
      }
   }
}
