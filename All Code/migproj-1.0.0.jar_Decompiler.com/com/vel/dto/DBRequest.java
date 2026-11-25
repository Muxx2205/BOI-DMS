package com.vel.dto;

import java.util.ArrayList;
import java.util.List;

public class DBRequest {
   private String sourceDB;
   private String sourceSchema;
   private String sourceHost;
   private String sourcePort;
   private String sourceUser;
   private String sourcePassword;
   private String targetDB;
   private String targetSchema;
   private String targetHost;
   private String targetPort;
   private String targetUser;
   private String targetPassword;
   private List<String> tableNames = new ArrayList();
   private String prefix;

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public String getSourceDb() {
      return this.sourceDB;
   }

   public void setSourceDb(String sourceDB) {
      this.sourceDB = sourceDB;
   }

   public String getSourceSchema() {
      return this.sourceSchema;
   }

   public void setSourceSchema(String sourceSchema) {
      this.sourceSchema = sourceSchema;
   }

   public String getSourceHost() {
      return this.sourceHost;
   }

   public void setSourceHost(String sourceHost) {
      this.sourceHost = sourceHost;
   }

   public String getSourcePort() {
      return this.sourcePort;
   }

   public void setSourcePort(String sourcePort) {
      this.sourcePort = sourcePort;
   }

   public String getSourceUser() {
      return this.sourceUser;
   }

   public void setSourceUser(String sourceUser) {
      this.sourceUser = sourceUser;
   }

   public String getSourcePassword() {
      return this.sourcePassword;
   }

   public void setSourcePassword(String sourcePassword) {
      this.sourcePassword = sourcePassword;
   }

   public String getTargetDb() {
      return this.targetDB;
   }

   public void setTargetDb(String targetDB) {
      this.targetDB = targetDB;
   }

   public String getTargetSchema() {
      return this.targetSchema;
   }

   public void setTargetSchema(String targetSchema) {
      this.targetSchema = targetSchema;
   }

   public String getTargetHost() {
      return this.targetHost;
   }

   public void setTargetHost(String targetHost) {
      this.targetHost = targetHost;
   }

   public String getTargetPort() {
      return this.targetPort;
   }

   public void setTargetPort(String targetPort) {
      this.targetPort = targetPort;
   }

   public String getTargetUser() {
      return this.targetUser;
   }

   public void setTargetUser(String targetUser) {
      this.targetUser = targetUser;
   }

   public String getTargetPassword() {
      return this.targetPassword;
   }

   public void setTargetPassword(String targetPassword) {
      this.targetPassword = targetPassword;
   }

   public List<String> getTableNames() {
      return this.tableNames;
   }

   public void setTableNames(List<String> tableNames) {
      this.tableNames = tableNames;
   }
}
