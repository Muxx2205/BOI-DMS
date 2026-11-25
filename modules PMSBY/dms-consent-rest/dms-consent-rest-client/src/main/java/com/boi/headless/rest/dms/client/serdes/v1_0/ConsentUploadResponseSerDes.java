package com.boi.headless.rest.dms.client.serdes.v1_0;

import com.boi.headless.rest.dms.client.dto.v1_0.ConsentUploadResponse;
import com.boi.headless.rest.dms.client.json.BaseJSONParser;

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
public class ConsentUploadResponseSerDes {

	public static ConsentUploadResponse toDTO(String json) {
		ConsentUploadResponseJSONParser consentUploadResponseJSONParser =
			new ConsentUploadResponseJSONParser();

		return consentUploadResponseJSONParser.parseToDTO(json);
	}

	public static ConsentUploadResponse[] toDTOs(String json) {
		ConsentUploadResponseJSONParser consentUploadResponseJSONParser =
			new ConsentUploadResponseJSONParser();

		return consentUploadResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ConsentUploadResponse consentUploadResponse) {
		if (consentUploadResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (consentUploadResponse.getDocumentReferenceNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(_escape(consentUploadResponse.getDocumentReferenceNo()));

			sb.append("\"");
		}

		if (consentUploadResponse.getEntryId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"entryId\": ");

			sb.append(consentUploadResponse.getEntryId());
		}

		if (consentUploadResponse.getMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"message\": ");

			sb.append("\"");

			sb.append(_escape(consentUploadResponse.getMessage()));

			sb.append("\"");
		}

		if (consentUploadResponse.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(consentUploadResponse.getStatus());

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ConsentUploadResponseJSONParser consentUploadResponseJSONParser =
			new ConsentUploadResponseJSONParser();

		return consentUploadResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		ConsentUploadResponse consentUploadResponse) {

		if (consentUploadResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (consentUploadResponse.getDocumentReferenceNo() == null) {
			map.put("documentReferenceNo", null);
		}
		else {
			map.put(
				"documentReferenceNo",
				String.valueOf(consentUploadResponse.getDocumentReferenceNo()));
		}

		if (consentUploadResponse.getEntryId() == null) {
			map.put("entryId", null);
		}
		else {
			map.put(
				"entryId", String.valueOf(consentUploadResponse.getEntryId()));
		}

		if (consentUploadResponse.getMessage() == null) {
			map.put("message", null);
		}
		else {
			map.put(
				"message", String.valueOf(consentUploadResponse.getMessage()));
		}

		if (consentUploadResponse.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put(
				"status", String.valueOf(consentUploadResponse.getStatus()));
		}

		return map;
	}

	public static class ConsentUploadResponseJSONParser
		extends BaseJSONParser<ConsentUploadResponse> {

		@Override
		protected ConsentUploadResponse createDTO() {
			return new ConsentUploadResponse();
		}

		@Override
		protected ConsentUploadResponse[] createDTOArray(int size) {
			return new ConsentUploadResponse[size];
		}

		@Override
		protected void setField(
			ConsentUploadResponse consentUploadResponse,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "documentReferenceNo")) {
				if (jsonParserFieldValue != null) {
					consentUploadResponse.setDocumentReferenceNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "entryId")) {
				if (jsonParserFieldValue != null) {
					consentUploadResponse.setEntryId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "message")) {
				if (jsonParserFieldValue != null) {
					consentUploadResponse.setMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					consentUploadResponse.setStatus(
						ConsentUploadResponse.Status.create(
							(String)jsonParserFieldValue));
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