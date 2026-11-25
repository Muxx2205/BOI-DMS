package com.boi.headless.rest.common.application;

import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskResult;
import com.liferay.portal.kernel.backgroundtask.BaseBackgroundTaskExecutor;
import com.liferay.portal.kernel.backgroundtask.display.BackgroundTaskDisplay;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, configurationPid = {
		"com.boi.headless.rest.common.application.BOIWorkflowConfiguation" }, property = "background.task.executor.class.name=com.boi.headless.rest.common.application.FileExportBackgourndJob", service = BackgroundTaskExecutor.class)
public class FileExportBackgourndJob extends BaseBackgroundTaskExecutor {

	@Override
	public BackgroundTaskResult execute(BackgroundTask backgroundTask) throws Exception {

		Map<String, Serializable> taskContextMap = backgroundTask.getTaskContextMap();

		long companyId = GetterUtil.getLong(taskContextMap.get("companyId"), 0l);
		long userid = GetterUtil.getLong(taskContextMap.get("userId"), 0l);

		String screenName = GetterUtil.getString(taskContextMap.get("screenName"), "default");
		long objectDefinitionId = GetterUtil.getLong(taskContextMap.get("objectDefinitionId"), 0l);
		long folderId = GetterUtil.getLong(taskContextMap.get("folderId"), 0l);
		String statusQuery = GetterUtil.getString(taskContextMap.get("statusQuery"), "Stattatatta");
		ObjectDefinition objectDefinition = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
		User user = UserLocalServiceUtil.getUser(userid);

		Collection<com.liferay.object.rest.dto.v1_0.ObjectEntry> entries = getEntries(objectDefinition, user,
				statusQuery);
		String systemClassName = BOIDMSConstants.getDepartmentNameId(boiWorkflowConfiguation,
				objectDefinition.getObjectDefinitionId());
		UserHierarchy userHierarchy = new UserHierarchy(user, folderId, systemClassName, _dlAppService);
		if (Validator.isNotNull(entries)) {
			deleteOldJob(userid, user.getGroupId(), userHierarchy);
			jsonObject = getColumns(objectDefinitionId);
			ServiceContext serviceContext = (ServiceContext) taskContextMap.get("serviceContext");
			String url = createExcelForObjectEntry(user.getGroupId(), companyId,
					userHierarchy.getUploadFolder().getFolderId(), userid, serviceContext, entries,
					("Report_" + systemClassName.concat("_" + userid)));
			taskContextMap.put("fileURL", url);
		}

		logger.debug("companyId " + companyId);
		logger.debug("userid " + userid);
		logger.debug("screenName " + screenName);
		logger.debug("objectDefinitionId " + objectDefinitionId);
		logger.debug("folderId " + folderId);
		logger.debug("statusQuery " + statusQuery);

		return BackgroundTaskResult.SUCCESS;

	}

	@Override
	public BackgroundTaskDisplay getBackgroundTaskDisplay(BackgroundTask backgroundTask) {
		return null;
	}

	@Override
	public BackgroundTaskExecutor clone() {
		return this;
	}

	private void deleteOldJob(long userid, long groupId, UserHierarchy userHierarchy) {
		try {
			DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
			dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", userid));
			dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", groupId));
			// dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", true));
			// dynamicQuery.add(RestrictionsFactoryUtil.eq("servletContextNames",
			// "FileExportBackgourndJob"));
			dynamicQuery.add(RestrictionsFactoryUtil.eq("name", userHierarchy.getDepartmentId()));
			List<com.liferay.portal.background.task.model.BackgroundTask> backgroundTasks = BackgroundTaskLocalServiceUtil
					.dynamicQuery(dynamicQuery);
			logger.info("Old data ***************** " + backgroundTasks.size());

			for (com.liferay.portal.background.task.model.BackgroundTask backgroundTask : backgroundTasks) {
				if (backgroundTask.isCompleted()) {
					try {
						BackgroundTaskLocalServiceUtil.deleteBackgroundTask(backgroundTask.getBackgroundTaskId());

					} catch (PortalException e) {
						logger.error("Export Entry not able to delete " + backgroundTask.getBackgroundTaskId());
					}
				} else {
					throw new Exception("BackGroundTask in "+backgroundTask.getStatusLabel()+" with " + userHierarchy.getUser().getScreenName()
							+ " for " + userHierarchy.getDepartmentId());
				}
			}

		} catch (Exception e) {
			logger.error("Exception " + e.getMessage());
		}
	}

