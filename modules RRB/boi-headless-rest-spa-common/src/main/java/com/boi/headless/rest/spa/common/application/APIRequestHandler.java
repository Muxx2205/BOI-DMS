package com.boi.headless.rest.spa.common.application;

import com.boi.headless.rest.spa.common.model.UserHierarchy;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;

public class APIRequestHandler {

	private static final Log _log = LogFactoryUtil.getLog(APIRequestHandler.class);

	private final User _user;
	private final HttpServletRequest _httpServletRequest;
	private final AcceptLanguage _acceptLanguage;
	private final ObjectFieldLocalService _objectFieldLocalService;
	private final DLAppService _dlAppService;
	private final UriInfo _uriInfo;
	private final DTOConverterRegistry _dtoConverterRegistry;
	private final ObjectEntryManagerRegistry _objectEntryManagerRegistry;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final WorkflowTaskManager _workflowTaskManager;
	private final BOIVkycConfiguation _config;
	private final UserHierarchy _userHierarchy;
	private final Random _random = new Random();

	public APIRequestHandler(
		User user, HttpServletRequest httpServletRequest,
		AcceptLanguage acceptLanguage,
		ObjectFieldLocalService objectFieldLocalService,
		DLAppService dlAppService, UriInfo uriInfo,
		DTOConverterRegistry dtoConverterRegistry,
		ObjectEntryManagerRegistry objectEntryManagerRegistry,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryLocalService objectEntryLocalService,
		WorkflowTaskManager workflowTaskManager,
		BOIVkycConfiguation config, String departmentId, long hoFolderId) {

		_user = user;
		_httpServletRequest = httpServletRequest;
		_acceptLanguage = acceptLanguage;
		_objectFieldLocalService = objectFieldLocalService;
		_dlAppService = dlAppService;
		_uriInfo = uriInfo;
		_dtoConverterRegistry = dtoConverterRegistry;
		_objectEntryManagerRegistry = objectEntryManagerRegistry;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryLocalService = objectEntryLocalService;
		_workflowTaskManager = workflowTaskManager;
		_config = config;
		_userHierarchy = new UserHierarchy(user, hoFolderId, departmentId, dlAppService);
	}

	public User getUser() { return _user; }
	public UserHierarchy getUserHierarchy() { return _userHierarchy; }
	public BOIVkycConfiguation getConfig() { return _config; }

	public ObjectDefinition getObjectDefinition(long objectDefinitionId) {
		try {
			return _objectDefinitionLocalService.fetchObjectDefinition(objectDefinitionId);
		} catch (Exception e) {
			return null;
		}
	}

	public com.liferay.object.model.ObjectEntry getObjectEntry(long objectEntryId) {
		try {
			return _objectEntryLocalService.fetchObjectEntry(objectEntryId);
		} catch (Exception e) {
			return null;
		}
	}

