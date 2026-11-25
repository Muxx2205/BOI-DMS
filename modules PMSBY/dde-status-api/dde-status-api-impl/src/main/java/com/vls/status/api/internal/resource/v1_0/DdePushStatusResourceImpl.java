package com.vls.status.api.internal.resource.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.Validator;
import com.vls.status.api.dto.v1_0.DdePushStatusRequest;
import com.vls.status.api.dto.v1_0.DdePushStatusResponse;
import com.vls.status.api.dto.v1_0.LoanDocDataRequest;
import com.vls.status.api.resource.v1_0.DdePushStatusResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * NESL DDE Push Status API Implementation
 * Following Consent/PMSBY pattern
 * 
 * @author asveld12243145vel
 */
@Component(
    properties = "OSGI-INF/liferay/rest/v1_0/dde-push-status.properties",
    scope = ServiceScope.PROTOTYPE,
    service = DdePushStatusResource.class
)
public class DdePushStatusResourceImpl extends BaseDdePushStatusResourceImpl {

    private static final long DDE_PUSH_OBJECT_DEF_ID = 373125; // DdePush Object
    private static final long DDE_RETURN_OBJECT_DEF_ID = 373653; // DdeReturnDocument Object
    
    private static final String FOLDER_BOI_DMS_HO = "BOI DMS HO";
    private static final String FOLDER_NESL = "Nesl";
    private static final String PFX_FILE_PATH = "F:\\LockerUat.pfx";
    private static final String PFX_PASSWORD = "12345";

    private static final Log _log = LogFactoryUtil.getLog(DdePushStatusResourceImpl.class);

    @Reference
    private ObjectEntryLocalService _objectEntryLocalService;

    @Context
    private HttpServletRequest _request;

    // ==================== Main API Method ====================

