<%@ include file="../init.jsp"%>

<portlet:actionURL name="/assignUsersToBranch" var="assignUsersToBranch" />

<portlet:renderURL var="assign_user">
	<portlet:param name="mvcRenderCommandName"
		value="/branch/assign_user_list" />
	<portlet:param name="branchId" value="${branchId}" />
</portlet:renderURL>

<!-- Loader Overlay -->
<div id="<portlet:namespace />loaderOverlay" class="loading-overlay" style="display: none;">
    <div class="loading-spinner">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden"></span>
        </div>
        <p class="mt-2">Assigning users, please wait...</p>
    </div>
</div>

<aui:form id="assignUsersForm">

	<aui:input type="hidden" name="branchId" value="${branchId}" />

	<div class="mb-3">
		<div class="d-flex align-items-center">
			<a href="<%=assign_user%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Select Users to Assign</h4>
		</div>

		<table class="table table-striped table-hover" id="userTable">
			<thead>
				<tr>
					<th><input type="checkbox" id="selectAllUsers" /></th>
					<th>Full Name</th>
					<th>Screen Name</th>
					<th>Email</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="user" items="${users}">
					<tr>
						<td><aui:input type="checkbox" label="" name="userIds"
								value="${user.userId}" cssClass="user-checkbox" /></td>
						<td>${user.fullName}</td>
						<td>${user.screenName}</td>
						<td>${user.emailAddress}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>

	<div class="text-end mt-3">
		<div class="text-end mt-3">
			<aui:button type="button" value="Cancel"
				onClick="Liferay.Util.getWindow().hide();"
				cssClass="btn btn-secondary" />
			<aui:button type="button" value="Assign" id="assignUsersBtn"
				name="assignUsersBtn" cssClass="btn btn-success" />
		</div>
	</div>

</aui:form>

<style>
.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
}

.loading-spinner {
    background: white;
    padding: 30px;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.loading-spinner p {
    margin: 0;
    color: #333;
    font-weight: 500;
}
</style>

<aui:script>

AUI().ready(function(A) {

    var dataTable = $('#userTable').DataTable({
        "pageLength": 10,
        "lengthMenu": [5, 10, 15, 20],
        "ordering": true,
        "searching": true,
        "language": {
            "search": "Filter records:"
        }
    });

    $('#selectAllUsers').on('click', function() {
        var rows = dataTable.rows({ 'search': 'applied' }).nodes();
        $('input.user-checkbox', rows).prop('checked', this.checked);
    });

    // Function to show loader
    function showLoader() {
        document.getElementById('<portlet:namespace />loaderOverlay').style.display = 'flex';
    }

    // Function to hide loader
    function hideLoader() {
        document.getElementById('<portlet:namespace />loaderOverlay').style.display = 'none';
    }

    // Function to show Liferay toast notification using modern method
    function showToast(message, type, title) {
        var toastType = type === 'success' ? 'success' : 
                       type === 'error' ? 'danger' : 
                       type === 'warning' ? 'warning' : 'info';
        
        var toastTitle = title || (type === 'success' ? 'Success' : 
                                type === 'error' ? 'Error' : 
                                type === 'warning' ? 'Warning' : 'Info');
        
        // Use Liferay.Util.openToast for modern Liferay versions
        if (Liferay.Util && Liferay.Util.openToast) {
            Liferay.Util.openToast({
                message: message,
                title: toastTitle,
                type: toastType,
                closeable: true,
                autoClose: 5000
            });
        } else {
            // Fallback for very old versions - use simple alert
            alert(toastTitle + ': ' + message);
        }
    }

    A.one('#<portlet:namespace />assignUsersBtn').on('click', function() {

        var branchId = A.one('[name="<portlet:namespace />branchId"]').get('value');
        var assignBtn = A.one('#<portlet:namespace />assignUsersBtn');

        var selectedUsers = [];
        dataTable.$('.user-checkbox:checked').each(function() {
            selectedUsers.push($(this).val());
        });

        if (selectedUsers.length === 0) {
            showToast('Please select at least one user.', 'warning', 'Selection Required');
            return;
        }

        // Show loader and disable button
        showLoader();
        assignBtn.set('disabled', true);
        assignBtn.set('value', 'Assigning...');

        A.io.request('${assignUsersToBranch}', {
            method: 'POST',
            data: {
                '<portlet:namespace />branchId': branchId,
                '<portlet:namespace />userIds': selectedUsers.join(',')
            },
            on: {
                success: function() {
                    hideLoader();
                    
                    // Show success toast
                    showToast('Users assigned to branch successfully!', 'success', 'Success');
                    
                    // Redirect after a short delay to allow user to see the success message
                    setTimeout(function() {
                        window.location.href = "${assign_user}";
                    }, 1500);
                },
                failure: function() {
                    hideLoader();
                    
                    // Show error toast
                    showToast('Failed to assign users to branch. Please try again.', 'error', 'Error');
                    
                    // Re-enable the assign button
                    assignBtn.set('disabled', false);
                    assignBtn.set('value', 'Assign');
                },
                exception: function() {
                    hideLoader();
                    
                    // Show error toast for exceptions
                    showToast('An unexpected error occurred. Please try again.', 'error', 'Error');
                    
                    // Re-enable the assign button
                    assignBtn.set('disabled', false);
                    assignBtn.set('value', 'Assign');
                }
            }
        });
    });
});
</aui:script>