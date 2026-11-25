package com.liferay.boi.dashboard.report.application;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Component(
   immediate = true,
   property = {"osgi.jaxrs.application.base=/dashboardreport", "osgi.jaxrs.name=DashboardReport.Rest"},
   service = {Application.class}
)
public class BoiDashboardReportApplication extends Application {
   String sql = "SELECT category, objectDefinitionId, name, total_count FROM (  SELECT 'OBJECT_ENTRY' AS category,         oe.objectDefinitionId AS objectDefinitionId,         NULL AS name,         COUNT(*) AS total_count  FROM ObjectEntry oe  WHERE oe.createDate >= ?    AND oe.createDate <  ?  GROUP BY oe.objectDefinitionId  UNION ALL  SELECT 'DLFILEENTRY', NULL, 'Total Files Received', COUNT(*)  FROM DLFileEntry d  WHERE d.createDate >= ?    AND d.createDate <  ?  UNION ALL  SELECT 'LOGIN', NULL, 'Total Users Logged In', COUNT(DISTINCT userId)  FROM Audit_AuditEvent a  WHERE a.eventType = 'LOGIN'    AND a.createDate >= ?    AND a.createDate <  ?  UNION ALL  SELECT 'ALL_FILE_SIZE_GB', NULL, 'All File Size (GB)',         ROUND(SUM(d1.size_) / 1024 / 1024 / 1024, 2)  FROM DLFileEntry d1  WHERE d1.createDate >= ?    AND d1.createDate <  ? ) t ORDER BY category, total_count DESC";
   String sql12 = "SELECT * FROM (     SELECT         XMLCAST(             XMLQUERY(                 '/root/Label[@language-id=\"en_US\"]/text()'                 PASSING XMLTYPE(od.LABEL)                 RETURNING CONTENT             ) AS VARCHAR2(4000)         ) AS category,         COUNT(oe.objectEntryId) AS total_count     FROM ObjectEntry oe     JOIN ObjectDefinition od         ON od.objectDefinitionId = oe.objectDefinitionId     WHERE oe.createDate >= ?     AND oe.createDate < ?     AND oe.objectDefinitionId NOT IN (288564,13814050,45430606,77117812)     GROUP BY od.LABEL     UNION ALL     SELECT 'Total Files Received', COUNT(*)     FROM DLFileEntry d     WHERE d.createDate >= ?     AND d.createDate < ?     UNION ALL     SELECT 'Total Users Logged In', COUNT(DISTINCT userId)     FROM Audit_AuditEvent a     WHERE a.eventType = 'LOGIN'     AND a.createDate >= ?     AND a.createDate < ?     UNION ALL     SELECT 'All File Size (GB)',            ROUND(SUM(d1.size_) / 1024 / 1024 / 1024, 2)     FROM DLFileEntry d1     WHERE d1.createDate >= ?     AND d1.createDate < ?     UNION ALL     SELECT 'Live SAML Users', COUNT(*)     FROM SAMLSPSESSION s     WHERE s.createDate > ? ) ORDER BY total_count DESC";
   String reportSql = "SELECT category, objectDefinitionId, name, total_count FROM (  SELECT 'OBJECT_ENTRY' AS category,         oe.objectDefinitionId AS objectDefinitionId,         NULL AS name,         COUNT(*) AS total_count  FROM ObjectEntry oe  WHERE oe.createDate >= ?    AND oe.createDate <  ?  GROUP BY oe.objectDefinitionId  UNION ALL  SELECT 'DLFILEENTRY', NULL, 'Total Files Received', COUNT(*)  FROM DLFileEntry  WHERE createDate >= ?    AND createDate <  ?  UNION ALL  SELECT 'LOGIN', NULL, 'Total Users Logged In', COUNT(DISTINCT userId)  FROM Audit_AuditEvent  WHERE eventType = 'LOGIN'    AND createDate >= ?    AND createDate <  ? ) t ORDER BY category, total_count DESC";
   String dailyReportSQL = "SELECT\r\n    metric_key,\r\n    metric_name,\r\n    metric_value\r\nFROM (\r\n\r\n    /* Total distinct logged-in users */\r\n    SELECT\r\n        'TOTAL_USER_LOGGED_IN_COUNT' AS metric_key,\r\n        'Total User Logged In Count' AS metric_name,\r\n        COUNT(*) AS metric_value\r\n    FROM (\r\n        SELECT s.userId\r\n        FROM Audit_AuditEvent s\r\n        WHERE s.eventType = 'LOGIN'\r\n          AND s.createDate > ?\r\n        GROUP BY s.userId\r\n    ) login_users\r\n\r\n    UNION ALL\r\n\r\n    /* Live users */\r\n    SELECT\r\n        'LIVE_USER_COUNT',\r\n        'Live User Count',\r\n        COUNT(*)\r\n    FROM SAMLSPSESSION s\r\n    WHERE s.createDate > ?\r\n\r\n    UNION ALL\r\n\r\n    /* All transition count */\r\n    SELECT\r\n        'ALL_TRANSITION_COUNT',\r\n        'All Transition Count',\r\n        COUNT(*)\r\n    FROM ObjectEntry o\r\n    WHERE o.createDate > ?\r\n      AND o.objectDefinitionId NOT IN (288564, 13814050, 45430606)\r\n\r\n    UNION ALL\r\n\r\n    /* All file count */\r\n    SELECT\r\n        'ALL_FILE_COUNT',\r\n        'All File Count',\r\n        COUNT(*)\r\n    FROM DLFileEntry d\r\n    WHERE d.createDate > ?\r\n\r\n    UNION ALL\r\n\r\n    /* All file size in GB */\r\n    SELECT\r\n        'ALL_FILE_SIZE_GB',\r\n        'All File Size (GB)',\r\n        ROUND(SUM(d.size_) / 1024 / 1024 / 1024, 2)\r\n    FROM DLFileEntry d\r\n    WHERE d.createDate > ?\r\n\r\n) dashboard_metrics\r\n";

