package boi.user.details.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=BoiUserDetails", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=boi_user_details_BoiUserDetailsPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class BoiUserDetailsPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(BoiUserDetailsPortlet.class);

   public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException {
      JSONObject json = JSONFactoryUtil.createJSONObject();
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      String screenName = ParamUtil.getString(resourceRequest, "screenName");
      _log.info("Requested user screenName: " + screenName);

      try {
         User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), screenName);
         _log.info("User found: " + user.getFullName());
         String userImage = "/documents/d/boi-dms/avatar2";

         try {
            if (Validator.isNotNull(user.getPortraitURL(themeDisplay))) {
               userImage = user.getPortraitURL(themeDisplay);
               _log.info("User portrait URL: " + userImage);
            }
         } catch (PortalException e1) {
            _log.warn("Could not fetch portrait URL", e1);
         }

         String fullName = user.getFullName();
         String email = user.getEmailAddress();
         _log.info("User details - Full Name: " + fullName + ", Email: " + email + ", Department: ");
         List<String> roleNames = new ArrayList();

         for(Role role : user.getRoles()) {
            String roleName = role.getName();
            if (!"User".equalsIgnoreCase(roleName)) {
               roleNames.add(roleName);
               _log.info("User role: " + roleName);
            }
         }

         long organizationId = 0L;
         String branch = "";
         String zone = "";
         String nbg = "";
         long branchId = 0L;
         long zoneId = 0L;
         long nbgId = 0L;
         List<Organization> orgs = user.getOrganizations();
         if (!orgs.isEmpty()) {
            Organization currentOrg = (Organization)orgs.get(0);
            _log.info("Initial organization type: " + currentOrg.getType() + ", name: " + currentOrg.getName());
            if ("Branch".equalsIgnoreCase(currentOrg.getType())) {
               branch = currentOrg.getName();
               branchId = currentOrg.getOrganizationId();
               organizationId = branchId;
            } else if ("Zone".equalsIgnoreCase(currentOrg.getType())) {
               zone = currentOrg.getName();
               zoneId = currentOrg.getOrganizationId();
               organizationId = zoneId;
            } else if ("NBG".equalsIgnoreCase(currentOrg.getType())) {
               nbg = currentOrg.getName();
               nbgId = currentOrg.getOrganizationId();
               organizationId = nbgId;
            }

            Organization parent;
            for(long parentId = currentOrg.getParentOrganizationId(); parentId > 0L; parentId = parent.getParentOrganizationId()) {
               parent = OrganizationLocalServiceUtil.getOrganization(parentId);
               _log.info("Traversing parent org - Type: " + parent.getType() + ", Name: " + parent.getName());
               if ("Branch".equalsIgnoreCase(parent.getType()) && branch.isEmpty()) {
                  branch = parent.getName();
                  branchId = parent.getOrganizationId();
               } else if ("Zone".equalsIgnoreCase(parent.getType()) && zone.isEmpty()) {
                  zone = parent.getName();
                  zoneId = parent.getOrganizationId();
               } else if ("NBG".equalsIgnoreCase(parent.getType()) && nbg.isEmpty()) {
                  nbg = parent.getName();
                  nbgId = parent.getOrganizationId();
               }
            }
         }

         json.put("fullName", fullName);
         json.put("email", email);
         json.put("screenName", screenName);
         json.put("organizationId", organizationId);
         json.put("image", userImage);
         json.put("branch", branch);
         json.put("zone", zone);
         json.put("nbg", nbg);
         json.put("branchId", branchId);
         json.put("zoneId", zoneId);
         json.put("nbgId", nbgId);
         json.put("roleNames", roleNames);
         _log.info("JSON response prepared successfully");
      } catch (Exception e) {
         _log.error("User fetch error: ", e);
         json.put("error", "User not found with screen name: " + screenName);
      }

      try {
         resourceResponse.setContentType("application/json");
         resourceResponse.getWriter().write(json.toString());
         _log.info("JSON response sent successfully");
      } catch (IOException e) {
         throw new PortletException("Failed to write JSON response", e);
      }
   }
}
