package com.boi.headless.rest.consent.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Page;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class CustomBOIHeadlessResponseAsList extends BOIHeadlessResponse {

    private static final long serialVersionUID = 8632989910729714104L;

    private long pageSize;
    private long page = 1;
    private long totalCount = 0;
    private long lastPage = 1;
    private JSONArray items = JSONFactoryUtil.createJSONArray();
    private String downloadUrl = StringPool.BLANK;
    private Set<String> statusList = new LinkedHashSet<>();

    public CustomBOIHeadlessResponseAsList(
            Page<ObjectEntry> page,
            boolean isMaker,
            String downloadUrl,
            Set<String> statusList) {
        if (Validator.isNull(page)) {
            return;
        }
        try {
            this.downloadUrl = Validator.isNotNull(downloadUrl) ? downloadUrl : "not-available";
            this.statusList = new LinkedHashSet<>(Validator.isNotNull(statusList) ? statusList : Collections.emptySet());
            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.lastPage = page.getLastPage();

            for (ObjectEntry entry : page.getItems()) {
                items.put(buildItemJson(entry, isMaker));
            }
        } catch (Exception e) {
            items.put("error: " + e.getMessage());
        }
    }

    private JSONObject buildItemJson(ObjectEntry entry, boolean isMaker) {
        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        entry.getProperties().forEach(jsonObject::put);

        int workflowStatus = entry.getStatus().getCode();
        String customStatusByHandler = (String) entry.getPropertyValue("customStatusByHandler");
        
        int stage = 0;
        Object stageObj = entry.getPropertyValue("stage");
        if (stageObj != null) {
            try {
                stage = Integer.parseInt(String.valueOf(stageObj));
            } catch (Exception ignored) {
            }
        }
        
        String redirectTo = (isMaker && (stage == 0 || stage == 1)) ? "scanner" : "detailed";
        jsonObject.put("redirectTo", redirectTo);
        jsonObject.put("status", resolveStatusLabel(stage, workflowStatus, customStatusByHandler));
        jsonObject.put("createdBy", entry.getCreator().getName());
        jsonObject.put("createdById", entry.getCreator().getId());
        jsonObject.put("dateModified", entry.getDateModified());
        jsonObject.put("dateCreated", entry.getDateCreated());
        jsonObject.put("id", entry.getId());

        return jsonObject;
    }

    private String resolveStatusLabel(int stage, int workflowStatus, String customStatusByHandler) {
        switch (stage) {
            case 0: return "To be Initiated";
            case 1: return "Pending with Branch Maker";
            case 2: return "Pending with Branch Checker";
            case 3: return "Approved";
            case 4: return "Deleted";
        }
        if (!Validator.isBlank(customStatusByHandler) && workflowStatus == WorkflowConstants.STATUS_APPROVED) {
            return WorkflowConstants.LABEL_APPROVED;
        }
        if (!Validator.isBlank(customStatusByHandler)) {
            return customStatusByHandler;
        }
        if (workflowStatus == WorkflowConstants.STATUS_APPROVED) {
            return WorkflowConstants.LABEL_APPROVED;
        }
        if (workflowStatus == WorkflowConstants.STATUS_PENDING) {
            return WorkflowConstants.LABEL_PENDING;
        }
        return WorkflowConstants.getStatusLabel(workflowStatus);
    }

    // Getters
    public long getPageSize() { return pageSize; }
    public long getPage() { return page; }
    public long getTotalCount() { return totalCount; }
    public long getLastPage() { return lastPage; }
    public JSONArray getItems() { return items; }
    public String getDownloadUrl() { return downloadUrl; }
    public Set<String> getStatusList() { return statusList; }

    @Override
    public String toString() {
        return String.format("CustomBOIHeadlessResponseAsList [pageSize=%d, page=%d, totalCount=%d, lastPage=%d, items=%s, downloadUrl=%s, statusList=%s]",
                pageSize, page, totalCount, lastPage, items.toJSONString(), downloadUrl, statusList);
    }
}