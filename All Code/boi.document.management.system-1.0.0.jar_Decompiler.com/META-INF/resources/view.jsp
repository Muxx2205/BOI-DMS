<%@page import="com.liferay.portal.util.PropsValues"%>
<%@page import="javax.portlet.PortletSession"%>
<%@page import="boi.document.management.system.dto.DepartmentFileLimitsDTO"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@ page import="com.liferay.portal.kernel.util.Validator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.liferay.portal.kernel.util.GetterUtil"%>
<%@ include file="/init.jsp"%>

<link rel="stylesheet"
	href="<%= request.getContextPath() %>/css/main.css" />
<!--<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">-->

<%

Map<Long, String> folderIdNameMap = (Map<Long, String>) request.getAttribute("folderIdNameMap");
String userScreenName = (String) request.getAttribute("userScreenName");
//String archiveEndpoint = (String) request.getAttribute("archiveEndpoint");
//String dmsEndpoint = (String) request.getAttribute("dmsEndPoint");
//String archiveSecretId = (String) request.getAttribute("archiveSecretId");
//String archiveClientId = (String) request.getAttribute("archiveClientId");

boolean canViewHistory = GetterUtil.getBoolean(request.getAttribute("canViewHistory"), false);
boolean canEdit = GetterUtil.getBoolean(request.getAttribute("canEdit"), false);

if (folderIdNameMap == null) {
    folderIdNameMap = new HashMap<Long, String>();
}

long maxFiles = GetterUtil.getLong(
        portletSession.getAttribute("MAX_FILES", PortletSession.PORTLET_SCOPE),
       10L
);

long maxFileSize = GetterUtil.getLong(
        portletSession.getAttribute("MAX_FILE_SIZE", PortletSession.PORTLET_SCOPE),
        10L 
);

long maxTotalSize = GetterUtil.getLong(
        portletSession.getAttribute("MAX_TOTAL_SIZE", PortletSession.PORTLET_SCOPE),
        100L
);

int folderCur = ParamUtil.getInteger(renderRequest, "folderCur", 1);
int folderDelta = ParamUtil.getInteger(renderRequest, "folderDelta", 10);
int docCur = ParamUtil.getInteger(renderRequest, "docCur", 1);
int docDelta = ParamUtil.getInteger(renderRequest, "docDelta", 20);

boolean haveArchiveAccess = GetterUtil.getBoolean(request.getAttribute("haveArchiveAccess"), false);
%>

<liferay-portlet:resourceURL id="getSubFoldersAndDocs"
	var="getSubFoldersAndDocsURL" />
<liferay-portlet:resourceURL id="getFolderHistory"
	var="getFolderHistoryURL" />
<liferay-portlet:resourceURL id="getDocumentHistory"
	var="getDocumentHistoryURL" />
<liferay-portlet:resourceURL id="deleteFolder" var="deleteFolderURL" />
<liferay-portlet:resourceURL id="deleteDocument" var="deleteDocumentURL" />
<liferay-portlet:resourceURL id="getDocumentDetails"
	var="getDocumentDetailsURL" />
<liferay-portlet:resourceURL id="getItemsForMultiDelete"
	var="getItemsForMultiDeleteURL" />
<liferay-portlet:resourceURL id="multiDeleteItems"
	var="multiDeleteItemsURL" />
<liferay-portlet:resourceURL id="/archive/search-documents"
	var="archiveSearchDocumentsURL" />
<liferay-portlet:resourceURL id="/archive/download-document"
	var="archiveDownloadDocumentURL" />

<liferay-portlet:renderURL var="baseRenderURL">
	<liferay-portlet:param name="folderDelta"
		value="<%= String.valueOf(folderDelta) %>" />
	<liferay-portlet:param name="docCur"
		value="<%= String.valueOf(docCur) %>" />
	<liferay-portlet:param name="folderCur"
		value="<%= String.valueOf(folderCur) %>" />
	<liferay-portlet:param name="docDelta"
		value="<%= String.valueOf(docDelta) %>" />
</liferay-portlet:renderURL>

<div class="container-fluid custom-document-library">
	<liferay-portlet:resourceURL id="createFolderResource"
		var="createFolderURL" />
	<liferay-portlet:resourceURL id="createDocument"
		var="createDocumentURL" />

	<div class="document-library-container">

		<% 

boolean isHavingFullAccess = Boolean.TRUE.equals(request.getAttribute("isHavingFullAccess"));
boolean isHavingAddUpdateAction = isHavingFullAccess || Boolean.TRUE.equals(request.getAttribute("isHavingAddUpdateAction"));
boolean isHavingDeleteAction = isHavingFullAccess || Boolean.TRUE.equals(request.getAttribute("isHavingDeleteAction"));
boolean isHavingPreviewAction = isHavingFullAccess || Boolean.TRUE.equals(request.getAttribute("isHavingPreviewAction"));
boolean isShowingDownload = true;

