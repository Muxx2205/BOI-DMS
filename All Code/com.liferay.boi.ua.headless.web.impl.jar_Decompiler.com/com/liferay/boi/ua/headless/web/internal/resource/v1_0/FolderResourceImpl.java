package com.liferay.boi.ua.headless.web.internal.resource.v1_0;

import com.liferay.boi.ua.headless.web.dto.v1_0.Folder;
import com.liferay.boi.ua.headless.web.dto.v1_0.FolderCreateRequest;
import com.liferay.boi.ua.headless.web.internal.resource.configuration.UAFileUploadConfiguration;
import com.liferay.boi.ua.headless.web.resource.v1_0.FolderResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import java.util.HashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.liferay.boi.ua.headless.web.internal.resource.configuration.UAFileUploadConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/folder.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FolderResource.class}
)
public class FolderResourceImpl extends BaseFolderResourceImpl {
   private UAFileUploadConfiguration uaFileUploadConfiguration;
   @Reference
   private DLAppService _dlAppService;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.uaFileUploadConfiguration = (UAFileUploadConfiguration)ConfigurableUtil.createConfigurable(UAFileUploadConfiguration.class, properties);
   }

   private long getParentFolderId(long parentFolderId) throws Exception {
      try {
         return this.getFolder(parentFolderId).getFolderId();
      } catch (Exception var4) {
         throw new Exception("Parent folder is missing or you have not access");
      }
   }

   public Folder postFolder(FolderCreateRequest folderCreateRequest) throws Exception {
      System.out.println("Request Recieved postFolder");
      Folder folder = new Folder();
      if (!this.uaFileUploadConfiguration.isActive()) {
         folder.setCode(500);
         folder.setMessage("API Is disabled");
         return folder;
      } else {
         try {
            com.liferay.portal.kernel.repository.model.Folder folder2 = this.createFolder(folderCreateRequest.getName(), this.getParentFolderId((long)folderCreateRequest.getParentFolderId()));
            folder.setCode(200);
            folder.setCreatedFolderId(String.valueOf(folder2.getFolderId()));
            folder.setCreatedFolderName(folder2.getName());
            folder.setParentFolderId((int)folder2.getParentFolderId());
            folder.setMessage("Folder created!");
         } catch (Exception e) {
            folder.setCode(500);
            folder.setMessage(e.getMessage());
         }

         return folder;
      }
   }

   private com.liferay.portal.kernel.repository.model.Folder getFolder(long folderId) throws Exception {
      try {
         return this._dlAppService.getFolder(folderId);
      } catch (Exception e) {
         throw new Exception(e);
      }
   }

   private com.liferay.portal.kernel.repository.model.Folder createFolder(String folderName, long parentFolderId) throws Exception {
      com.liferay.portal.kernel.repository.model.Folder folder = null;
      com.liferay.portal.kernel.repository.model.Folder folder2 = this.getFolder(parentFolderId);
      final long roleId = this.getUserRole(folder2);
      HashMap<Long, String[]> rolesForFolder = new HashMap<Long, String[]>() {
         {
            this.put(roleId, new String[]{"VIEW", "ACCESS", "ADD_SUBFOLDER", "ADD_DOCUMENT"});
         }
      };
      if (!Validator.isBlank(folderName) && !Validator.isNull(folder2)) {
         if (!this.getPermission(rolesForFolder, roleId, folder2)) {
            throw new Exception("You have not access of parent folder");
         } else {
            ServiceContext context = new ServiceContext();
            context.setScopeGroupId(folder2.getGroupId());

            try {
               folder = this._dlAppService.getFolder(folder2.getRepositoryId(), folder2.getFolderId(), folderName);
            } catch (Exception var13) {
               try {
                  folder = this._dlAppService.addFolder((String)null, folder2.getRepositoryId(), parentFolderId, folderName, "", context);
                  boolean isTrue = this.getPermission(rolesForFolder, roleId, folder);
                  if (!isTrue) {
                     this.addPermission(folder, roleId, rolesForFolder);
                  }
               } catch (PortalException e1) {
                  System.out.println(e1);
               }
            }

            return folder;
         }
      } else {
         return folder;
      }
   }

   private boolean getPermission(HashMap<Long, String[]> rolesForFolder, long roleId, com.liferay.portal.kernel.repository.model.Folder folder) {
      try {
         System.out.println("checking permission " + roleId);
         return ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), roleId, "ADD_DOCUMENT");
      } catch (PortalException e) {
         System.out.println(e);
         return false;
      }
   }

   private boolean addPermission(com.liferay.portal.kernel.repository.model.Folder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {
      try {
         System.out.println("Adding permission " + roleId);
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), rolesForFolder);
         return true;
      } catch (PortalException var6) {
         return false;
      }
   }

   private long getUserRole(com.liferay.portal.kernel.repository.model.Folder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "UA User").getRoleId();
      } catch (PortalException var3) {
         return 0L;
      }
   }
}
