package com.data.mig.dto;

import java.util.ArrayList;
import java.util.List;

public class TableMetadata {
   private String dbName;
   private String name;
   private TableColumns primaryColumn;
   private List<TableColumns> columnsList;
   private String primaryKeys;
   private static List<String> tabList = new ArrayList();

   public String getPrimaryKeys() {
      return this.primaryKeys;
   }

   public void setPrimaryKeys(String primaryKeys) {
      this.primaryKeys = primaryKeys;
   }

   public String getDbName() {
      return this.dbName;
   }

   public void setDbName(String dbName) {
      this.dbName = dbName;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public TableColumns getPrimaryColumn() {
      return this.primaryColumn;
   }

   public void setPrimaryColumn(TableColumns primaryColumn) {
      this.primaryColumn = primaryColumn;
   }

   public List<TableColumns> getColumnsList() {
      return this.columnsList;
   }

   public void setColumnsList(List<TableColumns> columnsList) {
      this.columnsList = columnsList;
   }
}
