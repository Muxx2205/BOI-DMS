package boi.document.management.system.dto;

import boi.document.management.system.configuration.DMSConfiguration;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import java.util.List;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

public class DepartmentFileLimitsDTO {
   private String departmentName;
   private long maxFiles;
   private long maxFileSizeBytes;
   private long maxTotalSizeBytes;
   private static Log _log = LogFactoryUtil.getLog(DepartmentFileLimitsDTO.class.getName());

   private DepartmentFileLimitsDTO(Builder builder) {
      this.departmentName = builder.departmentName;
      this.maxFiles = builder.maxFiles;
      this.maxFileSizeBytes = builder.maxFileSizeBytes;
      this.maxTotalSizeBytes = builder.maxTotalSizeBytes;
   }

   private String getDepartmentName() {
      return this.departmentName;
   }

   private long getMaxFiles() {
      return this.maxFiles;
   }

   private long getMaxFileSizeBytes() {
      return this.maxFileSizeBytes;
   }

   private long getMaxTotalSizeBytes() {
      return this.maxTotalSizeBytes;
   }

   public String toString() {
      return String.format("%s => Max Files: %d, Max File Size: %d bytes, Max Total Size: %d bytes", this.departmentName, this.maxFiles, this.maxFileSizeBytes, this.maxTotalSizeBytes);
   }

   public static void initializeForUser(RenderRequest renderRequest, List<String> departmentFilter, boolean isHavingAddUpdateAction, DMSConfiguration dmsConfiguration) {
      if (isHavingAddUpdateAction) {
         try {
            PortletSession session = renderRequest.getPortletSession();
            Long maxFiles = getSessionLong(session, "MAX_FILES");
            Long maxFileSize = getSessionLong(session, "MAX_FILE_SIZE");
            Long maxTotalSize = getSessionLong(session, "MAX_TOTAL_SIZE");
            _log.info("Max Files " + maxFiles);
            _log.info("maxFileSize " + maxFileSize);
            _log.info("maxTotalSize " + maxTotalSize);
            if (isNullOrZero(maxFiles) || isNullOrZero(maxFileSize) || isNullOrZero(maxTotalSize)) {
               DepartmentFileLimitsDTO dto = parseConfig(dmsConfiguration.filterFileFolderByDepartment(), departmentFilter);
               _log.info(dto.toString());
               if (dto != null) {
                  maxFiles = dto.getMaxFiles();
                  maxFileSize = dto.getMaxFileSizeBytes();
                  maxTotalSize = dto.getMaxTotalSizeBytes();
               } else {
                  maxFiles = dmsConfiguration.maxFiles();
                  maxFileSize = dmsConfiguration.maxFileSize();
                  maxTotalSize = 100L;
                  (new Builder()).departmentName("Default").maxFiles(maxFiles).maxFileSizeBytes(maxFileSize).maxTotalSizeBytes(maxTotalSize).build();
               }

               session.setAttribute("MAX_FILES", maxFiles, 2);
               session.setAttribute("MAX_FILE_SIZE", maxFileSize, 2);
               session.setAttribute("MAX_TOTAL_SIZE", maxTotalSize, 2);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }

      }
   }

   private static boolean isNullOrZero(Long value) {
      return value == null || value == 0L;
   }

   private static Long getSessionLong(PortletSession session, String key) {
      Object val = session.getAttribute(key, 2);
      return val instanceof Long ? (Long)val : null;
   }

   private static DepartmentFileLimitsDTO parseConfig(String configString, List<String> departmentFileFilter) {
      if (!Validator.isBlank(configString) && !ListUtil.isEmpty(departmentFileFilter)) {
         String[] segments = configString.split("\\s*\\|\\s*");

         for(String segment : segments) {
            segment = segment.trim();
            if (!segment.isEmpty()) {
               String[] parts = segment.split(":", 2);
               if (parts.length >= 2) {
                  String[] deptNames = parts[0].split(",");
                  String limits = parts[1].trim();
                  long maxFiles = 0L;
                  long maxFileSizeBytes = 0L;
                  long maxTotalSizeBytes = 0L;

                  for(String limitPart : limits.split(",")) {
                     limitPart = limitPart.trim();
                     if (limitPart.startsWith("Max Files")) {
                        maxFiles = GetterUtil.getLong(limitPart.replaceAll("\\D+", ""));
                     } else if (limitPart.startsWith("Max File Size")) {
                        maxFileSizeBytes = GetterUtil.getLong(limitPart.replaceAll("\\D+", ""));
                     } else if (limitPart.startsWith("Max Total Size")) {
                        maxTotalSizeBytes = GetterUtil.getLong(limitPart.replaceAll("\\D+", ""));
                     }
                  }

                  for(String deptName : deptNames) {
                     String trimmedDept = deptName.trim();
                     if (departmentFileFilter.stream().anyMatch((d) -> d.equalsIgnoreCase(trimmedDept))) {
                        return (new Builder()).departmentName(trimmedDept).maxFiles(maxFiles).maxFileSizeBytes(maxFileSizeBytes).maxTotalSizeBytes(maxTotalSizeBytes).build();
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static class Builder {
      private String departmentName;
      private long maxFiles;
      private long maxFileSizeBytes;
      private long maxTotalSizeBytes;

      public Builder departmentName(String departmentName) {
         this.departmentName = departmentName;
         return this;
      }

      public Builder maxFiles(long maxFiles) {
         this.maxFiles = maxFiles;
         return this;
      }

      public Builder maxFileSizeBytes(long maxFileSizeBytes) {
         this.maxFileSizeBytes = maxFileSizeBytes;
         return this;
      }

      public Builder maxTotalSizeBytes(long maxTotalSizeBytes) {
         this.maxTotalSizeBytes = maxTotalSizeBytes;
         return this;
      }

      public DepartmentFileLimitsDTO build() {
         return new DepartmentFileLimitsDTO(this);
      }
   }
}
