package com.vls.status.api.client.serdes.v1_0;

import com.vls.status.api.client.dto.v1_0.LoanDocDataResponse;
import com.vls.status.api.client.json.BaseJSONParser;

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
public class LoanDocDataResponseSerDes {

	public static LoanDocDataResponse toDTO(String json) {
		LoanDocDataResponseJSONParser loanDocDataResponseJSONParser =
			new LoanDocDataResponseJSONParser();

		return loanDocDataResponseJSONParser.parseToDTO(json);
	}

	public static LoanDocDataResponse[] toDTOs(String json) {
		LoanDocDataResponseJSONParser loanDocDataResponseJSONParser =
			new LoanDocDataResponseJSONParser();

		return loanDocDataResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(LoanDocDataResponse loanDocDataResponse) {
		if (loanDocDataResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (loanDocDataResponse.getCipherText() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"cipherText\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataResponse.getCipherText()));

			sb.append("\"");
		}

		if (loanDocDataResponse.getDigisign() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"digisign\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataResponse.getDigisign()));

			sb.append("\"");
		}

		if (loanDocDataResponse.getSessionId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"sessionId\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataResponse.getSessionId()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		LoanDocDataResponseJSONParser loanDocDataResponseJSONParser =
			new LoanDocDataResponseJSONParser();

		return loanDocDataResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		LoanDocDataResponse loanDocDataResponse) {

		if (loanDocDataResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (loanDocDataResponse.getCipherText() == null) {
			map.put("cipherText", null);
		}
		else {
			map.put(
				"cipherText",
				String.valueOf(loanDocDataResponse.getCipherText()));
		}

		if (loanDocDataResponse.getDigisign() == null) {
			map.put("digisign", null);
		}
		else {
			map.put(
				"digisign", String.valueOf(loanDocDataResponse.getDigisign()));
		}

		if (loanDocDataResponse.getSessionId() == null) {
			map.put("sessionId", null);
		}
		else {
			map.put(
				"sessionId",
				String.valueOf(loanDocDataResponse.getSessionId()));
		}

		return map;
	}

	public static class LoanDocDataResponseJSONParser
		extends BaseJSONParser<LoanDocDataResponse> {

		@Override
		protected LoanDocDataResponse createDTO() {
			return new LoanDocDataResponse();
		}

		@Override
		protected LoanDocDataResponse[] createDTOArray(int size) {
			return new LoanDocDataResponse[size];
		}

		@Override
		protected void setField(
			LoanDocDataResponse loanDocDataResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "cipherText")) {
				if (jsonParserFieldValue != null) {
					loanDocDataResponse.setCipherText(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "digisign")) {
				if (jsonParserFieldValue != null) {
					loanDocDataResponse.setDigisign(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "sessionId")) {
				if (jsonParserFieldValue != null) {
					loanDocDataResponse.setSessionId(
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