package com.boi.headless.rest.consent.client.dto.v1_0;

import com.boi.headless.rest.consent.client.function.UnsafeSupplier;
import com.boi.headless.rest.consent.client.serdes.v1_0.StatusCheckResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class StatusCheckResponse implements Cloneable, Serializable {

	public static StatusCheckResponse toDTO(String json) {
		return StatusCheckResponseSerDes.toDTO(json);
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

	public Boolean getDocumentVerified() {
		return documentVerified;
	}

	public void setDocumentVerified(Boolean documentVerified) {
		this.documentVerified = documentVerified;
	}

	public void setDocumentVerified(
		UnsafeSupplier<Boolean, Exception> documentVerifiedUnsafeSupplier) {

		try {
			documentVerified = documentVerifiedUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean documentVerified;

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

	@Override
	public StatusCheckResponse clone() throws CloneNotSupportedException {
		return (StatusCheckResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof StatusCheckResponse)) {
			return false;
		}

		StatusCheckResponse statusCheckResponse = (StatusCheckResponse)object;

		return Objects.equals(toString(), statusCheckResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return StatusCheckResponseSerDes.toJSON(this);
	}

}