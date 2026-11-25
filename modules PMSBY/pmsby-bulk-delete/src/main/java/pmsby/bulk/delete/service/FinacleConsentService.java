package pmsby.bulk.delete.service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Service for sending consent data to Finacle API
 */
@Component(immediate = true, service = FinacleConsentService.class)
public class FinacleConsentService {

    private static final Log _log = LogFactoryUtil.getLog(FinacleConsentService.class);

    // UAT Endpoint (as provided)
    private static final String UAT_ENDPOINT = "https://dcfinxuw01.boicorp.net:32061/FISERVLET/fihttp";
    
    // Configuration - can be moved to system settings
    private static final String BANK_ID = "01";
    private static final String CHANNEL_ID = "DMS";
    private static final String SERVICE_REQUEST_ID = "executeFinacleScript";
    private static final String SERVICE_VERSION = "10.2";
    private static final String REQUEST_ID = "FI_Inbound.scr";
    private static final String IDENTIFIER = "JBYSBYCONSENT";

    /**
     * Send consent to Finacle API
     * 
     * @param urn URN of the record
     * @param customerId Customer ID
     * @param debitAccount Debit account number
     * @param bimaType Bima type (JBY/SBY)
     * @param consentTaken Consent flag (Y/N)
     * @return FinacleResponse object with status and message
     */
    public FinacleResponse sendConsent(String urn, String customerId, String debitAccount, 
                                       String bimaType, String consentTaken) {
        return sendConsent(urn, customerId, debitAccount, bimaType, consentTaken, UAT_ENDPOINT);
    }

    /**
     * Send consent to Finacle API with custom endpoint
     */
    public FinacleResponse sendConsent(String urn, String customerId, String debitAccount,
                                       String bimaType, String consentTaken, String endpoint) {
        
        FinacleResponse response = new FinacleResponse();
        String requestUUID = "Req_" + new SimpleDateFormat("ddMMyyyyHHmmssSSS").format(new Date());
        String consentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        
        try {
            // Build XML request
            String xmlRequest = buildXMLRequest(requestUUID, customerId, debitAccount, 
                                                urn, bimaType, consentTaken, consentDate);
            
            _log.info("Sending Finacle consent request for URN: " + urn);
            _log.debug("Request XML: " + xmlRequest);
            
            // Send HTTP request
            String xmlResponse = sendHttpRequest(endpoint, xmlRequest);
            _log.debug("Response XML: " + xmlResponse);
            
            // Parse response
            parseResponse(xmlResponse, response);
            
            response.setSuccess("SUCCESS".equalsIgnoreCase(response.getStatus()));
            response.setRequestId(requestUUID);
            
            _log.info("Finacle response for URN " + urn + " - Status: " + response.getStatus() + 
                      ", Message: " + response.getMessage());
            
        } catch (Exception e) {
            _log.error("Error sending consent to Finacle for URN: " + urn, e);
            response.setSuccess(false);
            response.setStatus("ERROR");
            response.setMessage("Exception: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Build the complete SOAP/XML request for Finacle API
     */
    private String buildXMLRequest(String requestUUID, String customerId, String debitAccount,
                                   String urn, String bimaType, String consentTaken, String consentDate) 
            throws Exception {
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        
        // Root FIXML element
        Element fixmlRoot = doc.createElementNS("http://www.finacle.com/fixml", "FIXML");
        fixmlRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        fixmlRoot.setAttribute("xsi:schemaLocation", 
            "http://www.finacle.com/fixml executeFinacleScript.xsd");
        doc.appendChild(fixmlRoot);
        
        // Header
        Element header = doc.createElement("Header");
        fixmlRoot.appendChild(header);
        
        // RequestHeader
        Element requestHeader = doc.createElement("RequestHeader");
        header.appendChild(requestHeader);
        
        // MessageKey
        Element messageKey = doc.createElement("MessageKey");
        requestHeader.appendChild(messageKey);
        
        addElement(doc, messageKey, "RequestUUID", requestUUID);
        addElement(doc, messageKey, "ServiceRequestId", SERVICE_REQUEST_ID);
        addElement(doc, messageKey, "ServiceRequestVersion", SERVICE_VERSION);
        addElement(doc, messageKey, "ChannelId", CHANNEL_ID);
        addElement(doc, messageKey, "LanguageId", "");
        
        // RequestMessageInfo
        Element requestMessageInfo = doc.createElement("RequestMessageInfo");
        requestHeader.appendChild(requestMessageInfo);
        
        addElement(doc, requestMessageInfo, "BankId", BANK_ID);
        addElement(doc, requestMessageInfo, "TimeZone", "");
        addElement(doc, requestMessageInfo, "EntityId", "");
        addElement(doc, requestMessageInfo, "EntityType", "");
        addElement(doc, requestMessageInfo, "ArmCorrelationId", "");
        addElement(doc, requestMessageInfo, "MessageDateTime", 
                   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()));
        
        // Security
        Element security = doc.createElement("Security");
        requestHeader.appendChild(security);
        
        Element token = doc.createElement("Token");
        security.appendChild(token);
        
        Element passwordToken = doc.createElement("PasswordToken");
        token.appendChild(passwordToken);
        
        addElement(doc, passwordToken, "UserId", "");
        addElement(doc, passwordToken, "Password", "");
        
        addElement(doc, token, "FICertToken", "");
        addElement(doc, token, "RealUserLoginSessionId", "");
        addElement(doc, token, "RealUser", "");
        addElement(doc, token, "RealUserPwd", "");
        addElement(doc, token, "SSOTransferToken", "");
        
        // Body
        Element body = doc.createElement("Body");
        fixmlRoot.appendChild(body);
        
        // executeFinacleScriptRequest
        Element execRequest = doc.createElement("executeFinacleScriptRequest");
        body.appendChild(execRequest);
        
        // ExecuteFinacleScriptInputVO
        Element inputVO = doc.createElement("ExecuteFinacleScriptInputVO");
        execRequest.appendChild(inputVO);
        
        addElement(doc, inputVO, "requestId", REQUEST_ID);
        
        // executeFinacleScript_CustomData
        Element customData = doc.createElement("executeFinacleScript_CustomData");
        execRequest.appendChild(customData);
        
        addElement(doc, customData, "Identifier", IDENTIFIER);
        addElement(doc, customData, "customerId", customerId);
        addElement(doc, customData, "debitAccount", debitAccount);
        addElement(doc, customData, "urn", urn);
        addElement(doc, customData, "bimaType", bimaType);
        addElement(doc, customData, "consentTaken", consentTaken);
        addElement(doc, customData, "consentTime", consentDate);
        addElement(doc, customData, "autoRenewalConsent", consentTaken);
        addElement(doc, customData, "autoRenewalConsentTime", consentDate);
        addElement(doc, customData, "channelId", CHANNEL_ID);
        
        // Convert Document to String
        return documentToString(doc);
    }
    
    private void addElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        if (textContent != null) {
            element.setTextContent(textContent);
        } else {
            element.setTextContent("");
        }
        parent.appendChild(element);
    }
    
    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\r\n?", "\n");
    }
    
