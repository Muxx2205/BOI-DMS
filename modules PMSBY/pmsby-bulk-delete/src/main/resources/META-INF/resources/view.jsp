<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/init.jsp" %>

<%
String success = ParamUtil.getString(request, "success");
String error = ParamUtil.getString(request, "error");
%>

<portlet:actionURL name="startBulkDelete" var="startBulkDeleteURL" />
<portlet:actionURL name="startURNBulkDelete" var="startURNBulkDelete" />
<portlet:actionURL name="startBulkWorkflowDelete" var="startBulkWorkflowDeleteURL" />
<portlet:actionURL name="startBulkUpdateAccountNo" var="startBulkUpdateAccountNoURL" />
<portlet:actionURL name="exportObjectDataByURNs" var="exportObjectDataByURNsURL" />
<portlet:actionURL name="startFinacleConsentSync" var="startFinacleConsentSyncURL" />

<style>
    /* Bootstrap 5 Custom Styles */
    :root {
        --bs-primary: #4361ee;
        --bs-success: #06ffa5;
        --bs-danger: #ef233c;
        --bs-warning: #ff9f1c;
        --bs-dark: #2b2d42;
        --bs-gray: #8d99ae;
        --bs-light: #edf2f4;
        --bs-info: #0ea5e9;
        --bs-purple: #8b5cf6;
    }
    
    .delete-container {
        padding: 2rem;
        background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
        min-height: 100vh;
    }
    
    .header-section {
        text-align: center;
        margin-bottom: 2rem;
    }
    
    .header-section h1 {
        font-size: 2.5rem;
        font-weight: 700;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin-bottom: 0.5rem;
    }
    
    .header-section p {
        color: #6c757d;
        font-size: 1rem;
    }
    
    .nav-tabs-custom {
        border-bottom: 2px solid #e9ecef;
        margin-bottom: 1.5rem;
        display: flex;
        gap: 0.5rem;
        flex-wrap: wrap;
    }
    
    .nav-tab {
        padding: 0.75rem 1.5rem;
        background: transparent;
        border: none;
        font-weight: 600;
        color: #6c757d;
        cursor: pointer;
        transition: all 0.3s;
        border-radius: 0.5rem 0.5rem 0 0;
        position: relative;
    }
    
    .nav-tab.active {
        color: #4361ee;
        background: white;
    }
    
    .nav-tab.active::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        right: 0;
        height: 2px;
        background: #4361ee;
    }
    
    .nav-tab:hover:not(.active) {
        color: #4361ee;
        background: rgba(67, 97, 238, 0.05);
    }
    
    .tab-content {
        display: none;
        animation: fadeIn 0.3s ease;
    }
    
    .tab-content.active {
        display: block;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .card-custom {
        background: white;
        border-radius: 1rem;
        box-shadow: 0 10px 40px rgba(0,0,0,0.08);
        overflow: hidden;
        margin-bottom: 1.5rem;
        border: 1px solid rgba(0,0,0,0.05);
    }
    
    .card-header-custom {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 1.25rem 1.5rem;
        font-weight: 600;
        font-size: 1.1rem;
    }
    
    .card-header-custom.export-header {
        background: linear-gradient(135deg, #0ea5e9 0%, #8b5cf6 100%);
    }
    
    .card-header-custom.finacle-header {
        background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
    }
    
    .card-header-custom i {
        margin-right: 0.5rem;
    }
    
    .card-body-custom {
        padding: 1.5rem;
    }
    
    .form-group-custom {
        margin-bottom: 1.5rem;
    }
    
    .form-label-custom {
        display: block;
        font-weight: 600;
        margin-bottom: 0.5rem;
        color: #2b2d42;
        font-size: 0.9rem;
    }
    
    .form-label-custom .required {
        color: #ef233c;
        margin-left: 0.25rem;
    }
    
    .form-control-custom {
        width: 100%;
        padding: 0.625rem 0.875rem;
        border: 1px solid #dee2e6;
        border-radius: 0.5rem;
        font-size: 0.9rem;
        transition: all 0.2s;
    }
    
    .form-control-custom:focus {
        outline: none;
        border-color: #4361ee;
        box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
    }
    
    select.form-control-custom {
        cursor: pointer;
    }
    
    .checkbox-group-custom {
        display: flex;
        flex-wrap: wrap;
        gap: 1rem;
        margin-top: 0.5rem;
    }
    
    .checkbox-custom {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        cursor: pointer;
    }
    
    .checkbox-custom input[type="checkbox"] {
        width: 1rem;
        height: 1rem;
        cursor: pointer;
    }
    
    .upload-area-custom {
        border: 2px dashed #dee2e6;
        border-radius: 0.75rem;
        padding: 2rem;
        text-align: center;
        background: #f8f9fa;
        transition: all 0.2s;
        cursor: pointer;
    }
    
    .upload-area-custom:hover {
        border-color: #4361ee;
        background: #f1f3f5;
    }
    
    .upload-icon {
        font-size: 2.5rem;
        margin-bottom: 0.5rem;
    }
    
    .upload-text {
        color: #6c757d;
        font-size: 0.9rem;
    }
    
    .upload-text strong {
        color: #4361ee;
    }
    
    .file-info-custom {
        font-size: 0.8rem;
        color: #6c757d;
        margin-top: 0.5rem;
    }
    
    .btn-custom {
        width: 100%;
        padding: 0.875rem;
        border: none;
        border-radius: 0.5rem;
        font-weight: 600;
        font-size: 1rem;
        cursor: pointer;
        transition: all 0.2s;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
    }
    
    .btn-custom:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
    
    .btn-danger-custom {
        background: linear-gradient(135deg, #ef233c 0%, #d90429 100%);
    }
    
    .btn-danger-custom:hover {
        box-shadow: 0 4px 12px rgba(239, 35, 60, 0.4);
    }
    
    .btn-export-custom {
        background: linear-gradient(135deg, #0ea5e9 0%, #8b5cf6 100%);
    }
    
    .btn-finacle-custom {
        background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
    }
    
    .alert-custom {
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 1.5rem;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        animation: slideDown 0.3s ease;
    }
    
    @keyframes slideDown {
        from { transform: translateY(-20px); opacity: 0; }
        to { transform: translateY(0); opacity: 1; }
    }
    
    .alert-success-custom {
        background: #d1fae5;
        border-left: 4px solid #10b981;
        color: #065f46;
    }
    
    .alert-danger-custom {
        background: #fee2e2;
        border-left: 4px solid #ef233c;
        color: #991b1b;
    }
    
    .info-box-custom {
        background: #e0f2fe;
        border-left: 4px solid #0ea5e9;
        padding: 1rem;
        border-radius: 0.5rem;
        margin-top: 1rem;
    }
    
    .warning-box-custom {
        background: #fed7aa;
        border-left: 4px solid #f97316;
        padding: 1rem;
        border-radius: 0.5rem;
        margin-top: 1rem;
    }
    
    .sample-csv-custom {
        background: #f1f5f9;
        padding: 0.75rem;
        border-radius: 0.5rem;
        font-family: 'Courier New', monospace;
        font-size: 0.8rem;
        margin: 0.75rem 0;
        border: 1px solid #e2e8f0;
    }
    
    .list-custom {
        margin: 0.75rem 0 0 1.25rem;
        padding: 0;
    }
    
    .list-custom li {
        margin-bottom: 0.5rem;
        line-height: 1.4;
    }
    
    .row-custom {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 1.5rem;
        margin-bottom: 1.5rem;
    }
    
    @media (max-width: 768px) {
        .delete-container {
            padding: 1rem;
        }
        .row-custom {
            grid-template-columns: 1fr;
        }
        .nav-tabs-custom {
            flex-direction: column;
        }
        .nav-tab {
            text-align: center;
        }
    }
    
    input[type="file"] {
        display: none;
    }
    
    .loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
    }
    
    .spinner-custom {
        width: 50px;
        height: 50px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #4361ee;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
    
    .badge-custom {
        display: inline-block;
        padding: 0.25rem 0.5rem;
        font-size: 0.75rem;
        font-weight: 600;
        border-radius: 0.25rem;
        margin-left: 0.5rem;
    }
    
    .badge-jby {
        background: #dbeafe;
        color: #1e40af;
    }
    
    .badge-sby {
        background: #fce7f3;
        color: #be185d;
    }
</style>

<div class="delete-container">
    <div class="header-section">
        <h1>Bulk Operations Manager</h1>
        <p>Efficiently manage bulk operations for PMS BY records</p>
        <hr style="width: 100px; margin: 1rem auto; border: 2px solid #4361ee;">
    </div>
    
    <c:if test="<%= Validator.isNotNull(success) %>">
        <div class="alert-custom alert-success-custom">
            <div class="alert-icon">✅</div>
            <div><%= success %></div>
        </div>
    </c:if>
    
    <c:if test="<%= Validator.isNotNull(error) %>">
        <div class="alert-custom alert-danger-custom">
            <div class="alert-icon">❌</div>
            <div><%= error %></div>
        </div>
    </c:if>
    
    <div id="loadingOverlay" style="display: none;">
        <div class="loading-overlay">
            <div class="spinner-custom"></div>
        </div>
    </div>
    
    <div class="nav-tabs-custom">
        <button class="nav-tab" onclick="showTab('by-id')">🗑️ Delete by IDs</button>
        <button class="nav-tab" onclick="showTab('by-urn')">📝 Delete by URNs</button>
        <button class="nav-tab" onclick="showTab('workflow')">⚙️ Workflow Cleanup</button>
        <button class="nav-tab" onclick="showTab('update-account')">🔄 Update Account No</button>
        <button class="nav-tab" onclick="showTab('export-data')">📥 Export by URNs</button>
        <button class="nav-tab active" onclick="showTab('finacle-consent')">🏦 Finacle Consent Sync</button>
    </div>
    
    <div id="by-id" class="tab-content">
        <!-- Your existing Delete by IDs content -->
        <div class="card-custom">
            <div class="card-header-custom">
                <i>🗑️</i> Bulk Delete by Object Entry IDs
            </div>
            <div class="card-body-custom">
                <aui:form action="<%= startBulkDeleteURL %>" method="post" enctype="multipart/form-data" name="deleteForm">
                    <div class="form-group-custom">
                        <label class="form-label-custom">CSV File with Object Entry IDs <span class="required">*</span></label>
                        <div class="upload-area-custom" onclick="document.getElementById('<portlet:namespace />csvFile').click();">
                            <div class="upload-icon">📄</div>
                            <div class="upload-text"><strong>Click to select</strong> or drag & drop CSV file</div>
                        </div>
                        <aui:input name="csvFile" type="file" id="csvFile" accept=".csv" required="true" label="" style="display: none;" />
                        <div class="file-info-custom" id="fileInfo">Supported formats: .csv (Max 250K records)</div>
                    </div>
                    <div class="row-custom">
                        <div class="form-group-custom">
                            <label class="form-label-custom">ID Column Name</label>
                            <aui:input name="idColumnName" type="text" id="idColumnName" value="objectEntryId" placeholder="e.g., objectEntryId, id, entryId" label=""><aui:validator name="required" /></aui:input>
                        </div>
                        <div class="form-group-custom">
                            <label class="form-label-custom">CSV Delimiter</label>
                            <select name="<portlet:namespace />delimiter" class="form-control-custom">
                                <option value=",">Comma (,)</option>
                                <option value=";">Semicolon (;)</option>
                                <option value="\t">Tab</option>
                                <option value="|">Pipe (|)</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group-custom">
                        <label class="form-label-custom">Description (Optional)</label>
                        <aui:input name="description" type="text" placeholder="e.g., Jan 2024 cleanup" label="" />
                    </div>
                    <button type="submit" class="btn-custom">🚀 Upload and Delete Records</button>
                </aui:form>
                <div class="warning-box-custom">
                    <strong>⚠️ Important Notes:</strong>
                    <ul class="list-custom"><li>CSV file must have a header row with ID column</li><li>Each ID will be permanently deleted</li><li>Track progress in <strong>Control Panel → System → Background Tasks</strong></li></ul>
                </div>
            </div>
        </div>
    </div>
    
    <div id="by-urn" class="tab-content">
        <!-- Your existing Delete by URNs content -->
        <div class="card-custom">
            <div class="card-header-custom">
                <i>📝</i> Bulk Delete by URNs (Duplicate Cleanup)
            </div>
            <div class="card-body-custom">
                <aui:form action="<%= startURNBulkDelete %>" method="post" enctype="multipart/form-data" name="deleteURNForm">
                    <div class="form-group-custom">
                        <label class="form-label-custom">CSV File with URNs <span class="required">*</span></label>
                        <div class="upload-area-custom" onclick="document.getElementById('<portlet:namespace />csvURNFile').click();">
                            <div class="upload-icon">📄</div>
                            <div class="upload-text"><strong>Click to select</strong> CSV file with URNs</div>
                        </div>
                        <aui:input name="csvURNFile" type="file" id="csvURNFile" accept=".csv" required="true" label="" style="display: none;" />
                    </div>
                    <div class="form-group-custom">
                        <label class="form-label-custom">Description (Optional)</label>
                        <aui:input name="description" type="text" placeholder="e.g., Duplicate cleanup - March 2024" label="" />
                    </div>
                    <button type="submit" class="btn-custom">🧹 Clean Duplicates by URN</button>
                </aui:form>
            </div>
        </div>
    </div>
    
    <div id="workflow" class="tab-content">
        <!-- Your existing Workflow Cleanup content -->
        <div class="card-custom">
            <div class="card-header-custom">
                <i>⚙️</i> Workflow Records Cleanup
            </div>
            <div class="card-body-custom">
                <aui:form action="<%= startBulkWorkflowDeleteURL %>" method="post">
                    <div class="row-custom">
                        <div class="form-group-custom">
                            <label class="form-label-custom">Class Name ID <span class="required">*</span></label>
                            <aui:input name="classNameId" type="number" required="true" label="" placeholder="Enter Class Name ID"><aui:validator name="required" /></aui:input>
                        </div>
                        <div class="form-group-custom">
                            <label class="form-label-custom">Site ID <span class="required">*</span></label>
                            <aui:input name="siteId" type="number" required="true" label="" placeholder="Enter Site ID"><aui:validator name="required" /></aui:input>
                        </div>
                    </div>
                    <button type="submit" class="btn-custom btn-danger-custom">🗑️ Delete Workflow Records</button>
                </aui:form>
            </div>
        </div>
    </div>
    
    <div id="update-account" class="tab-content">
        <!-- Your existing Update Account No content -->
        <div class="card-custom">
            <div class="card-header-custom">
                <i>🔄</i> Bulk Update Account Number by URN
            </div>
            <div class="card-body-custom">
                <form action="<%= startBulkUpdateAccountNoURL %>" method="post" enctype="multipart/form-data">
                    <div class="form-group-custom">
                        <label class="form-label-custom">CSV File with URN and Account Number <span class="required">*</span></label>
                        <div class="upload-area-custom" onclick="document.getElementById('csvUpdateFileInput').click();">
                            <div class="upload-icon">📄</div>
                            <div class="upload-text"><strong>Click to select</strong> CSV file</div>
                        </div>
                        <input type="file" name="csvUpdateFile" id="csvUpdateFileInput" accept=".csv" required style="display: none;" />
                    </div>
                    <div class="row-custom">
                        <div class="form-group-custom">
                            <label class="form-label-custom">URN Column Name</label>
                            <input type="text" name="urnColumnName" class="form-control-custom" value="URN">
                        </div>
                        <div class="form-group-custom">
                            <label class="form-label-custom">Account Number Column Name</label>
                            <input type="text" name="accountNoColumnName" class="form-control-custom" value="accountNumber">
                        </div>
                    </div>
                    <button type="submit" class="btn-custom" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%);">🔄 Update Account Numbers</button>
                </form>
            </div>
        </div>
    </div>
    
    <div id="export-data" class="tab-content">
        <!-- Your existing Export by URNs content -->
        <div class="card-custom">
            <div class="card-header-custom export-header">
                <i>📥</i> Export Object Data by URNs
            </div>
            <div class="card-body-custom">
                <aui:form action="<%= exportObjectDataByURNsURL %>" method="post" enctype="multipart/form-data">
                    <div class="form-group-custom">
                        <label class="form-label-custom">CSV File with URNs <span class="required">*</span></label>
                        <div class="upload-area-custom" onclick="document.getElementById('<portlet:namespace />exportCSVFile').click();">
                            <div class="upload-icon">📄</div>
                            <div class="upload-text"><strong>Click to select</strong> CSV file with URNs</div>
                        </div>
                        <aui:input name="exportCSVFile" type="file" id="exportCSVFile" accept=".csv" required="true" label="" style="display: none;" />
                    </div>
                    <button type="submit" class="btn-custom btn-export-custom">📥 Export Object Data</button>
                </aui:form>
            </div>
        </div>
    </div>
    
    <!-- Tab 6: Finacle Consent Sync (NEW - COMPLETE) -->
    <div id="finacle-consent" class="tab-content active">
        <div class="card-custom">
            <div class="card-header-custom finacle-header">
                <i>🏦</i> Finacle Consent Sync
            </div>
            <div class="card-body-custom">
                <!-- Info Box -->
                <div class="info-box-custom" style="background: #ede9fe; border-left-color: #8b5cf6; margin-top: 0;">
                    <strong>📌 How Finacle Consent Sync Works:</strong>
                    <ul class="list-custom">
                        <li>Finds records with <strong>Stage = 3</strong> AND <strong>FinacleStatus IS NULL</strong> OR <strong>FinacleStatusCode != 200</strong></li>
                        <li>Sends consent data to Finacle core banking system via SOAP API</li>
                        <li>Processes in batches of <strong>5,000 records</strong> for optimal performance</li>
                        <li>Updates records with <strong>SUCCESS/FAILED</strong> status and HTTP status code</li>
                        <li>Runs continuously in background until all records are processed</li>
                    </ul>
                </div>
                
                <aui:form action="<%= startFinacleConsentSyncURL %>" method="post" name="finacleForm">
                    <!-- Insurance Type Selection -->
                    <div class="form-group-custom">
                        <label class="form-label-custom">Insurance Type <span class="required">*</span></label>
                        <select name="<portlet:namespace />insuranceType" id="insuranceType" class="form-control-custom" required>
                            <option value="JBY">JBY (Janashree Bima Yojana)</option>
                            <option value="SBY">SBY (Suraksha Bima Yojana)</option>
                        </select>
                        <div class="file-info-custom">
                            <span id="insuranceTypeInfo">
                                <span class="badge-custom badge-jby">JBY</span> - For Janashree Bima Yojana policies
                                <span class="badge-custom badge-sby">SBY</span> - For Suraksha Bima Yojana policies
                            </span>
                        </div>
                    </div>
                    
                    <!-- Object Definition ID (Hidden - passed automatically) -->
                    <input type="hidden" name="<portlet:namespace />objectDefinitionId" value="395809" />
                    
                    <!-- Description -->
                    <div class="form-group-custom">
                        <label class="form-label-custom">Description (Optional)</label>
                        <input type="text" name="<portlet:namespace />description" class="form-control-custom" 
                               placeholder="e.g., Monthly consent sync - May 2024" id="finacleDescription">
                        <div class="file-info-custom">Add a description for tracking purposes</div>
                    </div>
                    
                    <!-- Field Mappings Info -->
                    <div class="info-box-custom" style="background: #dbeafe; border-left-color: #3b82f6;">
                        <strong>📋 Field Mappings (Database → Finacle API):</strong>
                        <ul class="list-custom">
                            <li><strong>typeOfInsurance</strong> → <strong>bimaType</strong> (JBY/SBY)</li>
                            <li><strong>accountNumber</strong> → <strong>debitAccount</strong></li>
                            <li><strong>customerId</strong> → <strong>customerId</strong></li>
                            <li><strong>modifiedDate</strong> → <strong>consentTime & autoRenewalConsentTime</strong></li>
                            <li><strong>uRN</strong> → <strong>urn</strong></li>
                        </ul>
                    </div>
                    
                    <!-- Hardcoded Values Info -->
                    <div class="info-box-custom" style="background: #fce7f3; border-left-color: #ec4899;">
                        <strong>🔧 Hardcoded Values in API Request:</strong>
                        <ul class="list-custom">
                            <li>consentTaken = <strong>Y</strong></li>
                            <li>autoRenewalConsent = <strong>Y</strong></li>
                            <li>channelId = <strong>DMS</strong></li>
                            <li>Identifier = <strong>JBYSBYCONSENT</strong></li>
                        </ul>
                    </div>
                    
                    <!-- Condition Info -->
                    <div class="warning-box-custom" style="background: #fee2e2; border-left-color: #dc2626;">
                        <strong>⚠️ Records Processing Condition (ALL must be TRUE):</strong>
                        <ul class="list-custom">
                            <li>✅ Stage = <strong>3</strong></li>
                            <li>✅ FinacleStatus = <strong>NULL</strong> OR FinacleStatusCode ≠ <strong>200</strong></li>
                        </ul>
                        <div class="sample-csv-custom" style="margin-top: 0.5rem;">
                            <strong>Example:</strong>
                            <table style="width:100%; font-size:12px; margin-top:5px;">
                                <tr><th>Stage</th><th>FinacleStatus</th><th>FinacleCode</th><th>Action</th></tr>
                                <tr><td>3</td><td>NULL</td><td>NULL</td><td style="color:green;">✅ Send</td></tr>
                                <tr><td>3</td><td>NULL</td><td>500</td><td style="color:green;">✅ Send</td></tr>
                                <tr><td>3</td><td>SUCCESS</td><td>200</td><td style="color:red;">❌ Skip</td></tr>
                                <tr><td>2</td><td>NULL</td><td>NULL</td><td style="color:red;">❌ Skip</td></tr>
                            </table>
                        </div>
                    </div>
                    
                    <!-- What Gets Updated -->
                    <div class="info-box-custom" style="background: #d1fae5; border-left-color: #10b981;">
                        <strong>📊 What Gets Updated in Database:</strong>
                        <ul class="list-custom">
                            <li><strong>finacleStatus</strong> = "SUCCESS" (if HTTP 200) or "FAILED" (if any other code)</li>
                            <li><strong>finacleStatusCode</strong> = HTTP response code (200, 400, 403, 404, 500, etc.)</li>
                        </ul>
                    </div>
                    
                    <!-- Important Notes -->
                    <div class="warning-box-custom" style="background: #fed7aa; border-left-color: #f97316;">
                        <strong>⚠️ Important Notes:</strong>
                        <ul class="list-custom">
                            <li>Task runs in <strong>background</strong> - you can close this window</li>
                            <li>Processes <strong>5,000 records per batch</strong> automatically</li>
                            <li>Already successful records (200) are <strong>SKIPPED</strong> in future runs</li>
                            <li>Monitor progress at: <strong>Control Panel → System → Background Tasks</strong></li>
                            <li>For large datasets (20L+ records), the task may take several hours/days</li>
                            <li>The task will continue processing until ALL matching records are synced</li>
                        </ul>
                    </div>
                    
                    <!-- Submit Button -->
                    <button type="submit" class="btn-custom btn-finacle-custom" onclick="showLoading()">
                        🚀 Start Finacle Consent Sync
                    </button>
                </aui:form>
                
                <!-- Sample Response Format -->
                <div class="sample-csv-custom" style="margin-top: 1rem; background: #1e1e2e; color: #e2e8f0;">
                    <strong style="color: #8b5cf6;">📨 Sample Finacle API Response:</strong>
                    <pre style="margin-top: 0.5rem; font-size: 11px; overflow-x: auto;">
&lt;FIXML&gt;
  &lt;Header&gt;
    &lt;ResponseHeader&gt;
      &lt;HostTransaction&gt;
        &lt;Status&gt;SUCCESS&lt;/Status&gt;
      &lt;/HostTransaction&gt;
    &lt;/ResponseHeader&gt;
  &lt;/Header&gt;
  &lt;Body&gt;
    &lt;executeFinacleScriptResponse&gt;
      &lt;executeFinacleScript_CustomData&gt;
        &lt;GETCONSENT&gt;
          &lt;Status&gt;Success&lt;/Status&gt;
          &lt;errorMessage&gt;Consent updated Successfully&lt;/errorMessage&gt;
        &lt;/GETCONSENT&gt;
      &lt;/executeFinacleScript_CustomData&gt;
    &lt;/executeFinacleScriptResponse&gt;
  &lt;/Body&gt;
&lt;/FIXML&gt;</pre>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Simple tab switching without external dependencies
function showTab(tabId) {
    var tabs = document.getElementsByClassName('tab-content');
    for (var i = 0; i < tabs.length; i++) {
        tabs[i].classList.remove('active');
    }
    var navTabs = document.getElementsByClassName('nav-tab');
    for (var i = 0; i < navTabs.length; i++) {
        navTabs[i].classList.remove('active');
    }
    document.getElementById(tabId).classList.add('active');
    if (event && event.currentTarget) {
        event.currentTarget.classList.add('active');
    }
}

// File name display for first form
var csvFileInput = document.getElementById('<portlet:namespace />csvFile');
if (csvFileInput) {
    csvFileInput.addEventListener('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            var fileName = e.target.files[0].name;
            var fileSize = (e.target.files[0].size / 1024).toFixed(2);
            var infoDiv = document.getElementById('fileInfo');
            if (infoDiv) infoDiv.innerHTML = '✅ Selected: ' + fileName + ' (' + fileSize + ' KB)';
        }
    });
}

// File name display for second form
var csvURNFileInput = document.getElementById('<portlet:namespace />csvURNFile');
if (csvURNFileInput) {
    csvURNFileInput.addEventListener('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            var fileName = e.target.files[0].name;
            var fileSize = (e.target.files[0].size / 1024).toFixed(2);
            var infoDiv = document.getElementById('fileInfoUrn');
            if (infoDiv) infoDiv.innerHTML = '✅ Selected: ' + fileName + ' (' + fileSize + ' KB)';
        }
    });
}

