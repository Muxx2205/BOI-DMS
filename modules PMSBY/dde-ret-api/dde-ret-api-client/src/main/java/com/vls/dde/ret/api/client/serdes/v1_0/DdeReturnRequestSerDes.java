package com.vls.dde.ret.api.client.serdes.v1_0;

import com.vls.dde.ret.api.client.dto.v1_0.DdeReturnRequest;
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
public class DdeReturnRequestSerDes {

	public static DdeReturnRequest toDTO(String json) {
		DdeReturnRequestJSONParser ddeReturnRequestJSONParser =
			new DdeReturnRequestJSONParser();

		return ddeReturnRequestJSONParser.parseToDTO(json);
	}

	public static DdeReturnRequest[] toDTOs(String json) {
		DdeReturnRequestJSONParser ddeReturnRequestJSONParser =
			new DdeReturnRequestJSONParser();

		return ddeReturnRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DdeReturnRequest ddeReturnRequest) {
		if (ddeReturnRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (ddeReturnRequest.getBranch() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getBranch()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getBranchCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getBranchCode()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getDocId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"docId\": ");

			sb.append(ddeReturnRequest.getDocId());
		}

		if (ddeReturnRequest.getMatchPercentage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"matchPercentage\": ");

			sb.append(ddeReturnRequest.getMatchPercentage());
		}

		if (ddeReturnRequest.getNbgCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nbgCode\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getNbgCode()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getReltocntrct() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"reltocntrct\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getReltocntrct()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getSignatoryName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signatoryName\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getSignatoryName()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getSignedDocData() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signedDocData\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getSignedDocData()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getSignedName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signedName\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getSignedName()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getUin() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"uin\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getUin()));

			sb.append("\"");
		}

		if (ddeReturnRequest.getZone() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"zone\": ");

			sb.append("\"");

			sb.append(_escape(ddeReturnRequest.getZone()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DdeReturnRequestJSONParser ddeReturnRequestJSONParser =
			new DdeReturnRequestJSONParser();

		return ddeReturnRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(DdeReturnRequest ddeReturnRequest) {
		if (ddeReturnRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (ddeReturnRequest.getBranch() == null) {
			map.put("branch", null);
		}
		else {
			map.put("branch", String.valueOf(ddeReturnRequest.getBranch()));
		}

		if (ddeReturnRequest.getBranchCode() == null) {
			map.put("branchCode", null);
		}
		else {
			map.put(
				"branchCode", String.valueOf(ddeReturnRequest.getBranchCode()));
		}

		if (ddeReturnRequest.getDocId() == null) {
			map.put("docId", null);
		}
		else {
			map.put("docId", String.valueOf(ddeReturnRequest.getDocId()));
		}

		if (ddeReturnRequest.getMatchPercentage() == null) {
			map.put("matchPercentage", null);
		}
		else {
			map.put(
				"matchPercentage",
				String.valueOf(ddeReturnRequest.getMatchPercentage()));
		}

		if (ddeReturnRequest.getNbgCode() == null) {
			map.put("nbgCode", null);
		}
		else {
			map.put("nbgCode", String.valueOf(ddeReturnRequest.getNbgCode()));
		}

		if (ddeReturnRequest.getReltocntrct() == null) {
			map.put("reltocntrct", null);
		}
		else {
			map.put(
				"reltocntrct",
				String.valueOf(ddeReturnRequest.getReltocntrct()));
		}

		if (ddeReturnRequest.getSignatoryName() == null) {
			map.put("signatoryName", null);
		}
		else {
			map.put(
				"signatoryName",
				String.valueOf(ddeReturnRequest.getSignatoryName()));
		}

		if (ddeReturnRequest.getSignedDocData() == null) {
			map.put("signedDocData", null);
		}
		else {
			map.put(
				"signedDocData",
				String.valueOf(ddeReturnRequest.getSignedDocData()));
		}

		if (ddeReturnRequest.getSignedName() == null) {
			map.put("signedName", null);
		}
		else {
			map.put(
				"signedName", String.valueOf(ddeReturnRequest.getSignedName()));
		}

		if (ddeReturnRequest.getUin() == null) {
			map.put("uin", null);
		}
		else {
			map.put("uin", String.valueOf(ddeReturnRequest.getUin()));
		}

		if (ddeReturnRequest.getZone() == null) {
			map.put("zone", null);
		}
		else {
			map.put("zone", String.valueOf(ddeReturnRequest.getZone()));
		}

		return map;
	}

	public static class DdeReturnRequestJSONParser
		extends BaseJSONParser<DdeReturnRequest> {

		@Override
		protected DdeReturnRequest createDTO() {
			return new DdeReturnRequest();
		}

		@Override
		protected DdeReturnRequest[] createDTOArray(int size) {
			return new DdeReturnRequest[size];
		}

		@Override
		protected void setField(
			DdeReturnRequest ddeReturnRequest, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "branch")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setBranch((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "branchCode")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setBranchCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "docId")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setDocId(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "matchPercentage")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setMatchPercentage(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "nbgCode")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setNbgCode((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "reltocntrct")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setReltocntrct(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "signatoryName")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setSignatoryName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "signedDocData")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setSignedDocData(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "signedName")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setSignedName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "uin")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setUin((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "zone")) {
				if (jsonParserFieldValue != null) {
					ddeReturnRequest.setZone((String)jsonParserFieldValue);
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