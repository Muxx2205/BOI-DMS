package com.boi.web.headless.delivery.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;
import java.util.Collection;
import java.util.Map;

public class CustomFBDFEBOBOIHeadlessResponseAsList extends BOIHeadlessResponse {
   private static final long serialVersionUID = 8632989910729714104L;
   private long pageSize;
   private long page = 1L;
   private long totalCount = 0L;
   private long lastPage = 1L;
   private JSONArray items = JSONFactoryUtil.createJSONArray();
   private String downloadUrl = "";
   private JSONArray statusList = JSONFactoryUtil.createJSONArray();

   public CustomFBDFEBOBOIHeadlessResponseAsList(Page<ObjectEntry> page, boolean isMaker, String downloadUrl, boolean isGandhiNagar, boolean isSwitcell) {
      try {
         if (Validator.isNotNull(page)) {
            Collection<ObjectEntry> itemss = page.getItems();
            if (Validator.isNotNull(itemss) && itemss.size() > 0) {
               if (isMaker) {
                  this.statusList.put("draft");
                  this.statusList.put("Pending with FEBO");
                  this.statusList.put("Pending with swift cell");
                  this.statusList.put("denied");
               }

               this.statusList.put("approved");
               if (isGandhiNagar) {
                  this.statusList.put("Pending with FEBO");
                  this.statusList.put("Pending with swift cell");
                  this.statusList.put("denied");
               }

               if (isSwitcell) {
                  this.statusList.put("Pending with swift cell");
                  this.statusList.put("denied");
               }
            }

            this.downloadUrl = downloadUrl;
            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.pageSize = page.getPageSize();
            this.lastPage = page.getLastPage();
            page.getItems().forEach((x) -> {
               JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
               x.getProperties().forEach((c, d) -> itemObjects.put(c, d));
               String status = this.getStatus(x);
               if (!isMaker || !status.equalsIgnoreCase("draft") && !status.equalsIgnoreCase("denied")) {
                  itemObjects.put("redirectTo", "detailed");
               } else {
                  itemObjects.put("redirectTo", "scanner");
               }

               itemObjects.put("status", status);
               itemObjects.put("createdBy", x.getCreator().getName());
               itemObjects.put("createdById", x.getCreator().getId());
               itemObjects.put("dateModified", x.getDateModified());
               itemObjects.put("dateCreated", x.getDateCreated());
               itemObjects.put("id", x.getId());
               this.items.put(itemObjects);
            });
         }
      } catch (Exception e) {
         this.items.put("error " + e.getMessage());
      }

   }

   private String getStatus(ObjectEntry objectEntry) {
      Map<String, Object> values = objectEntry.getProperties();
      if (objectEntry.getStatus().getCode() == 2) {
         return "draft";
      } else if (objectEntry.getStatus().getCode() == 4) {
         return "denied";
      } else {
         if (values.containsKey("stage")) {
            int stage = (Integer)values.get("stage");
            if (stage == 0 && objectEntry.getStatus().getCode() == 1) {
               return "Pending with FEBO";
            }

            if (stage == 0 && objectEntry.getStatus().getCode() == 0) {
               return "approved";
            }

            if (stage == 1) {
               return "denied";
            }

            if (stage == 2) {
               return "Pending with FEBO";
            }

            if (stage == 3) {
               return "Pending with swift cell";
            }

            if (stage == 4 || stage == 5) {
               return "approved";
            }
         }

         return "NA";
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

   public JSONArray getStatusList() {
      return this.statusList;
   }

   public void setStatusList(JSONArray statusList) {
      this.statusList = statusList;
   }

   public String toString() {
      long var10000 = this.pageSize;
      return "CustomBOIHeadlessResponseAsList [pageSize=" + var10000 + ", page=" + this.page + ", totalCount=" + this.totalCount + ", lastPage=" + this.lastPage + ", items=" + this.items.toJSONString() + ", downloadUrl=" + this.downloadUrl + ", statusList=" + this.statusList.toJSONString() + "]";
   }
}
