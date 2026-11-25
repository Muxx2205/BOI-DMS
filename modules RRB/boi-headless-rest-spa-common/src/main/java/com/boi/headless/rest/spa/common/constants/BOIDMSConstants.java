package com.boi.headless.rest.spa.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BOIDMSConstants {

	public static final String VKYC_UPLOAD = "VKYC_UPLOAD";

	public static final String BRANCH = "BRANCH";
	public static final String REGIONAL_OFFICE = "REGIONAL OFFICE";
	public static final String HO = "HO";

	public static final String BRANCHMAKER = "Branch Maker";
	public static final String BRANCHCHECKER = "Branch Checker";
	public static final String ROMAKER = "RO Maker";
	public static final String ROCHECKER = "RO Checker";
	public static final String HOCHECKER = "HO Checker";

	public static Map<String, String> getObjectFileNames(String departmentName) {
		if (VKYC_UPLOAD.equals(departmentName)) {
			return VKYC_UPLOAD_Files;
		}
		return new HashMap<>();
	}

	private static final Map<String, String> VKYC_UPLOAD_Files;

	static {
		Map<String, String> m = new HashMap<>();
		// Left side: the form-data key sent by the frontend (e.g., -F "vkycDocument=@file.pdf")
		// Right side: the actual Object Field name in your VKYCDocument object
		m.put("vkycDocument", "imageData"); 
		
		// If you have a second file upload field for a photo, map it to another object field
		// m.put("customerPhoto", "customerPhoto"); 
		
		VKYC_UPLOAD_Files = Collections.unmodifiableMap(m);
	}
}