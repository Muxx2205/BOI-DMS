package com.vel.util;

import com.data.mig.db.AppDBConn;
import com.data.mig.util.FileStorageUtil;
import com.liferay.document.library.kernel.exception.DuplicateFileEntryException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileVersionLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.newgen.dao.NewGenDbService;
import com.vel.dto.NewGenFileData;
import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FileMigrationUtil extends Thread {
   int dataclass;
   int userId;
   ServiceContext ctx;
   String toDate;
   String fromDate;
   long roleId;
   FileStorageUtil storageUtil;
   public static boolean startFlag = true;
   private long defaultFolderId = 4806995L;
   String flag;
   DLFolder defaulFolder;

   public FileMigrationUtil(int dataclass, String fromDate, String toDate, int userId, String rootpath, ServiceContext ctx) {
      this.dataclass = dataclass;
      this.ctx = ctx;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.userId = userId;
      this.storageUtil = new FileStorageUtil(rootpath);
   }

   public FileMigrationUtil(int dataclass, String fromDate, String toDate, String flag, int userId, String rootpath, ServiceContext ctx) {
      this.dataclass = dataclass;
      this.ctx = ctx;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.flag = flag;
      this.userId = userId;
      this.storageUtil = new FileStorageUtil(rootpath);
   }

   public void run() {
      startFlag = true;
      this.startMig();
      startFlag = true;
   }

   public void startMig() {
      Connection con = null;
      NewGenDbService service = new NewGenDbService();

      try {
         con = DataAccess.getConnection();
         List<Map<String, Object>> list = null;
         this.defaulFolder = DLFolderLocalServiceUtil.getDLFolder(this.defaultFolderId);
         if (this.flag == null) {
            list = service.getFilesFromDataClassParent2(this.dataclass, this.fromDate, this.toDate, con);
         } else {
            list = service.getFilesFromDataClassDir2(this.dataclass, this.fromDate, this.toDate, con);
         }

         PrintStream var10000 = System.out;
         int var10001 = list.size();
         var10000.println(var10001 + "    DataClass  " + this.dataclass + " From " + this.fromDate + " To " + this.toDate);

         for(Map<String, Object> mp : list) {
            if (!startFlag) {
               break;
            }

            NewGenFileData data = new NewGenFileData();
            data.setStream(this.storageUtil.getFileData("" + this.dataclass, mp.get("DID").toString()));
            data.setFileName(mp.get("DOCNAME").toString());
            data.setMimetype(mp.get("MTYPE").toString());
            data.setData((Date)mp.get("Date"));
            data.setSize(Long.parseLong(mp.get("SIZE").toString()));
            if (data != null) {
               DLFileEntry fl = this.createFile(data, mp);
               service.updateFileIds(mp.get("DID").toString(), fl.getUuid(), "" + fl.getFileEntryId(), con);
            } else {
               service.updateFileIds(mp.get("DID").toString(), "NOFILE", "0", con);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         AppDBConn.closeConnection(con);
      }

   }

   private DLFileEntry createFile(NewGenFileData data, Map<String, Object> mp) throws Exception {
      Long fid = this.defaulFolder.getFolderId();
      if (mp.get("DLFLD") != null && !mp.get("DLFLD").toString().equals("0")) {
         fid = (Long)mp.get("DLFLD");
      }

      long uid = this.defaulFolder.getUserId();
      if (mp.get("USERID") != null) {
         uid = Long.parseLong(mp.get("USERID").toString());
      }

      String var10000 = String.valueOf(mp.get("DID"));
      String tittle = var10000 + "_" + data.getFileName() + "-" + String.valueOf(mp.get("REF_NO"));
      if (tittle.length() > 252) {
         tittle = tittle.substring(0, 250);
      }

      DLFileEntry fl = null;

      try {
         fl = DLFileEntryLocalServiceUtil.addFileEntry(PortalUUIDUtil.generate(), uid, this.defaulFolder.getGroupId(), this.defaulFolder.getRepositoryId(), fid, String.valueOf(mp.get("DID")) + "_" + data.getFileName(), data.getMimetype(), tittle, String.valueOf(mp.get("DID")) + "-" + String.valueOf(mp.get("REF_NO")), String.valueOf(mp.get("REF_NO")) + "  " + data.getFileName(), (String)null, 0L, (Map)null, (File)null, data.getStream(), data.getSize(), data.getData(), (Date)null, data.getData(), this.ctx);
      } catch (DuplicateFileEntryException var9) {
         fl = DLFileEntryLocalServiceUtil.addFileEntry(PortalUUIDUtil.generate(), uid, this.defaulFolder.getGroupId(), this.defaulFolder.getRepositoryId(), fid, String.valueOf(mp.get("DID")) + "D21_" + data.getFileName(), data.getMimetype(), tittle + "_AD21", String.valueOf(mp.get("DID")) + "+_D21-" + String.valueOf(mp.get("REF_NO")), String.valueOf(mp.get("REF_NO")) + "  " + data.getFileName(), (String)null, 0L, (Map)null, (File)null, data.getStream(), data.getSize(), data.getData(), (Date)null, data.getData(), this.ctx);
      }

      data.getStream().close();
      DLFileEntryMetadataLocalServiceUtil.updateFileEntryMetadata(PortalUUIDUtil.generate(), fl.getFileEntryTypeId(), fl.getFileEntryId(), fl.getFileVersion().getFileVersionId(), (Map)null, this.ctx);
      fl.setLastPublishDate(data.getData());
      DLFileEntryLocalServiceUtil.updateDLFileEntry(fl);
      DLFileVersion dv = fl.getFileVersion();
      dv.setStatus(0);
      DLFileVersionLocalServiceUtil.updateDLFileVersion(dv);
      this.setResourcePermission(fl);
      return fl;
   }

   private void setResourcePermission(DLFileEntry journalFolder) throws PortalException {
      if (this.roleId == 0L) {
         Role userRole = RoleLocalServiceUtil.getRole(journalFolder.getCompanyId(), "User");
         this.roleId = userRole.getRoleId();
      }

      ResourcePermissionLocalServiceUtil.setResourcePermissions(journalFolder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(journalFolder.getFileEntryId()), this.roleId, new String[]{"VIEW", "DOWNLOAD"});
   }
}
