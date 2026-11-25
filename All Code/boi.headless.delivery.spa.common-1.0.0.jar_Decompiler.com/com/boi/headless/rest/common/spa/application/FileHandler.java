package com.boi.headless.rest.common.spa.application;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FileHandler {
   private static final Log log = LogFactoryUtil.getLog(FileHandler.class);
   private static DLAppService dlAppService;

   public FileHandler(DLAppService dlAppService) {
      FileHandler.dlAppService = dlAppService;
   }

   public Folder createFolderStructure(User user, String finalName, long parentFolderId) {
      if (Validator.isNull(user)) {
         return null;
      } else {
         UserHierarchy userHierarchy = new UserHierarchy(user);
         if (validateUserHierarchyForFolderCreation(userHierarchy)) {
            Folder folder = this.createFolderForAO(userHierarchy, finalName, parentFolderId);
            if (Validator.isNotNull(folder)) {
               return folder;
            }
         }

         return null;
      }
   }

   public FileEntry createOrRenameFileEntryBranchDigi(Folder folder, File file, String fileName) {
      try {
         return this.uploadFiles(folder, file, fileName);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   public FileEntry createOrReplaceFileEntry(Folder folder, File file, String fileName) {
      try {
         FileEntry fileEntry = this.getFileEntry(folder, fileName);
         if (Validator.isNotNull(fileEntry)) {
            this.deleteFileEntry(fileEntry);
         }

         return this.uploadFiles(folder, file, fileName);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   public FileEntry createFileEntry(Folder folder, File file, String fileName) {
      try {
         return this.uploadFiles(folder, file, fileName);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   public FileEntry createFileEntryArchive(Folder folder, File file, String fileName, String description) {
      try {
         return this.uploadFilesArchive(folder, file, fileName, description);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   public FileEntry createFileEntryProd(Folder folder, File file, String fileName, String description) {
      try {
         return this.uploadFilesProd(folder, file, fileName, description);
      } catch (Exception e) {
         log.info(e);
         return null;
      }
   }

   public FileEntry getFileEntry(Folder folder, String fileName) {
      try {
         return dlAppService.getFileEntry(folder.getGroupId(), folder.getFolderId(), fileName);
      } catch (Exception var4) {
         return null;
      }
   }

   private void deleteFileEntry(FileEntry entry) {
      try {
         dlAppService.deleteFileEntry(entry.getFileEntryId());
      } catch (Exception e) {
         log.info(e);
      }

   }

   private FileEntry uploadFiles(Folder folder, File file, String fileName) {
      try {
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
         context.setScopeGroupId(0L);
         HashMap<Long, String[]> roles = new HashMap();
         roles.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         HashMap<Long, String[]> rolesForFolder = new HashMap();
         rolesForFolder.put(roleId, new String[]{"VIEW"});
         FileEntry fileEntry = null;
         fileEntry = dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(file.getName()), fileName, " ", " ", file, context);
         boolean checkPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roleId, "VIEW");
         if (!checkPermission) {
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roles);
         }

         return fileEntry;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   private FileEntry uploadFilesArchive(Folder folder, File file, String fileName, String description) {
      try {
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
         context.setScopeGroupId(0L);
         HashMap<Long, String[]> roles = new HashMap();
         roles.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         HashMap<Long, String[]> rolesForFolder = new HashMap();
         rolesForFolder.put(roleId, new String[]{"VIEW"});
         FileEntry fileEntry = null;
         fileEntry = dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(file.getName()), fileName, description, " ", file, context);
         boolean checkPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roleId, "VIEW");
         if (!checkPermission) {
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roles);
         }

         return fileEntry;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   private FileEntry uploadFilesProd(Folder folder, File file, String fileName, String description) {
      try {
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
         context.setScopeGroupId(0L);
         HashMap<Long, String[]> roles = new HashMap();
         roles.put(roleId, new String[]{"VIEW", "DOWNLOAD"});
         HashMap<Long, String[]> rolesForFolder = new HashMap();
         rolesForFolder.put(roleId, new String[]{"VIEW"});
         FileEntry fileEntry = null;
         fileEntry = dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName, MimeTypesUtil.getContentType(file.getName()), fileName, description, " ", file, context);
         boolean checkPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roleId, "VIEW");
         if (!checkPermission) {
            ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, Long.toString(fileEntry.getFileEntryId()), roles);
         }

         return fileEntry;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   private static boolean validateUserHierarchyForFolderCreation(UserHierarchy userHierarchy) {
      return !Validator.isNull(userHierarchy) && !Validator.isBlank(userHierarchy.getBranchName()) && !Validator.isBlank(userHierarchy.getZoneName()) && !Validator.isBlank(userHierarchy.getNbgName());
   }

   public static String removeAllWhitespace(String input) {
      return input == null ? null : input.replaceAll("\\s+", "");
   }

   private Folder createFolderForAO(UserHierarchy userHierarchy, String finalName, long parentFolderId) {
      String branchName = userHierarchy.getBranchName().strip();
      String zoneName = userHierarchy.getZoneName().strip();
      String nbgName = userHierarchy.getNbgName().strip();
      final Folder nBGFolder = this.createFolder(removeAllWhitespace(nbgName), parentFolderId);
      final Folder zoneFolder = this.createFolder(removeAllWhitespace(zoneName), nBGFolder.getFolderId());
      final Folder branchFolder = this.createFolder(removeAllWhitespace(branchName), zoneFolder.getFolderId());
      List<Folder> folders = new ArrayList<Folder>() {
         {
            if (Validator.isNotNull(nBGFolder)) {
               this.add(nBGFolder);
            }

            if (Validator.isNotNull(zoneFolder)) {
               this.add(zoneFolder);
            }

            if (Validator.isNotNull(branchFolder)) {
               this.add(branchFolder);
            }

         }
      };
      String lastFolderName = (String)folders.stream().map(Folder::getName).filter((name) -> !Validator.isBlank(name)).collect(Collectors.joining("-"));
      String date = DateUtil.getDate(new Date(), "ddMMyyyy", Locale.getDefault());
      long randomNumber = CounterLocalServiceUtil.increment();
      finalName = Validator.isNull(finalName) ? date + "-" + lastFolderName + "-" + randomNumber : finalName;
      Folder urnGenerate = this.createFolder(finalName, branchFolder.getFolderId());
      return urnGenerate;
   }

   public Folder getFolder(long folderId) {
      try {
         return dlAppService.getFolder(folderId);
      } catch (PortalException e) {
         e.printStackTrace();
         return null;
      }
   }

   public Folder getFolderDtl(long folderId) {
      try {
         return dlAppService.getFolder(folderId);
      } catch (PortalException e) {
         e.printStackTrace();
         return null;
      }
   }

   public Folder createFolder(String folderName, long parentFolderId) {
      if (Validator.isBlank(folderName)) {
         log.warn("Folder name is blank.");
         return null;
      } else {
         Folder parentFolder = this.getFolder(parentFolderId);
         if (parentFolder == null) {
            log.warn("Parent folder not found for ID: {}" + parentFolderId);
            return null;
         } else {
            log.info("Looking for folder '{}' under parent ID: {} (repository ID: {})" + folderName + parentFolderId + parentFolder.getRepositoryId());

            try {
               return dlAppService.getFolder(parentFolder.getRepositoryId(), parentFolder.getFolderId(), folderName);
            } catch (PortalException notFoundEx) {
               log.info("Folder not found. Attempting to create: {}" + notFoundEx.getMessage());

               try {
                  ServiceContext context = ServiceContextThreadLocal.getServiceContext();
                  context.setScopeGroupId(parentFolder.getGroupId());
                  Folder newFolder = dlAppService.addFolder((String)null, parentFolder.getRepositoryId(), parentFolderId, folderName, "", context);
                  this.getPermission(newFolder);
                  log.info("Folder created successfully: {}" + newFolder.getName());
                  return newFolder;
               } catch (PortalException createEx) {
                  log.error("Failed to create folder '{}': {}" + folderName + createEx.getMessage() + createEx);
                  return null;
               }
            }
         }
      }
   }

   public Folder createFolder(String folderName, Folder parentFolder) {
      long parentFolderId = parentFolder.getFolderId();
      log.info("Searching for folder '{}' under parentFolderId: {} with repositoryId: {}" + folderName + parentFolderId + parentFolder.getRepositoryId());

      try {
         Folder folder = dlAppService.getFolder(parentFolder.getRepositoryId(), parentFolder.getFolderId(), folderName);
         return folder;
      } catch (PortalException e) {
         log.info("Folder not found, attempting to create new folder: {}" + e.getMessage());

         try {
            ServiceContext context = ServiceContextThreadLocal.getServiceContext();
            context.setScopeGroupId(parentFolder.getGroupId());
            Folder newFolder = dlAppService.addFolder((String)null, parentFolder.getRepositoryId(), parentFolderId, folderName, "", context);
            this.getPermission(newFolder);
            log.info("Folder '{}' created successfully under parent ID {}" + folderName + parentFolderId);
            return newFolder;
         } catch (PortalException creationException) {
            log.error("Error creating folder '{}': {}" + folderName + creationException.getMessage() + creationException);
            return null;
         }
      }
   }

   private boolean getPermission(Folder folder) {
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

   private boolean addPermission(Folder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {
      try {
         log.info("Adding permission " + roleId);
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), rolesForFolder);
         return true;
      } catch (PortalException e) {
         e.printStackTrace();
         return false;
      }
   }

   private long getUserRole(Folder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
      } catch (PortalException e) {
         e.printStackTrace();
         return 0L;
      }
   }
}
