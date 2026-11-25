package com.liferay.boi.dashboard.report.application;

public class DashboardStatDTO {
   private String category;
   private String name;
   private long count;

   public DashboardStatDTO(String category, String name, long count) {
      this.category = category;
      this.name = name;
      this.count = count;
   }

   public String getCategory() {
      return this.category;
   }

   public String getName() {
      return this.name;
   }

   public long getCount() {
      return this.count;
   }
}
