<%@ include file="/init.jsp" %>

<portlet:defineObjects />

<portlet:actionURL name="uploadExcel" var="uploadExcelURL" />

<div class="container">
    <h3>User Folder Mapping</h3>
    
    <form action="<%= uploadExcelURL %>" method="post" enctype="multipart/form-data">
        <div class="form-group">
            <label for="excelFiles">Select Excel File (.xlsx):</label>
            <input type="file" name="excelFile" accept=".xlsx"  required class="form-control">
        </div>
        
        <button type="submit" class="btn btn-primary">Upload and Process</button>
    </form>

    <%-- Success Message --%>
    <% if (request.getAttribute("successMessage") != null) { %>
        <div style="color: green; margin-top: 15px; padding: 10px; background: #f0fff0; border: 1px solid green;">
            <%= request.getAttribute("successMessage") %>
        </div>
    <% } %>

    <%-- Error Message --%>
    <% if (request.getAttribute("errorMessage") != null) { %>
        <div style="color: red; margin-top: 15px; padding: 10px; background: #fff0f0; border: 1px solid red;">
            <%= request.getAttribute("errorMessage") %>
        </div>
    <% } %>
</div>

<style>
.container {
    padding: 20px;
}
.form-group {
    margin-bottom: 15px;
}
.form-control {
    display: block;
    width: 100%;
    padding: 8px;
    margin-top: 5px;
}
.btn {
    padding: 10px 20px;
    background: #0b5fff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}
.btn:hover {
    background: #0053f0;
}
</style>