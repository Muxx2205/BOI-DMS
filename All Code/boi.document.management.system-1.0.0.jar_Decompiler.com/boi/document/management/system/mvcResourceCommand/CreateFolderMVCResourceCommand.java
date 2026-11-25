package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderService;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=createFolderResource"},
   service = {MVCResourceCommand.class}
)
public class CreateFolderMVCResourceCommand implements MVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(CreateFolderMVCResourceCommand.class);
   @Reference
   private DLFolderService _dlFolderService;

   public boolean serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      JSONObject responseJson = JSONFactoryUtil.createJSONObject();
      long userId = themeDisplay.getUserId();
      User user = null;

      try {
         user = UserLocalServiceUtil.getUser(userId);
         boolean isHavingAddUpdateAction = user.getRoles().stream().anyMatch((x) -> x.getName().equalsIgnoreCase("DMS Create Rights"));
         if (!isHavingAddUpdateAction) {
            responseJson.put("success", false);
            responseJson.put("error", "Create access denied.");
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         String folderName = ParamUtil.getString(resourceRequest, "folderName");
         String folderDescription = ParamUtil.getString(resourceRequest, "folderDescription");
         long parentFolderId = ParamUtil.getLong(resourceRequest, "parentFolderId");
         if (folderName == null || folderName.trim().isEmpty()) {
            responseJson.put("success", false);
            responseJson.put("error", "Folder name is required");
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         folderName = folderName.trim();
         if (parentFolderId == 0L) {
            responseJson.put("success", false);
            responseJson.put("error", "Create access denied.");
            this.writeResponse(resourceResponse, responseJson);
            return false;
         }

         ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFolder.class.getName(), resourceRequest);
         DLFolder folder = this._dlFolderService.addFolder((String)null, themeDisplay.getScopeGroupId(), themeDisplay.getScopeGroupId(), false, parentFolderId, folderName, folderDescription, serviceContext);
         responseJson.put("success", true);
         responseJson.put("message", "Folder created successfully");
         responseJson.put("folderId", folder.getFolderId());
         responseJson.put("folderName", folder.getName());
      } catch (Exception e) {
         _log.error("Error creating folder: " + e.getMessage(), e);
         responseJson.put("success", false);
         responseJson.put("error", "Failed to create folder: " + e.getMessage());
      }

      this.writeResponse(resourceResponse, responseJson);
      return true;
   }

   private void writeResponse(ResourceResponse resourceResponse, JSONObject responseJson) {
      try {
         resourceResponse.setContentType("application/json");
         resourceResponse.setCharacterEncoding("UTF-8");
         resourceResponse.getWriter().write(responseJson.toString());
      } catch (Exception e) {
         _log.error("Error writing response: " + e.getMessage(), e);
      }

   }
}
