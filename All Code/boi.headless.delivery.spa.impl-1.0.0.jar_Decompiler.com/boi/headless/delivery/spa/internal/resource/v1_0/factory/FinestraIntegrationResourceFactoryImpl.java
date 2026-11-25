package boi.headless.delivery.spa.internal.resource.v1_0.factory;

import boi.headless.delivery.spa.internal.security.permission.LiberalPermissionChecker;
import boi.headless.delivery.spa.resource.v1_0.FinestraIntegrationResource;
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
   property = {"resource.locator.key=/boi-headless-delivery-spa/v1.0/FinestraIntegration"},
   service = {FinestraIntegrationResource.Factory.class}
)
public class FinestraIntegrationResourceFactoryImpl implements FinestraIntegrationResource.Factory {
   @Reference
   private CompanyLocalService _companyLocalService;
   @Reference(
      scope = ReferenceScope.PROTOTYPE_REQUIRED
   )
   private ComponentServiceObjects<FinestraIntegrationResource> _componentServiceObjects;
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

   public FinestraIntegrationResource.Builder create() {
      return new FinestraIntegrationResource.Builder() {
         private boolean _checkPermissions = true;
         private HttpServletRequest _httpServletRequest;
         private HttpServletResponse _httpServletResponse;
         private Locale _preferredLocale;
         private UriInfo _uriInfo;
         private User _user;

         public FinestraIntegrationResource build() {
            if (this._user == null) {
               throw new IllegalArgumentException("User is not set");
            } else {
               Function<InvocationHandler, FinestraIntegrationResource> finestraIntegrationResourceProxyProviderFunction = FinestraIntegrationResourceFactoryImpl.ResourceProxyProviderFunctionHolder._finestraIntegrationResourceProxyProviderFunction;
               return (FinestraIntegrationResource)finestraIntegrationResourceProxyProviderFunction.apply((InvocationHandler)(proxy, method, arguments) -> FinestraIntegrationResourceFactoryImpl.this._invoke(method, arguments, this._checkPermissions, this._httpServletRequest, this._httpServletResponse, this._preferredLocale, this._uriInfo, this._user));
            }
         }

         public FinestraIntegrationResource.Builder checkPermissions(boolean checkPermissions) {
            this._checkPermissions = checkPermissions;
            return this;
         }

         public FinestraIntegrationResource.Builder httpServletRequest(HttpServletRequest httpServletRequest) {
            this._httpServletRequest = httpServletRequest;
            return this;
         }

         public FinestraIntegrationResource.Builder httpServletResponse(HttpServletResponse httpServletResponse) {
            this._httpServletResponse = httpServletResponse;
            return this;
         }

         public FinestraIntegrationResource.Builder preferredLocale(Locale preferredLocale) {
            this._preferredLocale = preferredLocale;
            return this;
         }

         public FinestraIntegrationResource.Builder uriInfo(UriInfo uriInfo) {
            this._uriInfo = uriInfo;
            return this;
         }

         public FinestraIntegrationResource.Builder user(User user) {
            this._user = user;
            return this;
         }
      };
   }

   private static Function<InvocationHandler, FinestraIntegrationResource> _getProxyProviderFunction() {
      Class<?> proxyClass = ProxyUtil.getProxyClass(FinestraIntegrationResource.class.getClassLoader(), new Class[]{FinestraIntegrationResource.class});

      try {
         Constructor<FinestraIntegrationResource> constructor = proxyClass.getConstructor(InvocationHandler.class);
         return (invocationHandler) -> {
            try {
               return (FinestraIntegrationResource)constructor.newInstance(invocationHandler);
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

      FinestraIntegrationResource finestraIntegrationResource = (FinestraIntegrationResource)this._componentServiceObjects.getService();
      finestraIntegrationResource.setContextAcceptLanguage(new AcceptLanguageImpl(httpServletRequest, preferredLocale, user));
      Company company = this._companyLocalService.getCompany(user.getCompanyId());
      finestraIntegrationResource.setContextCompany(company);
      finestraIntegrationResource.setContextHttpServletRequest(httpServletRequest);
      finestraIntegrationResource.setContextHttpServletResponse(httpServletResponse);
      finestraIntegrationResource.setContextUriInfo(uriInfo);
      finestraIntegrationResource.setContextUser(user);
      finestraIntegrationResource.setExpressionConvert(this._expressionConvert);
      finestraIntegrationResource.setFilterParserProvider(this._filterParserProvider);
      finestraIntegrationResource.setGroupLocalService(this._groupLocalService);
      finestraIntegrationResource.setResourceActionLocalService(this._resourceActionLocalService);
      finestraIntegrationResource.setResourcePermissionLocalService(this._resourcePermissionLocalService);
      finestraIntegrationResource.setRoleLocalService(this._roleLocalService);
      finestraIntegrationResource.setSortParserProvider(this._sortParserProvider);

      Object var13;
      try {
         var13 = method.invoke(finestraIntegrationResource, arguments);
      } catch (InvocationTargetException invocationTargetException) {
         throw invocationTargetException.getTargetException();
      } finally {
         this._componentServiceObjects.ungetService(finestraIntegrationResource);
         PrincipalThreadLocal.setName(name);
         PermissionThreadLocal.setPermissionChecker(permissionChecker);
      }

      return var13;
   }

   private static class ResourceProxyProviderFunctionHolder {
      private static final Function<InvocationHandler, FinestraIntegrationResource> _finestraIntegrationResourceProxyProviderFunction = FinestraIntegrationResourceFactoryImpl._getProxyProviderFunction();
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
