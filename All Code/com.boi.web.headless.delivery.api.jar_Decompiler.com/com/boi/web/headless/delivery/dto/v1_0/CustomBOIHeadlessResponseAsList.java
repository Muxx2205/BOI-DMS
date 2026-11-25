package com.boi.web.headless.delivery.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Page;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CustomBOIHeadlessResponseAsList extends BOIHeadlessResponse {
   private static final long serialVersionUID = 8632989910729714104L;
   private long pageSize;
   private long page = 1L;
   private long totalCount = 0L;
   private long lastPage = 1L;
   private JSONArray items = JSONFactoryUtil.createJSONArray();
   private String downloadUrl = "";
   private Set<String> statusList = new LinkedHashSet();

   public CustomBOIHeadlessResponseAsList(Page<ObjectEntry> page, boolean isMaker, String downloadUrl, Set<String> statusList) {
      if (!Validator.isNull(page)) {
         try {
            this.downloadUrl = Validator.isNotNull(downloadUrl) ? downloadUrl : "";
            this.statusList = new LinkedHashSet(Validator.isNotNull(statusList) ? statusList : Collections.emptySet());
            if (isMaker) {
               this.statusList.add("draft");
            }

            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.lastPage = page.getLastPage();

            for(ObjectEntry entry : page.getItems()) {
               this.items.put(this.buildItemJson(entry, isMaker));
            }
         } catch (Exception e) {
            this.items.put("error: " + e.getMessage());
         }

      }
   }

   public CustomBOIHeadlessResponseAsList(Page<ObjectEntry> page, boolean isMaker, String downloadUrl, Set<String> statusList, String customTable) {
      if (!Validator.isNull(page)) {
         try {
            this.downloadUrl = Validator.isNotNull(downloadUrl) ? downloadUrl : "";
            this.statusList = new LinkedHashSet(Validator.isNotNull(statusList) ? statusList : Collections.emptySet());
            if (isMaker) {
               this.statusList.add("draft");
            }

            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.lastPage = page.getLastPage();

            for(ObjectEntry entry : page.getItems()) {
               this.items.put(this.customBuildItemJson(entry, isMaker, customTable));
            }
         } catch (Exception e) {
            this.items.put("error: " + e.getMessage());
         }

      }
   }

   private JSONObject customBuildItemJson(ObjectEntry entry, boolean isMaker, String customTable) {
      JSONObject jsonObject;
      int status;
      String customStatus;
      int collST;
      boolean var9;
      label31: {
         label30: {
            jsonObject = JSONFactoryUtil.createJSONObject();
            Map var10000 = entry.getProperties();
            Objects.requireNonNull(jsonObject);
            var10000.forEach(jsonObject::put);
            jsonObject.put("modifiedDate", entry.getDateModified());
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

            var9 = false;
            break label31;
         }

         var9 = true;
      }

      boolean redirectToScanner = var9;
      jsonObject.put("redirectTo", redirectToScanner ? "scanner" : "detailed");
      jsonObject.put("status", this.resolveStatusLabel(status, customStatus, collST, customTable));
      jsonObject.put("createdBy", entry.getCreator().getName());
      jsonObject.put("createdById", entry.getCreator().getId());
      jsonObject.put("dateModified", entry.getDateModified());
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      return jsonObject;
   }

   private String resolveStatusLabel(int status, String customStatusByHandler, int collST, String customTable) {
      if (customTable.equalsIgnoreCase("FINANCE_PLOE")) {
         if ("pending with zone checker".equalsIgnoreCase(customStatusByHandler) && status == 0 && collST == 0) {
            return "approved";
         }
      } else if (customTable.equalsIgnoreCase("FBD_FE_BO_FINANCIAL") && "pending with ho checker".equalsIgnoreCase(customStatusByHandler) && status == 0 && collST == 0) {
         return "approved";
      }

      if (!Validator.isBlank(customStatusByHandler)) {
         return customStatusByHandler;
      } else if (status == 2) {
         return "draft";
      } else {
         switch (collST) {
            case -1:
               return "pending with branch maker";
            case 0:
            default:
               return WorkflowConstants.getStatusLabel(status);
            case 1:
               if (customTable.equalsIgnoreCase("FINANCE_PLOE")) {
                  return "pending with zone checker";
               } else if (customTable.equalsIgnoreCase("FBD_FE_BO_FINANCIAL")) {
                  return "pending with ho checker";
               }
            case 2:
               return "approved";
         }
      }
   }

   private JSONObject buildItemJson(ObjectEntry entry, boolean isMaker) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map var10000 = entry.getProperties();
      Objects.requireNonNull(jsonObject);
      var10000.forEach(jsonObject::put);
      jsonObject.put("modifiedDate", entry.getDateModified());
      int status = entry.getStatus().getCode();
      String customStatusByHandler = (String)entry.getPropertyValue("customStatusByHandler");
      String redirectTo = !isMaker || status != 2 && status != 4 ? "detailed" : "scanner";
      jsonObject.put("redirectTo", redirectTo);
      jsonObject.put("status", this.resolveStatusLabel(status, customStatusByHandler));
      jsonObject.put("createdBy", entry.getCreator().getName());
      jsonObject.put("createdById", entry.getCreator().getId());
      jsonObject.put("dateModified", entry.getDateModified());
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      return jsonObject;
   }

   private String resolveStatusLabel(int status, String customStatusByHandler) {
      if (!Validator.isBlank(customStatusByHandler) && status == 0) {
         return "approved";
      } else if (!Validator.isBlank(customStatusByHandler)) {
         return customStatusByHandler;
      } else if (status == 0) {
         return "approved";
      } else {
         return status == 1 ? "pending" : WorkflowConstants.getStatusLabel(status);
      }
   }

   public long getPageSize() {
      return this.pageSize;
   }

   public void setPageSize(long pageSize) {
      this.pageSize = pageSize;
   }

   public long getPage() {
      return this.page;
   }

   public void setPage(long page) {
      this.page = page;
   }

   public long getTotalCount() {
      return this.totalCount;
   }

   public void setTotalCount(long totalCount) {
      this.totalCount = totalCount;
   }

   public long getLastPage() {
      return this.lastPage;
   }

   public void setLastPage(long lastPage) {
      this.lastPage = lastPage;
   }

   public JSONArray getItems() {
      return this.items;
   }

   public String getDownloadUrl() {
      return this.downloadUrl;
   }

   public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
   }

   public Set<String> getStatusList() {
      return this.statusList;
   }

   public void setStatusList(Set<String> statusList) {
      this.statusList = statusList;
   }

   public String toString() {
      return String.format("CustomBOIHeadlessResponseAsList [pageSize=%d, page=%d, totalCount=%d, lastPage=%d, items=%s, downloadUrl=%s, statusList=%s]", this.pageSize, this.page, this.totalCount, this.lastPage, this.items.toJSONString(), this.downloadUrl, this.statusList);
   }
}
