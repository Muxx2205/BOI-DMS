package com.boi.headless.rest.dms.internal.resource.v1_0;

import com.boi.headless.rest.dms.dto.v1_0.ConsentMetadata;
import com.boi.headless.rest.dms.dto.v1_0.ConsentRecordResponse;
import com.boi.headless.rest.dms.dto.v1_0.ConsentRecordsResponse;
import com.boi.headless.rest.dms.dto.v1_0.ConsentResponse;
import com.boi.headless.rest.dms.dto.v1_0.ConsentUploadRequest;
import com.boi.headless.rest.dms.resource.v1_0.ConsentResource;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author BOI DMS Team
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/consent.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ConsentResource.class
)
public class ConsentResourceImpl extends BaseConsentResourceImpl {

	private static final Log LOG = LogFactoryUtil.getLog(ConsentResourceImpl.class);

	private static final String OBJECT_DEFINITION_NAME = "ConsentRecord";
	private static final String FOLDER_NAME = "JanSurakshaConsents";
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	// ==================== API 1: Upload Consent ====================
	@Override
	public ConsentResponse uploadConsent(ConsentUploadRequest request) throws Exception {
		LOG.info("=== Upload Consent START ===");

		try {
			validateRequest(request);

			ObjectDefinition objectDefinition = _objectDefinitionLocalService.getObjectDefinition(
				contextCompany.getCompanyId(), OBJECT_DEFINITION_NAME);

			long objectDefinitionId = objectDefinition.getObjectDefinitionId();

			// Check for existing record
			ObjectEntry existingEntry = _objectEntryLocalService.fetchObjectEntry(
				"CBS_" + request.getMetadata().getBankAccountNo(), objectDefinitionId);

			if (existingEntry != null) {
				ConsentResponse response = new ConsentResponse();
				response.setStatus(ConsentResponse.Status.SUCCESS);
				response.setMessage("Consent record already exists for this account");
				return response;
			}

			byte[] documentBytes = Base64.decode(request.getDocumentBase64());

			if (documentBytes.length > MAX_FILE_SIZE) {
				ConsentResponse response = new ConsentResponse();
				response.setStatus(ConsentResponse.Status.FAILED);
				response.setMessage("File too large. Max 5 MB");
				return response;
			}

			String documentReferenceNo = generateDocumentReferenceNo();
			FileEntry fileEntry = storeDocument(documentBytes, request.getFileName(), request.getFileType());

			Map<String, Serializable> values = convertMetadataToValues(
				request.getMetadata(), documentReferenceNo, fileEntry.getFileEntryId());

			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setCompanyId(contextCompany.getCompanyId());
			serviceContext.setUserId(contextUser.getUserId());
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

			_objectEntryLocalService.addOrUpdateObjectEntry(
				documentReferenceNo, contextUser.getUserId(), 0, objectDefinitionId, values, serviceContext);

			LOG.info("=== Upload Consent END. DRN: " + documentReferenceNo);

			ConsentResponse response = new ConsentResponse();
			response.setStatus(ConsentResponse.Status.SUCCESS);
			response.setDocumentReferenceNo(documentReferenceNo);
			response.setMessage("Consent form stored successfully");
			return response;

		} catch (Exception e) {
			LOG.error("Error uploading consent", e);
			ConsentResponse response = new ConsentResponse();
			response.setStatus(ConsentResponse.Status.FAILED);
			response.setMessage(e.getMessage());
			return response;
		}
	}

