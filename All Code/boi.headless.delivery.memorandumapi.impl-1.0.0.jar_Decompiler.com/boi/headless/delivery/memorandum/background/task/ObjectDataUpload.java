package boi.headless.delivery.memorandum.background.task;

import boi.headless.delivery.memorandum.configuration.PSRSConfiguration;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
   immediate = true,
   configurationPid = {"boi.headless.delivery.memorandum.configuration.PSRSConfiguration"},
   property = {"background.task.executor.class.name=boi.headless.delivery.memorandum.background.task.ObjectDataUpload"},
   service = {BackgroundTaskExecutor.class}
)
public class ObjectDataUpload extends BaseBackgroundTaskExecutor {
   private volatile PSRSConfiguration psrsConfiguration;
   private static final DateTimeFormatter INPUT_FORMAT = (new DateTimeFormatterBuilder()).appendOptional(DateTimeFormatter.ofPattern("d/M/yyyy")).appendOptional(DateTimeFormatter.ofPattern("d-M-yyyy")).toFormatter();
   private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   private static final Log log = LogFactoryUtil.getLog(ObjectDataUpload.class);
   String reportType = null;

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.psrsConfiguration = (PSRSConfiguration)ConfigurableUtil.createConfigurable(PSRSConfiguration.class, properties);
   }

   public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {
      List<Map<String, Serializable>> successList = new ArrayList();
      List<Map<String, Serializable>> failedList = new ArrayList();
      Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();
      if (!Validator.isNull(taskContextMap) && !taskContextMap.isEmpty()) {
         Object dataObj = taskContextMap.get("data");
         if (!(dataObj instanceof List)) {
            log.error("Invalid data format. Expected List");
            return BackgroundTaskResult.SUCCESS;
         } else {
            List<Map<String, Serializable>> dataList = (List)dataObj;
            long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"));
            long userId = GetterUtil.getLong(taskContextMap.get("userId"));
            this.reportType = GetterUtil.getString(taskContextMap.get("reportType"));
            User user = UserLocalServiceUtil.fetchUser(userId);
            if (user == null) {
               log.error("User not found for userId: " + userId);
               return BackgroundTaskResult.SUCCESS;
            } else {
               log.info("Background task started. Total records: " + dataList.size());
               int successCount = 0;
               int failureCount = 0;
               List<String> dateColumns = this.getDateColumns();
               log.info("Date Column " + dateColumns);
               int row = 1;

               for(Map<String, Serializable> record : dataList) {
                  try {
                     Map<String, Serializable> normalized = this.normalizeKeysSerializable(record, dateColumns);
                     ObjectEntry updateData = this.updateData(user, normalized, objectDefinitionId);
                     if (Validator.isNotNull(updateData)) {
                        Map<String, Serializable> success = new HashMap();
                        success.put("Entry Id", updateData.getObjectEntryId());
                        success.put("Row", row);
                        success.put("Status", "SUCCESS");
                        successList.add(success);
                        ++successCount;
                     } else {
                        Map<String, Serializable> fail = new HashMap();
                        fail.put("Reason", "Internal Error");
                        fail.put("Row", row);
                        fail.put("Status", "FAILED");
                        failedList.add(fail);
                        ++failureCount;
                     }
                  } catch (Exception e) {
                     ++failureCount;
                     log.error("record: " + record);
                     log.error("Error processing : " + e.getMessage());
                     Map<String, Serializable> fail = new HashMap();
                     fail.put("Reason", e.getMessage());
                     fail.put("Row", row);
                     fail.put("Status", "FAILED");
                     failedList.add(fail);
                  }

                  ++row;
               }

               log.info("Task completed. Success: " + successCount + ", Failed: " + failureCount);
               return BackgroundTaskResult.SUCCESS;
            }
         }
      } else {
         log.error("Task context map is null or empty");
         return BackgroundTaskResult.SUCCESS;
      }
   }

   private Map<String, Serializable> normalizeKeysSerializable(Map<String, Serializable> map, List<String> dateColumns) {
      Map<String, Serializable> normalized = new HashMap();

      for(Map.Entry<String, Serializable> entry : map.entrySet()) {
         String key = ((String)entry.getKey()).trim();
         Serializable value = (Serializable)entry.getValue();
         if (dateColumns.contains(key)) {
            Serializable normalizeDateValue = this.normalizeDateValue(value);
            log.info(key + "-" + normalizeDateValue);
            normalized.put(key, normalizeDateValue);
         } else if (value != null) {
            String strValue = String.valueOf(value).trim();
            if (strValue.matches("^\\d+(\\.\\d+)?$")) {
               if (strValue.contains(".")) {
                  int decimalIndex = strValue.indexOf(".");
                  String decimalPart = strValue.substring(decimalIndex + 1);
                  if (decimalPart.length() > 2) {
                     strValue = String.format("%.2f", Double.parseDouble(strValue));
                  }
               }

               normalized.put(key, strValue);
            } else {
               normalized.put(key, value);
            }
         } else {
            normalized.put(key, value);
         }
      }

      return normalized;
   }

   private Serializable normalizeDateValue(Serializable value) {
      if (!(value instanceof String)) {
         return value;
      } else {
         String str = ((String)value).trim();
         if (str.isEmpty()) {
            return value;
         } else {
            try {
               LocalDate date = LocalDate.parse(str, INPUT_FORMAT);
               LocalDateTime dateTime = date.atTime(LocalTime.now());
               return dateTime.format(OUTPUT_FORMAT);
            } catch (Exception var5) {
               return value;
            }
         }
      }
   }

   public ObjectEntry updateData(User user, Map<String, Serializable> values, long objectDefinitionId) throws Exception {
      ServiceContext serviceContext = new ServiceContext();
      serviceContext.setCompanyId(user.getCompanyId());
      serviceContext.setUserId(user.getUserId());
      serviceContext.setWorkflowAction(1);
      List<ObjectField> fields = ObjectFieldLocalServiceUtil.getObjectFields(objectDefinitionId);
      boolean collSTExists = fields.stream().anyMatch((f) -> "collST".equalsIgnoreCase(f.getName()));
      if (collSTExists) {
         values.put("collST", "-2");
      }

      ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, user.getUserId(), 0L, objectDefinitionId, values, serviceContext);
      log.info("ObjectEntry updated successfully for ObjectDefinitionId: " + objectDefinitionId);
      return objectEntry;
   }

   public List<String> getDateColumns() {
      String psrsDateColumns = this.psrsConfiguration.PSRSDateColumns();
      if (psrsDateColumns != null && !psrsDateColumns.trim().isEmpty()) {
         Map<String, List<String>> map = (Map)Arrays.stream(psrsDateColumns.split("\\s*;\\s*")).map((s) -> s.split("\\s*=\\s*")).filter((p) -> p.length == 2).collect(Collectors.toMap((p) -> p[0].toLowerCase(), (p) -> Arrays.asList(p[1].split("\\s*,\\s*"))));
         return (List)map.getOrDefault(this.reportType, (Object)null);
      } else {
         return null;
      }
   }

   public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
      return null;
   }

   public BackgroundTaskExecutor clone() {
      return this;
   }
}
