package com.vel.util;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserMigrationUtil extends Thread {
   long companyId;
   long creatorId;
   long userRole;
   long groupId;
   ServiceContext ctx;

   public UserMigrationUtil(long companyId, long creatorId, long userRole, long groupId, ServiceContext ctx) {
      this.companyId = companyId;
      this.creatorId = creatorId;
      this.groupId = groupId;
      this.userRole = userRole;
      this.ctx = ctx;
   }

   public void run() {
      this.migUser();
   }

   private void migUser() {
      Connection conn = null;

      try {
         conn = DataAccess.getConnection();
         long[] roles = new long[1];
         roles[0] = this.userRole;
         long[] grps = new long[1];
         grps[0] = this.groupId;
         long[] orgs = new long[1];

         for(Map<String, String> mp : this.getUserList(conn)) {
            if (mp.get("uid") == null) {
               orgs = new long[1];
               if (mp.get("orgid") != null) {
                  orgs[0] = Long.parseLong((String)mp.get("orgid"));
               } else {
                  orgs = null;
               }

               String[] name = this.parseName((String)mp.get("fname"));
               PrintStream var10000 = System.out;
               String var10001 = (String)mp.get("scname");
               var10000.println(var10001 + "    " + (String)mp.get("fname"));
               User user = null;

               try {
                  user = UserLocalServiceUtil.addUser(this.creatorId, this.companyId, false, "password123", "password123", false, ((String)mp.get("scname")).trim(), this.parseEmail((String)mp.get("email"), (String)mp.get("scname")), Locale.ENGLISH, name[0], (String)null, name[1], 0L, 0L, true, 2, 1, 2000, (String)null, 1, grps, orgs, roles, (long[])null, false, this.ctx);
               } catch (Exception e) {
                  System.out.println("Exception >> " + String.valueOf(e));
               }

               if (user != null) {
                  UserLocalServiceUtil.addRoleUser(roles[0], user.getUserId());
                  UserLocalServiceUtil.updatePasswordManually(user.getUserId(), "password123", false, false, new Date());
                  this.updateUserId((String)mp.get("uindex"), user.getUserId(), conn);
                  if (orgs != null) {
                     UserLocalServiceUtil.addOrganizationUser(orgs[0], user);
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

   private String parseEmail(String email, String scname) {
      return scname + "@bankofindia.co.in";
   }

   private String[] parseName(String name) {
      if (name == null) {
         name = "BOI User";
      }

      name = name.trim();
      if (name.length() < 2) {
         name = "BOI User";
      }

      name = name.trim().replace(")", "").replace("(", " ").replace("&", " ").replace("@", " ").replace("[", "");
      String[] st = new String[]{name, " "};
      String[] s = name.split(" ");
      if (s.length > 1) {
         st[0] = s[0];

         for(int x = 1; x < s.length; ++x) {
            st[1] = " " + s[x];
         }

         st[1] = s[s.length - 1];
      } else {
         st[0] = name;
         st[1] = "User";
      }

      return st;
   }

   private void updateUserId(String uindex, long userId, Connection conn) {
      try {
         Statement stm = conn.createStatement();
         stm.executeUpdate("update zz_pdbuser set userid=" + userId + " where userindex=" + uindex);
         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private List<Map<String, String>> getUserList(Connection conn) {
      List<Map<String, String>> list = new ArrayList();
      Map<String, String> mp = null;
      List<String> lst = new ArrayList();

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT zp.UserIndex ,zp.UserName ,zp.PersonalName ,zp.MailId ,zp.UserAlive,zp.userid, mb.branchid FROM zz_PDBUSER zp left join zz_os_master_user mu on (mu.userid =zp.UserName ) left join zz_os_master_branch mb on (mu.branch_c =mb.branch_c )");

         while(set.next()) {
            String st = set.getString(2);
            st = st.replace(" ", "");
            if (!lst.contains(st)) {
               lst.add(st);
               mp = new HashMap();
               mp.put("uindex", set.getString(1));
               mp.put("scname", st);
               mp.put("fname", set.getString(3));
               mp.put("email", set.getString(4));
               mp.put("status", set.getString(5));
               mp.put("uid", set.getString(6));
               mp.put("orgId", set.getString(7));
               list.add(mp);
            }
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public static void main(String[] args) {
      System.out.println("M MM&M M".replace("&", ""));
   }
}
