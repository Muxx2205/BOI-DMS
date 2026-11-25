package com.boi.headless.rest.rrb.consent.internal.resource.v1_0;

import com.boi.headless.rest.rrb.consent.resource.v1_0.ConsentResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.application.CustomWorkflowLogs;
import com.boi.headless.rest.common.application.FileHandler;
import com.boi.headless.rest.common.application.ObjectConverter;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.rest.common.validators.RequestValidator;
import com.boi.headless.rest.rrb.consent.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.rrb.consent.dto.v1_0.CBSMetadataRequest;
import com.boi.headless.rest.rrb.consent.dto.v1_0.ConsentResponse;
import com.boi.headless.rest.rrb.consent.dto.v1_0.CustomBOIHeadlessResponse;
import com.boi.headless.rest.rrb.consent.dto.v1_0.CustomBOIHeadlessResponseAsList;
import com.boi.headless.rest.rrb.consent.dto.v1_0.StatusCheckResponse;
import com.boi.headless.rest.rrb.consent.dto.v1_0.URNUpdateRequest;
import com.boi.headless.rest.rrb.consent.resource.v1_0.ConsentResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.RangeTermQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(configurationPid = "com.boi.headless.rest.common.application.BOIWorkflowConfiguation", properties = "OSGI-INF/liferay/rest/v1_0/consent.properties", scope = ServiceScope.PROTOTYPE, service = ConsentResource.class)
public class ConsentResourceImpl extends BaseConsentResourceImpl {

	private static final Log LOG = LogFactoryUtil.getLog(ConsentResourceImpl.class);

	// ==================== CBS Role Name ====================
	private static final String CBS_ROLE_NAME = "CBS API User";

	// ==================== STATIC OPTIMIZATIONS ====================
	private static final Pattern NON_NUMERIC_PATTERN = Pattern.compile("[^0-9]");
	private static final Set<String> STATUS_SET = Set.of("initiated", "pending with branch maker",
			"pending with branch checker", "approved", "deleted");
	private static final Map<String, Integer> STATUS_TO_STAGE_MAP = Map.of("initiated", 0, "pending with branch maker",
			1, "pending with branch checker", 2, "approved", 3, "rejected", 1, "deleted", 4);
	private static final Map<String, String> FILE_UPLOAD_MAP = Map.of("consentFormDocumentDetailedFile",
			"consentFormDocumentDetailed");
	private static final String[] SEARCH_FIELDS = { "entryClassPK", "uid", "objectDefinitionId", "createDate_sortable",
			"modified_sortable", "createDate", "modified", "stage", "cbsReferenceNumber", "branchCode", "regionalOffice",
			"customerId", "accountNumber", "typeOfInsurance", "urn", "documentReferenceNo", "delFlag" };
	private static final Set<String> TOP_LEVEL_ORGS = Set.of("BOI", "BANK OF INDIA", "HEAD OFFICE", "HO");

	// ==================== CBS Role Cache ====================
	private volatile Boolean cachedCBSRole;
	private volatile long lastCBSCheckTime;
	private static final long CBS_ROLE_CACHE_TTL = 30000;

	// ==================== Hierarchy Cache ====================
	private static final Map<String, HierarchyInfo> HIERARCHY_CACHE = new ConcurrentHashMap<>();
	private static final long CACHE_TTL = 3600000;

	// ==================== ThreadLocal Formatters ====================
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
	private static final ThreadLocal<DateTimeFormatter> DATE_TIME_FORMATTER = ThreadLocal
			.withInitial(() -> DateTimeFormatter.ofPattern("yyyyMMdd"));
	private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

	// ==================== Inner Class: HierarchyInfo ====================
	private static class HierarchyInfo {
		final String branchCode;
		final String branchName;
		final String regionalOffice;
		final long timestamp;

		HierarchyInfo(String branchCode, String branchName, String regionalOffice) {
			this.branchCode = branchCode;
			this.branchName = branchName;
			this.regionalOffice = regionalOffice;
			this.timestamp = System.currentTimeMillis();
		}

		boolean isValid() {
			return (System.currentTimeMillis() - timestamp) < CACHE_TTL;
		}

		boolean isEmpty() {
			return branchCode == null && branchName == null && regionalOffice == null;
		}

		Map<String, String> toMap() {
			Map<String, String> map = new HashMap<>(3);
			if (branchCode != null)
				map.put("branchCode", branchCode);
			if (branchName != null)
				map.put("branch", branchName);
			if (regionalOffice != null)
				map.put("regionalOffice", regionalOffice);
			return map;
		}
	}

	// ==================== Params Constants ====================
	private static final class Params {
		static final String STAGE = "stage";
		static final String STATUS = "status";
		static final String FROM_DATE = "fromDate";
		static final String TO_DATE = "toDate";
		static final String ENTRY_ID = "entryId";
		static final String TRANSITION_ID = "transitionId";
		static final String REMARKS = "remarks";
		static final String WORKFLOW_TASK_ID = "workflowTaskId";
		static final String CBS_REFERENCE_NUMBER = "cbsReferenceNumber";
		static final String CUSTOMER_ID = "customerId";
		static final String ACCOUNT_NUMBER = "accountNumber";
		static final String BRANCH_CODE = "branchCode";
		static final String REGIONAL_OFFICE = "regionalOffice";
		static final String TYPE_OF_INSURANCE = "typeOfInsurance";
		static final String APPLY_HIERARCHY = "applyHierarchy";
		static final String FORCE_DB_QUERY = "forceDbQuery";
		static final String DOCUMENT_REFERENCE_NO = "documentReferenceNo";
		static final String DEL_FLAG = "delFlag";
		static final String URN = "urn";
		static final String YEAR = "year";
		static final String ATTACHMENTS = "attachments";
		static final String DECISION = "decision";
		static final String ENTRY_CLASS_PK = "entryClassPK";
		static final String UID = "uid";
		static final String TRANSITION_NAME = "transitionName";
		static final String OBJECT_ENTRY_ID = "objectEntryId";
		static final String CUSTOM_ACTION = "customAction";

		private Params() {
		}
	}

	// ==================== Nested Field Constants ====================
	private static final class NestedField {
		static final String ARRAY = "nestedFieldArray";
		static final String NAME = "fieldName";
		static final String VALUE_INTEGER = "value_integer";
		static final String VALUE_BOOLEAN = "value_boolean";
		static final String VALUE_KEYWORD = "value_keyword_lowercase";

		private NestedField() {
		}
	}

	// ==================== Helper Methods ====================

	private String extractFirstFourDigits(String value) {
		if (Validator.isNull(value))
			return null;
		String numericOnly = NON_NUMERIC_PATTERN.matcher(value).replaceAll(StringPool.BLANK);
		if (numericOnly.length() >= 4) {
			return numericOnly.substring(0, 4);
		}
		return numericOnly.isEmpty() ? null : numericOnly;
	}

	private String cleanBranchName(String branchCode, String branchName) {
		if (Validator.isNull(branchCode) || Validator.isNull(branchName)) {
			return branchName;
		}
		if (branchName.startsWith(branchCode)) {
			String cleaned = branchName.substring(branchCode.length()).trim();
			if (cleaned.startsWith("-") || cleaned.startsWith("–") || cleaned.startsWith("—")) {
				cleaned = cleaned.substring(1).trim();
			}
			if (Validator.isNotNull(cleaned)) {
				return cleaned;
			}
		}
		return branchName;
	}

	private boolean isTopLevelOrganization(String orgName) {
		if (Validator.isNull(orgName))
			return false;
		return TOP_LEVEL_ORGS.contains(orgName.toUpperCase());
	}

	private boolean isRegionalOfficeOrganization(String orgName) {
		if (Validator.isNull(orgName))
			return false;
		// Regional offices typically have "RO" or "REGIONAL" in their name
		String upperName = orgName.toUpperCase();
		return upperName.contains("REGIONAL") || upperName.contains("RO");
	}

