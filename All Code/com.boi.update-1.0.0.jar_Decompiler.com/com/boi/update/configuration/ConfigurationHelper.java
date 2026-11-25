package com.boi.update.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   configurationPid = {"com.boi.update.configuration.FolderIDUpdateConfiguration"},
   immediate = true,
   service = {ConfigurationHelper.class}
)
public class ConfigurationHelper {
   private static volatile FolderIDUpdateConfiguration configuration;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      configuration = (FolderIDUpdateConfiguration)ConfigurableUtil.createConfigurable(FolderIDUpdateConfiguration.class, properties);
   }

   public static String getDepartmentName() {
      return configuration.departmentName();
   }
}
