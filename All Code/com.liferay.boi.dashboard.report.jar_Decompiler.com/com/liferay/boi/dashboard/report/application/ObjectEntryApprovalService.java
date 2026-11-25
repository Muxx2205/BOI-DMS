package com.liferay.boi.dashboard.report.application;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryTable;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import java.util.List;

public class ObjectEntryApprovalService {
   private static final Log log = LogFactoryUtil.getLog(ObjectEntryApprovalService.class);

   public void fetchByStatusBatch(long objectDefinitionId, int batchSize) {
      int start = 0;
      Integer[] statuses = new Integer[]{2, 1, 6};

      while(true) {
         DSLQuery dslQuery = DSLQueryFactoryUtil.select(ObjectEntryTable.INSTANCE).from(ObjectEntryTable.INSTANCE).where(ObjectEntryTable.INSTANCE.objectDefinitionId.eq(objectDefinitionId).and(ObjectEntryTable.INSTANCE.status.in(statuses))).limit(start, start + batchSize);
         List<ObjectEntry> entries = (List)ObjectEntryLocalServiceUtil.dslQuery(dslQuery);
         if (entries.isEmpty()) {
            return;
         }

         log.info("entries : = " + entries.size());

         for(ObjectEntry entry : entries) {
            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setCompanyId(entry.getCompanyId());
            serviceContext.setUserId(entry.getUserId());
            serviceContext.setWorkflowAction(0);
            log.info("EntryId: " + entry.getObjectEntryId());

            try {
               log.info("Entry : = " + entry.getObjectEntryId());
               this.approveObjectEntry(entry.getUserId(), entry.getCompanyId(), 20118L, entry.getObjectEntryId(), serviceContext);
            } catch (Exception e) {
               Log var10000 = log;
               long var10001 = entry.getObjectEntryId();
               var10000.info("Error EntryId: " + var10001 + " : " + e.getMessage());
            }
         }

         start += batchSize;
      }
   }

   public void approveObjectEntry(long userId, long companyId, long groupId, long objectEntryId, ServiceContext serviceContext) throws Exception {
      ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(objectEntryId);
      if (entry == null) {
         throw new IllegalArgumentException("Invalid ObjectEntryId: " + objectEntryId);
      } else {
         String className = entry.getModelClassName();
         long classPK = entry.getObjectEntryId();

         try {
            WorkflowInstanceLink link = WorkflowInstanceLinkLocalServiceUtil.fetchWorkflowInstanceLink(companyId, groupId, className, classPK);
            if (link != null) {
               long workflowInstanceId = link.getWorkflowInstanceId();
               WorkflowInstanceManagerUtil.deleteWorkflowInstance(companyId, workflowInstanceId);
            }
         } catch (Exception var17) {
         }

         ObjectEntryLocalServiceUtil.updateStatus(userId, objectEntryId, 0, serviceContext);
      }
   }
}