	private HierarchyInfo getHierarchyFromOrganization(String branchPrefix) {
		if (Validator.isNull(branchPrefix))
			return new HierarchyInfo(null, null, null);

		HierarchyInfo cached = HIERARCHY_CACHE.get(branchPrefix);
		if (cached != null && cached.isValid())
			return cached;

		try {
			List<Organization> organizations = _organizationLocalService.getOrganizations(contextCompany.getCompanyId(),
					OrganizationConstants.ANY_PARENT_ORGANIZATION_ID);

			Organization branchOrg = null;
			for (Organization org : organizations) {
				String orgName = org.getName();
				if (orgName.startsWith(branchPrefix) || orgName.startsWith(branchPrefix + " - ")
						|| orgName.startsWith(branchPrefix + "-")) {
					branchOrg = org;
					break;
				}
			}

			if (branchOrg == null) {
				for (Organization org : organizations) {
					String numericOnly = NON_NUMERIC_PATTERN.matcher(org.getName()).replaceAll(StringPool.BLANK);
					if (numericOnly.startsWith(branchPrefix)) {
						branchOrg = org;
						break;
					}
				}
			}

			if (branchOrg == null)
				return new HierarchyInfo(null, null, null);

			String branchName = branchOrg.getName();
			String regionalOffice = null;
			long parentId = branchOrg.getParentOrganizationId();
			boolean foundRegionalOffice = false;

			while (parentId > 0) {
				try {
					Organization parentOrg = _organizationLocalService.getOrganization(parentId);
					String parentName = parentOrg.getName();

					if (!foundRegionalOffice && isRegionalOfficeOrganization(parentName)) {
						regionalOffice = parentName;
						foundRegionalOffice = true;
					}

					if (foundRegionalOffice)
						break;
					if (isTopLevelOrganization(parentName))
						break;
					parentId = parentOrg.getParentOrganizationId();
				} catch (Exception e) {
					break;
				}
			}

			HierarchyInfo info = new HierarchyInfo(branchPrefix, branchName, regionalOffice);
			HIERARCHY_CACHE.put(branchPrefix, info);
			return info;

		} catch (Exception e) {
			LOG.error("Error fetching hierarchy for: " + branchPrefix, e);
			return new HierarchyInfo(null, null, null);
		}
	}

	private Map<String, String> getCachedHierarchy(String branchPrefix) {
		if (Validator.isNull(branchPrefix))
			return new HashMap<>();
		HierarchyInfo info = getHierarchyFromOrganization(branchPrefix);
		return info.isEmpty() ? new HashMap<>() : info.toMap();
	}

