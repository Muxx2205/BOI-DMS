package com.boi.headless.rest.common.listeners;

import com.boi.headless.rest.common.application.BOIDuplicateValidation;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(configurationPid = {
		"com.boi.headless.rest.common.application.BOIDuplicateValidation" }, immediate = true, service = ModelListener.class)
public class ObjectEntryListener extends BaseModelListener<ObjectEntry> {

	private static final Log log = LogFactoryUtil.getLog(ObjectEntryListener.class);
	private static final String FIELD_BRANCH_CODE = "branchCode";
	private static final String FIELD_ZONE = "zone";
	private static final String FIELD_NBG_CODE = "nbgCode";
	private static final String SKIP_EXTERNALREFRENCECODE = "MEMORANDUM";
    private static final String DESCRIPTION_FIELD = "description";

	 
	@Override
	public void onBeforeCreate(ObjectEntry model) throws ModelListenerException {
		log.debug("onBeforeCreate " + model.getObjectDefinitionId());
		log.debug("onBeforeCreate " + model.getObjectEntryId());

		Map<String, Serializable> values = model.getValues();
		
        Map<String, Serializable> valuesForScriptingCheck = new HashMap<>(values);
        
        if (model.getExternalReferenceCode().equalsIgnoreCase(SKIP_EXTERNALREFRENCECODE)) {
            valuesForScriptingCheck.remove(DESCRIPTION_FIELD);
        }

		if (boiDuplicateValidation.scriptingCheck() && checkScriptLang(valuesForScriptingCheck)) {
			throw new ModelListenerException("Scripting not allowed");
		}

		if (boiDuplicateValidation.isActive()) {
			validateDuplicateEntry(model, valuesForScriptingCheck);
		}

		super.onBeforeCreate(model);
	}

	private void validateDuplicateEntry(ObjectEntry model, Map<String, Serializable> values)
			throws ModelListenerException {
		try {
			User user = UserLocalServiceUtil.getUser(model.getUserId());
			List<String> filter = buildDuplicateFilter(values, user);

			String timeRangeFilter = buildDateRangeFilter("dateCreated", boiDuplicateValidation.intervalOfMinutes());
			filter.add(timeRangeFilter);
			filter.add("userId eq " + user.getUserId());

			String statusQuery = filter.stream()
				.filter(Validator::isNotNull)
				.filter(x -> !Validator.isBlank(x))
				.collect(Collectors.joining(" and "));

			log.debug("Validation Query : " + statusQuery);

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = getUserDeparments(
				statusQuery, user, model.getObjectDefinitionId());

			if (page != null && page.getTotalCount() > 0) {
				throw new ModelListenerException("Try to Submit Multiple same entry");
			}
		} catch (PortalException e) {
			throw new ModelListenerException("Duplicate Entry Validation Failed");
		}
	}

	private List<String> buildDuplicateFilter(Map<String, Serializable> values, User user) {
		List<String> filter = new ArrayList<>();
		addValueFilter(values, filter, FIELD_BRANCH_CODE);
		addValueFilter(values, filter, FIELD_ZONE);
		addValueFilter(values, filter, FIELD_NBG_CODE);
		return filter;
	}

	private void addValueFilter(Map<String, Serializable> values, List<String> filter, String field) {
		if (values.containsKey(field) && !Validator.isBlank(String.valueOf(values.get(field)))) {
			filter.add(field + " eq '" + values.get(field) + "'");
		}
	}

	private boolean checkScriptLang(Map<String, Serializable> values) {

		try {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray(boiDuplicateValidation.scritpStringBlocker());
			for (Map.Entry<String, Serializable> entry : values.entrySet()) {
				if (entry.getValue() instanceof String) {
					String value = (String) entry.getValue();
					Iterator<String> iterator = jsonArray.iterator();
					while (iterator.hasNext()) {
						if (value.contains(iterator.next())) {
							return true;
						}
					}

				}
			}

		} catch (JSONException e) {
			log.error(e);
			throw new ModelListenerException("Scripting validation failed!");
		}

		return false;
	}

	private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> getUserDeparments(String query, User user,
			long objectDefinitionId) {
		try {
			ObjectDefinition userDepartmentObject = ObjectDefinitionLocalServiceUtil
					.getObjectDefinition(objectDefinitionId);

			DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(dtoConverterRegistry,
					userDepartmentObject.getObjectDefinitionId(), Locale.getDefault(), null, user);

			ObjectEntryManager objectEntryManager = objectEntryManagerRegistry
					.getObjectEntryManager(userDepartmentObject.getStorageType());

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = null;

			page = objectEntryManager.getObjectEntries(userDepartmentObject.getCompanyId(), userDepartmentObject,
					userDepartmentObject.getScope(), null, dtoConverterContext, query, Pagination.of(-1, -1), null,
					null);

			return page;
		} catch (Exception e) {
			log.error(e);
			
		}
		return null;
	}

	public static String buildDateRangeFilter(String fieldName, int durationInMinutes) {
		// Get current time in UTC
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		ZonedDateTime before = now.minusMinutes(durationInMinutes);

		// Format to ISO 8601 with seconds only, no nanoseconds
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

		String from = formatter.format(before);
		String to = formatter.format(now);

		// Construct filter string
		return String.format("%s gt %s and %s lt %s", fieldName, from, fieldName, to);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {

		boiDuplicateValidation = ConfigurableUtil.createConfigurable(BOIDuplicateValidation.class, properties);

	}

	private BOIDuplicateValidation boiDuplicateValidation;

	@Reference
	private DTOConverterRegistry dtoConverterRegistry;

	@Reference
	private ObjectEntryManagerRegistry objectEntryManagerRegistry;

}