    @Override
    public DdePushStatusResponse postDdePushStatus(DdePushStatusRequest request) throws Exception {
        _log.info("=== DDE Push Status START ===");
        _log.info("TransId: " + (request != null ? request.getTransId() : "null"));

        ServiceContext serviceContext = ServiceContextFactory.getInstance(
                ObjectEntry.class.getName(), _request);
        long scopeGroupId = 34299; // BOI-DMS group ID

        // Validation
        if (request == null || !isValidRequest(request)) {
            _log.warn("Invalid request body");
            return buildErrorResponse(request, "Invalid request body", "0");
        }

        try {
            // Step 1: Check if document exists in DMS
            FileEntry existingFileEntry = findFileInDMS(request.getTransId(), scopeGroupId);
            
            if (existingFileEntry != null) {
                _log.info("Document found in DMS for transId=" + request.getTransId());
                DdePushStatusResponse response = buildSuccessResponse(
                    request, 
                    "Successfully Pushed SDV DOC to DMS",
                    existingFileEntry.getCreateDate()
                );
                persistMetadata(request, response, serviceContext, null);
                return response;
            }

            // Step 2: Document not found - Call LoanDocData API
            _log.info("Document not found. Calling Loan Doc Data API for transId=" + request.getTransId());
            LoanDocDataResponse apiResponse = callLoanDocDataAPI(request);

            if (apiResponse == null || !isValidApiResponse(apiResponse)) {
                _log.error("Loan Doc Data API failed for transId=" + request.getTransId());
                return buildErrorResponse(
                    request, 
                    apiResponse != null ? apiResponse.getStatusMessage() : "Failed Pushing SDV DOC to DMS",
                    apiResponse != null ? apiResponse.getStatusCode() : "0"
                );
            }

            // Step 3: Process decryption (2-layer)
            byte[] pdfBytes = decryptAndExtractPDF(apiResponse);
            
            // Step 4: Store in DMS
            FileEntry storedFile = storeFileInDMS(
                request.getTransId(), 
                pdfBytes, 
                scopeGroupId, 
                serviceContext
            );

            // Step 5: Build success response
            DdePushStatusResponse response = buildSuccessResponse(
                request,
                "Successfully Pushed SDV DOC to DMS via LoanDocData API",
                new Date()
            );
            
            // Step 6: Persist metadata (following consent pattern)
            persistMetadata(request, response, serviceContext, apiResponse);

            _log.info("=== DDE Push Status END. Success for transId=" + request.getTransId());
            return response;

        } catch (Exception e) {
            _log.error("Error in Push Status for transId=" + request.getTransId(), e);
            return buildErrorResponse(request, "Failed Pushing SDV DOC to DMS", "0");
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Check if document exists in DMS (Following Consent pattern)
     */
    private FileEntry findFileInDMS(String transId, long scopeGroupId) {
        try {
            Folder parentFolder = DLAppServiceUtil.getFolder(
                scopeGroupId,
                DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
                FOLDER_BOI_DMS_HO
            );
            Folder childFolder = DLAppLocalServiceUtil.getFolder(
                scopeGroupId,
                parentFolder.getFolderId(),
                FOLDER_NESL
            );
            return DLAppLocalServiceUtil.getFileEntry(
                scopeGroupId,
                childFolder.getFolderId(),
                transId
            );
        } catch (Exception e) {
            _log.debug("File not found in DMS for transId=" + transId);
            return null;
        }
    }

    /**
     * Store file in DMS (Following Consent pattern with folder creation)
     */
    private FileEntry storeFileInDMS(
            String transId,
            byte[] pdfBytes,
            long scopeGroupId,
            ServiceContext serviceContext) throws Exception {

        // Get or create BOI DMS HO folder
        DLFolder boiFolder = DLFolderLocalServiceUtil.fetchFolder(
            scopeGroupId,
            DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
            FOLDER_BOI_DMS_HO
        );
        
        if (boiFolder == null) {
            boiFolder = DLFolderLocalServiceUtil.addFolder(
                UUID.randomUUID().toString(),
                contextUser.getUserId(),
                scopeGroupId,
                scopeGroupId,
                false,
                DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
                FOLDER_BOI_DMS_HO,
                "Auto-created folder for NESL DMS",
                false,
                serviceContext
            );
            _log.info("Created BOI DMS HO folder");
        }

        // Get or create Nesl folder
        DLFolder neslFolder = DLFolderLocalServiceUtil.fetchFolder(
            scopeGroupId,
            boiFolder.getFolderId(),
            FOLDER_NESL
        );
        
        if (neslFolder == null) {
            neslFolder = DLFolderLocalServiceUtil.addFolder(
                UUID.randomUUID().toString(),
                contextUser.getUserId(),
                scopeGroupId,
                scopeGroupId,
                false,
                boiFolder.getFolderId(),
                FOLDER_NESL,
                "Auto-created Nesl folder for DDE documents",
                false,
                serviceContext
            );
            _log.info("Created Nesl folder under BOI DMS HO");
        }

        // Store file
        String fileName = transId + ".pdf";
        try (InputStream is = new ByteArrayInputStream(pdfBytes)) {
            DLFileEntry fileEntry = DLFileEntryLocalServiceUtil.addFileEntry(
                null,
                contextUser.getUserId(),
                scopeGroupId,
                scopeGroupId,
                neslFolder.getFolderId(),
                fileName,
                "application/pdf",
                transId,
                transId,
                "Uploaded via DDE Push Status fallback",
                "",
                0,
                new HashMap<>(),
                null,
                is,
                pdfBytes.length,
                new Date(),
                null,
                null,
                serviceContext
            );
            _log.info("Stored file entry ID=" + fileEntry.getFileEntryId() +
                      " for transId=" + transId);
            return DLAppLocalServiceUtil.getFileEntry(fileEntry.getFileEntryId());
        }
    }

    /**
     * 2-layer decryption (Following security standards)
     */
    private byte[] decryptAndExtractPDF(LoanDocDataResponse apiResponse) throws Exception {
        // Layer 1: Decrypt session key using RSA
        PrivateKey privateKey = LoanDocDecryptor.loadPrivateKey(PFX_FILE_PATH, PFX_PASSWORD);
        byte[] aesKeyBytes = LoanDocDecryptor.decryptSessionKey(
            apiResponse.getSessionId(), 
            privateKey
        );

        // Layer 2: Decrypt cipher text using AES
        byte[] decryptedJsonBytes = LoanDocDecryptor.decryptCipherText(
            apiResponse.getCipherText(), 
            aesKeyBytes
        );
        String decryptedJson = new String(decryptedJsonBytes, StandardCharsets.UTF_8);
        
        _log.debug("Decrypted JSON length=" + decryptedJson.length());

        // Parse response
        ObjectMapper mapper = new ObjectMapper();
        LoanDocDataResponse fullResponse = mapper.readValue(
            decryptedJson, 
            LoanDocDataResponse.class
        );

        // Extract Base64 PDF and decode
        String docDataBase64 = fullResponse.getDocStatus().get(0).getDocData();
        return Base64.getDecoder().decode(docDataBase64);
    }

    /**
     * Persist metadata following Consent/PMSBY pattern
     */
    private void persistMetadata(
            DdePushStatusRequest request,
            DdePushStatusResponse response,
            ServiceContext serviceContext,
            LoanDocDataResponse apiResponse) throws Exception {

        Map<String, Serializable> values = new HashMap<>();

        // Core fields
        values.put("transId", request.getTransId());
        values.put("loanno", request.getLoanno());
        values.put("snctnno", request.getSnctnno());
        values.put("digisign", request.getDigisign());
        values.put("statusCode", Integer.parseInt(response.getStatusCode()));
        values.put("statusMsg", response.getStatusMsg());

        // Parse date
        try {
            Date docDate = new SimpleDateFormat("dd-MM-yyyy").parse(response.getDocDate());
            values.put("docDate", docDate);
        } catch (Exception e) {
            values.put("docDate", new Date());
        }

        // Additional fields from API response (like consent pattern)
        if (apiResponse != null) {
            if (apiResponse.getEstampCertificate() != null) {
                values.put("estampCertificate", 
                    String.join(",", apiResponse.getEstampCertificate()));
            }

            if (apiResponse.getDocStatus() != null && !apiResponse.getDocStatus().isEmpty()) {
                LoanDocDataResponse.DocStatus docStatus = apiResponse.getDocStatus().get(0);
                values.put("signtryName", docStatus.getSigntryName());
                values.put("pan", docStatus.getPan());
                values.put("rltnshp", docStatus.getRltnshp());
                values.put("esignStatus", docStatus.getEsignStatus());
                values.put("esignDate", docStatus.getEsignDate());
                values.put("estampStatus", docStatus.getEstampStatus());
                values.put("estampDate", docStatus.getEstampDate());
                values.put("seqNo", docStatus.getSeqNo());
            }
        }

        _objectEntryLocalService.addObjectEntry(
            contextUser.getUserId(),
            0,
            DDE_PUSH_OBJECT_DEF_ID,
            values,
            serviceContext
        );

        _log.info("Persisted metadata for transId=" + request.getTransId());
    }

    /**
     * Call external LoanDocData API (Following consent pattern)
     */
    private LoanDocDataResponse callLoanDocDataAPI(DdePushStatusRequest request) throws Exception {
        LoanDocDataRequest apiRequest = new LoanDocDataRequest();
        apiRequest.setTransId(request.getTransId());
        apiRequest.setLoanno(request.getLoanno());
        apiRequest.setDigisign(request.getDigisign());

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(apiRequest);

        // Build HTTP request (following consent pattern)
        com.liferay.change.tracking.rest.client.http.HttpInvoker httpInvoker = 
            com.liferay.change.tracking.rest.client.http.HttpInvoker.newHttpInvoker();
        httpInvoker.body(jsonBody, "application/json");
        httpInvoker.httpMethod(com.liferay.change.tracking.rest.client.http.HttpInvoker.HttpMethod.POST);
        httpInvoker.path("https://uaapibkg.bankofindia.co.in/npcorporate/external/v1/loandocument/loandocdata/request");

        // Headers (like CBS API in consent)
        httpInvoker.header("api-key", "GhrfdAOetTkEZKz8PxIH");
        httpInvoker.header("clientID", "BOILocker");
        httpInvoker.header("X-IBM-Client-Id", "32d691444b43a902dd418eec1f34d758");
        httpInvoker.header("X-IBM-Client-Secret", "a509e0a2f494dafb39aca558658ba01f");
        httpInvoker.header("Content-Type", "application/json");
        httpInvoker.header("Authorization", "Basic Qk9JTG9ja2VyOmJtNEVCa0Vy");
        httpInvoker.header("Username", "nesluat");
        httpInvoker.header("Password", "test123");

        _log.info("Calling Loan Doc Data API for transId=" + request.getTransId());
        
        com.liferay.change.tracking.rest.client.http.HttpInvoker.HttpResponse httpResponse = 
            httpInvoker.invoke();
        
        String content = httpResponse.getContent();
        _log.debug("Loan Doc Data API response: " + content);

        LoanDocDataResponse response = mapper.readValue(content, LoanDocDataResponse.class);
        
        if (response != null && Validator.isNotNull(response.getCipherText())) {
            _log.info("Loan Doc Data API SUCCESS for transId=" + request.getTransId());
        } else {
            _log.warn("Loan Doc Data API FAILURE for transId=" + request.getTransId() +
                      " - statusCode=" + (response != null ? response.getStatusCode() : "null"));
        }

        return response;
    }

    // ==================== Validation Methods ====================

    private boolean isValidRequest(DdePushStatusRequest request) {
        return notBlank(request.getTransId()) && notBlank(request.getDigisign());
    }

    private boolean isValidApiResponse(LoanDocDataResponse response) {
        return response != null && notBlank(response.getCipherText());
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ==================== Response Builders ====================

    private DdePushStatusResponse buildSuccessResponse(
            DdePushStatusRequest request,
            String message,
            Date docDate) {
        DdePushStatusResponse response = new DdePushStatusResponse();
        response.setTransId(request.getTransId());
        response.setLoanno(request.getLoanno());
        response.setSnctnno(request.getSnctnno());
        response.setStatusCode("1");
        response.setStatusMsg(message);
        response.setDocDate(new SimpleDateFormat("dd-MM-yyyy").format(docDate));
        return response;
    }

    private DdePushStatusResponse buildErrorResponse(
            DdePushStatusRequest request,
            String statusMsg,
            String statusCode) {
        DdePushStatusResponse errorResponse = new DdePushStatusResponse();
        if (request != null) {
            errorResponse.setTransId(request.getTransId());
            errorResponse.setLoanno(request.getLoanno());
            errorResponse.setSnctnno(request.getSnctnno());
        }
        errorResponse.setStatusCode(statusCode);
        errorResponse.setStatusMsg(statusMsg);
        _log.error("Error response: " + statusMsg + " (code=" + statusCode + ")");
        return errorResponse;
    }

    // ==================== Inner Class: LoanDocDataResponse ====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanDocDataResponse {
        private String cipherText;
        private String sessionId;
        private String digisign;
        private String statusCode;
        private String statusMessage;
        private List<DocStatus> docStatus;
        private List<String> estampCertificate;

        // Getters and Setters
        public String getCipherText() { return cipherText; }
        public void setCipherText(String cipherText) { this.cipherText = cipherText; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getDigisign() { return digisign; }
        public void setDigisign(String digisign) { this.digisign = digisign; }

        public String getStatusCode() { return statusCode; }
        public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

        public String getStatusMessage() { return statusMessage; }
        public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

        public List<DocStatus> getDocStatus() { return docStatus; }
        public void setDocStatus(List<DocStatus> docStatus) { this.docStatus = docStatus; }

        public List<String> getEstampCertificate() { return estampCertificate; }
        public void setEstampCertificate(List<String> estampCertificate) { 
            this.estampCertificate = estampCertificate; 
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DocStatus {
            private String signtryName;
            private String pan;
            private String rltnshp;
            private String esignStatus;
            private String esignDate;
            private String estampStatus;
            private String estampDate;
            private String seqNo;
            private String docData;

            // Getters and Setters
            public String getSigntryName() { return signtryName; }
            public void setSigntryName(String signtryName) { this.signtryName = signtryName; }

            public String getPan() { return pan; }
            public void setPan(String pan) { this.pan = pan; }

            public String getRltnshp() { return rltnshp; }
            public void setRltnshp(String rltnshp) { this.rltnshp = rltnshp; }

            public String getEsignStatus() { return esignStatus; }
            public void setEsignStatus(String esignStatus) { this.esignStatus = esignStatus; }

            public String getEsignDate() { return esignDate; }
            public void setEsignDate(String esignDate) { this.esignDate = esignDate; }

            public String getEstampStatus() { return estampStatus; }
            public void setEstampStatus(String estampStatus) { this.estampStatus = estampStatus; }

            public String getEstampDate() { return estampDate; }
            public void setEstampDate(String estampDate) { this.estampDate = estampDate; }

            public String getSeqNo() { return seqNo; }
            public void setSeqNo(String seqNo) { this.seqNo = seqNo; }

            public String getDocData() { return docData; }
            public void setDocData(String docData) { this.docData = docData; }
        }
    }
}