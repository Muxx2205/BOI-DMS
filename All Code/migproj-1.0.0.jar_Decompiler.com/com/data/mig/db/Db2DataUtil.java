package com.data.mig.db;

import com.data.mig.dto.TableColumns;
import com.data.mig.dto.TableMetadata;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Db2DataUtil extends DbUtil {
   private static String dBDriverClass = "com.ibm.db2.jcc.DB2Driver";
   public static Map<String, String> DTYPE_MAPPING = new HashMap();
   public static Map<String, String> COLS_MAPPING = new HashMap();
   private String jdbcUrl;
   private String userName;
   private String password;
   private String schema;

   static {
      DTYPE_MAPPING.put("CHAR FOR BIT DATA", "CHARACTER_VAL");
      DTYPE_MAPPING.put("VARCHAR FOR BIT DATA", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("VARCHAR", "STRING_VAL");
      DTYPE_MAPPING.put("BIGINT", "BIGINT_VAL");
      DTYPE_MAPPING.put("BLOB", "BYTE_VAL");
      DTYPE_MAPPING.put("TIMESTAMP", "TIMESTAMP_VAL");
      DTYPE_MAPPING.put("DECFLOAT", "FLOAT_VAL");
      DTYPE_MAPPING.put("CLOB", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("CHAR", "CHAR_VAL");
      DTYPE_MAPPING.put("INTEGER", "NUMBER_VAL");
      DTYPE_MAPPING.put("SMALLINT", "NUMBER_VAL");
      DTYPE_MAPPING.put("DOUBLE", "FLOAT_VAL");
      DTYPE_MAPPING.put("TIME", "TIME_VAL");
      DTYPE_MAPPING.put("DATE", "DATE_VAL");
      COLS_MAPPING.put("STRING_VAL", "VARCHAR");
      COLS_MAPPING.put("BIGINT_VAL", "BIGINT");
      COLS_MAPPING.put("BYTE_VAL", "BYTEA");
      COLS_MAPPING.put("TIMESTAMP_VAL", "TIMESTAMP");
      COLS_MAPPING.put("LONGTEXT_VAL", "TEXT");
      COLS_MAPPING.put("CHAR_VAL", "CHAR");
      COLS_MAPPING.put("CHARACTER_VAL", "CHARACTER");
      COLS_MAPPING.put("NUMBER_VAL", "INTEGER");
      COLS_MAPPING.put("FLOAT_VAL", "DOUBLE");
      COLS_MAPPING.put("TIME_VAL", "TIME");
      COLS_MAPPING.put("DATE_VAL", "TIMESTAMP");

      try {
         Class cl = Class.forName(dBDriverClass);
         System.out.println("postgres Class loader" + String.valueOf(cl));
      } catch (Exception e) {
         System.out.println(" Class loader Exception " + String.valueOf(e));
      }

   }

   public Db2DataUtil(String host, String port, String userName, String password, String schema) {
      this.jdbcUrl = host + port;
      this.userName = userName;
      this.password = password;
      this.schema = schema;
      this.conn = this.getConnection();
   }

   public String getCreateTableScript(TableMetadata meta) {
      return null;
   }

   public String getColType(String valType) {
      return (String)COLS_MAPPING.get(valType);
   }

   public String getValType(String colType) {
      return (String)DTYPE_MAPPING.get(colType);
   }

   public List<List<TableColumns>> selectData(int start, TableMetadata tmd) throws Exception {
      List<List<TableColumns>> lst = new ArrayList();
      Statement stm = this.conn.createStatement();
      String var10001 = tmd.getName();
      ResultSet set = stm.executeQuery("select * from " + var10001 + " limit 800 offset " + start);

      while(set.next()) {
         List<TableColumns> row = new ArrayList();

         for(TableColumns tc : tmd.getColumnsList()) {
            row.add(tc.populatVal(set));
         }

         lst.add(row);
      }

      stm.close();
      return lst;
   }

   public Connection getConnection() {
      try {
         return DriverManager.getConnection(this.jdbcUrl, this.userName, this.password);
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
         return null;
      }
   }
}
