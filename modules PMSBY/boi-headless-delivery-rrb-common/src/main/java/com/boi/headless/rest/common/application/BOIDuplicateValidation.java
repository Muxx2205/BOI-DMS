package com.boi.headless.rest.common.application;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category= "BOIWorkflowConfiguation",scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id="com.boi.headless.rest.common.application.BOIDuplicateValidation")

public interface BOIDuplicateValidation {
	
	@Meta.AD(required=false,deflt = "false")
	public boolean isActive();
	
	@Meta.AD(required=false,deflt = "false")
	public boolean scriptingCheck();
	
	@Meta.AD(required=false,deflt = "2")
	public int intervalOfMinutes();
	
	@Meta.AD(required=false,deflt = "")
	public String scritpStringBlocker();
	

	
	
}