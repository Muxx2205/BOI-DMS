package boi.headless.delivery.dms.common.internal.resource.v1_0;

import boi.headless.delivery.dms.common.background.task.FeboDashboardConfiguration;
import boi.headless.delivery.dms.common.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.dms.common.resource.v1_0.FeboDashboardResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.Query;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.dms.common.background.task.FeboDashboardConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/febo-dashboard.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FeboDashboardResource.class}
)
public class FeboDashboardResourceImpl extends BaseFeboDashboardResourceImpl {
   private FeboDashboardConfiguration configuration;
   private static final Log _log = LogFactoryUtil.getLog(FeboDashboardResourceImpl.class);
   @Reference
   private DTOConverterRegistry dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;
   @Reference
   private Searcher _searcher;
   @Reference
   private SearchRequestBuilderFactory _searchRequestBuilderFactory;
   @Reference
   private Queries _queries;

   public BOIHeadlessResponse preData(Object object) throws Exception {
      BOIHeadlessResponse response = new BOIHeadlessResponse();
      JSONArray orgArray = JSONFactoryUtil.createJSONArray();
      JSONObject jsonObject = null;
      if (object == null) {
         throw new IllegalArgumentException("Input object cannot be null");
      } else {
         if (object instanceof JSONObject) {
            jsonObject = (JSONObject)object;
         } else if (object instanceof String) {
            jsonObject = JSONFactoryUtil.createJSONObject((String)object);
         } else {
            if (!(object instanceof Map)) {
               throw new IllegalArgumentException("Unsupported object type: " + object.getClass());
            }

            jsonObject = JSONFactoryUtil.createJSONObject((Map)object);
         }

         _log.info("Input JSON: " + jsonObject);
         long organizationId = jsonObject.getLong("organizationId", 0L);
         _log.info(", organizationId: " + organizationId);
         if (organizationId == 0L) {
            _log.info("Input object is NULL â†’ Fetching all NBG");

            for(Organization org : OrganizationLocalServiceUtil.getSuborganizations(this.contextUser.getCompanyId(), this.configuration.parentOrgId())) {
               JSONObject obj = JSONFactoryUtil.createJSONObject();
               obj.put("organizationId", org.getOrganizationId());
               obj.put("name", org.getName());
               orgArray.put(obj);
            }

            return this.buildResponse(response, orgArray, "NBG List fetched");
         } else {
            Organization parent = OrganizationLocalServiceUtil.getOrganization(organizationId);

            for(Organization org : OrganizationLocalServiceUtil.getSuborganizations(this.contextUser.getCompanyId(), parent.getOrganizationId())) {
               JSONObject obj = JSONFactoryUtil.createJSONObject();
               obj.put("organizationId", org.getOrganizationId());
               obj.put("name", org.getName());
               orgArray.put(obj);
            }

            return this.buildResponse(response, orgArray, "Data fetched successfully");
         }
      }
   }

   private BOIHeadlessResponse buildResponse(BOIHeadlessResponse response, JSONArray data, String message) {
      JSONObject result = JSONFactoryUtil.createJSONObject();
      result.put("organizations", data);
      response.setData(result.toMap());
      response.setResponseId(200);
      response.setResponseStatus("SUCCESS");
      _log.info(message + " | Count: " + data.length());
      return response;
   }

