package com.boi.headless.rest.jansuraksha.client.serdes.v1_0;

import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaUploadRequest;
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
public class JanSurakshaUploadRequestSerDes {

	public static JanSurakshaUploadRequest toDTO(String json) {
		JanSurakshaUploadRequestJSONParser janSurakshaUploadRequestJSONParser =
			new JanSurakshaUploadRequestJSONParser();

		return janSurakshaUploadRequestJSONParser.parseToDTO(json);
	}

	public static JanSurakshaUploadRequest[] toDTOs(String json) {
		JanSurakshaUploadRequestJSONParser janSurakshaUploadRequestJSONParser =
			new JanSurakshaUploadRequestJSONParser();

		return janSurakshaUploadRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		JanSurakshaUploadRequest janSurakshaUploadRequest) {

		if (janSurakshaUploadRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (janSurakshaUploadRequest.getDocument() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"document\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaUploadRequest.getDocument()));

			sb.append("\"");
		}

		if (janSurakshaUploadRequest.getDocumentFileName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileName\": ");

			sb.append("\"");

			sb.append(_escape(janSurakshaUploadRequest.getDocumentFileName()));

			sb.append("\"");
		}

		if (janSurakshaUploadRequest.getDocumentFileType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileType\": ");

			sb.append("\"");

			sb.append(janSurakshaUploadRequest.getDocumentFileType());

			sb.append("\"");
		}

		if (janSurakshaUploadRequest.getMetadata() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"metadata\": ");

			sb.append(String.valueOf(janSurakshaUploadRequest.getMetadata()));
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		JanSurakshaUploadRequestJSONParser janSurakshaUploadRequestJSONParser =
			new JanSurakshaUploadRequestJSONParser();

		return janSurakshaUploadRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		JanSurakshaUploadRequest janSurakshaUploadRequest) {

		if (janSurakshaUploadRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (janSurakshaUploadRequest.getDocument() == null) {
			map.put("document", null);
		}
		else {
			map.put(
				"document",
				String.valueOf(janSurakshaUploadRequest.getDocument()));
		}

		if (janSurakshaUploadRequest.getDocumentFileName() == null) {
			map.put("documentFileName", null);
		}
		else {
			map.put(
				"documentFileName",
				String.valueOf(janSurakshaUploadRequest.getDocumentFileName()));
		}

		if (janSurakshaUploadRequest.getDocumentFileType() == null) {
			map.put("documentFileType", null);
		}
		else {
			map.put(
				"documentFileType",
				String.valueOf(janSurakshaUploadRequest.getDocumentFileType()));
		}

		if (janSurakshaUploadRequest.getMetadata() == null) {
			map.put("metadata", null);
		}
		else {
			map.put(
				"metadata",
				String.valueOf(janSurakshaUploadRequest.getMetadata()));
		}

		return map;
	}

	public static class JanSurakshaUploadRequestJSONParser
		extends BaseJSONParser<JanSurakshaUploadRequest> {

		@Override
		protected JanSurakshaUploadRequest createDTO() {
			return new JanSurakshaUploadRequest();
		}

		@Override
		protected JanSurakshaUploadRequest[] createDTOArray(int size) {
			return new JanSurakshaUploadRequest[size];
		}

		@Override
		protected void setField(
			JanSurakshaUploadRequest janSurakshaUploadRequest,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "document")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadRequest.setDocument(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "documentFileName")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadRequest.setDocumentFileName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "documentFileType")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadRequest.setDocumentFileType(
						JanSurakshaUploadRequest.DocumentFileType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "metadata")) {
				if (jsonParserFieldValue != null) {
					janSurakshaUploadRequest.setMetadata(
						JanSurakshaMetadataSerDes.toDTO(
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