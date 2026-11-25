package com.boi.headless.rest.consent.client.dto.v1_0;

import com.boi.headless.rest.consent.client.function.UnsafeSupplier;
import com.boi.headless.rest.consent.client.serdes.v1_0.ConsentResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class ConsentResponse implements Cloneable, Serializable {

	public static ConsentResponse toDTO(String json) {
		return ConsentResponseSerDes.toDTO(json);
	}

	public String getCbsReferenceNumber() {
		return cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(String cbsReferenceNumber) {
		this.cbsReferenceNumber = cbsReferenceNumber;
	}

	public void setCbsReferenceNumber(
		UnsafeSupplier<String, Exception> cbsReferenceNumberUnsafeSupplier) {

		try {
			cbsReferenceNumber = cbsReferenceNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String cbsReferenceNumber;

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

	public Integer getStage() {
		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;
	}

	public void setStage(
		UnsafeSupplier<Integer, Exception> stageUnsafeSupplier) {

		try {
			stage = stageUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer stage;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public void setSuccess(
		UnsafeSupplier<Boolean, Exception> successUnsafeSupplier) {

		try {
			success = successUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean success;

	@Override
	public ConsentResponse clone() throws CloneNotSupportedException {
		return (ConsentResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ConsentResponse)) {
			return false;
		}

		ConsentResponse consentResponse = (ConsentResponse)object;

		return Objects.equals(toString(), consentResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ConsentResponseSerDes.toJSON(this);
	}

}