package boi.headless.delivery.dms.common.internal.resource.v1_0;

import boi.headless.delivery.dms.common.background.task.ArchivalDashboard1Configuration;
import boi.headless.delivery.dms.common.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.dms.common.resource.v1_0.ArchiveDashboard1Resource;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.validation.constraints.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.dms.common.background.task.ArchivalDashboard1Configuration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/archive-dashboard1.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {ArchiveDashboard1Resource.class}
)
public class ArchiveDashboard1ResourceImpl extends BaseArchiveDashboard1ResourceImpl {
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private ArchivalDashboard1Configuration configuration;
   private static final Log log = LogFactoryUtil.getLog(ArchiveDashboard1ResourceImpl.class);

   public BOIHeadlessResponse initArchiveDashboard() throws Exception {
      JSONObject jsonObject = this.getMatchingUserRoles();
      BOIHeadlessResponse response = new BOIHeadlessResponse();
      response.setData(jsonObject.toMap());
      response.setResponseId(200);
      response.setResponseStatus("SUCCESS");
      log.info("matchingUserRoles " + jsonObject);
      return response;
   }

   public BOIHeadlessResponse listArchiveDashboard1(@NotNull String template, String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      BOIHeadlessResponse response = new BOIHeadlessResponse();
      List<Role> roles = this.contextUser.getRoles();
      log.info("Current User Roles " + roles);
      if (roles != null && !roles.isEmpty()) {
         boolean hasAccessToTemplate = this.hasAccessToTemplate(template, roles);
         log.info("hasAccessToTemplate " + hasAccessToTemplate);
         if (!hasAccessToTemplate) {
            jsonObject.put("status", "ERROR");
            jsonObject.put("message", "Permission denied. Please contact the dms support team.");
            response.setData(jsonObject.toMap());
            response.setResponseId(402);
            response.setResponseStatus("ERROR");
            return response;
         } else {
            boolean isHOChecker = roles.stream().anyMatch((x) -> "HO Checker".equalsIgnoreCase(x.getName()));
            String archiveEndPoint = this.configuration.ArchiveEndPoint();
            log.info("archiveEndPoint " + archiveEndPoint);
            String token = this.getAccessToken(archiveEndPoint);
            String userFilterQuery = this.getUserFilterQuery(isHOChecker);
            log.info("userFilterQuery " + userFilterQuery);
            if (!isHOChecker && Validator.isBlank(userFilterQuery)) {
               response.setData(jsonObject.toMap());
               response.setResponseId(402);
               response.setResponseStatus("ERROR");
               return response;
            } else {
               JSONObject archiveJsonObject = getArchiveDashboard(template, userFilterQuery, search, filter, pagination, sorts, token, archiveEndPoint);
               log.info(archiveJsonObject);
               response.setData(archiveJsonObject.toMap());
               response.setResponseId(200);
               response.setResponseStatus("SUCCESS");
               return response;
            }
         }
      } else {
         jsonObject.put("status", "ERROR");
         jsonObject.put("message", "Permission denied. Please contact the dms support team.");
         response.setData(jsonObject.toMap());
         response.setResponseId(402);
         response.setResponseStatus("ERROR");
         return response;
      }
   }

