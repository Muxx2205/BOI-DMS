package com.data.mig.db;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestConnectionM {
   static int numflag = 0;
   static int DATA_SIZE = 800;
   static String[] tabs = new String[]{"MASTER_BRANCH", "MASTER_BRANCH_BOIDMS", "MASTER_USER", "PDBDATADEFINITION", "PDBDATAFIELDSTABLE", "PDBDATAFIELDVALIDATION", "PDBDOCUMENT", "PDBDOCUMENTCONTENT", "PDBDOCUMENTNAME", "PDBDOCUMENTVERSION", "PDBFOLDER", "PDBFOLDERCONTENT", "PDBFOLDERNAME", "PDBGLOBALINDEX", "PDBGROUP", "PDBGROUPMEMBER", "PDBGROUPROLES", "PDBUSER"};

   public static void main(String[] args) throws Exception {
      DbUtil ora = DbUtil.getInstance("ORACLE", "//rac-biocorp-scan.boicorp.net", "1532", "BOIDMSPROD", "Veloc#sAug#2025", "boidms");
      run(ora);
   }

   public static void run(DbUtil targetUtil) {
      numflag = 0;

      try {
         targetUtil.prefix = "XX_";
         List<String> tList = null;
         if (tabs != null) {
            tList = new ArrayList();

            for(int x = 1; x < 162; ++x) {
               tList.add("DDT_" + x);
            }
         }

         int x = 0;

         for(String tname : tList) {
            ++x;
            Statement stm = targetUtil.conn.createStatement();

            try {
               stm.execute("ALTER TABLE XX_" + tname + " add objectid integer");
               stm.execute("create index indx_" + tname + "_fdflg on XX_" + tname + "(folddocflag)");
               stm.execute("create index indx_" + tname + "_objectid on XX_" + tname + "(objectid)");
               System.out.println("Index created on zz_" + tname);
            } catch (Exception e) {
               System.out.println("Exception on zz_" + tname + " >>   " + String.valueOf(e));
            }

            stm.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      } finally {
         targetUtil.closeConnection();
      }

   }
}
