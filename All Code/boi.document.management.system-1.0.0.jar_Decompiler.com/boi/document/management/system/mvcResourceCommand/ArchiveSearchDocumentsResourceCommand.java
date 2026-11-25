package boi.document.management.system.mvcResourceCommand;

import boi.document.management.system.configuration.DMSConfiguration;
import boi.document.management.system.portlet.BoiDocumentManagementSystemPortlet;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.security.auth.AuthTokenUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.Http.Method;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   immediate = true,
   configurationPid = {"boi.document.management.system.configuration.DMSConfiguration"},
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=/archive/search-documents"},
   service = {MVCResourceCommand.class}
)
public class ArchiveSearchDocumentsResourceCommand extends BaseMVCResourceCommand {
   private static final Map<Long, Long> lastRequestTimes = new ConcurrentHashMap();
   private static final Map<Long, String> tokenCache = new ConcurrentHashMap();
   private static final long INTERVAL = 595000L;
   private static final Log _log = LogFactoryUtil.getLog(ArchiveSearchDocumentsResourceCommand.class);
   private User user = null;
   private boolean isZoneLevel = false;
   private boolean isNBGLevel = false;
   private boolean isBranchLevel = false;
   private boolean isBranchUnderLevel = false;
   private static DMSConfiguration _dmsConfiguration;

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();
      boolean isHavingHideDocPrmsn = false;
      String templateName = "";

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long userId = themeDisplay.getUserId();
         this.user = UserLocalServiceUtil.getUser(userId);
         String csrfToken = AuthTokenUtil.getToken(PortalUtil.getHttpServletRequest(resourceRequest));
         Set<String> roleNames = (Set)this.user.getRoles().stream().map((r) -> r.getName().toLowerCase()).collect(Collectors.toSet());
         if (!roleNames.contains("dms archive rights")) {
            responseJson.put("error", "Permission Denied.");
            responseJson.put("success", false);
            this.writeJSON(resourceResponse, responseJson);
            return;
         }

         if (roleNames.contains("eplatform")) {
            templateName = "EPLATFORMAPI";
         } else if (roleNames.contains("kccjansmarth")) {
            templateName = "KCCJansmarthAPI";
         } else {
            templateName = "COMMONAPICALL";
         }

         String folderId = ParamUtil.getString(resourceRequest, "folderId");
         int page = ParamUtil.getInteger(resourceRequest, "page", 1);
         int pageSize = ParamUtil.getInteger(resourceRequest, "pageSize", 20);
         String archiveEndpoint = _dmsConfiguration.archiveEndPoints();
         isHavingHideDocPrmsn = roleNames.contains("dms doc hide");
         if (isHavingHideDocPrmsn) {
            Set<String> nbgFolder = (Set)Arrays.stream(BoiDocumentManagementSystemPortlet.nbgFoldersName().split("\\s*,\\s*")).collect(Collectors.toSet());
            this.isAt3Level(Long.parseLong(folderId), nbgFolder);
         }

         if (isHavingHideDocPrmsn && (this.isBranchLevel || this.isZoneLevel || this.isNBGLevel || this.isBranchUnderLevel)) {
            responseJson.put("error", "Permission Denied.");
            responseJson.put("success", false);
            this.writeJSON(resourceResponse, responseJson);
            return;
         }

         String token = this.getCurrentToken();
         if (Validator.isNull(token)) {
            token = this.getAccessToken(archiveEndpoint);
         }

         _log.info("Archive search called for folder: " + folderId + ", page: " + page + ", pageSize: " + pageSize);
         if (Validator.isNull(token)) {
            _log.error("No access token available");
            responseJson.put("error", "No access token available");
            responseJson.put("success", false);
            this.writeJSON(resourceResponse, responseJson);
            return;
         }

         if (Validator.isNull(archiveEndpoint)) {
            _log.error("No archive endpoint available");
            responseJson.put("error", "No archive endpoint available");
            responseJson.put("success", false);
            this.writeJSON(resourceResponse, responseJson);
            return;
         }

         DLFolder folder = DLFolderLocalServiceUtil.getFolder(Long.parseLong(folderId));
         boolean isBoardSecretariatExist = Arrays.stream(folder.getTreePath().split("/")).filter((id) -> !id.isEmpty()).anyMatch((id) -> id.equals(String.valueOf(_dmsConfiguration.boardSecretariatFolderId())));
         String searchQuery = "folderIndex eq " + folderId;
         if (isBoardSecretariatExist) {
            searchQuery = searchQuery + " and boardsecretariat ge true";
         }

         String encodedSearch = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
         String apiUrl = archiveEndpoint + "/o/boi-headless-delivery-archival-common/v1.0/search-ep-document?search=" + encodedSearch + "&templateName=" + templateName + "&page=" + page + "&pageSize=" + pageSize;
         _log.info("Calling archive API: " + apiUrl);
         Http.Options options = new Http.Options();
         options.setLocation(apiUrl);
         options.setMethod(Method.GET);
         options.addHeader("accept", "application/json");
         options.addHeader("Authorization", "Bearer " + token);
         options.setTimeout(100000);
         String apiResponse = HttpUtil.URLtoString(options);
         Http.Response httpResponse = options.getResponse();
         _log.info("Archive API response code: " + httpResponse.getResponseCode());
         if (httpResponse.getResponseCode() != 200) {
            Log var34 = _log;
            int var36 = httpResponse.getResponseCode();
            var34.error("Archive API error: " + var36 + " - " + apiResponse);
            responseJson.put("error", "Archive API error: " + httpResponse.getResponseCode());
            responseJson.put("success", false);
            this.writeJSON(resourceResponse, responseJson);
            return;
         }

