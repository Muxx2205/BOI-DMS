package com.data.mig.util;

import com.data.mig.db.AppDBConn;
import com.data.mig.db.DbUtil;
import com.data.mig.dto.TableColumns;
import com.data.mig.dto.TableMetadata;
import com.vel.dto.DBRequest;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppDataUtil extends Thread {
   static int DATA_SIZE = 800;
   private String dbname;
   public static int numflag = 0;
   private DBRequest req;
   private String sourceDB;
   private String targetDB;
   private DbUtil sourceUtil;
   private DbUtil targetUtil;
   static List<String> rclist = new ArrayList();

   public AppDataUtil(DBRequest user) {
      this.req = user;
      this.sourceDB = user.getSourceDb();
      this.targetDB = user.getTargetDb();
      this.sourceUtil = DbUtil.getInstance(this.sourceDB, user.getSourceHost(), user.getSourcePort(), user.getSourceUser(), user.getSourcePassword(), user.getSourceSchema());
      this.targetUtil = DbUtil.getInstance(this.targetDB, user.getTargetHost(), user.getTargetPort(), user.getTargetUser(), user.getTargetPassword(), user.getTargetSchema());
      this.targetUtil.prefix = user.getPrefix();
   }

   public void run() {
      numflag = 0;

      try {
         List<String> tList = this.req.getTableNames();
         if (tList == null || tList.isEmpty()) {
            tList = this.sourceUtil.getAllTableName();
         }

         List<String> aList = this.targetUtil.getAllTableName();
         tList.removeAll(aList);

         for(String tname : tList) {
            TableMetadata tmd = this.sourceUtil.getTableMetadata(tname);
            this.targetUtil.createTable(tmd);
            int rows = 0;
            boolean flag = true;
            long cnt = this.sourceUtil.getDataCount(tmd);
            System.out.println(tname + " records count >>  " + cnt);

            while(flag && numflag < 1) {
               List<List<TableColumns>> data = this.sourceUtil.selectData(rows, tmd);
               if (data.size() > 0) {
                  try {
                     this.targetUtil.inserRecords(data, tmd);
                  } catch (SQLException e) {
                     System.out.println(tname + "    " + String.valueOf(e) + "  " + e.getErrorCode());
                     throw e;
                  }
               }

               flag = data.size() == DATA_SIZE;
               rows += data.size();
            }

            System.out.println(tname + " >>>>>>>>>>>>> Table " + rows + " rows imported..  ");
            numflag = 0;
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      } finally {
         this.targetUtil.closeConnection();
         this.sourceUtil.closeConnection();
      }

   }

   public static void main(String[] args) throws Exception {
      Connection con = AppDBConn.getTargetConnection("reports1");
      DatabaseMetaData dmd = con.getMetaData();
      ResultSet set = dmd.getTables((String)null, (String)null, (String)null, new String[]{"TABLE"});

      while(set.next()) {
         PrintStream var10000 = System.out;
         String var10001 = set.getString(1);
         var10000.println(var10001 + "\t" + set.getString(2) + "\t" + set.getString(3) + "\t" + set.getString(4));
      }

      con.close();
   }
}
