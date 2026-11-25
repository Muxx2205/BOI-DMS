package com.vls.dde.ret.api.internal.resource.v1_0;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.Authenticator;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.vls.dde.ret.api.dto.v1_0.DdeReturnRequest;
import com.vls.dde.ret.api.dto.v1_0.DdeReturnResponse;
import com.vls.dde.ret.api.resource.v1_0.DdeReturnResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * NESL DDE Return API
 * 
 * Stores signed documents (Base64) from CBS in a branch-wise folder structure.
 * 
 * Key design:
 *   - Filenames are UNIQUE: dde_{docId}_{txnID}_{timestamp}_{random}.pdf
 *     (uses APIRequestHandler.appendTimestamp() — same pattern as Consent module)
 *   - NO idempotency check — every send is processed
 *   - OVERWRITE-ONLY on same txnID + docId: if a matching Object Entry exists,
 *     the old file is deleted and the entry is updated (no duplicate entries)
 *   - Branch folder resolved from branchCode (first 4 digits) via Organizations
 * 
 * Folder structure (shared with Consent module):
 *   [HODefualtFolderId] → NBG → Zone → Branch → Nesl
 */
@Component(
    configurationPid = "com.boi.headless.rest.common.application.BOIWorkflowConfiguation",
    properties = "OSGI-INF/liferay/rest/v1_0/dde-return.properties",
    scope = ServiceScope.PROTOTYPE,
    service = DdeReturnResource.class
)
public class DdeReturnResourceImpl extends BaseDdeReturnResourceImpl {

    // ==================== CONSTANTS ====================
    private static final String FOLDER_NESL = "Nesl";
    private static final String REQUIRED_ROLE_NAME = "Nesldde";
    private static final String ADMINISTRATOR_ROLE_NAME = "Administrator";
    private static final String FILE_BASE_NAME = "dde";
    private static final String FILE_EXTENSION = "pdf";

    // Security
    private static final int MAX_CONTENT_LENGTH = 10485760;
    private static final int MAX_TXNID_LENGTH = 50;
    private static final int MIN_TXNID_LENGTH = 5;
    private static final String ALLOWED_TXNID_PATTERN = "^[a-zA-Z0-9]{" + MIN_TXNID_LENGTH + "," + MAX_TXNID_LENGTH + "}$";
    private static final String ALLOWED_UIN_PATTERN = "^[A-Z0-9]{5,20}$";
    private static final String ALLOWED_RELATION_PATTERN = "^[a-zA-Z0-9\\s]{1,50}$";
    private static final String ALLOWED_HOST_PATTERN = "^[a-zA-Z0-9.-]+(:[0-9]+)?$";
    private static final String ALLOWED_CONTENT_TYPE_PATTERN = "^(application/json|application/xml)(;.*)?$";

    // Branch
    private static final Pattern NON_NUMERIC_PATTERN = Pattern.compile("[^0-9]");
    private static final Set<String> TOP_LEVEL_ORGS = Set.of("BOI", "BANK OF INDIA", "HEAD OFFICE", "HO");

    // Cache
    private static final Map<String, HierarchyInfo> HIERARCHY_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 3600000;

    private static final ConcurrentHashMap<String, FailedLoginAttempt> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 900000;

    private static final long GUEST_USER_ID = 0;
    private static final Log _log = LogFactoryUtil.getLog(DdeReturnResourceImpl.class);

    // Config
    private long ddeReturnObjectDefinitionId = 0L;
    private long defaultFolderId = 0L;
    private BOIWorkflowConfiguation boiWorkflowConfiguation;

    // ==================== OSGI REFERENCES ====================
    @Reference
    private ObjectEntryLocalService objectEntryLocalService;

    @Reference
    private ObjectDefinitionLocalService objectDefinitionLocalService;

    @Reference
    private ObjectFieldLocalService objectFieldLocalService;

    @Reference
    private OrganizationLocalService organizationLocalService;

    @Reference
    private DLAppService dlAppService;

    @Reference
    private DTOConverterRegistry dtoConverterRegistry;

    @Reference
    private ObjectEntryManagerRegistry objectEntryManagerRegistry;

    @Reference
    private WorkflowTaskManager workflowTaskManager;

    // ==================== ACTIVATE ====================
    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(
                BOIWorkflowConfiguation.class, properties);

        ddeReturnObjectDefinitionId = boiWorkflowConfiguation.DDEReturnObjectDefinitionId();
        defaultFolderId = boiWorkflowConfiguation.HODefualtFolderId();

