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

import jakarta.validation.constraints.NotEmpty;

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
@GraphQLName("CBSMetadataRequest")
@io.swagger.v3.oas.annotations.media.Schema(
	requiredProperties = {
		"cbsReferenceNumber", "accountNumber", "customerName", "branchCode",
		"typeOfInsurance"
	}
)
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "CBSMetadataRequest")
public class CBSMetadataRequest implements Serializable {

	public static CBSMetadataRequest toDTO(String json) {
		return ObjectMapperUtil.readValue(CBSMetadataRequest.class, json);
	}

	public static CBSMetadataRequest unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(CBSMetadataRequest.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema
	public String getAccountNumber() {
		if (_accountNumberSupplier != null) {
			accountNumber = _accountNumberSupplier.get();

			_accountNumberSupplier = null;
		}

		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;

		_accountNumberSupplier = null;
	}

	@JsonIgnore
	public void setAccountNumber(
		UnsafeSupplier<String, Exception> accountNumberUnsafeSupplier) {

		_accountNumberSupplier = () -> {
			try {
				return accountNumberUnsafeSupplier.get();
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
	@NotEmpty
	protected String accountNumber;

	@JsonIgnore
	private Supplier<String> _accountNumberSupplier;

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
	public String getBranchCode() {
		if (_branchCodeSupplier != null) {
			branchCode = _branchCodeSupplier.get();

			_branchCodeSupplier = null;
		}

		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;

		_branchCodeSupplier = null;
	}

	@JsonIgnore
	public void setBranchCode(
		UnsafeSupplier<String, Exception> branchCodeUnsafeSupplier) {

		_branchCodeSupplier = () -> {
			try {
				return branchCodeUnsafeSupplier.get();
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
	@NotEmpty
	protected String branchCode;

	@JsonIgnore
	private Supplier<String> _branchCodeSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getCbsReferenceNumber() {
		if (_cbsReferenceNumberSupplier != null) {
			cbsReferenceNumber = _cbsReferenceNumberSupplier.get();

			_cbsReferenceNumberSupplier = null;
		}

		return cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(String cbsReferenceNumber) {
		this.cbsReferenceNumber = cbsReferenceNumber;

		_cbsReferenceNumberSupplier = null;
	}

	@JsonIgnore
	public void setCbsReferenceNumber(
		UnsafeSupplier<String, Exception> cbsReferenceNumberUnsafeSupplier) {

		_cbsReferenceNumberSupplier = () -> {
			try {
				return cbsReferenceNumberUnsafeSupplier.get();
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
	@NotEmpty
	protected String cbsReferenceNumber;

	@JsonIgnore
	private Supplier<String> _cbsReferenceNumberSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Document start page number"
	)
	public String getConsentFormDocumentFrom() {
		if (_consentFormDocumentFromSupplier != null) {
			consentFormDocumentFrom = _consentFormDocumentFromSupplier.get();

			_consentFormDocumentFromSupplier = null;
		}

		return consentFormDocumentFrom;
	}

	public void setConsentFormDocumentFrom(String consentFormDocumentFrom) {
		this.consentFormDocumentFrom = consentFormDocumentFrom;

		_consentFormDocumentFromSupplier = null;
	}

	@JsonIgnore
	public void setConsentFormDocumentFrom(
		UnsafeSupplier<String, Exception>
			consentFormDocumentFromUnsafeSupplier) {

		_consentFormDocumentFromSupplier = () -> {
			try {
				return consentFormDocumentFromUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "Document start page number")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String consentFormDocumentFrom;

	@JsonIgnore
	private Supplier<String> _consentFormDocumentFromSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Document end page number"
	)
	public String getConsentFormDocumentTo() {
		if (_consentFormDocumentToSupplier != null) {
			consentFormDocumentTo = _consentFormDocumentToSupplier.get();

			_consentFormDocumentToSupplier = null;
		}

		return consentFormDocumentTo;
	}

	public void setConsentFormDocumentTo(String consentFormDocumentTo) {
		this.consentFormDocumentTo = consentFormDocumentTo;

		_consentFormDocumentToSupplier = null;
	}

	@JsonIgnore
	public void setConsentFormDocumentTo(
		UnsafeSupplier<String, Exception> consentFormDocumentToUnsafeSupplier) {

		_consentFormDocumentToSupplier = () -> {
			try {
				return consentFormDocumentToUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "Document end page number")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String consentFormDocumentTo;

	@JsonIgnore
	private Supplier<String> _consentFormDocumentToSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getCustomerId() {
		if (_customerIdSupplier != null) {
			customerId = _customerIdSupplier.get();

			_customerIdSupplier = null;
		}

		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;

		_customerIdSupplier = null;
	}

	@JsonIgnore
	public void setCustomerId(
		UnsafeSupplier<String, Exception> customerIdUnsafeSupplier) {

		_customerIdSupplier = () -> {
			try {
				return customerIdUnsafeSupplier.get();
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
	protected String customerId;

	@JsonIgnore
	private Supplier<String> _customerIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getCustomerName() {
		if (_customerNameSupplier != null) {
			customerName = _customerNameSupplier.get();

			_customerNameSupplier = null;
		}

		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;

		_customerNameSupplier = null;
	}

	@JsonIgnore
	public void setCustomerName(
		UnsafeSupplier<String, Exception> customerNameUnsafeSupplier) {

		_customerNameSupplier = () -> {
			try {
				return customerNameUnsafeSupplier.get();
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
	@NotEmpty
	protected String customerName;

	@JsonIgnore
	private Supplier<String> _customerNameSupplier;

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
	public String getSolId() {
		if (_solIdSupplier != null) {
			solId = _solIdSupplier.get();

			_solIdSupplier = null;
		}

		return solId;
	}

	public void setSolId(String solId) {
		this.solId = solId;

		_solIdSupplier = null;
	}

	@JsonIgnore
	public void setSolId(
		UnsafeSupplier<String, Exception> solIdUnsafeSupplier) {

		_solIdSupplier = () -> {
			try {
				return solIdUnsafeSupplier.get();
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
	protected String solId;

	@JsonIgnore
	private Supplier<String> _solIdSupplier;

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "Transaction date (format: yyyy-MM-dd)"
	)
	public String getTransDate() {
		if (_transDateSupplier != null) {
			transDate = _transDateSupplier.get();

			_transDateSupplier = null;
		}

		return transDate;
	}

	public void setTransDate(String transDate) {
		this.transDate = transDate;

		_transDateSupplier = null;
	}

	@JsonIgnore
	public void setTransDate(
		UnsafeSupplier<String, Exception> transDateUnsafeSupplier) {

		_transDateSupplier = () -> {
			try {
				return transDateUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "Transaction date (format: yyyy-MM-dd)")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String transDate;

	@JsonIgnore
	private Supplier<String> _transDateSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getTypeOfInsurance() {
		if (_typeOfInsuranceSupplier != null) {
			typeOfInsurance = _typeOfInsuranceSupplier.get();

			_typeOfInsuranceSupplier = null;
		}

		return typeOfInsurance;
	}

	public void setTypeOfInsurance(String typeOfInsurance) {
		this.typeOfInsurance = typeOfInsurance;

		_typeOfInsuranceSupplier = null;
	}

	@JsonIgnore
	public void setTypeOfInsurance(
		UnsafeSupplier<String, Exception> typeOfInsuranceUnsafeSupplier) {

		_typeOfInsuranceSupplier = () -> {
			try {
				return typeOfInsuranceUnsafeSupplier.get();
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
	@NotEmpty
	protected String typeOfInsurance;

	@JsonIgnore
	private Supplier<String> _typeOfInsuranceSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getUrn() {
		if (_urnSupplier != null) {
			urn = _urnSupplier.get();

			_urnSupplier = null;
		}

		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;

		_urnSupplier = null;
	}

	@JsonIgnore
	public void setUrn(UnsafeSupplier<String, Exception> urnUnsafeSupplier) {
		_urnSupplier = () -> {
			try {
				return urnUnsafeSupplier.get();
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
	protected String urn;

	@JsonIgnore
	private Supplier<String> _urnSupplier;

	@io.swagger.v3.oas.annotations.media.Schema
	public String getYear() {
		if (_yearSupplier != null) {
			year = _yearSupplier.get();

			_yearSupplier = null;
		}

		return year;
	}

	public void setYear(String year) {
		this.year = year;

		_yearSupplier = null;
	}

	@JsonIgnore
	public void setYear(UnsafeSupplier<String, Exception> yearUnsafeSupplier) {
		_yearSupplier = () -> {
			try {
				return yearUnsafeSupplier.get();
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
	protected String year;

	@JsonIgnore
	private Supplier<String> _yearSupplier;

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

		String accountNumber = getAccountNumber();

		if (accountNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountNumber\": ");

			sb.append("\"");

			sb.append(_escape(accountNumber));

			sb.append("\"");
		}

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

		String branchCode = getBranchCode();

		if (branchCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(branchCode));

			sb.append("\"");
		}

		String cbsReferenceNumber = getCbsReferenceNumber();

		if (cbsReferenceNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(cbsReferenceNumber));

			sb.append("\"");
		}

		String consentFormDocumentFrom = getConsentFormDocumentFrom();

		if (consentFormDocumentFrom != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentFrom\": ");

			sb.append("\"");

			sb.append(_escape(consentFormDocumentFrom));

			sb.append("\"");
		}

		String consentFormDocumentTo = getConsentFormDocumentTo();

		if (consentFormDocumentTo != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentTo\": ");

			sb.append("\"");

			sb.append(_escape(consentFormDocumentTo));

			sb.append("\"");
		}

		String customerId = getCustomerId();

		if (customerId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerId\": ");

			sb.append("\"");

			sb.append(_escape(customerId));

			sb.append("\"");
		}

		String customerName = getCustomerName();

		if (customerName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerName\": ");

			sb.append("\"");

			sb.append(_escape(customerName));

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

		String solId = getSolId();

		if (solId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"solId\": ");

			sb.append("\"");

			sb.append(_escape(solId));

			sb.append("\"");
		}

		String transDate = getTransDate();

		if (transDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(_escape(transDate));

			sb.append("\"");
		}

		String typeOfInsurance = getTypeOfInsurance();

		if (typeOfInsurance != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"typeOfInsurance\": ");

			sb.append("\"");

			sb.append(_escape(typeOfInsurance));

			sb.append("\"");
		}

		String urn = getUrn();

		if (urn != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(urn));

			sb.append("\"");
		}

		String year = getYear();

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

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
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
// LIFERAY-REST-BUILDER-HASH:179686796