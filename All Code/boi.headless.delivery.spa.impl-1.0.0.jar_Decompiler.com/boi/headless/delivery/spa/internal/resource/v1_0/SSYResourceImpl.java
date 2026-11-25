package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.common.RequestValidator;
import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponse;
import boi.headless.delivery.spa.dto.v1_0.CustomBOIHeadlessResponseAsList;
import boi.headless.delivery.spa.resource.v1_0.SSYResource;
import com.boi.headless.rest.common.spa.application.APIRequestHandler;
import com.boi.headless.rest.common.spa.application.BOISPAConfiguation;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HttpsURLConnection;
import javax.validation.constraints.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.spa.application.BOISPAConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/ssy.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {SSYResource.class}
)
public class SSYResourceImpl extends BaseSSYResourceImpl {
   private static final Map<Long, Long> lastRequestTimes = new ConcurrentHashMap();
   private static final Map<Long, String> tokenCache = new ConcurrentHashMap();
   private static final long INTERVAL = 595000L;
   private static final String ROLE_SGB_SSY = "SGB SSY";
   private static final String VALID_FOLDER_NAME_REGEX = "^[a-zA-Z0-9_-][a-zA-Z0-9 _-]*$";
   private long defaultFolderId = 0L;
   private long objectDefinitionId = 0L;
   private BOISPAConfiguation boiSpaConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(SSYResourceImpl.class);
   @Reference
   private DTOConverterRegistry dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;
   @Reference
   private ObjectDefinitionLocalService objectDefinitionLocalService;
   @Reference
   private ObjectEntryLocalService objectEntryLocalService;
   @Reference
   private WorkflowTaskManager workflowTaskManager;

   private boolean checkAuthType() {
      String authHeader = this.contextHttpServletRequest.getHeader("Authorization");
      return Validator.isNotNull(authHeader) && authHeader.startsWith("Bearer ");
   }

