<%@ include file="/init.jsp" %>
<portlet:actionURL name="dbmigformSubmit" var="dbmigformSubmitURL" />
<div style="margin-left: 150px;float: left;">
<p>
	<h1>DB Migration Form</h1>
</p>

<aui:form action="<%= dbmigformSubmitURL %>" method="post" 
name="userForm" cssClass="user-form">
    <div class="row" ">
    <aui:select name="sourceDB" label="Source DB:" cssClass="form-control col-md-12">
    <aui:option value="DB2">DB2</aui:option>
    <aui:option value="MYSQL">MYSQL</aui:option>
    <aui:option value="POSTGRESQL">POSTGRESQL</aui:option>
    <aui:option value="ORACLE">ORACLE</aui:option>
    <aui:option value="MSSQL">MSSQL</aui:option>
    <aui:validator name="required" />
    </aui:select>
   
    <aui:input name="sourceSchema" label="Source Schema" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
    
    </div><div class="row" >
     <aui:input name="sourceHost" label="Source Host Name" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
        </aui:input>
      
        
      <aui:input name="sourcePort" label="Source Port" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
    <aui:validator name="number" />
    </aui:input>
    </div><div class="row">
    
       <aui:input name="sourceUserName" label="Source UserName" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
      
       
        <aui:input name="sourcePassword" label="Source Password" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
     </aui:input>
     </div>
      <br/><br/><br/>
     <div class="row" >
    
   
    <aui:select name="targetDB" label="Target DB:" cssClass="form-control col-md-12">
    <aui:option value="DB2">DB2</aui:option>
    <aui:option value="MYSQL">MYSQL</aui:option>
    <aui:option value="POSTGRESQL">POSTGRESQL</aui:option>
    <aui:option value="ORACLE">ORACLE</aui:option>
    <aui:option value="MSSQL">MSSQL</aui:option> 
    <aui:validator name="required" />
    </aui:select>
   
     
    <aui:input name="targetSchema" label="Target Schema" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div><div class="row">
        
     <aui:input name="targetHost" label="Target Host Name" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
      </aui:input>
     
       
      <aui:input name="targetPort" label="Target Port" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
    <aui:validator name="number" />
    </aui:input>
     </div><div class="row" >
     
       <aui:input name="targetUserName" label="Target UserName" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
      
        <aui:input name="targetPassword" label="Target Password" cssClass="form-control col-md-12"  >
     <aui:validator name="required" />
       </aui:input>
        </div>
        <div class="row" >
      <aui:input name="prefix" label="Table Name Prefix" cssClass="form-control col-md-12"  >
     
       </aui:input>
       <aui:input name="tableNames" label="Table Name List (Comma Separated)"   type="textarea" rows="5" cols="40"  cssClass="form-control col-md-12"  >
     
       </aui:input>
      
       
        </div>
    <br/>
    
  
     <div class="row" >
    <aui:button type="submit" value="Submit" cssClass="form-control col-md-6 btn btn-primary"/>
    </div>
</aui:form>
</div>