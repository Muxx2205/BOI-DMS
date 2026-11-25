package com.liferay.object.delete.portlet;

import com.liferay.osgi.service.tracker.collections.map.ServiceReferenceMapperFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class NodeExecutorRegistryUtil {
   private static final ServiceTrackerMap<String, NodeExecutor> _serviceTrackerMap;

   public static NodeExecutor getNodeExecutor(String nodeTypeString) {
      return (NodeExecutor)_serviceTrackerMap.getService(nodeTypeString);
   }

   static {
      Bundle bundle = FrameworkUtil.getBundle(NodeExecutorRegistryUtil.class);
      BundleContext bundleContext = bundle.getBundleContext();
      _serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(bundleContext, NodeExecutor.class, (String)null, ServiceReferenceMapperFactory.create(bundleContext, (nodeExecutor, emitter) -> emitter.emit(String.valueOf(nodeExecutor.getNodeType()))));
   }
}
