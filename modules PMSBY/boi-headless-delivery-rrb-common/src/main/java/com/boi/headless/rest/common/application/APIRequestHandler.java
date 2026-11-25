package com.boi.headless.rest.common.application;

import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.rest.common.model.WorkflowLogs;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Order;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalServiceUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTransition;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalServiceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public class APIRequestHandler {
	private static final String PARAM_FROM_DATE = "fromDate";
	private static final String PARAM_TO_DATE = "toDate";
	private static final String PARAM_STATUS = "status";

	public com.liferay.object.model.ObjectEntry getObjectEntry(long objectEntryId) {
		try {
			return _objectEntryLocalService.fetchObjectEntry(objectEntryId);
		} catch (Exception exx) {
			return null;
		}
	}

	public UserHierarchy getUserHierarchy() {
		return userHierarchy;
	}

	public void checkPermission(String templateName) throws Exception {
		boolean isTemplateAccess = hasRole(templateName);
		if (!isTemplateAccess) {
			throw new Exception("Access Denied!");
		}
	}

	public void exportToExcelData(String statusQuery, long objectDefinitionid, long folderId) {

		Map<String, Serializable> taskContextMap = new HashMap<String, Serializable>();
		ServiceContext serviceContext = new ServiceContext();

		taskContextMap.put("companyId", user.getCompanyId());
		taskContextMap.put("statusQuery", statusQuery);
		taskContextMap.put("userId", user.getUserId());
		taskContextMap.put("serviceContext", serviceContext);
		taskContextMap.put("screenName", user.getScreenName());
		taskContextMap.put("objectDefinitionId", objectDefinitionid);
		taskContextMap.put("folderId", folderId);

		try {
			// deleteOldTask(objectDefinitionid);
			log.debug("invoking  MyBackgroundTaskExecutor");
			BackgroundTask backgroundTask = BackgroundTaskLocalServiceUtil.addBackgroundTask(user.getUserId(),
					user.getGroupId(), userHierarchy.getDepartmentId(), null, FileExportBackgourndJob.class,
					taskContextMap, serviceContext);

			// backgroundTask =
			// BackgroundTaskLocalServiceUtil.fetchBackgroundTask(backgroundTask.getBackgroundTaskId());

			// BackgroundTaskManagerUtil.triggerBackgroundTask(backgroundTask.getBackgroundTaskId());

			log.debug("End  MyBackgroundTaskExecutor" + backgroundTask.getBackgroundTaskId());

		} catch (PortalException e1) {
			log.error(e1.getMessage());
		}

	}

	private void deleteOldTask(long objectDefinitionId) {
		List<BackgroundTask> backgroundTasks = getBackgroundTasks(objectDefinitionId);
		for (BackgroundTask backgroundTask : backgroundTasks) {
			if (backgroundTask.isCompleted()) {
				try {
					BackgroundTaskLocalServiceUtil.deleteBackgroundTask(backgroundTask.getBackgroundTaskId());
				} catch (PortalException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	private List<BackgroundTask> getBackgroundTasks(long objectDefinitionId) {
		DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
		dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", user.getUserId()));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", user.getGroupId()));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("name", userHierarchy.getDepartmentId()));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", true));
		Order order = OrderFactoryUtil.desc("modifiedDate");
		dynamicQuery.addOrder(order);
		return BackgroundTaskLocalServiceUtil.dynamicQuery(dynamicQuery);
	}

	public boolean isHaveExportToRole() {
		if (!boiWorkflowConfiguation.isExportToExcel()) {
			return false;
		}

		if (userHierarchy.isROMaker() || userHierarchy.isHOChecker() || userHierarchy.isROChecker()) {
			return true;
		}

		Role role = getRole("Export Excel");

		return Validator.isNotNull(role) && UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
	}

	public String getTaskComplatedURL(long objectDefinitionid) {

		try {

			List<BackgroundTask> backgroundTasks = getBackgroundTasks(objectDefinitionid);
			log.debug(" backgroundTasks : " + (backgroundTasks == null));
			if (Validator.isNotNull(backgroundTasks) && backgroundTasks.size() > 0) {
				BackgroundTask backgroundTask = backgroundTasks.get(0);
				if (backgroundTask.isCompleted()) {
					String fileType = (String) backgroundTask.getTaskContextMap().get("fileURL");
					return Validator.isNotNull(fileType) ? fileType : StringPool.BLANK;
				}
				log.debug(backgroundTask.isInProgress() + " : " + backgroundTask.getStatusLabel() + " --- "
						+ backgroundTask.isNew() + " --- " + backgroundTask.getBackgroundTaskId());

				return backgroundTask.getStatusLabel();

			}

		} catch (Exception w) {
			w.printStackTrace();
			return StringPool.BLANK;
		}

		return StringPool.BLANK;
	}

	public String getUserFilterQuery() {
		List<String> queryList = new LinkedList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Date toDate = ParamUtil.getDate(contextHttpServletRequest, PARAM_TO_DATE, sdf, null);
		Date fromDate = ParamUtil.getDate(contextHttpServletRequest, PARAM_FROM_DATE, sdf, null);
		String status = ParamUtil.getString(contextHttpServletRequest, PARAM_STATUS, null);
		String typeOfRemittance = ParamUtil.getString(contextHttpServletRequest, "typeOfRemittance", null);

		addHierarchyFilter(queryList);
		addStatusFilter(queryList, status);
		addDateFilter(queryList, fromDate, toDate, sdf);
		addTypeOfRemittance(typeOfRemittance, queryList);
		String queryResult = queryList.stream().filter(Validator::isNotNull).filter(x -> !Validator.isBlank(x))
				.collect(Collectors.joining(" and "));

		if (containsHierarchyFilters(queryResult) || userHierarchy.isHOChecker()) {
			log.debug("Final queryResult:  " + queryResult);
			return queryResult;
		}

		log.debug("Final queryResult:  Blank");
		return StringPool.BLANK;
	}

	private void addTypeOfRemittance(String typeOfRemittance, List<String> queryList) {
		if (!Validator.isBlank(typeOfRemittance)) {
			queryList.add("typeOfRemittance eq '" + typeOfRemittance + "'");
		}
	}

	private void addHierarchyFilter(List<String> queryList) {
		if (!Validator.isBlank(userHierarchy.getBranchName())) {
			queryList.add("branchCode eq '" + userHierarchy.getBranchName() + "'");
			return;
		}
		if (!Validator.isBlank(userHierarchy.getRegionalOfficeName())) {
			queryList.add("regionalOffice eq '" + userHierarchy.getRegionalOfficeName() + "'");
			return;
		}
		
	}

	private int getStageInt(List<String> queryList, String status) {

		if (status.equalsIgnoreCase("draft") && (userHierarchy.isMaker())) {
			queryList.add("status eq " + WorkflowConstants.STATUS_DRAFT);
		}
		if (status.equalsIgnoreCase("denied")) {
			queryList.add("stage eq 1");
			queryList.add("status eq " + WorkflowConstants.STATUS_APPROVED);
		}
		if (status.equalsIgnoreCase("Pending with FEBO")) {
			queryList.add("stage eq 2");
			queryList.add("status eq " + WorkflowConstants.STATUS_APPROVED);
		}
		if (status.equalsIgnoreCase("Pending with swift cell")) {
			queryList.add("stage eq 3");
			queryList.add("status eq " + WorkflowConstants.STATUS_APPROVED);
		}
		if (status.equalsIgnoreCase("approved")) {
			queryList.add("stage eq 4");
			// queryList.add("stage eq 4 or stage eq 0");
			queryList.add("status eq " + WorkflowConstants.STATUS_APPROVED);
		}

		return 999;
	}

	private void addStatusFilter(List<String> queryList, String status) {
		log.debug("Request required status " + status);
		
		if (!userHierarchy.isMaker() && Validator.isNull(status)) {
			
				queryList.add("status ne " + WorkflowConstants.STATUS_DRAFT);
		}

	}
	 
	private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
		if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
			Map<String, String> isoRange = convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
			String dateFilter = "dateModified gt " + isoRange.get(PARAM_FROM_DATE) + " and dateModified lt "
					+ isoRange.get(PARAM_TO_DATE);
			log.debug("Request required dateFilter  " + dateFilter);
			queryList.add(dateFilter);
		}
	}

	private boolean containsHierarchyFilters(String queryResult) {
		return queryResult.contains("branchCode") ||queryResult.contains("regionalOffice");
	}

	public Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
		DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;

		// Parse the dates
		LocalDate fromDate = LocalDate.parse(fromDateStr); // e.g., "2025-07-21"
		LocalDate toDate = LocalDate.parse(toDateStr);

		// Convert to ZonedDateTime in UTC
		ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC); // 00:00:00Z
		ZonedDateTime toDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC); // 23:59:59Z

		Map<String, String> result = new HashMap<>();
		result.put("fromDate", isoFormatter.format(fromDateTime));
		result.put("toDate", isoFormatter.format(toDateTime));

		return result;
	}

	public JSONObject convertEntryToArray(Map<String, Serializable> values) {
		if (Validator.isNull(values)) {
			return null;
		}

		JSONObject jsonArray = JSONFactoryUtil.createJSONObject();

		values.forEach((key, value) -> {
			if (key.endsWith("Detailed")) {
				handleDetailedEntry(jsonArray, key, value);
			} else {
				jsonArray.put(key, value);
			}
		});

		return jsonArray;
	}

	private void handleDetailedEntry(JSONObject jsonArray, String key, Serializable value) {
		try {
			JSONObject detailedObject = JSONFactoryUtil.createJSONObject((String) value);
			  
			
			if ("0".equals(detailedObject.getString(key + "From")) &&
				    "0".equals(detailedObject.getString(key + "To"))) {
             log.info(detailedObject);
				 return;
				}

			if (Validator.isNotNull(detailedObject) && detailedObject.length() > 0) {
				addAllKeysToJson(jsonArray, detailedObject);
			} else {
				jsonArray.put(key, value);
			}
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
	}

	private void addAllKeysToJson(JSONObject target, JSONObject source) {
		for (String sourceKey : source.keySet()) {
			target.put(sourceKey, source.get(sourceKey));
		}
	}

	public List<WorkflowTask> getWorkflowTasks(ObjectDefinition objectDefinition, User user, long classPk) {
		try {
			return _workflowTaskManager
					.searchWorkflowTasks(objectDefinition.getCompanyId(), user.getUserId(), null, null,
							new String[] { objectDefinition.getClassName() }, new Long[] { classPk },
							"com.liferay.portal.kernel.model.User", new Long[] { user.getUserId() }, null, null, false,
							false, false, null, null, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)
					.getWorkflowModels();
		} catch (WorkflowException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public List<WorkflowTransition> getWorkflowTransitions(long workflowTaskId) {
		try {
			return _workflowTaskManager.getWorkflowTaskWorkflowTransitions(workflowTaskId);
		} catch (WorkflowException e) {

			log.error(e.getMessage());
		}
		return null;
	}

	public ObjectDefinition getObjectDefinition(long objectDefinitionId) {
		try {
			return _objectDefinitionLocalService.fetchObjectDefinition(objectDefinitionId);
		} catch (Exception e) {
			return null;
		}
	}

	public DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {

		return new DefaultDTOConverterContext(contextAcceptLanguage.isAcceptAllLanguages(), null, _dtoConverterRegistry,
				contextHttpServletRequest, objectEntryId, contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				user);
	}

	public void addWorkflowLogs(JSONObject jsonObject, long objectEntryId, long objectDefinitionid) {
		Page<ObjectEntry> page2 = null;
		ObjectDefinition workflowlogs = _objectDefinitionLocalService.fetchObjectDefinition(objectDefinitionid);
		ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
				.getObjectEntryManager(workflowlogs.getStorageType());
		String query = "objectEntryId eq " + objectEntryId;
		try {
			Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
			Sort[] sorts = new Sort[1];
			sorts[0] = sort;

			page2 = objectEntryManager.getObjectEntries(workflowlogs.getCompanyId(), workflowlogs,
					workflowlogs.getScope(), null, getDTOConverterContext(null), query, Pagination.of(-1, -1), null,
					sorts);

			jsonObject.put("logs", new WorkflowLogs().getArray(page2));

		} catch (Exception e) {

			log.error(e.getMessage());
		}

	}

	public WorkflowTask getWorkflowTask(long workflowTaskId) {
		try {
			long companyId = CompanyThreadLocal.getCompanyId();
			return WorkflowTaskManagerUtil.getWorkflowTask(companyId, workflowTaskId);
		} catch (WorkflowException e) {
			return null;
		}
	}

	public File createFileFromInputStream(InputStream inputStream, String fileName) throws IOException {

		// Extract extension (e.g., .pdf, .jpg)
		String extension = "";
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
			extension = fileName.substring(dotIndex); // includes the "."
			fileName = fileName.substring(0, dotIndex); // prefix without extension
		}

		// Create temp file with proper extension
		File file = File.createTempFile(fileName + "_", extension);

		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}

		return file;

		/*
		 * try { File file = File.createTempFile(fileName, null); try (FileOutputStream
		 * outputStream = new FileOutputStream(file)) { byte[] buffer = new byte[8192];
		 * int bytesRead; while ((bytesRead = inputStream.read(buffer)) != -1) {
		 * outputStream.write(buffer, 0, bytesRead); } } return file; } catch (Exception
		 * e) { return null; }
		 */
	}

	public File uploadAndGetFile(File uploadFile) throws IOException {
		String boundary = "----Boundary" + System.currentTimeMillis();
		String LINE_FEED = "\r\n";

		log.debug("Preparing connection to server...");

		URL url = new URL("http://localhost:8800/show");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", "image/jpeg");
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		log.debug("Connection initialized with URL: {}" + url);

		try (OutputStream outputStream = conn.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

			String fileName = uploadFile.getName();
			log.debug("Attaching file: {}" + fileName);

			writer.append("--" + boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"")
					.append(LINE_FEED);
			writer.append("Content-Type: application/pdf").append(LINE_FEED);
			writer.append(LINE_FEED);
			writer.flush();

			log.debug("Writing file content to request...");
			try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			}
			outputStream.flush();
			log.debug("File upload complete.");

			writer.append(LINE_FEED).flush();
			writer.append("--" + boundary + "--").append(LINE_FEED);
			writer.close();
		}

		int responseCode = conn.getResponseCode();
		log.debug("Server responded with status code: {}" + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) {
			File outputFile = File.createTempFile("result_", ".jpg");
			try (InputStream responseStream = conn.getInputStream();
					FileOutputStream fileOut = new FileOutputStream(outputFile)) {

				log.debug("Saving server response to: {}" + outputFile.getAbsolutePath());

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = responseStream.read(buffer)) != -1) {
					fileOut.write(buffer, 0, bytesRead);
				}
			}

			conn.disconnect();
			log.debug("Request finished successfully. Output saved at: {}" + outputFile.getAbsolutePath());
			return outputFile;
		} else {
			log.error("API call failed with status: {}" + responseCode);

			try (InputStream errorStream = conn.getErrorStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					log.error("Error Response: {}" + line);
				}
			}

			conn.disconnect();
			return null;
		}
	}

	public boolean updaloadFile(Map<String, Serializable> values, Map<String, String> fileUploadMap,
			MultipartBody multipartBody, HttpServletRequest httpServletRequest, long objectDefinitionId,
			String uniqueId, boolean goWithUIFileName) throws Exception {

		Folder folderCreated = getFolder(getObjectDefinition(objectDefinitionId));
		log.info("folder status " + (folderCreated == null));

		if (Validator.isNull(multipartBody) || Validator.isNull(folderCreated)) {
			return false;
		}

		log.info("Going to upload file for " + fileUploadMap.size() + " in folder id " + folderCreated.getFolderId());

		FileHandler fileHandler = new FileHandler(_dlAppService);
		List<Exception> errors = new ArrayList<>();
		fileUploadMap.forEach((key, value) -> {

			try {
				processSingleFile(key, value, multipartBody, fileHandler, folderCreated, values, httpServletRequest,
						uniqueId, goWithUIFileName);
			} catch (Exception e) {
				errors.add(new RuntimeException("Error processing file: " + key, e));
			}

		});
		if (!errors.isEmpty()) {
			throw new RuntimeException("Multiple file upload errors: " + errors.size(), errors.get(0));
		}
		return true;
	}

	private final Random random = new Random();

	public String getTimeStamp() {
		return new Date().getTime() + "_" + random.nextInt(999999);
	}

	public String appendTimestamp(String fileName, String uniqueId, boolean goWithUIFileName) throws Exception {

		if (goWithUIFileName) {
			return fileName;
		}
		String[] parts = fileName.split("\\.", 2);

		if (parts.length == 2) {
			String name = parts[0];
			String extension = parts[1];

			if (Validator.isNotNull(uniqueId)) {
				return name + "_" + uniqueId + "_" + getTimeStamp() + "." + extension;
			}

			return name + "_" + getTimeStamp() + "." + extension;
		} else {
			throw new Exception("File Have not extenssion");
		}
	}

	private void processSingleFile(String key, String value, MultipartBody multipartBody, FileHandler fileHandler,
			Folder folderCreated, Map<String, Serializable> values, HttpServletRequest httpServletRequest,
			String uniqueId, boolean goWithUIFileName) throws Exception {

		BinaryFile binaryFile = multipartBody.getBinaryFile(key);
		log.debug("Searching file for  : " + key + " : " + (binaryFile == null));

		if (Validator.isNull(binaryFile)) {
			return;
		}

		String fileName = binaryFile.getFileName();
		log.debug("Received file name isNull " + (fileName == null));

		String updatedFileName = appendTimestamp(fileName, uniqueId, goWithUIFileName);

		File file = createFileFromInputStream(binaryFile.getInputStream(), updatedFileName);
		log.debug("Convert into file stream  " + (file == null));
		log.debug("fileName " + updatedFileName);

		if (Validator.isNull(file)) {
			return;
		}

		/*
		 * if(updatedFileName.contains("Aadhar")) { try {
		 * log.debug("Adhar Recieved "+updatedFileName); File
		 * fileAdhar=uploadAndGetFile(file); if(Validator.isNotNull(fileAdhar)) {
		 * file=fileAdhar; updatedFileName =
		 * appendTimestamp(file.getName(),uniqueId,goWithUIFileName); } }catch
		 * (Exception e) { log.debug("Error from Adhar call "+e.getMessage());
		 * e.printStackTrace(); log.debug("Error from Adhar call "+e.getMessage()); } }
		 */

		FileEntry entry = fileHandler.createOrReplaceFileEntry(folderCreated, file, updatedFileName);
		log.debug(" file isNotSaved  " + (entry == null));

		if (Validator.isNull(entry)) {
			return;
		}
//		if(file.exists()) {
//			file.delete();
//		}

		try {
			if(values.containsKey(value) && (!Validator.isBlank((String) values.get(value)))) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject((String) values.get(value));
			String fileEntryId = jsonObject.getString(value.concat("EntryId"));
			log.debug(value + "************Old File Entry**********  " + (fileEntryId)
					+ "************ New File Entry**********  " + (entry.getFileEntryId()));
			_dlAppService.deleteFileEntry(Long.parseLong(fileEntryId));
			}
		} catch (Exception e) {
			log.error(" file switch  " +e);
		}

		updateValuesWithFileEntry(values, value, entry, httpServletRequest);

	}

	private void updateValuesWithFileEntry(Map<String, Serializable> values, String value, FileEntry entry,
			HttpServletRequest httpServletRequest) {

		String fileEntryKey = getKeyOfFileEntryId(value);

		values.put(fileEntryKey, entry.getFileEntryId());
		log.debug("Putting values of key  " + fileEntryKey + " : " + entry.getFileEntryId());

		values.put(value, getExtraFieldsForCustomerAccount(httpServletRequest, value, entry));
		log.debug("Json formatted in  " + value + " : " + entry.getFileEntryId());
	}

	private String getKeyOfFileEntryId(String detailsKey) {

		if (detailsKey.contains("Detailed")) {
			String[] keys = detailsKey.split("Detailed");
			return keys[0].concat("FileEntryId");
		}

		return null;
	}

	private String getExtraFieldsForCustomerAccount(HttpServletRequest httpServletRequest, String value,
			FileEntry entry) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		List<String> valuess = new ArrayList<String>() {

			private static final long serialVersionUID = 1L;

			{
				add("To");
				add("From");
				add("SheetType");
				add("URL");
				add("EntryId");
			}
		};

		valuess.forEach(s -> {
			String key = value.concat(s);
			String values = StringPool.BLANK;
			if (s.equalsIgnoreCase("URL")) {
				updateURL(key, entry, jsonObject);
			} else if (s.equalsIgnoreCase("EntryId")) {
				jsonObject.put(key, entry.getFileEntryId());
			} else {
				values = ParamUtil.getString(httpServletRequest, key, StringPool.BLANK);
				jsonObject.put(key, values);
			}

		});

		return jsonObject.toJSONString();

	}

	private void updateURL(String key, FileEntry entry, JSONObject jsonObject) {
		try {
			String downloadURL = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), null, null);
			jsonObject.put(key, downloadURL);
		} catch (PortalException e) {
			log.error(e.getMessage());
		}
	}

	public User getUser() {
		return user;
	}

	public ObjectEntryManagerRegistry get_objectEntryManagerRegistry() {
		return _objectEntryManagerRegistry;
	}

	public BOIWorkflowConfiguation getBoiWorkflowConfiguation() {
		return boiWorkflowConfiguation;
	}

	public com.liferay.object.model.ObjectEntry updateData(User user, Map<String, Serializable> values, int status,
			String referanceCode, long objectDefinitionId) throws Exception {

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setUserId(user.getUserId());
		serviceContext.setWorkflowAction(status);
		com.liferay.object.model.ObjectEntry objectEntry = _objectEntryLocalService.addOrUpdateObjectEntry(
				referanceCode, user.getUserId(), 0l, objectDefinitionId, values, serviceContext);
		return objectEntry;

	}

	private boolean loadHOChecker() {
		Role role = getRole(BOIDMSConstants.HOCHECKER);
		if (Validator.isNotNull(role)) {
			return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
		}
		return false;
	}

	public boolean hasRole(String roleName) {
		Role role = getRole(roleName);
		if (Validator.isNotNull(role)) {
			return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
		}
		return false;
	}

	private Role getRole(String roleName) {
		try {
			Role role = RoleLocalServiceUtil.getRole(user.getCompanyId(), roleName);
			return role;
		} catch (PortalException e) {
			return null;
		}

	}

	private Folder getFolder(ObjectDefinition objectDefinition) {
		String systemClassName = BOIDMSConstants.getDepartmentNameId(boiWorkflowConfiguation,
				objectDefinition.getObjectDefinitionId());
		Organization organization = userHierarchy.getUserOrganization();
		log.debug("looking for organization Id " + organization);
		if (Validator.isNull(organization)) {
			return null;
		}
		log.debug("found organization name " + organization.getName());
		String query = buildFilter(systemClassName, organization.getOrganizationId(), user.getScreenName());
		log.debug("looking for folder Id from master " + query);
		Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = getUserDeparments(query, user);
		// log.debug("Matched Entries master " + page.getTotalCount());
		long folderId = getMasterFolderId(page);
		log.debug("folder ID found : " + folderId);
		Folder folder = null;
		if (folderId > 0l) {
			try {
				folder = DLAppServiceUtil.getFolder(folderId);
			} catch (Exception e) {
				folder = null;
			}
		}
		if (Validator.isNull(folder)) {
			folder = userHierarchy.getUploadFolder();
		}

		return folder;
	}

	private long getMasterFolderId(Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page) {
		if (Validator.isNotNull(page) && page.getTotalCount() > 0) {
			for (com.liferay.object.rest.dto.v1_0.ObjectEntry item : page.getItems()) {
				Map<String, Object> map = item.getProperties();
				if (map.containsKey("folderId")) {
					return Long.parseLong(String.valueOf(map.get("folderId")));
				}
			}
		}
		return 0L;
	}

	private String buildFilter(String systemClassName, long organizationId, String screenName) {
		List<String> filters = new ArrayList<>();
		filters.add("departmentId eq '" + systemClassName + "'");
		filters.add("organizationId eq " + organizationId);
		filters.add("userScreenName eq '" + screenName + "'");

		return filters.stream().filter(Validator::isNotNull).filter(x -> !Validator.isBlank(x))
				.collect(Collectors.joining(" and "));
	}

	private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> getUserDeparments(String query, User user) {
		try {
			ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil
					.getObjectDefinition(boiWorkflowConfiguation.UserDepartmentObjectDefinitionId());

			DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(_dtoConverterRegistry,
					userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), null, user);

			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
					.getObjectEntryManager(userDepartmentObject.getStorageType());

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;

			page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject,
					userDepartmentObject.getScope(), null, dtoConverterContext, query, Pagination.of(-1, -1), null,
					null);
			log.debug("List from user deaprtment " + page.getTotalCount());
			return page;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	private static final Log log = LogFactoryUtil.getLog(APIRequestHandler.class);

	private User user;

	private UserHierarchy userHierarchy;

	private HttpServletRequest contextHttpServletRequest;

	private AcceptLanguage contextAcceptLanguage;

	private DLAppService _dlAppService;

	private UriInfo contextUriInfo;

	private DTOConverterRegistry _dtoConverterRegistry;

	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectEntryLocalService _objectEntryLocalService;
	private BOIWorkflowConfiguation boiWorkflowConfiguation;

	private WorkflowTaskManager _workflowTaskManager;

	public APIRequestHandler(User user, HttpServletRequest contextHttpServletRequest,
			AcceptLanguage contextAcceptLanguage, ObjectFieldLocalService _objectFieldLocalService,
			DLAppService _dlAppService, UriInfo contextUriInfo, DTOConverterRegistry _dtoConverterRegistry,
			ObjectEntryManagerRegistry _objectEntryManagerRegistry,
			ObjectDefinitionLocalService _objectDefinitionLocalService,
			ObjectEntryLocalService _objectEntryLocalService, WorkflowTaskManager _workflowTaskManager,
			BOIWorkflowConfiguation boiWorkflowConfiguation, String departmentId, long hoFolderId) {
		this.user = user;
		this.contextHttpServletRequest = contextHttpServletRequest;
		this.contextAcceptLanguage = contextAcceptLanguage;

		this._dlAppService = _dlAppService;
		this.contextUriInfo = contextUriInfo;
		this._dtoConverterRegistry = _dtoConverterRegistry;
		this._objectEntryManagerRegistry = _objectEntryManagerRegistry;
		this._objectDefinitionLocalService = _objectDefinitionLocalService;
		this.boiWorkflowConfiguation = boiWorkflowConfiguation;
		this._objectEntryLocalService = _objectEntryLocalService;
		userHierarchy = new UserHierarchy(user, hoFolderId, departmentId, _dlAppService);
		this._workflowTaskManager = _workflowTaskManager;

	}

}
