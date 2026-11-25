<%@ include file="/init.jsp" %>


<portlet:actionURL name='saveIt' var="publishData"/>

 

<aui:form cssClass="custom-form" action="<%=publishData %>" method="post" name="myForm">
    <aui:fieldset>
        <aui:input
            name="userScreenName"
            label="User Screen Name"
            type="autocomplete"
            placeholder="Search User..."
        >
            <aui:validator name="required" />
            
        </aui:input>

       <aui:select name="departmentId" label="Department ID" required="true">
    <aui:option value="">-- Select Department --</aui:option>
    <aui:option value="BOARD_LEVEL_MEETINGS">BOARD_LEVEL_MEETINGS</aui:option>
    <aui:option value="CPD_INSURANCE_CLAIM">CPD_INSURANCE_CLAIM</aui:option>
    <aui:option value="DBD_MANDATE_CANCELLATION">DBD_MANDATE_CANCELLATION</aui:option>
    <aui:option value="DIGITAL_BANKING_DEPARTMENT">DIGITAL_BANKING_DEPARTMENT</aui:option>
    <aui:option value="UPI_Insurance">UPI_Insurance</aui:option>
    <aui:option value="FINANCE_PLOE">FINANCE_PLOE</aui:option>
    <aui:option value="FBD">FBD</aui:option>
    <aui:option value="FBD_FE_BO">FBD_FE_BO</aui:option>
    <aui:option value="ACCOUNT_OPENING">ACCOUNT_OPENING</aui:option>
    <aui:option value="FE_BO_NON_Financial">FE_BO_NON_Financial</aui:option>
</aui:select>
        <aui:input name="organizationId" label="Organization ID" required="true" />
        <aui:input name="folderId" label="Folder ID" required="true" />

        <aui:button type="submit" value="Submit" cssClass="submit-btn"/>
    </aui:fieldset>
</aui:form>

 