   public BOIHeadlessResponse getFeboDashboard(Object object) throws Exception {
      BOIHeadlessResponse response = new BOIHeadlessResponse();
      if (object == null) {
         throw new IllegalArgumentException("Input object is null");
      } else {
         JSONObject jsonObject;
         if (object instanceof JSONObject) {
            jsonObject = (JSONObject)object;
         } else if (object instanceof String) {
            jsonObject = JSONFactoryUtil.createJSONObject((String)object);
         } else {
            if (!(object instanceof Map)) {
               throw new IllegalArgumentException("Unsupported object type: " + object.getClass());
            }

            jsonObject = JSONFactoryUtil.createJSONObject((Map)object);
         }

         List<Role> roles = this.contextUser.getRoles();
         if (roles != null && !roles.isEmpty()) {
            boolean hasDashboardReportRole = roles.stream().anyMatch((role) -> "FBD FE BO DASHBOARD".equalsIgnoreCase(role.getName()));
            if (!hasDashboardReportRole) {
               jsonObject.put("status", "ERROR");
               jsonObject.put("message", "Permission denied. Please contact the dms support team.");
               response.setData(jsonObject.toMap());
               response.setResponseId(402);
               response.setResponseStatus("ERROR");
               return response;
            } else {
               boolean isAll = "true".equalsIgnoreCase(jsonObject.getString("ALL", ""));
               boolean hideMigrated = this.configuration.hideMigratedCount();
               ObjectDefinition objectDefinition = null;

               try {
                  objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(this.configuration.FeboObjectDefinitionId());
               } catch (PortalException e) {
                  jsonObject.put("status", "ERROR");
                  jsonObject.put("message", "Unable to fetch ObjectDefinition" + e.getMessage());
                  response.setData(jsonObject.toMap());
                  response.setResponseId(402);
                  response.setResponseStatus("ERROR");
                  return response;
               }

               if (isAll) {
                  _log.info("FEBO DashBoard Calling...");
                  BOIHeadlessResponse generateResultAll = this.generateResultAll(response, objectDefinition, hideMigrated);
                  return generateResultAll;
               } else {
                  _log.info("FEBO DashBoard Filter Calling...");
                  BOIHeadlessResponse processFilters = this.processFilters(jsonObject, response, objectDefinition, hideMigrated);
                  return processFilters;
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
   }

   public BOIHeadlessResponse processFilters(JSONObject jsonObject, BOIHeadlessResponse response, ObjectDefinition objectDefinition, boolean hideMigrated) {
      String typeOfRemittance = this.getString(jsonObject, "typeOfRemittance");
      String branchCode = this.getString(jsonObject, "branchCode");
      String zone = this.getString(jsonObject, "zone");
      String nbgCode = this.getString(jsonObject, "nbgCode");
      String modifiedDateFrom = this.getString(jsonObject, "modifiedDateFrom");
      String modifiedDateTo = this.getString(jsonObject, "modifiedDateTo");
      String createDateFrom = this.getString(jsonObject, "createDateFrom");
      String createDateTo = this.getString(jsonObject, "createDateTo");
      BOIHeadlessResponse generateResultFilter = this.generateResultFilter(typeOfRemittance, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo, response, objectDefinition, hideMigrated);
      return generateResultFilter;
   }

   private String getString(JSONObject jsonObject, String key) {
      if (jsonObject.has(key) && !jsonObject.isNull(key)) {
         String value = jsonObject.getString(key, "").trim();
         return value.isEmpty() ? null : value;
      } else {
         return null;
      }
   }

   private BOIHeadlessResponse generateResultFilter(String typeOfRemittance, String branchCode, String zone, String nbgCode, String modifiedDateFrom, String modifiedDateTo, String createDateFrom, String createDateTo, BOIHeadlessResponse response, ObjectDefinition objectDefinition, boolean hideMigrated) {
      _log.info("===== Starting generateResultFilter() =====");
      _log.info("Input Params -> typeOfRemittance: " + typeOfRemittance + ", branchCode: " + branchCode + ", zone: " + zone + ", nbgCode: " + nbgCode + ", modifiedDateFrom: " + modifiedDateFrom + ", modifiedDateTo: " + modifiedDateTo + ", createDateFrom: " + createDateFrom + ", createDateTo: " + createDateTo);
      JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
      JSONArray jsonArrayFY = JSONFactoryUtil.createJSONArray();
      JSONObject parentJson = JSONFactoryUtil.createJSONObject();
      Map<String, String> stageMap = (Map)Arrays.stream(this.configuration.StageValue().split(";")).map(String::trim).filter((s) -> !s.isEmpty()).map((entryx) -> entryx.split("=")).filter((arr) -> arr.length == 2).collect(Collectors.toMap((arr) -> arr[0].trim(), (arr) -> arr[1].trim(), (a, b) -> a));
      _log.info("Stage Map: " + stageMap);
      JSONObject stageJson = JSONFactoryUtil.createJSONObject();
      long total = 0L;

      for(Map.Entry<String, String> entry : stageMap.entrySet()) {
         String stageKey = (String)entry.getKey();
         String stageValue = (String)entry.getValue();
         long totalCountForStage = 0L;
         if (stageKey.contains(",")) {
            String[] keys = stageKey.split(",");

            for(String k : keys) {
               Integer key = Integer.parseInt(k.trim());
               Log var10000 = _log;
               String var10001 = k.trim();
               var10000.info("Processing Stage Key: " + var10001 + ", Value: " + stageValue);
               totalCountForStage += hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, typeOfRemittance, key, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo) : this.getCountWithFilters(objectDefinition, typeOfRemittance, key, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo);
            }
         } else {
            Integer key = Integer.parseInt(stageKey.trim());
            _log.info("Processing Stage Key: " + key + ", Value: " + stageValue);
            totalCountForStage = hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, typeOfRemittance, key, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo) : this.getCountWithFilters(objectDefinition, typeOfRemittance, key, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo);
         }

         _log.info("Processing Stage -> Key: " + stageKey + ", Value: " + stageValue);
         _log.info("Stage Count [" + stageValue + "] = " + totalCountForStage);
         total += totalCountForStage;
         stageJson.put(stageValue, totalCountForStage);
      }

      _log.info("Total Object Entries Count: " + total);
      stageJson.put("total", total);
      parentJson.put("statusCounts", stageJson);
      long totalAll = 0L;
      long totalAllFY = 0L;
      int year = LocalDate.now().getYear();
      int month = LocalDate.now().getMonthValue();
      String fyFrom = month >= 4 ? "01-04-" + year : "01-04-" + (year - 1);
      String fyTo = month >= 4 ? "31-03-" + (year + 1) : "31-03-" + year;
      String modifiedFromDateFY = null;
      String modifiedToDateFY = null;
      String createdFromDateFY = null;
      String createdToDateFY = null;
      if (Validator.isNotNull(modifiedDateFrom) && Validator.isNotNull(modifiedDateTo)) {
         modifiedFromDateFY = fyFrom;
         modifiedToDateFY = fyTo;
      } else if (Validator.isNotNull(createDateFrom) && Validator.isNotNull(createDateTo)) {
         createdFromDateFY = fyFrom;
         createdToDateFY = fyTo;
      } else {
         modifiedFromDateFY = fyFrom;
         modifiedToDateFY = fyTo;
      }

      if (typeOfRemittance != null) {
         _log.info("Filtering specifically for typeOfRemittance: " + typeOfRemittance);
         String trimmedName = typeOfRemittance.trim();
         String id = typeOfRemittance.contains("_") ? typeOfRemittance.split("_")[0].trim() : typeOfRemittance;
         long countWithFilters = hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, typeOfRemittance, -1, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo) : this.getCountWithFilters(objectDefinition, typeOfRemittance, -1, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo);
         totalAll += countWithFilters;
         JSONObject subJson = JSONFactoryUtil.createJSONObject();
         subJson.put("id", id);
         subJson.put("name", trimmedName);
         subJson.put("label", trimmedName);
         subJson.put("currentSelection", countWithFilters);
         jsonArray.put(subJson);
         _log.info("Current Financial Year : " + fyFrom + " To " + fyTo);
         long countForFY = hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, typeOfRemittance, -1, branchCode, zone, nbgCode, modifiedFromDateFY, modifiedToDateFY, createdFromDateFY, createdToDateFY) : this.getCountWithFilters(objectDefinition, typeOfRemittance, -1, branchCode, zone, nbgCode, modifiedFromDateFY, modifiedToDateFY, createdFromDateFY, createdToDateFY);
         totalAllFY += countForFY;
         JSONObject subJsonFY = JSONFactoryUtil.createJSONObject();
         subJsonFY.put("id", id);
         subJsonFY.put("name", trimmedName);
         subJsonFY.put("label", trimmedName);
         subJsonFY.put("currentFY", countForFY);
         jsonArrayFY.put(subJsonFY);
      } else {
         Set<String> typeOfRemittanceNames = (Set)Arrays.stream(this.configuration.TypeOfRemittanceNames().split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toSet());
         _log.info("Type Of Remittance Names: " + typeOfRemittanceNames);
         _log.info("Current Financial Year : " + fyFrom + " To " + fyTo);

         for(String name : typeOfRemittanceNames) {
            String trimmedName = name.trim();
            String id = trimmedName.contains("_") ? trimmedName.split("_")[0].trim() : trimmedName;
            _log.info("Processing Remittance -> Name: " + trimmedName + ", ID: " + id);
            long totalCountForRemittance = hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, name, -1, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo) : this.getCountWithFilters(objectDefinition, name, -1, branchCode, zone, nbgCode, modifiedDateFrom, modifiedDateTo, createDateFrom, createDateTo);
            _log.info("Count for [" + trimmedName + "] = " + totalCountForRemittance);
            totalAll += totalCountForRemittance;
            JSONObject subJson = JSONFactoryUtil.createJSONObject();
            subJson.put("id", id);
            subJson.put("name", trimmedName);
            subJson.put("label", trimmedName);
            subJson.put("currentSelection", totalCountForRemittance);
            jsonArray.put(subJson);
            long totalCountForRemittanceFY = hideMigrated ? this.getCountWithFiltersIfMigrateDisabled(objectDefinition, name, -1, branchCode, zone, nbgCode, modifiedFromDateFY, modifiedToDateFY, createdFromDateFY, createdToDateFY) : this.getCountWithFilters(objectDefinition, name, -1, branchCode, zone, nbgCode, modifiedFromDateFY, modifiedToDateFY, createdFromDateFY, createdToDateFY);
            _log.info("Count for FY [" + trimmedName + "] = " + totalCountForRemittanceFY);
            totalAllFY += totalCountForRemittanceFY;
            JSONObject subJsonFY = JSONFactoryUtil.createJSONObject();
            subJsonFY.put("id", id);
            subJsonFY.put("name", trimmedName);
            subJsonFY.put("label", trimmedName);
            subJsonFY.put("currentFY", totalCountForRemittanceFY);
            jsonArrayFY.put(subJsonFY);
         }
      }

