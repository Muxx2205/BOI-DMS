package com.vel.util;

import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CleanDocumentUtil extends Thread {
   String datadefid;

   public CleanDocumentUtil(String datadefid) {
      this.datadefid = datadefid;
   }

   public void run() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();

         for(Map<String, Long> mp : this.getDocEntries(conn)) {
            if ((Long)mp.get("fid") > 0L) {
               DLFileEntryLocalServiceUtil.deleteFileEntry((Long)mp.get("fid"));
            }

            this.updateData(mp, conn);
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

      System.out.println("Document Cleaning finish for " + this.datadefid);
   }

   private void updateData(Map<String, Long> mp, Connection con) throws Exception {
      Statement stm = con.createStatement();
      stm.executeUpdate("update zz_pdbdocument set fid=null, uuid=null where documentindex=" + String.valueOf(mp.get("did")));
      stm.close();
   }

   private List<Map<String, Long>> getDocEntries(Connection con) throws Exception {
      Statement stm = con.createStatement();
      ResultSet set = stm.executeQuery("select d.documentindex,d.fid from zz_pdbdocument d where d.fid is not null and d.datadefinitionindex=" + this.datadefid);
      Map<String, Long> mp = null;
      List<Map<String, Long>> list = new ArrayList();

      while(set.next()) {
         mp = new HashMap();
         mp.put("did", set.getLong(1));
         mp.put("fid", set.getLong(2));
         list.add(mp);
      }

      stm.close();
      return list;
   }
}
