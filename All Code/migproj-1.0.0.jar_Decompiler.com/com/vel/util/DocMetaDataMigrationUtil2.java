package com.vel.util;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.service.ServiceContext;
import com.vel.dto.MetaMappingDto;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocMetaDataMigrationUtil2 extends Thread {
   private static int datasize = 500;
   private static String docsKeyName = "migrateDataKey";
   long dataClassId;
   long liferayObjectId;
   long companyId;
   long adminUserId;
   String fldMapping;
   String date;
   String dirFlag;
   ServiceContext ctx;
   List<MetaMappingDto> colList;
   private String dbselectquery;

   public DocMetaDataMigrationUtil2(long dataClassId, long liferayObjectId, long companyId, long adminUserId, String fldMapping, String date, ServiceContext ctx) {
      this.dataClassId = dataClassId;
      this.liferayObjectId = liferayObjectId;
      this.companyId = companyId;
      this.adminUserId = adminUserId;
      this.fldMapping = fldMapping;
      this.date = date;
      this.ctx = ctx;
      this.parseMapping();
   }

   public DocMetaDataMigrationUtil2(long dataClassId, long liferayObjectId, long companyId, long adminUserId, String fldMapping, String date, String flag, ServiceContext ctx) {
      this.dataClassId = dataClassId;
      this.liferayObjectId = liferayObjectId;
      this.companyId = companyId;
      this.adminUserId = adminUserId;
      this.fldMapping = fldMapping;
      this.date = date;
      this.ctx = ctx;
      this.dirFlag = flag;
      this.parseMapping();
   }

   private void parseMapping() {
      this.colList = new ArrayList();
      String[] arr = this.fldMapping.split(";");
      MetaMappingDto dto = null;

      for(String str : arr) {
         if (str.contains("=")) {
            dto = new MetaMappingDto();
            dto.setObjectFld(str.split("=")[0].trim());
            dto.setDataClassFld(str.split("=")[1].toUpperCase().trim());
            this.colList.add(dto);
         }
      }

   }

   private void queryBuild() {
      this.dbselectquery = "SELECT ";

      for(MetaMappingDto dto : this.colList) {
         String var10001 = this.dbselectquery;
         this.dbselectquery = var10001 + " d." + dto.getColumnName() + ",";
      }

      if (this.dirFlag == null) {
         this.dbselectquery = this.dbselectquery + " u.userid ,d.FOLDDOCINDEX,f.CREATEDDATETIME  FROM zz_ddt_" + this.dataClassId + " d  inner join zz_pdbfolder f on d.FOLDDOCINDEX=f.FOLDERINDEX and f.createddatetime>to_date('" + this.date + "','YYYY-MM-DD')  left join zz_pdbuser u on f.owner=u.userindex where d.objectid is null and d.FOLDDOCFLAG='F' ORDER BY d.folddocindex DESC";
      } else {
         this.dbselectquery = this.dbselectquery + " u.userid ,d.FOLDDOCINDEX,f.CREATEDDATETIME  FROM zz_ddt_" + this.dataClassId + " d  inner join zz_pdbdocument f on d.FOLDDOCINDEX=f.documentindex and f.createddatetime>to_date('" + this.date + "','YYYY-MM-DD')  left join zz_pdbuser u on f.owner=u.userindex where d.objectid is null and d.FOLDDOCFLAG='D' ORDER BY d.folddocindex DESC";
      }

      System.out.println("dbselectquery >>  " + this.dbselectquery);
   }

   private void getGlobalIndexes(Connection con) throws Exception {
      Statement stm = con.createStatement();
      ResultSet set = stm.executeQuery("SELECT d.DATAFIELDINDEX,c.DATAFIELDNAME,c.DATAFIELDTYPE FROM \t\tzz_pdbdatafieldstable d, zz_pdbglobalindex c \t\twhere c.DATAFIELDINDEX=d.DATAFIELDINDEX and d.DATADEFINDEX=" + this.dataClassId);
      Map<String, MetaMappingDto> mp = new HashMap();
      MetaMappingDto a = null;

      while(set.next()) {
         a = new MetaMappingDto();
         a.setColumnName("FIELD_" + set.getString(1));
         a.setDataClassFld(set.getString(2).toUpperCase().trim());
         a.setDataType(set.getString(3));
         mp.put(a.getDataClassFld(), a);
      }

      stm.close();

      for(MetaMappingDto b : this.colList) {
         if (mp.get(b.getDataClassFld()) == null) {
            throw new Exception(b.getDataClassFld() + " mapping not exist in Global Index ...");
         }

         a = (MetaMappingDto)mp.get(b.getDataClassFld());
         b.setDataType(a.getDataType());
         b.setColumnName(a.getColumnName());
      }

      this.queryBuild();
   }

   public void run() {
      this.startMig();
   }

   private void startMig() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();
         this.getGlobalIndexes(conn);
         int offset = 0;

         for(List<ValueDto> list = this.getListRecords(offset, conn); list.size() > 0; list = this.getListRecords(offset, conn)) {
            for(ValueDto dto : list) {
               this.saveObject(dto, this.getDocs(dto.folderIndex, conn));
               this.updateDataClass(dto, conn);
            }

            if (list.size() < datasize) {
               break;
            }

            offset += datasize;
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

   }

   private void updateDataClass(ValueDto dto, Connection conn) throws Exception {
      if (this.dirFlag == null) {
         Statement stm = conn.createStatement();
         long var10001 = this.dataClassId;
         stm.executeUpdate("update zz_ddt_" + var10001 + " set objectid=" + String.valueOf(dto.objectEntryId) + " where FOLDDOCFLAG='F' and folddocindex=" + String.valueOf(dto.folderIndex));
         stm.close();

         for(Map<String, String> m : dto.docs) {
            stm = conn.createStatement();
            var10001 = this.dataClassId;
            stm.executeUpdate("update zz_ddt_" + var10001 + " set objectid=" + String.valueOf(dto.objectEntryId) + " where FOLDDOCFLAG='D' and folddocindex=" + (String)m.get("fid"));
            stm.close();
         }
      } else {
         Statement stm = conn.createStatement();
         long var9 = this.dataClassId;
         stm.executeUpdate("update zz_ddt_" + var9 + " set objectid=" + String.valueOf(dto.objectEntryId) + " where FOLDDOCFLAG='D' and folddocindex=" + String.valueOf(dto.folderIndex));
         stm.close();
      }

   }

   private void saveObject(ValueDto dto, List<Map<String, String>> docs) throws Exception {
      StringBuffer sb = new StringBuffer("[");
      int x = 0;
      dto.docs = docs;

      for(Map<String, String> m : docs) {
         if (x > 0) {
            sb.append(",");
         }

         Set<String> keys = m.keySet();
         sb.append("{");
         int y = 0;

         for(String key : keys) {
            String val = (String)m.get(key);
            if (val == null) {
               val = "0";
            }

            if (y > 0) {
               sb.append(",");
            }

            sb.append("\"").append(key).append("\":\"").append(val).append("\"");
            ++y;
         }

         sb.append("}");
         ++x;
      }

      sb.append("]");
      dto.values.put(docsKeyName, sb.toString());
      new Date();
      ServiceContext serviceContext = new ServiceContext();
      serviceContext.setCompanyId(this.companyId);
      serviceContext.setUserId(dto.userId);
      serviceContext.setWorkflowAction(1);
      serviceContext.setCreateDate(dto.date);
      new Date();
   }

   private List<Map<String, String>> getDocs(Long folderIndex, Connection conn) {
      List<Map<String, String>> list = new ArrayList();
      Map<String, String> mp = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = null;
         if (this.dirFlag == null) {
            long var10001 = this.dataClassId;
            set = stm.executeQuery("select d.uuid,d.fid,d.name,c.DOCUMENTORDERNO, d.NOOFPAGES from zz_pdbdocument d, zz_pdbdocumentcontent c where   d.DOCUMENTINDEX=c.DOCUMENTINDEX and d.DATADEFINITIONINDEX=" + var10001 + " and c.PARENTFOLDERINDEX= " + String.valueOf(folderIndex));
         } else {
            set = stm.executeQuery("select d.uuid,d.fid,d.name,c.DOCUMENTORDERNO, d.NOOFPAGES from zz_pdbdocument d  left join  zz_pdbdocumentcontent c on  (d.DOCUMENTINDEX=c.DOCUMENTINDEX ) where    d.DOCUMENTINDEX=" + String.valueOf(folderIndex));
         }

         while(set.next()) {
            mp = new HashMap();
            mp.put("uuid", set.getString(1));
            mp.put("fid", set.getString(2));
            mp.put("fname", set.getString(3));
            mp.put("orderno", set.getString(4));
            mp.put("pages", set.getString(5));
            list.add(mp);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   private List<ValueDto> getListRecords(int start, Connection conn) {
      List<ValueDto> list = new ArrayList();
      ValueDto mp = null;

      try {
         Statement stm = conn.createStatement();

         for(ResultSet set = stm.executeQuery(this.dbselectquery + " offset " + start + " ROWS FETCH NEXT " + datasize + " ROWS only"); set.next(); list.add(mp)) {
            mp = new ValueDto();

            for(MetaMappingDto m : this.colList) {
               if (m.getDataType().equals("I")) {
                  mp.values.put(m.getObjectFld(), set.getLong(m.getColumnName()));
               } else if (m.getDataType().equals("D")) {
                  mp.values.put(m.getObjectFld(), set.getDate(m.getColumnName()));
               } else {
                  mp.values.put(m.getObjectFld(), set.getString(m.getColumnName()));
               }
            }

            mp.folderIndex = set.getLong("FOLDDOCINDEX");
            mp.userId = set.getLong("userid");
            mp.date = set.getDate("CREATEDDATETIME");
            if (mp.userId == null || mp.userId == 0L) {
               mp.userId = this.adminUserId;
               PrintStream var10000 = System.out;
               String var10001 = String.valueOf(mp.folderIndex);
               var10000.println(var10001 + " Owner id not found of records >>>>" + String.valueOf(mp.folderIndex));
            }
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }
}
