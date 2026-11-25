package boi.headless.delivery.memorandumapi.internal.resource.v1_0;

import boi.headless.delivery.memorandumapi.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.memorandumapi.resource.v1_0.PAIMSConsentResource;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.GroupedModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.odata.filter.ExpressionConvert;
import com.liferay.portal.odata.filter.FilterParserProvider;
import com.liferay.portal.odata.sort.SortParserProvider;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.ActionUtil;
import com.liferay.portal.vulcan.util.TransformUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/v1.0")
public abstract class BasePAIMSConsentResourceImpl implements PAIMSConsentResource {
   protected AcceptLanguage contextAcceptLanguage;
   protected Company contextCompany;
   protected HttpServletRequest contextHttpServletRequest;
   protected HttpServletResponse contextHttpServletResponse;
   protected Object contextScopeChecker;
   protected UriInfo contextUriInfo;
   protected User contextUser;
   protected ExpressionConvert<Filter> expressionConvert;
   protected FilterParserProvider filterParserProvider;
   protected GroupLocalService groupLocalService;
   protected ResourceActionLocalService resourceActionLocalService;
   protected ResourcePermissionLocalService resourcePermissionLocalService;
   protected RoleLocalService roleLocalService;
   protected SortParserProvider sortParserProvider;
   private static final Log _log = LogFactoryUtil.getLog(BasePAIMSConsentResourceImpl.class);

   @Parameters({@Parameter(
   in = ParameterIn.QUERY,
   name = "search"
), @Parameter(
   in = ParameterIn.QUERY,
   name = "filter"
), @Parameter(
   in = ParameterIn.QUERY,
   name = "page"
), @Parameter(
   in = ParameterIn.QUERY,
   name = "pageSize"
), @Parameter(
   in = ParameterIn.QUERY,
   name = "sort"
)})
   @Tags({@Tag(
   name = "PAIMSConsent"
)})
   @GET
   @Path("/request-paims-list")
   @Produces({"application/json", "application/xml"})
   public BOIHeadlessResponse getPaimsList(@Parameter(hidden = true) @QueryParam("search") String search, @Context Filter filter, @Context Pagination pagination, @Context Sort[] sorts) throws Exception {
      return new BOIHeadlessResponse();
   }

   @Parameters({@Parameter(
   in = ParameterIn.PATH,
   name = "id"
)})
   @Tags({@Tag(
   name = "PAIMSConsent"
)})
   @GET
   @Path("/request-paims/{id}")
   @Produces({"application/json", "application/xml"})
   public BOIHeadlessResponse getPaims(@Parameter(hidden = true) @PathParam("id") @NotNull Integer id) throws Exception {
      return new BOIHeadlessResponse();
   }

   @Tags({@Tag(
   name = "PAIMSConsent"
)})
   @Consumes({"multipart/form-data"})
   @Path("/add-paims/saveAsDraft")
   @POST
   @Produces({"application/json", "application/xml"})
   public BOIHeadlessResponse addPaimsAsDraft(MultipartBody multipartBody) throws Exception {
      return new BOIHeadlessResponse();
   }

   @Tags({@Tag(
   name = "PAIMSConsent"
)})
   @Consumes({"multipart/form-data"})
   @Path("/add-paims/saveAsExport")
   @POST
   @Produces({"application/json", "application/xml"})
   public BOIHeadlessResponse addPaimsAsExport(MultipartBody multipartBody) throws Exception {
      return new BOIHeadlessResponse();
   }

   @Tags({@Tag(
   name = "PAIMSConsent"
)})
   @Consumes({"multipart/form-data"})
   @Path("/workflow-paims")
   @POST
   @Produces({"application/json", "application/xml"})
   public BOIHeadlessResponse approveOrRejectPaims(MultipartBody multipartBody) throws Exception {
      return new BOIHeadlessResponse();
   }

   public void setContextAcceptLanguage(AcceptLanguage contextAcceptLanguage) {
      this.contextAcceptLanguage = contextAcceptLanguage;
   }

   public void setContextCompany(Company contextCompany) {
      this.contextCompany = contextCompany;
   }

   public void setContextHttpServletRequest(HttpServletRequest contextHttpServletRequest) {
      this.contextHttpServletRequest = contextHttpServletRequest;
   }

