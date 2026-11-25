package com.boi.headless.rest.jansuraksha.client.serdes.v1_0;

import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaMetadata;
import com.boi.headless.rest.jansuraksha.client.json.BaseJSONParser;

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
public class JanSurakshaMetadataSerDes {

	public static JanSurakshaMetadata toDTO(String json) {
		JanSurakshaMetadataJSONParser janSurakshaMetadataJSONParser =
			new JanSurakshaMetadataJSONParser();

		return janSurakshaMetadataJSONParser.parseToDTO(json);
	}

	public static JanSurakshaMetadata[] toDTOs(String json) {
		JanSurakshaMetadataJSONParser janSurakshaMetadataJSONParser =
			new JanSurakshaMetadataJSONParser();

		return janSurakshaMetadataJSONParser.parseToDTOs(json);
	}

	public static String toJSON(JanSurakshaMetadata janSurakshaMetadata) {
		if (janSurakshaMetadata == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (janSurakshaMetadata.getAccountNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountNumber\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getAccountNumber()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getBranch() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getBranch()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getBranchCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getBranchCode()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getCustomerId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerId\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getCustomerId()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getCustomerName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"customerName\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getCustomerName()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getNBGCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nBGCode\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getNBGCode()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getSolId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"solId\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getSolId()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getTransDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(
					janSurakshaMetadata.getTransDate()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getTypeOfInsurance() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"typeOfInsurance\": ");

			sb.append("\"");

			sb.append(janSurakshaMetadata.getTypeOfInsurance());

			sb.append("\"");
		}

		if (janSurakshaMetadata.getUrn() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getUrn()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getYear() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"year\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getYear()));

			sb.append("\"");
		}

		if (janSurakshaMetadata.getZone() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"zone\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaMetadata.getZone()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		JanSurakshaMetadataJSONParser janSurakshaMetadataJSONParser =
			new JanSurakshaMetadataJSONParser();

		return janSurakshaMetadataJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		JanSurakshaMetadata janSurakshaMetadata) {

		if (janSurakshaMetadata == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (janSurakshaMetadata.getAccountNumber() == null) {
			map.put("accountNumber", null);
		}
		else {
			map.put(
				"accountNumber",
				String.valueOf(janSurakshaMetadata.getAccountNumber()));
		}

		if (janSurakshaMetadata.getBranch() == null) {
			map.put("branch", null);
		}
		else {
			map.put("branch", String.valueOf(janSurakshaMetadata.getBranch()));
		}

		if (janSurakshaMetadata.getBranchCode() == null) {
			map.put("branchCode", null);
		}
		else {
			map.put(
				"branchCode",
				String.valueOf(janSurakshaMetadata.getBranchCode()));
		}

		if (janSurakshaMetadata.getCustomerId() == null) {
			map.put("customerId", null);
		}
		else {
			map.put(
				"customerId",
				String.valueOf(janSurakshaMetadata.getCustomerId()));
		}

		if (janSurakshaMetadata.getCustomerName() == null) {
			map.put("customerName", null);
		}
		else {
			map.put(
				"customerName",
				String.valueOf(janSurakshaMetadata.getCustomerName()));
		}

		if (janSurakshaMetadata.getNBGCode() == null) {
			map.put("nBGCode", null);
		}
		else {
			map.put(
				"nBGCode", String.valueOf(janSurakshaMetadata.getNBGCode()));
		}

		if (janSurakshaMetadata.getSolId() == null) {
			map.put("solId", null);
		}
		else {
			map.put("solId", String.valueOf(janSurakshaMetadata.getSolId()));
		}

		if (janSurakshaMetadata.getTransDate() == null) {
			map.put("transDate", null);
		}
		else {
			map.put(
				"transDate",
				liferayToJSONDateFormat.format(
					janSurakshaMetadata.getTransDate()));
		}

		if (janSurakshaMetadata.getTypeOfInsurance() == null) {
			map.put("typeOfInsurance", null);
		}
		else {
			map.put(
				"typeOfInsurance",
				String.valueOf(janSurakshaMetadata.getTypeOfInsurance()));
		}

		if (janSurakshaMetadata.getUrn() == null) {
			map.put("urn", null);
		}
		else {
			map.put("urn", String.valueOf(janSurakshaMetadata.getUrn()));
		}

		if (janSurakshaMetadata.getYear() == null) {
			map.put("year", null);
		}
		else {
			map.put("year", String.valueOf(janSurakshaMetadata.getYear()));
		}

		if (janSurakshaMetadata.getZone() == null) {
			map.put("zone", null);
		}
		else {
			map.put("zone", String.valueOf(janSurakshaMetadata.getZone()));
		}

		return map;
	}

	public static class JanSurakshaMetadataJSONParser
		extends BaseJSONParser<JanSurakshaMetadata> {

		@Override
		protected JanSurakshaMetadata createDTO() {
			return new JanSurakshaMetadata();
		}

		@Override
		protected JanSurakshaMetadata[] createDTOArray(int size) {
			return new JanSurakshaMetadata[size];
		}

		@Override
		protected void setField(
			JanSurakshaMetadata janSurakshaMetadata, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "accountNumber")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setAccountNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branch")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setBranch((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branchCode")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setBranchCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customerId")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setCustomerId(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "customerName")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setCustomerName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nBGCode")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setNBGCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "solId")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setSolId((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transDate")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setTransDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "typeOfInsurance")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setTypeOfInsurance(
						JanSurakshaMetadata.TypeOfInsurance.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "urn")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setUrn((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "year")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setYear((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "zone")) {
				if (jsonParserFieldValue != null) {
					janSurakshaMetadata.setZone((String)jsonParserFieldValue);
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