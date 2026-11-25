package com.boi.headless.rest.consent.client.serdes.v1_0;

import com.boi.headless.rest.consent.client.dto.v1_0.CBSMetadataRequest;
import com.boi.headless.rest.consent.client.json.BaseJSONParser;

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
public class CBSMetadataRequestSerDes {

	public static CBSMetadataRequest toDTO(String json) {
		CBSMetadataRequestJSONParser cbsMetadataRequestJSONParser =
			new CBSMetadataRequestJSONParser();

		return cbsMetadataRequestJSONParser.parseToDTO(json);
	}

	public static CBSMetadataRequest[] toDTOs(String json) {
		CBSMetadataRequestJSONParser cbsMetadataRequestJSONParser =
			new CBSMetadataRequestJSONParser();

		return cbsMetadataRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(CBSMetadataRequest cbsMetadataRequest) {
		if (cbsMetadataRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (cbsMetadataRequest.getAccountNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountNumber\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getAccountNumber()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getBranch() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getBranch()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getBranchCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getBranchCode()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getCbsReferenceNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getCbsReferenceNumber()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getConsentFormDocumentFrom() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentFrom\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getConsentFormDocumentFrom()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getConsentFormDocumentTo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"consentFormDocumentTo\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getConsentFormDocumentTo()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getCustomerId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerId\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getCustomerId()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getCustomerName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerName\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getCustomerName()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getNBGCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nBGCode\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getNBGCode()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getSolId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"solId\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getSolId()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getTransDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getTransDate()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getTypeOfInsurance() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"typeOfInsurance\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getTypeOfInsurance()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getUrn() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getUrn()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getYear() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"year\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getYear()));

			sb.append("\"");
		}

		if (cbsMetadataRequest.getZone() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"zone\": ");

			sb.append("\"");

			sb.append(_escape(cbsMetadataRequest.getZone()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		CBSMetadataRequestJSONParser cbsMetadataRequestJSONParser =
			new CBSMetadataRequestJSONParser();

		return cbsMetadataRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		CBSMetadataRequest cbsMetadataRequest) {

		if (cbsMetadataRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (cbsMetadataRequest.getAccountNumber() == null) {
			map.put("accountNumber", null);
		}
		else {
			map.put(
				"accountNumber",
				String.valueOf(cbsMetadataRequest.getAccountNumber()));
		}

		if (cbsMetadataRequest.getBranch() == null) {
			map.put("branch", null);
		}
		else {
			map.put("branch", String.valueOf(cbsMetadataRequest.getBranch()));
		}

		if (cbsMetadataRequest.getBranchCode() == null) {
			map.put("branchCode", null);
		}
		else {
			map.put(
				"branchCode",
				String.valueOf(cbsMetadataRequest.getBranchCode()));
		}

		if (cbsMetadataRequest.getCbsReferenceNumber() == null) {
			map.put("cbsReferenceNumber", null);
		}
		else {
			map.put(
				"cbsReferenceNumber",
				String.valueOf(cbsMetadataRequest.getCbsReferenceNumber()));
		}

		if (cbsMetadataRequest.getConsentFormDocumentFrom() == null) {
			map.put("consentFormDocumentFrom", null);
		}
		else {
			map.put(
				"consentFormDocumentFrom",
				String.valueOf(
					cbsMetadataRequest.getConsentFormDocumentFrom()));
		}

		if (cbsMetadataRequest.getConsentFormDocumentTo() == null) {
			map.put("consentFormDocumentTo", null);
		}
		else {
			map.put(
				"consentFormDocumentTo",
				String.valueOf(cbsMetadataRequest.getConsentFormDocumentTo()));
		}

		if (cbsMetadataRequest.getCustomerId() == null) {
			map.put("customerId", null);
		}
		else {
			map.put(
				"customerId",
				String.valueOf(cbsMetadataRequest.getCustomerId()));
		}

		if (cbsMetadataRequest.getCustomerName() == null) {
			map.put("customerName", null);
		}
		else {
			map.put(
				"customerName",
				String.valueOf(cbsMetadataRequest.getCustomerName()));
		}

		if (cbsMetadataRequest.getNBGCode() == null) {
			map.put("nBGCode", null);
		}
		else {
			map.put("nBGCode", String.valueOf(cbsMetadataRequest.getNBGCode()));
		}

		if (cbsMetadataRequest.getSolId() == null) {
			map.put("solId", null);
		}
		else {
			map.put("solId", String.valueOf(cbsMetadataRequest.getSolId()));
		}

		if (cbsMetadataRequest.getTransDate() == null) {
			map.put("transDate", null);
		}
		else {
			map.put(
				"transDate", String.valueOf(cbsMetadataRequest.getTransDate()));
		}

		if (cbsMetadataRequest.getTypeOfInsurance() == null) {
			map.put("typeOfInsurance", null);
		}
		else {
			map.put(
				"typeOfInsurance",
				String.valueOf(cbsMetadataRequest.getTypeOfInsurance()));
		}

		if (cbsMetadataRequest.getUrn() == null) {
			map.put("urn", null);
		}
		else {
			map.put("urn", String.valueOf(cbsMetadataRequest.getUrn()));
		}

		if (cbsMetadataRequest.getYear() == null) {
			map.put("year", null);
		}
		else {
			map.put("year", String.valueOf(cbsMetadataRequest.getYear()));
		}

		if (cbsMetadataRequest.getZone() == null) {
			map.put("zone", null);
		}
		else {
			map.put("zone", String.valueOf(cbsMetadataRequest.getZone()));
		}

		return map;
	}

	public static class CBSMetadataRequestJSONParser
		extends BaseJSONParser<CBSMetadataRequest> {

		@Override
		protected CBSMetadataRequest createDTO() {
			return new CBSMetadataRequest();
		}

		@Override
		protected CBSMetadataRequest[] createDTOArray(int size) {
			return new CBSMetadataRequest[size];
		}

		@Override
		protected void setField(
			CBSMetadataRequest cbsMetadataRequest, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "accountNumber")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setAccountNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branch")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setBranch((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branchCode")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setBranchCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "cbsReferenceNumber")) {

				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setCbsReferenceNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "consentFormDocumentFrom")) {

				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setConsentFormDocumentFrom(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "consentFormDocumentTo")) {

				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setConsentFormDocumentTo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customerId")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setCustomerId(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customerName")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setCustomerName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nBGCode")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setNBGCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "solId")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setSolId((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transDate")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setTransDate(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "typeOfInsurance")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setTypeOfInsurance(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "urn")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setUrn((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "year")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setYear((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "zone")) {
				if (jsonParserFieldValue != null) {
					cbsMetadataRequest.setZone((String)jsonParserFieldValue);
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