if(isHavingAddUpdateAction || isHavingDeleteAction){
%>
		<div class="fab-container">
			<button class="fab-btn" id="fabMain">
				<i class="fa fa-plus fa-md"></i>
			</button>
			<div class="fab-menu">
				<%if(isHavingAddUpdateAction){ %>
				<button class="fab-option" onclick="showCreateFolderModal()"
					data-tooltip="Create Folder">
					<i class="fa fa-folder-o fa-md"></i>
				</button>
				<button class="fab-option" onclick="showCreateDocumentModal()"
					data-tooltip="Upload Documents">
					<i class="fa fa-upload fa-md"></i>
				</button>
				<%} if(isHavingDeleteAction){%>
				<button class="fab-option" onclick="showMultiDeleteModal()"
					data-tooltip="Multi Delete">
					<i class="fa fa-trash-o fa-md"></i>
				</button>
				<%} %>
			</div>
		</div>
		<%} %>

		<!-- Create Folder Modal -->
		<div class="modal-overlay" id="createFolderModal">
			<div class="modal-container">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-folder-o"></i>
					</div>
					<div class="modal-title">
						<h3>Create New Folder</h3>
					</div>
					<button class="modal-close" onclick="closeCreateFolderModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>

				<div id="errorFolderModelContainer" class="error-model-container">
					<div class="error-model-message">
						<i class="fa fa-exclamation-triangle"></i> <span
							id="errorFolderModelMessage"></span>
					</div>
				</div>

				<div class="modal-body">
					<form id="createFolderForm" class="modern-form">
						<div class="form-group">
							<label class="form-label">Folder Name</label>
							<div class="input-with-icon">
								<i class="fa fa-folder"></i> <input type="text"
									class="form-input" id="folderName" name="folderName"
									placeholder="Enter folder name" maxlength="75" required>
							</div>
							<div class="form-helper">Maximum 75 characters allowed</div>
						</div>
						<div class="form-group">
							<label class="form-label">Description <span
								class="optional">(Optional)</span></label>
							<div class="input-with-icon">
								<i class="fa fa-align-left"></i>
								<textarea class="form-textarea" id="folderDescription"
									name="folderDescription" rows="3"
									placeholder="Describe what this folder contains..."
									maxlength="255"></textarea>
							</div>
							<div class="form-helper">Maximum 255 characters allowed</div>
						</div>
						<input type="hidden" id="parentFolderId" name="parentFolderId"
							value="<%= ParamUtil.getInteger(renderRequest, "folderId", 1) %>">
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-secondary"
						onclick="closeCreateFolderModal()">Cancel</button>
					<button class="btn btn-primary" onclick="submitCreateFolder()">
						<i class="fa fa-plus"></i> Create Folder
					</button>
				</div>
			</div>
		</div>

		<!-- Create Document Modal -->
		<div class="modal-overlay" id="createDocumentModal">
			<div class="modal-container">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-upload"></i>
					</div>
					<div class="modal-title">
						<h3>Upload Documents</h3>
					</div>
					<button class="modal-close" onclick="closeCreateDocumentModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>

				<div id="errorDocumentModelContainer" class="error-model-container">
					<div class="error-model-message">
						<i class="fa fa-exclamation-triangle"></i> <span
							id="errorDocumentModelMessage"></span>
					</div>
				</div>

				<div class="modal-body">
					<form id="createDocumentForm" class="modern-form"
						enctype="multipart/form-data">
						<div class="form-group">
							<label class="form-label">Select Files</label>
							<div class="file-upload-area" id="fileUploadArea">
								<div class="file-upload-content">
									<i class="fa fa-cloud-upload"></i>
									<h4>Drag & Drop your files here</h4>
									<p>or click to browse multiple files</p>
									<span class="file-types"> Allowed files: PDF, DOC, DOCX,
										XLS, XLSX, TXT, Images &mdash; Up to <strong><%= maxFiles %></strong>
										files &bull; <strong><%= maxFileSize %></strong> MB per file
										&bull; <strong><%= maxTotalSize %></strong> MB total upload
										limit
									</span>
								</div>
								<input type="file" class="file-input" id="documentFiles"
									name="documentFiles" multiple
									accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.jpg,.jpeg,.png,.gif,.tif,.tiff">
							</div>
							<div class="files-preview" id="filesPreview"
								style="display: none;">
								<h6 class="mb-3">
									Selected Files (<span id="filesCount">0</span>)
								</h6>
								<div class="files-actions mb-2">
									<button type="button" class="btn btn-sm btn-outline-danger"
										onclick="removeAllFiles()">
										<i class="fa fa-trash"></i> Remove All
									</button>
								</div>
								<div id="filesList" class="files-list"></div>
							</div>
						</div>
						<input type="hidden" id="documentParentFolderId"
							name="parentFolderId"
							value="<%= ParamUtil.getInteger(renderRequest, "folderId", 1) %>">
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-secondary"
						onclick="closeCreateDocumentModal()">Cancel</button>
					<button class="btn btn-primary" onclick="submitCreateDocument()">
						<i class="fa fa-upload"></i> Upload All Documents
					</button>
				</div>
			</div>
		</div>

		<!-- History Modal -->
		<div class="modal-overlay" id="historyModal" style="display: none;">
			<div class="modal-container" style="max-width: 700px;">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-history"></i>
					</div>
					<div class="modal-title">
						<h3 id="historyModalTitle">History</h3>
					</div>
					<button class="modal-close" onclick="closeHistoryModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>
				<div class="modal-body">
					<div class="timeline-container" id="historyTimeline">
						<!-- History content will be loaded here -->
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-secondary" onclick="closeHistoryModal()">
						Close</button>
				</div>
			</div>
		</div>

		<!-- Delete Folder Confirmation Modal -->
		<div class="modal-overlay" id="deleteFolderModal"
			style="display: none;">
			<div class="modal-container" style="max-width: 500px;">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-trash text-danger"></i>
					</div>
					<div class="modal-title">
						<h3>Delete Folder</h3>
					</div>
					<button class="modal-close" onclick="closeDeleteFolderModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>
				<div class="modal-body">
					<div class="text-center mb-4">
						<i class="fa fa-exclamation-triangle fa-3x text-warning mb-3"></i>
						<h4 id="deleteFolderTitle">Are you sure?</h4>
						<p id="deleteFolderMessage" class="text-muted"></p>
						<p class="text-danger">
							<strong>Warning:</strong> This action cannot be undone. All
							documents inside this folder will also be deleted.
						</p>
					</div>
					<div class="form-group">
						<label class="form-label">Type "DELETE" to confirm:</label> <input
							type="text" class="form-input" id="deleteFolderConfirm"
							placeholder="Type DELETE here"
							onkeyup="validateDeleteConfirmation()">
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-secondary"
						onclick="closeDeleteFolderModal()">Cancel</button>
					<button class="btn btn-danger" id="deleteFolderBtn"
						onclick="confirmDeleteFolder()" disabled>
						<i class="fa fa-trash mr-2"></i> Delete Folder
					</button>
				</div>
			</div>
		</div>

		<!-- Delete Document Confirmation Modal -->
		<div class="modal-overlay" id="deleteDocumentModal"
			style="display: none;">
			<div class="modal-container" style="max-width: 500px;">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-trash text-danger"></i>
					</div>
					<div class="modal-title">
						<h3>Delete Document</h3>
					</div>
					<button class="modal-close" onclick="closeDeleteDocumentModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>
				<div class="modal-body">
					<div class="text-center mb-4">
						<i class="fa fa-exclamation-triangle fa-3x text-warning mb-3"></i>
						<h4 id="deleteDocumentTitle">Are you sure?</h4>
						<p id="deleteDocumentMessage" class="text-muted"></p>
						<p class="text-danger">
							<strong>Warning:</strong> This action cannot be undone.
						</p>
					</div>
					<div class="form-group">
						<label class="form-label">Type "DELETE" to confirm:</label> <input
							type="text" class="form-input" id="deleteDocumentConfirm"
							placeholder="Type DELETE here"
							onkeyup="validateDeleteDocumentConfirmation()">
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-secondary"
						onclick="closeDeleteDocumentModal()">Cancel</button>
					<button class="btn btn-danger" id="deleteDocumentBtn"
						onclick="confirmDeleteDocument()" disabled>
						<i class="fa fa-trash mr-2"></i> Delete Document
					</button>
				</div>
			</div>
		</div>

		<!-- Multi Delete Modal -->
		<div class="modal-overlay" id="multiDeleteModal"
			style="display: none;">
			<div class="modal-container multi-delete-modal">
				<div class="modal-header">
					<div class="modal-icon">
						<i class="fa fa-trash text-danger"></i>
					</div>
					<div class="modal-title">
						<h3>Bulk Delete</h3>
					</div>
					<button class="modal-close" onclick="closeMultiDeleteModal()">
						<i class="fa fa-times"></i>
					</button>
				</div>

				<div class="modal-body">
					<!-- Search and Filter Section -->
					<div class="multi-delete-controls mb-4">
						<div
							class="d-flex flex-md-row justify-content-between align-items-md-center gap-3">
							<!-- Search Box -->
							<div class="search-container col-7"
								style="flex: 1; min-width: 250px;">
								<div class="input-with-icon search-input">
									<i class="fa fa-search"></i> <input type="text"
										class="form-input search-box" id="multiDeleteSearch"
										placeholder="Search items by name..."
										onkeyup="filterMultiDeleteItems()">
									<button class="fa fa-times search-clear"
										onclick="clearSearch()" style="display: none;"></button>
								</div>
							</div>

							<!-- Selection Stats -->
							<div class="selection-stats d-flex align-items-center gap-3">
								<div class="stats-badge">
									<span class="badge badge-selected" id="selectedCount">0
										selected</span> <span class="badge badge-total ml-2" id="totalCount">0
										total</span>
								</div>
								<div class="selection-actions">
									<button class="btn btn-sm btn-outline-light"
										onclick="selectAllItems()">
										<i class="fa fa-check-square mr-1"></i> Select All
									</button>
									<button class="btn btn-sm btn-outline-light ml-2"
										onclick="deselectAllItems()">
										<i class="fa fa-square mr-1"></i> Clear All
									</button>
								</div>
							</div>
						</div>
					</div>

					<!-- Filter Tabs -->
					<div class="multi-delete-filters mb-4">
						<div class="filter-tabs">
							<button type="button" class="filter-tab active" data-type="all"
								onclick="filterByType('all')">
								<i class="fa fa-th-list mr-2"></i> All Items
							</button>
							<button type="button" class="filter-tab" data-type="folder"
								onclick="filterByType('folder')">
								<i class="fa fa-folder mr-2"></i> Folders <span
									class="filter-count" id="folderCountBadge">0</span>
							</button>
							<button type="button" class="filter-tab" data-type="document"
								onclick="filterByType('document')">
								<i class="fa fa-file mr-2"></i> Documents <span
									class="filter-count" id="documentCountBadge">0</span>
							</button>
						</div>
					</div>

					<!-- Items Grid -->
					<div class="multi-delete-items-container">
						<div class="items-grid-header">
							<div class="grid-checkbox">
								<div class="custom-control custom-checkbox">
									<input type="checkbox" class="custom-control-input"
										id="selectAllCheckbox" onchange="toggleSelectAll(this)">
									<label class="custom-control-label" for="selectAllCheckbox"></label>
								</div>
							</div>
							<div class="grid-header-name">Name</div>
							<div class="grid-header-type">Type</div>
							<div class="grid-header-size">Size</div>
							<div class="grid-header-date">Modified</div>
						</div>

						<div class="items-grid-body" id="multiDeleteItemsGrid">
							<!-- Items will be loaded here -->
						</div>

						<!-- Empty States -->
						<div class="empty-state" id="multiDeleteEmpty"
							style="display: none;">
							<div class="empty-icon">
								<i class="fa fa-folder-open fa-3x"></i>
							</div>
							<div class="empty-text">
								<h4>No items found</h4>
								<p>There are no items in this folder to delete.</p>
							</div>
						</div>

						<div class="loading-state" id="multiDeleteLoading">
							<div class="spinner">
								<div class="spinner-dot"></div>
								<div class="spinner-dot"></div>
								<div class="spinner-dot"></div>
							</div>
							<p>Loading items...</p>
						</div>
					</div>

					<!-- Summary and Confirmation -->
					<div class="multi-delete-summary mt-4">
						<div class="summary-card">
							<div class="summary-header">
								<h5>
									<i class="fa fa-info-circle mr-2"></i>Deletion Summary
								</h5>
							</div>
							<div class="summary-content">
								<div class="row">
									<div class="col-lg-6">
										<div class="summary-details" id="summaryDetails">
											<div class="detail-placeholder">
												<i class="fa fa-mouse-pointer"></i>
												<p>Select items to see deletion summary</p>
											</div>
										</div>
									</div>
									<div class="col-lg-6">
										<div class="confirmation-section">
											<div class="form-group mb-3">
												<label class="form-label"> <i
													class="fa fa-exclamation-triangle text-warning mr-1"></i>
													Type "DELETE ALL" to confirm:
												</label>
												<div class="input-with-icon">
													<i class="fa fa-key text-muted"></i> <input type="text"
														class="form-input" id="multiDeleteConfirm"
														placeholder="Type DELETE ALL here"
														onkeyup="validateMultiDeleteConfirmation()">
												</div>
												<small class="form-text text-muted"> This action
													will permanently delete all selected items. </small>
											</div>

											<div class="warning-box" style="display: none;"
												id="folderWarning">
												<i class="fa fa-exclamation-circle"></i> <span>Deleting
													folders will also remove all contents inside them.</span>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>

				<div class="modal-footer">
					<button class="btn btn-outline-secondary"
						onclick="closeMultiDeleteModal()"
						style="background: darkturquoise;">
						<i class="fa fa-times mr-2"></i>Cancel
					</button>
					<button class="btn btn-danger btn-delete-all" id="multiDeleteBtn"
						onclick="confirmMultiDelete()" disabled>
						<i class="fa fa-trash mr-2"></i> Delete Selected <span
							class="delete-count" id="deleteCount">0</span>
					</button>
				</div>
			</div>
		</div>

		<!-- Success Notification -->
		<div class="notification-container" id="notificationContainer">
			<div class="notification success">
				<div class="notification-icon">
					<i class="fa fa-check-circle"></i>
				</div>
				<div class="notification-content">
					<h4>Success!</h4>
					<p id="notificationMessage"></p>
				</div>
				<button class="notification-close" onclick="hideNotification()">
					<i class="fa fa-times"></i>
				</button>
			</div>
		</div>

		<!-- Error Container -->
		<div id="globalErrorContainer" class="error-container"
			style="display: none;">
			<div class="error-message">
				<i class="fa fa-exclamation-triangle"></i> <span
					id="globalErrorMessage"></span>
			</div>
		</div>

		<div class="row portlet-header mb-4">
			<h2 class="col-8 mb-0">
				<i class="icon-documents"></i> BOI Document Management System : <span
					class="text-primary"><%= HtmlUtil.escape(userScreenName != null ? userScreenName.toUpperCase() : "") %></span>
			</h2>
			<!-- ARCHIVE DASHBOARD BUTTON - CONDITIONAL DISPLAY -->
			<% if (haveArchiveAccess) { %>
			<div class="col-4 archive-dashboard-tab text-right">
				<a href="javascript:void(0)" onclick="switchToArchiveDashboard()"
					class="btn btn-outline-primary archive-switch-btn"
					id="archiveDashboardBtn"> <i class="fa fa-archive mr-2"></i>
					Switch to Archive Dashboard
				</a>
			</div>
			<% } %>
		</div>

		<nav aria-label="breadcrumb">
			<ol class="breadcrumb" id="breadcrumbTrail">
				<li class="breadcrumb-item"><a
					href="/web/boi-dms/boi-document-management"
					onclick="window.location.reload(true)">BOI Home</a></li>
			</ol>
		</nav>

		<div class="loader-backdrop" id="loaderBackdrop"></div>

		<div id="loader">
			<div class="loader-content">
				<div class="spinner-border text-primary" role="status">
					<span class="visually-hidden"></span>
				</div>
				<div class="loader-text">Please Wait ...</div>
			</div>
		</div>

		<!-- Folders Section -->
		<div class="folders-section" id="foldersSection">
			<div class="d-flex justify-content-between align-items-center mb-3">
				<h4 class="text-secondary mt-3 mb-0">
					<i class="bi bi-folder text-warning"></i> Folders
				</h4>
				<div>
					<span class="badge badge-secondary" id="folderCount">0</span> <select
						class="form-control-sm ml-2" id="folderDeltaSelect"
						onchange="changeFolderDelta(this.value)">
						<option value="5" <%= folderDelta == 5 ? "selected" : "" %>>5
							per page</option>
						<option value="10" <%= folderDelta == 10 ? "selected" : "" %>>10
							per page</option>
						<option value="20" <%= folderDelta == 20 ? "selected" : "" %>>20
							per page</option>
					</select>
				</div>
			</div>

			<div id="folderResults">
				<div class="row" id="folderGrid">
					<% if (folderIdNameMap.isEmpty()) { %>
					<div class="col-12">
						<div class="alert alert-info">
							<i class="icon-info-sign"></i> No folders found for your account.
						</div>
					</div>
					<% } else { 
                    for (Map.Entry<Long, String> entry : folderIdNameMap.entrySet()) { 
                        String folderName = entry.getValue();
                        String displayName = getDisplayName(folderName, 25);
                %>
					<div class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">
						<div class="card card-horizontal folder-card h-100">
							<div class="card-body p-3">
								<div class="d-flex align-items-center justify-content-between">
									<div class="d-flex align-items-center folder-content"
										onclick="navigateToFolder(<%= entry.getKey() %>, '<%= HtmlUtil.escape(folderName.replace("'", "\\'")) %>')"
										style="cursor: pointer; flex-grow: 1; min-width: 0;">
										<div class="flex-shrink-0 mr-3">
											<span class="sticker sticker-secondary"> <span
												class="sticker-overlay"> <svg
														class="lexicon-icon lexicon-icon-folder"
														role="presentation" width="24" height="24">
                                                            <use
															href="/o/admin-theme/images/clay/icons.svg#folder"></use>
                                                        </svg>
											</span>
											</span>
										</div>
										<div class="flex-grow-1 text-truncate" style="min-width: 0;">
											<p class="card-title mb-0"
												title="<%= HtmlUtil.escapeAttribute(folderName) %>">
												<span class="text-truncate-inline"
													data-full-text="<%= HtmlUtil.escape(folderName) %>">
													<%= HtmlUtil.escape(displayName) %>
												</span>
											</p>
											<small class="text-muted d-block text-truncate"
												style="font-size: 0.75rem;"> <strong>Creator:</strong>
												<%= HtmlUtil.escape(userScreenName) %><br> <strong>Date:</strong>
												<%= new java.util.Date().toString() %>
											</small>
										</div>
									</div>

									<!-- Three-dot menu integrated in the card row -->
									<% if (canViewHistory || isHavingDeleteAction) { %>
									<div class="dropdown folder-actions"
										style="position: relative;">
										<button
											class="btn btn-sm btn-icon btn-link text-muted dropdown-toggle three-dot-btn"
											type="button" onclick="toggleDropdown(event, this)">
											<i class="fa fa-ellipsis-v"></i>
										</button>
										<div class="dropdown-menu dropdown-menu-right shadow-sm">
											<% if (canViewHistory) { %>
											<a class="dropdown-item" href="javascript:void(0)"
												onclick="showFolderHistory(<%= entry.getKey() %>, '<%= HtmlUtil.escape(folderName.replace("'", "\\'")) %>')">
												<i class="fa fa-history mr-2 text-muted"></i> History
											</a>
											<% } %>
											<a class="dropdown-item" href="javascript:void(0)"
												onclick="navigateToFolder(<%= entry.getKey() %>, '<%= HtmlUtil.escape(folderName.replace("'", "\\'")) %>')">
												<i class="fa fa-folder-open mr-2 text-muted"></i> Open
											</a>
											<% if (isHavingDeleteAction) { %>
											<div class="dropdown-divider"></div>
											<a class="dropdown-item text-danger"
												href="javascript:void(0)"
												onclick="showDeleteFolderModal(<%= entry.getKey() %>, '<%= HtmlUtil.escape(folderName.replace("'", "\\'")) %>')">
												<i class="fa fa-trash mr-2"></i> Delete
											</a>
											<% } %>
										</div>
									</div>
									<% } %>
								</div>
							</div>
						</div>
					</div>
					<% } } %>
				</div>
			</div>

			<!-- Folder Pagination -->
			<div class="folder-pagination-container mt-4"
				id="folderPaginationContainer" style="display: none;">
				<nav aria-label="Folder pagination">
					<ul class="pagination justify-content-center" id="folderPagination">

					</ul>
				</nav>
			</div>
		</div>

		<!-- Documents Section -->
		<div class="documents-section" id="documentsSection"
			style="display: none;">
			<div class="d-flex justify-content-between align-items-center mb-3">
				<h4 class="text-secondary mt-4 mb-0">
					<i class="bi bi-file-earmark-text text-primary"></i> Documents
				</h4>
				<div class="d-flex align-items-center">
					<span class="badge badge-secondary mr-2" id="documentCount">0</span>

					<!-- Regular document page size selector -->
					<select class="form-control-sm mr-2" id="docDeltaSelect"
						onchange="changeDocDelta(this.value)">
						<option value="10" <%= docDelta == 10 ? "selected" : "" %>>10
							per page</option>
						<option value="20" <%= docDelta == 20 ? "selected" : "" %>>20
							per page</option>
						<option value="50" <%= docDelta == 50 ? "selected" : "" %>>50
							per page</option>
					</select>

					<!-- Archive page size selector (hidden by default) -->
					<% if (haveArchiveAccess) { %>
					<select class="form-control-sm" id="archiveDeltaSelect"
						style="display: none;"
						onchange="changeArchivePageSize(this.value)">
						<option value="10">10 per page</option>
						<option value="20" selected>20 per page</option>
						<option value="50">50 per page</option>
						<option value="100">100 per page</option>
					</select>
					<% } %>

					<!-- Page info display for archive -->
					<span class="archive-page-info ml-3" id="archivePageInfo"
						style="display: none; color: #6c757d; font-size: 0.9rem;">
						Showing <span id="archiveStartItem">0</span>-<span
						id="archiveEndItem">0</span> of <span id="archiveTotalItems">0</span>
					</span>
				</div>
			</div>

			<div id="documentResults">
				<div class="row" id="documentGrid">
					<!-- Documents will be loaded here -->
				</div>
			</div>

			<!-- Document Pagination Container (used for both regular and archive documents) -->
			<div class="document-pagination-container mt-4"
				id="documentPaginationContainer" style="display: none;">
				<nav aria-label="Document pagination">
					<ul class="pagination justify-content-center"
						id="documentPagination">

					</ul>
				</nav>
			</div>
		</div>

		<!-- Page Error Container -->
		<div class="alert alert-danger" id="pageErrorContainer"
			style="display: none;">
			<i class="icon-warning-sign"></i> <span id="pageErrorMessage"></span>
		</div>
	</div>

	<%!

private String getDisplayName(String name, int maxLength) {
    if (name == null) return "";
    if (name.length() <= maxLength) return name;
    
    int lastSpace = name.lastIndexOf(' ', maxLength - 3);
    if (lastSpace > 0 && lastSpace > maxLength / 2) {
        return name.substring(0, lastSpace) + "...";
    }
    
    return name.substring(0, maxLength - 3) + "...";
}
%>

	<script type="text/javascript">
//============================================================================
//GLOBAL VARIABLES
//============================================================================
let currentDeleteFolderId = null;
let currentDeleteFolderName = null;
let currentDeleteDocumentId = null;
let currentDeleteDocumentName = null;
let multiDeleteItems = [];
let selectedItems = [];
let currentFilterType = 'all';

let breadcrumbStack = [];
let currentFolderId = 0;
let currentFolderPage = <%= folderCur %>;
let currentDocPage = <%= docCur %>;
let folderPageSize = <%= folderDelta %>;
let docPageSize = <%= docDelta %>;


let isArchiveMode = false;
let archiveAccessToken = null;
let archiveCurrentPage = 1;
let archivePageSize = 20;
let archiveTotalItems = 0;

let haveArchiveAccess = <%= haveArchiveAccess %>;

<%-- const archiveEndpoint = '<%= archiveEndpoint %>';
const dmsEndPoint = '<%= dmsEndpoint %>'; --%>
<%-- const archiveClientId = '<%= archiveClientId %>';
const archiveSecretId = '<%= archiveSecretId %>'; --%>

const DEBUG_MODE = true; 
const eventListeners = [];

//============================================================================
//ARCHIVE DASHBOARD FUNCTIONS
//============================================================================
async function switchToArchiveDashboard() {
    console.log('=== switchToArchiveDashboard called ===');
    
   
    if (!haveArchiveAccess) {
        showError('You do not have permission to access the Archive Dashboard');
        return;
    }
    
   
    if (!currentFolderId || currentFolderId <= 0) {
        showError('Please navigate to a folder first to view archive');
        return;
    }
    
    if (!isArchiveMode) {
        showLoader();
        
        try {
           
            archiveCurrentPage = 1;
            
           
          //  console.log('Fetching access token...');
         //   archiveAccessToken = await getAuthToken();
          //  console.log('Access token received');
            
            
            console.log('Loading archive documents for folder:', currentFolderId);
            await loadArchiveDocuments(currentFolderId, archiveCurrentPage, archivePageSize);
            
           
            document.getElementById('archiveDashboardBtn').innerHTML = 
                '<i class="fa fa-folder mr-2"></i>Switch to Main Dashboard';
            document.getElementById('archiveDashboardBtn').classList.add('active');
            
           
            isArchiveMode = true;
            
           
            document.getElementById('docDeltaSelect').style.display = 'none';
            document.getElementById('archiveDeltaSelect').style.display = 'inline-block';
            
           
            document.getElementById('archivePageInfo').style.display = 'inline-block';
            
          
            const portletHeader = document.querySelector('.portlet-header h2');
            if (portletHeader) {
                const currentFolderName = getFolderNameFromBreadcrumb() || 'Unknown Folder';
                portletHeader.innerHTML = 
                    '<i class="fa fa-archive mr-2"></i>' +
                    'Archive Dashboard - ' + currentFolderName + 
                    ' <span class="archive-badge">Archive</span>';
            }
            
           
            showSuccessMessage('Switched to Archive Dashboard');
            
        } catch (error) {
            hideLoader();
            console.error('Archive switch error:', error);
            showError('Failed to switch to Archive Dashboard: ' + error.message);
        }
    } else {
        switchToMainDashboard();
    }
}

