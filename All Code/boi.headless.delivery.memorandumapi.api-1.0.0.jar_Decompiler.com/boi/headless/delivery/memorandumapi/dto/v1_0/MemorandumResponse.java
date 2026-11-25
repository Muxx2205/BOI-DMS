package boi.headless.delivery.memorandumapi.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import java.util.Map;
import javax.ws.rs.core.Response;

public class MemorandumResponse extends BOIHeadlessResponse {
   private static final long serialVersionUID = -5577572936277163653L;
   @JsonProperty(
      access = Access.WRITE_ONLY
   )
   private JSONObject jsonData = JSONFactoryUtil.createJSONObject();

   public MemorandumResponse() {
   }

   public MemorandumResponse(JSONObject data, int statusCode, String statusMessage) {
      super.setResponseId(statusCode);
      super.setResponseStatus(statusMessage);
      this.jsonData = data != null ? data : JSONFactoryUtil.createJSONObject();
      super.setData(this.jsonData.toMap());
   }

   public void setData(Map<String, ?> map) {
      if (map != null) {
         this.jsonData = JSONFactoryUtil.createJSONObject(map);
      } else {
         this.jsonData = JSONFactoryUtil.createJSONObject();
      }

      super.setData(this.jsonData.toMap());
   }

   @JsonIgnore
   public JSONObject getJsonData() {
      return this.jsonData;
   }

   public void setJsonData(JSONObject data) {
      this.jsonData = data != null ? data : JSONFactoryUtil.createJSONObject();
      super.setData(this.jsonData.toMap());
   }

   public String toString() {
      Integer var10000 = this.getResponseId();
      return "MemorandumResponse {responseId=" + var10000 + ", responseStatus='" + this.getResponseStatus() + "', data=" + this.jsonData + "}";
   }

   public static Builder builder() {
      return new Builder();
   }

   public static class Builder {
      private int responseId = 200;
      private String responseStatus = "OK";
      private JSONObject data = JSONFactoryUtil.createJSONObject();

      public Builder status(Response.Status status) {
         this.responseId = status.getStatusCode();
         this.responseStatus = status.getReasonPhrase();
         return this;
      }

      public Builder responseId(int responseId) {
         this.responseId = responseId;
         return this;
      }

      public Builder responseStatus(String status) {
         this.responseStatus = status;
         return this;
      }

      public Builder data(JSONObject json) {
         this.data = json != null ? json : JSONFactoryUtil.createJSONObject();
         return this;
      }

      public Builder add(String key, Object value) {
         this.data.put(key, value);
         return this;
      }

      public MemorandumResponse build() {
         return new MemorandumResponse(this.data, this.responseId, this.responseStatus);
      }
   }
}
