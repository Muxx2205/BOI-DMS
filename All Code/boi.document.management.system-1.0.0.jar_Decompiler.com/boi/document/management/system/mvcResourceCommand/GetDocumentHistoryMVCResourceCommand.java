package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileVersionLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=getDocumentHistory"},
   service = {MVCResourceCommand.class}
)
public class GetDocumentHistoryMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(GetDocumentHistoryMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long documentId = ParamUtil.getLong(resourceRequest, "documentId");
      JSONObject result = JSONFactoryUtil.createJSONObject();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      try {
         FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(documentId);
         DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getDLFileEntry(documentId);
         List<DLFileVersion> versions = DLFileVersionLocalServiceUtil.getFileVersions(dlFileEntry.getFileEntryId(), 0, -1, -1);
         JSONArray historyArray = JSONFactoryUtil.createJSONArray();
         JSONObject createEvent = JSONFactoryUtil.createJSONObject();
         createEvent.put("action", "Document Created");
         createEvent.put("description", "Document was uploaded to the system");
         createEvent.put("timestamp", dateFormat.format(fileEntry.getCreateDate()));
         User creator = UserLocalServiceUtil.getUser(fileEntry.getUserId());
         createEvent.put("user", creator.getScreenName());
         createEvent.put("userId", fileEntry.getUserId());
         historyArray.put(createEvent);
         if (versions != null && !versions.isEmpty()) {
            for(DLFileVersion version : versions) {
               JSONObject versionEvent = JSONFactoryUtil.createJSONObject();
               if (version.getVersion().equals("1.0")) {
                  versionEvent.put("action", "Version 1.0");
                  versionEvent.put("description", "Initial version created");
               } else {
                  versionEvent.put("action", "Version " + version.getVersion());
                  versionEvent.put("description", "Document was updated to version " + version.getVersion());
               }

               versionEvent.put("timestamp", dateFormat.format(version.getCreateDate()));
               versionEvent.put("version", version.getVersion());
               User versionUser = UserLocalServiceUtil.getUser(version.getUserId());
               versionEvent.put("user", versionUser.getScreenName());
               versionEvent.put("userId", version.getUserId());
               versionEvent.put("size", version.getSize());
               String var10002 = this.formatFileSize(version.getSize());
               versionEvent.put("changes", "File size: " + var10002);
               if (version.getMimeType() != null) {
                  versionEvent.put("mimeType", version.getMimeType());
               }

               historyArray.put(versionEvent);
            }
         }

         if (!fileEntry.getCreateDate().equals(fileEntry.getModifiedDate())) {
            JSONObject modifyEvent = JSONFactoryUtil.createJSONObject();
            modifyEvent.put("action", "Document Modified");
            modifyEvent.put("description", "Document metadata was updated");
            modifyEvent.put("timestamp", dateFormat.format(fileEntry.getModifiedDate()));
            User modifier = UserLocalServiceUtil.getUser(fileEntry.getUserId());
            modifyEvent.put("user", modifier.getScreenName());
            modifyEvent.put("userId", fileEntry.getUserId());
            StringBuilder changes = new StringBuilder();
            changes.append("Title: ").append(fileEntry.getTitle());
            if (fileEntry.getDescription() != null && !fileEntry.getDescription().isEmpty()) {
               changes.append(", Description updated");
            }

            modifyEvent.put("changes", changes.toString());
            historyArray.put(modifyEvent);
         }

         try {
            JSONObject downloadEvent = JSONFactoryUtil.createJSONObject();
            downloadEvent.put("action", "Document Viewed/Downloaded");
            downloadEvent.put("description", "Document was accessed by user");
            downloadEvent.put("timestamp", dateFormat.format(new Date()));
            downloadEvent.put("user", themeDisplay.getUser().getScreenName());
            downloadEvent.put("changes", "Accessed from DMS portal");
            historyArray.put(downloadEvent);
         } catch (Exception var18) {
         }

         result.put("success", true);
         result.put("documentId", documentId);
         result.put("documentName", fileEntry.getTitle());
         result.put("currentVersion", fileEntry.getVersion());
         result.put("totalVersions", versions != null ? versions.size() : 1);
         result.put("history", historyArray);
      } catch (Exception e) {
         _log.error("Error fetching document history for documentId " + documentId + ": " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Failed to load document history: " + e.getMessage());
         result.put("history", JSONFactoryUtil.createJSONArray());
      }

      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }

   private String formatFileSize(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else if (bytes < 1048576L) {
         return String.format("%.1f KB", (double)bytes / (double)1024.0F);
      } else {
         return bytes < 1073741824L ? String.format("%.1f MB", (double)bytes / (double)1048576.0F) : String.format("%.1f GB", (double)bytes / (double)1.0737418E9F);
      }
   }
}
