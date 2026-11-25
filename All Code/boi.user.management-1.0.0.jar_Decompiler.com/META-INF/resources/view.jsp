<%@ include file="/init.jsp" %>

<portlet:defineObjects />
<portlet:resourceURL var="getProgressURL" id="getProgress" />
<portlet:actionURL name="uploadExcel" var="uploadExcelURL" />
<portlet:resourceURL var="getUserRolesURL" id="getUserRoles" />
<portlet:resourceURL var="assignRoleURL" id="assignRole" />
<portlet:resourceURL var="getRoleCountURL" id="getRoleCount" />

<div class="container">
    <!-- Tabs Header -->
    <ul class="nav nav-tabs" id="mainTabs" role="tablist">
        <li class="nav-item">
            <a class="nav-link active" id="upload-tab" data-toggle="tab" href="#upload" role="tab">
                <i class="fa fa-upload"></i> Excel Upload
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" id="role-access-tab" data-toggle="tab" href="#roleAccess" role="tab">
                <i class="fa fa-user-plus"></i> Give Role Access
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" id="role-count-tab" data-toggle="tab" href="#roleCount" role="tab">
                <i class="fa fa-bar-chart"></i> Role Count Dashboard
            </a>
        </li>
    </ul>

    <!-- Tab Content -->
    <div class="tab-content">
        
        <!-- Tab 1: Excel Upload -->
        <div class="tab-pane fade show active" id="upload" role="tabpanel">
            <div class="tab-content-inner">
                <h3>Upload Excel File for PSRS Role Assignment</h3>
                <p class="text-muted">Upload Excel file with ADID, Role Type, and Organization Type columns to assign PSRS roles to users.</p>
                
                <form id="uploadForm" action="<%= uploadExcelURL %>" method="post" enctype="multipart/form-data">
                    <div class="form-group">
                        <label for="excelFiles">Select Excel File (.xlsx):</label>
                        <input type="file" name="excelFiles" accept=".xlsx" required class="form-control" id="excelFiles">
                        <small class="form-text text-muted">Required columns: ADID, ROLE(Maker/Checker), Organisation Type(Branch/Zone/FGMO/HO)</small>
                    </div>
                    
                    <button type="submit" class="btn btn-primary" id="uploadBtn">
                        <i class="fa fa-upload"></i> Upload and Process
                    </button>
                </form>

                <!-- Progress Section -->
                <div id="progressSection" style="display: none; margin-top: 20px;">
                    <div class="card">
                        <div class="card-header">
                            <h4>Processing Progress</h4>
                        </div>
                        <div class="card-body">
                            <div class="progress">
                                <div id="progressBar" class="progress-bar progress-bar-striped progress-bar-animated" 
                                     role="progressbar" style="width: 0%">0%</div>
                            </div>
                            
                            <div class="mt-3">
                                <p><strong>Status:</strong> <span id="statusText">Initializing...</span></p>
                                <p><strong>Processed:</strong> <span id="processedCount">0</span> / <span id="totalCount">0</span> records</p>
                                <p><strong>Success:</strong> <span id="successCount" class="text-success">0</span> | 
                                   <strong>Failed:</strong> <span id="failedCount" class="text-danger">0</span></p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Results Section -->
                <div id="resultsSection" style="display: none; margin-top: 20px;">
                    <div class="card">
                        <div class="card-header">
                            <ul class="nav nav-tabs card-header-tabs" id="resultTabs" role="tablist">
                                <li class="nav-item">
                                    <a class="nav-link active" id="success-tab" data-toggle="tab" href="#success" role="tab">
                                        Success (<span id="successTabCount">0</span>)
                                    </a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" id="failed-tab" data-toggle="tab" href="#failed" role="tab">
                                        Failed (<span id="failedTabCount">0</span>)
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="card-body">
                            <div class="tab-content">
                                <!-- Success Tab -->
                                <div class="tab-pane fade show active" id="success" role="tabpanel">
                                    <div class="button-group mb-3">
                                        <button type="button" class="btn btn-sm btn-success" id="copySuccessADIDsBtn">
                                            <i class="fa fa-copy"></i> Copy All Success ADIDs
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-success" id="copySuccessADIDsCommaBtn">
                                            <i class="fa fa-copy"></i> Copy as Comma Separated
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-success" id="copySuccessADIDsNewLineBtn">
                                            <i class="fa fa-copy"></i> Copy as New Line Separated
                                        </button>
                                    </div>
                                    <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                                        <table class="table table-bordered table-hover" id="successTable">
                                            <thead style="position: sticky; top: 0; z-index: 10;">
                                                <tr style="background-color: black;">
                                                    <th style="background-color: black; color: white; width: 20%;">ADID</th>
                                                    <th style="background-color: black; color: white; width: 20%;">Organisation Type</th>
                                                    <th style="background-color: black; color: white; width: 20%;">ROLE (Maker/Checker)</th>
                                                    <th style="background-color: black; color: white; width: 15%;">Status</th>
                                                    <th style="background-color: black; color: white; width: 25%;">Assigned Roles</th>
                                                </tr>
                                            </thead>
                                            <tbody id="successTableBody">
                                                <tr><td colspan="5" class="text-center text-muted">No data available</td></tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                
                                <!-- Failed Tab -->
                                <div class="tab-pane fade" id="failed" role="tabpanel">
                                    <div class="button-group mb-3">
                                        <button type="button" class="btn btn-sm btn-danger" id="copyFailedADIDsBtn">
                                            <i class="fa fa-copy"></i> Copy All Failed ADIDs
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-danger" id="copyFailedADIDsCommaBtn">
                                            <i class="fa fa-copy"></i> Copy as Comma Separated
                                        </button>
                                        <button type="button" class="btn btn-sm btn-outline-danger" id="copyFailedADIDsNewLineBtn">
                                            <i class="fa fa-copy"></i> Copy as New Line Separated
                                        </button>
                                    </div>
                                    <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                                        <table class="table table-bordered table-hover" id="failedTable">
                                            <thead style="position: sticky; top: 0; z-index: 10;">
                                                <tr style="background-color: black;">
                                                    <th style="background-color: black; color: white; width: 15%;">ADID</th>
                                                    <th style="background-color: black; color: white; width: 20%;">Organisation Type</th>
                                                    <th style="background-color: black; color: white; width: 20%;">ROLE (Maker/Checker)</th>
                                                    <th style="background-color: black; color: white; width: 10%;">Status</th>
                                                    <th style="background-color: black; color: white; width: 35%;">Reason</th>
                                                </tr>
                                            </thead>
                                            <tbody id="failedTableBody">
                                                <tr><td colspan="5" class="text-center text-muted">No data available</td></tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Tab 2: Give Role Access -->
        <div class="tab-pane fade" id="roleAccess" role="tabpanel">
            <div class="tab-content-inner">
                <h3>Give Role Access to User</h3>
                <p class="text-muted">Enter ADID to view and assign PSRS roles.</p>
                
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="adidInput">ADID:</label>
                            <div class="input-group">
                                <input type="text" class="form-control" id="adidInput" placeholder="Enter ADID">
                                <div class="input-group-append">
                                    <button class="btn btn-primary" id="checkUserBtn" type="button">
                                        <i class="fa fa-search"></i> Check User
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- User Info Section -->
                <div id="userInfoSection" style="display: none; margin-top: 20px;">
                    <div class="card">
                        <div class="card-header bg-info text-white">
                            <h5 class="mb-0"><i class="fa fa-user"></i> User Information</h5>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>ADID:</strong> <span id="userADID"></span></p>
                                    <p><strong>Name:</strong> <span id="userName"></span></p>
                                    <p><strong>Email:</strong> <span id="userEmail"></span></p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Current Role Type:</strong></p>
                                    <div id="currentRoles" class="role-badge"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Role Selection Section -->
                <div id="roleSelectionSection" style="display: none; margin-top: 20px;">
                    <div class="card">
                        <div class="card-header bg-success text-white">
                            <h5 class="mb-0"><i class="fa fa-tasks"></i> Available Role Assignments</h5>
                        </div>
                        <div class="card-body">
                            <p class="text-muted mb-3">Click on any role below to convert the user. Current role is highlighted in green.</p>
                            <div class="row">
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Branch Maker</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS Branch Maker</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="branch_maker">
                                            <i class="fa fa-user-plus"></i> Convert to Branch Maker
                                        </button>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Branch Checker</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS Branch Maker, PSRS Branch Checker</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="branch_checker">
                                            <i class="fa fa-user-plus"></i> Convert to Branch Checker
                                        </button>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Zone Maker</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS Zone Maker</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="zone_maker">
                                            <i class="fa fa-user-plus"></i> Convert to Zone Maker
                                        </button>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Zone Checker</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS Zone Maker, PSRS Zone Checker</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="zone_checker">
                                            <i class="fa fa-user-plus"></i> Convert to Zone Checker
                                        </button>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Recommending Authority (RA DZM)</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS RA DZM</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="ra_dzm">
                                            <i class="fa fa-user-plus"></i> Convert to RA DZM
                                        </button>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="role-card">
                                        <h5>Noting Authority</h5>
                                        <p class="text-muted small">Assigns: PSRS, PSRS Noting Authority</p>
                                        <button class="btn btn-outline-success btn-block assign-role-btn" data-role="noting_authority">
                                            <i class="fa fa-user-plus"></i> Convert to Noting Authority
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Message Section -->
                <div id="roleAccessMessageDiv" style="display: none; margin-top: 20px;"></div>
            </div>
        </div>

        <!-- Tab 3: Role Count Dashboard -->
        <div class="tab-pane fade" id="roleCount" role="tabpanel">
            <div class="tab-content-inner">
                <h3>Role Count Dashboard</h3>
                <p class="text-muted">View how many users have each PSRS role. Click on any card to see the list of ADIDs.</p>
                
                <div class="row mb-3">
                    <div class="col-md-12">
                        <button class="btn btn-primary" id="refreshRoleCountBtn">
                            <i class="fa fa-refresh"></i> Refresh Counts
                        </button>
                    </div>
                </div>
                
                <div class="row" id="roleCountCards">
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS">
                            <div class="role-count-header" style="background-color: #0b5fff;">
                                <h4>PSRS</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countPSRS">0</div>
                                <p>Users with base PSRS role</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_Branch_Maker">
                            <div class="role-count-header" style="background-color: #28a745;">
                                <h4>PSRS Branch Maker</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countBranchMaker">0</div>
                                <p>Users who can make branch entries</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_Branch_Checker">
                            <div class="role-count-header" style="background-color: #17a2b8;">
                                <h4>PSRS Branch Checker</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countBranchChecker">0</div>
                                <p>Users who can check branch entries</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_Zone_Maker">
                            <div class="role-count-header" style="background-color: #ffc107;">
                                <h4>PSRS Zone Maker</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countZoneMaker">0</div>
                                <p>Users who can make zone entries</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_Zone_Checker">
                            <div class="role-count-header" style="background-color: #fd7e14;">
                                <h4>PSRS Zone Checker</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countZoneChecker">0</div>
                                <p>Users who can check zone entries</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_RA_DZM">
                            <div class="role-count-header" style="background-color: #6f42c1;">
                                <h4>PSRS RA DZM</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countRADZM">0</div>
                                <p>Recommending Authority users</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="role-count-card" data-role-key="PSRS_Noting_Authority">
                            <div class="role-count-header" style="background-color: #e83e8c;">
                                <h4>PSRS Noting Authority</h4>
                            </div>
                            <div class="role-count-body">
                                <div class="count-number" id="countNotingAuthority">0</div>
                                <p>Noting Authority users</p>
                                <small class="text-muted">Click to view users</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Error Message -->
    <div id="errorMessageDiv" style="display: none; margin-top: 20px;">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <span id="errorMessage"></span>
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    </div>
</div>

