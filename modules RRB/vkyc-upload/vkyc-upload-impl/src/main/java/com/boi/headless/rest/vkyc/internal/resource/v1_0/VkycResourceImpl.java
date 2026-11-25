package com.boi.headless.rest.vkyc.internal.resource.v1_0;

import com.boi.headless.rest.spa.common.application.APIRequestHandler;
import com.boi.headless.rest.spa.common.application.BOIVkycConfiguation;
import com.boi.headless.rest.spa.common.constants.BOIDMSConstants;
import com.boi.headless.rest.spa.common.validators.RequestValidator;
import com.boi.headless.rest.vkyc.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.vkyc.dto.v1_0.CustomBOIHeadlessResponse;
import com.boi.headless.rest.vkyc.dto.v1_0.CustomBOIHeadlessResponseAsList;
import com.boi.headless.rest.vkyc.resource.v1_0.VkycResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
	configurationPid = "com.boi.headless.rest.spa.common.application.BOIVkycConfiguation",
	properties = "OSGI-INF/liferay/rest/v1_0/vkyc.properties",
	scope = ServiceScope.PROTOTYPE,
	service = VkycResource.class)
public class VkycResourceImpl extends BaseVkycResourceImpl {

	private static final Log _log = LogFactoryUtil.getLog(VkycResourceImpl.class);

