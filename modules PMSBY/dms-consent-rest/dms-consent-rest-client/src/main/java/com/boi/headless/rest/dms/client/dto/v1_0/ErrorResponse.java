package com.boi.headless.rest.dms.client.dto.v1_0;

import com.boi.headless.rest.dms.client.function.UnsafeSupplier;
import com.boi.headless.rest.dms.client.serdes.v1_0.ErrorResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class ErrorResponse implements Cloneable, Serializable {

	public static ErrorResponse toDTO(String json) {
		return ErrorResponseSerDes.toDTO(json);
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setReason(
		UnsafeSupplier<String, Exception> reasonUnsafeSupplier) {

		try {
			reason = reasonUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String reason;

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

	@Override
	public ErrorResponse clone() throws CloneNotSupportedException {
		return (ErrorResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ErrorResponse)) {
			return false;
		}

		ErrorResponse errorResponse = (ErrorResponse)object;

		return Objects.equals(toString(), errorResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ErrorResponseSerDes.toJSON(this);
	}

}