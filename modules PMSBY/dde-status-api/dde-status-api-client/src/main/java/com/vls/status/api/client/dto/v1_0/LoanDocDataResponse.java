package com.vls.status.api.client.dto.v1_0;

import com.vls.status.api.client.function.UnsafeSupplier;
import com.vls.status.api.client.serdes.v1_0.LoanDocDataResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class LoanDocDataResponse implements Cloneable, Serializable {

	public static LoanDocDataResponse toDTO(String json) {
		return LoanDocDataResponseSerDes.toDTO(json);
	}

	public String getCipherText() {
		return cipherText;
	}

	public void setCipherText(String cipherText) {
		this.cipherText = cipherText;
	}

	public void setCipherText(
		UnsafeSupplier<String, Exception> cipherTextUnsafeSupplier) {

		try {
			cipherText = cipherTextUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String cipherText;

	public String getDigisign() {
		return digisign;
	}

	public void setDigisign(String digisign) {
		this.digisign = digisign;
	}

	public void setDigisign(
		UnsafeSupplier<String, Exception> digisignUnsafeSupplier) {

		try {
			digisign = digisignUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String digisign;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setSessionId(
		UnsafeSupplier<String, Exception> sessionIdUnsafeSupplier) {

		try {
			sessionId = sessionIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String sessionId;

	@Override
	public LoanDocDataResponse clone() throws CloneNotSupportedException {
		return (LoanDocDataResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof LoanDocDataResponse)) {
			return false;
		}

		LoanDocDataResponse loanDocDataResponse = (LoanDocDataResponse)object;

		return Objects.equals(toString(), loanDocDataResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return LoanDocDataResponseSerDes.toJSON(this);
	}

}