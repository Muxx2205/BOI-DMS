package com.vls.status.api.client.dto.v1_0;

import com.vls.status.api.client.function.UnsafeSupplier;
import com.vls.status.api.client.serdes.v1_0.DdePushStatusResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class DdePushStatusResponse implements Cloneable, Serializable {

	public static DdePushStatusResponse toDTO(String json) {
		return DdePushStatusResponseSerDes.toDTO(json);
	}

	public String getDocDate() {
		return docDate;
	}

	public void setDocDate(String docDate) {
		this.docDate = docDate;
	}

	public void setDocDate(
		UnsafeSupplier<String, Exception> docDateUnsafeSupplier) {

		try {
			docDate = docDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String docDate;

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

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public void setStatusMsg(
		UnsafeSupplier<String, Exception> statusMsgUnsafeSupplier) {

		try {
			statusMsg = statusMsgUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String statusMsg;

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
	public DdePushStatusResponse clone() throws CloneNotSupportedException {
		return (DdePushStatusResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DdePushStatusResponse)) {
			return false;
		}

		DdePushStatusResponse ddePushStatusResponse =
			(DdePushStatusResponse)object;

		return Objects.equals(toString(), ddePushStatusResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DdePushStatusResponseSerDes.toJSON(this);
	}

}