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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@GraphQLName("FolderCreateRequest")
@JsonFilter("Liferay.Vulcan")
@Schema(
   requiredProperties = {"parentFolderId", "name"}
)
@XmlRootElement(
   name = "FolderCreateRequest"
)
public class FolderCreateRequest implements Serializable {
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField String description;
   @JsonIgnore
   private Supplier<String> _descriptionSupplier;
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField @NotEmpty String name;
   @JsonIgnore
   private Supplier<String> _nameSupplier;
   @JsonProperty(
      access = Access.READ_WRITE
   )
   protected @GraphQLField @NotNull Integer parentFolderId;
   @JsonIgnore
   private Supplier<Integer> _parentFolderIdSupplier;
   @Schema(
      accessMode = AccessMode.READ_ONLY,
      defaultValue = "com.liferay.boi.ua.headless.web.dto.v1_0.FolderCreateRequest",
      name = "x-class-name"
   )
   public String xClassName;
   private static final String[][] _JSON_ESCAPE_STRINGS = new String[][]{{"\\", "\"", "\b", "\f", "\n", "\r", "\t"}, {"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}};
   private Map<String, Serializable> _extendedProperties;

   public static FolderCreateRequest toDTO(String json) {
      return (FolderCreateRequest)ObjectMapperUtil.readValue(FolderCreateRequest.class, json);
   }

   public static FolderCreateRequest unsafeToDTO(String json) {
      return (FolderCreateRequest)ObjectMapperUtil.unsafeReadValue(FolderCreateRequest.class, json);
   }

   @Schema
   public String getDescription() {
      if (this._descriptionSupplier != null) {
         this.description = (String)this._descriptionSupplier.get();
         this._descriptionSupplier = null;
      }

      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
      this._descriptionSupplier = null;
   }

   @JsonIgnore
   public void setDescription(UnsafeSupplier<String, Exception> descriptionUnsafeSupplier) {
      this._descriptionSupplier = () -> {
         try {
            return (String)descriptionUnsafeSupplier.get();
         } catch (RuntimeException runtimeException) {
            throw runtimeException;
         } catch (Exception exception) {
            throw new RuntimeException(exception);
         }
      };
   }

   @Schema
   public String getName() {
      if (this._nameSupplier != null) {
         this.name = (String)this._nameSupplier.get();
         this._nameSupplier = null;
      }

      return this.name;
   }

   public void setName(String name) {
      this.name = name;
      this._nameSupplier = null;
   }

   @JsonIgnore
   public void setName(UnsafeSupplier<String, Exception> nameUnsafeSupplier) {
      this._nameSupplier = () -> {
         try {
            return (String)nameUnsafeSupplier.get();
         } catch (RuntimeException runtimeException) {
            throw runtimeException;
         } catch (Exception exception) {
            throw new RuntimeException(exception);
         }
      };
   }

   @Schema
   public Integer getParentFolderId() {
      if (this._parentFolderIdSupplier != null) {
         this.parentFolderId = (Integer)this._parentFolderIdSupplier.get();
         this._parentFolderIdSupplier = null;
      }

      return this.parentFolderId;
   }

   public void setParentFolderId(Integer parentFolderId) {
      this.parentFolderId = parentFolderId;
      this._parentFolderIdSupplier = null;
   }

   @JsonIgnore
   public void setParentFolderId(UnsafeSupplier<Integer, Exception> parentFolderIdUnsafeSupplier) {
      this._parentFolderIdSupplier = () -> {
         try {
            return (Integer)parentFolderIdUnsafeSupplier.get();
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
      } else if (!(object instanceof FolderCreateRequest)) {
         return false;
      } else {
         FolderCreateRequest folderCreateRequest = (FolderCreateRequest)object;
         return Objects.equals(this.toString(), folderCreateRequest.toString());
      }
   }

   public int hashCode() {
      String string = this.toString();
      return string.hashCode();
   }

   public String toString() {
      StringBundler sb = new StringBundler();
      sb.append("{");
      String description = this.getDescription();
      if (description != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"description\": ");
         sb.append("\"");
         sb.append(_escape(description));
         sb.append("\"");
      }

      String name = this.getName();
      if (name != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"name\": ");
         sb.append("\"");
         sb.append(_escape(name));
         sb.append("\"");
      }

      Integer parentFolderId = this.getParentFolderId();
      if (parentFolderId != null) {
         if (sb.length() > 1) {
            sb.append(", ");
         }

         sb.append("\"parentFolderId\": ");
         sb.append(parentFolderId);
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
