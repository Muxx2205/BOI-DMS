package com.boi.headless.rest.jansuraksha.internal.resource.v1_0;

import com.boi.headless.rest.common.application.APIRequestHandler;
import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;
import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.boi.headless.rest.jansuraksha.dto.v1_0.JanSurakshaMetadata;
import com.boi.headless.rest.jansuraksha.dto.v1_0.JanSurakshaResponse;
import com.boi.headless.rest.jansuraksha.dto.v1_0.JanSurakshaUploadRequest;
import com.boi.headless.rest.jansuraksha.dto.v1_0.JanSurakshaUploadResponse;
import com.boi.headless.rest.jansuraksha.internal.resource.v1_0.BaseJanSurakshaResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    configurationPid = "com.boi.headless.rest.common.application.BOIWorkflowConfiguation",
    properties = "OSGI-INF/liferay/rest/v1_0/jansuraksha.properties",
    scope = ServiceScope.PROTOTYPE,
    service = JanSurakshaResourceImpl.class
)
public class JanSurakshaResourceImpl extends BaseJanSurakshaResourceImpl {

    private static final Log LOG = LogFactoryUtil.getLog(JanSurakshaResourceImpl.class);
    
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Pattern NON_NUMERIC_PATTERN = Pattern.compile("[^0-9]");
    private static final Set<String> TOP_LEVEL_ORGS = Set.of("BOI", "BANK OF INDIA", "HEAD OFFICE", "HO");
    
    // ==================== FILE UPLOAD MAP - JanSuraksha Specific ====================
    private static final Map<String, String> FILE_UPLOAD_MAP = Map.of(
        "jansurakshaFormDocumentDetailedFile", "jansurakshaFormDocumentDetailed"
    );
    
    // ==================== Document fields stored in object ====================
    private static final String DOCUMENT_FILE_NAME = "documentFileName";
    private static final String DOCUMENT_FILE_TYPE = "documentFileType";
    private static final String DOCUMENT_BASE64_PREVIEW = "documentBase64Preview";
    
    // ==================== Object Field Names ====================
    private static final String JANSURAKSHA_FORM_DOCUMENT_DETAILED = "jansurakshaFormDocumentDetailed";
    private static final String JANSURAKSHA_FORM_DOCUMENT_FILE_ENTRY_ID = "jansurakshaFormDocumentFileEntryId";
    
    // Hierarchy Cache
    private static final Map<String, HierarchyInfo> HIERARCHY_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = 3600000;

    private static class HierarchyInfo {
        final String branchCode;
        final String branchName;
        final String zone;
        final String nbgCode;
        final long timestamp;

