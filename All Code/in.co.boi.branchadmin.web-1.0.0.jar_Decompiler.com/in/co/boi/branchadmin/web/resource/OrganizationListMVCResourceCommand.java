package in.co.boi.branchadmin.web.resource;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/organization/get_zones"},
   service = {MVCResourceCommand.class}
)
public class OrganizationListMVCResourceCommand implements MVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(OrganizationListMVCResourceCommand.class);
   @Reference
   private OrganizationLocalService organizationLocalService;

   public boolean serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException {
      JSONArray zonesArray = JSONFactoryUtil.createJSONArray();

      try {
         long companyId = PortalUtil.getCompanyId(resourceRequest);
         long nbgId = ParamUtil.getLong(resourceRequest, "nbgId");
         _log.info("Fetching zones for NBG ID: " + nbgId + " in company: " + companyId);

         for(Organization zone : this.organizationLocalService.getSuborganizations(companyId, nbgId)) {
            JSONObject zoneJson = JSONFactoryUtil.createJSONObject();
            zoneJson.put("id", zone.getOrganizationId());
            zoneJson.put("name", zone.getName());
            zonesArray.put(zoneJson);
            Log var10000 = _log;
            String var10001 = zone.getName();
            var10000.info("Found zone: " + var10001 + " [ID: " + zone.getOrganizationId() + "]");
         }

         resourceResponse.setContentType("application/json");
         resourceResponse.getWriter().write(zonesArray.toString());
         return true;
      } catch (Exception e) {
         _log.error("Error fetching zones", e);
         throw new PortletException("Error fetching zones", e);
      }
   }
}
