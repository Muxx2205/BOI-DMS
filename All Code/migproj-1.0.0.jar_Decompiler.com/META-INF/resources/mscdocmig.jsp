<%@ include file="/init.jsp" %>
<portlet:actionURL name="mscdocmigformSubmit" var="mscdocmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>File Migration Form(Msc)</h1>
</p>

<aui:form action="<%= mscdocmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
      <br/><br/><br/>
     <div class="row" >

    
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
        
        <aui:input name="userNumber" label="User Number" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
        
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