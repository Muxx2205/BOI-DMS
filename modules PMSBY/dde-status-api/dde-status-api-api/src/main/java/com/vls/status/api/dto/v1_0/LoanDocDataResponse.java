package com.vls.status.api.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("LoanDocDataResponse")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "LoanDocDataResponse")
public class LoanDocDataResponse implements Serializable {

	public static LoanDocDataResponse toDTO(String json) {
		return ObjectMapperUtil.readValue(LoanDocDataResponse.class, json);
	}

	public static LoanDocDataResponse unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			LoanDocDataResponse.class, json);
	}

	@Schema
	public String getCipherText() {
		return cipherText;
	}

	public void setCipherText(String cipherText) {
		this.cipherText = cipherText;
	}

	@JsonIgnore
	public void setCipherText(
		UnsafeSupplier<String, Exception> cipherTextUnsafeSupplier) {

		try {
			cipherText = cipherTextUnsafeSupplier.get();
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
	protected String cipherText;

	@Schema
	public String getDigisign() {
		return digisign;
	}

	public void setDigisign(String digisign) {
		this.digisign = digisign;
	}

	@JsonIgnore
	public void setDigisign(
		UnsafeSupplier<String, Exception> digisignUnsafeSupplier) {

		try {
			digisign = digisignUnsafeSupplier.get();
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
	protected String digisign;

	@Schema
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@JsonIgnore
	public void setSessionId(
		UnsafeSupplier<String, Exception> sessionIdUnsafeSupplier) {

		try {
			sessionId = sessionIdUnsafeSupplier.get();
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
	protected String sessionId;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof LoanDocDataResponse)) {
			return false;
		}

		LoanDocDataResponse loanDocDataResponse = (LoanDocDataResponse)object;

		return Objects.equals(toString(), loanDocDataResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		if (cipherText != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cipherText\": ");

			sb.append("\"");

			sb.append(_escape(cipherText));

			sb.append("\"");
		}

		if (digisign != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"digisign\": ");

			sb.append("\"");

			sb.append(_escape(digisign));

			sb.append("\"");
		}

		if (sessionId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"sessionId\": ");

			sb.append("\"");

			sb.append(_escape(sessionId));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.vls.status.api.dto.v1_0.LoanDocDataResponse",
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