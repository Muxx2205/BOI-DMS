package boi.user.management.dao;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOrganizationMasterUtil {
   private static final Log _log = LogFactoryUtil.getLog(UserOrganizationMasterUtil.class);

   public static boolean addUserOrganizationMaster(ServiceContext serviceContext, long objectDefinitionId, long folderId, String departmentId, long organizationId, String userScreenName, long userId) {
      try {
         if (organizationId != 0L && folderId != 0L) {
            Map<String, Serializable> values = new HashMap();
            values.put("folderId", folderId);
            values.put("departmentId", departmentId);
            values.put("organizationId", organizationId);
            values.put("userScreenName", userScreenName);
            ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addObjectEntry(userId, 0L, objectDefinitionId, values, serviceContext);
            _log.info("Inserted ObjectEntry: " + objectEntry.getObjectEntryId());
            return true;
         } else {
            return false;
         }
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public static Long getOrganizationIdFromList(String name, List<Organization> organizations, String organizationType) {
      String searchName = name.trim().toLowerCase();

      for(Organization organization : organizations) {
         String orgName = organization.getName().trim().toLowerCase();
         if (organizationType.equalsIgnoreCase("BRANCH")) {
            if (orgName.startsWith(searchName)) {
               return organization.getOrganizationId();
            }
         } else if (organizationType.equalsIgnoreCase("ZONE") && organization.getType().equalsIgnoreCase("ZONE") && searchName.contains(orgName)) {
            return organization.getOrganizationId();
         }
      }

      return 0L;
   }

   public static List<UserOrganizationRecord> getRecordsByOrganizationId(long groupId, long objectDefinitionId, long organizationId) {
      List<UserOrganizationRecord> records = new ArrayList();
      int start = 0;
      int batchSize = 5000;

      while(true) {
         List<ObjectEntry> entries = ObjectEntryLocalServiceUtil.getObjectEntries(groupId, objectDefinitionId, start, start + batchSize);
         if (entries.isEmpty()) {
            return records;
         }

         for(ObjectEntry entry : entries) {
            Map<String, Serializable> values = entry.getValues();
            if (values != null && values.get("organizationId") != null) {
               long orgId = Long.parseLong(((Serializable)values.get("organizationId")).toString());
               if (orgId == organizationId) {
                  String screenName = values.get("userScreenName") != null ? ((Serializable)values.get("userScreenName")).toString() : "";
                  String departmentId = values.get("departmentId") != null ? ((Serializable)values.get("departmentId")).toString() : "";
                  long folderId = values.get("folderId") != null ? Long.parseLong(((Serializable)values.get("folderId")).toString()) : 0L;
                  records.add(new UserOrganizationRecord(folderId, departmentId, orgId, screenName));
               }
            }
         }

         start += batchSize;
      }
   }

   public static String buildFilter(String userScreenName, Long organizationId) {
      List<String> filters = new ArrayList();
      if (Validator.isNotNull(userScreenName) && !Validator.isBlank(userScreenName)) {
         filters.add("userScreenName eq '" + userScreenName + "'");
      }

      if (organizationId != null && organizationId > 0L) {
         filters.add("organizationId eq " + organizationId);
      }

      return String.join(" and ", filters);
   }

   public static class UserOrganizationRecord {
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
}
