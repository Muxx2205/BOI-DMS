package in.co.boi.branchadmin.web.action;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ListTypeLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/branch/create_nbg_organization"},
   service = {MVCActionCommand.class}
)
public class CreateNBGOrganizationMVCActionCommand implements MVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(CreateNBGOrganizationMVCActionCommand.class);

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) {
      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(actionRequest);
         long companyId = serviceContext.getCompanyId();
         long creatorUserId = serviceContext.getUserId();
         String nbgName = ParamUtil.getString(actionRequest, "nbgName");
         long nbgId = ParamUtil.getLong(actionRequest, "nbgId");
         Organization parentOrganization = this.getParentOrganization(companyId, actionRequest);
         if (parentOrganization == null) {
            return false;
         }

         ListType defaultStatus = ListTypeLocalServiceUtil.getListType(companyId, "full-member", "com.liferay.portal.kernel.model.Organization.status");
         long statusId = defaultStatus.getListTypeId();
         if (nbgId > 0L) {
            long parentFolderId = this.navigateToParentFolder(serviceContext, actionRequest);
            this.updateOrganizationFolder(serviceContext, parentFolderId, nbgName, nbgId);
            this.updateExistingOrganization(nbgId, nbgName);
         } else {
            this.createNewOrganization(serviceContext, parentOrganization, nbgName, creatorUserId, statusId, actionRequest);
         }

         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/view-nbg");
      } catch (Exception e) {
         _log.error("Error creating or updating NBG organization", e);
      }

      return true;
   }

   private void updateOrganizationFolder(ServiceContext serviceContext, long parentFolderId, String nbgName, long nbgId) {
      DLFolder folder = null;

      try {
         Organization organization = OrganizationLocalServiceUtil.getOrganization(nbgId);
         folder = DLFolderLocalServiceUtil.getFolder(serviceContext.getScopeGroupId(), parentFolderId, organization.getName());
         DLFolderLocalServiceUtil.updateFolder(folder.getFolderId(), parentFolderId, nbgName, "Auto-updated branch folder", 0L, (List)null, 0, serviceContext);
      } catch (PortalException e) {
         e.printStackTrace();
      }

   }

   private Organization getParentOrganization(long companyId, ActionRequest actionRequest) {
      String parentOrganizationName = actionRequest.getPreferences().getValue("parentOrganizationName", "BOI HO");

      try {
         return OrganizationLocalServiceUtil.getOrganization(companyId, parentOrganizationName);
      } catch (Exception ex) {
         _log.error("Parent organization '" + parentOrganizationName + "' not found for companyId: " + companyId, ex);
         return null;
      }
   }

   private void updateExistingOrganization(long nbgId, String nbgName) throws PortalException {
      Organization existingOrganization = OrganizationLocalServiceUtil.getOrganization(nbgId);
      existingOrganization.setName(nbgName);
      OrganizationLocalServiceUtil.updateOrganization(existingOrganization);
      _log.info("Updated NBG organization: " + nbgName + " (" + nbgId + ")");
   }

   private void createNewOrganization(ServiceContext serviceContext, Organization parentOrganization, String nbgName, long creatorUserId, long statusId, ActionRequest actionRequest) throws PortalException {
      long parentFolderId = this.navigateToParentFolder(serviceContext, actionRequest);
      this.createOrganizationFolder(serviceContext, parentFolderId, nbgName);
      OrganizationLocalServiceUtil.addOrganization(nbgName, creatorUserId, parentOrganization.getOrganizationId(), nbgName, "NBG", 0L, 0L, statusId, "NBG Organization", false, serviceContext);
      _log.info("Created new NBG organization: " + nbgName);
   }

   private long navigateToParentFolder(ServiceContext serviceContext, ActionRequest actionRequest) {
      String parentFolderName = actionRequest.getPreferences().getValue("parentFolderName", "BOI Document");
      String[] folderPathParts = parentFolderName.split("/");
      long parentFolderId = 0L;

      for(String folderName : folderPathParts) {
         String trimmedName = folderName.trim();
         if (!trimmedName.isEmpty()) {
            try {
               DLFolder currentFolder = DLFolderLocalServiceUtil.getFolder(serviceContext.getScopeGroupId(), parentFolderId, trimmedName);
               parentFolderId = currentFolder.getFolderId();
               _log.debug("Navigated to folder: " + trimmedName + " (ID: " + parentFolderId + ")");
            } catch (Exception e) {
               _log.error("Folder not found: " + trimmedName + " under parent ID: " + parentFolderId, e);
            }
         }
      }

      return parentFolderId;
   }

   private void createOrganizationFolder(ServiceContext serviceContext, long parentFolderId, String nbgName) throws PortalException {
      DLAppLocalServiceUtil.addFolder((String)null, serviceContext.getUserId(), serviceContext.getScopeGroupId(), parentFolderId, nbgName, "Auto-created folder for " + nbgName, serviceContext);
      _log.debug("Created folder for organization: " + nbgName);
   }
}
