package com.boi.headless.web.customeraccount.internal.resource.v1_0;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.web.customeraccount.dto.v1_0.CustomRequestAccountOpeningResponse;
import com.boi.headless.web.customeraccount.dto.v1_0.RequestAccountOpeningResponse;
import com.boi.headless.web.customeraccount.resource.v1_0.MetadataResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import java.util.Map;
import java.util.TimeZone;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/metadata.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {MetadataResource.class}
)
public class MetadataResourceImpl extends BaseMetadataResourceImpl {
   private BOIWorkflowConfiguation boiWorkflowConfiguation;
   @Reference
   private DLAppService _dlAppService;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.boiWorkflowConfiguation = (BOIWorkflowConfiguation)ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
   }

   public RequestAccountOpeningResponse getRequestMetaData() throws Exception {
      try {
         if (Validator.isNotNull(this.contextUser) && !this.contextUser.isGuestUser() && !this.contextUser.isDefaultUser()) {
            JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
            UserHierarchy userHierarchy = new UserHierarchy(this.contextUser, this.boiWorkflowConfiguation.HODefualtFolderId(), (String)null, this._dlAppService);
            jsonObject.put("zoneName", userHierarchy.getZoneName());
            if (!Validator.isBlank(userHierarchy.getZCode())) {
               jsonObject.put("zCode", userHierarchy.getZCode());
            }

            jsonObject.put("branch", userHierarchy.getBranchName());
            jsonObject.put("nbgCode", userHierarchy.getNbgName());
            jsonObject.put("userId", userHierarchy.getUserId());
            jsonObject.put("screenName", userHierarchy.getName());
            return new CustomRequestAccountOpeningResponse(jsonObject, Status.ACCEPTED);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return new CustomRequestAccountOpeningResponse(JSONFactoryUtil.createJSONObject(), Status.ACCEPTED);
   }

   static {
      TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
   }
}