<!-- CSS Styles -->
<style>
.container {
    padding: 20px;
    max-width: 1400px;
    margin: 0 auto;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.nav-tabs {
    border-bottom: 2px solid #ddd;
    margin-bottom: 20px;
}

.nav-tabs .nav-link {
    padding: 12px 20px;
    font-size: 16px;
    font-weight: 500;
    color: #666;
    border: none;
}

.nav-tabs .nav-link.active {
    color: #0b5fff;
    border-bottom: 2px solid #0b5fff;
    background: none;
}

.tab-content-inner {
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.form-group {
    margin-bottom: 20px;
}

.form-control {
    display: block;
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.btn {
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
}

.btn-primary {
    background: #0b5fff;
    color: white;
    border: none;
}

.btn-primary:hover {
    background: #0053f0;
}

.btn-success {
    background-color: #28a745;
    color: white;
    border: none;
}

.btn-danger {
    background-color: #dc3545;
    color: white;
    border: none;
}

.btn-sm {
    padding: 5px 10px;
    font-size: 12px;
}

.btn-block {
    width: 100%;
}

.role-card {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
    text-align: center;
    transition: transform 0.2s;
    background: #f8f9fa;
}

.role-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.role-card h5 {
    margin-bottom: 10px;
    color: #333;
}

.role-count-card {
    border: 1px solid #ddd;
    border-radius: 8px;
    margin-bottom: 20px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
}

.role-count-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.role-count-header {
    padding: 15px;
    text-align: center;
}

.role-count-header h4 {
    margin: 0;
    color: white;
}

.role-count-body {
    padding: 20px;
    text-align: center;
    background: #fff;
}

.count-number {
    font-size: 48px;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
}

.button-group {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-bottom: 15px;
}

.progress {
    background-color: #f0f0f0;
    border-radius: 4px;
    height: 30px;
    overflow: hidden;
}

.progress-bar {
    background-color: #0b5fff;
    line-height: 30px;
    color: white;
    text-align: center;
    transition: width 0.3s ease;
}

.card {
    border: 1px solid #ddd;
    border-radius: 8px;
    margin-bottom: 20px;
}

.card-header {
    background-color: #f8f9fa;
    padding: 12px 15px;
    border-bottom: 1px solid #ddd;
    font-weight: bold;
}

.table-responsive {
    overflow-x: auto;
    border-radius: 4px;
}

.table {
    width: 100%;
    margin-bottom: 0;
    border-collapse: collapse;
}

.table th, .table td {
    padding: 10px;
    border: 1px solid #ddd;
    vertical-align: top;
}

.table th {
    background-color: black;
    color: white;
    font-weight: 600;
}

.table td {
    word-wrap: break-word;
    word-break: break-word;
    white-space: normal;
}

.table tbody tr:hover {
    background-color: #f5f5f5;
}

.text-success {
    color: #28a745;
    font-weight: bold;
}

.text-danger {
    color: #dc3545;
    font-weight: bold;
}

.text-muted {
    color: #6c757d;
}

.text-center {
    text-align: center;
}

.mt-3 {
    margin-top: 15px;
}

.mb-3 {
    margin-bottom: 15px;
}

.fa {
    margin-right: 5px;
}

.alert {
    padding: 12px 15px;
    border-radius: 4px;
    margin-bottom: 15px;
}

.alert-success {
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
    color: #155724;
}

.alert-danger {
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    color: #721c24;
}

.alert-warning {
    background-color: #fff3cd;
    border: 1px solid #ffeeba;
    color: #856404;
}

.alert-info {
    background-color: #d1ecf1;
    border: 1px solid #bee5eb;
    color: #0c5460;
}

.role-badge {
    display: inline-block;
    padding: 8px 15px;
    background-color: #0b5fff;
    color: white;
    border-radius: 20px;
    font-weight: bold;
}

/* Modal styles */
.modal-content {
    border-radius: 8px;
}

.modal-header {
    border-bottom: none;
    border-radius: 8px 8px 0 0;
}

#adidsTableBody tr:hover {
    background-color: #f5f5f5;
}

.copy-single-adid {
    padding: 2px 8px;
    font-size: 11px;
}

/* Scrollbar styling */
.table-responsive::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

.table-responsive::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-thumb:hover {
    background: #555;
}

.reason-container {
    position: relative;
    line-height: 1.4;
}

.btn-link {
    background: none;
    border: none;
    color: #0b5fff;
    cursor: pointer;
    text-decoration: underline;
    padding: 0;
    margin-left: 5px;
    font-size: 12px;
}

.btn-link:hover {
    color: #0053f0;
}

thead tr th {
    position: sticky;
    top: 0;
    z-index: 10;
}

.table tbody tr:nth-child(even) {
    background-color: #f8f9fa;
}

.table tbody tr:hover {
    background-color: #e9ecef;
}
</style>



<script>

let progressInterval = null;
let backgroundTaskId = null;
let currentSuccessADIDs = [];
let currentFailedADIDs = [];
let currentSuccessData = [];
let currentFailedData = [];
let currentUserADID = null;
let roleADIDsMap = {};

const namespace = '<portlet:namespace />';

console.log('Script loaded and initialized');

// ==================== Helper Functions ====================

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function copyToClipboard(text, successMessage) {
    if (!text || text.length === 0) {
        showWarning('No data to copy');
        return;
    }
    
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function() {
            showSuccess(successMessage || 'Copied to clipboard!');
        }).catch(function(err) {
            console.error('Could not copy text: ', err);
            fallbackCopy(text, successMessage);
        });
    } else {
        fallbackCopy(text, successMessage);
    }
}

