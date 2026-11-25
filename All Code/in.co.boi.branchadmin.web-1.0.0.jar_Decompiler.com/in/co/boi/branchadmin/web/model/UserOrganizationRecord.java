package in.co.boi.branchadmin.web.model;

public class UserOrganizationRecord {
   private long objectEntryId;
   private long folderId;
   private String departmentId;
   private long organizationId;
   private String screenName;

   public UserOrganizationRecord(long objectEntryId, long folderId, String departmentId, long organizationId, String screenName) {
      this.objectEntryId = objectEntryId;
      this.folderId = folderId;
      this.departmentId = departmentId;
      this.organizationId = organizationId;
      this.screenName = screenName;
   }

   public long getObjectEntryId() {
      return this.objectEntryId;
   }

   public void setObjectEntryId(long objectEntryId) {
      this.objectEntryId = objectEntryId;
   }

   public long getFolderId() {
      return this.folderId;
   }

   public void setFolderId(long folderId) {
      this.folderId = folderId;
   }

   public String getDepartmentId() {
      return this.departmentId;
   }

   public void setDepartmentId(String departmentId) {
      this.departmentId = departmentId;
   }

   public long getOrganizationId() {
      return this.organizationId;
   }

   public void setOrgId(long organizationId) {
      this.organizationId = organizationId;
   }

   public String getScreenName() {
      return this.screenName;
   }

   public void setScreenName(String screenName) {
      this.screenName = screenName;
   }
}
