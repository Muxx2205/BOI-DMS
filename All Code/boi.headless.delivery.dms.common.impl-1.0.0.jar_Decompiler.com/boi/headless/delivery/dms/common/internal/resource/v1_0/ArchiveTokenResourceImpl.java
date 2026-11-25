package boi.headless.delivery.dms.common.internal.resource.v1_0;

import boi.headless.delivery.dms.common.background.task.ArchiveTokenConfiguration;
import boi.headless.delivery.dms.common.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.dms.common.resource.v1_0.ArchiveTokenResource;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HttpsURLConnection;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.dms.common.background.task.ArchiveTokenConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/archive-token.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {ArchiveTokenResource.class}
)
public class ArchiveTokenResourceImpl extends BaseArchiveTokenResourceImpl {
   private static final Log log = LogFactoryUtil.getLog(ArchiveTokenResourceImpl.class);
   private static final String ROLE_BOI_DMS = "DMS Archive Rights";
   private static final String ROLE_ARCHIVE_DASHBOARD_1 = "Archive Dashboard 1";
   private static final String ROLE_BRANCH_DIGITILIZATION = "Branch Digitilization";
   private static final long ROLE_BOI_DMS_USER = 428911L;
   private static final long ROLE_ARCHIVE_DASHBOARD_1_USER = 92367L;
   private static final long ROLE_BRANCH_DIGITILIZATION_USER = 92369L;
   private static final Map<Long, Long> tokenExpiryMap = new ConcurrentHashMap();
   private static final Map<Long, String> tokenCache = new ConcurrentHashMap();
   private static final long INTERVAL = 590000L;
   private String secretId = "";
   private String clientId = "";
   private long userKey = 0L;
   private ArchiveTokenConfiguration archiveTokenConfiguration;

   public BOIHeadlessResponse getArchiveToken() {
      JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();

      try {
         String error = this.initConfig();
         if (error != null) {
            jsonResponse.put("error", error);
            return this.buildResponse(402, "FAILED", jsonResponse);
         } else {
            String cachedToken = this.getCachedToken();
            if (cachedToken != null) {
               jsonResponse.put("access_token", cachedToken);
               return this.buildResponse(200, "SUCCESS", jsonResponse);
            } else {
               String accessToken = this.fetchTokenFromAPI();
               if (accessToken != null && !accessToken.isBlank()) {
                  long expiryTime = System.currentTimeMillis() + 590000L;
                  tokenExpiryMap.put(this.userKey, expiryTime);
                  tokenCache.put(this.userKey, accessToken);
                  jsonResponse.put("access_token", accessToken);
                  return this.buildResponse(200, "SUCCESS", jsonResponse);
               } else {
                  jsonResponse.put("error", "Failed to fetch access token");
                  return this.buildResponse(402, "FAILED", jsonResponse);
               }
            }
         }
      } catch (Exception e) {
         log.error("Error generating OAuth2 token", e);
         jsonResponse.put("error", e.getMessage());
         return this.buildResponse(500, "FAILED", jsonResponse);
      }
   }

   private String initConfig() {
      if (this.contextUser == null) {
         return "User context missing";
      } else if (this.contextUser.getRoles() != null && !this.contextUser.getRoles().isEmpty()) {
         String matchedRole = (String)this.contextUser.getRoles().stream().map((role) -> role.getName()).filter((roleName) -> "DMS Archive Rights".equalsIgnoreCase(roleName) || "Archive Dashboard 1".equalsIgnoreCase(roleName) || "Branch Digitilization".equalsIgnoreCase(roleName)).findFirst().orElse((Object)null);
         if (matchedRole == null) {
            return "Access denied";
         } else {
            switch (matchedRole) {
               case "DMS Archive Rights":
                  this.secretId = this.archiveTokenConfiguration.boiDMSSecret();
                  this.clientId = this.archiveTokenConfiguration.boiDMSClientId();
                  this.userKey = 428911L;
                  break;
               case "Archive Dashboard 1":
                  this.secretId = this.archiveTokenConfiguration.archiveDashboard1Secret();
                  this.clientId = this.archiveTokenConfiguration.archiveDashboard1ClientId();
                  this.userKey = 92367L;
                  break;
               case "Branch Digitilization":
                  this.secretId = this.archiveTokenConfiguration.branchDigitilizationSecret();
                  this.clientId = this.archiveTokenConfiguration.branchDigitilizationClientId();
                  this.userKey = 92369L;
                  break;
               default:
                  return "Invalid role configuration";
            }

            return null;
         }
      } else {
         return "User has no roles";
      }
   }

   private String getCachedToken() {
      Long expiry = (Long)tokenExpiryMap.get(this.userKey);
      if (expiry != null && System.currentTimeMillis() <= expiry) {
         return (String)tokenCache.get(this.userKey);
      } else {
         tokenExpiryMap.remove(this.userKey);
         tokenCache.remove(this.userKey);
         return null;
      }
   }

   private String fetchTokenFromAPI() throws Exception {
      URL url = new URL(this.archiveTokenConfiguration.TokenURL());
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(10000);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept", "application/json");
      String var10000 = this.archiveTokenConfiguration.grantType();
      String body = "grant_type=" + var10000 + "&client_id=" + URLEncoder.encode(this.clientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(this.secretId, "UTF-8");
      OutputStream os = conn.getOutputStream();

      try {
         os.write(body.getBytes(StandardCharsets.UTF_8));
      } catch (Throwable var13) {
         if (os != null) {
            try {
               os.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (os != null) {
         os.close();
      }

      int responseCode = conn.getResponseCode();
      InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

      String var10;
      try {
         StringBuilder responseBuilder = new StringBuilder();

         String line;
         while((line = br.readLine()) != null) {
            responseBuilder.append(line);
         }

         JSONObject json = JSONFactoryUtil.createJSONObject(responseBuilder.toString());
         var10 = json.getString("access_token", "");
      } catch (Throwable var14) {
         try {
            br.close();
         } catch (Throwable var11) {
            var14.addSuppressed(var11);
         }

         throw var14;
      }

      br.close();
      return var10;
   }

   private BOIHeadlessResponse buildResponse(int code, String status, JSONObject data) {
      BOIHeadlessResponse response = new BOIHeadlessResponse();
      response.setResponseId(code);
      response.setResponseStatus(status);
      response.setData(data.toMap());
      return response;
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.archiveTokenConfiguration = (ArchiveTokenConfiguration)ConfigurableUtil.createConfigurable(ArchiveTokenConfiguration.class, properties);
   }
}
