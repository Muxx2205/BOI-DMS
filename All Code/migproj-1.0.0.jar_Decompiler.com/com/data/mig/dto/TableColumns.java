package com.data.mig.dto;

import java.io.PrintStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class TableColumns {
   private String newName;
   private String name;
   private String sourceType;
   private String type;
   private String valType;
   private int nullable;
   private int size;
   private Object value;
   private int scale;
   static Map<String, String> nameMap = new HashMap();

   static {
      nameMap.put("DISTINCT", "DISTINCT_C");
      nameMap.put("distinct", "DISTINCT_C");
      nameMap.put("ROW_COUNT", "ROW_COUNT_NUM");
      nameMap.put("row_count", "ROW_COUNT_NUM");
      nameMap.put("CLUSTER", "CLUSTER_C");
      nameMap.put("cluster", "CLUSTER_C");
      nameMap.put("USER", "USER_C");
      nameMap.put("user", "USER_C");
      nameMap.put("COMMENT", "COMMENTS");
      nameMap.put("DATE", "DATE_C");
      nameMap.put("BRANCH", "BRANCH_C");
   }

   public TableColumns() {
   }

   public TableColumns(TableColumns obj) {
      this.name = obj.name;
      this.newName = obj.newName;
      this.sourceType = obj.sourceType;
      this.type = obj.type;
      this.valType = obj.valType;
   }

   private static byte[] toBytes2(char[] chars) {
      CharBuffer charBuffer = CharBuffer.wrap(chars);
      ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
      byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
      Arrays.fill(charBuffer.array(), '\u0000');
      Arrays.fill(byteBuffer.array(), (byte)0);
      return bytes;
   }

   private byte[] toBytes(char[] chars) {
      CharBuffer charBuffer = CharBuffer.wrap(chars);
      ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
      byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
      Arrays.fill(charBuffer.array(), '\u0000');
      Arrays.fill(byteBuffer.array(), (byte)0);
      return bytes;
   }

   public void setValue(String val) {
      if (val != null) {
         try {
            StringJoiner joiner = new StringJoiner("");
            Stream.of(val.split("\u0000")).forEach(joiner::add);
            this.value = joiner.toString();
         } catch (Exception e) {
            PrintStream var10000 = System.out;
            String var10001 = String.valueOf(e);
            var10000.println(var10001 + "   Error Value parse UTF " + val);
         }
      } else {
         this.value = val;
      }

   }

   public void setValue(Object val) {
      this.value = val;
   }

   public int getScale() {
      return this.scale;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public String getSourceType() {
      return this.sourceType;
   }

   public String getValType() {
      return this.valType;
   }

   public void setValType(String valType) {
      this.valType = valType;
   }

   public Object getValue() {
      return this.value;
   }

   public void setSourceType(String sourceType) {
      this.sourceType = sourceType;
   }

   public String getNewName() {
      return this.newName;
   }

   public void setNewName(String newName) {
      this.newName = newName;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
      this.newName = name;
      if (nameMap.containsKey(name)) {
         this.newName = (String)nameMap.get(name);
      }

   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public int getNullable() {
      return this.nullable;
   }

   public void setNullable(int nullable) {
      this.nullable = nullable;
   }

   public int getSize() {
      return this.size;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public void setQueryValue(PreparedStatement pst, int index) throws Exception {
      if (!this.valType.contentEquals("STRING_VAL") && !this.valType.contentEquals("CHAR_VAL")) {
         if (!this.valType.contentEquals("BIGINT_VAL") && !this.valType.contentEquals("LONG_VAL")) {
            if (!this.valType.contentEquals("NUMBER_VAL") && !this.valType.contentEquals("SNUMBER_VAL")) {
               if (this.valType.contentEquals("FLOAT_VAL")) {
                  pst.setDouble(index, (Double)this.value);
               } else if (!this.valType.contentEquals("BIGBYTE_VAL") && !this.valType.contentEquals("BYTE_VAL") && !this.valType.contentEquals("BLOB_VAL")) {
                  if (this.valType.contentEquals("DATE_VAL")) {
                     pst.setDate(index, (Date)this.value);
                  } else if (this.valType.contentEquals("TIMESTAMP_VAL")) {
                     pst.setTimestamp(index, (Timestamp)this.value);
                  } else if (this.valType.contentEquals("TIME_VAL")) {
                     pst.setTime(index, (Time)this.value);
                  } else if (this.valType.contentEquals("TEXT_VAL") || this.valType.contentEquals("LONGTEXT_VAL")) {
                     pst.setString(index, (String)this.value);
                  }
               } else {
                  pst.setBytes(index, (byte[])this.value);
               }
            } else {
               pst.setInt(index, (Integer)this.value);
            }
         } else {
            pst.setLong(index, (Long)this.value);
         }
      } else {
         pst.setString(index, (String)this.value);
      }

   }

   public TableColumns populatVal(ResultSet set) throws Exception {
      TableColumns tt = new TableColumns(this);
      if (!this.valType.contentEquals("STRING_VAL") && !this.valType.contentEquals("CHAR_VAL")) {
         if (!this.valType.contentEquals("BIGINT_VAL") && !this.valType.contentEquals("LONG_VAL")) {
            if (!this.valType.contentEquals("NUMBER_VAL") && !this.valType.contentEquals("SNUMBER_VAL")) {
               if (this.valType.contentEquals("FLOAT_VAL")) {
                  tt.setValue((Object)set.getDouble(this.name));
               } else if (!this.valType.contentEquals("BIGBYTE_VAL") && !this.valType.contentEquals("BYTE_VAL") && !this.valType.contentEquals("BLOB_VAL")) {
                  if (this.valType.contentEquals("DATE_VAL")) {
                     tt.setValue((Object)set.getDate(this.name));
                  } else if (this.valType.contentEquals("TIMESTAMP_VAL")) {
                     tt.setValue((Object)set.getTimestamp(this.name));
                  } else if (this.valType.contentEquals("TIME_VAL")) {
                     tt.setValue((Object)set.getTime(this.name));
                  } else if (this.valType.contentEquals("TEXT_VAL") || this.valType.contentEquals("LONGTEXT_VAL")) {
                     if (this.sourceType.contentEquals("CLOB")) {
                        Clob cl = set.getClob(this.name);
                        if (cl != null) {
                           Reader r = cl.getCharacterStream();
                           char[] ch = new char[(int)cl.length()];
                           r.read(ch);
                           tt.setValue(new String(ch));
                        }
                     } else {
                        tt.setValue(set.getString(this.name));
                     }
                  }
               } else {
                  tt.setValue((Object)set.getBytes(this.name));
               }
            } else {
               tt.setValue((Object)set.getInt(this.name));
            }
         } else {
            tt.setValue((Object)set.getLong(this.name));
         }
      } else {
         tt.setValue(set.getString(this.name));
      }

      return tt;
   }
}
