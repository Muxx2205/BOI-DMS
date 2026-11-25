package com.boi.headless.rest.vkyc.dto.v1_0;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Map;

public class CustomBOIHeadlessResponseAsList extends BOIHeadlessResponse {

	private final JSONObject jsonObject;

	public CustomBOIHeadlessResponseAsList(Page<?> page, Pagination pagination) {
		JSONObject obj = JSONFactoryUtil.createJSONObject();
		JSONArray items = JSONFactoryUtil.createJSONArray();

		if ((page != null) && (page.getItems() != null)) {
			for (Object item : page.getItems()) {
				items.put(item);
			}
		}

		obj.put("items", items);

		if (page != null) {
			obj.put("totalCount", page.getTotalCount());
			if (pagination != null && pagination.getPageSize() > 0) {
				obj.put("lastPage", Math.max(1, (int) Math.ceil((double) page.getTotalCount() / pagination.getPageSize())));
			}
		}

		if (pagination != null) {
			obj.put("page", pagination.getPage());
			obj.put("pageSize", pagination.getPageSize());
		}

		this.jsonObject = obj;
	}

	@Override
	public Map<String, Object> getValues() {
		return null; 
	}

	@Override
	public String getResponseStatus() {
		return "SUCCESS";
	}
	
	public JSONObject getRawJson() {
		return jsonObject;
	}
}