function switchToMainDashboard() {
    console.log('=== switchToMainDashboard called ===');
    
    isArchiveMode = false;
    archiveAccessToken = null;
    archiveCurrentPage = 1;
    
   
    document.getElementById('archiveDashboardBtn').innerHTML = 
        '<i class="fa fa-archive mr-2"></i>Switch to Archive Dashboard';
    document.getElementById('archiveDashboardBtn').classList.remove('active');
    
   
    const portletHeader = document.querySelector('.portlet-header h2');
    if (portletHeader) {
        portletHeader.innerHTML = 
            '<i class="icon-documents"></i>' +
            'BOI Document Management System : ' +
            '<span class="text-primary"><%= HtmlUtil.escape(userScreenName != null ? userScreenName.toUpperCase() : "") %></span>';
    }
    
  
    document.getElementById('docDeltaSelect').style.display = 'inline-block';
    document.getElementById('archiveDeltaSelect').style.display = 'none';
    document.getElementById('archivePageInfo').style.display = 'none';
    
    
    const paginationContainer = document.getElementById('documentPaginationContainer');
    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
    
    
    document.getElementById('foldersSection').style.display = 'block';
    document.getElementById('documentsSection').style.display = 'none';
    
   
    loadFolderContent();
    
    showSuccessMessage('Switched to Main Dashboard');
}

/* async function getAuthToken() {
    if (!haveArchiveAccess) {
        throw new Error('You do not have permission to access the Archive Dashboard');
    }
    
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/x-www-form-urlencoded");

    const urlencoded = new URLSearchParams();
    urlencoded.append("grant_type", "client_credentials");
    urlencoded.append("client_id", archiveClientId);
    urlencoded.append("client_secret", archiveSecretId);

    const requestOptions = {
        method: "POST",
        headers: myHeaders,
        body: urlencoded,
        redirect: "follow"
    };
   
    try {
        const response = await fetch('https://stardmsarchive.bankofindia.bank.in/o/oauth2/token', requestOptions);
       // const response = await fetch(' https://dmsarchive.boicorp.net/o/oauth2/token', requestOptions);

        if (!response.ok) {
            throw new Error('HTTP error! Status: ' + response.status);
        }

        const data = await response.json();
        return data.access_token;
    } catch (error) {
        console.error("Error fetching access token:", error);
        throw error;
    }
} */

async function loadArchiveDocuments(folderId, page = 1, pageSize = 20) {
    console.log('loadArchiveDocuments called with folderId:', folderId, 'page:', page, 'pageSize:', pageSize);
       
    if (!haveArchiveAccess) {
        throw new Error('You do not have permission to access the Archive Dashboard');
    }
    
    
    
    try {
        const formData = new FormData();
        formData.append('<portlet:namespace/>folderId', folderId);
        formData.append('<portlet:namespace/>page', page);
        formData.append('<portlet:namespace/>pageSize', pageSize);
        /* formData.append('<portlet:namespace/>archiveEndpoint', archiveEndpoint);
        formData.append('<portlet:namespace/>dmsEndpoint', dmsEndpoint); */
        
       /*  if (archiveAccessToken) {
            formData.append('<portlet:namespace/>accessToken', archiveAccessToken);
        } */
        
      <%--  console.log('Calling archive search URL:', '<%= archiveSearchDocumentsURL %>'); --%>
        
        const response = await fetch('<%= archiveSearchDocumentsURL %>', {
            method: 'POST',
            body: formData,
            credentials: 'same-origin'
        });

       /*  if (response.status === 401) {
            console.log('Token expired, fetching new token...');
            archiveAccessToken = await getAuthToken();
            return loadArchiveDocuments(folderId, page, pageSize);
        } */

        if (!response.ok) {
            console.error('ResourceURL error status:', response.status, response.statusText);
            throw new Error('Error loading archive documents: ' + response.status);
        }

        const data = await response.json();
        console.log('Archive response from backend:', data);
        if (data.success) {
       
        archiveCurrentPage = page;
        archivePageSize = pageSize;
        archiveTotalItems = data.total || data.totalCount || data.count || 0;
        
       
        document.getElementById('foldersSection').style.display = 'none';
        document.getElementById('documentsSection').style.display = 'block';
        
       
        renderArchiveDocuments(data);      
        updateArchivePageInfo();
        
        } else {
        	throw new Error(data.error || 'Unknown error occurred');
        }
        
        hideLoader();
    } catch (error) {
        console.error('Error loading archive documents:', error);
        hideLoader();
        throw error;
    }
}

function renderArchiveDocuments(data) {
    const documentGrid = document.getElementById('documentGrid');
    const documentCount = document.getElementById('documentCount');
    const documentsSection = document.getElementById('documentsSection');
    const paginationContainer = document.getElementById('documentPaginationContainer');
    const paginationElement = document.getElementById('documentPagination');
    
  
    archiveTotalItems = data.total || data.totalCount || data.count || 
                       (data.files ? data.files.length : 0);
    
    if (data.files && data.files.length > 0) {
        documentGrid.classList.add("row");
        let documentHTML = '';
        
        data.files.forEach((doc, index) => {
            const fileName = escapeHtml(doc.documentName || doc.name || '');
            const fileSize = formatFileSize(doc.size || 0);
            const createdDate = doc.createdDate ? formatHistoryDate(doc.createdDate) : '';
            const createdBy = escapeHtml(doc.createdBy || doc.uploadedBy || '');
            const displayName = getSmartTruncatedName(fileName, 25);
            const sizeInMB = doc.sizeInMB; // ? parseFloat(doc.sizeInMB).toFixed(2) + ' MB' : fileSize;
            const archiveId = doc.uploadedFileId || doc.id || 'N/A';
            
          
            const extension = doc.documentExtension;//fileName.split('.').pop().toLowerCase();
            const iconType = getFileIconType(extension);
            
            documentHTML += 
                '<div class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">' +
                    '<div class="card card-horizontal doc-card h-100 archive-doc-card">' +
                        '<div class="card-body p-3">' +
                            '<div class="d-flex align-items-center justify-content-between">' +
                                '<div class="d-flex align-items-center document-content" style="flex-grow: 1; min-width: 0;">' +
                                    '<div class="flex-shrink-0 mr-3">' +
                                        '<span class="sticker">' +
                                            '<span class="sticker-overlay">' +
                                                '<svg class="lexicon-icon lexicon-icon-' + iconType + '" role="presentation" width="24" height="24">' +
                                                    '<use href="/o/admin-theme/images/clay/icons.svg#' + iconType + '"></use>' +
                                                '</svg>' +
                                            '</span>' +
                                        '</span>' +
                                    '</div>' +
                                    '<div class="flex-grow-1 text-truncate" style="min-width: 0;" ' +
                                    'title="Name: ' + fileName + '\nArchived By: ' + createdBy + '\nDate: ' + createdDate + '\nArchive ID: ' + archiveId + '">' +
                                        '<p class="card-title mb-1">' +
                                            '<strong class="text-truncate-inline" data-full-text="' + fileName + '">' + displayName + '</strong>' +
                                        '</p>' +
                                        '<small class="text-muted d-block text-truncate" style="font-size: 0.75rem;">' +
                                            (extension ? extension.toUpperCase() + ' &bull; ' : '') + sizeInMB + '<br>' +
                                            '<strong>Archived By:</strong> ' + createdBy + '<br>' +
                                            '<strong>Archived Date:</strong> ' + createdDate +
                                        '</small>' +
                                    '</div>' +
                                '</div>' +
                                '<div class="dropdown document-actions" style="position: relative;">' +
                                    '<button class="btn btn-sm btn-icon btn-link text-muted dropdown-toggle three-dot-btn" ' +
                                            'type="button" ' +
                                            'onclick="toggleDropdown(event, this)" ' +
                                            'aria-label="Archive options for ' + fileName + '" ' +
                                            'aria-haspopup="true" ' +
                                            'aria-expanded="false">' +
                                        '<i class="fa fa-ellipsis-v"></i>' +
                                    '</button>' +
                                    '<div class="dropdown-menu dropdown-menu-right shadow-sm">' +
                                    (<%= isHavingPreviewAction %> ?
                                        '<a class="dropdown-item" href="javascript:void(0)" ' +
                                            'onclick="showArchiveDocumentPreview(' + archiveId + ', \'' + fileName.replace(/'/g, "\\'") + '\')">' +
                                            '<i class="fa fa-info-circle mr-2 text-muted"></i> Preview' +
                                        '</a>' +
                                        '<div class="dropdown-divider"></div>'
                                        :
                                        '<a class="dropdown-item" href="javascript:void(0)" ' +
                                            'onclick="showArchiveDocumentInfo(' + archiveId + ', \'' + fileName.replace(/'/g, "\\'") + '\')">' +
                                            '<i class="fa fa-info-circle mr-2 text-muted"></i> Info' +
                                        '</a>' +
                                        '<div class="dropdown-divider"></div>'
                                    ) +
                                    '<a class="dropdown-item text-muted" href="javascript:void(0)" onclick="downloadArchiveFile(' + archiveId + ')">' +
                                        '<i class="fa fa-download mr-2"></i> Download' +
                                    '</a>' +
                                '</div>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>';
        });
        
        documentGrid.innerHTML = documentHTML;
        documentCount.textContent = archiveTotalItems;
        documentsSection.style.display = 'block';
        
        
        updateArchivePagination(archiveTotalItems);
        
        
        setTimeout(() => {
            fixThreeDotIcons();
            setupResponsiveBehavior();
        }, 100);
        
    } else {
        documentGrid.classList.remove("row");
        documentGrid.innerHTML = 
        	'<div class="document-container ml-5" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesContainer">' +
            '<!-- Empty results -->' +
            '<div class="container-view" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesEmptyResultsMessage">' +
                '<div class="mt-4 taglib-empty-result-message">' +
                    '<div class="taglib-empty-result-message-header"></div>' +
                    '<div class="mt-2 text-center text-secondary">' +
                        ' No archived documents found for this folder.' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>';
        documentCount.textContent = '0';
        documentsSection.style.display = 'block';
        paginationContainer.style.display = 'none';
    }
    
   
    addArchiveStyles();
}

//============================================================================
//UPDATED ARCHIVE PAGINATION FUNCTIONS
//============================================================================

function updateArchivePagination(totalItems) {
    const paginationContainer = document.getElementById('documentPaginationContainer');
    const paginationElement = document.getElementById('documentPagination');
    
    if (totalItems > archivePageSize) {
        const totalPages = Math.ceil(totalItems / archivePageSize);
        let paginationHTML = generateArchivePagination(totalPages, archiveCurrentPage);
        paginationElement.innerHTML = paginationHTML;
        paginationContainer.style.display = 'block';
        
       
        setTimeout(() => {
            document.querySelectorAll('.archive-page-link').forEach(link => {
               
                const newLink = link.cloneNode(true);
                link.parentNode.replaceChild(newLink, link);
                
                
                newLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                   
                    let page = this.getAttribute('data-page');
                    if (!page) {
                        page = this.textContent.trim();
                    }
                    
                    if (page && !isNaN(page) && page !== '...' && page !== '«' && page !== '»') {
                        const pageNum = parseInt(page);
                        if (pageNum >= 1 && pageNum <= totalPages) {
                            changeArchivePage(pageNum);
                        }
                    } else if (this.getAttribute('aria-label') === 'Previous' && archiveCurrentPage > 1) {
                        changeArchivePage(archiveCurrentPage - 1);
                    } else if (this.getAttribute('aria-label') === 'Next' && archiveCurrentPage < totalPages) {
                        changeArchivePage(archiveCurrentPage + 1);
                    }
                });
            });
        }, 100);
    } else {
        paginationContainer.style.display = 'none';
        paginationElement.innerHTML = '';
    }
}

function generateArchivePagination(totalPages, currentPage) {
    if (totalPages <= 1) return '';
    
    let paginationHTML = '';
    const maxVisiblePages = 7;
    
   
    if (currentPage > 1) {
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link archive-page-link" href="javascript:void(0)" data-page="' + (currentPage - 1) + '" aria-label="Previous">' +
            '<span aria-hidden="true">&laquo;</span>' +
            '</a>' +
            '</li>';
    } else {
        paginationHTML += '<li class="page-item disabled">' +
            '<span class="page-link" aria-hidden="true">&laquo;</span>' +
            '</li>';
    }
    
   
    let startPage, endPage;
    
    if (totalPages <= maxVisiblePages) {
       
        startPage = 1;
        endPage = totalPages;
    } else {
        
        const maxPagesBeforeCurrent = Math.floor(maxVisiblePages / 2);
        const maxPagesAfterCurrent = Math.floor(maxVisiblePages / 2);
        
        if (currentPage <= maxPagesBeforeCurrent + 1) {
           
            startPage = 1;
            endPage = maxVisiblePages;
        } else if (currentPage + maxPagesAfterCurrent >= totalPages) {
           
            startPage = totalPages - maxVisiblePages + 1;
            endPage = totalPages;
        } else {
           
            startPage = currentPage - maxPagesBeforeCurrent;
            endPage = currentPage + maxPagesAfterCurrent;
        }
    }
    
    
    if (startPage > 1) {
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link archive-page-link" href="javascript:void(0)" data-page="1">1</a>' +
            '</li>';
        if (startPage > 2) {
            paginationHTML += '<li class="page-item disabled">' +
                '<span class="page-link">...</span>' +
                '</li>';
        }
    }
    
   
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            paginationHTML += '<li class="page-item active">' +
                '<span class="page-link archive-page-link active">' + i + '</span>' +
                '</li>';
        } else {
            paginationHTML += '<li class="page-item">' +
                '<a class="page-link archive-page-link" href="javascript:void(0)" data-page="' + i + '">' + i + '</a>' +
                '</li>';
        }
    }
    
   
    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            paginationHTML += '<li class="page-item disabled">' +
                '<span class="page-link">...</span>' +
                '</li>';
        }
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link archive-page-link" href="javascript:void(0)" data-page="' + totalPages + '">' + totalPages + '</a>' +
            '</li>';
    }
    
   
    if (currentPage < totalPages) {
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link archive-page-link" href="javascript:void(0)" data-page="' + (currentPage + 1) + '" aria-label="Next">' +
            '<span aria-hidden="true">&raquo;</span>' +
            '</a>' +
            '</li>';
    } else {
        paginationHTML += '<li class="page-item disabled">' +
            '<span class="page-link" aria-hidden="true">&raquo;</span>' +
            '</li>';
    }
    
    return paginationHTML;
}

function changeArchivePage(page) {
    console.log('changeArchivePage called:', page);
    
    
    if (page < 1) page = 1;
    
    
    const totalPages = Math.ceil(archiveTotalItems / archivePageSize);
    if (page > totalPages) page = totalPages;
    
   
    archiveCurrentPage = page;
    
   
    showLoader();
    
   
    loadArchiveDocuments(currentFolderId, archiveCurrentPage, archivePageSize);
}

function changeArchivePageSize(size) {
    console.log('changeArchivePageSize called:', size);
    
   
    const newSize = parseInt(size);
    
   
    archivePageSize = newSize;
    
   
    archiveCurrentPage = 1;
    
   
    showLoader();
    
   
    loadArchiveDocuments(currentFolderId, archiveCurrentPage, archivePageSize);
}

function updateArchivePageInfo() {
    const startItem = ((archiveCurrentPage - 1) * archivePageSize) + 1;
    const endItem = Math.min(archiveCurrentPage * archivePageSize, archiveTotalItems);
    
    document.getElementById('archiveStartItem').textContent = archiveTotalItems > 0 ? startItem : 0;
    document.getElementById('archiveEndItem').textContent = archiveTotalItems > 0 ? endItem : 0;
    document.getElementById('archiveTotalItems').textContent = archiveTotalItems;
}

function showArchiveDocumentInfo(documentId, documentName) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }
    
   
    Liferay.Util.openToast({
        message: 'Archive Document ID: ' + documentId + '<br>Name: ' + documentName,
        title: 'Archive Document Info',
        type: 'info',
        autoClose: true,
        duration: 10000
    });
}

