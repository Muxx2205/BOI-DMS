package com.boi.headless.web.customeraccount.dto.v1_0;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class CustomRequestAccountOpeningResponse extends RequestAccountOpeningResponse {
   private static final long serialVersionUID = 3766615148789477018L;
   private final JSONObject data;

   public CustomRequestAccountOpeningResponse(JSONObject data, Response.Status status) {
      this.data = data != null ? data : JSONFactoryUtil.createJSONObject();
      if (status != null) {
         super.responseId = status.getStatusCode();
      } else {
         super.responseId = Status.INTERNAL_SERVER_ERROR.getStatusCode();
      }

   }

   public JSONObject getData() {
      return this.data;
   }

   public String toString() {
      return "CustomRequestAccountOpeningResponse {responseId=" + this.responseId + ", data=" + this.data + "}";
   }
}
