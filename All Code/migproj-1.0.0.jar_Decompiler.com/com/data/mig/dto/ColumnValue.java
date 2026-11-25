package com.data.mig.dto;

public class ColumnValue {
   private String type;
   private Object value;

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public Object getValue() {
      return this.value;
   }

   public void setValue(Object value) {
      this.value = value;
   }
}
