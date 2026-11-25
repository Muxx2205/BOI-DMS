package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/assignRoleToUser"},
   service = {MVCRenderCommand.class}
)
public class AssignRolePopupMVCRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(AssignRolePopupMVCRenderCommand.class);
   @Reference
   private RoleLocalService roleLocalService;
   @Reference
   private OrganizationLocalService organizationLocalService;

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      long userId = ParamUtil.getLong(renderRequest, "userId");
      long branchId = ParamUtil.getLong(renderRequest, "branchId");
      _log.info("Rendering Assign Role popup for User ID: " + userId + ", Branch ID: " + branchId);
      User userInfo = null;
      List<Role> rolesToAssign = Collections.emptyList();

      try {
         userInfo = UserLocalServiceUtil.getUser(userId);
         _log.debug("Fetched user: " + userInfo.getFullName());
         List<Role> allRoles = this.roleLocalService.getRoles(-1, -1);
         _log.debug("Total available roles: " + allRoles.size());
         List<User> branchUsers = UserLocalServiceUtil.getOrganizationUsers(branchId);
         _log.debug("Total users in branch: " + branchUsers.size());
         Set<Long> assignedRoleIds = (Set)branchUsers.stream().flatMap((u) -> u.getRoles().stream()).map(RoleModel::getRoleId).collect(Collectors.toSet());
         _log.debug("Total assigned role IDs in branch: " + assignedRoleIds.size());
         rolesToAssign = allRoles.stream().filter((r) -> !assignedRoleIds.contains(r.getRoleId())).toList();
         _log.info("Roles available for assignment: " + rolesToAssign.size());
      } catch (PortalException e) {
         _log.error("Error retrieving user or organization data for userId: " + userId, e);
      } catch (Exception e) {
         _log.error("Unexpected error during AssignRolePopupMVCRenderCommand execution", e);
      }

      renderRequest.setAttribute("userId", userId);
      renderRequest.setAttribute("userInfo", userInfo);
      renderRequest.setAttribute("branchId", branchId);
      renderRequest.setAttribute("rolesToAssign", rolesToAssign);
      _log.info("Assign Role popup render complete.");
      return "/organization/user-role.jsp";
   }
}