	public DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
		return new DefaultDTOConverterContext(
			_acceptLanguage.isAcceptAllLanguages(), null,
			_dtoConverterRegistry, _httpServletRequest, objectEntryId,
			_acceptLanguage.getPreferredLocale(), _uriInfo, _user);
	}

	public JSONObject convertEntryToArray(Map<String, Serializable> values) {
		if (values == null) return null;
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		values.forEach(jsonObject::put);
		return jsonObject;
	}

	// ==================== SECURE FOLDER RESOLUTION ====================

	public Folder getBranchFolderFromOrg(String branchCode) throws Exception {
		if (Validator.isNull(branchCode)) {
			throw new Exception("branchCode is required to resolve the folder.");
		}

		List<Organization> orgs = OrganizationLocalServiceUtil.getOrganizations(
			_user.getCompanyId(), 
			OrganizationConstants.ANY_PARENT_ORGANIZATION_ID,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS, 
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS
		);
		
		Organization branchOrg = null;
		for (Organization org : orgs) {
			if (org.getName().startsWith(branchCode) && "BRANCH".equalsIgnoreCase(org.getType())) {
				branchOrg = org;
				break;
			}
		}

		if (branchOrg == null) {
			throw new Exception("Organization not found for branchCode: " + branchCode);
		}

		String roName = "UNKNOWN_RO";
		if (branchOrg.getParentOrganizationId() > 0) {
			Organization roOrg = OrganizationLocalServiceUtil.getOrganization(branchOrg.getParentOrganizationId());
			roName = roOrg.getName();
		}

		FileHandler fileHandler = new FileHandler(_dlAppService);
		Folder currentFolder = fileHandler.getFolder(_config.VkycDefaultFolderId());

		if (currentFolder == null) {
			throw new Exception("VKYC Root Folder is not configured properly.");
		}

		currentFolder = fileHandler.createFolder(UserHierarchy.removeAllWhitespace(roName), currentFolder);
		if (currentFolder == null) throw new Exception("Failed to create RO folder.");

		currentFolder = fileHandler.createFolder(UserHierarchy.removeAllWhitespace(branchOrg.getName()), currentFolder);
		if (currentFolder == null) throw new Exception("Failed to create Branch folder.");

		currentFolder = fileHandler.createFolder(_config.VkycDepartmentName(), currentFolder);
		if (currentFolder == null) throw new Exception("Failed to create VKYC folder.");

		return currentFolder;
	}

	// ==================== NATIVE ADD FOLDER ====================

	public com.liferay.object.model.ObjectEntry handleAddFolderRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, long folderId) throws Exception {
		long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
		Long parentFolderIndex = ParamUtil.getLong(httpServletRequest, "parentFolderIndex");
		String folderName = ParamUtil.getString(httpServletRequest, "folderName");
		
		FileHandler fileHandler = new FileHandler(_dlAppService);
		if (Validator.isBlank(folderName)) {
			_log.warn("Folder name is blank.");
			return null;
		} 
		
		Folder parentFolder = fileHandler.getFolder(parentFolderIndex);
		if (parentFolder == null) {
			throw new Exception("ResponseCode = 304 , Message = Parent folder not found for ID: " + parentFolderIndex);
		} 
		
		Folder folder = fileHandler.createFolder(folderName, parentFolder);
		if (folder == null) {
			throw new Exception("ResponseCode = 305 , Message = There is some issue while calling add folder api .");
		} 
		
		_log.info("Created folder with ID: " + folder.getFolderId());
		
		long parentId = folder.getParentFolderId();
		String uniqueERC = parentId + "-" + folder.getFolderId() + "-" + folder.getName();
		
		com.liferay.object.model.ObjectEntry entry = null;
		try {
			DynamicQuery dynamicQuery = _objectEntryLocalService.dynamicQuery();
			dynamicQuery.add(RestrictionsFactoryUtil.eq("externalReferenceCode", uniqueERC));
			dynamicQuery.add(RestrictionsFactoryUtil.eq("objectDefinitionId", objectDefinitionId));
			
			List<com.liferay.object.model.ObjectEntry> entries = _objectEntryLocalService.dynamicQuery(dynamicQuery);
			if (entries != null && !entries.isEmpty()) {
				entry = entries.get(0);
			}
		} catch (Exception e) {
			_log.error("Error checking ERC: " + uniqueERC, e);
		}

		if (entry != null) {
			throw new Exception("ResponseCode = 302 , Message = Folder already created with this name : " + folderName + " under parent " + parentFolderIndex);
		} 
		
		httpServletRequest.setAttribute("folderIndex", folder.getFolderId());
		com.liferay.object.model.ObjectEntry objectEntry = this.getObjectEntry(entryId);
		Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap<>());
		
		// Notice there is NO branchCode mapping here. It only maps what is in the HTTP request or Object Fields.
		Map<String, Serializable> updatedValues = getDefaultData(httpServletRequest, values, objectDefinitionId, _objectFieldLocalService, null, _userHierarchy);
		
		updatedValues.put("folderIndex", folder.getFolderId());
		return this.updateData(this._user, updatedValues, status, uniqueERC, objectDefinitionId);
	}

	// ==================== NATIVE ADD DOCUMENT ====================

	public com.liferay.object.model.ObjectEntry handleProdAddDocumentRequest(HttpServletRequest httpServletRequest, MultipartBody multipartBody, int status, long objectDefinitionId, Map<String, String> filesValueUpload) throws Exception {
		long entryId = ParamUtil.getLong(httpServletRequest, "entryId", 0L);
		Long folderIndex = ParamUtil.getLong(httpServletRequest, "folderIndex");
		FileHandler fileHandler = new FileHandler(_dlAppService);
		Folder folder = fileHandler.getFolder(folderIndex);
		
		if (folder == null) {
			throw new Exception("ResponseCode = 205 , Message = No folder exists for the provided index: " + folderIndex);
		} else {
			com.liferay.object.model.ObjectEntry objectEntry = this.getObjectEntry(entryId);
			Map<String, Serializable> values = (Map<String, Serializable>)(objectEntry != null ? objectEntry.getValues() : new HashMap<>());
			
			Map<String, Serializable> var18 = getDefaultData(httpServletRequest, values, objectDefinitionId, _objectFieldLocalService, null, _userHierarchy);
			String referenceCode = objectEntry != null ? objectEntry.getExternalReferenceCode() : null;
			
			long uploadedFileId = this.uploadFileForProd(folder, var18, filesValueUpload, multipartBody, folderIndex);
			
			if (uploadedFileId == -4L) {
				throw new Exception("ResponseCode = 203 , Message = Upload failed: file with the same name already exists.");
			} else if (uploadedFileId == 0L) {
				throw new Exception("ResponseCode = 204 , Message = File upload failed. Please try again");
			} else {
				var18.put("uploadedFileId", uploadedFileId);
				var18.put("folderIndex", folderIndex);
				return this.updateData(this._user, var18, status, referenceCode, objectDefinitionId);
			}
		}
	}

	private long uploadFileForProd(Folder folder, Map<String, Serializable> values, Map<String, String> fileUploadMap, MultipartBody multipartBody, long folderIndex) {
		long uploadedFileId = 0L;
		if (multipartBody != null && folder != null) {
			FileHandler fileHandler = new FileHandler(_dlAppService);
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
							FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folder, file, fileName); 
							if (fileEntry != null) {
								uploadedFileId = fileEntry.getFileEntryId();
								values.put(valueField, _getFileEntryJson(fileEntry));
								return uploadedFileId;
							}
						}
					}
				} catch (IOException e) {
					_log.error("Error uploading file for key: " + key, e);
				}
			}
			return uploadedFileId;
		} else {
			return 0L;
		}
	}

	// ==================== FILE UTILITIES ====================

	public File createFileFromInputStream(InputStream inputStream, String fileName) throws IOException {
		String extension = "";
		int dotIndex = fileName.lastIndexOf('.');
		if ((dotIndex > 0) && (dotIndex < (fileName.length() - 1))) {
			extension = fileName.substring(dotIndex);
			fileName = fileName.substring(0, dotIndex);
		}
		File file = File.createTempFile(fileName + "_", extension);
		try (FileOutputStream out = new FileOutputStream(file)) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}
		return file;
	}

	public String getTimeStamp() {
		return new Date().getTime() + "_" + _random.nextInt(999999);
	}

	public String appendTimestamp(String fileName, String uniqueId, boolean goWithUIFileName) {
		if (goWithUIFileName) return fileName;
		String[] parts = fileName.split("\\.", 2);
		if (parts.length == 2) {
			if (Validator.isNotNull(uniqueId)) {
				return parts[0] + "_" + uniqueId + "_" + getTimeStamp() + "." + parts[1];
			}
			return parts[0] + "_" + getTimeStamp() + "." + parts[1];
		}
		return fileName;
	}

	// ==================== MANUAL UPLOAD (LEGACY) ====================

	public boolean updaloadFile(
			Map<String, Serializable> values, Map<String, String> fileUploadMap,
			MultipartBody multipartBody, HttpServletRequest httpServletRequest,
			long objectDefinitionId, String uniqueId, boolean goWithUIFileName,
			long targetFolderId) throws Exception {

		Folder folderCreated = null;

		if (targetFolderId > 0L) {
			folderCreated = new FileHandler(_dlAppService).getFolder(targetFolderId);
		}

		if (folderCreated == null) {
			folderCreated = _userHierarchy.getUploadFolder();
		}

		if ((multipartBody == null) || (folderCreated == null)) {
			_log.warn("updaloadFile: null multipart or folder");
			return false;
		}

		FileHandler fileHandler = new FileHandler(_dlAppService);
		for (Map.Entry<String, String> entry : fileUploadMap.entrySet()) {
			String key = entry.getKey();
			String valueField = entry.getValue();
			BinaryFile binaryFile = multipartBody.getBinaryFile(key);
			if (binaryFile == null) continue;

			String fileName = binaryFile.getFileName();
			if (Validator.isNull(fileName)) continue;

			String updatedName = appendTimestamp(fileName, uniqueId, goWithUIFileName);
			File file = createFileFromInputStream(binaryFile.getInputStream(), updatedName);
			FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(folderCreated, file, updatedName);

			if (fileEntry != null) {
				values.put(valueField, _getFileEntryJson(fileEntry));
			}
		}
		return true;
	}

	private String _getFileEntryJson(FileEntry entry) {
		try {
			String url = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), null, null);
			JSONObject json = JSONFactoryUtil.createJSONObject();
			json.put("URL", url);
			json.put("fileId", entry.getFileEntryId());
			json.put("fileName", entry.getFileName());
			json.put("fileExtension", entry.getExtension());
			return json.toJSONString();
		} catch (Exception e) {
			return String.valueOf(entry.getFileEntryId());
		}
	}

	// ==================== DATA ====================

	public com.liferay.object.model.ObjectEntry updateData(
			User user, Map<String, Serializable> values, int status,
			String referenceCode, long objectDefinitionId) throws Exception {

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(status);

		return _objectEntryLocalService.addOrUpdateObjectEntry(
			referenceCode, 0L, user.getUserId(), objectDefinitionId, 0L, values, serviceContext);
	}

	public static Map<String, Serializable> getDefaultData(
		HttpServletRequest httpServletRequest,
		Map<String, Serializable> values, long objectDefinitionId,
		ObjectFieldLocalService objectFieldLocalService,
		List<String> onlyWithTheseFields,
		UserHierarchy userHierarchy) {

		List<ObjectField> fields = objectFieldLocalService.getObjectFields(objectDefinitionId);

		if ((onlyWithTheseFields != null) && !onlyWithTheseFields.isEmpty()) {
			fields = fields.stream()
				.filter(field -> onlyWithTheseFields.contains(field.getName()))
				.collect(Collectors.toList());
		}

		for (ObjectField objectField : fields) {
			String fieldName = objectField.getName();
			String dbType = objectField.getDBType();

			if ("branchCode".equals(fieldName)) {
				String reqBranch = ParamUtil.getString(httpServletRequest, "branchCode", null);
				if (Validator.isNull(reqBranch)) {
					reqBranch = userHierarchy.getBranchName();
				}
				values.put(fieldName, reqBranch);
				continue;
			}
			if ("regionalOffice".equals(fieldName)) {
				String reqRo = ParamUtil.getString(httpServletRequest, "regionalOffice", null);
				if (Validator.isNull(reqRo)) {
					reqRo = userHierarchy.getRegionalOfficeName();
				}
				values.put(fieldName, reqRo);
				continue;
			}

			if (dbType.equalsIgnoreCase("String") || dbType.equalsIgnoreCase("Clob")) {
				String value = ParamUtil.getString(httpServletRequest, fieldName, StringPool.BLANK);
				if (value.equals(StringPool.BLANK) && values.containsKey(fieldName)) {
					value = (String) values.get(fieldName);
				}
				values.put(fieldName, value);
			} else if (dbType.equalsIgnoreCase("Date")) {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
				Date defaultValue = new Date();
				Date value = ParamUtil.getDate(httpServletRequest, fieldName, sdf, defaultValue);
				if (value.equals(defaultValue) && values.containsKey(fieldName)) {
					value = (Date) values.get(fieldName);
				}
				values.put(fieldName, value);
			} else if (dbType.equalsIgnoreCase("LongInteger") || dbType.equalsIgnoreCase("Long")) {
				long value = ParamUtil.getLong(httpServletRequest, fieldName, 0L);
				if ((value == 0L) && values.containsKey(fieldName)) {
					value = (long) values.get(fieldName);
				}
				values.put(fieldName, value);
			} else if (dbType.equalsIgnoreCase("Integer")) {
				int value = ParamUtil.getInteger(httpServletRequest, fieldName, 0);
				if ((value == 0) && values.containsKey(fieldName)) {
					value = (int) values.get(fieldName);
				}
				values.put(fieldName, value);
			}
		}
		return values;
	}
}