<%@ include file="../init.jsp" %>



<portlet:actionURL name="/branch/create_nbg_organization" var="addNBG" />
<portlet:renderURL var="viewNBG">
	<portlet:param name="mvcRenderCommandName" value="/view-nbg" />
</portlet:renderURL>
<div class="container mt-4">
  <div class="card shadow-sm">
    <div class="card-header bg-primary text-white">
    <div class="d-flex align-items-center">
			<a href="<%=viewNBG%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Add NBG</h4>
		</div>
    </div>
    <div class="card-body">
      <aui:form action="<%= addNBG %>" method="post">
        <aui:input type="hidden" name="nbgId" value="${organization.organizationId }"></aui:input>
        <!-- NBG Name -->
        <div class="mb-3">
          <aui:input type="text" name="nbgName" label="NBG Name" value="${organization.name }"
                 placeholder="Enter NBG Name"></aui:input>
          
        </div>
        
        <!-- Submit Button -->
        <div class="text-end">
          <aui:button type="submit" class="btn btn-success">
          </aui:button>
        </div>
      </aui:form>
    </div>
  </div>
</div>