	private boolean logAlreadyExists(ObjectEntry objectEntry, String action) {
		try {
			List<ObjectEntry> existingLogs = _objectEntryLocalService.getObjectEntries(0L, workflowObjectDefinitionId,
					0, -1);
			for (ObjectEntry logEntry : existingLogs) {
				Map<String, Serializable> logValues = logEntry.getValues();
				String transitionName = (String) logValues.get(Params.TRANSITION_NAME);
				String objectId = String.valueOf(logValues.get(Params.OBJECT_ENTRY_ID));
				if (action.equals(transitionName) && String.valueOf(objectEntry.getObjectEntryId()).equals(objectId)) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	private String nullToBlank(String value) {
		return value != null ? value : StringPool.BLANK;
	}

	// ==================== File Validation ====================
	private void fileValidation(MultipartBody multipartBody) throws Exception {
		new RequestValidator(multipartBody, BOIDMSConstants.RRB_CONSENT).validateRequestFiles();
	}

	// ==================== CBS Role Check ====================
	private boolean hasCBSRole() {
		long currentTime = System.currentTimeMillis();
		if (cachedCBSRole != null && (currentTime - lastCBSCheckTime) < CBS_ROLE_CACHE_TTL) {
			return cachedCBSRole;
		}
		try {
			Role role = RoleLocalServiceUtil.getRole(contextUser.getCompanyId(), CBS_ROLE_NAME);
			boolean hasRole = UserLocalServiceUtil.hasRoleUser(role.getRoleId(), contextUser.getUserId());
			cachedCBSRole = hasRole;
			lastCBSCheckTime = currentTime;
			return hasRole;
		} catch (Exception e) {
			return false;
		}
	}

	// ==================== CREATE ENTRY ====================
	private ObjectEntry createObjectEntry(Map<String, Serializable> values, String externalRefCode) throws Exception {
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(contextUser.getCompanyId());
		serviceContext.setUserId(contextUser.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		return _objectEntryLocalService.addOrUpdateObjectEntry(externalRefCode, contextUser.getUserId(), 0,
				consentObjectDefinitionId, values, serviceContext);
	}

	// ==================== FIND BY CBS REFERENCE (FIELD-BASED) ====================
	/**
	 * Find object entry by CBS Reference Number (FIELD-BASED)
	 */
	private ObjectEntry findByCBSReference(String cbsReferenceNumber) throws PortalException {
		if (Validator.isNull(cbsReferenceNumber))
			return null;

		if (LOG.isInfoEnabled()) {
			LOG.info("🔍 Searching for CBS Reference: " + cbsReferenceNumber);
		}

		// ✅ PMSBY PATTERN: Use fetchObjectEntry() with externalReferenceCode
		try {
			ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(cbsReferenceNumber,
					consentObjectDefinitionId);
			if (entry != null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("✅ Found by externalReferenceCode: " + entry.getObjectEntryId());
				}
				return entry;
			}
		} catch (Exception e) {
			// Ignore, try next method
		}

		// ✅ PMSBY PATTERN: Use ObjectEntryManager for search
		try {
			ObjectDefinition objectDefinition = _objectDefinitionLocalService
					.getObjectDefinition(consentObjectDefinitionId);
			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
					.getObjectEntryManager(objectDefinition.getStorageType());

			DTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(_dtoConverterRegistry, 0,
					Locale.getDefault(), null, null);

			// Build filter query
			String filter = "cbsReferenceNumber eq '" + cbsReferenceNumber + "'";

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = objectEntryManager.getObjectEntries(
					contextCompany.getCompanyId(), objectDefinition, null, null, dtoConverterContext, filter,
					Pagination.of(1, 1), null, null);

			if (page != null && page.getTotalCount() > 0) {
				List<com.liferay.object.rest.dto.v1_0.ObjectEntry> items = (List<com.liferay.object.rest.dto.v1_0.ObjectEntry>) page
						.getItems();
				if (items != null && !items.isEmpty()) {
					long entryId = items.get(0).getId();
					ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(entryId);
					if (entry != null) {
						if (LOG.isInfoEnabled()) {
							LOG.info("✅ Found by ObjectEntryManager search: " + entry.getObjectEntryId());
						}
						return entry;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error using ObjectEntryManager: " + e.getMessage(), e);
		}

		if (LOG.isWarnEnabled()) {
			LOG.warn("❌ No entry found for CBS Reference: " + cbsReferenceNumber);
		}
		return null;
	}

	// ==================== API 1: CBS Metadata Push ====================
	@Override
	public ConsentResponse pushConsentMetadata(CBSMetadataRequest request) throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("=== CBS Metadata Push START ===");
			LOG.info("CBS Reference: " + request.getCbsReferenceNumber());
		}

		if (!hasCBSRole()) {
			return createErrorResponse("Access denied: Only CBS service user can call this API");
		}

		try {
			validateMetadataRequest(request);

			String branchPrefix = extractFirstFourDigits(request.getBranchCode());
			if (Validator.isNull(branchPrefix)) {
				branchPrefix = extractFirstFourDigits(request.getBranch());
			}

			if (Validator.isNull(branchPrefix)) {
				return createErrorResponse("Unable to extract branch code from request");
			}

			ObjectEntry existingEntry = findByCBSReference(request.getCbsReferenceNumber());
			if (existingEntry != null) {
				return createErrorResponse("Record already exists for this CBS Reference");
			}

			Map<String, Serializable> values = convertMetadataToValues(request);
			values.put(Params.STAGE, 0);
			values.put(Params.DOCUMENT_REFERENCE_NO, StringPool.BLANK);
			values.put(Params.DEL_FLAG, "N");
			values.put(Params.URN, StringPool.BLANK);

			values = getDefaultData(contextHttpServletRequest, values, consentObjectDefinitionId,
					_objectFieldLocalService, null);

			ObjectEntry entry = createObjectEntry(values, request.getCbsReferenceNumber());

			new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(entry, "METADATA_RECEIVED_FROM_CBS",
					workflowObjectDefinitionId);

			if (LOG.isInfoEnabled()) {
				LOG.info("=== CBS Metadata Push END. EntryId: " + entry.getObjectEntryId());
			}

			return createSuccessResponse(entry, 0, "Metadata received successfully. Stage: Initiated");

		} catch (Exception e) {
			LOG.error("Error in metadata push", e);
			return createErrorResponse("Failed to process metadata: " + e.getMessage());
		}
	}

	// ==================== API 2: Check Document Status ====================
	@Override
	public StatusCheckResponse checkDocumentStatus(String cbsReferenceNumber) throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("=== Check Document Status START ===");
			LOG.info("CBS Reference: " + cbsReferenceNumber);
		}

		if (!hasCBSRole()) {
			StatusCheckResponse response = new StatusCheckResponse();
			response.setDocumentVerified(false);
			response.setMessage("Access denied: Only CBS service user can call this API");
			return response;
		}

		ObjectEntry entry = findByCBSReference(cbsReferenceNumber);

		StatusCheckResponse response = new StatusCheckResponse();
		response.setDocumentVerified(false);

		if (entry == null) {
			response.setMessage("Record not found for CBS Reference: " + cbsReferenceNumber);
			return response;
		}

		Map<String, Serializable> values = entry.getValues();
		int stage = GetterUtil.getInteger(values.get(Params.STAGE));
		String drn = (String) values.get(Params.DOCUMENT_REFERENCE_NO);
		String delFlag = (String) values.get(Params.DEL_FLAG);

		response.setStage(stage);
		response.setDocumentReferenceNo(drn);

		if ("Y".equals(delFlag)) {
			response.setDocumentVerified(false);
			response.setMessage("Consent has been deleted");
			return response;
		}

		String message = switch (stage) {
		case 3 -> "Document verified in DMS. DRN: " + drn;
		case 2 -> "Document is pending with branch checker";
		case 1 -> "Document is pending with branch maker";
		case 0 -> "Document not yet uploaded. Stage: Initiated";
		default -> "Document not verified. Current stage: " + stage;
		};

		response.setDocumentVerified(stage == 3);
		response.setMessage(message);
		return response;
	}

	// ==================== API 3: Update URN ====================
	@Override
	public ConsentResponse updateURN(URNUpdateRequest request) throws Exception {
	    if (LOG.isInfoEnabled()) {
	        LOG.info("=== Update URN START ===");
	        LOG.info("CBS Reference: " + request.getCbsReferenceNumber());
	        LOG.info("URN: " + request.getUrn());
	        LOG.info("Status: " + request.getStatus());
	        LOG.info("TransDate (incoming): " + request.getTransDate());
	    }

	    if (!hasCBSRole()) {
	        return createErrorResponse("Access denied: Only CBS service user can call this API");
	    }

	    if (Validator.isNull(request.getCbsReferenceNumber())) {
	        return createErrorResponse("cbsReferenceNumber is required");
	    }
	    if (Validator.isNull(request.getUrn())) {
	        return createErrorResponse("urn is required");
	    }
	    if (Validator.isNull(request.getStatus())) {
	        return createErrorResponse("status is required (APPROVED/REJECTED/DELETED)");
	    }

	    ObjectEntry entry = findByCBSReference(request.getCbsReferenceNumber());
	    if (entry == null) {
	        return createErrorResponse("Record not found for CBS Reference: " + request.getCbsReferenceNumber());
	    }

	    Map<String, Serializable> values = entry.getValues();
	    int currentStage = GetterUtil.getInteger(values.get(Params.STAGE));
	    String drn = (String) values.get(Params.DOCUMENT_REFERENCE_NO);

	    if (currentStage != 3) {
	        return createErrorResponse(
	                "URN update not allowed. Document not verified in DMS. Current stage: " + currentStage);
	    }

	    // ✅ LOG: Current transDate before update
	    LOG.info("📌 Current transDate before update: " + values.get("transDate"));
	    LOG.info("📌 TransDate class before update: " + 
	        (values.get("transDate") != null ? values.get("transDate").getClass().getName() : "null"));

	    String transDate = request.getTransDate();

	    // ✅ Process based on status
	    ConsentResponse response;
	    switch (request.getStatus().toUpperCase()) {
	        case "APPROVED":
	            response = processCBSApproval(entry, values, drn, request.getUrn(), transDate);
	            break;
	        case "REJECTED":
	            response = processCBSRejection(entry, values, drn, request.getUrn(), transDate);
	            break;
	        case "DELETED":
	            response = processCBSDeletion(entry, values, drn, request.getUrn(), transDate);
	            break;
	        default:
	            return createErrorResponse("Invalid status. Use APPROVED, REJECTED, or DELETED");
	    }

	    // ✅ LOG: After update
	    Map<String, Serializable> updatedValues = entry.getValues();
	    LOG.info("📌 transDate after update: " + updatedValues.get("transDate"));
	    LOG.info("📌 transDate class after update: " + 
	        (updatedValues.get("transDate") != null ? updatedValues.get("transDate").getClass().getName() : "null"));

	    return response;
	}

	private ConsentResponse processCBSApproval(ObjectEntry entry, Map<String, Serializable> values, String drn,
	        String urn, String transDate) throws Exception {
	    
	    LOG.info("📌 processCBSApproval - Updating URN and transDate");
	    
	    values.put(Params.URN, urn);
	    values.put(Params.DEL_FLAG, "N");
	    values.put("cbsStatus", "APPROVED");
	    values.put("cbsVerificationDate", new Date());

	    // ✅ FIX: Use SAME logic as convertMetadataToValues()
	    if (Validator.isNotNull(transDate)) {
	        try {
	            LOG.info("📌 Parsing transDate: " + transDate);
	            
	            // Clean the date string (remove time if present)
	            String datePart = transDate;
	            if (datePart.contains("T")) {
	                datePart = datePart.split("T")[0];
	            }
	            if (datePart.contains(" ")) {
	                datePart = datePart.split(" ")[0];
	            }
	            if (datePart.contains("+")) {
	                datePart = datePart.split("\\+")[0];
	            }
	            if (datePart.contains("Z")) {
	                datePart = datePart.replace("Z", "");
	            }
	            
	            LOG.info("📌 Cleaned datePart: " + datePart);
	            
	            // ✅ SAME LOGIC as convertMetadataToValues()
	            Date date = Date.from(
	                LocalDate.parse(datePart).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()
	            );
	            values.put("transDate", date);
	            LOG.info("✅ Updated transDate to: " + date);
	            
	        } catch (Exception e) {
	            LOG.warn("⚠️ Failed to parse transDate: " + transDate + " - " + e.getMessage());
	        }
	    } else {
	        LOG.info("📌 No transDate provided, keeping existing value: " + values.get("transDate"));
	    }

	    APIRequestHandler handler = getAPIRequestHandler();
	    entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH,
	            entry.getExternalReferenceCode(), consentObjectDefinitionId);

	    new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(entry, "CBS_APPROVED_URN_" + urn,
	            workflowObjectDefinitionId);

	    return createSuccessResponse(entry, 3, "Consent approved by CBS. URN: " + urn);
	}

	private ConsentResponse processCBSRejection(ObjectEntry entry, Map<String, Serializable> values, String drn,
	        String urn, String transDate) throws Exception {
	    
	    LOG.info("📌 processCBSRejection - Updating URN and transDate");
	    
	    values.put(Params.URN, urn);
	    values.put(Params.DEL_FLAG, "N");
	    values.put("cbsStatus", "REJECTED");

	    // ✅ FIX: Use SAME logic as convertMetadataToValues()
	    if (Validator.isNotNull(transDate)) {
	        try {
	            LOG.info("📌 Parsing transDate: " + transDate);
	            
	            String datePart = transDate;
	            if (datePart.contains("T")) {
	                datePart = datePart.split("T")[0];
	            }
	            if (datePart.contains(" ")) {
	                datePart = datePart.split(" ")[0];
	            }
	            if (datePart.contains("+")) {
	                datePart = datePart.split("\\+")[0];
	            }
	            if (datePart.contains("Z")) {
	                datePart = datePart.replace("Z", "");
	            }
	            
	            LOG.info("📌 Cleaned datePart: " + datePart);
	            
	            Date date = Date.from(
	                LocalDate.parse(datePart).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()
	            );
	            values.put("transDate", date);
	            LOG.info("✅ Updated transDate to: " + date);
	            
	        } catch (Exception e) {
	            LOG.warn("⚠️ Failed to parse transDate: " + transDate + " - " + e.getMessage());
	        }
	    } else {
	        LOG.info("📌 No transDate provided, keeping existing value: " + values.get("transDate"));
	    }

	    APIRequestHandler handler = getAPIRequestHandler();
	    entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH,
	            entry.getExternalReferenceCode(), consentObjectDefinitionId);

	    new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(entry, "CBS_REJECTED_URN_" + urn,
	            workflowObjectDefinitionId);

	    return createSuccessResponse(entry, 3, "Consent rejected by CBS. URN: " + urn);
	}

