package com.boi.headless.rest.pmsby.internal.resource.v1_0;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.application.CustomWorkflowLogs;
import com.boi.headless.rest.common.application.FileHandler;
import com.boi.headless.rest.common.application.ObjectConverter;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.common.model.UserHierarchy;
import com.boi.headless.rest.common.validators.RequestValidator;
import com.boi.headless.rest.pmsby.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.pmsby.dto.v1_0.CustomBOIHeadlessResponse;
import com.boi.headless.rest.pmsby.dto.v1_0.CustomBOIHeadlessResponseAsList;
import com.boi.headless.rest.pmsby.resource.v1_0.PmsbyResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
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
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    configurationPid = "com.boi.headless.rest.common.application.BOIWorkflowConfiguation", 
    properties = "OSGI-INF/liferay/rest/v1_0/pmsby.properties", 
    scope = ServiceScope.PROTOTYPE, 
    service = PmsbyResource.class
)
public class PmsbyResourceImpl extends BasePmsbyResourceImpl {

    private static final Log LOG = LogFactoryUtil.getLog(PmsbyResourceImpl.class);

    // Parameter constants
    private static final class Params {
        static final String STAGE = "stage";
        static final String CONSENT_RECEIVED = "consentRecived";
        static final String STATUS = "status";
        static final String FROM_DATE = "fromDate";
        static final String TO_DATE = "toDate";
        static final String ENTRY_ID = "entryId";
        static final String TRANSITION_ID = "transitionId";
        static final String REMARKS = "remarks";
        static final String WORKFLOW_TASK_ID = "workflowTaskId";
        static final String ATTACHMENTS = "attachments";
        static final String URN = "uRN";
        static final String CUSTOMER_ID = "customerId";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String BRANCH_CODE = "branchCode";
        static final String ZONE = "zone";
        static final String NBG_CODE = "nbgCode";
        static final String YEAR = "year";
        static final String APPLY_HIERARCHY = "applyHierarchy";
        static final String FORCE_DB_QUERY = "forceDbQuery";
        static final String CONSENT_RECEIVED_PARAM = "consentRecived";
        static final String DECISION = "decision";
        
        private Params() {}
    }
    
  

    // Nested field constants
    private static final class NestedField {
        static final String ARRAY = "nestedFieldArray";
        static final String NAME = "fieldName";
        static final String VALUE_INTEGER = "value_integer";
        static final String VALUE_BOOLEAN = "value_boolean";
        static final String VALUE_KEYWORD = "value_keyword_lowercase";
        
        private NestedField() {}
    }

    // Stage mapping
    private static final Map<String, Integer> STATUS_TO_STAGE_MAP = Map.of(
        "initiated", 0,
        "pending with branch maker", 1,
        "pending with branch checker", 2,
        "approved", 3,
        "rejected", 1,
        "BC Channel", 4
    );

    // File upload mapping
    private static final Map<String, String> FILE_UPLOAD_MAP = Map.of(
        "consentFormDocumentDetailedFile", "consentFormDocumentDetailed"
    );

    @Override
    public BOIHeadlessResponse addPmsbyAsDraft(MultipartBody multipartBody) throws Exception {
        LOG.info("=== addPmsbyAsDraft START ===");

        APIRequestHandler handler = getAPIRequestHandler();
        JSONObject json = JSONFactoryUtil.createJSONObject();

        validateMakerRole(handler.getUserHierarchy());

        fileValidation(multipartBody);

        long entryId = ParamUtil.getLong(contextHttpServletRequest, Params.ENTRY_ID);
        ObjectEntry entry = handler.getObjectEntry(entryId);

        String refCode = entry == null ? null : entry.getExternalReferenceCode();
        Map<String, Serializable> values = entry == null 
            ? createDefaultValues(handler.getUserHierarchy())
            : entry.getValues();

        values = ObjectConverter.getDefaultData(
            contextHttpServletRequest, values, objectDefinitionId,
            _objectFieldLocalService, null
        );
        values.put(Params.STAGE, 0);

        handler.updaloadFile(values, FILE_UPLOAD_MAP, multipartBody, contextHttpServletRequest, 
            objectDefinitionId, StringPool.BLANK, true);
        entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, 
            refCode, objectDefinitionId);

