<%@ include file="/init.jsp" %>


<portlet:actionURL name='saveIt' var="publishData"/>
<style>
    body {
      font-family: Arial, sans-serif;
      margin: 40px;
    }
    form {
      max-width: 300px;
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 8px;
      background: #f9f9f9;
    }
    label {
      display: block;
      margin-top: 10px;
      margin-bottom: 5px;
      font-weight: bold;
    }
    select, input[type="number"], button {
      width: 100%;
      padding: 8px;
      margin-bottom: 15px;
      border-radius: 5px;
      border: 1px solid #ccc;
    }
    button {
      background: #4CAF50;
      color: white;
      font-size: 16px;
      border: none;
      cursor: pointer;
    }
    button:hover {
      background: #45a049;
    }
  </style>

<aui:form cssClass="custom-form" action="<%=publishData %>" method="post" name="myForm">
    <aui:fieldset>
 
        <aui:input name="classPk"  label="Class PK ID" required="true" />
               
           <aui:button type="submit" value="Submit" cssClass="submit-btn"/>
    </aui:fieldset>
</aui:form>