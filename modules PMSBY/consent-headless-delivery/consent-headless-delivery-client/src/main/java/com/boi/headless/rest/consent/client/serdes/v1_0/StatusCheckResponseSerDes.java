package com.boi.headless.rest.consent.client.serdes.v1_0;

import com.boi.headless.rest.consent.client.dto.v1_0.StatusCheckResponse;
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
public class StatusCheckResponseSerDes {

	public static StatusCheckResponse toDTO(String json) {
		StatusCheckResponseJSONParser statusCheckResponseJSONParser =
			new StatusCheckResponseJSONParser();

		return statusCheckResponseJSONParser.parseToDTO(json);
	}

	public static StatusCheckResponse[] toDTOs(String json) {
		StatusCheckResponseJSONParser statusCheckResponseJSONParser =
			new StatusCheckResponseJSONParser();

		return statusCheckResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(StatusCheckResponse statusCheckResponse) {
		if (statusCheckResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (statusCheckResponse.getDocumentReferenceNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(statusCheckResponse.getDocumentReferenceNo()));

			sb.append("\"");
		}

		if (statusCheckResponse.getDocumentVerified() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentVerified\": ");

			sb.append(statusCheckResponse.getDocumentVerified());
		}

		if (statusCheckResponse.getMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"message\": ");

			sb.append("\"");

			sb.append(_escape(statusCheckResponse.getMessage()));

			sb.append("\"");
		}

		if (statusCheckResponse.getStage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"stage\": ");

			sb.append(statusCheckResponse.getStage());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		StatusCheckResponseJSONParser statusCheckResponseJSONParser =
			new StatusCheckResponseJSONParser();

		return statusCheckResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		StatusCheckResponse statusCheckResponse) {

		if (statusCheckResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (statusCheckResponse.getDocumentReferenceNo() == null) {
			map.put("documentReferenceNo", null);
		}
		else {
			map.put(
				"documentReferenceNo",
				String.valueOf(statusCheckResponse.getDocumentReferenceNo()));
		}

		if (statusCheckResponse.getDocumentVerified() == null) {
			map.put("documentVerified", null);
		}
		else {
			map.put(
				"documentVerified",
				String.valueOf(statusCheckResponse.getDocumentVerified()));
		}

		if (statusCheckResponse.getMessage() == null) {
			map.put("message", null);
		}
		else {
			map.put(
				"message", String.valueOf(statusCheckResponse.getMessage()));
		}

		if (statusCheckResponse.getStage() == null) {
			map.put("stage", null);
		}
		else {
			map.put("stage", String.valueOf(statusCheckResponse.getStage()));
		}

		return map;
	}

	public static class StatusCheckResponseJSONParser
		extends BaseJSONParser<StatusCheckResponse> {

		@Override
		protected StatusCheckResponse createDTO() {
			return new StatusCheckResponse();
		}

		@Override
		protected StatusCheckResponse[] createDTOArray(int size) {
			return new StatusCheckResponse[size];
		}

		@Override
		protected void setField(
			StatusCheckResponse statusCheckResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "documentReferenceNo")) {
				if (jsonParserFieldValue != null) {
					statusCheckResponse.setDocumentReferenceNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "documentVerified")) {
				if (jsonParserFieldValue != null) {
					statusCheckResponse.setDocumentVerified(
						(Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "message")) {
				if (jsonParserFieldValue != null) {
					statusCheckResponse.setMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "stage")) {
				if (jsonParserFieldValue != null) {
					statusCheckResponse.setStage(
						Integer.valueOf((String)jsonParserFieldValue));
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