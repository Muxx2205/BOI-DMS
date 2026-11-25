package in.co.boi.branchadmin.web.action;

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/assignRoleToUser"},
   service = {MVCActionCommand.class}
)
public class AssignRoleMVCActionCommand extends BaseMVCActionCommand {
   @Reference
   private UserLocalService userLocalService;
   @Reference
   private OrganizationLocalService organizationLocalService;
   @Reference
   private RoleLocalService roleLocalService;

   protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      long userId = ParamUtil.getLong(actionRequest, "userId");
      long branchId = ParamUtil.getLong(actionRequest, "branchId");
      long roleId = ParamUtil.getLong(actionRequest, "roleId");

      for(User u : UserLocalServiceUtil.getOrganizationUsers(branchId)) {
         u.getRoles().removeIf((r) -> r.getRoleId() == roleId);
      }

      this.roleLocalService.addUserRole(userId, roleId);
      SessionMessages.add(actionRequest, "roleAssignedSuccess");
   }
}
