package com.vel.util;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgMigrationUtil extends Thread {
   long parentOrgId;
   ServiceContext ctx;

   public OrgMigrationUtil(long parentOrgId, ServiceContext ctx) {
      this.ctx = ctx;
      this.parentOrgId = parentOrgId;
   }

   public void run() {
      this.migOrg();
   }

   private void migOrg() {
      Connection conn = null;

      try {
         Organization ho = OrganizationLocalServiceUtil.fetchOrganization(this.parentOrgId);
         Organization nbgOrg = null;
         Organization zoneOrg = null;
         Organization brOrg = null;
         conn = DataAccess.getConnection();

         for(Map<String, String> nbg : this.getNBGList(conn)) {
            if (nbg.get("id") == null) {
               nbgOrg = this.createOrg(ho, "NBG", (String)nbg.get("nbg"), (String)null);
               nbg.put("id", "" + nbgOrg.getOrganizationId());
               this.updateNbg(nbg, conn);
            } else {
               nbgOrg = OrganizationLocalServiceUtil.fetchOrganization(Long.parseLong((String)nbg.get("id")));
            }

            for(Map<String, String> zn : this.getZoneList((String)nbg.get("nbg"), conn)) {
               if (zn.get("id") == null) {
                  zoneOrg = this.createOrg(nbgOrg, "ZONE", (String)zn.get("code"), (String)zn.get("name"));
                  zn.put("id", "" + zoneOrg.getOrganizationId());
                  this.updateZone(zn, conn);
               } else {
                  zoneOrg = OrganizationLocalServiceUtil.fetchOrganization(Long.parseLong((String)zn.get("id")));
               }

               for(Map<String, String> b : this.getBranchList((String)zn.get("name"), conn)) {
                  if (b.get("id") == null) {
                     brOrg = this.createOrg(zoneOrg, "BRANCH", (String)b.get("code"), (String)b.get("name"));
                     b.put("id", "" + brOrg.getOrganizationId());
                     this.updateBranch(b, conn);
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         DataAccess.cleanUp(conn);
      }

   }

   private void updateNbg(Map<String, String> nbg, Connection conn) throws Exception {
      Statement stm = conn.createStatement();
      String var10001 = (String)nbg.get("id");
      stm.executeUpdate("update ZZ_OS_MASTER_BRANCH set nbgid=" + var10001 + " where nbg='" + (String)nbg.get("nbg") + "'");
      stm.close();
   }

   private void updateZone(Map<String, String> nbg, Connection conn) throws Exception {
      Statement stm = conn.createStatement();
      String var10001 = (String)nbg.get("id");
      stm.executeUpdate("update ZZ_OS_MASTER_BRANCH set zoneid=" + var10001 + " where zcod='" + (String)nbg.get("name") + "'");
      stm.close();
   }

   private void updateBranch(Map<String, String> nbg, Connection conn) throws Exception {
      Statement stm = conn.createStatement();
      String var10001 = (String)nbg.get("id");
      stm.executeUpdate("update ZZ_OS_MASTER_BRANCH set branchid=" + var10001 + " where branch_c='" + (String)nbg.get("code") + "'");
      stm.close();
   }

   private Organization createOrg(Organization parent, String type, String name, String comments) throws Exception {
      Organization org = null;
      org = OrganizationLocalServiceUtil.addOrganization(PortalUUIDUtil.generate(), parent.getUserId(), parent.getOrganizationId(), name, type, parent.getRegionId(), parent.getCountryId(), parent.getStatusListTypeId(), comments, false, this.ctx);
      return org;
   }

   private List<Map<String, String>> getNBGList(Connection conn) {
      List<String> list = new ArrayList();
      List<Map<String, String>> zlist = new ArrayList();
      Map<String, String> mp = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT nbg,nbgid FROM  ZZ_OS_MASTER_BRANCH ");

         while(set.next()) {
            String nbg = set.getString(1);
            if (!list.contains(nbg)) {
               list.add(nbg);
               mp = new HashMap();
               mp.put("nbg", nbg);
               mp.put("id", set.getString(2));
               zlist.add(mp);
            }
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return zlist;
   }

   private List<Map<String, String>> getZoneList(String nbg, Connection conn) {
      List<String> list = new ArrayList();
      List<Map<String, String>> zlist = new ArrayList();
      Map<String, String> mp = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT zone,zcod , zoneid FROM  ZZ_OS_MASTER_BRANCH where nbg='" + nbg + "'");

         while(set.next()) {
            String code = set.getString(1);
            if (!list.contains(code)) {
               list.add(code);
               mp = new HashMap();
               mp.put("code", code);
               mp.put("name", set.getString(2));
               mp.put("id", set.getString(3));
               zlist.add(mp);
            }
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return zlist;
   }

   private List<Map<String, String>> getBranchList(String zone, Connection conn) {
      List<String> list = new ArrayList();
      List<Map<String, String>> zlist = new ArrayList();
      Map<String, String> mp = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT branch_c , branchid  FROM  ZZ_OS_MASTER_BRANCH where zcod='" + zone + "'");

         while(set.next()) {
            String code = set.getString(1);
            if (!list.contains(code)) {
               list.add(code);
               mp = new HashMap();
               mp.put("code", code);
               mp.put("id", set.getString(2));
               zlist.add(mp);
            }
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return zlist;
   }
}
