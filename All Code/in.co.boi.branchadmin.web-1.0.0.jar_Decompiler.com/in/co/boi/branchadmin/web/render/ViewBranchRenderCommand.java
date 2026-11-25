package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/view-branch"},
   service = {MVCRenderCommand.class}
)
public class ViewBranchRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(ViewBranchRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long companyId = themeDisplay.getCompanyId();
      _log.info("Rendering branches for companyId: " + companyId);
      List<Organization> branchOrgs = Collections.emptyList();

      try {
         List<Organization> allOrgs = OrganizationLocalServiceUtil.getOrganizations(companyId, 0L);
         if (!allOrgs.isEmpty()) {
            List<Organization> nbgList = OrganizationLocalServiceUtil.getSuborganizations(companyId, ((Organization)allOrgs.get(0)).getOrganizationId());
            branchOrgs = nbgList.stream().flatMap((nbg) -> this.getBranchesFromNBG(companyId, nbg)).toList();
         }
      } catch (Exception e) {
         _log.error("Error fetching branch organizations", e);
      }

      _log.info("Total branches fetched: " + branchOrgs.size());
      renderRequest.setAttribute("branchOrgs", branchOrgs);
      return "/organization/view-branch.jsp";
   }

   private Stream<Organization> getBranchesFromNBG(long companyId, Organization nbg) {
      try {
         List<Organization> zones = OrganizationLocalServiceUtil.getSuborganizations(companyId, nbg.getOrganizationId());
         return zones.stream().flatMap((zone) -> {
            try {
               List<Organization> branches = OrganizationLocalServiceUtil.getSuborganizations(companyId, zone.getOrganizationId());
               return branches.stream();
            } catch (Exception e) {
               _log.error("Error fetching branches for zone: " + zone.getName(), e);
               return Stream.empty();
            }
         });
      } catch (Exception e) {
         _log.error("Error fetching zones for NBG: " + nbg.getName(), e);
         return Stream.empty();
      }
   }
}