	private Collection<com.liferay.object.rest.dto.v1_0.ObjectEntry> getEntries(ObjectDefinition objectDefinition,
			User user, String filter) {
		try {
			DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(_dtoConverterRegistry,
					objectDefinition.getObjectDefinitionId(), Locale.getDefault(), null, user);

			logger.debug("calling ...");

			logger.debug("calling ...1");
			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
					.getObjectEntryManager(objectDefinition.getStorageType());

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;

			logger.debug("calling ...2");
			/*
			 * Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
			 * Sort[] sorts = new Sort[1]; sorts[0] = sort;
			 */
			logger.debug("calling ...4");
			Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
			Sort[] sorts = new Sort[1];
			sorts[0] = sort;
			page = objectEntryManager.getObjectEntries(objectDefinition.getCompanyId(), objectDefinition,
					objectDefinition.getScope(), null, dtoConverterContext, filter, Pagination.of(1, 500), null, sorts);

			logger.debug("calling ...5 == " + page.getItems().size());
			return page.getItems();
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	private FileEntry getfileEntry(long groupId, long folderId, String fileName) {
		try {
			return DLAppLocalServiceUtil.getFileEntryByFileName(groupId, folderId, fileName);
		} catch (Exception ex) {
			return null;
		}
	}

	public File writeBytesToTempFile(byte[] bytes, String prefix, String suffix) throws IOException {
		File tempFile = File.createTempFile(prefix, suffix);
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			fos.write(bytes);
		}
		return tempFile;
	}

	private String createExcelForObjectEntry(long groupId, long companyId, long folderId, long userId,
			ServiceContext serviceContext, Collection<com.liferay.object.rest.dto.v1_0.ObjectEntry> entries,
			String fileName) {

		try {

			byte[] bytes = createDownloadExcel(entries);
			DLFolder dlFolder = DLFolderLocalServiceUtil.getDLFolder(folderId);
			FileEntry fileEntry1 = getfileEntry(groupId, folderId, (fileName + ".xlsx"));
			if (Validator.isNotNull(fileEntry1)) {
				logger.debug("File Deletionnnnn..... " + folderId);
				DLAppLocalServiceUtil.deleteFileEntry(fileEntry1.getFileEntryId());
			}
			/*
			 * //
			 * 
			 * try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(bytes);
			 * logger.debug("File written to: " + file.getAbsolutePath()); } catch
			 * (IOException e) { e.printStackTrace(); }
			 */

			File file = writeBytesToTempFile(bytes, fileName, ".xlsx");

			FileEntry fileEntry = _dlAppService.addFileEntry(dlFolder.getRepositoryId(), dlFolder.getFolderId(),
					(fileName + ".xlsx"), MimeTypesUtil.getContentType((fileName + ".xlsx")), (fileName + ".xlsx"), " ",
					" ", file, serviceContext);

			if (file.exists()) {
				file.delete();
			}

			FileVersion fileVersion = fileEntry.getFileVersion();
			final long roleId = RoleLocalServiceUtil.getRole(companyId, "User").getRoleId();

			final HashMap<Long, String[]> rolesForFolder = new HashMap<>();
			rolesForFolder.put(roleId, new String[] { ActionKeys.VIEW, ActionKeys.DOWNLOAD });
			boolean permission = ResourcePermissionLocalServiceUtil.hasResourcePermission(companyId,
					DLFileEntry.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
					Long.toString(fileEntry.getFileEntryId()), roleId, ActionKeys.VIEW);

			if (!permission) {

				ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId, DLFileEntry.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL, Long.toString(fileEntry.getFileEntryId()), rolesForFolder);

			}
			String url = DLURLHelperUtil.getDownloadURL(fileEntry, fileVersion, null, "");
			// sendMail(portalURL + url, userid);
			logger.debug("url " + url);
			return url;

		} catch (Exception ex) {
			logger.error("Exception eeeeeeeeeeeeeeeee" + ex.getMessage());

		}
		return StringPool.BLANK;
	}

	public byte[] createDownloadExcel(Collection<com.liferay.object.rest.dto.v1_0.ObjectEntry> entries)
			throws PortalException {

		workbook = new SXSSFWorkbook(50000);
		sheet = workbook.createSheet("Sheet");

		logger.debug("Craeting headre");
		fillHeader(sheet);
		logger.debug("created header");
		fillOthersData(entries, sheet);
		logger.debug("data populated");
		byte[] outArray = null;
		try (ByteArrayOutputStream outByteStream = new ByteArrayOutputStream()) {

			workbook.write(outByteStream);
			outArray = outByteStream.toByteArray();

			workbook.dispose();
			workbook.close();
			// PortletResponseUtil.sendFile(resourceRequest, resourceResponse.,
			// "report.xlsx", outArray);

		} catch (IOException e) {
			logger.error(e);
		}
		return outArray;
	}

