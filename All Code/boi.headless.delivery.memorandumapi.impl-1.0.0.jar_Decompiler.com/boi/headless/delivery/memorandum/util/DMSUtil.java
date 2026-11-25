package boi.headless.delivery.memorandum.util;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DMSUtil {
   private static final Log log = LogFactoryUtil.getLog(FileUtil.class);

   private DMSUtil() {
   }

   public static Folder getFolder(long folderId) {
      try {
         return DLAppServiceUtil.getFolder(folderId);
      } catch (Exception e) {
         log.error("Unable to get folder with ID " + folderId, e);
         return null;
      }
   }

   public static Folder getFolder(long repoId, long parentId, String name) {
      try {
         return DLAppServiceUtil.getFolder(repoId, parentId, name);
      } catch (Exception var6) {
         return null;
      }
   }

   public static Folder createFolder(String folderName, long parentFolderId) {
      try {
         if (Validator.isBlank(folderName)) {
            log.warn("createFolder() : folderName is blank");
            return null;
         } else {
            Folder parent = getFolder(parentFolderId);
            if (parent == null) {
               log.warn("Parent folder missing: " + parentFolderId);
               return null;
            } else {
               Folder existing = getFolder(parent.getRepositoryId(), parent.getFolderId(), folderName);
               if (existing != null) {
                  return existing;
               } else {
                  ServiceContext ctx = ServiceContextThreadLocal.getServiceContext();
                  ctx.setScopeGroupId(parent.getGroupId());
                  Folder newFolder = DLAppServiceUtil.addFolder((String)null, parent.getRepositoryId(), parentFolderId, folderName, "", ctx);
                  giveDefaultPermissions(newFolder);
                  return newFolder;
               }
            }
         }
      } catch (Exception e) {
         log.error("Error creating folder '" + folderName + "'", e);
         return null;
      }
   }

   public static FileEntry getFileEntry(Folder folder, String fileName) {
      try {
         return DLAppServiceUtil.getFileEntry(folder.getGroupId(), folder.getFolderId(), fileName);
      } catch (Exception var3) {
         return null;
      }
   }

   public static boolean deleteFileEntry(FileEntry entry) {
      try {
         DLAppServiceUtil.deleteFileEntry(entry.getFileEntryId());
         return true;
      } catch (Exception e) {
         log.error("Failed to delete file entry " + entry.getFileEntryId(), e);
         return false;
      }
   }

   public static FileEntry createOrReplaceFile(Folder folder, File file, String fileName) {
      try {
         FileEntry existing = getFileEntry(folder, fileName);
         if (existing != null) {
            deleteFileEntry(existing);
         }

         return uploadFile(folder, file, fileName);
      } catch (Exception e) {
         log.error("createOrReplaceFile failed", e);
         return null;
      }
   }

   public static FileEntry uploadFile(Folder folder, File file, String fileName) {
      try {
         ServiceContext ctx = ServiceContextThreadLocal.getServiceContext();
         ctx.setScopeGroupId(folder.getGroupId());
         FileEntry fileEntry = DLAppServiceUtil.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(file.getName()), fileName, "", "", file, ctx);
         giveFilePermissions(folder, fileEntry);
         return fileEntry;
      } catch (Exception e) {
         log.error("File upload failed for file: " + fileName, e);
         return null;
      }
   }

   private static long getUserRoleId(long companyId) {
      try {
         return RoleLocalServiceUtil.getRole(companyId, "User").getRoleId();
      } catch (Exception e) {
         log.error("Cannot fetch 'User' role", e);
         return 0L;
      }
   }

   private static void giveFilePermissions(Folder folder, FileEntry fileEntry) {
      try {
         long roleId = getUserRoleId(folder.getCompanyId());
         Map<Long, String[]> permissions = new HashMap();
         permissions.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         boolean already = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(fileEntry.getFileEntryId()), roleId, "VIEW");
         if (!already) {
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(fileEntry.getFileEntryId()), permissions);
         }
      } catch (Exception e) {
         log.error("Error setting file permissions", e);
      }

   }

   private static void giveDefaultPermissions(Folder folder) {
      try {
         long roleId = getUserRoleId(folder.getCompanyId());
         Map<Long, String[]> permissions = new HashMap();
         permissions.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_SUBFOLDER", "ADD_DOCUMENT"});
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, String.valueOf(folder.getFolderId()), permissions);
      } catch (Exception e) {
         log.error("Failed to set folder permissions", e);
      }

   }

   public static String removeWhitespace(String str) {
      return str == null ? null : str.replaceAll("\\s+", "");
   }

   public static String generateUniqueFolderName(String base) {
      String date = DateUtil.getDate(new Date(), "ddMMyyyy", Locale.getDefault());
      long random = CounterLocalServiceUtil.increment();
      return date + "-" + base + "-" + random;
   }
}
