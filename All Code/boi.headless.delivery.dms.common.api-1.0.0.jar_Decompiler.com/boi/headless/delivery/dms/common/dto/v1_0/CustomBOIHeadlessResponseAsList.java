package boi.headless.delivery.dms.common.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;

public class CustomBOIHeadlessResponseAsList extends BOIHeadlessResponse {
   private static final long serialVersionUID = 1579178406690376326L;
   private long pageSize;
   private long page = 1L;
   private long totalCount = 0L;
   private long lastPage = 1L;
   private String downloadUrl;
   private JSONArray items = JSONFactoryUtil.createJSONArray();

   public CustomBOIHeadlessResponseAsList(Page<ObjectEntry> page) {
      try {
         if (Validator.isNotNull(page)) {
            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.pageSize = page.getPageSize();
            this.lastPage = page.getLastPage();
            page.getItems().forEach((x) -> {
               JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
               x.getProperties().forEach((c, d) -> itemObjects.put(c, d));
               itemObjects.put("status", x.getStatus().getLabel());
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

   public CustomBOIHeadlessResponseAsList(Page<ObjectEntry> page, String url) {
      this.items = JSONFactoryUtil.createJSONArray();
      if (Validator.isNotNull(page)) {
         this.pageSize = page.getPageSize();
         this.page = page.getPage();
         this.totalCount = page.getTotalCount();
         this.pageSize = page.getPageSize();
         this.lastPage = page.getLastPage();
         this.downloadUrl = url;
         page.getItems().forEach((x) -> {
            JSONObject itemObjects = JSONFactoryUtil.createJSONObject();
            x.getProperties().forEach((c, d) -> itemObjects.put(c, d));
            itemObjects.put("status", x.getStatus().getLabel());
            itemObjects.put("createdBy", x.getCreator().getName());
            itemObjects.put("createdById", x.getCreator().getId());
            itemObjects.put("dateModified", x.getDateModified());
            itemObjects.put("dateCreated", x.getDateCreated());
            itemObjects.put("id", x.getId());
            this.items.put(itemObjects);
         });
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

   public void setItems(JSONArray items) {
   }

   public String toString() {
      return "CustomBOIHeadlessResponseAsList [pageSize=" + this.pageSize + ", page=" + this.page + ", totalCount=" + this.totalCount + ", lastPage=" + this.lastPage + ", downloadUrl=" + this.downloadUrl + ", items=" + this.items + "]";
   }

   public String getDownloadUrl() {
      return this.downloadUrl;
   }

   public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
   }
}
