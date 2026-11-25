package com.vls.status.api.client.dto.v1_0;

import com.vls.status.api.client.function.UnsafeSupplier;
import com.vls.status.api.client.serdes.v1_0.DdePushStatusRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class DdePushStatusRequest implements Cloneable, Serializable {

	public static DdePushStatusRequest toDTO(String json) {
		return DdePushStatusRequestSerDes.toDTO(json);
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

	public String getSnctnno() {
		return snctnno;
	}

	public void setSnctnno(String snctnno) {
		this.snctnno = snctnno;
	}

	public void setSnctnno(
		UnsafeSupplier<String, Exception> snctnnoUnsafeSupplier) {

		try {
			snctnno = snctnnoUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String snctnno;

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
	public DdePushStatusRequest clone() throws CloneNotSupportedException {
		return (DdePushStatusRequest)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DdePushStatusRequest)) {
			return false;
		}

		DdePushStatusRequest ddePushStatusRequest =
			(DdePushStatusRequest)object;

		return Objects.equals(toString(), ddePushStatusRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DdePushStatusRequestSerDes.toJSON(this);
	}

}