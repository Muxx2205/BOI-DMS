package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import java.util.Collections;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/view-nbg"},
   service = {MVCRenderCommand.class}
)
public class ViewNBGRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(ViewNBGRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long companyId = themeDisplay.getCompanyId();
      _log.info("Fetching NBG organizations for companyId: " + companyId);
      List<Organization> nbgList = Collections.emptyList();

      try {
         List<Organization> allOrgs = OrganizationLocalServiceUtil.getOrganizations(companyId, 0L);
         if (!allOrgs.isEmpty()) {
            nbgList = OrganizationLocalServiceUtil.getSuborganizations(companyId, ((Organization)allOrgs.get(0)).getOrganizationId());
            _log.info("Total NBG organizations fetched: " + nbgList.size());
         } else {
            _log.warn("No organizations found for companyId: " + companyId);
         }
      } catch (Exception e) {
         _log.error("Error fetching NBG organizations", e);
      }

      renderRequest.setAttribute("nbgOrgs", nbgList);
      return "/organization/view-nbg.jsp";
   }
}
