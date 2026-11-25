package boi.headless.delivery.memorandumapi.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
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
               this.items.put(this.customBuildItemJson(entry, isMaker));
            }
         } catch (Exception e) {
            this.items.put("error: " + e.getMessage());
         }

      }
   }

   private JSONObject customBuildItemJson(ObjectEntry entry, boolean isMaker) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      Map var10000 = entry.getProperties();
      Objects.requireNonNull(jsonObject);
      var10000.forEach(jsonObject::put);
      jsonObject.put("modifiedDate", entry.getDateModified());
      String customStatus = (String)entry.getPropertyValue("paimsStatus");
      int currST = entry.getPropertyValue("currST") != null ? (Integer)entry.getPropertyValue("currST") : 10;
      boolean redirectToScanner = isMaker && (currST == 0 && "draft".equalsIgnoreCase(customStatus) || currST == -1 && "pending with branch checker".equalsIgnoreCase(customStatus));
      jsonObject.put("redirectTo", redirectToScanner ? "scanner" : "detailed");
      jsonObject.put("status", this.resolveStatusLabel(customStatus, currST));
      jsonObject.put("createdBy", entry.getCreator().getName());
      jsonObject.put("createdById", entry.getCreator().getId());
      jsonObject.put("dateModified", entry.getDateModified());
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      jsonObject.remove("currST");
      jsonObject.remove("paimsStatus");
      return jsonObject;
   }

   private String resolveStatusLabel(String customStatus, int currST) {
      if (currST == 0) {
         return "draft";
      } else if (currST == -1 && "pending with branch maker".equalsIgnoreCase(customStatus)) {
         return "pending with branch maker";
      } else if (currST == 1 && "pending at branch checker".equalsIgnoreCase(customStatus)) {
         return "pending at branch checker";
      } else {
         return currST == 2 && "approved".equalsIgnoreCase(customStatus) ? "approved" : "need action";
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
