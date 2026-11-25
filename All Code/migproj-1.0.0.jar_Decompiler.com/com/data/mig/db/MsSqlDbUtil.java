package com.data.mig.db;

import com.data.mig.dto.TableColumns;
import com.data.mig.dto.TableMetadata;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class MsSqlDbUtil extends DbUtil {
   private static String dBDriverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
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
      DTYPE_MAPPING.put("NVARCHAR", "STRING_VAL");
      DTYPE_MAPPING.put("NTEXT", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("TEXT", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("BIGINT", "BIGINT_VAL");
      DTYPE_MAPPING.put("BLOB", "BYTE_VAL");
      DTYPE_MAPPING.put("TIMESTAMP", "TIMESTAMP_VAL");
      DTYPE_MAPPING.put("DECFLOAT", "FLOAT_VAL");
      DTYPE_MAPPING.put("DECIMAL", "FLOAT_VAL");
      DTYPE_MAPPING.put("BIT", "BIT_VAL");
      DTYPE_MAPPING.put("CLOB", "LONGTEXT_VAL");
      DTYPE_MAPPING.put("CHAR", "STRING_VAL");
      DTYPE_MAPPING.put("NCHAR", "STRING_VAL");
      DTYPE_MAPPING.put("VARBINARY", "BLOB_VAL");
      DTYPE_MAPPING.put("IMAGE", "BLOB_VAL");
      DTYPE_MAPPING.put("NUMERIC", "NUMBER_VAL");
      DTYPE_MAPPING.put("INTEGER", "NUMBER_VAL");
      DTYPE_MAPPING.put("INT", "NUMBER_VAL");
      DTYPE_MAPPING.put("SMALLINT", "NUMBER_VAL");
      DTYPE_MAPPING.put("DOUBLE", "FLOAT_VAL");
      DTYPE_MAPPING.put("FLOAT", "FLOAT_VAL");
      DTYPE_MAPPING.put("TIME", "TIME_VAL");
      DTYPE_MAPPING.put("DATE", "DATE_VAL");
      DTYPE_MAPPING.put("DATETIME", "TIMESTAMP_VAL");
      COLS_MAPPING.put("BLOB_VAL", "IMAGE");
      COLS_MAPPING.put("STRING_VAL", "VARCHAR");
      COLS_MAPPING.put("BIGINT_VAL", "BIGINT");
      COLS_MAPPING.put("BYTE_VAL", "BYTEA");
      COLS_MAPPING.put("TIMESTAMP_VAL", "DATETIME");
      COLS_MAPPING.put("LONGTEXT_VAL", "NTEXT");
      COLS_MAPPING.put("CHAR_VAL", "CHAR");
      COLS_MAPPING.put("CHARACTER_VAL", "CHARACTER");
      COLS_MAPPING.put("NUMBER_VAL", "INTEGER");
      COLS_MAPPING.put("FLOAT_VAL", "DOUBLE");
      COLS_MAPPING.put("TIME_VAL", "TIME");
      COLS_MAPPING.put("DATE_VAL", "TIMESTAMP");
      DTYPE_MAPPING.put("BIT_VAL", "BIT");

      try {
         Class cl = Class.forName(dBDriverClass);
         System.out.println("postgres Class loader" + String.valueOf(cl));
      } catch (Exception e) {
         System.out.println(" Class loader Exception " + String.valueOf(e));
      }

   }

   public MsSqlDbUtil(String host, String port, String userName, String password, String schema) {
      this.jdbcUrl = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + schema + ";encrypt=false";
      this.userName = userName;
      this.password = password;
      this.schema = schema;
      this.conn = this.getConnection();
   }

   public String getCreateTableScript(TableMetadata meta) {
      StringBuffer sb = new StringBuffer();
      StringBuffer var10000 = sb.append("CREATE TABLE ");
      String var10001 = this.prefix;
      var10000.append(var10001 + meta.getName()).append(" ( ");
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
      System.out.println("colType >> " + colType);
      return (String)DTYPE_MAPPING.get(colType);
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