      _log.info("Total Count for ALL Remittances = " + totalAll);
      JSONObject allJson = JSONFactoryUtil.createJSONObject();
      allJson.put("id", "ALL");
      allJson.put("name", "ALL");
      allJson.put("label", "ALL");
      allJson.put("currentSelection", totalAll);
      jsonArray.put(allJson);
      JSONObject allJsonFY = JSONFactoryUtil.createJSONObject();
      allJsonFY.put("id", "ALL");
      allJsonFY.put("name", "ALL");
      allJsonFY.put("label", "ALL");
      allJsonFY.put("currentFY", totalAllFY);
      jsonArrayFY.put(allJsonFY);
      parentJson.put("currentFYData", jsonArrayFY);
      parentJson.put("selectionData", jsonArray);
      _log.info("Parent Json = " + parentJson);
      response.setData(parentJson.toMap());
      response.setResponseId(200);
      response.setResponseStatus("SUCCESS");
      _log.info("===== generateResultFilter() completed successfully =====");
      return response;
   }

   private BOIHeadlessResponse generateResultAll(BOIHeadlessResponse response, ObjectDefinition objectDefinition, boolean hideMigrated) {
      _log.info("Starting generateResultAll()");
      JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
      JSONObject parentJson = JSONFactoryUtil.createJSONObject();
      Map<String, String> stageMap = (Map)Arrays.stream(this.configuration.StageValue().split(";")).map(String::trim).filter((s) -> !s.isEmpty()).map((entry) -> entry.split("=")).filter((arr) -> arr.length == 2).collect(Collectors.toMap((arr) -> arr[0].trim(), (arr) -> arr[1].trim(), (a, b) -> a));
      _log.info("Stage Map: " + stageMap);
      JSONObject stageJson = JSONFactoryUtil.createJSONObject();
      long[] total = new long[]{0L};
      stageMap.forEach((key, value) -> {
         _log.info("Processing Stage Key: " + key + ", Value: " + value);
         long totalCountForStage = 0L;
         if (key.contains(",")) {
            String[] keys = key.split(",");

            for(String k : keys) {
               Integer stageKey = Integer.parseInt(k.trim());
               Log var10000 = _log;
               String var10001 = k.trim();
               var10000.info("Processing Stage Key: " + var10001 + ", Value: " + value);
               totalCountForStage += hideMigrated ? this.getCountAllIfMigrateDisabled(objectDefinition, (String)null, stageKey) : this.getCountALL(objectDefinition, (String)null, stageKey);
            }
         } else {
            Integer stageKey = Integer.parseInt(key.trim());
            _log.info("Processing Stage Key: " + key + ", Value: " + value);
            totalCountForStage = hideMigrated ? this.getCountAllIfMigrateDisabled(objectDefinition, (String)null, stageKey) : this.getCountALL(objectDefinition, (String)null, stageKey);
         }

         _log.info("Total Count for Stage [" + value + "] = " + totalCountForStage);
         total[0] += totalCountForStage;
         stageJson.put(value, totalCountForStage);
      });
      _log.info("Final Total Count = " + total[0]);
      stageJson.put("total", total[0]);
      parentJson.put("statusCounts", stageJson);
      Set<String> typeOfRemittanceNames = (Set)Arrays.stream(this.configuration.TypeOfRemittanceNames().split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toSet());
      _log.info("Type Of Remittance Names: " + typeOfRemittanceNames);
      long totalAll = 0L;

      for(String name : typeOfRemittanceNames) {
         String trimmedName = name.trim();
         String id = trimmedName.contains("_") ? trimmedName.split("_")[0].trim() : trimmedName;
         _log.info("Processing Remittance: " + trimmedName + ", ID: " + id);
         long totalCountForRemittance = hideMigrated ? this.getCountAllIfMigrateDisabled(objectDefinition, trimmedName, -1) : this.getCountALL(objectDefinition, trimmedName, -1);
         _log.info("Total Count for Remittance [" + trimmedName + "] = " + totalCountForRemittance);
         totalAll += totalCountForRemittance;
         JSONObject subJson = JSONFactoryUtil.createJSONObject();
         subJson.put("id", id);
         subJson.put("name", trimmedName);
         subJson.put("label", trimmedName);
         subJson.put("totalCount", totalCountForRemittance);
         jsonArray.put(subJson);
      }

      _log.info("Total Count for ALL Remittances = " + totalAll);
      JSONObject allJson = JSONFactoryUtil.createJSONObject();
      allJson.put("id", "ALL");
      allJson.put("name", "ALL");
      allJson.put("label", "ALL");
      allJson.put("totalCount", totalAll);
      jsonArray.put(allJson);
      parentJson.put("TotalData", jsonArray);
      response.setData(parentJson.toMap());
      response.setResponseId(200);
      response.setResponseStatus("SUCCESS");
      _log.info("generateResultAll() completed successfully");
      return response;
   }

   private long getTotalCountForRemittance(String blueprintERC, String typeOfRemittance, String typeOfRemittanceFieldName) {
      try {
         long startTime = System.currentTimeMillis();
         SearchRequestBuilder searchRequestBuilder = this._searchRequestBuilderFactory.builder();
         searchRequestBuilder.companyId(this.contextCompany.getCompanyId());
         searchRequestBuilder.emptySearchEnabled(true);
         searchRequestBuilder.from(0);
         searchRequestBuilder.size(1);
         searchRequestBuilder.fetchSource(false);
         searchRequestBuilder.withSearchContext((searchContext) -> {
            searchContext.setAttribute("search.experiences.blueprint.external.reference.code", blueprintERC);
            searchContext.setAttribute("search.experiences.scope.group.id", "0");
            searchContext.setAttribute("search.empty.search", "true");
            searchContext.setStart(0);
            searchContext.setCompanyId(this.contextCompany.getCompanyId());
            searchContext.setEnd(1);
            searchContext.setGroupIds(new long[]{0L});
            if (Validator.isNotNull(this.contextCompany.getCompanyId())) {
               searchContext.setAttribute(typeOfRemittanceFieldName, typeOfRemittance);
            }

         });
         SearchRequest searchRequest = searchRequestBuilder.build();
         SearchResponse searchResponse = this._searcher.search(searchRequest);
         long elapsed = System.currentTimeMillis() - startTime;
         long totalHits = searchResponse != null ? (long)searchResponse.getTotalHits() : 0L;
         _log.debug("Blueprint " + blueprintERC + " with stage  returned " + totalHits + " in " + elapsed + "ms");
         return totalHits;
      } catch (Exception e) {
         _log.error("Error getting count with blueprint: " + blueprintERC + " for stage: " + e);
         return 0L;
      }
   }

   private long getTotalCountForStage(String blueprintERC, Integer stage, String stageFieldName) {
      try {
         long startTime = System.currentTimeMillis();
         SearchRequestBuilder searchRequestBuilder = this._searchRequestBuilderFactory.builder();
         searchRequestBuilder.companyId(this.contextCompany.getCompanyId());
         searchRequestBuilder.emptySearchEnabled(true);
         searchRequestBuilder.from(0);
         searchRequestBuilder.size(1);
         searchRequestBuilder.fetchSource(false);
         searchRequestBuilder.withSearchContext((searchContext) -> {
            searchContext.setAttribute("search.experiences.blueprint.external.reference.code", blueprintERC);
            searchContext.setAttribute("search.experiences.scope.group.id", "0");
            searchContext.setAttribute("search.empty.search", "true");
            searchContext.setStart(0);
            searchContext.setEnd(1);
            searchContext.setCompanyId(this.contextCompany.getCompanyId());
            searchContext.setGroupIds(new long[]{0L});
            if (stage != null) {
               searchContext.setAttribute(stageFieldName, stage);
            }

         });
         SearchRequest searchRequest = searchRequestBuilder.build();
         SearchResponse searchResponse = this._searcher.search(searchRequest);
         long elapsed = System.currentTimeMillis() - startTime;
         long totalHits = searchResponse != null ? (long)searchResponse.getTotalHits() : 0L;
         _log.debug("Blueprint " + blueprintERC + " with stage " + stage + " returned " + totalHits + " in " + elapsed + "ms");
         return totalHits;
      } catch (Exception e) {
         _log.error("Error getting count with blueprint: " + blueprintERC + " for stage: " + stage, e);
         return 0L;
      }
   }

   private long getCountALL(ObjectDefinition objectDefinition, String typeOfRemittance, Integer stage) {
      try {
         BooleanQuery mainQuery = this._queries.booleanQuery();
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("objectDefinitionId", objectDefinition.getObjectDefinitionId())});
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("entryClassName", objectDefinition.getClassName())});
         if (Validator.isNotNull(typeOfRemittance) && stage == -1) {
            BooleanQuery typeQuery = this._queries.booleanQuery();
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "typeOfRemittance")});
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", typeOfRemittance.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", typeQuery)});
            BooleanQuery outer = this._queries.booleanQuery();
            BooleanQuery stageQuery = this._queries.booleanQuery();
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
            stageQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 0)});
            outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            BooleanQuery migrateQuery = this._queries.booleanQuery();
            migrateQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
            migrateQuery.addMustQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
            migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
            migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
            outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateQuery)});
            outer.setMinimumShouldMatch(1);
            mainQuery.addMustQueryClauses(new Query[]{outer});
         } else if (Validator.isNull(typeOfRemittance) && stage != -1) {
            if (stage == 4) {
               BooleanQuery outer = this._queries.booleanQuery();
               BooleanQuery stageFourQuery = this._queries.booleanQuery();
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 4)});
               outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageFourQuery)});
               BooleanQuery migrateQuery = this._queries.booleanQuery();
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
               outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateQuery)});
               outer.setMinimumShouldMatch(1);
               mainQuery.addMustQueryClauses(new Query[]{outer});
            } else {
               BooleanQuery stageQuery = this._queries.booleanQuery();
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", stage)});
               mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            }
         }

         _log.info("Final ES Query: " + mainQuery.toString());
         SearchRequest searchRequest = this._searchRequestBuilderFactory.builder().companyId(this.contextCompany.getCompanyId()).emptySearchEnabled(true).from(0).size(1).withSearchContext((searchContext) -> {
            searchContext.setStart(0);
            searchContext.setEnd(1);
         }).query(mainQuery).build();
         SearchResponse response = this._searcher.search(searchRequest);
         return (long)response.getTotalHits();
      } catch (Exception e) {
         _log.error("Error fetching count", e);
         return 0L;
      }
   }

   private long getCountAllIfMigrateDisabled(ObjectDefinition objectDefinition, String typeOfRemittance, Integer stage) {
      try {
         BooleanQuery mainQuery = this._queries.booleanQuery();
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("objectDefinitionId", objectDefinition.getObjectDefinitionId())});
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("entryClassName", objectDefinition.getClassName())});
         if (Validator.isNotNull(typeOfRemittance)) {
            BooleanQuery typeQuery = this._queries.booleanQuery();
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "typeOfRemittance")});
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", typeOfRemittance.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", typeQuery)});
         }

         if (stage != null && stage == -1) {
            BooleanQuery outer = this._queries.booleanQuery();
            BooleanQuery stageQuery = this._queries.booleanQuery();
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
            stageQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 0)});
            outer.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            BooleanQuery migrateBlankQuery = this._queries.booleanQuery();
            migrateBlankQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
            BooleanQuery blankCondition = this._queries.booleanQuery();
            blankCondition.addShouldQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
            blankCondition.addShouldQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
            BooleanQuery notExistsQuery = this._queries.booleanQuery();
            notExistsQuery.addMustNotQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
            blankCondition.addShouldQueryClauses(new Query[]{notExistsQuery});
            blankCondition.setMinimumShouldMatch(1);
            migrateBlankQuery.addMustQueryClauses(new Query[]{blankCondition});
            outer.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateBlankQuery)});
            mainQuery.addMustQueryClauses(new Query[]{outer});
         } else if (stage != null && stage != -1) {
            BooleanQuery stageQuery = this._queries.booleanQuery();
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", stage)});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
         }

         _log.info("Final ES Query: " + mainQuery.toString());
         SearchRequest searchRequest = this._searchRequestBuilderFactory.builder().companyId(this.contextCompany.getCompanyId()).emptySearchEnabled(true).from(0).size(1).withSearchContext((searchContext) -> {
            searchContext.setStart(0);
            searchContext.setEnd(1);
         }).query(mainQuery).build();
         SearchResponse response = this._searcher.search(searchRequest);
         return (long)response.getTotalHits();
      } catch (Exception e) {
         _log.error("Error fetching count", e);
         return 0L;
      }
   }

   private long getCountWithFilters(ObjectDefinition objectDefinition, String typeOfRemittance, Integer stage, String branchCode, String zone, String nbgCode, String modifiedDateFrom, String modifiedDateTo, String createDateFrom, String createDateTo) {
      try {
         BooleanQuery mainQuery = this._queries.booleanQuery();
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("objectDefinitionId", objectDefinition.getObjectDefinitionId())});
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("entryClassName", objectDefinition.getClassName())});
         if (Validator.isNotNull(typeOfRemittance) && stage != null && stage == -1) {
            BooleanQuery typeQuery = this._queries.booleanQuery();
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "typeOfRemittance")});
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", typeOfRemittance.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", typeQuery)});
            BooleanQuery outer = this._queries.booleanQuery();
            BooleanQuery stageQuery = this._queries.booleanQuery();
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
            stageQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 0)});
            outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            BooleanQuery migrateQuery = this._queries.booleanQuery();
            migrateQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
            migrateQuery.addMustQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
            migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
            migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
            outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateQuery)});
            outer.setMinimumShouldMatch(1);
            mainQuery.addMustQueryClauses(new Query[]{outer});
         } else if (Validator.isNotNull(typeOfRemittance) && stage != null && stage != -1) {
            BooleanQuery typeQuery = this._queries.booleanQuery();
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "typeOfRemittance")});
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", typeOfRemittance.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", typeQuery)});
            if (stage != 4) {
               BooleanQuery stageQuery = this._queries.booleanQuery();
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", stage)});
               mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            } else if (stage == 4) {
               BooleanQuery outerQuery = this._queries.booleanQuery();
               BooleanQuery stageFourQuery = this._queries.booleanQuery();
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 4)});
               outerQuery.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageFourQuery)});
               BooleanQuery migrateQuery = this._queries.booleanQuery();
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
               outerQuery.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateQuery)});
               outerQuery.setMinimumShouldMatch(1);
               mainQuery.addMustQueryClauses(new Query[]{outerQuery});
            }
         } else if (Validator.isNull(typeOfRemittance) && stage != null && stage != -1) {
            if (stage == 4) {
               BooleanQuery outer = this._queries.booleanQuery();
               BooleanQuery stageFourQuery = this._queries.booleanQuery();
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageFourQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 4)});
               outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageFourQuery)});
               BooleanQuery migrateQuery = this._queries.booleanQuery();
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "migrateDataKey")});
               migrateQuery.addMustQueryClauses(new Query[]{this._queries.exists("nestedFieldArray.value_en_US")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "")});
               migrateQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_en_US", "[]")});
               outer.addShouldQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", migrateQuery)});
               outer.setMinimumShouldMatch(1);
               mainQuery.addMustQueryClauses(new Query[]{outer});
            } else {
               BooleanQuery stageQuery = this._queries.booleanQuery();
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", stage)});
               mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
            }
         }

         if (Validator.isNotNull(branchCode)) {
            BooleanQuery inner = this._queries.booleanQuery();
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "branchCode")});
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", branchCode.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", inner)});
         }

         if (Validator.isNotNull(zone)) {
            BooleanQuery inner = this._queries.booleanQuery();
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "zone")});
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", zone.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", inner)});
         }

         if (Validator.isNotNull(nbgCode)) {
            BooleanQuery inner = this._queries.booleanQuery();
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "nbgCode")});
            inner.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", nbgCode.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", inner)});
         }

         if (Validator.isNotNull(createDateFrom) && Validator.isNotNull(createDateTo)) {
            long from = this.convertToEpoch(createDateFrom, true);
            long to = this.convertToEpoch(createDateTo, false);
            _log.info("CREATE DATE FILTER -> From: " + from + " To: " + to);
            mainQuery.addMustQueryClauses(new Query[]{this._queries.rangeTerm("createDate_sortable", true, true, from, to)});
         }

         if (Validator.isNotNull(modifiedDateFrom) && Validator.isNotNull(modifiedDateTo)) {
            long from = this.convertToEpoch(modifiedDateFrom, true);
            long to = this.convertToEpoch(modifiedDateTo, false);
            _log.info("MODIFIED DATE FILTER -> From: " + from + " To: " + to);
            mainQuery.addMustQueryClauses(new Query[]{this._queries.rangeTerm("modified_sortable", true, true, from, to)});
         }

         _log.info("Final ES Query: " + mainQuery.toString());
         SearchRequest searchRequest = this._searchRequestBuilderFactory.builder().companyId(this.contextCompany.getCompanyId()).emptySearchEnabled(true).from(0).size(1).withSearchContext((searchContext) -> {
            searchContext.setStart(0);
            searchContext.setEnd(1);
         }).query(mainQuery).build();
         SearchResponse response = this._searcher.search(searchRequest);
         return (long)response.getTotalHits();
      } catch (Exception e) {
         _log.error("Error fetching count", e);
         return 0L;
      }
   }

   private long getCountWithFiltersIfMigrateDisabled(ObjectDefinition objectDefinition, String typeOfRemittance, Integer stage, String branchCode, String zone, String nbgCode, String modifiedDateFrom, String modifiedDateTo, String createDateFrom, String createDateTo) {
      try {
         BooleanQuery mainQuery = this._queries.booleanQuery();
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("objectDefinitionId", objectDefinition.getObjectDefinitionId())});
         mainQuery.addMustQueryClauses(new Query[]{this._queries.term("entryClassName", objectDefinition.getClassName())});
         if (Validator.isNotNull(typeOfRemittance)) {
            BooleanQuery typeQuery = this._queries.booleanQuery();
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "typeOfRemittance")});
            typeQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", typeOfRemittance.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", typeQuery)});
         }

         if (stage != null) {
            BooleanQuery stageQuery = this._queries.booleanQuery();
            stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "stage")});
            if (stage == -1) {
               stageQuery.addMustNotQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", 0)});
            } else {
               stageQuery.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_integer", stage)});
            }

            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", stageQuery)});
         }

         if (Validator.isNotNull(branchCode)) {
            BooleanQuery q = this._queries.booleanQuery();
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "branchCode")});
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", branchCode.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", q)});
         }

         if (Validator.isNotNull(zone)) {
            BooleanQuery q = this._queries.booleanQuery();
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "zone")});
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", zone.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", q)});
         }

         if (Validator.isNotNull(nbgCode)) {
            BooleanQuery q = this._queries.booleanQuery();
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.fieldName", "nbgCode")});
            q.addMustQueryClauses(new Query[]{this._queries.term("nestedFieldArray.value_keyword_lowercase", nbgCode.toLowerCase())});
            mainQuery.addMustQueryClauses(new Query[]{this._queries.nested("nestedFieldArray", q)});
         }

         if (Validator.isNotNull(createDateFrom) && Validator.isNotNull(createDateTo)) {
            long from = this.convertToEpoch(createDateFrom, true);
            long to = this.convertToEpoch(createDateTo, false);
            mainQuery.addMustQueryClauses(new Query[]{this._queries.rangeTerm("createDate_sortable", true, true, from, to)});
         }

         if (Validator.isNotNull(modifiedDateFrom) && Validator.isNotNull(modifiedDateTo)) {
            long from = this.convertToEpoch(modifiedDateFrom, true);
            long to = this.convertToEpoch(modifiedDateTo, false);
            mainQuery.addMustQueryClauses(new Query[]{this._queries.rangeTerm("modified_sortable", true, true, from, to)});
         }

         _log.info("Final ES Query (FIXED): " + mainQuery);
         SearchRequest searchRequest = this._searchRequestBuilderFactory.builder().companyId(this.contextCompany.getCompanyId()).emptySearchEnabled(true).from(0).size(1).query(mainQuery).build();
         SearchResponse response = this._searcher.search(searchRequest);
         return (long)response.getTotalHits();
      } catch (Exception e) {
         _log.error("Error fetching count", e);
         return 0L;
      }
   }

   private long convertToEpoch(String dateStr, boolean startOfDay) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
      LocalDate date = LocalDate.parse(dateStr, formatter);
      ZonedDateTime zdt = startOfDay ? date.atStartOfDay(ZoneOffset.UTC) : date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      return zdt.toInstant().toEpochMilli();
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.configuration = (FeboDashboardConfiguration)ConfigurableUtil.createConfigurable(FeboDashboardConfiguration.class, properties);
   }
}
