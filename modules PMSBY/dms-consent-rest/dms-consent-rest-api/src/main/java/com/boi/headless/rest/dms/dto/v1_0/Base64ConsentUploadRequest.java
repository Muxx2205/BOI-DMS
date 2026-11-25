package com.boi.headless.rest.dms.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Generated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("Base64ConsentUploadRequest")
@JsonFilter("Liferay.Vulcan")
@Schema(
	requiredProperties = {
		"document", "documentFileName", "documentFileType", "metadata"
	}
)
@XmlRootElement(name = "Base64ConsentUploadRequest")
public class Base64ConsentUploadRequest implements Serializable {

	public static Base64ConsentUploadRequest toDTO(String json) {
		return ObjectMapperUtil.readValue(
			Base64ConsentUploadRequest.class, json);
	}

	public static Base64ConsentUploadRequest unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			Base64ConsentUploadRequest.class, json);
	}

	@Schema(description = "Base64 encoded document content")
	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	@JsonIgnore
	public void setDocument(
		UnsafeSupplier<String, Exception> documentUnsafeSupplier) {

		try {
			document = documentUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Base64 encoded document content")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String document;

	@Schema(description = "Original filename with extension")
	public String getDocumentFileName() {
		return documentFileName;
	}

	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}

	@JsonIgnore
	public void setDocumentFileName(
		UnsafeSupplier<String, Exception> documentFileNameUnsafeSupplier) {

		try {
			documentFileName = documentFileNameUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Original filename with extension")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String documentFileName;

	@Schema(description = "File type - pdf, jpg, or png")
	@Valid
	public DocumentFileType getDocumentFileType() {
		return documentFileType;
	}

	@JsonIgnore
	public String getDocumentFileTypeAsString() {
		if (documentFileType == null) {
			return null;
		}

		return documentFileType.toString();
	}

	public void setDocumentFileType(DocumentFileType documentFileType) {
		this.documentFileType = documentFileType;
	}

	@JsonIgnore
	public void setDocumentFileType(
		UnsafeSupplier<DocumentFileType, Exception>
			documentFileTypeUnsafeSupplier) {

		try {
			documentFileType = documentFileTypeUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "File type - pdf, jpg, or png")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotNull
	protected DocumentFileType documentFileType;

	@Schema
	@Valid
	public ConsentMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ConsentMetadata metadata) {
		this.metadata = metadata;
	}

	@JsonIgnore
	public void setMetadata(
		UnsafeSupplier<ConsentMetadata, Exception> metadataUnsafeSupplier) {

		try {
			metadata = metadataUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotNull
	protected ConsentMetadata metadata;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Base64ConsentUploadRequest)) {
			return false;
		}

		Base64ConsentUploadRequest base64ConsentUploadRequest =
			(Base64ConsentUploadRequest)object;

		return Objects.equals(
			toString(), base64ConsentUploadRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		if (document != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"document\": ");

			sb.append("\"");

			sb.append(_escape(document));

			sb.append("\"");
		}

		if (documentFileName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileName\": ");

			sb.append("\"");

			sb.append(_escape(documentFileName));

			sb.append("\"");
		}

		if (documentFileType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileType\": ");

			sb.append("\"");

			sb.append(documentFileType);

			sb.append("\"");
		}

		if (metadata != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"metadata\": ");

			sb.append(String.valueOf(metadata));
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.dms.dto.v1_0.Base64ConsentUploadRequest",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("DocumentFileType")
	public static enum DocumentFileType {

		PDF("pdf"), JPG("jpg"), PNG("png");

		@JsonCreator
		public static DocumentFileType create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (DocumentFileType documentFileType : values()) {
				if (Objects.equals(documentFileType.getValue(), value)) {
					return documentFileType;
				}
			}

			throw new IllegalArgumentException("Invalid enum value: " + value);
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private DocumentFileType(String value) {
			_value = value;
		}

		private final String _value;

	}

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

}