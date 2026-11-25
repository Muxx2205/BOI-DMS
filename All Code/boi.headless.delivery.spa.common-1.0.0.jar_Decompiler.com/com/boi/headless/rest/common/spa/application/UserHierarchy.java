package com.boi.headless.rest.common.spa.application;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.Validator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserHierarchy {
   private String name;
   private long userId;
   private String emailId;
   private String fullName;
   private String departmentId;
   private String branchName;
   private String zoneName;
   private String nbgName;
   private String hoName;
   private Organization branchOrganization;
   private Organization zoneOrganization;
   private Organization nbgOrganization;
   private Organization hoOrganization;
   private User user;
   private long branchFolderId;
   private long zoneFolderId;
   private long nbgFolderId;
   private long hoFolderId;
   private long departmentFolderId;
   private boolean isMaker = false;
   private boolean isZoneChecker = false;
   private ArrayList<String> folders;
   private static final Log log = LogFactoryUtil.getLog(UserHierarchy.class);
   private DLAppService _dlAppService;

   public String getURN() {
      String lastFolderName = (String)this.folders.stream().filter((name) -> !Validator.isBlank(name)).collect(Collectors.joining("-"));
      return lastFolderName;
   }

   public UserHierarchy(User user) {
      this.user = user;
      this.name = user.getScreenName();
      this.userId = user.getUserId();
      this.emailId = user.getEmailAddress();
      this.fullName = user.getFullName();
      this.loadUserOrganizationData(user);
      this.isBranchMaker();
      this.loadZoneChecker();
   }

   public UserHierarchy(User user, long hoFolderId, String departmentId, DLAppService dlAppService) {
      if (!Validator.isNull(user)) {
         this.user = user;
         this._dlAppService = dlAppService;
         this.name = user.getScreenName();
         this.userId = user.getUserId();
         this.emailId = user.getEmailAddress();
         this.fullName = user.getFullName();
         this.departmentId = departmentId;
         this.hoFolderId = hoFolderId;
         this.loadUserOrganizationData(user);
      }
   }

   public long getUploadFolderId() {
      long folderReturn = 0L;
      if (this.nbgFolderId != 0L) {
         folderReturn = this.nbgFolderId;
      }

      if (this.zoneFolderId != 0L) {
         folderReturn = this.zoneFolderId;
      }

      if (this.branchFolderId != 0L) {
         folderReturn = this.branchFolderId;
      }

      if (this.departmentFolderId != 0L) {
         folderReturn = this.departmentFolderId;
      }

      return folderReturn == 0L ? this.hoFolderId : folderReturn;
   }

   public Folder getUploadFolder() {
      long folderReturn = this.getUploadFolderId();
      return folderReturn == 0L ? this.getFolder(this.hoFolderId) : this.getFolder(folderReturn);
   }

   private void loadFolderStructure() {
      String branchName = this.getBranchName();
      String zoneName = this.getZoneName();
      String nbgName = this.getNbgName();
      final Folder nBGFolder = this.createFolder(removeAllWhitespace(nbgName), this.hoFolderId);
      final Folder zoneFolder = this.createFolder(removeAllWhitespace(zoneName), nBGFolder.getFolderId());
      final Folder branchFolder = this.createFolder(removeAllWhitespace(branchName), zoneFolder.getFolderId());
      Folder finalFolder = null;
      this.folders = new ArrayList<String>() {
         {
            String date = DateUtil.getDate(new Date(), "ddMMyyyy", Locale.getDefault());
            this.add(date);
            if (Validator.isNotNull(nBGFolder)) {
               this.add(nBGFolder.getName());
            }

            if (Validator.isNotNull(zoneFolder)) {
               this.add(zoneFolder.getName());
            }

            if (Validator.isNotNull(branchFolder)) {
               this.add(branchFolder.getName());
            }

            this.add(String.valueOf(CounterLocalServiceUtil.increment()));
         }
      };
      if (Validator.isNotNull(nBGFolder)) {
         this.nbgFolderId = nBGFolder.getFolderId();
         finalFolder = nBGFolder;
      }

      if (Validator.isNotNull(zoneFolder)) {
         this.zoneFolderId = zoneFolder.getFolderId();
         finalFolder = zoneFolder;
      }

      if (Validator.isNotNull(branchFolder)) {
         this.branchFolderId = branchFolder.getFolderId();
         finalFolder = branchFolder;
      }

      if (Validator.isNotNull(finalFolder)) {
         Folder urnGenerate = this.createFolder(this.departmentId, finalFolder.getFolderId());
         this.departmentFolderId = urnGenerate.getFolderId();
      } else {
         Folder urnGenerate = this.createFolder(this.departmentId, this.hoFolderId);
         this.departmentFolderId = urnGenerate.getFolderId();
      }

   }

   public static String removeAllWhitespace(String input) {
      return input == null ? null : input.replaceAll("\\s+", "");
   }

   private Organization getOrganization(String type, Organization organization) {
      if (Validator.isNotNull(organization) && organization.getType().equalsIgnoreCase(type)) {
         return organization;
      } else {
         if (Validator.isNotNull(organization) && organization.getParentOrganizationId() > 0L) {
            try {
               return this.getOrganization(type, organization.getParentOrganization());
            } catch (PortalException var4) {
            }
         }

         return null;
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
         this.branchName = this.getOrganization(user.getOrganizations(), "BRANCH");
         this.zoneName = this.getOrganization(user.getOrganizations(), "ZONE");
         this.nbgName = this.getOrganization(user.getOrganizations(), "NBG");
         this.hoName = this.getOrganization(user.getOrganizations(), "HO");
      } catch (PortalException var3) {
      }

   }

   private void isBranchMaker() {
      Role role = this.getRole("Branch Maker");
      if (Validator.isNotNull(role)) {
         this.isMaker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.userId);
      }

   }

   private void loadZoneChecker() {
      Role role = this.getRole("Zone Checker");
      if (Validator.isNotNull(role)) {
         this.isZoneChecker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.userId);
      }

   }

   private Role getRole(String roleName) {
      try {
         Role role = RoleLocalServiceUtil.getRole(this.user.getCompanyId(), roleName);
         return role;
      } catch (PortalException var3) {
         return null;
      }
   }

   private Folder getFolder(long folderId) {
      try {
         return this._dlAppService.getFolder(folderId);
      } catch (PortalException var4) {
         return null;
      }
   }

   private Folder createFolder(String folderName, long parentFolderId) {
      Folder folder = null;
      Folder folder2 = this.getFolder(parentFolderId);
      if (!Validator.isBlank(folderName) && !Validator.isNull(folder2)) {
         ServiceContext context = ServiceContextThreadLocal.getServiceContext();
         context.setScopeGroupId(folder2.getGroupId());

         try {
            folder = this._dlAppService.getFolder(folder2.getRepositoryId(), folder2.getFolderId(), folderName);
         } catch (Exception var10) {
            try {
               folder = this._dlAppService.addFolder((String)null, folder2.getRepositoryId(), parentFolderId, folderName, "", context);
               this.getPermission(folder);
            } catch (PortalException e1) {
               e1.printStackTrace();
            }
         }

         return folder;
      } else {
         return folder;
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
      } catch (PortalException var6) {
      }

      return false;
   }

   private boolean addPermission(Folder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {
      try {
         log.info("Adding permission " + roleId);
         ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder", 4, Long.toString(folder.getFolderId()), rolesForFolder);
         return true;
      } catch (PortalException var6) {
         return false;
      }
   }

   private long getUserRole(Folder folder) {
      try {
         return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
      } catch (PortalException var3) {
         return 0L;
      }
   }

   public String getName() {
      return this.name;
   }

   public long getUserId() {
      return this.userId;
   }

   public String getEmailId() {
      return this.emailId;
   }

   public String getFullName() {
      return this.fullName;
   }

   public String getDepartmentId() {
      return this.departmentId;
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

   public User getUser() {
      return this.user;
   }

   public long getBranchFolderId() {
      return this.branchFolderId;
   }

   public long getZoneFolderId() {
      return this.zoneFolderId;
   }

   public long getNbgFolderId() {
      return this.nbgFolderId;
   }

   public long getHoFolderId() {
      return this.hoFolderId;
   }

   public long getDepartmentFolderId() {
      return this.departmentFolderId;
   }

   public boolean isMaker() {
      return this.isMaker;
   }

   public boolean isZoneChecker() {
      return this.isZoneChecker;
   }

   public static Log getLog() {
      return log;
   }

   public DLAppService get_dlAppService() {
      return this._dlAppService;
   }
}
