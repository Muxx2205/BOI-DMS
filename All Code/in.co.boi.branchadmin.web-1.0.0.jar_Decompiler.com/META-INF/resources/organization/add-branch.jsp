<%@ include file="../init.jsp" %>
<portlet:actionURL name="/branch/create_branch_organization" var="addBranch" />

<portlet:renderURL var="viewBranch">
	<portlet:param name="mvcRenderCommandName" value="/view-branch" />
</portlet:renderURL>

<portlet:resourceURL var="getZonesURL" id="/organization/get_zones" />
<div class="container mt-4">
  <div class="card shadow-sm">
    <div class="card-header bg-primary text-white">
    <div class="d-flex align-items-center">
			<a href="<%=viewBranch%>" class="btn btn-outline-secondary me-3" ">
				<i class="fa fa-arrow-left me-1"></i> Back
			</a>
			<h4 class="mb-0">Add Organization Details</h4>
		</div>
    
    </div>
    <div class="card-body">

      <aui:form name="addUserForm" method="post" action="<%= addBranch %>">

		<aui:input type="hidden" name="branchId" value="${organization.organizationId }"></aui:input>
        <!-- NBG Dropdown -->
        <aui:select name="nbgId" label="NBG Name" required="true" value="${parentOrganization.parentOrganizationId }" cssClass="form-control" id="nbgSelect">
          <aui:option value="">-- Select NBG --</aui:option>
          <c:forEach var="nbg" items="${nbgList}">
            <aui:option value="${nbg.organizationId}">
              ${nbg.name}
            </aui:option>
          </c:forEach>
        </aui:select>

        <!-- Zone Dropdown (Initially empty) -->
        <aui:select name="zoneId" label="Zone Name" required="true" cssClass="form-control" value="${organization.parentOrganizationId }" id="zoneSelect">
          <aui:option value="">-- Select Zone --</aui:option>
        </aui:select>

        <!-- Branch Name -->
        <aui:input name="branchName" label="Branch Name" value="${organization.name }"  type="text" required="true" />

        <!-- Submit -->
        <div class="text-end mt-3">
          <aui:button type="submit" cssClass="btn btn-success" value="Submit" />
        </div>
      </aui:form>
    </div>
  </div>
</div>




<aui:script use="aui-base,aui-io-request">

	var nbgOrganizationId="${parentOrganization.parentOrganizationId }";
	var branchId="${organization.parentOrganizationId }"
	
	if(nbgOrganizationId && nbgOrganizationId==""){
		nbgOrganizationId=0;
		branchId=0
	}
	if(nbgOrganizationId!=0 && branchId!=0){
	
		loadZone(nbgOrganizationId,branchId);
	}

    A.one('#<portlet:namespace />nbgSelect').on('change', function() {
        var nbgId = this.val();

       loadZone(nbgId,0);
        
    });
    
    function loadZone(nbgId,selectedZoneId){
    var zoneSelect = A.one('#<portlet:namespace />zoneSelect');
    zoneSelect.empty();
        zoneSelect.append('<option value="">-- Select Zone --</option>');

        if (!nbgId) return;
		
        // AJAX request using resource URL tag
        A.io.request('${getZonesURL}', {
            method: 'GET',	
            data: { <portlet:namespace />nbgId: nbgId },
            on: {
                success: function() {
                    var data = this.get('responseData');
                    var zones = JSON.parse(data);
					
                    zones.forEach(function(zone) {
                    console.log("================="+zone.id +"-------------"+selectedZoneId);
                     zoneSelect.append(
                        '<option value="'+zone.id+'"'+
                        '>'+zone.name+'</option>'
                    );
                    if (selectedZoneId) {
                    	zoneSelect.val(selectedZoneId);
                	}
                      
                    });
                    
                }
            }
        });
    }
    
</aui:script>

