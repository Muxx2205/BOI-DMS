package com.boi.headless.rest.consent.client.serdes.v1_0;

import com.boi.headless.rest.consent.client.dto.v1_0.ConsentResponse;
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
public class ConsentResponseSerDes {

	public static ConsentResponse toDTO(String json) {
		ConsentResponseJSONParser consentResponseJSONParser =
			new ConsentResponseJSONParser();

		return consentResponseJSONParser.parseToDTO(json);
	}

	public static ConsentResponse[] toDTOs(String json) {
		ConsentResponseJSONParser consentResponseJSONParser =
			new ConsentResponseJSONParser();

		return consentResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ConsentResponse consentResponse) {
		if (consentResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (consentResponse.getCbsReferenceNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(consentResponse.getCbsReferenceNumber()));

			sb.append("\"");
		}

		if (consentResponse.getDocumentReferenceNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(consentResponse.getDocumentReferenceNo()));

			sb.append("\"");
		}

		if (consentResponse.getEntryId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"entryId\": ");

			sb.append(consentResponse.getEntryId());
		}

		if (consentResponse.getMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"message\": ");

			sb.append("\"");

			sb.append(_escape(consentResponse.getMessage()));

			sb.append("\"");
		}

		if (consentResponse.getStage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"stage\": ");

			sb.append(consentResponse.getStage());
		}

		if (consentResponse.getSuccess() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"success\": ");

			sb.append(consentResponse.getSuccess());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ConsentResponseJSONParser consentResponseJSONParser =
			new ConsentResponseJSONParser();

		return consentResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ConsentResponse consentResponse) {
		if (consentResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (consentResponse.getCbsReferenceNumber() == null) {
			map.put("cbsReferenceNumber", null);
		}
		else {
			map.put(
				"cbsReferenceNumber",
				String.valueOf(consentResponse.getCbsReferenceNumber()));
		}

		if (consentResponse.getDocumentReferenceNo() == null) {
			map.put("documentReferenceNo", null);
		}
		else {
			map.put(
				"documentReferenceNo",
				String.valueOf(consentResponse.getDocumentReferenceNo()));
		}

		if (consentResponse.getEntryId() == null) {
			map.put("entryId", null);
		}
		else {
			map.put("entryId", String.valueOf(consentResponse.getEntryId()));
		}

		if (consentResponse.getMessage() == null) {
			map.put("message", null);
		}
		else {
			map.put("message", String.valueOf(consentResponse.getMessage()));
		}

		if (consentResponse.getStage() == null) {
			map.put("stage", null);
		}
		else {
			map.put("stage", String.valueOf(consentResponse.getStage()));
		}

		if (consentResponse.getSuccess() == null) {
			map.put("success", null);
		}
		else {
			map.put("success", String.valueOf(consentResponse.getSuccess()));
		}

		return map;
	}

	public static class ConsentResponseJSONParser
		extends BaseJSONParser<ConsentResponse> {

		@Override
		protected ConsentResponse createDTO() {
			return new ConsentResponse();
		}

		@Override
		protected ConsentResponse[] createDTOArray(int size) {
			return new ConsentResponse[size];
		}

		@Override
		protected void setField(
			ConsentResponse consentResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "cbsReferenceNumber")) {
				if (jsonParserFieldValue != null) {
					consentResponse.setCbsReferenceNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "documentReferenceNo")) {

				if (jsonParserFieldValue != null) {
					consentResponse.setDocumentReferenceNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "entryId")) {
				if (jsonParserFieldValue != null) {
					consentResponse.setEntryId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "message")) {
				if (jsonParserFieldValue != null) {
					consentResponse.setMessage((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "stage")) {
				if (jsonParserFieldValue != null) {
					consentResponse.setStage(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "success")) {
				if (jsonParserFieldValue != null) {
					consentResponse.setSuccess((Boolean)jsonParserFieldValue);
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