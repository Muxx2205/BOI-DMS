package com.boi.dashboard.rest.consent.application;

import com.boi.dashboard.rest.consent.constants.ConsentDashboardConstants;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

@Component(
    configurationPid = "com.boi.headless.rest.common.application.BOIWorkflowConfiguation",
    property = {
        JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=" + ConsentDashboardConstants.APPLICATION_BASE,
        JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + ConsentDashboardConstants.APPLICATION_NAME
    },
    service = Application.class
)
public class ConsentDashboardRestApplication extends Application {

    private static final Log log = LogFactoryUtil.getLog(ConsentDashboardRestApplication.class);

    // ==================== CONSTANTS ====================
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(20);
    
    // Role names for authorization
    private static final String CONSENT_DASHBOARD_ROLE = "Consent Dashboard";
    private static final String ADMINISTRATOR_ROLE = "Administrator";

    // ==================== OSGi REFERENCES ====================
    @Reference
    private Searcher _searcher;

    @Reference
    private SearchRequestBuilderFactory _searchRequestBuilderFactory;

    @Reference
    private OrganizationLocalService _organizationLocalService;

    @Reference
    private RoleLocalService _roleLocalService;

    @Reference
    private UserLocalService _userLocalService;

    // ==================== JAX-RS CONTEXTS ====================
    @Context
    private AcceptLanguage contextAcceptLanguage;

    @Context
    private HttpServletRequest contextHttpServletRequest;

    @Context
    private HttpServletResponse contextHttpServletResponse;

    @Context
    private Company contextCompany;

    @Context
    private UriInfo contextUriInfo;

