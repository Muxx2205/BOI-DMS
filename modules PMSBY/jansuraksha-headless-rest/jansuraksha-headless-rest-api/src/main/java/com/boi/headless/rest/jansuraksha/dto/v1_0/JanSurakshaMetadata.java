package com.boi.headless.rest.jansuraksha.dto.v1_0;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
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
@GraphQLName("JanSurakshaMetadata")
@JsonFilter("Liferay.Vulcan")
@Schema(
	requiredProperties = {
		"accountNumber", "branchCode", "customerName", "typeOfInsurance"
	}
)
@XmlRootElement(name = "JanSurakshaMetadata")
public class JanSurakshaMetadata implements Serializable {

	public static JanSurakshaMetadata toDTO(String json) {
		return ObjectMapperUtil.readValue(JanSurakshaMetadata.class, json);
	}

	public static JanSurakshaMetadata unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			JanSurakshaMetadata.class, json);
	}

	@Schema(description = "Bank account number")
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	@JsonIgnore
	public void setAccountNumber(
		UnsafeSupplier<String, Exception> accountNumberUnsafeSupplier) {

		try {
			accountNumber = accountNumberUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Bank account number")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String accountNumber;

	@Schema(description = "Branch name")
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@JsonIgnore
	public void setBranch(
		UnsafeSupplier<String, Exception> branchUnsafeSupplier) {

		try {
			branch = branchUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Branch name")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String branch;

	@Schema(description = "4-digit branch code")
	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	@JsonIgnore
	public void setBranchCode(
		UnsafeSupplier<String, Exception> branchCodeUnsafeSupplier) {

		try {
			branchCode = branchCodeUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "4-digit branch code")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String branchCode;

	@Schema(description = "Bank's internal customer ID")
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@JsonIgnore
	public void setCustomerId(
		UnsafeSupplier<String, Exception> customerIdUnsafeSupplier) {

		try {
			customerId = customerIdUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Bank's internal customer ID")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String customerId;

	@Schema(description = "Full name of customer")
	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	@JsonIgnore
	public void setCustomerName(
		UnsafeSupplier<String, Exception> customerNameUnsafeSupplier) {

		try {
			customerName = customerNameUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Full name of customer")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String customerName;

	@Schema(description = "NBG Code")
	public String getNBGCode() {
		return nBGCode;
	}

	public void setNBGCode(String nBGCode) {
		this.nBGCode = nBGCode;
	}

	@JsonIgnore
	public void setNBGCode(
		UnsafeSupplier<String, Exception> nBGCodeUnsafeSupplier) {

		try {
			nBGCode = nBGCodeUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "NBG Code")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String nBGCode;

	@Schema(description = "SOL ID")
	public String getSolId() {
		return solId;
	}

	public void setSolId(String solId) {
		this.solId = solId;
	}

	@JsonIgnore
	public void setSolId(
		UnsafeSupplier<String, Exception> solIdUnsafeSupplier) {

		try {
			solId = solIdUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "SOL ID")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String solId;

	@Schema(description = "Transaction date (YYYY-MM-DD)")
	public Date getTransDate() {
		return transDate;
	}

	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}

	@JsonIgnore
	public void setTransDate(
		UnsafeSupplier<Date, Exception> transDateUnsafeSupplier) {

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

	@GraphQLField(description = "Transaction date (YYYY-MM-DD)")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Date transDate;

	@Schema(description = "Type of insurance scheme")
	@Valid
	public TypeOfInsurance getTypeOfInsurance() {
		return typeOfInsurance;
	}

	@JsonIgnore
	public String getTypeOfInsuranceAsString() {
		if (typeOfInsurance == null) {
			return null;
		}

		return typeOfInsurance.toString();
	}

	public void setTypeOfInsurance(TypeOfInsurance typeOfInsurance) {
		this.typeOfInsurance = typeOfInsurance;
	}

	@JsonIgnore
	public void setTypeOfInsurance(
		UnsafeSupplier<TypeOfInsurance, Exception>
			typeOfInsuranceUnsafeSupplier) {

		try {
			typeOfInsurance = typeOfInsuranceUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Type of insurance scheme")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotNull
	protected TypeOfInsurance typeOfInsurance;

	@Schema(description = "JanSuraksha URN")
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

	@GraphQLField(description = "JanSuraksha URN")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String urn;

	@Schema(description = "Policy year (e.g., 2026-27)")
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@JsonIgnore
	public void setYear(UnsafeSupplier<String, Exception> yearUnsafeSupplier) {
		try {
			year = yearUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Policy year (e.g., 2026-27)")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String year;

	@Schema(description = "Zone name")
	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	@JsonIgnore
	public void setZone(UnsafeSupplier<String, Exception> zoneUnsafeSupplier) {
		try {
			zone = zoneUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Zone name")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String zone;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof JanSurakshaMetadata)) {
			return false;
		}

		JanSurakshaMetadata janSurakshaMetadata = (JanSurakshaMetadata)object;

		return Objects.equals(toString(), janSurakshaMetadata.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (accountNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountNumber\": ");

			sb.append("\"");

			sb.append(_escape(accountNumber));

			sb.append("\"");
		}

		if (branch != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(branch));

			sb.append("\"");
		}

		if (branchCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(branchCode));

			sb.append("\"");
		}

		if (customerId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerId\": ");

			sb.append("\"");

			sb.append(_escape(customerId));

			sb.append("\"");
		}

		if (customerName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerName\": ");

			sb.append("\"");

			sb.append(_escape(customerName));

			sb.append("\"");
		}

		if (nBGCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nBGCode\": ");

			sb.append("\"");

			sb.append(_escape(nBGCode));

			sb.append("\"");
		}

		if (solId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"solId\": ");

			sb.append("\"");

			sb.append(_escape(solId));

			sb.append("\"");
		}

		if (transDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(transDate));

			sb.append("\"");
		}

		if (typeOfInsurance != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"typeOfInsurance\": ");

			sb.append("\"");

			sb.append(typeOfInsurance);

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

		if (year != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"year\": ");

			sb.append("\"");

			sb.append(_escape(year));

			sb.append("\"");
		}

		if (zone != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"zone\": ");

			sb.append("\"");

			sb.append(_escape(zone));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.jansuraksha.dto.v1_0.JanSurakshaMetadata",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("TypeOfInsurance")
	public static enum TypeOfInsurance {

		PMJJBY("PMJJBY"), PMSBY("PMSBY");

		@JsonCreator
		public static TypeOfInsurance create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (TypeOfInsurance typeOfInsurance : values()) {
				if (Objects.equals(typeOfInsurance.getValue(), value)) {
					return typeOfInsurance;
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

		private TypeOfInsurance(String value) {
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