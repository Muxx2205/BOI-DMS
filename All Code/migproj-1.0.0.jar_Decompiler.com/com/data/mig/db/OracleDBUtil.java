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

public class OracleDBUtil extends DbUtil {
   private static String dBDriverClass = "oracle.jdbc.OracleDriver";
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
      DTYPE_MAPPING.put("NUMBER", "BIGINT_VAL");
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
      COLS_MAPPING.put("BLOB", "BLOB_VAL");
      COLS_MAPPING.put("STRING_VAL", "VARCHAR2");
      COLS_MAPPING.put("BIGINT_VAL", "NUMBER");
      COLS_MAPPING.put("BIT_VAL", "NUMBER");
      COLS_MAPPING.put("BYTE_VAL", "BYTEA");
      COLS_MAPPING.put("TIMESTAMP_VAL", "TIMESTAMP");
      COLS_MAPPING.put("LONGTEXT_VAL", "NCLOB");
      COLS_MAPPING.put("CHAR_VAL", "CHAR");
      COLS_MAPPING.put("CHARACTER_VAL", "CHARACTER");
      COLS_MAPPING.put("NUMBER_VAL", "INTEGER");
      COLS_MAPPING.put("FLOAT_VAL", "DOUBLE");
      COLS_MAPPING.put("TIME_VAL", "TIME");
      COLS_MAPPING.put("DATE_VAL", "TIMESTAMP");
      COLS_MAPPING.put("BLOB_VAL", "BLOB");

      try {
         Class cl = Class.forName(dBDriverClass);
         System.out.println("postgres Class loader" + String.valueOf(cl));
      } catch (Exception e) {
         System.out.println(" Class loader Exception " + String.valueOf(e));
      }

   }

   public OracleDBUtil(String host, String port, String userName, String password, String schema) {
      this.jdbcUrl = "jdbc:oracle:thin:@" + host + ":" + port + "/" + schema;
      this.userName = userName;
      this.password = password;
      this.schema = schema;
      this.conn = this.getConnection();
   }

   public String getCreateTableScript(TableMetadata meta) {
      StringBuffer sb = new StringBuffer();
      StringBuffer var10000 = sb.append("CREATE TABLE ");
      String var10001 = this.prefix;
      var10000.append(var10001 + meta.getName() + this.postfix).append(" ( ");
      int x = 0;

      for(TableColumns tc : meta.getColumnsList()) {
         if (x > 0) {
            sb.append(", ");
         }

         sb.append(this.getCollsDef(tc));
         ++x;
      }

      if (meta.getPrimaryKeys() != null) {
         sb.append(", PRIMARY KEY (").append(meta.getPrimaryKeys()).append(") ");
      }

      sb.append(" )");
      System.out.println(sb);
      return sb.toString();
   }

   private String getCollsDef(TableColumns tc) {
      String st = (String)COLS_MAPPING.get(tc.getValType());
      if (st.startsWith("VARCHAR")) {
         if (tc.getSize() > 4000) {
            return tc.getNewName() + " NCLOB ";
         } else {
            String var4 = tc.getNewName();
            return var4 + " " + st + " (" + (tc.getSize() + 100) + ") ";
         }
      } else {
         String typ = st;
         if (st.contentEquals("DOUBLE")) {
            typ = st + " PRECISION";
         } else if (st.contentEquals("CHARACTER")) {
            typ = st + " VARYING";
         }

         String var10000 = tc.getNewName();
         return var10000 + " " + typ + " ";
      }
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
