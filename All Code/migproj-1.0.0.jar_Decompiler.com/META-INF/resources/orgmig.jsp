<%@ include file="/init.jsp" %>
<portlet:actionURL name="orgmigformSubmit" var="orgmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>Organization Migration Form</h1>
</p>

<aui:form action="<%= orgmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="orgId" label="Organzation ID (HO)" cssClass="form-control col-md-12"  >
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