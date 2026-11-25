package com.vls.dde.ret.api.client.dto.v1_0;

import com.vls.dde.ret.api.client.function.UnsafeSupplier;
import com.vls.dde.ret.api.client.serdes.v1_0.DdeReturnResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class DdeReturnResponse implements Cloneable, Serializable {

	public static DdeReturnResponse toDTO(String json) {
		return DdeReturnResponseSerDes.toDTO(json);
	}

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

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public void setStatusCode(
		UnsafeSupplier<String, Exception> statusCodeUnsafeSupplier) {

		try {
			statusCode = statusCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String statusCode;

	public String getTxnID() {
		return txnID;
	}

	public void setTxnID(String txnID) {
		this.txnID = txnID;
	}

	public void setTxnID(
		UnsafeSupplier<String, Exception> txnIDUnsafeSupplier) {

		try {
			txnID = txnIDUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String txnID;

	@Override
	public DdeReturnResponse clone() throws CloneNotSupportedException {
		return (DdeReturnResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DdeReturnResponse)) {
			return false;
		}

		DdeReturnResponse ddeReturnResponse = (DdeReturnResponse)object;

		return Objects.equals(toString(), ddeReturnResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DdeReturnResponseSerDes.toJSON(this);
	}

}