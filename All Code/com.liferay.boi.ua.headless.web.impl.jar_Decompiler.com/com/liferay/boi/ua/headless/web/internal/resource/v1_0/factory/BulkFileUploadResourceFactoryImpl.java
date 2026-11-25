package com.liferay.boi.ua.headless.web.internal.resource.v1_0.factory;

import com.liferay.boi.ua.headless.web.internal.security.permission.LiberalPermissionChecker;
import com.liferay.boi.ua.headless.web.resource.v1_0.BulkFileUploadResource;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.odata.filter.ExpressionConvert;
import com.liferay.portal.odata.filter.FilterParserProvider;
import com.liferay.portal.odata.sort.SortParserProvider;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
   property = {"resource.locator.key=/boi-headless-ua-web/v1.0/BulkFileUpload"},
   service = {BulkFileUploadResource.Factory.class}
)
public class BulkFileUploadResourceFactoryImpl implements BulkFileUploadResource.Factory {
   @Reference
   private CompanyLocalService _companyLocalService;
   @Reference(
      scope = ReferenceScope.PROTOTYPE_REQUIRED
   )
   private ComponentServiceObjects<BulkFileUploadResource> _componentServiceObjects;
   @Reference
   private PermissionCheckerFactory _defaultPermissionCheckerFactory;
   @Reference(
      target = "(result.class.name=com.liferay.portal.kernel.search.filter.Filter)"
   )
   private ExpressionConvert<Filter> _expressionConvert;
   @Reference
   private FilterParserProvider _filterParserProvider;
   @Reference
   private GroupLocalService _groupLocalService;
   @Reference
   private ResourceActionLocalService _resourceActionLocalService;
   @Reference
   private ResourcePermissionLocalService _resourcePermissionLocalService;
   @Reference
   private RoleLocalService _roleLocalService;
   @Reference
   private SortParserProvider _sortParserProvider;
   @Reference
   private UserLocalService _userLocalService;

   public BulkFileUploadResource.Builder create() {
      return new BulkFileUploadResource.Builder() {
         private boolean _checkPermissions = true;
         private HttpServletRequest _httpServletRequest;
         private HttpServletResponse _httpServletResponse;
         private Locale _preferredLocale;
         private UriInfo _uriInfo;
         private User _user;

         public BulkFileUploadResource build() {
            if (this._user == null) {
               throw new IllegalArgumentException("User is not set");
            } else {
               Function<InvocationHandler, BulkFileUploadResource> bulkFileUploadResourceProxyProviderFunction = BulkFileUploadResourceFactoryImpl.ResourceProxyProviderFunctionHolder._bulkFileUploadResourceProxyProviderFunction;
               return (BulkFileUploadResource)bulkFileUploadResourceProxyProviderFunction.apply((InvocationHandler)(proxy, method, arguments) -> BulkFileUploadResourceFactoryImpl.this._invoke(method, arguments, this._checkPermissions, this._httpServletRequest, this._httpServletResponse, this._preferredLocale, this._uriInfo, this._user));
            }
         }

         public BulkFileUploadResource.Builder checkPermissions(boolean checkPermissions) {
            this._checkPermissions = checkPermissions;
            return this;
         }

         public BulkFileUploadResource.Builder httpServletRequest(HttpServletRequest httpServletRequest) {
            this._httpServletRequest = httpServletRequest;
            return this;
         }

         public BulkFileUploadResource.Builder httpServletResponse(HttpServletResponse httpServletResponse) {
            this._httpServletResponse = httpServletResponse;
            return this;
         }

         public BulkFileUploadResource.Builder preferredLocale(Locale preferredLocale) {
            this._preferredLocale = preferredLocale;
            return this;
         }

         public BulkFileUploadResource.Builder uriInfo(UriInfo uriInfo) {
            this._uriInfo = uriInfo;
            return this;
         }

         public BulkFileUploadResource.Builder user(User user) {
            this._user = user;
            return this;
         }
      };
   }