function fallbackCopy(text, successMessage) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
        document.execCommand('copy');
        showSuccess(successMessage || 'Copied to clipboard!');
    } catch (err) {
        console.error('Fallback copy failed:', err);
        showError('Failed to copy to clipboard');
    }
    document.body.removeChild(textarea);
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessageDiv');
    const errorSpan = document.getElementById('errorMessage');
    if (errorSpan) errorSpan.innerHTML = escapeHtml(message);
    if (errorDiv) {
        errorDiv.style.display = 'block';
        errorDiv.className = 'alert alert-danger alert-dismissible fade show';
    }
    setTimeout(function() {
        if (errorDiv) errorDiv.style.display = 'none';
    }, 5000);
}

function showSuccess(message) {
    const errorDiv = document.getElementById('errorMessageDiv');
    const errorSpan = document.getElementById('errorMessage');
    if (errorSpan) errorSpan.innerHTML = escapeHtml(message);
    if (errorDiv) {
        errorDiv.style.display = 'block';
        errorDiv.className = 'alert alert-success alert-dismissible fade show';
    }
    setTimeout(function() {
        if (errorDiv) errorDiv.style.display = 'none';
    }, 5000);
}

function showWarning(message) {
    const errorDiv = document.getElementById('errorMessageDiv');
    const errorSpan = document.getElementById('errorMessage');
    if (errorSpan) errorSpan.innerHTML = escapeHtml(message);
    if (errorDiv) {
        errorDiv.style.display = 'block';
        errorDiv.className = 'alert alert-warning alert-dismissible fade show';
    }
    setTimeout(function() {
        if (errorDiv) errorDiv.style.display = 'none';
    }, 5000);
}