   public void setContextHttpServletResponse(HttpServletResponse contextHttpServletResponse) {
      this.contextHttpServletResponse = contextHttpServletResponse;
   }

   public void setContextUriInfo(UriInfo contextUriInfo) {
      this.contextUriInfo = contextUriInfo;
   }

   public void setContextUser(User contextUser) {
      this.contextUser = contextUser;
   }

   public void setExpressionConvert(ExpressionConvert<Filter> expressionConvert) {
      this.expressionConvert = expressionConvert;
   }

   public void setFilterParserProvider(FilterParserProvider filterParserProvider) {
      this.filterParserProvider = filterParserProvider;
   }

   public void setGroupLocalService(GroupLocalService groupLocalService) {
      this.groupLocalService = groupLocalService;
   }

   public void setResourceActionLocalService(ResourceActionLocalService resourceActionLocalService) {
      this.resourceActionLocalService = resourceActionLocalService;
   }

   public void setResourcePermissionLocalService(ResourcePermissionLocalService resourcePermissionLocalService) {
      this.resourcePermissionLocalService = resourcePermissionLocalService;
   }

   public void setRoleLocalService(RoleLocalService roleLocalService) {
      this.roleLocalService = roleLocalService;
   }

   public void setSortParserProvider(SortParserProvider sortParserProvider) {
      this.sortParserProvider = sortParserProvider;
   }

   protected Map<String, String> addAction(String actionName, GroupedModel groupedModel, String methodName) {
      return ActionUtil.addAction(actionName, this.getClass(), groupedModel, methodName, this.contextScopeChecker, this.contextUriInfo);
   }

   protected Map<String, String> addAction(String actionName, Long id, String methodName, Long ownerId, String permissionName, Long siteId) {
      return ActionUtil.addAction(actionName, this.getClass(), id, methodName, this.contextScopeChecker, ownerId, permissionName, siteId, this.contextUriInfo);
   }

   protected Map<String, String> addAction(String actionName, Long id, String methodName, ModelResourcePermission modelResourcePermission) {
      return ActionUtil.addAction(actionName, this.getClass(), id, methodName, this.contextScopeChecker, modelResourcePermission, this.contextUriInfo);
   }

   protected Map<String, String> addAction(String actionName, String methodName, String permissionName, Long siteId) {
      return this.addAction(actionName, siteId, methodName, (Long)null, permissionName, siteId);
   }

   protected <T, R, E extends Throwable> List<R> transform(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction) {
      return TransformUtil.transform(collection, unsafeFunction);
   }

   protected <T, R, E extends Throwable> R[] transform(T[] array, UnsafeFunction<T, R, E> unsafeFunction, Class<? extends R> clazz) {
      return (R[])TransformUtil.transform(array, unsafeFunction, clazz);
   }

   protected <T, R, E extends Throwable> R[] transformToArray(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction, Class<? extends R> clazz) {
      return (R[])TransformUtil.transformToArray(collection, unsafeFunction, clazz);
   }

   protected <T, R, E extends Throwable> List<R> transformToList(T[] array, UnsafeFunction<T, R, E> unsafeFunction) {
      return TransformUtil.transformToList(array, unsafeFunction);
   }

   protected <T, R, E extends Throwable> List<R> unsafeTransform(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction) throws E {
      return TransformUtil.unsafeTransform(collection, unsafeFunction);
   }

   protected <T, R, E extends Throwable> R[] unsafeTransform(T[] array, UnsafeFunction<T, R, E> unsafeFunction, Class<? extends R> clazz) throws E {
      return (R[])TransformUtil.unsafeTransform(array, unsafeFunction, clazz);
   }

   protected <T, R, E extends Throwable> R[] unsafeTransformToArray(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction, Class<? extends R> clazz) throws E {
      return (R[])TransformUtil.unsafeTransformToArray(collection, unsafeFunction, clazz);
   }

   protected <T, R, E extends Throwable> List<R> unsafeTransformToList(T[] array, UnsafeFunction<T, R, E> unsafeFunction) throws E {
      return TransformUtil.unsafeTransformToList(array, unsafeFunction);
   }
}
