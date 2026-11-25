package com.data.mig.db;

import com.data.mig.dto.TableColumns;
import com.data.mig.dto.TableMetadata;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestConnection {
   static int numflag = 0;
   static int DATA_SIZE = 800;
   static String[] tabs = new String[]{"OS_MASTER_BRANCH", "MASTER_BRANCH_BOIDMS", "OS_MASTER_USER", "PDBDATADEFINITION", "PDBDATAFIELDSTABLE", "PDBDATAFIELDVALIDATION", "PDBDOCUMENT", "PDBDOCUMENTCONTENT", "PDBDOCUMENTNAME", "PDBDOCUMENTVERSION", "PDBFOLDER", "PDBFOLDERCONTENT", "PDBFOLDERNAME", "PDBGLOBALINDEX", "PDBGROUP", "PDBGROUPMEMBER", "PDBGROUPROLES", "PDBUSER"};

   public static void main(String[] args) throws Exception {
      DbUtil ora = DbUtil.getInstance("ORACLE", "//rac-biocorp-scan.boicorp.net", "1532", "BOIDMSPROD", "Veloc#sAug#2025", "boidms");
      DbUtil ms = DbUtil.getInstance("MSSQL", "172.2.46.201", "1433", "newdms", "Newdms@123", "boidms");
      run(ms, ora);
   }

   public static void run(DbUtil sourceUtil, DbUtil targetUtil) {
      numflag = 0;

      try {
         targetUtil.prefix = "XX_";
         List<String> tList = null;
         if (tabs != null) {
            tList = new ArrayList();

            for(int x = 1; x < 162; ++x) {
               tList.add("DDT_" + x);
            }

            String[] var6;
            for(String t : var6 = tabs) {
               tList.add(t);
            }
         }

         if (tList == null) {
            tList = sourceUtil.getAllTableName();
         }

         List<String> aList = targetUtil.getAllTableName();
         tList.removeAll(aList);
         int x = 0;

         for(String tname : tList) {
            ++x;
            if (!tname.toUpperCase().contains("_TEST") && !tname.toUpperCase().contains("_NONAD") && x > -1) {
               System.out.println(tname + " Table Started..  " + x);
               TableMetadata tmd = sourceUtil.getTableMetadata(tname);
               if (targetUtil.createTable(tmd)) {
                  int rows = 0;
                  boolean flag = true;
                  long cnt = sourceUtil.getDataCount(tmd);
                  System.out.println(tname + " records count >>  " + cnt);

                  while(flag && numflag < 1) {
                     List<List<TableColumns>> data = sourceUtil.selectData(rows, tmd);
                     if (data.size() > 0) {
                        try {
                           targetUtil.inserRecords(data, tmd);
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
                  sourceUtil.set.close();
                  sourceUtil.set = null;
                  sourceUtil.stm.close();
                  sourceUtil.stm = null;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      } finally {
         targetUtil.closeConnection();
         sourceUtil.closeConnection();
      }

   }
}
