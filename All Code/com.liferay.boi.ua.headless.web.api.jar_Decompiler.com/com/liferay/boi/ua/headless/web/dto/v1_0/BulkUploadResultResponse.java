package com.liferay.boi.ua.headless.web.dto.v1_0;

import com.liferay.portal.kernel.json.JSONObject;

public class BulkUploadResultResponse extends BulkUploadResult {
   private JSONObject fileDetails;

   public JSONObject getFileDetails() {
      return this.fileDetails;
   }

   public void setFileDetails(JSONObject fileDetails) {
      this.fileDetails = fileDetails;
   }

   public String toString() {
      return "BulkUploadResultResponse [fileDetails=" + this.fileDetails.toJSONString() + "]";
   }
}
