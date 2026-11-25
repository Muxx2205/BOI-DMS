package com.boi.web.headless.delivery.internal.resource.v1_0;

import com.boi.web.headless.delivery.resource.v1_0.CustomWorkflowLogsResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"com.boi.headless.rest.common.application.BOIWorkflowConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/custom-workflow-logs.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {CustomWorkflowLogsResource.class}
)
public class CustomWorkflowLogsResourceImpl extends BaseCustomWorkflowLogsResourceImpl {
}
