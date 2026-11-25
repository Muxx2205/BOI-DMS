<%@ include file="/init.jsp" %>
<%@ taglib uri="http://liferay.com/tld/clay" prefix="clay" %>




<%
String parentFolderName = (String) request.getAttribute("parentFolderName");
String parentOrganizationName = (String) request.getAttribute("parentOrganizationName");
%>

    
<portlet:renderURL var="viewNBG">
	<portlet:param name="mvcRenderCommandName" value="/view-nbg" />
</portlet:renderURL>
<portlet:renderURL var="viewBranch">
	<portlet:param name="mvcRenderCommandName" value="/view-branch" />
</portlet:renderURL>
<portlet:renderURL var="viewUser">
	<portlet:param name="mvcRenderCommandName" value="/view-users" />
</portlet:renderURL>



<div class="container mt-4">
    <h2 class="mb-4 text-center">Organization Dashboard</h2>

    <div class="row justify-content-center g-4">

        <!-- NBG -->
        <div class="col-md-3 col-sm-6">
            <a href="<%= viewNBG %>" class="text-decoration-none">
                <div class="card shadow-sm h-100 text-center p-4 dashboard-card">
                    <i class="fas fa-landmark fa-3x text-primary"></i>
                    <h5 class="card-title mt-3">NBG</h5>
                </div>
            </a>
        </div>

        <!-- Branch -->
        <div class="col-md-3 col-sm-6">
            <a href="<%= viewBranch %>" class="text-decoration-none">
                <div class="card shadow-sm h-100 text-center p-4 dashboard-card">
                    <i class="fas fa-sitemap fa-3x text-success"></i>
                    <h5 class="card-title mt-3">Branch</h5>
                </div>
            </a>
        </div>

        <!-- User -->
        <div class="col-md-3 col-sm-6">
            <a href="<%= viewUser %>" class="text-decoration-none">
                <div class="card shadow-sm h-100 text-center p-4 dashboard-card">
                    <i class="fas fa-user fa-3x text-warning"></i>
                    <h5 class="card-title mt-3">User</h5>
                </div>
            </a>
        </div>
    </div>
</div>


<style>
    .dashboard-card {
        border-radius: 1rem;
        transition: all 0.3s ease-in-out;
    }
    .dashboard-card:hover {
        transform: translateY(-5px);
        box-shadow: 0px 8px 16px rgba(0,0,0,0.15);
    }
    .dashboard-card i {
        transition: transform 0.3s ease-in-out;
    }
    .dashboard-card:hover i {
        transform: scale(1.2);
    }
</style>
