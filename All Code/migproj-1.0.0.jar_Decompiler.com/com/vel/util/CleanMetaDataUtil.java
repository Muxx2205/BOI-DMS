package com.vel.util;

import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CleanMetaDataUtil extends Thread {
   String datadefid;

   public CleanMetaDataUtil(String datadefid) {
      this.datadefid = datadefid;
   }

   public void run() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();

         for(Map<String, Long> mp : this.getDocEntries(conn)) {
            if ((Long)mp.get("oid") > 0L) {
               ObjectEntryLocalServiceUtil.deleteObjectEntry((Long)mp.get("oid"));
            }

            this.updateData(mp, conn);
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

      System.out.println("Metadata Cleaning finish for " + this.datadefid);
   }

   private void updateData(Map<String, Long> mp, Connection con) throws Exception {
      Statement stm = con.createStatement();
      String var10001 = this.datadefid;
      stm.executeUpdate("update zz_ddt_" + var10001 + " set objectid=null where folddocflag='F' and  folddocindex=" + String.valueOf(mp.get("did")));
      stm.close();
   }

   private List<Map<String, Long>> getDocEntries(Connection con) throws Exception {
      Statement stm = con.createStatement();
      ResultSet set = stm.executeQuery("select d.folddocindex,d.objectid from zz_ddt_" + this.datadefid + " d where d.objectid is not null and d.folddocflag='F'");
      Map<String, Long> mp = null;
      List<Map<String, Long>> list = new ArrayList();

      while(set.next()) {
         mp = new HashMap();
         mp.put("did", set.getLong(1));
         mp.put("oid", set.getLong(2));
         list.add(mp);
      }

      stm.close();
      return list;
   }
}