// File name display for update form
var csvUpdateFileInput = document.getElementById('csvUpdateFileInput');
if (csvUpdateFileInput) {
    csvUpdateFileInput.addEventListener('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            var fileName = e.target.files[0].name;
            var fileSize = (e.target.files[0].size / 1024).toFixed(2);
            var infoDiv = document.getElementById('fileInfoUpdate');
            if (infoDiv) infoDiv.innerHTML = '✅ Selected: ' + fileName + ' (' + fileSize + ' KB)';
        }
    });
}

// File name display for export form
var exportFileInput = document.getElementById('<portlet:namespace />exportCSVFile');
if (exportFileInput) {
    exportFileInput.addEventListener('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            var fileName = e.target.files[0].name;
            var fileSize = (e.target.files[0].size / 1024).toFixed(2);
            var infoDiv = document.getElementById('fileInfoExport');
            if (infoDiv) infoDiv.innerHTML = '✅ Selected: ' + fileName + ' (' + fileSize + ' KB)';
        }
    });
}

// Show loading overlay
function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'block';
}

// Attach loading to all forms
var forms = document.querySelectorAll('form');
for (var i = 0; i < forms.length; i++) {
    forms[i].addEventListener('submit', function() { showLoading(); });
}

// Set Finacle Consent as active tab on load
document.addEventListener('DOMContentLoaded', function() {
    var activeTab = window.location.hash;
    if (activeTab) {
        var tabId = activeTab.substring(1);
        var tabElement = document.getElementById(tabId);
        if (tabElement) {
            showTab(tabId);
        }
    } else {
        showTab('finacle-consent');
    }
});
</script>