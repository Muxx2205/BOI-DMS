package in.co.boi.branchadmin.web.render;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/edit-nbg"},
   service = {MVCRenderCommand.class}
)
public class EditNBGRenderCommand implements MVCRenderCommand {
   private static final Log _log = LogFactoryUtil.getLog(EditNBGRenderCommand.class);

   public String render(RenderRequest renderRequest, RenderResponse renderResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long companyId = themeDisplay.getCompanyId();
      long organizationId = ParamUtil.getLong(renderRequest, "id");
      _log.info("Rendering Edit NBG page for organizationId=" + organizationId + ", companyId=" + companyId);
      Organization organization = null;

      try {
         organization = OrganizationLocalServiceUtil.getOrganization(organizationId);
         String var10001 = organization != null ? organization.getName() : "null";
         _log.debug("Loaded NBG organization: " + var10001);
      } catch (PortalException e) {
         _log.error("Error fetching organization for ID: " + organizationId, e);
      }

      renderRequest.setAttribute("organization", organization);
      _log.info("EditNBGRenderCommand completed successfully for organizationId=" + organizationId);
      return "/organization/add-nbg.jsp";
   }
}
