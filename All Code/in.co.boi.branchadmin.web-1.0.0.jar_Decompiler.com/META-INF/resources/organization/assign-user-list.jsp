<%@ include file="../init.jsp"%>

<portlet:renderURL var="assignUser">
	<portlet:param name="branchId" value="${branchId}" />
	<portlet:param name="mvcRenderCommandName" value="/branch/assign-user" />
</portlet:renderURL>
<portlet:renderURL var="viewBranch">
	<portlet:param name="mvcRenderCommandName" value="/view-branch" />
</portlet:renderURL>
<liferay-ui:success key="usersAssignedSuccessfully" message="User assign successfully." />
<liferay-ui:error key="userAssignmentFailed" message="Failed to assign user. Please try again." />
<liferay-ui:error key="unexpectedError" message="An unexpected error occurred. Contact administrator." />



<div class="container mt-4">

	<!-- Header Section -->
	<div class="d-flex justify-content-between align-items-center mb-3">
		<div class="d-flex align-items-center">
			<a href="<%=viewBranch%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Branch User Management</h4>
		</div>
		
		<a href="<%=assignUser%>"
			class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center"
			style="width: 40px; height: 40px;" title="Add Branch User"> <i
			class="fa fa-plus"></i>
		</a>
	</div>

	<!-- Table Card -->
	<div class="card shadow-sm">
		<div class="card-body p-0">
			<table id="branch-users-table"
				class="table table-striped table-hover mb-0 align-middle">
				<thead class="table-dark">
					<tr>
						<th scope="col">#</th>
						<th scope="col">Name</th>
						<th scope="col">Screen Name</th>
						<th scope="col">Email</th>
						<th scope="col">Role</th>
						<th scope="col" class="text-center">Actions</th>
					</tr>
				</thead>
				<tbody>

					<c:forEach var="branchUser" items="${branchUsers}" varStatus="loop">
						<tr>
							<td>${loop.index + 1}</td>
							<td>${branchUser.firstName}${branchUser.lastName}</td>
							<td>${branchUser.screenName}</td>
							<td>${branchUser.emailAddress}</td>
							<td><c:choose>
									<c:when test="${not empty branchUser.roles}">
										<c:forEach var="organizationsRole" items="${branchUser.roles}"
											varStatus="roleLoop">
                                                    ${organizationsRole.name}<c:if
												test="${!roleLoop.last}">, </c:if>
										</c:forEach>
									</c:when>
									<c:otherwise>
										<span class="text-muted">No Role</span>
									</c:otherwise>
								</c:choose></td>

							<td class="text-center">
								<!-- Assign Role Button --> <portlet:renderURL
									var="assignRoleUrl">
									<portlet:param name="userId" value="${branchUser.userId}" />
									<portlet:param name="branchId" value="${branchId}" />
									<portlet:param name="mvcRenderCommandName"
										value="/assignRoleToUser" />
								</portlet:renderURL> <a href="<%=assignRoleUrl%>"
								class="btn btn-warning btn-sm me-1"> <i
									class="fa fa-user-plus"></i> Assign Role
							</a> <!-- Delete User Button --> <portlet:actionURL
									var="deleteUserUrl" name="/deleteUserFromBranch">
									<portlet:param name="userId" value="${branchUser.userId}" />
									<portlet:param name="branchId" value="${branchId}" />
								</portlet:actionURL> <a href="<%=deleteUserUrl%>" class="btn btn-danger btn-sm"
								onclick="return confirm('Are you sure you want to remove this user from the branch?');">
									<i class="fa fa-trash"></i> Delete
							</a>
							</td>
						</tr>
					</c:forEach>

				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- DataTables Init -->
<aui:script>
    $(document).ready(function () {
        $('#branch-users-table').DataTable({
            pageLength: 10,
            lengthMenu: [5, 10, 25, 50],
            ordering: true,
            searching: true,
            language: {
                search: "Filter records:",
                emptyTable: "No users data found."
            }
        });
    });
</aui:script>
