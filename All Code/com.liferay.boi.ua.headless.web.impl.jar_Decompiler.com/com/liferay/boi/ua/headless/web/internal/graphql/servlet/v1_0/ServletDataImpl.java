package com.liferay.boi.ua.headless.web.internal.graphql.servlet.v1_0;

import com.liferay.boi.ua.headless.web.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.boi.ua.headless.web.internal.graphql.query.v1_0.Query;
import com.liferay.boi.ua.headless.web.internal.resource.v1_0.FolderResourceImpl;
import com.liferay.boi.ua.headless.web.resource.v1_0.FolderResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

@Component(
   service = {ServletData.class}
)
public class ServletDataImpl implements ServletData {
   private static final Map<String, ObjectValuePair<Class<?>, String>> _resourceMethodObjectValuePairs = new HashMap<String, ObjectValuePair<Class<?>, String>>() {
      {
         this.put("mutation#createFolder", new ObjectValuePair(FolderResourceImpl.class, "postFolder"));
         this.put("mutation#createFolderBatch", new ObjectValuePair(FolderResourceImpl.class, "postFolderBatch"));
      }
   };
   @Reference(
      scope = ReferenceScope.PROTOTYPE_REQUIRED
   )
   private ComponentServiceObjects<FolderResource> _folderResourceComponentServiceObjects;

   @Activate
   public void activate(BundleContext bundleContext) {
      Mutation.setFolderResourceComponentServiceObjects(this._folderResourceComponentServiceObjects);
   }

   public String getApplicationName() {
      return "BoiHeadlessUaWeb";
   }

   public Mutation getMutation() {
      return new Mutation();
   }

   public String getPath() {
      return "/boi-headless-ua-web-graphql/v1_0";
   }

   public Query getQuery() {
      return new Query();
   }

   public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(String methodName, boolean mutation) {
      return mutation ? (ObjectValuePair)_resourceMethodObjectValuePairs.get("mutation#" + methodName) : (ObjectValuePair)_resourceMethodObjectValuePairs.get("query#" + methodName);
   }
}
