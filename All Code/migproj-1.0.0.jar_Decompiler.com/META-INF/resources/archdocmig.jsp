<%@ include file="/init.jsp" %>
<portlet:actionURL name="archdocmigformSubmit" var="archdocmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>Archived File Migration Form(DataClass)</h1>
</p>

<aui:form action="<%= archdocmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="dataclassId" label="NewGen Data Class" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        
             <div class="row" >

     
    <aui:input name="folderId" label="Liferay Archive Folder Id" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <br/>
        <div class="row">
      
   <aui:input name="fromindex" label="Data Class From Index" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
      <aui:input name="toindex" label="Data Class To Index" cssClass="form-control col-md-12"  >
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
    <br/>
     <div class="row" >
    <aui:button type="submit" value="Submit" cssClass="form-control col-md-6 btn btn-primary"/>
    </div>
</aui:form>
</div>