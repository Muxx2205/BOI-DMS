package boi.headless.delivery.spa.internal.resource.v1_0;

import boi.headless.delivery.spa.resource.v1_0.FileDownloadResource;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   properties = {"OSGI-INF/liferay/rest/v1_0/file-download.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {FileDownloadResource.class}
)
public class FileDownloadResourceImpl extends BaseFileDownloadResourceImpl {
   public Response downloadFile(Long fileEntryId) throws Exception {
      try {
         FileEntry fileEntry = DLAppServiceUtil.getFileEntry(fileEntryId);
         FileVersion fileVersion = fileEntry.getFileVersion();
         InputStream inputStream = fileVersion.getContentStream(false);
         Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
         responseBuilder.header("Content-Disposition", "attachment; filename=\"" + fileEntry.getFileName() + "\"");
         responseBuilder.header("Content-Type", fileEntry.getMimeType());
         responseBuilder.header("Content-Length", fileEntry.getSize());
         return responseBuilder.build();
      } catch (Exception var6) {
         return Response.status(Status.NOT_FOUND).entity("File not found for id: " + fileEntryId).build();
      }
   }
}
