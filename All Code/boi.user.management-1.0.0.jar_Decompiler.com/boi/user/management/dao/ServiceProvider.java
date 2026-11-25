package boi.user.management.dao;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   service = {ServiceProvider.class}
)
public class ServiceProvider {
   private static final Log _log = LogFactoryUtil.getLog(ServiceProvider.class);
   private static DLAppService _dlAppService;

   @Reference
   public void setDLAppService(DLAppService dlAppService) {
      _dlAppService = dlAppService;
      _log.info("DLAppService injected in ServiceProvider");
   }

   public static DLAppService getDLAppService() {
      if (_dlAppService == null) {
         _log.error("DLAppService is null! Service not available.");
         throw new IllegalStateException("DLAppService is not available");
      } else {
         return _dlAppService;
      }
   }
}
