package com.boi.headless.rest.common.model;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;

public class WorkflowLogs {

	private JSONArray items;

	public WorkflowLogs(Page<ObjectEntry> page) {
		items = JSONFactoryUtil.createJSONArray();
		if (Validator.isNotNull(page)) {
			page.getItems().forEach(x -> {
				JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
				x.getProperties().forEach((c, d) -> {
					itemObjects.put(c, d);
				});
				itemObjects.put("status", x.getStatus().getLabel());
				itemObjects.put("createdBy", x.getCreator().getName());
				itemObjects.put("createdById", x.getCreator().getId());
				itemObjects.put("dateModified", x.getDateModified());
				itemObjects.put("dateCreated", x.getDateCreated());
				itemObjects.put("id", x.getId());
				items.put(itemObjects);
			});
		}

	}

	public WorkflowLogs() {

	}

	public JSONArray getArray(Page<ObjectEntry> page) {
		JSONArray items = JSONFactoryUtil.createJSONArray();
		if (Validator.isNotNull(page)) {
			page.getItems().forEach(x -> {
				JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
				x.getProperties().forEach((c, d) -> {
					itemObjects.put(c, d);
				});
				itemObjects.put("status", x.getStatus().getLabel());
				itemObjects.put("createdBy", x.getCreator().getName());
				itemObjects.put("createdById", x.getCreator().getId());
				itemObjects.put("dateModified", x.getDateModified());
				itemObjects.put("dateCreated", x.getDateCreated());
				itemObjects.put("id", x.getId());
				items.put(itemObjects);
			});
		}
		return items;
	}

	public int getCurrentStatus(Page<ObjectEntry> page) {

		int maxStatus = 0;

		if (Validator.isNotNull(page)) {

			for (ObjectEntry entry : page.getItems()) {

				Object currentStatusObj = entry.getProperties().get("currentStatus");

				if (currentStatusObj != null) {

					int currentStatus = Integer.parseInt(String.valueOf(currentStatusObj));

					if (currentStatus > maxStatus) {

						maxStatus = currentStatus;
					}
				}
			}
		}

		return maxStatus;
	}

	@Override
	public String toString() {
		return "CustomerResponse [ items=" + items.toString() + "]";
	}

}