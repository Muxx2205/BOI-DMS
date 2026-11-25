package com.boi.headless.rest.common.constants;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;

import java.util.HashMap;
import java.util.Map;

public class BOIDMSConstants {

	public static final String BOARD_LEVEL_MEETINGS = "BOARD_LEVEL_MEETINGS";
	public static final String CPD_INSURANCE_CLAIM = "CPD_Insurance_Claim";
	public static final String DBD_MANDATE_CANCELLATION = "DBD_Mandate_Cancellation";
	public static final String DIGITAL_BANKING_DEPARTMENT = "Digital_Banking_Department";
	public static final String ACCOUNT_OPENING = "Account_Opening";
	public static final String FBD_FE_BO = "FBD_FE_BO";
	public static final String FBD = "FBD";
	public static final String FINANCE_PLOE = "Finance_Ploe";
	public static final String UPI_Insurance = "UPI_Insurance";
	public static final String FE_BO_NON_Financial = "FE_BO_NON_Financial";
	
	// ==================== NEW: Consent Module Constant ====================
	public static final String CONSENT = "CONSENT";

// ==================== ADD ====================
public static final String DDE_RETURN = "DDE_RETURN";
// ==================== END ADD ====================

public static final String PMSBY = "PMSBY";

	public static final String BRANCH = "BRANCH";
	public static final String ZONE = "ZONE";
	public static final String NBG = "NBG";
	public static final String HO = "HO";

	public static final String BRANCHMAKER = "Branch Maker";
	public static final String BRANCHCHECKER = "Branch Checker";
	public static final String ZONEMAKER = "Zone Maker";
	public static final String ZONECHECKER = "Zone Checker";
	public static final String NGBCHECKER = "NBG Checker";
	public static final String NBGMAKER = "NBG Maker";
	public static final String HOMAKER = "HO Maker";
	public static final String HOCHECKER = "HO Checker";
	
	public static final String CPD_INSURANCE_CLAIM_ROLE = "CPD Insurance";
	public static final String DBD_MANDATE_CANCELLATION_ROLE = "DBD Mandate Cancellation";
	public static final String DIGITAL_BANKING_DEPARTMENT_ROLE = "Digital Banking Department";
	public static final String ACCOUNT_OPENING_ROLE = "Account Opening";
	public static final String FBD_FE_BO_ROLE = "FBD FE BO";
	public static final String FBD_ROLE = "FBD";
	public static final String FINANCE_PLOE_ROLE = "Finance Ploe";
	public static final String UPI_Insurance_ROLE = "UPI Insurance";
	public static final String FE_BO_NON_Financial_ROLE = "FE BO NON Financial";
	
	public static long dbdCounter=9999l;

	public static long getObjectDefinitionBasedOnDeptName(String departmentName,
			BOIWorkflowConfiguation boiWorkflowConfiguation) {

		switch (departmentName) {
		case BOARD_LEVEL_MEETINGS:
			return 0l;

		case CPD_INSURANCE_CLAIM:
			return boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId();
		case DBD_MANDATE_CANCELLATION:
			return boiWorkflowConfiguation.DBDMandateObjectDefinitionId();
		case DIGITAL_BANKING_DEPARTMENT:
			return boiWorkflowConfiguation.DBDepartmentObjectDefinitionId();
		case ACCOUNT_OPENING:
			return boiWorkflowConfiguation.CustomerAccountObjectDefinitionId();
		case FBD_FE_BO:
			return boiWorkflowConfiguation.FBDFRBOObjectDefinitionId();
		case FBD:
			return boiWorkflowConfiguation.FBDObjectDefinitionId();
		case FINANCE_PLOE:
			return boiWorkflowConfiguation.FinancePloeObjectDefinitionId();
		case UPI_Insurance:
			return boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId();
		case FE_BO_NON_Financial:
			return boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId();
		// ==================== ADD ====================
		case DDE_RETURN:
			return boiWorkflowConfiguation.DDEReturnObjectDefinitionId();
		// ==================== END ADD ====================
		default:
			return 0l;
		}
	}
	
