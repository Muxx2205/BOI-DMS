package in.co.boi.branchadmin.web.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

@Component(
   configurationPid = {"in.co.boi.branchadmin.web.config.ParentSetupConfiguration"},
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "javax.portlet.display-name=Branch Admin", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/branch-admin.jsp", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class BranchAdminWebPortlet extends MVCPortlet {
}
