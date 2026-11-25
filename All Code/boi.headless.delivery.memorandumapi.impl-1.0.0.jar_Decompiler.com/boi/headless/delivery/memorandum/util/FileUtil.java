package boi.headless.delivery.memorandum.util;

import boi.headless.delivery.memorandum.constant.MemorandumConstants;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
   private static final Log log = LogFactoryUtil.getLog(FileUtil.class);
   private final MultipartBody _multipartBody;
   private final String _className;
   private String zoneName;
   private String nbgName;
   private String hoName;
   private String branchName;
   private static final long maxSize = 10485760L;
   private static final List<String> _allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "tif", "pdf", "docs", "docx", "xls", "xlsx", "csv", "gif", "txt");
   private static DLAppService dlAppService;

   public FileUtil(MultipartBody _multipartBody, String _className) {
      this._multipartBody = _multipartBody;
      this._className = _className;
   }

   public Map<String, BinaryFile> validateFile(JSONObject jsonObject) {
      JSONArray errorsArray = JSONFactoryUtil.createJSONArray();
      Map<String, String> objectFileNames = MemorandumConstants.getObjectFileNames(this._className);
      Map<String, BinaryFile> validatedFiles = new HashMap();

      for(Map.Entry<String, String> entry : objectFileNames.entrySet()) {
         String fieldName = (String)entry.getValue();
         BinaryFile binaryFile = this._multipartBody.getBinaryFile(fieldName);
         if (!this.isFilePresent(binaryFile)) {
            errorsArray.put("No file provided for upload: " + fieldName);
         } else {
            String extError = this.validateFileExtension(binaryFile);
            if (Validator.isNotNull(extError)) {
               errorsArray.put(extError + " (" + fieldName + ")");
            }

            String sizeError = this.validateFileSize(binaryFile);
            if (Validator.isNotNull(sizeError)) {
               errorsArray.put(sizeError + " (" + fieldName + ")");
            }

            if (Validator.isNull(extError) && Validator.isNull(sizeError)) {
               validatedFiles.put((String)entry.getKey(), binaryFile);
            }
         }
      }

      if (errorsArray.length() > 0) {
         jsonObject.put("errors", errorsArray);
         return validatedFiles;
      } else {
         return validatedFiles;
      }
   }

   public List<FileEntry> uploadFile(long folderId, JSONObject jsonObject, String erc, Map<String, BinaryFile> validatedFiles) {
      List<FileEntry> files = new ArrayList();

      try {
         JSONArray errorsArray = JSONFactoryUtil.createJSONArray();
         Folder folder = DLAppLocalServiceUtil.getFolder(folderId);

         for(BinaryFile binaryFile : validatedFiles.values()) {
            File tempFile = null;

            FileEntry fileEntry;
            try {
               tempFile = this.convertBinaryFileToFile(binaryFile);
               if (tempFile == null) {
                  throw new PortalException("Failed to convert file");
               }

               String uniqueFileName = erc != null ? erc : this.getUniqueFileName(folder, binaryFile.getFileName());
               if (this._className.equalsIgnoreCase("PSRS")) {
               }

               fileEntry = this.createOrReplaceFileEntry(folder, tempFile, uniqueFileName);
               if (fileEntry == null) {
                  throw new PortalException("Failed to upload file : " + binaryFile.getFileName());
               }

               files.add(fileEntry);
               continue;
            } catch (Exception e) {
               errorsArray.put(e.getMessage());
               jsonObject.put("errors", errorsArray);
               fileEntry = null;
            } finally {
               if (tempFile != null && tempFile.exists()) {
                  tempFile.delete();
               }

            }

            return fileEntry;
         }
      } catch (Exception var20) {
      }

      return files;
   }

   private boolean isFilePresent(BinaryFile binaryFile) {
      return Validator.isNotNull(binaryFile) && !Validator.isBlank(binaryFile.getFileName());
   }

   private String validateFileSize(BinaryFile binaryFile) {
      return binaryFile.getSize() > 10485760L ? "File '" + binaryFile.getFileName() + "' exceeds 10MB limit." : null;
   }

   private String validateFileExtension(BinaryFile binaryFile) {
      String fileName = binaryFile.getFileName();
      if (Validator.isBlank(fileName)) {
         return "Invalid file name.";
      } else {
         int dotCount = fileName.length() - fileName.replace(".", "").length();
         if (dotCount > 1) {
            return "File '" + fileName + "' contains multiple extensions, which is not allowed.";
         } else {
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex == -1) {
               return "File '" + fileName + "' has no extension.";
            } else {
               String extension = fileName.substring(lastDotIndex + 1);
               return _allowedExtensions.stream().noneMatch((ext) -> ext.equalsIgnoreCase(extension)) ? "Extension '" + extension + "' is not allowed for file '" + fileName + "'" : null;
            }
         }
      }
   }

   private File convertBinaryFileToFile(BinaryFile binaryFile) {
      try {
         File tempFile = File.createTempFile("upload-", "-" + binaryFile.getFileName());
         Files.copy(binaryFile.getInputStream(), tempFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
         return tempFile;
      } catch (Exception e) {
         log.error("Error converting BinaryFile to File", e);
         return null;
      }
   }

   private String getUniqueFileName(Folder folder, String fileName) {
      String baseName = fileName;
      String extension = "";
      int dotIndex = fileName.lastIndexOf(".");
      if (dotIndex > 0) {
         baseName = fileName.substring(0, dotIndex);
         extension = fileName.substring(dotIndex);
      }

      String timestamp = (new SimpleDateFormat("ddMMyyyyHHmmss")).format(new Date());
      String newFileName = baseName + "_" + timestamp + extension;
      return newFileName;
   }

   public FileEntry createOrReplaceFileEntry(Folder folder, File file, String fileName) {
      try {
         FileEntry fileEntry = this.getFileEntry(folder, fileName);
         if (Validator.isNotNull(fileEntry)) {
            this.deleteFileEntry(fileEntry);
         }

         return this.uploadFile(folder, file, fileName);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   private void deleteFileEntry(FileEntry entry) {
      try {
         DLAppLocalServiceUtil.deleteFileEntry(entry.getFileEntryId());
      } catch (Exception e) {
         log.info(e);
      }

   }

   private FileEntry getFileEntry(Folder folder, String fileName) {
      try {
         return DLAppLocalServiceUtil.getFileEntry(folder.getGroupId(), folder.getFolderId(), fileName);
      } catch (Exception var4) {
         return null;
      }
   }

   private FileEntry uploadFile(Folder folder, File file, String fileName) {
      try {
         log.info("Starting file upload: " + fileName + " to folderId: " + folder.getFolderId());
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         log.info("ServiceContext obtained: " + context);
         String contentType = MimeTypesUtil.getContentType(file.getName());
         log.info("Detected file content type: " + contentType);
         FileEntry fileEntry = DLAppLocalServiceUtil.addFileEntry(folder.getUserId(), folder.getRepositoryId(), folder.getFolderId(), fileName, contentType, fileName, "", "", file, context);
         log.info("File uploaded successfully with fileEntryId: " + fileEntry.getFileEntryId());
         long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
         log.info("Role 'User' found with roleId: " + roleId);
         boolean hasPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(fileEntry.getFileEntryId()), roleId, "VIEW");
         log.info("Permission check for VIEW: " + hasPermission);
         if (!hasPermission) {
            HashMap<Long, String[]> roles = new HashMap();
            roles.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(fileEntry.getFileEntryId()), roles);
            log.info("Permissions set for fileEntryId: " + fileEntry.getFileEntryId());
         }

         return fileEntry;
      } catch (Exception e) {
         log.error("Error uploading file: " + fileName, e);
         return null;
      }
   }

   public long getPSRSFolder(long headOfficeFolderId, long psrsFolderId, String moduleName, User user) {
      try {
         this.loadUserOrganizationData(user);
         String psrsSubFolderName = "";
         if (Validator.isBlank(this.nbgName) && Validator.isBlank(this.zoneName) && Validator.isBlank(this.branchName)) {
            log.error("Organization data missing for user: " + user.getUserId());
            return -1234L;
         } else {
            DLFolder rootFolder = DLFolderLocalServiceUtil.fetchFolder(headOfficeFolderId);
            log.info("rootFolder = " + rootFolder != null);
            DLFolder nbgFolder = DLFolderLocalServiceUtil.fetchFolder(rootFolder.getRepositoryId(), rootFolder.getFolderId(), this.nbgName);
            log.info("nbgFolder = " + nbgFolder != null);
            DLFolder zoneFolder = DLFolderLocalServiceUtil.fetchFolder(nbgFolder.getRepositoryId(), nbgFolder.getFolderId(), this.zoneName);
            log.info("zoneFolder = " + zoneFolder != null);
            DLFolder branchFolder = DLFolderLocalServiceUtil.fetchFolder(zoneFolder.getRepositoryId(), zoneFolder.getFolderId(), this.branchName);
            log.info("branchFolder = " + branchFolder != null);
            DLFolder dlFolder = Validator.isNull(branchFolder) ? DLFolderLocalServiceUtil.fetchFolder(psrsFolderId) : branchFolder;
            psrsSubFolderName = Validator.isNull(branchFolder) ? moduleName + " - " + this.nbgName + " - " + this.zoneName + " - " + this.branchName : moduleName;
            DLFolder psrsFolder = this.createORGetFolder(dlFolder.getRepositoryId(), dlFolder.getFolderId(), dlFolder.getGroupId(), "PSRS");
            log.info("psrsFolder = " + psrsFolder != null);
            DLFolder moduleFolder = this.createORGetFolder(psrsFolder.getRepositoryId(), psrsFolder.getFolderId(), psrsFolder.getGroupId(), psrsSubFolderName);
            log.info("moduleFolder = " + moduleFolder != null);
            return Validator.isNotNull(moduleFolder) ? moduleFolder.getFolderId() : psrsFolderId;
         }
      } catch (Exception e) {
         log.error("Error in getPSRSFolder: ", e);
         return psrsFolderId;
      }
   }

   public DLFolder createORGetFolder(long repositoryId, long folderId, long groupId, String folderName) {
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
               log.info("Folder created: " + folderName);
               return newFolder;
            } catch (Exception e) {
               log.error("Failed to create folder: " + folderName, e);
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
         log.info("checking permission " + roleId);
         boolean isTrue = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), roleId, "ADD_DOCUMENT");
         if (!isTrue) {
            return this.addPermission(folder, roleId, rolesForFolder);
         }
      } catch (PortalException e) {
         e.printStackTrace();
      }

      return false;
   }

   private boolean addPermission(DLFolder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {
      try {
         log.info("Adding permission " + roleId);
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), rolesForFolder);
         return true;
      } catch (PortalException e) {
         e.printStackTrace();
         return false;
      }
   }

   private long getUserRole(DLFolder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
      } catch (PortalException e) {
         e.printStackTrace();
         return 0L;
      }
   }

   private String getOrganization(List<Organization> organizations, String type) {
      for(Organization organization : organizations) {
         Organization organization2 = this.getOrganization(type, organization);
         if (Validator.isNotNull(organization2)) {
            return organization2.getName();
         }
      }

      return null;
   }

   private void loadUserOrganizationData(User user) {
      try {
         List<Organization> orgs = user.getOrganizations();
         this.branchName = this.getOrganization(orgs, "BRANCH");
         this.zoneName = this.getOrganization(orgs, "ZONE");
         this.nbgName = this.getOrganization(orgs, "NBG");
         log.info("Branch = " + this.branchName);
         log.info("Zone = " + this.zoneName);
         log.info("NBG = " + this.nbgName);
      } catch (Exception e) {
         log.error("Error loading org data", e);
      }

   }

   public String getBranchName() {
      return this.branchName;
   }

   public String getZoneName() {
      return this.zoneName;
   }

   public String getNbgName() {
      return this.nbgName;
   }

   public String getHoName() {
      return this.hoName;
   }

   private Organization getOrganization(String type, Organization org) {
      if (org == null) {
         return null;
      } else if (type.equalsIgnoreCase(org.getType())) {
         return org;
      } else {
         try {
            return this.getOrganization(type, org.getParentOrganization());
         } catch (Exception var4) {
            return null;
         }
      }
   }
}
