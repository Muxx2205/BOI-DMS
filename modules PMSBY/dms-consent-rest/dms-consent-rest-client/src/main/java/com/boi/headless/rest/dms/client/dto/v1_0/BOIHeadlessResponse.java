package com.boi.headless.rest.dms.client.dto.v1_0;

import com.boi.headless.rest.dms.client.function.UnsafeSupplier;
import com.boi.headless.rest.dms.client.serdes.v1_0.BOIHeadlessResponseSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class BOIHeadlessResponse implements Cloneable, Serializable {

	public static BOIHeadlessResponse toDTO(String json) {
		return BOIHeadlessResponseSerDes.toDTO(json);
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
	public BOIHeadlessResponse clone() throws CloneNotSupportedException {
		return (BOIHeadlessResponse)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof BOIHeadlessResponse)) {
			return false;
		}

		BOIHeadlessResponse boiHeadlessResponse = (BOIHeadlessResponse)object;

		return Objects.equals(toString(), boiHeadlessResponse.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return BOIHeadlessResponseSerDes.toJSON(this);
	}

}