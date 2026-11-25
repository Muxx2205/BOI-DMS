<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ include file="../init.jsp"%>
<%@ include file="../breadcrumb.jspf" %>
<portlet:renderURL var="branchAdmin">
	<portlet:param name="mvcRenderCommandName" value="/branch-admin.jsp" />
</portlet:renderURL>
<div class="container mt-4">

	<portlet:renderURL var="addUser">
		<portlet:param name="mvcPath" value="/user/add_user.jsp" />
	</portlet:renderURL>
	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-3">
		<div class="d-flex align-items-center">
			<a href="<%=branchAdmin%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Users</h4>
		</div>
	
		<a href="<%=addUser%>"
			class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center"
			style="width: 40px; height: 40px;" title="Add User"> <i
			class="fa fa-plus"></i>
		</a>
	</div>
	<!-- User Table -->
	<div class="card shadow-sm">
		<div class="card-body p-0">
			<table id="user-list" class="table table-striped table-hover mb-0">
				<thead class="table-dark">
					<tr>
						<th>#</th>
						<th>Full Name</th>
						<th>Screen Name</th>
						<th>Email</th>
						<th class="text-center">Actions</th>
					</tr>
				</thead>
				<tbody>
					<c:choose>
						<c:when test="${not empty users}">
							<c:forEach var="user" items="${users}" varStatus="loop">
								<tr>
									<td>${loop.index + 1}</td>
									<td>${user.fullName}</td>
									<td>${user.screenName}</td>
									<td>${user.emailAddress}</td>

									<td class="text-center">
										<div class="d-flex justify-content-center">

											<!-- Edit User Button -->
											<portlet:renderURL var="editUser">
												<portlet:param name="userId" value="${user.userId}" />
												<portlet:param name="mvcRenderCommandName" value="/editUser" />
											</portlet:renderURL>
											<a href="<%=editUser%>"
												class="btn btn-sm btn-outline-primary rounded-circle d-flex align-items-center justify-content-center me-2"
												style="width: 35px; height: 35px;" title="Edit User"> <i
												class="fa fa-edit"></i>
											</a>

											<!-- Delete User Button -->
											<portlet:actionURL name="/deleteUser" var="deleteUser">
												<portlet:param name="userId" value="${user.userId}" />
											</portlet:actionURL>
											<a href="<%=deleteUser%>"
												class="btn btn-sm btn-outline-danger rounded-circle d-flex align-items-center justify-content-center"
												style="width: 35px; height: 35px;"
												onclick="return confirm('Are you sure you want to delete this user?');"
												title="Delete User"> <i class="fa fa-trash"></i>
											</a>

										</div>
									</td>

								</tr>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<tr>
								<td colspan="5" class="text-center text-muted">No users
									found.</td>
							</tr>
						</c:otherwise>
					</c:choose>
				</tbody>
			</table>
		</div>
	</div>
</div>


<aui:script>
$(document).ready(function() {
    $('#user-list').DataTable({
        "pageLength": 10,       // default rows per page
        "lengthMenu": [5, 10, 25, 50],
        "ordering": true,       // enable sorting
        "searching": true,      // enable search
        "language": {
            "search": "Filter records:" // custom search label
        }
    });
});
</aui:script>
