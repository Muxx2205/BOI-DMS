package com.vls.status.api.client.dto.v1_0;

import com.vls.status.api.client.function.UnsafeSupplier;
import com.vls.status.api.client.serdes.v1_0.LoanDocDataRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class LoanDocDataRequest implements Cloneable, Serializable {

	public static LoanDocDataRequest toDTO(String json) {
		return LoanDocDataRequestSerDes.toDTO(json);
	}

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

	public String getLoanno() {
		return loanno;
	}

	public void setLoanno(String loanno) {
		this.loanno = loanno;
	}

	public void setLoanno(
		UnsafeSupplier<String, Exception> loannoUnsafeSupplier) {

		try {
			loanno = loannoUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String loanno;

	public String getTransId() {
		return transId;
	}

	public void setTransId(String transId) {
		this.transId = transId;
	}

	public void setTransId(
		UnsafeSupplier<String, Exception> transIdUnsafeSupplier) {

		try {
			transId = transIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String transId;

	@Override
	public LoanDocDataRequest clone() throws CloneNotSupportedException {
		return (LoanDocDataRequest)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof LoanDocDataRequest)) {
			return false;
		}

		LoanDocDataRequest loanDocDataRequest = (LoanDocDataRequest)object;

		return Objects.equals(toString(), loanDocDataRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return LoanDocDataRequestSerDes.toJSON(this);
	}

}