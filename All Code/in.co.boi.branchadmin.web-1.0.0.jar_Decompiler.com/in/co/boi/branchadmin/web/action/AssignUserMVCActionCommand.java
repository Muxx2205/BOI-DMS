package in.co.boi.branchadmin.web.action;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import in.co.boi.branchadmin.web.model.UserOrganizationRecord;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/assignUsersToBranch"},
   service = {MVCActionCommand.class}
)
public class AssignUserMVCActionCommand extends BaseMVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(AssignUserMVCActionCommand.class);
   @Reference
   private UserLocalService userLocalService;
   @Reference
   private OrganizationLocalService organizationLocalService;
   @Reference
   private DTOConverterRegistry dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;

   protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      String parentFolderName = actionRequest.getPreferences().getValue("parentFolderName", "BOI Document");
      String parentOrganizationName = actionRequest.getPreferences().getValue("parentOrganizationName", "BOI HO");
      _log.info("Using configuration - Parent Folder: " + parentFolderName + ", Parent Organization: " + parentOrganizationName);
      ServiceContext serviceContext = ServiceContextFactory.getInstance(User.class.getName(), actionRequest);
      long branchId = ParamUtil.getLong(actionRequest, "branchId");
      String userIdsStr = ParamUtil.getString(actionRequest, "userIds");
      if (branchId > 0L && !userIdsStr.isEmpty()) {
         String[] userIdsArray = userIdsStr.split(",");
         _log.info("â–¶ Assigning users to branch ID: " + branchId + " | Total users: " + userIdsArray.length);

         for(String userIdStr : userIdsArray) {
            long userId = Long.parseLong(userIdStr.trim());

            try {
               User user = this.userLocalService.getUser(userId);
               Log var10000 = _log;
               String var10001 = user.getFullName();
               var10000.info("Processing user: " + var10001 + " (" + user.getScreenName() + ")");
               this.updateOrgIds(serviceContext, user, branchId, serviceContext.getScopeGroupId(), parentFolderName);

               for(Organization org : this.organizationLocalService.getUserOrganizations(userId)) {
                  this.organizationLocalService.deleteUserOrganization(userId, org.getOrganizationId());
                  _log.debug("Removed user from previous organization: " + org.getName());
               }

               this.organizationLocalService.addUserOrganization(userId, branchId);
               var10000 = _log;
               var10001 = user.getScreenName();
               var10000.info("Assigned user '" + var10001 + "' to branch organization ID: " + branchId);
            } catch (PortalException e) {
               _log.error(" Failed assigning user ID " + userId + " to branch " + branchId, e);
               SessionErrors.add(actionRequest, "userAssignmentFailed");
            }
         }

         SessionMessages.add(actionRequest, "usersAssignedSuccessfully");
         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/branch/assign_user_list");
         _log.info("All users processed successfully for branch: " + branchId);
      } else {
         SessionErrors.add(actionRequest, "invalid-branch-or-users");
         _log.error(" Invalid branchId or empty user list.");
      }
   }

   public List<UserOrganizationRecord> getUserDepartments(String query, User user, long objectDefinitionId) {
      List<UserOrganizationRecord> records = new ArrayList();

      try {
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
         ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this.dtoConverterRegistry, objectDefinition.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);

         for(ObjectEntry objectEntry : page.getItems()) {
            Map<String, Object> props = objectEntry.getProperties();
            if (props.containsKey("userScreenName") && props.containsKey("departmentId") && props.containsKey("folderId") && props.containsKey("organizationId")) {
               UserOrganizationRecord userOrganizationRecord = new UserOrganizationRecord(objectEntry.getId(), (Long)props.get("folderId"), String.valueOf(props.get("departmentId")), (Long)props.get("organizationId"), String.valueOf(props.get("userScreenName")));
               records.add(userOrganizationRecord);
               _log.debug("Found record for user: " + userOrganizationRecord.getScreenName());
            }
         }
      } catch (Exception e) {
         _log.error("Error retrieving user departments for query: " + query, e);
      }

      return records;
   }

   public void updateOrgIds(ServiceContext serviceContext, User user, long branchId, long scopeKey, String parentFolderName) {
      try {
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinitionByExternalReferenceCode("UsersDepartment", serviceContext.getCompanyId());
         OrganizationHierarchy orgHierarchy = this.getOrganizationHierarchy(branchId);
         DLFolder branchFolder = this.getBranchFolder(serviceContext, parentFolderName, orgHierarchy);
         if (branchFolder == null) {
            _log.error("Failed to get branch folder for: " + orgHierarchy.branch.getName());
            return;
         }

         this.processUserOrganizationRecords(serviceContext, user, branchId, scopeKey, objectDefinition, branchFolder);
      } catch (Exception e) {
         _log.error("Error updating organization records for user: " + user.getScreenName(), e);
      }

   }

   private OrganizationHierarchy getOrganizationHierarchy(long branchId) throws PortalException {
      Organization branch = this.organizationLocalService.getOrganization(branchId);
      Organization zone = this.organizationLocalService.getOrganization(branch.getParentOrganizationId());
      Organization nbg = this.organizationLocalService.getOrganization(zone.getParentOrganizationId());
      Log var10000 = _log;
      String var10001 = branch.getName();
      var10000.info("Building folder structure for branch: " + var10001 + " â†’ zone: " + zone.getName() + " â†’ nbg: " + nbg.getName());
      return new OrganizationHierarchy(branch, zone, nbg);
   }

   private DLFolder getBranchFolder(ServiceContext serviceContext, String parentFolderName, OrganizationHierarchy orgHierarchy) {
      try {
         long parentFolderId = this.navigateToParentFolder(serviceContext, parentFolderName);
         DLFolder nbgFolder = this.getSubFolder(serviceContext, parentFolderId, orgHierarchy.nbg.getName().trim());
         DLFolder zoneFolder = this.getSubFolder(serviceContext, nbgFolder.getFolderId(), orgHierarchy.zone.getName().trim());
         return this.getSubFolder(serviceContext, zoneFolder.getFolderId(), orgHierarchy.branch.getName().trim());
      } catch (Exception e) {
         _log.error("Error getting branch folder for: " + orgHierarchy.branch.getName(), e);
         return null;
      }
   }

   private long navigateToParentFolder(ServiceContext serviceContext, String parentFolderName) throws PortalException {
      String[] folderPathParts = parentFolderName.split("/");
      long parentFolderId = 0L;

      for(String folderName : folderPathParts) {
         String trimmedName = folderName.trim();
         if (!trimmedName.isEmpty()) {
            DLFolder currentFolder = DLFolderLocalServiceUtil.getFolder(serviceContext.getScopeGroupId(), parentFolderId, trimmedName);
            parentFolderId = currentFolder.getFolderId();
            _log.debug("Navigated to folder: " + trimmedName + " (ID: " + parentFolderId + ")");
         }
      }

      return parentFolderId;
   }

   private DLFolder getSubFolder(ServiceContext serviceContext, long parentFolderId, String folderName) throws PortalException {
      DLFolder folder = DLFolderLocalServiceUtil.getFolder(serviceContext.getScopeGroupId(), parentFolderId, folderName);
      _log.debug("Found folder: " + folderName + " (ID: " + folder.getFolderId() + ")");
      return folder;
   }

   private void processUserOrganizationRecords(ServiceContext serviceContext, User user, long branchId, long scopeKey, ObjectDefinition objectDefinition, DLFolder branchFolder) {
      String query = "userScreenName eq '" + user.getScreenName() + "'";
      List<UserOrganizationRecord> records = this.getUserDepartments(query, user, objectDefinition.getObjectDefinitionId());
      List<Long> processedFolderIds = this.updateExistingRecords(serviceContext, user, branchId, scopeKey, objectDefinition, branchFolder, records);
      this.createMissingRecords(serviceContext, objectDefinition.getObjectDefinitionId(), branchFolder, branchId, user, processedFolderIds);
   }

   private List<Long> updateExistingRecords(ServiceContext serviceContext, User user, long branchId, long scopeKey, ObjectDefinition objectDefinition, DLFolder branchFolder, List<UserOrganizationRecord> records) {
      List<Long> processedFolderIds = new ArrayList();
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      DefaultDTOConverterContext dtoConverterContext = this.createDTOConverterContext(objectDefinition, user);

      for(UserOrganizationRecord userOrganizationRecord : records) {
         try {
            this.updateSingleRecord(serviceContext, branchId, scopeKey, objectDefinition, branchFolder, userOrganizationRecord, objectEntryManager, dtoConverterContext);
            processedFolderIds.add(userOrganizationRecord.getFolderId());
         } catch (Exception e) {
            _log.error("Failed updating record for user " + userOrganizationRecord.getScreenName(), e);
         }
      }

      return processedFolderIds;
   }

   private void updateSingleRecord(ServiceContext serviceContext, long branchId, long scopeKey, ObjectDefinition objectDefinition, DLFolder branchFolder, UserOrganizationRecord userOrganizationRecord, ObjectEntryManager objectEntryManager, DefaultDTOConverterContext dtoConverterContext) throws Exception {
      com.liferay.object.model.ObjectEntry objectEntryModel = ObjectEntryLocalServiceUtil.getObjectEntry(userOrganizationRecord.getObjectEntryId());
      String externalReferenceCode = objectEntryModel.getExternalReferenceCode();
      ObjectEntry objectEntry = objectEntryManager.getObjectEntry(objectDefinition.getCompanyId(), dtoConverterContext, externalReferenceCode, objectDefinition, String.valueOf(serviceContext.getCompanyId()));
      DLFolder previousFolder = DLFolderLocalServiceUtil.getFolder(userOrganizationRecord.getFolderId());
      DLFolder latestFolder = this.findCorrespondingFolder(serviceContext, branchFolder, previousFolder);
      this.updateObjectEntryProperties(objectEntry, latestFolder.getFolderId(), userOrganizationRecord.getDepartmentId(), branchId, userOrganizationRecord.getScreenName());
      objectEntryManager.updateObjectEntry(objectDefinition.getCompanyId(), dtoConverterContext, objectEntry.getExternalReferenceCode(), objectDefinition, objectEntry, String.valueOf(scopeKey));
      _log.info("Updated ObjectEntry for user '" + userOrganizationRecord.getScreenName() + "' â†’ OrgID: " + branchId + ", Folder: " + latestFolder.getName());
   }

   private DLFolder findCorrespondingFolder(ServiceContext serviceContext, DLFolder branchFolder, DLFolder previousFolder) {
      try {
         return DLFolderLocalServiceUtil.getFolder(serviceContext.getScopeGroupId(), branchFolder.getFolderId(), previousFolder.getName());
      } catch (Exception var5) {
         Log var10000 = _log;
         String var10001 = previousFolder.getName();
         var10000.warn("Folder '" + var10001 + "' not found under branch '" + branchFolder.getName() + "'. Using original folder.");
         return previousFolder;
      }
   }

   private void updateObjectEntryProperties(ObjectEntry objectEntry, long folderId, String departmentId, long organizationId, String screenName) {
      Map<String, Object> props = objectEntry.getProperties();
      props.put("folderId", folderId);
      props.put("departmentId", departmentId);
      props.put("organizationId", organizationId);
      props.put("userScreenName", screenName);
      objectEntry.setProperties(props);
   }

   private void createMissingRecords(ServiceContext serviceContext, long objectDefinitionId, DLFolder branchFolder, long branchId, User user, List<Long> processedFolderIds) {
      try {
         List<DLFolder> folders = DLFolderLocalServiceUtil.getFolders(serviceContext.getScopeGroupId(), branchFolder.getFolderId());
         Map<String, String> templateNames = this.createTemplateMapping();

         for(DLFolder folder : this.filterMissingFolders(folders, processedFolderIds)) {
            Log var10000 = _log;
            Object var10001 = templateNames.get(folder.getName());
            var10000.info("templateNames.get(folder.getName())" + (String)var10001);
            if (!((String)templateNames.get(folder.getName())).isBlank()) {
               addUserOrganizationMaster(serviceContext, objectDefinitionId, folder.getFolderId(), (String)templateNames.get(folder.getName()), branchId, user.getScreenName(), user.getUserId());
            }
         }
      } catch (Exception e) {
         _log.error("Error creating missing records for user: " + user.getScreenName(), e);
      }

   }

   private Map<String, String> createTemplateMapping() {
      Map<String, String> templateNames = new HashMap();
      templateNames.put("UPI_Insurance", "UPI_Insurance");
      templateNames.put("Finance_Ploe", "Finance_Ploe");
      templateNames.put("Digital_Banking_Department", "Digital_Banking_Department");
      templateNames.put("DBD_Mandate_Cancellation", "DBD_Mandate_Cancellation");
      templateNames.put("Account Opening", "Account_Opening");
      templateNames.put("CPD_Insurance_Claim", "CPD_Insurance_Claim");
      templateNames.put("FBD", "FBD");
      templateNames.put("FE_BO", "FBD_FE_BO");
      templateNames.put("FE_BO_NON_FINANCIAL", "FE_BO_NON_Financial");
      return templateNames;
   }

   private List<DLFolder> filterMissingFolders(List<DLFolder> folders, List<Long> processedFolderIds) {
      return folders.stream().filter((folder) -> processedFolderIds.stream().noneMatch((id) -> id == folder.getFolderId())).toList();
   }

   private DefaultDTOConverterContext createDTOConverterContext(ObjectDefinition objectDefinition, User user) {
      return new DefaultDTOConverterContext(this.dtoConverterRegistry, objectDefinition.getObjectDefinitionId(), LocaleUtil.getDefault(), (UriInfo)null, user);
   }

   public static boolean addUserOrganizationMaster(ServiceContext serviceContext, long objectDefinitionId, long folderId, String departmentId, long organizationId, String userScreenName, long userId) {
      try {
         if (organizationId != 0L && folderId != 0L) {
            Map<String, Serializable> values = new HashMap();
            values.put("folderId", folderId);
            values.put("departmentId", departmentId);
            values.put("organizationId", organizationId);
            values.put("userScreenName", userScreenName);
            ObjectEntryLocalServiceUtil.addObjectEntry(userId, 0L, objectDefinitionId, values, serviceContext);
            return true;
         } else {
            return false;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   private static class OrganizationHierarchy {
      final Organization branch;
      final Organization zone;
      final Organization nbg;

      OrganizationHierarchy(Organization branch, Organization zone, Organization nbg) {
         this.branch = branch;
         this.zone = zone;
         this.nbg = nbg;
      }
   }
}
