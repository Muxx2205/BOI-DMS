<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<style>
    .custom-form {
        max-width: 500px;
        margin: 30px auto;
        padding: 30px 40px;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 8px 16px rgba(0,0,0,0.1);
    }

    .custom-form fieldset {
        border: none;
        padding: 0;
        margin: 0;
    }

    .custom-form .form-group {
        margin-bottom: 15px;
    }

    .custom-form label {
        color: #555;
        font-weight: 500;
        margin-bottom: 6px;
        display: block;
    }

    .custom-form input {
        width: 100%;
        padding: 10px 12px;
        border: 1px solid #ccc;
        border-radius: 6px;
        font-size: 15px;
        transition: border-color 0.3s ease;
    }

    .custom-form input:focus {
        border-color: #3498db;
        outline: none;
    }

    .custom-form .submit-btn {
        width: 100%;
        padding: 12px;
        background-color: #3498db;
        color: #fff;
        border: none;
        border-radius: 6px;
        font-size: 16px;
        cursor: pointer;
        transition: background-color 0.3s ease;
    }

    .custom-form .submit-btn:hover {
        background-color: #2980b9;
    }
</style>