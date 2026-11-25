<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ include file="../init.jsp"%>
<%@ include file="../breadcrumb.jspf"%>
<portlet:renderURL var="addBranch">
	<portlet:param name="mvcRenderCommandName" value="/add-branch" />
</portlet:renderURL>

<portlet:renderURL var="branchAdmin">
	<portlet:param name="mvcRenderCommandName" value="/branch-admin.jsp" />
</portlet:renderURL>

<div class="container mt-4">

	<!-- Header -->
	<div class="d-flex justify-content-between align-items-center mb-3">
		
		<div class="d-flex align-items-center">
			<a href="<%=branchAdmin%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Branch Organizations</h4>
		</div>
		
		<a href="<%=addBranch%>"
			class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center"
			style="width: 40px; height: 40px;" title="Add Branch"> <i
			class="fa fa-plus"></i>
		</a>
	</div>

	<!-- Branch Table -->
	<div class="card shadow-sm">
		<div class="card-body p-0">
			<table id="branchTable"
				class="table table-striped table-hover mb-0 align-middle">
				<thead class="table-dark">
					<tr>
						<th>#</th>
						<th>Branch Name</th>
						<th>Parent Zone</th>
						<th>Parent NBG</th>
						<th>Created Date</th>
						<th class="text-center">Actions</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="branch" items="${branchOrgs}" varStatus="loop">
						<tr>
							<td>${loop.index + 1}</td>
							<td>${branch.name}</td>
							<td><c:choose>
									<c:when test="${not empty branch.parentOrganization}">
                                                ${branch.parentOrganization.name}
                                            </c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose></td>
							<td><c:choose>
									<c:when
										test="${not empty branch.parentOrganization.parentOrganization}">
                                                ${branch.parentOrganization.parentOrganization.name}
                                            </c:when>
									<c:otherwise>-</c:otherwise>
								</c:choose></td>
							<td>${branch.createDate}</td>
							<td class="text-center">
								<div class="d-flex justify-content-center">
									<!-- Edit Branch Button -->
									<portlet:renderURL var="editBranch">
										<portlet:param name="id" value="${branch.organizationId}" />
										<portlet:param name="mvcRenderCommandName"
											value="/edit-branch" />
									</portlet:renderURL>
									<a href="<%=editBranch%>"
										class="btn btn-sm btn-outline-primary rounded-circle d-flex align-items-center justify-content-center me-2"
										style="width: 35px; height: 35px;" title="Edit Branch"> <i
										class="fa fa-edit"></i>
									</a>
									<portlet:renderURL var="assign_user">
										<portlet:param name="mvcRenderCommandName"
											value="/branch/assign_user_list" />
										<portlet:param name="branchId"
											value="${branch.organizationId}" />
									</portlet:renderURL>
									<!-- Assign User Button -->
									<a href="<%=assign_user%>"
										class="btn btn-sm btn-outline-success rounded-circle d-flex align-items-center assignUserBtn justify-content-center"
										style="width: 35px; height: 35px;" title="Assign User"> <i
										class="fa fa-user-plus"></i>
									</a>
								</div>
							</td>

						</tr>
					</c:forEach>


				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- Hidden Modal Container -->
<div id="assignUserModalContainer" style="display: none;"></div>





<style>
.btn i {
	transition: transform 0.2s ease-in-out;
}

.btn:hover i {
	transform: scale(1.2);
}
</style>

<aui:script>
$(document).ready(function() {
    $('#branchTable').DataTable({
        "pageLength": 10,       // default rows per page
        "lengthMenu": [5, 10, 25, 50],
        "ordering": true,       // enable sorting
        "searching": true,      // enable search
        "language": {
            "search": "Filter records:",
                emptyTable: "No branches found." // custom search label
        }
    });
});
</aui:script>

