package boi.headless.delivery.mail.internal.graphql.servlet.v1_0;

import boi.headless.delivery.mail.internal.graphql.mutation.v1_0.Mutation;
import boi.headless.delivery.mail.internal.graphql.query.v1_0.Query;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
   service = {ServletData.class}
)
public class ServletDataImpl implements ServletData {
   private static final Map<String, ObjectValuePair<Class<?>, String>> _resourceMethodObjectValuePairs = new HashMap<String, ObjectValuePair<Class<?>, String>>() {
   };

   @Activate
   public void activate(BundleContext bundleContext) {
   }

   public String getApplicationName() {
      return "BoiHeadlessDeliveryMail";
   }

   public Mutation getMutation() {
      return new Mutation();
   }

   public String getPath() {
      return "/boi-headless-delivery-mail-graphql/v1_0";
   }

   public Query getQuery() {
      return new Query();
   }

   public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(String methodName, boolean mutation) {
      return mutation ? (ObjectValuePair)_resourceMethodObjectValuePairs.get("mutation#" + methodName) : (ObjectValuePair)_resourceMethodObjectValuePairs.get("query#" + methodName);
   }
}
