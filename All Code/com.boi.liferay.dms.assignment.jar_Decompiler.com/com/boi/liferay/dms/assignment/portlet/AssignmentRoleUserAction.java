package com.boi.liferay.dms.assignment.portlet;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   immediate = true,
   configurationPid = {"com.boi.liferay.dms.assignment.portlet.RolesAllowToAssignment"},
   property = {"javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "mvc.command.name=assignmentRoleWithUser"},
   service = {MVCActionCommand.class}
)
public class AssignmentRoleUserAction implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(AssignmentRoleUserAction.class);
   private RolesAllowToAssignment allowToAssignment;

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String[] selectedKeys = ParamUtil.getParameterValues(actionRequest, "selectedKeys");
      long[] removeRoles = ParamUtil.getLongValues(actionRequest, "removeRoles");
      String userName = ParamUtil.getString(actionRequest, "userName");
      ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      log.info("Fetch user Details " + userName);

      try {
         User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), userName);

         for(String key : selectedKeys) {
            log.info("Selected Key (Role Name): " + key);

            try {
               if (!key.equalsIgnoreCase("false")) {
                  Role role = RoleLocalServiceUtil.getRole(themeDisplay.getCompanyId(), key);
                  if (role.getType() == 1) {
                     RoleLocalServiceUtil.addUserRoles(user.getUserId(), new long[]{role.getRoleId()});
                  }
               }
            } catch (Exception e) {
               SessionErrors.add(actionRequest, "requestFailed");
               log.error("Failed to assign role: " + key);
               e.printStackTrace();
            }
         }

         for(long key : removeRoles) {
            log.info("Selected Key (Role Name): " + key);

            try {
               if (key != 0L) {
                  Role role = RoleLocalServiceUtil.getRole(key);
                  if (role.getType() == 1) {
                     RoleLocalServiceUtil.deleteUserRole(user.getUserId(), key);
                  }
               }
            } catch (Exception e) {
               SessionErrors.add(actionRequest, "requestFailed");
               log.error("Failed to assign role: " + key);
               e.printStackTrace();
            }
         }

         actionRequest.setAttribute("requestedUser", user);
         actionRequest.setAttribute("userName", userName);
         actionRequest.setAttribute("allowToAssignment", this.allowToAssignment.roles());
         LiferayPortletResponse liferayPortletResponse = PortalUtil.getLiferayPortletResponse(actionResponse);
         PortletURL renderURL = liferayPortletResponse.createRenderURL();
         renderURL.setParameter("mvcPath", "/role_assignment.jsp");
         renderURL.setParameter("userName", userName);
         actionResponse.sendRedirect(renderURL.toString());
         SessionMessages.add(actionRequest, "requestProcessed");
         return false;
      } catch (Exception e) {
         SessionErrors.add(actionRequest, "requestFailed");
         e.printStackTrace();
         return false;
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.allowToAssignment = (RolesAllowToAssignment)ConfigurableUtil.createConfigurable(RolesAllowToAssignment.class, properties);
   }
}
