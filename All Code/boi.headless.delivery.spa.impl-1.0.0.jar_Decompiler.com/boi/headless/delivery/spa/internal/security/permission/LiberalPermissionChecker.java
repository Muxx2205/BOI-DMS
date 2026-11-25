package boi.headless.delivery.spa.internal.security.permission;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.UserBag;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiberalPermissionChecker implements PermissionChecker {
   private static final Log _log = LogFactoryUtil.getLog(LiberalPermissionChecker.class);
   private Role _ownerRole;
   private User _user;

   public LiberalPermissionChecker(User user) {
      this.init(user);
   }

   public PermissionChecker clone() {
      return this;
   }

   public long getCompanyId() {
      return this._user.getCompanyId();
   }

   public long[] getGuestUserRoleIds() {
      return PermissionChecker.DEFAULT_ROLE_IDS;
   }

   /** @deprecated */
   @Deprecated
   public List<Long> getOwnerResourceBlockIds(long companyId, long groupId, String name, String actionId) {
      return new ArrayList();
   }

   public long getOwnerRoleId() {
      return this._ownerRole.getRoleId();
   }

   public Map<Object, Object> getPermissionChecksMap() {
      return new HashMap();
   }

   /** @deprecated */
   @Deprecated
   public List<Long> getResourceBlockIds(long companyId, long groupId, long userId, String name, String actionId) {
      return new ArrayList();
   }

   public long[] getRoleIds(long userId, long groupId) {
      return PermissionChecker.DEFAULT_ROLE_IDS;
   }

   public User getUser() {
      return this._user;
   }

   public UserBag getUserBag() {
      return null;
   }

   public long getUserId() {
      return this._user.getUserId();
   }

   public boolean hasOwnerPermission(long companyId, String name, long primKey, long ownerId, String actionId) {
      return true;
   }

   public boolean hasOwnerPermission(long companyId, String name, String primKey, long ownerId, String actionId) {
      return true;
   }

   public boolean hasPermission(Group group, String name, long primKey, String actionId) {
      return true;
   }

   public boolean hasPermission(Group group, String name, String primKey, String actionId) {
      return true;
   }

   public boolean hasPermission(long groupId, String name, long primKey, String actionId) {
      return true;
   }

   public boolean hasPermission(long groupId, String name, String primKey, String actionId) {
      return true;
   }

   public void init(User user) {
      this._user = user;

      try {
         this._ownerRole = RoleLocalServiceUtil.getRole(user.getCompanyId(), "Owner");
      } catch (Exception exception) {
         _log.error(exception);
      }

   }

   public boolean isCheckGuest() {
      return GetterUtil.getBoolean(PropsUtil.get("permissions.check.guest.enabled"));
   }

   public boolean isCompanyAdmin() {
      return true;
   }

   public boolean isCompanyAdmin(long companyId) {
      return true;
   }

   public boolean isContentReviewer(long companyId, long groupId) {
      return true;
   }

   public boolean isGroupAdmin(long groupId) {
      return true;
   }

   public boolean isGroupMember(long groupId) {
      return true;
   }

   public boolean isGroupOwner(long groupId) {
      return true;
   }

   public boolean isOmniadmin() {
      return true;
   }

   public boolean isOrganizationAdmin(long organizationId) {
      return true;
   }

   public boolean isOrganizationOwner(long organizationId) {
      return true;
   }

   public boolean isSignedIn() {
      return true;
   }
}
