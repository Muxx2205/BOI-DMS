package com.boi.headless.rest.common.application;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category= "BOIWorkflowConfiguation",scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id="com.boi.headless.rest.common.application.BOIWorkflowConfiguation")

public interface BOIWorkflowConfiguation {
	
	@Meta.AD(required=false,deflt = "false")
	public boolean isExportToExcel();
	
	@Meta.AD(required=false,deflt = "0")
	public long HODefualtFolderId();
	
	@Meta.AD(required=false,deflt = "false")
	public boolean selfAssignmentIsAllow();
	
	@Meta.AD(required=false)
	public long CustomerAccountObjectDefinitionId();
	
	@Meta.AD(required=false,deflt = "false")
	public boolean enableES();
	
	@Meta.AD(required=false)
	public long NewAccountOpeningWorkflowLogsId();
 	
	@Meta.AD(required=false)
	public long UPIInsuranceObjectDefinitionId();
 
	@Meta.AD(required=false)
	public long CPDInsuranceObjectDefinitionId();
		
	@Meta.AD(required=false)
	public long CustomWorkflowLogsPMSBYId();
	 
	@Meta.AD(required=false)
	public long CustomWorkflowLogsObjectDefinitionId();
	
	 
	@Meta.AD(required=false)
	public long DBDepartmentObjectDefinitionId();
	 
	
	@Meta.AD(required=false)
	public long DBDMandateObjectDefinitionId();
	
	 
	@Meta.AD(required=false)
	public long FBDObjectDefinitionId();
	 
	@Meta.AD(required=false)
	public long FEBONONFinancialObjectDefinitionId();
	
	@Meta.AD(required=false)
	public long PMSBYId();
	
	@Meta.AD(required = false)
	public long ConsentObjectDefinitionId();
	
	@Meta.AD(required = false)
	public long ConsentWorkflowLogsId();

	@Meta.AD(required = false, deflt = "false")
	public boolean enablePIIEncryption();

	@Meta.AD(required = false, deflt = "")
	public String aesEncryptionKey();
	
	 
	@Meta.AD(required=false)
	public long FinancePloeObjectDefinitionId();
	
	
	@Meta.AD(required=false)
	public long FBDFRBOObjectDefinitionId();
	
	
	@Meta.AD(required=false)
	public long UserDepartmentObjectDefinitionId();
	
	@Meta.AD(required=false)
	public long AdminUserId();
	
	 
	@Meta.AD(required=false)
	public String CustomerAccountObjectColumns();

	@Meta.AD(required=false)
	public String UPIInsuranceObjectColumns();

	@Meta.AD(required=false)
	public String CPDInsuranceObjectColumns();

	@Meta.AD(required=false)
	public String DBDepartmentObjectColumns();
	
	@Meta.AD(required=false)
	public String DBDMandateObjectColumns();
	
	@Meta.AD(required=false)
	public String FBDObjectColumns();
	
	@Meta.AD(required=false)
	public String FEBONONFinancialObjectColumns();
	
	@Meta.AD(required=false)
	public String FinancePloeObjectColumns();
	
	@Meta.AD(required=false)
	public String FBDFOBOObjectColumns();
	
	// ==================== ADD THIS ====================
	@Meta.AD(required = false)
	public long DDEReturnObjectDefinitionId();
	// ==================== END ADD ====================
	
}