        _log.info("=== DDE RETURN ACTIVATION ===");
        _log.info("DDE Return Object Definition ID: " + ddeReturnObjectDefinitionId);
        _log.info("HO Default Folder ID (from config): " + defaultFolderId);
    }

    private String generateExternalRefCode(String txnID, Integer docId) {
        return txnID + "_" + docId;
    }
    
    // ==================== HierarchyInfo ====================
    private static class HierarchyInfo {
        final String branchCode;
        final String branchName;
        final String branchFolderName;
        final String zone;
        final String zoneFolderName;
        final String nbgCode;
        final String nbgFolderName;
        final long timestamp;

        HierarchyInfo(String bc, String bn, String bfn, String z, String zfn, String n, String nfn) {
            this.branchCode = bc;
            this.branchName = bn;
            this.branchFolderName = bfn;
            this.zone = z;
            this.zoneFolderName = zfn;
            this.nbgCode = n;
            this.nbgFolderName = nfn;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() { return (System.currentTimeMillis() - timestamp) < CACHE_TTL; }
        boolean isEmpty() { return branchCode == null && branchName == null && zone == null && nbgCode == null; }
    }

    // ==================== MAIN API ====================
    @Override
    public DdeReturnResponse postDdeReturn(String txnID, DdeReturnRequest[] ddeReturnRequests) throws Exception {
        _log.info("=== START POST DDE RETURN ===");

        // 1. HTTP Method
        String httpMethod = contextHttpServletRequest.getMethod();
        if (!"POST".equalsIgnoreCase(httpMethod)) {
            throw new WebApplicationException(
                Response.status(Response.Status.METHOD_NOT_ALLOWED)
                        .header("Allow", "POST")
                        .entity(buildErrorResponse(
                            "HTTP method '" + httpMethod + "' is not allowed. Only POST is supported",
                            "004"))
                        .build());
        }

        // 2. Auth
        User authenticatedUser = validateAuthenticationAndAuthorization();

        // 3. Headers
        validateHttpHeaders();

        // 4. txnID
        if (Validator.isBlank(txnID)) {
            throw buildWebAppException(
                "Transaction ID (txnID) is required in the request header",
                "002", Response.Status.BAD_REQUEST);
        }
        if (!txnID.matches(ALLOWED_TXNID_PATTERN)) {
            throw buildWebAppException(
                "Invalid txnID format: '" + txnID + "'. Only alphanumeric characters (A-Z, a-z, 0-9), " +
                "length 5-50 allowed",
                "002", Response.Status.BAD_REQUEST);
        }

        // 5. Body
        if (ddeReturnRequests == null || ddeReturnRequests.length == 0) {
            throw buildWebAppException(
                "Request body cannot be empty. Please provide at least one document to process",
                "004", Response.Status.BAD_REQUEST);
        }
        if (ddeReturnRequests.length > 100) {
            throw buildWebAppException(
                "Maximum 100 documents allowed per request. Received: " + ddeReturnRequests.length,
                "004", Response.Status.BAD_REQUEST);
        }

        // 6. Service Context
        ServiceContext serviceContext = ServiceContextFactory.getInstance(
                ObjectDefinition.class.getName(), contextHttpServletRequest);
        Group group = GroupLocalServiceUtil.getGroup(
                PortalUtil.getCompanyId(contextHttpServletRequest), "BOI-DMS");
        long scopeGroupId = group.getGroupId();
        serviceContext.setUserId(authenticatedUser.getUserId());
        serviceContext.setCreateDate(new Date());

        // 7. Verify Object Definition
        ObjectDefinition objectDefinition = objectDefinitionLocalService.fetchObjectDefinition(ddeReturnObjectDefinitionId);
        if (objectDefinition == null) {
            throw buildWebAppException(
                "Object definition not found for ID: " + ddeReturnObjectDefinitionId +
                ". Please verify DDEReturnObjectDefinitionId in BOIWorkflowConfiguation",
                "005", Response.Status.INTERNAL_SERVER_ERROR);
        }

        // 8. HO folder from config
        long hoFolderId = defaultFolderId;
        if (hoFolderId <= 0) {
            throw buildWebAppException(
                "HODefualtFolderId is not configured in BOIWorkflowConfiguation. " +
                "Please set it in System Settings → BOIWorkflowConfiguation",
                "005", Response.Status.INTERNAL_SERVER_ERROR);
        }

        _log.info("Using Head Office folder ID from config: " + hoFolderId);

        // 9. Build APIRequestHandler for filename generation (Consent-style)
        APIRequestHandler apiRequestHandler = getAPIRequestHandler(hoFolderId);

        // 10. Process each request
        int successCount = 0, errorCount = 0, overwriteCount = 0;
        int totalRequested = ddeReturnRequests.length;

        List<String> failureReasons = new ArrayList<>();
        List<String> successDetails = new ArrayList<>();

        for (DdeReturnRequest request : ddeReturnRequests) {
            _log.info("--- Processing docId: " + request.getDocId() + " ---");

            // Validation
            String validationError = validateRequest(request);
            if (validationError != null) {
                failureReasons.add("docId=" + request.getDocId() + ": " + validationError);
                errorCount++;
                continue;
            }

            try {
                // Resolve branch
                String branchPrefix = extractFirstFourDigits(request.getBranchCode());
                if (Validator.isNull(branchPrefix)) {
                    failureReasons.add("docId=" + request.getDocId() +
                        ": Cannot extract first 4 digits from branchCode: '" + request.getBranchCode() + "'");
                    errorCount++;
                    continue;
                }

                HierarchyInfo hierarchy = getHierarchyFromOrganization(branchPrefix);

                if (hierarchy.isEmpty() || Validator.isNull(hierarchy.branchFolderName)) {
                    _log.warn("Falling back to request values for branch: " + branchPrefix);
                    hierarchy = new HierarchyInfo(
                        branchPrefix,
                        request.getBranch(),
                        removeAllWhitespace(Validator.isNotNull(request.getBranch()) ?
                                            request.getBranch() : branchPrefix),
                        request.getZone(),
                        removeAllWhitespace(request.getZone()),
                        request.getNbgCode(),
                        removeAllWhitespace(request.getNbgCode())
                    );
                }

                // Build folder path
                DLFolder neslFolder = buildBranchFolderPath(
                        scopeGroupId, hoFolderId, hierarchy, serviceContext);

                // ==================== CHECK FOR EXISTING ENTRY (txnID + docId) ====================
                ObjectEntry existingEntry = findObjectEntryByTxnDocKey(txnID, request.getDocId());
                boolean isOverwrite = existingEntry != null;

                if (isOverwrite) {
                    _log.info("Re-send detected for txnID=" + txnID + ", docId=" + request.getDocId() +
                              ". Old entry ID: " + existingEntry.getObjectEntryId() + " — will overwrite.");
                }

                // ==================== GENERATE UNIQUE FILENAME (Consent-style) ====================
                String baseName = FILE_BASE_NAME + "_" + request.getDocId() + ".pdf";
                String uniqueFileName = apiRequestHandler.appendTimestamp(baseName, txnID, false);
                _log.info("Generated filename: " + uniqueFileName);

                // ==================== DELETE OLD FILE IF OVERWRITING ====================
                if (isOverwrite) {
                    Long oldFileEntryId = getLongFromValues(existingEntry.getValues(), "fileEntryId");
                    if (oldFileEntryId != null && oldFileEntryId > 0) {
                        try {
                            DLFileEntry oldFile = DLFileEntryLocalServiceUtil.fetchDLFileEntry(oldFileEntryId);
                            if (oldFile != null) {
                                DLFileEntryLocalServiceUtil.deleteDLFileEntry(oldFile);
                                _log.info("Deleted old file: fileEntryId=" + oldFileEntryId);
                            }
                        } catch (Exception e) {
                            _log.warn("Could not delete old file: " + oldFileEntryId + " — " + e.getMessage());
                        }
                    }
                }

                // ==================== STORE NEW FILE ====================
                DLFileEntry fileEntry = storeFileFromBase64(
                        txnID, request, neslFolder.getFolderId(), scopeGroupId,
                        serviceContext, uniqueFileName);

                // ==================== BUILD OBJECT ENTRY VALUES ====================
                Map<String, Serializable> values = new HashMap<>();
                values.put("txnID", sanitizeInput(txnID));
                values.put("docId", request.getDocId());
                values.put("uin", sanitizeInput(request.getUin()));
                values.put("relToCntrct", sanitizeInput(request.getReltocntrct()));
                values.put("signatoryName", sanitizeInput(request.getSignatoryName()));
                values.put("signedName", sanitizeInput(request.getSignedName()));
                values.put("matchPercentage", request.getMatchPercentage());
                values.put("txnDocKey", txnID + "_" + request.getDocId());
                values.put("fileEntryId", fileEntry.getFileEntryId());
                values.put("fileName", uniqueFileName);

                values.put("branchCode", hierarchy.branchCode);
                if (Validator.isNotNull(hierarchy.branchName)) {
                    values.put("branch", hierarchy.branchName);
                } else if (Validator.isNotNull(request.getBranch())) {
                    values.put("branch", request.getBranch());
                }
                if (Validator.isNotNull(hierarchy.zone)) {
                    values.put("zone", hierarchy.zone);
                } else if (Validator.isNotNull(request.getZone())) {
                    values.put("zone", request.getZone());
                }
                if (Validator.isNotNull(hierarchy.nbgCode)) {
                    values.put("nBGCode", hierarchy.nbgCode);
                } else if (Validator.isNotNull(request.getNbgCode())) {
                    values.put("nBGCode", request.getNbgCode());
                }

                // ==================== UPDATE OR CREATE ENTRY ====================
                if (isOverwrite) {
                    // Merge into existing values and update
                    Map<String, Serializable> existingValues = existingEntry.getValues();
                    existingValues.putAll(values);

                    objectEntryLocalService.updateObjectEntry(
                            authenticatedUser.getUserId(),
                            existingEntry.getObjectEntryId(),
                            existingValues,
                            serviceContext);

                    overwriteCount++;
                    successDetails.add("docId=" + request.getDocId() + " (overwritten, fileEntryId=" + fileEntry.getFileEntryId() + ")");
                    _log.info("✅ Overwrote entry ID=" + existingEntry.getObjectEntryId());

                } else {
                    // Use externalReferenceCode = txnID + "_" + docId for indexed lookup
                    String extRefCode = generateExternalRefCode(txnID, request.getDocId());
                    objectEntryLocalService.addOrUpdateObjectEntry(
                            extRefCode,
                            authenticatedUser.getUserId(), 0,
                            ddeReturnObjectDefinitionId, values, serviceContext);

                    successCount++;
                    successDetails.add("docId=" + request.getDocId() + " (new, fileEntryId=" + fileEntry.getFileEntryId() + ")");
                    _log.info("✅ Created new entry, fileEntryId=" + fileEntry.getFileEntryId() +
                              ", externalRefCode=" + extRefCode);
                }

            } catch (Exception e) {
                _log.error("Failed to process docId=" + request.getDocId(), e);
                String reason = extractFailureReason(e, request.getDocId(), txnID);
                failureReasons.add(reason);
                errorCount++;
            }
        }

        // ==================== BUILD RESPONSE ====================
        int totalSuccess = successCount + overwriteCount;

        DdeReturnResponse response = new DdeReturnResponse();
        response.setTxnID(txnID);

        if (errorCount == 0 && totalSuccess == totalRequested) {
            response.setStatus(String.format(
                "All %d document(s) received and stored successfully " +
                "(%d new, %d overwritten): [%s]",
                totalRequested, successCount, overwriteCount,
                String.join(", ", successDetails)));
            response.setStatusCode("001");

        } else if (totalSuccess > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "Partial success: %d of %d document(s) stored (%d new, %d overwritten), %d failed. ",
                totalSuccess, totalRequested, successCount, overwriteCount, errorCount));

            if (!failureReasons.isEmpty()) {
                sb.append("Failures: [");
                sb.append(String.join("; ", failureReasons));
                sb.append("]");
            }

            response.setStatus(sb.toString());
            response.setStatusCode("001");

        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "All %d document(s) failed to process. ", totalRequested));

            if (!failureReasons.isEmpty()) {
                sb.append("Reasons: [");
                sb.append(String.join("; ", failureReasons));
                sb.append("]");
            } else {
                sb.append("Please check server logs for details");
            }

            response.setStatus(sb.toString());
            response.setStatusCode("009");
        }

        addSecurityHeaders(response);
        _log.info("=== END POST DDE RETURN === Response: " + response.getStatus());
        return response;
    }

    // ==================== API REQUEST HANDLER ====================
    private APIRequestHandler getAPIRequestHandler(long hoFolderId) {
        return new APIRequestHandler(
                contextUser,
                contextHttpServletRequest,
                contextAcceptLanguage,
                objectFieldLocalService,
                dlAppService,
                contextUriInfo,
                dtoConverterRegistry,
                objectEntryManagerRegistry,
                objectDefinitionLocalService,
                objectEntryLocalService,
                workflowTaskManager,
                boiWorkflowConfiguation,
                BOIDMSConstants.DDE_RETURN,
                hoFolderId
        );
    }

    // ==================== EXISTING ENTRY LOOKUP ====================
    private ObjectEntry findObjectEntryByTxnDocKey(String txnID, Integer docId) {
        try {
            String extRefCode = generateExternalRefCode(txnID, docId);
            ObjectEntry entry = objectEntryLocalService.fetchObjectEntry(
                    extRefCode, ddeReturnObjectDefinitionId);
            if (entry != null) {
                _log.info("Found existing entry by externalRefCode=" + extRefCode +
                          " (ID: " + entry.getObjectEntryId() + ")");
                return entry;
            }
        } catch (Exception e) {
            _log.error("Error finding entry by externalRefCode=" + txnID + "_" + docId, e);
        }
        return null;
    }

    private Long getLongFromValues(Map<String, Serializable> values, String key) {
        if (values == null) return null;
        Object val = values.get(key);
        if (val == null) return null;
        try {
            if (val instanceof Number) return ((Number) val).longValue();
            return Long.parseLong(String.valueOf(val));
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== VALIDATION ====================
    private String validateRequest(DdeReturnRequest request) {
        if (!hasRequiredFields(request)) {
            return "Missing required fields. Required: docId, uin, reltocntrct, signedDocData, " +
                   "signatoryName, signedName, matchPercentage";
        }
        if (Validator.isBlank(request.getBranchCode())) {
            return "branchCode is required to resolve branch hierarchy";
        }
        if (!request.getUin().matches(ALLOWED_UIN_PATTERN)) {
            return "Invalid UIN format: '" + request.getUin() +
                   "'. Only uppercase alphanumeric (5-20 chars) allowed";
        }
        if (!request.getReltocntrct().matches(ALLOWED_RELATION_PATTERN)) {
            return "Invalid reltocntrct value: '" + request.getReltocntrct() +
                   "'. Only alphanumeric and spaces (max 50 chars) allowed";
        }
        if (request.getMatchPercentage() < 0 || request.getMatchPercentage() > 100) {
            return "Invalid matchPercentage: " + request.getMatchPercentage() +
                   ". Must be between 0 and 100";
        }
        if (!isValidBase64(request.getSignedDocData())) {
            return "Invalid Base64 value for signedDocData. " +
                   "Please verify it is a valid Base64-encoded PDF";
        }
        if (containsXSSPatterns(request.getSignatoryName()) ||
            containsXSSPatterns(request.getSignedName())) {
            return "Invalid characters detected in signatoryName or signedName";
        }
        return null;
    }

    // ==================== ERROR REASON EXTRACTION ====================
    private String extractFailureReason(Exception e, Integer docId, String txnID) {
        String prefix = "docId=" + docId + ": ";
        String msg = e.getMessage();

        if (Validator.isBlank(msg)) {
            Throwable cause = e.getCause();
            if (cause != null && !Validator.isBlank(cause.getMessage())) {
                msg = cause.getMessage();
            } else {
                msg = e.getClass().getSimpleName();
            }
        }

        msg = msg.replaceAll("\\s+", " ").trim();

        if (msg.length() > 200) {
            msg = msg.substring(0, 200) + "...";
        }

        return prefix + msg;
    }

    // ==================== FOLDER PATH BUILDER ====================
    private DLFolder buildBranchFolderPath(
            long scopeGroupId, long hoFolderId,
            HierarchyInfo hierarchy, ServiceContext serviceContext) throws Exception {

        long currentParentId = hoFolderId;

        // NBG
        if (Validator.isNotNull(hierarchy.nbgFolderName)) {
            DLFolder nbgFolder = getOrCreateFolder(scopeGroupId, currentParentId,
                    hierarchy.nbgFolderName, serviceContext);
            currentParentId = nbgFolder.getFolderId();
            _log.info("NBG folder resolved: " + hierarchy.nbgFolderName + " (ID: " + currentParentId + ")");
        }

        // Zone
        if (Validator.isNotNull(hierarchy.zoneFolderName)) {
            DLFolder zoneFolder = getOrCreateFolder(scopeGroupId, currentParentId,
                    hierarchy.zoneFolderName, serviceContext);
            currentParentId = zoneFolder.getFolderId();
            _log.info("Zone folder resolved: " + hierarchy.zoneFolderName + " (ID: " + currentParentId + ")");
        }

        // Branch
        if (Validator.isNotNull(hierarchy.branchFolderName)) {
            DLFolder branchFolder = getOrCreateFolder(scopeGroupId, currentParentId,
                    hierarchy.branchFolderName, serviceContext);
            currentParentId = branchFolder.getFolderId();
            _log.info("Branch folder resolved: " + hierarchy.branchFolderName + " (ID: " + currentParentId + ")");
        }

        // Nesl
        DLFolder neslFolder = getOrCreateFolder(scopeGroupId, currentParentId,
                FOLDER_NESL, serviceContext);
        _log.info("Nesl folder resolved: " + FOLDER_NESL + " (ID: " + neslFolder.getFolderId() + ")");

        return neslFolder;
    }

    // ==================== HIERARCHY LOOKUP ====================
    private HierarchyInfo getHierarchyFromOrganization(String branchPrefix) {
        if (Validator.isNull(branchPrefix)) {
            return new HierarchyInfo(null, null, null, null, null, null, null);
        }

        HierarchyInfo cached = HIERARCHY_CACHE.get(branchPrefix);
        if (cached != null && cached.isValid()) {
            return cached;
        }

        try {
            List<Organization> organizations = organizationLocalService.getOrganizations(
                    contextCompany.getCompanyId(),
                    OrganizationConstants.ANY_PARENT_ORGANIZATION_ID);

            Organization branchOrg = null;
            for (Organization org : organizations) {
                String orgName = org.getName();
                if (orgName.startsWith(branchPrefix) ||
                    orgName.startsWith(branchPrefix + " - ") ||
                    orgName.startsWith(branchPrefix + "-")) {
                    branchOrg = org;
                    break;
                }
            }

            if (branchOrg == null) {
                for (Organization org : organizations) {
                    String numericOnly = NON_NUMERIC_PATTERN.matcher(org.getName()).replaceAll("");
                    if (numericOnly.startsWith(branchPrefix)) {
                        branchOrg = org;
                        break;
                    }
                }
            }

            if (branchOrg == null) {
                _log.warn("No branch Organization found for prefix: " + branchPrefix);
                return new HierarchyInfo(branchPrefix, null, null, null, null, null, null);
            }

            String branchName = branchOrg.getName();
            String branchFolderName = removeAllWhitespace(branchName);

            String zoneName = null, zoneFolderName = null;
            String nbgCode = null, nbgFolderName = null;

            long parentId = branchOrg.getParentOrganizationId();
            boolean foundZone = false, foundNbg = false;

            while (parentId > 0) {
                try {
                    Organization parentOrg = organizationLocalService.getOrganization(parentId);
                    String parentName = parentOrg.getName();
                    String parentType = parentOrg.getType();

                    if (!foundNbg && isNBGOrganization(parentName, parentType)) {
                        nbgCode = parentName;
                        nbgFolderName = removeAllWhitespace(parentName);
                        foundNbg = true;
                    } else if (!foundZone && !isTopLevelOrganization(parentName) &&
                               !isNBGOrganization(parentName, parentType)) {
                        zoneName = parentName;
                        zoneFolderName = removeAllWhitespace(parentName);
                        foundZone = true;
                    }

                    if (foundZone && foundNbg) break;
                    if (isTopLevelOrganization(parentName)) break;

                    parentId = parentOrg.getParentOrganizationId();
                } catch (Exception e) {
                    break;
                }
            }

            HierarchyInfo info = new HierarchyInfo(
                branchPrefix, branchName, branchFolderName,
                zoneName, zoneFolderName, nbgCode, nbgFolderName);

            HIERARCHY_CACHE.put(branchPrefix, info);
            return info;

        } catch (Exception e) {
            _log.error("Error fetching hierarchy for: " + branchPrefix, e);
            return new HierarchyInfo(branchPrefix, null, null, null, null, null, null);
        }
    }

    // ==================== HELPERS ====================
    private String extractFirstFourDigits(String value) {
        if (Validator.isNull(value)) return null;
        String numericOnly = NON_NUMERIC_PATTERN.matcher(value).replaceAll("");
        if (numericOnly.length() >= 4) return numericOnly.substring(0, 4);
        return numericOnly.isEmpty() ? null : numericOnly;
    }

    private String removeAllWhitespace(String input) {
        return input == null ? null : input.replaceAll("\\s+", "");
    }

    private boolean isTopLevelOrganization(String orgName) {
        if (Validator.isNull(orgName)) return false;
        return TOP_LEVEL_ORGS.contains(orgName.toUpperCase());
    }

    private boolean isNBGOrganization(String orgName, String orgType) {
        if (Validator.isNull(orgName)) return false;
        if (Validator.isNotNull(orgType) && "NBG".equalsIgnoreCase(orgType)) return true;
        return orgName.regionMatches(true, 0, "NBG", 0, 3);
    }

    // ==================== FOLDER HELPERS ====================
    private DLFolder getOrCreateFolder(long scopeGroupId, long parentFolderId,
                                       String folderName, ServiceContext serviceContext) throws Exception {
        if (Validator.isBlank(folderName)) {
            throw new Exception(
                "Cannot create folder with blank name (parent folder ID: " + parentFolderId + ")");
        }

        DLFolder folder = DLFolderLocalServiceUtil.fetchFolder(scopeGroupId, parentFolderId, folderName);

        if (folder == null) {
            _log.info("Creating folder: " + folderName + " (parentId=" + parentFolderId + ")");
            folder = DLFolderLocalServiceUtil.addFolder(
                    UUID.randomUUID().toString(),
                    contextUser.getUserId(),
                    scopeGroupId,
                    scopeGroupId,
                    false,
                    parentFolderId,
                    folderName,
                    "Auto-created folder for DDE Return",
                    false,
                    serviceContext);
        }

        return folder;
    }

    // ==================== STORE FILE ====================
    private DLFileEntry storeFileFromBase64(String txnID, DdeReturnRequest request,
            long folderId, long scopeGroupId,
            ServiceContext serviceContext,
            String uniqueFileName) throws Exception {

        _log.info("📁 Storing file: " + uniqueFileName + " in folder=" + folderId);

        byte[] fileBytes;
        try {
            fileBytes = Base64.getDecoder().decode(request.getSignedDocData());
        } catch (IllegalArgumentException e) {
            throw new Exception("Failed to decode Base64 document data: " + e.getMessage());
        }

        if (fileBytes.length > MAX_CONTENT_LENGTH) {
            throw new Exception(
                "File size (" + fileBytes.length + " bytes) exceeds maximum allowed " +
                "(" + MAX_CONTENT_LENGTH + " bytes)");
        }

        serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            Date now = new Date();

            DLFileEntry fileEntry = DLFileEntryLocalServiceUtil.addFileEntry(
                    null, contextUser.getUserId(), scopeGroupId, scopeGroupId,
                    folderId, uniqueFileName, "application/pdf",
                    uniqueFileName,    // ← title = uniqueFileName (unique per attempt)
                    null,
                    "Uploaded via DDE Return", "", 0, new HashMap<>(), null,
                    is, fileBytes.length, now, null, null, serviceContext);

            _log.info("✅ File added: fileEntryId=" + fileEntry.getFileEntryId());

            // Force APPROVED status
            try {
                DLFileVersion fileVersion = fileEntry.getLatestFileVersion(true);
                Map<String, Serializable> workflowContext = new HashMap<>();
                workflowContext.put("status", WorkflowConstants.STATUS_APPROVED);
                DLFileEntryLocalServiceUtil.updateStatus(
                    contextUser.getUserId(), fileVersion.getFileVersionId(),
                    WorkflowConstants.STATUS_APPROVED, serviceContext, workflowContext);
                _log.info("✅ Status set to APPROVED");
            } catch (Exception e) {
                _log.warn("Could not update status: " + e.getMessage());
            }

            return DLFileEntryLocalServiceUtil.getFileEntry(fileEntry.getFileEntryId());

        } catch (Exception e) {
            _log.error("❌ Error storing file: " + uniqueFileName, e);
            throw e;
        }
    }

    // ==================== AUTH ====================
    private User validateAuthenticationAndAuthorization() throws WebApplicationException {
        try {
            String authHeader = contextHttpServletRequest.getHeader("Authorization");

            if (Validator.isBlank(authHeader)) {
                throw buildWebAppException(
                    "Authentication required. Please provide Basic Auth credentials",
                    "401", Response.Status.UNAUTHORIZED);
            }
            if (!authHeader.startsWith("Basic ")) {
                throw buildWebAppException(
                    "Invalid authentication type. Only Basic Auth is supported",
                    "401", Response.Status.UNAUTHORIZED);
            }

            String base64Credentials = authHeader.substring(6);
            String credentials;
            try {
                credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw buildWebAppException(
                    "Invalid credentials format. Basic Auth must be Base64-encoded 'username:password'",
                    "401", Response.Status.UNAUTHORIZED);
            }

            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                throw buildWebAppException(
                    "Invalid credentials format. Expected 'username:password' after Base64 decoding",
                    "401", Response.Status.UNAUTHORIZED);
            }

            String username = parts[0];
            String password = parts[1];

            if (Validator.isBlank(username) || Validator.isBlank(password)) {
                throw buildWebAppException(
                    "Username and password cannot be empty",
                    "401", Response.Status.UNAUTHORIZED);
            }

            checkFailedLoginAttempts(username);

            User authenticatedUser = validateLiferayUser(username, password);
            if (authenticatedUser == null) {
                recordFailedLoginAttempt(username);
                throw buildWebAppException(
                    "Invalid username or password",
                    "401", Response.Status.UNAUTHORIZED);
            }

            clearFailedLoginAttempts(username);

            if (authenticatedUser.isGuestUser() || authenticatedUser.getUserId() == GUEST_USER_ID) {
                throw buildWebAppException(
                    "Guest access is not permitted. Please authenticate with valid credentials",
                    "401", Response.Status.UNAUTHORIZED);
            }

            if (!hasRequiredRole(authenticatedUser)) {
                throw buildWebAppException(
                    "Access denied. User '" + username + "' must have '" + REQUIRED_ROLE_NAME +
                    "' role or be an Administrator to access this API",
                    "403", Response.Status.FORBIDDEN);
            }

            if (contextUser == null || !contextUser.getScreenName().equals(username)) {
                throw buildWebAppException(
                    "Authentication mismatch. Please verify your credentials",
                    "401", Response.Status.UNAUTHORIZED);
            }

            if (!authenticatedUser.isActive()) {
                throw buildWebAppException(
                    "User account '" + username + "' is inactive. Please contact administrator",
                    "401", Response.Status.UNAUTHORIZED);
            }

            _log.info("✅ Auth successful for: " + username);
            return authenticatedUser;

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            _log.error("Auth error", e);
            throw buildWebAppException(
                "Authentication failed. Please check your credentials and try again",
                "401", Response.Status.UNAUTHORIZED);
        }
    }

    private User validateLiferayUser(String username, String password) {
        try {
            long companyId = PortalUtil.getCompanyId(contextHttpServletRequest);
            User user = UserLocalServiceUtil.getUserByScreenName(companyId, username);
            if (user == null || !user.isActive() || user.isGuestUser()) return null;

            Map<String, Object> resultsMap = new HashMap<>();
            int authResult = UserLocalServiceUtil.authenticateByScreenName(
                companyId, username, password, null, null, resultsMap);

            if (authResult == Authenticator.SUCCESS) {
                Long userId = (Long) resultsMap.get("userId");
                if (userId != null && userId > 0) {
                    User u = UserLocalServiceUtil.getUser(userId);
                    if (u != null && !u.isGuestUser()) return u;
                }
                if (!user.isGuestUser()) return user;
            }
            return null;
        } catch (PrincipalException e) {
            return null;
        } catch (Exception e) {
            _log.error("Error validating user: " + username, e);
            return null;
        }
    }

    private boolean isAdministrator(User user) {
        if (user == null) return false;
        try {
            long companyId = PortalUtil.getCompanyId(contextHttpServletRequest);
            Role adminRole = RoleLocalServiceUtil.getRole(companyId, ADMINISTRATOR_ROLE_NAME);
            if (adminRole != null) {
                return UserLocalServiceUtil.hasRoleUser(adminRole.getRoleId(), user.getUserId());
            }
        } catch (Exception e) {
            _log.warn("Error checking admin: " + e.getMessage());
        }
        return false;
    }

    private boolean hasRequiredRole(User user) throws Exception {
        if (user == null || user.isGuestUser()) return false;
        if (isAdministrator(user)) return true;

        long companyId = PortalUtil.getCompanyId(contextHttpServletRequest);

        try {
            Role role = RoleLocalServiceUtil.getRole(companyId, REQUIRED_ROLE_NAME);
            if (role != null && UserLocalServiceUtil.hasRoleUser(role.getRoleId(), user.getUserId())) {
                return true;
            }
        } catch (Exception e) {
            _log.error("Error checking role: " + e.getMessage());
        }

        for (Role r : RoleLocalServiceUtil.getUserRoles(user.getUserId())) {
            if (REQUIRED_ROLE_NAME.equalsIgnoreCase(r.getName())) return true;
        }
        return false;
    }

    private void checkFailedLoginAttempts(String username) throws WebApplicationException {
        FailedLoginAttempt attempt = failedLoginAttempts.get(username);
        if (attempt != null && attempt.isLocked()) {
            long secondsRemaining = attempt.getLockTimeRemaining() / 1000;
            if (secondsRemaining > 0) {
                throw buildWebAppException(
                    String.format("Account '%s' temporarily locked due to multiple failed attempts. " +
                                  "Please try again in %d seconds", username, secondsRemaining),
                    "401", Response.Status.UNAUTHORIZED);
            } else {
                failedLoginAttempts.remove(username);
            }
        }
    }

    private void recordFailedLoginAttempt(String username) {
        FailedLoginAttempt a = failedLoginAttempts.get(username);
        if (a == null) { a = new FailedLoginAttempt(); failedLoginAttempts.put(username, a); }
        a.incrementAttempts();
    }

    private void clearFailedLoginAttempts(String username) {
        failedLoginAttempts.remove(username);
    }

    // ==================== HEADERS ====================
    private void validateHttpHeaders() throws WebApplicationException {
        String cl = contextHttpServletRequest.getHeader("Content-Length");
        if (cl != null) {
            try {
                int len = Integer.parseInt(cl);
                if (len < 0 || len > MAX_CONTENT_LENGTH) {
                    throw buildWebAppException(
                        "Invalid Content-Length header: " + len + ". Must be between 0 and " + MAX_CONTENT_LENGTH,
                        "004", Response.Status.BAD_REQUEST);
                }
            } catch (NumberFormatException e) {
                throw buildWebAppException(
                    "Invalid Content-Length format: '" + cl + "'. Must be a number",
                    "004", Response.Status.BAD_REQUEST);
            }
        }
        String te = contextHttpServletRequest.getHeader("Transfer-Encoding");
        if (te != null && !"chunked".equalsIgnoreCase(te)) {
            throw buildWebAppException(
                "Invalid Transfer-Encoding: '" + te + "'. Only 'chunked' is supported",
                "004", Response.Status.BAD_REQUEST);
        }
        if (cl != null && te != null) {
            throw buildWebAppException(
                "Invalid request headers. Content-Length and Transfer-Encoding cannot both be present",
                "004", Response.Status.BAD_REQUEST);
        }
        String host = contextHttpServletRequest.getHeader("Host");
        if (host != null && !host.matches(ALLOWED_HOST_PATTERN)) {
            throw buildWebAppException(
                "Invalid Host header: '" + host + "'",
                "004", Response.Status.BAD_REQUEST);
        }
        String ct = contextHttpServletRequest.getHeader("Content-Type");
        if (ct != null && !ct.matches(ALLOWED_CONTENT_TYPE_PATTERN)) {
            throw buildWebAppException(
                "Invalid Content-Type header: '" + ct + "'. Only application/json or application/xml allowed",
                "004", Response.Status.BAD_REQUEST);
        }
    }

    // ==================== XSS ====================
    private boolean containsXSSPatterns(String input) {
        if (input == null) return false;
        String[] patterns = {"<script", "</script>", "javascript:", "onerror=", "onload=",
            "onclick=", "onmouseover=", "alert(", "eval(", "document.", "window.",
            "location.", "<img", "<iframe", "onfocus", "onblur"};
        String lower = input.toLowerCase();
        for (String p : patterns) if (lower.contains(p.toLowerCase())) return true;
        return false;
    }

    private String sanitizeInput(String input) {
        return input == null ? null : input.replaceAll("<[^>]*>", "").replaceAll("[<>\"'&]", "").trim();
    }

    // ==================== SECURITY HEADERS ====================
    private void addSecurityHeaders(DdeReturnResponse response) {
        contextHttpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
        contextHttpServletResponse.setHeader("X-Frame-Options", "DENY");
        contextHttpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");
        contextHttpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        contextHttpServletResponse.setHeader("Pragma", "no-cache");
        contextHttpServletResponse.setHeader("Content-Security-Policy",
            "default-src 'none'; frame-ancestors 'none'; form-action 'none'");
        contextHttpServletResponse.setHeader("Server", "");
        contextHttpServletResponse.setHeader("X-Powered-By", "");
    }

    // ==================== VALIDATION ====================
    private boolean hasRequiredFields(DdeReturnRequest request) {
        return request.getDocId() != null
                && notBlank(request.getUin())
                && notBlank(request.getReltocntrct())
                && notBlank(request.getSignedDocData())
                && notBlank(request.getSignatoryName())
                && notBlank(request.getSignedName())
                && request.getMatchPercentage() != null;
    }

    private boolean notBlank(String v) { return v != null && !v.trim().isEmpty(); }

    private boolean isValidBase64(String v) {
        if (!notBlank(v) || v.length() % 4 != 0) return false;
        try { Base64.getDecoder().decode(v); return true; }
        catch (IllegalArgumentException e) { return false; }
    }

    // ==================== EXCEPTION BUILDERS ====================
    private WebApplicationException buildWebAppException(String status, String code, Response.Status httpStatus) {
        DdeReturnResponse r = new DdeReturnResponse();
        r.setStatus(status);
        r.setStatusCode(code);
        logResponse(r);
        return new WebApplicationException(Response.status(httpStatus).entity(r).build());
    }

    private DdeReturnResponse buildErrorResponse(String status, String code) {
        DdeReturnResponse r = new DdeReturnResponse();
        r.setStatus(status);
        r.setStatusCode(code);
        return r;
    }

    private void logResponse(DdeReturnResponse r) {
        if (_log.isInfoEnabled()) {
            _log.info("Response: txnID=" + r.getTxnID() + ", status=" + r.getStatus() + ", code=" + r.getStatusCode());
        }
    }

    // ==================== INNER CLASSES ====================
    private static class FailedLoginAttempt {
        private int attemptCount = 0;
        private long lockTime = 0;
        public void incrementAttempts() {
            attemptCount++;
            if (attemptCount >= MAX_FAILED_ATTEMPTS) lockTime = System.currentTimeMillis();
        }
        public boolean isLocked() {
            if (lockTime == 0) return false;
            return System.currentTimeMillis() - lockTime < LOCKOUT_DURATION_MS;
        }
        public long getLockTimeRemaining() {
            if (!isLocked()) return 0;
            return LOCKOUT_DURATION_MS - (System.currentTimeMillis() - lockTime);
        }
    }
}