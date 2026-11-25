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
	
    @Meta.AD(required = false, deflt = "http://10.118.7.132:9060/OpenKM")
    public String OpenKmBaseUrl();

    @Meta.AD(required = false, deflt = "okmAdmin")
    public String OpenKmUsername();

    @Meta.AD(required = false, deflt = "admin")
    public String OpenKmPassword();

    @Meta.AD(required = false, deflt = "/okm:root/ekyc")
    public String OpenKmRootFolder();

    @Meta.AD(required = false)
    public long VkycObjectDefinitionId();

    @Meta.AD(required = false)
    public long VkycWorkflowLogsId();

    @Meta.AD(required = false, deflt = "0")
    public long VkycDefaultFolderId();
}