	private BOIVkycConfiguation _config;
	private long _vkycDefaultFolderId;
	private long _vkycObjectDefinitionId;

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_config = ConfigurableUtil.createConfigurable(BOIVkycConfiguation.class, properties);
		_vkycDefaultFolderId = _config.VkycDefaultFolderId();
		_vkycObjectDefinitionId = _config.VkycObjectDefinitionId();
	}

	// =================================================================
	// API 2 — POST /vkyc/folder/create
	// =================================================================

	@Override
	public BOIHeadlessResponse createFolder(MultipartBody multipartBody) throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		try {
			APIRequestHandler apiRequestHandler = _getAPIRequestHandler();
			
			com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleAddFolderRequest(
					contextHttpServletRequest, 
					multipartBody, 
					WorkflowConstants.ACTION_PUBLISH, 
					_vkycObjectDefinitionId, 
					_vkycDefaultFolderId);

			if (Validator.isNotNull(objectEntry)) {
				jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));				jsonObject.put("values", objectEntry.getValues());
			} else {
				jsonObject.put("Error", "Failed to create folder");
			}
		} catch (Exception e) {
			_log.error("createFolder failed", e);
			jsonObject.put("Error", e.getMessage());
		}
		return new CustomBOIHeadlessResponse(jsonObject);
	}

	// =================================================================
	// API 3 — GET /vkyc/folder/search
	// =================================================================

	@Override
	public BOIHeadlessResponse searchFolders(
			String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		try {
			APIRequestHandler apiRequestHandler = _getAPIRequestHandler();
			ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(_vkycObjectDefinitionId);
			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry.getObjectEntryManager(
					objectDefinition.getCompanyId(), objectDefinition.getStorageType());

			Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
			sorts = new Sort[] {sort};

			Page<ObjectEntry> page = objectEntryManager.getObjectEntries(
				contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), null,
				apiRequestHandler.getDTOConverterContext(null), search, pagination, null, sorts);

			return new CustomBOIHeadlessResponseAsList(page, pagination);
		} catch (Exception e) {
			_log.error("searchFolders failed", e);
			jsonObject.put("Error", e.getMessage());
			return new CustomBOIHeadlessResponse(jsonObject);
		}
	}

	// =================================================================
	// API 4 — POST /vkyc/document/upload
	// =================================================================

	@Override
	public BOIHeadlessResponse uploadDocument(MultipartBody multipartBody) throws Exception {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		try {
			new RequestValidator(multipartBody, BOIDMSConstants.VKYC_UPLOAD).validateRequestFiles();

			APIRequestHandler apiRequestHandler = _getAPIRequestHandler();
			Map<String, String> fileUploadMap = new HashMap<>();
			fileUploadMap.put("vkycDocument", "imageData");

			com.liferay.object.model.ObjectEntry objectEntry = apiRequestHandler.handleProdAddDocumentRequest(
					contextHttpServletRequest, multipartBody, WorkflowConstants.ACTION_PUBLISH, _vkycObjectDefinitionId, fileUploadMap);

			if (Validator.isNotNull(objectEntry)) {
				jsonObject.put("status", WorkflowConstants.getStatusLabel(objectEntry.getStatus()));				Map<String, Serializable> values = new HashMap<>(objectEntry.getValues());
				jsonObject.put("values", values);
			} else {
				jsonObject.put("Error", "File upload failed");
			}
		} catch (Exception e) {
			_log.error("uploadDocument failed", e);
			jsonObject.put("Error", e.getMessage());
		}
		return new CustomBOIHeadlessResponse(jsonObject);
	}

	// =================================================================
	// API 5 — GET /vkyc/document/search
	// =================================================================

	@Override
	public BOIHeadlessResponse searchDocuments(
			String search, Filter filter, Pagination pagination, Sort[] sorts) throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		try {
			APIRequestHandler apiRequestHandler = _getAPIRequestHandler();
			ObjectDefinition objectDefinition = apiRequestHandler.getObjectDefinition(_vkycObjectDefinitionId);
			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry.getObjectEntryManager(
					objectDefinition.getCompanyId(), objectDefinition.getStorageType());

			Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
			sorts = new Sort[] {sort};

			Page<ObjectEntry> page = objectEntryManager.getObjectEntries(
				contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(), null,
				apiRequestHandler.getDTOConverterContext(null), search, pagination, null, sorts);

			return new CustomBOIHeadlessResponseAsList(page, pagination);
		} catch (Exception e) {
			_log.error("searchDocuments failed", e);
			jsonObject.put("Error", e.getMessage());
			return new CustomBOIHeadlessResponse(jsonObject);
		}
	}

	// =================================================================
	// API 6 — GET /vkyc/document/download/{fileEntryId}
	// =================================================================

	@Override
	public Response downloadDocument(Long fileEntryId) throws Exception {
		if ((fileEntryId == null) || (fileEntryId <= 0)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("fileEntryId is required").build();
		}

		try {
			com.liferay.portal.kernel.repository.model.FileEntry entry = _dlAppService.getFileEntry(fileEntryId);
			String fileName = entry.getFileName();
			String mimeType = entry.getMimeType();
			long size = entry.getSize();
			java.io.InputStream inputStream = entry.getContentStream();

			StreamingOutput stream = output -> {
				try (var in = inputStream) {
					byte[] buffer = new byte[8192];
					int n;
					while ((n = in.read(buffer)) != -1) {
						output.write(buffer, 0, n);
					}
					output.flush();
				}
			};

			return Response.ok(stream, mimeType)
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.header("Content-Length", String.valueOf(size))
				.build();
		} catch (Exception e) {
			_log.error("downloadDocument failed", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity("Download failed: " + e.getMessage()).build();
		}
	}

	// =================================================================
	// Helpers
	// =================================================================

	private APIRequestHandler _getAPIRequestHandler() {
		return new APIRequestHandler(
			contextUser, contextHttpServletRequest, contextAcceptLanguage,
			_objectFieldLocalService, _dlAppService, contextUriInfo,
			_dtoConverterRegistry, _objectEntryManagerRegistry,
			_objectDefinitionLocalService, _objectEntryLocalService,
			_workflowTaskManager, _config, _config.VkycDepartmentName(), _vkycDefaultFolderId);
	}

	// ==================== OSGi references ====================

	@Reference private DLAppService _dlAppService;
	@Reference private DTOConverterRegistry _dtoConverterRegistry;
	@Reference private ObjectDefinitionLocalService _objectDefinitionLocalService;
	@Reference private ObjectEntryLocalService _objectEntryLocalService;
	@Reference private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
	@Reference private ObjectFieldLocalService _objectFieldLocalService;
	@Reference private WorkflowTaskManager _workflowTaskManager;
}