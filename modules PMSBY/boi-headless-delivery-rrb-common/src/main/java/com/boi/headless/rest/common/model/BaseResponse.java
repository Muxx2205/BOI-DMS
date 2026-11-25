package com.boi.headless.rest.common.model;

import javax.ws.rs.core.Response;

public class BaseResponse{

	private Response.Status responseCode;
	private String customMessage;
	private BaseModel data;
	public Response.Status getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(Response.Status responseCode) {
		this.responseCode = responseCode;
	}
	public String getCustomMessage() {
		return customMessage;
	}
	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}
	public BaseModel getData() {
		return data;
	}
	public void setData(BaseModel data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "BaseResponse [responseCode=" + responseCode + ", customMessage=" + customMessage + ", data=" + data
				+ "]";
	}
	
	
	 
	

}
