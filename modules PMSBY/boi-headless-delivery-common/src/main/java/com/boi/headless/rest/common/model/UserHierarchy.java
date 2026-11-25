package com.boi.headless.rest.common.model;

import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserHierarchy {

	private String name;
	private long userId;
	private String emailId;
	private String fullName;

	private String departmentId;

	private String branchName;
	private String zoneName;
	private String nbgName;
	private String hoName;
	private String zCode;

	private Organization branchOrganization;
	private Organization zoneOrganization;
	private Organization nbgOrganization;
	private Organization hoOrganization;

	private User user;

	private long branchFolderId;
	private long zoneFolderId;
	private long nbgFolderId;
	private long hoFolderId;
	private long departmentFolderId;
	private boolean isMaker = false;
	private boolean isBrachChecker = false;

	private boolean isZoneChecker = false;

	private boolean isZoneMaker = false;

	public String getURN() {
		String branchName = getBranchName();
		String zoneName = getZoneName();
		String nbgName = getNbgName();

		folders = new ArrayList<String>() {
			{
				String date = DateUtil.getDate(new Date(), "ddMMyyyy", Locale.getDefault());
				add(date);
				if (Validator.isNotNull(nbgName)) {
					add(nbgName);
				}
				if (Validator.isNotNull(zoneName)) {
					add(zoneName);
				}
				if (Validator.isNotNull(branchName)) {
					add(branchName);
				}
				add(String.valueOf(CounterLocalServiceUtil.increment()));
			}
		};
		String lastFolderName = folders.stream().filter(name -> !Validator.isBlank(name))
				.collect(Collectors.joining("-"));
		return lastFolderName;
	}

	public String getZCode() {
		if (Validator.isNotNull(zoneOrganization)) {
			return zoneOrganization.getComments();
		}
		return StringPool.BLANK;
	}

	public UserHierarchy(User user, long hoFolderId, String departmentId, DLAppService dlAppService) {
		this.user = user;
		this._dlAppService = dlAppService;
		this.departmentId = departmentId;
		this.hoFolderId = hoFolderId;
		name = user.getScreenName();
		userId = user.getUserId();
		emailId = user.getEmailAddress();
		fullName = user.getFullName();
		loadUserOrganizationData(user);
		isBranchMaker();
		loadBrachChecker();
		loadZoneMaker();
		loadZoneChecker();
		loadHOChecker();
		 
	}
	public boolean isGandhiNagarUser() {
		Role role;
		try {
			role = RoleLocalServiceUtil.getRole(user.getCompanyId(), this.roleGandhiNager);
			return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
		} catch (PortalException e) {
			return false;
		}

	}
	private String roleGandhiNager = "Gandhi Nagar";
	private String roleSwiftcell = "Swift Cell";
	public boolean isSwiftCellUser() {
		Role role;
		try {
			role = RoleLocalServiceUtil.getRole(user.getCompanyId(), this.roleSwiftcell);
			return UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId());
		} catch (PortalException e) {
			return false;
		}

	}
	
	private long getUploadFolderId() {
		long folderReturn = 0l;
		if (nbgFolderId != 0l) {
			folderReturn = nbgFolderId;

		}
		if (zoneFolderId != 0l) {
			folderReturn = zoneFolderId;

		}
		if (branchFolderId != 0l) {
			folderReturn = branchFolderId;

		}
		if (departmentFolderId != 0l) {
			folderReturn = departmentFolderId;

		}
		if (folderReturn == 0l) {
			return hoFolderId;

		}
		return folderReturn;
	}

	public Folder getUploadFolder() {
		loadFolderStructure();
		
		long folderReturn = getUploadFolderId();

		if (folderReturn == 0l) {
			return getFolder(hoFolderId);
		}
		return getFolder(folderReturn);
	}

	private void loadFolderStructure() {
		try {
			Folder nBGFolder = createFolder(removeAllWhitespace(nbgName), hoFolderId);
			Folder zoneFolder = Validator.isNotNull(nBGFolder)
					? createFolder(removeAllWhitespace(zoneName), nBGFolder.getFolderId())
					: null;
			Folder branchFolder = Validator.isNotNull(nBGFolder)
					? createFolder(removeAllWhitespace(branchName), zoneFolder.getFolderId())
					: null;
			Folder finalFolder = null;

			if (Validator.isNotNull(nBGFolder)) {
				this.nbgFolderId = nBGFolder.getFolderId();
				finalFolder = nBGFolder;
			}
			if (Validator.isNotNull(zoneFolder)) {
				this.zoneFolderId = zoneFolder.getFolderId();
				finalFolder = zoneFolder;
			}
			if (Validator.isNotNull(branchFolder)) {
				this.branchFolderId = branchFolder.getFolderId();
				finalFolder = branchFolder;
			}
			if (Validator.isNotNull(finalFolder)) {
				Folder urnGenerate = createFolder(departmentId, finalFolder.getFolderId());
				this.departmentFolderId = urnGenerate.getFolderId();
			} else {
				Folder urnGenerate = createFolder(departmentId, hoFolderId);
				this.departmentFolderId = urnGenerate.getFolderId();
			}
		} catch (Exception e) {
			log.error(e);
			log.debug(userId + " branchName : " + branchName + " zoneName " + zoneName + " nbgName " + nbgName);
		}
	}

	public static String removeAllWhitespace(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("\\s+", "");
	}

	private Organization getOrganization(String type, Organization organization) {
		if (Validator.isNotNull(organization) && organization.getType().equalsIgnoreCase(type)) {
			return organization;
		}
		if (Validator.isNotNull(organization) && organization.getParentOrganizationId() > 0) {
			try {
				return getOrganization(type, organization.getParentOrganization());
			} catch (PortalException e) {
				log.error(e);
			}
		}
		return null;
	}

	private Organization getOrganization(List<Organization> organizations, String type) {
		for (Organization organization : organizations) {
			Organization organization2 = getOrganization(type, organization);
			if (Validator.isNotNull(organization2)) {

				return organization2;
			}
		}
		return null;
	}

	private void loadUserOrganizationData(User user) {
		try {
			branchOrganization = getOrganization(user.getOrganizations(), BOIDMSConstants.BRANCH);
			zoneOrganization = getOrganization(user.getOrganizations(), BOIDMSConstants.ZONE);
			nbgOrganization = getOrganization(user.getOrganizations(), BOIDMSConstants.NBG);
			hoOrganization = getOrganization(user.getOrganizations(), "organization");

			branchName = Validator.isNotNull(branchOrganization) ? branchOrganization.getName() : null;
			zoneName = Validator.isNotNull(zoneOrganization) ? zoneOrganization.getName() : null;
			nbgName = Validator.isNotNull(nbgOrganization) ? nbgOrganization.getName() : null;
			hoName = Validator.isNotNull(hoOrganization) ? hoOrganization.getName() : null;

			log.debug(userId + " branch name " + branchName + " zone Name " + zoneName + " nbg name " + nbgName + " Ho "
					+ hoName);
		} catch (PortalException e) {
			log.error(e);
		}
	}

	public Organization getUserOrganization() {
		if (Validator.isNotNull(branchOrganization)) {
			return branchOrganization;
		}
		if (Validator.isNotNull(zoneOrganization)) {
			return zoneOrganization;
		}
		if (Validator.isNotNull(nbgOrganization)) {
			return nbgOrganization;
		}
		if (Validator.isNotNull(hoOrganization)) {
			return hoOrganization;
		}
		return null;
	}

	private void isBranchMaker() {
		Role role = getRole(BOIDMSConstants.BRANCHMAKER);
		if (Validator.isNotNull(role)) {
			isMaker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), userId);
		}
	}

	private void loadBrachChecker() {
		Role role = getRole(BOIDMSConstants.BRANCHCHECKER);
		if (Validator.isNotNull(role)) {
			isBrachChecker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), userId);
		}
	}

	public boolean isBranchChecker() {
		return isBrachChecker;
	}

	private void loadZoneChecker() {
		Role role = getRole(BOIDMSConstants.ZONECHECKER);
		if (Validator.isNotNull(role)) {
			isZoneChecker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), userId);
		}
	}

	private boolean isHOChecker;

	private void loadZoneMaker() {
		Role role = getRole(BOIDMSConstants.ZONEMAKER);
		if (Validator.isNotNull(role)) {
			isZoneMaker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), userId);
		}
	}

	public boolean isHOChecker() {
		return isHOChecker;
	}

	private void loadHOChecker() {
		Role role = getRole("HO Checker");
		if (Validator.isNotNull(role)) {
			isHOChecker = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), userId);
		}

	}

	private Role getRole(String roleName) {
		try {
			Role role = RoleLocalServiceUtil.getRole(user.getCompanyId(), roleName);
			return role;
		} catch (PortalException e) {
			return null;
		}

	}

	private Folder getFolder(long folderId) {
		try {
			return _dlAppService.getFolder(folderId);
		} catch (PortalException e) {
			return null;
		}
	}

	private Folder createFolder(String folderName, long parentFolderId) {
		Folder folder = null;
		Folder folder2 = getFolder(parentFolderId);

		if (Validator.isBlank(folderName) || Validator.isNull(folder2)) {
			return folder;
		}

		ServiceContext context = new ServiceContext();
		context.setScopeGroupId(folder2.getGroupId());

		try {

			folder = _dlAppService.getFolder(folder2.getRepositoryId(), folder2.getFolderId(), folderName);

		} catch (Exception e) {

			try {

				folder = _dlAppService.addFolder(null, folder2.getRepositoryId(), parentFolderId, folderName, "",
						context);
				getPermission(folder);

			} catch (PortalException e1) {
				log.error(e1);
			}
		}
		return folder;
	}

	private boolean getPermission(Folder folder) {
		long roleId = getUserRole(folder);

		final HashMap<Long, String[]> rolesForFolder = new HashMap<>();
		rolesForFolder.put(roleId,
				new String[] { ActionKeys.VIEW, ActionKeys.ACCESS, ActionKeys.ADD_SUBFOLDER, ActionKeys.ADD_DOCUMENT });
		try {
			log.debug("checking permission " + roleId);
			boolean isTrue = ResourcePermissionLocalServiceUtil.hasResourcePermission(folder.getCompanyId(),
					"com.liferay.document.library.kernel.model.DLFolder", ResourceConstants.SCOPE_INDIVIDUAL,
					Long.toString(folder.getFolderId()), roleId, ActionKeys.ADD_DOCUMENT);
			if (!isTrue) {
				return addPermission(folder, roleId, rolesForFolder);
			}
		} catch (PortalException e) {
			log.error(e);
		}
		return false;
	}

	private boolean addPermission(Folder folder, long roleId, HashMap<Long, String[]> rolesForFolder) {

		try {
			log.debug("Adding permission " + roleId);
			ResourcePermissionLocalServiceUtil.setResourcePermissions(folder.getCompanyId(),
					"com.liferay.document.library.kernel.model.DLFolder", ResourceConstants.SCOPE_INDIVIDUAL,
					Long.toString(folder.getFolderId()), rolesForFolder);
			return true;
		} catch (PortalException e) {

			return false;
		}
	}

	private long getUserRole(Folder folder) {
		try {
			return RoleLocalServiceUtil.getRole(folder.getCompanyId(), "User").getRoleId();
		} catch (PortalException e) {

			return 0l;
		}
	}

	public String getName() {
		return name;
	}

	public long getUserId() {
		return userId;
	}

	public String getEmailId() {
		return emailId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getZoneName() {
		return zoneName;
	}

	public String getNbgName() {
		return nbgName;
	}

	public String getHoName() {
		return hoName;
	}

	public User getUser() {
		return user;
	}

	/*
	 * public long getBranchFolderId() { return branchFolderId; }
	 * 
	 * public long getZoneFolderId() { return zoneFolderId; }
	 * 
	 * public long getNbgFolderId() { return nbgFolderId; }
	 * 
	 * public long getHoFolderId() { return hoFolderId; }
	 * 
	 * public long getDepartmentFolderId() { return departmentFolderId; }
	 */

	public boolean isMaker() {
		return isMaker;
	}

	public boolean isZoneChecker() {
		return isZoneChecker;
	}

	public boolean isZoneMaker() {
		return isZoneMaker;
	}

	public static Log getLog() {
		return log;
	}

	public DLAppService get_dlAppService() {
		return _dlAppService;
	}

	private ArrayList<String> folders;

	private static final Log log = LogFactoryUtil.getLog(UserHierarchy.class);
	private DLAppService _dlAppService;

}