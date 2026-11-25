package com.file.entry.path.portlet;

import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.ParamUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=FileEntryPath", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_file_entry_path_FileEntryPathPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class FileEntryPathPortlet extends MVCPortlet {
   private static final Log _log = LogFactoryUtil.getLog(FileEntryPathPortlet.class);
   private static final List<String> consoleLogs = Collections.synchronizedList(new ArrayList());
   private static final List<Long> fileEntryIds = new ArrayList();

   public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
      long objectDefinitionId = ParamUtil.getLong(renderRequest, "objectDefinitionId");
      long fileEntryId = ParamUtil.getLong(renderRequest, "fileEntryId");
      String actionType = ParamUtil.getString(renderRequest, "actionType");
      List<String> paths = new ArrayList();

      try {
         if ("object".equals(actionType) && objectDefinitionId > 0L) {
            paths = generatePaths(objectDefinitionId);
         } else if ("stream".equals(actionType) && fileEntryId > 0L) {
            checkInputStreamSize(fileEntryId);
         } else if ("reupload".equals(actionType) && fileEntryId > 0L) {
            reUploadExistingContent(fileEntryId);
         } else if ("clearLogs".equals(actionType)) {
            consoleLogs.clear();
         }
      } catch (Exception e) {
         addLog("Error In doView : " + e.getMessage());
      }

      renderRequest.setAttribute("paths", paths);
      renderRequest.setAttribute("consoleLogs", consoleLogs);
      super.doView(renderRequest, renderResponse);
   }

   public static List<String> generatePaths(long objectDefinitionId) {
      List<String> pathList = new ArrayList();

      try {
         List<ObjectEntry> objectEntries = ObjectEntryLocalServiceUtil.getObjectEntries(0L, objectDefinitionId, -1, -1);
         addLog("Total Object Entries : " + objectEntries.size());

         for(ObjectEntry objectEntry : objectEntries) {
            try {
               Object migrateDataObj = objectEntry.getValues().get("migrateDataKey");
               if (migrateDataObj != null) {
                  String migrateDataKey = String.valueOf(migrateDataObj);
                  if (!migrateDataKey.trim().isEmpty() && !"null".equals(migrateDataKey)) {
                     JSONArray jsonArray = JSONFactoryUtil.createJSONArray(migrateDataKey);

                     for(int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        long fid = jsonObject.getLong("fid");
                        String fname = jsonObject.getString("fname");
                        if (fid > 0L) {
                           reUploadExistingContent(fid);
                           addLog("ObjectEntryId : " + objectEntry.getObjectEntryId());
                           addLog("Fid : " + fid);
                           addLog("Original File Name : " + fname);
                        }
                     }
                  }
               }
            } catch (Exception e) {
               long var10000 = objectEntry.getObjectEntryId();
               addLog("Error Processing ObjectEntryId : " + var10000 + " | " + e.getMessage());
            }
         }
      } catch (Exception e) {
         addLog("Error Fetching Object Entries : " + e.getMessage());
      }

      return pathList;
   }

   public static void checkInputStreamSize(long fileEntryId) {
      try {
         FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(fileEntryId);
         InputStream inputStream = fileEntry.getContentStream();

         try {
            byte[] bytes = inputStream.readAllBytes();
            addLog("FileEntryId : " + fileEntryId + " | Stream Size : " + bytes.length + " bytes");
            addLog("FileEntryId : " + fileEntryId + " | Stream Size MB : " + (double)bytes.length / (double)1048576.0F);
         } catch (Throwable var7) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (inputStream != null) {
            inputStream.close();
         }
      } catch (Exception e) {
         addLog("Error Checking Stream Size : " + e.getMessage());
      }

   }

   public static void reUploadExistingContent(long fileEntryId) {
      try {
         addLog("FileEntryId : " + fileEntryId);
         FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(fileEntryId);
         InputStream inputStream = fileEntry.getContentStream();

         byte[] bytes;
         try {
            bytes = inputStream.readAllBytes();
         } catch (Throwable var8) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         if (bytes == null || bytes.length <= 0) {
            addLog("Empty Content For FileEntryId : " + fileEntryId);
            return;
         }

         updateFileEntry(fileEntry, bytes);
      } catch (Exception e) {
         addLog("Error ReUploading FileEntry : " + e.getMessage());
      }

   }

   private static void updateFileEntry(FileEntry fileEntry, byte[] bytes) throws Exception {
      ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
      if (serviceContext == null) {
         serviceContext = new ServiceContext();
      }

      InputStream inputStream = new ByteArrayInputStream(bytes);
      FileEntry updatedFileEntry = DLAppLocalServiceUtil.updateFileEntry(fileEntry.getUserId(), fileEntry.getFileEntryId(), fileEntry.getFileName(), fileEntry.getMimeType(), fileEntry.getTitle(), fileEntry.getTitle(), fileEntry.getDescription(), "ReUploaded Content", DLVersionNumberIncrease.MINOR, inputStream, (long)bytes.length, (Date)null, (Date)null, (Date)null, serviceContext);
      addLog("Successfully Updated FileEntryId : " + updatedFileEntry.getFileEntryId());
      inputStream.close();
   }

   public static byte[] convertFileToBytes(String filePath) throws IOException {
      Path path = Paths.get(filePath);
      return Files.readAllBytes(path);
   }

   private static void addLog(String message) {
      consoleLogs.add(message);
      _log.info(message);
   }
}
