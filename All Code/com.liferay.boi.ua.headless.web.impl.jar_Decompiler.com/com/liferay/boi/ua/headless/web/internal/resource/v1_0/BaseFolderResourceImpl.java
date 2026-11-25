package com.liferay.boi.ua.headless.web.internal.resource.v1_0;

import com.liferay.boi.ua.headless.web.dto.v1_0.Folder;
import com.liferay.boi.ua.headless.web.dto.v1_0.FolderCreateRequest;
import com.liferay.boi.ua.headless.web.resource.v1_0.FolderResource;
import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.petra.function.transform.TransformUtil;
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
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.odata.filter.ExpressionConvert;
import com.liferay.portal.odata.filter.FilterParser;
import com.liferay.portal.odata.filter.FilterParserProvider;
import com.liferay.portal.odata.sort.SortField;
import com.liferay.portal.odata.sort.SortParser;
import com.liferay.portal.odata.sort.SortParserProvider;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.batch.engine.VulcanBatchEngineTaskItemDelegate;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineExportTaskResource;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineImportTaskResource;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.resource.EntityModelResource;
import com.liferay.portal.vulcan.util.ActionUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/v1.0")
public abstract class BaseFolderResourceImpl implements EntityModelResource, FolderResource, VulcanBatchEngineTaskItemDelegate<Folder> {
   protected AcceptLanguage contextAcceptLanguage;
   protected UnsafeBiConsumer<Collection<Folder>, UnsafeFunction<Folder, Folder, Exception>, Exception> contextBatchUnsafeBiConsumer;
   protected UnsafeBiConsumer<Collection<Folder>, UnsafeConsumer<Folder, Exception>, Exception> contextBatchUnsafeConsumer;
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
   protected VulcanBatchEngineExportTaskResource vulcanBatchEngineExportTaskResource;
   protected VulcanBatchEngineImportTaskResource vulcanBatchEngineImportTaskResource;
   private static final Log _log = LogFactoryUtil.getLog(BaseFolderResourceImpl.class);

   @Tags({@Tag(
   name = "Folder"
)})
   @Consumes({"application/json", "application/xml"})
   @Path("/folders")
   @POST
   @Produces({"application/json", "application/xml"})
   public Folder postFolder(FolderCreateRequest folderCreateRequest) throws Exception {
      return new Folder();
   }

   @Parameters({@Parameter(
   in = ParameterIn.QUERY,
   name = "callbackURL"
)})
   @Tags({@Tag(
   name = "Folder"
)})
   @Consumes({"application/json"})
   @Path("/folders/batch")
   @POST
   @Produces({"application/json"})
   public Response postFolderBatch(FolderCreateRequest folderCreateRequest, @Parameter(hidden = true) @QueryParam("callbackURL") String callbackURL, Object object) throws Exception {
      this.vulcanBatchEngineImportTaskResource.setContextAcceptLanguage(this.contextAcceptLanguage);
      this.vulcanBatchEngineImportTaskResource.setContextCompany(this.contextCompany);
      this.vulcanBatchEngineImportTaskResource.setContextHttpServletRequest(this.contextHttpServletRequest);
      this.vulcanBatchEngineImportTaskResource.setContextUriInfo(this.contextUriInfo);
      this.vulcanBatchEngineImportTaskResource.setContextUser(this.contextUser);
      Response.ResponseBuilder responseBuilder = Response.accepted();
      return responseBuilder.entity(this.vulcanBatchEngineImportTaskResource.postImportTask(Folder.class.getName(), callbackURL, (String)null, object)).build();
   }

   public void create(Collection<Folder> folders, Map<String, Serializable> parameters) throws Exception {
      UnsafeFunction<Folder, Folder, Exception> folderUnsafeFunction = null;
      String createStrategy = (String)parameters.getOrDefault("createStrategy", "INSERT");
      if (StringUtil.equalsIgnoreCase(createStrategy, "INSERT")) {
         folderUnsafeFunction = (folderx) -> this.postFolder((FolderCreateRequest)parameters.get("folderCreateRequest"));
      }

      if (folderUnsafeFunction == null) {
         throw new NotSupportedException("Create strategy \"" + createStrategy + "\" is not supported for Folder");
      } else {
         if (this.contextBatchUnsafeBiConsumer != null) {
            this.contextBatchUnsafeBiConsumer.accept(folders, folderUnsafeFunction);
         } else if (this.contextBatchUnsafeConsumer != null) {
            UnsafeBiConsumer var10000 = this.contextBatchUnsafeConsumer;
            Objects.requireNonNull(folderUnsafeFunction);
            var10000.accept(folders, folderUnsafeFunction::apply);
         } else {
            for(Folder folder : folders) {
               folderUnsafeFunction.apply(folder);
            }
         }

      }
   }

   public void delete(Collection<Folder> folders, Map<String, Serializable> parameters) throws Exception {
      throw new UnsupportedOperationException("This method needs to be implemented");
   }

   public Set<String> getAvailableCreateStrategies() {
      return SetUtil.fromArray(new String[]{"INSERT"});
   }

