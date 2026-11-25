package com.vel.util;

import com.data.mig.db.AppDBConn;
import com.data.mig.util.FileStorageUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.service.ServiceContext;
import com.newgen.dao.NewGenDbService;
import com.vel.dto.NewGenFileData;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class FileDumpUtil extends Thread {
   int dataclass;
   int userId;
   ServiceContext ctx;
   String fromDate;
   String toDate;
   long roleId;
   String rootPath;
   public static boolean startFlag = true;
   private long defaultFolderId = 4806995L;
   String flag;
   private FileStorageUtil fileUtil;

   public FileDumpUtil(int dataclass, String fromDate, String toDate, int userId, String rootPath, ServiceContext ctx) {
      this.dataclass = dataclass;
      this.ctx = ctx;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.userId = userId;
      this.rootPath = rootPath;
      this.fileUtil = new FileStorageUtil(rootPath);
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
         List<Map<String, Object>> list = null;

         for(Map<String, Object> mp : service.getFilesFromDataClassDir(this.dataclass, this.fromDate, this.toDate, con)) {
            if (!startFlag) {
               break;
            }

            NewGenFileData data = service.callApi(mp.get("DID").toString(), this.userId);
            if (data != null) {
               this.createFile(data, mp);
               service.updateFileIds(mp.get("DID").toString(), "", "1", data.getFileName(), data.getMimetype(), con);
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

   private void createFile(NewGenFileData data, Map<String, Object> mp) throws Exception {
      this.fileUtil.saveFile("" + this.dataclass, mp.get("DID").toString(), data.getBytes());
   }
}
