package com.boi.headless.rest.jansuraksha.client.dto.v1_0;

import com.boi.headless.rest.jansuraksha.client.function.UnsafeSupplier;
import com.boi.headless.rest.jansuraksha.client.serdes.v1_0.JanSurakshaResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class JanSurakshaResponse implements Cloneable, Serializable {

	public static JanSurakshaResponse toDTO(String json) {
		return JanSurakshaResponseSerDes.toDTO(json);
	}

	public Integer getResponseId() {
		return responseId;
	}

	public void setResponseId(Integer responseId) {
		this.responseId = responseId;
	}

	public void setResponseId(
		UnsafeSupplier<Integer, Exception> responseIdUnsafeSupplier) {

		try {
			responseId = responseIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer responseId;

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public void setResponseStatus(
		UnsafeSupplier<String, Exception> responseStatusUnsafeSupplier) {

		try {
			responseStatus = responseStatusUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String responseStatus;

	@Override
	public JanSurakshaResponse clone() throws CloneNotSupportedException {
		return (JanSurakshaResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof JanSurakshaResponse)) {
			return false;
		}

		JanSurakshaResponse janSurakshaResponse = (JanSurakshaResponse)object;

		return Objects.equals(toString(), janSurakshaResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return JanSurakshaResponseSerDes.toJSON(this);
	}

}