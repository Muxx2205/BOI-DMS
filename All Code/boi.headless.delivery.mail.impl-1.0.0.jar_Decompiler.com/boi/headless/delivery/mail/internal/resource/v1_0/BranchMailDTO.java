package boi.headless.delivery.mail.internal.resource.v1_0;

public class BranchMailDTO {
   private String branchCode;
   private String branchName;
   private String sOLID;
   private String zone;
   private String email;

   public BranchMailDTO(String branchCode, String branchName, String sOLID, String zone, String email) {
      this.branchCode = this.normalizeBranchCode(branchCode);
      this.branchName = branchName;
      this.sOLID = sOLID;
      this.zone = zone;
      this.email = email;
   }

   public String getBranchCode() {
      return this.branchCode;
   }

   public String getBranchName() {
      return this.branchName;
   }

   public String getSOLID() {
      return this.sOLID;
   }

   public String getZone() {
      return this.zone;
   }

   public String getEmail() {
      return this.email;
   }

   private String normalizeBranchCode(String branchCode) {
      if (branchCode == null) {
         return null;
      } else {
         branchCode = branchCode.trim();
         return branchCode.length() >= 4 ? branchCode : String.format("%4s", branchCode).replace(' ', '0');
      }
   }
}
