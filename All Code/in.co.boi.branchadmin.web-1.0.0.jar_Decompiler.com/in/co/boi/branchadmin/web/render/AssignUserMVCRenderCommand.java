package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserModel;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/branch/assign-user"},
   service = {MVCRenderCommand.class}
)
public class AssignUserMVCRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(AssignUserMVCRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      long branchId = ParamUtil.getLong(renderRequest, "branchId");
      _log.info("Rendering Assign User page for Branch ID: " + branchId);
      List<User> allUsers = Collections.emptyList();
      List<User> branchUsers = Collections.emptyList();
      List<User> unassignedUsers = Collections.emptyList();

      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(User.class.getName(), renderRequest);
         long companyId = serviceContext.getCompanyId();
         _log.debug("Company ID: " + companyId);
         allUsers = UserLocalServiceUtil.getUsers(companyId, 0, -1, -1, (OrderByComparator)null);
         _log.debug("Total users fetched: " + allUsers.size());
         branchUsers = UserLocalServiceUtil.getOrganizationUsers(branchId);
         _log.debug("Branch users fetched: " + branchUsers.size());
         Set<Long> branchUserIds = (Set)branchUsers.stream().map(UserModel::getUserId).collect(Collectors.toSet());
         _log.debug("Branch user IDs collected: " + branchUserIds.size());
         unassignedUsers = allUsers.stream().filter((user) -> !branchUserIds.contains(user.getUserId())).toList();
         _log.info("Unassigned users available for branch: " + unassignedUsers.size());
      } catch (PortalException e) {
         _log.error("Error fetching ServiceContext for AssignUserMVCRenderCommand", e);
      } catch (Exception e) {
         _log.error("Unexpected error in AssignUserMVCRenderCommand for branchId: " + branchId, e);
      }

      renderRequest.setAttribute("branchId", branchId);
      renderRequest.setAttribute("branchUsers", branchUsers);
      renderRequest.setAttribute("users", unassignedUsers);
      _log.info("Completed rendering Assign User page for Branch ID: " + branchId);
      return "/organization/assign_user.jsp";
   }
}