	private ConsentResponse processCBSDeletion(ObjectEntry entry, Map<String, Serializable> values, String drn,
	        String urn, String transDate) throws Exception {
	    
	    LOG.info("📌 processCBSDeletion - Updating URN and transDate");
	    
	    values.put(Params.URN, urn);
	    values.put(Params.DEL_FLAG, "Y");
	    values.put(Params.STAGE, 4);
	    values.put("cbsStatus", "DELETED");

	    // ✅ FIX: Use SAME logic as convertMetadataToValues() (optional for deletion)
	    if (Validator.isNotNull(transDate)) {
	        try {
	            LOG.info("📌 Parsing transDate: " + transDate);
	            
	            String datePart = transDate;
	            if (datePart.contains("T")) {
	                datePart = datePart.split("T")[0];
	            }
	            if (datePart.contains(" ")) {
	                datePart = datePart.split(" ")[0];
	            }
	            if (datePart.contains("+")) {
	                datePart = datePart.split("\\+")[0];
	            }
	            if (datePart.contains("Z")) {
	                datePart = datePart.replace("Z", "");
	            }
	            
	            LOG.info("📌 Cleaned datePart: " + datePart);
	            
	            Date date = Date.from(
	                LocalDate.parse(datePart).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()
	            );
	            values.put("transDate", date);
	            LOG.info("✅ Updated transDate to: " + date);
	            
	        } catch (Exception e) {
	            LOG.warn("⚠️ Failed to parse transDate: " + transDate + " - " + e.getMessage());
	        }
	    } else {
	        LOG.info("📌 No transDate provided, keeping existing value: " + values.get("transDate"));
	    }

	    APIRequestHandler handler = getAPIRequestHandler();
	    entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH,
	            entry.getExternalReferenceCode(), consentObjectDefinitionId);

	    new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(entry, "CBS_DELETED_URN_" + urn,
	            workflowObjectDefinitionId);

