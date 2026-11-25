package com.boi.headless.rest.common.mail;

public class BranchMailDTO {
	private String branchCode;
	private String branchName;
	private String sOLID;
	private String zone;
	private String email;

	public BranchMailDTO(String branchCode, String branchName, String sOLID, String zone, String email) {
		this.branchCode = normalizeBranchCode(branchCode);
		this.branchName = branchName;
		this.sOLID = sOLID;
		this.zone = zone;
		this.email = email;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getSOLID() {
		return sOLID;
	}

	public String getZone() {
		return zone;
	}

	public String getEmail() {
		return email;
	}
	private String normalizeBranchCode(String branchCode) {
	    if (branchCode == null) {
	        return null;
	    }

	    branchCode = branchCode.trim();

	    if (branchCode.length() >= 4) {
	        return branchCode;
	    }

	    return String.format("%4s", branchCode).replace(' ', '0');
	}

}
