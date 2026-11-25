package boi.headless.delivery.memorandumapi.internal.graphql.query.v1_0;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import java.util.function.BiFunction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.ComponentServiceObjects;

public class Query {
   private AcceptLanguage _acceptLanguage;
   private Company _company;
   private BiFunction<Object, String, Filter> _filterBiFunction;
   private GroupLocalService _groupLocalService;
   private HttpServletRequest _httpServletRequest;
   private HttpServletResponse _httpServletResponse;
   private RoleLocalService _roleLocalService;
   private BiFunction<Object, String, Sort[]> _sortsBiFunction;
   private UriInfo _uriInfo;
   private User _user;

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
}
