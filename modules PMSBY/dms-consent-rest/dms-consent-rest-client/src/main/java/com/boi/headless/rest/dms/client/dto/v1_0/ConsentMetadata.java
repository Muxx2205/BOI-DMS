package com.boi.headless.rest.dms.client.dto.v1_0;

import com.boi.headless.rest.dms.client.function.UnsafeSupplier;
import com.boi.headless.rest.dms.client.serdes.v1_0.ConsentMetadataSerDes;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class ConsentMetadata implements Cloneable, Serializable {

	public static ConsentMetadata toDTO(String json) {
		return ConsentMetadataSerDes.toDTO(json);
	}

	public String getAadhaarNumber() {
		return aadhaarNumber;
	}

	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}

	public void setAadhaarNumber(
		UnsafeSupplier<String, Exception> aadhaarNumberUnsafeSupplier) {

		try {
			aadhaarNumber = aadhaarNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String aadhaarNumber;

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public void setAccountHolderName(
		UnsafeSupplier<String, Exception> accountHolderNameUnsafeSupplier) {

		try {
			accountHolderName = accountHolderNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String accountHolderName;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAddress(
		UnsafeSupplier<String, Exception> addressUnsafeSupplier) {

		try {
			address = addressUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String address;

	public String getBankAccountNo() {
		return bankAccountNo;
	}

	public void setBankAccountNo(String bankAccountNo) {
		this.bankAccountNo = bankAccountNo;
	}

	public void setBankAccountNo(
		UnsafeSupplier<String, Exception> bankAccountNoUnsafeSupplier) {

		try {
			bankAccountNo = bankAccountNoUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String bankAccountNo;

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

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public void setBranchName(
		UnsafeSupplier<String, Exception> branchNameUnsafeSupplier) {

		try {
			branchName = branchNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String branchName;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCity(UnsafeSupplier<String, Exception> cityUnsafeSupplier) {
		try {
			city = cityUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String city;

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

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setDateOfBirth(
		UnsafeSupplier<Date, Exception> dateOfBirthUnsafeSupplier) {

		try {
			dateOfBirth = dateOfBirthUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date dateOfBirth;

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setDistrict(
		UnsafeSupplier<String, Exception> districtUnsafeSupplier) {

		try {
			district = districtUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String district;

	public Date getDocumentPushDateTime() {
		return documentPushDateTime;
	}

	public void setDocumentPushDateTime(Date documentPushDateTime) {
		this.documentPushDateTime = documentPushDateTime;
	}

	public void setDocumentPushDateTime(
		UnsafeSupplier<Date, Exception> documentPushDateTimeUnsafeSupplier) {

		try {
			documentPushDateTime = documentPushDateTimeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date documentPushDateTime;

	public String getDocumentReferenceNo() {
		return documentReferenceNo;
	}

	public void setDocumentReferenceNo(String documentReferenceNo) {
		this.documentReferenceNo = documentReferenceNo;
	}

	public void setDocumentReferenceNo(
		UnsafeSupplier<String, Exception> documentReferenceNoUnsafeSupplier) {

		try {
			documentReferenceNo = documentReferenceNoUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String documentReferenceNo;

	public DocumentSentStatus getDocumentSentStatus() {
		return documentSentStatus;
	}

	public String getDocumentSentStatusAsString() {
		if (documentSentStatus == null) {
			return null;
		}

		return documentSentStatus.toString();
	}

	public void setDocumentSentStatus(DocumentSentStatus documentSentStatus) {
		this.documentSentStatus = documentSentStatus;
	}

	public void setDocumentSentStatus(
		UnsafeSupplier<DocumentSentStatus, Exception>
			documentSentStatusUnsafeSupplier) {

		try {
			documentSentStatus = documentSentStatusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected DocumentSentStatus documentSentStatus;

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public void setEmailId(
		UnsafeSupplier<String, Exception> emailIdUnsafeSupplier) {

		try {
			emailId = emailIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String emailId;

	public String getFatherHusbandName() {
		return fatherHusbandName;
	}

	public void setFatherHusbandName(String fatherHusbandName) {
		this.fatherHusbandName = fatherHusbandName;
	}

	public void setFatherHusbandName(
		UnsafeSupplier<String, Exception> fatherHusbandNameUnsafeSupplier) {

		try {
			fatherHusbandName = fatherHusbandNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String fatherHusbandName;

	public String getGuardianAddress() {
		return guardianAddress;
	}

	public void setGuardianAddress(String guardianAddress) {
		this.guardianAddress = guardianAddress;
	}

	public void setGuardianAddress(
		UnsafeSupplier<String, Exception> guardianAddressUnsafeSupplier) {

		try {
			guardianAddress = guardianAddressUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String guardianAddress;

	public String getGuardianEmail() {
		return guardianEmail;
	}

	public void setGuardianEmail(String guardianEmail) {
		this.guardianEmail = guardianEmail;
	}

	public void setGuardianEmail(
		UnsafeSupplier<String, Exception> guardianEmailUnsafeSupplier) {

		try {
			guardianEmail = guardianEmailUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String guardianEmail;

	public String getGuardianMobile() {
		return guardianMobile;
	}

	public void setGuardianMobile(String guardianMobile) {
		this.guardianMobile = guardianMobile;
	}

	public void setGuardianMobile(
		UnsafeSupplier<String, Exception> guardianMobileUnsafeSupplier) {

		try {
			guardianMobile = guardianMobileUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String guardianMobile;

	public String getGuardianName() {
		return guardianName;
	}

	public void setGuardianName(String guardianName) {
		this.guardianName = guardianName;
	}

	public void setGuardianName(
		UnsafeSupplier<String, Exception> guardianNameUnsafeSupplier) {

		try {
			guardianName = guardianNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String guardianName;

	public String getGuardianRelation() {
		return guardianRelation;
	}

	public void setGuardianRelation(String guardianRelation) {
		this.guardianRelation = guardianRelation;
	}

	public void setGuardianRelation(
		UnsafeSupplier<String, Exception> guardianRelationUnsafeSupplier) {

		try {
			guardianRelation = guardianRelationUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String guardianRelation;

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public void setIfscCode(
		UnsafeSupplier<String, Exception> ifscCodeUnsafeSupplier) {

		try {
			ifscCode = ifscCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String ifscCode;

	public InsuranceType getInsuranceType() {
		return insuranceType;
	}

	public String getInsuranceTypeAsString() {
		if (insuranceType == null) {
			return null;
		}

		return insuranceType.toString();
	}

	public void setInsuranceType(InsuranceType insuranceType) {
		this.insuranceType = insuranceType;
	}

	public void setInsuranceType(
		UnsafeSupplier<InsuranceType, Exception> insuranceTypeUnsafeSupplier) {

		try {
			insuranceType = insuranceTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected InsuranceType insuranceType;

	public String getKycDocumentName() {
		return kycDocumentName;
	}

	public void setKycDocumentName(String kycDocumentName) {
		this.kycDocumentName = kycDocumentName;
	}

	public void setKycDocumentName(
		UnsafeSupplier<String, Exception> kycDocumentNameUnsafeSupplier) {

		try {
			kycDocumentName = kycDocumentNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String kycDocumentName;

	public String getKycIdNumber() {
		return kycIdNumber;
	}

	public void setKycIdNumber(String kycIdNumber) {
		this.kycIdNumber = kycIdNumber;
	}

	public void setKycIdNumber(
		UnsafeSupplier<String, Exception> kycIdNumberUnsafeSupplier) {

		try {
			kycIdNumber = kycIdNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String kycIdNumber;

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setMobileNumber(
		UnsafeSupplier<String, Exception> mobileNumberUnsafeSupplier) {

		try {
			mobileNumber = mobileNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String mobileNumber;

	public String getNomineeAddress() {
		return nomineeAddress;
	}

	public void setNomineeAddress(String nomineeAddress) {
		this.nomineeAddress = nomineeAddress;
	}

	public void setNomineeAddress(
		UnsafeSupplier<String, Exception> nomineeAddressUnsafeSupplier) {

		try {
			nomineeAddress = nomineeAddressUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nomineeAddress;

	public Date getNomineeDob() {
		return nomineeDob;
	}

	public void setNomineeDob(Date nomineeDob) {
		this.nomineeDob = nomineeDob;
	}

	public void setNomineeDob(
		UnsafeSupplier<Date, Exception> nomineeDobUnsafeSupplier) {

		try {
			nomineeDob = nomineeDobUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date nomineeDob;

	public String getNomineeName() {
		return nomineeName;
	}

	public void setNomineeName(String nomineeName) {
		this.nomineeName = nomineeName;
	}

	public void setNomineeName(
		UnsafeSupplier<String, Exception> nomineeNameUnsafeSupplier) {

		try {
			nomineeName = nomineeNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nomineeName;

	public String getNomineeRelation() {
		return nomineeRelation;
	}

	public void setNomineeRelation(String nomineeRelation) {
		this.nomineeRelation = nomineeRelation;
	}

	public void setNomineeRelation(
		UnsafeSupplier<String, Exception> nomineeRelationUnsafeSupplier) {

		try {
			nomineeRelation = nomineeRelationUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nomineeRelation;

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public void setOccupation(
		UnsafeSupplier<String, Exception> occupationUnsafeSupplier) {

		try {
			occupation = occupationUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String occupation;

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	public void setPanNumber(
		UnsafeSupplier<String, Exception> panNumberUnsafeSupplier) {

		try {
			panNumber = panNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String panNumber;

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public void setPinCode(
		UnsafeSupplier<String, Exception> pinCodeUnsafeSupplier) {

		try {
			pinCode = pinCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String pinCode;

	public PolicyIssuanceStatus getPolicyIssuanceStatus() {
		return policyIssuanceStatus;
	}

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

	public void setPolicyIssuanceStatus(
		UnsafeSupplier<PolicyIssuanceStatus, Exception>
			policyIssuanceStatusUnsafeSupplier) {

		try {
			policyIssuanceStatus = policyIssuanceStatusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected PolicyIssuanceStatus policyIssuanceStatus;

	public String getPolicyYear() {
		return policyYear;
	}

	public void setPolicyYear(String policyYear) {
		this.policyYear = policyYear;
	}

	public void setPolicyYear(
		UnsafeSupplier<String, Exception> policyYearUnsafeSupplier) {

		try {
			policyYear = policyYearUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String policyYear;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setState(
		UnsafeSupplier<String, Exception> stateUnsafeSupplier) {

		try {
			state = stateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String state;

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public void setTransactionDate(
		UnsafeSupplier<Date, Exception> transactionDateUnsafeSupplier) {

		try {
			transactionDate = transactionDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date transactionDate;

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

	@Override
	public ConsentMetadata clone() throws CloneNotSupportedException {
		return (ConsentMetadata)super.clone();
	}

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
		return ConsentMetadataSerDes.toJSON(this);
	}

	public static enum DocumentSentStatus {

		SUCCESS("success"), FAILED("failed");

		public static DocumentSentStatus create(String value) {
			for (DocumentSentStatus documentSentStatus : values()) {
				if (Objects.equals(documentSentStatus.getValue(), value) ||
					Objects.equals(documentSentStatus.name(), value)) {

					return documentSentStatus;
				}
			}

			return null;
		}

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

	public static enum InsuranceType {

		PMJJBY("PMJJBY"), PMSBY("PMSBY");

		public static InsuranceType create(String value) {
			for (InsuranceType insuranceType : values()) {
				if (Objects.equals(insuranceType.getValue(), value) ||
					Objects.equals(insuranceType.name(), value)) {

					return insuranceType;
				}
			}

			return null;
		}

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

	public static enum PolicyIssuanceStatus {

		SUCCESS("success"), FAILED("failed");

		public static PolicyIssuanceStatus create(String value) {
			for (PolicyIssuanceStatus policyIssuanceStatus : values()) {
				if (Objects.equals(policyIssuanceStatus.getValue(), value) ||
					Objects.equals(policyIssuanceStatus.name(), value)) {

					return policyIssuanceStatus;
				}
			}

			return null;
		}

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

}