package com.boi.headless.rest.consent.internal.resource.v1_0;

import com.boi.headless.rest.consent.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.consent.dto.v1_0.Base64ConsentUploadRequest;
import com.boi.headless.rest.consent.dto.v1_0.ConsentMetadata;
import com.boi.headless.rest.consent.dto.v1_0.ConsentUploadResponse;
import com.boi.headless.rest.consent.resource.v1_0.JanSurakshaResource;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    properties = "OSGI-INF/liferay/rest/v1_0/consent.properties",
    scope = ServiceScope.PROTOTYPE,
    service = JanSurakshaResource.class
)
public class JanSurakshaResourceImpl extends BaseJanSurakshaResourceImpl {

    private static final Log LOG = LogFactoryUtil.getLog(JanSurakshaResource.class);
    
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    // ==================== API 1: Upload Base64 Document ====================
    @Override
    public ConsentUploadResponse uploadConsentDocument(Base64ConsentUploadRequest request) throws Exception {
        LOG.info("=== Upload Consent Document ===");
        LOG.info("File: " + request.getDocumentFileName());
        LOG.info("Branch: " + request.getMetadata().getBranchName());

        // Validate
        validateRequest(request);

        // Decode and validate document
        byte[] documentBytes = Base64.decode(request.getDocument());
        if (documentBytes.length > MAX_FILE_SIZE) {
            return errorResponse("File too large. Max 5 MB");
        }

        // Extract branch code from branchName (e.g., "0000-Andheri" -> "0000")
        ConsentMetadata metadata = request.getMetadata();
        String branchCode = extractBranchCode(metadata.getBranchName());
        metadata.setBranchCode(branchCode);

        // Create Object Entry with metadata
        Map<String, Serializable> values = convertMetadataToValues(metadata);
        values.put("stage", 0);
        values.put("documentReferenceNo", generateDocumentReferenceNo());
        values.put("delFlag", "N");

        ObjectEntry entry = createObjectEntry(values, request.getDocumentFileName());

        LOG.info("Upload successful. EntryId: " + entry.getObjectEntryId());
        
        return successResponse((String) values.get("documentReferenceNo"), entry.getObjectEntryId());
    }

    // ==================== API 2: Get Records (Frontend) ====================
    @Override
    public BOIHeadlessResponse getConsentRecords(
            String branchCode,
            String insuranceType,
            String policyYear,
            String mobileNumber,
            String bankAccountNo,
            String urn,
            java.util.Date fromDate,
            java.util.Date toDate,
            Pagination pagination) throws Exception {

        LOG.info("=== Get Consent Records ===");
        LOG.info("Branch Code: " + branchCode);
        LOG.info("Insurance Type: " + insuranceType);
        LOG.info("Policy Year: " + policyYear);

        // Build query with filters
        StringBuilder query = new StringBuilder();
        query.append("stage ne 4 and delFlag ne 'Y'");
        
        // Branch hierarchy filter
        if (Validator.isNotNull(branchCode)) {
            query.append(" and branchCode eq '").append(branchCode).append("'");
            LOG.info("Filter by Branch: " + branchCode);
        }
        
        // Additional filters
        addFilter(query, "insuranceType", insuranceType);
        addFilter(query, "policyYear", policyYear);
        addFilter(query, "mobileNumber", mobileNumber);
        addFilter(query, "bankAccountNo", bankAccountNo);
        addFilter(query, "urn", urn);
        
        // Date range filter
        if (fromDate != null && toDate != null) {
            query.append(" and createDate ge ").append(fromDate.toString());
            query.append(" and createDate le ").append(toDate.toString());
        }

        LOG.info("Query: " + query.toString());

        // Create response - FIXED: Convert to Integer
        BOIHeadlessResponse response = new BOIHeadlessResponse();
        response.setResponseId(1);  // Changed from 1L to 1
        response.setResponseStatus("Success");
        
        return response;
    }

    @Override
    public BOIHeadlessResponse getConsentRecord(Integer id) throws Exception {
        LOG.info("=== Get Consent Record: " + id + " ===");

        // Get entry by ID
        ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(id.longValue());
        
        if (entry == null) {
            throw new BadRequestException("Record not found with id: " + id);
        }

        Map<String, Serializable> values = entry.getValues();
        int stage = GetterUtil.getInteger(values.get("stage"));
        values.put("status", getStatusLabel(stage));

        // FIXED: id is already Integer, use directly
        BOIHeadlessResponse response = new BOIHeadlessResponse();
        response.setResponseId(id);  // id is already Integer
        response.setResponseStatus(JSONFactoryUtil.looseSerialize(values));
        
        return response;
    }

