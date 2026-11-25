package com.liferay.boi.dashboard.report.application;

import com.liferay.dispatch.executor.BaseDispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutor;
import com.liferay.dispatch.executor.DispatchTaskExecutorOutput;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"dispatch.task.executor.name=ObjectEntryApproveSchedularJob", "dispatch.task.executor.type=ObjectEntryApproveSchedularJob"},
   service = {DispatchTaskExecutor.class}
)
public class ObjectEntryApproveSchedularJob extends BaseDispatchTaskExecutor {
   private static final Log log = LogFactoryUtil.getLog(ObjectEntryApproveSchedularJob.class);

   public String getName() {
      return "ObjectEntryApproveSchedularJob";
   }

   public void doExecute(DispatchTrigger dispatchTrigger, DispatchTaskExecutorOutput dispatchTaskExecutorOutput) throws Exception {
      try {
         log.info("ObjectEntryApproveSchedularJob Started:");
         (new ObjectEntryApprovalService()).fetchByStatusBatch(395809L, 40);
         log.info("ObjectEntryApproveSchedularJob End:");
      } catch (Exception e) {
         log.info("Error End:" + e.getMessage());
         e.printStackTrace();
      }

   }

   static {
      System.out.println("com.liferay.object.delete.portlet.ObjectEntryDeleteSchedularJob loaded..");
   }
}
