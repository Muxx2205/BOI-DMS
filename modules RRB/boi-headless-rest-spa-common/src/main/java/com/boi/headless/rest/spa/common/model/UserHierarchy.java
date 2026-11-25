package com.boi.headless.rest.spa.common.model;

import com.boi.headless.rest.spa.common.constants.BOIDMSConstants;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import java.util.List;

public class UserHierarchy {

	private static final Log _log = LogFactoryUtil.getLog(UserHierarchy.class);

	private final User _user;
	private final long _hoFolderId;
	private final String _departmentId;
	private final DLAppService _dlAppService;

	private String _branchName;
	private String _regionalOfficeName;
	private String _hoName;

	private Organization _branchOrganization;
	private Organization _regionalOfficeOrganization;
	private Organization _hoOrganization;

	private boolean _isMaker;
	private boolean _isBranchChecker;
	private boolean _isROMaker;
	private boolean _isROChecker;
	private boolean _isHOChecker;

	public UserHierarchy(User user, DLAppService dlAppService) {
		this(user, 0L, null, dlAppService);
	}

	public UserHierarchy(User user, long hoFolderId, String departmentId, DLAppService dlAppService) {
		_user = user;
		_hoFolderId = hoFolderId;
		_departmentId = departmentId;
		_dlAppService = dlAppService;

		if (user != null) {
			_loadUserOrganizationData(user);
			_isMaker = _hasRole(BOIDMSConstants.BRANCHMAKER);
			_isBranchChecker = _hasRole(BOIDMSConstants.BRANCHCHECKER);
			_isROMaker = _hasRole(BOIDMSConstants.ROMAKER);
			_isROChecker = _hasRole(BOIDMSConstants.ROCHECKER);
			_isHOChecker = _hasRole(BOIDMSConstants.HOCHECKER);
		}
	}

	private void _loadUserOrganizationData(User user) {
		try {
			List<Organization> organizations = user.getOrganizations();
			_branchOrganization = _getOrganization(organizations, BOIDMSConstants.BRANCH);
			_regionalOfficeOrganization = _getOrganization(organizations, BOIDMSConstants.REGIONAL_OFFICE);
			_hoOrganization = _getOrganization(organizations, BOIDMSConstants.HO);

			_branchName = (_branchOrganization != null) ? _branchOrganization.getName() : null;
			_regionalOfficeName = (_regionalOfficeOrganization != null) ? _regionalOfficeOrganization.getName() : null;
			_hoName = (_hoOrganization != null) ? _hoOrganization.getName() : null;
		} catch (Exception e) {
			_log.error("Failed to load organizations for " + user.getUserId(), e);
		}
	}

	private Organization _getOrganization(List<Organization> organizations, String type) {
		for (Organization org : organizations) {
			Organization match = _getOrganization(type, org);
			if (match != null) return match;
		}
		return null;
	}

	private Organization _getOrganization(String type, Organization organization) {
		if (organization == null) return null;
		if (type.equalsIgnoreCase(organization.getType())) return organization;

		if (organization.getParentOrganizationId() > 0) {
			try {
				return _getOrganization(type, organization.getParentOrganization());
			} catch (PortalException e) {
				return null;
			}
		}
		return null;
	}

	private boolean _hasRole(String roleName) {
		try {
			Role role = RoleLocalServiceUtil.getRole(_user.getCompanyId(), roleName);
			return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), _user.getUserId());
		} catch (Exception e) {
			return false;
		}
	}

	public User getUser() { return _user; }
	public long getUserId() { return _user.getUserId(); }
	public String getName() { return _user.getScreenName(); }
	public String getBranchName() { return _branchName; }
	public String getRegionalOfficeName() { return _regionalOfficeName; }
	public String getHoName() { return _hoName; }
	public String getDepartmentId() { return _departmentId; }
	public boolean isMaker() { return _isMaker; }
	public boolean isBranchChecker() { return _isBranchChecker; }
	public boolean isROMaker() { return _isROMaker; }
	public boolean isROChecker() { return _isROChecker; }
	public boolean isHOChecker() { return _isHOChecker; }
	public long getHoFolderId() { return _hoFolderId; }
	public DLAppService getDlAppService() { return _dlAppService; }

	public Folder getUploadFolder() {
		long folderId = _hoFolderId;
		if (folderId <= 0) {
			_log.warn("No HO folder id set for hierarchy");
			return null;
		}
		try {
			return _dlAppService.getFolder(folderId);
		} catch (Exception e) {
			_log.warn("Upload folder not found: " + folderId, e);
			return null;
		}
	}

	public static String removeAllWhitespace(String input) {
		if (input == null) return null;
		return input.replaceAll("\\s+", "");
	}
}