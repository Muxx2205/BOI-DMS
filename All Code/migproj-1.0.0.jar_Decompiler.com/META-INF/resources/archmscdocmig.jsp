<%@ include file="/init.jsp" %>
<portlet:actionURL name="archmscdocmigformSubmit" var="archmscdocmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>Archive File Migration Form(Msc)</h1>
</p>

<aui:form action="<%= archmscdocmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="folderId" label="Liferay Archive Folder Id" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <br/>
        <div class="row">
        
     <aui:input name="fromindex" label="Document From Index" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
      <aui:input name="toindex" label="Document To Index" cssClass="form-control col-md-12"  >
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