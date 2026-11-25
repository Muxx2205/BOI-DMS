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
@GraphQLName("RequestMetadataResponse")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "RequestMetadataResponse")
public class RequestMetadataResponse implements Serializable {

	public static RequestMetadataResponse toDTO(String json) {
		return ObjectMapperUtil.readValue(RequestMetadataResponse.class, json);
	}

	public static RequestMetadataResponse unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			RequestMetadataResponse.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public String getBranch() {
		if (_branchSupplier != null) {
			branch = _branchSupplier.get();

			_branchSupplier = null;
		}

		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;

		_branchSupplier = null;
	}

	@JsonIgnore
	public void setBranch(
		UnsafeSupplier<String, Exception> branchUnsafeSupplier) {

		_branchSupplier = () -> {
			try {
				return branchUnsafeSupplier.get();
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
	protected String branch;

	@JsonIgnore
	private Supplier<String> _branchSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getRegionalOffice() {
		if (_regionalOfficeSupplier != null) {
			regionalOffice = _regionalOfficeSupplier.get();

			_regionalOfficeSupplier = null;
		}

		return regionalOffice;
	}

	public void setRegionalOffice(String regionalOffice) {
		this.regionalOffice = regionalOffice;

		_regionalOfficeSupplier = null;
	}

	@JsonIgnore
	public void setRegionalOffice(
		UnsafeSupplier<String, Exception> regionalOfficeUnsafeSupplier) {

		_regionalOfficeSupplier = () -> {
			try {
				return regionalOfficeUnsafeSupplier.get();
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
	protected String regionalOffice;

	@JsonIgnore
	private Supplier<String> _regionalOfficeSupplier;

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

	@io.swagger.v3.oas.annotations.media.Schema
	public String getScreenName() {
		if (_screenNameSupplier != null) {
			screenName = _screenNameSupplier.get();

			_screenNameSupplier = null;
		}

		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;

		_screenNameSupplier = null;
	}

	@JsonIgnore
	public void setScreenName(
		UnsafeSupplier<String, Exception> screenNameUnsafeSupplier) {

		_screenNameSupplier = () -> {
			try {
				return screenNameUnsafeSupplier.get();
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
	protected String screenName;

	@JsonIgnore
	private Supplier<String> _screenNameSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public Long getUserId() {
		if (_userIdSupplier != null) {
			userId = _userIdSupplier.get();

			_userIdSupplier = null;
		}

		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;

		_userIdSupplier = null;
	}

	@JsonIgnore
	public void setUserId(
		UnsafeSupplier<Long, Exception> userIdUnsafeSupplier) {

		_userIdSupplier = () -> {
			try {
				return userIdUnsafeSupplier.get();
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
	protected Long userId;

	@JsonIgnore
	private Supplier<Long> _userIdSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof RequestMetadataResponse)) {
			return false;
		}

		RequestMetadataResponse requestMetadataResponse =
			(RequestMetadataResponse)object;

		return Objects.equals(toString(), requestMetadataResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		String branch = getBranch();

		if (branch != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(branch));

			sb.append("\"");
		}

		String regionalOffice = getRegionalOffice();

		if (regionalOffice != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"regionalOffice\": ");

			sb.append("\"");

			sb.append(_escape(regionalOffice));

			sb.append("\"");
		}

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

		String screenName = getScreenName();

		if (screenName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"screenName\": ");

			sb.append("\"");

			sb.append(_escape(screenName));

			sb.append("\"");
		}

		Long userId = getUserId();

		if (userId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"userId\": ");

			sb.append(userId);
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.rrb.consent.dto.v1_0.RequestMetadataResponse",
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
// LIFERAY-REST-BUILDER-HASH:2056892712