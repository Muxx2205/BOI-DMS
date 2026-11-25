package com.boi.headless.rest.jansuraksha.client.serdes.v1_0;

import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaUploadResponse;
import com.boi.headless.rest.jansuraksha.client.json.BaseJSONParser;

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
public class JanSurakshaUploadResponseSerDes {

	public static JanSurakshaUploadResponse toDTO(String json) {
		JanSurakshaUploadResponseJSONParser
			janSurakshaUploadResponseJSONParser =
				new JanSurakshaUploadResponseJSONParser();

		return janSurakshaUploadResponseJSONParser.parseToDTO(json);
	}

	public static JanSurakshaUploadResponse[] toDTOs(String json) {
		JanSurakshaUploadResponseJSONParser
			janSurakshaUploadResponseJSONParser =
				new JanSurakshaUploadResponseJSONParser();

		return janSurakshaUploadResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		JanSurakshaUploadResponse janSurakshaUploadResponse) {

		if (janSurakshaUploadResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (janSurakshaUploadResponse.getDocumentReferenceNo() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentReferenceNo\": ");

			sb.append("\"");

			sb.append(
				_escape(janSurakshaUploadResponse.getDocumentReferenceNo()));

			sb.append("\"");
		}

		if (janSurakshaUploadResponse.getEntryId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"entryId\": ");

			sb.append(janSurakshaUploadResponse.getEntryId());
		}

		if (janSurakshaUploadResponse.getMessage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"message\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaUploadResponse.getMessage()));

			sb.append("\"");
		}

		if (janSurakshaUploadResponse.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(janSurakshaUploadResponse.getStatus());

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		JanSurakshaUploadResponseJSONParser
			janSurakshaUploadResponseJSONParser =
				new JanSurakshaUploadResponseJSONParser();

		return janSurakshaUploadResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		JanSurakshaUploadResponse janSurakshaUploadResponse) {

		if (janSurakshaUploadResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (janSurakshaUploadResponse.getDocumentReferenceNo() == null) {
			map.put("documentReferenceNo", null);
		}
		else {
			map.put(
				"documentReferenceNo",
				String.valueOf(
					janSurakshaUploadResponse.getDocumentReferenceNo()));
		}

		if (janSurakshaUploadResponse.getEntryId() == null) {
			map.put("entryId", null);
		}
		else {
			map.put(
				"entryId",
				String.valueOf(janSurakshaUploadResponse.getEntryId()));
		}

		if (janSurakshaUploadResponse.getMessage() == null) {
			map.put("message", null);
		}
		else {
			map.put(
				"message",
				String.valueOf(janSurakshaUploadResponse.getMessage()));
		}

		if (janSurakshaUploadResponse.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put(
				"status",
				String.valueOf(janSurakshaUploadResponse.getStatus()));
		}

		return map;
	}

	public static class JanSurakshaUploadResponseJSONParser
		extends BaseJSONParser<JanSurakshaUploadResponse> {

		@Override
		protected JanSurakshaUploadResponse createDTO() {
			return new JanSurakshaUploadResponse();
		}

		@Override
		protected JanSurakshaUploadResponse[] createDTOArray(int size) {
			return new JanSurakshaUploadResponse[size];
		}

		@Override
		protected void setField(
			JanSurakshaUploadResponse janSurakshaUploadResponse,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "documentReferenceNo")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadResponse.setDocumentReferenceNo(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "entryId")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadResponse.setEntryId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "message")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadResponse.setMessage(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadResponse.setStatus(
						JanSurakshaUploadResponse.Status.create(
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