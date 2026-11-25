<%@ include file="../init.jsp"%>
<%@ include file="../breadcrumb.jspf"%>

<portlet:renderURL var="addNBG">
	<portlet:param name="mvcPath" value="/organization/add-nbg.jsp" />
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
			<h4 class="mb-0">NBG Organizations</h4>
		</div>
		<a href="<%=addNBG%>"
			class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center"
			style="width: 40px; height: 40px;" title="Add NBG"> <i
			class="fa fa-plus"></i>
		</a>
	</div>

	<!-- Branch Table -->
	<div class="card shadow-sm">
		<div class="card-body p-0">
			<table id="nbgTable"
				class="table table-striped table-hover mb-0 align-middle">
				<thead class="table-dark">
					<tr>
						<th>#</th>
						<th>NBG Name</th>
						<th>Created Date</th>
						<th class="text-center">Actions</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="nbg" items="${nbgOrgs}" varStatus="loop">
						<tr>
							<td>${loop.index + 1}</td>
							<td>${nbg.name}</td>
							<td>${nbg.createDate}</td>
							<td class="text-center"><portlet:renderURL var="editNBG">
									<portlet:param name="id" value="${nbg.organizationId}" />
									<portlet:param name="mvcRenderCommandName" value="/edit-nbg" />
								</portlet:renderURL> <a href="<%=editNBG%>"
								class="btn btn-sm btn-outline-primary rounded-circle d-flex align-items-center justify-content-center me-2"
								style="width: 35px; height: 35px;" title="Edit"> <i
									class="fa fa-edit"></i>
							</a> 
						</tr>
					</c:forEach>

				</tbody>
			</table>
		</div>
	</div>
</div>


<aui:script>
$(document).ready(function() {
    $('#nbgTable').DataTable({
        "pageLength": 10,       // default rows per page
        "lengthMenu": [5, 10, 25, 50],
        "ordering": true,       // enable sorting
        "searching": true,      // enable search
        "language": {
            "search": "Filter records:",
             emptyTable: "No NBG Organization found."// custom search label
        }
    });
});
</aui:script>

