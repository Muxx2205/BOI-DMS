package boi.document.management.system.mvcResourceCommand;

import boi.document.management.system.portlet.BoiDocumentManagementSystemPortlet;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderService;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=createDocument"},
   service = {MVCResourceCommand.class}
)
public class CreateDocumentMVCResourceCommand implements MVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(CreateDocumentMVCResourceCommand.class);
   @Reference
   private DLAppService _dlAppService;
   @Reference
   private DLFolderService _dlFolderService;

   public boolean serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(resourceRequest);
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();
      long userId = themeDisplay.getUserId();
      User user = null;

      try {
         user = UserLocalServiceUtil.getUser(userId);
         boolean isHavingAddUpdateAction = user.getRoles().stream().anyMatch((x) -> x.getName().equalsIgnoreCase("DMS Create Rights"));
         if (!isHavingAddUpdateAction) {
            responseJson.put("success", false);
            responseJson.put("error", "Create access denied.");
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         PortletSession session = resourceRequest.getPortletSession();
         long maxFiles = GetterUtil.getLong(session.getAttribute("MAX_FILES", 2), BoiDocumentManagementSystemPortlet.maxFiles());
         long maxFileSizeMB = GetterUtil.getLong(session.getAttribute("MAX_FILE_SIZE", 2), BoiDocumentManagementSystemPortlet.maxFileSize());
         long maxFileSizeBytes = maxFileSizeMB * 1024L * 1024L;
         long maxTotalSizeMB = GetterUtil.getLong(session.getAttribute("MAX_TOTAL_SIZE", 2), 100L);
         long maxTotalSizeBytes = maxTotalSizeMB * 1024L * 1024L;
         Set<String> allowedExtensions = this.parseExtensions(BoiDocumentManagementSystemPortlet.allowedExtensions());
         Set<String> blockedExtensions = this.parseExtensions(BoiDocumentManagementSystemPortlet.blockedExtensions());
         long parentFolderId = ParamUtil.getLong(uploadPortletRequest, "parentFolderId");
         if (parentFolderId == 0L) {
            responseJson.put("success", false);
            responseJson.put("error", "Create access denied.");
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         File[] documentFiles = uploadPortletRequest.getFiles("documentFiles");
         String[] documentFileNames = uploadPortletRequest.getFileNames("documentFiles");
         ValidationResult validationResult = this.validateUploadedFiles(documentFiles, documentFileNames, maxFileSizeBytes, maxFiles, maxTotalSizeBytes, allowedExtensions, blockedExtensions);
         if (!validationResult.isValid()) {
            responseJson.put("success", false);
            responseJson.put("error", validationResult.getErrorMessage());
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFileEntry.class.getName(), uploadPortletRequest);
         List<String> uploadedFiles = new ArrayList();
         List<String> failedFiles = new ArrayList();

         for(int i = 0; i < documentFiles.length; ++i) {
            File file = documentFiles[i];
            String fileName = documentFileNames[i];
            if (file != null && file.exists() && file.length() > 0L) {
               try {
                  if (!this.isFileSafe(file, fileName)) {
                     failedFiles.add(fileName + " - Security check failed");
                  } else {
                     String title = this.getFileTitle(fileName);
                     String mimeType = this.getValidatedMimeType(fileName, allowedExtensions);
                     if (mimeType == null) {
                        failedFiles.add(fileName + " - Invalid file type");
                     } else {
                        this._dlAppService.addFileEntry(themeDisplay.getScopeGroupId(), parentFolderId, fileName, mimeType, title, "", "", file, serviceContext);
                        uploadedFiles.add(fileName);
                     }
                  }
               } catch (Exception e) {
                  _log.error("Error uploading file " + fileName + ": " + e.getMessage(), e);
                  failedFiles.add(fileName + " - " + e.getMessage());
               }
            }
         }

         if (!uploadedFiles.isEmpty()) {
            responseJson.put("success", true);
            responseJson.put("message", "Successfully uploaded " + uploadedFiles.size() + " file(s)");
            responseJson.put("uploadedFiles", JSONFactoryUtil.createJSONArray(uploadedFiles));
            if (!failedFiles.isEmpty()) {
               responseJson.put("warning", "Failed to upload " + failedFiles.size() + " file(s)");
               responseJson.put("failedFiles", JSONFactoryUtil.createJSONArray(failedFiles));
            }
         } else {
            responseJson.put("success", false);
            responseJson.put("error", "No files were successfully uploaded");
         }
      } catch (Exception e) {
         _log.error("Error uploading documents: " + e.getMessage(), e);
         responseJson.put("success", false);
         responseJson.put("error", "Failed to upload documents: " + e.getMessage());
      }

      this.writeResponse(resourceResponse, responseJson);
      return true;
   }

   private Set<String> parseExtensions(String extensionsString) {
      Set<String> extensions = new HashSet();
      if (extensionsString != null && !extensionsString.trim().isEmpty()) {
         String[] parts = extensionsString.split(",");

         for(String part : parts) {
            String extension = part.trim().toLowerCase();
            if (!extension.isEmpty()) {
               extensions.add(extension);
            }
         }
      }

      return extensions;
   }

   private ValidationResult validateUploadedFiles(File[] files, String[] fileNames, long maxFileSize, long maxFiles, long maxTotalSize, Set<String> allowedExtensions, Set<String> blockedExtensions) {
      if (files != null && files.length != 0) {
         if ((long)files.length > maxFiles) {
            return new ValidationResult(false, "Too many files. Maximum " + maxFiles + " files allowed.");
         } else {
            Set<String> uniqueNames = new HashSet();
            List<String> duplicates = new ArrayList();

            for(String fileName : fileNames) {
               if (!uniqueNames.add(fileName.toLowerCase())) {
                  duplicates.add(fileName);
               }
            }

            if (!duplicates.isEmpty()) {
               return new ValidationResult(false, "Duplicate filenames found: " + String.join(", ", duplicates));
            } else {
               long totalSize = 0L;
               int i = 0;

               while(i < files.length) {
                  File file = files[i];
                  String fileName = fileNames[i];
                  if (file != null && file.exists()) {
                     if (file.length() == 0L) {
                        return new ValidationResult(false, "File '" + fileName + "' is empty");
                     }

                     String extension = this.getFileExtension(fileName).toLowerCase();
                     if (extension.isEmpty()) {
                        return new ValidationResult(false, "File '" + fileName + "' has no extension");
                     }

                     totalSize += file.length();
                     if (file.length() > maxFileSize) {
                        String var22 = file.getName();
                        return new ValidationResult(false, "File '" + var22 + "' exceeds max size of " + this.formatFileSize(maxFileSize));
                     }

                     if (totalSize > maxTotalSize) {
                        String var10003 = this.formatFileSize(maxTotalSize);
                        return new ValidationResult(false, "Total upload size exceeds " + var10003);
                     }

                     if (!allowedExtensions.contains(extension)) {
                        return new ValidationResult(false, "File type '" + extension + "' is not allowed for '" + fileName + "'. Allowed types: " + String.join(", ", allowedExtensions));
                     }

                     if (blockedExtensions.contains(extension)) {
                        return new ValidationResult(false, "File type '" + extension + "' is blocked for security reasons");
                     }

                     if (!fileName.contains("..") && this.countFileExtensions(fileName) <= 1) {
                        if (this.isReservedFilename(fileName)) {
                           return new ValidationResult(false, "Filename '" + fileName + "' is not allowed");
                        }

                        if (fileName.length() > 255) {
                           return new ValidationResult(false, "Filename '" + fileName + "' is too long. Maximum 255 characters allowed.");
                        }

                        if (this.containsInvalidCharacters(fileName)) {
                           return new ValidationResult(false, "Filename '" + fileName + "' contains invalid characters");
                        }

                        ++i;
                        continue;
                     }

                     return new ValidationResult(false, "Invalid filename '" + fileName + "'. Potential security risk detected.");
                  }

                  return new ValidationResult(false, "File '" + fileName + "' is invalid or corrupted");
               }

               return new ValidationResult(true, (String)null);
            }
         }
      } else {
         return new ValidationResult(false, "No files selected for upload");
      }
   }

   private String formatFileSize(long bytes) {
      if (bytes < 1024L) {
         return bytes + " bytes";
      } else {
         return bytes < 1048576L ? bytes / 1024L + " KB" : bytes / 1048576L + " MB";
      }
   }

   private boolean isFileSafe(File file, String fileName) {
      try {
         if (!this.isValidFileSignature(file, fileName)) {
            _log.warn("File signature validation failed for: " + fileName);
            return false;
         } else {
            return true;
         }
      } catch (Exception e) {
         _log.error("Security check failed for file: " + fileName, e);
         return false;
      }
   }

   private boolean isValidFileSignature(File file, String fileName) {
      InputStream inputStream = null;

      boolean var26;
      try {
         String extension = this.getFileExtension(fileName).toLowerCase();
         if (file.length() >= 8L) {
            inputStream = new FileInputStream(file);
            byte[] fileHeader = new byte[8];
            int bytesRead = inputStream.read(fileHeader);
            if (bytesRead < 8) {
               boolean var32 = true;
               return var32;
            }

            if ("pdf".equals(extension)) {
               boolean var31 = (new String(fileHeader, 0, 4)).equals("%PDF");
               return var31;
            }

            if (!Arrays.asList("jpg", "jpeg").contains(extension)) {
               if (!"png".equals(extension)) {
                  if (!Arrays.asList("tif", "tiff").contains(extension)) {
                     boolean var30 = true;
                     return var30;
                  }

                  boolean var29 = fileHeader[0] == 73 && fileHeader[1] == 73 && fileHeader[2] == 42 && fileHeader[3] == 0 || fileHeader[0] == 77 && fileHeader[1] == 77 && fileHeader[2] == 0 && fileHeader[3] == 42;
                  return var29;
               }

               byte[] pngSignature = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
               boolean e = Arrays.equals(fileHeader, pngSignature);
               return e;
            }

            boolean var7 = (fileHeader[0] & 255) == 255 && (fileHeader[1] & 255) == 216 && (fileHeader[2] & 255) == 255;
            return var7;
         }

         var26 = true;
      } catch (Exception e) {
         _log.error("Error reading file signature for: " + fileName, e);
         var26 = false;
         return var26;
      } finally {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (IOException e) {
               _log.error("Error closing input stream for: " + fileName, e);
            }
         }

      }

      return var26;
   }

   private String getFileTitle(String fileName) {
      int lastDotIndex = fileName.lastIndexOf(46);
      return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
   }

   private String getFileExtension(String fileName) {
      int lastDotIndex = fileName.lastIndexOf(46);
      return lastDotIndex > 0 && lastDotIndex < fileName.length() - 1 ? fileName.substring(lastDotIndex + 1) : "";
   }

   private int countFileExtensions(String fileName) {
      return fileName.split("\\.").length - 1;
   }

   private String getValidatedMimeType(String fileName, Set<String> allowedExtensions) {
      String extension = this.getFileExtension(fileName).toLowerCase();
      if (!allowedExtensions.contains(extension)) {
         return null;
      } else {
         switch (extension) {
            case "pdf":
               return "application/pdf";
            case "doc":
               return "application/msword";
            case "docx":
               return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
               return "application/vnd.ms-excel";
            case "xlsx":
               return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
               return "text/plain";
            case "jpg":
            case "jpeg":
               return "image/jpeg";
            case "png":
               return "image/png";
            case "gif":
               return "image/gif";
            case "tif":
            case "tiff":
               return "image/tiff";
            default:
               return "application/octet-stream";
         }
      }
   }

   private boolean isReservedFilename(String fileName) {
      String nameWithoutExtension = this.getFileTitle(fileName).toUpperCase();
      String[] reservedNames = BoiDocumentManagementSystemPortlet.reservedNames();

      for(String reserved : reservedNames) {
         if (nameWithoutExtension.equals(reserved)) {
            return true;
         }
      }

      return false;
   }

   private boolean containsInvalidCharacters(String fileName) {
      if (fileName.contains("\u0000")) {
         return true;
      } else if (!fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")) {
         for(char c : fileName.toCharArray()) {
            if (Character.isISOControl(c)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private void writeResponse(ResourceResponse resourceResponse, JSONObject responseJson) {
      try {
         resourceResponse.setContentType("application/json");
         resourceResponse.setCharacterEncoding("UTF-8");
         resourceResponse.getWriter().write(responseJson.toString());
      } catch (IOException e) {
         _log.error("Error writing response: " + e.getMessage(), e);
      }

   }

   private static class ValidationResult {
      private final boolean valid;
      private final String errorMessage;

      public ValidationResult(boolean valid, String errorMessage) {
         this.valid = valid;
         this.errorMessage = errorMessage;
      }

      public boolean isValid() {
         return this.valid;
      }

      public String getErrorMessage() {
         return this.errorMessage;
      }
   }
}
