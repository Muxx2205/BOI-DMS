package boi.headless.delivery.dms.common.internal.resource.v1_0.factory;

import boi.headless.delivery.dms.common.internal.security.permission.LiberalPermissionChecker;
import boi.headless.delivery.dms.common.resource.v1_0.DashboardGraphResource;
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
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
   property = {"resource.locator.key=/boi-headless-delivery-dms-common/v1.0/DashboardGraph"},
   service = {DashboardGraphResource.Factory.class}
)
public class DashboardGraphResourceFactoryImpl implements DashboardGraphResource.Factory {
   private static final Function<InvocationHandler, DashboardGraphResource> _dashboardGraphResourceProxyProviderFunction = _getProxyProviderFunction();
   @Reference
   private CompanyLocalService _companyLocalService;
   @Reference(
      scope = ReferenceScope.PROTOTYPE_REQUIRED
   )
   private ComponentServiceObjects<DashboardGraphResource> _componentServiceObjects;
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

   public DashboardGraphResource.Builder create() {
      return new DashboardGraphResource.Builder() {
         private boolean _checkPermissions = true;
         private HttpServletRequest _httpServletRequest;
         private HttpServletResponse _httpServletResponse;
         private Locale _preferredLocale;
         private User _user;

         public DashboardGraphResource build() {
            if (this._user == null) {
               throw new IllegalArgumentException("User is not set");
            } else {
               return (DashboardGraphResource)DashboardGraphResourceFactoryImpl._dashboardGraphResourceProxyProviderFunction.apply((InvocationHandler)(proxy, method, arguments) -> DashboardGraphResourceFactoryImpl.this._invoke(method, arguments, this._checkPermissions, this._httpServletRequest, this._httpServletResponse, this._preferredLocale, this._user));
            }
         }

         public DashboardGraphResource.Builder checkPermissions(boolean checkPermissions) {
            this._checkPermissions = checkPermissions;
            return this;
         }

         public DashboardGraphResource.Builder httpServletRequest(HttpServletRequest httpServletRequest) {
            this._httpServletRequest = httpServletRequest;
            return this;
         }

         public DashboardGraphResource.Builder httpServletResponse(HttpServletResponse httpServletResponse) {
            this._httpServletResponse = httpServletResponse;
            return this;
         }

         public DashboardGraphResource.Builder preferredLocale(Locale preferredLocale) {
            this._preferredLocale = preferredLocale;
            return this;
         }

         public DashboardGraphResource.Builder user(User user) {
            this._user = user;
            return this;
         }
      };
   }

   private static Function<InvocationHandler, DashboardGraphResource> _getProxyProviderFunction() {
      Class<?> proxyClass = ProxyUtil.getProxyClass(DashboardGraphResource.class.getClassLoader(), new Class[]{DashboardGraphResource.class});

      try {
         Constructor<DashboardGraphResource> constructor = proxyClass.getConstructor(InvocationHandler.class);
         return (invocationHandler) -> {
            try {
               return (DashboardGraphResource)constructor.newInstance(invocationHandler);
            } catch (ReflectiveOperationException reflectiveOperationException) {
               throw new InternalError(reflectiveOperationException);
            }
         };
      } catch (NoSuchMethodException noSuchMethodException) {
         throw new InternalError(noSuchMethodException);
      }
   }

   private Object _invoke(Method method, Object[] arguments, boolean checkPermissions, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale preferredLocale, User user) throws Throwable {
      String name = PrincipalThreadLocal.getName();
      PrincipalThreadLocal.setName(user.getUserId());
      PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();
      if (checkPermissions) {
         PermissionThreadLocal.setPermissionChecker(this._defaultPermissionCheckerFactory.create(user));
      } else {
         PermissionThreadLocal.setPermissionChecker(new LiberalPermissionChecker(user));
      }

      DashboardGraphResource dashboardGraphResource = (DashboardGraphResource)this._componentServiceObjects.getService();
      dashboardGraphResource.setContextAcceptLanguage(new AcceptLanguageImpl(httpServletRequest, preferredLocale, user));
      Company company = this._companyLocalService.getCompany(user.getCompanyId());
      dashboardGraphResource.setContextCompany(company);
      dashboardGraphResource.setContextHttpServletRequest(httpServletRequest);
      dashboardGraphResource.setContextHttpServletResponse(httpServletResponse);
      dashboardGraphResource.setContextUser(user);
      dashboardGraphResource.setExpressionConvert(this._expressionConvert);
      dashboardGraphResource.setFilterParserProvider(this._filterParserProvider);
      dashboardGraphResource.setGroupLocalService(this._groupLocalService);
      dashboardGraphResource.setResourceActionLocalService(this._resourceActionLocalService);
      dashboardGraphResource.setResourcePermissionLocalService(this._resourcePermissionLocalService);
      dashboardGraphResource.setRoleLocalService(this._roleLocalService);
      dashboardGraphResource.setSortParserProvider(this._sortParserProvider);

      Object var12;
      try {
         var12 = method.invoke(dashboardGraphResource, arguments);
      } catch (InvocationTargetException invocationTargetException) {
         throw invocationTargetException.getTargetException();
      } finally {
         this._componentServiceObjects.ungetService(dashboardGraphResource);
         PrincipalThreadLocal.setName(name);
         PermissionThreadLocal.setPermissionChecker(permissionChecker);
      }

      return var12;
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
