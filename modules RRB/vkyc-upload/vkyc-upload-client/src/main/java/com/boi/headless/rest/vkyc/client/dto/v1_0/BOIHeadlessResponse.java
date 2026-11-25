/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.boi.headless.rest.vkyc.client.dto.v1_0;

import com.boi.headless.rest.vkyc.client.function.UnsafeSupplier;
import com.boi.headless.rest.vkyc.client.serdes.v1_0.BOIHeadlessResponseSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

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

	public Map<String, ?> getValues() {
		return values;
	}

	public void setValues(Map<String, ?> values) {
		this.values = values;
	}

	public void setValues(
		UnsafeSupplier<Map<String, ?>, Exception> valuesUnsafeSupplier) {

		try {
			values = valuesUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Map<String, ?> values;

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
// LIFERAY-REST-BUILDER-HASH:-915187981