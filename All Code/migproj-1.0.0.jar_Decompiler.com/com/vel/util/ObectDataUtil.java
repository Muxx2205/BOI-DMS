package com.vel.util;

import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObectDataUtil {
   private long objectDefId;
   private Connection conn;
   private List<String> tabNames;
   private List<String> insertQuries;
   Map<String, List<String>> columnsMap;
   Map<String, String> columnsTypeMap;
   Map<String, String> dataMap;
   private String pkcolumnName;
   private long classpknameid;
   private long companyId;
   String classname = null;

   public ObectDataUtil(long objectDefId, Connection conn) throws Exception {
      this.objectDefId = objectDefId;
      this.conn = conn;
      this.init();
   }

   private void init() throws Exception {
      Statement stm = this.conn.createStatement();
      ResultSet set = stm.executeQuery("SELECT o.COMPANYID ,o.PKOBJECTFIELDDBCOLUMNNAME,o.NAME  , classname  FROM OBJECTDEFINITION o WHERE o.OBJECTDEFINITIONID =" + this.objectDefId);
      if (set.next()) {
         this.companyId = set.getLong(1);
         this.pkcolumnName = set.getString(2);
         this.classname = set.getString(4);
      }

      stm.close();
      stm = this.conn.createStatement();
      set = stm.executeQuery("SELECT dbtablename,dbtype,name,o.DBCOLUMNNAME  FROM OBJECTFIELD o WHERE OBJECTDEFINITIONID =" + this.objectDefId);
      this.dataMap = new HashMap();
      this.tabNames = new ArrayList();
      this.insertQuries = new ArrayList();
      this.columnsMap = new HashMap();
      this.columnsTypeMap = new HashMap();

      while(set.next()) {
         String tabname = set.getString(1);
         String type = set.getString(2);
         String name = set.getString(3);
         String colname = set.getString(4);
         this.columnsTypeMap.put(colname, type);
         this.dataMap.put(colname, name);
         if (!this.columnsMap.containsKey(tabname)) {
            this.columnsMap.put(tabname, new ArrayList());
         }

         ((List)this.columnsMap.get(tabname)).add(colname);
         if (!tabname.equalsIgnoreCase("ObjectEntry") && !this.tabNames.contains(tabname)) {
            this.tabNames.add(tabname);
         }
      }

      stm.close();
      stm = this.conn.createStatement();
      set = stm.executeQuery("select classnameid from classname_ where value='" + this.classname + "'");
      if (set.next()) {
         this.classpknameid = set.getLong(1);
      }

      stm.close();
      this.buildQuery();
   }

   private void buildQuery() {
      for(String st : this.tabNames) {
         String query = "insert into " + st + " ( " + this.pkcolumnName;
         String prm = " ? ";

         for(String c : (List)this.columnsMap.get(st)) {
            query = query + ", " + c;
            prm = prm + " ,?";
         }

         query = query + ") values (" + prm + ")";
         this.insertQuries.add(query);
         System.out.println("query>> " + query);
      }

   }

   private void setValue(PreparedStatement pst, int index, Map<String, Object> data, String cname) throws Exception {
      Object val = data.get(this.dataMap.get(cname));
      if (val != null) {
         if (!((String)this.columnsTypeMap.get(cname)).equalsIgnoreCase("Long") && !((String)this.columnsTypeMap.get(cname)).equalsIgnoreCase("Integer") && !((String)this.columnsTypeMap.get(cname)).equalsIgnoreCase("BigDecimal")) {
            if (!((String)this.columnsTypeMap.get(cname)).equalsIgnoreCase("Date") && !((String)this.columnsTypeMap.get(cname)).equalsIgnoreCase("DateTime")) {
               pst.setString(index, (String)val);
            } else {
               pst.setDate(index, new Date(((java.util.Date)val).getTime()));
            }
         } else {
            pst.setLong(index, (Long)val);
         }
      } else {
         pst.setString(index, (String)null);
      }

   }

   public Long save(Map<String, Object> data, long userid, String username, java.util.Date date) throws Exception {
      Exception ex = null;
      long id = 0L;

      try {
         this.conn.setAutoCommit(false);
         Statement stm = this.conn.createStatement();
         ResultSet set = stm.executeQuery("select max(entryid) from assetentry ");
         if (set.next()) {
            id = set.getLong(1) + 1L;
         }

         stm.close();
         String uuid = PortalUUIDUtil.generate();
         PreparedStatement pst = this.conn.prepareStatement("INSERT INTO BOIDMSPROD.OBJECTENTRY(MVCCVERSION, UUID_, EXTERNALREFERENCECODE, OBJECTENTRYID, GROUPID, COMPANYID, USERID, USERNAME, CREATEDATE, MODIFIEDDATE, OBJECTDEFINITIONID, ROOTOBJECTENTRYID, LASTPUBLISHDATE, STATUS, STATUSBYUSERID, STATUSBYUSERNAME, STATUSDATE)VALUES(? ,?, ?,?, ?, ?, ?, ?,?,?, ?,?,?,?,?,?,?)");
         pst.setLong(1, 0L);
         pst.setString(2, uuid);
         pst.setString(3, uuid);
         pst.setLong(4, id);
         pst.setLong(5, 0L);
         pst.setLong(6, this.companyId);
         pst.setLong(7, userid);
         pst.setString(8, username);
         pst.setDate(9, new Date(date.getTime()));
         pst.setDate(10, new Date(date.getTime()));
         pst.setLong(11, this.objectDefId);
         pst.setLong(12, 0L);
         pst.setDate(13, new Date(date.getTime()));
         pst.setLong(14, 0L);
         pst.setLong(15, userid);
         pst.setString(16, username);
         pst.setDate(17, new Date(date.getTime()));
         pst.executeUpdate();
         pst.close();
         int x = 0;

         for(String tabName : this.tabNames) {
            String query = (String)this.insertQuries.get(x);
            pst = this.conn.prepareStatement(query);
            pst.setLong(1, id);
            int num = 2;

            for(String col : (List)this.columnsMap.get(tabName)) {
               this.setValue(pst, num, data, col);
               ++num;
            }

            pst.executeUpdate();
            pst.close();
            ++x;
         }

         long aid = id + 1L;
         pst = this.conn.prepareStatement("INSERT INTO ASSETENTRY (MVCCVERSION, CTCOLLECTIONID, ENTRYID, GROUPID, COMPANYID, USERID, USERNAME, CREATEDATE, MODIFIEDDATE, CLASSNAMEID, CLASSPK, CLASSUUID, CLASSTYPEID, LISTABLE, VISIBLE,  MIMETYPE, TITLE, DESCRIPTION,  HEIGHT, WIDTH, PRIORITY) VALUES(? ,? ,?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         pst.setLong(1, 0L);
         pst.setLong(2, 0L);
         pst.setLong(3, aid);
         pst.setLong(4, 20121L);
         pst.setLong(5, this.companyId);
         pst.setLong(6, userid);
         pst.setString(7, username);
         pst.setDate(8, new Date(date.getTime()));
         pst.setDate(9, new Date(date.getTime()));
         pst.setLong(10, this.classpknameid);
         pst.setLong(11, id);
         pst.setString(12, uuid);
         pst.setLong(13, 0L);
         pst.setLong(14, 1L);
         pst.setLong(15, 0L);
         pst.setString(16, "text/plain");
         pst.setString(17, "" + id);
         pst.setString(18, "" + id);
         pst.setLong(19, 0L);
         pst.setLong(20, 0L);
         pst.setLong(21, 0L);
         pst.executeUpdate();
         pst.close();
         long rid = 0L;
         stm = this.conn.createStatement();
         set = stm.executeQuery("select max(RESOURCEPERMISSIONID) from RESOURCEPERMISSION ");
         if (set.next()) {
            rid = set.getLong(1) + 1L;
         }

         stm.close();
         pst = this.conn.prepareStatement("INSERT INTO BOIDMSPROD.RESOURCEPERMISSION (MVCCVERSION, CTCOLLECTIONID, RESOURCEPERMISSIONID, COMPANYID, NAME, \"SCOPE\", PRIMKEY, PRIMKEYID, ROLEID, OWNERID, ACTIONIDS, VIEWACTIONID) VALUES(? ,? ,?, ?, ?, ?, ?,?,?,?,?,?)");
         pst.setLong(1, 0L);
         pst.setLong(2, 0L);
         pst.setLong(3, rid);
         pst.setLong(4, this.companyId);
         pst.setString(5, this.classname);
         pst.setLong(6, 4L);
         pst.setLong(7, id);
         pst.setLong(8, this.classpknameid);
         pst.setLong(9, 20103L);
         pst.setLong(10, userid);
         pst.setLong(11, 15L);
         pst.setLong(12, 1L);
         pst.executeUpdate();
         pst.close();
         this.conn.commit();
      } catch (Exception e) {
         System.out.println("Exception >>  " + String.valueOf(e));
         this.conn.rollback();
         id = 0L;
         ex = e;
      }

      this.conn.setAutoCommit(true);
      if (id == 0L) {
         throw ex;
      } else {
         return id;
      }
   }

   public void updateCounter() {
      try {
         long rid = 0L;
         Statement stm = this.conn.createStatement();
         ResultSet set = stm.executeQuery("select max(RESOURCEPERMISSIONID) from RESOURCEPERMISSION ");
         if (set.next()) {
            rid = set.getLong(1) + 1L;
         }

         stm.close();
         stm = this.conn.createStatement();
         stm.executeUpdate("update counter set CURRENTID=" + rid + " where name='com.liferay.portal.kernel.model.ResourcePermission' ");
         stm.close();
         System.out.println("Counter Updated >> " + rid);
      } catch (Exception e) {
         System.out.println("Exception in updateCounter() >> " + String.valueOf(e));
      }

   }
}
