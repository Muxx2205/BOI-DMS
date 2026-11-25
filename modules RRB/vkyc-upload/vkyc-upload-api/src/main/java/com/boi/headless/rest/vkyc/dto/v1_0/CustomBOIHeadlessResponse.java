package com.boi.headless.rest.vkyc.dto.v1_0;

import com.liferay.portal.kernel.json.JSONObject;

import java.util.Map;

public class CustomBOIHeadlessResponse extends BOIHeadlessResponse {

	private final JSONObject jsonObject;

	public CustomBOIHeadlessResponse(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public Map<String, Object> getValues() {
		return (jsonObject == null) ? null : jsonObject.toMap();
	}

	@Override
	public String getResponseStatus() {
		if (jsonObject == null) {
			return "ERROR";
		}

		return jsonObject.has("error") ? "ERROR" : "SUCCESS";
	}
}