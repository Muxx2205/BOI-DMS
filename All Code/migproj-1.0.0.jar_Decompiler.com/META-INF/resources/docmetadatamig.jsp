<%@ include file="/init.jsp" %>
<portlet:actionURL name="metamigformSubmit" var="metamigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>Metadata Migration Form</h1>
</p>

<aui:form action="<%= metamigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="dataClassId" label="NewGen Data Class ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <br/>
        <div class="row">
        
     <aui:input name="liferayObjectId" label="Liferay Object ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
        
      <br/>
        <div class="row">
        
     <aui:input name="mdate" label="From Date" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
     
      <br/>
        <div class="row">
        
     <aui:input name="companyId" label="Company ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
       
      <br/>
        <div class="row">
        
     <aui:input name="adminUserId" label="Admin User ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
     
       <br/>
         <div class="row">
        
     <aui:input  name="fldMapping" label="Fields Mapping" cssClass="form-control col-md-12"  
      type="textarea" rows="50" cols="100" >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
     <br/>
         <div class="row">
        
     <aui:input name="startStop" label="Start/Stop" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
     <br/>
     <div class="row" >
     
    
       
        </div>
    <br/><br/>
     <div class="row" >
    <aui:button type="submit" value="Submit" cssClass="form-control col-md-6 btn btn-primary"/>
    </div>
</aui:form>
</div>