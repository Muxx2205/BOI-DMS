package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Collections;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/branch/assign_user_list"},
   service = {MVCRenderCommand.class}
)
public class AssignUserListMVCRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(AssignUserListMVCRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      long branchId = ParamUtil.getLong(renderRequest, "branchId");
      _log.info("Rendering Assign User List for Branch ID: " + branchId);
      List<User> users = Collections.emptyList();
      List<User> branchUsers = Collections.emptyList();

      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(User.class.getName(), renderRequest);
         long companyId = serviceContext.getCompanyId();
         _log.debug("Company ID: " + companyId);
         users = UserLocalServiceUtil.getUsers(companyId, 0, -1, -1, (OrderByComparator)null);
         _log.debug("Total users fetched: " + users.size());
         branchUsers = UserLocalServiceUtil.getOrganizationUsers(branchId);
         _log.debug("Users already assigned to branch: " + branchUsers.size());
      } catch (PortalException e) {
         _log.error("Error while fetching ServiceContext or user data for branch ID: " + branchId, e);
      } catch (Exception e) {
         _log.error("Unexpected error in AssignUserListMVCRenderCommand", e);
      }

      renderRequest.setAttribute("branchId", branchId);
      renderRequest.setAttribute("users", users);
      renderRequest.setAttribute("branchUsers", branchUsers);
      _log.info("Assign User List render complete for Branch ID: " + branchId);
      return "/organization/assign-user-list.jsp";
   }
}
