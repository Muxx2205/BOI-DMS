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
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ListTypeLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/branch/create_branch_organization"},
   service = {MVCActionCommand.class}
)
public class CreateBranchOrganizationMVCActionCommand implements MVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(CreateBranchOrganizationMVCActionCommand.class);
   private static final String[] BRANCH_SUBFOLDERS = new String[]{"UPI_Insurance", "Finance_Ploe", "Digital_Banking_Department", "DBD_Mandate_Cancellation", "Account Opening", "CPD_Insurance_Claim", "FBD", "FE_BO", "FE_BO_NON_FINANCIAL"};

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) {
      try {
         ServiceContext serviceContext = ServiceContextFactory.getInstance(actionRequest);
         long companyId = PortalUtil.getCompanyId(actionRequest);
         long creatorUserId = PortalUtil.getUserId(actionRequest);
         long groupId = serviceContext.getScopeGroupId();
         String parentFolderName = actionRequest.getPreferences().getValue("parentFolderName", "BOI Document");
         String parentOrganizationName = actionRequest.getPreferences().getValue("parentOrganizationName", "BOI HO");
         _log.info("Using configuration - Parent Folder: " + parentFolderName + ", Parent Organization: " + parentOrganizationName);
         long branchId = ParamUtil.getLong(actionRequest, "branchId");
         long nbgId = ParamUtil.getLong(actionRequest, "nbgId");
         long zoneId = ParamUtil.getLong(actionRequest, "zoneId");
         String branchName = ParamUtil.getString(actionRequest, "branchName");
         ListType defaultStatus = ListTypeLocalServiceUtil.getListType(companyId, "full-member", "com.liferay.portal.kernel.model.Organization.status");
         long statusId = defaultStatus.getListTypeId();
         Organization nbg = OrganizationLocalServiceUtil.getOrganization(nbgId);
         Organization zone = OrganizationLocalServiceUtil.getOrganization(zoneId);
         if (branchId > 0L) {
            this.updateExistingBranch(branchId, branchName, zoneId, nbg, zone, groupId, serviceContext, parentFolderName);
         } else {
            this.createNewBranch(branchName, creatorUserId, zoneId, statusId, nbg, zone, groupId, serviceContext, parentFolderName);
         }

         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/view-branch");
         return true;
      } catch (Exception e) {
         _log.error("Error processing branch creation", e);
         return false;
      }
   }

   private void updateExistingBranch(long branchId, String branchName, long zoneId, Organization nbg, Organization zone, long groupId, ServiceContext serviceContext, String parentFolderName) throws PortalException {
      Organization branchOrg = OrganizationLocalServiceUtil.getOrganization(branchId);
      Organization preZone = OrganizationLocalServiceUtil.getOrganization(branchOrg.getParentOrganizationId());
      Organization preNbg = OrganizationLocalServiceUtil.getOrganization(preZone.getParentOrganizationId());
      DLFolder branchFolder = this.getBranchFolder(groupId, preNbg, preZone, nbg, zone, branchOrg.getName(), parentFolderName);
      branchOrg.setParentOrganizationId(zoneId);
      branchOrg.setName(branchName);
      OrganizationLocalServiceUtil.updateOrganization(branchOrg);
      if (branchFolder != null) {
         DLFolderLocalServiceUtil.updateFolder(branchFolder.getFolderId(), branchFolder.getParentFolderId(), branchName, "Auto-updated branch folder", 0L, (List)null, 0, serviceContext);
         _log.info("Updated folder for existing branch: " + branchName);
      }

   }

   private void createNewBranch(String branchName, long creatorUserId, long zoneId, long statusId, Organization nbg, Organization zone, long groupId, ServiceContext serviceContext, String parentFolderName) throws PortalException {
      Organization newBranch = OrganizationLocalServiceUtil.addOrganization((String)null, creatorUserId, zoneId, branchName, "BRANCH", 0L, 0L, statusId, "Branch Organization", false, serviceContext);
      _log.info("Created new branch organization: " + branchName + " with ID: " + newBranch.getOrganizationId());
      DLFolder zoneFolder = this.getZoneFolder(groupId, nbg, zone, parentFolderName);
      if (zoneFolder != null) {
         this.createBranchAndSubFolders(zoneFolder, branchName, serviceContext);
      } else {
         _log.warn("Zone folder not found for branch: " + branchName);
      }

   }

   private DLFolder getBranchFolder(long groupId, Organization preNbg, Organization preZone, Organization nbg, Organization zone, String branchName, String parentFolderName) {
      try {
         _log.info("Looking for branch folder with config - Parent Folder: " + parentFolderName + ", Parent Org: ");
         String[] folderPathParts = parentFolderName.split("/");
         DLFolder currentFolder = null;
         long parentFolderId = 0L;

         for(String folderName : folderPathParts) {
            String trimmedName = folderName.trim();
            if (!trimmedName.isEmpty()) {
               try {
                  currentFolder = DLFolderLocalServiceUtil.getFolder(groupId, parentFolderId, trimmedName);
                  parentFolderId = currentFolder.getFolderId();
                  _log.debug("Navigated to folder: " + trimmedName + " (ID: " + parentFolderId + ")");
               } catch (Exception e) {
                  _log.error("Folder not found: " + trimmedName + " under parent ID: " + parentFolderId, e);
                  return null;
               }
            }
         }

         DLFolder preNbgFolder = DLFolderLocalServiceUtil.getFolder(groupId, parentFolderId, preNbg.getName().trim());
         DLFolder preZoneFolder = DLFolderLocalServiceUtil.getFolder(groupId, preNbgFolder.getFolderId(), preZone.getName().trim());
         DLFolder branchFolder = DLFolderLocalServiceUtil.getFolder(groupId, preZoneFolder.getFolderId(), branchName.trim());
         DLFolder nbgFolder = DLFolderLocalServiceUtil.getFolder(groupId, parentFolderId, nbg.getName().trim());
         DLFolder zoneFolder = DLFolderLocalServiceUtil.getFolder(groupId, nbgFolder.getFolderId(), zone.getName().trim());
         if (branchFolder != null) {
            branchFolder.setParentFolderId(zoneFolder.getFolderId());
         }

         return branchFolder;
      } catch (Exception e) {
         _log.warn("Branch folder not found for: " + branchName + " â†’ " + e.getMessage());
         return null;
      }
   }

   private DLFolder getZoneFolder(long groupId, Organization nbg, Organization zone, String parentFolderName) {
      try {
         _log.info("Looking for zone folder with config - Parent Folder: " + parentFolderName + ", Parent Org: ");
         String[] parts = parentFolderName.split("/");
         DLFolder boiFolder = DLFolderLocalServiceUtil.getFolder(groupId, 0L, parts[0]);
         if (parts.length > 1) {
            boiFolder = DLFolderLocalServiceUtil.getFolder(groupId, boiFolder.getFolderId(), parts[parts.length - 1]);
         }

         DLFolder nbgFolder = DLFolderLocalServiceUtil.getFolder(groupId, boiFolder.getFolderId(), nbg.getName().trim());
         return DLFolderLocalServiceUtil.getFolder(groupId, nbgFolder.getFolderId(), zone.getName().trim());
      } catch (Exception e) {
         _log.error("Unable to fetch zone folder for: " + zone.getName(), e);
         return null;
      }
   }

   private void createBranchAndSubFolders(DLFolder zoneFolder, String branchName, ServiceContext serviceContext) throws PortalException {
      long groupId = zoneFolder.getGroupId();
      Folder branchFolder = null;

      try {
         branchFolder = DLAppLocalServiceUtil.addFolder((String)null, serviceContext.getUserId(), groupId, zoneFolder.getFolderId(), branchName, "Auto-created folder for " + branchName, serviceContext);
         _log.info("Created branch folder: " + branchName + " under zone folder: " + zoneFolder.getName());
      } catch (Exception e) {
         _log.warn("Could not create branch folder " + branchName + ": " + e.getMessage());
         return;
      }

      if (branchFolder != null) {
         for(String subFolderName : BRANCH_SUBFOLDERS) {
            try {
               DLAppLocalServiceUtil.addFolder((String)null, serviceContext.getUserId(), groupId, branchFolder.getFolderId(), subFolderName, "Auto-created folder for " + subFolderName, serviceContext);
               _log.info("Created subfolder: " + subFolderName + " under branch: " + branchName);
            } catch (Exception e) {
               _log.warn("Could not create subfolder " + subFolderName + ": " + e.getMessage());
            }
         }
      }

   }
}
