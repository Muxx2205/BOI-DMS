<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="com.liferay.portal.kernel.model.Organization"%>
<%@ include file="/init.jsp" %>



<portlet:renderURL var="entry_assignment">
    <portlet:param name="mvcPath" value="/entry_assignment.jsp" />
</portlet:renderURL>

<portlet:renderURL var="sync_pending_branch_data">
    <portlet:param name="mvcPath" value="/sync_pending_branch_data.jsp" />
</portlet:renderURL>



<%
User requestedUser = (User) request.getAttribute("requestedUser");
%>

<style>
/* --- Layout Card --- */
.page-wrapper {
  display: flex;
  justify-content: center;
  padding: 30px 15px;
  background: #f8fafc;
  min-height: calc(100vh - 120px);
}

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

/* --- Header --- */
.card-header {
 display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 12px 12px 0 0;
  padding: 16px 22px;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}
.card-header h3 {
   font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.card-header .note {
  font-size: 13px;
  color: #334155;
  background: #f1f5f9;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  font-weight: 500;
  letter-spacing: 0.3px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* --- Buttons --- */
.button-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 25px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 22px;
  text-decoration: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(180deg, #2563eb, #1e40af);
  color: #ffffff;
}
.btn-primary:hover {
  background: linear-gradient(180deg, #1d4ed8, #1e3a8a);
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.25);
}

.btn-secondary {
  background: #f1f5f9;
  color: #0f172a;
}
.btn-secondary:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.icon {
  width: 16px;
  height: 16px;
}

/* --- Form Section --- */
.form-container {
  margin-top: 30px;
  background: #f9fafb;
  border-radius: 12px;
  padding: 25px 30px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.05);
}

.form-container h4 {
  font-size: 18px;
  color: #1e293b;
  margin-bottom: 15px;
}

.aui-fieldset {
  border: none !important;
  padding: 0 !important;
}

.submit-btn {
  background: linear-gradient(180deg, #059669, #047857);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.submit-btn:hover {
  background: linear-gradient(180deg, #10b981, #065f46);
  transform: translateY(-2px);
}
</style>

<div class="page-wrapper">
  <div class="card">
    <div class="card-header">
      <div>
        <h3> DMS Support </h3>
        <div class="note"><%= themeDisplay.getUser().getScreenName()%></div>
      </div>
    </div>

    <div class="button-row">
      <c:if test="<%= Validator.isNull(requestedUser) %>">
        <portlet:actionURL name="fetchUser" var="fetchUser" />
        <div class="form-container">
          <h4>Fetch User Based on ADID</h4>
          <liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
          <liferay-ui:error key="requestFailed" message="Failed to process your request." />
          <liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Please try again." />
          <aui:form cssClass="custom-form" action="<%= fetchUser %>" method="post" name="myUserForm">
            <aui:fieldset>
              <aui:input name="userName" label="Staff ADID" required="true" cssClass="field-wrapper" />
              <aui:button type="submit" value="Submit" cssClass="submit-btn" />
            </aui:fieldset>
          </aui:form>
        </div>
       <a href="<%= entry_assignment %>" class="btn btn-secondary">
        Entry Assignment
      </a>

      <a href="<%= sync_pending_branch_data %>" class="btn btn-secondary">
        Sync Pending Branch Data
      </a>
      
               <%-- <portlet:renderURL var="multiple_records">
    <portlet:param name="mvcPath" value="/multiple_records.jsp" />
    
</portlet:renderURL>
        <a href="<%= multiple_records %>" class="btn btn-primary">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M3 21l3-1 11-11 2 2L8 22H3zM20.7 7.3l-2-2 1.4-1.4a1 1 0 011.4 1.4L20.7 7.3z"
              stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Bulk Access
        </a> --%>
      
      </c:if>

      <c:if test="<%= Validator.isNotNull(requestedUser) %>">
     		 <portlet:renderURL var="organization_assignment">
				    <portlet:param name="mvcPath" value="/organization_assignment.jsp" />
				     <portlet:param name="userName" value="<%=requestedUser.getScreenName()%>" />
			</portlet:renderURL>


			<portlet:renderURL var="role_assignment">
   	 			<portlet:param name="mvcPath" value="/role_assignment.jsp" />
     			<portlet:param name="userName" value="<%=requestedUser.getScreenName()%>" />
			</portlet:renderURL>


        <a href="<%= organization_assignment %>" class="btn btn-primary">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M3 21l3-1 11-11 2 2L8 22H3zM20.7 7.3l-2-2 1.4-1.4a1 1 0 011.4 1.4L20.7 7.3z"
              stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Organization Assignment
        </a>

        <a href="<%= role_assignment %>" class="btn btn-primary">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M3 21l3-1 11-11 2 2L8 22H3zM20.7 7.3l-2-2 1.4-1.4a1 1 0 011.4 1.4L20.7 7.3z"
              stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Role Assignment
        </a>
        

        
        
        
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
      </c:if>

     
    </div>
  </div>
</div>
		

<%-- <c:if test="<%=Validator.isNull(requestedUser)%>">
<portlet:actionURL name='fetchUser' var="fetchUser"/>
 <div class="center-wrapper">
    <div class="form-container">
        <h2>Fetch user based on ADID</h2>
        <!-- Liferay Messages -->
        <liferay-ui:success key="requestProcessed" message="Your request was processed successfully." />
        <liferay-ui:error key="requestFailed" message="Failed to process your request." />
        <liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Please try again." />
        <aui:form cssClass="custom-form" action="<%= fetchUser %>" method="post" name="myUserForm">
            <aui:fieldset>
                <aui:input name="userName" label="Staff ADID" required="true" cssClass="field-wrapper" />
                <aui:button type="submit" value="Submit" cssClass="submit-btn" />
            </aui:fieldset>
        </aui:form>
 
</div>
</c:if>

<c:if test="<%= Validator.isNotNull(requestedUser) %>">

    <div class="user-card card shadow-sm p-4 mt-3">
        <h2 class="text-primary mb-3">
              User: <%= requestedUser.getFullName() %>
        </h2>

        <%
            List<Role> userRoles = requestedUser.getRoles();
            List<Organization> organizationss = requestedUser.getOrganizations();
        %>

        <c:if test="<%= !userRoles.isEmpty() %>">
            <div class="user-section mb-4">
                <h4 class="text-success border-bottom pb-2">  Roles</h4>
                <ul class="list-group mt-2">
                    <% for (Role role : userRoles) { %>
                        <li class="list-group-item">
                            <i class="fas fa-user-tag text-muted mr-2"></i>
                            <%= role.getName() %>
                        </li>
                    <% } %>
                </ul>
            </div>
        </c:if>

        <c:if test="<%= !organizationss.isEmpty() %>">
            <div class="user-section">
                <h4 class="text-info border-bottom pb-2"> Organizations</h4>
                <ul class="list-group mt-2">
                    <% for (Organization organization : organizationss) { %>
                        <li class="list-group-item">
                            <i class="fas fa-building text-muted mr-2"></i>
                            <%= organization.getName() %>
                        </li>
                    <% } %>
                </ul>
            </div>
        </c:if>
    </div>
     <%@include file="/tabs.jsp" %> 
</c:if>
 --%>


 