<%-- function downloadArchiveDocument(documentId, documentName) {
    if (!documentId) {
        showError('Invalid document ID');
        return;
    }
    
    showLoader();
    
   
    const formData = new FormData();
    formData.append('<portlet:namespace/>documentId', documentId);
    formData.append('<portlet:namespace/>archiveEndpoint', archiveEndpoint);
    
    if (archiveAccessToken) {
        formData.append('<portlet:namespace/>accessToken', archiveAccessToken);
    }
    
    
    fetch('<%= archiveDownloadDocumentURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => {
        if (response.status === 401) {
            return getAuthToken().then(newToken => {
                archiveAccessToken = newToken;
                formData.set('<portlet:namespace/>accessToken', newToken);
                return fetch('<%= archiveDownloadDocumentURL %>', {
                    method: 'POST',
                    body: formData,
                    credentials: 'same-origin'
                });
            });
        }
        return response;
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Download failed: ' + response.status);
        }
        return response.blob();
    })
    .then(blob => {
        hideLoader();
        
      
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = documentName || 'archive-document';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        showSuccessMessage('Document download started');
    })
    .catch(error => {
        hideLoader();
        console.error('Download error:', error);
        showError('Failed to download document: ' + error.message);
    });
}
 --%>
function addArchiveStyles() {
    const styleId = 'archive-styles';
    if (!document.getElementById(styleId)) {
        const style = document.createElement('style');
        style.id = styleId;
        style.innerHTML = 
            '.sticker-archive {' +
                'background-color: #6c757d !important;' +
            '}' +
            '.archive-doc-card {' +
                'border-left: 4px solid #6c757d;' +
            '}' +
            '.archive-doc-card:hover {' +
                'border-color: #495057;' +
                'box-shadow: 0 0.5rem 1rem rgba(108, 117, 125, 0.15);' +
                'transform: translateY(-2px);' +
                'transition: all 0.3s ease;' +
            '}' +
            '.archive-switch-btn {' +
                'border-color: #6c757d;' +
                'color: #6c757d;' +
                'transition: all 0.3s ease;' +
            '}' +
            '.archive-switch-btn:hover {' +
                'background-color: #6c757d;' +
                'color: white;' +
            '}' +
            '.archive-switch-btn.active {' +
                'background-color: #6c757d;' +
                'border-color: #6c757d;' +
                'color: white;' +
            '}' +
            '.archive-page-link {' +
                'color: #6c757d !important;' +
                'border-color: #dee2e6 !important;' +
                'cursor: pointer;' +
            '}' +
            '.archive-page-link:hover {' +
                'background-color: #6c757d !important;' +
                'color: white !important;' +
                'border-color: #6c757d !important;' +
            '}' +
            '.page-item.active .archive-page-link.active {' +
                'background-color: #6c757d !important;' +
                'border-color: #6c757d !important;' +
                'color: white !important;' +
            '}' +
            '.archive-badge {' +
                'background-color: #6c757d;' +
                'color: white;' +
                'padding: 4px 8px;' +
                'border-radius: 4px;' +
                'font-size: 0.75rem;' +
                'margin-left: 8px;' +
                'vertical-align: middle;' +
            '}' +
            '#archiveDeltaSelect {' +
                'border-color: #6c757d;' +
                'color: #6c757d;' +
            '}' +
            '#archiveDeltaSelect:focus {' +
                'border-color: #495057;' +
                'box-shadow: 0 0 0 0.2rem rgba(108, 117, 125, 0.25);' +
            '}' +
            '.archive-page-info {' +
                'color: #6c757d;' +
                'font-size: 0.9rem;' +
                'padding: 4px 8px;' +
                'background-color: #f8f9fa;' +
                'border-radius: 4px;' +
                'border: 1px solid #e9ecef;' +
            '}';
        document.head.appendChild(style);
    }
}

//============================================================================
//ARCHIVE UI INITIALIZATION
//============================================================================
function initializeArchiveUI() {
    if (!haveArchiveAccess) {
        console.log('Archive access is disabled for this user');
        
       
        const archiveBtn = document.getElementById('archiveDashboardBtn');
        if (archiveBtn) {
            archiveBtn.style.display = 'none';
        }
        
        const archiveDeltaSelect = document.getElementById('archiveDeltaSelect');
        if (archiveDeltaSelect) {
            archiveDeltaSelect.style.display = 'none';
        }
        
        const archivePageInfo = document.getElementById('archivePageInfo');
        if (archivePageInfo) {
            archivePageInfo.style.display = 'none';
        }
        
       
        const archiveStyles = document.getElementById('archive-styles');
        if (archiveStyles) {
            archiveStyles.remove();
        }
        
       
        if (isArchiveMode) {
            switchToMainDashboard();
        }
    } else {
        console.log('Archive Dashboard access enabled for this user');
    }
}

//============================================================================
//EVENT LISTENER MANAGEMENT
//============================================================================
function addEventListenerWithCleanup(element, event, handler) {
    element.addEventListener(event, handler);
    eventListeners.push({ element, event, handler });
}

function cleanupEventListeners() {
    eventListeners.forEach(({ element, event, handler }) => {
       element.removeEventListener(event, handler);
    });
    eventListeners.length = 0;
}

//============================================================================
//UTILITY FUNCTIONS
//============================================================================
function logDebug(message, data) {
    if (DEBUG_MODE && console && console.log) {
       console.log('[DMS Debug] ' + message, data || '');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatFileSize(bytes) {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function getSmartTruncatedName(name, maxLength = 25) {
    if (!name) return '';
    if (name.length <= maxLength) return name;

    const lastDot = name.lastIndexOf('.');
    if (lastDot > 0) {
       const nameWithoutExt = name.substring(0, lastDot);
       const extension = name.substring(lastDot);
       
       if (nameWithoutExt.length <= maxLength - 3) {
           return nameWithoutExt + '...' + extension;
       }
       
       const truncatedName = nameWithoutExt.substring(0, maxLength - 6) + '...';
       return truncatedName + extension;
    }

    return name.substring(0, maxLength - 3) + '...';
}

function getFileIconType(extension) {
    if (!extension) return 'documents-and-media';

    const ext = extension.toLowerCase();
    if (ext === 'pdf') return 'document-pdf';
    if (ext === 'doc' || ext === 'docx') return 'document-text';
    if (ext === 'xls' || ext === 'xlsx') return 'table';
    if (ext === 'jpg' || ext === 'jpeg' || ext === 'png' || ext === 'gif' || ext === 'bmp') return 'picture';
    if (ext === 'zip' || ext === 'rar' || ext === '7z') return 'download';
    return 'documents-and-media';
}

function getFileExtension(filename) {
    const parts = filename.split('.');
    return parts.length > 1 ? parts.pop().toLowerCase() : '';
}

function validateFolderName(name) {
    if (!name || name.trim() === '') {
       return 'Folder name is required';
    }

    const invalidChars = /[<>:"/\\|?*]/;
    if (invalidChars.test(name)) {
       return 'Folder name cannot contain: < > : " / \\ | ? *';
    }

    const reservedNames = ['CON', 'PRN', 'AUX', 'NUL', 'COM1', 'COM2', 'COM3', 'COM4', 
                         'LPT1', 'LPT2', 'LPT3', '..', '.'];
    if (reservedNames.includes(name.toUpperCase())) {
       return 'Folder name is reserved';
    }

    if (name.length > 75) {
       return 'Folder name must be 75 characters or less';
    }

    return null; 
}

function handleApiError(error, context) {
    console.error('Error in ' + context + ':', error);
    hideLoader();

    if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
       showError('Network error. Please check your connection and try again.');
    } else if (error.name === 'SyntaxError') {
       showError('Server response format error. Please contact administrator.');
    } else if (error.message === 'Session expired') {
       showError('Your session has expired. Please refresh the page.');
       setTimeout(() => window.location.reload(), 2000);
    } else {
       showError('An unexpected error occurred: ' + error.message);
    }

    return false;
}

//============================================================================
//INITIALIZATION
//============================================================================
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== Document Management System Loaded ===');
    console.log('Current folder ID:', currentFolderId);
    console.log('Archive access:', haveArchiveAccess ? 'Enabled' : 'Disabled');
    
    initializePortlet();
    initializeFAB();
    initializeFileUpload();
    initializeModalClose();
    fixThreeDotIcons();
    setupResponsiveBehavior();
    setupClickOutside();
    initializeDropdownHandling();
    
    
    initializeArchiveUI();

    window.addEventListener('beforeunload', cleanupEventListeners);
});

//============================================================================
//UI SETUP FUNCTIONS
//============================================================================
function setupClickOutside() {
    const clickHandler = function(event) {
      
        if (!event.target.closest('.dropdown')) {
            closeAllDropdowns();
        }
        
        
        const overlay = event.target;
        if (overlay.classList.contains('modal-overlay') && 
            !overlay.querySelector('.modal-container').contains(event.target)) {
            overlay.style.display = 'none';
         
            const form = overlay.querySelector('form');
            if (form) form.reset();
            
            enableHoverEffect();
        }
    };
    
    const escapeHandler = function(e) {
        if (e.key === 'Escape') {
            closeAllDropdowns();
            closeAllModals();
        }
    };
    
    addEventListenerWithCleanup(document, 'click', clickHandler);
    addEventListenerWithCleanup(document, 'keydown', escapeHandler);
}

function setupResponsiveBehavior() {
    let resizeTimeout;

    function handleResize() {
        const isMobile = window.innerWidth <= 768;
        const isSmallMobile = window.innerWidth <= 480;
        
       
        const maxChars = isSmallMobile ? 15 : isMobile ? 20 : 25;
        
      
        document.querySelectorAll('.text-truncate-inline').forEach(el => {
            const fullText = el.getAttribute('data-full-text') || el.textContent;
            el.setAttribute('data-full-text', fullText);
            if (fullText.length > maxChars) {
                el.textContent = fullText.substring(0, maxChars) + '...';
            } else {
                el.textContent = fullText;
            }
        });
        
      
        const cards = document.querySelectorAll('.folder-card, .doc-card');
        cards.forEach(card => {
            const cardBody = card.querySelector('.card-body');
            if (cardBody) {
                if (isMobile) {
                    cardBody.style.padding = '12px';
                } else {
                    cardBody.style.padding = '1.25rem';
                }
            }
        });
    }

    const resizeHandler = function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(handleResize, 250);
    };

    addEventListenerWithCleanup(window, 'resize', resizeHandler);

    handleResize();
}

function fixThreeDotIcons() {
    document.querySelectorAll('.three-dot-btn').forEach(button => {
      
        button.style.display = 'inline-flex';
        button.style.alignItems = 'center';
        button.style.justifyContent = 'center';
        button.style.minWidth = '32px';
        button.style.minHeight = '32px';
        button.style.padding = '4px';
        button.style.margin = '0';
        button.style.cursor = 'pointer';
        button.style.border = 'none';
        button.style.background = 'transparent';
        button.style.outline = 'none';
        
      
        const icon = button.querySelector('i');
        if (icon) {
            icon.style.fontSize = '16px';
            icon.style.lineHeight = '1';
            icon.style.display = 'block';
            icon.style.color = 'inherit';
        }
    });
}

//============================================================================
//DROPDOWN FUNCTIONS
//============================================================================
function initializeDropdownHandling() {
    const clickHandler = function(e) {
      
        const threeDotBtn = e.target.closest('.three-dot-btn') || 
                           (e.target.classList.contains('fa-ellipsis-v') && e.target.closest('button'));
        
        if (threeDotBtn) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            
            const dropdown = threeDotBtn.closest('.dropdown');
            if (dropdown) {
                const isOpening = !dropdown.classList.contains('show');
                
               
                if (isOpening) {
                    disableHoverEffect();
                }
                
              
                document.querySelectorAll('.dropdown.show').forEach(otherDropdown => {
                    const otherBtn = otherDropdown.querySelector('.three-dot-btn');
                    if (otherBtn !== threeDotBtn) {
                        otherDropdown.classList.remove('show');
                       
                        enableHoverEffect();
                    }
                });
                
                dropdown.classList.toggle('show');
                if (dropdown.classList.contains('show')) {
                    repositionDropdown(dropdown);
                } else {
                   
                    enableHoverEffect();
                }
            }
            return false;
        }
        
       
        const dropdownItem = e.target.closest('.dropdown-item');
        if (dropdownItem) {
           
            const dropdown = dropdownItem.closest('.dropdown');
            if (dropdown) {
                dropdown.classList.remove('show');
                enableHoverEffect();
            }
            return;
        }
        
        
        if (!e.target.closest('.dropdown')) {
            document.querySelectorAll('.dropdown.show').forEach(dropdown => {
                dropdown.classList.remove('show');
            });
           
            enableHoverEffect();
        }
    };
    
    addEventListenerWithCleanup(document, 'click', clickHandler);
}

function toggleDropdown(event, button) {    
    if (event.defaultPrevented) {       
        return;
    }

    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();

    const dropdown = button.closest('.dropdown');
    if (!dropdown) {
        return;
    }

    const isOpening = !dropdown.classList.contains('show');

    if (isOpening) {
        disableHoverEffect();
    }

    document.querySelectorAll('.dropdown.show').forEach(otherDropdown => {
        if (otherDropdown !== dropdown) {
            console.log('Closing other open dropdown');
            otherDropdown.classList.remove('show');
        }
    });

    dropdown.classList.toggle('show');

    if (isOpening) {
        repositionDropdown(dropdown);
    } else {
        enableHoverEffect();
    }
}

function repositionDropdown(dropdown) {
    const menu = dropdown.querySelector('.dropdown-menu');
    if (!menu) return;

    const button = dropdown.querySelector('.dropdown-toggle');
    const buttonRect = button.getBoundingClientRect();

   
    menu.style.position = 'absolute';
    menu.style.left = 'auto';
    menu.style.right = '0';
    menu.style.top = '100%';
    menu.style.bottom = 'auto';
    menu.style.transform = 'none';
}

function closeAllDropdowns() {
    document.querySelectorAll('.dropdown.show').forEach(dropdown => {
        dropdown.classList.remove('show');
    });
    enableHoverEffect();
}

function closeAllModals() {
    document.querySelectorAll('.modal-overlay').forEach(modal => {
        if (modal.id !== 'loaderBackdrop') {
            modal.style.display = 'none';
          
            const form = modal.querySelector('form');
            if (form) form.reset();
        }
    });
    closeAllDropdowns();
}

//============================================================================
//MAIN PORTLET FUNCTIONS - MODIFIED FOR ARCHIVE SUPPORT
//============================================================================
function initializePortlet() {
    updateBreadcrumb();

    const urlParams = new URLSearchParams(window.location.search);
    const folderId = urlParams.get('<portlet:namespace />folderId');

    if (folderId && folderId !== '0') {
        const folderName = getFolderNameFromBreadcrumb();
        if (folderName && folderName != null) {
            navigateToFolder(parseInt(folderId), folderName);
        }
    }
}

function showLoader() {
    const loader = document.getElementById('loader');
    const backdrop = document.getElementById('loaderBackdrop');
    if (loader) loader.style.display = 'block';
    if (backdrop) backdrop.style.display = 'block';
}

function hideLoader() {
    const loader = document.getElementById('loader');
    const backdrop = document.getElementById('loaderBackdrop');
    if (loader) loader.style.display = 'none';
    if (backdrop) backdrop.style.display = 'none';
}

function showError(message) {
    const errorContainer = document.getElementById('globalErrorContainer');
    const errorMessage = document.getElementById('globalErrorMessage');
    if (errorContainer && errorMessage) {
        errorMessage.textContent = message;
        errorContainer.style.display = 'block';
        
        setTimeout(() => {
            errorContainer.style.display = 'none';
        }, 5000);
    }
}

function hideError() {
    const errorContainer = document.getElementById('globalErrorContainer');
    if (errorContainer) {
        errorContainer.style.display = 'none';
    }
}

function showModelError(message, type) {
    const errorContainer = document.getElementById('error' + type + 'ModelContainer');
    const errorMessage = document.getElementById('error' + type + 'ModelMessage');

    if (errorContainer && errorMessage) {
        errorMessage.textContent = message;
        errorContainer.style.display = 'block';
        
        setTimeout(() => {
            errorContainer.style.display = 'none';
        }, 5000);
    }
}

