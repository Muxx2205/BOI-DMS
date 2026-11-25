package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/add-branch"},
   service = {MVCRenderCommand.class}
)
public class AddBranchRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(AddBranchRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      _log.info("Rendering Add Branch page...");

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long companyId = themeDisplay.getCompanyId();
         _log.debug("Company ID: " + companyId);
         String parentOrganizationName = renderRequest.getPreferences().getValue("parentOrganizationName", "BOI HO");
         Organization organization = OrganizationLocalServiceUtil.getOrganization(companyId, parentOrganizationName);
         long parentOrgId = organization.getOrganizationId();
         _log.debug("Fetching sub-organizations for parent org ID: " + parentOrgId);
         List<Organization> nbgList = OrganizationLocalServiceUtil.getSuborganizations(companyId, parentOrgId);
         _log.info("Total NBG organizations found: " + nbgList.size());
         renderRequest.setAttribute("nbgList", nbgList);
      } catch (Exception e) {
         _log.error("Error while rendering Add Branch page", e);
         renderRequest.setAttribute("nbgList", List.of());
      }

      _log.info("Add Branch render command completed.");
      return "/organization/add-branch.jsp";
   }
}
