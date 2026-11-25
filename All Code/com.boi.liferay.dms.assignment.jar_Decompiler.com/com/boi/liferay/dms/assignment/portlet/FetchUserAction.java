package com.boi.liferay.dms.assignment.portlet;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   immediate = true,
   configurationPid = {"com.boi.liferay.dms.assignment.portlet.RolesAllowToAssignment"},
   property = {"javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "mvc.command.name=fetchUser"},
   service = {MVCActionCommand.class}
)
public class FetchUserAction implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(FetchUserAction.class);
   private RolesAllowToAssignment allowToAssignment;

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String userName = ParamUtil.getString(actionRequest, "userName");
      ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      log.info("Fetch user Details " + userName);

      try {
         User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), userName);
         actionRequest.setAttribute("requestedUser", user);
         actionRequest.setAttribute("userName", userName);
         actionRequest.setAttribute("allowToAssignment", this.allowToAssignment.roles());
      } catch (PortalException e) {
         e.printStackTrace();
         SessionErrors.add(actionRequest, "unexpectedError");
      }

      return false;
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.allowToAssignment = (RolesAllowToAssignment)ConfigurableUtil.createConfigurable(RolesAllowToAssignment.class, properties);
   }
}
