package in.co.boi.branchadmin.web.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/deleteUser"},
   service = {MVCActionCommand.class}
)
public class DeleteUserMVCActionCommand extends BaseMVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(DeleteUserMVCActionCommand.class);

   protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      long userId = ParamUtil.getLong(actionRequest, "userId");

      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(actionRequest);
         UserLocalServiceUtil.updateStatus(userId, 1, serviceContext);
         _log.info("User " + userId + " successfully deactivated");
         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/view-users");
         SessionMessages.add(actionRequest, "userDeletedSuccess");
      } catch (Exception e) {
         _log.error("Error deleting user with userId: " + userId, e);
         SessionErrors.add(actionRequest, "userDeletedError");
      }

   }
}
