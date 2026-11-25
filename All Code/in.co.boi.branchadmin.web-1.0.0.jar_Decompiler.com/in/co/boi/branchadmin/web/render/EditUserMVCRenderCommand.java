package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/editUser"},
   service = {MVCRenderCommand.class}
)
public class EditUserMVCRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(EditUserMVCRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      long userId = ParamUtil.getLong(renderRequest, "userId");
      _log.info("Editing user with ID: " + userId);

      try {
         User user = UserLocalServiceUtil.getUser(userId);
         if (user != null) {
            _log.debug("User screenName: " + user.getScreenName());
            _log.debug("User email: " + user.getEmailAddress());
            _log.debug("User firstName: " + user.getFirstName());
            renderRequest.setAttribute("userInfo", user);
         } else {
            _log.warn("No user found for ID: " + userId);
         }
      } catch (Exception e) {
         _log.error("Error fetching user with ID: " + userId, e);
      }

      return "/user/add_user.jsp";
   }
}
