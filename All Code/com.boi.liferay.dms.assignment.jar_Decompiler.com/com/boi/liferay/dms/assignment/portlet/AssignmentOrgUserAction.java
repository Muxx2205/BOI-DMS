package com.boi.liferay.dms.assignment.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "mvc.command.name=assignmentOrgWithUser"},
   service = {MVCActionCommand.class}
)
public class AssignmentOrgUserAction implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(AssignmentOrgUserAction.class);

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String keyword = ParamUtil.getString(actionRequest, "organizationData");
      long[] selectedKeys = ParamUtil.getLongValues(actionRequest, "selectedKeys");
      long[] removeOrganition = ParamUtil.getLongValues(actionRequest, "removeOrganition");
      String userName = ParamUtil.getString(actionRequest, "userName");
      ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      log.info("Fetch user Details " + userName);
      log.info("Fetch selectedKeys Details " + selectedKeys.length);
      log.info("Fetch removeOrganition Details " + removeOrganition.length);

      try {
         User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), userName);

         for(long key : selectedKeys) {
            if (key != 0L) {
               log.info("Fetch key Details " + key);
               UserLocalServiceUtil.addOrganizationUser(key, user.getUserId());
            }
         }

         for(long removeKey : removeOrganition) {
            if (removeKey != 0L) {
               log.info("Fetch removeKey Details " + removeKey);
               UserLocalServiceUtil.deleteOrganizationUser(removeKey, user);
            }
         }

         SessionMessages.add(actionRequest, "requestProcessed");
         actionRequest.setAttribute("requestedUser", user);
         actionRequest.setAttribute("userName", userName);
         LiferayPortletResponse liferayPortletResponse = PortalUtil.getLiferayPortletResponse(actionResponse);
         PortletURL renderURL = liferayPortletResponse.createRenderURL();
         renderURL.setParameter("mvcPath", "/organization_assignment.jsp");
         renderURL.setParameter("userName", userName);
         renderURL.setParameter("organizationData", keyword);
         actionResponse.sendRedirect(renderURL.toString());
         SessionMessages.add(actionRequest, "requestProcessed");
      } catch (Exception e) {
         e.printStackTrace();
         log.error(e);
         SessionErrors.add(actionRequest, "requestFailed");
      }

      return false;
   }
}
