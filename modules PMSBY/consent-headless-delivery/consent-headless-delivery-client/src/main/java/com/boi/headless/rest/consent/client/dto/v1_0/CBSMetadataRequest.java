package com.boi.headless.rest.consent.client.dto.v1_0;

import com.boi.headless.rest.consent.client.function.UnsafeSupplier;
import com.boi.headless.rest.consent.client.serdes.v1_0.CBSMetadataRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class CBSMetadataRequest implements Cloneable, Serializable {

	public static CBSMetadataRequest toDTO(String json) {
		return CBSMetadataRequestSerDes.toDTO(json);
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setAccountNumber(
		UnsafeSupplier<String, Exception> accountNumberUnsafeSupplier) {

		try {
			accountNumber = accountNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String accountNumber;

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setBranch(
		UnsafeSupplier<String, Exception> branchUnsafeSupplier) {

		try {
			branch = branchUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String branch;

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public void setBranchCode(
		UnsafeSupplier<String, Exception> branchCodeUnsafeSupplier) {

		try {
			branchCode = branchCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String branchCode;

	public String getCbsReferenceNumber() {
		return cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(String cbsReferenceNumber) {
		this.cbsReferenceNumber = cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(
		UnsafeSupplier<String, Exception> cbsReferenceNumberUnsafeSupplier) {

		try {
			cbsReferenceNumber = cbsReferenceNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String cbsReferenceNumber;

	public String getConsentFormDocumentFrom() {
		return consentFormDocumentFrom;
	}

	public void setConsentFormDocumentFrom(String consentFormDocumentFrom) {
		this.consentFormDocumentFrom = consentFormDocumentFrom;
	}

	public void setConsentFormDocumentFrom(
		UnsafeSupplier<String, Exception>
			consentFormDocumentFromUnsafeSupplier) {

		try {
			consentFormDocumentFrom =
				consentFormDocumentFromUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String consentFormDocumentFrom;

	public String getConsentFormDocumentTo() {
		return consentFormDocumentTo;
	}

	public void setConsentFormDocumentTo(String consentFormDocumentTo) {
		this.consentFormDocumentTo = consentFormDocumentTo;
	}

	public void setConsentFormDocumentTo(
		UnsafeSupplier<String, Exception> consentFormDocumentToUnsafeSupplier) {

		try {
			consentFormDocumentTo = consentFormDocumentToUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String consentFormDocumentTo;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setCustomerId(
		UnsafeSupplier<String, Exception> customerIdUnsafeSupplier) {

		try {
			customerId = customerIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String customerId;

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public void setCustomerName(
		UnsafeSupplier<String, Exception> customerNameUnsafeSupplier) {

		try {
			customerName = customerNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String customerName;

	public String getNBGCode() {
		return nBGCode;
	}

	public void setNBGCode(String nBGCode) {
		this.nBGCode = nBGCode;
	}

	public void setNBGCode(
		UnsafeSupplier<String, Exception> nBGCodeUnsafeSupplier) {

		try {
			nBGCode = nBGCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nBGCode;

	public String getSolId() {
		return solId;
	}

	public void setSolId(String solId) {
		this.solId = solId;
	}

	public void setSolId(
		UnsafeSupplier<String, Exception> solIdUnsafeSupplier) {

		try {
			solId = solIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String solId;

	public String getTransDate() {
		return transDate;
	}

	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}

	public void setTransDate(
		UnsafeSupplier<String, Exception> transDateUnsafeSupplier) {

		try {
			transDate = transDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String transDate;

	public String getTypeOfInsurance() {
		return typeOfInsurance;
	}

	public void setTypeOfInsurance(String typeOfInsurance) {
		this.typeOfInsurance = typeOfInsurance;
	}

	public void setTypeOfInsurance(
		UnsafeSupplier<String, Exception> typeOfInsuranceUnsafeSupplier) {

		try {
			typeOfInsurance = typeOfInsuranceUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String typeOfInsurance;

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	public void setUrn(UnsafeSupplier<String, Exception> urnUnsafeSupplier) {
		try {
			urn = urnUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String urn;

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setYear(UnsafeSupplier<String, Exception> yearUnsafeSupplier) {
		try {
			year = yearUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String year;

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public void setZone(UnsafeSupplier<String, Exception> zoneUnsafeSupplier) {
		try {
			zone = zoneUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String zone;

	@Override
	public CBSMetadataRequest clone() throws CloneNotSupportedException {
		return (CBSMetadataRequest)super.clone();
	}

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
		return CBSMetadataRequestSerDes.toJSON(this);
	}

}