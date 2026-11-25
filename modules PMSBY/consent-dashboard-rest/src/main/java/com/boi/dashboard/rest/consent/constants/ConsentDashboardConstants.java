package com.boi.dashboard.rest.consent.constants;

public class ConsentDashboardConstants {
    
    // ==================== APPLICATION ====================
    public static final String APPLICATION_BASE = "/consent-search";
    public static final String APPLICATION_NAME = "ConsentDashboard.Rest";
    
    // ==================== BLUEPRINT ERCs ====================
    public static final String BLUEPRINT_ZONE = "ConsentZoneSearch";
    public static final String BLUEPRINT_ZONE_YEAR = "ConsentZoneYearSearch";
    public static final String BLUEPRINT_STAGE = "ConsentStageSearch";
    public static final String BLUEPRINT_PENDING = "ConsentPendingSearch";
    public static final String BLUEPRINT_YEARWISE = "ConsentYearwiseSearch";
    public static final String BLUEPRINT_DELETED = "ConsentBCsearch";
    public static final String BLUEPRINT_NON_DELETED = "ConsentDMSSearch";
    public static final String BLUEPRINT_BASE = "ConsentSearch";
    
    // ==================== STAGE CONSTANTS (Matches Consent API) ====================
    public static final int STAGE_INITIATED = 0;
    public static final int STAGE_PENDING_MAKER = 1;
    public static final int STAGE_PENDING_CHECKER = 2;
    public static final int STAGE_APPROVED = 3;
    public static final int STAGE_DELETED = 4;
    
    // ==================== BLUEPRINT PARAMETERS ====================
    public static final String PARAM_DATE_FROM = "search.experiences.user_date_from";
    public static final String PARAM_DATE_TO = "search.experiences.user_date_to";
    public static final String PARAM_YEAR = "search.experiences.user_year";
    public static final String PARAM_ZONE = "search.experiences.user_zone";
    public static final String PARAM_STAGE_MIN = "search.experiences.user_stage_min";
    public static final String PARAM_STAGE_MAX = "search.experiences.user_stage_max";
    
    // ==================== REQUEST PARAMETERS ====================
    public static final String REQ_FROM_DATE = "fromDate";
    public static final String REQ_TO_DATE = "toDate";
    public static final String REQ_YEAR = "year";
    public static final String REQ_ORGANIZATION = "organizationId";
    public static final String REQ_STATUS = "status";
    public static final String REQ_CBS_REFERENCE = "cbsReferenceNumber";
    public static final String REQ_TYPE_OF_INSURANCE = "typeOfInsurance";
    public static final String REQ_URN = "urn";
    
    // ==================== RESPONSE FIELDS ====================
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_TOTAL_COUNT = "totalCount";
    public static final String RESPONSE_DELETED_COUNT = "deletedCount";
    public static final String RESPONSE_NON_DELETED_COUNT = "nonDeletedCount";
    public static final String RESPONSE_PENDING_APPROVAL = "pendingForApproval";
    public static final String RESPONSE_INITIATED_COUNT = "initiatedCount";
    public static final String RESPONSE_PENDING_MAKER_COUNT = "pendingMakerCount";
    public static final String RESPONSE_APPROVED_COUNT = "approvedCount";
    public static final String RESPONSE_ORGANIZATION_COUNTS = "organizationCounts";
    public static final String RESPONSE_FILTERS = "filters";
    public static final String RESPONSE_RESPONSE_TIME = "responseTimeMs";
    
    private ConsentDashboardConstants() {}
}