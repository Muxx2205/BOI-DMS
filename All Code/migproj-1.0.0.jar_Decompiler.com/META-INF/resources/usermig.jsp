<%@ include file="/init.jsp" %>
<portlet:actionURL name="usermigformSubmit" var="usermigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>User Migration Form</h1>
</p>

<aui:form action="<%= usermigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="companyId" label="Company ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <br/>
        <div class="row">
        
     <aui:input name="groupId" label="Group ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
         <br/>
        <div class="row">
        
     <aui:input name="creatorId" label="Admin User ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
         <br/>
        <div class="row">
        
     <aui:input name="userRoleId" label="UserRole ID" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     

     </div>
     <br/>
  
     <br/>
     <div class="row" >
     
    
       
        </div>
    <br/><br/>
     <div class="row" >
    <aui:button type="submit" value="Submit" cssClass="form-control col-md-6 btn btn-primary"/>
    </div>
</aui:form>
</div>