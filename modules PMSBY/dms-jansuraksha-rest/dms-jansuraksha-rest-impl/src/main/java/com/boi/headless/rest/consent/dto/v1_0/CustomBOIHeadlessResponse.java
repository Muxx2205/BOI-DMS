package com.boi.headless.rest.consent.dto.v1_0;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;

import javax.ws.rs.core.Response.Status;

public class CustomBOIHeadlessResponse extends BOIHeadlessResponse {
    
    private static final long serialVersionUID = -5577572936277163653L;
    
    private JSONObject data = JSONFactoryUtil.createJSONObject();

    public CustomBOIHeadlessResponse(JSONObject data, Status status) {
        try {
            super.setResponseId(status.getStatusCode());
            this.data = data;
        } catch (Exception e) {
        }
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomBOIHeadlessResponse [data=" + data + "]";
    }
}
