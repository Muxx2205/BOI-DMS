package com.boi.headless.rest.jansuraksha.client.serdes.v1_0;

import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaResponse;
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
public class JanSurakshaResponseSerDes {

	public static JanSurakshaResponse toDTO(String json) {
		JanSurakshaResponseJSONParser janSurakshaResponseJSONParser =
			new JanSurakshaResponseJSONParser();

		return janSurakshaResponseJSONParser.parseToDTO(json);
	}

	public static JanSurakshaResponse[] toDTOs(String json) {
		JanSurakshaResponseJSONParser janSurakshaResponseJSONParser =
			new JanSurakshaResponseJSONParser();

		return janSurakshaResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(JanSurakshaResponse janSurakshaResponse) {
		if (janSurakshaResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (janSurakshaResponse.getResponseId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseId\": ");

			sb.append(janSurakshaResponse.getResponseId());
		}

		if (janSurakshaResponse.getResponseStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseStatus\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaResponse.getResponseStatus()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		JanSurakshaResponseJSONParser janSurakshaResponseJSONParser =
			new JanSurakshaResponseJSONParser();

		return janSurakshaResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		JanSurakshaResponse janSurakshaResponse) {

		if (janSurakshaResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (janSurakshaResponse.getResponseId() == null) {
			map.put("responseId", null);
		}
		else {
			map.put(
				"responseId",
				String.valueOf(janSurakshaResponse.getResponseId()));
		}

		if (janSurakshaResponse.getResponseStatus() == null) {
			map.put("responseStatus", null);
		}
		else {
			map.put(
				"responseStatus",
				String.valueOf(janSurakshaResponse.getResponseStatus()));
		}

		return map;
	}

	public static class JanSurakshaResponseJSONParser
		extends BaseJSONParser<JanSurakshaResponse> {

		@Override
		protected JanSurakshaResponse createDTO() {
			return new JanSurakshaResponse();
		}

		@Override
		protected JanSurakshaResponse[] createDTOArray(int size) {
			return new JanSurakshaResponse[size];
		}

		@Override
		protected void setField(
			JanSurakshaResponse janSurakshaResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "responseId")) {
				if (jsonParserFieldValue != null) {
					janSurakshaResponse.setResponseId(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "responseStatus")) {
				if (jsonParserFieldValue != null) {
					janSurakshaResponse.setResponseStatus(
						(String)jsonParserFieldValue);
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