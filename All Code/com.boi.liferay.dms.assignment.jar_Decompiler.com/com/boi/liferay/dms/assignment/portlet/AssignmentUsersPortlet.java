package com.boi.liferay.dms.assignment.portlet;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.persistence.KaleoTaskInstanceTokenPersistence;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   configurationPid = {"com.boi.liferay.dms.assignment.portlet.RolesAllowToAssignment"},
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=AssignmentUsers", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class AssignmentUsersPortlet extends MVCPortlet {
   private RolesAllowToAssignment allowToAssignment;
   @Reference
   KaleoTaskInstanceTokenPersistence kaleoTaskInstanceTokenPersistence;

   public void render(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
      String userName = ParamUtil.getString(renderRequest, "userName");
      ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      if (!Validator.isBlank(userName)) {
         try {
            User user = UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), userName);
            renderRequest.setAttribute("requestedUser", user);
            renderRequest.setAttribute("userName", userName);
            String keyword = ParamUtil.getString(renderRequest, "organizationData");
            if (!Validator.isBlank(keyword)) {
               long parentOrganizationId = 0L;
               List<Organization> organizations = OrganizationLocalServiceUtil.search(themeDisplay.getCompanyId(), -1L, keyword, (String)null, (Long)null, (Long)null, new LinkedHashMap(), -1, -1, OrderByComparatorFactoryUtil.create("Organization_", new Object[]{"name", true}));
               System.out.println("Fetch organizations Details " + organizations.size());
               renderRequest.setAttribute("OrgType", organizations);
            }
         } catch (Exception var10) {
         }
      }

      renderRequest.setAttribute("allowToAssignment", this.allowToAssignment.roles());
      super.render(renderRequest, renderResponse);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.allowToAssignment = (RolesAllowToAssignment)ConfigurableUtil.createConfigurable(RolesAllowToAssignment.class, properties);
   }

   private void runWorkflowEntry(ThemeDisplay themeDisplay) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(themeDisplay.getCompanyId());
         serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
         serviceContext.setUserId(themeDisplay.getUserId());
         ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(33384L);
         ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.getObjectEntry(33813L);
         WorkflowInstanceLink workflowInstanceLink = WorkflowInstanceLinkLocalServiceUtil.getWorkflowInstanceLink(themeDisplay.getCompanyId(), 20121L, objectDefinition.getClassName(), objectEntry.getObjectEntryId());
         long workflowInstanceId = workflowInstanceLink.getWorkflowInstanceId();
         Long userId = null;
         Boolean completed = Boolean.FALSE;
         int start = 0;
         int end = -1;
         OrderByComparator<WorkflowTask> orderByComparator = null;
         KaleoInstance kaleoInstance = KaleoInstanceLocalServiceUtil.fetchKaleoInstance(workflowInstanceLink.getWorkflowInstanceId());
         if (kaleoInstance == null) {
            throw new PortalException("No workflow instance found for classPK=" + objectEntry.getObjectEntryId());
         }

         System.out.println(" Token Id " + workflowInstanceId);
         WorkflowInstance workflowInstance = WorkflowInstanceManagerUtil.getWorkflowInstance(themeDisplay.getCompanyId(), workflowInstanceId);
         Map<String, Serializable> workflowContext = workflowInstance.getWorkflowContext();
         ServiceContext serviceContext1 = (ServiceContext)workflowContext.get("serviceContext");
         DynamicQuery dynamicQuery = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery();
         dynamicQuery.add(RestrictionsFactoryUtil.eq("kaleoInstanceId", workflowInstanceId));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("classPK", objectEntry.getObjectEntryId()));
         dynamicQuery.add(RestrictionsFactoryUtil.eq("className", objectDefinition.getClassName()));
         List<KaleoTaskInstanceToken> allTokens = KaleoTaskInstanceTokenLocalServiceUtil.dynamicQuery(dynamicQuery);
         if (allTokens.isEmpty()) {
            throw new PortalException("No active task tokens found for this instance.");
         }

         KaleoTaskInstanceToken activeToken = (KaleoTaskInstanceToken)allTokens.get(0);
         List<KaleoTaskInstanceToken> activeTokens = new ArrayList();

         for(KaleoTaskInstanceToken token : allTokens) {
            if (!token.isCompleted()) {
               activeTokens.add(token);
            }
         }

         if (activeTokens.isEmpty()) {
            throw new PortalException("No active tasks found for asset " + objectEntry.getObjectEntryId());
         }

         List<KaleoTaskAssignmentInstance> assignments = KaleoTaskAssignmentInstanceLocalServiceUtil.getKaleoTaskAssignmentInstances(activeToken.getKaleoTaskInstanceTokenId());
         if (assignments.isEmpty()) {
            throw new PortalException("No assignments found for active task.");
         }

         for(KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance : assignments) {
            User user = UserLocalServiceUtil.getUser(kaleoTaskAssignmentInstance.getAssigneeClassPK());
            System.out.println("Currently Assign to " + user.getScreenName());
         }

         boolean alreadyAssigned = assignments.stream().anyMatch((a) -> a.getAssigneeClassPK() == 33313L);
         if (alreadyAssigned) {
            System.out.println("User 33313 is already assigned to this task.");
            return;
         }

         KaleoTaskAssignmentInstance newAssignmentInstance = KaleoTaskAssignmentInstanceLocalServiceUtil.addKaleoTaskAssignmentInstance(20121L, activeToken, User.class.getName(), 33313L, serviceContext1);
         Indexer<KaleoTaskAssignmentInstance> indexer1 = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskAssignmentInstance.class);
         indexer1.reindex(newAssignmentInstance);
         Indexer<KaleoTaskInstanceToken> indexer = IndexerRegistryUtil.nullSafeGetIndexer(KaleoTaskInstanceToken.class);
         indexer.reindex(activeToken);
         System.out.println("Successfully added user 33313 to active workflow task.");
      } catch (Exception e) {
         System.out.println("error " + e.getMessage());
         e.printStackTrace();
         System.out.println();
         System.out.println();
         System.out.println();
         System.out.println();
         System.out.println();
         System.out.println();
         System.out.println();
      }

   }
}
