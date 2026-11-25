<%@ include file="/init.jsp" %>
<portlet:actionURL name="fldmigformSubmit" var="fldmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>Folder Migration Form</h1>
</p>

<aui:form action="<%= fldmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="newGenFolderId" label="NewGen Root Folder ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <br/>
        <div class="row">
        
     <aui:input name="liferayFolderId" label="Liferay Root Folder ID" cssClass="form-control col-md-12"  >
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