   public Set<String> getAvailableUpdateStrategies() {
      return SetUtil.fromArray(new String[0]);
   }

   public EntityModel getEntityModel(Map<String, List<String>> multivaluedMap) throws Exception {
      return this.getEntityModel(new MultivaluedHashMap(multivaluedMap));
   }

   public EntityModel getEntityModel(MultivaluedMap multivaluedMap) throws Exception {
      return null;
   }

   public String getVersion() {
      return "v1.0";
   }

   public Page<Folder> read(Filter filter, Pagination pagination, Sort[] sorts, Map<String, Serializable> parameters, String search) throws Exception {
      throw new UnsupportedOperationException("This method needs to be implemented");
   }

   public void setLanguageId(final String languageId) {
      this.contextAcceptLanguage = new AcceptLanguage() {
         public List<Locale> getLocales() {
            return null;
         }

         public String getPreferredLanguageId() {
            return languageId;
         }

         public Locale getPreferredLocale() {
            return LocaleUtil.fromLanguageId(languageId);
         }
      };
   }

   public void update(Collection<Folder> folders, Map<String, Serializable> parameters) throws Exception {
      throw new UnsupportedOperationException("This method needs to be implemented");
   }

   public void setContextAcceptLanguage(AcceptLanguage contextAcceptLanguage) {
      this.contextAcceptLanguage = contextAcceptLanguage;
   }

   public void setContextBatchUnsafeBiConsumer(UnsafeBiConsumer<Collection<Folder>, UnsafeFunction<Folder, Folder, Exception>, Exception> contextBatchUnsafeBiConsumer) {
      this.contextBatchUnsafeBiConsumer = contextBatchUnsafeBiConsumer;
   }

   public void setContextBatchUnsafeConsumer(UnsafeBiConsumer<Collection<Folder>, UnsafeConsumer<Folder, Exception>, Exception> contextBatchUnsafeConsumer) {
      this.contextBatchUnsafeConsumer = contextBatchUnsafeConsumer;
   }

   public void setContextCompany(Company contextCompany) {
      this.contextCompany = contextCompany;
   }

   public void setContextHttpServletRequest(HttpServletRequest contextHttpServletRequest) {
      if (contextHttpServletRequest != null && contextHttpServletRequest.getAttribute("CTX") == null) {
         contextHttpServletRequest.setAttribute("CTX", ServletContextPool.get(""));
      }

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

   public void setVulcanBatchEngineExportTaskResource(VulcanBatchEngineExportTaskResource vulcanBatchEngineExportTaskResource) {
      this.vulcanBatchEngineExportTaskResource = vulcanBatchEngineExportTaskResource;
   }

   public void setVulcanBatchEngineImportTaskResource(VulcanBatchEngineImportTaskResource vulcanBatchEngineImportTaskResource) {
      this.vulcanBatchEngineImportTaskResource = vulcanBatchEngineImportTaskResource;
   }

   public Filter toFilter(String filterString, Map<String, List<String>> multivaluedMap) {
      try {
         EntityModel entityModel = this.getEntityModel(multivaluedMap);
         FilterParser filterParser = this.filterParserProvider.provide(entityModel);
         com.liferay.portal.odata.filter.Filter oDataFilter = new com.liferay.portal.odata.filter.Filter(filterParser.parse(filterString));
         return (Filter)this.expressionConvert.convert(oDataFilter.getExpression(), this.contextAcceptLanguage.getPreferredLocale(), entityModel);
      } catch (Exception exception) {
         _log.error("Invalid filter " + filterString, exception);
         return null;
      }
   }

   public Sort[] toSorts(String sortString) {
      if (Validator.isNull(sortString)) {
         return null;
      } else {
         try {
            SortParser sortParser = this.sortParserProvider.provide(this.getEntityModel(Collections.emptyMap()));
            if (sortParser == null) {
               return null;
            } else {
               com.liferay.portal.odata.sort.Sort oDataSort = new com.liferay.portal.odata.sort.Sort(sortParser.parse(sortString));
               List<SortField> sortFields = oDataSort.getSortFields();
               Sort[] sorts = new Sort[sortFields.size()];

               for(int i = 0; i < sortFields.size(); ++i) {
                  SortField sortField = (SortField)sortFields.get(i);
                  sorts[i] = new Sort(sortField.getSortableFieldName(this.contextAcceptLanguage.getPreferredLocale()), !sortField.isAscending());
               }

               return sorts;
            }
         } catch (Exception exception) {
            _log.error("Invalid sort " + sortString, exception);
            return new Sort[0];
         }
      }
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

   protected <T, R, E extends Throwable> long[] transformToLongArray(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction) {
      return TransformUtil.transformToLongArray(collection, unsafeFunction);
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

   protected <T, R, E extends Throwable> long[] unsafeTransformToLongArray(Collection<T> collection, UnsafeFunction<T, R, E> unsafeFunction) throws E {
      return TransformUtil.unsafeTransformToLongArray(collection, unsafeFunction);
   }
}
