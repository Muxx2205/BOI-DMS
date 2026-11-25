package in.co.boi.branchadmin.web.resource;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import java.io.PrintWriter;
import java.util.List;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/branch/list_users"},
   service = {MVCResourceCommand.class}
)
public class BranchUserListMVCResourceCommand implements MVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(BranchUserListMVCResourceCommand.class);

   public boolean serveResource(ResourceRequest request, ResourceResponse response) {
      JSONArray usersArray = JSONFactoryUtil.createJSONArray();

      try {
         long companyId = PortalUtil.getCompanyId(request);
         _log.info("Fetching branch users for companyId: " + companyId);

         for(Organization nbg : OrganizationLocalServiceUtil.getOrganizations(companyId, 0L)) {
            for(Organization zone : OrganizationLocalServiceUtil.getSuborganizations(companyId, nbg.getOrganizationId())) {
               for(Organization branch : OrganizationLocalServiceUtil.getSuborganizations(companyId, zone.getOrganizationId())) {
                  List<User> users = UserLocalServiceUtil.getOrganizationUsers(branch.getOrganizationId());
                  Log var10000 = _log;
                  String var10001 = branch.getName();
                  var10000.info("Branch: " + var10001 + ", Users: " + users.size());

                  for(User user : users) {
                     JSONObject obj = JSONFactoryUtil.createJSONObject();
                     obj.put("nbg", nbg.getName());
                     obj.put("zone", zone.getName());
                     obj.put("branch", branch.getName());
                     obj.put("screenName", user.getScreenName());
                     obj.put("email", user.getEmailAddress());
                     obj.put("firstName", user.getFirstName());
                     obj.put("lastName", user.getLastName());
                     obj.put("userId", user.getUserId());
                     usersArray.put(obj);
                  }
               }
            }
         }

         response.setContentType("application/json");
         PrintWriter out = response.getWriter();

         try {
            out.print(usersArray.toString());
            out.flush();
         } catch (Throwable var20) {
            if (out != null) {
               try {
                  out.close();
               } catch (Throwable var19) {
                  var20.addSuppressed(var19);
               }
            }

            throw var20;
         }

         if (out != null) {
            out.close();
         }

         _log.info("Total users sent: " + usersArray.length());
      } catch (Exception e) {
         _log.error("Error fetching branch users", e);
      }

      return true;
   }
}