         JSONObject apiData = JSONFactoryUtil.createJSONObject(apiResponse);
         JSONObject result = JSONFactoryUtil.createJSONObject();
         if (apiData.has("data")) {
            JSONObject dataWrapper = apiData.getJSONObject("data");
            if (dataWrapper.has("files") && !dataWrapper.isNull("files")) {
               result.put("files", dataWrapper.getJSONArray("files"));
            } else {
               result.put("files", JSONFactoryUtil.createJSONArray());
            }

            int total = dataWrapper.getInt("total", dataWrapper.getInt("count", 0));
            result.put("total", total);
            Log var33 = _log;
            int var35 = result.getJSONArray("files").length();
            var33.info("Found " + var35 + " files, total: " + total);
         } else {
            if (apiData.has("files") && !apiData.isNull("files")) {
               result.put("files", apiData.getJSONArray("files"));
            } else {
               result.put("files", JSONFactoryUtil.createJSONArray());
            }

            int total = apiData.getInt("total", apiData.getInt("count", 0));
            result.put("total", total);
            Log var10000 = _log;
            int var10001 = result.getJSONArray("files").length();
            var10000.info("No data wrapper, found " + var10001 + " files, total: " + total);
         }

         result.put("page", page);
         result.put("pageSize", pageSize);
         result.put("folderId", folderId);
         result.put("success", true);
         this.writeJSON(resourceResponse, result);
      } catch (Exception e) {
         _log.error("Error in archive search", e);
         responseJson.put("error", e.getMessage());
         responseJson.put("success", false);
         this.writeJSON(resourceResponse, responseJson);
      }

   }

   private String getAccessToken(String endPoint) throws IOException, JSONException {
      String urlStr = endPoint + "/o/customer-account-headless-delivery/v1.0/get-token";
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Authorization", "Basic YXJjaGl2YWxkb2N1bWVudHZpZXc6YXJjaGl2YWxkb2N1bWVudHZpZXdAdmlldy00MjgsIzQjJV4qJg==");
      conn.setDoInput(true);
      conn.setConnectTimeout(180000);
      conn.setReadTimeout(180000);
      int responseCode = conn.getResponseCode();
      InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
      String responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      conn.disconnect();
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject(responseBody);
      String token = jsonObject.getJSONObject("data").getString("access_token");
      if (token != null && !token.isEmpty()) {
         _log.info("DMS RETURNING NEW TOKEN ");
         long currentTimeMillis = System.currentTimeMillis();
         lastRequestTimes.put(428L, currentTimeMillis);
         tokenCache.put(428L, token);
      }

      return token;
   }

   private String getCurrentToken() {
      long userKey = 428L;
      long currentTime = System.currentTimeMillis();
      Long lastTime = (Long)lastRequestTimes.get(userKey);
      if (lastTime == null) {
         _log.info("No cache entry found . Generate New Token");
         return null;
      } else if (currentTime - lastTime <= 595000L) {
         _log.info("DMS RETURNING CACHE TOKEN");
         return (String)tokenCache.get(userKey);
      } else {
         lastRequestTimes.remove(userKey);
         tokenCache.remove(userKey);
         _log.info("Token expired . Generate New Token");
         return null;
      }
   }

   private void writeJSON(ResourceResponse resourceResponse, JSONObject json) throws Exception {
      resourceResponse.setContentType("application/json");
      PrintWriter writer = resourceResponse.getWriter();

      try {
         writer.write(json.toString());
      } catch (Throwable var7) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (writer != null) {
         writer.close();
      }

   }

   private void isAt3Level(long folderId, Set<String> nbgSet) {
      this.isZoneLevel = false;
      this.isBranchLevel = false;
      this.isBranchUnderLevel = false;
      this.isNBGLevel = false;
      if (folderId != 0L) {
         try {
            DLFolder folder = DLFolderLocalServiceUtil.fetchDLFolder(folderId);
            if (folder == null) {
               return;
            }

            String folderName = folder.getName().trim();
            if (this.startsWith4Digits(folderName)) {
               this.isBranchUnderLevel = true;
               return;
            }

            if (folderName.equalsIgnoreCase("Head Office")) {
               this.isNBGLevel = true;
               return;
            }

            if (nbgSet.contains(folderName)) {
               this.isZoneLevel = true;
               return;
            }

            DLFolder parent = folder.getParentFolder();
            if (parent == null) {
               return;
            }

            String parentName = parent.getName().trim();
            if (nbgSet.contains(parentName)) {
               this.isBranchLevel = true;
               return;
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

      }
   }

   private boolean startsWith4Digits(String name) {
      if (name == null) {
         return false;
      } else {
         name = name.trim();
         if (name.length() < 6) {
            return false;
         } else {
            for(int i = 0; i < 4; ++i) {
               if (!Character.isDigit(name.charAt(i))) {
                  return false;
               }
            }

            int index = 4;
            if (name.charAt(index) != '-') {
               return false;
            } else {
               ++index;

               while(index < name.length() && name.charAt(index) == ' ') {
                  ++index;
               }

               if (index >= name.length()) {
                  return false;
               } else {
                  return !Character.isDigit(name.charAt(index));
               }
            }
         }
      }
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      _dmsConfiguration = (DMSConfiguration)ConfigurableUtil.createConfigurable(DMSConfiguration.class, properties);
   }
}
