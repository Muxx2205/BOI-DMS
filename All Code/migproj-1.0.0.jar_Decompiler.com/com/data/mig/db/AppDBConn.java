package com.data.mig.db;

import com.vel.dto.DBRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AppDBConn {
   private static String[] dBDriverClass = new String[]{"com.ibm.db2.jcc.DB2Driver", "org.postgresql.Driver", "org.postgresql.Driver", "com.mysql.cj.jdbc.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "oracle.jdbc.OracleDriver"};
   private static String souceDBUserName = "db2inst1";
   private static String souceDBPassword = "password";
   private static String souceDBJdbcUrl = "jdbc:db2://192.168.37.42:50000/";
   private static String targetDBUserName = "appdbusr";
   private static String targetDBPassword = "app@May16";
   private static String targetDBJdbcUrl = "jdbc:postgresql://192.168.42.190:5001/";

   static {
      String[] var3;
      for(String cname : var3 = dBDriverClass) {
         try {
            Class cl = Class.forName(cname);
            System.out.println("postgres Class loader" + String.valueOf(cl));
         } catch (Exception e) {
            System.out.println(cname + " Class loader Exception " + String.valueOf(e));
         }
      }

   }

   public static void init(DBRequest req) {
      souceDBUserName = req.getSourceUser();
      souceDBPassword = req.getSourcePassword();
      targetDBUserName = req.getTargetUser();
      targetDBPassword = req.getTargetPassword();
   }

   public static void main(String[] args) {
      Connection con = null;

      try {
         con = getSoruceConnection("bandhan");
         System.out.println("success ");
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         closeConnection(con);
      }

   }

   public static void checkNewDb(String dbname) {
      Connection con = null;

      try {
         con = getTargetConnection("postgres");
         Statement stm = con.createStatement();
         stm.execute("CREATE DATABASE " + dbname + "    WITH \n    ENCODING = 'UTF8' \n    CONNECTION LIMIT = -1;");
         System.out.println("db created successfully " + dbname);
      } catch (Exception e) {
         System.out.println(dbname + "  Exception in createNewDb method " + String.valueOf(e));
      } finally {
         closeConnection(con);
      }

   }

   public static Connection getSoruceConnection(String dbname) {
      Connection con = null;

      try {
         con = DriverManager.getConnection(souceDBJdbcUrl + dbname, souceDBUserName, souceDBPassword);
      } catch (Exception ex) {
         System.out.println("getSoruceConnection " + String.valueOf(ex));
      }

      return con;
   }

   public static Connection getTargetConnection(String dbname) {
      Connection con = null;

      try {
         con = DriverManager.getConnection(targetDBJdbcUrl + dbname, targetDBUserName, targetDBPassword);
      } catch (Exception ex) {
         ex.printStackTrace();
         System.out.println("getTargetConnection " + String.valueOf(ex));
      }

      return con;
   }

   public static void closeConnection(Connection con) {
      try {
         if (con != null) {
            con.close();
         }
      } catch (Exception ex) {
         System.out.println("close connection " + String.valueOf(ex));
      }

   }
}
