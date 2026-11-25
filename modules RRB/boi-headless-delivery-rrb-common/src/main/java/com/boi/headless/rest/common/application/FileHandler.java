package com.boi.headless.rest.common.application;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
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
	private static final Log log = LogFactoryUtil.getLog(FileHandler.class);
	
	
	
	
	public FileHandler(DLAppService _dlAppService) {
		super();
		this._dlAppService = _dlAppService;
	}
	 
	public   FileEntry createOrReplaceFileEntry(Folder folder,File file,String fileName) {
		try {
			/*
			 * FileEntry fileEntry= getFileEntry(folder, file, fileName);
			 * if(Validator.isNotNull(fileEntry)) { //deleteFileEntry(fileEntry); }
			 */
			return uploadFiles(folder, file, fileName);
		}catch (Exception e) {
			log.error("Error "+e.getMessage());
		}
		return null;
	}
	
	
	private FileEntry getFileEntry(Folder folder,File file,String fileName) {
		try {
		return	_dlAppService.getFileEntry(folder.getGroupId(), folder.getFolderId(), fileName);
		}catch (Exception e) {
			return null; 
		}
		
	}
	
	private    void  deleteFileEntry(FileEntry entry) {
		try {
			_dlAppService.deleteFileEntry(entry.getFileEntryId());
		}catch (Exception e) {
			return ; 
		}
		
	}
	
	
	
	
	
	private   FileEntry uploadFiles(Folder folder,File file,String fileName) {

		try {
		ServiceContext context = ServiceContextThreadLocal.getServiceContext();
		final long roleId = RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
		context.setScopeGroupId(0l);
		final HashMap<Long, String[]> roles = new HashMap<>();
		roles.put(roleId, new String[] { ActionKeys.VIEW, ActionKeys.DOWNLOAD });
		final HashMap<Long, String[]> rolesForFolder = new HashMap<>();
		rolesForFolder.put(roleId, new String[] { ActionKeys.VIEW });
		FileEntry fileEntry=null;
 
		
		 
		
		
		
		
		
		
		
		fileEntry= _dlAppService.addFileEntry(folder.getRepositoryId(), folder.getFolderId(), fileName,
				MimeTypesUtil.getContentType(fileName), fileName, " ", " ", file, context);
		 
		boolean checkPermission = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(),
				DLFileEntry.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
				Long.toString(fileEntry.getFileEntryId()), roleId, ActionKeys.VIEW);
		 
		if (!checkPermission) {
			ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(),
					DLFileEntry.class.getName(), ResourceConstants.SCOPE_INDIVIDUAL,
					Long.toString(fileEntry.getFileEntryId()), roles);
		}
		 
		return fileEntry;
		}catch (Exception e) {
			log.error("Error "+e.getMessage());
		}
		return null;
	}
	
	
	/*
	 * 
	 * 
	 * private static boolean validateUserHierarchyForFolderCreation(UserHierarchy
	 * userHierarchy) { if(Validator.isNull(userHierarchy) ||
	 * Validator.isBlank(userHierarchy.getBranchName()) ||
	 * Validator.isBlank(userHierarchy.getZoneName()) ||
	 * Validator.isBlank(userHierarchy.getNbgName())) { return false; } return true;
	 * }
	 */
	
	
	public static String removeAllWhitespace(String input) {
	    if (input == null) {
	        return null;
	    }
	    return input.replaceAll("\\s+", "");
	}
 
 
 
 
	private DLAppService _dlAppService;

}