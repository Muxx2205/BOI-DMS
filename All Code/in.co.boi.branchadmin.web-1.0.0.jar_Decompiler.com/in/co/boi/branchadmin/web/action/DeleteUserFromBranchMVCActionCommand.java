package in.co.boi.branchadmin.web.action;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/deleteUserFromBranch"},
   service = {MVCActionCommand.class}
)
public class DeleteUserFromBranchMVCActionCommand extends BaseMVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(DeleteUserFromBranchMVCActionCommand.class);
   @Reference
   private UserLocalService userLocalService;
   @Reference
   private OrganizationLocalService organizationLocalService;

   protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      long userId = ParamUtil.getLong(actionRequest, "userId");
      long branchId = ParamUtil.getLong(actionRequest, "branchId");

      try {
         this.organizationLocalService.deleteUserOrganization(userId, branchId);
         _log.info("User " + userId + " removed from branch " + branchId);
         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/branch/assign_user_list");
         SessionMessages.add(actionRequest, "userRemovedSuccess");
      } catch (Exception e) {
         _log.error("Error removing user " + userId + " from branch " + branchId, e);
         SessionErrors.add(actionRequest, "userRemovedError");
      }

   }
}