	    return createSuccessResponse(entry, 4, "Consent has been deleted. URN: " + urn);
	}

	// ==================== API 4: Save as Draft (100% PMSBY Pattern)
	// ====================
	@Override
	public BOIHeadlessResponse addConsentAsDraft(MultipartBody multipartBody) throws Exception {
		LOG.info("=== addConsentAsDraft START ===");

		APIRequestHandler handler = getAPIRequestHandler();
		JSONObject json = JSONFactoryUtil.createJSONObject();

		validateMakerRole(handler.getUserHierarchy());
		fileValidation(multipartBody);

		long entryId = ParamUtil.getLong(contextHttpServletRequest, Params.ENTRY_ID);
		ObjectEntry entry = handler.getObjectEntry(entryId);

		String cbsReferenceNumber = ParamUtil.getString(contextHttpServletRequest, Params.CBS_REFERENCE_NUMBER);

		if (entry == null && Validator.isNotNull(cbsReferenceNumber)) {
			entry = findByCBSReference(cbsReferenceNumber);
		}

		String refCode = entry == null ? null : entry.getExternalReferenceCode();
		Map<String, Serializable> values = entry == null ? createDefaultValues(handler.getUserHierarchy())
				: entry.getValues();

		values = getDefaultData(contextHttpServletRequest, values, consentObjectDefinitionId, _objectFieldLocalService,
				null);

		if (entry == null) {
			values.put(Params.STAGE, 0);
		}

		handler.updaloadFile(values, FILE_UPLOAD_MAP, multipartBody, contextHttpServletRequest,
				consentObjectDefinitionId, StringPool.BLANK, true);
		entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, refCode,
				consentObjectDefinitionId);

		// ✅ PMSBY PATTERN: Only completeWorkflowLogs (NO extra CustomWorkflowLogs)
		completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);

		json.put(Params.ENTRY_ID, entry.getObjectEntryId());

		LOG.info("=== addConsentAsDraft END ===");
		return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
	}

	// ==================== API 5: Save as Export (100% PMSBY Pattern)
	// ====================
	@Override
	public BOIHeadlessResponse addConsentAsExport(MultipartBody multipartBody) throws Exception {
		LOG.info("=== addConsentAsExport START ===");

		APIRequestHandler handler = getAPIRequestHandler();
		JSONObject json = JSONFactoryUtil.createJSONObject();

		long entryId = ParamUtil.getLong(contextHttpServletRequest, Params.ENTRY_ID);
		ObjectEntry entry = handler.getObjectEntry(entryId);

		if (entry == null) {
			String cbsReferenceNumber = ParamUtil.getString(contextHttpServletRequest, Params.CBS_REFERENCE_NUMBER);
			if (Validator.isNotNull(cbsReferenceNumber)) {
				entry = findByCBSReference(cbsReferenceNumber);
				LOG.info("Found entry by CBS Reference: " + cbsReferenceNumber);
			}
		}

		if (entry == null) {
			LOG.error("Entry not found for entryId: " + entryId);
			throw new Exception("Consent record not found");
		}

		Map<String, Serializable> values = entry.getValues();
		// ✅ PMSBY PATTERN: Get refCode
		String refCode = entry.getExternalReferenceCode();
		UserHierarchy hierarchy = handler.getUserHierarchy();
		int stage = GetterUtil.getInteger(values.get(Params.STAGE));

		// ✅ PMSBY PATTERN: Pass refCode to methods
		if (hierarchy.isMaker() && (stage == 0 || stage == 1)) {
			handleMakerSubmission(handler, entry, values, refCode, multipartBody);
		}

		if (hierarchy.isBranchChecker() && stage == 2) {
			handleCheckerAction(handler, entry, values, refCode, multipartBody);
		}

		json.put(Params.ENTRY_ID, entry.getObjectEntryId());

		LOG.info("=== addConsentAsExport END ===");
		return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
	}

	// ==================== API 6: Get Consent Records (100% PMSBY Pattern)
	@Override
	public BOIHeadlessResponse getConsentRecords(String search, Filter filter, Pagination pagination, Sort[] sorts)
			throws Exception {

		LOG.info("=== getConsentRecords START ===");

		Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
		sorts = new Sort[] { sort };

		APIRequestHandler handler = getAPIRequestHandler();
		ObjectDefinition objectDefinition = handler.getObjectDefinition(consentObjectDefinitionId);

		boolean forceDbQuery = ParamUtil.getBoolean(contextHttpServletRequest, Params.FORCE_DB_QUERY, false);

		Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page;

		if (forceDbQuery) {
			LOG.info("Using database query (forced)");
			page = getDatabaseEntries(handler, pagination);
		} else {
			page = performSearchWithFallback(handler, pagination, objectDefinition);
		}

		LOG.info("Final query result. TotalCount=" + page.getTotalCount());
		LOG.info("=== getConsentRecords END ===");

		// ✅ PMSBY PATTERN: Status set
		return new CustomBOIHeadlessResponseAsList(page, handler.getUserHierarchy().isMaker(), "",
				Set.of("initiated", "pending with branch maker", "pending with branch checker", "approved", "deleted"));
	}

	// ==================== API 7: Get Single Consent Record (100% PMSBY Pattern)
	@Override
	public BOIHeadlessResponse getConsentRecord(@NotNull Integer id) throws Exception {
		LOG.info("=== getConsentRecord START id=" + id + " ===");

		APIRequestHandler handler = getAPIRequestHandler();
		ObjectEntry entry = handler.getObjectEntry(id.longValue());

		JSONObject json = handler.convertEntryToArray(entry.getValues());
		formatDateFields(json, "transDate", "cbsVerificationDate");
		int stage = GetterUtil.getInteger(entry.getValues().get(Params.STAGE));
		json.put("status", getStatusLabel(stage));
		json.put(Params.ENTRY_ID, entry.getObjectEntryId());
		handler.addWorkflowLogs(json, entry.getObjectEntryId(), workflowObjectDefinitionId);

		addStageActions(handler, entry, json);

		LOG.info("=== getConsentRecord END ===");
		return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
	}

	// ==================== Private Helper Methods ====================

	private void validateMakerRole(UserHierarchy hierarchy) throws Exception {
		if (!hierarchy.isMaker()) {
			LOG.warn("User not Maker. Draft creation denied.");
			throw new Exception("Only Branch Maker can create draft");
		}
	}

	private void formatDateFields(JSONObject json, String... dateFields) {

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for (String field : dateFields) {

			if (json.has(field) && !json.isNull(field)) {

				String value = json.getString(field);

				if (Validator.isNotNull(value)) {

					LocalDateTime localDateTime = LocalDateTime.parse(value, inputFormatter);

					String formattedDate = localDateTime.format(outputFormatter);

					json.put(field, formattedDate);
				}
			}
		}
	}

	private void validateMetadataRequest(CBSMetadataRequest request) {
		if (Validator.isNull(request.getCbsReferenceNumber())) {
			throw new BadRequestException("cbsReferenceNumber is required");
		}
		if (Validator.isNull(request.getAccountNumber())) {
			throw new BadRequestException("accountNumber is required");
		}
		if (Validator.isNull(request.getCustomerName())) {
			throw new BadRequestException("customerName is required");
		}
		if (Validator.isNull(request.getBranchCode())) {
			throw new BadRequestException("branchCode is required");
		}
		if (Validator.isNull(request.getTypeOfInsurance())) {
			throw new BadRequestException("typeOfInsurance is required");
		}
	}

	private Map<String, Serializable> createDefaultValues(UserHierarchy userHierarchy) {
		Map<String, Serializable> values = new HashMap<>(8);
		values.put(Params.BRANCH_CODE, userHierarchy.getBranchName());
		values.put(Params.REGIONAL_OFFICE, userHierarchy.getRegionalOfficeName());
		return values;
	}

	private Map<String, Serializable> convertMetadataToValues(CBSMetadataRequest request) {
		Map<String, Serializable> values = new HashMap<>(24);

		values.put(Params.CBS_REFERENCE_NUMBER, request.getCbsReferenceNumber());
		values.put(Params.ACCOUNT_NUMBER, request.getAccountNumber());
		values.put("branch", nullToBlank(request.getBranch()));

		String incomingBranchCode = nullToBlank(request.getBranchCode());
		String incomingBranchName = nullToBlank(request.getBranch());
		String incomingRegionalOffice = nullToBlank(request.getRegionalOffice());

		String branchPrefix = extractFirstFourDigits(incomingBranchCode);
		if (Validator.isNull(branchPrefix)) {
			branchPrefix = extractFirstFourDigits(incomingBranchName);
		}

		Map<String, String> correctHierarchy = getCachedHierarchy(branchPrefix);

		if (!correctHierarchy.isEmpty()) {
			String correctBranchCode = correctHierarchy.getOrDefault("branchCode", incomingBranchCode);
			String correctBranchName = correctHierarchy.getOrDefault("branch", incomingBranchName);
			String correctRegionalOffice = correctHierarchy.getOrDefault("regionalOffice", incomingRegionalOffice);

			String cleanedBranchName = cleanBranchName(correctBranchCode, correctBranchName);
			String fullBranchName = correctBranchCode + " - " + cleanedBranchName;

			values.put(Params.BRANCH_CODE, fullBranchName);
			values.put("branch", cleanedBranchName);
			values.put(Params.REGIONAL_OFFICE, correctRegionalOffice);

			values.put("originalBranchCode", incomingBranchCode);
			values.put("originalRegionalOffice", incomingRegionalOffice);
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("⚠️ No organization found for prefix: " + branchPrefix + " - Using incoming values");
			}

			String branchCode = incomingBranchCode;
			String branchName = incomingBranchName;
			if (Validator.isNotNull(branchCode) && Validator.isNotNull(branchName)) {
				values.put(Params.BRANCH_CODE, branchCode + " - " + branchName);
			} else if (Validator.isNotNull(branchCode)) {
				values.put(Params.BRANCH_CODE, branchCode);
			} else {
				values.put(Params.BRANCH_CODE, branchName);
			}
			values.put("branch", branchName);
			values.put(Params.REGIONAL_OFFICE, incomingRegionalOffice);
		}

		values.put(Params.CUSTOMER_ID, nullToBlank(request.getCustomerId()));
		values.put("customerName", request.getCustomerName());
		values.put("consentFormDocumentFrom", nullToBlank(request.getConsentFormDocumentFrom()));
		values.put("consentFormDocumentTo", nullToBlank(request.getConsentFormDocumentTo()));
		values.put("solId", nullToBlank(request.getSolId()));
		values.put(Params.TYPE_OF_INSURANCE, request.getTypeOfInsurance());
		values.put("transDate",
				Validator.isNotNull(request.getTransDate()) ? Date.from(
						LocalDate.parse(request.getTransDate()).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant())
						: null);
		values.put(Params.YEAR, nullToBlank(request.getYear()));
		values.put(Params.URN, nullToBlank(request.getUrn()));

		return values;
	}
	
	// ==================== HANDLE MAKER SUBMISSION (100% PMSBY Pattern)
	// ====================
	private void handleMakerSubmission(APIRequestHandler handler, ObjectEntry entry, Map<String, Serializable> values,
			String refCode, MultipartBody multipartBody) throws Exception {

		LOG.info("Maker submitting to Checker. EntryId=" + entry.getObjectEntryId());

		values = getDefaultData(contextHttpServletRequest, values, consentObjectDefinitionId, _objectFieldLocalService,
				null);
		values.put(Params.STAGE, 2);
		handler.updaloadFile(values, FILE_UPLOAD_MAP, multipartBody, contextHttpServletRequest,
				consentObjectDefinitionId, StringPool.BLANK, true);

		entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, refCode,
				consentObjectDefinitionId);

		// ✅ PMSBY PATTERN: ONLY completeWorkflowLogs (NO extra CustomWorkflowLogs)
		completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);
	}

	// ==================== HANDLE CHECKER ACTION (100% PMSBY Pattern)
	// ====================
	private void handleCheckerAction(APIRequestHandler handler, ObjectEntry entry, Map<String, Serializable> values,
			String refCode, MultipartBody multipartBody) throws Exception {

		String transition = ParamUtil.getString(contextHttpServletRequest, Params.TRANSITION_ID);

		if ("Approve".equalsIgnoreCase(transition)) {
			LOG.info("Checker Approving. EntryId=" + entry.getObjectEntryId());

			String drn = generateDRN(values);
			values.put(Params.DOCUMENT_REFERENCE_NO, drn);
			values.put(Params.STAGE, 3);

			entry = handler.updateData(contextUser, values, WorkflowConstants.STATUS_APPROVED, refCode,
					consentObjectDefinitionId);

			// ✅ PMSBY PATTERN: ONLY completeWorkflowLogs (NO extra CustomWorkflowLogs)
			completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);

		} else if ("Reject".equalsIgnoreCase(transition)) {
			LOG.info("Checker Rejecting. EntryId=" + entry.getObjectEntryId());
			values.put(Params.STAGE, 1);
			values.put(Params.DOCUMENT_REFERENCE_NO, StringPool.BLANK);

			entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, refCode,
					consentObjectDefinitionId);

			// ✅ PMSBY PATTERN: ONLY completeWorkflowLogs (NO extra CustomWorkflowLogs)
			completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);
		}
	}

	// ==================== GENERATE DRN ====================
	private String generateDRN(Map<String, Serializable> values) {
		String date = LocalDate.now().format(DATE_TIME_FORMATTER.get());
		String branchCode = (String) values.get(Params.BRANCH_CODE);
		String accountNo = (String) values.get(Params.ACCOUNT_NUMBER);
		String accountLast4 = accountNo.length() >= 4 ? accountNo.substring(accountNo.length() - 4) : accountNo;
		String random = String.format("%04d", RANDOM.get().nextInt(9999));
		return "DRN_" + date + "_" + branchCode + "_" + accountLast4 + "_" + random;
	}

	private String getStatusLabel(int stage) {
		return switch (stage) {
		case 0 -> "initiated";
		case 1 -> "pending with branch maker";
		case 2 -> "pending with branch checker";
		case 3 -> "approved";
		case 4 -> "deleted";
		default -> "initiated";
		};
	}

	// ==================== RESPONSE HELPERS ====================
	private ConsentResponse createErrorResponse(String message) {
		ConsentResponse response = new ConsentResponse();
		response.setSuccess(false);
		response.setMessage(message);
		return response;
	}

	private ConsentResponse createSuccessResponse(ObjectEntry entry, int stage, String message) {
		ConsentResponse response = new ConsentResponse();
		response.setSuccess(true);
		response.setEntryId(entry.getObjectEntryId());
		response.setCbsReferenceNumber((String) entry.getValues().get(Params.CBS_REFERENCE_NUMBER));
		response.setStage(stage);
		response.setMessage(message);
		return response;
	}

	private APIRequestHandler getAPIRequestHandler() {
		return new APIRequestHandler(contextUser, contextHttpServletRequest, contextAcceptLanguage,
				_objectFieldLocalService, _dlAppService, contextUriInfo, _dtoConverterRegistry,
				_objectEntryManagerRegistry, _objectDefinitionLocalService, _objectEntryLocalService,
				_workflowTaskManager, boiWorkflowConfiguation, BOIDMSConstants.RRB_CONSENT, defaultFolderId);
	}

	// ==================== WORKFLOW LOGS ====================
	public void completeWorkflowLogs(HttpServletRequest request, APIRequestHandler apiRequestHandler,
			MultipartBody multipartBody, ObjectEntry objectEntry) {

		long workflowTaskId = ParamUtil.getLong(request, Params.WORKFLOW_TASK_ID, 0L);

		if (workflowTaskId == 0) {
			createExportLog(objectEntry);
			return;
		}

		try {
			String transitionId = ParamUtil.getString(request, Params.TRANSITION_ID);
			String comment = resolveComment(request, transitionId);

			Map<String, Serializable> values = new HashMap<>(8);
			attachFileIfPresent(apiRequestHandler, multipartBody, values);

			values.put(Params.CUSTOM_ACTION, transitionId);
			values.put(Params.TRANSITION_NAME, transitionId);
			values.put(Params.REMARKS, comment);
			values.put(Params.WORKFLOW_TASK_ID, workflowTaskId);
			values.put(Params.OBJECT_ENTRY_ID, objectEntry.getObjectEntryId());

			persistWorkflowLog(values);

		} catch (Exception e) {
			LOG.error("Error while completing workflow logs", e);
		}
	}

	private void createExportLog(ObjectEntry objectEntry) {
		new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(objectEntry, "Export", workflowObjectDefinitionId);
	}

	private String resolveComment(HttpServletRequest request, String transitionId) {
		String comment = ParamUtil.getString(request, Params.REMARKS);
		if (!Validator.isBlank(comment)) {
			return comment;
		}
		String decision = ParamUtil.getString(request, Params.DECISION);
		if ("Save".equalsIgnoreCase(transitionId)) {
			return Validator.isNotNull(decision) ? "Decision changed with " + decision : "Save action performed";
		}
		return StringPool.BLANK;
	}

	private void attachFileIfPresent(APIRequestHandler apiRequestHandler, MultipartBody multipartBody,
			Map<String, Serializable> values) throws Exception {

		BinaryFile binaryFile = multipartBody.getBinaryFile(Params.ATTACHMENTS);
		if (Validator.isNull(binaryFile))
			return;

		File file = apiRequestHandler.createFileFromInputStream(binaryFile.getInputStream(), binaryFile.getFileName());

		Folder uploadFolder = apiRequestHandler.getUserHierarchy().getUploadFolder();
		if (Validator.isNull(uploadFolder))
			return;

		FileHandler fileHandler = new FileHandler(_dlAppService);
		FileEntry fileEntry = fileHandler.createOrReplaceFileEntry(uploadFolder, file,
				apiRequestHandler.appendTimestamp(binaryFile.getFileName(), null, false));

		values.put("attachment", getFileEntryJson(fileEntry));
	}

	private String getFileEntryJson(FileEntry entry) {
		try {
			String downloadURL = DLURLHelperUtil.getDownloadURL(entry, entry.getFileVersion(), null, null);
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			jsonObject.put("URL", downloadURL);
			jsonObject.put("fileId", entry.getFileEntryId());
			jsonObject.put("fileName", entry.getFileName());
			jsonObject.put("fileExtension", entry.getExtension());
			return jsonObject.toJSONString();
		} catch (PortalException e) {
			return String.valueOf(entry.getFileEntryId());
		}
	}

	private void persistWorkflowLog(Map<String, Serializable> values) throws PortalException {
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(contextUser.getCompanyId());
		serviceContext.setUserId(contextUser.getUserId());
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

		ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(null, contextUser.getUserId(), 0, workflowObjectDefinitionId,
				values, serviceContext);
	}

	// ==================== SEARCH METHODS ====================

	private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> performSearchWithFallback(APIRequestHandler handler,
			Pagination pagination, ObjectDefinition objectDefinition) throws Exception {
		SearchResponse searchResponse = executeHybridSearch(objectDefinition, pagination);
		if (searchResponse == null) {
			LOG.warn("Elasticsearch failed, falling back to database query");
			return getDatabaseEntries(handler, pagination);
		}
		return convertSearchResponseToPage(handler, objectDefinition, pagination, searchResponse);
	}

	private SearchResponse executeHybridSearch(ObjectDefinition objectDefinition, Pagination pagination) {
		SearchResponse searchResponse = searchObjectEntriesWithNestedQuery(objectDefinition, pagination);
		if (searchResponse == null || searchResponse.getTotalHits() == 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Nested search returned no results");
			}
		}
		return searchResponse;
	}

	private SearchResponse searchObjectEntriesWithNestedQuery(ObjectDefinition objectDefinition,
			Pagination pagination) {
		try {
			int from = (pagination.getPage() - 1) * pagination.getPageSize();
			SearchFilters filters = extractSearchFilters();

			BooleanQuery mainQuery = buildBaseQuery(objectDefinition);
			addNestedFilters(mainQuery, filters);
			addDateRangeFilter(mainQuery, filters.fromDateStr, filters.toDateStr);

			SearchRequest searchRequest = buildSearchRequest(from, pagination.getPageSize(), mainQuery);

			long startTime = System.currentTimeMillis();
			SearchResponse searchResponse = _searcher.search(searchRequest);

			if (LOG.isInfoEnabled()) {
				LOG.info("Nested search completed in " + (System.currentTimeMillis() - startTime) + "ms. Total hits: "
						+ searchResponse.getTotalHits());
			}

			return searchResponse;
		} catch (Exception e) {
			LOG.error("Error executing nested search", e);
			return null;
		}
	}

	private BooleanQuery buildBaseQuery(ObjectDefinition objectDefinition) {
		BooleanQuery query = _queries.booleanQuery();
		query.addMustQueryClauses(_queries.term("objectDefinitionId", objectDefinition.getObjectDefinitionId()));
		query.addMustQueryClauses(_queries.term("entryClassName", objectDefinition.getClassName()));
		return query;
	}

	private void addNestedFilters(BooleanQuery mainQuery, SearchFilters filters) {
		addNestedHierarchyFilter(mainQuery, filters);

		addNestedStringFilter(mainQuery, Params.CBS_REFERENCE_NUMBER, filters.cbsReferenceNumber);
		addNestedStringFilter(mainQuery, Params.CUSTOMER_ID, filters.customerId);
		addNestedStringFilter(mainQuery, Params.ACCOUNT_NUMBER, filters.accountNumber);
		addNestedStringFilter(mainQuery, Params.REGIONAL_OFFICE, filters.regionalOffice);
		addNestedStringFilter(mainQuery, Params.TYPE_OF_INSURANCE, filters.typeOfInsurance);
		addNestedStringFilter(mainQuery, Params.URN, filters.urn);
		addNestedStringFilter(mainQuery, Params.YEAR, filters.year);
		addNestedIntegerFilter(mainQuery, Params.STAGE, filters.stage);
	}

	private void addNestedHierarchyFilter(BooleanQuery mainQuery, SearchFilters filters) {
		UserHierarchy hierarchy = filters.userHierarchy;
		if (hierarchy == null)
			return;

		String branchCode = hierarchy.getBranchName();
		if (Validator.isNotNull(branchCode)) {
			String branchPrefix = extractFirstFourDigits(branchCode);
			if (Validator.isNotNull(branchPrefix)) {
				addNestedBranchPrefixFilter(mainQuery, Params.BRANCH_CODE, branchPrefix);
			}
		}
	}

	private void addNestedBranchPrefixFilter(BooleanQuery mainQuery, String fieldName, String prefix) {
		if (Validator.isNull(prefix))
			return;

		BooleanQuery innerQuery = _queries.booleanQuery();
		innerQuery.addMustQueryClauses(_queries.term(NestedField.ARRAY + "." + NestedField.NAME, fieldName));
		innerQuery.addMustQueryClauses(
				_queries.prefix(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, prefix.toLowerCase()));
		mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
	}

	private void addNestedStringFilter(BooleanQuery mainQuery, String fieldName, String value) {
		if (Validator.isNull(value))
			return;

		BooleanQuery innerQuery = _queries.booleanQuery();
		innerQuery.addMustQueryClauses(_queries.term(NestedField.ARRAY + "." + NestedField.NAME, fieldName),
				_queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, value.toLowerCase()));
		mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
	}

	private void addNestedIntegerFilter(BooleanQuery mainQuery, String fieldName, Integer value) {
		if (value == null)
			return;

		BooleanQuery innerQuery = _queries.booleanQuery();
		innerQuery.addMustQueryClauses(_queries.term(NestedField.ARRAY + "." + NestedField.NAME, fieldName),
				_queries.term(NestedField.ARRAY + "." + NestedField.VALUE_INTEGER, value));
		mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
	}

	private void addDateRangeFilter(BooleanQuery mainQuery, String fromDateStr, String toDateStr) {
		if (Validator.isNull(fromDateStr) || Validator.isNull(toDateStr))
			return;

		try {
			long fromTimestamp = convertToTimestamp(fromDateStr, true);
			long toTimestamp = convertToTimestamp(toDateStr, false);

			RangeTermQuery dateRangeQuery = _queries.rangeTerm("createDate_sortable", true, true, fromTimestamp,
					toTimestamp);
			mainQuery.addMustQueryClauses(dateRangeQuery);
		} catch (Exception e) {
			LOG.error("Error adding date filter", e);
		}
	}

	private long convertToTimestamp(String dateStr, boolean isStartOfDay) {
		LocalDate date = LocalDate.parse(dateStr);
		ZonedDateTime zonedDateTime = isStartOfDay ? date.atStartOfDay(ZoneOffset.UTC)
				: date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
		return zonedDateTime.toInstant().toEpochMilli();
	}

	private SearchRequest buildSearchRequest(int from, int pageSize, BooleanQuery query) {
		return _searchRequestBuilderFactory.builder().companyId(contextCompany.getCompanyId()).emptySearchEnabled(true)
				.from(from).size(pageSize).fetchSource(true).fields(SEARCH_FIELDS).withSearchContext(searchContext -> {
					searchContext.setStart(from);
					searchContext.setEnd(from + pageSize);
					searchContext.setGroupIds(new long[] { 0 });
					searchContext.setAttribute("companyId", contextCompany.getCompanyId());
				}).query(query).build();
	}

	private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> convertSearchResponseToPage(APIRequestHandler handler,
			ObjectDefinition objectDefinition, Pagination pagination, SearchResponse searchResponse) {

		SearchHits searchHits = searchResponse.getSearchHits();
		if (searchHits == null || searchHits.getSearchHits() == null) {
			return Page.of(new ArrayList<>(), pagination, 0);
		}

		List<com.liferay.object.rest.dto.v1_0.ObjectEntry> objectEntries = convertSearchHitsToObjectEntries(handler,
				objectDefinition, searchHits);

		return Page.of(objectEntries, pagination, searchResponse.getTotalHits());
	}

	private List<com.liferay.object.rest.dto.v1_0.ObjectEntry> convertSearchHitsToObjectEntries(
			APIRequestHandler apiRequestHandler, ObjectDefinition objectDefinition, SearchHits searchHits) {

		List<SearchHit> hits = searchHits.getSearchHits();
		List<com.liferay.object.rest.dto.v1_0.ObjectEntry> objectEntries = new ArrayList<>(hits.size());

		for (SearchHit searchHit : hits) {
			try {
				Document document = searchHit.getDocument();
				if (document == null)
					continue;

				long entryClassPK = resolveEntryClassPK(document);
				if (entryClassPK <= 0)
					continue;

				com.liferay.object.model.ObjectEntry serviceBuilderEntry = _objectEntryLocalService
						.fetchObjectEntry(entryClassPK);

				if (serviceBuilderEntry == null
						|| serviceBuilderEntry.getObjectDefinitionId() != objectDefinition.getObjectDefinitionId()) {
					continue;
				}

				com.liferay.object.rest.dto.v1_0.ObjectEntry restObjectEntry = toRestObjectEntry(apiRequestHandler,
						objectDefinition, serviceBuilderEntry);

				if (restObjectEntry != null) {
					objectEntries.add(restObjectEntry);
				}
			} catch (Exception e) {
				LOG.error("Error processing search hit", e);
			}
		}

		return objectEntries;
	}

	private long resolveEntryClassPK(Document document) {
		try {
			Long entryClassPKValue = document.getLong(Params.ENTRY_CLASS_PK);
			if (entryClassPKValue != null && entryClassPKValue > 0) {
				return entryClassPKValue;
			}
			String uid = document.getString(Params.UID);
			return extractEntryClassPKFromUid(uid);
		} catch (Exception e) {
			return 0;
		}
	}

	private long extractEntryClassPKFromUid(String uid) {
		if (Validator.isNull(uid))
			return 0;
		int index = uid.lastIndexOf(StringPool.UNDERLINE);
		if ((index >= 0) && (index < (uid.length() - 1))) {
			return GetterUtil.getLong(uid.substring(index + 1));
		}
		return 0;
	}

	private com.liferay.object.rest.dto.v1_0.ObjectEntry toRestObjectEntry(APIRequestHandler apiRequestHandler,
			ObjectDefinition objectDefinition, com.liferay.object.model.ObjectEntry serviceBuilderEntry) {

		try {
			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
					.getObjectEntryManager(objectDefinition.getStorageType());

			DTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(_dtoConverterRegistry,
					serviceBuilderEntry.getObjectEntryId(), Locale.getDefault(), null, null);

			return objectEntryManager.getObjectEntry(objectDefinition.getCompanyId(), dtoConverterContext,
					String.valueOf(serviceBuilderEntry.getExternalReferenceCode()), objectDefinition, null);
		} catch (Exception e) {
			LOG.error("Unable to convert ObjectEntry to REST DTO for PK: " + serviceBuilderEntry.getObjectEntryId(), e);
		}
		return null;
	}

	// ==================== DATABASE METHODS ====================

	private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> getDatabaseEntries(APIRequestHandler apiRequestHandler,
			Pagination pagination) throws Exception {

		ObjectDefinition def = apiRequestHandler.getObjectDefinition(consentObjectDefinitionId);
		ObjectEntryManager manager = _objectEntryManagerRegistry.getObjectEntryManager(def.getStorageType());

		String query = buildDatabaseQuery(apiRequestHandler);

		return manager.getObjectEntries(def.getCompanyId(), def, def.getScope(), null,
				apiRequestHandler.getDTOConverterContext(null), query, pagination, null, null);
	}

	private String buildDatabaseQuery(APIRequestHandler handler) {
		List<String> filters = new ArrayList<>(6);
		UserHierarchy hierarchy = handler.getUserHierarchy();

		filters.add("stage ne 4");
		filters.add("delFlag ne 'Y'");

		String branchCode = hierarchy.getBranchName();
		if (Validator.isNotNull(branchCode)) {
			String branchPrefix = extractFirstFourDigits(branchCode);
			if (Validator.isNotNull(branchPrefix)) {
				filters.add("startswith(" + Params.BRANCH_CODE + ", '" + branchPrefix + "')");
			}
		}

		String status = ParamUtil.getString(contextHttpServletRequest, Params.STATUS);
		if (Validator.isNotNull(status)) {
			Integer stageValue = STATUS_TO_STAGE_MAP.get(status.toLowerCase());
			if (stageValue != null) {
				filters.add(Params.STAGE + " eq " + stageValue);
			}
		}

		String fromDateStr = ParamUtil.getString(contextHttpServletRequest, Params.FROM_DATE);
		String toDateStr = ParamUtil.getString(contextHttpServletRequest, Params.TO_DATE);
		if (Validator.isNotNull(fromDateStr) && Validator.isNotNull(toDateStr)) {
			filters.add("createDate ge " + fromDateStr + " and createDate le " + toDateStr);
		}

		String query = String.join(" and ", filters);
		if (LOG.isInfoEnabled()) {
			LOG.info("Database query: " + query);
		}
		return query;
	}

	// ==================== SEARCH FILTERS ====================

	private SearchFilters extractSearchFilters() {
		SearchFilters filters = new SearchFilters();

		filters.cbsReferenceNumber = cleanParameterValue(
				ParamUtil.getString(contextHttpServletRequest, Params.CBS_REFERENCE_NUMBER));
		filters.customerId = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.CUSTOMER_ID));
		filters.accountNumber = cleanParameterValue(
				ParamUtil.getString(contextHttpServletRequest, Params.ACCOUNT_NUMBER));
		filters.branchCode = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.BRANCH_CODE));
		filters.regionalOffice = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.REGIONAL_OFFICE));
		filters.typeOfInsurance = cleanParameterValue(
				ParamUtil.getString(contextHttpServletRequest, Params.TYPE_OF_INSURANCE));
		filters.urn = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.URN));
		filters.year = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.YEAR));
		filters.fromDateStr = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.FROM_DATE));
		filters.toDateStr = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.TO_DATE));
		filters.applyHierarchy = ParamUtil.getBoolean(contextHttpServletRequest, Params.APPLY_HIERARCHY, true);

		String status = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.STATUS));
		if (Validator.isNotNull(status)) {
			filters.stage = STATUS_TO_STAGE_MAP.get(status.toLowerCase());
		}

		filters.userHierarchy = getAPIRequestHandler().getUserHierarchy();
		return filters;
	}

	private String cleanParameterValue(String value) {
		if (Validator.isNull(value))
			return value;
		String trimmed = value.trim();
		if ((trimmed.startsWith("'") && trimmed.endsWith("'"))
				|| (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
			return trimmed.substring(1, trimmed.length() - 1);
		}
		return trimmed;
	}

	// ==================== STAGE ACTIONS ====================

	private void addStageActions(APIRequestHandler handler, ObjectEntry entry, JSONObject json) {
		JSONArray actions = JSONFactoryUtil.createJSONArray();
		int stage = GetterUtil.getInteger(entry.getValues().get(Params.STAGE));

		if (stage == 1 && handler.getUserHierarchy().isMaker()) {
			addAction(actions, "ReSubmit", Params.TRANSITION_ID, "ReSubmit", entry.getObjectEntryId());
		}

		if (stage == 2 && handler.getUserHierarchy().isBranchChecker()) {
			addAction(actions, "Approve", Params.TRANSITION_ID, "Approve", entry.getObjectEntryId());
			addAction(actions, "Reject", Params.TRANSITION_ID, "Reject", entry.getObjectEntryId());
		}

		json.put("actions", actions);
	}

	private void addAction(JSONArray actions, String actionName, String paramName, String paramValue, long entryId) {
		JSONObject action = JSONFactoryUtil.createJSONObject();
		action.put("actionName", actionName);
		action.put(paramName, paramValue);
		action.put(Params.WORKFLOW_TASK_ID, entryId);
		actions.put(action);
	}

	// ==================== ACTIVATION ====================
	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);

		defaultFolderId = boiWorkflowConfiguation.HODefualtFolderId();
		consentObjectDefinitionId = boiWorkflowConfiguation.ConsentObjectDefinitionId();
		workflowObjectDefinitionId = boiWorkflowConfiguation.ConsentWorkflowLogsId();

		if (LOG.isInfoEnabled()) {
			LOG.info("=== ACTIVATION ===");
			LOG.info("Consent Object Definition ID: " + consentObjectDefinitionId);
			LOG.info("Workflow Object Definition ID: " + workflowObjectDefinitionId);
			LOG.info("Default Folder ID: " + defaultFolderId);
		}
	}

	// ==================== SearchFilters Inner Class ====================
	private static class SearchFilters {
		String cbsReferenceNumber;
		String customerId;
		String accountNumber;
		String branchCode;
		String regionalOffice;
		String typeOfInsurance;
		String urn;
		String year;
		String fromDateStr;
		String toDateStr;
		boolean applyHierarchy;
		Integer stage;
		UserHierarchy userHierarchy;
	}

	public static Map<String, Serializable> getDefaultData(HttpServletRequest httpServletRequest,
			Map<String, Serializable> values, long objectDefinitionId, ObjectFieldLocalService _objectFieldLocalService,
			List<String> onlyWithTheseFields) {

		List<ObjectField> fields = _objectFieldLocalService.getObjectFields(objectDefinitionId);
		if (Validator.isNotNull(onlyWithTheseFields) && (!onlyWithTheseFields.isEmpty())) {
			fields = fields.stream().filter(field -> onlyWithTheseFields.contains(field.getName()))
					.collect(Collectors.toList());
		}
		for (ObjectField objectField : fields) {
			String fieldName = objectField.getName();
			LOG.info(fieldName + " - " + objectField.getDBType());
			if (objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_STRING)
					|| objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_CLOB)) {
				String value = ParamUtil.getString(httpServletRequest, fieldName, StringPool.BLANK);
				value = (String) getRestrictedValues(fieldName, values, value);
				if (value.equalsIgnoreCase(StringPool.BLANK)) {
					value = (String) getDefaultData(fieldName, values, StringPool.BLANK);
				}
				values.put(fieldName, value);
			}
			if (objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_DATE)) {
				LOG.info("date converter..");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
				Date defaultValue = new Date();
				Date value = ParamUtil.getDate(httpServletRequest, fieldName, sdf, defaultValue);
				LOG.info("value converter.." + value);
				if (value.compareTo(defaultValue) == 0) {
					value = (Date) getDefaultData(fieldName, values, null);
				}
				LOG.info("value converted.." + value);
				values.put(fieldName, value);

			}
			if (ObjectFieldConstants.DB_TYPE_DATE_TIME.equalsIgnoreCase(objectField.getDBType())) {

				LOG.info("date time converter..");

				String value = ParamUtil.getString(httpServletRequest, fieldName, StringPool.BLANK);
				LOG.info("date time converter.." + value);
				if (Validator.isNotNull(value)) {

					value = value.replace("\"", "");

					LocalDateTime dateTime;

					try {

						dateTime = OffsetDateTime.parse(value).toLocalDateTime();

					} catch (DateTimeParseException e) {

						dateTime = LocalDate.parse(value).atStartOfDay(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
					}
					LOG.info("date time converter.." + dateTime);
					values.put(fieldName, dateTime);

					LOG.info("Added date field to values map: " + fieldName + " = " + dateTime);

				} else {

					values.put(fieldName, null);
				}
			}
			if (objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_LONG)) {
				long value = ParamUtil.getLong(httpServletRequest, fieldName, 0l);
				if (value == 0l) {
					value = (long) getDefaultData(fieldName, values, 0l);
				}
				values.put(fieldName, value);
			}
			if (objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_INTEGER)) {
				int value = ParamUtil.getInteger(httpServletRequest, fieldName, 0);
				if (value == 0) {
					value = (int) getDefaultData(fieldName, values, 0);
				}
				values.put(fieldName, value);
			}
		}
		return values;
	}

	private static Serializable getRestrictedValues(String fieldName, Map<String, Serializable> values,
			Serializable serializable) {

		if (fieldName.equalsIgnoreCase("branchCode") || fieldName.equalsIgnoreCase("zcode")
				|| fieldName.equalsIgnoreCase("regionalOffice") || fieldName.equalsIgnoreCase("urn")) {
			Serializable value = values.get(fieldName);
			return Validator.isNotNull(value) ? value : serializable;
		}
		return serializable;

	}

	private static Serializable getDefaultData(String fieldName, Map<String, Serializable> values,
			Serializable serializable) {

		if (values.containsKey(fieldName)) {
			return values.get(fieldName);
		}
		return serializable;
	}

	// ==================== FIELDS ====================
	private long defaultFolderId = 0L;
	private long consentObjectDefinitionId = 0L;
	private long workflowObjectDefinitionId = 0L;
	private BOIWorkflowConfiguation boiWorkflowConfiguation;

	@Reference
	private ObjectFieldSettingLocalService _objectFieldSettingLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private WorkflowTaskManager _workflowTaskManager;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Reference
	private Queries _queries;

	@Reference
	private OrganizationLocalService _organizationLocalService;
}