   public BOIHeadlessResponse searchFolder(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      String endPoints = this.boiSpaConfiguation.DMSUrl();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("Error", "Authentication failed!");
            jsonObject.put("ResponseCode", "101");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasSSYRole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            boolean ssyArchival = this.boiSpaConfiguation.isSSYArchival();
            if (ssyArchival) {
               String token = this.getCurrentToken();
               if (Validator.isNull(token)) {
                  token = this.getAccessToken(this.boiSpaConfiguation.ArchiveUrl());
               }

               JSONObject searchFolder = searchFolder(search, filter, pagination, sorts, token, endPoints);
               return new CustomBOIHeadlessResponseAsList(searchFolder);
            } else {
               return this.listData(search, filter, pagination, sorts);
            }
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse searchDocument(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("Error", "Authentication failed!");
            jsonObject.put("ResponseCode", "101");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasSSYRole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            boolean ssyArchival = this.boiSpaConfiguation.isSSYArchival();
            if (ssyArchival) {
               return this.listDocument(search, pagination, sorts);
            } else {
               jsonObject.put("Error", "The service is currently unavailable. Please contact DMS Support Team for assistance.");
               jsonObject.put("ResponseCode", "801");
               return new CustomBOIHeadlessResponse(jsonObject);
            }
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse getFolder(@NotNull Integer id) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("Error", "Authentication failed!");
            jsonObject.put("ResponseCode", "101");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasSSYRole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            return this.getSingleEntry(id);
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse addFolder(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
      String endPoints = this.boiSpaConfiguation.DMSUrl();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("Error", "Authentication failed!");
            jsonObject.put("ResponseCode", "101");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasSSYRole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            boolean ssyArchival = this.boiSpaConfiguation.isSSYArchival();
            if (ssyArchival) {
               JSONObject folderJson = JSONFactoryUtil.createJSONObject();
               folderJson.put("parentFolderIndex", multipartBody.getValueAsString("parentFolderIndex"));
               folderJson.put("folderName", multipartBody.getValueAsString("folderName"));
               folderJson.put("dateCreated", multipartBody.getValueAsString("dateCreated"));
               folderJson.put("AccessType", multipartBody.getValueAsString("AccessType"));
               folderJson.put("ImageVolumeIndex", multipartBody.getValueAsString("ImageVolumeIndex"));
               folderJson.put("FolderType", multipartBody.getValueAsString("FolderType"));
               folderJson.put("Location", multipartBody.getValueAsString("Location"));
               folderJson.put("Comment", multipartBody.getValueAsString("Comment"));
               folderJson.put("EnableFtsFlag", multipartBody.getValueAsString("EnableFtsFlag"));
               String token = this.getCurrentToken();
               if (Validator.isNull(token)) {
                  token = this.getAccessToken(this.boiSpaConfiguation.ArchiveUrl());
               }

               String response = createFolder(token, endPoints, folderJson);
               JSONObject responseJson = JSONFactoryUtil.createJSONObject(response);
               JSONObject actualData = responseJson.getJSONObject("data");
               return new CustomBOIHeadlessResponse(actualData);
            } else {
               APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
               return this.addFolder(apiRequestHandler, multipartBody, 1);
            }
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   public BOIHeadlessResponse addDocument(MultipartBody multipartBody) throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!this.checkAuthType()) {
            jsonObject.put("Error", "Authentication failed!");
            jsonObject.put("ResponseCode", "101");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else if (!this.hasSSYRole(this.contextUser)) {
            jsonObject.put("Error", "Access denied. Insufficient privileges");
            jsonObject.put("ResponseCode", "403");
            return new CustomBOIHeadlessResponse(jsonObject);
         } else {
            boolean ssyArchival = this.boiSpaConfiguation.isSSYArchival();
            if (ssyArchival) {
               APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
               return this.addDocument(apiRequestHandler, multipartBody, 1);
            } else {
               jsonObject.put("Error", "The service is currently unavailable. Please contact DMS Support Team for assistance.");
               jsonObject.put("ResponseCode", "801");
               return new CustomBOIHeadlessResponse(jsonObject);
            }
         }
      } catch (Exception e) {
         jsonObject.put("Error", e.getMessage());
         return new CustomBOIHeadlessResponse(jsonObject);
      }
   }

   private BOIHeadlessResponse listData(String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Page<ObjectEntry> page = null;
      new LinkedList();
      Sort sort = SortFactoryUtil.create("modifiedDate", 3, true);
      sorts = new Sort[]{sort};
      String query = search;
      search = null;
      log.info("query result is " + query);
      page = objectEntryManager.getObjectEntries(this.contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), query, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   private BOIHeadlessResponse listDocument(String search, Pagination pagination, Sort[] sorts) throws Exception {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(objectDefinition.getStorageType());
      Page<ObjectEntry> page = null;
      String filterString = search;
      search = null;
      page = objectEntryManager.getObjectEntries(this.contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), (Aggregation)null, apiRequestHandler.getDTOConverterContext((Long)null), filterString, pagination, search, sorts);
      return new CustomBOIHeadlessResponseAsList(page);
   }

   private BOIHeadlessResponse getSingleEntry(Number id) {
      APIRequestHandler apiRequestHandler = this.getAPIRequestHandler();
      ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(this.objectDefinitionId);
      com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.getObjectEntry(id.longValue());
      if (Validator.isNotNull(objectEntry) && objectEntry.getObjectDefinitionId() == objectDefinition.getObjectDefinitionId()) {
         Map<String, Serializable> values = objectEntry.getValues();
         JSONObject jsonObject = apiRequestHandler.convertEntryToArray(values);
         return new CustomBOIHeadlessResponse(jsonObject);
      } else {
         return new CustomBOIHeadlessResponse(JSONFactoryUtil.createJSONObject());
      }
   }

   public static boolean isValidFolderName(String folderName) {
      return folderName != null && folderName.matches("^[a-zA-Z0-9_-][a-zA-Z0-9 _-]*$");
   }

   private BOIHeadlessResponse addFolder(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         if (!isValidFolderName(ParamUtil.getString(this.contextHttpServletRequest, "folderName"))) {
            jsonObject.put("Error", "Invalid folder name. Only letters, numbers, '-', '_', and spaces allowed; must start with letter, number, '-' or '_'");
            jsonObject.put("ResponseCode", "301");
            return new CustomBOIHeadlessResponse(jsonObject);
         }

         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleAddFolderRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, this.defaultFolderId);
         if (Validator.isNotNull(objectEntry)) {
            jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
            jsonObject.put("values", objectEntry.getValues());
         }
      } catch (Exception e) {
         String msg = e.getMessage();
         if (Validator.isNotNull(msg) && msg.contains("ResponseCode")) {
            String code = msg.replaceAll(".*ResponseCode\\s*=\\s*(\\d+).*", "$1");
            String message = msg.replaceAll(".*Message\\s*=\\s*", "");
            jsonObject.put("ResponseCode", code);
            jsonObject.put("Error", message);
         } else {
            jsonObject.put("Error", msg);
         }
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   private void fileValidation(MultipartBody multipartBody) throws Exception {
      (new RequestValidator(multipartBody, "SGB SSY")).validateRequestFiles();
   }

   private String getAccessToken(String endPoint) throws IOException, JSONException {
      String urlStr = endPoint + "/o/boi-headless-delivery-archival-common/v1.0/get-dms-token";
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", "Basic ZXBsYXRlZm9ybWFwaTplcEBhcGkjYm9pLTQyOC5pbiYq");
      conn.setDoInput(true);
      conn.setConnectTimeout(600000);
      conn.setReadTimeout(600000);
      int responseCode = conn.getResponseCode();
      InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
      String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      conn.disconnect();
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject(responseBody);
      String token = jsonObject.getJSONObject("data").getJSONObject("data").getString("access_token");
      if (token != null && !token.isEmpty()) {
         log.info("DMS RETURNING NEW TOKEN for SSY ");
         long currentTimeMillis = System.currentTimeMillis();
         lastRequestTimes.put(this.contextUser.getUserId(), currentTimeMillis);
         tokenCache.put(this.contextUser.getUserId(), token);
      }

      return token;
   }

   private static String createFolder(String bearerToken, String endPoint, JSONObject folderJson) throws IOException {
      String boundary = "----BOIFolderBoundary" + System.currentTimeMillis();
      String urlStr = endPoint + "/o/boi-headless-delivery-spa/v1.0/add-ssy/folder";
      URL url = new URL(urlStr);
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
      conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
      conn.setConnectTimeout(600000);
      conn.setReadTimeout(600000);
      DataOutputStream out = new DataOutputStream(conn.getOutputStream());

      try {
         for(String key : folderJson.keySet()) {
            String value = folderJson.getString(key, "");
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
            out.writeBytes(value + "\r\n");
         }

         out.writeBytes("--" + boundary + "--\r\n");
         out.flush();
      } catch (Throwable var12) {
         try {
            out.close();
         } catch (Throwable var11) {
            var12.addSuppressed(var11);
         }

         throw var12;
      }

      out.close();
      int responseCode = conn.getResponseCode();
      InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
      String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      conn.disconnect();
      return responseBody;
   }

   private static JSONObject searchFolder(String search, Filter filter, Pagination pagination, Sort[] sorts, String accessToken, String endPoints) throws IOException, JSONException {
      int page = pagination != null ? pagination.getPage() : 0;
      int pageSize = pagination != null ? pagination.getPageSize() : 50;
      StringBuilder urlBuilder = new StringBuilder(endPoints + "/o/boi-headless-delivery-spa/v1.0/search-ssy-folder?");
      if (search != null && !search.isEmpty()) {
         urlBuilder.append("search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8.name()));
      }

      urlBuilder.append("&pageSize=").append(pageSize).append("&page=").append(page);
      if (filter != null) {
         String filterStr = filter.toString();
         urlBuilder.append("&filter=").append(URLEncoder.encode(filterStr, StandardCharsets.UTF_8.name()));
      }

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
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Cookie", "COOKIE_SUPPORT=true; GUEST_LANGUAGE_ID=en_US");
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(15000);
      int responseCode = conn.getResponseCode();
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

   private String getCurrentToken() {
      long userKey = this.contextUser.getUserId();
      long currentTime = System.currentTimeMillis();
      Long lastTime = (Long)lastRequestTimes.get(userKey);
      if (lastTime == null) {
         log.info("No cache entry found for SSY â†’ Generate New Token for ID " + userKey);
         return null;
      } else if (currentTime - lastTime <= 595000L) {
         log.info("DMS RETURNING CACHE TOKEN for SSY");
         return (String)tokenCache.get(userKey);
      } else {
         lastRequestTimes.remove(userKey);
         tokenCache.remove(userKey);
         return null;
      }
   }

   private BOIHeadlessResponse addDocument(APIRequestHandler apiRequestHandler, MultipartBody multipartBody, int status) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         this.fileValidation(multipartBody);
         Map<String, String> fileUpdload = new HashMap();
         fileUpdload.put("imageData", "imageData");
         com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleArchiveAddDocumentRequest(this.contextHttpServletRequest, multipartBody, status, this.objectDefinitionId, fileUpdload, "SSY");
         if (Validator.isNotNull(objectEntry)) {
            jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));
            jsonObject.put("values", objectEntry.getValues());
         }
      } catch (Exception e) {
         String msg = e.getMessage();
         if (Validator.isNotNull(msg) && msg.contains("ResponseCode")) {
            String code = msg.replaceAll(".*ResponseCode\\s*=\\s*(\\d+).*", "$1");
            String message = msg.replaceAll(".*Message\\s*=\\s*", "");
            jsonObject.put("ResponseCode", code);
            jsonObject.put("Error", message);
         } else {
            jsonObject.put("Error", msg);
         }
      }

      return new CustomBOIHeadlessResponse(jsonObject);
   }

   private boolean hasSSYRole(User user) {
      return user != null && user.getRoles().stream().anyMatch((role) -> "SGB SSY".equalsIgnoreCase(role.getName()));
   }

   private APIRequestHandler getAPIRequestHandler() {
      log.info("APIRequestHandler Created for SGB SSY");
      return new APIRequestHandler(this.contextUser, this.contextHttpServletRequest, this.contextAcceptLanguage, this.objectFieldLocalService, this.dlAppService, this.contextUriInfo, this.dtoConverterRegistry, this.objectEntryManagerRegistry, this.objectDefinitionLocalService, this.objectEntryLocalService, this.workflowTaskManager, this.boiSpaConfiguation, "SGB SSY", this.defaultFolderId);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiSpaConfiguation = (BOISPAConfiguation)ConfigurableUtil.createConfigurable(BOISPAConfiguation.class, properties);
      this.defaultFolderId = this.boiSpaConfiguation.SSYDefaultFolderId();
      this.objectDefinitionId = this.boiSpaConfiguation.SSYObjectDefinitionId();
   }
}
