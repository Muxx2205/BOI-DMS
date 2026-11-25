package com.boi.headless.rest.spa.common.application;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

@ExtendedObjectClassDefinition(
	category = "BOI VKYC",
	scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = "com.boi.headless.rest.spa.common.application.BOIVkycConfiguation")
public interface BOIVkycConfiguation {

	@Meta.AD(required = false, deflt = "38037")
	public long VkycDefaultFolderId();

	@Meta.AD(required = false, deflt = "44984")
	public long VkycObjectDefinitionId();

	@Meta.AD(required = false, deflt = "VKYC")
	public String VkycDepartmentName();
}