function showSuccessMessage(message) {
    const notification = document.getElementById('notificationContainer');
    const messageElement = document.getElementById('notificationMessage');

    if (notification && messageElement) {
        messageElement.textContent = message;
        notification.style.display = 'block';
        
        setTimeout(hideNotification, 5000);
    }
}

function hideNotification() {
    const notification = document.getElementById('notificationContainer');
    if (notification) {
        notification.style.display = 'none';
    }
}

function updateBreadcrumb() {
    const breadcrumbTrail = document.getElementById('breadcrumbTrail');
    let breadcrumbHTML = '<li class="breadcrumb-item"><a href="/web/boi-dms/boi-document-management" onclick="window.location.reload(true)">BOI Home</a></li>';

    breadcrumbStack.forEach((crumb, index) => {
        if (index === breadcrumbStack.length - 1) {
            breadcrumbHTML += '<li class="breadcrumb-item active">' + crumb.name + '</li>';
        } else {
            breadcrumbHTML += '<li class="breadcrumb-item"><a href="javascript:void(0)" onclick="breadcrumbNavigate(' + index + ')">' + crumb.name + '</a></li>';
        }
    });

    breadcrumbTrail.innerHTML = breadcrumbHTML;
}

function breadcrumbNavigate(index) {
    if (index >= 0 && index < breadcrumbStack.length) {
        // Reset archive mode when navigating via breadcrumb
        if (isArchiveMode) {
            switchToMainDashboard();
        }
        
        const targetCrumb = breadcrumbStack[index];
        breadcrumbStack = breadcrumbStack.slice(0, index + 1);
        updateBreadcrumb();
        navigateToFolder(targetCrumb.id, targetCrumb.name, false);
    }
}

function getFolderNameFromBreadcrumb() {
    if (breadcrumbStack.length > 0) {
        return breadcrumbStack[breadcrumbStack.length - 1].name;
    }
    return null;
}

function resetToRoot() {
    if (isArchiveMode) {
        switchToMainDashboard();
    }
    
    breadcrumbStack = [];
    currentFolderId = 0;
    currentFolderPage = 1;
    currentDocPage = 1;

    updateBreadcrumb();
    hideError();

    document.getElementById('foldersSection').style.display = 'block';
    document.getElementById('documentsSection').style.display = 'none';
    document.getElementById('folderPaginationContainer').style.display = 'none';
    document.getElementById('documentPaginationContainer').style.display = 'none';

    updateURLParameters({});
}

function navigateToFolder(folderId, folderName, addToStack) {
    if (addToStack === undefined) {
        addToStack = true;
    }
    
   
    if (isArchiveMode) {
        switchToMainDashboard();
    }

    if (addToStack) {
        if (!breadcrumbStack.length || breadcrumbStack[breadcrumbStack.length - 1].id !== folderId) {
            breadcrumbStack.push({ id: folderId, name: folderName });
        }
    }

    currentFolderId = folderId;
    currentFolderPage = 1;
    folderPageSize = 10;
    currentDocPage = 1;
    docPageSize = 20;
    updateBreadcrumb();
    loadFolderContent();
}

//============================================================================
//API CALL FUNCTIONS
//============================================================================
function loadFolderContent() {
  
    if (isArchiveMode) {
        return;
    }
    
    showLoader();
    hideError();

    const url = '<%= getSubFoldersAndDocsURL %>';
    const formData = new FormData();
    formData.append('<portlet:namespace/>folderCur', currentFolderPage);
    formData.append('<portlet:namespace/>folderDelta', folderPageSize);
    formData.append('<portlet:namespace/>folderId', currentFolderId);
    formData.append('<portlet:namespace/>docCur', currentDocPage);
    formData.append('<portlet:namespace/>docDelta', docPageSize);

    fetch(url, {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => {
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            renderFolders(data);
            renderDocuments(data);
            updatePagination(data);
            updateURLParameters({
                folderId: currentFolderId,
                folderCur: currentFolderPage,
                folderDelta: folderPageSize,
                docCur: currentDocPage,
                docDelta: docPageSize
            });
        } else {
            throw new Error(data.error || 'Unknown error occurred');
        }
        hideLoader();
    })
    .catch(error => {
        handleApiError(error, 'loadFolderContent');
    });
}

//============================================================================
//RENDERING FUNCTIONS
//============================================================================

function toggleDropdown(event, button) {
    event.preventDefault();
    event.stopPropagation();
    
    const dropdownMenu = button.nextElementSibling;
    const isExpanded = button.getAttribute('aria-expanded') === 'true';
    
   
    closeAllDropdowns();
    
  
    if (!isExpanded) {
        dropdownMenu.classList.add('show');
        button.setAttribute('aria-expanded', 'true');
        
       
        const closeOnClickOutside = (e) => {
            if (!button.contains(e.target) && !dropdownMenu.contains(e.target)) {
                closeAllDropdowns();
                document.removeEventListener('click', closeOnClickOutside);
            }
        };
        
        setTimeout(() => {
            document.addEventListener('click', closeOnClickOutside);
        }, 0);
    }
}

function closeAllDropdowns() {
  
    document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
        menu.classList.remove('show');
        const btn = menu.previousElementSibling;
        if (btn) {
            btn.setAttribute('aria-expanded', 'false');
        }
    });
}


