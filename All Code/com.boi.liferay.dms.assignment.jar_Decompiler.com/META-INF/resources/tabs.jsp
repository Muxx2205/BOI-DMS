 

<%
String currentTab = ParamUtil.getString(request, "tabName", "Entry Assignment");
List<Organization> organizations =(List<Organization>)request.getAttribute("OrgType");
if(Validator.isNotNull(organizations) && (!organizations.isEmpty())){
	currentTab="Organization Assignment";
}
String tabs= "Entry Assignment,Branch Data Sync,Role Assignment,Organization Assignment";
%>


 

<div class="assignment-tabs-container mt-4">
    <liferay-ui:tabs 
        names="<%= tabs %>" 
        refresh="false" 
        value="<%= currentTab %>" 
        cssClass="custom-tabs mt-4">
       <liferay-ui:section>
            <%@ include file="/entry_assignment.jsp" %>
        </liferay-ui:section>

        <liferay-ui:section>
            <%@ include file="/sync_pending_branch_data.jsp" %>
        </liferay-ui:section>
        <liferay-ui:section>
            <%@ include file="/role_assignment.jsp" %>
        </liferay-ui:section>
        
                <liferay-ui:section>
            		<%@ include file="/organization_assignment.jsp" %>
        		</liferay-ui:section>

 

    </liferay-ui:tabs>
</div>
