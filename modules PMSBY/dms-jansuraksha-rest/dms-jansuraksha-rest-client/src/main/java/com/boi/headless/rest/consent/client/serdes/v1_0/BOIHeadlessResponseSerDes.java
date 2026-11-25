package com.boi.headless.rest.consent.client.serdes.v1_0;

import com.boi.headless.rest.consent.client.dto.v1_0.BOIHeadlessResponse;
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
public class BOIHeadlessResponseSerDes {

	public static BOIHeadlessResponse toDTO(String json) {
		BOIHeadlessResponseJSONParser boiHeadlessResponseJSONParser =
			new BOIHeadlessResponseJSONParser();

		return boiHeadlessResponseJSONParser.parseToDTO(json);
	}

	public static BOIHeadlessResponse[] toDTOs(String json) {
		BOIHeadlessResponseJSONParser boiHeadlessResponseJSONParser =
			new BOIHeadlessResponseJSONParser();

		return boiHeadlessResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(BOIHeadlessResponse boiHeadlessResponse) {
		if (boiHeadlessResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (boiHeadlessResponse.getResponseId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseId\": ");

			sb.append(boiHeadlessResponse.getResponseId());
		}

		if (boiHeadlessResponse.getResponseStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"responseStatus\": ");

			sb.append("\"");

			sb.append(_escape(boiHeadlessResponse.getResponseStatus()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		BOIHeadlessResponseJSONParser boiHeadlessResponseJSONParser =
			new BOIHeadlessResponseJSONParser();

		return boiHeadlessResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		BOIHeadlessResponse boiHeadlessResponse) {

		if (boiHeadlessResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (boiHeadlessResponse.getResponseId() == null) {
			map.put("responseId", null);
		}
		else {
			map.put(
				"responseId",
				String.valueOf(boiHeadlessResponse.getResponseId()));
		}

		if (boiHeadlessResponse.getResponseStatus() == null) {
			map.put("responseStatus", null);
		}
		else {
			map.put(
				"responseStatus",
				String.valueOf(boiHeadlessResponse.getResponseStatus()));
		}

		return map;
	}

	public static class BOIHeadlessResponseJSONParser
		extends BaseJSONParser<BOIHeadlessResponse> {

		@Override
		protected BOIHeadlessResponse createDTO() {
			return new BOIHeadlessResponse();
		}

		@Override
		protected BOIHeadlessResponse[] createDTOArray(int size) {
			return new BOIHeadlessResponse[size];
		}

		@Override
		protected void setField(
			BOIHeadlessResponse boiHeadlessResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "responseId")) {
				if (jsonParserFieldValue != null) {
					boiHeadlessResponse.setResponseId(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "responseStatus")) {
				if (jsonParserFieldValue != null) {
					boiHeadlessResponse.setResponseStatus(
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