<%@ include file="/init.jsp" %>
<portlet:actionURL name="opendbmigform" var="opendbmigformURL" />
<portlet:actionURL name="openFldmigform" var="openFldmigformURL" />
<portlet:actionURL name="openDocmigform" var="openDocmigformURL" />
<portlet:actionURL name="openMscDocmigform" var="openMscDocmigformURL" />
<portlet:actionURL name="openArchDocmigform" var="openArchDocmigformURL" />
<portlet:actionURL name="openArchMscDocmigform" var="openArchMscDocmigformURL" />
<portlet:actionURL name="openMetamigform" var="openMetamigformURL" />
<portlet:actionURL name="orgmigform" var="orgmigformURL" />
<portlet:actionURL name="usermigform" var="usermigformURL" />
<br/><br/>
<p style="padding-left: 100px;">
	<b><a href="<%= opendbmigformURL %>"> DB Migration Form</a></b> <br/><br/>
	<b><a href="<%= openFldmigformURL %>"> Folder Migration Form</a></b> <br/><br/>
	<b><a href="<%= openDocmigformURL %>"> Document Migration Form(DataClass)</a></b> <br/><br/>
	<b><a href="<%= openMscDocmigformURL %>"> Document Migration Form(Msc)</a></b> <br/><br/>
	<b><a href="<%= openArchDocmigformURL %>"> Document Archival Migration Form(DataClass)</a></b> <br/><br/>
	<b><a href="<%= openArchMscDocmigformURL %>">Document Archival Migration Form(Msc)</a></b> <br/><br/>
	<b><a href="<%= openMetamigformURL %>"> Document Metadata Migration Form</a></b> <br/><br/>
	<b><a href="<%= orgmigformURL %>"> Organzation Migration Form</a></b> <br/><br/>
	<b><a href="<%= usermigformURL %>">User Migration Form</a></b> <br/><br/>
</p>