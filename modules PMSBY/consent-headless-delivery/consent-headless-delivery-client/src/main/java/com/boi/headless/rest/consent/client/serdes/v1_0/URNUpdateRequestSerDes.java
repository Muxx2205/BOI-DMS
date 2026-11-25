package com.boi.headless.rest.consent.client.serdes.v1_0;

import com.boi.headless.rest.consent.client.dto.v1_0.URNUpdateRequest;
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
public class URNUpdateRequestSerDes {

	public static URNUpdateRequest toDTO(String json) {
		URNUpdateRequestJSONParser urnUpdateRequestJSONParser =
			new URNUpdateRequestJSONParser();

		return urnUpdateRequestJSONParser.parseToDTO(json);
	}

	public static URNUpdateRequest[] toDTOs(String json) {
		URNUpdateRequestJSONParser urnUpdateRequestJSONParser =
			new URNUpdateRequestJSONParser();

		return urnUpdateRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(URNUpdateRequest urnUpdateRequest) {
		if (urnUpdateRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (urnUpdateRequest.getCbsReferenceNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cbsReferenceNumber\": ");

			sb.append("\"");

			sb.append(_escape(urnUpdateRequest.getCbsReferenceNumber()));

			sb.append("\"");
		}

		if (urnUpdateRequest.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(urnUpdateRequest.getStatus()));

			sb.append("\"");
		}

		if (urnUpdateRequest.getTransDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transDate\": ");

			sb.append("\"");

			sb.append(_escape(urnUpdateRequest.getTransDate()));

			sb.append("\"");
		}

		if (urnUpdateRequest.getUrn() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"urn\": ");

			sb.append("\"");

			sb.append(_escape(urnUpdateRequest.getUrn()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		URNUpdateRequestJSONParser urnUpdateRequestJSONParser =
			new URNUpdateRequestJSONParser();

		return urnUpdateRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(URNUpdateRequest urnUpdateRequest) {
		if (urnUpdateRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (urnUpdateRequest.getCbsReferenceNumber() == null) {
			map.put("cbsReferenceNumber", null);
		}
		else {
			map.put(
				"cbsReferenceNumber",
				String.valueOf(urnUpdateRequest.getCbsReferenceNumber()));
		}

		if (urnUpdateRequest.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(urnUpdateRequest.getStatus()));
		}

		if (urnUpdateRequest.getTransDate() == null) {
			map.put("transDate", null);
		}
		else {
			map.put(
				"transDate", String.valueOf(urnUpdateRequest.getTransDate()));
		}

		if (urnUpdateRequest.getUrn() == null) {
			map.put("urn", null);
		}
		else {
			map.put("urn", String.valueOf(urnUpdateRequest.getUrn()));
		}

		return map;
	}

	public static class URNUpdateRequestJSONParser
		extends BaseJSONParser<URNUpdateRequest> {

		@Override
		protected URNUpdateRequest createDTO() {
			return new URNUpdateRequest();
		}

		@Override
		protected URNUpdateRequest[] createDTOArray(int size) {
			return new URNUpdateRequest[size];
		}

		@Override
		protected void setField(
			URNUpdateRequest urnUpdateRequest, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "cbsReferenceNumber")) {
				if (jsonParserFieldValue != null) {
					urnUpdateRequest.setCbsReferenceNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					urnUpdateRequest.setStatus((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transDate")) {
				if (jsonParserFieldValue != null) {
					urnUpdateRequest.setTransDate((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "urn")) {
				if (jsonParserFieldValue != null) {
					urnUpdateRequest.setUrn((String)jsonParserFieldValue);
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