package com.boi.headless.rest.consent.dto.v1_0;

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

import javax.validation.constraints.NotEmpty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("URNUpdateRequest")
@JsonFilter("Liferay.Vulcan")
@Schema(requiredProperties = {"cbsReferenceNumber", "urn", "status"})
@XmlRootElement(name = "URNUpdateRequest")
public class URNUpdateRequest implements Serializable {

	public static URNUpdateRequest toDTO(String json) {
		return ObjectMapperUtil.readValue(URNUpdateRequest.class, json);
	}

	public static URNUpdateRequest unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(URNUpdateRequest.class, json);
	}

	@Schema
	public String getCbsReferenceNumber() {
		return cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(String cbsReferenceNumber) {
		this.cbsReferenceNumber = cbsReferenceNumber;
	}

	@JsonIgnore
	public void setCbsReferenceNumber(
		UnsafeSupplier<String, Exception> cbsReferenceNumberUnsafeSupplier) {

		try {
			cbsReferenceNumber = cbsReferenceNumberUnsafeSupplier.get();
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
	@NotEmpty
	protected String cbsReferenceNumber;

	@Schema(description = "Status: APPROVED, REJECTED, or DELETED")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@JsonIgnore
	public void setStatus(
		UnsafeSupplier<String, Exception> statusUnsafeSupplier) {

		try {
			status = statusUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Status: APPROVED, REJECTED, or DELETED")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String status;

	@Schema(description = "Transaction date (optional, format: yyyy-MM-dd)")
	public String getTransDate() {
		return transDate;
	}

	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}

	@JsonIgnore
	public void setTransDate(
		UnsafeSupplier<String, Exception> transDateUnsafeSupplier) {

		try {
			transDate = transDateUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Transaction date (optional, format: yyyy-MM-dd)"
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String transDate;

	@Schema
	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	@JsonIgnore
	public void setUrn(UnsafeSupplier<String, Exception> urnUnsafeSupplier) {
		try {
			urn = urnUnsafeSupplier.get();
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
	@NotEmpty
	protected String urn;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof URNUpdateRequest)) {
			return false;
		}

		URNUpdateRequest urnUpdateRequest = (URNUpdateRequest)object;

		return Objects.equals(toString(), urnUpdateRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		if (cbsReferenceNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(cbsReferenceNumber));

			sb.append("\"");
		}

		if (status != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(status));

			sb.append("\"");
		}

		if (transDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(_escape(transDate));

			sb.append("\"");
		}

		if (urn != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(urn));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.consent.dto.v1_0.URNUpdateRequest",
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