    /**
     * Send HTTP request to Finacle endpoint
     */
    private String sendHttpRequest(String endpoint, String xmlRequest) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/xml;charset=UTF-8");
            httpPost.setHeader("Accept", "application/xml");
            httpPost.setEntity(new StringEntity(xmlRequest, "UTF-8"));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                
                if (statusCode != 200) {
                    _log.error("HTTP Error " + statusCode + " for endpoint: " + endpoint);
                    throw new Exception("HTTP Error: " + statusCode + " - " + responseBody);
                }
                
                return responseBody;
            }
        }
    }
    
    /**
     * Parse Finacle response XML
     */
    private void parseResponse(String xmlResponse, FinacleResponse response) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        // Get GETCONSENT element
        NodeList consentNodes = doc.getElementsByTagName("GETCONSENT");
        if (consentNodes.getLength() > 0) {
            Element consent = (Element) consentNodes.item(0);
            
            String status = getElementValue(consent, "Status");
            String errorMessage = getElementValue(consent, "errorMessage");
            String urn = getElementValue(consent, "urn");
            String customerId = getElementValue(consent, "customerId");
            
            response.setStatus(status);
            response.setMessage(errorMessage);
            response.setUrn(urn);
            response.setCustomerId(customerId);
        }
        
        // Also check HostTransaction status
        NodeList hostTransactionNodes = doc.getElementsByTagName("HostTransaction");
        if (hostTransactionNodes.getLength() > 0) {
            Element hostTransaction = (Element) hostTransactionNodes.item(0);
            String hostStatus = getElementValue(hostTransaction, "Status");
            if (response.getStatus() == null || response.getStatus().isEmpty()) {
                response.setStatus(hostStatus);
            }
        }
    }
    
    private String getElementValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }
    
    /**
     * Response wrapper class
     */
    public static class FinacleResponse {
        private boolean success;
        private String status;
        private String message;
        private String urn;
        private String customerId;
        private String requestId;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUrn() { return urn; }
        public void setUrn(String urn) { this.urn = urn; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        @Override
        public String toString() {
            return "FinacleResponse{success=" + success + ", status='" + status + 
                   "', message='" + message + "', urn='" + urn + "'}";
        }
    }
}