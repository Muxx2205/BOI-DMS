<%@ include file="/init.jsp" %>
<%@ page import="com.liferay.portal.kernel.model.Role" %>
<%@ page import="com.liferay.portal.kernel.model.Organization" %>

<liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
<liferay-ui:error key="requestFailed" message="Failed to process your request." />
<liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Please try again." />

 
<portlet:actionURL name="syncAssignment" var="publishData" />
<portlet:renderURL var="backURL">
    <portlet:param name="mvcPath" value="/view.jsp" />
</portlet:renderURL>
<style>
/* --- Wrapper --- */
.page-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background: #f8fafc;
  min-height: calc(100vh - 120px);
  padding: 40px 15px;
}

/* --- Card Container --- */
.card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 35px;
  width: 100%;
  max-width: 580px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease, transform 0.2s ease;
}
.card:hover {
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* --- Header --- */
.card h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  text-align: center;
  margin-bottom: 25px;
}

/* --- Form Styling --- */
.custom-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.aui-field-label {
  font-weight: 500;
  color: #334155;
  margin-bottom: 6px;
}

/* --- Input Fields --- */
.field-wrapper input,
.aui-field-select select {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 12px;
  width: 100%;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  font-size: 14px;
}

.field-wrapper input:focus,
.aui-field-select select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  outline: none;
}

/* --- Button --- */
.submit-btn {
  background: linear-gradient(180deg, #2563eb, #1e40af);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 500;
  letter-spacing: 0.3px;
  width: 100%;
  margin-top: 10px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
  transition: all 0.2s ease;
}
.submit-btn:hover {
  background: linear-gradient(180deg, #1d4ed8, #1e3a8a);
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.35);
}
.card-actions {
  margin: 20px 0;
  display: flex;
  justify-content: flex-start;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: #f1f5f9;
  color: #0f172a;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.btn-back .icon {
  width: 18px;
  height: 18px;
  stroke: #0f172a;
}

</style>

<div class="page-wrapper">
  <div class="card">
    <h2>Sync Entry with User Assignment</h2>

    <aui:form cssClass="custom-form" action="<%= publishData %>" method="post" name="myFormw">
      <aui:fieldset>
        <aui:select name="departmentId" label="Department ID" required="true">
          <aui:option value="">-- Select Department --</aui:option>
          <aui:option value="CPD_Insurance_Claim">CPD_INSURANCE_CLAIM</aui:option>
          <aui:option value="DBD_Mandate_Cancellation">DBD_MANDATE_CANCELLATION</aui:option>
          <aui:option value="Digital_Banking_Department">DIGITAL_BANKING_DEPARTMENT</aui:option>
          <aui:option value="UPI_Insurance">UPI_INSURANCE</aui:option>
          <aui:option value="Finance_Ploe">FINANCE_PLOE</aui:option>
          <aui:option value="FBD">FBD</aui:option>
          <aui:option value="FBD_FE_BO">FBD_FE_BO</aui:option>
          <aui:option value="Account_Opening">ACCOUNT_OPENING</aui:option>
          <aui:option value="FE_BO_NON_Financial">FE_BO_NON_FINANCIAL</aui:option>
        </aui:select>

        <aui:input name="branchName" label="Branch Name" required="true" cssClass="field-wrapper" />
        <aui:button type="submit" value="Submit" cssClass="submit-btn" />
      </aui:fieldset>
    </aui:form>
 
<div class="card-actions">
  <a href="<%= backURL %>" class="btn-back">
    <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M15 18l-6-6 6-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
    Back
  </a>
</div>
  </div>
</div>

 
