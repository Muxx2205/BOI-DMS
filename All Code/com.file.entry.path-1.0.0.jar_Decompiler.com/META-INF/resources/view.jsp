<%@ include file="/init.jsp"%>

<%@ page import="java.util.List"%>

<%
List<String> paths =
    (List<String>)request.getAttribute("paths");

List<String> consoleLogs =
    (List<String>)request.getAttribute("consoleLogs");
%>

<portlet:renderURL var="objectURL">
	<portlet:param name="actionType" value="object" />
</portlet:renderURL>

<portlet:renderURL var="streamURL">
	<portlet:param name="actionType" value="stream" />
</portlet:renderURL>

<portlet:renderURL var="reuploadURL">
	<portlet:param name="actionType" value="reupload" />
</portlet:renderURL>

<portlet:renderURL var="clearLogsURL">
	<portlet:param name="actionType" value="clearLogs" />
</portlet:renderURL>

<div class="container-fluid mt-4">

	<!-- NAVIGATION -->
	<ul class="nav nav-tabs" role="tablist">

		<li class="nav-item"><a class="nav-link active"
			style="cursor: pointer;"
			onclick="<portlet:namespace />showTab('tab1', this)"> Generate
				Paths </a></li>

		<li class="nav-item"><a class="nav-link" style="cursor: pointer;"
			onclick="<portlet:namespace />showTab('tab2', this)"> Check
				Stream Size </a></li>

		<li class="nav-item"><a class="nav-link" style="cursor: pointer;"
			onclick="<portlet:namespace />showTab('tab3', this)"> ReUpload
				Existing File </a></li>

	</ul>

	<!-- TAB CONTENT -->
	<div class="tab-content border border-top-0 p-4 bg-white">

		<!-- TAB 1 -->
		<div class="tab-pane show active" id="<portlet:namespace />tab1">

			<h3>Generate Paths</h3>

			<form action="<%= objectURL %>" method="post">

				<div class="form-group">

					<label> Object Definition Id </label> <input class="form-control"
						name="<portlet:namespace />objectDefinitionId" type="number" />
				</div>

				<button class="btn btn-primary mt-3" type="submit">

					Generate Paths</button>
			</form>

			<hr />

			<div class="d-flex justify-content-between align-items-center">

				<h4>Generated Paths</h4>

				<button class="btn btn-success btn-sm"
					onclick="<portlet:namespace />copyAllPaths()" type="button">

					Copy All Paths</button>
			</div>

			<div id="<portlet:namespace />allPathsContainer" class="mt-3">

				<%
                if (paths != null &&
                    !paths.isEmpty()) {

                    for (String path : paths) {
                %>

				<div class="border rounded p-2 mb-2 bg-light path-item">

					<%= path %>

				</div>

				<%
                    }
                }
                else {
                %>

				<div class="text-muted">No Paths Generated</div>

				<%
                }
                %>

			</div>

		</div>

		<!-- TAB 2 -->
		<div class="tab-pane d-none" id="<portlet:namespace />tab2">

			<h3>Check InputStream Size</h3>

			<form action="<%= streamURL %>" method="post">

				<div class="form-group">

					<label> FileEntryId </label> <input class="form-control"
						name="<portlet:namespace />fileEntryId" type="number" />
				</div>

				<button class="btn btn-warning mt-3" type="submit">Check
					Stream</button>
			</form>

		</div>

		<!-- TAB 3 -->
		<div class="tab-pane d-none" id="<portlet:namespace />tab3">

			<h3>ReUpload Existing Content</h3>

			<form action="<%= reuploadURL %>" method="post">

				<div class="form-group">

					<label> FileEntryId </label> <input class="form-control"
						name="<portlet:namespace />fileEntryId" type="number" />
				</div>

				<button class="btn btn-danger mt-3" type="submit">ReUpload
				</button>
			</form>

		</div>

	</div>

	<hr />

	<!-- CONSOLE -->
	<div class="d-flex justify-content-between align-items-center mb-3">

		<h3>Console Logs</h3>

		<form action="<%= clearLogsURL %>" method="post">

			<button class="btn btn-secondary" type="submit">Clear
				Console</button>
		</form>

	</div>

	<div id="<portlet:namespace />consoleBox"
		style="background-color: #000; color: #00ff00; padding: 15px; height: 400px; overflow-y: auto; border-radius: 8px; font-family: monospace; font-size: 14px;">

		<%
        if (consoleLogs != null &&
            !consoleLogs.isEmpty()) {

            for (String log : consoleLogs) {
        %>

		<div class="mb-1">

			>
			<%= log %>

		</div>

		<%
            }
        }
        else {
        %>

		<div>No Logs Available</div>

		<%
        }
        %>

	</div>

</div>

<script>

function <portlet:namespace />showTab(tabId, element) {

    document.querySelectorAll('.tab-pane')
        .forEach(function(tab) {

            tab.classList.add('d-none');

            tab.classList.remove(
                'show',
                'active'
            );
        });

    document.querySelectorAll('.nav-link')
        .forEach(function(link) {

            link.classList.remove('active');
        });

    const selectedTab =
        document.getElementById(
            '<portlet:namespace />' + tabId
        );

    if (selectedTab) {

        selectedTab.classList.remove('d-none');

        selectedTab.classList.add(
            'show',
            'active'
        );
    }

    element.classList.add('active');
}

function <portlet:namespace />copyAllPaths() {

    const pathElements =
        document.querySelectorAll(
            '#<portlet:namespace />allPathsContainer .path-item'
        );

    let allPaths = '';

    pathElements.forEach(function(item) {

        allPaths += item.innerText + '\n';
    });

    navigator.clipboard.writeText(allPaths)
        .then(function() {

            alert('All Paths Copied Successfully');
        });
}

window.onload = function() {

    const consoleBox =
        document.getElementById(
            '<portlet:namespace />consoleBox'
        );

    if (consoleBox) {

        consoleBox.scrollTop =
            consoleBox.scrollHeight;
    }
};

</script>