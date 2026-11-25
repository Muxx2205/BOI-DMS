package com.boi.headless.rest.dms.client.serdes.v1_0;

import com.boi.headless.rest.dms.client.dto.v1_0.Base64ConsentUploadRequest;
import com.boi.headless.rest.dms.client.json.BaseJSONParser;

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
public class Base64ConsentUploadRequestSerDes {

	public static Base64ConsentUploadRequest toDTO(String json) {
		Base64ConsentUploadRequestJSONParser
			base64ConsentUploadRequestJSONParser =
				new Base64ConsentUploadRequestJSONParser();

		return base64ConsentUploadRequestJSONParser.parseToDTO(json);
	}

	public static Base64ConsentUploadRequest[] toDTOs(String json) {
		Base64ConsentUploadRequestJSONParser
			base64ConsentUploadRequestJSONParser =
				new Base64ConsentUploadRequestJSONParser();

		return base64ConsentUploadRequestJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		Base64ConsentUploadRequest base64ConsentUploadRequest) {

		if (base64ConsentUploadRequest == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (base64ConsentUploadRequest.getDocument() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"document\": ");

			sb.append("\"");

			sb.append(_escape(base64ConsentUploadRequest.getDocument()));

			sb.append("\"");
		}

		if (base64ConsentUploadRequest.getDocumentFileName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileName\": ");

			sb.append("\"");

			sb.append(
				_escape(base64ConsentUploadRequest.getDocumentFileName()));

			sb.append("\"");
		}

		if (base64ConsentUploadRequest.getDocumentFileType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentFileType\": ");

			sb.append("\"");

			sb.append(base64ConsentUploadRequest.getDocumentFileType());

			sb.append("\"");
		}

		if (base64ConsentUploadRequest.getMetadata() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"metadata\": ");

			sb.append(String.valueOf(base64ConsentUploadRequest.getMetadata()));
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		Base64ConsentUploadRequestJSONParser
			base64ConsentUploadRequestJSONParser =
				new Base64ConsentUploadRequestJSONParser();

		return base64ConsentUploadRequestJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		Base64ConsentUploadRequest base64ConsentUploadRequest) {

		if (base64ConsentUploadRequest == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (base64ConsentUploadRequest.getDocument() == null) {
			map.put("document", null);
		}
		else {
			map.put(
				"document",
				String.valueOf(base64ConsentUploadRequest.getDocument()));
		}

		if (base64ConsentUploadRequest.getDocumentFileName() == null) {
			map.put("documentFileName", null);
		}
		else {
			map.put(
				"documentFileName",
				String.valueOf(
					base64ConsentUploadRequest.getDocumentFileName()));
		}

		if (base64ConsentUploadRequest.getDocumentFileType() == null) {
			map.put("documentFileType", null);
		}
		else {
			map.put(
				"documentFileType",
				String.valueOf(
					base64ConsentUploadRequest.getDocumentFileType()));
		}

		if (base64ConsentUploadRequest.getMetadata() == null) {
			map.put("metadata", null);
		}
		else {
			map.put(
				"metadata",
				String.valueOf(base64ConsentUploadRequest.getMetadata()));
		}

		return map;
	}

	public static class Base64ConsentUploadRequestJSONParser
		extends BaseJSONParser<Base64ConsentUploadRequest> {

		@Override
		protected Base64ConsentUploadRequest createDTO() {
			return new Base64ConsentUploadRequest();
		}

		@Override
		protected Base64ConsentUploadRequest[] createDTOArray(int size) {
			return new Base64ConsentUploadRequest[size];
		}

		@Override
		protected void setField(
			Base64ConsentUploadRequest base64ConsentUploadRequest,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "document")) {
				if (jsonParserFieldValue != null) {
					base64ConsentUploadRequest.setDocument(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "documentFileName")) {
				if (jsonParserFieldValue != null) {
					base64ConsentUploadRequest.setDocumentFileName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "documentFileType")) {
				if (jsonParserFieldValue != null) {
					base64ConsentUploadRequest.setDocumentFileType(
						Base64ConsentUploadRequest.DocumentFileType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "metadata")) {
				if (jsonParserFieldValue != null) {
					base64ConsentUploadRequest.setMetadata(
						ConsentMetadataSerDes.toDTO(
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