package com.boi.web.headless.delivery.internal.resource.v1_0.factory;

import com.boi.web.headless.delivery.internal.security.permission.LiberalPermissionChecker;
import com.boi.web.headless.delivery.resource.v1_0.DBDepartmentResource;
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
   property = {"resource.locator.key=/boi-headless-delivery-web/v1.0/DBDepartment"},
   service = {DBDepartmentResource.Factory.class}
)
public class DBDepartmentResourceFactoryImpl implements DBDepartmentResource.Factory {
   @Reference
   private CompanyLocalService _companyLocalService;
   @Reference(
      scope = ReferenceScope.PROTOTYPE_REQUIRED
   )
   private ComponentServiceObjects<DBDepartmentResource> _componentServiceObjects;
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

   public DBDepartmentResource.Builder create() {
      return new DBDepartmentResource.Builder() {
         private boolean _checkPermissions = true;
         private HttpServletRequest _httpServletRequest;
         private HttpServletResponse _httpServletResponse;
         private Locale _preferredLocale;
         private UriInfo _uriInfo;
         private User _user;

         public DBDepartmentResource build() {
            if (this._user == null) {
               throw new IllegalArgumentException("User is not set");
            } else {
               Function<InvocationHandler, DBDepartmentResource> dbDepartmentResourceProxyProviderFunction = DBDepartmentResourceFactoryImpl.ResourceProxyProviderFunctionHolder._dbDepartmentResourceProxyProviderFunction;
               return (DBDepartmentResource)dbDepartmentResourceProxyProviderFunction.apply((InvocationHandler)(proxy, method, arguments) -> DBDepartmentResourceFactoryImpl.this._invoke(method, arguments, this._checkPermissions, this._httpServletRequest, this._httpServletResponse, this._preferredLocale, this._uriInfo, this._user));
            }
         }

         public DBDepartmentResource.Builder checkPermissions(boolean checkPermissions) {
            this._checkPermissions = checkPermissions;
            return this;
         }

         public DBDepartmentResource.Builder httpServletRequest(HttpServletRequest httpServletRequest) {
            this._httpServletRequest = httpServletRequest;
            return this;
         }

         public DBDepartmentResource.Builder httpServletResponse(HttpServletResponse httpServletResponse) {
            this._httpServletResponse = httpServletResponse;
            return this;
         }

         public DBDepartmentResource.Builder preferredLocale(Locale preferredLocale) {
            this._preferredLocale = preferredLocale;
            return this;
         }

         public DBDepartmentResource.Builder uriInfo(UriInfo uriInfo) {
            this._uriInfo = uriInfo;
            return this;
         }

         public DBDepartmentResource.Builder user(User user) {
            this._user = user;
            return this;
         }
      };
   }

   private static Function<InvocationHandler, DBDepartmentResource> _getProxyProviderFunction() {
      Class<?> proxyClass = ProxyUtil.getProxyClass(DBDepartmentResource.class.getClassLoader(), new Class[]{DBDepartmentResource.class});

      try {
         Constructor<DBDepartmentResource> constructor = proxyClass.getConstructor(InvocationHandler.class);
         return (invocationHandler) -> {
            try {
               return (DBDepartmentResource)constructor.newInstance(invocationHandler);
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

      DBDepartmentResource dbDepartmentResource = (DBDepartmentResource)this._componentServiceObjects.getService();
      dbDepartmentResource.setContextAcceptLanguage(new AcceptLanguageImpl(httpServletRequest, preferredLocale, user));
      Company company = this._companyLocalService.getCompany(user.getCompanyId());
      dbDepartmentResource.setContextCompany(company);
      dbDepartmentResource.setContextHttpServletRequest(httpServletRequest);
      dbDepartmentResource.setContextHttpServletResponse(httpServletResponse);
      dbDepartmentResource.setContextUriInfo(uriInfo);
      dbDepartmentResource.setContextUser(user);
      dbDepartmentResource.setExpressionConvert(this._expressionConvert);
      dbDepartmentResource.setFilterParserProvider(this._filterParserProvider);
      dbDepartmentResource.setGroupLocalService(this._groupLocalService);
      dbDepartmentResource.setResourceActionLocalService(this._resourceActionLocalService);
      dbDepartmentResource.setResourcePermissionLocalService(this._resourcePermissionLocalService);
      dbDepartmentResource.setRoleLocalService(this._roleLocalService);
      dbDepartmentResource.setSortParserProvider(this._sortParserProvider);

      Object var13;
      try {
         var13 = method.invoke(dbDepartmentResource, arguments);
      } catch (InvocationTargetException invocationTargetException) {
         throw invocationTargetException.getTargetException();
      } finally {
         this._componentServiceObjects.ungetService(dbDepartmentResource);
         PrincipalThreadLocal.setName(name);
         PermissionThreadLocal.setPermissionChecker(permissionChecker);
      }

      return var13;
   }

   private static class ResourceProxyProviderFunctionHolder {
      private static final Function<InvocationHandler, DBDepartmentResource> _dbDepartmentResourceProxyProviderFunction = DBDepartmentResourceFactoryImpl._getProxyProviderFunction();
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
