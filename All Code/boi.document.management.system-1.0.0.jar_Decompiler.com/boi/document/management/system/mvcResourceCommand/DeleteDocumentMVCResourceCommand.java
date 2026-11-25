package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=deleteDocument"},
   service = {MVCResourceCommand.class}
)
public class DeleteDocumentMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(DeleteDocumentMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long documentId = ParamUtil.getLong(resourceRequest, "documentId");
      _log.info(documentId);
      JSONObject result = JSONFactoryUtil.createJSONObject();

      try {
         FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(documentId);
         String fileName = fileEntry.getTitle();
         DLAppLocalServiceUtil.deleteFileEntry(documentId);
         result.put("success", true);
         result.put("message", "Document deleted successfully");
         result.put("documentId", documentId);
         result.put("documentName", fileName);
         _log.info("Document deleted: " + fileName + " (ID: " + documentId + ") by user: " + themeDisplay.getUser().getScreenName());
      } catch (PortalException e) {
         _log.error("Error deleting document: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Failed to delete document: " + e.getMessage());
      } catch (Exception e) {
         _log.error("Unexpected error deleting document: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Unexpected error occurred");
      }

      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }
}
