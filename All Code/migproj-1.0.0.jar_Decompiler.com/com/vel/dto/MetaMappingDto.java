package com.vel.dto;

public class MetaMappingDto {
   String dataClassFld;
   String objectFld;
   String value;
   String columnName;
   String dataType;

   public String getDataClassFld() {
      return this.dataClassFld;
   }

   public void setDataClassFld(String dataClassFld) {
      this.dataClassFld = dataClassFld;
   }

   public String getObjectFld() {
      return this.objectFld;
   }

   public void setObjectFld(String objectFld) {
      this.objectFld = objectFld;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public String getColumnName() {
      return this.columnName;
   }

   public void setColumnName(String columnName) {
      this.columnName = columnName;
   }

   public String getDataType() {
      return this.dataType;
   }

   public void setDataType(String dataType) {
      this.dataType = dataType;
   }
}