   private static JSONObject getArchiveDashboard(String template, String userFilterQuery, String search, Filter filter, Pagination pagination, Sort[] sorts, String accessToken, String endPoints) throws IOException, JSONException {
      int page = pagination != null ? pagination.getPage() : 0;
      int pageSize = pagination != null ? pagination.getPageSize() : 20;
      StringBuilder urlBuilder = new StringBuilder(endPoints + "/o/boi-headless-delivery-archival-common/v1.0/list-archive/" + template + "?");
      if (search != null && !search.isEmpty()) {
         urlBuilder.append("search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8.name()));
      }

      urlBuilder.append("&pageSize=").append(pageSize).append("&page=").append(page);
      if (filter != null) {
         String filterStr = filter.toString();
         urlBuilder.append("&filter=").append(URLEncoder.encode(filterStr, StandardCharsets.UTF_8.name()));
      }

      urlBuilder.append("&filterQuery=").append(URLEncoder.encode(userFilterQuery, StandardCharsets.UTF_8.name()));
      if (sorts != null && sorts.length > 0) {
         StringBuilder sortStr = new StringBuilder();

         for(int i = 0; i < sorts.length; ++i) {
            sortStr.append(sorts[i].getFieldName()).append(":").append(sorts[i].isReverse() ? "desc" : "asc");
            if (i < sorts.length - 1) {
               sortStr.append(",");
            }
         }

         urlBuilder.append("&sort=").append(URLEncoder.encode(sortStr.toString(), StandardCharsets.UTF_8.name()));
      }

      URL url = new URL(urlBuilder.toString());
      log.info("url " + url);
      log.info("accessToken " + accessToken.length());
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Cookie", "COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US");
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(600000);
      int responseCode = conn.getResponseCode();
      log.info("responseCode " + responseCode);
      BufferedReader reader;
      if (responseCode >= 200 && responseCode < 300) {
         reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      } else {
         reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
      }

      StringBuilder responseStr = new StringBuilder();

      String line;
      while((line = reader.readLine()) != null) {
         responseStr.append(line);
      }

      reader.close();
      conn.disconnect();
      if (responseCode >= 200 && responseCode < 300) {
         return JSONFactoryUtil.createJSONObject(responseStr.toString());
      } else {
         throw new IOException("HTTP error code: " + responseCode + ", response: " + responseStr.toString());
      }
   }

   public JSONObject getMatchingUserRoles() {
      JSONObject result = JSONFactoryUtil.createJSONObject();
      JSONArray rolesArray = JSONFactoryUtil.createJSONArray();
      String archiveEndPoint = this.configuration.ArchiveEndPoint();

      try {
         Set<String> targetRoles = (Set)Arrays.stream(this.configuration.ArchiveTemplateNames().split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toSet());
         this.contextUser.getRoles().forEach((role) -> {
            String roleName = role.getName();
            if (targetRoles.contains(roleName)) {
               JSONObject obj = JSONFactoryUtil.createJSONObject();
               obj.put("name", roleName);
               obj.put("listUrl", "https://stardms.bankofindia.co.in/o/boi-headless-delivery-dms-common/v1.0/list-archive-dashboard-1/" + roleName);
               rolesArray.put(obj);
            }

         });
         result.put("archiveData", rolesArray);
      } catch (Exception var5) {
         result.put("archiveData", JSONFactoryUtil.createJSONArray());
      }

      return result;
   }

   public boolean hasAccessToTemplate(String templateName, List<Role> roles) {
      if (!Validator.isBlank(templateName) && roles != null) {
         String normalizedTemplate = templateName.trim().toLowerCase();
         Set<String> allowedRoles = (Set)Arrays.stream(this.configuration.ArchiveTemplateNames().split(",")).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
         return roles.stream().map(RoleModel::getName).filter(Objects::nonNull).map(String::toLowerCase).anyMatch((role) -> role.equals(normalizedTemplate) && allowedRoles.contains(role));
      } else {
         return false;
      }
   }

   private String getAccessToken(String endPoint) throws IOException, JSONException {
      String urlStr = endPoint + "/o/customer-account-headless-delivery/v1.0/get-token";
      URL url = new URL(urlStr);
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", this.configuration.ArchiveBasicAuth());
      conn.setDoInput(true);
      conn.setConnectTimeout(180000);
      conn.setReadTimeout(180000);
      int responseCode = conn.getResponseCode();
      InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
      String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      conn.disconnect();
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject(responseBody);
      String token = jsonObject.getJSONObject("data").getString("access_token");
      return token;
   }

   public String getUserFilterQuery(boolean isHOChecker) {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      if (!isHOChecker) {
         this.addHierarchyFilter(queryList, this.contextUser);
      }

      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      if (isHOChecker) {
         return queryResult;
      } else if (this.containsHierarchyFilters(queryResult)) {
         log.debug("Final queryResult:  " + queryResult);
         return queryResult;
      } else {
         log.debug("Final queryResult:  Blank");
         return "";
      }
   }

   private Organization getOrganization(String type, Organization organization) {
      if (Validator.isNotNull(organization) && organization.getType().equalsIgnoreCase(type)) {
         return organization;
      } else {
         if (Validator.isNotNull(organization) && organization.getParentOrganizationId() > 0L) {
            try {
               return this.getOrganization(type, organization.getParentOrganization());
            } catch (PortalException e) {
               log.error(e);
            }
         }

         return null;
      }
   }

   private Organization getOrganization(List<Organization> organizations, String type) {
      for(Organization organization : organizations) {
         Organization organization2 = this.getOrganization(type, organization);
         if (Validator.isNotNull(organization2)) {
            return organization2;
         }
      }

      return null;
   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + (String)isoRange.get("toDate");
         log.debug("Request required dateFilter  " + dateFilter);
         queryList.add(dateFilter);
      }

   }

   public Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      LocalDate fromDate = LocalDate.parse(fromDateStr);
      LocalDate toDate = LocalDate.parse(toDateStr);
      ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime toDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("fromDate", isoFormatter.format(fromDateTime));
      result.put("toDate", isoFormatter.format(toDateTime));
      return result;
   }

   private boolean containsHierarchyFilters(String queryResult) {
      return queryResult.contains("branchCode") || queryResult.contains("nBGCode") || queryResult.contains("zone");
   }

   private void addHierarchyFilter(List<String> queryList, User user) {
      Organization branchOrg = null;
      Organization zoneOrg = null;
      Organization nbgOrg = null;

      try {
         List<Organization> orgs = user.getOrganizations();
         branchOrg = this.getOrganization(orgs, "BRANCH");
         zoneOrg = this.getOrganization(orgs, "ZONE");
         nbgOrg = this.getOrganization(orgs, "NBG");
      } catch (PortalException e) {
         log.error("Error fetching organizations", e);
      }

      if (Validator.isNotNull(branchOrg)) {
         queryList.add("branchCode eq '" + branchOrg.getName() + "'");
      } else if (Validator.isNotNull(zoneOrg)) {
         queryList.add("zone eq '" + zoneOrg.getName() + "'");
      } else {
         if (Validator.isNotNull(nbgOrg)) {
            queryList.add("nBGCode eq '" + nbgOrg.getName() + "'");
         }

      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.configuration = (ArchivalDashboard1Configuration)ConfigurableUtil.createConfigurable(ArchivalDashboard1Configuration.class, properties);
   }
}
