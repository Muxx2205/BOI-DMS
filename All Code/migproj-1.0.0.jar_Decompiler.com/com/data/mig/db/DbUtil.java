package com.data.mig.db;

import com.data.mig.dto.TableColumns;
import com.data.mig.dto.TableMetadata;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class DbUtil {
   public static final String ORACLE = "ORACLE";
   public static final String DB2 = "DB2";
   public static final String MYSQL = "MYSQL";
   public static final String MSSQL = "MSSQL";
   public static final String POSTGRE = "POSTGRESQL";
   public static final int DATA_SIZE = 800;
   public String prefix;
   public String postfix = "";
   protected Connection conn;
   public ResultSet set;
   public Statement stm;

   public abstract String getCreateTableScript(TableMetadata var1);

   public String getInsertScript(TableMetadata meta) {
      StringBuffer sb = new StringBuffer();
      StringBuffer var10000 = sb.append("INSERT INTO ");
      String var10001 = this.prefix;
      var10000.append(var10001 + meta.getName() + this.postfix).append(" ( ");
      int x = 0;

      for(TableColumns tc : meta.getColumnsList()) {
         if (x > 0) {
            sb.append(", ");
         }

         sb.append(tc.getNewName());
         ++x;
      }

      sb.append(" ) VALUES (");
      x = 0;

      for(TableColumns tc : meta.getColumnsList()) {
         if (x > 0) {
            sb.append(", ");
         }

         sb.append("?");
         ++x;
      }

      sb.append(" )");
      return sb.toString();
   }

   public abstract String getColType(String var1);

   public abstract String getValType(String var1);

   public abstract Connection getConnection();

   public List<List<TableColumns>> selectData(int start, TableMetadata tmd) throws Exception {
      List<List<TableColumns>> lst = new ArrayList();
      if (this.set == null) {
         this.stm = this.conn.createStatement(1003, 1007);
         this.stm.setFetchSize(60000);
         this.set = this.stm.executeQuery("select * from " + tmd.getName());
      }

      for(int xx = 0; xx < 800 && this.set.next(); ++xx) {
         List<TableColumns> row = new ArrayList();

         for(TableColumns tc : tmd.getColumnsList()) {
            row.add(tc.populatVal(this.set));
         }

         lst.add(row);
      }

      return lst;
   }

   public TableMetadata getTableMetadata(String tname) throws Exception {
      TableMetadata tmt = new TableMetadata();
      tmt.setName(tname);

      try {
         try {
            DatabaseMetaData dmd = this.conn.getMetaData();
            ResultSet set = dmd.getPrimaryKeys((String)null, (String)null, tname);
            String keys = null;

            while(set.next()) {
               if (keys == null) {
                  keys = set.getString(4);
               } else {
                  keys = keys + ", " + set.getString(4);
               }
            }

            tmt.setPrimaryKeys(keys);
            set = dmd.getColumns((String)null, (String)null, tname, (String)null);
            TableColumns col = null;
            tmt.setColumnsList(new ArrayList());

            while(set.next()) {
               String tr = set.getString("COLUMN_NAME").trim().toUpperCase();
               col = new TableColumns();
               col.setName(tr);
               if (tr.equalsIgnoreCase("COMMENT")) {
                  col.setNewName("COMMENTS");
               }

               col.setSize(set.getInt("COLUMN_SIZE"));
               tr = set.getString("TYPE_NAME").toUpperCase();
               if (tr.indexOf("IDENTITY") > 0) {
                  tr = tr.replace("IDENTITY", "").trim();
               }

               col.setSourceType(tr);
               col.setValType(this.getValType(tr));
               PrintStream var10000 = System.out;
               String var10001 = col.getSourceType();
               var10000.println(var10001 + " ************ >>>>" + col.getValType());
               col.setScale(set.getInt("COLUMN_SIZE"));
               if (tr.length() >= 1 && !tr.contentEquals("DISTINCT")) {
                  tmt.getColumnsList().add(col);
               } else {
                  System.out.println(tr + " ************ Coulmns found in table >>>>" + tname);
               }
            }
         } catch (Exception e) {
            System.out.println("Promary key not found " + tname + " !!  Exceptions   >>>" + String.valueOf(e));
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(tname + "    >>    " + String.valueOf(e));
      }

      return tmt;
   }

   public void inserRecords(List<List<TableColumns>> data, TableMetadata tmd) throws Exception {
      PreparedStatement pst = this.conn.prepareStatement(this.getInsertScript(tmd));

      try {
         for(List<TableColumns> lst : data) {
            int index = 1;

            for(TableColumns tc : lst) {
               tc.setQueryValue(pst, index);
               ++index;
            }

            pst.addBatch();
         }

         pst.executeBatch();
      } catch (Exception e) {
         System.out.println("Find in nsted query execution >> " + String.valueOf(e));
      }

      pst.close();
   }

   public long getDataCount(TableMetadata tmd) {
      long cnt = 0L;

      try {
         PreparedStatement pst = this.conn.prepareStatement("select count(*) cnt from " + tmd.getName());
         ResultSet set = pst.executeQuery();
         if (set.next()) {
            cnt = set.getLong(1);
         }

         pst.close();
      } catch (Exception e) {
         System.out.println("Exception in getDataCount method >> " + String.valueOf(e));
      }

      return cnt;
   }

   public List<String> getAllTableName() throws Exception {
      List<String> list = new ArrayList();

      try {
         DatabaseMetaData dmd = this.conn.getMetaData();

         String tname;
         for(ResultSet set = dmd.getTables((String)null, (String)null, (String)null, new String[]{"TABLE"}); set.next(); list.add(tname)) {
            tname = set.getString(3).toUpperCase();
            if (this.prefix != null && tname.startsWith(this.prefix)) {
               tname = tname.replace(this.prefix, "");
            }
         }

         return list;
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   public boolean createTable(TableMetadata table) throws Exception {
      boolean flag = true;
      Statement stm = this.conn.createStatement();

      try {
         stm.execute(this.getCreateTableScript(table));
      } catch (Exception e) {
         System.out.println("createTable exception >> " + String.valueOf(e));
         flag = false;
      }

      stm.close();
      return flag;
   }

   public static DbUtil getInstance(String db, String host, String port, String user, String pass, String schema) {
      if ("ORACLE".contentEquals(db)) {
         return new OracleDBUtil(host, port, user, pass, schema);
      } else if ("DB2".contentEquals(db)) {
         return new Db2DataUtil(host, port, user, pass, schema);
      } else if ("MYSQL".contentEquals(db)) {
         return new MySqlDBUtil(host, port, user, pass, schema);
      } else if ("MSSQL".contentEquals(db)) {
         return new MsSqlDbUtil(host, port, user, pass, schema);
      } else {
         return "POSTGRESQL".contentEquals(db) ? new PostgreDbUtil(host, port, user, pass, schema) : null;
      }
   }

   public void closeConnection() {
      try {
         if (this.conn != null) {
            this.conn.close();
         }
      } catch (Exception ex) {
         System.out.println("close connection " + String.valueOf(ex));
      }

   }
}
