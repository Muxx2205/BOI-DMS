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
@GraphQLName("BOIHeadlessResponse")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "BOIHeadlessResponse")
public class BOIHeadlessResponse implements Serializable {

	public static BOIHeadlessResponse toDTO(String json) {
		return ObjectMapperUtil.readValue(BOIHeadlessResponse.class, json);
	}

	public static BOIHeadlessResponse unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			BOIHeadlessResponse.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public Integer getResponseId() {
		if (_responseIdSupplier != null) {
			responseId = _responseIdSupplier.get();

			_responseIdSupplier = null;
		}

		return responseId;
	}

	public void setResponseId(Integer responseId) {
		this.responseId = responseId;

		_responseIdSupplier = null;
	}

	@JsonIgnore
	public void setResponseId(
		UnsafeSupplier<Integer, Exception> responseIdUnsafeSupplier) {

		_responseIdSupplier = () -> {
			try {
				return responseIdUnsafeSupplier.get();
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
	protected Integer responseId;

	@JsonIgnore
	private Supplier<Integer> _responseIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getResponseStatus() {
		if (_responseStatusSupplier != null) {
			responseStatus = _responseStatusSupplier.get();

			_responseStatusSupplier = null;
		}

		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;

		_responseStatusSupplier = null;
	}

	@JsonIgnore
	public void setResponseStatus(
		UnsafeSupplier<String, Exception> responseStatusUnsafeSupplier) {

		_responseStatusSupplier = () -> {
			try {
				return responseStatusUnsafeSupplier.get();
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
	protected String responseStatus;

	@JsonIgnore
	private Supplier<String> _responseStatusSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof BOIHeadlessResponse)) {
			return false;
		}

		BOIHeadlessResponse boiHeadlessResponse = (BOIHeadlessResponse)object;

		return Objects.equals(toString(), boiHeadlessResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		Integer responseId = getResponseId();

		if (responseId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseId\": ");

			sb.append(responseId);
		}

		String responseStatus = getResponseStatus();

		if (responseStatus != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseStatus\": ");

			sb.append("\"");

			sb.append(_escape(responseStatus));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.rrb.consent.dto.v1_0.BOIHeadlessResponse",
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
// LIFERAY-REST-BUILDER-HASH:204530215