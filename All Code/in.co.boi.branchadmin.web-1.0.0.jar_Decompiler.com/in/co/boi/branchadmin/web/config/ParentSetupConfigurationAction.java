package in.co.boi.branchadmin.web.config;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet"},
   service = {ConfigurationAction.class}
)
public class ParentSetupConfigurationAction extends DefaultConfigurationAction {
   private static final Log _log = LogFactoryUtil.getLog(ParentSetupConfigurationAction.class);

   public void processAction(PortletConfig portletConfig, ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      String portletResource = ParamUtil.getString(actionRequest, "portletResource");
      String parentFolderName = ParamUtil.getString(actionRequest, "parentFolderName");
      String parentOrganizationName = ParamUtil.getString(actionRequest, "parentOrganizationName");
      _log.info("Saving configuration for portlet: " + portletResource);
      _log.info("Parent Folder Name: " + parentFolderName);
      _log.info("Parent Organization Name: " + parentOrganizationName);
      this.setPreference(actionRequest, "parentFolderName", parentFolderName);
      this.setPreference(actionRequest, "parentOrganizationName", parentOrganizationName);
      super.processAction(portletConfig, actionRequest, actionResponse);
   }
}
