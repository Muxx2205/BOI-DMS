package boi.headless.delivery.memorandum.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   configurationPid = {"boi.headless.delivery.memorandum.configuration.BOIConfiguration"},
   immediate = true,
   service = {ConfigurationUtil.class}
)
public class ConfigurationUtil {
   private static volatile BOIConfiguration boiConfiguration;
   private static volatile long defaultFolderId;
   private static volatile long memoObjectDefinitionId;
   private static volatile long memoWorkflowLogsObjectDefinitionId;
   private static volatile long memoUserMappingObjectDefinitionId;
   private static volatile long adminUserId;
   private Log log = LogFactoryUtil.getLog(ConfigurationUtil.class);

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      boiConfiguration = (BOIConfiguration)ConfigurableUtil.createConfigurable(BOIConfiguration.class, properties);
      defaultFolderId = boiConfiguration.DefaultFolderId();
      memoObjectDefinitionId = boiConfiguration.MEMORANDUMObjectDefinitionId();
      memoWorkflowLogsObjectDefinitionId = boiConfiguration.MEMOWorkflowLogsObjectDefinitionId();
      memoUserMappingObjectDefinitionId = boiConfiguration.MEMOUserMappingObjectDefinitionId();
      adminUserId = boiConfiguration.AdminUserId();
   }

   public static BOIConfiguration getConfiguration() {
      return boiConfiguration;
   }

   public static long getAdminUserId() {
      return adminUserId;
   }

   public static long getDefaultFolderId() {
      return defaultFolderId;
   }

   public static long getMemoObjectDefinitionId() {
      return memoObjectDefinitionId;
   }

   public static long getMemoWorkflowObjectDefinitionId() {
      return memoWorkflowLogsObjectDefinitionId;
   }

   public static long getMemoUserMappingObjectDefinitionId() {
      return memoUserMappingObjectDefinitionId;
   }
}
