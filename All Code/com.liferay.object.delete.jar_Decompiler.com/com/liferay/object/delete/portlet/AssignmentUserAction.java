package com.liferay.object.delete.portlet;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.workflow.kaleo.KaleoWorkflowModelConverter;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.GraphWalker;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceTokenLocalServiceUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=com_liferay_object_delete_ObjectEntryDeletionPortlet", "mvc.command.name=saveIt"},
   service = {MVCActionCommand.class}
)
public class AssignmentUserAction implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(AssignmentUserAction.class);
   @Reference
   private KaleoWorkflowModelConverter _kaleoWorkflowModelConverter;
   @Reference
   private GraphWalker _graphWalker;

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String classPks = ParamUtil.getString(actionRequest, "classPk");
      Long[] classPKLongArray = (Long[])Arrays.stream(classPks.split(",")).map(String::trim).map(Long::parseLong).toArray((x$0) -> new Long[x$0]);

      try {
         for(Long classPk : classPKLongArray) {
            log.info("classPk " + classPk);
            this.toWorkflowTask(classPk);
         }
      } catch (PortalException e) {
         e.printStackTrace();
      }

      return false;
   }

   private void toWorkflowTask(long classPk) throws PortalException {
      long groupId = 20121L;
      ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(classPk);
      ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectEntry.getObjectDefinitionId());
      WorkflowInstanceLink workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil.fetchWorkflowInstanceLink(objectDefinition.getCompanyId(), groupId, objectDefinition.getClassName(), classPk);
      if (workflowInstanceLink == null) {
         log.info("No workflow instance link found for classPK=" + classPk);
      } else {
         long workflowInstanceId = workflowInstanceLink.getWorkflowInstanceId();
         log.info("workflowInstanceId " + workflowInstanceId);
         WorkflowInstance workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(objectDefinition.getCompanyId(), workflowInstanceId);
         Map<String, Serializable> workflowContext = workflowInstance.getWorkflowContext();
         ServiceContext serviceContext = (ServiceContext)workflowContext.get("serviceContext");
         List<KaleoInstanceToken> kaleoInstanceTokens = KaleoInstanceTokenLocalServiceUtil.getKaleoInstanceTokens(workflowInstanceId);
         log.info("kaleoInstanceTokens " + kaleoInstanceTokens.isEmpty());

         for(KaleoInstanceToken kaleoInstanceToken : kaleoInstanceTokens) {
            if (!kaleoInstanceToken.isCompleted()) {
               ExecutionContext executionContext = new ExecutionContext(kaleoInstanceToken, workflowContext, serviceContext);
               NodeExecutor _taskNodeExecutor = NodeExecutorRegistryUtil.getNodeExecutor(kaleoInstanceToken.getCurrentKaleoNode().getType());
               log.info("_taskNodeExecutor " + (_taskNodeExecutor == null));
               if (Validator.isNotNull(_taskNodeExecutor)) {
                  log.info("_taskNodeExecutor getNodeType " + _taskNodeExecutor.getNodeType());
                  ExecutionContext newExecutionContext = new ExecutionContext(executionContext.getKaleoInstanceToken(), executionContext.getKaleoTaskInstanceToken(), executionContext.getWorkflowContext(), executionContext.getServiceContext());
                  KaleoNode currenctNode = kaleoInstanceToken.getCurrentKaleoNode();
                  KaleoNode nextNode = kaleoInstanceToken.getCurrentKaleoNode().getDefaultKaleoTransition().getTargetKaleoNode();
                  log.info("currenctNode " + currenctNode.getLabel());
                  log.info("nextNode " + nextNode.getLabel());
                  PathElement pathElement = new PathElement(currenctNode, nextNode, newExecutionContext);
                  List<PathElement> pathElements = new ArrayList();
                  pathElements.add(pathElement);
                  this._graphWalker.follow(currenctNode, nextNode, pathElements, newExecutionContext);
                  log.info("workflow Task 121232 _taskNodeExecutor.execute Done");
               }
            }
         }

      }
   }
}