	public static String getDepartmentNameId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {

	    if (classId == boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId()) {
	        return CPD_INSURANCE_CLAIM;
	    }

	    if (classId == boiWorkflowConfiguation.DBDMandateObjectDefinitionId()) {
	        return DBD_MANDATE_CANCELLATION;
	    }

	    if (classId == boiWorkflowConfiguation.DBDepartmentObjectDefinitionId()) {
	        return DIGITAL_BANKING_DEPARTMENT;
	    }

	    if (classId == boiWorkflowConfiguation.CustomerAccountObjectDefinitionId()) {
	        return ACCOUNT_OPENING;
	    }

	    if (classId == boiWorkflowConfiguation.FBDFRBOObjectDefinitionId()) {
	        return FBD_FE_BO;
	    }

	    if (classId == boiWorkflowConfiguation.FBDObjectDefinitionId()) {
	        return FBD;
	    }

	    if (classId == boiWorkflowConfiguation.FinancePloeObjectDefinitionId()) {
	        return FINANCE_PLOE;
	    }

	    if (classId == boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId()) {
	        return UPI_Insurance;
	    }

	    if (classId == boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId()) {
	        return FE_BO_NON_Financial;
	    }

	    // ==================== ADD ====================
	    if (classId == boiWorkflowConfiguation.DDEReturnObjectDefinitionId()) {
	        return DDE_RETURN;
	    }
	    // ==================== END ADD ====================

	    return null;
	}
	
	
	public static String getDepartmentRoleId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {

	    if (classId == boiWorkflowConfiguation.CPDInsuranceObjectDefinitionId()) {
	        return "CPD Insurance";
	    }

	    if (classId == boiWorkflowConfiguation.DBDMandateObjectDefinitionId()) {
	    	return "DBD Mandate Cancellation";
	    }

	    if (classId == boiWorkflowConfiguation.DBDepartmentObjectDefinitionId()) {
	    	return "Digital Banking Department";
	    }

	    if (classId == boiWorkflowConfiguation.CustomerAccountObjectDefinitionId()) {
	    	return "Account Opening";
	    }

	    if (classId == boiWorkflowConfiguation.FBDFRBOObjectDefinitionId()) {
	    	return "FBD FE BO";
	    }

	    if (classId == boiWorkflowConfiguation.FBDObjectDefinitionId()) {
	    	return "FBD";
	    }

	    if (classId == boiWorkflowConfiguation.FinancePloeObjectDefinitionId()) {
	    	return "Finance Ploe";
	    }

	    if (classId == boiWorkflowConfiguation.UPIInsuranceObjectDefinitionId()) {
	    	return "UPI Insurance";
	    }

	    if (classId == boiWorkflowConfiguation.FEBONONFinancialObjectDefinitionId()) {
	    	return "FE BO NON Financial";
	    }

	    // ==================== ADD ====================
	    if (classId == boiWorkflowConfiguation.DDEReturnObjectDefinitionId()) {
	    	return "Nesldde";
	    }
	    // ==================== END ADD ====================

	    return null;
	}

	 
	public static Map<String,String> getObjectFileNames(String departmentName) {

		switch (departmentName) {
		case CPD_INSURANCE_CLAIM:
			return CPD_INSURANCE_CLAIM_Files;
		case DBD_MANDATE_CANCELLATION:
			return DBD_MANDATE_CANCELLATION_Files;
		case DIGITAL_BANKING_DEPARTMENT:
			return DIGITAL_BANKING_DEPARTMENT_Files;
		case ACCOUNT_OPENING:
			return ACCOUNT_OPENING_Files;
		case FBD_FE_BO:
			return FBD_FE_BO_Files;
		case FBD:
			return FBD_Files;
		case FINANCE_PLOE:
			return FINANCE_PLOE_Files;
		case UPI_Insurance:
			return UPI_Insurance_Files;
		case FE_BO_NON_Financial:
			return FE_BO_NON_Financial_Files;
		default:
			return new HashMap<>();
		}
	}
	
	
	
	
	private static Map<String,String> CPD_INSURANCE_CLAIM_Files=new HashMap<String,String>() {{
		 put("claimFormDetailedFile", "claimFormDetailed");
		 put("claimVoucherDetailedFile", "claimVoucherDetailed");
		 put("letterOfSubrogationDetailedFile", "letterOfSubrogationDetailed");
		 put("investigationReportByBranchDetailedFile", "investigationReportByBranchDetailed");
		 put("policeComplainOrFIRCopyDetailedFile", "policeComplainOrFIRCopyDetailed");
		 put("chargeBackRejectionReportDetailedFile", "chargeBackRejectionReportDetailed");
		 put("hotlistingCertificateDetailedFile", "hotlistingCertificateDetailed");
		 put("accountStatementDetailedFile", "accountStatementDetailed");
		 put("anyOtherDocumentsDetailedFile", "anyOtherDocumentsDetailed");
	 }};
	
	
	private static Map<String,String> DIGITAL_BANKING_DEPARTMENT_Files=new HashMap<String,String>() {{
		 put("frontGrayScaleImageDetailedFile", "frontGrayScaleImageDetailed");
		 put("frontImageDetailedFile", "frontImageDetailed");
		 
	 }};
	private static Map<String,String> DBD_MANDATE_CANCELLATION_Files=new HashMap<String,String>() {{
		 put("cancellationDocumentDetailedFile", "cancellationDocumentDetailed");
	 }};
	
