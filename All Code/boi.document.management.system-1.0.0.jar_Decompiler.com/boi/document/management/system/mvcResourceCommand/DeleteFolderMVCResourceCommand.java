package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=deleteFolder"},
   service = {MVCResourceCommand.class}
)
public class DeleteFolderMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(DeleteFolderMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long folderId = ParamUtil.getLong(resourceRequest, "folderId");
      _log.info(folderId);
      JSONObject result = JSONFactoryUtil.createJSONObject();

      try {
         DLFolder folder = DLFolderLocalServiceUtil.getDLFolder(folderId);
         DLFolderLocalServiceUtil.deleteFolder(folderId);
         result.put("success", true);
         result.put("message", "Folder deleted successfully");
         result.put("folderId", folderId);
         result.put("folderName", folder.getName());
         _log.info("Folder deleted: " + folder.getName() + " (ID: " + folderId + ") by user: " + themeDisplay.getUser().getScreenName());
      } catch (PortalException e) {
         _log.error("Error deleting folder: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Failed to delete folder: " + e.getMessage());
      } catch (Exception e) {
         _log.error("Unexpected error deleting folder: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Unexpected error occurred");
      }

      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }
}
