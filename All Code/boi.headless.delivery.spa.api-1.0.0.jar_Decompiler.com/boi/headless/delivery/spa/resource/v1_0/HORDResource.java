package boi.headless.delivery.spa.resource.v1_0;

import boi.headless.delivery.spa.dto.v1_0.BOIHeadlessResponse;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface HORDResource {
   BOIHeadlessResponse getHordDtl(String var1, Filter var2, Pagination var3, Sort[] var4) throws Exception;

   BOIHeadlessResponse getHORD(Integer var1) throws Exception;

   BOIHeadlessResponse addHordAsDraft(MultipartBody var1) throws Exception;

   BOIHeadlessResponse addHordAsExport(MultipartBody var1) throws Exception;

   default void setContextAcceptLanguage(AcceptLanguage contextAcceptLanguage) {
   }

   void setContextCompany(Company var1);

   default void setContextHttpServletRequest(HttpServletRequest contextHttpServletRequest) {
   }

   default void setContextHttpServletResponse(HttpServletResponse contextHttpServletResponse) {
   }

   default void setContextUriInfo(UriInfo contextUriInfo) {
   }

   void setContextUser(User var1);

   void setExpressionConvert(ExpressionConvert<Filter> var1);

   void setFilterParserProvider(FilterParserProvider var1);

   void setGroupLocalService(GroupLocalService var1);

   void setResourceActionLocalService(ResourceActionLocalService var1);

   void setResourcePermissionLocalService(ResourcePermissionLocalService var1);

   void setRoleLocalService(RoleLocalService var1);

   void setSortParserProvider(SortParserProvider var1);

   default Filter toFilter(String filterString) {
      return this.toFilter(filterString, Collections.emptyMap());
   }

   default Filter toFilter(String filterString, Map<String, List<String>> multivaluedMap) {
      return null;
   }

   default Sort[] toSorts(String sortsString) {
      return new Sort[0];
   }

   @ProviderType
   public interface Builder {
      HORDResource build();

      Builder checkPermissions(boolean var1);

      Builder httpServletRequest(HttpServletRequest var1);

      Builder httpServletResponse(HttpServletResponse var1);

      Builder preferredLocale(Locale var1);

      Builder uriInfo(UriInfo var1);

      Builder user(User var1);
   }

   @ProviderType
   public interface Factory {
      Builder create();
   }
}
