<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%><%@
taglib
	uri="http://liferay.com/tld/portlet" prefix="liferay-portlet"%><%@
taglib
	uri="http://liferay.com/tld/theme" prefix="liferay-theme"%><%@
taglib
	uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
	



<liferay-theme:defineObjects />

<portlet:defineObjects />
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/all.min.css" />
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/jquery.dataTables.min.css" />

<!-- Local JS -->
<script src="<%= request.getContextPath() %>/js/jquery.min.js"></script>
<script src="<%= request.getContextPath() %>/js/jquery.dataTables.min.js"></script>
