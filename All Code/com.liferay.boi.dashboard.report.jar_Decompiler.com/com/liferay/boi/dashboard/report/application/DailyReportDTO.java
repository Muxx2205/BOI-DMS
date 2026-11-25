package com.liferay.boi.dashboard.report.application;

public class DailyReportDTO {
   private String metricKey;
   private String metricName;
   private double metricValue;

   public DailyReportDTO(String metricKey, String metricName, double metricValue) {
      this.metricKey = metricKey;
      this.metricName = metricName;
      this.metricValue = metricValue;
   }

   public String getMetricKey() {
      return this.metricKey;
   }

   public String getMetricName() {
      return this.metricName;
   }

   public double getMetricValue() {
      return this.metricValue;
   }
}
