package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=getFolderHistory"},
   service = {MVCResourceCommand.class}
)
public class GetFolderHistoryMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(GetFolderHistoryMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
      ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
      long folderId = ParamUtil.getLong(resourceRequest, "folderId");
      JSONObject result = JSONFactoryUtil.createJSONObject();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      try {
         DLFolder folder = DLFolderLocalServiceUtil.getDLFolder(folderId);
         JSONArray historyArray = JSONFactoryUtil.createJSONArray();
         JSONObject createEvent = JSONFactoryUtil.createJSONObject();
         createEvent.put("action", "Folder Created");
         createEvent.put("description", "Folder was created in the system");
         createEvent.put("timestamp", dateFormat.format(folder.getCreateDate()));
         User creator = UserLocalServiceUtil.getUser(folder.getUserId());
         createEvent.put("user", creator.getScreenName());
         createEvent.put("userId", folder.getUserId());
         historyArray.put(createEvent);
         if (!folder.getCreateDate().equals(folder.getModifiedDate())) {
            JSONObject modifyEvent = JSONFactoryUtil.createJSONObject();
            modifyEvent.put("action", "Folder Modified");
            modifyEvent.put("description", "Folder metadata was updated");
            modifyEvent.put("timestamp", dateFormat.format(folder.getModifiedDate()));
            User modifier = UserLocalServiceUtil.getUser(folder.getUserId());
            modifyEvent.put("user", modifier.getScreenName());
            modifyEvent.put("userId", folder.getUserId());
            historyArray.put(modifyEvent);
         }

         if (folder.getDescription() != null && !folder.getDescription().isEmpty()) {
            JSONObject descEvent = JSONFactoryUtil.createJSONObject();
            descEvent.put("action", "Description Updated");
            descEvent.put("description", "Folder description was set");
            descEvent.put("changes", folder.getDescription());
            descEvent.put("timestamp", dateFormat.format(folder.getModifiedDate()));
            User updater = UserLocalServiceUtil.getUser(folder.getUserId());
            descEvent.put("user", updater.getScreenName());
            historyArray.put(descEvent);
         }

         result.put("success", true);
         result.put("folderId", folderId);
         result.put("folderName", folder.getName());
         result.put("history", historyArray);
      } catch (Exception e) {
         _log.error("Error fetching folder history: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Failed to load folder history");
         result.put("history", JSONFactoryUtil.createJSONArray());
      }

      resourceResponse.setContentType("application/json");
      resourceResponse.setCharacterEncoding("UTF-8");
      PrintWriter writer = resourceResponse.getWriter();
      writer.write(result.toString());
   }
}