function showInfo(message) {
    const errorDiv = document.getElementById('errorMessageDiv');
    const errorSpan = document.getElementById('errorMessage');
    if (errorSpan) errorSpan.innerHTML = escapeHtml(message);
    if (errorDiv) {
        errorDiv.style.display = 'block';
        errorDiv.className = 'alert alert-info alert-dismissible fade show';
    }
    setTimeout(function() {
        if (errorDiv) errorDiv.style.display = 'none';
    }, 3000);
}

// ==================== Tab 1: Excel Upload Functions ====================

document.getElementById('uploadForm').addEventListener('submit', function(e) {
    console.log('Form submit event triggered');
    e.preventDefault();
    
    const fileInput = document.getElementById('excelFiles');
    if (!fileInput.files || fileInput.files.length === 0) {
        showError('Please select a file to upload');
        return;
    }
    
    const formData = new FormData(this);
    const uploadBtn = document.getElementById('uploadBtn');
    const progressSection = document.getElementById('progressSection');
    
    progressSection.style.display = 'block';
    uploadBtn.disabled = true;
    uploadBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Uploading...';
    
    resetDisplays();
    
    const uploadURL = '<%= uploadExcelURL %>';
    
    fetch(uploadURL, {
        method: 'POST',
        body: formData,
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {
        if (data.success && data.backgroundTaskId) {
            backgroundTaskId = data.backgroundTaskId;
            startProgressPolling();
            uploadBtn.innerHTML = '<i class="fa fa-check"></i> Processing...';
        } else {
            showError(data.message || 'Failed to start background task');
            uploadBtn.disabled = false;
            uploadBtn.innerHTML = '<i class="fa fa-upload"></i> Upload and Process';
            progressSection.style.display = 'none';
        }
    })
    .catch(function(error) {
        showError('Error uploading file: ' + error.message);
        uploadBtn.disabled = false;
        uploadBtn.innerHTML = '<i class="fa fa-upload"></i> Upload and Process';
        progressSection.style.display = 'none';
    });
});

function startProgressPolling() {
    if (progressInterval) clearInterval(progressInterval);
    
    progressInterval = setInterval(function() {
        const params = new URLSearchParams();
        params.append(namespace + 'backgroundTaskId', backgroundTaskId);
        
        fetch('<%= getProgressURL %>', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            updateProgress(data);
            if (data.status === 'SUCCESSFUL' || data.status === 'FAILED' || data.statusText === 'Completed') {
                clearInterval(progressInterval);
                progressInterval = null;
                finalizeResults(data);
            }
        })
        .catch(function(error) {
            console.error('Error polling progress:', error);
        });
    }, 2000);
}