function renderFolders(data) {
    const folderGrid = document.getElementById('folderGrid');
    const folderCount = document.getElementById('folderCount');

    if (data.subFolders && data.subFolders.length > 0) {
        document.getElementById("folderGrid").classList.add("row");
        let folderHTML = '';
        data.subFolders.forEach(folder => {
            const folderName = escapeHtml(folder.name);
            const displayName = getSmartTruncatedName(folderName, 25);
            const createDate = folder.createDate ? escapeHtml(folder.createDate) : '';
            const createdBy = folder.createdBy ? escapeHtml(folder.createdBy) : '';
            const folderId = folder.id || folder.folderId;
            
            folderHTML += 
                '<div class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">' +
                    '<div class="card card-horizontal folder-card h-100">' +
                        '<div class="card-body p-3">' +
                            '<div class="d-flex align-items-center justify-content-between">' +
                                '<div class="d-flex align-items-center folder-content" ' +
                                    'onclick="navigateToFolder(' + folderId + ', \'' + folderName.replace(/'/g, "\\'") + '\')" ' +
                                    'style="cursor: pointer; flex-grow: 1; min-width: 0;">' +
                                    '<div class="flex-shrink-0 mr-3">' +
                                        '<span class="sticker sticker-secondary">' +
                                            '<span class="sticker-overlay">' +
                                                '<svg class="lexicon-icon lexicon-icon-folder" role="presentation" width="24" height="24">' +
                                                    '<use href="/o/admin-theme/images/clay/icons.svg#folder"></use>' +
                                                '</svg>' +
                                            '</span>' +
                                        '</span>' +
                                    '</div>' +
                                    '<div class="flex-grow-1 text-truncate" style="min-width: 0;" ' +
                                    'title="Name: ' + folderName + '\nCreator: ' + createdBy + '\nDate: ' + createDate + '">' +
                                        '<p class="card-title mb-1">' +
                                            '<span class="text-truncate-inline" data-full-text="' + folderName + '">' + displayName + '</span>' +
                                        '</p>' +
                                        '<small class="text-muted d-block text-truncate" style="font-size: 0.75rem;">' +
                                        '<strong>Creator:</strong> ' + createdBy + '<br>' +
                                        '<strong>Date:</strong> ' + createDate +
                                    '</small>' +
                                    '</div>' +
                                '</div>';
    
            if (<%= canViewHistory || isHavingDeleteAction %>) {
                folderHTML += 
                                '<div class="dropdown folder-actions" style="position: relative;">' +
                                    '<button class="btn btn-sm btn-icon btn-link text-muted dropdown-toggle three-dot-btn" ' +
                                            'type="button" ' +
                                            'onclick="toggleDropdown(event, this)" ' +
                                            'aria-label="More options for ' + folderName + '" ' +
                                            'aria-haspopup="true" ' +
                                            'aria-expanded="false">' +
                                        '<i class="fa fa-ellipsis-v"></i>' +
                                    '</button>' +
                                    '<div class="dropdown-menu dropdown-menu-right shadow-sm">';
                
                if (<%= canViewHistory %>) {
                    folderHTML += 
                                        '<a class="dropdown-item" href="javascript:void(0)" ' +
                                        'onclick="showFolderHistory(' + folderId + ', \'' + folderName.replace(/'/g, "\\'") + '\'); closeAllDropdowns();">' +
                                            '<i class="fa fa-history mr-2 text-muted"></i> History' +
                                        '</a>';
                }
                
                folderHTML += 
                                        '<a class="dropdown-item" href="javascript:void(0)" ' +
                                        'onclick="navigateToFolder(' + folderId + ', \'' + folderName.replace(/'/g, "\\'") + '\'); closeAllDropdowns();">' +
                                            '<i class="fa fa-folder-open mr-2 text-muted"></i> Open' +
                                        '</a>';
                
                if (<%= isHavingDeleteAction %>) {
                    folderHTML += 
                                        '<div class="dropdown-divider"></div>' +
                                        '<a class="dropdown-item text-danger" href="javascript:void(0)" ' +
                                        'onclick="showDeleteFolderModal(' + folderId + ', \'' + folderName.replace(/'/g, "\\'") + '\'); closeAllDropdowns();">' +
                                            '<i class="fa fa-trash mr-2"></i> Delete' +
                                        '</a>';
                }
                
                folderHTML += 
                                    '</div>' +
                                '</div>';
            }
            
            folderHTML += 
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>';
        });
        folderGrid.innerHTML = folderHTML;
        folderCount.textContent = data.totalFolders || 0;
        document.getElementById('folderResults').style.display = 'block';
        
        setTimeout(() => {
            fixThreeDotIcons();
            setupResponsiveBehavior();
        }, 100);
    } else {
        document.getElementById("folderGrid").classList.remove("row");
        folderGrid.innerHTML =           
            '<div class="document-container ml-5" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesContainer">' +
                '<!-- Empty results -->' +
                '<div class="container-view" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesEmptyResultsMessage">' +
                    '<div class="mt-4 taglib-empty-result-message">' +
                        '<div class="taglib-empty-result-message-header"></div>' +
                        '<div class="mt-2 text-center text-secondary">' +
                            'No subfolders found in this folder.' +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>';
        folderCount.textContent = '0';
    }
}

function renderDocuments(data) {
  
    if (isArchiveMode) {
        return;
    }
    
    const documentGrid = document.getElementById('documentGrid');
    const documentCount = document.getElementById('documentCount');
    const documentsSection = document.getElementById('documentsSection');

    if (data.documents && data.documents.length > 0) {
        documentGrid.classList.add("row");
        let documentHTML = '';
        data.documents.forEach(doc => {
            const iconType = getFileIconType(doc.extension);
            const fileSize = formatFileSize(doc.size);
            const docName = escapeHtml(doc.name);
            const displayName = getSmartTruncatedName(docName, 25);
            const docUrl = escapeHtml(doc.url);
            const extension = doc.extension ? escapeHtml(doc.extension.toUpperCase()) : 'File';
            const createDate = doc.createDate ? escapeHtml(doc.createDate) : '';
            const createdBy = doc.createdBy ? escapeHtml(doc.createdBy) : '';
            const docId = doc.id || doc.documentId || doc.fileEntryId;
            
            documentHTML += 
                '<div class="col-xl-3 col-lg-4 col-md-6 col-sm-12 mb-4">' +
                    '<div class="card card-horizontal doc-card h-100">' +
                        '<div class="card-body p-3">' +
                            '<div class="d-flex align-items-center justify-content-between">' +
                                '<div class="d-flex align-items-center document-content" style="flex-grow: 1; min-width: 0;">' +
                                    '<div class="flex-shrink-0 mr-3">' +
                                        '<span class="sticker">' +
                                            '<span class="sticker-overlay inline-item">' +
                                                '<svg class="lexicon-icon lexicon-icon-' + iconType + '" role="presentation" width="24" height="24">' +
                                                    '<use href="/o/admin-theme/images/clay/icons.svg#' + iconType + '"></use>' +
                                                '</svg>' +
                                            '</span>' +
                                        '</span>' +
                                    '</div>' +
                                    '<div class="flex-grow-1 text-truncate" style="min-width: 0;" ' +
                                    'title="Name: ' + docName + '\nCreator: ' + createdBy + '\nDate: ' + createDate + '">' +
                                        '<p class="card-title mb-1" >' +
                                            '<a href="' + docUrl + '"  class="text-decoration-none text-dark text-truncate" ' +
                                            'aria-label="Download ' + docName + '">' +
                                                '<span class="text-truncate-inline" data-full-text="' + docName + '">' + displayName + '</span>' +
                                            '</a>' +
                                        '</p>' +
                                        '<small class="text-muted d-block text-truncate" style="font-size: 0.75rem;">' +
                                        extension + ' &bull; ' + fileSize + '<br>' +
                                        '<strong>Creator:</strong> ' + createdBy + 
                                        '<br><strong>Date:</strong> ' + createDate +
                                    '</small>' +
                                    '</div>' +
                                '</div>';
    
            if (<%= isHavingPreviewAction || canViewHistory || isHavingDeleteAction ||isShowingDownload %>) {
                documentHTML += 
                                '<div class="dropdown document-actions" style="position: relative;">' +
                                    '<button class="btn btn-sm btn-icon btn-link text-muted dropdown-toggle three-dot-btn" ' +
                                            'type="button" ' +
                                            'onclick="toggleDropdown(event, this)" ' +
                                            'aria-label="More options for ' + docName + '" ' +
                                            'aria-haspopup="true" ' +
                                            'aria-expanded="false">' +
                                        '<i class="fa fa-ellipsis-v"></i>' +
                                    '</button>' +
                                    '<div class="dropdown-menu dropdown-menu-right shadow-sm">';
                
                if (<%= canViewHistory %>) {
                    documentHTML += 
                                        '<a class="dropdown-item" href="javascript:void(0)" ' +
                                        'onclick="showDocumentHistory(' + docId + ', \'' + docName.replace(/'/g, "\\'") + '\'); closeAllDropdowns();">' +
                                            '<i class="fa fa-history mr-2 text-muted"></i> History' +
                                        '</a>';
                }
                
                if (<%= isHavingPreviewAction %>) {
                	 var cusextension = (doc.extension || '').toLowerCase();
                documentHTML += 
                	              
                                     '<a class="dropdown-item" href="javascript:void(0)" ' +
                              'onclick="showDocumentPreview(' + docId + ', \'' + docName.replace(/'/g,"\\'") + '\', \'' + docUrl.replace(/'/g,"\\'") + '\', \'' + cusextension.replace(/'/g,"\\'") + '\'); closeAllDropdowns();">' +
                              '<i class="fa fa-eye mr-2 text-muted"></i> Preview</a>';


                }                      
                                        
                documentHTML +=            '<a class="dropdown-item" href="' + docUrl + '" onclick="closeAllDropdowns();">' +
                                            '<i class="fa fa-download mr-2 text-muted"></i> Download' +
                                        '</a>';
                
                if (<%= isHavingDeleteAction %>) {
                    documentHTML += 
                                        '<div class="dropdown-divider"></div>' +
                                        '<a class="dropdown-item text-danger" href="javascript:void(0)" ' +
                                        'onclick="showDeleteDocumentModal(' + docId + ', \'' + docName.replace(/'/g, "\\'") + '\'); closeAllDropdowns();">' +
                                            '<i class="fa fa-trash mr-2"></i> Delete' +
                                        '</a>';
                }
                
                documentHTML += 
                                '</div>' +
                            '</div>';
            }
            
            documentHTML += 
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>';
        });
        documentGrid.innerHTML = documentHTML;
        documentCount.textContent = data.totalDocs || 0;
        documentsSection.style.display = 'block';
        
        setTimeout(() => {
            fixThreeDotIcons();
            setupResponsiveBehavior();
        }, 100);
    } else {
        documentGrid.classList.remove("row");
        documentGrid.innerHTML = '<div class="document-container ml-5" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesContainer">' +
            '<!-- Empty results -->' +
            '<div class="container-view" id="_com_liferay_document_library_web_portlet_DLAdminPortlet_entriesEmptyResultsMessage">' +
                '<div class="mt-4 taglib-empty-result-message">' +
                    '<div class="taglib-empty-result-message-header"></div>' +
                    '<div class="mt-2 text-center text-secondary">' +
                        ' No documents found in this folder.' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>';
        documentCount.textContent = '0';
        documentsSection.style.display = 'block';
    }
}

//============================================================================
//PAGINATION FUNCTIONS
//============================================================================
function updatePagination(data) {
   
    if (isArchiveMode) {
        return;
    }
    
    updateFolderPagination(data);
    updateDocumentPagination(data);
}

function updateFolderPagination(data) {
    const paginationContainer = document.getElementById('folderPaginationContainer');
    const paginationElement = document.getElementById('folderPagination');
    const totalFolders = data.totalFolders || 0;

    if (totalFolders > folderPageSize) {
        const totalPages = Math.ceil(totalFolders / folderPageSize);
        let paginationHTML = generateSmartPagination(totalPages, currentFolderPage, 'changeFolderPage');
        
        paginationElement.innerHTML = paginationHTML;
        paginationContainer.style.display = 'block';
    } else {
        paginationContainer.style.display = 'none';
    }
}

function updateDocumentPagination(data) {
    const paginationContainer = document.getElementById('documentPaginationContainer');
    const paginationElement = document.getElementById('documentPagination');
    const totalDocs = data.totalDocs || 0;

    if (totalDocs > docPageSize) {
        const totalPages = Math.ceil(totalDocs / docPageSize);
        let paginationHTML = generateSmartPagination(totalPages, currentDocPage, 'changeDocPage');
        
        paginationElement.innerHTML = paginationHTML;
        paginationContainer.style.display = 'block';
    } else {
        paginationContainer.style.display = 'none';
    }
}

function generateSmartPagination(totalPages, currentPage, pageChangeFunction) {
    if (totalPages <= 1) return '';

    let paginationHTML = '';
    const maxVisiblePages = 7;

    if (currentPage > 1) {
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + (currentPage - 1) + ')">&laquo;</a>' +
            '</li>';
    } else {
        paginationHTML += '<li class="page-item disabled"><span class="page-link">&laquo;</span></li>';
    }

    if (totalPages <= maxVisiblePages) {
        for (let i = 1; i <= totalPages; i++) {
            if (i === currentPage) {
                paginationHTML += '<li class="page-item active"><span class="page-link">' + i + '</span></li>';
            } else {
                paginationHTML += '<li class="page-item">' +
                    '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">' + i + '</a>' +
                    '</li>';
            }
        }
    } else {
        paginationHTML += '<li class="page-item' + (currentPage === 1 ? ' active' : '') + '">' +
            '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(1)">1</a>' +
            '</li>';
        
        let startPage, endPage;
        
        if (currentPage <= 4) {
            startPage = 2;
            endPage = 5;
            paginationHTML += generatePageLinks(startPage, endPage, currentPage, pageChangeFunction);
            
            const dropdownId = 'dropdown-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
            paginationHTML += '<li class="page-item dropdown dropup">' +
                '<a class="page-link dropdown-toggle" href="javascript:void(0)" id="' + dropdownId + '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                '...' +
                '</a>' +
                '<div class="dropdown-menu" aria-labelledby="' + dropdownId + '">';
            
            for (let i = 6; i < totalPages; i++) {
                paginationHTML += '<a class="dropdown-item" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">Page ' + i + '</a>';
            }
            
            paginationHTML += '</div></li>';
            
            paginationHTML += '<li class="page-item">' +
                '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + totalPages + ')">' + totalPages + '</a>' +
                '</li>';
        } else if (currentPage >= totalPages - 3) {
            const dropdownId = 'dropdown-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
            paginationHTML += '<li class="page-item dropdown dropup">' +
                '<a class="page-link dropdown-toggle" href="javascript:void(0)" id="' + dropdownId + '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                '...' +
                '</a>' +
                '<div class="dropdown-menu" aria-labelledby="' + dropdownId + '">';
            
            for (let i = 2; i <= totalPages - 5; i++) {
                paginationHTML += '<a class="dropdown-item" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">Page ' + i + '</a>';
            }
            
            paginationHTML += '</div></li>';
            
            startPage = totalPages - 4;
            endPage = totalPages;
            paginationHTML += generatePageLinks(startPage, endPage, currentPage, pageChangeFunction);
        } else {
            const dropdownId1 = 'dropdown-' + Date.now() + '-1-' + Math.random().toString(36).substr(2, 9);
            paginationHTML += '<li class="page-item dropdown dropup">' +
                '<a class="page-link dropdown-toggle" href="javascript:void(0)" id="' + dropdownId1 + '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                '...' +
                '</a>' +
                '<div class="dropdown-menu" aria-labelledby="' + dropdownId1 + '">';
            
            for (let i = 2; i < currentPage - 1; i++) {
                paginationHTML += '<a class="dropdown-item" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">Page ' + i + '</a>';
            }
            
            paginationHTML += '</div></li>';
            
            startPage = currentPage - 1;
            endPage = currentPage + 1;
            paginationHTML += generatePageLinks(startPage, endPage, currentPage, pageChangeFunction);
            
            const dropdownId2 = 'dropdown-' + Date.now() + '-2-' + Math.random().toString(36).substr(2, 9);
            paginationHTML += '<li class="page-item dropdown dropup">' +
                '<a class="page-link dropdown-toggle" href="javascript:void(0)" id="' + dropdownId2 + '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                '...' +
                '</a>' +
                '<div class="dropdown-menu" aria-labelledby="' + dropdownId2 + '">';
            
            for (let i = currentPage + 2; i < totalPages; i++) {
                paginationHTML += '<a class="dropdown-item" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">Page ' + i + '</a>';
            }
            
            paginationHTML += '</div></li>';
            
            paginationHTML += '<li class="page-item">' +
                '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + totalPages + ')">' + totalPages + '</a>' +
                '</li>';
        }
    }

    if (currentPage < totalPages) {
        paginationHTML += '<li class="page-item">' +
            '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + (currentPage + 1) + ')">&raquo;</a>' +
            '</li>';
    } else {
        paginationHTML += '<li class="page-item disabled"><span class="page-link">&raquo;</span></li>';
    }

    return paginationHTML;
}

function generatePageLinks(startPage, endPage, currentPage, pageChangeFunction) {
    let html = '';
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            html += '<li class="page-item active"><span class="page-link">' + i + '</span></li>';
        } else {
            html += '<li class="page-item">' +
                '<a class="page-link" href="javascript:void(0)" onclick="' + pageChangeFunction + '(' + i + ')">' + i + '</a>' +
                '</li>';
        }
    }
    return html;
}

function changeFolderPage(page) {
    currentFolderPage = page;
    loadFolderContent();
}

function changeDocPage(page) {
    currentDocPage = page;
    loadFolderContent();
}

function changeFolderDelta(delta) {
    folderPageSize = parseInt(delta);
    currentFolderPage = 1;
    if (currentFolderId !== 0) {
        loadFolderContent();
    }
}

function changeDocDelta(delta) {
    docPageSize = parseInt(delta);
    currentDocPage = 1;
    if (currentFolderId !== 0) {
        loadFolderContent();
    }
}

function updateURLParameters(params) {
    const url = new URL(window.location);

    const paramsToRemove = ['folderId', 'folderCur', 'folderDelta', 'docCur', 'docDelta'];
    paramsToRemove.forEach(function(param) {
        url.searchParams.delete('<portlet:namespace />' + param);
    });

    Object.keys(params).forEach(function(key) {
        if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
            url.searchParams.set('<portlet:namespace />' + key, params[key]);
        }
    });

    window.history.replaceState({}, '', url.toString());
}

//============================================================================
//FILE UPLOAD FUNCTIONS
//============================================================================
function initializeFAB() {
    const fabMain = document.getElementById('fabMain');
    const fabContainer = document.querySelector('.fab-container');

    if (fabMain && fabContainer) {
        const fabClickHandler = function(e) {
            e.preventDefault();
            e.stopPropagation();
            fabContainer.classList.toggle('active');
        };
        
        addEventListenerWithCleanup(fabMain, 'click', fabClickHandler);
        
        const documentClickHandler = function(e) {
            if (!fabContainer.contains(e.target)) {
                fabContainer.classList.remove('active');
            }
        };
        
        addEventListenerWithCleanup(document, 'click', documentClickHandler);
    }
}

function initializeFileUpload() {
    const fileUploadArea = document.getElementById('fileUploadArea');
    const fileInput = document.getElementById('documentFiles');
    const filesPreview = document.getElementById('filesPreview');
    const filesList = document.getElementById('filesList');
    const filesCount = document.getElementById('filesCount');

    if (fileUploadArea && fileInput) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            const handler = preventDefaults.bind(null, eventName);
            addEventListenerWithCleanup(fileUploadArea, eventName, handler);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        ['dragenter', 'dragover'].forEach(eventName => {
            const handler = highlight.bind(null, eventName);
            addEventListenerWithCleanup(fileUploadArea, eventName, handler);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            const handler = unhighlight.bind(null, eventName);
            addEventListenerWithCleanup(fileUploadArea, eventName, handler);
        });

        function highlight() {
            fileUploadArea.classList.add('dragover');
        }

        function unhighlight() {
            fileUploadArea.classList.remove('dragover');
        }

        const dropHandler = handleDrop.bind(null);
        addEventListenerWithCleanup(fileUploadArea, 'drop', dropHandler);

        function handleDrop(e) {
            const dt = e.dataTransfer;
            const files = dt.files;
            handleFiles(files);
        }

        const changeHandler = function() {
            handleFiles(this.files);
        };
        
        addEventListenerWithCleanup(fileInput, 'change', changeHandler);

        function handleFiles(files) {
            if (files.length > 0) {
                updateFilesPreview(files);
                filesPreview.style.display = 'block';
                filesCount.textContent = files.length;
            } else {
                filesPreview.style.display = 'none';
            }
        }

        window.updateFilesPreview = function(files) {
            filesList.innerHTML = '';
            
            Array.from(files).forEach((file, index) => {
                const fileElement = createFileElement(file, index);
                filesList.appendChild(fileElement);
            });
        };

        function createFileElement(file, index) {
            const fileName = escapeHtml(file.name);
            const displayName = fileName.length > 30 ? fileName.substring(0, 30) + '...' : fileName;
            
            const fileElement = document.createElement('div');
            fileElement.className = 'file-preview-item';
            fileElement.innerHTML = 
                '<div class="file-info">' +
                    '<i class="fa fa-file"></i>' +
                    '<div class="file-details">' +
                        '<span class="file-name" title="' + fileName + '">' + displayName + '</span>' +
                        '<span class="file-size">' + formatFileSize(file.size) + '</span>' +
                    '</div>' +
                    '<button type="button" class="file-remove" onclick="removeFile(' + index + ')">' +
                        '<i class="fa fa-times"></i>' +
                    '</button>' +
                '</div>';
            return fileElement;
        }
    }
}

function removeFile(index) {
    const fileInput = document.getElementById('documentFiles');
    const files = Array.from(fileInput.files);

    files.splice(index, 1);

    const dataTransfer = new DataTransfer();
    files.forEach(file => dataTransfer.items.add(file));
    fileInput.files = dataTransfer.files;

    if (fileInput.files.length > 0) {
        updateFilesPreview(fileInput.files);
        document.getElementById('filesCount').textContent = fileInput.files.length;
    } else {
        document.getElementById('filesPreview').style.display = 'none';
    }
}

function removeAllFiles() {
    const fileInput = document.getElementById('documentFiles');
    fileInput.value = '';
    document.getElementById('filesPreview').style.display = 'none';
    document.getElementById('filesCount').textContent = '0';
    document.getElementById('filesList').innerHTML = '';
}

function initializeModalClose() {
    const clickHandler = function(e) {
      
        if (e.target.classList.contains('modal-overlay')) {
            const modal = e.target;
            if (!modal.querySelector('.modal-container').contains(e.target)) {
                modal.style.display = 'none';
              
                const form = modal.querySelector('form');
                if (form) form.reset();
            }
        }
    };

    const escapeHandler = function(e) {
        if (e.key === 'Escape') {
            closeAllModals();
            closeAllDropdowns();
        }
    };

    addEventListenerWithCleanup(document, 'click', clickHandler);
    addEventListenerWithCleanup(document, 'keydown', escapeHandler);
}

//============================================================================
//CREATE FOLDER/DOCUMENT FUNCTIONS
//============================================================================
function showCreateFolderModal() {
    document.getElementById('parentFolderId').value = currentFolderId;
    document.getElementById('createFolderForm').reset();
    document.getElementById('createFolderModal').style.display = 'flex';
    document.querySelector('.fab-container').classList.remove('active');
}

function closeCreateFolderModal() {
    document.getElementById('createFolderModal').style.display = 'none';
    document.getElementById('createFolderForm').reset();
}

function showCreateDocumentModal() {
    document.getElementById('documentParentFolderId').value = currentFolderId;
    document.getElementById('createDocumentForm').reset();
    document.getElementById('filesPreview').style.display = 'none';
    document.getElementById('filesCount').textContent = '0';
    document.getElementById('filesList').innerHTML = '';
    const fileInput = document.getElementById('documentFiles');
    if (fileInput) fileInput.value = '';
    document.getElementById('createDocumentModal').style.display = 'flex';
    document.querySelector('.fab-container').classList.remove('active');
}

function closeCreateDocumentModal() {
    document.getElementById('createDocumentModal').style.display = 'none';
    const form = document.getElementById('createDocumentForm');
    if (form) form.reset();
    document.getElementById('filesPreview').style.display = 'none';
    document.getElementById('filesCount').textContent = '0';
    document.getElementById('filesList').innerHTML = '';
    const fileInput = document.getElementById('documentFiles');
    if (fileInput) fileInput.value = '';
}

function submitCreateDocument() {
    const form = document.getElementById('createDocumentForm');
    const documentFiles = document.getElementById('documentFiles').files;

    if (!documentFiles || documentFiles.length === 0) {
        showModelError('Please select at least one file to upload','Document');
        return;
    }

    const maxFiles = <%= maxFiles %>;                  
    const maxFileSizeMB = <%= maxFileSize%>; 
    const maxTotalSizeMB = <%= maxTotalSize%>; 

    const maxFileSizeBytes = maxFileSizeMB * 1024 * 1024;
    const maxTotalSizeBytes = maxTotalSizeMB * 1024 * 1024; 

    if (documentFiles.length > maxFiles) {
        showModelError('Maximum ' + maxFiles + ' files allowed per upload', 'Document');
        return;
    }

    for (let file of documentFiles) {
        if (file.size > maxFileSizeBytes) {
            showModelError('Each file must be less than ' + maxFileSizeMB + ' MB', 'Document');
            return;
        }
    }

    let totalSize = Array.from(documentFiles).reduce((acc, file) => acc + file.size, 0);
    if (totalSize > maxTotalSizeBytes) {
        showModelError('Total upload size must not exceed ' + maxTotalSizeMB + ' MB', 'Document');
        return;
    }

    const fileCount = documentFiles.length;

    showLoader();

    const formData = new FormData();
    formData.append('<portlet:namespace/>parentFolderId', currentFolderId);

    Array.from(documentFiles).forEach(file => {
        formData.append('<portlet:namespace/>documentFiles', file);
    });

    fetch('<%= createDocumentURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => {
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        return response.json();
    })
    .then(data => {
        hideLoader();
        closeCreateDocumentModal();
        
        if (data.success) {
            showSuccessMessage('Successfully uploaded ' + fileCount + ' file' + (fileCount !== 1 ? 's' : '') + '!');
            if (typeof loadFolderContent === 'function') {
                loadFolderContent();
            }
        } else {
            showError(data.error || 'Failed to upload documents');
        }
    })
    .catch(error => {
        handleApiError(error, 'submitCreateDocument');
    });
}

