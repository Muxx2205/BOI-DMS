<%@ include file="../init.jsp"%>

<portlet:actionURL name="/assignRoleToUser" var="addRoleToUser" />
<liferay-ui:success key="roleRemovedSuccessfully"
	message="Role has been removed successfully." />
<liferay-ui:error key="roleRemovalFailed"
	message="Failed to remove role. Please try again." />
<liferay-ui:error key="unexpectedError"
	message="An unexpected error occurred. Contact administrator." />

<portlet:renderURL var="assign_user">
	<portlet:param name="mvcRenderCommandName"
		value="/branch/assign_user_list" />
	<portlet:param name="branchId" value="${branchId}" />
</portlet:renderURL>

<div class="mb-3">
	<div class="d-flex align-items-center">
		<a href="<%=assign_user%>" class="btn btn-outline-secondary me-3"">
			<i class="fa fa-arrow-left me-1"></i> Back
		</a>
		<h4 class="mb-0">Assigned Roles</h4>
	</div>

	<c:if test="${not empty userInfo.roles}">
		<ul class="list-group">
			<c:forEach var="role" items="${userInfo.roles}">
				<li
					class="list-group-item d-flex justify-content-between align-items-center">

					<portlet:renderURL var="removeUserRole">
						<portlet:param name="userId" value="${userId}" />
						<portlet:param name="branchId" value="${branchId}" />
						<portlet:param name="roleId" value="${role.roleId}" />
						<portlet:param name="mvcRenderCommandName" value="/removeUserRole" />
					</portlet:renderURL> ${role.name} <aui:a href="${removeUserRole}"
						onClick="return confirm('Are you sure you want to remove this role?');"
						cssClass="btn btn-sm btn-danger">
                        Remove
                    </aui:a>
				</li>
			</c:forEach>
		</ul>
	</c:if>
	<c:if test="${empty userRoles}">
		<p>No roles assigned.</p>
	</c:if>
</div>

<hr />

<div class="mb-3">
	<h5>Assign New Role</h5>
	<aui:form action="<%=addRoleToUser%>" method="post">
		<aui:input type="hidden" name="userId" value="${userId}" />
		<aui:input type="hidden" name="branchId" value="${branchId}" />

		<aui:select name="roleId" label="Select Role" required="true">
			<aui:option value="">-- Select Role --</aui:option>
			<c:forEach var="role" items="${rolesToAssign}">
				<aui:option value="${role.roleId}">${role.name}</aui:option>
			</c:forEach>
		</aui:select>

		<div class="text-end mt-3">
			<aui:button type="button" value="Cancel"
				onClick="Liferay.Util.getWindow().hide();"
				cssClass="btn btn-secondary" />
			<aui:button type="submit" value="Assign" cssClass="btn btn-success" />
		</div>
	</aui:form>
</div>