function updateProgress(data) {
    const progress = data.progress || 0;
    const statusText = data.statusText || 'Processing...';
    const processed = data.processed || 0;
    const total = data.total || 0;
    const successCount = data.successCount || 0;
    const failedCount = data.failedCount || 0;
    
    const progressBar = document.getElementById('progressBar');
    if (progressBar) {
        progressBar.style.width = progress + '%';
        progressBar.innerHTML = progress + '%';
    }
    document.getElementById('statusText').innerHTML = escapeHtml(statusText);
    document.getElementById('processedCount').innerHTML = processed;
    document.getElementById('totalCount').innerHTML = total;
    document.getElementById('successCount').innerHTML = successCount;
    document.getElementById('failedCount').innerHTML = failedCount;
    
    if (progress === 100 && progressBar) {
        if (failedCount === 0) progressBar.style.backgroundColor = '#28a745';
        else if (successCount === 0) progressBar.style.backgroundColor = '#dc3545';
        else progressBar.style.backgroundColor = '#ffc107';
        progressBar.classList.remove('progress-bar-animated');
    }
}

function finalizeResults(data) {
    document.getElementById('uploadBtn').disabled = false;
    document.getElementById('uploadBtn').innerHTML = '<i class="fa fa-upload"></i> Upload and Process';
    document.getElementById('resultsSection').style.display = 'block';
    
    const successList = data.successList || [];
    const failedList = data.failedList || [];
    
    currentSuccessData = successList;
    currentFailedData = failedList;
    currentSuccessADIDs = successList.map(function(item) { return item.ADID; }).filter(function(adid) { return adid; });
    currentFailedADIDs = failedList.map(function(item) { return item.ADID; }).filter(function(adid) { return adid; });
    
    document.getElementById('successTabCount').innerHTML = successList.length;
    document.getElementById('failedTabCount').innerHTML = failedList.length;
    
    // Populate success table
    const successTableBody = document.getElementById('successTableBody');
    if (successTableBody) {
        if (successList.length === 0) {
            successTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No successful records</td></tr>';
        } else {
            successTableBody.innerHTML = '';
            successList.forEach(function(item) {
                const row = successTableBody.insertRow();
                row.insertCell(0).innerHTML = escapeHtml(item.ADID || '');
                row.insertCell(1).innerHTML = escapeHtml(item.orgType || 'N/A');
                row.insertCell(2).innerHTML = escapeHtml(item.roleType || 'N/A');
                row.insertCell(3).innerHTML = '<span class="text-success"> SUCCESS</span>';
                row.insertCell(4).innerHTML = escapeHtml(item.role || item.roles || 'N/A');
            });
        }
    }
    
    // Populate failed table
    const failedTableBody = document.getElementById('failedTableBody');
    if (failedTableBody) {
        if (failedList.length === 0) {
            failedTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No failed records</td></tr>';
        } else {
            failedTableBody.innerHTML = '';
            failedList.forEach(function(item) {
                const row = failedTableBody.insertRow();
                row.insertCell(0).innerHTML = escapeHtml(item.ADID || '');
                row.insertCell(1).innerHTML = escapeHtml(item.orgType || 'N/A');
                row.insertCell(2).innerHTML = escapeHtml(item.roleType || 'N/A');
                row.insertCell(3).innerHTML = '<span class="text-danger"> FAILED</span>';
                row.insertCell(4).innerHTML = escapeHtml(item.reason || item.message || 'Unknown error');
            });
        }
    }
}

