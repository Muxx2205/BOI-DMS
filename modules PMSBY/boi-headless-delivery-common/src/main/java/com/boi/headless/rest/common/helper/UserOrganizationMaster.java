/*
 * package com.boi.headless.rest.common.helper;
 * 
 * import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
 * import com.liferay.object.model.ObjectDefinition; import
 * com.liferay.object.rest.manager.v1_0.ObjectEntryManager; import
 * com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry; import
 * com.liferay.object.service.ObjectDefinitionLocalServiceUtil; import
 * com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil; import
 * com.liferay.portal.kernel.log.Log; import
 * com.liferay.portal.kernel.log.LogFactoryUtil; import
 * com.liferay.portal.kernel.model.Organization; import
 * com.liferay.portal.kernel.model.User; import
 * com.liferay.portal.kernel.util.Validator; import
 * com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry; import
 * com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext; import
 * com.liferay.portal.vulcan.pagination.Page; import
 * com.liferay.portal.vulcan.pagination.Pagination;
 * 
 * import java.util.ArrayList; import java.util.List; import java.util.Locale;
 * import java.util.Map; import java.util.stream.Collectors;
 * 
 * import org.osgi.service.component.annotations.Activate; import
 * org.osgi.service.component.annotations.Modified; import
 * org.osgi.service.component.annotations.Reference;
 * 
 * public class UserOrganizationMaster { private static final Log log =
 * LogFactoryUtil.getLog(UserOrganizationMaster.class); public
 * UserOrganizationMaster(long organizationId,String screenName,String
 * systemClassName,DTOConverterRegistry _dtoConverterRegistry) {
 * buildFilter(systemClassName, organizationId, screenName); }
 * 
 * private String buildFilter(String systemClassName, long organizationId,String
 * screenName) { List<String> filters = new ArrayList<>();
 * filters.add("departmentId eq '" + systemClassName + "'");
 * filters.add("organizationId eq " + organizationId);
 * filters.add("userScreenName eq '" + screenName + "'");
 * 
 * return filters.stream() .filter(Validator::isNotNull) .filter(x ->
 * !Validator.isBlank(x)) .collect(Collectors.joining(" and ")); }
 * 
 * private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry>
 * getUserDeparments(String query, User user) { try { ObjectDefinition
 * userDepartmentObject = ObjectDefinitionLocalServiceUtil
 * .getObjectDefinition(boiWorkflowConfiguation.UserDepartmentObjectDefinitionId
 * ());
 * 
 * DefaultDTOConverterContext dtoConverterContext = new
 * DefaultDTOConverterContext(_dtoConverterRegistry,
 * userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), null,
 * user);
 * 
 * ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
 * .getObjectEntryManager(userDepartmentObject.getStorageType());
 * 
 * Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;
 * 
 * page =
 * objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(),
 * userDepartmentObject, userDepartmentObject.getScope(), null,
 * dtoConverterContext, query, Pagination.of(-1, -1), null, null);
 * log.debug("List from user deaprtment " + page.getTotalCount()); return page;
 * } catch (Exception e) { log.error(e); } return null; }
 * 
 * 
 * @Activate
 * 
 * @Modified protected void activate(Map<String, Object> properties) {
 * boiWorkflowConfiguation =
 * ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class,
 * properties);
 * 
 * }
 * 
 * private BOIWorkflowConfiguation boiWorkflowConfiguation;
 * 
 * @Reference private DTOConverterRegistry _dtoConverterRegistry;
 * 
 * @Reference private ObjectEntryManagerRegistry _objectEntryManagerRegistry; }
 */