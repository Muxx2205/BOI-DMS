package com.newgen.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class MSSQLDao {
   private static String userName = "dmspoc";
   private static String password = "Welcome#123456";
   private static String jdbcUrl = "jdbc:sqlserver://10.92.51.145:1436;databaseName=boiuat;encrypt=false";
   private static String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

   static {
      try {
         Class.forName(driverClass);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }

   public static Connection getConnection() {
      Connection con = null;

      try {
         con = DriverManager.getConnection(jdbcUrl, userName, password);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      return con;
   }

   public static void closeConnection(Connection con) {
      try {
         if (con != null) {
            con.close();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

   }

   public static String getUserName() {
      return userName;
   }

   public static void setUserName(String userName) {
      MSSQLDao.userName = userName;
   }

   public static String getPassword() {
      return password;
   }

   public static void setPassword(String password) {
      MSSQLDao.password = password;
   }

   public static String getJdbcUrl() {
      return jdbcUrl;
   }

   public static void setJdbcUrl(String jdbcUrl) {
      MSSQLDao.jdbcUrl = jdbcUrl;
   }
}
