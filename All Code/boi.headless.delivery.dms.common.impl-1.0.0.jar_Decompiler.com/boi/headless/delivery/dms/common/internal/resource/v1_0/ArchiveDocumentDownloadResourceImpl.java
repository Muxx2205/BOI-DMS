package boi.headless.delivery.dms.common.internal.resource.v1_0;

import boi.headless.delivery.dms.common.resource.v1_0.ArchiveDocumentDownloadResource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   properties = {"OSGI-INF/liferay/rest/v1_0/archive-document-download.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {ArchiveDocumentDownloadResource.class}
)
public class ArchiveDocumentDownloadResourceImpl extends BaseArchiveDocumentDownloadResourceImpl {
   public Response downloadArchiveFile(@NotNull Long fileEntryId) throws Exception {
      return super.downloadArchiveFile(fileEntryId);
   }
}
