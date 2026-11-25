package com.liferay.boi.ua.headless.web.internal.graphql.mutation.v1_0;

import com.liferay.boi.ua.headless.web.dto.v1_0.Folder;
import com.liferay.boi.ua.headless.web.dto.v1_0.FolderCreateRequest;
import com.liferay.boi.ua.headless.web.resource.v1_0.FolderResource;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineExportTaskResource;
import com.liferay.portal.vulcan.batch.engine.resource.VulcanBatchEngineImportTaskResource;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import java.util.function.BiFunction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.ComponentServiceObjects;

public class Mutation {
   private static ComponentServiceObjects<FolderResource> _folderResourceComponentServiceObjects;
   private AcceptLanguage _acceptLanguage;
   private Company _company;
   private GroupLocalService _groupLocalService;
   private HttpServletRequest _httpServletRequest;
   private HttpServletResponse _httpServletResponse;
   private RoleLocalService _roleLocalService;
   private BiFunction<Object, String, Sort[]> _sortsBiFunction;
   private UriInfo _uriInfo;
   private User _user;
   private VulcanBatchEngineExportTaskResource _vulcanBatchEngineExportTaskResource;
   private VulcanBatchEngineImportTaskResource _vulcanBatchEngineImportTaskResource;

   public static void setFolderResourceComponentServiceObjects(ComponentServiceObjects<FolderResource> folderResourceComponentServiceObjects) {
      _folderResourceComponentServiceObjects = folderResourceComponentServiceObjects;
   }

   public @GraphQLField Folder createFolder(@GraphQLName("folderCreateRequest") FolderCreateRequest folderCreateRequest) throws Exception {
      return (Folder)this._applyComponentServiceObjects(_folderResourceComponentServiceObjects, this::_populateResourceContext, (folderResource) -> folderResource.postFolder(folderCreateRequest));
   }

   public @GraphQLField Response createFolderBatch(@GraphQLName("folderCreateRequest") FolderCreateRequest folderCreateRequest, @GraphQLName("callbackURL") String callbackURL, @GraphQLName("object") Object object) throws Exception {
      return (Response)this._applyComponentServiceObjects(_folderResourceComponentServiceObjects, this::_populateResourceContext, (folderResource) -> folderResource.postFolderBatch(folderCreateRequest, callbackURL, object));
   }

   private <T, R, E1 extends Throwable, E2 extends Throwable> R _applyComponentServiceObjects(ComponentServiceObjects<T> componentServiceObjects, UnsafeConsumer<T, E1> unsafeConsumer, UnsafeFunction<T, R, E2> unsafeFunction) throws E1, E2 {
      T resource = (T)componentServiceObjects.getService();

      Object var5;
      try {
         unsafeConsumer.accept(resource);
         var5 = unsafeFunction.apply(resource);
      } finally {
         componentServiceObjects.ungetService(resource);
      }

      return (R)var5;
   }

   private <T, E1 extends Throwable, E2 extends Throwable> void _applyVoidComponentServiceObjects(ComponentServiceObjects<T> componentServiceObjects, UnsafeConsumer<T, E1> unsafeConsumer, UnsafeConsumer<T, E2> unsafeFunction) throws E1, E2 {
      T resource = (T)componentServiceObjects.getService();

      try {
         unsafeConsumer.accept(resource);
         unsafeFunction.accept(resource);
      } finally {
         componentServiceObjects.ungetService(resource);
      }

   }

   private void _populateResourceContext(FolderResource folderResource) throws Exception {
      folderResource.setContextAcceptLanguage(this._acceptLanguage);
      folderResource.setContextCompany(this._company);
      folderResource.setContextHttpServletRequest(this._httpServletRequest);
      folderResource.setContextHttpServletResponse(this._httpServletResponse);
      folderResource.setContextUriInfo(this._uriInfo);
      folderResource.setContextUser(this._user);
      folderResource.setGroupLocalService(this._groupLocalService);
      folderResource.setRoleLocalService(this._roleLocalService);
      folderResource.setVulcanBatchEngineExportTaskResource(this._vulcanBatchEngineExportTaskResource);
      folderResource.setVulcanBatchEngineImportTaskResource(this._vulcanBatchEngineImportTaskResource);
   }
}
