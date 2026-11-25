package boi.headless.delivery.spa.common;

import com.boi.headless.rest.common.spa.application.BOIDMSConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestValidator {
   private static final Log _log = LogFactoryUtil.getLog(RequestValidator.class);
   private final MultipartBody _multipartBody;
   private final String _className;
   private static final List<String> _allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "tif", "pdf", "docs", "docx", "xls", "xlsx", "csv", "gif", "txt");
   private static final List<String> _allowedEPLTFRMExtensions = Arrays.asList("jpg", "jpeg", "png", "tif", "pdf", "docs", "docx", "xls", "xlsx", "csv", "gif", "txt", "mp4");
   private static final long MAX_FILE_SIZE_BYTES = 10485760L;
   private static final long MAX_BLM_FILE_SIZE_BYTES = 26214400L;
   private static final long MAX_INTERNATIONAL_DEP_FILE_SIZE_BYTES = 26214400L;
   private static final long MAX_EPLTFRM_FILE_SIZE_BYTES = 62914560L;
   private static final long MAX_BRANCH_DIGITIATION_FILE_SIZE_BYTES = 104857600L;
   private static final long MAX_INSPECTION_AUDIT_FILE_SIZE_BYTES = 26214400L;
   private static final Set<String> INSPECTION_AUDIT_CLASSES = Set.of("INSPECTION_AUDIT_ACB", "INSPECTION_AUDIT_ACE", "INSPECTION_AUDIT_OTHER_REPORT", "INSPECTION_AUDIT_CA_APPOINTMENTS", "INSPECTION_FOREIGN_AUDIT", "INSPECTION_AUDIT_REPORTS", "INSPECTION_AUDIT_MISC");

   public RequestValidator(MultipartBody multipartBody, String className) {
      this._multipartBody = multipartBody;
      this._className = className;
   }

   public void validateRequestParams() throws Exception {
   }

   public void validateRequestFiles() throws Exception {
      Map<String, String> fileUploadMap = BOIDMSConstants.getObjectFileNames(this._className);
      _log.info("File Validator Matrix loaded for class: " + this._className);

      for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
         String fieldKey = (String)entry.getKey();
         BinaryFile binaryFile = this._multipartBody.getBinaryFile(fieldKey);
         if (!Validator.isNull(binaryFile) && !Validator.isBlank(binaryFile.getFileName())) {
            String fileName = binaryFile.getFileName();
            _log.info("Received File Name: " + fileName);
            if (Validator.isNull(fileName) || fileName.trim().isEmpty()) {
               throw new Exception("File name is required.");
            }

            if (!fileName.matches("^[a-zA-Z0-9_-][a-zA-Z0-9_\\-\\s]*(\\.[a-zA-Z0-9]+)?$")) {
               throw new Exception("File name cannot start with space and can only contain letters, numbers, _, -, spaces and a valid extension.");
            }

            if (!fileName.matches("^[a-zA-Z0-9_-][a-zA-Z0-9_\\-\\s]*\\.[a-zA-Z0-9]+$")) {
               throw new Exception("File name must contain only one '.' and valid characters.");
            }

            long fileSize = binaryFile.getSize();
            _log.info("File Size (bytes): " + fileSize);
            if (this._className.equalsIgnoreCase("BOARD_LEVEL_MEETINGS")) {
               if (fileSize > 26214400L) {
                  throw new Exception("File " + fileName + " exceeds the maximum allowed size of 25 MB.");
               }
            } else if (this._className.equalsIgnoreCase("E_PLATEFORM")) {
               if (fileSize > 62914560L) {
                  throw new Exception("File " + fileName + " exceeds the maximum allowed size of 20 MB.");
               }
            } else if (this._className.equalsIgnoreCase("INTERNATIONAL_DEPARTMENT")) {
               if (fileSize > 26214400L) {
                  throw new Exception("File " + fileName + " exceeds the maximum allowed size of 25 MB.");
               }
            } else if (this._className.equalsIgnoreCase("BRANCH_DIGITIATION")) {
               if (fileSize > 104857600L) {
                  throw new Exception("File " + fileName + " exceeds the maximum allowed size of 100 MB.");
               }
            } else if (INSPECTION_AUDIT_CLASSES.contains(this._className)) {
               if (fileSize > 26214400L) {
                  throw new Exception("File " + fileName + " exceeds the maximum allowed size of 25 MB.");
               }
            } else if (fileSize > 10485760L) {
               throw new Exception("File " + fileName + " exceeds the maximum allowed size of 10 MB.");
            }

            String extension = fileName.substring(fileName.lastIndexOf(46) + 1).toLowerCase();
            if (this._className.equalsIgnoreCase("E_PLATEFORM")) {
               if (!_allowedEPLTFRMExtensions.contains(extension)) {
                  throw new Exception(extension + " extension is not allowed.");
               }
            } else if (!_allowedExtensions.contains(extension)) {
               throw new Exception(extension + " extension is not allowed.");
            }

            _log.info("File " + fileName + " passed validation.");
         } else {
            _log.info("No file found for key: " + fieldKey);
         }
      }

   }

   public void validateRequestValues(MultipartBody multipartBody) throws Exception {
   }
}
