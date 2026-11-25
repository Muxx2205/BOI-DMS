package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import java.util.Collections;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/view-users"},
   service = {MVCRenderCommand.class}
)
public class ViewUsersMVCRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(ViewUsersMVCRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      List<User> users = Collections.emptyList();

      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(renderRequest);
         long companyId = serviceContext.getCompanyId();
         _log.info("Fetching users for companyId: " + companyId);
         users = UserLocalServiceUtil.getUsers(companyId, 0, -1, -1, (OrderByComparator)null);
         _log.info("Total users fetched: " + users.size());
      } catch (Exception e) {
         _log.error("Error fetching users", e);
      }

      renderRequest.setAttribute("users", users);
      return "/user/view-users.jsp";
   }
}
