package com.boi.headless.rest.jansuraksha.client.dto.v1_0;

import com.boi.headless.rest.jansuraksha.client.function.UnsafeSupplier;
import com.boi.headless.rest.jansuraksha.client.serdes.v1_0.JanSurakshaUploadRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class JanSurakshaUploadRequest implements Cloneable, Serializable {

	public static JanSurakshaUploadRequest toDTO(String json) {
		return JanSurakshaUploadRequestSerDes.toDTO(json);
	}

	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public void setDocument(
		UnsafeSupplier<String, Exception> documentUnsafeSupplier) {

		try {
			document = documentUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String document;

	public String getDocumentFileName() {
		return documentFileName;
	}

	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}

	public void setDocumentFileName(
		UnsafeSupplier<String, Exception> documentFileNameUnsafeSupplier) {

		try {
			documentFileName = documentFileNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String documentFileName;

	public DocumentFileType getDocumentFileType() {
		return documentFileType;
	}

	public String getDocumentFileTypeAsString() {
		if (documentFileType == null) {
			return null;
		}

		return documentFileType.toString();
	}

	public void setDocumentFileType(DocumentFileType documentFileType) {
		this.documentFileType = documentFileType;
	}

	public void setDocumentFileType(
		UnsafeSupplier<DocumentFileType, Exception>
			documentFileTypeUnsafeSupplier) {

		try {
			documentFileType = documentFileTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected DocumentFileType documentFileType;

	public JanSurakshaMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(JanSurakshaMetadata metadata) {
		this.metadata = metadata;
	}

	public void setMetadata(
		UnsafeSupplier<JanSurakshaMetadata, Exception> metadataUnsafeSupplier) {

		try {
			metadata = metadataUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected JanSurakshaMetadata metadata;

	@Override
	public JanSurakshaUploadRequest clone() throws CloneNotSupportedException {
		return (JanSurakshaUploadRequest)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof JanSurakshaUploadRequest)) {
			return false;
		}

		JanSurakshaUploadRequest janSurakshaUploadRequest =
			(JanSurakshaUploadRequest)object;

		return Objects.equals(toString(), janSurakshaUploadRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return JanSurakshaUploadRequestSerDes.toJSON(this);
	}

	public static enum DocumentFileType {

		PDF("pdf"), JPG("jpg"), PNG("png");

		public static DocumentFileType create(String value) {
			for (DocumentFileType documentFileType : values()) {
				if (Objects.equals(documentFileType.getValue(), value) ||
					Objects.equals(documentFileType.name(), value)) {

					return documentFileType;
				}
			}

			return null;
		}

		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private DocumentFileType(String value) {
			_value = value;
		}

		private final String _value;

	}

}