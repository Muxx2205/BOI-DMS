package com.vel.util;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.newgen.dao.NewGenDbService;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderPermissionUtill extends Thread {
   static Map<Long, Long> m = new HashMap();
   private static String roleName = "User";
   long folderId;
   long dlFolderId;
   String sflag;
   public static boolean startFlag = true;
   ServiceContext ctx;
   DLFolderLocalService fldservice = DLFolderLocalServiceUtil.getService();
   DLFolder fold = null;
   long userid;
   ResourcePermissionLocalService roleService = ResourcePermissionLocalServiceUtil.getService();

   public FolderPermissionUtill(long folderId, long dlFolderId, String sflag, ServiceContext ctx) {
      this.folderId = folderId;
      this.dlFolderId = dlFolderId;
      this.ctx = ctx;
      this.sflag = sflag;
   }

   public void run() {
      if (this.sflag.equals("Start2")) {
         this.migFolders2();
      } else {
         this.migFolders();
      }

      startFlag = true;
   }

   public void migFolders2() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();
         NewGenDbService service = new NewGenDbService();
         List<Map<String, String>> list = service.getSubFoldersPrnding(this.folderId, conn);
         long uid = this.userid;
         int xx = 0;

         for(Map<String, String> mp : list) {
            ++xx;

            try {
               this.fold = this.fldservice.getDLFolder(Long.parseLong((String)mp.get("pfid")));
               this.userid = this.fold.getUserId();
               mp.get("userid");
               DLFolder dlfold = this.fldservice.addFolder(PortalUUIDUtil.generate(), this.userid, this.fold.getGroupId(), this.fold.getRepositoryId(), false, this.fold.getFolderId(), (String)mp.get("name") + " D" + xx, (String)mp.get("name") + " D" + xx, false, this.ctx);
               this.setResourcePermission(dlfold);
               service.updateFolderIds(Long.parseLong((String)mp.get("folderindex")), dlfold.getUuid(), dlfold.getFolderId(), conn);
            } catch (Exception e) {
               PrintStream var10000 = System.out;
               String var10001 = String.valueOf(e);
               var10000.println("Exception in " + var10001 + "   " + (String)mp.get("folderindex"));
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

   }

   public void migFolders() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();
         this.fold = this.fldservice.getDLFolder(this.dlFolderId);
         this.userid = this.fold.getUserId();
         NewGenDbService service = new NewGenDbService();
         List<Map<String, String>> list = service.getSubFolders(this.folderId, conn);
         long uid = this.userid;

         for(Map<String, String> mp : list) {
            if (!startFlag) {
               break;
            }

            try {
               if (mp.get("fid") == null) {
                  mp.get("userid");
                  DLFolder dlfold = this.fldservice.addFolder(PortalUUIDUtil.generate(), uid, this.fold.getGroupId(), this.fold.getRepositoryId(), false, this.fold.getFolderId(), (String)mp.get("name"), (String)mp.get("name"), false, this.ctx);
                  this.setResourcePermission(dlfold);
                  service.updateFolderIds(Long.parseLong((String)mp.get("folderindex")), dlfold.getUuid(), dlfold.getFolderId(), conn);
                  if (!this.sflag.equalsIgnoreCase("Start1")) {
                     this.migSubFolders(Long.parseLong((String)mp.get("folderindex")), dlfold.getFolderId(), conn, service);
                  }
               } else if (!this.sflag.equalsIgnoreCase("Start1")) {
                  this.migSubFolders(Long.parseLong((String)mp.get("folderindex")), Long.parseLong((String)mp.get("fid")), conn, service);
               }
            } catch (Exception e) {
               PrintStream var10000 = System.out;
               String var10001 = String.valueOf(e);
               var10000.println("Exception in " + var10001 + "   " + (String)mp.get("folderindex"));
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

   }

   private void migSubFolders(long folderId, Long pFol, Connection con, NewGenDbService service) throws Exception {
      List<Map<String, String>> list = service.getSubFolders(folderId, con);
      long uid = this.userid;

      for(Map<String, String> mp : list) {
         try {
            if (mp.get("fid") == null) {
               mp.get("userid");
               DLFolder dlfold = this.fldservice.addFolder(PortalUUIDUtil.generate(), uid, this.fold.getGroupId(), this.fold.getRepositoryId(), false, pFol, (String)mp.get("name"), (String)mp.get("name"), false, this.ctx);
               this.setResourcePermission(dlfold);
               service.updateFolderIds(Long.parseLong((String)mp.get("folderindex")), dlfold.getUuid(), dlfold.getFolderId(), con);
               this.migSubFolders(Long.parseLong((String)mp.get("folderindex")), dlfold.getFolderId(), con, service);
            } else {
               this.migSubFolders(Long.parseLong((String)mp.get("folderindex")), Long.parseLong((String)mp.get("fid")), con, service);
            }
         } catch (Exception e) {
            PrintStream var10000 = System.out;
            String var10001 = String.valueOf(e);
            var10000.println("Exception in " + var10001 + "   " + (String)mp.get("folderindex"));
         }
      }

   }

   public void setResourcePermission(DLFolder folder) throws PortalException {
      long roleid = 1L;
      if (m.containsKey(folder.getCompanyId())) {
         roleid = (Long)m.get(folder.getCompanyId());
      } else {
         Role userRole = RoleLocalServiceUtil.getRole(folder.getCompanyId(), roleName);
         roleid = userRole.getRoleId();
         m.put(folder.getCompanyId(), roleid);
      }

      this.roleService.setResourcePermissions(folder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(folder.getFolderId()), roleid, new String[]{"VIEW"});
   }
}