    // ==================== Helper Methods ====================

    private void validateRequest(Base64ConsentUploadRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        
        if (Validator.isNull(request.getDocument())) {
            throw new BadRequestException("document is required");
        }
        
        if (Validator.isNull(request.getDocumentFileName())) {
            throw new BadRequestException("documentFileName is required");
        }
        
        if (Validator.isNull(request.getDocumentFileType())) {
            throw new BadRequestException("documentFileType is required");
        }
        
        ConsentMetadata metadata = request.getMetadata();
        if (metadata == null) {
            throw new BadRequestException("metadata is required");
        }
        
        // Required fields validation
        if (Validator.isNull(metadata.getAccountHolderName())) {
            throw new BadRequestException("accountHolderName is required");
        }
        if (Validator.isNull(metadata.getMobileNumber()) || metadata.getMobileNumber().length() != 10) {
            throw new BadRequestException("valid mobileNumber is required (10 digits)");
        }
        if (Validator.isNull(metadata.getBankAccountNo())) {
            throw new BadRequestException("bankAccountNo is required");
        }
        if (Validator.isNull(metadata.getIfscCode())) {
            throw new BadRequestException("ifscCode is required");
        }
        if (Validator.isNull(metadata.getInsuranceType())) {
            throw new BadRequestException("insuranceType is required");
        }
        if (Validator.isNull(metadata.getPolicyYear())) {
            throw new BadRequestException("policyYear is required");
        }
        if (Validator.isNull(metadata.getPolicyIssuanceStatus())) {
            throw new BadRequestException("policyIssuanceStatus is required");
        }
        if (Validator.isNull(metadata.getTransactionDate())) {
            throw new BadRequestException("transactionDate is required");
        }
        if (Validator.isNull(metadata.getDocumentPushDateTime())) {
            throw new BadRequestException("documentPushDateTime is required");
        }
        if (Validator.isNull(metadata.getDocumentSentStatus())) {
            throw new BadRequestException("documentSentStatus is required");
        }
        if (Validator.isNull(metadata.getBranchName()) || !metadata.getBranchName().contains("-")) {
            throw new BadRequestException("branchName is required in format: BranchCode-BranchName");
        }
    }

    private String extractBranchCode(String branchName) {
        if (Validator.isNull(branchName)) return StringPool.BLANK;
        int index = branchName.indexOf("-");
        return index > 0 ? branchName.substring(0, index) : branchName;
    }

