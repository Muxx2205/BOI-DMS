package boi.headless.delivery.memorandumapi.internal.resource.v1_0;

import com.liferay.portal.vulcan.resource.OpenAPIResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@OpenAPIDefinition(
   info = @Info(
   description = "BoiHeadlessDeliveryMemorandumapi REST API",
   license = @License(
   name = "Apache 2.0",
   url = "http://www.apache.org/licenses/LICENSE-2.0.html"
),
   title = "BoiHeadlessDeliveryMemorandumapi",
   version = "v1.0"
)
)
@Path("/v1.0")
@Component(
   properties = {"OSGI-INF/liferay/rest/v1_0/openapi.properties"},
   service = {OpenAPIResourceImpl.class}
)
public class OpenAPIResourceImpl {
   @Context
   private HttpServletRequest _httpServletRequest;
   @Reference
   private OpenAPIResource _openAPIResource;
   @Context
   private UriInfo _uriInfo;
   private final Set<Class<?>> _resourceClasses = new HashSet<Class<?>>() {
      {
         this.add(MemorandumResourceImpl.class);
         this.add(PAIMSConsentResourceImpl.class);
         this.add(PSRSDocumentManagementResourceImpl.class);
         this.add(OpenAPIResourceImpl.class);
      }
   };

   @GET
   @Path("/openapi.{type:json|yaml}")
   @Produces({"application/json", "application/yaml"})
   public Response getOpenAPI(@PathParam("type") String type) throws Exception {
      Class<? extends OpenAPIResource> clazz = this._openAPIResource.getClass();

      try {
         Method method = clazz.getMethod("getOpenAPI", HttpServletRequest.class, Set.class, String.class, UriInfo.class);
         return (Response)method.invoke(this._openAPIResource, this._httpServletRequest, this._resourceClasses, type, this._uriInfo);
      } catch (NoSuchMethodException var6) {
         try {
            Method method = clazz.getMethod("getOpenAPI", Set.class, String.class, UriInfo.class);
            return (Response)method.invoke(this._openAPIResource, this._resourceClasses, type, this._uriInfo);
         } catch (NoSuchMethodException var5) {
            return this._openAPIResource.getOpenAPI(this._resourceClasses, type);
         }
      }
   }
}