   private static Function<InvocationHandler, BulkFileUploadResource> _getProxyProviderFunction() {
      Class<?> proxyClass = ProxyUtil.getProxyClass(BulkFileUploadResource.class.getClassLoader(), new Class[]{BulkFileUploadResource.class});

      try {
         Constructor<BulkFileUploadResource> constructor = proxyClass.getConstructor(InvocationHandler.class);
         return (invocationHandler) -> {
            try {
               return (BulkFileUploadResource)constructor.newInstance(invocationHandler);
            } catch (ReflectiveOperationException reflectiveOperationException) {
               throw new InternalError(reflectiveOperationException);
            }
         };
      } catch (NoSuchMethodException noSuchMethodException) {
         throw new InternalError(noSuchMethodException);
      }
   }

   private Object _invoke(Method method, Object[] arguments, boolean checkPermissions, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale preferredLocale, UriInfo uriInfo, User user) throws Throwable {
      String name = PrincipalThreadLocal.getName();
      PrincipalThreadLocal.setName(user.getUserId());
      PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();
      if (checkPermissions) {
         PermissionThreadLocal.setPermissionChecker(this._defaultPermissionCheckerFactory.create(user));
      } else {
         PermissionThreadLocal.setPermissionChecker(new LiberalPermissionChecker(user));
      }

      BulkFileUploadResource bulkFileUploadResource = (BulkFileUploadResource)this._componentServiceObjects.getService();
      bulkFileUploadResource.setContextAcceptLanguage(new AcceptLanguageImpl(httpServletRequest, preferredLocale, user));
      Company company = this._companyLocalService.getCompany(user.getCompanyId());
      bulkFileUploadResource.setContextCompany(company);
      bulkFileUploadResource.setContextHttpServletRequest(httpServletRequest);
      bulkFileUploadResource.setContextHttpServletResponse(httpServletResponse);
      bulkFileUploadResource.setContextUriInfo(uriInfo);
      bulkFileUploadResource.setContextUser(user);
      bulkFileUploadResource.setExpressionConvert(this._expressionConvert);
      bulkFileUploadResource.setFilterParserProvider(this._filterParserProvider);
      bulkFileUploadResource.setGroupLocalService(this._groupLocalService);
      bulkFileUploadResource.setResourceActionLocalService(this._resourceActionLocalService);
      bulkFileUploadResource.setResourcePermissionLocalService(this._resourcePermissionLocalService);
      bulkFileUploadResource.setRoleLocalService(this._roleLocalService);
      bulkFileUploadResource.setSortParserProvider(this._sortParserProvider);

      Object var13;
      try {
         var13 = method.invoke(bulkFileUploadResource, arguments);
      } catch (InvocationTargetException invocationTargetException) {
         throw invocationTargetException.getTargetException();
      } finally {
         this._componentServiceObjects.ungetService(bulkFileUploadResource);
         PrincipalThreadLocal.setName(name);
         PermissionThreadLocal.setPermissionChecker(permissionChecker);
      }

      return var13;
   }

   private static class ResourceProxyProviderFunctionHolder {
      private static final Function<InvocationHandler, BulkFileUploadResource> _bulkFileUploadResourceProxyProviderFunction = BulkFileUploadResourceFactoryImpl._getProxyProviderFunction();
   }

   private class AcceptLanguageImpl implements AcceptLanguage {
      private final HttpServletRequest _httpServletRequest;
      private final Locale _preferredLocale;
      private final User _user;

      public AcceptLanguageImpl(HttpServletRequest httpServletRequest, Locale preferredLocale, User user) {
         this._httpServletRequest = httpServletRequest;
         this._preferredLocale = preferredLocale;
         this._user = user;
      }

      public List<Locale> getLocales() {
         return Arrays.asList(this.getPreferredLocale());
      }

      public String getPreferredLanguageId() {
         return LocaleUtil.toLanguageId(this.getPreferredLocale());
      }

      public Locale getPreferredLocale() {
         if (this._preferredLocale != null) {
            return this._preferredLocale;
         } else {
            if (this._httpServletRequest != null) {
               Locale locale = (Locale)this._httpServletRequest.getAttribute("LOCALE");
               if (locale != null) {
                  return locale;
               }
            }

            return this._user.getLocale();
         }
      }

      public boolean isAcceptAllLanguages() {
         return false;
      }
   }
}
