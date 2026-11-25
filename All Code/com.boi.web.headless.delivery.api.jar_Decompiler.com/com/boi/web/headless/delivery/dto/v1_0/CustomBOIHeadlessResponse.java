package com.boi.web.headless.delivery.dto.v1_0;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import javax.ws.rs.core.Response;

public class CustomBOIHeadlessResponse extends BOIHeadlessResponse {
   private static final long serialVersionUID = -5577572936277163653L;
   private JSONObject data = JSONFactoryUtil.createJSONObject();

   public CustomBOIHeadlessResponse(JSONObject data, Response.Status status) {
      try {
         super.responseId = status.getStatusCode();
         this.data = data;
      } catch (Exception var4) {
      }

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