   public Set<Object> getSingletons() {
      return Collections.singleton(this);
   }

   @GET
   @Path("/report")
   @Produces({"application/json"})
   public Response getReport(@QueryParam("fromDate") String fromDate, @QueryParam("toDate") String toDate) {
      List<DashboardStatDTO> resultList = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Response toTs;
      try {
         Timestamp fromTs = Timestamp.valueOf(fromDate + " 00:00:00");
         Timestamp toTs = Timestamp.valueOf(toDate + " 00:00:00");
         con = DataAccess.getConnection();
         ps = con.prepareStatement(this.sql12);
         ps.setTimestamp(1, fromTs);
         ps.setTimestamp(2, toTs);
         ps.setTimestamp(3, fromTs);
         ps.setTimestamp(4, toTs);
         ps.setTimestamp(5, fromTs);
         ps.setTimestamp(6, toTs);
         ps.setTimestamp(7, fromTs);
         ps.setTimestamp(8, toTs);
         ps.setTimestamp(9, fromTs);
         rs = ps.executeQuery();

         while(rs.next()) {
            String category = rs.getString("CATEGORY");
            long totalCount = rs.getLong("TOTAL_COUNT");
            resultList.add(new DashboardStatDTO(category, category, totalCount));
         }

         Response var18 = Response.ok(resultList).build();
         return var18;
      } catch (Exception e) {
         toTs = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } finally {
         DataAccess.cleanUp(con, ps, rs);
      }

      return toTs;
   }

   public static String extractLabelSafe(String value) {
      if (value != null && !value.trim().isEmpty()) {
         value = value.replace("\ufeff", "").trim();
         if (!value.startsWith("<")) {
            return value.trim();
         } else {
            try {
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               factory.setNamespaceAware(false);
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document document = builder.parse(new InputSource(new StringReader(value)));
               XPath xpath = XPathFactory.newInstance().newXPath();
               return xpath.evaluate("/root/Label[@language-id='en_US']/text()", document).trim();
            } catch (Exception var5) {
               return value.replaceAll("<[^>]+>", "").trim();
            }
         }
      } else {
         return null;
      }
   }

   @GET
   @Path("/daily-report")
   @Produces({"application/json"})
   public Response getDailyReport(@QueryParam("fromDate") String fromDate) {
      List<DailyReportDTO> resultList = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Response var7;
      try {
         Timestamp fromTs = Timestamp.valueOf(fromDate + " 00:00:00");
         con = DataAccess.getConnection();
         ps = con.prepareStatement(this.dailyReportSQL);
         ps.setTimestamp(1, fromTs);
         ps.setTimestamp(2, fromTs);
         ps.setTimestamp(3, fromTs);
         ps.setTimestamp(4, fromTs);
         ps.setTimestamp(5, fromTs);
         rs = ps.executeQuery();

         while(rs.next()) {
            resultList.add(new DailyReportDTO(rs.getString("metric_key"), rs.getString("metric_name"), rs.getDouble("metric_value")));
         }

         var7 = Response.ok(resultList).build();
         return var7;
      } catch (Exception e) {
         var7 = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } finally {
         DataAccess.cleanUp(con, ps, rs);
      }

      return var7;
   }

   @GET
   @Path("/updateStatus")
   @Produces({"application/json"})
   public Response updateStatus() {
      (new ObjectEntryApprovalService()).fetchByStatusBatch(395809L, 40);
      return Response.ok("Work done").build();
   }

   static {
      System.out.println("Data Loaded");
   }
}
