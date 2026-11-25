package boi.headless.delivery.spa.dto.v1_0;

import com.liferay.portal.kernel.json.JSONObject;

public class CustomBOIHeadlessResponse extends BOIHeadlessResponse {
   private static final long serialVersionUID = 1L;
   private JSONObject data;

   public CustomBOIHeadlessResponse(JSONObject data) {
      this.data = data;
   }

   public JSONObject getData() {
      return this.data;
   }

   public void setData(JSONObject data) {
      this.data = data;
   }

   public String toString() {
      return "CustomBOIHeadlessResponse [data=" + this.data + "]";
   }
}
