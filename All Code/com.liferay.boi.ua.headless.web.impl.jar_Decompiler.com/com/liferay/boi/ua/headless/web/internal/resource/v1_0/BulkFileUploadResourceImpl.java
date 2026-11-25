package com.liferay.boi.ua.headless.web.internal.resource.v1_0;

import com.liferay.boi.ua.headless.web.dto.v1_0.BulkUploadResult;
import com.liferay.boi.ua.headless.web.dto.v1_0.BulkUploadResultResponse;
import com.liferay.boi.ua.headless.web.internal.resource.configuration.UAFileUploadConfiguration;
import com.liferay.boi.ua.headless.web.resource.v1_0.BulkFileUploadResource;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.liferay.boi.ua.headless.web.internal.resource.configuration.UAFileUploadConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/bulk-file-upload.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {BulkFileUploadResource.class}
)
public class BulkFileUploadResourceImpl extends BaseBulkFileUploadResourceImpl {
   private UAFileUploadConfiguration uaFileUploadConfiguration;
   private List<String> filesName = new ArrayList<String>() {
      {
         this.add("file1");
         this.add("file2");
         this.add("file3");
         this.add("file4");
         this.add("file5");
         this.add("file6");
         this.add("file7");
         this.add("file8");
         this.add("file9");
         this.add("file10");
      }
   };
   @Reference
   private DLAppService _dlAppService;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.uaFileUploadConfiguration = (UAFileUploadConfiguration)ConfigurableUtil.createConfigurable(UAFileUploadConfiguration.class, properties);
   }

   public BulkUploadResult postBulkFileUpload(MultipartBody multipartBody) throws Exception {
      Map<String, BinaryFile> listFiles = new HashMap();
      BulkUploadResultResponse bulkUploadResult = new BulkUploadResultResponse();
      if (!this.uaFileUploadConfiguration.isActive()) {
         bulkUploadResult.setCode(500);
         bulkUploadResult.setMessage("API Is disabled");
         return bulkUploadResult;
      } else {
         JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

         for(String string : this.filesName) {
            try {
               BinaryFile binaryFile = multipartBody.getBinaryFile(string);
               if (Validator.isNotNull(binaryFile)) {
                  if (binaryFile.getSize() > (long)this.getFileSize(string)) {
                     bulkUploadResult.setCode(500);
                     bulkUploadResult.setMessage("File size exceed please check " + binaryFile.getFileName());
                     return bulkUploadResult;
                  }

                  listFiles.put(string, binaryFile);
               }
            } catch (Exception e) {
               System.out.println(e.getMessage());
               bulkUploadResult.setCode(500);
               bulkUploadResult.setMessage("Error " + e.getMessage());
               return bulkUploadResult;
            }
         }

         try {
            if (!listFiles.isEmpty()) {
               Folder folder = this.getFolder(Long.parseLong(multipartBody.getValueAsString("folderId")));

               for(String keys : listFiles.keySet()) {
                  JSONObject filesStatus = JSONFactoryUtil.createJSONObject();

                  try {
                     BinaryFile binaryFile = (BinaryFile)listFiles.get(keys);
                     File file = this.createFileFromInputStream(binaryFile.getInputStream(), binaryFile.getFileName());
                     FileEntry fileEntry = this.uploadFiles(folder, file, binaryFile.getFileName());
                     if (Validator.isNotNull(fileEntry)) {
                        String downloadURL = DLURLHelperUtil.getDownloadURL(fileEntry, fileEntry.getFileVersion(), (ThemeDisplay)null, (String)null);
                        file.deleteOnExit();
                        filesStatus.put("fileStatusCode", 200);
                        filesStatus.put("fileId", fileEntry.getFileEntryId());
                        filesStatus.put("fileURL", downloadURL);
                     }

                     jsonObject.put(keys, filesStatus);
                  } catch (Exception e) {
                     filesStatus.put("fileStatusCode", 500);
                     filesStatus.put("errorMessage", e.getMessage());
                     jsonObject.put(keys, filesStatus);
                  }
               }

               bulkUploadResult.setFileDetails(jsonObject);
               return bulkUploadResult;
            }

            jsonObject.put("errorCode", 500);
            jsonObject.put("errorMessage", "File Missing");
            bulkUploadResult.setFileDetails(jsonObject);
         } catch (Exception e) {
            jsonObject.put("errorMessage", e.getMessage());
            jsonObject.put("errorCode", 500);
            bulkUploadResult.setFileDetails(jsonObject);
         }

         return bulkUploadResult;
      }
   }

   public File createFileFromInputStream(InputStream inputStream, String fileName) throws IOException {
      String extension = "";
      int dotIndex = fileName.lastIndexOf(46);
      if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
         extension = fileName.substring(dotIndex);
         fileName = fileName.substring(0, dotIndex);
      }

      File file = File.createTempFile(fileName + "_", extension);
      FileOutputStream outputStream = new FileOutputStream(file);

      try {
         byte[] buffer = new byte[8192];

         int bytesRead;
         while((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
         }
      } catch (Throwable var10) {
         try {
            outputStream.close();
         } catch (Throwable var9) {
            var10.addSuppressed(var9);
         }

         throw var10;
      }

      outputStream.close();
      return file;
   }

   private Folder getFolder(long folderId) throws Exception {
      try {
         Folder folder = this._dlAppService.getFolder(folderId);
         final long roleId = this.getUserRole(folder);
         HashMap<Long, String[]> rolesForFolder = new HashMap<Long, String[]>() {
            {
               this.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_SUBFOLDER", "ADD_DOCUMENT"});
            }
         };
         boolean isTrue = this.getPermission(rolesForFolder, roleId, folder);
         if (isTrue) {
            return folder;
         } else {
            throw new Exception("You Have not access of folderid " + folderId);
         }
      } catch (PortalException e) {
         throw new Exception(e);
      }
   }

   private long getUserRole(Folder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "UA User").getRoleId();
      } catch (PortalException var3) {
         return 0L;
      }
   }

   private boolean getPermission(HashMap<Long, String[]> rolesForFolder, long roleId, Folder folder) {
      try {
         System.out.println("checking permission " + roleId);
         return ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), roleId, "ADD_DOCUMENT");
      } catch (PortalException e) {
         System.out.println(e);
         return false;
      }
   }

   private FileEntry uploadFiles(Folder folder, File file, String fileName) throws Exception {
      try {
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         long roleId = this.getUserRole(folder);
         context.setScopeGroupId(0L);
         HashMap<Long, String[]> roles = new HashMap();
         roles.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         HashMap<Long, String[]> rolesForFolder = new HashMap();
         rolesForFolder.put(roleId, new String[]{"VIEW"});
         FileEntry fileEntry = null;
         fileEntry = this._dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(fileName), fileName, " ", " ", file, context);
         boolean checkPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roleId, "VIEW");
         if (!checkPermission) {
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roles);
         }

         return fileEntry;
      } catch (Exception e) {
         throw new Exception(e);
      }
   }

   private int getFileSize(String fileName) {
      switch (fileName) {
         case "file1":
            return this.uaFileUploadConfiguration.file1Size();
         case "file2":
            return this.uaFileUploadConfiguration.file2Size();
         case "file3":
            return this.uaFileUploadConfiguration.file3Size();
         case "file4":
            return this.uaFileUploadConfiguration.file4Size();
         case "file5":
            return this.uaFileUploadConfiguration.file5Size();
         case "file6":
            return this.uaFileUploadConfiguration.file6Size();
         case "file7":
            return this.uaFileUploadConfiguration.file7Size();
         case "file8":
            return this.uaFileUploadConfiguration.file8Size();
         case "file9":
            return this.uaFileUploadConfiguration.file9Size();
         case "file10":
            return this.uaFileUploadConfiguration.file10Size();
         default:
            return 0;
      }
   }
}