function submitCreateFolder() {
    const form = document.getElementById('createFolderForm');
    const folderName = document.getElementById('folderName').value.trim();

    const validationError = validateFolderName(folderName);
    if (validationError) {
        showModelError(validationError, 'Folder');
        return;
    }

    showLoader();

    const formData = new FormData();
    formData.append('<portlet:namespace/>folderName', folderName);
    formData.append('<portlet:namespace/>folderDescription', document.getElementById('folderDescription').value);
    formData.append('<portlet:namespace/>parentFolderId', currentFolderId);

    fetch('<%= createFolderURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => {
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        return response.json();
    })
    .then(data => {
        hideLoader();
        closeCreateFolderModal();
        
        if (data.success) {
            showSuccessMessage('Folder created successfully!');
            if (typeof loadFolderContent === 'function') {
                loadFolderContent();
            }
        } else {
            showError(data.error || 'Failed to create folder');
        }
    })
    .catch(error => {
        handleApiError(error, 'submitCreateFolder');
    });
}

//============================================================================
//HISTORY MODAL FUNCTIONS
//============================================================================
function showFolderHistory(folderId, folderName) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }

    showLoader();

    const formData = new FormData();
    formData.append('<portlet:namespace/>folderId', folderId);

    fetch('<%= getFolderHistoryURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        hideLoader();
        
        if (data.success && data.history) {
            showHistoryModal('Folder History: ' + folderName, data.history, 'folder');
        } else {
            showError(data.error || 'Failed to load folder history');
        }
    })
    .catch(error => {
        hideLoader();
        showError('Error loading folder history: ' + error.message);
    });
}

function showDocumentHistory(documentId, documentName) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }

    showLoader();

    const formData = new FormData();
    formData.append('<portlet:namespace/>documentId', documentId);

    fetch('<%= getDocumentHistoryURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        hideLoader();
        
        if (data.success && data.history) {
            showHistoryModal('Document History: ' + documentName, data.history, 'document');
        } else {
            showError(data.error || 'Failed to load document history');
        }
    })
    .catch(error => {
        hideLoader();
        showError('Error loading document history: ' + error.message);
    });
}

function showHistoryModal(title, historyItems, type) {
    const modal = document.getElementById('historyModal');
    const titleElement = document.getElementById('historyModalTitle');
    const timelineContainer = document.getElementById('historyTimeline');

    titleElement.textContent = title;

    let timelineHTML = '<div class="timeline">';

    historyItems.forEach((item, index) => {
        const isLast = index === historyItems.length - 1;
        const actionType = item.actionType || 'Modified';
        const userName = item.userName || 'Unknown User';
        const timestamp = item.timestamp || new Date().toISOString();
        const details = item.details || {};
        
        let markerClass = '';
        let markerIcon = '';
        
        switch(actionType.toLowerCase()) {
            case 'created':
                markerClass = 'bg-success';
                markerIcon = 'fa-plus';
                break;
            case 'deleted':
                markerClass = 'bg-danger';
                markerIcon = 'fa-trash';
                break;
            case 'modified':
            case 'updated':
                markerClass = 'bg-warning';
                markerIcon = 'fa-edit';
                break;
            case 'viewed':
                markerClass = 'bg-info';
                markerIcon = 'fa-eye';
                break;
            default:
                markerClass = 'bg-secondary';
                markerIcon = 'fa-history';
        }
        
        timelineHTML += 
            '<div class="timeline-item' + (isLast ? ' last' : '') + '">' +
                '<div class="timeline-marker ' + markerClass + '">' +
                    '<i class="fa ' + markerIcon + '"></i>' +
                '</div>' +
                '<div class="timeline-content">' +
                    '<div class="timeline-header">' +
                        '<h5>' + actionType + ' by ' + userName + '</h5>' +
                        '<div class="timeline-time">' + formatHistoryDate(timestamp) + '</div>' +
                    '</div>' +
                    '<p>' + (details.description || 'No description available') + '</p>';
        
        if (Object.keys(details).length > 0) {
            timelineHTML += '<div class="timeline-details">';
            for (const [key, value] of Object.entries(details)) {
                if (key !== 'description') {
                    timelineHTML += 
                        '<div class="detail-item">' +
                            '<div class="detail-label">' + key + ':</div>' +
                            '<div class="detail-value">' + value + '</div>' +
                        '</div>';
                }
            }
            timelineHTML += '</div>';
        }
        
        timelineHTML += '</div></div>';
    });

    timelineHTML += '</div>';
    timelineContainer.innerHTML = timelineHTML;

    modal.style.display = 'flex';
}

function formatHistoryDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function closeHistoryModal() {
    document.getElementById('historyModal').style.display = 'none';
}

//============================================================================
//PREVIEW MODAL FUNCTIONS
//============================================================================
const sanitizeDescription = (description) => {
    if (!description || typeof description !== 'string') return '';
    
    const isHtmlError = description.includes('404 Not Found') || 
                        description.includes('<html>') || 
                        description.includes('<title>404') ||
                        description.includes('nginx/') ||
                        description.trim().startsWith('<');
    
    return isHtmlError ? '' : description.trim();
};

function showDocumentPreview(documentId, documentName, documentUrl,documentExtension) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }
    
    const formData = new FormData();
    formData.append('<portlet:namespace/>documentId', documentId);
    
    fetch('<%= getDocumentDetailsURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && data.document) {
            const cleanDetails = {
                createdBy: data.document.createdBy || '',
                createDate: data.document.createDate || '',
                modifiedDate: data.document.modifiedDate || '',
                fileSize: formatFileSize(data.document.size),
                description: sanitizeDescription(data.document.description),
                mimeType: data.document.mimeType || '',
                version: data.document.version || ''
            };
            renderPreviewModal(documentName, documentUrl,documentExtension, cleanDetails);
        } else {
            showError(data.error || 'Failed to load document details');
        }
    })
    .catch(error => {
        showError('Error loading document details: ' + error.message);
    });
}

function renderPreviewModal(documentName, documentUrl,documentExtension, details) {
    const params = new URLSearchParams();
    
    params.append('documentName', documentName || '');
    params.append('documentExtension', documentExtension || '');
    params.append('documentUrl', encodeURIComponent(documentUrl || ''));
    
    if (details) {
        if (details.createdBy) params.append('createdBy', details.createdBy);
        if (details.createDate) params.append('createDate', details.createDate);
        if (details.modifiedDate) params.append('modifiedDate', details.modifiedDate);
        if (details.fileSize) params.append('fileSize', details.fileSize);
        if (details.description) params.append('description', encodeURIComponent(details.description));
        if (details.mimeType) params.append('mimeType', details.mimeType);
        if (details.version) params.append('version', details.version);
    }
    
    window.open(
         'https://stardms.bankofindia.co.in/web/boi-dms/document-preview?' + params.toString(),
        '_blank'
    );
}

//============================================================================
//DELETE MODAL FUNCTIONS
//============================================================================
function showDeleteFolderModal(folderId, folderName) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }

    console.log('=== showDeleteFolderModal called ===');
    console.log('Raw folderId:', folderId);
    console.log('Raw folderName:', folderName);

    const numericId = parseInt(folderId);
    console.log('Parsed numericId:', numericId);

    if (isNaN(numericId) || numericId <= 0) {
        console.error(' Invalid folder ID:', folderId, 'converted to:', numericId);
        showError('Invalid folder ID: ' + folderId);
        return;
    }

    currentDeleteFolderId = numericId; 
    currentDeleteFolderName = folderName;

    console.log(' Stored for deletion:', { 
        id: currentDeleteFolderId, 
        name: currentDeleteFolderName 
    });

    document.getElementById('deleteFolderTitle').textContent = 'Delete "' + folderName + '"?';
    document.getElementById('deleteFolderMessage').textContent = 
        'You are about to delete the folder "' + folderName + '". All documents and subfolders inside this folder will be permanently removed.';

    document.getElementById('deleteFolderConfirm').value = '';
    document.getElementById('deleteFolderBtn').disabled = true;

    document.getElementById('deleteFolderModal').style.display = 'flex';

    closeAllDropdowns();
}

function closeDeleteFolderModal() {
    document.getElementById('deleteFolderModal').style.display = 'none';
    currentDeleteFolderId = null;
    currentDeleteFolderName = null;
    document.getElementById('deleteFolderConfirm').value = '';
}

function validateDeleteConfirmation() {
    const input = document.getElementById('deleteFolderConfirm');
    const deleteBtn = document.getElementById('deleteFolderBtn');

    if (input.value.toUpperCase() === 'DELETE') {
        deleteBtn.disabled = false;
        input.style.borderColor = '#28a745';
    } else {
        deleteBtn.disabled = true;
        input.style.borderColor = '#dc3545';
    }
}

