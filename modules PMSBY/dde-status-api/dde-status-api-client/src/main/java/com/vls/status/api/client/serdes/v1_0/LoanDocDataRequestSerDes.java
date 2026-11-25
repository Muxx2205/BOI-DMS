package com.vls.status.api.client.serdes.v1_0;

import com.vls.status.api.client.dto.v1_0.LoanDocDataRequest;
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
public class LoanDocDataRequestSerDes {

	public static LoanDocDataRequest toDTO(String json) {
		LoanDocDataRequestJSONParser loanDocDataRequestJSONParser =
			new LoanDocDataRequestJSONParser();

		return loanDocDataRequestJSONParser.parseToDTO(json);
	}

	public static LoanDocDataRequest[] toDTOs(String json) {
		LoanDocDataRequestJSONParser loanDocDataRequestJSONParser =
			new LoanDocDataRequestJSONParser();

		return loanDocDataRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(LoanDocDataRequest loanDocDataRequest) {
		if (loanDocDataRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (loanDocDataRequest.getDigisign() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"digisign\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataRequest.getDigisign()));

			sb.append("\"");
		}

		if (loanDocDataRequest.getLoanno() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"loanno\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataRequest.getLoanno()));

			sb.append("\"");
		}

		if (loanDocDataRequest.getTransId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"transId\": ");

			sb.append("\"");

			sb.append(_escape(loanDocDataRequest.getTransId()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		LoanDocDataRequestJSONParser loanDocDataRequestJSONParser =
			new LoanDocDataRequestJSONParser();

		return loanDocDataRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		LoanDocDataRequest loanDocDataRequest) {

		if (loanDocDataRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (loanDocDataRequest.getDigisign() == null) {
			map.put("digisign", null);
		}
		else {
			map.put(
				"digisign", String.valueOf(loanDocDataRequest.getDigisign()));
		}

		if (loanDocDataRequest.getLoanno() == null) {
			map.put("loanno", null);
		}
		else {
			map.put("loanno", String.valueOf(loanDocDataRequest.getLoanno()));
		}

		if (loanDocDataRequest.getTransId() == null) {
			map.put("transId", null);
		}
		else {
			map.put("transId", String.valueOf(loanDocDataRequest.getTransId()));
		}

		return map;
	}

	public static class LoanDocDataRequestJSONParser
		extends BaseJSONParser<LoanDocDataRequest> {

		@Override
		protected LoanDocDataRequest createDTO() {
			return new LoanDocDataRequest();
		}

		@Override
		protected LoanDocDataRequest[] createDTOArray(int size) {
			return new LoanDocDataRequest[size];
		}

		@Override
		protected void setField(
			LoanDocDataRequest loanDocDataRequest, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "digisign")) {
				if (jsonParserFieldValue != null) {
					loanDocDataRequest.setDigisign(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "loanno")) {
				if (jsonParserFieldValue != null) {
					loanDocDataRequest.setLoanno((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "transId")) {
				if (jsonParserFieldValue != null) {
					loanDocDataRequest.setTransId((String)jsonParserFieldValue);
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