<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>

<%
// Get portletResource from request parameter
String portletResource = ParamUtil.getString(request, "portletResource");

// Get current preferences with default values
String parentFolderName = renderRequest.getPreferences().getValue("parentFolderName", "BOI Document");
String parentOrganizationName = renderRequest.getPreferences().getValue("parentOrganizationName", "BOI HO");
%>

<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />
<liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />

<aui:form action="<%= configurationActionURL %>" method="post" name="fm">
    <aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
    <aui:input name="redirect" type="hidden" value="<%= configurationRenderURL %>" />
    <aui:input name="portletResource" type="hidden" value="<%= portletResource %>" />

    <div class="portlet-configuration">
        <aui:fieldset>
            <aui:input 
                name="parentFolderName" 
                label="Parent Folder Name" 
                type="text" 
                value="<%= parentFolderName %>" 
                helpMessage="Enter the parent folder name for document storage"
            />
            
            <aui:input 
                name="parentOrganizationName" 
                label="Parent Organization Name" 
                type="text" 
                value="<%= parentOrganizationName %>" 
                helpMessage="Enter the parent organization name"
            />
        </aui:fieldset>
    </div>

    <aui:button-row>
        <aui:button type="submit" value="save" />
        <aui:button type="cancel" onClick="<%= configurationRenderURL.toString() %>" />
    </aui:button-row>
</aui:form>