	private static  Map<String,String> FBD_FE_BO_Files=new HashMap<String,String>() {{
		 put("documentNameDetailedFile", "documentNameDetailed");
		  
	 }};
	
	private static Map<String,String> UPI_Insurance_Files=new HashMap<String,String>() {{
		 put("policeIntimationFIRDetailedFile", "policeIntimationFIRDetailed");
		 put("completePassportCopyDetailedFile", "completePassportCopyDetailed");
		 put("claimFormDetailedFile", "claimFormDetailed");
		 put("briefNoteFromBranchDetailedFile", "briefNoteFromBranchDetailed");
		 put("anyOtherDocumentsDetailedFile", "anyOtherDocumentsDetailed");
		 put("accountStatementDetailedFile", "accountStatementDetailed");
	 }};
	 
	 private static Map<String,String> FINANCE_PLOE_Files=new HashMap<String,String>() {{
		 put("invoiceDetailedFile", "invoiceDetailed");
		 
	 }};
	
	 private static Map<String,String> FE_BO_NON_Financial_Files=new HashMap<String,String>() {{
		 put("documentNameDetailedFile", "documentNameDetailed");
		  
	 }};
	
	 private static Map<String, String> ACCOUNT_OPENING_Files = new HashMap<String, String>() {
			{
				put("applicationDetailedFile", "applicationDetailed");
				put("poiDocumentName1DetailedFile", "poiDocumentName1Detailed");
				put("poiDocumentName2DetailedFile", "poiDocumentName2Detailed");
				put("poiDocumentName3DetailedFile", "poiDocumentName3Detailed");
				put("poiDocumentName4DetailedFile", "poiDocumentName4Detailed");
				put("poaDocumentName1DetailedFile", "poaDocumentName1Detailed");
				put("poaDocumentName2DetailedFile", "poaDocumentName2Detailed");
				put("poaDocumentName3DetailedFile", "poaDocumentName3Detailed");
				put("poaDocumentName4DetailedFile", "poaDocumentName4Detailed");
				put("additionalDocumentNameDetailedFile", "additionalDocumentNameDetailed");
				put("customerProfileSheetDetailedFile", "customerProfileSheetDetailed");
				put("photoDetailedFile", "photoDetailed");
				put("signatureDetailedFile", "signatureDetailed");
			}
		};
	
		 private static Map<String,String> FBD_Files=new HashMap<String,String>() {{
			 put("documentDetailedFile", "documentDetailed");
			 
			  
		 }};

}
