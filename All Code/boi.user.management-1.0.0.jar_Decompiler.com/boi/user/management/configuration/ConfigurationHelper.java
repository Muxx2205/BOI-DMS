package boi.user.management.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   configurationPid = {"boi.user.management.configuration.RoleMappingConfiguration"},
   immediate = true,
   service = {ConfigurationHelper.class}
)
public class ConfigurationHelper {
   private static volatile RoleMappingConfiguration configuration;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      configuration = (RoleMappingConfiguration)ConfigurableUtil.createConfigurable(RoleMappingConfiguration.class, properties);
   }

   public static Map<String, List<String>> getRoleMappings1() {
      Map<String, List<String>> roleMap = new HashMap();
      if (configuration != null && configuration.roleMappings() != null) {
         for(String mapping : configuration.roleMappings()) {
            String[] parts = mapping.split("=");
            if (parts.length == 2) {
               String source = parts[0].trim();
               String[] targets = parts[1].split(",");
               List<String> targetList = new ArrayList();

               for(String t : targets) {
                  targetList.add(t.trim());
               }

               roleMap.put(source, targetList);
            }
         }
      }

      return roleMap;
   }

   public static Map<String, String> getRoleMappings() {
      Map<String, String> roleMap = new HashMap();
      if (configuration != null && configuration.roleMappings() != null) {
         for(String mapping : configuration.roleMappings()) {
            String[] parts = mapping.split("=");
            if (parts.length == 2) {
               String source = parts[0].trim();
               String targets = parts[1].trim();
               roleMap.put(source, targets);
            }
         }
      }

      return roleMap;
   }

   public static Map<String, String> getDepartmentMap() {
      Map<String, String> result = new HashMap();
      String input = configuration.departmentMappings();
      if (input != null && !input.isEmpty()) {
         String[] entries = input.split(",");

         for(String entry : entries) {
            entry = entry.trim();
            if (entry.startsWith("\"") && entry.contains("=")) {
               String[] parts = entry.split("=");
               if (parts.length == 2) {
                  String key = parts[0].replaceAll("^\"|\"$", "").trim();
                  String value = parts[1].replaceAll("^\"|\"$", "").trim();
                  result.put(key, value);
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   public static long getObjectDefinitionId() {
      return configuration.organizationId();
   }

   public static long getfolderIdForDMS() {
      return configuration.folderId();
   }
}
