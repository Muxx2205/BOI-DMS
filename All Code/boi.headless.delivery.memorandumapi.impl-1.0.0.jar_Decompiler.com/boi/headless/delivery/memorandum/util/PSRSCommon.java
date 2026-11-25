package boi.headless.delivery.memorandum.util;

import boi.headless.delivery.memorandum.background.task.ObjectDataUpload;
import boi.headless.delivery.memorandum.background.task.PDFReportGeneratorEngine;
import boi.headless.delivery.memorandum.configuration.PSRSConfiguration;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Order;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationModel;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class PSRSCommon {
   private final String PARAM_START_DATE = "startDate";
   private final String PARAM_END_DATE = "endDate";
   private final String PARAM_STATUS = "status";
   private final String PARAM_MONTH = "month";
   private final String PARAM_YEAR = "year";
   private HttpServletRequest contextHttpServletRequest;
   private PSRSConfiguration psrsConfiguration;
   private User user;
   private String action = "";
   private String currentRole = "";
   public boolean isBranchMaker;
   public boolean isObservationNoted;
   public boolean isBranchChecker;
   public boolean isZoneMaker;
   public boolean isZoneChecker;
   public boolean isRADZM;
   public boolean isNotingAuthority;
   private String pdfFromDate = "";
   private String pdfToDate = "";
   private String branchName = "";
   private String zoneName = "";
   private String nbgName = "";
   private String hoName = "";
   private Organization zoneOrg = null;
   private String moduleName = "";
   private static final DateTimeFormatter F_DD_MM_YYYY;
   private static final DateTimeFormatter F_DD_MM_YYYY_DASH;
   private static final List<Pattern> DATE_PATTERNS;
   private static final Set<String> NON_WORKFLOW_REPORTS;
   private static final Set<String> WORKFLOW_REPORTS;
   private static final Set<String> CHECKLIST_REPORTS;
   Log _log = LogFactoryUtil.getLog(this.getClass());

   public PSRSCommon(HttpServletRequest contextHttpServletRequest, User user, PSRSConfiguration configuration, String reportType, boolean loadOrgHierachy) {
      this.contextHttpServletRequest = contextHttpServletRequest;
      this.user = user;
      if (loadOrgHierachy) {
         this.loadOrganizationHierarchy(user);
      }

      this.psrsConfiguration = configuration;
      String lowerReportType = reportType.toLowerCase();
      this.moduleName = lowerReportType.endsWith("s") ? lowerReportType.substring(0, lowerReportType.length() - 1) : lowerReportType;
   }

   public PSRSCommon(HttpServletRequest contextHttpServletRequest, User user, PSRSConfiguration configuration, String reportType) {
      this.contextHttpServletRequest = contextHttpServletRequest;
      this.user = user;
      this.loadOrganizationHierarchy(user);
      this.loadUserRoles(user);
      this.psrsConfiguration = configuration;
      String lowerReportType = reportType.toLowerCase();
      this.moduleName = lowerReportType.endsWith("s") ? lowerReportType.substring(0, lowerReportType.length() - 1) : lowerReportType;
   }

   public PSRSCommon() {
   }

   public void exportToPDF(String search, String filterQuery, String reportType) throws PortalException {
      Map<String, Serializable> taskContextMap = new HashMap();
      ServiceContext serviceContext = new ServiceContext();
      taskContextMap.put("Query", filterQuery);
      taskContextMap.put("Search", search);
      taskContextMap.put("serviceContext", serviceContext);
      taskContextMap.put("UserId", this.user.getUserId());
      taskContextMap.put("ReportName", reportType);
      taskContextMap.put("pdfFromDate", this.pdfFromDate);
      taskContextMap.put("pdfToDate", this.pdfToDate);
      String branch = Validator.isNotNull(this.branchName) ? this.branchName : "";
      taskContextMap.put("branchName", branch);
      String branchCode = "";
      if (Validator.isNotNull(branch)) {
         String[] branchParts = branch.split("-");
         if (branchParts.length > 0) {
            branchCode = branchParts[0].trim();
         }
      }

      taskContextMap.put("branchCode", branchCode);
      BackgroundTaskLocalServiceUtil.addBackgroundTask(this.user.getUserId(), this.user.getGroupId(), reportType, (String[])null, PDFReportGeneratorEngine.class, taskContextMap, serviceContext);
   }

   public boolean isHoChecker() {
      boolean isHoChecker = this.user.getRoles().stream().anyMatch((x) -> x.getName().equalsIgnoreCase("HO Checker"));
      return isHoChecker;
   }

   public String getUserFilterQuery() {
      List<String> queryList = new LinkedList();
      String filter = ParamUtil.getString(this.contextHttpServletRequest, "filter", (String)null);
      this._log.info("filter: " + filter);
      if (!this.isHoChecker()) {
         this.addHierarchyFilter(queryList);
         if ("psrs ra dzm".equalsIgnoreCase(this.currentRole) && !CHECKLIST_REPORTS.contains(this.moduleName)) {
            queryList.add("collST eq '2'");
         }

         if ("psrs noting authority".equalsIgnoreCase(this.currentRole) && !CHECKLIST_REPORTS.contains(this.moduleName)) {
            queryList.add("collST eq '3'");
         }
      }

      if (Validator.isNotNull(filter)) {
         queryList.add(filter);
      }

      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      this._log.info("queryResult: " + queryResult);
      return queryResult;
   }

   private void addDateFilter(List<String> queryList, Date startDate, Date endDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(startDate) && Validator.isNotNull(endDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(startDate), sdf.format(endDate));
         String var10000 = (String)isoRange.get("startDate");
         String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + (String)isoRange.get("endDate");
         this.pdfFromDate = (String)isoRange.get("startDate");
         this.pdfToDate = (String)isoRange.get("endDate");
         queryList.add(dateFilter);
      }

   }

   private void addMonthYearFilter(List<String> queryList, String month, String year) {
      if (Validator.isNotNull(month) && !month.isBlank() && Validator.isNotNull(year) && !year.isBlank()) {
         try {
            int monthInt = Integer.parseInt(month);
            int yearInt = Integer.parseInt(year);
            if (monthInt < 1 || monthInt > 12) {
               throw new IllegalArgumentException("Invalid month: " + month);
            }

            LocalDate startDate = LocalDate.of(yearInt, monthInt, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            this.addDateRangeFilter(queryList, startDate, endDate);
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid month or year format", e);
         }
      } else if (!Validator.isNotNull(year) || year.isBlank() || !Validator.isNull(month) && !month.isBlank()) {
         if (Validator.isNotNull(month) && !month.isBlank() && (Validator.isNull(year) || year.isBlank())) {
            try {
               int monthInt = Integer.parseInt(month);
               int yearInt = Year.now().getValue();
               if (monthInt < 1 || monthInt > 12) {
                  throw new IllegalArgumentException("Invalid month: " + month);
               }

               LocalDate startDate = LocalDate.of(yearInt, monthInt, 1);
               LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
               this.addDateRangeFilter(queryList, startDate, endDate);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Invalid month format", e);
            }
         }
      } else {
         try {
            int yearInt = Integer.parseInt(year);
            LocalDate startDate = LocalDate.of(yearInt, 1, 1);
            LocalDate endDate = LocalDate.of(yearInt, 12, 31);
            this.addDateRangeFilter(queryList, startDate, endDate);
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid year format", e);
         }
      }

   }

   private void addDateRangeFilter(List<String> queryList, LocalDate startDate, LocalDate endDate) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime endDateTime = endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      String var10000 = isoFormatter.format(startDateTime);
      String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + isoFormatter.format(endDateTime);
      this.pdfFromDate = isoFormatter.format(startDateTime);
      this.pdfToDate = isoFormatter.format(endDateTime);
      queryList.add(dateFilter);
   }

   private Map<String, String> convertToISO8601Range(String startDateStr, String endDateStr) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      LocalDate fromDate = LocalDate.parse(startDateStr);
      LocalDate toDate = LocalDate.parse(endDateStr);
      ZonedDateTime startDateTime = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime endDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("startDate", isoFormatter.format(startDateTime));
      result.put("endDate", isoFormatter.format(endDateTime));
      return result;
   }

   private void addHierarchyFilter(List<String> queryList) {
      if (!Validator.isBlank(this.branchName) && this.branchName.contains("-")) {
         if (this.isMonthlyCheckListReport()) {
            queryList.add("branch eq '" + this.branchName + "'");
         } else {
            String[] parts = this.branchName.split("-");
            queryList.add("branchCode eq '" + parts[0].trim() + "'");
            queryList.add("branchName eq '" + parts[1].trim() + "'");
         }
      }

      if (!Validator.isBlank(this.zoneName)) {
         queryList.add("zone eq '" + this.zoneName.trim() + "'");
      }

   }

   public List<String> zoneFilter() {
      List<String> zones = new ArrayList();
      this._log.info("Zone " + this.zoneName);
      if (!Validator.isBlank(this.zoneName)) {
         zones.add(this.zoneName);
         return zones;
      } else {
         return zones;
      }
   }

   public List<String> branchFilter() {
      return Validator.isNotNull(this.zoneOrg) ? (List)OrganizationLocalServiceUtil.getSuborganizations(this.zoneOrg.getCompanyId(), this.zoneOrg.getOrganizationId()).stream().filter((org) -> "BRANCH".equalsIgnoreCase(org.getType())).map(OrganizationModel::getName).collect(Collectors.toList()) : Collections.emptyList();
   }

   private void loadOrganizationHierarchy(User user) {
      try {
         this.branchName = this.getOrganization(user.getOrganizations(), "BRANCH");
         this.zoneName = this.getOrganization(user.getOrganizations(), "ZONE");
         this.nbgName = this.getOrganization(user.getOrganizations(), "NBG");
         this.hoName = this.getOrganization(user.getOrganizations(), "Organization");
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private Organization getOrganization(String type, Organization organization) {
      if (Validator.isNotNull(organization) && type.equalsIgnoreCase(organization.getType())) {
         return organization;
      } else {
         if (organization != null && organization.getParentOrganizationId() > 0L) {
            try {
               return this.getOrganization(type, organization.getParentOrganization());
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return null;
      }
   }

   public JSONArray getKeysFromConfig(String jsonString) {
      JSONArray keysArray = JSONFactoryUtil.createJSONArray();
      if (Validator.isNull(jsonString)) {
         return keysArray;
      } else {
         String cleaned = jsonString.replace("{", "").replace("}", "");
         String[] pairs = cleaned.split(",");

         for(String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length > 0) {
               String key = keyValue[0].replace("\"", "").trim();
               keysArray.put(key);
            }
         }

         this._log.info(keysArray);
         return keysArray;
      }
   }

   public JSONObject arrangeListByConfig(String configString, JSONObject dataJson) throws JSONException {
      JSONArray keysFromConfig = this.getKeysFromConfig(configString);
      if (!Validator.isNull(keysFromConfig) && !Validator.isNull(dataJson)) {
         Map<String, Object> orderedMap = new LinkedHashMap();

         for(int i = 0; i < keysFromConfig.length(); ++i) {
            String key = keysFromConfig.getString(i);
            if (dataJson.has(key)) {
               orderedMap.put(key, dataJson.get(key));
            } else {
               orderedMap.put(key, "");
            }
         }

         Iterator<String> dataKeys = dataJson.keys();

         while(dataKeys.hasNext()) {
            String key = (String)dataKeys.next();
            if (!orderedMap.containsKey(key)) {
               orderedMap.put(key, dataJson.get(key));
            }
         }

         JSONObject orderedJson = JSONFactoryUtil.createJSONObject(JSONFactoryUtil.looseSerialize(orderedMap));
         this._log.info("Ordered JSON: " + orderedJson);
         return orderedJson;
      } else {
         return JSONFactoryUtil.createJSONObject();
      }
   }

   private String getOrganization(List<Organization> organizations, String type) {
      if (organizations == null) {
         return null;
      } else {
         String orgName = null;

         for(Organization org : organizations) {
            Organization match = this.getOrganization(type, org);
            if (match != null) {
               if (type.equalsIgnoreCase("ZONE")) {
                  this.zoneOrg = match;
               }

               orgName = match.getName();
               break;
            }
         }

         return orgName;
      }
   }

   public long getObjectDefinitionId() {
      switch (PSRSCommon.ReportType.valueOf(this.moduleName)) {
         case m1:
            return this.psrsConfiguration.M1Id();
         case m2:
            return this.psrsConfiguration.M2Id();
         case m3:
            return this.psrsConfiguration.M3Id();
         case m4:
            return this.psrsConfiguration.M4Id();
         case m5:
            return this.psrsConfiguration.M5Id();
         case m6:
            return this.psrsConfiguration.M6Id();
         case m7:
            return this.psrsConfiguration.M7Id();
         case m8:
            return this.psrsConfiguration.M8Id();
         case monthlychecklist:
            return this.psrsConfiguration.MonthlyCheckListId();
         default:
            return 0L;
      }
   }

   public String getObjectColumns() {
      switch (PSRSCommon.ReportType.valueOf(this.moduleName)) {
         case m1:
            return this.psrsConfiguration.M1ObjectColumns();
         case m2:
            return this.psrsConfiguration.M2ObjectColumns();
         case m3:
            return this.psrsConfiguration.M3ObjectColumns();
         case m4:
            return this.psrsConfiguration.M4ObjectColumns();
         case m5:
            return this.psrsConfiguration.M5ObjectColumns();
         case m6:
            return this.psrsConfiguration.M6ObjectColumns();
         case m7:
            return this.psrsConfiguration.M7ObjectColumns();
         case m8:
            return this.psrsConfiguration.M8ObjectColumns();
         default:
            return "";
      }
   }

   public String getOrderColumns() {
      switch (PSRSCommon.ReportType.valueOf(this.moduleName)) {
         case m1:
            return this.psrsConfiguration.M1OrderColumns();
         case m2:
            return this.psrsConfiguration.M2OrderColumns();
         case m3:
            return this.psrsConfiguration.M3OrderColumns();
         case m4:
            return this.psrsConfiguration.M4OrderColumns();
         case m5:
            return this.psrsConfiguration.M5OrderColumns();
         case m6:
            return this.psrsConfiguration.M6OrderColumns();
         case m7:
            return this.psrsConfiguration.M7OrderColumns();
         case m8:
            return this.psrsConfiguration.M8OrderColumns();
         default:
            return this.psrsConfiguration.MonthlyCheckListOrderColumns();
      }
   }

   public String getDateFilter() {
      switch (PSRSCommon.ReportType.valueOf(this.moduleName)) {
         case m1:
            return "dateOfTemporaryOverdraftGranted";
         case m2:
            return "dateOfTemporaryOverLimitGranted";
         case m3:
            return "dateOfTODTOL";
         case m4:
            return "dateOfSanction";
         case m5:
            return "dateOfDisbursement";
         case m6:
         case m7:
         case m8:
            return "reviewDate";
         default:
            return "dateModified";
      }
   }

   public List<String> getDateColumns() {
      String psrsDateColumns = this.psrsConfiguration.PSRSDateColumns();
      if (psrsDateColumns != null && !psrsDateColumns.trim().isEmpty()) {
         Map<String, List<String>> map = (Map)Arrays.stream(psrsDateColumns.split("\\s*;\\s*")).map((s) -> s.split("\\s*=\\s*")).filter((p) -> p.length == 2).collect(Collectors.toMap((p) -> p[0].toLowerCase(), (p) -> Arrays.asList(p[1].split("\\s*,\\s*"))));
         return (List)map.getOrDefault(this.moduleName, (Object)null);
      } else {
         return null;
      }
   }

   private List<BackgroundTask> getBackgroundTasks(long objectDefinitionId, String name) {
      DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
      dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", this.user.getUserId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", this.user.getGroupId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("name", name));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", true));
      Order order = OrderFactoryUtil.desc("modifiedDate");
      dynamicQuery.addOrder(order);
      return BackgroundTaskLocalServiceUtil.dynamicQuery(dynamicQuery);
   }

   public boolean isHaveExportToPDFRole() {
      if (!this.isZoneChecker && !this.isRADZM && !this.isNotingAuthority) {
         if (!this.psrsConfiguration.isExportToExcel()) {
            return false;
         } else {
            Role role = null;

            try {
               role = RoleLocalServiceUtil.getRole(this.user.getCompanyId(), "Export PDF");
            } catch (PortalException e) {
               e.printStackTrace();
            }

            return Validator.isNotNull(role) && UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.user.getUserId());
         }
      } else {
         return true;
      }
   }

   public String getTaskCompletedURL(long objectDefinitionId, String name) {
      try {
         List<BackgroundTask> backgroundTasks = this.getBackgroundTasks(objectDefinitionId, name);
         if (Validator.isNotNull(backgroundTasks) && backgroundTasks.size() > 0) {
            BackgroundTask backgroundTask = (BackgroundTask)backgroundTasks.get(0);
            if (backgroundTask.isCompleted()) {
               String fileType = (String)backgroundTask.getTaskContextMap().get("fileURL");
               return Validator.isNotNull(fileType) ? fileType : "";
            } else {
               return backgroundTask.getStatusLabel();
            }
         } else {
            return "";
         }
      } catch (Exception w) {
         w.printStackTrace();
         return "";
      }
   }

   public void loadUserRoles(User user) {
      Map<String, Long> roleMap = new HashMap();

      for(Role role : user.getRoles()) {
         String roleName = role.getName().toLowerCase().trim();
         roleMap.put(roleName, role.getRoleId());
      }

      if (this.hasBoth(roleMap, "psrs branch maker", "psrs branch checker")) {
         this.isBranchChecker = true;
         this.action = "approve";
         this.currentRole = "psrs branch checker";
      } else if (this.hasBoth(roleMap, "psrs zone maker", "psrs zone checker")) {
         this.isZoneChecker = true;
         this.action = "approve,reject,complete";
         this.currentRole = "psrs zone checker";
      } else if (roleMap.containsKey("psrs branch maker")) {
         this.isBranchMaker = true;
         this.action = "approve";
         this.currentRole = "psrs branch maker";
      } else if (roleMap.containsKey("psrs ra dzm")) {
         this.isRADZM = true;
         this.action = "approve,reject";
         this.currentRole = "psrs ra dzm";
      } else if (roleMap.containsKey("psrs noting authority")) {
         this.isNotingAuthority = true;
         this.action = "Noted & Closed,Noted with Observations,reject";
         this.currentRole = "psrs noting authority";
      } else if (roleMap.containsKey("psrs zone maker")) {
         this.isZoneMaker = true;
         this.action = "approve,complete";
         this.currentRole = "psrs zone maker";
      }

      this._log.info("Final Role Selected: " + this.currentRole + " | Action: " + this.action);
   }

   public JSONObject buildItemJson(ObjectEntry entry, Map<String, String[]> stageFlow) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      String stage = (String)entry.getPropertyValue("collST");
      String[] stages = (String[])stageFlow.get(stage);
      String boiCurrent = stages[0];
      String boiStatus = stages[1];
      if (Validator.isNull(boiCurrent)) {
         this._log.info("boiCurrent is null, setting to blank");
         boiCurrent = "";
      }

      boolean canUpdateComplainceRemarks = false;
      if (boiStatus.equalsIgnoreCase("Pending for Observations Noted") && "psrs branch maker".equalsIgnoreCase(boiCurrent) && ("psrs branch checker".equalsIgnoreCase(this.currentRole) || "psrs branch maker".equalsIgnoreCase(this.currentRole))) {
         this.isObservationNoted = true;
         canUpdateComplainceRemarks = true;
      }

      entry.getProperties().forEach((key, value) -> {
         this._log.info("WORKFLOW  - KEY : " + key + " VALUE : " + value);
         if (!"wrkTsk".equalsIgnoreCase(key) && !"boiCurrent".equalsIgnoreCase(key) && !"collST".equalsIgnoreCase(key)) {
            jsonObject.put(key, value);
         }

      });
      String customAction = "";
      boolean canUpdateRow = false;
      if ("psrs branch maker".equalsIgnoreCase(boiCurrent) && "psrs branch checker".equalsIgnoreCase(this.currentRole) || "psrs zone maker".equalsIgnoreCase(boiCurrent) && "psrs zone checker".equalsIgnoreCase(this.currentRole) || boiCurrent.equalsIgnoreCase(this.currentRole)) {
         canUpdateRow = true;
         customAction = this.action;
         if ("psrs zone maker".equalsIgnoreCase(boiCurrent) && "psrs zone checker".equalsIgnoreCase(this.currentRole)) {
            customAction = (String)Arrays.stream(this.action.split(",")).filter((a) -> !a.equalsIgnoreCase("reject")).collect(Collectors.joining(","));
         }
      }

      jsonObject.put("customAction", customAction);
      jsonObject.put("canUpdateRow", canUpdateRow);
      jsonObject.put("canUpdateComplaince", canUpdateComplainceRemarks);
      jsonObject.put("dateModified", entry.getDateModified());
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      return jsonObject;
   }

   public JSONObject buildItemJsonForSimple(ObjectEntry entry) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      entry.getProperties().forEach((key, value) -> {
         this._log.info("NON-WORKFLOW  - KEY : " + key + " VALUE : " + value);
         if (!"collST".equalsIgnoreCase(key) && !"boiCurrent".equalsIgnoreCase(key)) {
            jsonObject.put(key, value);
         }

      });
      jsonObject.put("customAction", "");
      jsonObject.put("canUpdateRow", true);
      jsonObject.put("dateModified", entry.getDateModified());
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      return jsonObject;
   }

   public JSONArray buildItemJsonArrayMain(Page<ObjectEntry> page, JSONArray items) {
      if (NON_WORKFLOW_REPORTS.contains(this.moduleName)) {
         for(ObjectEntry entry : page.getItems()) {
            JSONObject itemJson = this.buildItemJsonForSimple(entry);
            if (Validator.isNotNull(itemJson)) {
               items.put(itemJson);
            }
         }

         return items;
      } else if (WORKFLOW_REPORTS.contains(this.moduleName)) {
         Map<String, String[]> stageflow = PSRSWorkflow.stageFlow;

         for(ObjectEntry entry : page.getItems()) {
            JSONObject itemJson = this.buildItemJson(entry, stageflow);
            if (Validator.isNotNull(itemJson)) {
               items.put(itemJson);
            }
         }

         return items;
      } else if (CHECKLIST_REPORTS.contains(this.moduleName)) {
         for(ObjectEntry entry : page.getItems()) {
            JSONObject itemJson = this.buildItemJsonForCheckList(entry);
            if (Validator.isNotNull(itemJson)) {
               items.put(itemJson);
            }
         }

         return items;
      } else {
         return items;
      }
   }

   public JSONObject buildItemJsonForCheckList(ObjectEntry entry) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      JSONObject viewJson = JSONFactoryUtil.createJSONObject();
      entry.getProperties().forEach((key, value) -> {
         if (!"nBG".equalsIgnoreCase(key) && !"zone".equalsIgnoreCase(key) && !"branch".equalsIgnoreCase(key)) {
            viewJson.put(key, value);
         } else {
            jsonObject.put(key, value);
         }

      });
      User creator = UserLocalServiceUtil.fetchUser(entry.getCreator().getId());
      jsonObject.put("adid", creator != null ? creator.getScreenName() : "-");
      jsonObject.put("customAction", "view");
      jsonObject.put("dateOfSubmission", entry.getDateModified());
      jsonObject.put("viewFields", viewJson);
      jsonObject.put("dateCreated", entry.getDateCreated());
      jsonObject.put("id", entry.getId());
      return jsonObject;
   }

   public boolean isMonthlyCheckListReport() {
      return CHECKLIST_REPORTS.contains(this.moduleName);
   }

   public boolean isNonWorkflowReport() {
      return NON_WORKFLOW_REPORTS.contains(this.moduleName);
   }

   public boolean isWorkflowReport() {
      return WORKFLOW_REPORTS.contains(this.moduleName);
   }

   public JSONObject validateAndAddData(String reportType, Object[] object, List<Organization> zoneOrg, List<Organization> branchOrg) {
      JSONObject rowError = JSONFactoryUtil.createJSONObject();
      if (object != null && object.length != 0) {
         int rowNumber = 1;
         List<String> dateColumns = this.getDateColumns();
         List<Map<String, Serializable>> validDataList = new ArrayList();

         for(Object obj : object) {
            String subError = "";
            this._log.info("Error " + rowNumber + " " + subError);
            Map<String, Object> map = null;
            if (!(obj instanceof Map)) {
               subError = this.addError(subError, "Invalid row format");
            } else {
               map = this.normalizeKeys((Map)obj);
               String zone = this.getString(map, "zone");
               String branchCode = this.getString(map, "branchCode");
               if (ListUtil.isNotNull(dateColumns)) {
                  for(String date : dateColumns) {
                     Object value = map.get(date);
                     subError = this.addError(subError, this.validateDate(date, value));
                  }
               }

               this._log.info("Error + Date " + rowNumber + " " + subError);
               String zoneError = this.validateZone(zone, zoneOrg);
               String var20 = this.addError(subError, zoneError);
               this._log.info("Error + Zone " + rowNumber + " " + var20);
               long zoneOrgId = zoneError.isEmpty() ? this.getZoneId(zone, zoneOrg) : 0L;
               subError = this.addError(var20, this.validateBranch(map, branchCode, zoneOrgId, branchOrg));
               this._log.info("Error + Branch " + rowNumber + " " + subError);
            }

            if (!subError.isBlank()) {
               this.addError(rowError, "row " + rowNumber, subError);
            } else {
               validDataList.add(this.convertToSerializableMap(map));
            }

            ++rowNumber;
         }

         if (rowError.length() == 0 && !validDataList.isEmpty()) {
            this.triggerBackGroundJobForObject(validDataList, reportType);
         }

         return rowError;
      } else {
         rowError.put("error", "NO DATA FOUND!!!");
         return rowError;
      }
   }

   private Map<String, Serializable> convertToSerializableMap(Map<String, Object> input) {
      Map<String, Serializable> output = new HashMap();

      for(Map.Entry<String, Object> entry : input.entrySet()) {
         Object value = entry.getValue();
         if (value instanceof Serializable) {
            output.put((String)entry.getKey(), (Serializable)value);
         }
      }

      return output;
   }

   private String formatFieldName(String key) {
      if (key != null && !key.isEmpty()) {
         String result = key.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2").replaceAll("_", " ");
         char var10000 = Character.toUpperCase(result.charAt(0));
         return var10000 + result.substring(1);
      } else {
         return key;
      }
   }

   private void triggerBackGroundJobForObject(List<Map<String, Serializable>> dataList, String reportType) {
      if (dataList != null && !dataList.isEmpty()) {
         Map<String, Serializable> taskContextMap = new HashMap();
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setUserId(this.user.getUserId());
         serviceContext.setScopeGroupId(this.user.getGroupId());
         serviceContext.setCompanyId(this.user.getCompanyId());
         taskContextMap.put("data", dataList);
         taskContextMap.put("objectDefinitionId", this.getObjectDefinitionId());
         taskContextMap.put("userId", this.user.getUserId());
         taskContextMap.put("reportType", reportType);
         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
         String taskName = "ExcelADD_" + reportType + "_" + timestamp;

         try {
            BackgroundTaskManagerUtil.addBackgroundTask(this.user.getUserId(), this.user.getGroupId(), taskName, ObjectDataUpload.class.getName(), taskContextMap, serviceContext);
            this._log.info("Background task triggered. ");
         } catch (Exception e) {
            this._log.error("Error while triggering background task", e);
         }

      } else {
         this._log.error("No data provided for background task");
      }
   }

   private Map<String, Object> normalizeKeys(Map<String, Object> map) {
      Map<String, Object> normalized = new HashMap();

      for(Map.Entry<String, Object> entry : map.entrySet()) {
         String key = ((String)entry.getKey()).trim();
         normalized.put(key, entry.getValue());
      }

      return normalized;
   }

   private String getString(Map<String, Object> map, String key) {
      Object val = map.get(key);
      return val != null && !val.toString().trim().isEmpty() ? val.toString().trim() : null;
   }

   private void putString(Map<String, Object> map, String key, Object value) {
      if (value != null && !value.toString().trim().isEmpty()) {
         map.put(key, value.toString().trim());
      }

   }

   private Long getLong(Map<String, Object> map, String key) {
      Object val = map.get(key);
      if (val instanceof Number) {
         return ((Number)val).longValue();
      } else {
         try {
            return val != null ? Long.parseLong(val.toString()) : null;
         } catch (Exception var5) {
            return null;
         }
      }
   }

   private String validateDate(String key, Object value) {
      String subError = "";
      if (!(value instanceof String)) {
         return this.addError(subError, key + " must be a valid date");
      } else {
         String trimmed = ((String)value).trim();
         if (!"-".equals(trimmed) && !trimmed.isEmpty()) {
            if (!this.matchesPattern(trimmed)) {
               return this.addError(subError, key + " must be in format d/M/yyyy or d-M-yyyy");
            } else {
               return !this.parseDateSafely(trimmed).isPresent() ? this.addError(subError, key + " contains an invalid date (use d/M/yyyy or d-M-yyyy)") : subError;
            }
         } else {
            return subError;
         }
      }
   }

   private boolean matchesPattern(String value) {
      return value != null && DATE_PATTERNS.stream().anyMatch((pattern) -> pattern.matcher(value).matches());
   }

   private Optional<LocalDate> parseDateSafely(String input) {
      if (input != null && !input.isBlank()) {
         try {
            return Optional.of(LocalDate.parse(input, F_DD_MM_YYYY));
         } catch (Exception var5) {
            try {
               return Optional.of(LocalDate.parse(input, F_DD_MM_YYYY_DASH));
            } catch (Exception var4) {
               return Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   private String validateZone(String zone, List<Organization> zoneOrg) {
      if (zone != null && !zone.trim().isEmpty()) {
         if (zoneOrg != null && !zoneOrg.isEmpty()) {
            Optional<Organization> orgOpt = zoneOrg.stream().filter(Objects::nonNull).filter((org) -> org.getName() != null && org.getName().equals(zone)).findFirst();
            return orgOpt.isPresent() ? "" : "Invalid zone: '" + zone + "'";
         } else {
            return "No zone data available for validation.";
         }
      } else {
         return "Zone is Missing.";
      }
   }

   private String validateBranch(Map<String, Object> map, String branchCode, long zoneOrgId, List<Organization> branchOrg) {
      if (zoneOrgId <= 0L) {
         return "While Validating Branch.Invalid Zone Id 0.";
      } else {
         if (branchCode != null) {
            branchCode = branchCode.trim();
            if (branchCode.matches("\\d+")) {
               if (branchCode.length() == 5) {
                  branchCode = branchCode.substring(0, 4);
               } else {
                  if (branchCode.length() >= 5) {
                     return "Branch code cannot be greater than 5 digits";
                  }

                  branchCode = String.format("%04d", Integer.parseInt(branchCode));
               }
            }
         }

         if (branchCode != null && branchCode.matches("\\d{4}")) {
            Optional<Organization> branchOpt = branchOrg.stream().filter(Objects::nonNull).filter((x) -> x.getName() != null && x.getName().contains(branchCode)).filter((x) -> x.getParentOrganizationId() == zoneOrgId).findFirst();
            return !branchOpt.isPresent() ? "Invalid branch code: " + branchCode : "";
         } else {
            return "Branch code must be a 4-digit numeric value.";
         }
      }
   }

   private long getZoneId(String zone, List<Organization> zoneOrg) {
      if (zone != null && !zone.trim().isEmpty()) {
         if (zoneOrg != null && !zoneOrg.isEmpty()) {
            Optional<Organization> orgOpt = zoneOrg.stream().filter(Objects::nonNull).filter((org) -> org.getName() != null && org.getName().equals(zone)).findFirst();
            return (Long)orgOpt.map(OrganizationModel::getOrganizationId).orElse(0L);
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   private void addError(JSONObject error, String field, String message) {
      error.put(field, message);
   }

   private String addError(String error, String message) {
      if (message != null && !message.isBlank()) {
         return error != null && !error.isBlank() ? error + ", " + message : message;
      } else {
         return error;
      }
   }

   private JSONObject buildResponse(JSONObject errorArray) {
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();
      responseJson.put("isValid", errorArray.length() == 0);
      responseJson.put("errors", errorArray);
      return responseJson;
   }

   private boolean hasBoth(Map<String, Long> map, String r1, String r2) {
      return map.containsKey(r1) && map.containsKey(r2);
   }

   public String getPdfFromDate() {
      return this.pdfFromDate;
   }

   public String getPdfToDate() {
      return this.pdfToDate;
   }

   public String getReportName() {
      return this.moduleName;
   }

   public String toString() {
      return "PSRSCommon [action=" + this.action + ", currentRole=" + this.currentRole + ", isBranchMaker=" + this.isBranchMaker + ", isBranchChecker=" + this.isBranchChecker + ", isZoneMaker=" + this.isZoneMaker + ", isZoneChecker=" + this.isZoneChecker + ", isRADZM=" + this.isRADZM + ", isNotingAuthority=" + this.isNotingAuthority + ", pdfFromDate=" + this.pdfFromDate + ", pdfToDate=" + this.pdfToDate + ", branchName=" + this.branchName + ", zoneName=" + this.zoneName + ", nbgName=" + this.nbgName + ", hoName=" + this.hoName + ", moduleName=" + this.moduleName + "]";
   }

   static {
      F_DD_MM_YYYY = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
      F_DD_MM_YYYY_DASH = DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT);
      DATE_PATTERNS = List.of(Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}"), Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{4}"));
      NON_WORKFLOW_REPORTS = Set.of("m1", "m2", "m3", "m5");
      WORKFLOW_REPORTS = Set.of("m4", "m6", "m7", "m8");
      CHECKLIST_REPORTS = Set.of("monthlychecklist");
   }

   private static enum ReportType {
      m1,
      m2,
      m3,
      m4,
      m5,
      m6,
      m7,
      m8,
      monthlychecklist;
   }
}
