package com.boi.headless.rest.consent.client.dto.v1_0;

import com.boi.headless.rest.consent.client.function.UnsafeSupplier;
import com.boi.headless.rest.consent.client.serdes.v1_0.URNUpdateRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class URNUpdateRequest implements Cloneable, Serializable {

	public static URNUpdateRequest toDTO(String json) {
		return URNUpdateRequestSerDes.toDTO(json);
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStatus(
		UnsafeSupplier<String, Exception> statusUnsafeSupplier) {

		try {
			status = statusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String status;

	public String getTransDate() {
		return transDate;
	}

	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}

	public void setTransDate(
		UnsafeSupplier<String, Exception> transDateUnsafeSupplier) {

		try {
			transDate = transDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String transDate;

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	public void setUrn(UnsafeSupplier<String, Exception> urnUnsafeSupplier) {
		try {
			urn = urnUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String urn;

	@Override
	public URNUpdateRequest clone() throws CloneNotSupportedException {
		return (URNUpdateRequest)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof URNUpdateRequest)) {
			return false;
		}

		URNUpdateRequest urnUpdateRequest = (URNUpdateRequest)object;

		return Objects.equals(toString(), urnUpdateRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return URNUpdateRequestSerDes.toJSON(this);
	}

}