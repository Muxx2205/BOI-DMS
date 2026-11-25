package com.vls.dde.ret.api.client.serdes.v1_0;

import com.vls.dde.ret.api.client.dto.v1_0.DdeReturnResponse;
import com.vls.dde.ret.api.client.json.BaseJSONParser;

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
public class DdeReturnResponseSerDes {

	public static DdeReturnResponse toDTO(String json) {
		DdeReturnResponseJSONParser ddeReturnResponseJSONParser =
			new DdeReturnResponseJSONParser();

		return ddeReturnResponseJSONParser.parseToDTO(json);
	}

	public static DdeReturnResponse[] toDTOs(String json) {
		DdeReturnResponseJSONParser ddeReturnResponseJSONParser =
			new DdeReturnResponseJSONParser();

		return ddeReturnResponseJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DdeReturnResponse ddeReturnResponse) {
		if (ddeReturnResponse == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (ddeReturnResponse.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnResponse.getStatus()));

			sb.append("\"");
		}

		if (ddeReturnResponse.getStatusCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"statusCode\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnResponse.getStatusCode()));

			sb.append("\"");
		}

		if (ddeReturnResponse.getTxnID() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"txnID\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnResponse.getTxnID()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DdeReturnResponseJSONParser ddeReturnResponseJSONParser =
			new DdeReturnResponseJSONParser();

		return ddeReturnResponseJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DdeReturnResponse ddeReturnResponse) {

		if (ddeReturnResponse == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (ddeReturnResponse.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(ddeReturnResponse.getStatus()));
		}

		if (ddeReturnResponse.getStatusCode() == null) {
			map.put("statusCode", null);
		}
		else {
			map.put(
				"statusCode",
				String.valueOf(ddeReturnResponse.getStatusCode()));
		}

		if (ddeReturnResponse.getTxnID() == null) {
			map.put("txnID", null);
		}
		else {
			map.put("txnID", String.valueOf(ddeReturnResponse.getTxnID()));
		}

		return map;
	}

	public static class DdeReturnResponseJSONParser
		extends BaseJSONParser<DdeReturnResponse> {

		@Override
		protected DdeReturnResponse createDTO() {
			return new DdeReturnResponse();
		}

		@Override
		protected DdeReturnResponse[] createDTOArray(int size) {
			return new DdeReturnResponse[size];
		}

		@Override
		protected void setField(
			DdeReturnResponse ddeReturnResponse, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					ddeReturnResponse.setStatus((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "statusCode")) {
				if (jsonParserFieldValue != null) {
					ddeReturnResponse.setStatusCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "txnID")) {
				if (jsonParserFieldValue != null) {
					ddeReturnResponse.setTxnID((String)jsonParserFieldValue);
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