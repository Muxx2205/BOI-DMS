<%@ include file="/init.jsp" %>
<%@ page import="com.liferay.portal.kernel.model.Organization" %>

<%
User requestedUser = (User) request.getAttribute("requestedUser");
String allowToAssignment = (String) request.getAttribute("allowToAssignment");
List<Organization> organizationss = requestedUser.getOrganizations();
List<Organization> organizations = (List<Organization>) request.getAttribute("OrgType");
%>

<liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
<liferay-ui:error key="requestFailed" message="Failed to process your request." />

<portlet:actionURL name="assignmentOrgWithUser" var="assignmentOrgWithUser" />

<style>
/* --- Page Layout --- */
.page-wrapper {
  display: flex;
  justify-content: center;
  padding: 30px 15px;
  background: #f8fafc;
  min-height: calc(100vh - 120px);
}

/* --- Card Container --- */
.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
  max-width: 900px;
  width: 100%;
  transition: box-shadow 0.2s ease;
}
.card:hover {
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

/* --- Title and Notes --- */
.card-header {
  margin-bottom: 20px;
}
.card-header h2 {
  font-size: 22px;
  color: #1e293b;
  font-weight: 600;
  margin: 0;
}
.card-header .note {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

/* --- Form Styling --- */
.custom-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.aui-fieldset {
  border: none !important;
  padding: 0 !important;
}

.field-wrapper {
  border-radius: 8px;
}

/* --- Section Titles --- */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a8a;
  margin-top: 10px;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* --- Checkbox Grid --- */
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3 columns */
  gap: 10px 20px; /* spacing between boxes */
  margin-top: 10px;
}

.checkbox-grid .aui-field {
  margin: 0;
  background: #f9fafb;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.checkbox-grid .aui-field:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

/* --- Submit Button --- */
.submit-btn {
  background: linear-gradient(180deg, #2563eb, #1e40af);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 26px;
  font-weight: 500;
  letter-spacing: 0.3px;
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
    <div class="card-header">
      <h2>Organization Assignment for <%=requestedUser.getScreenName() %></h2>
      <div class="note">Assign or remove organizations for the selected user.</div>
    </div>

    <div class="form-container">
      <liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
      <liferay-ui:error key="requestFailed" message="Failed to process your request." />
      <liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Please try again." />

      <aui:form cssClass="custom-form" action="<%= assignmentOrgWithUser %>" method="post" name="myUserForm">
        <aui:fieldset>
          <aui:input name="userName" type="hidden" value="<%= requestedUser.getScreenName() %>" />
          <aui:input name="organizationData" label="Search Organization" cssClass="field-wrapper" />

          <!-- Add Organization Section -->
          <c:if test="<%= Validator.isNotNull(organizations) && !organizations.isEmpty() %>">
            <div>
              <div class="section-title">Add Organization</div>
              <div class="checkbox-grid">
                <%
                  for (Organization organization : organizations) {
                    String key = organization.getName()+" : "+organization.getType();
                %>
                  <aui:input type="checkbox"
                             name="selectedKeys"
                             value="<%= organization.getOrganizationId() %>"
                             label="<%= key %>" />
                <%
                  }
                %>
              </div>
            </div>
          </c:if>

          <!-- Remove Organization Section -->
          <c:if test="<%= Validator.isNotNull(organizationss) && !organizationss.isEmpty() %>">
            <div>
              <div class="section-title">Remove Organization</div>
              <div class="checkbox-grid">
                <%
                  for (Organization organization : organizationss) {
                    String key = organization.getName()+" : "+organization.getType();
                %>
                  <aui:input type="checkbox"
                             name="removeOrganition"
                             value="<%= organization.getOrganizationId() %>"
                             label="<%= key %>" />
                <%
                  }
                %>
              </div>
            </div>
          </c:if>

          <aui:button type="submit" value="Submit" cssClass="submit-btn" />
        </aui:fieldset>
      </aui:form>
    </div>
    <portlet:renderURL var="backURL">
    <portlet:param name="mvcPath" value="/view.jsp" />
    <portlet:param name="userName" value="<%=requestedUser.getScreenName()%>" />
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
