package com.data.mig.util;

import java.util.HashMap;
import java.util.Map;

public class DBConstants {
   public static final String STRING_VAL = "STRING_VAL";
   public static final String CHAR_VAL = "CHAR_VAL";
   public static final String NUMBER_VAL = "NUMBER_VAL";
   public static final String SNUMBER_VAL = "SNUMBER_VAL";
   public static final String BIGINT_VAL = "BIGINT_VAL";
   public static final String LONG_VAL = "LONG_VAL";
   public static final String BYTE_VAL = "BYTE_VAL";
   public static final String LBYTE_VAL = "LBYTE_VAL";
   public static final String BIGBYTE_VAL = "BIGBYTE_VAL";
   public static final String TIMESTAMP_VAL = "TIMESTAMP_VAL";
   public static final String DATE_VAL = "DATE_VAL";
   public static final String TIME_VAL = "TIME_VAL";
   public static final String FLOAT_VAL = "FLOAT_VAL";
   public static final String TEXT_VAL = "TEXT_VAL";
   public static final String LONGTEXT_VAL = "LONGTEXT_VAL";
   public static final String BLOB_VAL = "BLOB_VAL";
   public static Map<String, String> DB2_MAPPING = new HashMap();
   public static Map<String, String> POSTGRES_COL_MAPPING = new HashMap();

   static {
      DB2_MAPPING.put("CHAR FOR BIT DATA", "CHARACTER_VAL");
      DB2_MAPPING.put("VARCHAR FOR BIT DATA", "LONGTEXT_VAL");
      DB2_MAPPING.put("VARCHAR", "STRING_VAL");
      DB2_MAPPING.put("BIGINT", "BIGINT_VAL");
      DB2_MAPPING.put("BLOB", "BYTE_VAL");
      DB2_MAPPING.put("TIMESTAMP", "TIMESTAMP_VAL");
      DB2_MAPPING.put("DECFLOAT", "FLOAT_VAL");
      DB2_MAPPING.put("CLOB", "LONGTEXT_VAL");
      DB2_MAPPING.put("CHAR", "CHAR_VAL");
      DB2_MAPPING.put("INTEGER", "NUMBER_VAL");
      DB2_MAPPING.put("SMALLINT", "NUMBER_VAL");
      DB2_MAPPING.put("DOUBLE", "FLOAT_VAL");
      DB2_MAPPING.put("TIME", "TIME_VAL");
      DB2_MAPPING.put("DATE", "DATE_VAL");
      POSTGRES_COL_MAPPING.put("STRING_VAL", "VARCHAR");
      POSTGRES_COL_MAPPING.put("BIGINT_VAL", "BIGINT");
      POSTGRES_COL_MAPPING.put("BYTE_VAL", "BYTEA");
      POSTGRES_COL_MAPPING.put("TIMESTAMP_VAL", "TIMESTAMP");
      POSTGRES_COL_MAPPING.put("LONGTEXT_VAL", "TEXT");
      POSTGRES_COL_MAPPING.put("CHAR_VAL", "CHAR");
      POSTGRES_COL_MAPPING.put("CHARACTER_VAL", "CHARACTER");
      POSTGRES_COL_MAPPING.put("NUMBER_VAL", "INTEGER");
      POSTGRES_COL_MAPPING.put("FLOAT_VAL", "DOUBLE");
      POSTGRES_COL_MAPPING.put("TIME_VAL", "TIME");
      POSTGRES_COL_MAPPING.put("DATE_VAL", "TIMESTAMP");
   }
}
