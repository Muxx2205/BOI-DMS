package com.boi.headless.rest.spa.common.application;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.util.HashMap;

public class FileHandler {

	private static final Log _log = LogFactoryUtil.getLog(FileHandler.class);

	private final DLAppService _dlAppService;

	public FileHandler(DLAppService dlAppService) {
		_dlAppService = dlAppService;
	}

	public Folder getFolder(long folderId) {
		try {
			return _dlAppService.getFolder(folderId);
		} catch (PortalException e) {
			_log.error("Folder not found: " + folderId, e);
			return null;
		}
	}

	public Folder createFolder(String folderName, Folder parentFolder) {
		if (Validator.isBlank(folderName) || (parentFolder == null)) return null;

		try {
			return _dlAppService.getFolder(parentFolder.getRepositoryId(), parentFolder.getFolderId(), folderName);
		} catch (PortalException notFound) {
			try {
				ServiceContext context = ServiceContextThreadLocal.getServiceContext();
				if (context == null) {
					context = new ServiceContext();
					context.setCompanyId(parentFolder.getCompanyId());
					context.setScopeGroupId(parentFolder.getGroupId());
				}
				Folder newFolder = _dlAppService.addFolder(
					null, parentFolder.getRepositoryId(), parentFolder.getFolderId(), folderName, StringPool.BLANK, context);
				_grantFolderPermissions(newFolder);
				return newFolder;
			} catch (PortalException createErr) {
				_log.error("Failed to create folder '" + folderName + "'", createErr);
				return null;
			}
		}
	}

	public FileEntry createOrReplaceFileEntry(Folder folder, File file, String fileName) {
		try {
			FileEntry existing = _getFileEntry(folder, fileName);
			if (existing != null) {
				_dlAppService.deleteFileEntry(existing.getFileEntryId());
			}
			return _uploadFile(folder, file, fileName);
		} catch (Exception e) {
			_log.error("createOrReplaceFileEntry failed for " + fileName, e);
			return null;
		}
	}

	private FileEntry _getFileEntry(Folder folder, String fileName) {
		try {
			return _dlAppService.getFileEntry(folder.getGroupId(), folder.getFolderId(), fileName);
		} catch (Exception e) {
			return null;
		}
	}

	private FileEntry _uploadFile(Folder folder, File file, String fileName) throws PortalException {
		ServiceContext context = ServiceContextThreadLocal.getServiceContext();
		if (context == null) {
			context = new ServiceContext();
			context.setCompanyId(folder.getCompanyId());
		}
		FileEntry fileEntry = _dlAppService.addFileEntry(
			folder.getRepositoryId(), folder.getFolderId(), fileName,
			MimeTypesUtil.getContentType(fileName), fileName,
			StringPool.SPACE, StringPool.SPACE, file, context);
		_grantFilePermissions(fileEntry);
		return fileEntry;
	}

	private void _grantFolderPermissions(Folder folder) {
		try {
			long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
			HashMap<Long, String[]> roles = new HashMap<>();
			roles.put(roleId, new String[] {ActionKeys.VIEW, ActionKeys.ACCESS, ActionKeys.ADD_SUBFOLDER, ActionKeys.ADD_DOCUMENT});
			ResourcePermissionLocalServiceUtil.setResourcePermissions(
				folder.getCompanyId(), "com.liferay.document.library.kernel.model.DLFolder",
				ResourceConstants.SCOPE_INDIVIDUAL, Long.toString(folder.getFolderId()), roles);
		} catch (Exception e) {
			_log.warn("Grant folder permission failed for " + folder.getFolderId(), e);
		}
	}

	private void _grantFilePermissions(FileEntry fileEntry) {
		try {
			long roleId = RoleLocalServiceUtil.getRole(fileEntry.getCompanyId(), "User").getRoleId();
			HashMap<Long, String[]> roles = new HashMap<>();
			roles.put(roleId, new String[] {ActionKeys.VIEW, ActionKeys.DOWNLOAD});
			ResourcePermissionLocalServiceUtil.setResourcePermissions(
				fileEntry.getCompanyId(), DLFileEntry.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL, Long.toString(fileEntry.getFileEntryId()), roles);
		} catch (Exception e) {
			_log.warn("Grant file permission failed for " + fileEntry.getFileEntryId(), e);
		}
	}

	public static String removeAllWhitespace(String input) {
		if (input == null) return null;
		return input.replaceAll("\\s+", "");
	}
}