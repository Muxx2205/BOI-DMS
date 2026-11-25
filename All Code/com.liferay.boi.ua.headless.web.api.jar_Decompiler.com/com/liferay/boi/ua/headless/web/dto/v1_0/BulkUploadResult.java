package com.liferay.boi.ua.headless.web.dto.v1_0;

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
import java.util.function.Supplier;
import javax.xml.bind.annotation.XmlRootElement;

@GraphQLName("BulkUploadResult")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(
   name = "BulkUploadResult"
)
public class BulkUploadResult implements Serializable {
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField Integer code;
   @JsonIgnore
   private Supplier<Integer> _codeSupplier;
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField String message;
   @JsonIgnore
   private Supplier<String> _messageSupplier;
   @Schema(
      accessMode = AccessMode.READ_ONLY,
      defaultValue = "com.liferay.boi.ua.headless.web.dto.v1_0.BulkUploadResult",
      name = "x-class-name"
   )
   public String xClassName;
   private static final String[][] _JSON_ESCAPE_STRINGS = new String[][]{{"\\", "\"", "\b", "\f", "\n", "\r", "\t"}, {"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}};
   private Map<String, Serializable> _extendedProperties;

   public static BulkUploadResult toDTO(String json) {
      return (BulkUploadResult)ObjectMapperUtil.readValue(BulkUploadResult.class, json);
   }

   public static BulkUploadResult unsafeToDTO(String json) {
      return (BulkUploadResult)ObjectMapperUtil.unsafeReadValue(BulkUploadResult.class, json);
   }

   @Schema
   public Integer getCode() {
      if (this._codeSupplier != null) {
         this.code = (Integer)this._codeSupplier.get();
         this._codeSupplier = null;
      }

      return this.code;
   }

   public void setCode(Integer code) {
      this.code = code;
      this._codeSupplier = null;
   }

   @JsonIgnore
   public void setCode(UnsafeSupplier<Integer, Exception> codeUnsafeSupplier) {
      this._codeSupplier = () -> {
         try {
            return (Integer)codeUnsafeSupplier.get();
         } catch (RuntimeException runtimeException) {
            throw runtimeException;
         } catch (Exception exception) {
            throw new RuntimeException(exception);
         }
      };
   }

   @Schema
   public String getMessage() {
      if (this._messageSupplier != null) {
         this.message = (String)this._messageSupplier.get();
         this._messageSupplier = null;
      }

      return this.message;
   }

   public void setMessage(String message) {
      this.message = message;
      this._messageSupplier = null;
   }

   @JsonIgnore
   public void setMessage(UnsafeSupplier<String, Exception> messageUnsafeSupplier) {
      this._messageSupplier = () -> {
         try {
            return (String)messageUnsafeSupplier.get();
         } catch (RuntimeException runtimeException) {
            throw runtimeException;
         } catch (Exception exception) {
            throw new RuntimeException(exception);
         }
      };
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof BulkUploadResult)) {
         return false;
      } else {
         BulkUploadResult bulkUploadResult = (BulkUploadResult)object;
         return Objects.equals(this.toString(), bulkUploadResult.toString());
      }
   }

   public int hashCode() {
      String string = this.toString();
      return string.hashCode();
   }

   public String toString() {
      StringBundler sb = new StringBundler();
      sb.append("{");
      Integer code = this.getCode();
      if (code != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"code\": ");
         sb.append(code);
      }

      String message = this.getMessage();
      if (message != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"message\": ");
         sb.append("\"");
         sb.append(_escape(message));
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
