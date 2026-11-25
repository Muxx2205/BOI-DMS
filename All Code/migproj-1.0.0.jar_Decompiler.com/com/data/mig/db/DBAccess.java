package com.data.mig.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBAccess {
   private static String dBDriverClass = "oracle.jdbc.OracleDriver";

   static {
      try {
         Class.forName(dBDriverClass);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public static Connection getConnection() {
      try {
         return DriverManager.getConnection("jdbc:oracle:thin:@10.92.50.60:5432:boidmsora19", "boidmsapp", "password");
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
         return null;
      }
   }
}
