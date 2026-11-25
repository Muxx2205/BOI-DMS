package boi.headless.delivery.memorandumapi.dto.v1_0;

import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomResponseAsList extends BOIHeadlessResponse {
   private static final long serialVersionUID = 8632989910729714104L;
   private long pageSize;
   private long page = 1L;
   private long totalCount = 0L;
   private long lastPage = 1L;
   private JSONArray items = JSONFactoryUtil.createJSONArray();
   private String downloadUrl = "";
   private boolean canExportToPDF = false;
   private boolean canBranch = false;
   private boolean canZone = false;
   private boolean showOrgFilter = false;
   private String hideColumns = "";
   private String orderColumns = "";
   private List<String> statusFilter = Collections.emptyList();
   private String dateFilter = "";
   private List<String> branchOptions = Collections.emptyList();
   private List<String> zoneOptions = Collections.emptyList();
   private String canCertificate = "";

   public CustomResponseAsList(Page<ObjectEntry> page, JSONArray items, String downloadUrl, boolean canExportToPDF, boolean canBranch, boolean canZone, String canCertificate, String hideColumns, String orderColumns, List<String> statusFilter, String dateFilter, List<String> branchOptions, List<String> zoneOptions) {
      if (!Validator.isNull(page)) {
         try {
            this.downloadUrl = Validator.isNotNull(downloadUrl) ? downloadUrl : "";
            this.items = Validator.isNotNull(items) ? items : JSONFactoryUtil.createJSONArray();
            this.pageSize = page.getPageSize();
            this.page = page.getPage();
            this.totalCount = page.getTotalCount();
            this.lastPage = page.getLastPage();
            this.canExportToPDF = canExportToPDF;
            this.canBranch = canBranch;
            this.canZone = canZone;
            this.canCertificate = canCertificate;
            this.hideColumns = hideColumns;
            this.orderColumns = orderColumns;
            this.statusFilter = (List<String>)(statusFilter != null ? new ArrayList(statusFilter) : Collections.emptyList());
            this.dateFilter = dateFilter;
            this.branchOptions = (List<String>)(branchOptions != null ? new ArrayList(branchOptions) : Collections.emptyList());
            this.zoneOptions = (List<String>)(zoneOptions != null ? new ArrayList(zoneOptions) : Collections.emptyList());
            this.showOrgFilter = !ListUtil.isEmpty(branchOptions);
         } catch (Exception e) {
            this.items.put("error: " + e.getMessage());
         }

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
      this.items = items;
   }

   public String getDownloadUrl() {
      return this.downloadUrl;
   }

   public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
   }

   public boolean getCanExportToPDF() {
      return this.canExportToPDF;
   }

   public void setCanExportToPDF(boolean canExportToPDF) {
      this.canExportToPDF = canExportToPDF;
   }

   public boolean getCanBranch() {
      return this.canBranch;
   }

   public void setCanBranch(boolean canBranch) {
      this.canBranch = canBranch;
   }

   public boolean getCanZone() {
      return this.canZone;
   }

   public void setCanZone(boolean canZone) {
      this.canZone = canZone;
   }

   public String getCanCertificate() {
      return this.canCertificate;
   }

   public void setCanCertificate(String canCertificate) {
      this.canCertificate = canCertificate;
   }

   public String getHideColumns() {
      return this.hideColumns;
   }

   public void setHideColumns(String hideColumns) {
      this.hideColumns = hideColumns;
   }

   public String getOrderColumns() {
      return this.orderColumns;
   }

   public void setOrderColumns(String orderColumns) {
      this.orderColumns = orderColumns;
   }

   public List<String> getStatusFilter() {
      return this.statusFilter;
   }

   public void setStatusFilter(List<String> statusFilter) {
      this.statusFilter = (List<String>)(statusFilter != null ? new ArrayList(statusFilter) : Collections.emptyList());
   }

   public String getDateFilter() {
      return this.dateFilter;
   }

   public void setDateFilter(String dateFilter) {
      this.dateFilter = dateFilter;
   }

   public List<String> getBranchOptions() {
      return this.branchOptions;
   }

   public void setBranchOptions(List<String> branchOptions) {
      this.branchOptions = (List<String>)(branchOptions != null ? new ArrayList(branchOptions) : Collections.emptyList());
   }

   public List<String> getZoneOptions() {
      return this.zoneOptions;
   }

   public void setZoneOptions(List<String> zoneOptions) {
      this.zoneOptions = (List<String>)(zoneOptions != null ? new ArrayList(zoneOptions) : Collections.emptyList());
   }

   public boolean isShowOrgFilter() {
      return this.showOrgFilter;
   }

   public void setShowOrgFilter(boolean showOrgFilter) {
      this.showOrgFilter = showOrgFilter;
   }

   public String toString() {
      return String.format("CustomResponseAsList [pageSize=%d, page=%d, totalCount=%d, lastPage=%d, items=%s, downloadUrl=%s, canExportToPDF=%s, canBranch=%s, canZone=%s, showOrgFilter=%s, canCertificate=%s, hideColumns=%s, orderColumns=%s, statusFilter=%s, dateFilter=%s, branchOptions=%s, zoneOptions=%s]", this.pageSize, this.page, this.totalCount, this.lastPage, this.items.toJSONString(), this.downloadUrl, this.canExportToPDF, this.canBranch, this.canZone, this.showOrgFilter, this.canCertificate, this.hideColumns, this.orderColumns, this.statusFilter, this.dateFilter, this.branchOptions, this.zoneOptions);
   }
}
