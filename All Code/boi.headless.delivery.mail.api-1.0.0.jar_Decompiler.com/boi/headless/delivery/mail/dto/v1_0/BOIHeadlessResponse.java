package boi.headless.delivery.mail.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

@GraphQLName("BOIHeadlessResponse")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(
   name = "BOIHeadlessResponse"
)
public class BOIHeadlessResponse implements Serializable {
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField Integer responseId;
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField String responseStatus;
   @Schema(
      accessMode = AccessMode.READ_ONLY,
      defaultValue = "boi.headless.delivery.mail.dto.v1_0.BOIHeadlessResponse",
      name = "x-class-name"
   )
   public String xClassName;
   private static final String[][] _JSON_ESCAPE_STRINGS = new String[][]{{"\\", "\"", "\b", "\f", "\n", "\r", "\t"}, {"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}};

   public static BOIHeadlessResponse toDTO(String json) {
      return (BOIHeadlessResponse)ObjectMapperUtil.readValue(BOIHeadlessResponse.class, json);
   }

   public static BOIHeadlessResponse unsafeToDTO(String json) {
      return (BOIHeadlessResponse)ObjectMapperUtil.unsafeReadValue(BOIHeadlessResponse.class, json);
   }

   @Schema
   public Integer getResponseId() {
      return this.responseId;
   }

   public void setResponseId(Integer responseId) {
      this.responseId = responseId;
   }

   @JsonIgnore
   public void setResponseId(UnsafeSupplier<Integer, Exception> responseIdUnsafeSupplier) {
      try {
         this.responseId = (Integer)responseIdUnsafeSupplier.get();
      } catch (RuntimeException re) {
         throw re;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Schema
   public String getResponseStatus() {
      return this.responseStatus;
   }

   public void setResponseStatus(String responseStatus) {
      this.responseStatus = responseStatus;
   }

   @JsonIgnore
   public void setResponseStatus(UnsafeSupplier<String, Exception> responseStatusUnsafeSupplier) {
      try {
         this.responseStatus = (String)responseStatusUnsafeSupplier.get();
      } catch (RuntimeException re) {
         throw re;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof BOIHeadlessResponse)) {
         return false;
      } else {
         BOIHeadlessResponse boiHeadlessResponse = (BOIHeadlessResponse)object;
         return Objects.equals(this.toString(), boiHeadlessResponse.toString());
      }
   }

   public int hashCode() {
      String string = this.toString();
      return string.hashCode();
   }

   public String toString() {
      StringBundler sb = new StringBundler();
      sb.append("{");
      if (this.responseId != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"responseId\": ");
         sb.append(this.responseId);
      }

      if (this.responseStatus != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"responseStatus\": ");
         sb.append("\"");
         sb.append(_escape(this.responseStatus));
         sb.append("\"");
      }

      sb.append("}");
      return sb.toString();
   }

   private static String _escape(Object object) {
      return StringUtil.replace(String.valueOf(object), _JSON_ESCAPE_STRINGS[0], _JSON_ESCAPE_STRINGS[1]);
   }

   private static boolean _isArray(Object value) {
      if (value == null) {
         return false;
      } else {
         Class<?> clazz = value.getClass();
         return clazz.isArray();
      }
   }

   private static String _toJSON(Map<String, ?> map) {
      StringBuilder sb = new StringBuilder("{");
      Set set = map.entrySet();
      Iterator<Map.Entry<String, ?>> iterator = set.iterator();

      while(iterator.hasNext()) {
         Map.Entry<String, ?> entry = (Map.Entry)iterator.next();
         sb.append("\"");
         sb.append(_escape(entry.getKey()));
         sb.append("\": ");
         Object value = entry.getValue();
         if (!_isArray(value)) {
            if (value instanceof Map) {
               sb.append(_toJSON((Map)value));
            } else if (value instanceof String) {
               sb.append("\"");
               sb.append(_escape(value));
               sb.append("\"");
            } else {
               sb.append(value);
            }
         } else {
            sb.append("[");
            Object[] valueArray = value;

            for(int i = 0; i < valueArray.length; ++i) {
               if (valueArray[i] instanceof String) {
                  sb.append("\"");
                  sb.append(valueArray[i]);
                  sb.append("\"");
               } else {
                  sb.append(valueArray[i]);
               }

               if (i + 1 < valueArray.length) {
                  sb.append(", ");
               }
            }

            sb.append("]");
         }

         if (iterator.hasNext()) {
            sb.append(", ");
         }
      }

      sb.append("}");
      return sb.toString();
   }
}
