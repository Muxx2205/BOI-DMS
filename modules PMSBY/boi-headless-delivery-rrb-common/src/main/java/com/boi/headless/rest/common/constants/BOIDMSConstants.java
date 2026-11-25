package com.boi.headless.rest.common.constants;

import com.boi.headless.rest.common.application.BOIWorkflowConfiguation;

import java.util.HashMap;
import java.util.Map;

public class BOIDMSConstants {

	// ==================== NEW: Consent Module Constant ====================
	public static final String RRB_CONSENT = "RRB CONSENT";

	public static final String BRANCH = "BRANCH";
	public static final String REGIONAL_OFFICE = "REGIONAL OFFICE";
	public static final String HO = "HO";

	public static final String BRANCHMAKER = "Branch Maker";
	public static final String BRANCHCHECKER = "Branch Checker";
	public static final String ROMAKER = "RO Maker";
	public static final String ROCHECKER = "RO Checker";
	public static final String HOMAKER = "HO Maker";
	public static final String HOCHECKER = "HO Checker";

	public static long getObjectDefinitionBasedOnDeptName(String departmentName,
			BOIWorkflowConfiguation boiWorkflowConfiguation) {

		switch (departmentName) {

		case RRB_CONSENT:
			return boiWorkflowConfiguation.ConsentObjectDefinitionId();
		default:
			return 0l;
		}
	}

	public static String getDepartmentNameId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {

		if (classId == boiWorkflowConfiguation.ConsentObjectDefinitionId()) {
			return RRB_CONSENT;
		}
		return null;
	}

	public static String getDepartmentRoleId(BOIWorkflowConfiguation boiWorkflowConfiguation, long classId) {

		if (classId == boiWorkflowConfiguation.ConsentObjectDefinitionId()) {
			return "RRB Consent";
		}

		return null;
	}

	public static Map<String, String> getObjectFileNames(String departmentName) {

		switch (departmentName) {
		case RRB_CONSENT:
			return RRB_CONSENT_Files;
		default:
			return new HashMap<>();
		}
	}

	private static Map<String, String> RRB_CONSENT_Files = new HashMap<String, String>() {
		{
			put("documentNameDetailed", "documentNameDetailed");

		}
	};

}
