package com.vel.util;

import com.data.mig.db.AppDBConn;
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
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MscFileMigrationUtil extends Thread {
   ServiceContext ctx;
   String fromDate;
   String toDate;
   long roleId;
   int userId;
   public static boolean startFlag = true;
   private long defaultFolderId = 4806995L;
   private Map<Long, DLFolder> folderMap = new HashMap();

   public MscFileMigrationUtil(String fromDate, String toDate, int userId, ServiceContext ctx) {
      this.ctx = ctx;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.userId = userId;
   }

   public void run() {
      this.startMig();
      startFlag = true;
   }

   public void startMig() {
      Connection con = null;
      NewGenDbService service = new NewGenDbService();

      try {
         con = DataAccess.getConnection();

         for(Map<String, Object> mp : service.getFilesDir(this.fromDate, this.toDate, con)) {
            if (!startFlag) {
               break;
            }

            NewGenFileData data = service.callApi(mp.get("DID").toString(), this.userId);
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
      Long fid = (Long)mp.get("DLFLD");
      DLFolder pf = (DLFolder)this.folderMap.get(fid);
      if (pf == null) {
         pf = DLFolderLocalServiceUtil.getDLFolder(fid);
         this.folderMap.put(fid, pf);
      }

      long uid = pf.getUserId();
      if (mp.get("USERID") != null) {
         uid = Long.parseLong(mp.get("USERID").toString());
      }

      DLFileEntry fl = DLFileEntryLocalServiceUtil.addFileEntry(PortalUUIDUtil.generate(), uid, pf.getGroupId(), pf.getRepositoryId(), pf.getFolderId(), String.valueOf(mp.get("DID")) + "_" + data.getFileName(), data.getMimetype(), String.valueOf(mp.get("DID")) + "_" + data.getFileName(), String.valueOf(mp.get("DID")) + "_" + data.getFileName(), String.valueOf(mp.get("DID")) + "_" + data.getFileName(), (String)null, 0L, (Map)null, (File)null, data.getStream(), data.getSize(), data.getData(), (Date)null, data.getData(), this.ctx);
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

      ResourcePermissionLocalServiceUtil.setResourcePermissions(journalFolder.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(journalFolder.getFileEntryId()), this.roleId, new String[]{"VIEW"});
   }
}
