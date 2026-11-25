package com.boi.headless.rest.common.spa.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BOIDMSConstants {
   public static final String BOARD_LEVEL_MEETINGS = "BOARD_LEVEL_MEETINGS";
   public static final String INSPECTION_AUDIT_ACB = "INSPECTION_AUDIT_ACB";
   public static final String INSPECTION_AUDIT_ACE = "INSPECTION_AUDIT_ACE";
   public static final String INSPECTION_AUDIT_OTHER_REPORT = "INSPECTION_AUDIT_OTHER_REPORT";
   public static final String INSPECTION_AUDIT_CA_APPOINTMENTS = "INSPECTION_AUDIT_CA_APPOINTMENTS";
   public static final String INSPECTION_FOREIGN_AUDIT = "INSPECTION_FOREIGN_AUDIT";
   public static final String INSPECTION_AUDIT_REPORTS = "INSPECTION_AUDIT_REPORTS";
   public static final String INSPECTION_AUDIT_MISC = "INSPECTION_AUDIT_MISC";
   public static final String CDAD = "CDAD";
   public static final String DATA_WAREHOUSE = "DATA_WAREHOUSE";
   public static final String E_PLATEFORM = "E_PLATEFORM";
   public static final String E_CRM = "E_CRM";
   public static final String SGB_SSY = "SGB SSY";
   public static final String KCC_JANSAMARTH = "KCC_JANSAMARTH";
   public static final String UTOPIA = "UTOPIA";
   public static final String ELECTRICAL_WIRING_DIAGRAM = "ELECTRICAL_WIRING_DIAGRAM";
   public static final String HO_RECRUITMENT_DIVISION = "HO_RECRUITMENT_DIVISION";
   public static final String HO_SECURITY_DEPARTMENT = "HO_SECURITY_DEPARTMENT";
   public static final String INTERNAL_LAYOUT_MAP = "INTERNAL_LAYOUT_MAP";
   public static final String INTERNATIONAL_DEPARTMENT = "INTERNATIONAL_DEPARTMENT";
   public static final String BRANCH_DIGITIATION = "BRANCH_DIGITIATION";
   public static final String PROJECT_MANAGEMENT_LIST = "PROJECT_MANAGEMENT_LIST";
   public static final String FINASTRA = "FINASTRA";
   public static final String FINASTRA_OTHER_DOC = "FINASTRA_OTHER_DOC";
   public static final String BRANCH = "BRANCH";
   public static final String ZONE = "ZONE";
   public static final String NBG = "NBG";
   public static final String HO = "HO";
   public static final String DOCUMENT = "document";
   public static final String MEETING_DOCUMENT_NAME = "mEETINGDOCUMENTNAME";
   public static final String DOCUMENT_NAME_ATT = "documentNameAttNew";
   public static final String REPORT_1_XLSX = "Report-1.xlsx";
   public static final String ENTRYID = "entryId";
   public static final String FONDER_INDEX = "folderIndex";
   public static final String FILEID = "fileId";
   public static final String FINASTRA_STATUS = "finastraStatus";
   public static final String FINANCIAL_YEAR = "financialYear";
   public static final String REFERENCE_NO = "referenceNo";
   public static final String STATUS = "status";
   public static final String BRANCHMAKER = "Branch Maker";
   public static final String BRANCHCHECKER = "Branch Checker";
   public static final String ZONEMAKER = "Zone Maker";
   public static final String ZONECHECKER = "Zone Checker";
   public static final String NGBCHECKER = "NBG Checker";
   public static final String NBGMAKER = "NBG Maker";
   public static final String HOMAKER = "HO Maker";
   public static final String HOCHECKER = "HO Checker";
   public static final Map<String, String> BOARD_LEVEL_MEETINGS_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_ACB_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_ACE_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_OTHER_REPORT_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_CA_APPOINTMENTS_FILES;
   public static final Map<String, String> INSPECTION_FOREIGN_AUDIT_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_REPORTS_FILES;
   public static final Map<String, String> INSPECTION_AUDIT_MISC_FILES;
   public static final Map<String, String> CDAD_FILES;
   public static final Map<String, String> HFINASTRA_FILES;
   public static final Map<String, String> FINASTRA_OTHER_DOC_FILES;
   public static final Map<String, String> DATA_WAREHOUSE_FILES;
   public static final Map<String, String> E_PLATEFORM_FILES;
   public static final Map<String, String> E_CRM_FILES;
   public static final Map<String, String> KCC_JANSAMARTH_FILES;
   public static final Map<String, String> UTOPIA_FILES;
   public static final Map<String, String> ELECTRICAL_WIRING_DIAGRAM_FILES;
   public static final Map<String, String> HO_RECRUITMENT_DIVISION_FILES;
   public static final Map<String, String> HO_SECURITY_DEPARTMENT_FILES;
   public static final Map<String, String> INTERNAL_LAYOUT_MAP_FILES;
   public static final Map<String, String> INTERNATIONAL_DEPARTMENT_FILES;
   public static final Map<String, String> PROJECT_MANAGEMENT_LIST_FILES;
   public static final Map<String, String> BRANCH_DIGITIATION_FILES;

   public static Map<String, String> getObjectFileNames(String departmentName) {
      switch (departmentName) {
         case "BOARD_LEVEL_MEETINGS":
            return BOARD_LEVEL_MEETINGS_FILES;
         case "INSPECTION_AUDIT_ACB":
            return INSPECTION_AUDIT_ACB_FILES;
         case "INSPECTION_AUDIT_ACE":
            return INSPECTION_AUDIT_ACE_FILES;
         case "INSPECTION_AUDIT_OTHER_REPORT":
            return INSPECTION_AUDIT_OTHER_REPORT_FILES;
         case "INSPECTION_AUDIT_CA_APPOINTMENTS":
            return INSPECTION_AUDIT_CA_APPOINTMENTS_FILES;
         case "INSPECTION_FOREIGN_AUDIT":
            return INSPECTION_FOREIGN_AUDIT_FILES;
         case "INSPECTION_AUDIT_REPORTS":
            return INSPECTION_AUDIT_REPORTS_FILES;
         case "INSPECTION_AUDIT_MISC":
            return INSPECTION_AUDIT_MISC_FILES;
         case "INTERNAL_LAYOUT_MAP":
            return INTERNAL_LAYOUT_MAP_FILES;
         case "INTERNATIONAL_DEPARTMENT":
            return INTERNATIONAL_DEPARTMENT_FILES;
         case "CDAD":
            return CDAD_FILES;
         case "DATA_WAREHOUSE":
            return DATA_WAREHOUSE_FILES;
         case "E_PLATEFORM":
            return E_PLATEFORM_FILES;
         case "E_CRM":
            return E_CRM_FILES;
         case "KCC_JANSAMARTH":
            return KCC_JANSAMARTH_FILES;
         case "UTOPIA":
            return UTOPIA_FILES;
         case "ELECTRICAL_WIRING_DIAGRAM":
            return ELECTRICAL_WIRING_DIAGRAM_FILES;
         case "HO_RECRUITMENT_DIVISION":
            return HO_RECRUITMENT_DIVISION_FILES;
         case "HO_SECURITY_DEPARTMENT":
            return HO_SECURITY_DEPARTMENT_FILES;
         case "FINASTRA":
            return HFINASTRA_FILES;
         case "FINASTRA_OTHER_DOC":
            return FINASTRA_OTHER_DOC_FILES;
         case "PROJECT_MANAGEMENT_LIST":
            return PROJECT_MANAGEMENT_LIST_FILES;
         case "BRANCH_DIGITIATION":
            return BRANCH_DIGITIATION_FILES;
         default:
            return new HashMap();
      }
   }

   static {
      Map<String, String> map = new HashMap();
      map.put("mEETINGDOCUMENTNAME", "mEETINGDOCUMENTNAME");
      BOARD_LEVEL_MEETINGS_FILES = Collections.unmodifiableMap(map);
      Map<String, String> var1 = new HashMap();
      var1.put("document", "document");
      INSPECTION_AUDIT_ACB_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_AUDIT_ACE_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_AUDIT_OTHER_REPORT_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_AUDIT_CA_APPOINTMENTS_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_FOREIGN_AUDIT_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_AUDIT_REPORTS_FILES = Collections.unmodifiableMap(var1);
      INSPECTION_AUDIT_MISC_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("investigationReportAndA", "investigationReportAndA");
      var1.put("memorandumCallingExplan", "memorandumCallingExplan");
      var1.put("replayToMemorandum", "replayToMemorandum");
      var1.put("tabularChart", "tabularChart");
      var1.put("iACMinutes", "iACMinutes");
      var1.put("firstStageAdvice", "firstStageAdvice");
      var1.put("aOCCSSOIL", "aOCCSSOIL");
      var1.put("wSOD", "wSOD");
      var1.put("iAPOAppointment", "iAPOAppointment");
      var1.put("dailyOrderSheets", "dailyOrderSheets");
      var1.put("pOBrief", "pOBrief");
      var1.put("dRBrief", "dRBrief");
      var1.put("iAFindings", "iAFindings");
      var1.put("representationOnIAFindi", "representationOnIAFindi");
      var1.put("sSA", "sSA");
      var1.put("sCPNforAwardStaff", "sCPNforAwardStaff");
      var1.put("penaltyOrder", "penaltyOrder");
      var1.put("appealWithDAComments", "appealWithDAComments");
      var1.put("appellateOrder", "appellateOrder");
      var1.put("reviewOrderWithAACom", "reviewOrderWithAACom");
      var1.put("suspensionOrder", "suspensionOrder");
      var1.put("suspensionRevocation", "suspensionRevocation");
      var1.put("otherDoc1", "otherDoc1");
      var1.put("otherDoc2", "otherDoc2");
      var1.put("otherDoc3", "otherDoc3");
      CDAD_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("document", "document");
      DATA_WAREHOUSE_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("imageData", "imageData");
      E_PLATEFORM_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("imageData", "imageData");
      E_CRM_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("imageData", "imageData");
      KCC_JANSAMARTH_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("imageData", "imageData");
      UTOPIA_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("electricalWiringDiagram", "electricalWiringDiagram");
      ELECTRICAL_WIRING_DIAGRAM_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("applicationCallLetter", "applicationCallLetter");
      var1.put("kYCDocument", "kYCDocument");
      var1.put("ageCertificate", "ageCertificate");
      var1.put("casteCertificate", "casteCertificate");
      var1.put("diplomaHSC12th", "diplomaHSC12th");
      var1.put("graduationCertificate", "graduationCertificate");
      HO_RECRUITMENT_DIVISION_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("documentNameDetailed", "documentNameDetailed");
      HFINASTRA_FILES = Collections.unmodifiableMap(var1);
      FINASTRA_OTHER_DOC_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("documentNameAttNew", "documentNameAttNew");
      HO_SECURITY_DEPARTMENT_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("internalLayoutMap", "internalLayoutMap");
      INTERNAL_LAYOUT_MAP_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("document", "document");
      var1.put("aMLOCKTM", "aMLOCKTM");
      INTERNATIONAL_DEPARTMENT_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("documentName1", "documentName1");
      var1.put("documentName2", "documentName2");
      var1.put("documentName3", "documentName3");
      var1.put("documentName4", "documentName4");
      PROJECT_MANAGEMENT_LIST_FILES = Collections.unmodifiableMap(var1);
      var1 = new HashMap();
      var1.put("document", "document");
      BRANCH_DIGITIATION_FILES = Collections.unmodifiableMap(var1);
   }
}
