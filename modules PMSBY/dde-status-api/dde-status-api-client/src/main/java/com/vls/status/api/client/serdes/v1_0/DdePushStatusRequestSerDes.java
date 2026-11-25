package com.vls.status.api.client.serdes.v1_0;

import com.vls.status.api.client.dto.v1_0.DdePushStatusRequest;
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
public class DdePushStatusRequestSerDes {

	public static DdePushStatusRequest toDTO(String json) {
		DdePushStatusRequestJSONParser ddePushStatusRequestJSONParser =
			new DdePushStatusRequestJSONParser();

		return ddePushStatusRequestJSONParser.parseToDTO(json);
	}

	public static DdePushStatusRequest[] toDTOs(String json) {
		DdePushStatusRequestJSONParser ddePushStatusRequestJSONParser =
			new DdePushStatusRequestJSONParser();

		return ddePushStatusRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DdePushStatusRequest ddePushStatusRequest) {
		if (ddePushStatusRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (ddePushStatusRequest.getDigisign() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"digisign\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusRequest.getDigisign()));

			sb.append("\"");
		}

		if (ddePushStatusRequest.getLoanno() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"loanno\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusRequest.getLoanno()));

			sb.append("\"");
		}

		if (ddePushStatusRequest.getSnctnno() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"snctnno\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusRequest.getSnctnno()));

			sb.append("\"");
		}

		if (ddePushStatusRequest.getTransId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transId\": ");

			sb.append("\"");

			sb.append(_escape(ddePushStatusRequest.getTransId()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DdePushStatusRequestJSONParser ddePushStatusRequestJSONParser =
			new DdePushStatusRequestJSONParser();

		return ddePushStatusRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DdePushStatusRequest ddePushStatusRequest) {

		if (ddePushStatusRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (ddePushStatusRequest.getDigisign() == null) {
			map.put("digisign", null);
		}
		else {
			map.put(
				"digisign", String.valueOf(ddePushStatusRequest.getDigisign()));
		}

		if (ddePushStatusRequest.getLoanno() == null) {
			map.put("loanno", null);
		}
		else {
			map.put("loanno", String.valueOf(ddePushStatusRequest.getLoanno()));
		}

		if (ddePushStatusRequest.getSnctnno() == null) {
			map.put("snctnno", null);
		}
		else {
			map.put(
				"snctnno", String.valueOf(ddePushStatusRequest.getSnctnno()));
		}

		if (ddePushStatusRequest.getTransId() == null) {
			map.put("transId", null);
		}
		else {
			map.put(
				"transId", String.valueOf(ddePushStatusRequest.getTransId()));
		}

		return map;
	}

	public static class DdePushStatusRequestJSONParser
		extends BaseJSONParser<DdePushStatusRequest> {

		@Override
		protected DdePushStatusRequest createDTO() {
			return new DdePushStatusRequest();
		}

		@Override
		protected DdePushStatusRequest[] createDTOArray(int size) {
			return new DdePushStatusRequest[size];
		}

		@Override
		protected void setField(
			DdePushStatusRequest ddePushStatusRequest,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "digisign")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusRequest.setDigisign(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "loanno")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusRequest.setLoanno(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "snctnno")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusRequest.setSnctnno(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transId")) {
				if (jsonParserFieldValue != null) {
					ddePushStatusRequest.setTransId(
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