    // ==================== CONFIGURATION ====================
    private BOIWorkflowConfiguation boiWorkflowConfiguation;
    private long consentObjectDefinitionId = 0L;

    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(BOIWorkflowConfiguation.class, properties);
        consentObjectDefinitionId = boiWorkflowConfiguation.ConsentObjectDefinitionId();
        log.info("ConsentDashboardRestApplication activated with objectDefinitionId: " + consentObjectDefinitionId);
    }

    public Set<Object> getSingletons() {
        return Collections.<Object>singleton(this);
    }

    // ==================== GET CURRENT USER ====================
    /**
     * Get the current authenticated user from the request
     */
    private User getCurrentUser() {
        try {
            // Method 1: Get from PrincipalThreadLocal
            long userId = PrincipalThreadLocal.getUserId();
            if (userId > 0) {
                return _userLocalService.fetchUser(userId);
            }

            // Method 2: Get from HttpServletRequest
            if (contextHttpServletRequest != null) {
                User user = PortalUtil.getUser(contextHttpServletRequest);
                if (user != null && !user.isDefaultUser()) {
                    return user;
                }
            }

            // Method 3: Get from Company and Principal
            if (contextCompany != null) {
                long companyId = contextCompany.getCompanyId();
                userId = PrincipalThreadLocal.getUserId();
                if (userId > 0) {
                    return _userLocalService.fetchUser(userId);
                }
            }

            log.warn("Unable to get current user from any source");
            return null;
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * Get company ID from various sources
     */
    private long getCompanyId() {
        try {
            // Method 1: From context
            if (contextCompany != null) {
                return contextCompany.getCompanyId();
            }

            // Method 2: From request
            if (contextHttpServletRequest != null) {
                return PortalUtil.getCompanyId(contextHttpServletRequest);
            }

            // Method 3: From ThreadLocal
            return CompanyThreadLocal.getCompanyId();
        } catch (Exception e) {
            log.debug("Error getting company from context", e);
        }
        return CompanyThreadLocal.getCompanyId();
    }

    // ==================== ROLE CHECK METHOD ====================
    /**
     * Check if user has required role to access Consent Dashboard
     * Allowed roles: "Administrator" OR "Consent Dashboard"
     */
    private boolean hasConsentDashboardAccess(User user) {
        try {
            if (user == null || user.isDefaultUser()) {
                log.warn("Unauthenticated access attempt to consent dashboard");
                return false;
            }

            long companyId = user.getCompanyId();
            long userId = user.getUserId();

            // Check for Administrator role
            Role adminRole = _roleLocalService.fetchRole(companyId, ADMINISTRATOR_ROLE);
            if (adminRole != null && _userLocalService.hasRoleUser(adminRole.getRoleId(), userId)) {
                log.debug("User " + user.getEmailAddress() + " has Administrator role");
                return true;
            }

            // Check for Consent Dashboard role
            Role consentRole = _roleLocalService.fetchRole(companyId, CONSENT_DASHBOARD_ROLE);
            if (consentRole != null && _userLocalService.hasRoleUser(consentRole.getRoleId(), userId)) {
                log.debug("User " + user.getEmailAddress() + " has Consent Dashboard role");
                return true;
            }

            log.warn("User " + user.getEmailAddress() + " denied access to consent dashboard. " +
                     "Required roles: " + ADMINISTRATOR_ROLE + " or " + CONSENT_DASHBOARD_ROLE);
            return false;

        } catch (Exception e) {
            log.error("Error checking user roles for consent dashboard access", e);
            return false;
        }
    }

    // ==================== MAIN API ENDPOINT ====================
    @GET
    @Path("/blueprint/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlueprintCount(
            @QueryParam(ConsentDashboardConstants.REQ_FROM_DATE) String fromDate,
            @QueryParam(ConsentDashboardConstants.REQ_TO_DATE) String toDate,
            @QueryParam(ConsentDashboardConstants.REQ_YEAR) String year,
            @QueryParam(ConsentDashboardConstants.REQ_ORGANIZATION) long organizationId,
            @QueryParam(ConsentDashboardConstants.REQ_STATUS) String status,
            @QueryParam(ConsentDashboardConstants.REQ_CBS_REFERENCE) String cbsReference,
            @QueryParam(ConsentDashboardConstants.REQ_TYPE_OF_INSURANCE) String typeOfInsurance,
            @QueryParam(ConsentDashboardConstants.REQ_URN) String urn) {

        long startTime = System.currentTimeMillis();

        // ==================== GET CURRENT USER ====================
        User currentUser = getCurrentUser();

        // ==================== AUTHENTICATION CHECK ====================
        if (currentUser == null || currentUser.isDefaultUser()) {
            log.warn("Unauthenticated access attempt to consent dashboard");
            JSONObject error = JSONFactoryUtil.createJSONObject();
            error.put("success", false);
            error.put("error", "Authentication required. Please log in.");
            error.put("status", "UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(error.toString())
                .build();
        }

        // ==================== AUTHORIZATION CHECK ====================
        if (!hasConsentDashboardAccess(currentUser)) {
            JSONObject error = JSONFactoryUtil.createJSONObject();
            error.put("success", false);
            error.put("error", "Access denied. Required roles: " + ADMINISTRATOR_ROLE + " or " + CONSENT_DASHBOARD_ROLE);
            error.put("status", "FORBIDDEN");
            error.put("user", currentUser.getEmailAddress());
            error.put("userId", currentUser.getUserId());
            return Response.status(Response.Status.FORBIDDEN)
                .entity(error.toString())
                .build();
        }

        try {
            log.info("=== Consent Dashboard Request ===");
            log.info("User: " + currentUser.getEmailAddress() + " (ID: " + currentUser.getUserId() + ")");
            log.info("fromDate: " + fromDate + ", toDate: " + toDate);
            log.info("year: " + year + ", organizationId: " + organizationId);
            log.info("cbsReference: " + cbsReference + ", typeOfInsurance: " + typeOfInsurance);

            // Set default date range (last 365 days)
            if (Validator.isNull(fromDate) || Validator.isNull(toDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date to = new Date();
                Date from = new Date(to.getTime() - (365L * 24 * 60 * 60 * 1000));
                fromDate = sdf.format(from);
                toDate = sdf.format(to);
            }

            long companyId = getCompanyId();
            log.info("Using companyId: " + companyId);

            // ==================== GET TOTAL COUNTS ====================
            Map<String, Long> totalCounts = getTotalCounts(companyId, fromDate, toDate, year, cbsReference, typeOfInsurance);

            // ==================== GET ORGANIZATIONS ====================
            List<Organization> organizations = getOrganizations(companyId, organizationId);
            log.info("Found " + organizations.size() + " organizations");

            // ==================== GET ORGANIZATION COUNTS ====================
            JSONArray organizationCounts = getOrganizationCounts(organizations, fromDate, toDate, year, companyId, 
                cbsReference, typeOfInsurance);

            // ==================== BUILD RESPONSE ====================
            JSONObject response = buildResponse(totalCounts, organizationCounts, fromDate, toDate, year, 
                cbsReference, typeOfInsurance, urn);

            response.put(ConsentDashboardConstants.RESPONSE_RESPONSE_TIME, 
                System.currentTimeMillis() - startTime);
            response.put("user", currentUser.getEmailAddress());
            response.put("userId", currentUser.getUserId());

            log.info("=== Consent Dashboard Response Complete ===");
            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            log.error("Error in consent dashboard", e);
            JSONObject error = JSONFactoryUtil.createJSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error.toString())
                .build();
        }
    }

    // ==================== GET TOTAL COUNTS ====================
    private Map<String, Long> getTotalCounts(long companyId, String fromDate, String toDate, String year,
            String cbsReference, String typeOfInsurance) {
        Map<String, Long> results = new HashMap<>();
        
        try {
            List<CompletableFuture<Map.Entry<String, Long>>> futures = new ArrayList<>();

            // Total count - using ConsentSearch blueprint
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("totalCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_BASE, 
                    fromDate, toDate, year, null, null, cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Deleted count - using ConsentBCsearch blueprint (delFlag='Y')
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("deletedCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_DELETED, 
                    fromDate, toDate, year, null, null, cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Non-deleted count - using ConsentDMSSearch blueprint (delFlag='N')
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("nonDeletedCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_NON_DELETED, 
                    fromDate, toDate, year, null, null, cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Pending for Approval (Stage 2 - Pending with Checker)
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("pendingForApproval", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_STAGE, 
                    fromDate, toDate, year, String.valueOf(ConsentDashboardConstants.STAGE_PENDING_CHECKER), 
                    String.valueOf(ConsentDashboardConstants.STAGE_PENDING_CHECKER), cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Initiated (Stage 0)
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("initiatedCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_STAGE, 
                    fromDate, toDate, year, String.valueOf(ConsentDashboardConstants.STAGE_INITIATED), 
                    String.valueOf(ConsentDashboardConstants.STAGE_INITIATED), cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Pending with Maker (Stage 1)
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("pendingMakerCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_STAGE, 
                    fromDate, toDate, year, String.valueOf(ConsentDashboardConstants.STAGE_PENDING_MAKER), 
                    String.valueOf(ConsentDashboardConstants.STAGE_PENDING_MAKER), cbsReference, typeOfInsurance)),
                EXECUTOR));

            // Approved (Stage 3)
            futures.add(CompletableFuture.supplyAsync(() -> 
                Map.entry("approvedCount", getCount(companyId, ConsentDashboardConstants.BLUEPRINT_STAGE, 
                    fromDate, toDate, year, String.valueOf(ConsentDashboardConstants.STAGE_APPROVED), 
                    String.valueOf(ConsentDashboardConstants.STAGE_APPROVED), cbsReference, typeOfInsurance)),
                EXECUTOR));

            for (CompletableFuture<Map.Entry<String, Long>> future : futures) {
                try {
                    Map.Entry<String, Long> entry = future.get(5, TimeUnit.SECONDS);
                    results.put(entry.getKey(), entry.getValue());
                } catch (TimeoutException e) {
                    log.warn("Timeout getting count");
                    future.cancel(true);
                } catch (Exception e) {
                    log.error("Error getting count", e);
                }
            }

        } catch (Exception e) {
            log.error("Error in parallel count processing", e);
        }

        return results;
    }

    // ==================== GET ORGANIZATION COUNTS ====================
    private JSONArray getOrganizationCounts(List<Organization> organizations, String fromDate, 
            String toDate, String year, long companyId, String cbsReference, String typeOfInsurance) {

        JSONArray result = JSONFactoryUtil.createJSONArray();

        if (organizations.isEmpty()) {
            return result;
        }

        List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

        for (Organization org : organizations) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                JSONObject orgResult = JSONFactoryUtil.createJSONObject();
                orgResult.put("organizationId", String.valueOf(org.getOrganizationId()));
                orgResult.put("organizationName", org.getName());
                orgResult.put("organizationType", org.getType() != null ? org.getType() : "BRANCH");

                try {
                    String orgName = org.getName();
                    String orgType = org.getType() != null ? org.getType() : "BRANCH";

                    // Get counts for each stage
                    long initiated = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_INITIATED),
                        String.valueOf(ConsentDashboardConstants.STAGE_INITIATED),
                        cbsReference, typeOfInsurance);

                    long pendingMaker = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_PENDING_MAKER),
                        String.valueOf(ConsentDashboardConstants.STAGE_PENDING_MAKER),
                        cbsReference, typeOfInsurance);

                    long pendingChecker = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_PENDING_CHECKER),
                        String.valueOf(ConsentDashboardConstants.STAGE_PENDING_CHECKER),
                        cbsReference, typeOfInsurance);

                    long approved = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_APPROVED),
                        String.valueOf(ConsentDashboardConstants.STAGE_APPROVED),
                        cbsReference, typeOfInsurance);

                    long deleted = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_DELETED),
                        String.valueOf(ConsentDashboardConstants.STAGE_DELETED),
                        cbsReference, typeOfInsurance);

                    // Total assigned (all stages)
                    long total = getCountWithOrg(companyId, orgName, orgType, fromDate, toDate, year,
                        String.valueOf(ConsentDashboardConstants.STAGE_INITIATED),
                        String.valueOf(ConsentDashboardConstants.STAGE_APPROVED),
                        cbsReference, typeOfInsurance);

                    orgResult.put("initiatedCount", String.valueOf(initiated));
                    orgResult.put("pendingMakerCount", String.valueOf(pendingMaker));
                    orgResult.put("pendingCheckerCount", String.valueOf(pendingChecker));
                    orgResult.put("approvedCount", String.valueOf(approved));
                    orgResult.put("deletedCount", String.valueOf(deleted));
                    orgResult.put("totalCount", String.valueOf(total));

                    log.debug("Organization: " + orgName + 
                        " - Initiated: " + initiated +
                        ", Pending Maker: " + pendingMaker +
                        ", Pending Checker: " + pendingChecker +
                        ", Approved: " + approved +
                        ", Deleted: " + deleted);

                } catch (Exception e) {
                    log.error("Error processing org: " + org.getName(), e);
                    orgResult.put("error", e.getMessage());
                }

                return orgResult;
            }, EXECUTOR));
        }

        for (CompletableFuture<JSONObject> future : futures) {
            try {
                JSONObject orgResult = future.get(20, TimeUnit.SECONDS);
                result.put(orgResult);
            } catch (TimeoutException e) {
                log.warn("Timeout processing organization");
                future.cancel(true);
                JSONObject errorResult = JSONFactoryUtil.createJSONObject();
                errorResult.put("organizationId", "unknown");
                errorResult.put("organizationName", "Timeout");
                result.put(errorResult);
            } catch (Exception e) {
                log.error("Error getting organization count", e);
            }
        }

        return result;
    }

    // ==================== CORE COUNT METHODS ====================
    private long getCount(long companyId, String blueprintERC, String fromDate, String toDate, 
            String year, String stageMin, String stageMax, String cbsReference, String typeOfInsurance) {
        try {
            SearchRequestBuilder builder = _searchRequestBuilderFactory.builder();
            builder.companyId(companyId);
            builder.emptySearchEnabled(true);
            builder.from(0);
            builder.size(1);
            builder.fetchSource(false);

            builder.withSearchContext(searchContext -> {
                searchContext.setAttribute("search.experiences.blueprint.external.reference.code", blueprintERC);
                searchContext.setAttribute("search.experiences.scope.group.id", "0");
                searchContext.setAttribute("search.empty.search", "true");
                searchContext.setStart(0);
                searchContext.setCompanyId(companyId);
                searchContext.setEnd(1);
                searchContext.setGroupIds(new long[] { 0 });

                if (Validator.isNotNull(fromDate)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_DATE_FROM, fromDate);
                }
                if (Validator.isNotNull(toDate)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_DATE_TO, toDate);
                }
                if (Validator.isNotNull(year)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_YEAR, year);
                }
                if (Validator.isNotNull(stageMin)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_STAGE_MIN, stageMin);
                }
                if (Validator.isNotNull(stageMax)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_STAGE_MAX, stageMax);
                }
                if (Validator.isNotNull(cbsReference)) {
                    searchContext.setAttribute("search.experiences.user_cbs_reference", cbsReference);
                }
                if (Validator.isNotNull(typeOfInsurance)) {
                    searchContext.setAttribute("search.experiences.user_type_of_insurance", typeOfInsurance);
                }
            });

            SearchRequest request = builder.build();
            SearchResponse response = _searcher.search(request);

            return response != null ? response.getTotalHits() : 0;

        } catch (Exception e) {
            log.error("Error in getCount for blueprint: " + blueprintERC, e);
            return 0;
        }
    }

    private long getCountWithOrg(long companyId, String orgName, String orgType, String fromDate, 
            String toDate, String year, String stageMin, String stageMax, String cbsReference, String typeOfInsurance) {

        String blueprintERC;

        // Determine which blueprint to use based on organization type and filters
        // For ZONE organizations, use zone-based blueprints
        // For BRANCH organizations, use pending/yearwise blueprints
        if (Validator.isNotNull(year) && !"ZONE".equalsIgnoreCase(orgType)) {
            blueprintERC = ConsentDashboardConstants.BLUEPRINT_YEARWISE;
        } else if (Validator.isNotNull(year) && "ZONE".equalsIgnoreCase(orgType)) {
            blueprintERC = ConsentDashboardConstants.BLUEPRINT_ZONE_YEAR;
        } else if ("ZONE".equalsIgnoreCase(orgType)) {
            blueprintERC = ConsentDashboardConstants.BLUEPRINT_ZONE;
        } else {
            blueprintERC = ConsentDashboardConstants.BLUEPRINT_PENDING;
        }

        try {
            SearchRequestBuilder builder = _searchRequestBuilderFactory.builder();
            builder.companyId(companyId);
            builder.from(0);
            builder.size(1);
            builder.fetchSource(false);

            // For non-ZONE organizations, search by organization name
            if (!"ZONE".equalsIgnoreCase(orgType)) {
                builder.emptySearchEnabled(false);
                builder.queryString("\"" + orgName + "\"");
            } else {
                builder.emptySearchEnabled(true);
            }

            builder.withSearchContext(searchContext -> {
                searchContext.setAttribute("search.experiences.blueprint.external.reference.code", blueprintERC);
                searchContext.setAttribute("search.experiences.scope.group.id", "0");
                searchContext.setStart(0);
                searchContext.setCompanyId(companyId);
                searchContext.setEnd(1);
                searchContext.setGroupIds(new long[] { 0 });

                // Date filters
                if (Validator.isNotNull(fromDate)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_DATE_FROM, fromDate);
                }
                if (Validator.isNotNull(toDate)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_DATE_TO, toDate);
                }
                
                // Year filter
                if (Validator.isNotNull(year)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_YEAR, year);
                }
                
                // Zone filter - for ZONE organizations only
                if ("ZONE".equalsIgnoreCase(orgType)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_ZONE, orgName);
                }
                
                // Stage filters
                if (Validator.isNotNull(stageMin)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_STAGE_MIN, stageMin);
                }
                if (Validator.isNotNull(stageMax)) {
                    searchContext.setAttribute(ConsentDashboardConstants.PARAM_STAGE_MAX, stageMax);
                }
                
                // Additional filters
                if (Validator.isNotNull(cbsReference)) {
                    searchContext.setAttribute("search.experiences.user_cbs_reference", cbsReference);
                }
                if (Validator.isNotNull(typeOfInsurance)) {
                    searchContext.setAttribute("search.experiences.user_type_of_insurance", typeOfInsurance);
                }
            });

            SearchRequest request = builder.build();
            SearchResponse response = _searcher.search(request);

            long totalHits = response != null ? response.getTotalHits() : 0;
            
            log.debug("Org: " + orgName + ", Blueprint: " + blueprintERC + 
                ", Stage: " + stageMin + "-" + stageMax + 
                ", Year: " + year + ", Result: " + totalHits);
            
            return totalHits;

        } catch (Exception e) {
            log.error("Error getting count for org: " + orgName + ", stage: " + stageMin + "-" + stageMax, e);
            return 0;
        }
    }

    // ==================== HELPER METHODS ====================
    private List<Organization> getOrganizations(long companyId, long organizationId) {
        try {
            List<Organization> organizations;
            if (organizationId == 0) {
                // Get top-level organizations
                organizations = OrganizationLocalServiceUtil.getOrganizations(companyId, 0);
                if (!organizations.isEmpty()) {
                    long topLevelOrgId = organizations.get(0).getOrganizationId();
                    organizations = OrganizationLocalServiceUtil.getOrganizations(companyId, topLevelOrgId);
                }
            } else {
                // Get specific organization and its children
                organizations = OrganizationLocalServiceUtil.getOrganizations(companyId, organizationId);
            }
            return organizations;
        } catch (Exception e) {
            log.error("Error getting organizations", e);
            return new ArrayList<>();
        }
    }

    private JSONObject buildResponse(Map<String, Long> totals, JSONArray orgCounts,
            String fromDate, String toDate, String year, String cbsReference,
            String typeOfInsurance, String urn) {

        JSONObject response = JSONFactoryUtil.createJSONObject();

        // ==================== TOTAL COUNTS ====================
        long totalCount = totals.getOrDefault("totalCount", 0L);
        long deletedCount = totals.getOrDefault("deletedCount", 0L);
        long nonDeletedCount = totals.getOrDefault("nonDeletedCount", 0L);
        long pendingForApproval = totals.getOrDefault("pendingForApproval", 0L);
        long initiatedCount = totals.getOrDefault("initiatedCount", 0L);
        long pendingMakerCount = totals.getOrDefault("pendingMakerCount", 0L);
        long approvedCount = totals.getOrDefault("approvedCount", 0L);

        response.put(ConsentDashboardConstants.RESPONSE_SUCCESS, true);
        response.put(ConsentDashboardConstants.RESPONSE_TOTAL_COUNT, totalCount);
        response.put(ConsentDashboardConstants.RESPONSE_DELETED_COUNT, deletedCount);
        response.put(ConsentDashboardConstants.RESPONSE_NON_DELETED_COUNT, nonDeletedCount);
        response.put(ConsentDashboardConstants.RESPONSE_PENDING_APPROVAL, pendingForApproval);
        response.put(ConsentDashboardConstants.RESPONSE_INITIATED_COUNT, initiatedCount);
        response.put(ConsentDashboardConstants.RESPONSE_PENDING_MAKER_COUNT, pendingMakerCount);
        response.put(ConsentDashboardConstants.RESPONSE_APPROVED_COUNT, approvedCount);

        // ==================== CALCULATE ORGANIZATION TOTALS ====================
        long totalInitiated = 0, totalPendingMaker = 0, totalPendingChecker = 0;
        long totalApproved = 0, totalDeleted = 0, totalAll = 0;

        for (int i = 0; i < orgCounts.length(); i++) {
            JSONObject org = orgCounts.getJSONObject(i);
            if (org.has("error")) continue;

            totalInitiated += Long.parseLong(org.getString("initiatedCount"));
            totalPendingMaker += Long.parseLong(org.getString("pendingMakerCount"));
            totalPendingChecker += Long.parseLong(org.getString("pendingCheckerCount"));
            totalApproved += Long.parseLong(org.getString("approvedCount"));
            totalDeleted += Long.parseLong(org.getString("deletedCount"));
            totalAll += Long.parseLong(org.getString("totalCount"));
        }

        // ==================== ORGANIZATION SUMMARY ====================
        response.put("totalInitiatedFromOrgs", String.valueOf(totalInitiated));
        response.put("totalPendingMakerFromOrgs", String.valueOf(totalPendingMaker));
        response.put("totalPendingCheckerFromOrgs", String.valueOf(totalPendingChecker));
        response.put("totalApprovedFromOrgs", String.valueOf(totalApproved));
        response.put("totalDeletedFromOrgs", String.valueOf(totalDeleted));
        response.put("totalAllFromOrgs", String.valueOf(totalAll));

        response.put(ConsentDashboardConstants.RESPONSE_ORGANIZATION_COUNTS, orgCounts);
        response.put("totalCountSource", "blueprint");

        // ==================== FILTERS USED ====================
        JSONObject filters = JSONFactoryUtil.createJSONObject();
        filters.put(ConsentDashboardConstants.REQ_FROM_DATE, fromDate);
        filters.put(ConsentDashboardConstants.REQ_TO_DATE, toDate);
        filters.put(ConsentDashboardConstants.REQ_YEAR, year);
        filters.put(ConsentDashboardConstants.REQ_CBS_REFERENCE, cbsReference);
        filters.put(ConsentDashboardConstants.REQ_TYPE_OF_INSURANCE, typeOfInsurance);
        filters.put(ConsentDashboardConstants.REQ_URN, urn);
        response.put(ConsentDashboardConstants.RESPONSE_FILTERS, filters);

        // ==================== STAGE SUMMARY ====================
        JSONObject stageSummary = JSONFactoryUtil.createJSONObject();
        stageSummary.put("Initiated (Stage 0)", initiatedCount);
        stageSummary.put("Pending with Maker (Stage 1)", pendingMakerCount);
        stageSummary.put("Pending with Checker (Stage 2)", pendingForApproval);
        stageSummary.put("Approved (Stage 3)", approvedCount);
        stageSummary.put("Deleted (Stage 4)", deletedCount);
        response.put("stageSummary", stageSummary);

        return response;
    }

    // ==================== SHUTDOWN HOOK ====================
    @Override
    protected void finalize() throws Throwable {
        EXECUTOR.shutdown();
        super.finalize();
    }
}