function resetDisplays() {
    document.getElementById('resultsSection').style.display = 'none';
    document.getElementById('errorMessageDiv').style.display = 'none';
    const progressBar = document.getElementById('progressBar');
    if (progressBar) {
        progressBar.style.width = '0%';
        progressBar.innerHTML = '0%';
        progressBar.style.backgroundColor = '#0b5fff';
        progressBar.classList.add('progress-bar-animated');
    }
    document.getElementById('statusText').innerHTML = 'Initializing...';
    document.getElementById('processedCount').innerHTML = '0';
    document.getElementById('totalCount').innerHTML = '0';
    document.getElementById('successCount').innerHTML = '0';
    document.getElementById('failedCount').innerHTML = '0';
    currentSuccessADIDs = [];
    currentFailedADIDs = [];
}

// Copy button listeners
document.getElementById('copySuccessADIDsBtn')?.addEventListener('click', function() {
    if (currentSuccessADIDs.length === 0) { showWarning('No Success ADIDs to copy'); return; }
    copyToClipboard(currentSuccessADIDs.join('\n'), currentSuccessADIDs.length + ' Success ADIDs copied!');
});
document.getElementById('copySuccessADIDsCommaBtn')?.addEventListener('click', function() {
    if (currentSuccessADIDs.length === 0) { showWarning('No Success ADIDs to copy'); return; }
    copyToClipboard(currentSuccessADIDs.join(', '), currentSuccessADIDs.length + ' Success ADIDs copied!');
});
document.getElementById('copySuccessADIDsNewLineBtn')?.addEventListener('click', function() {
    if (currentSuccessADIDs.length === 0) { showWarning('No Success ADIDs to copy'); return; }
    copyToClipboard(currentSuccessADIDs.join('\n'), currentSuccessADIDs.length + ' Success ADIDs copied!');
});
document.getElementById('copyFailedADIDsBtn')?.addEventListener('click', function() {
    if (currentFailedADIDs.length === 0) { showWarning('No Failed ADIDs to copy'); return; }
    copyToClipboard(currentFailedADIDs.join('\n'), currentFailedADIDs.length + ' Failed ADIDs copied!');
});
document.getElementById('copyFailedADIDsCommaBtn')?.addEventListener('click', function() {
    if (currentFailedADIDs.length === 0) { showWarning('No Failed ADIDs to copy'); return; }
    copyToClipboard(currentFailedADIDs.join(', '), currentFailedADIDs.length + ' Failed ADIDs copied!');
});
document.getElementById('copyFailedADIDsNewLineBtn')?.addEventListener('click', function() {
    if (currentFailedADIDs.length === 0) { showWarning('No Failed ADIDs to copy'); return; }
    copyToClipboard(currentFailedADIDs.join('\n'), currentFailedADIDs.length + ' Failed ADIDs copied!');
});

// ==================== Tab 2: Give Role Access Functions ====================

document.getElementById('checkUserBtn')?.addEventListener('click', function() {
    const adid = document.getElementById('adidInput').value.trim();
    if (!adid) {
        showRoleAccessMessage('Please enter ADID', 'warning');
        return;
    }
    currentUserADID = adid;
    checkUserRoles(adid);
});

function checkUserRoles(adid) {
    showRoleAccessMessage('Checking user...', 'info');
    
    const params = new URLSearchParams();
    params.append(namespace + 'adid', adid);
    
    fetch('<%= getUserRolesURL %>', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {
        if (data.success) {
            displayUserInfo(data);
            document.getElementById('userInfoSection').style.display = 'block';
            document.getElementById('roleSelectionSection').style.display = 'block';
            hideRoleAccessMessage();
        } else {
            showRoleAccessMessage(data.message || 'User not found', 'danger');
            document.getElementById('userInfoSection').style.display = 'none';
            document.getElementById('roleSelectionSection').style.display = 'none';
        }
    })
    .catch(function(error) {
        console.error('Error checking user:', error);
        showRoleAccessMessage('Error checking user: ' + error.message, 'danger');
    });
}

