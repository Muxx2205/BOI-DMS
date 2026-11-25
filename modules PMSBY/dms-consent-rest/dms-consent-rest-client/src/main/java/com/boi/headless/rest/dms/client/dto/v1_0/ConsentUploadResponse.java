package com.boi.headless.rest.dms.client.dto.v1_0;

import com.boi.headless.rest.dms.client.function.UnsafeSupplier;
import com.boi.headless.rest.dms.client.serdes.v1_0.ConsentUploadResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class ConsentUploadResponse implements Cloneable, Serializable {

	public static ConsentUploadResponse toDTO(String json) {
		return ConsentUploadResponseSerDes.toDTO(json);
	}

	public String getDocumentReferenceNo() {
		return documentReferenceNo;
	}

	public void setDocumentReferenceNo(String documentReferenceNo) {
		this.documentReferenceNo = documentReferenceNo;
	}

	public void setDocumentReferenceNo(
		UnsafeSupplier<String, Exception> documentReferenceNoUnsafeSupplier) {

		try {
			documentReferenceNo = documentReferenceNoUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String documentReferenceNo;

	public Long getEntryId() {
		return entryId;
	}

	public void setEntryId(Long entryId) {
		this.entryId = entryId;
	}

	public void setEntryId(
		UnsafeSupplier<Long, Exception> entryIdUnsafeSupplier) {

		try {
			entryId = entryIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long entryId;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessage(
		UnsafeSupplier<String, Exception> messageUnsafeSupplier) {

		try {
			message = messageUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String message;

	public Status getStatus() {
		return status;
	}

	public String getStatusAsString() {
		if (status == null) {
			return null;
		}

		return status.toString();
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStatus(
		UnsafeSupplier<Status, Exception> statusUnsafeSupplier) {

		try {
			status = statusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Status status;

	@Override
	public ConsentUploadResponse clone() throws CloneNotSupportedException {
		return (ConsentUploadResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ConsentUploadResponse)) {
			return false;
		}

		ConsentUploadResponse consentUploadResponse =
			(ConsentUploadResponse)object;

		return Objects.equals(toString(), consentUploadResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ConsentUploadResponseSerDes.toJSON(this);
	}

	public static enum Status {

		SUCCESS("Success"), FAILED("Failed");

		public static Status create(String value) {
			for (Status status : values()) {
				if (Objects.equals(status.getValue(), value) ||
					Objects.equals(status.name(), value)) {

					return status;
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

		private Status(String value) {
			_value = value;
		}

		private final String _value;

	}

}