<%@ include file="/init.jsp" %>

<liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
<liferay-ui:error key="requestFailed" message="Failed to process your request." />
<liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Please try again." />

<portlet:actionURL name="saveIt" var="publishData" />

<style>
/* --- Layout Wrapper --- */
.page-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 15px;
  background: #f8fafc;
  min-height: calc(100vh - 120px);
}

/* --- Card Container --- */
.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 40px 35px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 550px;
  transition: all 0.2s ease;
}
.card:hover {
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.10);
}

/* --- Header --- */
.card h2 {
  text-align: center;
  color: #1e293b;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 25px;
}

/* --- Form --- */
.custom-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-wrapper input {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 12px;
  width: 100%;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field-wrapper input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  outline: none;
}

/* --- Submit Button --- */
.submit-btn {
  background: linear-gradient(180deg, #2563eb, #1e40af);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 500;
  letter-spacing: 0.3px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
  transition: all 0.2s ease;
  margin-top: 10px;
  width: 100%;
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
        <aui:input name="classPk" label="Class PK ID" required="true" cssClass="field-wrapper" />
        <aui:button type="submit" value="Submit" cssClass="submit-btn" />
      </aui:fieldset>
    </aui:form>
    <portlet:renderURL var="backURL">
    <portlet:param name="mvcPath" value="/view.jsp" />
</portlet:renderURL>
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