	// ==================== API 2: Get Consent by Reference ====================
	@Override
	public ConsentRecordResponse getConsentByReference(String documentReferenceNo) throws Exception {
		LOG.info("=== Get Consent by Reference: " + documentReferenceNo + " ===");

		ObjectDefinition objectDefinition = _objectDefinitionLocalService.getObjectDefinition(
			contextCompany.getCompanyId(), OBJECT_DEFINITION_NAME);

		ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(
			documentReferenceNo, objectDefinition.getObjectDefinitionId());

		if (entry == null) {
			throw new BadRequestException("Consent record not found: " + documentReferenceNo);
		}

		Map<String, Serializable> values = entry.getModelAttributes();

		ConsentRecordResponse response = new ConsentRecordResponse();
		response.setDocumentReferenceNo(documentReferenceNo);
		response.setFileEntryId(GetterUtil.getLong(values.get("fileEntryId")));
		response.setStage(GetterUtil.getInteger(values.get("stage")));
		response.setStatus(getStatusLabel(response.getStage()));
		response.setMetadata(convertToMetadata(values));
		response.setDownloadUrl("/o/dms-consent/v1.0/consent/download/" + documentReferenceNo);

		return response;
	}

	// ==================== API 3: Download Document ====================
	@Override
	public Response downloadDocument(String documentReferenceNo) throws Exception {
		LOG.info("=== Download Document: " + documentReferenceNo + " ===");

		ObjectDefinition objectDefinition = _objectDefinitionLocalService.getObjectDefinition(
			contextCompany.getCompanyId(), OBJECT_DEFINITION_NAME);

		ObjectEntry entry = _objectEntryLocalService.fetchObjectEntry(
			documentReferenceNo, objectDefinition.getObjectDefinitionId());

		if (entry == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		Map<String, Serializable> values = entry.getModelAttributes();
		long fileEntryId = GetterUtil.getLong(values.get("fileEntryId"));

		if (fileEntryId == 0) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		FileEntry fileEntry = _dlAppLocalService.getFileEntry(fileEntryId);

		return Response.ok(fileEntry.getContentStream())
			.header("Content-Disposition", "attachment; filename=\"" + fileEntry.getFileName() + "\"")
			.header("Content-Type", fileEntry.getMimeType())
			.build();
	}

	// ==================== API 4: Get Consent Records ====================
	@Override
	public ConsentRecordsResponse getConsentRecords(
			String search, Filter filter, Pagination pagination, Sort[] sorts)
			throws Exception {

		LOG.info("=== Get Consent Records START ===");

		String branchCode = getBranchCodeFromRequest();
		String status = getStatusFromRequest();

		ObjectDefinition objectDefinition = _objectDefinitionLocalService.getObjectDefinition(
			contextCompany.getCompanyId(), OBJECT_DEFINITION_NAME);

		ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry.getObjectEntryManager(
			objectDefinition.getStorageType());

		String query = buildQuery(branchCode, status);
		LOG.info("Query: " + query);

		DTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(
			false, null, null, contextAcceptLanguage.getPreferredLocale(), contextUriInfo, null);

		// REST DTO ObjectEntry - has getValues()
		Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> objectEntryPage =
			objectEntryManager.getObjectEntries(
				contextCompany.getCompanyId(), objectDefinition, objectDefinition.getScope(),
				null, dtoConverterContext, query, pagination, null, null);

		// ==================== BUILD ITEMS LIST ====================
		// This is where we convert ObjectEntry data to our response DTOs
		List<ConsentRecordResponse> items = new ArrayList<>();
		
		for (com.liferay.object.rest.dto.v1_0.ObjectEntry restEntry : objectEntryPage.getItems()) {
			// REST DTO has getValues() which returns Map<String, Object>
			Map<String, Object> values = restEntry.getValues();
			
			ConsentRecordResponse recordResponse = new ConsentRecordResponse();
			recordResponse.setDocumentReferenceNo((String) values.get("documentReferenceNo"));
			recordResponse.setFileEntryId(GetterUtil.getLong(values.get("fileEntryId")));
			recordResponse.setStage(GetterUtil.getInteger(values.get("stage")));
			recordResponse.setStatus(getStatusLabel(recordResponse.getStage()));
			recordResponse.setMetadata(convertObjectMapToMetadata(values));
			recordResponse.setDownloadUrl("/o/dms-consent/v1.0/consent/download/" +
				recordResponse.getDocumentReferenceNo());
			
			items.add(recordResponse);
		}

		// ==================== SET ITEMS IN RESPONSE ====================
		// The frontend expects "items" array in the JSON response
		// So we MUST set it using setItems()
		ConsentRecordResponse[] itemsArray = items.toArray(new ConsentRecordResponse[0]);
		
		ConsentRecordsResponse response = new ConsentRecordsResponse();
		response.setItems(itemsArray);  // ← This is CRITICAL for frontend
		response.setTotalCount((int) objectEntryPage.getTotalCount());
		response.setPage(pagination.getPage());
		response.setPageSize(pagination.getPageSize());

		return response;
	}

	// ==================== Private Helper Methods ====================

	private void validateRequest(ConsentUploadRequest request) {
		if (Validator.isNull(request.getDocumentBase64())) {
			throw new BadRequestException("documentBase64 is required");
		}
		if (Validator.isNull(request.getFileName())) {
			throw new BadRequestException("fileName is required");
		}
		if (Validator.isNull(request.getFileType())) {
			throw new BadRequestException("fileType is required");
		}

		ConsentMetadata metadata = request.getMetadata();
		if (metadata == null) {
			throw new BadRequestException("metadata is required");
		}
		if (Validator.isNull(metadata.getAccountHolderName())) {
			throw new BadRequestException("accountHolderName is required");
		}
		if (Validator.isNull(metadata.getMobileNumber())) {
			throw new BadRequestException("mobileNumber is required");
		}
		if (Validator.isNull(metadata.getBankAccountNo())) {
			throw new BadRequestException("bankAccountNo is required");
		}
		if (Validator.isNull(metadata.getIfscCode())) {
			throw new BadRequestException("ifscCode is required");
		}
		if (metadata.getInsuranceType() == null) {
			throw new BadRequestException("insuranceType is required");
		}
		if (Validator.isNull(metadata.getPolicyYear())) {
			throw new BadRequestException("policyYear is required");
		}
		if (metadata.getPolicyIssuanceStatus() == null) {
			throw new BadRequestException("policyIssuanceStatus is required");
		}
		if (metadata.getTransactionDate() == null) {
			throw new BadRequestException("transactionDate is required");
		}
		if (metadata.getDocumentPushDateTime() == null) {
			throw new BadRequestException("documentPushDateTime is required");
		}
		if (metadata.getDocumentSentStatus() == null) {
			throw new BadRequestException("documentSentStatus is required");
		}
		if (Validator.isNull(metadata.getBranchCode())) {
			throw new BadRequestException("branchCode is required");
		}
	}

	private FileEntry storeDocument(byte[] documentBytes, String fileName, String fileType)
			throws PortalException {

		long groupId = contextCompany.getGroupId();
		long userId = contextUser.getUserId();

		Folder folder = getOrCreateFolder(groupId, userId);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(contextCompany.getCompanyId());
		serviceContext.setUserId(userId);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(false);

		return _dlAppLocalService.addFileEntry(
			null, userId, groupId, folder.getFolderId(), fileName, fileType,
			documentBytes, null, null, null, serviceContext);
	}

	private Folder getOrCreateFolder(long groupId, long userId) throws PortalException {
		try {
			return _dlAppLocalService.getFolder(groupId, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, FOLDER_NAME);
		} catch (PortalException e) {
			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setCompanyId(contextCompany.getCompanyId());
			serviceContext.setUserId(userId);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(false);

			return _dlAppLocalService.addFolder(
				null, userId, groupId, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				FOLDER_NAME, "Folder for JanSuraksha consent forms", serviceContext);
		}
	}

	private Map<String, Serializable> convertMetadataToValues(ConsentMetadata metadata, 
			String documentReferenceNo, long fileEntryId) {

		Map<String, Serializable> values = new HashMap<>();

		values.put("accountHolderName", metadata.getAccountHolderName());
		values.put("fatherHusbandName", nullToBlank(metadata.getFatherHusbandName()));
		values.put("address", nullToBlank(metadata.getAddress()));
		values.put("city", nullToBlank(metadata.getCity()));
		values.put("district", nullToBlank(metadata.getDistrict()));
		values.put("state", nullToBlank(metadata.getState()));
		values.put("pinCode", nullToBlank(metadata.getPinCode()));
		values.put("mobileNumber", metadata.getMobileNumber());
		values.put("bankAccountNo", metadata.getBankAccountNo());
		values.put("ifscCode", metadata.getIfscCode());
		values.put("customerId", nullToBlank(metadata.getCustomerId()));
		values.put("occupation", nullToBlank(metadata.getOccupation()));
		values.put("kycDocumentName", nullToBlank(metadata.getKycDocumentName()));
		values.put("kycIdNumber", nullToBlank(metadata.getKycIdNumber()));
		values.put("panNumber", nullToBlank(metadata.getPanNumber()));
		values.put("aadhaarNumber", nullToBlank(metadata.getAadhaarNumber()));
		values.put("dateOfBirth", metadata.getDateOfBirth());
		values.put("emailId", nullToBlank(metadata.getEmailId()));
		values.put("nomineeName", nullToBlank(metadata.getNomineeName()));
		values.put("nomineeAddress", nullToBlank(metadata.getNomineeAddress()));
		values.put("nomineeDob", metadata.getNomineeDob());
		values.put("nomineeRelation", nullToBlank(metadata.getNomineeRelation()));
		values.put("guardianName", nullToBlank(metadata.getGuardianName()));
		values.put("guardianAddress", nullToBlank(metadata.getGuardianAddress()));
		values.put("guardianRelation", nullToBlank(metadata.getGuardianRelation()));
		values.put("guardianMobile", nullToBlank(metadata.getGuardianMobile()));
		values.put("guardianEmail", nullToBlank(metadata.getGuardianEmail()));
		values.put("urn", nullToBlank(metadata.getUrn()));
		
		if (metadata.getInsuranceType() != null) {
			values.put("insuranceType", metadata.getInsuranceType().toString());
		}
		values.put("policyYear", metadata.getPolicyYear());
		if (metadata.getPolicyIssuanceStatus() != null) {
			values.put("policyIssuanceStatus", metadata.getPolicyIssuanceStatus().toString());
		}
		values.put("transactionDate", metadata.getTransactionDate());
		values.put("documentPushDateTime", metadata.getDocumentPushDateTime());
		values.put("documentReferenceNo", documentReferenceNo);
		if (metadata.getDocumentSentStatus() != null) {
			values.put("documentSentStatus", metadata.getDocumentSentStatus().toString());
		}
		values.put("branchCode", metadata.getBranchCode());

		values.put("fileEntryId", fileEntryId);
		values.put("stage", 0);
		values.put("delFlag", "N");

		return values;
	}

	private ConsentMetadata convertToMetadata(Map<String, Serializable> values) {
		ConsentMetadata metadata = new ConsentMetadata();

		metadata.setAccountHolderName((String) values.get("accountHolderName"));
		metadata.setFatherHusbandName((String) values.get("fatherHusbandName"));
		metadata.setAddress((String) values.get("address"));
		metadata.setCity((String) values.get("city"));
		metadata.setDistrict((String) values.get("district"));
		metadata.setState((String) values.get("state"));
		metadata.setPinCode((String) values.get("pinCode"));
		metadata.setMobileNumber((String) values.get("mobileNumber"));
		metadata.setBankAccountNo((String) values.get("bankAccountNo"));
		metadata.setIfscCode((String) values.get("ifscCode"));
		metadata.setCustomerId((String) values.get("customerId"));
		metadata.setOccupation((String) values.get("occupation"));
		metadata.setKycDocumentName((String) values.get("kycDocumentName"));
		metadata.setKycIdNumber((String) values.get("kycIdNumber"));
		metadata.setPanNumber((String) values.get("panNumber"));
		metadata.setAadhaarNumber((String) values.get("aadhaarNumber"));

		Date dob = (Date) values.get("dateOfBirth");
		if (dob != null) {
			metadata.setDateOfBirth(dob);
		}

		metadata.setEmailId((String) values.get("emailId"));
		metadata.setNomineeName((String) values.get("nomineeName"));
		metadata.setNomineeAddress((String) values.get("nomineeAddress"));

		Date nomineeDob = (Date) values.get("nomineeDob");
		if (nomineeDob != null) {
			metadata.setNomineeDob(nomineeDob);
		}

		metadata.setNomineeRelation((String) values.get("nomineeRelation"));
		metadata.setGuardianName((String) values.get("guardianName"));
		metadata.setGuardianAddress((String) values.get("guardianAddress"));
		metadata.setGuardianRelation((String) values.get("guardianRelation"));
		metadata.setGuardianMobile((String) values.get("guardianMobile"));
		metadata.setGuardianEmail((String) values.get("guardianEmail"));
		metadata.setUrn((String) values.get("urn"));

		String insuranceTypeStr = (String) values.get("insuranceType");
		if (Validator.isNotNull(insuranceTypeStr)) {
			try {
				metadata.setInsuranceType(ConsentMetadata.InsuranceType.valueOf(insuranceTypeStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid insurance type: " + insuranceTypeStr);
			}
		}

		metadata.setPolicyYear((String) values.get("policyYear"));

		String issuanceStatusStr = (String) values.get("policyIssuanceStatus");
		if (Validator.isNotNull(issuanceStatusStr)) {
			try {
				metadata.setPolicyIssuanceStatus(
					ConsentMetadata.PolicyIssuanceStatus.valueOf(issuanceStatusStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid policy issuance status: " + issuanceStatusStr);
			}
		}

		Date transDate = (Date) values.get("transactionDate");
		if (transDate != null) {
			metadata.setTransactionDate(transDate);
		}

		Date pushDate = (Date) values.get("documentPushDateTime");
		if (pushDate != null) {
			metadata.setDocumentPushDateTime(pushDate);
		}

		metadata.setDocumentReferenceNo((String) values.get("documentReferenceNo"));

		String sentStatusStr = (String) values.get("documentSentStatus");
		if (Validator.isNotNull(sentStatusStr)) {
			try {
				metadata.setDocumentSentStatus(
					ConsentMetadata.DocumentSentStatus.valueOf(sentStatusStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid document sent status: " + sentStatusStr);
			}
		}

		metadata.setBranchCode((String) values.get("branchCode"));

		return metadata;
	}

	private ConsentMetadata convertObjectMapToMetadata(Map<String, Object> values) {
		ConsentMetadata metadata = new ConsentMetadata();

		metadata.setAccountHolderName((String) values.get("accountHolderName"));
		metadata.setFatherHusbandName((String) values.get("fatherHusbandName"));
		metadata.setAddress((String) values.get("address"));
		metadata.setCity((String) values.get("city"));
		metadata.setDistrict((String) values.get("district"));
		metadata.setState((String) values.get("state"));
		metadata.setPinCode((String) values.get("pinCode"));
		metadata.setMobileNumber((String) values.get("mobileNumber"));
		metadata.setBankAccountNo((String) values.get("bankAccountNo"));
		metadata.setIfscCode((String) values.get("ifscCode"));
		metadata.setCustomerId((String) values.get("customerId"));
		metadata.setOccupation((String) values.get("occupation"));
		metadata.setKycDocumentName((String) values.get("kycDocumentName"));
		metadata.setKycIdNumber((String) values.get("kycIdNumber"));
		metadata.setPanNumber((String) values.get("panNumber"));
		metadata.setAadhaarNumber((String) values.get("aadhaarNumber"));

		Object dob = values.get("dateOfBirth");
		if (dob instanceof Date) {
			metadata.setDateOfBirth((Date) dob);
		}

		metadata.setEmailId((String) values.get("emailId"));
		metadata.setNomineeName((String) values.get("nomineeName"));
		metadata.setNomineeAddress((String) values.get("nomineeAddress"));

		Object nomineeDob = values.get("nomineeDob");
		if (nomineeDob instanceof Date) {
			metadata.setNomineeDob((Date) nomineeDob);
		}

		metadata.setNomineeRelation((String) values.get("nomineeRelation"));
		metadata.setGuardianName((String) values.get("guardianName"));
		metadata.setGuardianAddress((String) values.get("guardianAddress"));
		metadata.setGuardianRelation((String) values.get("guardianRelation"));
		metadata.setGuardianMobile((String) values.get("guardianMobile"));
		metadata.setGuardianEmail((String) values.get("guardianEmail"));
		metadata.setUrn((String) values.get("urn"));

		String insuranceTypeStr = (String) values.get("insuranceType");
		if (Validator.isNotNull(insuranceTypeStr)) {
			try {
				metadata.setInsuranceType(ConsentMetadata.InsuranceType.valueOf(insuranceTypeStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid insurance type: " + insuranceTypeStr);
			}
		}

		metadata.setPolicyYear((String) values.get("policyYear"));

		String issuanceStatusStr = (String) values.get("policyIssuanceStatus");
		if (Validator.isNotNull(issuanceStatusStr)) {
			try {
				metadata.setPolicyIssuanceStatus(
					ConsentMetadata.PolicyIssuanceStatus.valueOf(issuanceStatusStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid policy issuance status: " + issuanceStatusStr);
			}
		}

		Object transDate = values.get("transactionDate");
		if (transDate instanceof Date) {
			metadata.setTransactionDate((Date) transDate);
		}

		Object pushDate = values.get("documentPushDateTime");
		if (pushDate instanceof Date) {
			metadata.setDocumentPushDateTime((Date) pushDate);
		}

		metadata.setDocumentReferenceNo((String) values.get("documentReferenceNo"));

		String sentStatusStr = (String) values.get("documentSentStatus");
		if (Validator.isNotNull(sentStatusStr)) {
			try {
				metadata.setDocumentSentStatus(
					ConsentMetadata.DocumentSentStatus.valueOf(sentStatusStr));
			} catch (IllegalArgumentException e) {
				LOG.warn("Invalid document sent status: " + sentStatusStr);
			}
		}

		metadata.setBranchCode((String) values.get("branchCode"));

		return metadata;
	}

	private String buildQuery(String branchCode, String status) {
		StringBuilder query = new StringBuilder();
		query.append("stage ne 4");
		query.append(" and delFlag ne 'Y'");

		if (Validator.isNotNull(branchCode)) {
			query.append(" and branchCode eq '").append(branchCode).append("'");
		}

		if (Validator.isNotNull(status)) {
			Integer stage = mapStatusToStage(status);
			if (stage != null) {
				query.append(" and stage eq ").append(stage);
			}
		}

		return query.toString();
	}

	private String generateDocumentReferenceNo() {
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String random = String.format("%04d", new Random().nextInt(9999));
		return "DMS-" + date + "-" + random;
	}

	private String getStatusLabel(int stage) {
		switch (stage) {
			case 0: return "initiated";
			case 1: return "pending with branch maker";
			case 2: return "pending with branch checker";
			case 3: return "approved";
			case 4: return "deleted";
			default: return "initiated";
		}
	}

	private Integer mapStatusToStage(String status) {
		if (Validator.isNull(status)) return null;
		switch (status.toLowerCase()) {
			case "initiated": return 0;
			case "pending with branch maker": return 1;
			case "pending with branch checker": return 2;
			case "approved": return 3;
			case "deleted": return 4;
			default: return null;
		}
	}

	private String nullToBlank(Object value) {
		return value != null ? value.toString() : StringPool.BLANK;
	}

	private String nullToBlank(String value) {
		return value != null ? value : StringPool.BLANK;
	}

	private String getBranchCodeFromRequest() {
		try {
			return contextUriInfo.getQueryParameters().getFirst("branchCode");
		} catch (Exception e) {
			return null;
		}
	}

	private String getStatusFromRequest() {
		try {
			return contextUriInfo.getQueryParameters().getFirst("status");
		} catch (Exception e) {
			return null;
		}
	}

	// ==================== OSGi References ====================

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;
}