function displayUserInfo(data) {
    document.getElementById('userADID').innerHTML = escapeHtml(data.adid);
    document.getElementById('userName').innerHTML = escapeHtml(data.name || 'N/A');
    document.getElementById('userEmail').innerHTML = escapeHtml(data.email || 'N/A');
    
    const roleType = data.roleType || 'No PSRS Role Assigned';
    const currentRolesDiv = document.getElementById('currentRoles');
    currentRolesDiv.innerHTML = '<div class="role-badge">' + escapeHtml(roleType) + '</div>';
    
    // Highlight current role button
    const roleCodeMap = {
        'Branch Maker': 'branch_maker',
        'Branch Checker': 'branch_checker',
        'Zone Maker': 'zone_maker',
        'Zone Checker': 'zone_checker',
        'Recommending Authority (RA DZM)': 'ra_dzm',
        'Noting Authority': 'noting_authority'
    };
    
    const roleCode = roleCodeMap[roleType];
    document.querySelectorAll('.assign-role-btn').forEach(function(btn) {
        btn.classList.remove('btn-success');
        btn.classList.add('btn-outline-success');
    });
    
    if (roleCode && roleCodeMap[roleType]) {
        const currentBtn = document.querySelector('.assign-role-btn[data-role="' + roleCode + '"]');
        if (currentBtn) {
            currentBtn.classList.remove('btn-outline-success');
            currentBtn.classList.add('btn-success');
        }
    }
}

function assignRoleToUser(adid, roleType) {
    const roleDisplayNames = {
        'branch_maker': 'Branch Maker',
        'branch_checker': 'Branch Checker',
        'zone_maker': 'Zone Maker',
        'zone_checker': 'Zone Checker',
        'ra_dzm': 'RA DZM',
        'noting_authority': 'Noting Authority'
    };
    showRoleAccessMessage('Assigning ' + (roleDisplayNames[roleType] || roleType) + ' role...', 'info');
    
    const params = new URLSearchParams();
    params.append(namespace + 'adid', adid);
    params.append(namespace + 'roleType', roleType);
    
    fetch('<%= assignRoleURL %>', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {
        if (data.success) {
            showRoleAccessMessage(data.message, 'success');
            checkUserRoles(adid);
            if (document.getElementById('role-count-tab').classList.contains('active')) {
                refreshRoleCounts();
            }
        } else {
            showRoleAccessMessage(data.message || 'Failed to assign role', 'danger');
        }
    })
    .catch(function(error) {
        console.error('Error assigning role:', error);
        showRoleAccessMessage('Error assigning role: ' + error.message, 'danger');
    });
}

document.querySelectorAll('.assign-role-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
        if (!currentUserADID) {
            showRoleAccessMessage('Please check a user first', 'warning');
            return;
        }
        assignRoleToUser(currentUserADID, this.getAttribute('data-role'));
    });
});

function showRoleAccessMessage(message, type) {
    const messageDiv = document.getElementById('roleAccessMessageDiv');
    messageDiv.innerHTML = '<div class="alert alert-' + type + ' alert-dismissible fade show">' +
        '<i class="fa fa-info-circle"></i> ' + escapeHtml(message) +
        '<button type="button" class="close" data-dismiss="alert">&times;</button></div>';
    messageDiv.style.display = 'block';
    setTimeout(function() { messageDiv.style.display = 'none'; }, 5000);
}

function hideRoleAccessMessage() {
    document.getElementById('roleAccessMessageDiv').style.display = 'none';
}

// ==================== Tab 3: Role Count Functions ====================

function refreshRoleCounts() {
    showInfo('Loading role counts...');
    
    fetch('<%= getRoleCountURL %>', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(data) {
        if (data.success) {
            document.getElementById('countPSRS').innerHTML = data.counts.PSRS || 0;
            document.getElementById('countBranchMaker').innerHTML = data.counts.PSRS_Branch_Maker || 0;
            document.getElementById('countBranchChecker').innerHTML = data.counts.PSRS_Branch_Checker || 0;
            document.getElementById('countZoneMaker').innerHTML = data.counts.PSRS_Zone_Maker || 0;
            document.getElementById('countZoneChecker').innerHTML = data.counts.PSRS_Zone_Checker || 0;
            document.getElementById('countRADZM').innerHTML = data.counts.PSRS_RA_DZM || 0;
            document.getElementById('countNotingAuthority').innerHTML = data.counts.PSRS_Noting_Authority || 0;
            
            roleADIDsMap = data.adids || {};
            attachCardClickHandlers();
            hideRoleCountMessage();
        } else {
            showRoleCountMessage(data.message || 'Failed to load role counts', 'danger');
        }
    })
    .catch(function(error) {
        console.error('Error loading role counts:', error);
        showRoleCountMessage('Error loading role counts: ' + error.message, 'danger');
    });
}

function attachCardClickHandlers() {
    document.querySelectorAll('.role-count-card').forEach(function(card) {
        card.removeEventListener('click', card.clickHandler);
        card.clickHandler = function() {
            const roleKey = card.getAttribute('data-role-key');
            const adids = roleADIDsMap[roleKey] || [];
            let roleNameElement = card.querySelector('.role-count-header h4');
            const roleName = roleNameElement ? roleNameElement.innerText : roleKey;
            showADIDsModal(roleName, adids, adids.length);
        };
        card.addEventListener('click', card.clickHandler);
    });
}

