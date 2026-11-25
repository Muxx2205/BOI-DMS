package com.boi.headless.rest.common.application;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category= "BOIWorkflowConfiguation",scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id="com.boi.headless.rest.common.application.BOITokenGenerate")

public interface BOITokenGenerate{
	
	@Meta.AD(required=false)
	public String URL();
	
	@Meta.AD(required=false)
	public String Secret();
	
	@Meta.AD(required=false)
	public String Id();
	
	@Meta.AD(required=false)
	public String grantType();
	
	// ================= UTOPIA =================

    @Meta.AD(required = false)
    public String utopiaClientId();

    @Meta.AD(required = false)
    public String utopiaSecret();
	
    // ================= E-PLATFORM =================
 
    @Meta.AD(required = false)
    public String eplatformClientId();

    @Meta.AD(required = false)
    public String eplatformSecret();


    // ================= KCC JANSAMARTH =================
   
    @Meta.AD(required = false)
    public String kccJansamarthClientId();

    @Meta.AD(required = false)
    public String kccJansamarthSecret();


    // ================= CRM NEXT =================
 
    @Meta.AD(required = false)
    public String crmNextClientId();

    @Meta.AD(required = false)
    public String crmNextSecret();
    
	
}
