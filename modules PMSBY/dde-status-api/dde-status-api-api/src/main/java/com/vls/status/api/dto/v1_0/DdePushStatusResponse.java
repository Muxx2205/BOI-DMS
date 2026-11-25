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

import javax.validation.constraints.NotEmpty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("DdePushStatusResponse")
@JsonFilter("Liferay.Vulcan")
@Schema(requiredProperties = {"transId", "statusCode", "statusMsg"})
@XmlRootElement(name = "DdePushStatusResponse")
public class DdePushStatusResponse implements Serializable {

	public static DdePushStatusResponse toDTO(String json) {
		return ObjectMapperUtil.readValue(DdePushStatusResponse.class, json);
	}

	public static DdePushStatusResponse unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			DdePushStatusResponse.class, json);
	}

	@Schema
	public String getDocDate() {
		return docDate;
	}

	public void setDocDate(String docDate) {
		this.docDate = docDate;
	}

	@JsonIgnore
	public void setDocDate(
		UnsafeSupplier<String, Exception> docDateUnsafeSupplier) {

		try {
			docDate = docDateUnsafeSupplier.get();
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
	protected String docDate;

	@Schema
	public String getLoanno() {
		return loanno;
	}

	public void setLoanno(String loanno) {
		this.loanno = loanno;
	}

	@JsonIgnore
	public void setLoanno(
		UnsafeSupplier<String, Exception> loannoUnsafeSupplier) {

		try {
			loanno = loannoUnsafeSupplier.get();
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
	protected String loanno;

	@Schema
	public String getSnctnno() {
		return snctnno;
	}

	public void setSnctnno(String snctnno) {
		this.snctnno = snctnno;
	}

	@JsonIgnore
	public void setSnctnno(
		UnsafeSupplier<String, Exception> snctnnoUnsafeSupplier) {

		try {
			snctnno = snctnnoUnsafeSupplier.get();
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
	protected String snctnno;

	@Schema
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	@JsonIgnore
	public void setStatusCode(
		UnsafeSupplier<String, Exception> statusCodeUnsafeSupplier) {

		try {
			statusCode = statusCodeUnsafeSupplier.get();
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
	protected String statusCode;

	@Schema
	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	@JsonIgnore
	public void setStatusMsg(
		UnsafeSupplier<String, Exception> statusMsgUnsafeSupplier) {

		try {
			statusMsg = statusMsgUnsafeSupplier.get();
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
	protected String statusMsg;

	@Schema
	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}

	@JsonIgnore
	public void setTransId(
		UnsafeSupplier<String, Exception> transIdUnsafeSupplier) {

		try {
			transId = transIdUnsafeSupplier.get();
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
	protected String transId;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DdePushStatusResponse)) {
			return false;
		}

		DdePushStatusResponse ddePushStatusResponse =
			(DdePushStatusResponse)object;

		return Objects.equals(toString(), ddePushStatusResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		if (docDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"docDate\": ");

			sb.append("\"");

			sb.append(_escape(docDate));

			sb.append("\"");
		}

		if (loanno != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"loanno\": ");

			sb.append("\"");

			sb.append(_escape(loanno));

			sb.append("\"");
		}

		if (snctnno != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"snctnno\": ");

			sb.append("\"");

			sb.append(_escape(snctnno));

			sb.append("\"");
		}

		if (statusCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"statusCode\": ");

			sb.append("\"");

			sb.append(_escape(statusCode));

			sb.append("\"");
		}

		if (statusMsg != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"statusMsg\": ");

			sb.append("\"");

			sb.append(_escape(statusMsg));

			sb.append("\"");
		}

		if (transId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transId\": ");

			sb.append("\"");

			sb.append(_escape(transId));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.vls.status.api.dto.v1_0.DdePushStatusResponse",
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