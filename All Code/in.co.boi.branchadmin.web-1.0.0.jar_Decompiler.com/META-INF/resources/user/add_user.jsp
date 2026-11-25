<%@ include file="../init.jsp" %>

<portlet:actionURL name="/addUser" var="addUser" />


<portlet:renderURL var="branchAdmin">
	<portlet:param name="mvcRenderCommandName" value="/branch-admin.jsp" />
</portlet:renderURL>
<portlet:renderURL var="viewUser">
	<portlet:param name="mvcRenderCommandName" value="/view-users" />
</portlet:renderURL>

<div class="d-flex align-items-center">
			<a href="<%=viewUser%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Add User</h4>
		</div>

<aui:form id="addUserForm" action="<%= addUser %>" method="post">
<aui:input name="userId" type="hidden" value="${userInfo.userId}" />
    <!-- User Details -->
    <div class="mb-3">
        <aui:input name="firstName" label="First Name" value="${userInfo.firstName}" required="true"/>
    </div>
    <div class="mb-3">
        <aui:input name="lastName" label="Last Name" value="${userInfo.lastName}" required="true"/>
    </div>
    <div class="mb-3">
        <aui:input name="emailAddress" label="Email" value="${userInfo.emailAddress}" type="email" required="true"/>
    </div>
    <div class="mb-3">
        <aui:input name="screenName" label="Screen Name"  value="${userInfo.screenName}" required="true"/>
    </div>

    <div class="text-end mt-3">
        <aui:button id="addUserBtn" type="submit" value="Add User" cssClass="btn btn-success"/>
        
    </div>
</aui:form>