        HierarchyInfo(String branchCode, String branchName, String zone, String nbgCode) {
            this.branchCode = branchCode;
            this.branchName = branchName;
            this.zone = zone;
            this.nbgCode = nbgCode;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_TTL;
        }

        Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>(4);
            if (branchCode != null) map.put("branchCode", branchCode);
            if (branchName != null) map.put("branch", branchName);
            if (zone != null) map.put("zone", zone);
            if (nbgCode != null) map.put("nbgCode", nbgCode);
            return map;
        }
    }

    // ==================== Params Constants ====================
    private static final class Params {
        static final String DOCUMENT_REFERENCE_NO = "documentReferenceNo";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String BRANCH = "branch";
        static final String BRANCH_CODE = "branchCode";
        static final String CUSTOMER_ID = "customerId";
        static final String CUSTOMER_NAME = "customerName";
        static final String NBG_CODE = "nBGCode";
        static final String SOL_ID = "solId";
        static final String TYPE_OF_INSURANCE = "typeOfInsurance";
        static final String TRANS_DATE = "transDate";
        static final String URN = "urn";
        static final String YEAR = "year";
        static final String ZONE = "zone";
        static final String FROM_DATE = "fromDate";
        static final String TO_DATE = "toDate";

        private Params() {}
    }

    // ==================== API 1: Upload Consent Document ====================
    @Override
    public JanSurakshaUploadResponse uploadConsentDocument(JanSurakshaUploadRequest request) throws Exception {
        LOG.info("=== JanSuraksha Upload START ===");
        LOG.info("File: " + request.getDocumentFileName());
        LOG.info("Branch Code: " + request.getMetadata().getBranchCode());

        try {
            validateRequest(request);

            // Decode Base64 Document
            byte[] documentBytes = Base64.decode(request.getDocument());
            if (documentBytes.length > MAX_FILE_SIZE) {
                return createUploadErrorResponse("File too large. Max 5 MB");
            }

            JanSurakshaMetadata metadata = request.getMetadata();

            // ==================== GET HIERARCHY FROM ORGANIZATION ====================
            String branchPrefix = extractFirstFourDigits(metadata.getBranchCode());
            Map<String, String> hierarchy = getHierarchyFromOrganization(branchPrefix);
            
            String zone = hierarchy.getOrDefault("zone", metadata.getZone());
            String nbgCode = hierarchy.getOrDefault("nbgCode", metadata.getNBGCode());
            String branchName = hierarchy.getOrDefault("branch", metadata.getBranch());

            LOG.info("Branch: " + branchPrefix + ", Zone: " + zone + ", NBG: " + nbgCode);

            // Generate Document Reference No
            String documentReferenceNo = generateDocumentReferenceNo();

            // ==================== CONVERT TO OBJECT VALUES ====================
            Map<String, Serializable> values = new HashMap<>();
            
            // 12 Metadata Fields
            values.put(Params.ACCOUNT_NUMBER, nullSafe(metadata.getAccountNumber()));
            values.put(Params.BRANCH, nullSafe(branchName));
            values.put(Params.BRANCH_CODE, nullSafe(branchPrefix));
            values.put(Params.CUSTOMER_ID, nullSafe(metadata.getCustomerId()));
            values.put(Params.CUSTOMER_NAME, nullSafe(metadata.getCustomerName()));
            values.put(Params.NBG_CODE, nullSafe(nbgCode));
            values.put(Params.SOL_ID, nullSafe(metadata.getSolId()));
            values.put(Params.TYPE_OF_INSURANCE, enumToString(metadata.getTypeOfInsurance()));
            values.put(Params.TRANS_DATE, parseDate(metadata.getTransDate()));
            values.put(Params.URN, nullSafe(metadata.getUrn()));
            values.put(Params.YEAR, nullSafe(metadata.getYear()));
            values.put(Params.ZONE, nullSafe(zone));

            // System fields
            values.put(Params.DOCUMENT_REFERENCE_NO, documentReferenceNo);
            values.put(DOCUMENT_FILE_NAME, nullSafe(request.getDocumentFileName()));
            values.put(DOCUMENT_FILE_TYPE, enumToString(request.getDocumentFileType()));
            
            // Store Base64 preview
            String document = request.getDocument();
            if (Validator.isNotNull(document)) {
                int previewLength = Math.min(1000, document.length());
                values.put(DOCUMENT_BASE64_PREVIEW, document.substring(0, previewLength) + "...");
            }

            long objectDefinitionId = boiWorkflowConfiguation.JansurakshaObjectDefinitionId();
            if (objectDefinitionId == 0) {
                return createUploadErrorResponse("System configuration error: Object Definition ID not set");
            }

            // ==================== CREATE OBJECT ENTRY ====================
            ObjectEntry entry = createObjectEntry(values, documentReferenceNo, objectDefinitionId);

            // ==================== SAVE DOCUMENT USING APIRequestHandler ====================
            MultipartBody multipartBody = createMultipartBody(documentBytes, request.getDocumentFileName());
            
            APIRequestHandler handler = getAPIRequestHandler();
            
            handler.updaloadFile(
                values, 
                FILE_UPLOAD_MAP, 
                multipartBody, 
                contextHttpServletRequest,
                objectDefinitionId, 
                StringPool.BLANK, 
                true
            );
            
            // Refresh entry to get updated values
            entry = _objectEntryLocalService.fetchObjectEntry(entry.getObjectEntryId());

            LOG.info("=== JanSuraksha Upload END. EntryId: " + entry.getObjectEntryId());
            
            return createUploadSuccessResponse(documentReferenceNo, entry.getObjectEntryId());

        } catch (Exception e) {
            LOG.error("Error uploading document", e);
            return createUploadErrorResponse("Failed to process upload: " + e.getMessage());
        }
    }

    // ==================== CREATE MULTIPART BODY ====================
    
    private MultipartBody createMultipartBody(byte[] documentBytes, String fileName) {
        try {
            InputStream inputStream = new ByteArrayInputStream(documentBytes);
            String contentType = getContentType(fileName);
            long size = documentBytes.length;
            
            // Use the correct field name from FILE_UPLOAD_MAP
            BinaryFile binaryFile = new BinaryFile(
                contentType,        
                fileName,           
                inputStream,        
                size                
            );
            
            Map<String, BinaryFile> binaryFiles = new HashMap<>();
            binaryFiles.put("jansurakshaFormDocumentDetailedFile", binaryFile);
            
            MultipartBody.ObjectMapperProvider objectMapperProvider = clazz -> new ObjectMapper();
            
            return MultipartBody.of(
                binaryFiles, 
                objectMapperProvider, 
                new HashMap<>()
            );
        } catch (Exception e) {
            LOG.error("Error creating MultipartBody", e);
            return null;
        }
    }
    
    private String getContentType(String fileName) {
        if (Validator.isNull(fileName)) {
            return "application/pdf";
        }
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }
        return "application/pdf";
    }

    // ==================== API 2: Get All Records ====================
    @Override
    public JanSurakshaResponse getJanSurakshaRecords(
            String accountNumber,
            String branchCode,
            String customerName,
            String typeOfInsurance,
            String urn,
            String year,
            String documentFileName,
            String documentFileType,
            java.util.Date fromDate,
            java.util.Date toDate,
            Pagination pagination) throws Exception {

        LOG.info("=== JanSuraksha Get Records START ===");

        try {
            long objectDefinitionId = boiWorkflowConfiguation.JansurakshaObjectDefinitionId();
            if (objectDefinitionId == 0) {
                return createResponseError("System configuration error: Object Definition ID not set");
            }

            ObjectDefinition objectDefinition = _objectDefinitionLocalService.fetchObjectDefinition(objectDefinitionId);
            if (objectDefinition == null) {
                return createResponseError("Object Definition not found");
            }

            List<String> filters = new ArrayList<>();
            
            addFilter(filters, Params.ACCOUNT_NUMBER, accountNumber);
            addFilter(filters, Params.BRANCH_CODE, branchCode);
            addFilter(filters, Params.CUSTOMER_NAME, customerName);
            addFilter(filters, Params.TYPE_OF_INSURANCE, typeOfInsurance);
            addFilter(filters, Params.URN, urn);
            addFilter(filters, Params.YEAR, year);
            addFilter(filters, DOCUMENT_FILE_NAME, documentFileName);
            addFilter(filters, DOCUMENT_FILE_TYPE, documentFileType);
            
            if (fromDate != null && toDate != null) {
                filters.add("createDate ge " + fromDate.toString() + " and createDate le " + toDate.toString());
            }

            String query = String.join(" and ", filters);
            LOG.info("Query: " + query);

            ObjectEntryManager manager = _objectEntryManagerRegistry
                    .getObjectEntryManager(objectDefinition.getStorageType());
            
            Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = 
                manager.getObjectEntries(
                    contextCompany.getCompanyId(),
                    objectDefinition,
                    objectDefinition.getScope(),
                    null,
                    null,
                    query,
                    pagination,
                    null,
                    null
                );

            List<Map<String, Object>> records = new ArrayList<>();
            for (com.liferay.object.rest.dto.v1_0.ObjectEntry entry : page.getItems()) {
                Map<String, Object> properties = entry.getProperties();
                records.add(properties);
            }

            JanSurakshaResponse response = new JanSurakshaResponse();
            response.setResponseId(1);
            response.setResponseStatus(JSONFactoryUtil.looseSerialize(records));

            LOG.info("=== JanSuraksha Get Records END. Total: " + records.size());
            return response;

        } catch (Exception e) {
            LOG.error("Error getting records", e);
            return createResponseError("Failed to get records: " + e.getMessage());
        }
    }

    // ==================== API 3: Get Single Record ====================
    @Override
    public JanSurakshaResponse getJanSurakshaRecord(Integer id) throws Exception {
        LOG.info("=== JanSuraksha Get Record START. ID: " + id + " ===");

        try {
            ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(id.longValue());
            if (entry == null) {
                return createResponseError("Record not found with id: " + id);
            }

            Map<String, Serializable> values = entry.getValues();

            JanSurakshaResponse response = new JanSurakshaResponse();
            response.setResponseId(id);
            response.setResponseStatus(JSONFactoryUtil.looseSerialize(values));

            LOG.info("=== JanSuraksha Get Record END ===");
            return response;

        } catch (Exception e) {
            LOG.error("Error getting record", e);
            return createResponseError("Failed to get record: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private void validateRequest(JanSurakshaUploadRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        
        if (Validator.isNull(request.getDocument())) {
            throw new BadRequestException("document is required");
        }
        if (Validator.isNull(request.getDocumentFileName())) {
            throw new BadRequestException("documentFileName is required");
        }
        if (request.getDocumentFileType() == null) {
            throw new BadRequestException("documentFileType is required");
        }
        
        JanSurakshaMetadata metadata = request.getMetadata();
        if (metadata == null) {
            throw new BadRequestException("metadata is required");
        }
        
        if (Validator.isNull(metadata.getAccountNumber())) {
            throw new BadRequestException("accountNumber is required");
        }
        if (Validator.isNull(metadata.getBranchCode())) {
            throw new BadRequestException("branchCode is required");
        }
        if (Validator.isNull(metadata.getCustomerName())) {
            throw new BadRequestException("customerName is required");
        }
        if (metadata.getTypeOfInsurance() == null) {
            throw new BadRequestException("typeOfInsurance is required");
        }
    }

    // ==================== Hierarchy Methods ====================
    
    private String extractFirstFourDigits(String value) {
        if (Validator.isNull(value)) return null;
        String numericOnly = NON_NUMERIC_PATTERN.matcher(value).replaceAll(StringPool.BLANK);
        if (numericOnly.length() >= 4) {
            return numericOnly.substring(0, 4);
        }
        return numericOnly.isEmpty() ? null : numericOnly;
    }

    private boolean isTopLevelOrganization(String orgName) {
        if (Validator.isNull(orgName)) return false;
        return TOP_LEVEL_ORGS.contains(orgName.toUpperCase());
    }

    private boolean isNBGOrganization(String orgName) {
        if (Validator.isNull(orgName)) return false;
        return orgName.regionMatches(true, 0, "NBG", 0, 3);
    }

    private Map<String, String> getHierarchyFromOrganization(String branchPrefix) {
        if (Validator.isNull(branchPrefix)) return new HashMap<>();

        HierarchyInfo cached = HIERARCHY_CACHE.get(branchPrefix);
        if (cached != null && cached.isValid()) return cached.toMap();

        try {
            List<Organization> organizations = _organizationLocalService.getOrganizations(
                contextCompany.getCompanyId(),
                OrganizationConstants.ANY_PARENT_ORGANIZATION_ID
            );

            Organization branchOrg = null;
            for (Organization org : organizations) {
                String orgName = org.getName();
                if (orgName.startsWith(branchPrefix) || orgName.startsWith(branchPrefix + " - ") ||
                    orgName.startsWith(branchPrefix + "-")) {
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

            if (branchOrg == null) {
                LOG.warn("⚠️ No organization found for branch prefix: " + branchPrefix);
                return new HashMap<>();
            }

            String branchName = branchOrg.getName();
            if (branchName.startsWith(branchPrefix)) {
                branchName = branchName.substring(branchPrefix.length()).trim();
                if (branchName.startsWith("-") || branchName.startsWith("–") || branchName.startsWith("—")) {
                    branchName = branchName.substring(1).trim();
                }
            }

            String zone = null, nbgCode = null;
            long parentId = branchOrg.getParentOrganizationId();
            boolean foundZone = false, foundNbg = false;

            while (parentId > 0) {
                try {
                    Organization parentOrg = _organizationLocalService.getOrganization(parentId);
                    String parentName = parentOrg.getName();

                    if (!foundNbg && isNBGOrganization(parentName)) {
                        nbgCode = parentName;
                        foundNbg = true;
                    } else if (!foundZone && !isTopLevelOrganization(parentName) && !isNBGOrganization(parentName)) {
                        zone = parentName;
                        foundZone = true;
                    }

                    if (foundZone && foundNbg) break;
                    if (isTopLevelOrganization(parentName)) break;
                    parentId = parentOrg.getParentOrganizationId();
                } catch (Exception e) {
                    break;
                }
            }

            HierarchyInfo info = new HierarchyInfo(branchPrefix, branchName, zone, nbgCode);
            HIERARCHY_CACHE.put(branchPrefix, info);
            return info.toMap();

        } catch (Exception e) {
            LOG.error("Error fetching hierarchy for: " + branchPrefix, e);
            return new HashMap<>();
        }
    }

    private String generateDocumentReferenceNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(999999));
        return "DRN-" + date + "-" + random;
    }

    // ==================== Conversion Methods ====================

    private Date parseDate(Date date) {
        return date;
    }

    private Date parseDate(String dateStr) {
        if (Validator.isNull(dateStr)) return null;
        try {
            return Date.from(LocalDate.parse(dateStr).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
        } catch (Exception e) {
            LOG.warn("Failed to parse date: " + dateStr, e);
            return null;
        }
    }

    private String enumToString(JanSurakshaMetadata.TypeOfInsurance type) {
        if (type == null) return null;
        return type.toString();
    }

    private String enumToString(JanSurakshaUploadRequest.DocumentFileType type) {
        if (type == null) return null;
        return type.toString();
    }

    private String nullSafe(String value) {
        return value != null ? value : StringPool.BLANK;
    }

    private void addFilter(List<String> filters, String fieldName, String value) {
        if (Validator.isNotNull(value)) {
            filters.add(fieldName + " eq '" + value + "'");
        }
    }

    // ==================== Object Entry Methods ====================

    private ObjectEntry createObjectEntry(Map<String, Serializable> values, String externalRefCode, long objectDefinitionId) throws Exception {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setCompanyId(contextCompany.getCompanyId());
        serviceContext.setUserId(contextUser.getUserId());
        serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

        return _objectEntryLocalService.addOrUpdateObjectEntry(
            externalRefCode,
            contextUser.getUserId(),
            0,
            objectDefinitionId,
            values,
            serviceContext
        );
    }

    // ==================== APIRequestHandler - Same as Consent ====================
    
    private APIRequestHandler getAPIRequestHandler() {
        return new APIRequestHandler(
            contextUser, 
            contextHttpServletRequest, 
            contextAcceptLanguage,
            _objectFieldLocalService, 
            _dlAppService, 
            contextUriInfo, 
            _dtoConverterRegistry,
            _objectEntryManagerRegistry, 
            _objectDefinitionLocalService, 
            _objectEntryLocalService,
            null,
            boiWorkflowConfiguation, 
            BOIDMSConstants.JANSURAKSHA,
            defaultFolderId
        );
    }

    // ==================== Response Creation Methods ====================

    private JanSurakshaUploadResponse createUploadSuccessResponse(String drn, long entryId) {
        JanSurakshaUploadResponse response = new JanSurakshaUploadResponse();
        response.setStatus(JanSurakshaUploadResponse.Status.SUCCESS);
        response.setDocumentReferenceNo(drn);
        response.setEntryId(entryId);
        response.setMessage("Consent form stored successfully");
        return response;
    }

    private JanSurakshaUploadResponse createUploadErrorResponse(String message) {
        JanSurakshaUploadResponse response = new JanSurakshaUploadResponse();
        response.setStatus(JanSurakshaUploadResponse.Status.FAILED);
        response.setMessage(message);
        return response;
    }

    private JanSurakshaResponse createResponseError(String message) {
        JanSurakshaResponse response = new JanSurakshaResponse();
        response.setResponseId(0);
        response.setResponseStatus("Error: " + message);
        return response;
    }

    // ==================== Activation ====================
    
    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        boiWorkflowConfiguation = ConfigurableUtil.createConfigurable(
            BOIWorkflowConfiguation.class, properties);
        
        defaultFolderId = boiWorkflowConfiguation.HODefualtFolderId();
        
        LOG.info("=== JanSuraksha Configuration Activated ===");
        LOG.info("JanSuraksha Object Definition ID: " + boiWorkflowConfiguation.JansurakshaObjectDefinitionId());
        LOG.info("Default Folder ID: " + defaultFolderId);
    }

    // ==================== Fields ====================
    
    private long defaultFolderId = 0L;
    private BOIWorkflowConfiguation boiWorkflowConfiguation;

    @Reference
    private ObjectDefinitionLocalService _objectDefinitionLocalService;

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

    @Reference
    private DTOConverterRegistry _dtoConverterRegistry;

    @Reference
    private DLAppService _dlAppService;

    @Reference
    private ObjectFieldLocalService _objectFieldLocalService;

    @Reference
    private OrganizationLocalService _organizationLocalService;
}