function showADIDsModal(title, adids, totalCount) {
    // Build table rows using string concatenation
    let tableRows = '';
    for (var i = 0; i < adids.length; i++) {
        var adid = adids[i];
        tableRows += '<tr>' +
            '<td>' + (i + 1) + '</td>' +
            '<td class="adid-value">' + escapeHtml(adid) + '</td>' +
            '<td><button class="btn btn-sm btn-info copy-single-adid" data-adid="' + escapeHtml(adid) + '"><i class="fa fa-copy"></i> Copy</button></td>' +
            '</tr>';
    }
    
    var modalHtml = 
        '<div class="modal fade" id="adidsModal" tabindex="-1" role="dialog">' +
            '<div class="modal-dialog modal-lg" role="document">' +
                '<div class="modal-content">' +
                    '<div class="modal-header" style="background-color: #0b5fff; color: white;">' +
                        '<h5 class="modal-title"><i class="fa fa-users"></i> ' + escapeHtml(title) + ' (' + totalCount + ' users)</h5>' +
                        '<button type="button" class="close" data-dismiss="modal"><span style="color: white;">&times;</span></button>' +
                    '</div>' +
                    '<div class="modal-body">' +
                        '<div class="form-group">' +
                            '<div class="input-group mb-3">' +
                                '<input type="text" class="form-control" id="searchADID" placeholder="Search ADID...">' +
                                '<div class="input-group-append">' +
                                    '<button class="btn btn-primary" id="copyAllADIDsBtn"><i class="fa fa-copy"></i> Copy All</button>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                        '<div style="max-height: 400px; overflow-y: auto;">' +
                            '<table class="table table-bordered table-hover">' +
                                '<thead><tr><th>#</th><th>ADID</th><th>Action</th></tr></thead>' +
                                '<tbody id="adidsTableBody">' + tableRows + '</tbody>' +
                            '</table>' +
                        '</div>' +
                    '</div>' +
                    '<div class="modal-footer">' +
                        '<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>';
    
    var existingModal = document.getElementById('adidsModal');
    if (existingModal) existingModal.remove();
    
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    var modal = new bootstrap.Modal(document.getElementById('adidsModal'));
    modal.show();
    
    document.getElementById('searchADID')?.addEventListener('keyup', function() {
        var term = this.value.toLowerCase();
        var rows = document.querySelectorAll('#adidsTableBody tr');
        for (var j = 0; j < rows.length; j++) {
            var row = rows[j];
            var adidCell = row.querySelector('.adid-value');
            var adidText = adidCell ? adidCell.textContent.toLowerCase() : '';
            row.style.display = adidText.includes(term) ? '' : 'none';
        }
    });
    
    document.getElementById('copyAllADIDsBtn')?.addEventListener('click', function() {
        var validADIDs = [];
        for (var k = 0; k < adids.length; k++) {
            if (!adids[k].startsWith('...')) {
                validADIDs.push(adids[k]);
            }
        }
        copyToClipboard(validADIDs.join('\n'), validADIDs.length + ' ADIDs copied!');
    });
    
    document.querySelectorAll('.copy-single-adid').forEach(function(btn) {
        btn.addEventListener('click', function() {
            copyToClipboard(btn.getAttribute('data-adid'), 'ADID copied!');
        });
    });
    
    document.getElementById('adidsModal')?.addEventListener('hidden.bs.modal', function() {
        var modalElement = document.getElementById('adidsModal');
        if (modalElement) modalElement.remove();
    });
}

function showRoleCountMessage(message, type) {
    var msgDiv = document.getElementById('roleCountMessageDiv');
    if (!msgDiv) {
        msgDiv = document.createElement('div');
        msgDiv.id = 'roleCountMessageDiv';
        msgDiv.style.marginTop = '20px';
        var container = document.querySelector('#roleCount .tab-content-inner');
        if (container) container.appendChild(msgDiv);
    }
    msgDiv.innerHTML = '<div class="alert alert-' + type + '">' + escapeHtml(message) + '</div>';
    msgDiv.style.display = 'block';
    setTimeout(function() { msgDiv.style.display = 'none'; }, 3000);
}

function hideRoleCountMessage() {
    var msgDiv = document.getElementById('roleCountMessageDiv');
    if (msgDiv) msgDiv.style.display = 'none';
}

// Tab click handlers
document.getElementById('role-count-tab')?.addEventListener('click', function() { refreshRoleCounts(); });
document.getElementById('refreshRoleCountBtn')?.addEventListener('click', function() { refreshRoleCounts(); });

// Tab switching for result tabs
document.getElementById('success-tab')?.addEventListener('click', function() {
    document.getElementById('failed').classList.remove('show', 'active');
    document.getElementById('success').classList.add('show', 'active');
    document.getElementById('failed-tab').classList.remove('active');
    this.classList.add('active');
});

document.getElementById('failed-tab')?.addEventListener('click', function() {
    document.getElementById('success').classList.remove('show', 'active');
    document.getElementById('failed').classList.add('show', 'active');
    document.getElementById('success-tab').classList.remove('active');
    this.classList.add('active');
});

window.addEventListener('error', function(e) {
    console.error('Global error:', e.message, 'at', e.filename, 'line', e.lineno);
});

console.log('Script initialization complete');
</script>