package com.vls.status.api.client.serdes.v1_0;

import com.vls.status.api.client.dto.v1_0.DdePushStatusResponse;
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
public class DdePushStatusResponseSerDes {

	public static DdePushStatusResponse toDTO(String json) {
		DdePushStatusResponseJSONParser ddePushStatusResponseJSONParser =
			new DdePushStatusResponseJSONParser();

		return ddePushStatusResponseJSONParser.parseToDTO(json);
	}

	public static DdePushStatusResponse[] toDTOs(String json) {
		DdePushStatusResponseJSONParser ddePushStatusResponseJSONParser =
			new DdePushStatusResponseJSONParser();

		return ddePushStatusResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DdePushStatusResponse ddePushStatusResponse) {
		if (ddePushStatusResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (ddePushStatusResponse.getDocDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"docDate\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getDocDate()));

			sb.append("\"");
		}

		if (ddePushStatusResponse.getLoanno() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"loanno\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getLoanno()));

			sb.append("\"");
		}

		if (ddePushStatusResponse.getSnctnno() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"snctnno\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getSnctnno()));

			sb.append("\"");
		}

		if (ddePushStatusResponse.getStatusCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"statusCode\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getStatusCode()));

			sb.append("\"");
		}

		if (ddePushStatusResponse.getStatusMsg() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"statusMsg\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getStatusMsg()));

			sb.append("\"");
		}

		if (ddePushStatusResponse.getTransId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transId\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusResponse.getTransId()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DdePushStatusResponseJSONParser ddePushStatusResponseJSONParser =
			new DdePushStatusResponseJSONParser();

		return ddePushStatusResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DdePushStatusResponse ddePushStatusResponse) {

		if (ddePushStatusResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (ddePushStatusResponse.getDocDate() == null) {
			map.put("docDate", null);
		}
		else {
			map.put(
				"docDate", String.valueOf(ddePushStatusResponse.getDocDate()));
		}

		if (ddePushStatusResponse.getLoanno() == null) {
			map.put("loanno", null);
		}
		else {
			map.put(
				"loanno", String.valueOf(ddePushStatusResponse.getLoanno()));
		}

		if (ddePushStatusResponse.getSnctnno() == null) {
			map.put("snctnno", null);
		}
		else {
			map.put(
				"snctnno", String.valueOf(ddePushStatusResponse.getSnctnno()));
		}

		if (ddePushStatusResponse.getStatusCode() == null) {
			map.put("statusCode", null);
		}
		else {
			map.put(
				"statusCode",
				String.valueOf(ddePushStatusResponse.getStatusCode()));
		}

		if (ddePushStatusResponse.getStatusMsg() == null) {
			map.put("statusMsg", null);
		}
		else {
			map.put(
				"statusMsg",
				String.valueOf(ddePushStatusResponse.getStatusMsg()));
		}

		if (ddePushStatusResponse.getTransId() == null) {
			map.put("transId", null);
		}
		else {
			map.put(
				"transId", String.valueOf(ddePushStatusResponse.getTransId()));
		}

		return map;
	}

	public static class DdePushStatusResponseJSONParser
		extends BaseJSONParser<DdePushStatusResponse> {

		@Override
		protected DdePushStatusResponse createDTO() {
			return new DdePushStatusResponse();
		}

		@Override
		protected DdePushStatusResponse[] createDTOArray(int size) {
			return new DdePushStatusResponse[size];
		}

		@Override
		protected void setField(
			DdePushStatusResponse ddePushStatusResponse,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "docDate")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setDocDate(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "loanno")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setLoanno(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "snctnno")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setSnctnno(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "statusCode")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setStatusCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "statusMsg")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setStatusMsg(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transId")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusResponse.setTransId(
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