function confirmDeleteFolder() {
    console.log('=== confirmDeleteFolder called ===');
    console.log('Current delete folder ID:', currentDeleteFolderId);
    console.log('Current delete folder name:', currentDeleteFolderName);

    const folderId = currentDeleteFolderId;
    const folderName = currentDeleteFolderName;

    if (!currentDeleteFolderId || currentDeleteFolderId === 0 || !currentDeleteFolderName) {
        console.error(' Invalid folder selection for deletion');
        showError('Invalid folder selection');
        closeDeleteFolderModal();
        return;
    }

    console.log(' Proceeding with deletion of folder:', {
        id: currentDeleteFolderId,
        name: currentDeleteFolderName
    });

    showLoader();
    closeDeleteFolderModal();

    const formData = new FormData();
    formData.append('<portlet:namespace />folderId', folderId);
    formData.append('<portlet:namespace />action', 'deleteFolder');

    console.log('FormData being sent:');
    for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
    }

    console.log('Sending request to:', '<%= deleteFolderURL %>');

    fetch('<%= deleteFolderURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin',
        headers: {
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => {
        console.log('Response status:', response.status, response.statusText);
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        if (!response.ok) {
            throw new Error('Network response was not ok: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('Delete response data:', data);
        hideLoader();
        if (data.success) {
            showSuccessMessage('Folder "' + folderName + '" deleted successfully!');
            loadFolderContent();
        } else {
            showError('Failed to delete folder: ' + (data.error || data.message || 'Unknown error'));
        }
        currentDeleteFolderId = null;
        currentDeleteFolderName = null;
    })
    .catch(error => {
        hideLoader();
        console.error('Delete folder error:', error);
        showError('Error deleting folder: ' + error.message);
    });
}

function showDeleteDocumentModal(documentId, documentName) {
    if (event) {
        event.stopPropagation();
        event.preventDefault();
    }

    console.log('=== showDeleteDocumentModal called ===');
    console.log('Raw documentId:', documentId);
    console.log('Raw documentName:', documentName);

    const numericId = parseInt(documentId);
    console.log('Parsed numericId:', numericId);

    if (isNaN(numericId) || numericId <= 0) {
        console.error(' Invalid document ID:', documentId, 'converted to:', numericId);
        showError('Invalid document ID: ' + documentId);
        return;
    }

    currentDeleteDocumentId = numericId; 
    currentDeleteDocumentName = documentName;

    console.log(' Stored for deletion:', { 
        id: currentDeleteDocumentId, 
        name: currentDeleteDocumentName 
    });

    document.getElementById('deleteDocumentTitle').textContent = 'Delete "' + documentName + '"?';
    document.getElementById('deleteDocumentMessage').textContent = 
        'You are about to delete the document "' + documentName + '". This action cannot be undone.';

    document.getElementById('deleteDocumentConfirm').value = '';
    document.getElementById('deleteDocumentBtn').disabled = true;

    document.getElementById('deleteDocumentModal').style.display = 'flex';

    closeAllDropdowns();
}

function closeDeleteDocumentModal() {
    document.getElementById('deleteDocumentModal').style.display = 'none';
    currentDeleteDocumentId = null;
    currentDeleteDocumentName = null;
    document.getElementById('deleteDocumentConfirm').value = '';
}

function validateDeleteDocumentConfirmation() {
    const input = document.getElementById('deleteDocumentConfirm');
    const deleteBtn = document.getElementById('deleteDocumentBtn');

    if (input.value.toUpperCase() === 'DELETE') {
        deleteBtn.disabled = false;
        input.style.borderColor = '#28a745';
    } else {
        deleteBtn.disabled = true;
        input.style.borderColor = '#dc3545';
    }
}

function confirmDeleteDocument(){
    const documentId = currentDeleteDocumentId;
    const documentName = currentDeleteDocumentName;
    
    if (!currentDeleteDocumentId || currentDeleteDocumentId === 0 || !currentDeleteDocumentName) {
        showError('Invalid document selection');
        closeDeleteDocumentModal();
        return;
    }
    showLoader();
    closeDeleteDocumentModal();
    const formData = new FormData();
    formData.append('<portlet:namespace />documentId', documentId);
    formData.append('<portlet:namespace />action', 'deleteDocument');

    console.log('FormData being sent:');
    for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
    }

    console.log('Sending request to:', '<%= deleteDocumentURL %>');

    fetch('<%= deleteDocumentURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin',
        headers: {
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => {
        console.log('Response status:', response.status, response.statusText);
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        if (!response.ok) {
            throw new Error('Network response was not ok: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('Delete response data:', data);
        hideLoader();
        if (data.success) {
            showSuccessMessage('Document "' + documentName + '" deleted successfully!');
            loadFolderContent();
        } else {
            showError('Failed to delete document: ' + (data.error || data.message || 'Unknown error'));
        }
        currentDeleteDocumentId = null;
        currentDeleteDocumentName = null;
    })
    .catch(error => {
        hideLoader();
        console.error('Delete document error:', error);
        showError('Error deleting document: ' + error.message);
    });
}

//============================================================================
//MULTI DELETE FUNCTIONS
//============================================================================
function showMultiDeleteModal() {
    showLoader();
    closeAllDropdowns();
    document.querySelector('.fab-container').classList.remove('active');

    selectedItems = [];
    currentFilterType = 'all';
    document.getElementById('multiDeleteConfirm').value = '';
    document.getElementById('multiDeleteBtn').disabled = true;
    document.getElementById('selectAllCheckbox').checked = false;
    document.getElementById('selectAllCheckbox').indeterminate = false;
    document.getElementById('multiDeleteSearch').value = '';
    document.getElementById('multiDeleteLoading').style.display = 'flex';
    document.getElementById('multiDeleteEmpty').style.display = 'none';
    document.getElementById('multiDeleteItemsGrid').innerHTML = '';

    loadItemsForMultiDelete();
}

function closeMultiDeleteModal() {
    document.getElementById('multiDeleteModal').style.display = 'none';
    multiDeleteItems = [];
    selectedItems = [];
    document.getElementById('multiDeleteConfirm').value = '';
    document.getElementById('multiDeleteBtn').disabled = true;
    document.getElementById('selectAllCheckbox').checked = false;
    document.getElementById('selectAllCheckbox').indeterminate = false;
    document.getElementById('folderWarning').style.display = 'none';
}

function loadItemsForMultiDelete() {
    const url = '<%= getItemsForMultiDeleteURL %>';
    const formData = new FormData();
    formData.append('<portlet:namespace />folderId', currentFolderId);

    fetch(url, {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        hideLoader();
        if (data.success) {
            multiDeleteItems = data.items || [];
            console.log('Loaded items for multi-delete:', multiDeleteItems);
            renderMultiDeleteItems();
            document.getElementById('multiDeleteModal').style.display = 'flex';
        } else {
            showError(data.error || 'Failed to load items');
        }
    })
    .catch(error => {
        hideLoader();
        handleApiError(error, 'loadItemsForMultiDelete');
    });
}

function renderMultiDeleteItems() {
    const itemsGrid = document.getElementById('multiDeleteItemsGrid');
    const emptyState = document.getElementById('multiDeleteEmpty');
    const loadingState = document.getElementById('multiDeleteLoading');

    if (multiDeleteItems.length === 0) {
        itemsGrid.innerHTML = '';
        emptyState.style.display = 'flex';
        loadingState.style.display = 'none';
        document.getElementById('totalCount').textContent = '0 total';
        document.getElementById('selectAllCheckbox').checked = false;
        document.getElementById('selectAllCheckbox').indeterminate = false;
        updateSelectionCount();
        updateTypeCounts();
        return;
    }

    emptyState.style.display = 'none';
    loadingState.style.display = 'none';

    let itemsHTML = '';
    let visibleCount = 0;
    const searchTerm = document.getElementById('multiDeleteSearch').value.toLowerCase();

    // Update type counts
    updateTypeCounts();

    multiDeleteItems.forEach(function(item, index) {
        // Check if item matches current filter type
        const matchesType = currentFilterType === 'all' || currentFilterType === item.type;
        
        // Check if item matches search term
        const matchesSearch = !searchTerm || item.name.toLowerCase().includes(searchTerm);
        
        if (!matchesType || !matchesSearch) {
            return; 
        }
        
        visibleCount++;

        const isChecked = selectedItems.some(function(selected) {
            return selected.id === item.id && selected.type === item.type;
        });

        const iconType = item.type === 'folder' ? 'folder' : getFileIconType(item.extension);
        const iconClass = item.type === 'folder' ? 'fa-folder text-warning' : 'fa-file text-primary';
        const size = item.type === 'folder' ? '&mdash;' : formatFileSize(item.size);
        const modifiedDate = item.modifiedDate || item.createDate || '';
        const displayDate = modifiedDate ? formatHistoryDate(modifiedDate) : '';
        const displayName = getSmartTruncatedName(item.name, 35);
        const extension = item.extension ? item.extension.toUpperCase() : '';

        itemsHTML +=
            '<div class="grid-item' + (isChecked ? ' selected' : '') + '" ' +
                'data-type="' + item.type + '" ' +
                'data-id="' + item.id + '" ' +
                'style="display: flex;">' +
                '<div class="grid-checkbox">' +
                    '<div class="custom-control custom-checkbox">' +
                        '<input type="checkbox" class="custom-control-input item-checkbox" ' +
                                'id="item_' + item.type + '_' + item.id + '" ' +
                                (isChecked ? 'checked' : '') + ' ' +
                                'onchange="toggleItemSelection(' + index + ', this)">' +
                        '<label class="custom-control-label" for="item_' + item.type + '_' + item.id + '"></label>' +
                    '</div>' +
                '</div>' +
                '<div class="grid-name">' +
                    '<div class="item-name-container">' +
                        '<i class="fa ' + iconClass + ' mr-2"></i>' +
                        '<div class="item-name-details">' +
                            '<div class="item-name" title="' + escapeHtml(item.name) + '">' + escapeHtml(displayName) + '</div>' +
                            (item.type === 'document' ? 
                                '<div class="item-extension">' + extension + '</div>' : 
                                '<div class="item-type">Folder</div>'
                            ) +
                        '</div>' +
                    '</div>' +
                '</div>' +
                '<div class="grid-type">' +
                    '<span class="type-badge ' + (item.type === 'folder' ? 'badge-folder' : 'badge-document') + '">' +
                        (item.type === 'folder' ? 'Folder' : 'Document') +
                    '</span>' +
                '</div>' +
                '<div class="grid-size">' + size + '</div>' +
                '<div class="grid-date">' + displayDate + '</div>' +
            '</div>';
    });

    itemsGrid.innerHTML = itemsHTML;

    document.getElementById('totalCount').textContent = visibleCount + ' total';

    updateSelectAllCheckbox();
    updateSelectionCount();
    updateSummary();
    validateMultiDeleteConfirmation();

    if (visibleCount === 0) {
        emptyState.style.display = 'flex';
        document.getElementById('selectAllCheckbox').checked = false;
        document.getElementById('selectAllCheckbox').indeterminate = false;
    }
}

function updateTypeCounts() {
    const folders = multiDeleteItems.filter(item => item.type === 'folder').length;
    const documents = multiDeleteItems.filter(item => item.type === 'document').length;

    document.getElementById('folderCountBadge').textContent = folders;
    document.getElementById('documentCountBadge').textContent = documents;
}

function filterMultiDeleteItems() {
    const searchTerm = document.getElementById('multiDeleteSearch').value.toLowerCase();
    const searchInput = document.getElementById('multiDeleteSearch');
    const searchClear = document.querySelector('.search-clear');

    if (searchTerm) {
        searchClear.style.display = 'flex';
    } else {
        searchClear.style.display = 'none';
    }

    const items = document.querySelectorAll('.grid-item');

    let visibleCount = 0;

    items.forEach(item => {
        const itemName = item.querySelector('.item-name').textContent.toLowerCase();
        const itemType = item.getAttribute('data-type');
        
       
        const matchesType = currentFilterType === 'all' || currentFilterType === itemType;
        const matchesSearch = !searchTerm || itemName.includes(searchTerm);
        
        if (matchesType && matchesSearch) {
            item.style.display = 'flex';
            visibleCount++;
        } else {
            item.style.display = 'none';
        }
    });

    const emptyState = document.getElementById('multiDeleteEmpty');
    if (visibleCount === 0) {
        emptyState.style.display = 'flex';
        if (searchTerm) {
            emptyState.querySelector('.empty-text h4').textContent = 'No matching items found';
            emptyState.querySelector('.empty-text p').textContent = 'No items match your search criteria.';
            emptyState.querySelector('.empty-icon i').className = 'fa fa-search fa-3x';
        } else {
            emptyState.querySelector('.empty-text h4').textContent = 'No items found';
            emptyState.querySelector('.empty-text p').textContent = 'No items match your filter criteria.';
            emptyState.querySelector('.empty-icon i').className = 'fa fa-folder-open fa-3x';
        }
    } else {
        emptyState.style.display = 'none';
    }

    document.getElementById('totalCount').textContent = visibleCount + ' total';

    updateSelectAllCheckbox();
}

function clearSearch() {
    document.getElementById('multiDeleteSearch').value = '';
    document.querySelector('.search-clear').style.display = 'none';
    filterMultiDeleteItems();
}

function filterByType(type) {
    currentFilterType = type;

   
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
        if (tab.getAttribute('data-type') === type) {
            tab.classList.add('active');
        }
    });

   
    document.getElementById('selectAllCheckbox').checked = false;
    document.getElementById('selectAllCheckbox').indeterminate = false;

    filterMultiDeleteItems();
}

function toggleItemSelection(index, checkbox) {
    const item = multiDeleteItems[index];

    if (checkbox.checked) {
        if (!selectedItems.some(selected => selected.id === item.id && selected.type === item.type)) {
            selectedItems.push({
                id: item.id,
                type: item.type,
                name: item.name
            });
        }
    } else {
        selectedItems = selectedItems.filter(selected => 
            !(selected.id === item.id && selected.type === item.type)
        );
    }

    const gridItem = checkbox.closest('.grid-item');
    if (checkbox.checked) {
        gridItem.classList.add('selected');
    } else {
        gridItem.classList.remove('selected');
    }

    updateSelectAllCheckbox();
    updateSelectionCount();
    updateSummary();
    validateMultiDeleteConfirmation();
}

function toggleSelectAll(checkbox) {
   
    const filteredItems = multiDeleteItems.filter(item => {
       
        const matchesType = currentFilterType === 'all' || currentFilterType === item.type;
        
      
        const searchTerm = document.getElementById('multiDeleteSearch').value.toLowerCase();
        const matchesSearch = !searchTerm || item.name.toLowerCase().includes(searchTerm);
        
        return matchesType && matchesSearch;
    });

    if (checkbox.checked) {
      
        filteredItems.forEach(item => {
            if (!selectedItems.some(selected => selected.id === item.id && selected.type === item.type)) {
                selectedItems.push({
                    id: item.id,
                    type: item.type,
                    name: item.name
                });
            }
        });
    } else {
      
        selectedItems = selectedItems.filter(selected => {
           
            const isInFiltered = filteredItems.some(item => 
                item.id === selected.id && item.type === selected.type
            );
          
            return !isInFiltered;
        });
    }

   
    document.querySelectorAll('.item-checkbox').forEach((cb, index) => {
        const item = multiDeleteItems[index];
       
        const isVisible = filteredItems.some(filteredItem => 
            filteredItem.id === item.id && filteredItem.type === item.type
        );
        
        if (isVisible) {
            cb.checked = checkbox.checked;
            const gridItem = cb.closest('.grid-item');
            if (checkbox.checked) {
                gridItem.classList.add('selected');
            } else {
                gridItem.classList.remove('selected');
            }
        }
    });

    updateSelectAllCheckbox();
    updateSelectionCount();
    updateSummary();
    validateMultiDeleteConfirmation();
}

function updateSelectAllCheckbox() {
    
    const filteredItems = multiDeleteItems.filter(item => {
        const matchesType = currentFilterType === 'all' || currentFilterType === item.type;
        const searchTerm = document.getElementById('multiDeleteSearch').value.toLowerCase();
        const matchesSearch = !searchTerm || item.name.toLowerCase().includes(searchTerm);
        
        return matchesType && matchesSearch;
    });

    
    const filteredSelected = selectedItems.filter(selected => {
        return filteredItems.some(item => item.id === selected.id && item.type === selected.type);
    });

    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    if (filteredItems.length === 0) {
        selectAllCheckbox.checked = false;
        selectAllCheckbox.indeterminate = false;
        selectAllCheckbox.disabled = true;
    } else {
        selectAllCheckbox.disabled = false;
        if (filteredSelected.length === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        } else if (filteredSelected.length === filteredItems.length) {
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        }
    }
}

function selectAllItems() {
    const checkbox = document.getElementById('selectAllCheckbox');
    checkbox.checked = true;
    checkbox.indeterminate = false;
    toggleSelectAll(checkbox);
}

function deselectAllItems() {
    const checkbox = document.getElementById('selectAllCheckbox');
    checkbox.checked = false;
    checkbox.indeterminate = false;
    toggleSelectAll(checkbox);
}

function updateSelectionCount() {
    const selectedCount = selectedItems.length;
    document.getElementById('selectedCount').textContent = selectedCount + ' selected';
    document.getElementById('deleteCount').textContent = selectedCount;
}

function updateSummary() {
    const summaryElement = document.getElementById('summaryDetails');

    if (selectedItems.length === 0) {
        summaryElement.innerHTML =
            '<div class="detail-placeholder">' +
                '<i class="fa fa-mouse-pointer"></i>' +
                '<p>Select items to see deletion summary</p>' +
            '</div>';
        
        document.getElementById('folderWarning').style.display = 'none';
        return;
    }

    const folders = selectedItems.filter(function (item) {
        return item.type === 'folder';
    }).length;

    const documents = selectedItems.filter(function (item) {
        return item.type === 'document';
    }).length;

    let summaryHTML =
        '<div class="summary-stats">' +
            '<div class="stat-item">' +
                '<div class="stat-icon">' +
                    '<i class="fa fa-folder text-warning"></i>' +
                '</div>' +
                '<div class="stat-details">' +
                    '<div class="stat-value">' + folders + '</div>' +
                    '<div class="stat-label">' + (folders === 1 ? 'Folder' : 'Folders') + '</div>' +
                '</div>' +
            '</div>' +
            '<div class="stat-item">' +
                '<div class="stat-icon">' +
                    '<i class="fa fa-file text-primary"></i>' +
                '</div>' +
                '<div class="stat-details">' +
                    '<div class="stat-value">' + documents + '</div>' +
                    '<div class="stat-label">' + (documents === 1 ? 'Document' : 'Documents') + '</div>' +
                '</div>' +
            '</div>' +
        '</div>';

    summaryElement.innerHTML = summaryHTML;

    
    if (folders > 0) {
        document.getElementById('folderWarning').style.display = 'flex';
    } else {
        document.getElementById('folderWarning').style.display = 'none';
    }
}

function validateMultiDeleteConfirmation() {
    const input = document.getElementById('multiDeleteConfirm');
    const deleteBtn = document.getElementById('multiDeleteBtn');

    if (input.value.toUpperCase() === 'DELETE ALL' && selectedItems.length > 0) {
        deleteBtn.disabled = false;
        input.style.borderColor = '#28a745';
        input.style.boxShadow = '0 0 0 0.2rem rgba(40, 167, 69, 0.25)';
    } else {
        deleteBtn.disabled = true;
        input.style.borderColor = selectedItems.length > 0 ? '#dc3545' : '#ced4da';
        input.style.boxShadow = 'none';
    }
}

function confirmMultiDelete() {
    console.log('=== confirmMultiDelete called ===');
    console.log('Selected items:', selectedItems);

    const selectItems = selectedItems;

    if (selectedItems.length === 0) {
        showError('No items selected for deletion');
        return;
    }

    const confirmation = document.getElementById('multiDeleteConfirm').value;
    if (confirmation.toUpperCase() !== 'DELETE ALL') {
        showError('Please type "DELETE ALL" to confirm');
        return;
    }

   
    const invalidItems = selectedItems.filter(item => !item.id || item.id === 0);
    if (invalidItems.length > 0) {
        console.error('Invalid items found:', invalidItems);
        showError('Some selected items have invalid IDs');
        return;
    }

    console.log(' Proceeding with multi-delete of', selectedItems.length, 'items');

    showLoader();
    closeMultiDeleteModal();

    const formData = new FormData();
    formData.append('<portlet:namespace />selectedItems', JSON.stringify(selectItems));
    formData.append('<portlet:namespace />folderId', currentFolderId);
    formData.append('<portlet:namespace />action', 'multiDelete');

    console.log('FormData being sent for multi-delete:');
    for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
    }

    console.log('Sending request to:', '<%= multiDeleteItemsURL %>');

    fetch('<%= multiDeleteItemsURL %>', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin',
        headers: {
            'Cache-Control': 'no-cache'
        }
    })
    .then(response => {
        console.log('Response status:', response.status, response.statusText);
        if (response.status === 401 || response.status === 403) {
            throw new Error('Session expired');
        }
        if (!response.ok) {
            throw new Error('Network response was not ok: ' + response.status);
        }
        return response.json();
    })
    .then(data => {
        console.log('Multi-delete response data:', data);
        hideLoader();
        if (data.success) {
            const totalDeleted = selectItems.length;
            showSuccessMessage('Successfully deleted ' + totalDeleted + ' item' + (totalDeleted !== 1 ? 's' : '') + '!');
            loadFolderContent();
        } else {
            showError(data.error || data.message || 'Failed to delete items');
        }
    })
    .catch(error => {
        hideLoader();
        console.error('Multi delete error:', error);
        showError('Error deleting items: ' + error.message);
    });
}

//============================================================================
//HELPER FUNCTIONS
//============================================================================
function enableHoverEffect() {
    document.querySelectorAll('.folder-card, .doc-card').forEach(el => {
        el.classList.remove('menu-cstm-open');
    });
}

function disableHoverEffect() {
    document.querySelectorAll('.folder-card, .doc-card').forEach(el => {
        el.classList.add('menu-cstm-open');
    });
}

//============================================================================
//POLYFILLS FOR OLDER BROWSERS
//============================================================================
if (!String.prototype.includes) {
    String.prototype.includes = function(search, start) {
        if (typeof start !== 'number') start = 0;
        return this.indexOf(search, start) !== -1;
    };
}

if (!Element.prototype.closest) {
    Element.prototype.closest = function(s) {
        var el = this;
        do {
            if (el.matches(s)) return el;
            el = el.parentElement || el.parentNode;
        } while (el !== null && el.nodeType === 1);
        return null;
    };
}

//============================================================================
//CLEANUP ON UNLOAD
//============================================================================
window.addEventListener('beforeunload', function() {
    cleanupEventListeners();

    // Clean up any remaining modals
    const modals = ['historyModal', 'createFolderModal', 'createDocumentModal', 'deleteFolderModal', 'deleteDocumentModal', 'multiDeleteModal'];
    modals.forEach(modalId => {
        const modal = document.getElementById(modalId);
        if (modal) modal.style.display = 'none';
    });
});
</script>