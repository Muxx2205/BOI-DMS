<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%-- <%@ taglib uri="http://liferay.com/tld/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %> --%>
<%@ include file="/init.jsp" %>
<liferay-theme:defineObjects />

<portlet:resourceURL var="searchUserResourceURL" id="searchUserDetails" />
<!-- <liferay-ui:success key="success" message=" Action Completed Successfully..."/>
<liferay-ui:error key="error" message=" Action Failed..."/> -->
<style>
  .custom-modal {
    display: none; 
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    overflow: auto;
    background-color: rgba(0,0,0,0.6);
  }

  .custom-modal-content {
    background-color: #28a745;
    color: #fff;
    margin: 15% auto;
    padding: 20px;
    border-radius: 8px;
    width: 90%;
    max-width: 400px;
    text-align: center;
    box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  }

  .close-button {
    float: right;
    font-size: 1.5rem;
    font-weight: bold;
    cursor: pointer;
  }
  .loader-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: none;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #ffffff;
  border-top: 5px solid #28a745;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>

<div class="d-flex justify-content-center">
  <div class="card border-0 mb-4" style="max-width: 500px; width: 100%; box-shadow: 0 10px 25px rgba(0,0,0,0.2); background-color: #f8f9fa;">
    <div class="card-header text-white" style="background: linear-gradient(90deg, #007bff, #00c6ff);">
      <h5 class="mb-0"> Maker-Checker Role Transfer</h5>
    </div>
    <div class="card-body">
      <form id="userSearchForm">
        <div class="mb-3">
          <label for="screenNameInput" class="form-label">User Screen Name</label>
          <input type="text" class="form-control" id="screenNameInput" name="screenName" placeholder="Enter user's screen name" required>
        </div>

        <div class="mb-3">
          <label for="directionSelect" class="form-label">Select Direction</label>
          <select class="form-select form-control" id="directionSelect" name="direction" required>
            <option value="" disabled selected>Select Direction</option>
            <option value="MakerToChecker">Maker to Checker</option>
            <option value="CheckerToMaker">Checker to Maker</option>
          </select>
        </div>

        <div class="mb-3">
          <label for="roleSelect" class="form-label">Current Role</label>
          <select class="form-select form-control" id="roleSelect" name="currentRole" required>
            <option value="" disabled selected>Select Current Role</option>
            <option value="Branch">Branch</option>
            <option value="Zone">Zone</option>
          </select>
        </div>

        <div class="text-end">
          <button type="submit" class="btn btn-success">Submit Request</button>
        </div>
      </form>
    </div>
  </div>
</div>
<div id="customConsole" style="max-width: 500px; margin: 20px auto; padding: 10px; background-color: #212529; color: #f8f9fa; font-family: monospace; height: 200px; overflow-y: auto; border-radius: 4px; box-shadow: 0 0 10px rgba(0,0,0,0.5);">
  <strong>Console Output:</strong>
  <div id="logContent"></div>
</div>

<div id="loaderOverlay" class="loader-overlay">
  <div class="spinner"></div>
</div>
<!-- Modal -->
<div id="successModal" class="custom-modal">
  <div class="custom-modal-content">
    <span class="close-button" onclick="closeModal()">&times;</span>
    <h2> Success!</h2>
    <p>Request submitted successfully!</p>
  </div>
</div>


<script>
document.getElementById("userSearchForm").addEventListener("submit", function (e) {
    e.preventDefault();
    document.getElementById("loaderOverlay").style.display = "flex";
    
    const direction = document.getElementById('directionSelect').value.trim();
    const currentRole = document.getElementById('roleSelect').value.trim();
    const screenName = document.getElementById('screenNameInput').value.trim();

    if (!screenName) {
        alert("Please enter a screen name.");
        return;
    }

    let baseURL = "<%= searchUserResourceURL.toString() %>";
    const delimiter = baseURL.includes('?') ? '&' : '?';

    const resourceURL = baseURL + delimiter +
    "<portlet:namespace />screenName=" + encodeURIComponent(screenName) +
    "&<portlet:namespace />direction=" + encodeURIComponent(direction)+
    "&<portlet:namespace />currentRole=" + encodeURIComponent(currentRole);

    fetch(resourceURL)
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then(data => {
            console.log("User data fetched:", data);
            
             Object.keys(data).forEach(key => {
            	 logToCustomConsole(data[key]);
                
             });
             document.getElementById("loaderOverlay").style.display = "none"; 
        })
        .catch(error => {
            console.error("Error fetching user data:", error);
            alert("Failed to fetch user data.");
        });
});
  


 function showSuccessModal() {
document.getElementById("successModal").style.display = "block";
}

function closeModal() {
document.getElementById("successModal").style.display = "none";
}

function logToCustomConsole(message) {
	  const logContainer = document.getElementById("logContent");
	  if(message==="new"){
		  logContainer.innerHTML = "";  
	  }
	  const logEntry = document.createElement("div");
	  logEntry.textContent = "* " + message;
	  logContainer.appendChild(logEntry);
	  logContainer.scrollTop = logContainer.scrollHeight; 
	}




</script>