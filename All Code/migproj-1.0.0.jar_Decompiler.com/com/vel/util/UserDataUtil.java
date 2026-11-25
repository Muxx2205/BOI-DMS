package com.vel.util;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.vel.util.xml.service.UserXMLParserService;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UserDataUtil extends Thread {
   private long[] roleIds;
   private long adminId;
   ServiceContext ctx;
   String path;
   static boolean startFlag = true;
   private long companyId;

   public void setValue(ServiceContext ctx, long adminId, String path) {
      this.adminId = adminId;
      this.ctx = ctx;
      this.path = path;
   }

   public void run() {
      if (startFlag) {
         startFlag = false;

         try {
            if (this.companyId == 0L) {
               User u = UserLocalServiceUtil.getUser(this.adminId);
               this.companyId = u.getCompanyId();
            }

            Role x = RoleLocalServiceUtil.getRole(this.companyId, "User");
            this.roleIds = new long[1];
            this.roleIds[0] = x.getRoleId();
            File folder = new File(this.path);
            String[] fls = folder.list();

            for(String f : fls) {
               Map<String, String> data = UserXMLParserService.getUserData(this.path + "/" + f);

               try {
                  this.create(data, this.ctx);
               } catch (Exception e) {
                  System.out.println("Exception in uaer import " + String.valueOf(e));
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      startFlag = true;
   }

   public void create(Map<String, String> data, ServiceContext ctx) throws Exception {
      String sc = (String)data.get("Login");
      long[] vals = null;
      if (data.get("Email") != null && ((String)data.get("Email")).trim().length() > 3) {
         if (sc.contains("\\")) {
            sc = sc.substring(sc.indexOf(92) + 1, sc.length());
         }

         sc = sc.replace(" ", "");
         String name = (String)data.get("Title");
         String lname = name;
         if (name.contains(" ")) {
            lname = name.substring(name.lastIndexOf(" ") + 1);
            name = name.substring(0, name.lastIndexOf(" "));
         }

         User user = UserLocalServiceUtil.addUser(this.adminId, this.companyId, false, "Welcome@1", "Welcome@1", false, sc, ((String)data.get("Email")).trim(), Locale.US, name, "", lname, 0L, 0L, true, 1, 1, 2000, (String)null, 1, vals, vals, this.roleIds, vals, false, ctx);
         UserLocalServiceUtil.addRoleUser(this.roleIds[0], user.getUserId());
         UserLocalServiceUtil.updatePasswordManually(user.getUserId(), "Welcome@1", false, false, new Date());
      }

   }
}
