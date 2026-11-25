package com.boi.update.portlet;

import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.PortalUtil;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=UpdateFoldeId", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_boi_update_UpdateFoldeIdPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class UpdateFoldeIdPortlet extends MVCPortlet {
   public void uploadExcel(ActionRequest actionRequest, ActionResponse actionResponse) {
      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         ServiceContext serviceContext = ServiceContextFactory.getInstance(this.getClass().getName(), actionRequest);
         UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);
         File file = uploadPortletRequest.getFile("excelFile");
         String fileName = uploadPortletRequest.getFileName("excelFile");
         byte[] fileContent = Files.readAllBytes(file.toPath());
         String base64Content = Base64.getEncoder().encodeToString(fileContent);
         Map<String, Serializable> taskContextMap = new HashMap();
         taskContextMap.put("fileContent", base64Content);
         taskContextMap.put("fileName", fileName);
         taskContextMap.put("companyId", themeDisplay.getCompanyId());
         taskContextMap.put("groupId", themeDisplay.getScopeGroupId());
         taskContextMap.put("userId", themeDisplay.getUserId());
         taskContextMap.put("serviceContext", serviceContext);
         BackgroundTaskManagerUtil.addBackgroundTask(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), "UpdateFolderIdExecutor", UpdateFolderIdExecutor.class.getName(), taskContextMap, new ServiceContext());
         actionRequest.setAttribute("successMessage", "Excel file uploaded successfully. Processing started in background.");
      } catch (Exception e) {
         actionRequest.setAttribute("errorMessage", "Error uploading file: " + e.getMessage());
      }

   }
}