    private String generateDocumentReferenceNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(999999));
        return "DRN-" + date + "-" + random;
    }

    private Map<String, Serializable> convertMetadataToValues(ConsentMetadata metadata) {
        Map<String, Serializable> values = new HashMap<>();
        
        // Customer Information
        values.put("accountHolderName", nullSafe(metadata.getAccountHolderName()));
        values.put("fatherHusbandName", nullSafe(metadata.getFatherHusbandName()));
        values.put("address", nullSafe(metadata.getAddress()));
        values.put("city", nullSafe(metadata.getCity()));
        values.put("district", nullSafe(metadata.getDistrict()));
        values.put("state", nullSafe(metadata.getState()));
        values.put("pinCode", nullSafe(metadata.getPinCode()));
        
        // Bank & KYC Details
        values.put("mobileNumber", nullSafe(metadata.getMobileNumber()));
        values.put("bankAccountNo", nullSafe(metadata.getBankAccountNo()));
        values.put("ifscCode", nullSafe(metadata.getIfscCode()));
        values.put("customerId", nullSafe(metadata.getCustomerId()));
        values.put("occupation", nullSafe(metadata.getOccupation()));
        values.put("kycDocumentName", nullSafe(metadata.getKycDocumentName()));
        values.put("kycIdNumber", nullSafe(metadata.getKycIdNumber()));
        values.put("panNumber", nullSafe(metadata.getPanNumber()));
        values.put("aadhaarNumber", nullSafe(metadata.getAadhaarNumber()));
        values.put("dateOfBirth", toStringSafe(metadata.getDateOfBirth()));
        values.put("emailId", nullSafe(metadata.getEmailId()));
        
        // Nominee Details
        values.put("nomineeName", nullSafe(metadata.getNomineeName()));
        values.put("nomineeAddress", nullSafe(metadata.getNomineeAddress()));
        values.put("nomineeDob", toStringSafe(metadata.getNomineeDob()));
        values.put("nomineeRelation", nullSafe(metadata.getNomineeRelation()));
        
        // Guardian Details
        values.put("guardianName", nullSafe(metadata.getGuardianName()));
        values.put("guardianAddress", nullSafe(metadata.getGuardianAddress()));
        values.put("guardianRelation", nullSafe(metadata.getGuardianRelation()));
        values.put("guardianMobile", nullSafe(metadata.getGuardianMobile()));
        values.put("guardianEmail", nullSafe(metadata.getGuardianEmail()));
        
        // Insurance Policy Details
        values.put("urn", nullSafe(metadata.getUrn()));
        values.put("insuranceType", toStringSafe(metadata.getInsuranceType()));
        values.put("policyYear", nullSafe(metadata.getPolicyYear()));
        values.put("policyIssuanceStatus", toStringSafe(metadata.getPolicyIssuanceStatus()));
        values.put("transactionDate", toStringSafe(metadata.getTransactionDate()));
        values.put("documentPushDateTime", toStringSafe(metadata.getDocumentPushDateTime()));
        
        // Document Management
        values.put("documentReferenceNo", nullSafe(metadata.getDocumentReferenceNo()));
        values.put("documentSentStatus", toStringSafe(metadata.getDocumentSentStatus()));
        
        // Branch Hierarchy
        values.put("branchName", nullSafe(metadata.getBranchName()));
        values.put("branchCode", nullSafe(metadata.getBranchCode()));
        
        return values;
    }

    private String nullSafe(String value) {
        return value != null ? value : StringPool.BLANK;
    }

    private String toStringSafe(Object value) {
        if (value == null) {
            return StringPool.BLANK;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toString();
        }
        if (value instanceof java.time.LocalDate) {
            return ((java.time.LocalDate) value).toString();
        }
        if (value instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) value).toString();
        }
        if (value instanceof java.time.ZonedDateTime) {
            return ((java.time.ZonedDateTime) value).toString();
        }
        if (value instanceof java.time.OffsetDateTime) {
            return ((java.time.OffsetDateTime) value).toString();
        }
        return value.toString();
    }

    private String toStringSafe(ConsentMetadata.InsuranceType value) {
        return value != null ? value.toString() : StringPool.BLANK;
    }

    private String toStringSafe(ConsentMetadata.PolicyIssuanceStatus value) {
        return value != null ? value.toString() : StringPool.BLANK;
    }

    private String toStringSafe(ConsentMetadata.DocumentSentStatus value) {
        return value != null ? value.toString() : StringPool.BLANK;
    }

    private ObjectEntry createObjectEntry(Map<String, Serializable> values, String externalRefCode) throws Exception {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setCompanyId(contextCompany.getCompanyId());
        serviceContext.setUserId(contextUser.getUserId());
        serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

        long consentObjectDefinitionId = 0L; // TODO: Get from configuration
        
        return _objectEntryLocalService.addOrUpdateObjectEntry(
            externalRefCode,
            contextUser.getUserId(),
            0,
            consentObjectDefinitionId,
            values,
            serviceContext
        );
    }

    private void addFilter(StringBuilder query, String fieldName, String value) {
        if (Validator.isNotNull(value)) {
            query.append(" and ").append(fieldName).append(" eq '").append(value).append("'");
        }
    }

    private String getStatusLabel(int stage) {
        switch(stage) {
            case 0: return "initiated";
            case 1: return "pending with branch maker";
            case 2: return "pending with branch checker";
            case 3: return "approved";
            case 4: return "deleted";
            default: return "initiated";
        }
    }

    private ConsentUploadResponse successResponse(String drn, long entryId) {
        ConsentUploadResponse response = new ConsentUploadResponse();
        response.setStatus(ConsentUploadResponse.Status.SUCCESS);
        response.setDocumentReferenceNo(drn);
        response.setEntryId(entryId);
        response.setMessage("Consent form stored successfully");
        return response;
    }

    private ConsentUploadResponse errorResponse(String message) {
        ConsentUploadResponse response = new ConsentUploadResponse();
        response.setStatus(ConsentUploadResponse.Status.FAILED);
        response.setMessage(message);
        return response;
    }

    // ==================== Fields ====================
    
    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Reference
    private ObjectFieldLocalService _objectFieldLocalService;
}