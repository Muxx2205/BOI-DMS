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

public class PostgreDbUtil extends DbUtil {
   private static String dBDriverClass = "org.postgresql.Driver";
   public static Map<String, String> DTYPE_MAPPING = new HashMap();
   public static Map<String, String> COLS_MAPPING = new HashMap();
   private String jdbcUrl = "jdbc:postgresql://";
   private String userName;
   private String password;
   private String schema;

   static {
      DTYPE_MAPPING.put("INTEGER", "NUMBER_VAL");
      DTYPE_MAPPING.put("INT", "NUMBER_VAL");
      DTYPE_MAPPING.put("INT4", "NUMBER_VAL");
      DTYPE_MAPPING.put("INT2", "SNUMBER_VAL");
      DTYPE_MAPPING.put("INT8", "NUMBER_VAL");
      DTYPE_MAPPING.put("SMALLINT", "SNUMBER_VAL");
      DTYPE_MAPPING.put("TINYINT", "SNUMBER_VAL");
      DTYPE_MAPPING.put("NUMERIC", "NUMBER_VAL");
      DTYPE_MAPPING.put("SERIAL", "BIGINT_VAL");
      DTYPE_MAPPING.put("BIGSERIAL", "BIGINT_VAL");
      DTYPE_MAPPING.put("DOUBLE", "FLOAT_VAL");
      DTYPE_MAPPING.put("FLOAT8", "FLOAT_VAL");
      DTYPE_MAPPING.put("FLOAT", "FLOAT_VAL");
      DTYPE_MAPPING.put("DECIMAL", "FLOAT_VAL");
      DTYPE_MAPPING.put("REAL", "FLOAT_VAL");
      DTYPE_MAPPING.put("VARCHAR", "STRING_VAL");
      DTYPE_MAPPING.put("CHAR", "CHAR_VAL");
      DTYPE_MAPPING.put("TEXT", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("MEDIUMTEXT", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("LONGTEXT", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("LONGBLOB", "BIGBYTE_VAL");
      DTYPE_MAPPING.put("BYTEA", "BYTE_VAL");
      DTYPE_MAPPING.put("CLOB", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("TIMESTAMP", "TIMESTAMP_VAL");
      DTYPE_MAPPING.put("TIME", "TIME_VAL");
      DTYPE_MAPPING.put("DATE", "DATE_VAL");
      DTYPE_MAPPING.put("DATETIME", "TIMESTAMP_VAL");
      COLS_MAPPING.put("STRING_VAL", "VARCHAR");
      COLS_MAPPING.put("BIGINT_VAL", "BIGINT");
      COLS_MAPPING.put("BYTE_VAL", "BYTEA");
      COLS_MAPPING.put("BIGBYTE_VAL", "BYTEA");
      COLS_MAPPING.put("TIMESTAMP_VAL", "TIMESTAMP");
      COLS_MAPPING.put("LONGTEXT_VAL", "TEXT");
      COLS_MAPPING.put("CHAR_VAL", "CHAR");
      COLS_MAPPING.put("CHARACTER_VAL", "VARCHAR");
      COLS_MAPPING.put("NUMBER_VAL", "INTEGER");
      COLS_MAPPING.put("SNUMBER_VAL", "SMALLINT");
      COLS_MAPPING.put("FLOAT_VAL", "DOUBLE");
      COLS_MAPPING.put("TIME_VAL", "TIME");
      COLS_MAPPING.put("DATE_VAL", "DATE");

      try {
         Class cl = Class.forName(dBDriverClass);
         System.out.println("postgres Class loader" + String.valueOf(cl));
      } catch (Exception e) {
         System.out.println(" Class loader Exception " + String.valueOf(e));
      }

   }

   public PostgreDbUtil(String host, String port, String userName, String password, String schema) {
      this.jdbcUrl = this.jdbcUrl + host + ":" + port + "/" + schema;
      this.userName = userName;
      this.password = password;
      this.schema = schema;
      this.conn = this.getConnection();
   }

   public String getCreateTableScript(TableMetadata meta) {
      StringBuffer sb = new StringBuffer();
      sb.append("CREATE TABLE ").append(meta.getName()).append(" ( ");
      int x = 0;

      for(TableColumns tc : meta.getColumnsList()) {
         if (x > 0) {
            sb.append(", ");
         }

         sb.append(this.getCollsDef(tc));
         ++x;
      }

      meta.getPrimaryKeys();
      sb.append(" )");
      System.out.println(" Table Query >> " + String.valueOf(sb));
      return sb.toString();
   }

   private String getCollsDef(TableColumns tc) {
      String st = (String)COLS_MAPPING.get(tc.getValType());
      if (st.contentEquals("VARCHAR")) {
         return tc.getNewName() + " " + st + " (" + tc.getSize() + ") ";
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

   public List<String> getAllTableName() throws Exception {
      List<String> list = new ArrayList();

      try {
         String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
         System.out.println(query);
         Statement stmt = this.conn.createStatement();
         ResultSet set = stmt.executeQuery(query);

         while(set.next()) {
            String tname = set.getString("table_name");
            list.add(tname);
         }

         return list;
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
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
