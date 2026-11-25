package boi.document.management.system.portlet;

import boi.document.management.system.configuration.DMSConfiguration;
import boi.document.management.system.dto.DepartmentFileLimitsDTO;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"boi.document.management.system.configuration.DMSConfiguration"},
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=BoiDocumentManagementSystem", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class BoiDocumentManagementSystemPortlet extends MVCPortlet {
   private static String userScreenName = "";
   private static List<String> departmentFileFilter = new ArrayList();
   private static final Map<Long, Long> folderIdKey = new HashMap();
   private static Long objectDefinitionId = 0L;
   private static Long folderMappingObjectDefinitionId = 0L;
   private static final Log _log = LogFactoryUtil.getLog(BoiDocumentManagementSystemPortlet.class);
   private static User user = null;
   private static Map<Long, String> folderIdNameMap = new HashMap();
   private static boolean isHavingAddUpdateAction = false;
   private static boolean isHavingDeleteAction = false;
   private static boolean isHavingPreviewAction = false;
   private static boolean isHavingFullAccess = false;
   private static boolean haveArchiveAccess = false;
   private boolean isWorkFlowUser = false;
   private boolean isSPAUser = false;
   private boolean isAPIUser = false;
   private static boolean isHavingDocHidePermission = false;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   private static DMSConfiguration _dmsConfiguration;

   public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         isHavingDocHidePermission = themeDisplay.getUser().getRoles().stream().anyMatch((x) -> x.getName().equalsIgnoreCase("DMS DOC HIDE"));
         long userId = themeDisplay.getUserId();
         user = UserLocalServiceUtil.getUser(userId);
         this.checkUserRoles(user);
         userScreenName = user.getScreenName();
         objectDefinitionId = _dmsConfiguration.organizationMasterObjectId();
         folderMappingObjectDefinitionId = _dmsConfiguration.userFolderObjectId();
         folderIdNameMap.clear();
         folderIdKey.clear();
         _log.info("Workflow User: " + this.isWorkFlowUser);
         _log.info("SPA User: " + this.isSPAUser);
         _log.info("API User: " + this.isAPIUser);
         _log.info("isHavingAddUpdateAction: " + isHavingAddUpdateAction);
         _log.info("isHavingDeleteAction: " + isHavingDeleteAction);
         _log.info("isHavingPreviewAction: " + isHavingPreviewAction);
         _log.info("isHavingFullAccess: " + isHavingFullAccess);

         try {
            if (folderMappingObjectDefinitionId > 0L) {
               List<UserFolderMapping> userFolderMapping = this.getAllUserFolderMappings(folderMappingObjectDefinitionId, userScreenName);
               if (userFolderMapping != null && !userFolderMapping.isEmpty()) {
                  Set<Long> folderIds = (Set)userFolderMapping.stream().filter((x) -> x.isActive() && x.getFolderId() > 0L).map(UserFolderMapping::getFolderId).collect(Collectors.toSet());
                  if (folderIds != null && !folderIds.isEmpty()) {
                     folderIdNameMap = this.getFolderIdNameMap(folderIds);
                  }
               }
            }
         } catch (Exception var12) {
         }

         try {
            if (objectDefinitionId > 0L) {
               Set<Long> folderIdsByUserScreenName = new HashSet();
               if (this.isWorkFlowUser) {
                  String query = this.buildFilter(userScreenName, (Long)null);
                  if (query != null && !query.trim().isEmpty()) {
                     List<UserOrganizationRecord> userDeparments = this.getUserDeparments(query, user, objectDefinitionId);
                     if (userDeparments != null && !userDeparments.isEmpty()) {
                        folderIdsByUserScreenName = (Set)userDeparments.stream().map((x) -> x.getFolderId()).filter((id) -> id != null && id > 0L).collect(Collectors.toSet());
                     }
                  }

                  folderIdNameMap = this.getFolderIdNameMap(folderIdsByUserScreenName);
               }
            }
         } catch (Exception var11) {
         }

         try {
            if (this.isSPAUser) {
               List<String> matchedRoles = this.getMatchedSPARoles(user);
               if (matchedRoles.size() > 0) {
                  this.getAndSetMappedSPAFolderId(matchedRoles, folderIdNameMap);
               }
            }
         } catch (Exception var10) {
         }

         try {
            DepartmentFileLimitsDTO.initializeForUser(renderRequest, departmentFileFilter, true, _dmsConfiguration);
         } catch (Exception e) {
            _log.debug(e.getMessage());
         }

         departmentFileFilter.clear();
         renderRequest.setAttribute("folderIdNameMap", folderIdNameMap);
         renderRequest.setAttribute("userScreenName", user.getScreenName());
         renderRequest.setAttribute("isHavingAddUpdateAction", isHavingAddUpdateAction);
         renderRequest.setAttribute("isHavingDeleteAction", isHavingDeleteAction);
         renderRequest.setAttribute("isHavingPreviewAction", isHavingPreviewAction);
         renderRequest.setAttribute("isHavingFullAccess", isHavingFullAccess);
         renderRequest.setAttribute("haveArchiveAccess", haveArchiveAccess);
      } catch (PortalException e) {
         e.printStackTrace();
      }

      super.doView(renderRequest, renderResponse);
   }

   private Map<Long, String> getFolderIdNameMap(Set<Long> folderIds) {
      if (folderIds != null && !folderIds.isEmpty()) {
         for(Long folderId : folderIds) {
            try {
               DLFolder folder = DLFolderLocalServiceUtil.getDLFolder(folderId);
               folderIdNameMap.put(folderId, folder.getName());
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return folderIdNameMap;
      } else {
         return folderIdNameMap;
      }
   }

   public static List<Role> getRoles() {
      return user.getRoles();
   }

   private List<String> getMatchedSPARoles(User user) {
      List<String> matchedRoles = new ArrayList();
      String spaRoles = _dmsConfiguration.spaRoles();
      if (Validator.isNull(spaRoles)) {
         return matchedRoles;
      } else {
         List<String> specialRoles = Arrays.asList(spaRoles.split("\\s*,\\s*"));

         for(Role role : user.getRoles()) {
            String roleName = role.getName().trim();

            for(String special : specialRoles) {
               if (special.trim().equalsIgnoreCase(roleName)) {
                  matchedRoles.add(roleName);
                  break;
               }
            }
         }

         return matchedRoles;
      }
   }

   private void checkUserRoles(User user) {
      if (user != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
         Set<String> roleNames = (Set)user.getRoles().stream().map((r) -> r.getName().toLowerCase()).collect(Collectors.toSet());
         isHavingAddUpdateAction = roleNames.contains("dms create rights");
         isHavingDeleteAction = roleNames.contains("dms delete rights");
         isHavingPreviewAction = roleNames.contains("dms preview rights");
         isHavingFullAccess = roleNames.contains("dms full rights");
         haveArchiveAccess = roleNames.contains("dms archive rights");
         Set<String> workflowRoles = this.getRoleSet(_dmsConfiguration.workflowRoleName());
         Set<String> spaRoles = this.getRoleSet(_dmsConfiguration.spaRoles());

         for(String role : roleNames) {
            if (!this.isWorkFlowUser && workflowRoles.contains(role)) {
               this.isWorkFlowUser = true;
            }

            if (!this.isSPAUser && spaRoles.contains(role)) {
               this.isSPAUser = true;
            }

            if (this.isWorkFlowUser && this.isSPAUser) {
               break;
            }
         }

      }
   }

   private Set<String> getRoleSet(String roles) {
      return roles != null && !roles.isBlank() ? (Set)Arrays.stream(roles.split("\\s*,\\s*")).filter(Objects::nonNull).map((r) -> r.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toSet()) : Collections.emptySet();
   }

   public boolean isWorkFlowUser() {
      return this.isWorkFlowUser;
   }

   public boolean isSPAUser() {
      return this.isSPAUser;
   }

   public boolean isAPIUser() {
      return this.isAPIUser;
   }

   public static String workflowFolderNameMapping() {
      return _dmsConfiguration.workflowFolderNameMapping();
   }

   public static String boardSecretariatFileRenameFolderIds() {
      return _dmsConfiguration.boardSecretariatFileRenameFolderIds();
   }

   public static String nbgFoldersName() {
      return _dmsConfiguration.nbgLevelFolderNames();
   }

   public static String api3LevelHideFolder() {
      return _dmsConfiguration.api3LevelHideFolder();
   }

   public static String blockedExtensions() {
      return _dmsConfiguration.blockedExtensions();
   }

   public static String allowedExtensions() {
      return _dmsConfiguration.allowedExtensions();
   }

   public static String[] reservedNames() {
      return _dmsConfiguration.reservedNames();
   }

   public static long maxFiles() {
      return _dmsConfiguration.maxFiles();
   }

   public static String getBoardSectraitFolderId() {
      return _dmsConfiguration.boardSecretariatFolderId();
   }

   public static long maxFileSize() {
      return _dmsConfiguration.maxFileSize();
   }

   public static boolean isHavingDocHidePermission() {
      return isHavingDocHidePermission;
   }

   public List<UserFolderMapping> getAllUserFolderMappings(long objectDefinitionId, String userSN) {
      List<UserFolderMapping> records = new ArrayList();

      try {
         int start = 0;
         int delta = 500;

         while(true) {
            List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0L, objectDefinitionId, start, start + delta);
            if (objectEntries.isEmpty()) {
               break;
            }

            for(ObjectEntry entry : objectEntries) {
               Map<String, Serializable> map = entry.getValues();
               Serializable folderIdValue = (Serializable)map.get("folderId");
               Serializable folderNameValue = (Serializable)map.get("folderName");
               Serializable userScreenNameValue = (Serializable)map.get("userScreenName");
               Serializable isActiveValue = (Serializable)map.get("isActive");
               if (map != null && userScreenNameValue != null) {
                  long folderId = folderIdValue != null ? Long.parseLong(folderIdValue.toString()) : 0L;
                  String userScreenName = (String)userScreenNameValue;
                  boolean isActive = isActiveValue != null && Boolean.parseBoolean(isActiveValue.toString());
                  String folderName = folderNameValue != null ? (String)folderNameValue : "";
                  if (userScreenName != null && userScreenName.trim().equalsIgnoreCase(userSN.trim())) {
                     UserFolderMapping mapping = new UserFolderMapping(folderId, userScreenName, isActive, folderName);
                     departmentFileFilter.add(folderName);
                     records.add(mapping);
                  }
               }
            }

            start += delta;
         }
      } catch (Exception e) {
         _log.error("Error fetching UserFolder mappings: " + e.getMessage(), e);
      }

      return records;
   }

   public List<UserFolderMapping> getUserFolderMapping(String query, User user, long objectDefinitionId) {
      List<UserFolderMapping> records = new ArrayList();

      try {
         ObjectDefinition userFolderMappingObject = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, userFolderMappingObject.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(userFolderMappingObject.getStorageType());
         Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(userFolderMappingObject.getCompanyId(), userFolderMappingObject, userFolderMappingObject.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);
         page.getItems().forEach((x) -> {
            try {
               Map<String, Object> map = x.getProperties();
               if (map.containsKey("folderId") && map.containsKey("userScreenName") && map.containsKey("isActive") && map.containsKey("folderName")) {
                  records.add(new UserFolderMapping((Long)map.get("folderId"), (String)map.get("userScreenName"), (Boolean)map.get("isActive"), (String)map.get("folderName")));
                  departmentFileFilter.add((String)map.get("folderName"));
               }
            } catch (Exception e) {
               _log.error("error... " + e.getMessage());
            }

         });
         return records;
      } catch (Exception e) {
         _log.error(e);
         return records;
      }
   }

   public void getAndSetMappedSPAFolderId(List<String> matchedRoles, Map<Long, String> folderIdNameMap) {
      String spaFolderIdsWithTemplate = _dmsConfiguration.spaFolderIdsWithTemplate();
      if (spaFolderIdsWithTemplate != null && !spaFolderIdsWithTemplate.isEmpty()) {
         String[] entries = spaFolderIdsWithTemplate.split(",");

         for(String entry : entries) {
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length == 2) {
               String folderName = keyValue[0].trim().replace("\"", "");
               String folderIdStr = keyValue[1].trim().replace("\"", "");

               try {
                  long folderId = Long.parseLong(folderIdStr);
                  if (matchedRoles.contains(folderName)) {
                     folderIdNameMap.put(folderId, folderName);
                  }
               } catch (NumberFormatException var14) {
                  _log.error("Invalid folderId for entry: " + entry);
               }
            }
         }

      }
   }

   public List<UserOrganizationRecord> getUserDeparments(String query, User user, long objectDefinitionId) {
      List<UserOrganizationRecord> records = new ArrayList();

      try {
         ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(userDepartmentObject.getStorageType());
         Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject, userDepartmentObject.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);
         _log.info("List from user department " + page.getTotalCount());
         page.getItems().forEach((x) -> {
            try {
               Map<String, Object> map = x.getProperties();
               if (map.containsKey("userScreenName") && map.containsKey("departmentId") && map.containsKey("folderId") && map.containsKey("organizationId")) {
                  records.add(new UserOrganizationRecord((Long)map.get("folderId"), (String)map.get("departmentId"), (Long)map.get("organizationId"), (String)map.get("userScreenName")));
               }
            } catch (Exception e) {
               _log.error("error... " + e.getMessage());
            }

         });
         return records;
      } catch (Exception e) {
         _log.error(e);
         return records;
      }
   }

   public String buildFilter(String userScreenName, Long organizationId) {
      List<String> filters = new ArrayList();
      if (Validator.isNotNull(userScreenName) && !Validator.isBlank(userScreenName)) {
         filters.add("userScreenName eq '" + userScreenName + "'");
      }

      if (organizationId != null && organizationId > 0L) {
         filters.add("organizationId eq " + organizationId);
      }

      return String.join(" and ", filters);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      _dmsConfiguration = (DMSConfiguration)ConfigurableUtil.createConfigurable(DMSConfiguration.class, properties);
   }

   public class UserOrganizationRecord {
      private long folderId;
      private String departmentId;
      private long organizationId;
      private String userScreenName;

      public UserOrganizationRecord(long folderId, String departmentId, long organizationId, String userScreenName) {
         this.folderId = folderId;
         this.departmentId = departmentId;
         this.organizationId = organizationId;
         this.userScreenName = userScreenName;
      }

      public long getFolderId() {
         return this.folderId;
      }

      public String getDepartmentId() {
         return this.departmentId;
      }

      public long getOrganizationId() {
         return this.organizationId;
      }

      public String getUserScreenName() {
         return this.userScreenName;
      }

      public String toString() {
         return "UserOrganizationRecord{folderId=" + this.folderId + ", departmentId=" + this.departmentId + ", organizationId='" + this.organizationId + "', userScreenName='" + this.userScreenName + "'}";
      }
   }

   public class UserFolderMapping {
      private long folderId;
      private String userScreenName;
      private boolean isActive;
      private String folderName;

      public UserFolderMapping() {
      }

      public UserFolderMapping(long folderId, String userScreenName, boolean isActive, String folderName) {
         this.folderId = folderId;
         this.userScreenName = userScreenName;
         this.isActive = isActive;
         this.folderName = folderName;
      }

      public long getFolderId() {
         return this.folderId;
      }

      public void setFolderId(long folderId) {
         this.folderId = folderId;
      }

      public String getUserScreenName() {
         return this.userScreenName;
      }

      public void setUserScreenName(String userScreenName) {
         this.userScreenName = userScreenName;
      }

      public boolean isActive() {
         return this.isActive;
      }

      public void setActive(boolean isActive) {
         this.isActive = isActive;
      }

      public String folderName() {
         return this.folderName;
      }

      public void setfolderName(String folderName) {
         this.folderName = folderName;
      }

      public String toString() {
         return "UserFolderMapping {folderId=" + this.folderId + ", userScreenName='" + this.userScreenName + "', isActive=" + this.isActive + "}";
      }
   }
}
