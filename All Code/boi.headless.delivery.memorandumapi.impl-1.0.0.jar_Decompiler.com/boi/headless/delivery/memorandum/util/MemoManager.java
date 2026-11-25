package boi.headless.delivery.memorandum.util;

import boi.headless.delivery.memorandum.configuration.ConfigurationUtil;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import java.util.List;

public class MemoManager {
   private final User user;
   private final long userId;
   private final User admin;
   private final String branchName;
   private final String zoneName;
   private final String nbgName;
   private final String hoName;
   private final boolean isMaker;
   private final boolean isRecommender;
   private final boolean isApprover;
   private final boolean onlyShowApproved;
   private final boolean isHavingPFNumber;
   private final DTOConverterRegistry _dtoConverterRegistry;
   private final ObjectEntryManagerRegistry _objectEntryManagerRegistry;
   private final long objectDefinitionId = ConfigurationUtil.getMemoObjectDefinitionId();
   private static Log log = LogFactoryUtil.getLog(MemoManager.class);

   private MemoManager(Builder builder) {
      this.user = builder.user;
      this.userId = builder.userId;
      this.admin = builder.admin;
      this.branchName = builder.branchName;
      this.zoneName = builder.zoneName;
      this.nbgName = builder.nbgName;
      this.hoName = builder.hoName;
      this.isMaker = builder.isMaker;
      this.isRecommender = builder.isRecommender;
      this.isApprover = builder.isApprover;
      this.onlyShowApproved = builder.onlyShowApproved;
      this.isHavingPFNumber = builder.isHavingPFNumber;
      this._dtoConverterRegistry = builder._dtoConverterRegistry;
      this._objectEntryManagerRegistry = builder._objectEntryManagerRegistry;
   }

   public String getBranchName() {
      return this.branchName;
   }

   public String getZoneName() {
      return this.zoneName;
   }

   public String getNbgName() {
      return this.nbgName;
   }

   public String getHoName() {
      return this.hoName;
   }

   public boolean isMaker() {
      return this.isMaker;
   }

   public boolean isRecommender() {
      return this.isRecommender;
   }

   public boolean isApprover() {
      return this.isApprover;
   }

   public boolean onlyShowApproved() {
      return this.onlyShowApproved;
   }

   public User getUser() {
      return this.user;
   }

   public long getUserId() {
      return this.userId;
   }

   public User getAdminUser() {
      return this.admin;
   }

   public boolean isOnlyShowApproved() {
      return this.onlyShowApproved;
   }

   public boolean isHavingPFNumber() {
      return this.isHavingPFNumber;
   }

   public DTOConverterRegistry get_dtoConverterRegistry() {
      return this._dtoConverterRegistry;
   }

   public ObjectEntryManagerRegistry get_objectEntryManagerRegistry() {
      return this._objectEntryManagerRegistry;
   }

   public long getObjectDefinitionId() {
      return this.objectDefinitionId;
   }

   public static class Builder {
      private User user;
      private long userId;
      private User admin;
      private String branchName;
      private String zoneName;
      private String nbgName;
      private String hoName;
      private boolean isMaker;
      private boolean isRecommender;
      private boolean isApprover;
      private boolean onlyShowApproved;
      private boolean isHavingPFNumber;
      private final DTOConverterRegistry _dtoConverterRegistry;
      private final ObjectEntryManagerRegistry _objectEntryManagerRegistry;

      public Builder(User user, DTOConverterRegistry dtoConverterRegistry, ObjectEntryManagerRegistry objectEntryManagerRegistry) {
         this.user = user;
         this._dtoConverterRegistry = dtoConverterRegistry;
         this._objectEntryManagerRegistry = objectEntryManagerRegistry;
         MemoManager.log.info("Builder constructor called with user: " + user + ", dtoConverterRegistry: " + dtoConverterRegistry + ", objectEntryManagerRegistry: " + objectEntryManagerRegistry);
         if (user != null) {
            this.userId = user.getUserId();
            this.admin = UserLocalServiceUtil.fetchUser(ConfigurationUtil.getAdminUserId());
            this.loadOrganizationHierarchy(user);
            MemoManager.log.info("Organization hierarchy loaded for user: " + user.getUserId());
            this.loadUserRoles(user);
            MemoManager.log.info("User roles loaded for user: " + user.getUserId());
         }

      }

      private void loadOrganizationHierarchy(User user) {
         try {
            this.branchName = this.getOrganization(user.getOrganizations(), "BRANCH");
            this.zoneName = this.getOrganization(user.getOrganizations(), "ZONE");
            this.nbgName = this.getOrganization(user.getOrganizations(), "NBG");
            this.hoName = this.getOrganization(user.getOrganizations(), "Organization");
         } catch (PortalException var3) {
         }

      }

      private Organization getOrganization(String type, Organization organization) {
         if (Validator.isNotNull(organization) && type.equalsIgnoreCase(organization.getType())) {
            return organization;
         } else {
            if (organization != null && organization.getParentOrganizationId() > 0L) {
               try {
                  return this.getOrganization(type, organization.getParentOrganization());
               } catch (PortalException var4) {
               }
            }

            return null;
         }
      }

      private String getOrganization(List<Organization> organizations, String type) {
         if (organizations == null) {
            return null;
         } else {
            for(Organization org : organizations) {
               Organization match = this.getOrganization(type, org);
               if (match != null) {
                  return match.getName();
               }
            }

            return null;
         }
      }

      private void loadUserRoles(User user) {
         this.isMaker = this.hasRole(user, "MAKER");
         this.isRecommender = this.hasRole(user, "RECOMMENDER");
         this.isApprover = this.hasRole(user, "APPROVAL");
         this.onlyShowApproved = this.hasRole(user, "MEMOAPPROVED");
      }

      private boolean hasRole(User user, String roleName) {
         try {
            Role role = RoleLocalServiceUtil.getRole(user.getCompanyId(), roleName);
            return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
         } catch (Exception var4) {
            return false;
         }
      }

      public MemoManager build() {
         return new MemoManager(this);
      }
   }
}
