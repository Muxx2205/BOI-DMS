package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=getDocumentDetails"},
   service = {MVCResourceCommand.class}
)
public class GetDocumentDetailsMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(GetDocumentDetailsMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      long documentId = ParamUtil.getLong(resourceRequest, "documentId");
      JSONObject result = JSONFactoryUtil.createJSONObject();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      try {
         FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(documentId);
         JSONObject documentDetails = JSONFactoryUtil.createJSONObject();
         documentDetails.put("id", fileEntry.getFileEntryId());
         documentDetails.put("name", fileEntry.getTitle());
         documentDetails.put("extension", fileEntry.getExtension());
         documentDetails.put("size", fileEntry.getSize());
         documentDetails.put("mimeType", fileEntry.getMimeType());
         documentDetails.put("createDate", dateFormat.format(fileEntry.getCreateDate()));
         documentDetails.put("modifiedDate", dateFormat.format(fileEntry.getModifiedDate()));
         User creator = UserLocalServiceUtil.getUser(fileEntry.getUserId());
         documentDetails.put("createdBy", creator.getScreenName());
         documentDetails.put("createdById", fileEntry.getUserId());
         String description = fileEntry.getDescription();
         if (description != null && !description.isEmpty()) {
            documentDetails.put("description", description);
         }

         documentDetails.put("version", fileEntry.getVersion());
         result.put("success", true);
         result.put("document", documentDetails);
      } catch (Exception e) {
         _log.error("Error fetching document details: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Failed to load document details");
      }

      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }
}
