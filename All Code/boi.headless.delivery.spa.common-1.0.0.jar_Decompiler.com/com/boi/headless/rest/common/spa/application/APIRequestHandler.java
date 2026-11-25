package com.boi.headless.rest.common.spa.application;

import boi.headless.delivery.spa.common.FileExportBackgourndJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Order;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTransition;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public class APIRequestHandler {
   private static final long FILE_UPLOAD_FAILED = 0L;
   private static final long FILE_ALREADY_EXISTS = -4L;
   private final Random random = new Random();
   private static final Log log = LogFactoryUtil.getLog(APIRequestHandler.class);
   private User user;
   private HttpServletRequest contextHttpServletRequest;
   private AcceptLanguage contextAcceptLanguage;
   private ObjectFieldLocalService objectFieldLocalService;
   private DLAppService dlAppService;
   private UriInfo contextUriInfo;
   private DTOConverterRegistry dtoConverterRegistry;
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;
   private ObjectDefinitionLocalService objectDefinitionLocalService;
   private UserHierarchy userHierarchy;
   private ObjectEntryLocalService objectEntryLocalService;
   private BOISPAConfiguation boiSpaConfiguation;
   private WorkflowTaskManager workflowTaskManager;
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";

   public APIRequestHandler() {
   }

   public ObjectEntry getObjectEntry(long objectEntryId) {
      try {
         return this.objectEntryLocalService.fetchObjectEntry(objectEntryId);
      } catch (Exception var4) {
         return null;
      }
   }

   public String getFilterQuery(String status, String createdFrom, String createdTo) {
      StringBuilder query = new StringBuilder();
      log.info("status:" + status);
      log.info("createdFrom:" + createdFrom);
      log.info("createdTo:" + createdTo);
      if (Validator.isNotNull(status)) {
         query.append("status='").append(status).append("'");
      }

      return query.toString();
   }

   public JSONObject convertEntryToArray(Map<String, Serializable> values) {
      if (Validator.isNotNull(values)) {
         JSONObject jsonArray = JSONFactoryUtil.createJSONObject();
         Objects.requireNonNull(jsonArray);
         values.forEach(jsonArray::put);
         return jsonArray;
      } else {
         return null;
      }
   }

   public List<WorkflowTask> getWorkflowTasks(ObjectDefinition objectDefinition, User user, long classPk) {
      try {
         return this.workflowTaskManager.searchWorkflowTasks(objectDefinition.getCompanyId(), user.getUserId(), (String)null, (String[])null, new String[]{objectDefinition.getClassName()}, new Long[]{classPk}, "com.liferay.portal.kernel.model.User", new Long[]{user.getUserId()}, (Date)null, (Date)null, false, false, false, (Long)null, (Long[])null, true, -1, -1, (OrderByComparator)null).getWorkflowModels();
      } catch (WorkflowException e) {
         e.printStackTrace();
         return Collections.emptyList();
      }
   }

   public List<WorkflowTransition> getWorkflowTransitions(long workflowTaskId) {
      try {
         return this.workflowTaskManager.getWorkflowTaskWorkflowTransitions(workflowTaskId);
      } catch (WorkflowException e) {
         e.printStackTrace();
         return Collections.emptyList();
      }
   }

   public ObjectDefinition getObjectDefinition(long objectDefinitionId) {
      try {
         return this.objectDefinitionLocalService.fetchObjectDefinition(objectDefinitionId);
      } catch (Exception var4) {
         return null;
      }
   }

   public DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
      return new DefaultDTOConverterContext(this.contextAcceptLanguage.isAcceptAllLanguages(), (Map)null, this.dtoConverterRegistry, this.contextHttpServletRequest, objectEntryId, this.contextAcceptLanguage.getPreferredLocale(), this.contextUriInfo, this.user);
   }

   public File createFileFromInputStream(InputStream inputStream, String fileName) throws IOException {
      File file = File.createTempFile(fileName, (String)null);
      FileOutputStream outputStream = new FileOutputStream(file);

      try {
         byte[] buffer = new byte[8192];

         int bytesRead;
         while((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
         }
      } catch (Throwable var8) {
         try {
            outputStream.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      outputStream.close();
      return file;
   }

   private boolean updaloadMultipleFilesForFinastra(Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody) {
      Folder folderCreated = this.userHierarchy.getUploadFolder();
      if (multipartBody != null && folderCreated != null) {
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         System.out.println("---------------");

         for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            log.info(key + " :key: " + value);
            System.out.println(key + " :key: " + value);

            try {
               Object existingValue = values.get(value);
               JSONArray jsonArray = toJsonArray((String)existingValue);
               System.out.println(" :jsonObject: " + jsonArray.toJSONString());
               log.info(" :jsonObject: " + jsonArray.toJSONString());
               BinaryFile binaryFile = multipartBody.getBinaryFile(key);
               if (binaryFile != null) {
                  String fileName = binaryFile.getFileName();
                  File file = this.createFileFromInputStream(binaryFile.getInputStream(), fileName);
                  if (file != null) {
                     FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folderCreated, file, fileName);
                     if (fileEntry != null) {
                        JSONObject fileJson = this.getFileEntryJsonObject(fileEntry);
                        if (Validator.isNotNull(fileJson)) {
                           jsonArray.put(fileJson);
                        }

                        values.put(value, jsonArray.toString());
                     }
                  }
               }
            } catch (Exception e) {
               Log var10000 = log;
               String var10001 = (String)entry.getKey();
               var10000.error("File upload failed for key: {} 123 " + var10001 + e);
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static JSONArray toJsonArray(String jsonData) throws JSONException {
      jsonData = jsonData.trim();
      if (jsonData.startsWith("[")) {
         return JSONFactoryUtil.createJSONArray(jsonData);
      } else if (jsonData.startsWith("{")) {
         JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
         jsonArray.put(JSONFactoryUtil.createJSONObject(jsonData));
         return jsonArray;
      } else {
         throw new IllegalArgumentException("Invalid JSON string: " + jsonData);
      }
   }

   public String getTimeStamp() {
      long var10000 = (new Date()).getTime();
      return var10000 + "_" + this.random.nextInt(999999);
   }

   public String appendTimestamp(String fileName, String uniqueId) throws Exception {
      String[] parts = fileName.split("\\.", 2);
      if (parts.length == 2) {
         String name = parts[0];
         String extension = parts[1];
         return Validator.isNotNull(uniqueId) ? name + "_" + uniqueId + "_" + this.getTimeStamp() + "." + extension : name + "_" + this.getTimeStamp() + "." + extension;
      } else {
         throw new Exception("File Have not extenssion");
      }
   }

   private boolean updaloadFile(Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody, String uniqueId, Folder folderCreated) throws Exception {
      if (multipartBody != null && folderCreated != null && fileUploadMap != null && !fileUploadMap.isEmpty()) {
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         boolean uploadSuccess = false;

         for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
            String key = (String)entry.getKey();
            String valueField = (String)entry.getValue();

            try {
               BinaryFile binaryFile = multipartBody.getBinaryFile(key);
               if (binaryFile != null) {
                  String fileName = binaryFile.getFileName();
                  log.info("Received file name isNull " + (fileName == null));
                  InputStream inputStream = binaryFile.getInputStream();

                  try {
                     File file = this.createFileFromInputStream(inputStream, fileName);
                     if (file != null) {
                        FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folderCreated, file, fileName);
                        if (fileEntry != null) {
                           values.put(valueField, this.getFileEntryJson(fileEntry));
                           uploadSuccess = true;
                        }
                     }
                  } catch (Throwable var18) {
                     if (inputStream != null) {
                        try {
                           inputStream.close();
                        } catch (Throwable var17) {
                           var18.addSuppressed(var17);
                        }
                     }

                     throw var18;
                  }

                  if (inputStream != null) {
                     inputStream.close();
                  }
               }
            } catch (IOException e) {
               log.error("Error uploading file for key: {}" + key + e);
            }
         }

         return uploadSuccess;
      } else {
         return false;
      }
   }

   private boolean updaloadFile(Folder folderCreated, Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody) {
      if (multipartBody != null && folderCreated != null) {
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         boolean uploadSuccess = false;

         for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
            String key = (String)entry.getKey();
            String valueField = (String)entry.getValue();

            try {
               BinaryFile binaryFile = multipartBody.getBinaryFile(key);
               if (binaryFile != null) {
                  String fileName = binaryFile.getFileName();
                  File file = this.createFileFromInputStream(binaryFile.getInputStream(), fileName);
                  if (file != null) {
                     FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folderCreated, file, fileName);
                     if (fileEntry != null) {
                        values.put(valueField, this.getFileEntryJson(fileEntry));
                        uploadSuccess = true;
                     }
                  }
               }
            } catch (IOException e) {
               log.error("Error uploading file for key: " + key, e);
            }
         }

         return uploadSuccess;
      } else {
         return false;
      }
   }

   private long uploadFileForArchive(Folder folderCreated, Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody, long folderIndex) {
      long uploadedFileId = 0L;
      if (multipartBody != null && folderCreated != null) {
         FileHandler fileHandler = new FileHandler(this.dlAppService);

         for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
            String key = (String)entry.getKey();
            String valueField = (String)entry.getValue();

            try {
               BinaryFile binaryFile = multipartBody.getBinaryFile(key);
               if (binaryFile != null) {
                  String documentName = multipartBody.getValueAsString("documentName");
                  String originalFileName = binaryFile.getFileName();
                  String extension = "";
                  String description = "";
                  int dotIndex = originalFileName.lastIndexOf(46);
                  if (dotIndex > -1) {
                     extension = originalFileName.substring(dotIndex);
                  }

                  String fileName = "";
                  String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                  fileName = documentName != null && !documentName.isBlank() ? documentName.replaceFirst("\\.[^.]+$", "") + "_" + folderIndex + "_" + timeStamp + extension : originalFileName.replaceAll("(\\.[^.]+)$", "_" + folderIndex + "_" + timeStamp + "$1");
                  description = String.valueOf(folderIndex);
                  File file = this.createFileFromInputStream(binaryFile.getInputStream(), fileName);
                  if (file != null) {
                     FileEntry fileEntry = null;
                     fileEntry = fileHandler.createFileEntryArchive(folderCreated, file, fileName, description);
                     if (fileEntry != null) {
                        uploadedFileId = fileEntry.getFileEntryId();
                        values.put(valueField, this.getFileEntryJson(fileEntry));
                        return uploadedFileId;
                     }
                  }
               }
            } catch (IOException e) {
               log.error("Error uploading file for key: " + key, e);
            }
         }

         return uploadedFileId;
      } else {
         return 0L;
      }
   }

   public ObjectEntry handleProdAddDocumentRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload) throws Exception {
      long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
      Long folderIndex = ParamUtil.getLong(httpServletRequest, "folderIndex");
      FileHandler fileHandler = new FileHandler(this.dlAppService);
      Folder folder = fileHandler.getFolderDtl(folderIndex);
      if (folder == null) {
         throw new Exception("ResponseCode = 205 , Message = No folder exists for the provided index: " + folderIndex);
      } else {
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var18 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         long uploadedFileId = this.uploadFileForProd(folder, var18, filesValueUpload, multipartBody, folderIndex);
         if (uploadedFileId == -4L) {
            throw new Exception("ResponseCode = 203 , Message = Upload failed: file with the same name already exists.");
         } else if (uploadedFileId == 0L) {
            throw new Exception("ResponseCode = 204 , Message = File upload failed. Please try again");
         } else {
            var18.put("uploadedFileId", uploadedFileId);
            var18.put("folderIndex", folderIndex);
            return this.updateData(this.user, var18, status, referenceCode, objectDefinitionId);
         }
      }
   }

   private long uploadFileForProd(Folder folder, Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody, long folderIndex) {
      long uploadedFileId = 0L;
      if (multipartBody != null && folder != null) {
         FileHandler fileHandler = new FileHandler(this.dlAppService);

         for(Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
            String key = (String)entry.getKey();
            String valueField = (String)entry.getValue();

            try {
               BinaryFile binaryFile = multipartBody.getBinaryFile(key);
               if (binaryFile != null) {
                  String documentName = multipartBody.getValueAsString("documentName");
                  String originalFileName = binaryFile.getFileName();
                  String extension = "";
                  String description = "";
                  int dotIndex = originalFileName.lastIndexOf(46);
                  if (dotIndex > -1) {
                     extension = originalFileName.substring(dotIndex);
                  }

                  String fileName = "";
                  String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                  fileName = documentName != null && !documentName.isBlank() ? documentName.replaceFirst("\\.[^.]+$", "") + "_" + folderIndex + "_" + timeStamp + extension : originalFileName.replaceAll("(\\.[^.]+)$", "_" + folderIndex + "_" + timeStamp + "$1");
                  description = String.valueOf(folderIndex);
                  File file = this.createFileFromInputStream(binaryFile.getInputStream(), fileName);
                  if (file != null) {
                     FileEntry fileEntry = null;
                     fileEntry = fileHandler.createFileEntryProd(folder, file, fileName, description);
                     if (fileEntry != null) {
                        uploadedFileId = fileEntry.getFileEntryId();
                        values.put(valueField, this.getFileEntryJson(fileEntry));
                        return uploadedFileId;
                     }
                  }
               }
            } catch (IOException e) {
               log.error("Error uploading file for key: " + key, e);
            }
         }

         return uploadedFileId;
      } else {
         return 0L;
      }
   }

   private String updaloadFileBranchDigi(Folder folder, BinaryFile binaryFile, String branchCode) {
      if (folder == null) {
         log.warn("Folder is null. Cannot proceed with file upload.");
         return null;
      } else if (binaryFile == null) {
         log.warn("Binary file is null. Skipping upload.");
         return null;
      } else {
         try {
            String fileName = binaryFile.getFileName();
            fileName = this.getUpdatedFileName(fileName, branchCode);
            File file = this.createFileFromInputStream(binaryFile.getInputStream(), fileName);
            if (file == null) {
               log.warn("Failed to create file from input stream for file: " + fileName);
               return null;
            } else {
               FileHandler fileHandler = new FileHandler(this.dlAppService);
               FileEntry entry = fileHandler.createOrRenameFileEntryBranchDigi(folder, file, fileName);
               if (entry == null) {
                  log.warn("FileEntry is null after attempting upload for file: " + fileName);
                  return null;
               } else {
                  return this.getFileEntryJson(entry);
               }
            }
         } catch (IOException e) {
            log.error("IOException occurred while uploading file", e);
            return null;
         }
      }
   }

   private String getUpdatedFileName(String originalFileName, String newBranchCode) {
      String extension = "";
      int dotIndex = originalFileName.lastIndexOf(46);
      String baseName;
      if (dotIndex != -1) {
         baseName = originalFileName.substring(0, dotIndex);
         extension = originalFileName.substring(dotIndex);
      } else {
         baseName = originalFileName;
      }

      baseName = baseName.replaceAll("(_[A-Za-z0-9]+)?_\\d{8}_\\d{6,}$", "");
      String timestamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
      baseName = baseName + "_" + newBranchCode + "_" + timestamp;
      return baseName + extension;
   }

   private JSONObject getFileEntryJsonObject(FileEntry entry) {
      JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

      try {
         String downloadURL = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), (ThemeDisplay)null, (String)null);
         jsonObject.put("URL", downloadURL);
         jsonObject.put("documentNameDetailedEntryId", entry.getFileEntryId());
         jsonObject.put("fileName", entry.getFileName());
         jsonObject.put("fileExtension", entry.getExtension());
      } catch (PortalException var4) {
         jsonObject.put("fileId", entry.getFileEntryId());
         jsonObject.put("error", "Failed to generate download URL");
      }

      return jsonObject;
   }

   private String getFileEntryJson(FileEntry entry) {
      try {
         String downloadURL = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), (ThemeDisplay)null, (String)null);
         JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
         jsonObject.put("URL", downloadURL);
         jsonObject.put("fileId", entry.getFileEntryId());
         jsonObject.put("fileName", entry.getFileName());
         jsonObject.put("fileExtension", entry.getExtension());
         return jsonObject.toJSONString();
      } catch (PortalException var4) {
         return String.valueOf(entry.getFileEntryId());
      }
   }

   public static User getHello() {
      try {
         log.info("Reuring to UI");
         return UserLocalServiceUtil.getUser(41425L);
      } catch (PortalException var1) {
         return null;
      }
   }

   public ObjectEntry handleAddFolderRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId) throws Exception {
      long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
      Long parentFolderIndex = ParamUtil.getLong(httpServletRequest, "parentFolderIndex");
      String folderName = ParamUtil.getString(httpServletRequest, "folderName");
      FileHandler fileHandler = new FileHandler(this.dlAppService);
      if (Validator.isBlank(folderName)) {
         log.warn("Folder name is blank.");
         return null;
      } else {
         Folder parentFolder = fileHandler.getFolder(parentFolderIndex);
         if (parentFolder == null) {
            throw new Exception("ResponseCode = 304 , Message = Parent folder not found for ID: " + parentFolderIndex);
         } else {
            Folder folder = fileHandler.createFolder(folderName, parentFolder);
            if (folder == null) {
               throw new Exception("ResponseCode = 305 , Message = There is some issue while calling add folder api .");
            } else {
               log.info("Created folder with ID: " + folder.getFolderId());
               long var10000 = folder.getParentFolderId();
               String uniqueERC = var10000 + "-" + folder.getFolderId() + "-" + folder.getName();
               ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(uniqueERC, objectDefinitionId);
               if (entry != null) {
                  throw new Exception("ResponseCode = 302 , Message = Folder already created with this name : " + folderName + " under parent " + parentFolderIndex);
               } else {
                  httpServletRequest.setAttribute("folderIndex", folder.getFolderId());
                  ObjectEntry objectEntry = this.getObjectEntry(entryId);
                  Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
                  List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
                  Map<String, Serializable> var21 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
                  var21.put("folderIndex", folder.getFolderId());
                  return this.updateData(this.user, var21, status, uniqueERC, objectDefinitionId);
               }
            }
         }
      }
   }

   public ObjectEntry handleArchiveAddDocumentRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, String apiName) throws Exception {
      long folderIndex = 0L;
      long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
      Long parentFolderIndex = ParamUtil.getLong(httpServletRequest, "folderIndex");
      FileHandler fileHandler = new FileHandler(this.dlAppService);
      if (apiName.equalsIgnoreCase("Eplatform")) {
         long eplatFormDefaultFolderId = this.boiSpaConfiguation.EplatFormDefaultFolderId();
         folderIndex = eplatFormDefaultFolderId;
      } else if (apiName.equalsIgnoreCase("KCCJansmarth")) {
         long kccJansmarthDefaultFolderId = this.boiSpaConfiguation.KCCJansmarthDefaultFolderId();
         folderIndex = kccJansmarthDefaultFolderId;
      } else if (apiName.equalsIgnoreCase("SSY")) {
         long ssyDefaultFolderId = this.boiSpaConfiguation.SSYDefaultFolderId();
         folderIndex = ssyDefaultFolderId;
      }

      Folder folder = fileHandler.getFolderDtl(folderIndex);
      if (folder == null) {
         throw new Exception("ResponseCode = 205 , Message = No folder exists for the provided index: " + folderIndex);
      } else {
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var24 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         long uploadedFileId = this.uploadFileForArchive(folder, var24, filesValueUpload, multipartBody, parentFolderIndex);
         if (uploadedFileId == -4L) {
            throw new Exception("ResponseCode = 203 , Message = Upload failed: file with the same name already exists.");
         } else if (uploadedFileId == 0L) {
            throw new Exception("ResponseCode = 204 , Message = File upload failed. Please try again");
         } else {
            var24.put("uploadedFileId", uploadedFileId);
            var24.put("folderIndex", parentFolderIndex);
            return this.updateData(this.user, var24, status, referenceCode, objectDefinitionId);
         }
      }
   }

   public ObjectEntry handleFinastraAddDoc(HttpServletRequest httpServletRequest, MultipartBody multipartBody, long objectDefinitionId, Map<String, String> filesValueUpload, long entryId) throws Exception {
      ObjectEntry entry = this.getObjectEntry(entryId);
      if (Validator.isNull(entry)) {
         throw new Exception("Invalid entry try to update");
      } else {
         String finastraStatus = ParamUtil.getString(httpServletRequest, "finastraStatus", (String)null);
         Map<String, Serializable> values = Validator.isNotNull(entry) ? entry.getValues() : null;
         if (!Validator.isBlank(finastraStatus) && !Validator.isNull(values)) {
            values.put("finastraStatus", finastraStatus);
            String referanceCode = Validator.isNotNull(entry) ? entry.getExternalReferenceCode() : null;
            this.updaloadMultipleFilesForFinastra(values, filesValueUpload, multipartBody);
            return this.finUpdateData(this.user, values, referanceCode, objectDefinitionId);
         } else {
            throw new Exception("Invalid entry status with values try to update");
         }
      }
   }

   public ObjectEntry handleFinastraOtherAddDoc(HttpServletRequest httpServletRequest, MultipartBody multipartBody, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload, long entryId) {
      ObjectEntry entry = null;
      if (entryId != 0L) {
         entry = this.getObjectEntry(entryId);
      }

      try {
         String finastraStatus = ParamUtil.getString(httpServletRequest, "finastraStatus", (String)null);
         String refrenceNumber = ParamUtil.getString(httpServletRequest, "referenceNumber", (String)null);
         Map<String, Serializable> values = (Map<String, Serializable>)(entry != null ? entry.getValues() : new HashMap());
         values.put("finastraStatus", finastraStatus);
         values.put("referenceNumber", refrenceNumber);
         String referenceCode = entry != null ? entry.getExternalReferenceCode() : null;
         boolean filesUploaded = this.updaloadMultipleFilesForFinastra(values, filesValueUpload, multipartBody);
         if (filesUploaded) {
            log.info("Files uploaded successfully for Finastra document.");
         }

         return this.updateData(this.user, values, 1, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handleFinastraOtherAddDoc for entryId: {}" + entryId + e);
         return null;
      }
   }

   public ObjectEntry handleBLMRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         new UserHierarchy(this.user);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var25 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("BOARD_LEVEL_MEETINGS", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "mEETINGTYPE"), boardLevelMeetingFolder.getFolderId());
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "fINANCIALYEAR", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "mONTH", (String)null), fINANCIALYEAR.getFolderId());
         String agendaName = ParamUtil.getString(httpServletRequest, "aGENDANAME", (String)null);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         boolean isUploadSuccess = Validator.isNotNull(agendaName) && this.updaloadFile(var25, filesValueUpload, multipartBody, agendaName, mONTH);
         boolean isPublishAndExistingEntry = status == 1 && objectEntry != null;
         return !isUploadSuccess && !isPublishAndExistingEntry ? this.updateData(this.user, var25, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var25, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error while handling BLM request" + e);
         return null;
      }
   }

   private String getSafeText(JsonNode node, String fieldName) {
      JsonNode fieldNode = node.get(fieldName);
      if (fieldNode != null && !fieldNode.isNull()) {
         String value = fieldNode.asText();
         if (value != null && !value.trim().isEmpty()) {
            return value.trim();
         }
      }

      return "";
   }

   public BranchDigitalResponse handleBranchDigitalRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId) {
      BranchDigitalResponse response = new BranchDigitalResponse();
      List<ObjectEntry> entryList = response.getEntries();

      try {
         new FileHandler(this.dlAppService);
         Folder folder = DLAppLocalServiceUtil.getFolder(folderId);
         String metadataJson = multipartBody.getValueAsString("metadata");
         ObjectMapper mapper = new ObjectMapper();
         JsonNode root = mapper.readTree(metadataJson);
         JsonNode records = root.get("records");
         if (records == null || !records.isArray() || records.size() == 0) {
            response.setSuccess(false);
            response.setMessage("No records found in metadata");
            return response;
         }

         int totalRows = records.size();
         if (totalRows > 50) {
            response.setSuccess(false);
            response.setMessage("Maximum 50 rows allowed. Uploaded file contains " + totalRows + " rows.");
            return response;
         }

         List<String> errors = new ArrayList();
         int rowNumber = 1;

         for(JsonNode record : records) {
            this.getSafeText(record, "branchCode");
            String documentName = this.getSafeText(record, "documentName");
            BinaryFile file = multipartBody.getBinaryFile(documentName);
            if (documentName != null && !documentName.isEmpty()) {
               if (file == null) {
                  errors.add("Row " + rowNumber + ": File not found for documentName -> " + documentName);
               }
            } else {
               errors.add("Row " + rowNumber + ": Missing documentName");
            }

            ++rowNumber;
         }

         if (!errors.isEmpty()) {
            response.setSuccess(false);
            response.setMessage(String.join(", ", errors));
            return response;
         }

         for(JsonNode record : records) {
            String documentName = this.getSafeText(record, "documentName");
            log.info("Processing documentName: {}" + documentName);
            String branchName = this.getSafeText(record, "branchName");
            String branchCode = this.getSafeText(record, "branchCode");
            long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
            ObjectEntry objectEntry = this.getObjectEntry(entryId);
            Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
            List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
            Map<String, Serializable> var39 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
            var39.put("branchName", branchName);
            var39.put("branchCode", branchCode);
            var39.put("cartonBoxBarcode", this.getSafeText(record, "cartonBoxBarcode"));
            var39.put("documentFileBarcode", this.getSafeText(record, "documentFileBarcode"));
            var39.put("lANAccountNumberfileNo", this.getSafeText(record, "lANAccountNumberfileNo"));
            var39.put("accountType", this.getSafeText(record, "accountType"));
            var39.put("accountTypeCategory1", this.getSafeText(record, "accountTypeCategory1"));
            var39.put("accountTypeCategory2", this.getSafeText(record, "accountTypeCategory2"));
            var39.put("descriptionOfTheDocumentFileNoAndName", this.getSafeText(record, "descriptionOfTheDocumentFileNoAndName"));
            var39.put("totalNumberOfPagesInTheDocument", this.getSafeText(record, "totalNumberOfPagesInTheDocument"));
            var39.put("boxNoHavingCountOfDocuments", this.getSafeText(record, "boxNoHavingCountOfDocuments"));
            var39.put("collectedFromNominatedDEPARTMENTBranch", this.getSafeText(record, "collectedFromNominatedDEPARTMENTBranch"));
            var39.put("collectedByServiceProviderRepresentative", this.getSafeText(record, "collectedByServiceProviderRepresentative"));
            var39.put("dateOfCollection", this.getSafeText(record, "dateOfCollection"));
            var39.put("expectedDateOfReturn", this.getSafeText(record, "expectedDateOfReturn"));
            var39.put("returnedByServiceProviderRepresentative", this.getSafeText(record, "returnedByServiceProviderRepresentative"));
            var39.put("actualDateOfReturn", this.getSafeText(record, "actualDateOfReturn"));
            var39.put("documentName", documentName);
            String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
            BinaryFile file = multipartBody.getBinaryFile(documentName);
            if (file != null) {
               log.info("Uploading file: {}" + file.getFileName());
               String documentPath = this.updaloadFileBranchDigi(folder, file, branchCode);
               var39.put("document", documentPath);
               ObjectEntry updatedEntry = this.updateData(this.user, var39, status, referenceCode, objectDefinitionId);
               entryList.add(updatedEntry);
               log.info("Entry updated successfully for document: {}" + documentName);
            } else {
               log.warn("File not found for documentName: {}" + documentName);
            }
         }

         response.setEntries(entryList);
         response.setSuccess(true);
         response.setMessage("All records processed successfully");
      } catch (JsonProcessingException e) {
         log.error("Error parsing metadata JSON for branch digital request.", e);
         response.setSuccess(false);
         response.setMessage("Error parsing metadata JSON: " + e.getMessage());
      } catch (Exception e) {
         log.error("Error processing branch digital request.", e);
         response.setSuccess(false);
         response.setMessage("Error processing branch digital request: " + e.getMessage());
      }

      return response;
   }

   public ObjectEntry handleCDPDRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder cdadFolder = fileHandler.createFolder("CDAD", folderId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var23 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         Folder meetingTypeFolder = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "fGMO"), cdadFolder.getFolderId());
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zONE", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "cASEOFBrZone", (String)null), fINANCIALYEAR.getFolderId());
         Folder cASENo = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "cASENo", (String)null), mONTH.getFolderId());
         boolean uploadSuccess = cdadFolder != null && this.updaloadFile(var23, filesValueUpload, multipartBody, "", cASENo);
         return !uploadSuccess && (status != 1 || objectEntry == null) ? this.updateData(this.user, var23, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error while processing CDPD request" + e);
         return null;
      }
   }

   public ObjectEntry handleHORDRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var22 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("HR Department", folderId);
         Folder recuritment = fileHandler.createFolder("Recruitment", boardLevelMeetingFolder.getFolderId());
         Folder meetingTypeFolder = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zone"), recuritment.getFolderId());
         boolean uploadSuccess = this.updaloadFile(var22, filesValueUpload, multipartBody, "HORD", meetingTypeFolder);
         boolean isPublishAndExistingEntry = status == 1 && objectEntry != null;
         return !uploadSuccess && !isPublishAndExistingEntry ? this.updateData(this.user, var22, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var22, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error while processing HORD request" + e);
         return null;
      }
   }

   public ObjectEntry handleILMRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(Validator.isNotNull(objectEntry) ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var22 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder meetingTypeFolder = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "nBG"), folderId);
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zone", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "branchName", (String)null), fINANCIALYEAR.getFolderId());
         boolean isFileUploaded = this.updaloadFile(var22, filesValueUpload, multipartBody, "ILM", mONTH);
         boolean isPublishWithEntry = status == 1 && objectEntry != null;
         return !isFileUploaded && !isPublishWithEntry ? this.updateData(this.user, var22, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var22, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handleILMRequest: ", e);
         return null;
      }
   }

   public ObjectEntry handleIntDepRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var21 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("International Department", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "documentType"), boardLevelMeetingFolder.getFolderId());
         boolean isFileUploaded = this.updaloadFile(var21, filesValueUpload, multipartBody, "INT_DEP", meetingTypeFolder);
         boolean isPublishAction = status == 1 && objectEntry != null;
         return !isFileUploaded && !isPublishAction ? this.updateData(this.user, var21, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var21, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handleIntDepRequest", e);
         return null;
      }
   }

   public ObjectEntry handlePrManageRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var22 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("IT Department", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder("Project Management", boardLevelMeetingFolder.getFolderId());
         Folder projectName = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "projectName", (String)null), meetingTypeFolder.getFolderId());
         boolean isFileUploaded = this.updaloadFile(var22, filesValueUpload, multipartBody, "PR", projectName);
         boolean isPublishWithEntry = status == 1 && objectEntry != null;
         return !isFileUploaded && !isPublishWithEntry ? this.updateData(this.user, var22, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var22, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handlePrManageRequest", e);
         return null;
      }
   }

   public ObjectEntry handleIfaAuditRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var28 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder("Foreign Audit", boardLevelMeetingFolder.getFolderId());
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "centre", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "branches", (String)null), fINANCIALYEAR.getFolderId());
         Folder branch = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "financialYear", (String)null), mONTH.getFolderId());
         String financialYear = ParamUtil.getString(httpServletRequest, "financialYear");
         String reportDate = ParamUtil.getString(httpServletRequest, "reportDate");
         long uniqueNumber = System.currentTimeMillis();
         String referenceNo = "INSPECTION_FOREIGN_AUDIT_" + financialYear + "_" + reportDate + "_" + uniqueNumber;
         var28.put("referenceNo", referenceNo);
         this.updaloadFile(var28, filesValueUpload, multipartBody, referenceNo, branch);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         return this.updateData(this.user, var28, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handleIfaAuditRequest", e);
         return null;
      }
   }

   public ObjectEntry handleEwaRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var22 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder dwaFolder = fileHandler.createFolder("Electrical Wiring Diagram", folderId);
         Folder nBG = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "nBG"), dwaFolder.getFolderId());
         Folder zONE = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zONE", (String)null), nBG.getFolderId());
         Folder branchNAME = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "branchNAME", (String)null), zONE.getFolderId());
         boolean uploadSuccess = this.updaloadFile(var22, filesValueUpload, multipartBody, "EWA", branchNAME);
         return !uploadSuccess && (status != 1 || objectEntry == null) ? this.updateData(this.user, var22, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var22, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error processing EWA request", e);
         return null;
      }
   }

   public ObjectEntry handlePocRequest(HttpServletRequest httpServletRequest, MultipartBody records, int status, long objectDefinitionId, long folderId, Map<String, String> filesValueUpload) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var16 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         return this.updateData(this.user, var16, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error handling POC request", e);
         return null;
      }
   }

   public ObjectEntry handleDwaRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var21 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder dwaFolder = fileHandler.createFolder("Data Warehouse", folderId);
         Folder subject = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "subject"), dwaFolder.getFolderId());
         Folder subType = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "subType", (String)null), subject.getFolderId());
         boolean uploaded = this.updaloadFile(var21, filesValueUpload, multipartBody, referenceCode, subType);
         return !uploaded && (status != 1 || objectEntry == null) ? this.updateData(this.user, var21, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var21, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error handling DWA request", e);
         return null;
      }
   }

   public ObjectEntry handleIacaRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var22 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder("CA Appointments", boardLevelMeetingFolder.getFolderId());
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "controllingOffice", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zoneDepartment", (String)null), fINANCIALYEAR.getFolderId());
         Folder unit = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "unitName", (String)null), mONTH.getFolderId());
         return !this.updaloadFile(var22, filesValueUpload, multipartBody, referenceCode, unit) && (status != 1 || objectEntry == null) ? this.updateData(this.user, var22, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var22, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error handling IACA request", e);
         return null;
      }
   }

   public ObjectEntry handleIamcRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      String referenceNo = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         if (status == 1) {
            String financialYear = ParamUtil.getString(httpServletRequest, "financialYear");
            String date = ParamUtil.getString(httpServletRequest, "date");
            long uniqueNumber = System.currentTimeMillis();
            referenceNo = "INSPECTION_AUDIT_MISC_" + financialYear + "_" + date + "_" + uniqueNumber;
         }

         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var23 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         if (status == 1) {
            var23.put("referenceNo", referenceNo);
            log.info("Reference number set: {}");
         }

         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder iaFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder acb = fileHandler.createFolder("Misc", iaFolder.getFolderId());
         Folder financialYear = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "financialYear"), acb.getFolderId());
         return !this.updaloadFile(var23, filesValueUpload, multipartBody, referenceNo, financialYear) && (status != 1 || objectEntry == null) ? this.updateData(this.user, var23, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error while handling IAMC request", e);
         return null;
      }
   }

   public ObjectEntry handleIarsRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      String referenceNo = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         if (status == 1) {
            long uniqueNumber = System.currentTimeMillis();
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy");
            String formattedDate = today.format(formatter);
            referenceNo = "INSPECTION_AUDIT_REPORTS_" + today.getYear() + "_" + formattedDate + "_" + uniqueNumber;
         }

         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var24 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         if (status == 1) {
            var24.put("referenceNo", referenceNo);
         }

         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder("Reports", boardLevelMeetingFolder.getFolderId());
         Folder fINANCIALYEAR = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zAO", (String)null), meetingTypeFolder.getFolderId());
         Folder mONTH = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "zone", (String)null), fINANCIALYEAR.getFolderId());
         Folder branch = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "branch", (String)null), mONTH.getFolderId());
         return !this.updaloadFile(var24, filesValueUpload, multipartBody, referenceCode, branch) && (status != 1 || objectEntry == null) ? this.updateData(this.user, var24, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var24, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         log.error("Error in handleIarsRequest", e);
         return null;
      }
   }

   public ObjectEntry handleHSDRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      ObjectEntry updatedEntry = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var20 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("Security Department", folderId);
         Folder folderCreated = fileHandler.createFolder("HO_SECURITY_DEPARTMENT", boardLevelMeetingFolder.getFolderId());
         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         this.updaloadFile(folderCreated, var20, filesValueUpload, multipartBody);
         updatedEntry = this.updateData(this.user, var20, status, referenceCode, objectDefinitionId);
      } catch (Exception e) {
         Log var10000 = log;
         String var10001 = e.getMessage();
         var10000.error("Error in handleHSDRequest: {}" + var10001 + e);
      }

      return updatedEntry;
   }

   public ObjectEntry handleAuditAceRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      String referenceNo = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         if (status == 1) {
            String financialYear = ParamUtil.getString(httpServletRequest, "financialYear");
            String acbDate = ParamUtil.getString(httpServletRequest, "date");
            long uniqueNumber = System.currentTimeMillis();
            referenceNo = "INSPECTION_AUDIT_ACE_" + financialYear + "_" + acbDate + "_" + uniqueNumber;
         }

         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var23 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         if (status == 1) {
            var23.put("referenceNo", referenceNo);
         }

         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder iaFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder acb = fileHandler.createFolder("ACE", iaFolder.getFolderId());
         Folder financialYear = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "financialYear"), acb.getFolderId());
         if (this.updaloadFile(var23, filesValueUpload, multipartBody, "AUDIT_ACE", financialYear)) {
            return this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
         } else {
            return status == 1 && objectEntry != null ? this.updateData(this.user, var23, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
         }
      } catch (Exception e) {
         log.error("Error in handleAuditAceRequest", e);
         return null;
      }
   }

   public ObjectEntry handleIAACBRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      String referenceNo = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         if (status == 1) {
            String financialYear = ParamUtil.getString(httpServletRequest, "financialYear");
            String acbDate = ParamUtil.getString(httpServletRequest, "aCBDate");
            long uniqueNumber = System.currentTimeMillis();
            referenceNo = "INSPECTION_AUDIT_ACB_" + financialYear + "_" + acbDate + "_" + uniqueNumber;
         } else {
            log.info("Got entry ID: {}" + entryId);
         }

         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var23 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder iaFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder acb = fileHandler.createFolder("ACB", iaFolder.getFolderId());
         Folder financialYear = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "financialYear"), acb.getFolderId());
         if (status == 1) {
            var23.put("referenceNo", referenceNo);
         }

         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         if (this.updaloadFile(var23, filesValueUpload, multipartBody, referenceCode, financialYear)) {
            return this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
         } else {
            return status == 1 && objectEntry != null ? this.updateData(this.user, var23, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var23, status, referenceCode, objectDefinitionId);
         }
      } catch (Exception e) {
         log.error("Error in handleIAACBRequest", e);
         return null;
      }
   }

   public ObjectEntry handleIOARRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload, long folderId) {
      String referenceNo = null;

      try {
         long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
         if (status == 1) {
            String reportDated = ParamUtil.getString(httpServletRequest, "reportDated");
            long uniqueNumber = System.currentTimeMillis();
            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));
            referenceNo = "INSPECTION_AUDIT_OTHER_REPORT_" + reportDated + "_" + formattedDate + "_" + uniqueNumber;
         }

         ObjectEntry objectEntry = this.getObjectEntry(entryId);
         Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap());
         List<ObjectField> fields = this.objectFieldLocalService.getObjectFields(objectDefinitionId);
         Map<String, Serializable> var24 = ObjectConverter.getDefaultData(httpServletRequest, fields, values);
         if (status == 1) {
            var24.put("referenceNo", referenceNo);
         }

         String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
         FileHandler fileHandler = new FileHandler(this.dlAppService);
         Folder boardLevelMeetingFolder = fileHandler.createFolder("Inspection Audit", folderId);
         Folder meetingTypeFolder = fileHandler.createFolder("Other Reports", boardLevelMeetingFolder.getFolderId());
         Folder unit = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "unit", (String)null), meetingTypeFolder.getFolderId());
         Folder subUnit = fileHandler.createFolder(ParamUtil.getString(httpServletRequest, "subUnit", (String)null), unit.getFolderId());
         if (this.updaloadFile(var24, filesValueUpload, multipartBody, referenceCode, subUnit)) {
            return this.updateData(this.user, var24, status, referenceCode, objectDefinitionId);
         } else {
            return status == 1 && objectEntry != null ? this.updateData(this.user, var24, status, referenceCode, objectDefinitionId) : this.updateData(this.user, var24, status, referenceCode, objectDefinitionId);
         }
      } catch (Exception e) {
         log.error("Error in handleIOARRequest", e);
         return null;
      }
   }

   private ObjectEntry finUpdateData(User user, Map<String, Serializable> values, String referanceCode, long objectDefinitionId) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(user.getCompanyId());
         serviceContext.setUserId(user.getUserId());
         ObjectEntry entry = this.objectEntryLocalService.addOrUpdateObjectEntry(referanceCode, user.getUserId(), 0L, objectDefinitionId, values, serviceContext);
         log.info("saved object entry is " + entry.getObjectEntryId());
         return entry;
      } catch (PortalException e) {
         e.printStackTrace();
         return null;
      }
   }

   private ObjectEntry updateData(User user, Map<String, Serializable> values, int status, String referanceCode, long objectDefinitionId) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(user.getCompanyId());
         serviceContext.setUserId(user.getUserId());
         serviceContext.setWorkflowAction(status);
         ObjectEntry entry = this.objectEntryLocalService.addOrUpdateObjectEntry(referanceCode, user.getUserId(), 0L, objectDefinitionId, values, serviceContext);
         log.info("saved object entry is " + entry.getObjectEntryId());
         return entry;
      } catch (PortalException e) {
         e.printStackTrace();
         return null;
      }
   }

   public APIRequestHandler(User user, HttpServletRequest contextHttpServletRequest, AcceptLanguage contextAcceptLanguage, ObjectFieldLocalService objectFieldLocalService, DLAppService dlAppService, UriInfo contextUriInfo, DTOConverterRegistry dtoConverterRegistry, ObjectEntryManagerRegistry objectEntryManagerRegistry, ObjectDefinitionLocalService objectDefinitionLocalService, ObjectEntryLocalService objectEntryLocalService, WorkflowTaskManager workflowTaskManager, BOISPAConfiguation boiWorkflowConfiguation, String departmentId, long hoFolderId) {
      this.user = user;
      this.contextHttpServletRequest = contextHttpServletRequest;
      this.contextAcceptLanguage = contextAcceptLanguage;
      this.objectFieldLocalService = objectFieldLocalService;
      this.dlAppService = dlAppService;
      this.contextUriInfo = contextUriInfo;
      this.dtoConverterRegistry = dtoConverterRegistry;
      this.objectEntryManagerRegistry = objectEntryManagerRegistry;
      this.objectDefinitionLocalService = objectDefinitionLocalService;
      this.boiSpaConfiguation = boiWorkflowConfiguation;
      this.objectEntryLocalService = objectEntryLocalService;
      this.userHierarchy = new UserHierarchy(user, hoFolderId, departmentId, dlAppService);
      this.workflowTaskManager = workflowTaskManager;
   }

   public void exportToExcelData(String statusQuery, long objectDefinitionid, long folderId) {
      Map<String, Serializable> taskContextMap = new HashMap();
      ServiceContext serviceContext = new ServiceContext();
      taskContextMap.put("companyId", this.user.getCompanyId());
      taskContextMap.put("statusQuery", statusQuery);
      taskContextMap.put("userId", this.user.getUserId());
      taskContextMap.put("serviceContext", serviceContext);
      taskContextMap.put("screenName", this.user.getScreenName());
      taskContextMap.put("objectDefinitionId", objectDefinitionid);
      taskContextMap.put("folderId", folderId);

      try {
         this.deleteOldTask(objectDefinitionid);
         log.info("invoking  MyBackgroundTaskExecutor");
         BackgroundTask backgroundTask = BackgroundTaskLocalServiceUtil.addBackgroundTask(this.user.getUserId(), this.user.getGroupId(), String.valueOf(objectDefinitionid), (String[])null, FileExportBackgourndJob.class, taskContextMap, serviceContext);
         BackgroundTaskManagerUtil.triggerBackgroundTask(backgroundTask.getBackgroundTaskId());
         log.info("End  MyBackgroundTaskExecutor" + backgroundTask.getBackgroundTaskId());
      } catch (PortalException e1) {
         e1.printStackTrace();
      }

   }

   private void deleteOldTask(long objectDefinitionId) {
      for(BackgroundTask backgroundTask : this.getBackgroundTasks(objectDefinitionId)) {
         if (backgroundTask.isCompleted()) {
            try {
               BackgroundTaskLocalServiceUtil.deleteBackgroundTask(backgroundTask.getBackgroundTaskId());
            } catch (PortalException e) {
               e.printStackTrace();
            }
         }
      }

   }

   private List<BackgroundTask> getBackgroundTasks(long objectDefinitionId) {
      DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
      dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", this.user.getUserId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", this.user.getGroupId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("name", String.valueOf(objectDefinitionId)));
      Order order = OrderFactoryUtil.desc("modifiedDate");
      dynamicQuery.addOrder(order);
      return BackgroundTaskLocalServiceUtil.dynamicQuery(dynamicQuery);
   }

   public String getTaskComplatedURL(long objectDefinitionid) {
      try {
         List<BackgroundTask> backgroundTasks = this.getBackgroundTasks(objectDefinitionid);
         log.info(" backgroundTasks : " + (backgroundTasks == null));
         if (Validator.isNotNull(backgroundTasks) && !backgroundTasks.isEmpty()) {
            BackgroundTask backgroundTask = (BackgroundTask)backgroundTasks.get(0);
            if (backgroundTask.isCompleted()) {
               String fileType = (String)backgroundTask.getTaskContextMap().get("fileURL");
               return Validator.isNotNull(fileType) ? fileType : "";
            }

            Log var10000 = log;
            boolean var10001 = backgroundTask.isInProgress();
            var10000.info(var10001 + " : " + backgroundTask.getStatusLabel() + " --- " + backgroundTask.isNew() + " --- " + backgroundTask.getBackgroundTaskId());
            if (backgroundTask.isInProgress() || backgroundTask.isNew() || !backgroundTask.isCompleted()) {
               return "in-progress";
            }
         }

         return null;
      } catch (Exception w) {
         w.printStackTrace();
         return null;
      }
   }

   public Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> getUserDeparments(long objectId, String query, User user) {
      try {
         ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectId);
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this.dtoConverterRegistry, userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this.objectEntryManagerRegistry.getObjectEntryManager(userDepartmentObject.getStorageType());
         Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;
         page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject, userDepartmentObject.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);
         log.info("List from user deaprtment " + page.getTotalCount());
         return page;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public String getUserFilterQuery() {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status", (String)null);
      this.addStatusFilter(queryList, status);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      log.debug("Final queryResult:  " + queryResult);
      return queryResult;
   }

   public String getUserFilterQuery(String departmentName) {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status", (String)null);
      if (departmentName.equalsIgnoreCase("BOARDLEVELMEETINGS")) {
         String financeYear = ParamUtil.getString(this.contextHttpServletRequest, "fINANCIALYEAR", (String)null);
         this.addFinancialYearFilter(financeYear, queryList);
      }

      if (departmentName.equalsIgnoreCase("INTERNAL_LAYOUT_MAP") || departmentName.equalsIgnoreCase("ELECTRICAL_WIRING_DIAGRAM")) {
         this.addHierarchyFilter(queryList);
      }

      this.addStatusFilter(queryList, status);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      log.debug("Final queryResult:  " + queryResult);
      return queryResult;
   }

   private boolean containsHierarchyFilters(String queryResult) {
      return queryResult.contains("branchCode") || queryResult.contains("nBGCode") || queryResult.contains("zone");
   }

   private void addFinancialYearFilter(String financeYear, List<String> queryList) {
      if (!Validator.isBlank(financeYear)) {
         queryList.add("fINANCIALYEAR eq '" + financeYear + "'");
      }

   }

   private void addStatusFilter(List<String> queryList, String status) {
      if (Validator.isNotNull(status)) {
         log.debug("Request required status " + status);
         queryList.add("status eq " + WorkflowConstants.getLabelStatus(status));
      }

   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateCreated gt " + var10000 + " and dateCreated lt " + (String)isoRange.get("toDate");
         log.debug("Request required dateFilter  " + dateFilter);
         queryList.add(dateFilter);
      }

   }

   public Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      LocalDate fromDate = LocalDate.parse(fromDateStr);
      LocalDate toDate = LocalDate.parse(toDateStr);
      ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime toDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("fromDate", isoFormatter.format(fromDateTime));
      result.put("toDate", isoFormatter.format(toDateTime));
      return result;
   }

   private void addHierarchyFilter(List<String> queryList) {
      if (!Validator.isBlank(this.userHierarchy.getBranchName())) {
         queryList.add("branchName eq '" + this.userHierarchy.getBranchName() + "'");
      } else if (!Validator.isBlank(this.userHierarchy.getZoneName())) {
         queryList.add("zone eq '" + this.userHierarchy.getZoneName() + "'");
      } else {
         if (!Validator.isBlank(this.userHierarchy.getNbgName())) {
            queryList.add("nBG eq '" + this.userHierarchy.getNbgName() + "'");
         }

      }
   }

   private void addHierarchyFilter(List<String> queryList, boolean branch, boolean nbg, boolean zone) {
      if (!Validator.isBlank(this.userHierarchy.getBranchName()) && branch) {
         queryList.add("branchName eq '" + this.userHierarchy.getBranchName() + "'");
      } else if (!Validator.isBlank(this.userHierarchy.getZoneName()) && zone) {
         queryList.add("zone eq '" + this.userHierarchy.getZoneName() + "'");
      } else {
         if (!Validator.isBlank(this.userHierarchy.getNbgName()) && nbg) {
            queryList.add("nBG eq '" + this.userHierarchy.getNbgName() + "'");
         }

      }
   }

   public class BranchDigitalResponse {
      private boolean success;
      private String message;
      private List<ObjectEntry> entries;

      public BranchDigitalResponse() {
         this.entries = new ArrayList();
         this.success = true;
         this.message = "Success";
      }

      public BranchDigitalResponse(boolean success, String message) {
         this.success = success;
         this.message = message;
         this.entries = new ArrayList();
      }

      public boolean isSuccess() {
         return this.success;
      }

      public void setSuccess(boolean success) {
         this.success = success;
      }

      public String getMessage() {
         return this.message;
      }

      public void setMessage(String message) {
         this.message = message;
      }

      public List<ObjectEntry> getEntries() {
         return this.entries;
      }

      public void setEntries(List<ObjectEntry> entries) {
         this.entries = entries;
      }
   }
}
