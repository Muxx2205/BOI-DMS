package com.boi.headless.web.customeraccount.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Page;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RequestAccountOpeningResponseAsList extends RequestAccountOpeningResponse {
   private static final long serialVersionUID = 3174131405275177876L;
   private static final Log log = LogFactoryUtil.getLog(RequestAccountOpeningResponseAsList.class);
   private long pageSize;
   private long page = 1L;
   private long totalCount = 0L;
   private long lastPage = 1L;
   private final JSONArray items = JSONFactoryUtil.createJSONArray();
   private String downloadUrl;
   private Set<String> statusList;

   public RequestAccountOpeningResponseAsList(Page<ObjectEntry> page, boolean isMaker, String downloadUrl, Set<String> statusList) {
      this.downloadUrl = Validator.isNotNull(downloadUrl) ? downloadUrl : "";
      this.statusList = new LinkedHashSet(Validator.isNotNull(statusList) ? statusList : Collections.emptySet());
      if (!Validator.isNull(page)) {
         if (isMaker) {
            this.statusList.add("draft");
         }

         this.pageSize = page.getPageSize();
         this.page = page.getPage();
         this.totalCount = page.getTotalCount();
         this.lastPage = page.getLastPage();
         this.buildItemsArray(page, isMaker);
      }
   }

   private void buildItemsArray(Page<ObjectEntry> page, boolean isMaker) {
      try {
         for(ObjectEntry entry : page.getItems()) {
            this.items.put(this.createItemJson(entry, isMaker));
         }
      } catch (Exception e) {
         this.items.put("error: " + e.getMessage());
      }

   }

   private JSONObject createItemJson(ObjectEntry entry, boolean isMaker) {
      JSONObject json;
      int status;
      String customStatus;
      int collST;
      boolean var8;
      label31: {
         label30: {
            json = JSONFactoryUtil.createJSONObject();
            Map var10000 = entry.getProperties();
            Objects.requireNonNull(json);
            var10000.forEach(json::put);
            json.put("modifiedDate", entry.getDateModified());
            status = entry.getStatus().getCode();
            customStatus = (String)entry.getPropertyValue("customStatusByHandler");
            collST = entry.getPropertyValue("collST") != null ? (Integer)entry.getPropertyValue("collST") : 10;
            if (isMaker) {
               if (status == 2) {
                  break label30;
               }

               if (collST == 0) {
                  if (status == 4) {
                     break label30;
                  }
               } else if (collST == -1) {
                  break label30;
               }
            }

            var8 = false;
            break label31;
         }

         var8 = true;
      }

      boolean redirectToScanner = var8;
      json.put("redirectTo", redirectToScanner ? "scanner" : "detailed");
      json.put("status", this.resolveStatusLabel(status, customStatus, collST));
      json.put("id", entry.getId());
      return json;
   }

   private String resolveStatusLabel(int status, String customStatusByHandler, int collST) {
      log.info("resolveStatusLabel called -> status: " + status + ", customStatusByHandler: " + customStatusByHandler + ", collST: " + collST);
      if ("pending with zone checker".equalsIgnoreCase(customStatusByHandler) && status == 0 && collST == 0) {
         log.info("Matched special approved condition for zone checker");
         return "approved";
      } else if (!Validator.isBlank(customStatusByHandler)) {
         log.info("Returning customStatusByHandler: " + customStatusByHandler);
         return customStatusByHandler;
      } else if (status == 2) {
         log.info("Returning draft status");
         return "draft";
      } else {
         switch (collST) {
            case -1:
               log.info("Returning pending with branch maker");
               return "pending with branch maker";
            case 0:
            default:
               log.info("Returning default workflow status label");
               return WorkflowConstants.getStatusLabel(status);
            case 1:
               log.info("Returning pending with branch checker");
               return "pending with branch checker";
            case 2:
               log.info("Returning pending with zone maker");
               return "pending with zone maker";
            case 3:
               log.info("Returning pending with zone checker");
               return "pending with zone checker";
            case 4:
               log.info("Returning approved");
               return "approved";
         }
      }
   }

   public long getPageSize() {
      return this.pageSize;
   }

   public long getPage() {
      return this.page;
   }

   public long getTotalCount() {
      return this.totalCount;
   }

   public long getLastPage() {
      return this.lastPage;
   }

   public JSONArray getItems() {
      return this.items;
   }

   public String getDownloadUrl() {
      return this.downloadUrl;
   }

   public Set<String> getStatusList() {
      return this.statusList;
   }

   public String toString() {
      return String.format("RequestAccountOpeningResponseAsList [pageSize=%d, page=%d, totalCount=%d, lastPage=%d, itemsCount=%d, downloadUrl=%s, statusListSize=%d]", this.pageSize, this.page, this.totalCount, this.lastPage, this.items.length(), this.downloadUrl, this.statusList.size());
   }
}
