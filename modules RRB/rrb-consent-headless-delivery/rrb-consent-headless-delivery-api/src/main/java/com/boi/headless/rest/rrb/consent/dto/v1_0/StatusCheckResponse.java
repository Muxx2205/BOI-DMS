/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.boi.headless.rest.rrb.consent.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("StatusCheckResponse")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "StatusCheckResponse")
public class StatusCheckResponse implements Serializable {

	public static StatusCheckResponse toDTO(String json) {
		return ObjectMapperUtil.readValue(StatusCheckResponse.class, json);
	}

	public static StatusCheckResponse unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			StatusCheckResponse.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public String getDocumentReferenceNo() {
		if (_documentReferenceNoSupplier != null) {
			documentReferenceNo = _documentReferenceNoSupplier.get();

			_documentReferenceNoSupplier = null;
		}

		return documentReferenceNo;
	}

	public void setDocumentReferenceNo(String documentReferenceNo) {
		this.documentReferenceNo = documentReferenceNo;

		_documentReferenceNoSupplier = null;
	}

	@JsonIgnore
	public void setDocumentReferenceNo(
		UnsafeSupplier<String, Exception> documentReferenceNoUnsafeSupplier) {

		_documentReferenceNoSupplier = () -> {
			try {
				return documentReferenceNoUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String documentReferenceNo;

	@JsonIgnore
	private Supplier<String> _documentReferenceNoSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public Boolean getDocumentVerified() {
		if (_documentVerifiedSupplier != null) {
			documentVerified = _documentVerifiedSupplier.get();

			_documentVerifiedSupplier = null;
		}

		return documentVerified;
	}

	public void setDocumentVerified(Boolean documentVerified) {
		this.documentVerified = documentVerified;

		_documentVerifiedSupplier = null;
	}

	@JsonIgnore
	public void setDocumentVerified(
		UnsafeSupplier<Boolean, Exception> documentVerifiedUnsafeSupplier) {

		_documentVerifiedSupplier = () -> {
			try {
				return documentVerifiedUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Boolean documentVerified;

	@JsonIgnore
	private Supplier<Boolean> _documentVerifiedSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getMessage() {
		if (_messageSupplier != null) {
			message = _messageSupplier.get();

			_messageSupplier = null;
		}

		return message;
	}

	public void setMessage(String message) {
		this.message = message;

		_messageSupplier = null;
	}

	@JsonIgnore
	public void setMessage(
		UnsafeSupplier<String, Exception> messageUnsafeSupplier) {

		_messageSupplier = () -> {
			try {
				return messageUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String message;

	@JsonIgnore
	private Supplier<String> _messageSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public Integer getStage() {
		if (_stageSupplier != null) {
			stage = _stageSupplier.get();

			_stageSupplier = null;
		}

		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;

		_stageSupplier = null;
	}

	@JsonIgnore
	public void setStage(
		UnsafeSupplier<Integer, Exception> stageUnsafeSupplier) {

		_stageSupplier = () -> {
			try {
				return stageUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Integer stage;

	@JsonIgnore
	private Supplier<Integer> _stageSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof StatusCheckResponse)) {
			return false;
		}

		StatusCheckResponse statusCheckResponse = (StatusCheckResponse)object;

		return Objects.equals(toString(), statusCheckResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		String documentReferenceNo = getDocumentReferenceNo();

		if (documentReferenceNo != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(documentReferenceNo));

			sb.append("\"");
		}

		Boolean documentVerified = getDocumentVerified();

		if (documentVerified != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentVerified\": ");

			sb.append(documentVerified);
		}

		String message = getMessage();

		if (message != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"message\": ");

			sb.append("\"");

			sb.append(_escape(message));

			sb.append("\"");
		}

		Integer stage = getStage();

		if (stage != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"stage\": ");

			sb.append(stage);
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.rrb.consent.dto.v1_0.StatusCheckResponse",
		name = "x-class-name"
	)
	public String xClassName;

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
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
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

	private Map<String, Serializable> _extendedProperties;

}
// LIFERAY-REST-BUILDER-HASH:1461306