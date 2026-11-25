package boi.document.management.system.mvcResourceCommand;

import com.liferay.document.library.kernel.exception.NoSuchFileEntryException;
import com.liferay.document.library.kernel.exception.NoSuchFolderException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=boi_document_management_system_BoiDocumentManagementSystemPortlet", "mvc.command.name=getItemsForMultiDelete", "mvc.command.name=multiDeleteItems"},
   service = {MVCResourceCommand.class}
)
public class MultiDeleteMVCResourceCommand extends BaseMVCResourceCommand {
   private static final Log _log = LogFactoryUtil.getLog(MultiDeleteMVCResourceCommand.class);

   protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
      String resourceID = resourceRequest.getResourceID();
      if ("getItemsForMultiDelete".equals(resourceID)) {
         this.getItemsForMultiDelete(resourceRequest, resourceResponse);
      } else if ("multiDeleteItems".equals(resourceID)) {
         this.multiDeleteItems(resourceRequest, resourceResponse);
      } else {
         JSONObject result = JSONFactoryUtil.createJSONObject();
         result.put("success", false);
         result.put("error", "Unknown resource command");
         resourceResponse.getWriter().write(result.toString());
      }

   }

   private void getItemsForMultiDelete(ResourceRequest request, ResourceResponse response) throws IOException {
      JSONObject result = JSONFactoryUtil.createJSONObject();

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long groupId = themeDisplay.getScopeGroupId();
         long folderId = ParamUtil.getLong(request, "folderId", 0L);
         PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
         JSONArray items = JSONFactoryUtil.createJSONArray();
         if (!this.hasPermissionToDeleteFromFolder(folderId, themeDisplay)) {
            result.put("success", false);
            result.put("error", "You don't have permission to delete items from this folder");
            this.writeResponse(response, result);
            return;
         }

         try {
            for(DLFolder folder : DLFolderLocalServiceUtil.getFolders(groupId, folderId, -1, -1, (OrderByComparator)null)) {
               if (permissionChecker.hasPermission(groupId, DLFolder.class.getName(), folder.getFolderId(), "DELETE")) {
                  JSONObject folderJson = JSONFactoryUtil.createJSONObject();
                  folderJson.put("id", folder.getFolderId());
                  folderJson.put("name", folder.getName());
                  folderJson.put("type", "folder");
                  folderJson.put("description", folder.getDescription());
                  folderJson.put("createDate", this.formatDate(folder.getCreateDate()));
                  folderJson.put("modifiedDate", this.formatDate(folder.getModifiedDate()));
                  folderJson.put("userId", folder.getUserId());
                  folderJson.put("userName", folder.getUserName());
                  folderJson.put("size", 0);
                  items.put(folderJson);
               }
            }
         } catch (Exception e) {
            _log.error("Error getting folders: " + e.getMessage(), e);
         }

         try {
            for(DLFileEntry document : DLFileEntryLocalServiceUtil.getFileEntries(groupId, folderId, -1, -1, (OrderByComparator)null)) {
               if (permissionChecker.hasPermission(groupId, DLFileEntry.class.getName(), document.getFileEntryId(), "DELETE")) {
                  JSONObject docJson = JSONFactoryUtil.createJSONObject();
                  docJson.put("id", document.getFileEntryId());
                  docJson.put("name", document.getTitle());
                  docJson.put("type", "document");
                  docJson.put("size", document.getSize());
                  docJson.put("extension", document.getExtension());
                  docJson.put("mimeType", document.getMimeType());
                  docJson.put("createDate", this.formatDate(document.getCreateDate()));
                  docJson.put("modifiedDate", this.formatDate(document.getModifiedDate()));
                  docJson.put("userId", document.getUserId());
                  docJson.put("userName", document.getUserName());
                  docJson.put("version", document.getVersion());
                  items.put(docJson);
               }
            }
         } catch (Exception e) {
            _log.error("Error getting documents: " + e.getMessage(), e);
         }

         result.put("success", true);
         result.put("items", items);
         result.put("totalCount", items.length());
      } catch (Exception e) {
         _log.error("Error in getItemsForMultiDelete: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Error loading items: " + e.getMessage());
      }

      this.writeResponse(response, result);
   }

   private void multiDeleteItems(ResourceRequest request, ResourceResponse response) throws IOException {
      JSONObject result = JSONFactoryUtil.createJSONObject();

      try {
         ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
         long userId = themeDisplay.getUserId();
         String selectedItemsJson = ParamUtil.getString(request, "selectedItems");
         long folderId = ParamUtil.getLong(request, "folderId", 0L);
         PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
         ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFolder.class.getName(), request);

         JSONArray selectedItems;
         try {
            selectedItems = JSONFactoryUtil.createJSONArray(selectedItemsJson);
         } catch (Exception var30) {
            result.put("success", false);
            result.put("error", "Invalid items data format");
            this.writeResponse(response, result);
            return;
         }

         if (selectedItems.length() == 0) {
            result.put("success", false);
            result.put("error", "No items selected for deletion");
            this.writeResponse(response, result);
            return;
         }

         if (!this.hasPermissionToDeleteFromFolder(folderId, themeDisplay)) {
            result.put("success", false);
            result.put("error", "You don't have permission to delete items from this folder");
            this.writeResponse(response, result);
            return;
         }

         int deletedCount = 0;
         int failedCount = 0;
         JSONArray failedItems = JSONFactoryUtil.createJSONArray();
         JSONArray successItems = JSONFactoryUtil.createJSONArray();

         for(int i = 0; i < selectedItems.length(); ++i) {
            JSONObject item = selectedItems.getJSONObject(i);
            String type = item.getString("type");
            long id = item.getLong("id");
            String name = item.getString("name");

            try {
               boolean hasPermission = false;
               long groupId = themeDisplay.getScopeGroupId();
               if ("folder".equals(type)) {
                  hasPermission = permissionChecker.hasPermission(groupId, DLFolder.class.getName(), id, "DELETE");
               } else if ("document".equals(type)) {
                  hasPermission = permissionChecker.hasPermission(groupId, DLFileEntry.class.getName(), id, "DELETE");
               }

               if (!hasPermission) {
                  throw new Exception("Permission denied");
               }

               if (!"folder".equals(type)) {
                  if ("document".equals(type)) {
                     DLFileEntryLocalServiceUtil.deleteDLFileEntry(id);
                     JSONObject successItem = JSONFactoryUtil.createJSONObject();
                     successItem.put("id", id);
                     successItem.put("name", name);
                     successItem.put("type", "document");
                     successItems.put(successItem);
                     ++deletedCount;
                     _log.info("User " + userId + " deleted document: " + name + " (ID: " + id + ")");
                  }
               } else {
                  DLFolder folder = DLFolderLocalServiceUtil.getFolder(id);
                  int fileCount = DLFileEntryLocalServiceUtil.getFileEntriesCount(groupId, id);
                  int folderCount = DLFolderLocalServiceUtil.getFoldersCount(groupId, id);
                  if (fileCount > 0 || folderCount > 0) {
                     _log.info("Deleting folder with " + fileCount + " files and " + folderCount + " subfolders: " + name);
                  }

                  DLFolderLocalServiceUtil.deleteFolder(id);
                  JSONObject successItem = JSONFactoryUtil.createJSONObject();
                  successItem.put("id", id);
                  successItem.put("name", name);
                  successItem.put("type", "folder");
                  successItems.put(successItem);
                  ++deletedCount;
                  _log.info("User " + userId + " deleted folder: " + name + " (ID: " + id + ")");
               }
            } catch (NoSuchFileEntryException | NoSuchFolderException var31) {
               _log.warn("Item no longer exists: " + name + " (ID: " + id + ", Type: " + type + ")");
               ++deletedCount;
            } catch (Exception e) {
               ++failedCount;
               JSONObject failedItem = JSONFactoryUtil.createJSONObject();
               failedItem.put("id", id);
               failedItem.put("name", name);
               failedItem.put("type", type);
               failedItem.put("error", e.getMessage());
               failedItems.put(failedItem);
               _log.error("Error deleting item: " + name + " (ID: " + id + ", Type: " + type + ") - " + e.getMessage(), e);
            }
         }

         if (failedCount == 0) {
            result.put("success", true);
            result.put("message", "Successfully deleted " + deletedCount + " item" + (deletedCount != 1 ? "s" : ""));
            result.put("deletedCount", deletedCount);
            result.put("successItems", successItems);
         } else if (deletedCount > 0) {
            result.put("success", true);
            result.put("message", "Partially completed. Deleted " + deletedCount + " item" + (deletedCount != 1 ? "s" : "") + ", failed to delete " + failedCount + " item" + (failedCount != 1 ? "s" : ""));
            result.put("deletedCount", deletedCount);
            result.put("failedCount", failedCount);
            result.put("successItems", successItems);
            result.put("failedItems", failedItems);
         } else {
            result.put("success", false);
            result.put("error", "Failed to delete all selected items");
            result.put("failedCount", failedCount);
            result.put("failedItems", failedItems);
         }
      } catch (Exception e) {
         _log.error("Error in multiDeleteItems: " + e.getMessage(), e);
         result.put("success", false);
         result.put("error", "Error processing deletion: " + e.getMessage());
      }

      this.writeResponse(response, result);
   }

   private boolean hasPermissionToDeleteFromFolder(long folderId, ThemeDisplay themeDisplay) {
      try {
         PermissionChecker permissionChecker = themeDisplay.getPermissionChecker();
         long groupId = themeDisplay.getScopeGroupId();
         return folderId == 0L ? permissionChecker.hasPermission(groupId, DLFolder.class.getName(), groupId, "ADD_DOCUMENT") : permissionChecker.hasPermission(groupId, DLFolder.class.getName(), folderId, "DELETE");
      } catch (Exception e) {
         _log.error("Error checking folder permission: " + e.getMessage(), e);
         return false;
      }
   }

   private String formatDate(Date date) {
      if (date == null) {
         return "";
      } else {
         try {
            return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(date);
         } catch (Exception var3) {
            return date.toString();
         }
      }
   }

   private void writeResponse(ResourceResponse response, JSONObject result) throws IOException {
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(result.toString());
   }
}
