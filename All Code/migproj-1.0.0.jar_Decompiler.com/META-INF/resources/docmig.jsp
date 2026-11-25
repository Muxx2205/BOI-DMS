<%@ include file="/init.jsp" %>
<portlet:actionURL name="docmigformSubmit" var="docmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>File Migration Form(DataClass)</h1>
</p>

<aui:form action="<%= docmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
   
      <br/><br/><br/>
     <div class="row" >

     
    <aui:input name="dataclassId" label="NewGen Data Class" cssClass="form-control col-md-12"  >
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
        
        
          <aui:input name="userNumber" label="User Number" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
      
     <aui:input name="rootpath" label="Root Folder" cssClass="form-control col-md-12"  >
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