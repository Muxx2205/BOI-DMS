package com.boi.headless.rest.dms.client.serdes.v1_0;

import com.boi.headless.rest.dms.client.dto.v1_0.ConsentMetadata;
import com.boi.headless.rest.dms.client.json.BaseJSONParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class ConsentMetadataSerDes {

	public static ConsentMetadata toDTO(String json) {
		ConsentMetadataJSONParser consentMetadataJSONParser =
			new ConsentMetadataJSONParser();

		return consentMetadataJSONParser.parseToDTO(json);
	}

	public static ConsentMetadata[] toDTOs(String json) {
		ConsentMetadataJSONParser consentMetadataJSONParser =
			new ConsentMetadataJSONParser();

		return consentMetadataJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ConsentMetadata consentMetadata) {
		if (consentMetadata == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (consentMetadata.getAadhaarNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"aadhaarNumber\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getAadhaarNumber()));

			sb.append("\"");
		}

		if (consentMetadata.getAccountHolderName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountHolderName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getAccountHolderName()));

			sb.append("\"");
		}

		if (consentMetadata.getAddress() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"address\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getAddress()));

			sb.append("\"");
		}

		if (consentMetadata.getBankAccountNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"bankAccountNo\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getBankAccountNo()));

			sb.append("\"");
		}

		if (consentMetadata.getBranchCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getBranchCode()));

			sb.append("\"");
		}

		if (consentMetadata.getBranchName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getBranchName()));

			sb.append("\"");
		}

		if (consentMetadata.getCity() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"city\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getCity()));

			sb.append("\"");
		}

		if (consentMetadata.getCustomerId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerId\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getCustomerId()));

			sb.append("\"");
		}

		if (consentMetadata.getDateOfBirth() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"dateOfBirth\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					consentMetadata.getDateOfBirth()));

			sb.append("\"");
		}

		if (consentMetadata.getDistrict() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"district\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getDistrict()));

			sb.append("\"");
		}

		if (consentMetadata.getDocumentPushDateTime() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentPushDateTime\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					consentMetadata.getDocumentPushDateTime()));

			sb.append("\"");
		}

		if (consentMetadata.getDocumentReferenceNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getDocumentReferenceNo()));

			sb.append("\"");
		}

		if (consentMetadata.getDocumentSentStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentSentStatus\": ");

			sb.append("\"");

			sb.append(consentMetadata.getDocumentSentStatus());

			sb.append("\"");
		}

		if (consentMetadata.getEmailId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"emailId\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getEmailId()));

			sb.append("\"");
		}

		if (consentMetadata.getFatherHusbandName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fatherHusbandName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getFatherHusbandName()));

			sb.append("\"");
		}

		if (consentMetadata.getGuardianAddress() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianAddress\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getGuardianAddress()));

			sb.append("\"");
		}

		if (consentMetadata.getGuardianEmail() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianEmail\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getGuardianEmail()));

			sb.append("\"");
		}

		if (consentMetadata.getGuardianMobile() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianMobile\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getGuardianMobile()));

			sb.append("\"");
		}

		if (consentMetadata.getGuardianName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getGuardianName()));

			sb.append("\"");
		}

		if (consentMetadata.getGuardianRelation() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"guardianRelation\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getGuardianRelation()));

			sb.append("\"");
		}

		if (consentMetadata.getIfscCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"ifscCode\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getIfscCode()));

			sb.append("\"");
		}

		if (consentMetadata.getInsuranceType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"insuranceType\": ");

			sb.append("\"");

			sb.append(consentMetadata.getInsuranceType());

			sb.append("\"");
		}

		if (consentMetadata.getKycDocumentName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"kycDocumentName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getKycDocumentName()));

			sb.append("\"");
		}

		if (consentMetadata.getKycIdNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"kycIdNumber\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getKycIdNumber()));

			sb.append("\"");
		}

		if (consentMetadata.getMobileNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"mobileNumber\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getMobileNumber()));

			sb.append("\"");
		}

		if (consentMetadata.getNomineeAddress() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeAddress\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getNomineeAddress()));

			sb.append("\"");
		}

		if (consentMetadata.getNomineeDob() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeDob\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					consentMetadata.getNomineeDob()));

			sb.append("\"");
		}

		if (consentMetadata.getNomineeName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeName\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getNomineeName()));

			sb.append("\"");
		}

		if (consentMetadata.getNomineeRelation() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nomineeRelation\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getNomineeRelation()));

			sb.append("\"");
		}

		if (consentMetadata.getOccupation() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"occupation\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getOccupation()));

			sb.append("\"");
		}

		if (consentMetadata.getPanNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"panNumber\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getPanNumber()));

			sb.append("\"");
		}

		if (consentMetadata.getPinCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"pinCode\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getPinCode()));

			sb.append("\"");
		}

		if (consentMetadata.getPolicyIssuanceStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"policyIssuanceStatus\": ");

			sb.append("\"");

			sb.append(consentMetadata.getPolicyIssuanceStatus());

			sb.append("\"");
		}

		if (consentMetadata.getPolicyYear() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"policyYear\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getPolicyYear()));

			sb.append("\"");
		}

		if (consentMetadata.getState() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"state\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getState()));

			sb.append("\"");
		}

		if (consentMetadata.getTransactionDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transactionDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					consentMetadata.getTransactionDate()));

			sb.append("\"");
		}

		if (consentMetadata.getUrn() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(consentMetadata.getUrn()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ConsentMetadataJSONParser consentMetadataJSONParser =
			new ConsentMetadataJSONParser();

		return consentMetadataJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ConsentMetadata consentMetadata) {
		if (consentMetadata == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (consentMetadata.getAadhaarNumber() == null) {
			map.put("aadhaarNumber", null);
		}
		else {
			map.put(
				"aadhaarNumber",
				String.valueOf(consentMetadata.getAadhaarNumber()));
		}

		if (consentMetadata.getAccountHolderName() == null) {
			map.put("accountHolderName", null);
		}
		else {
			map.put(
				"accountHolderName",
				String.valueOf(consentMetadata.getAccountHolderName()));
		}

		if (consentMetadata.getAddress() == null) {
			map.put("address", null);
		}
		else {
			map.put("address", String.valueOf(consentMetadata.getAddress()));
		}

		if (consentMetadata.getBankAccountNo() == null) {
			map.put("bankAccountNo", null);
		}
		else {
			map.put(
				"bankAccountNo",
				String.valueOf(consentMetadata.getBankAccountNo()));
		}

		if (consentMetadata.getBranchCode() == null) {
			map.put("branchCode", null);
		}
		else {
			map.put(
				"branchCode", String.valueOf(consentMetadata.getBranchCode()));
		}

		if (consentMetadata.getBranchName() == null) {
			map.put("branchName", null);
		}
		else {
			map.put(
				"branchName", String.valueOf(consentMetadata.getBranchName()));
		}

		if (consentMetadata.getCity() == null) {
			map.put("city", null);
		}
		else {
			map.put("city", String.valueOf(consentMetadata.getCity()));
		}

		if (consentMetadata.getCustomerId() == null) {
			map.put("customerId", null);
		}
		else {
			map.put(
				"customerId", String.valueOf(consentMetadata.getCustomerId()));
		}

		if (consentMetadata.getDateOfBirth() == null) {
			map.put("dateOfBirth", null);
		}
		else {
			map.put(
				"dateOfBirth",
				liferayToJSONDateFormat.format(
					consentMetadata.getDateOfBirth()));
		}

		if (consentMetadata.getDistrict() == null) {
			map.put("district", null);
		}
		else {
			map.put("district", String.valueOf(consentMetadata.getDistrict()));
		}

		if (consentMetadata.getDocumentPushDateTime() == null) {
			map.put("documentPushDateTime", null);
		}
		else {
			map.put(
				"documentPushDateTime",
				liferayToJSONDateFormat.format(
					consentMetadata.getDocumentPushDateTime()));
		}

		if (consentMetadata.getDocumentReferenceNo() == null) {
			map.put("documentReferenceNo", null);
		}
		else {
			map.put(
				"documentReferenceNo",
				String.valueOf(consentMetadata.getDocumentReferenceNo()));
		}

		if (consentMetadata.getDocumentSentStatus() == null) {
			map.put("documentSentStatus", null);
		}
		else {
			map.put(
				"documentSentStatus",
				String.valueOf(consentMetadata.getDocumentSentStatus()));
		}

		if (consentMetadata.getEmailId() == null) {
			map.put("emailId", null);
		}
		else {
			map.put("emailId", String.valueOf(consentMetadata.getEmailId()));
		}

		if (consentMetadata.getFatherHusbandName() == null) {
			map.put("fatherHusbandName", null);
		}
		else {
			map.put(
				"fatherHusbandName",
				String.valueOf(consentMetadata.getFatherHusbandName()));
		}

		if (consentMetadata.getGuardianAddress() == null) {
			map.put("guardianAddress", null);
		}
		else {
			map.put(
				"guardianAddress",
				String.valueOf(consentMetadata.getGuardianAddress()));
		}

		if (consentMetadata.getGuardianEmail() == null) {
			map.put("guardianEmail", null);
		}
		else {
			map.put(
				"guardianEmail",
				String.valueOf(consentMetadata.getGuardianEmail()));
		}

		if (consentMetadata.getGuardianMobile() == null) {
			map.put("guardianMobile", null);
		}
		else {
			map.put(
				"guardianMobile",
				String.valueOf(consentMetadata.getGuardianMobile()));
		}

		if (consentMetadata.getGuardianName() == null) {
			map.put("guardianName", null);
		}
		else {
			map.put(
				"guardianName",
				String.valueOf(consentMetadata.getGuardianName()));
		}

		if (consentMetadata.getGuardianRelation() == null) {
			map.put("guardianRelation", null);
		}
		else {
			map.put(
				"guardianRelation",
				String.valueOf(consentMetadata.getGuardianRelation()));
		}

		if (consentMetadata.getIfscCode() == null) {
			map.put("ifscCode", null);
		}
		else {
			map.put("ifscCode", String.valueOf(consentMetadata.getIfscCode()));
		}

		if (consentMetadata.getInsuranceType() == null) {
			map.put("insuranceType", null);
		}
		else {
			map.put(
				"insuranceType",
				String.valueOf(consentMetadata.getInsuranceType()));
		}

		if (consentMetadata.getKycDocumentName() == null) {
			map.put("kycDocumentName", null);
		}
		else {
			map.put(
				"kycDocumentName",
				String.valueOf(consentMetadata.getKycDocumentName()));
		}

		if (consentMetadata.getKycIdNumber() == null) {
			map.put("kycIdNumber", null);
		}
		else {
			map.put(
				"kycIdNumber",
				String.valueOf(consentMetadata.getKycIdNumber()));
		}

		if (consentMetadata.getMobileNumber() == null) {
			map.put("mobileNumber", null);
		}
		else {
			map.put(
				"mobileNumber",
				String.valueOf(consentMetadata.getMobileNumber()));
		}

		if (consentMetadata.getNomineeAddress() == null) {
			map.put("nomineeAddress", null);
		}
		else {
			map.put(
				"nomineeAddress",
				String.valueOf(consentMetadata.getNomineeAddress()));
		}

		if (consentMetadata.getNomineeDob() == null) {
			map.put("nomineeDob", null);
		}
		else {
			map.put(
				"nomineeDob",
				liferayToJSONDateFormat.format(
					consentMetadata.getNomineeDob()));
		}

		if (consentMetadata.getNomineeName() == null) {
			map.put("nomineeName", null);
		}
		else {
			map.put(
				"nomineeName",
				String.valueOf(consentMetadata.getNomineeName()));
		}

		if (consentMetadata.getNomineeRelation() == null) {
			map.put("nomineeRelation", null);
		}
		else {
			map.put(
				"nomineeRelation",
				String.valueOf(consentMetadata.getNomineeRelation()));
		}

		if (consentMetadata.getOccupation() == null) {
			map.put("occupation", null);
		}
		else {
			map.put(
				"occupation", String.valueOf(consentMetadata.getOccupation()));
		}

		if (consentMetadata.getPanNumber() == null) {
			map.put("panNumber", null);
		}
		else {
			map.put(
				"panNumber", String.valueOf(consentMetadata.getPanNumber()));
		}

		if (consentMetadata.getPinCode() == null) {
			map.put("pinCode", null);
		}
		else {
			map.put("pinCode", String.valueOf(consentMetadata.getPinCode()));
		}

		if (consentMetadata.getPolicyIssuanceStatus() == null) {
			map.put("policyIssuanceStatus", null);
		}
		else {
			map.put(
				"policyIssuanceStatus",
				String.valueOf(consentMetadata.getPolicyIssuanceStatus()));
		}

		if (consentMetadata.getPolicyYear() == null) {
			map.put("policyYear", null);
		}
		else {
			map.put(
				"policyYear", String.valueOf(consentMetadata.getPolicyYear()));
		}

		if (consentMetadata.getState() == null) {
			map.put("state", null);
		}
		else {
			map.put("state", String.valueOf(consentMetadata.getState()));
		}

		if (consentMetadata.getTransactionDate() == null) {
			map.put("transactionDate", null);
		}
		else {
			map.put(
				"transactionDate",
				liferayToJSONDateFormat.format(
					consentMetadata.getTransactionDate()));
		}

		if (consentMetadata.getUrn() == null) {
			map.put("urn", null);
		}
		else {
			map.put("urn", String.valueOf(consentMetadata.getUrn()));
		}

		return map;
	}

	public static class ConsentMetadataJSONParser
		extends BaseJSONParser<ConsentMetadata> {

		@Override
		protected ConsentMetadata createDTO() {
			return new ConsentMetadata();
		}

		@Override
		protected ConsentMetadata[] createDTOArray(int size) {
			return new ConsentMetadata[size];
		}

		@Override
		protected void setField(
			ConsentMetadata consentMetadata, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "aadhaarNumber")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setAadhaarNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "accountHolderName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setAccountHolderName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "address")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setAddress((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "bankAccountNo")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setBankAccountNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branchCode")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setBranchCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branchName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setBranchName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "city")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setCity((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customerId")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setCustomerId((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "dateOfBirth")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setDateOfBirth(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "district")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setDistrict((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "documentPushDateTime")) {

				if (jsonParserFieldValue != null) {
					consentMetadata.setDocumentPushDateTime(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "documentReferenceNo")) {

				if (jsonParserFieldValue != null) {
					consentMetadata.setDocumentReferenceNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "documentSentStatus")) {

				if (jsonParserFieldValue != null) {
					consentMetadata.setDocumentSentStatus(
						ConsentMetadata.DocumentSentStatus.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "emailId")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setEmailId((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "fatherHusbandName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setFatherHusbandName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "guardianAddress")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setGuardianAddress(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "guardianEmail")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setGuardianEmail(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "guardianMobile")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setGuardianMobile(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "guardianName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setGuardianName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "guardianRelation")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setGuardianRelation(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "ifscCode")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setIfscCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "insuranceType")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setInsuranceType(
						ConsentMetadata.InsuranceType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "kycDocumentName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setKycDocumentName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "kycIdNumber")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setKycIdNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "mobileNumber")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setMobileNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nomineeAddress")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setNomineeAddress(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nomineeDob")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setNomineeDob(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nomineeName")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setNomineeName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nomineeRelation")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setNomineeRelation(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "occupation")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setOccupation((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "panNumber")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setPanNumber((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "pinCode")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setPinCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "policyIssuanceStatus")) {

				if (jsonParserFieldValue != null) {
					consentMetadata.setPolicyIssuanceStatus(
						ConsentMetadata.PolicyIssuanceStatus.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "policyYear")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setPolicyYear((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "state")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setState((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transactionDate")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setTransactionDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "urn")) {
				if (jsonParserFieldValue != null) {
					consentMetadata.setUrn((String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
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
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}