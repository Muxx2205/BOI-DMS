package com.boi.headless.web.customeraccount.internal.resource.v1_0;

import com.boi.headless.rest.common.application.BOITokenGenerate;
import com.boi.headless.web.customeraccount.dto.v1_0.CustomRequestAccountOpeningResponse;
import com.boi.headless.web.customeraccount.dto.v1_0.RequestAccountOpeningResponse;
import com.boi.headless.web.customeraccount.resource.v1_0.CustomTokenResource;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOITokenGenerate"},
   properties = {"OSGI-INF/liferay/rest/v1_0/custom-token.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {CustomTokenResource.class}
)
public class CustomTokenResourceImpl extends BaseCustomTokenResourceImpl {
   private static final Log log = LogFactoryUtil.getLog(CustomTokenResourceImpl.class);
   private static final String ROLE_EPLATFORM = "Eplatform";
   private static final String ROLE_KCC_JANSMARTH = "KCCJansmarth";
   private static final String ROLE_UTOPIA = "UTOPIA";
   private static final String ROLE_CRM_NEXT = "CRMNEXT";
   private static final String ROLE_SGB_SSY = "SGB SSY";
   private String secretId = "";
   private String clientId = "";
   private boolean isAPIUser = false;
   private static final Map<Long, Long> lastRequestTimes = new ConcurrentHashMap();
   private static final Map<Long, String> tokenCache = new ConcurrentHashMap();
   private static final long INTERVAL = 595000L;
   BOITokenGenerate bOITokenGenerate;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.bOITokenGenerate = (BOITokenGenerate)ConfigurableUtil.createConfigurable(BOITokenGenerate.class, properties);
   }

   private Role getRole(String roleName) {
      try {
         Role role = RoleLocalServiceUtil.getRole(this.contextUser.getCompanyId(), roleName);
         return role;
      } catch (PortalException var3) {
         return null;
      }
   }

   public RequestAccountOpeningResponse getToken() throws Exception {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         String authHeader = this.contextHttpServletRequest.getHeader("Authorization");
         boolean hasRole = false;
         Role role = this.getRole("Token Access");
         if (Validator.isNotNull(role)) {
            hasRole = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), this.contextUser.getUserId());
         }

         if (!hasRole) {
            return new CustomRequestAccountOpeningResponse(jsonObject, Status.FORBIDDEN);
         } else {
            this.getSecretAndClientId();
            if (this.isAPIUser) {
               String existingToken = this.getCurrentToken();
               if (Validator.isNotNull(existingToken)) {
                  jsonObject.put("access_token", existingToken);
                  return new CustomRequestAccountOpeningResponse(jsonObject, Status.OK);
               }
            }

            URL url = new URL(this.bOITokenGenerate.URL());
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String var10000 = this.bOITokenGenerate.grantType();
            String body = "grant_type=" + var10000 + "&client_id=" + URLEncoder.encode(this.clientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(this.secretId, "UTF-8");
            OutputStream os = conn.getOutputStream();

            try {
               os.write(body.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable var19) {
               if (os != null) {
                  try {
                     os.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (os != null) {
               os.close();
            }

            int responseCode = conn.getResponseCode();
            InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            CustomRequestAccountOpeningResponse var24;
            try {
               StringBuilder response = new StringBuilder();

               String line;
               while((line = br.readLine()) != null) {
                  response.append(line);
               }

               JSONObject responseJson = JSONFactoryUtil.createJSONObject(response.toString());
               String accessToken = responseJson.getString("access_token");
               jsonObject.put("access_token", accessToken);
               if (this.isAPIUser && accessToken != null && !accessToken.isBlank()) {
                  long currentTimeMillis = System.currentTimeMillis();
                  lastRequestTimes.put(this.contextUser.getUserId(), currentTimeMillis);
                  tokenCache.put(this.contextUser.getUserId(), accessToken);
               }

               var24 = new CustomRequestAccountOpeningResponse(jsonObject, Status.OK);
            } catch (Throwable var20) {
               try {
                  br.close();
               } catch (Throwable var17) {
                  var20.addSuppressed(var17);
               }

               throw var20;
            }

            br.close();
            return var24;
         }
      } catch (Exception e) {
         log.info("Error generating OAuth2 token", e);
         jsonObject.put("error", e.getMessage());
         return new CustomRequestAccountOpeningResponse(jsonObject, Status.INTERNAL_SERVER_ERROR);
      }
   }

   private String getCurrentToken() {
      long userKey = this.contextUser.getUserId();
      long currentTime = System.currentTimeMillis();
      Long lastTime = (Long)lastRequestTimes.get(userKey);
      if (lastTime != null && currentTime - lastTime <= 595000L) {
         log.info("Sending Cache Token... ");
         return (String)tokenCache.get(userKey);
      } else {
         lastRequestTimes.remove(userKey);
         tokenCache.remove(userKey);
         log.info("Sending New Token... ");
         return null;
      }
   }

   private void getSecretAndClientId() {
      this.secretId = "";
      this.clientId = "";
      if (this.contextUser != null && this.contextUser.getRoles() != null) {
         String matchedRole = (String)this.contextUser.getRoles().stream().map((role) -> role.getName()).filter((roleName) -> "Eplatform".equalsIgnoreCase(roleName) || "KCCJansmarth".equalsIgnoreCase(roleName) || "UTOPIA".equalsIgnoreCase(roleName) || "CRMNEXT".equalsIgnoreCase(roleName) || "SGB SSY".equalsIgnoreCase(roleName)).map(String::toUpperCase).findFirst().orElse("DEFAULT");
         log.info("matchedRole ... " + matchedRole);
         switch (matchedRole) {
            case "EPLATFORM":
               this.secretId = this.bOITokenGenerate.eplatformSecret();
               this.clientId = this.bOITokenGenerate.eplatformClientId();
               this.isAPIUser = true;
               break;
            case "KCCJANSMARTH":
               this.secretId = this.bOITokenGenerate.kccJansamarthSecret();
               this.clientId = this.bOITokenGenerate.kccJansamarthClientId();
               this.isAPIUser = true;
               break;
            case "UTOPIA":
               this.secretId = this.bOITokenGenerate.utopiaSecret();
               this.clientId = this.bOITokenGenerate.utopiaClientId();
               this.isAPIUser = true;
               break;
            case "CRMNEXT":
               this.secretId = this.bOITokenGenerate.crmNextSecret();
               this.clientId = this.bOITokenGenerate.crmNextClientId();
               this.isAPIUser = true;
               break;
            case "SBG SSY":
               this.secretId = this.bOITokenGenerate.sbgSSYSecret();
               this.clientId = this.bOITokenGenerate.sbgSSYClientId();
               this.isAPIUser = true;
               break;
            default:
               this.secretId = this.bOITokenGenerate.Secret();
               this.clientId = this.bOITokenGenerate.Id();
               this.isAPIUser = false;
         }

      }
   }
}
