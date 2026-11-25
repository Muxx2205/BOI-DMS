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
	
	@Meta.AD(required = false)
	public long ConsentObjectDefinitionId();
	
	@Meta.AD(required = false)
	public long ConsentWorkflowLogsId();
		
	@Meta.AD(required=false)
	public long UserDepartmentObjectDefinitionId();
	
	@Meta.AD(required=false)
	public long AdminUserId();
	
	 
	@Meta.AD(required=false)
	public String ConsentObjectColumns();
}
