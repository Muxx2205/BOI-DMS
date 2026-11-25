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
@GraphQLName("CBSMetadataRequest")
@JsonFilter("Liferay.Vulcan")
@Schema(
	requiredProperties = {
		"cbsReferenceNumber", "accountNumber", "customerName", "branchCode",
		"typeOfInsurance"
	}
)
@XmlRootElement(name = "CBSMetadataRequest")
public class CBSMetadataRequest implements Serializable {

	public static CBSMetadataRequest toDTO(String json) {
		return ObjectMapperUtil.readValue(CBSMetadataRequest.class, json);
	}

	public static CBSMetadataRequest unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(CBSMetadataRequest.class, json);
	}

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String accountNumber;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String branch;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String branchCode;

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

	@Schema(description = "Document start page number")
	public String getConsentFormDocumentFrom() {
		return consentFormDocumentFrom;
	}

	public void setConsentFormDocumentFrom(String consentFormDocumentFrom) {
		this.consentFormDocumentFrom = consentFormDocumentFrom;
	}

	@JsonIgnore
	public void setConsentFormDocumentFrom(
		UnsafeSupplier<String, Exception>
			consentFormDocumentFromUnsafeSupplier) {

		try {
			consentFormDocumentFrom =
				consentFormDocumentFromUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Document start page number")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String consentFormDocumentFrom;

	@Schema(description = "Document end page number")
	public String getConsentFormDocumentTo() {
		return consentFormDocumentTo;
	}

	public void setConsentFormDocumentTo(String consentFormDocumentTo) {
		this.consentFormDocumentTo = consentFormDocumentTo;
	}

	@JsonIgnore
	public void setConsentFormDocumentTo(
		UnsafeSupplier<String, Exception> consentFormDocumentToUnsafeSupplier) {

		try {
			consentFormDocumentTo = consentFormDocumentToUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Document end page number")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String consentFormDocumentTo;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String customerId;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String customerName;

	@Schema
	public String getRegionalOffice() {
		return regionalOffice;
	}

	public void setRegionalOffice(String regionalOffice) {
		this.regionalOffice = regionalOffice;
	}

	@JsonIgnore
	public void setRegionalOffice(
		UnsafeSupplier<String, Exception> regionalOfficeUnsafeSupplier) {

		try {
			regionalOffice = regionalOfficeUnsafeSupplier.get();
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
	protected String regionalOffice;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String solId;

	@Schema(description = "Transaction date (format: yyyy-MM-dd)")
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

	@GraphQLField(description = "Transaction date (format: yyyy-MM-dd)")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String transDate;

	@Schema
	public String getTypeOfInsurance() {
		return typeOfInsurance;
	}

	public void setTypeOfInsurance(String typeOfInsurance) {
		this.typeOfInsurance = typeOfInsurance;
	}

	@JsonIgnore
	public void setTypeOfInsurance(
		UnsafeSupplier<String, Exception> typeOfInsuranceUnsafeSupplier) {

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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String typeOfInsurance;

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
	protected String urn;

	@Schema
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

	@GraphQLField
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String year;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CBSMetadataRequest)) {
			return false;
		}

		CBSMetadataRequest cbsMetadataRequest = (CBSMetadataRequest)object;

		return Objects.equals(toString(), cbsMetadataRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

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

		if (cbsReferenceNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(cbsReferenceNumber));

			sb.append("\"");
		}

		if (consentFormDocumentFrom != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentFrom\": ");

			sb.append("\"");

			sb.append(_escape(consentFormDocumentFrom));

			sb.append("\"");
		}

		if (consentFormDocumentTo != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentTo\": ");

			sb.append("\"");

			sb.append(_escape(consentFormDocumentTo));

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

		if (regionalOffice != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"regionalOffice\": ");

			sb.append("\"");

			sb.append(_escape(regionalOffice));

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

			sb.append(_escape(transDate));

			sb.append("\"");
		}

		if (typeOfInsurance != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"typeOfInsurance\": ");

			sb.append("\"");

			sb.append(_escape(typeOfInsurance));

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

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.boi.headless.rest.rrb.consent.dto.v1_0.CBSMetadataRequest",
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