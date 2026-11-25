<%@ include file="/init.jsp" %>
<%@ page import="com.liferay.portal.kernel.model.Role" %>
<%@ page import="com.liferay.portal.kernel.json.JSONFactoryUtil" %>
<%@ page import="com.liferay.portal.kernel.json.JSONArray" %>
<%@ page import="com.liferay.portal.kernel.json.JSONException" %>
<portlet:actionURL name="submitAssignments" var="submitAssignmentsURL" />
<style>
.form-container {
	  background: #fff;
	  padding: 30px;
	  border-radius: 12px;
	  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
	  max-width: 800px;
	  margin: 40px auto;
	}

	h2 {
	  text-align: center;
	  margin-bottom: 25px;
	  font-weight: 600;
	  color: #0f172a;
	}

	.field-group {
	  border: 1px solid #e2e8f0;
	  border-radius: 10px;
	  padding: 15px;
	  margin-bottom: 15px;
	  background: #f8fafc;
	}

	.form-row {
	  display: flex;
	  gap: 20px;
	  flex-wrap: wrap;
	}

	.input-field {
	  flex: 1;
	  min-width: 200px;
	}

	.button-row {
	  display: flex;
	  justify-content: flex-end;
	  margin: 10px 0;
	}

	.btn-add {
	  background: #e2e8f0;
	  border: none;
	  border-radius: 8px;
	  padding: 8px 16px;
	  font-weight: 500;
	  cursor: pointer;
	  transition: all 0.2s ease;
	}

	.btn-add:hover {
	  background: #cbd5e1;
	  transform: translateY(-1px);
	}

	.submit-row {
	  display: flex;
	  justify-content: center;
	  margin-top: 20px;
	}

	.btn-submit {
	  background: #2563eb;
	  color: white;
	  border: none;
	  padding: 10px 22px;
	  border-radius: 8px;
	  font-weight: 600;
	  cursor: pointer;
	  transition: all 0.2s ease;
	}

	.btn-submit:hover {
	  background: #1d4ed8;
	  transform: translateY(-2px);
	}
</style>
<%
String allowToAssignment = (String) request.getAttribute("allowToAssignment");

JSONArray jsonArray = null;
try {
    jsonArray = JSONFactoryUtil.createJSONArray(allowToAssignment);
} catch (JSONException e) {
    jsonArray = JSONFactoryUtil.createJSONArray();
}
%>

<div class="form-container">
    <h2>Assign Roles</h2>

    <aui:form action="<%= submitAssignmentsURL %>" method="post" name="assignmentForm" cssClass="custom-form">
        <div id="fieldContainer">
            <!-- Group Template -->
            <div class="field-group">
                <div class="form-row">
                    <aui:input name="adid_0" label="ADID" required="true" cssClass="input-field" />
                    <aui:input name="solid_0" label="SOLID" required="true" cssClass="input-field" />
                    <aui:select name="roles_0" label="Role(s)" multiple="true" cssClass="input-field">
                    
                               <%
              if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                  String key = jsonArray.getString(i);
            %>
              <aui:option value="<%= key %>"><%= key %></aui:option>
            <%
                }
              } else {
            %>
              <p style="color:#64748b;">No roles available to add.</p>
            <%
              }
            %>
  
                    </aui:select>
                </div>
            </div>
        </div>

        <div class="button-row">
            <button type="button" class="btn-add" id="addGroupBtn">+ Add Row</button>
        </div>

        <div class="submit-row">
            <aui:button type="submit" value="Submit All" cssClass="btn-submit" />
        </div>
    </aui:form>
</div>

<script>
(function () {
  // get Liferay portlet namespace injected by JSP
  var ns = '<portlet:namespace/>';
  var fieldContainer = document.getElementById('fieldContainer');
  var addGroupBtn = document.getElementById('addGroupBtn');

  if (!fieldContainer || !addGroupBtn) {
    return;
  }

  // count existing groups based on current children
  var groupCount = fieldContainer.children.length;
  var maxGroups = 10;

  addGroupBtn.addEventListener('click', function () {
    if (groupCount >= maxGroups) {
      alert('You can add up to ' + maxGroups + ' entries only.');
      return;
    }

    // clone the first group as template
    var template = fieldContainer.firstElementChild;
    var newGroup = template.cloneNode(true);

    // for each input/select/textarea inside the cloned group update name/id and clear value
    var formControls = newGroup.querySelectorAll('input, select, textarea');

    formControls.forEach(function (el) {
      var origName = el.getAttribute('name') || '';
      // origName includes namespace, remove it to find base like "adid_0" or "roles_0"
      var withoutNs = origName.replace(ns, '');
      // split base and index: e.g. 'adid_0' => ['adid','0']
      var parts = withoutNs.split('_');
      var base = parts[0] || withoutNs;

      // construct new name/id with namespace + base + '_' + groupCount
      var newName = ns + base + '_' + groupCount;
      el.setAttribute('name', newName);

      // also update id (if present) to keep label 'for' associations intact
      if (el.id) {
        var withoutNsId = el.id.replace(ns, '');
        var idParts = withoutNsId.split('_');
        var idBase = idParts[0] || withoutNsId;
        el.id = ns + idBase + '_' + groupCount;
      }

      // clear values according to type
      var tag = el.tagName.toLowerCase();
      var type = el.type ? el.type.toLowerCase() : '';

      if (tag === 'select') {
        // for multi-select, clear selected options
        for (var i = 0; i < el.options.length; i++) {
          el.options[i].selected = false;
        }
      } else if (type === 'checkbox' || type === 'radio') {
        el.checked = false;
      } else {
        el.value = '';
      }

      // Also update associated label's 'for' (if any) inside the group
      // Find label elements that reference the old id pattern and update them
      // (we'll attempt a best-effort: labels that have for="[oldId]" within the newGroup)
      var oldIdPattern = ns + base + '_0'; // assume template used index 0
      var labels = newGroup.querySelectorAll('label[for]');
      labels.forEach(function (lbl) {
        var forVal = lbl.getAttribute('for') || '';
        if (forVal.indexOf(oldIdPattern) !== -1 || forVal.indexOf(base + '_0') !== -1) {
          // compute new for value
          var newFor = ns + base + '_' + groupCount;
          lbl.setAttribute('for', newFor);
        }
      });
    });

    // append cloned group
    fieldContainer.appendChild(newGroup);
    groupCount++;
  });
})();
</script>