	private void fillHeader(Sheet sh) {

		String[] columns = new String[(jsonObject.length() + 1)];

		int index = 1;
		columns[0] = "S.No";
		for (String key : map.keySet()) {

			columns[index++] = jsonObject.getString(key);
		}
		org.apache.poi.ss.usermodel.Row row = null;
		Cell cell = null;

		for (int rownum = 0; rownum < 1; rownum++) {
			row = sh.createRow(rownum);

			for (int cellnum = 0; cellnum < columns.length; cellnum++) {

				cell = row.createCell(cellnum);
				cell.setCellValue(columns[cellnum]);
				sh.setColumnWidth(cellnum, 4000);
				CellStyle cellStyle = cell.getCellStyle();
				cellStyle.setWrapText(true);
				cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
				cellStyle.setLocked(true);
				cell.setCellStyle(cellStyle);
			}
		}

	}

	private String resolveStatusLabel(int status, String customStatusByHandler) {

		if (!Validator.isBlank(customStatusByHandler)) {
			return customStatusByHandler;
		}

		if (status == WorkflowConstants.STATUS_APPROVED) {
			return WorkflowConstants.LABEL_APPROVED;
		}

		if (status == WorkflowConstants.STATUS_PENDING) {
			return WorkflowConstants.LABEL_PENDING;
		}

		return WorkflowConstants.getStatusLabel(status);
	}

	private void fillOthersData(Collection<com.liferay.object.rest.dto.v1_0.ObjectEntry> entries, Sheet sh) {

		int rownum = 1;
		Row row = null;
		int cellCounter = -1;
		int rowCounter = 0;
		Cell cell = null;
		boolean rr = true;
		for (com.liferay.object.rest.dto.v1_0.ObjectEntry entry : entries) {
			row = sh.createRow(++rowCounter);
			cellCounter = -1;
			cell = row.createCell(++cellCounter);
			cell.setCellValue(rownum);
			for (String key : map.keySet()) {
				String value = "";
				if (key.contains("status")) {
					int status = entry.getStatus().getCode();
					String customStatusByHandler = (String) entry.getPropertyValue("customStatusByHandler");
					value = resolveStatusLabel(status, customStatusByHandler);

				} else if (key.contains("createDate")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					value = String.valueOf(sdf.format(entry.getDateCreated()));
				} else if (key.contains("modifiedDate")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					value = String.valueOf(sdf.format(entry.getDateModified()));
				} else if (key.contains("Date") || key.contains("date")) {
					try {

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						Date date = Date.from(Instant.parse(String.valueOf(entry.getPropertyValue(key))));

						value = String.valueOf(sdf.format(date));

					} catch (Exception e) {

						value = String.valueOf(entry.getPropertyValue(key));

					}
				} else {
					value = String.valueOf(entry.getPropertyValue(key));
				}

				cell = row.createCell(++cellCounter);
				cell.setCellValue(value);
				if (rr) {
					logger.info("Key " + key);
				}
			}
			rr = false;
			rownum++;
		}
	}

	private JSONObject getColumns(long definitionId) {
		String jsonData = StringPool.BLANK;
		logger.debug("Searching columns.." + definitionId);
		if (definitionId == boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.CPDInsuranceObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.DBDepartmentObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.DBDepartmentObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.DBDMandateObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.DBDMandateObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.UPIInsuranceObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.FBDFRBOObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.FBDFOBOObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.FBDObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.FBDObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.CustomerAccountObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.CustomerAccountObjectColumns();
		}
		if (definitionId == boiWorkflowConfiguation.FinancePloeObjectDefinitionId()) {
			jsonData = boiWorkflowConfiguation.FinancePloeObjectColumns();
		}
		logger.debug("Search Result " + Validator.isBlank(jsonData));
		if (!Validator.isBlank(jsonData)) {
			try {
				logger.info(jsonData);
				ObjectMapper mapper = new ObjectMapper();
				map = mapper.readValue(jsonData, LinkedHashMap.class);

				return JSONFactoryUtil.createJSONObject(jsonData);
			} catch (Exception e) {
				logger.error(" Error While Exporting Data " + e.getMessage());
				return null;
			}
		}
		return null;
	}

	JSONObject jsonObject = null;

	LinkedHashMap<String, Object> map = new LinkedHashMap<>();

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {

		boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
	}

	private BOIWorkflowConfiguation boiWorkflowConfiguation;

	@Reference
	private DLAppService _dlAppService;
	private SXSSFWorkbook workbook;
	private Sheet sheet;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
	private static final Log logger = LogFactoryUtil.getLog(FileExportBackgourndJob.class);
}