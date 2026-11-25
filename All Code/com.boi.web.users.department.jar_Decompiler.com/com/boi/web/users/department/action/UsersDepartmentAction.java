package com.boi.web.users.department.action;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   property = {"javax.portlet.name=com_boi_web_users_department_UsersDepartmentPortlet", "mvc.command.name=saveIt"},
   service = {MVCActionCommand.class}
)
public class UsersDepartmentAction implements MVCActionCommand {
   private BOIWorkflowConfiguation boiWorkflowConfiguation;

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String userScreenName = ParamUtil.getString(actionRequest, "userScreenName");
      String departmentId = ParamUtil.getString(actionRequest, "departmentId");
      long organizationId = ParamUtil.getLong(actionRequest, "organizationId");
      long folderId = ParamUtil.getLong(actionRequest, "folderId");
      ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      User user = themeDisplay.getUser();

      try {
         User masterUser = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), userScreenName);
         System.out.println("User found " + userScreenName);
         Organization organization = OrganizationLocalServiceUtil.getOrganization(organizationId);
         System.out.println("organization found " + organization.getName());
         DLFolder folder = DLFolderLocalServiceUtil.getFolder(folderId);
         System.out.println("folder found " + folder.getName());
         long[] organizations = masterUser.getOrganizationIds();
         PrintStream var10000 = System.out;
         Integer var10001 = Validator.isNotNull(organizations) ? organizations.length : null;
         var10000.println("User belong to total orgination " + var10001);
         OrganizationLocalServiceUtil.deleteUserOrganizations(masterUser.getUserId(), organizations);
         var10000 = System.out;
         var10001 = Validator.isNotNull(organizations) ? organizations.length : null;
         var10000.println("Removing from orginations " + var10001);
         OrganizationLocalServiceUtil.addUserOrganization(masterUser.getUserId(), organization);
         var10000 = System.out;
         var10001 = Validator.isNotNull(organizations) ? organizations.length : null;
         var10000.println("Add User to Organization " + var10001);
         OrganizationLocalServiceUtil.rebuildTree(organization.getCompanyId());
         System.out.println("Rebiuld the organization structure");
         Map<String, Serializable> values = new HashMap();
         values.put("userScreenName", userScreenName);
         values.put("departmentId", departmentId);
         values.put("organizationId", organizationId);
         values.put("folderId", folder.getFolderId());
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(user.getCompanyId());
         serviceContext.setUserId(user.getUserId());
         serviceContext.setWorkflowAction(1);
         ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, user.getUserId(), 0L, this.boiWorkflowConfiguation.UserDepartmentObjectDefinitionId(), values, serviceContext);
         System.out.println("Entry Saved into object ");
         return false;
      } catch (Exception e1) {
         e1.printStackTrace();
         SessionErrors.add(actionRequest, e1.getClass());
         return false;
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
   }
}