        new CustomWorkflowLogs(contextUser).createWorkflowEntryLogs(entry, "Initiated", workflowObjectDefinitionId);

        json.put(Params.ENTRY_ID, entry.getObjectEntryId());

        LOG.info("=== addPmsbyAsDraft END ===");
        return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
    }

    @Override
    public BOIHeadlessResponse addPmsbyAsExport(MultipartBody multipartBody) throws Exception {
        LOG.info("=== addPmsbyAsExport START ===");

        APIRequestHandler handler = getAPIRequestHandler();
        JSONObject json = JSONFactoryUtil.createJSONObject();

        long entryId = ParamUtil.getLong(contextHttpServletRequest, Params.ENTRY_ID);
        ObjectEntry entry = handler.getObjectEntry(entryId);

        Map<String, Serializable> values = entry.getValues();
        String refCode = entry.getExternalReferenceCode();
        UserHierarchy hierarchy = handler.getUserHierarchy();
        int stage = GetterUtil.getInteger(values.get(Params.STAGE));

        if (hierarchy.isMaker() && (stage == 0 || stage == 1)) {
            handleMakerSubmission(handler, entry, values, refCode, multipartBody);
        }

        if (hierarchy.isBranchChecker() && stage == 2) {
            handleCheckerAction(handler, entry, values, refCode, multipartBody);
        }

        json.put(Params.ENTRY_ID, entry.getObjectEntryId());

        LOG.info("=== addPmsbyAsExport END ===");
        return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
    }

    @Override
    public BOIHeadlessResponse getRequestPmsbys(String search, Filter filter, Pagination pagination, Sort[] sorts)
            throws Exception {

        LOG.info("=== getRequestPmsbys START ===");

        Sort sort = SortFactoryUtil.create("modifiedDate", Sort.STRING_TYPE, true);
        sorts = new Sort[] { sort };

        APIRequestHandler handler = getAPIRequestHandler();
        ObjectDefinition objectDefinition = handler.getObjectDefinition(objectDefinitionId);
        
        boolean forceDbQuery = ParamUtil.getBoolean(contextHttpServletRequest, Params.FORCE_DB_QUERY, false);
        
        Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page;
        
        if (forceDbQuery) {
            LOG.info("Using database query (forced)");
            page = getDatabaseEntries(handler, pagination);
        } else {
            page = performSearchWithFallback(handler, pagination, objectDefinition);
        }
        
        LOG.info("Final query result. TotalCount=" + page.getTotalCount());
        LOG.info("=== getRequestPmsbys END ===");

        return new CustomBOIHeadlessResponseAsList(page, handler.getUserHierarchy().isMaker(), "",
                Set.of("initiated", "pending with branch maker", "pending with branch checker", "approved","BC Channel"));
    }

    @Override
    public BOIHeadlessResponse getRequestPmsby(@NotNull Integer id) throws Exception {
        LOG.info("=== getRequestPmsby START id=" + id + " ===");

        APIRequestHandler handler = getAPIRequestHandler();
        ObjectEntry entry = handler.getObjectEntry(id.longValue());

        JSONObject json = handler.convertEntryToArray(entry.getValues());
        json.put("status", WorkflowConstants.getStatusLabel(entry.getStatus()));
        json.put(Params.ENTRY_ID, entry.getObjectEntryId());
        handler.addWorkflowLogs(json, entry.getObjectEntryId(), workflowObjectDefinitionId);

        addStageActions(handler, entry, json);

        LOG.info("=== getRequestPmsby END ===");
        return new CustomBOIHeadlessResponse(json, Response.Status.ACCEPTED);
    }

    // ==================== Private Methods ====================

    private void validateMakerRole(UserHierarchy hierarchy) throws Exception {
        if (!hierarchy.isMaker()) {
            LOG.warn("User not Maker. Draft creation denied.");
            throw new Exception("Only Branch Maker can create draft");
        }
    }

    private Map<String, Serializable> createDefaultValues(UserHierarchy userHierarchy) {
        Map<String, Serializable> values = new HashMap<>();
        values.put(Params.BRANCH_CODE, userHierarchy.getBranchName());
        values.put(Params.ZONE, userHierarchy.getZoneName());
        values.put(Params.NBG_CODE, userHierarchy.getNbgName());
        return values;
    }

    private void handleMakerSubmission(APIRequestHandler handler, ObjectEntry entry, 
            Map<String, Serializable> values, String refCode, MultipartBody multipartBody) throws Exception {

        LOG.info("Maker submitting to Checker. EntryId=" + entry.getObjectEntryId());

        values = ObjectConverter.getDefaultData(
            contextHttpServletRequest, values, objectDefinitionId,
            _objectFieldLocalService, null
        );
        values.put(Params.STAGE, 2);
        handler.updaloadFile(values, FILE_UPLOAD_MAP, multipartBody, contextHttpServletRequest,
            objectDefinitionId, StringPool.BLANK, true);

        entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, 
            refCode, objectDefinitionId);
        completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);
    }

    private void handleCheckerAction(APIRequestHandler handler, ObjectEntry entry,
            Map<String, Serializable> values, String refCode, MultipartBody multipartBody) throws Exception {

        String transition = ParamUtil.getString(contextHttpServletRequest, Params.TRANSITION_ID);

        if ("Approve".equalsIgnoreCase(transition)) {
            LOG.info("Checker Approving. EntryId=" + entry.getObjectEntryId());
            values.put(Params.STAGE, 3);
            entry = handler.updateData(contextUser, values, WorkflowConstants.STATUS_APPROVED, 
                refCode, objectDefinitionId);
        } else if ("Reject".equalsIgnoreCase(transition)) {
            LOG.info("Checker Rejecting. EntryId=" + entry.getObjectEntryId());
            values.put(Params.STAGE, 1);
            entry = handler.updateData(contextUser, values, WorkflowConstants.ACTION_PUBLISH, 
                refCode, objectDefinitionId);
        }

        completeWorkflowLogs(contextHttpServletRequest, handler, multipartBody, entry);
    }



    private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> performSearchWithFallback(
            APIRequestHandler handler, Pagination pagination, 
            ObjectDefinition objectDefinition) throws Exception {
        
        SearchResponse searchResponse =  executeHybridSearch(objectDefinition, pagination);
        
        
        if (searchResponse == null) {
            LOG.warn("All Elasticsearch approaches failed, falling back to database query");
            return getDatabaseEntries(handler, pagination);
        }
        
        return convertSearchResponseToPage(handler, objectDefinition, pagination, searchResponse);
    }

    private SearchResponse executeHybridSearch(ObjectDefinition objectDefinition, Pagination pagination) {
        LOG.info("Using hybrid search mode - trying nested first");
        SearchResponse searchResponse = searchObjectEntriesWithNestedQuery(objectDefinition, pagination);
        
        if (searchResponse == null || searchResponse.getTotalHits() == 0) {
            LOG.info("Nested search returned no results, trying simple search");
//            searchResponse = searchObjectEntriesSimple(objectDefinition, pagination);
        }
        
        return searchResponse;
    }

    private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> convertSearchResponseToPage(
            APIRequestHandler handler, ObjectDefinition objectDefinition, 
            Pagination pagination, SearchResponse searchResponse) {
        
        SearchHits searchHits = searchResponse.getSearchHits();
        
        if (searchHits == null || searchHits.getSearchHits() == null) {
            return Page.of(new ArrayList<>(), pagination, 0);
        }
        
        List<com.liferay.object.rest.dto.v1_0.ObjectEntry> objectEntries = 
            convertSearchHitsToObjectEntries(handler, objectDefinition, searchHits);
        
        return Page.of(objectEntries, pagination, searchResponse.getTotalHits());
    }

    private SearchResponse searchObjectEntriesWithNestedQuery(ObjectDefinition objectDefinition, 
            Pagination pagination) {
        try {
            int from = calculateFrom(pagination);
            SearchFilters filters = extractSearchFilters();
            
            LOG.info("=== NESTED SEARCH (SEPARATE QUERIES PER FIELD) ===");
            filters.log();
            
            BooleanQuery mainQuery = buildBaseQuery(objectDefinition);
            addNestedFilters(mainQuery, filters);
            addDateRangeFilter(mainQuery, filters.fromDateStr, filters.toDateStr);
            
            SearchRequest searchRequest = buildSearchRequest(from, pagination.getPageSize(), mainQuery, "nested");
            
            long startTime = System.currentTimeMillis();
            SearchResponse searchResponse = _searcher.search(searchRequest);
            LOG.info("Nested search completed in " + (System.currentTimeMillis() - startTime) + 
                "ms. Total hits: " + searchResponse.getTotalHits());
            
            return searchResponse;
        } catch (Exception e) {
            LOG.error("Error executing nested search", e);
            return null;
        }
    }

    private SearchResponse searchObjectEntriesSimple(ObjectDefinition objectDefinition, Pagination pagination) {
        try {
            int from = calculateFrom(pagination);
            SearchFilters filters = extractSearchFilters();
            
            LOG.info("=== SIMPLE SEARCH (TOP-LEVEL FIELDS) ===");
            filters.log();
            
            BooleanQuery mainQuery = buildBaseQuery(objectDefinition);
            addSimpleFilters(mainQuery, filters);
            addDateRangeFilter(mainQuery, filters.fromDateStr, filters.toDateStr);
            
            SearchRequest searchRequest = buildSearchRequest(from, pagination.getPageSize(), mainQuery, "simple");
            
            long startTime = System.currentTimeMillis();
            SearchResponse searchResponse = _searcher.search(searchRequest);
            LOG.info("Simple search completed in " + (System.currentTimeMillis() - startTime) + 
                "ms. Total hits: " + searchResponse.getTotalHits());
            
            return searchResponse;
        } catch (Exception e) {
            LOG.error("Error executing simple search", e);
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
        addNestedUrnFilter(mainQuery, filters.uRN);
        addNestedCustomerIdFilter(mainQuery, filters.customerId);
        addNestedAccountNumberFilter(mainQuery, filters.accountNumber);
        addNestedConsentFilter(mainQuery, filters.consentReceived);
        addNestedBranchCodeFilter(mainQuery, filters);
        addNestedZoneFilter(mainQuery, filters.zone);
        addNestedYearFilter(mainQuery, filters.year);
        addNestedNbgCodeFilter(mainQuery, filters.nbgCode);
        addNestedStageFilter(mainQuery, filters.status);
    }

    private void addNestedUrnFilter(BooleanQuery mainQuery, String urn) {
        if (Validator.isNull(urn)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.URN)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, urn.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested URN filter: " + urn);
    }

    private void addNestedCustomerIdFilter(BooleanQuery mainQuery, String customerId) {
        if (Validator.isNull(customerId)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.CUSTOMER_ID)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, customerId.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested Customer ID filter: " + customerId);
    }
    private void addNestedAccountNumberFilter(BooleanQuery mainQuery, String accountNumber) {
    	if (Validator.isNull(accountNumber)) return;
    	
    	BooleanQuery innerQuery = _queries.booleanQuery();
    	innerQuery.addMustQueryClauses(
    			_queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.ACCOUNT_NUMBER)
    			);
    	innerQuery.addMustQueryClauses(
    			_queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, accountNumber.toLowerCase())
    			);
    	mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
    	LOG.info("Added separate nested Account Number filter: " + accountNumber);
    }

    private void addNestedConsentFilter(BooleanQuery mainQuery, Boolean consentReceived) {
        if (consentReceived == null) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.CONSENT_RECEIVED)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_BOOLEAN, consentReceived)
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested Consent Received filter: " + consentReceived);
    }

    private void addNestedBranchCodeFilter(BooleanQuery mainQuery, SearchFilters filters) {
        String branchCode =  filters.userHierarchy.getBranchName();
        if (Validator.isNull(branchCode)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.BRANCH_CODE)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, branchCode.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested Branch Code filter: " + branchCode);
    }

    private void addNestedZoneFilter(BooleanQuery mainQuery, String zone) {
        if (Validator.isNull(zone)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.ZONE)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, zone.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested Zone filter: " + zone);
    }

    private void addNestedYearFilter(BooleanQuery mainQuery, String year) {
        if (Validator.isNull(year)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.YEAR)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, year.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested Year filter: " + year);
    }

    private void addNestedNbgCodeFilter(BooleanQuery mainQuery, String nbgCode) {
        if (Validator.isNull(nbgCode)) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.NBG_CODE)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_KEYWORD, nbgCode.toLowerCase())
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested NBG Code filter: " + nbgCode);
    }

    private void addNestedStageFilter(BooleanQuery mainQuery, String status) {
        if (Validator.isNull(status)) return;
        
        Integer stageValue = mapStatusToStage(status);
        if (stageValue == null) return;
        
        BooleanQuery innerQuery = _queries.booleanQuery();
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.NAME, Params.STAGE)
        );
        innerQuery.addMustQueryClauses(
            _queries.term(NestedField.ARRAY + "." + NestedField.VALUE_INTEGER, stageValue)
        );
        mainQuery.addMustQueryClauses(_queries.nested(NestedField.ARRAY, innerQuery));
        LOG.info("Added separate nested stage filter: " + stageValue);
    }

    private void addSimpleFilters(BooleanQuery mainQuery, SearchFilters filters) {
        addSimpleUrnFilter(mainQuery, filters.uRN);
        addSimpleStageFilter(mainQuery, filters.status);
    }

    private void addSimpleUrnFilter(BooleanQuery mainQuery, String urn) {
        if (Validator.isNull(urn)) return;
        
        BooleanQuery urnQuery = _queries.booleanQuery();
        urnQuery.addShouldQueryClauses(_queries.term(Params.URN, urn));
        urnQuery.addShouldQueryClauses(_queries.match(Params.URN, urn));
        mainQuery.addMustQueryClauses(urnQuery);
        LOG.info("Added top-level URN filter: " + urn);
    }

    private void addSimpleStageFilter(BooleanQuery mainQuery, String status) {
        if (Validator.isNull(status)) return;
        
        Integer stageValue = mapStatusToStage(status);
        if (stageValue != null) {
            mainQuery.addMustQueryClauses(_queries.term(Params.STAGE, stageValue));
            LOG.info("Added top-level stage filter: " + stageValue);
        }
    }

    private void addDateRangeFilter(BooleanQuery mainQuery, String fromDateStr, String toDateStr) {
        if (Validator.isNull(fromDateStr) || Validator.isNull(toDateStr)) return;

        try {
            long fromTimestamp = convertToTimestamp(fromDateStr, true);
            long toTimestamp = convertToTimestamp(toDateStr, false);

            RangeTermQuery dateRangeQuery = _queries.rangeTerm("createDate_sortable", true, true, 
                fromTimestamp, toTimestamp);
            mainQuery.addMustQueryClauses(dateRangeQuery);
            
            LOG.info("Added date range filter: " + fromDateStr + " to " + toDateStr);
        } catch (Exception e) {
            LOG.error("Error adding date filter", e);
        }
    }

    private SearchRequest buildSearchRequest(int from, int pageSize, BooleanQuery query, String searchType) {
        try {
            LOG.debug("[" + searchType + "] Executing query: " + query.toString());
        } catch (Exception e) {
            LOG.warn("Unable to log query", e);
        }
        
        return _searchRequestBuilderFactory.builder()
                .companyId(contextCompany.getCompanyId())
                .emptySearchEnabled(true)
                .from(from)
                .size(pageSize)
                .fetchSource(true)
                .fields("entryClassPK", "uid", "objectDefinitionId", "createDate_sortable", "modified_sortable", 
                        "createDate", "modified", "stage", Params.URN, Params.BRANCH_CODE, Params.ZONE, 
                        Params.NBG_CODE, Params.CUSTOMER_ID)
                .withSearchContext(searchContext -> {
                    searchContext.setStart(from);
                    searchContext.setEnd(from + pageSize);
                    searchContext.setGroupIds(new long[] { 0 });
                    searchContext.setAttribute("companyId", contextCompany.getCompanyId());
                })
                .query(query)
                .build();
    }

    private int calculateFrom(Pagination pagination) {
        return (pagination.getPage() - 1) * pagination.getPageSize();
    }

    private long convertToTimestamp(String dateStr, boolean isStartOfDay) {
        LocalDate date = LocalDate.parse(dateStr);
        ZonedDateTime zonedDateTime = isStartOfDay 
            ? date.atStartOfDay(ZoneOffset.UTC)
            : date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> getDatabaseEntries(
            APIRequestHandler apiRequestHandler, Pagination pagination) throws Exception {
        
        ObjectDefinition def = apiRequestHandler.getObjectDefinition(objectDefinitionId);
        ObjectEntryManager manager = _objectEntryManagerRegistry.getObjectEntryManager(def.getStorageType());
        
        return manager.getObjectEntries(def.getCompanyId(), def, def.getScope(), null,
                apiRequestHandler.getDTOConverterContext(null), buildDatabaseQuery(apiRequestHandler), 
                pagination, null, null);
    }

    private String buildDatabaseQuery(APIRequestHandler handler) {
        List<String> filters = new ArrayList<>();
        UserHierarchy hierarchy = handler.getUserHierarchy();

        String status = ParamUtil.getString(contextHttpServletRequest, Params.STATUS);
        String fromDateStr = ParamUtil.getString(contextHttpServletRequest, Params.FROM_DATE);
        String toDateStr = ParamUtil.getString(contextHttpServletRequest, Params.TO_DATE);
        String urn = ParamUtil.getString(contextHttpServletRequest, Params.URN);
        String customerId = ParamUtil.getString(contextHttpServletRequest, Params.CUSTOMER_ID);
        String zone = ParamUtil.getString(contextHttpServletRequest, Params.ZONE);
        String nbgCode = ParamUtil.getString(contextHttpServletRequest, Params.NBG_CODE);
        Boolean consentReceived = ParamUtil.getBoolean(contextHttpServletRequest, Params.CONSENT_RECEIVED_PARAM, true);
        
        boolean applyHierarchy = ParamUtil.getBoolean(contextHttpServletRequest, Params.APPLY_HIERARCHY, true);
        
        addHierarchyFilters(filters, hierarchy, applyHierarchy, urn, customerId, zone, nbgCode);
        addExplicitFilters(filters, urn, customerId, zone, nbgCode, consentReceived);
        addStatusFilter(filters, status);
        addDateRangeFilterForDb(filters, fromDateStr, toDateStr);
        
        String query = String.join(" and ", filters);
        LOG.info("Database query: " + query);
        
        return query;
    }

    private void addHierarchyFilters(List<String> filters, UserHierarchy hierarchy, boolean applyHierarchy,
            String urn, String customerId, String zone, String nbgCode) {
        if (applyHierarchy && Validator.isNull(urn) && Validator.isNull(customerId) && 
            Validator.isNull(zone) && Validator.isNull(nbgCode)) {
            if (Validator.isNotNull(hierarchy.getBranchName())) {
                filters.add(Params.BRANCH_CODE + " eq '" + hierarchy.getBranchName() + "'");
            } else if (Validator.isNotNull(hierarchy.getZoneName())) {
                filters.add(Params.ZONE + " eq '" + hierarchy.getZoneName() + "'");
            } else if (Validator.isNotNull(hierarchy.getNbgName())) {
                filters.add(Params.NBG_CODE + " eq '" + hierarchy.getNbgName() + "'");
            }
        }
    }

    private void addExplicitFilters(List<String> filters, String urn, String customerId, 
            String zone, String nbgCode, Boolean consentReceived) {
        if (Validator.isNotNull(urn)) {
            filters.add(Params.URN + " eq '" + urn + "'");
        }
        if (Validator.isNotNull(customerId)) {
            filters.add(Params.CUSTOMER_ID + " eq '" + customerId + "'");
        }
        if (Validator.isNotNull(zone)) {
            filters.add(Params.ZONE + " eq '" + zone + "'");
        }
        if (Validator.isNotNull(nbgCode)) {
            filters.add(Params.NBG_CODE + " eq '" + nbgCode + "'");
        }
        if (consentReceived != null) {
            filters.add(Params.CONSENT_RECEIVED + " eq " + consentReceived);
        }
    }

    private void addStatusFilter(List<String> filters, String status) {
        if (Validator.isNotNull(status)) {
            Integer stageValue = mapStatusToStage(status);
            if (stageValue != null) {
                filters.add(Params.STAGE + " eq " + stageValue);
            }
        }
    }

    private void addDateRangeFilterForDb(List<String> filters, String fromDateStr, String toDateStr) {
        if (Validator.isNotNull(fromDateStr) && Validator.isNotNull(toDateStr)) {
            try {
                Map<String, String> range = convertToISO8601Range(fromDateStr, toDateStr);
                filters.add("createDate ge " + range.get(Params.FROM_DATE) + 
                           " and createDate le " + range.get(Params.TO_DATE));
            } catch (Exception e) {
                LOG.error("Error adding date filter", e);
            }
        }
    }

    private Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
        LocalDate fromDate = LocalDate.parse(fromDateStr);
        LocalDate toDate = LocalDate.parse(toDateStr);

        ZonedDateTime from = fromDate.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime to = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);

        Map<String, String> result = new HashMap<>();
        result.put(Params.FROM_DATE, DateTimeFormatter.ISO_INSTANT.format(from));
        result.put(Params.TO_DATE, DateTimeFormatter.ISO_INSTANT.format(to));

        return result;
    }

    private List<com.liferay.object.rest.dto.v1_0.ObjectEntry> convertSearchHitsToObjectEntries(
            APIRequestHandler apiRequestHandler, ObjectDefinition objectDefinition, SearchHits searchHits) {

        List<com.liferay.object.rest.dto.v1_0.ObjectEntry> objectEntries = new ArrayList<>();

        for (SearchHit searchHit : searchHits.getSearchHits()) {
            try {
                Document document = searchHit.getDocument();
                if (document == null) continue;

                long entryClassPK = resolveEntryClassPK(document);
                if (entryClassPK <= 0) continue;

                com.liferay.object.model.ObjectEntry serviceBuilderEntry = 
                    _objectEntryLocalService.fetchObjectEntry(entryClassPK);

                if (serviceBuilderEntry == null || 
                    serviceBuilderEntry.getObjectDefinitionId() != objectDefinition.getObjectDefinitionId()) {
                    continue;
                }

                com.liferay.object.rest.dto.v1_0.ObjectEntry restObjectEntry = 
                    toRestObjectEntry(apiRequestHandler, objectDefinition, serviceBuilderEntry);

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
            Long entryClassPKValue = document.getLong("entryClassPK");
            if (entryClassPKValue != null && entryClassPKValue > 0) {
                return entryClassPKValue;
            }

            String uid = document.getString("uid");
            return extractEntryClassPKFromUid(uid);
        } catch (Exception e) {
            LOG.debug("Unable to resolve entryClassPK", e);
            return 0;
        }
    }

    private long extractEntryClassPKFromUid(String uid) {
        if (Validator.isNull(uid)) return 0;

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
            LOG.error("Unable to convert ObjectEntry to REST DTO for PK: " + 
                serviceBuilderEntry.getObjectEntryId(), e);
        }

        return null;
    }

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

    private void addAction(JSONArray actions, String actionName, String paramName, 
            String paramValue, long entryId) {
        JSONObject action = JSONFactoryUtil.createJSONObject();
        action.put("actionName", actionName);
        action.put(paramName, paramValue);
        action.put(Params.WORKFLOW_TASK_ID, entryId);
        actions.put(action);
    }

    private Integer mapStatusToStage(String status) {
        if (Validator.isNull(status)) return null;
        return STATUS_TO_STAGE_MAP.get(status.toLowerCase());
    }

    private SearchFilters extractSearchFilters() {
        SearchFilters filters = new SearchFilters();
        
        filters.uRN = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.URN));
        filters.accountNumber = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.ACCOUNT_NUMBER));
        filters.customerId = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.CUSTOMER_ID));
        filters.branchCode = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.BRANCH_CODE));
        filters.zone = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.ZONE));
        filters.year = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.YEAR));
        filters.nbgCode = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.NBG_CODE));
        filters.status = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.STATUS));
        filters.fromDateStr = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.FROM_DATE));
        filters.toDateStr = cleanParameterValue(ParamUtil.getString(contextHttpServletRequest, Params.TO_DATE));
        filters.applyHierarchy = ParamUtil.getBoolean(contextHttpServletRequest, Params.APPLY_HIERARCHY, true);
        
        String consentReceivedParam = ParamUtil.getString(contextHttpServletRequest, Params.CONSENT_RECEIVED_PARAM);
        if (Validator.isNotNull(consentReceivedParam)) {
            filters.consentReceived = Boolean.parseBoolean(consentReceivedParam);
        }

        filters.userHierarchy = getAPIRequestHandler().getUserHierarchy();
        LOG.info(filters.accountNumber);
        filters.hasAnyFilter = Validator.isNotNull(filters.uRN) || Validator.isNotNull(filters.customerId)
                || Validator.isNotNull(filters.branchCode) || Validator.isNotNull(filters.zone)
                || Validator.isNotNull(filters.nbgCode) || Validator.isNotNull(filters.status)
                || (Validator.isNotNull(filters.fromDateStr) && Validator.isNotNull(filters.toDateStr))
                || Validator.isNotNull(filters.accountNumber)
                || filters.consentReceived != null;

        return filters;
    }

    private String cleanParameterValue(String value) {
        if (Validator.isNull(value)) return value;

        String trimmed = value.trim();

        if ((trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        return trimmed;
    }

    private void fileValidation(MultipartBody multipartBody) throws Exception {
        new RequestValidator(multipartBody, BOIDMSConstants.PMSBY).validateRequestFiles();
    }

    private APIRequestHandler getAPIRequestHandler() {
        return new APIRequestHandler(contextUser, contextHttpServletRequest, contextAcceptLanguage,
                _objectFieldLocalService, _dlAppService, contextUriInfo, _dtoConverterRegistry,
                _objectEntryManagerRegistry, _objectDefinitionLocalService, _objectEntryLocalService,
                _workflowTaskManager, boiWorkflowConfiguation, BOIDMSConstants.PMSBY, defaultFolderId);
    }

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

            Map<String, Serializable> values = new HashMap<>();
            attachFileIfPresent(apiRequestHandler, multipartBody, values);

            values.put("customAction", transitionId);
            values.put("transitionName", transitionId);
            values.put(Params.REMARKS, comment);
            values.put(Params.WORKFLOW_TASK_ID, workflowTaskId);
            values.put("objectEntryId", objectEntry.getObjectEntryId());

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
        if (Validator.isNull(binaryFile)) return;

        File file = apiRequestHandler.createFileFromInputStream(
            binaryFile.getInputStream(), binaryFile.getFileName());

        Folder uploadFolder = apiRequestHandler.getUserHierarchy().getUploadFolder();
        if (Validator.isNull(uploadFolder)) return;

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

        ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(null, contextUser.getUserId(), 0, 
            workflowObjectDefinitionId, values, serviceContext);
    }

    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);

        defaultFolderId = boiWorkflowConfiguation.HODefualtFolderId();
        objectDefinitionId = boiWorkflowConfiguation.PMSBYId();
        workflowObjectDefinitionId = boiWorkflowConfiguation.CustomWorkflowLogsPMSBYId();
    }

    // ==================== Inner Classes ====================

    /**
     * SearchFilters inner class
     */
    private static class SearchFilters {
        String uRN;
        String customerId;
        String accountNumber;
        String branchCode;
        String zone;
        String nbgCode;
        String status;
        String fromDateStr;
        String toDateStr;
        String year;
        boolean applyHierarchy;
        boolean hasAnyFilter;
        Boolean consentReceived;
        UserHierarchy userHierarchy;
        
        void log() {
            LOG.info("URN: " + uRN);
            LOG.info("Customer ID: " + customerId);
            LOG.info("Branch Code: " + branchCode);
            LOG.info("Zone: " + zone);
            LOG.info("NBG Code: " + nbgCode);
            LOG.info("Status: " + status);
            LOG.info("From Date: " + fromDateStr);
            LOG.info("To Date: " + toDateStr);
            LOG.info("Consent Received: " + consentReceived);
            LOG.info("Apply Hierarchy: " + applyHierarchy);
            LOG.info("Has Any Filter: " + hasAnyFilter);
            if (userHierarchy != null) {
                LOG.info("User Branch: " + userHierarchy.getBranchName());
                LOG.info("User Zone: " + userHierarchy.getZoneName());
                LOG.info("User NBG: " + userHierarchy.getNbgName());
            }
        }
    }


    // ==================== Fields ====================

    private long defaultFolderId = 0L;
    private long objectDefinitionId = 0L;
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
}