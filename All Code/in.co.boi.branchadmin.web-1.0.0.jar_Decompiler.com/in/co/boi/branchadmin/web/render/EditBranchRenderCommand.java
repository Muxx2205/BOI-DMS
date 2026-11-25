package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Collections;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/edit-branch"},
   service = {MVCRenderCommand.class}
)
public class EditBranchRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(EditBranchRenderCommand.class);
   @Reference
   private UserLocalService userLocalService;
   @Reference
   private OrganizationLocalService organizationLocalService;

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long companyId = themeDisplay.getCompanyId();
      long organizationId = ParamUtil.getLong(renderRequest, "id");
      _log.info("Rendering Edit Branch page for organizationId=" + organizationId + ", companyId=" + companyId);
      Organization organization = null;
      Organization parentOrganization = null;
      List<User> branchUsers = Collections.emptyList();

      try {
         organization = this.organizationLocalService.getOrganization(organizationId);
         _log.debug("Loaded organization: " + organization.getName());
         parentOrganization = this.organizationLocalService.getOrganization(organization.getParentOrganizationId());
         _log.debug("Parent organization: " + parentOrganization.getName());
         branchUsers = this.userLocalService.getOrganizationUsers(organizationId);
         _log.info("Number of users in this branch: " + branchUsers.size());
      } catch (PortalException e) {
         _log.error("Error fetching branch or parent organization for ID: " + organizationId, e);
      }

      List<Organization> nbgList = Collections.emptyList();
      List<Organization> allOrganizations = OrganizationLocalServiceUtil.getOrganizations(companyId, 0L);
      if (!allOrganizations.isEmpty()) {
         nbgList = OrganizationLocalServiceUtil.getSuborganizations(companyId, ((Organization)allOrganizations.get(0)).getOrganizationId());
         _log.debug("Fetched " + nbgList.size() + " suborganizations for NBG list");
      }

      renderRequest.setAttribute("organization", organization);
      renderRequest.setAttribute("parentOrganization", parentOrganization);
      renderRequest.setAttribute("branchUsers", branchUsers);
      renderRequest.setAttribute("nbgList", nbgList);
      _log.info("EditBranchRenderCommand completed successfully for organizationId=" + organizationId);
      return "/organization/add-branch.jsp";
   }
}
