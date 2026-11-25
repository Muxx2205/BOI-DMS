package com.boi.headless.rest.consent.dto.v1_0;

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
@GraphQLName("ConsentMetadata")
@JsonFilter("Liferay.Vulcan")
@Schema(
	requiredProperties = {
		"accountHolderName", "mobileNumber", "bankAccountNo", "ifscCode",
		"insuranceType", "policyYear", "policyIssuanceStatus",
		"transactionDate", "documentPushDateTime", "documentSentStatus",
		"branchName"
	}
)
@XmlRootElement(name = "ConsentMetadata")
public class ConsentMetadata implements Serializable {

	public static ConsentMetadata toDTO(String json) {
		return ObjectMapperUtil.readValue(ConsentMetadata.class, json);
	}

	public static ConsentMetadata unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(ConsentMetadata.class, json);
	}

	@Schema
	public String getAadhaarNumber() {
		return aadhaarNumber;
	}

	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}

	@JsonIgnore
	public void setAadhaarNumber(
		UnsafeSupplier<String, Exception> aadhaarNumberUnsafeSupplier) {

		try {
			aadhaarNumber = aadhaarNumberUnsafeSupplier.get();
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
	protected String aadhaarNumber;

	@Schema
	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	@JsonIgnore
	public void setAccountHolderName(
		UnsafeSupplier<String, Exception> accountHolderNameUnsafeSupplier) {

		try {
			accountHolderName = accountHolderNameUnsafeSupplier.get();
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
	protected String accountHolderName;

	@Schema
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@JsonIgnore
	public void setAddress(
		UnsafeSupplier<String, Exception> addressUnsafeSupplier) {

		try {
			address = addressUnsafeSupplier.get();
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
	protected String address;

	@Schema
	public String getBankAccountNo() {
		return bankAccountNo;
	}

	public void setBankAccountNo(String bankAccountNo) {
		this.bankAccountNo = bankAccountNo;
	}

	@JsonIgnore
	public void setBankAccountNo(
		UnsafeSupplier<String, Exception> bankAccountNoUnsafeSupplier) {

		try {
			bankAccountNo = bankAccountNoUnsafeSupplier.get();
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
	protected String bankAccountNo;

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
	protected String branchCode;

	@Schema
	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	@JsonIgnore
	public void setBranchName(
		UnsafeSupplier<String, Exception> branchNameUnsafeSupplier) {

		try {
			branchName = branchNameUnsafeSupplier.get();
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
	protected String branchName;

	@Schema
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@JsonIgnore
	public void setCity(UnsafeSupplier<String, Exception> cityUnsafeSupplier) {
		try {
			city = cityUnsafeSupplier.get();
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
	protected String city;

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
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@JsonIgnore
	public void setDateOfBirth(
		UnsafeSupplier<Date, Exception> dateOfBirthUnsafeSupplier) {

		try {
			dateOfBirth = dateOfBirthUnsafeSupplier.get();
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
	protected Date dateOfBirth;

	@Schema
	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	@JsonIgnore
	public void setDistrict(
		UnsafeSupplier<String, Exception> districtUnsafeSupplier) {

		try {
			district = districtUnsafeSupplier.get();
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
	protected String district;

	@Schema
	public Date getDocumentPushDateTime() {
		return documentPushDateTime;
	}

	public void setDocumentPushDateTime(Date documentPushDateTime) {
		this.documentPushDateTime = documentPushDateTime;
	}

	@JsonIgnore
	public void setDocumentPushDateTime(
		UnsafeSupplier<Date, Exception> documentPushDateTimeUnsafeSupplier) {

		try {
			documentPushDateTime = documentPushDateTimeUnsafeSupplier.get();
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
	@NotNull
	protected Date documentPushDateTime;

	@Schema
	public String getDocumentReferenceNo() {
		return documentReferenceNo;
	}

	public void setDocumentReferenceNo(String documentReferenceNo) {
		this.documentReferenceNo = documentReferenceNo;
	}

	@JsonIgnore
	public void setDocumentReferenceNo(
		UnsafeSupplier<String, Exception> documentReferenceNoUnsafeSupplier) {

		try {
			documentReferenceNo = documentReferenceNoUnsafeSupplier.get();
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
	protected String documentReferenceNo;

	@Schema
	@Valid
	public DocumentSentStatus getDocumentSentStatus() {
		return documentSentStatus;
	}

	@JsonIgnore
	public String getDocumentSentStatusAsString() {
		if (documentSentStatus == null) {
			return null;
		}

		return documentSentStatus.toString();
	}

	public void setDocumentSentStatus(DocumentSentStatus documentSentStatus) {
		this.documentSentStatus = documentSentStatus;
	}

	@JsonIgnore
	public void setDocumentSentStatus(
		UnsafeSupplier<DocumentSentStatus, Exception>
			documentSentStatusUnsafeSupplier) {

		try {
			documentSentStatus = documentSentStatusUnsafeSupplier.get();
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
	@NotNull
	protected DocumentSentStatus documentSentStatus;

	@Schema
	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	@JsonIgnore
	public void setEmailId(
		UnsafeSupplier<String, Exception> emailIdUnsafeSupplier) {

		try {
			emailId = emailIdUnsafeSupplier.get();
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
	protected String emailId;

	@Schema
	public String getFatherHusbandName() {
		return fatherHusbandName;
	}

	public void setFatherHusbandName(String fatherHusbandName) {
		this.fatherHusbandName = fatherHusbandName;
	}

	@JsonIgnore
	public void setFatherHusbandName(
		UnsafeSupplier<String, Exception> fatherHusbandNameUnsafeSupplier) {

		try {
			fatherHusbandName = fatherHusbandNameUnsafeSupplier.get();
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
	protected String fatherHusbandName;

	@Schema
	public String getGuardianAddress() {
		return guardianAddress;
	}

	public void setGuardianAddress(String guardianAddress) {
		this.guardianAddress = guardianAddress;
	}

	@JsonIgnore
	public void setGuardianAddress(
		UnsafeSupplier<String, Exception> guardianAddressUnsafeSupplier) {

		try {
			guardianAddress = guardianAddressUnsafeSupplier.get();
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
	protected String guardianAddress;

	@Schema
	public String getGuardianEmail() {
		return guardianEmail;
	}

	public void setGuardianEmail(String guardianEmail) {
		this.guardianEmail = guardianEmail;
	}

	@JsonIgnore
	public void setGuardianEmail(
		UnsafeSupplier<String, Exception> guardianEmailUnsafeSupplier) {

		try {
			guardianEmail = guardianEmailUnsafeSupplier.get();
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
	protected String guardianEmail;

	@Schema
	public String getGuardianMobile() {
		return guardianMobile;
	}

	public void setGuardianMobile(String guardianMobile) {
		this.guardianMobile = guardianMobile;
	}

	@JsonIgnore
	public void setGuardianMobile(
		UnsafeSupplier<String, Exception> guardianMobileUnsafeSupplier) {

		try {
			guardianMobile = guardianMobileUnsafeSupplier.get();
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
	protected String guardianMobile;

	@Schema
	public String getGuardianName() {
		return guardianName;
	}

	public void setGuardianName(String guardianName) {
		this.guardianName = guardianName;
	}

	@JsonIgnore
	public void setGuardianName(
		UnsafeSupplier<String, Exception> guardianNameUnsafeSupplier) {

		try {
			guardianName = guardianNameUnsafeSupplier.get();
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
	protected String guardianName;

	@Schema
	public String getGuardianRelation() {
		return guardianRelation;
	}

	public void setGuardianRelation(String guardianRelation) {
		this.guardianRelation = guardianRelation;
	}

	@JsonIgnore
	public void setGuardianRelation(
		UnsafeSupplier<String, Exception> guardianRelationUnsafeSupplier) {

		try {
			guardianRelation = guardianRelationUnsafeSupplier.get();
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
	protected String guardianRelation;

	@Schema
	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	@JsonIgnore
	public void setIfscCode(
		UnsafeSupplier<String, Exception> ifscCodeUnsafeSupplier) {

		try {
			ifscCode = ifscCodeUnsafeSupplier.get();
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
	protected String ifscCode;

	@Schema
	@Valid
	public InsuranceType getInsuranceType() {
		return insuranceType;
	}

	@JsonIgnore
	public String getInsuranceTypeAsString() {
		if (insuranceType == null) {
			return null;
		}

		return insuranceType.toString();
	}

	public void setInsuranceType(InsuranceType insuranceType) {
		this.insuranceType = insuranceType;
	}

	@JsonIgnore
	public void setInsuranceType(
		UnsafeSupplier<InsuranceType, Exception> insuranceTypeUnsafeSupplier) {

		try {
			insuranceType = insuranceTypeUnsafeSupplier.get();
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
	@NotNull
	protected InsuranceType insuranceType;

	@Schema
	public String getKycDocumentName() {
		return kycDocumentName;
	}

	public void setKycDocumentName(String kycDocumentName) {
		this.kycDocumentName = kycDocumentName;
	}

	@JsonIgnore
	public void setKycDocumentName(
		UnsafeSupplier<String, Exception> kycDocumentNameUnsafeSupplier) {

		try {
			kycDocumentName = kycDocumentNameUnsafeSupplier.get();
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
	protected String kycDocumentName;

	@Schema
	public String getKycIdNumber() {
		return kycIdNumber;
	}

	public void setKycIdNumber(String kycIdNumber) {
		this.kycIdNumber = kycIdNumber;
	}

	@JsonIgnore
	public void setKycIdNumber(
		UnsafeSupplier<String, Exception> kycIdNumberUnsafeSupplier) {

		try {
			kycIdNumber = kycIdNumberUnsafeSupplier.get();
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
	protected String kycIdNumber;

	@Schema
	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	@JsonIgnore
	public void setMobileNumber(
		UnsafeSupplier<String, Exception> mobileNumberUnsafeSupplier) {

		try {
			mobileNumber = mobileNumberUnsafeSupplier.get();
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
	protected String mobileNumber;

	@Schema
	public String getNomineeAddress() {
		return nomineeAddress;
	}

	public void setNomineeAddress(String nomineeAddress) {
		this.nomineeAddress = nomineeAddress;
	}

	@JsonIgnore
	public void setNomineeAddress(
		UnsafeSupplier<String, Exception> nomineeAddressUnsafeSupplier) {

		try {
			nomineeAddress = nomineeAddressUnsafeSupplier.get();
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
	protected String nomineeAddress;

	@Schema
	public Date getNomineeDob() {
		return nomineeDob;
	}

	public void setNomineeDob(Date nomineeDob) {
		this.nomineeDob = nomineeDob;
	}

	@JsonIgnore
	public void setNomineeDob(
		UnsafeSupplier<Date, Exception> nomineeDobUnsafeSupplier) {

		try {
			nomineeDob = nomineeDobUnsafeSupplier.get();
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
	protected Date nomineeDob;

	@Schema
	public String getNomineeName() {
		return nomineeName;
	}

	public void setNomineeName(String nomineeName) {
		this.nomineeName = nomineeName;
	}

	@JsonIgnore
	public void setNomineeName(
		UnsafeSupplier<String, Exception> nomineeNameUnsafeSupplier) {

		try {
			nomineeName = nomineeNameUnsafeSupplier.get();
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
	protected String nomineeName;

	@Schema
	public String getNomineeRelation() {
		return nomineeRelation;
	}

	public void setNomineeRelation(String nomineeRelation) {
		this.nomineeRelation = nomineeRelation;
	}

	@JsonIgnore
	public void setNomineeRelation(
		UnsafeSupplier<String, Exception> nomineeRelationUnsafeSupplier) {

		try {
			nomineeRelation = nomineeRelationUnsafeSupplier.get();
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
	protected String nomineeRelation;

	@Schema
	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	@JsonIgnore
	public void setOccupation(
		UnsafeSupplier<String, Exception> occupationUnsafeSupplier) {

		try {
			occupation = occupationUnsafeSupplier.get();
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
	protected String occupation;

	@Schema
	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	@JsonIgnore
	public void setPanNumber(
		UnsafeSupplier<String, Exception> panNumberUnsafeSupplier) {

		try {
			panNumber = panNumberUnsafeSupplier.get();
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
	protected String panNumber;

	@Schema
	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	@JsonIgnore
	public void setPinCode(
		UnsafeSupplier<String, Exception> pinCodeUnsafeSupplier) {

		try {
			pinCode = pinCodeUnsafeSupplier.get();
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
	protected String pinCode;

	@Schema
	@Valid
	public PolicyIssuanceStatus getPolicyIssuanceStatus() {
		return policyIssuanceStatus;
	}

	@JsonIgnore
	public String getPolicyIssuanceStatusAsString() {
		if (policyIssuanceStatus == null) {
			return null;
		}

		return policyIssuanceStatus.toString();
	}

	public void setPolicyIssuanceStatus(
		PolicyIssuanceStatus policyIssuanceStatus) {

		this.policyIssuanceStatus = policyIssuanceStatus;
	}

	@JsonIgnore
	public void setPolicyIssuanceStatus(
		UnsafeSupplier<PolicyIssuanceStatus, Exception>
			policyIssuanceStatusUnsafeSupplier) {

		try {
			policyIssuanceStatus = policyIssuanceStatusUnsafeSupplier.get();
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
	@NotNull
	protected PolicyIssuanceStatus policyIssuanceStatus;

	@Schema
	public String getPolicyYear() {
		return policyYear;
	}

	public void setPolicyYear(String policyYear) {
		this.policyYear = policyYear;
	}

	@JsonIgnore
	public void setPolicyYear(
		UnsafeSupplier<String, Exception> policyYearUnsafeSupplier) {

		try {
			policyYear = policyYearUnsafeSupplier.get();
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
	protected String policyYear;

	@Schema
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@JsonIgnore
	public void setState(
		UnsafeSupplier<String, Exception> stateUnsafeSupplier) {

		try {
			state = stateUnsafeSupplier.get();
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
	protected String state;

	@Schema
	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	@JsonIgnore
	public void setTransactionDate(
		UnsafeSupplier<Date, Exception> transactionDateUnsafeSupplier) {

		try {
			transactionDate = transactionDateUnsafeSupplier.get();
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
	@NotNull
	protected Date transactionDate;

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

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ConsentMetadata)) {
			return false;
		}

		ConsentMetadata consentMetadata = (ConsentMetadata)object;

		return Objects.equals(toString(), consentMetadata.toString());
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

		if (aadhaarNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"aadhaarNumber\": ");

			sb.append("\"");

			sb.append(_escape(aadhaarNumber));

			sb.append("\"");
		}

		if (accountHolderName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountHolderName\": ");

			sb.append("\"");

			sb.append(_escape(accountHolderName));

			sb.append("\"");
		}

		if (address != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"address\": ");

			sb.append("\"");

			sb.append(_escape(address));

			sb.append("\"");
		}

		if (bankAccountNo != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"bankAccountNo\": ");

			sb.append("\"");

			sb.append(_escape(bankAccountNo));

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

		if (branchName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchName\": ");

			sb.append("\"");

			sb.append(_escape(branchName));

			sb.append("\"");
		}

		if (city != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"city\": ");

			sb.append("\"");

			sb.append(_escape(city));

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

		if (dateOfBirth != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateOfBirth\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(dateOfBirth));

			sb.append("\"");
		}

		if (district != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"district\": ");

			sb.append("\"");

			sb.append(_escape(district));

			sb.append("\"");
		}

		if (documentPushDateTime != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentPushDateTime\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(documentPushDateTime));

			sb.append("\"");
		}

		if (documentReferenceNo != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(documentReferenceNo));

			sb.append("\"");
		}

		if (documentSentStatus != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentSentStatus\": ");

			sb.append("\"");

			sb.append(documentSentStatus);

			sb.append("\"");
		}

		if (emailId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"emailId\": ");

			sb.append("\"");

			sb.append(_escape(emailId));

			sb.append("\"");
		}

		if (fatherHusbandName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fatherHusbandName\": ");

			sb.append("\"");

			sb.append(_escape(fatherHusbandName));

			sb.append("\"");
		}

		if (guardianAddress != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianAddress\": ");

			sb.append("\"");

			sb.append(_escape(guardianAddress));

			sb.append("\"");
		}

		if (guardianEmail != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianEmail\": ");

			sb.append("\"");

			sb.append(_escape(guardianEmail));

			sb.append("\"");
		}

		if (guardianMobile != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianMobile\": ");

			sb.append("\"");

			sb.append(_escape(guardianMobile));

			sb.append("\"");
		}

		if (guardianName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianName\": ");

			sb.append("\"");

			sb.append(_escape(guardianName));

			sb.append("\"");
		}

		if (guardianRelation != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianRelation\": ");

			sb.append("\"");

			sb.append(_escape(guardianRelation));

			sb.append("\"");
		}

		if (ifscCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"ifscCode\": ");

			sb.append("\"");

			sb.append(_escape(ifscCode));

			sb.append("\"");
		}

		if (insuranceType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"insuranceType\": ");

			sb.append("\"");

			sb.append(insuranceType);

			sb.append("\"");
		}

		if (kycDocumentName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"kycDocumentName\": ");

			sb.append("\"");

			sb.append(_escape(kycDocumentName));

			sb.append("\"");
		}

		if (kycIdNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"kycIdNumber\": ");

			sb.append("\"");

			sb.append(_escape(kycIdNumber));

			sb.append("\"");
		}

		if (mobileNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"mobileNumber\": ");

			sb.append("\"");

			sb.append(_escape(mobileNumber));

			sb.append("\"");
		}

		if (nomineeAddress != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeAddress\": ");

			sb.append("\"");

			sb.append(_escape(nomineeAddress));

			sb.append("\"");
		}

		if (nomineeDob != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeDob\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(nomineeDob));

			sb.append("\"");
		}

		if (nomineeName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeName\": ");

			sb.append("\"");

			sb.append(_escape(nomineeName));

			sb.append("\"");
		}

		if (nomineeRelation != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeRelation\": ");

			sb.append("\"");

			sb.append(_escape(nomineeRelation));

			sb.append("\"");
		}

		if (occupation != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"occupation\": ");

			sb.append("\"");

			sb.append(_escape(occupation));

			sb.append("\"");
		}

		if (panNumber != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"panNumber\": ");

			sb.append("\"");

			sb.append(_escape(panNumber));

			sb.append("\"");
		}

		if (pinCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"pinCode\": ");

			sb.append("\"");

			sb.append(_escape(pinCode));

			sb.append("\"");
		}

		if (policyIssuanceStatus != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"policyIssuanceStatus\": ");

			sb.append("\"");

			sb.append(policyIssuanceStatus);

			sb.append("\"");
		}

		if (policyYear != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"policyYear\": ");

			sb.append("\"");

			sb.append(_escape(policyYear));

			sb.append("\"");
		}

		if (state != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"state\": ");

			sb.append("\"");

			sb.append(_escape(state));

			sb.append("\"");
		}

		if (transactionDate != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transactionDate\": ");

			sb.append("\"");

			sb.append(liferayToJSONDateFormat.format(transactionDate));

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
		defaultValue = "com.boi.headless.rest.consent.dto.v1_0.ConsentMetadata",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("DocumentSentStatus")
	public static enum DocumentSentStatus {

		SUCCESS("success"), FAILED("failed");

		@JsonCreator
		public static DocumentSentStatus create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (DocumentSentStatus documentSentStatus : values()) {
				if (Objects.equals(documentSentStatus.getValue(), value)) {
					return documentSentStatus;
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

		private DocumentSentStatus(String value) {
			_value = value;
		}

		private final String _value;

	}

	@GraphQLName("InsuranceType")
	public static enum InsuranceType {

		PMJJBY("PMJJBY"), PMSBY("PMSBY");

		@JsonCreator
		public static InsuranceType create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (InsuranceType insuranceType : values()) {
				if (Objects.equals(insuranceType.getValue(), value)) {
					return insuranceType;
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

		private InsuranceType(String value) {
			_value = value;
		}

		private final String _value;

	}

	@GraphQLName("PolicyIssuanceStatus")
	public static enum PolicyIssuanceStatus {

		SUCCESS("success"), FAILED("failed");

		@JsonCreator
		public static PolicyIssuanceStatus create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (PolicyIssuanceStatus policyIssuanceStatus : values()) {
				if (Objects.equals(policyIssuanceStatus.getValue(), value)) {
					return policyIssuanceStatus;
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

		private PolicyIssuanceStatus(String value) {
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