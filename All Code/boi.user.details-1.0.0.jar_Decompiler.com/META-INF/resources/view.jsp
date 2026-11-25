<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%-- <%@ taglib uri="http://liferay.com/tld/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %> --%>
<%@ include file="/init.jsp" %>
<liferay-theme:defineObjects />

<portlet:resourceURL var="searchUserResourceURL" id="searchUserDetails" />

<style>
    .profile-card {
        display: none;
        max-width: 600px;
        margin: 20px auto;
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 0 10px rgba(0,0,0,0.1);
        padding: 20px;
        font-family: sans-serif;
    }
    .profile-header {
        display: flex;
        align-items: center;
    }
    .profile-header img {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        margin-right: 20px;
    }
    .info-label { font-weight: bold; }
    .role-badge, .org-badge {
        display: inline-block;
        padding: 4px 10px;
        margin: 5px 5px 0 0;
        background-color: #007bff;
        color: white;
        border-radius: 12px;
        font-size: 12px;
    }
</style>

<form id="userSearchForm" class="row g-2 align-items-center mb-3">
    <div class="col-auto">
        <input 
            type="text" 
            class="form-control" 
            id="screenNameInput" 
            name="screenName" 
            placeholder="Enter user's screen name" 
            required 
        />
    </div>
    <div class="col-auto">
        <button type="submit" class="btn btn-primary">Search</button>
    </div>
</form>


<div class="profile-card">
    <div class="profile-header">
        <img src="/documents/d/boi-dms/avatar2" alt="Profile Image" id="userImage"/>
        <div>
            <div class="info-label">Full Name:</div>
            <div class="info-value" id="fullName">-</div>
            <div class="info-label">Email:</div>
            <div class="info-value" id="email">-</div>
            <div class="info-label">Screen Name:</div>
            <div class="info-value" id="screenName">-</div>
            <div class="info-label">Department:</div>
            <div class="info-value" id="department">-</div>
        </div>
    </div>
    <div class="profile-info">
        <div class="info-label">Roles:</div>
        <div id="rolesContainer"></div>

        <div class="info-label">Branch:</div>
        <span class="org-badge" id="branch">-</span>
        <div class="info-label">Zone:</div>
        <span class="org-badge" id="zone">-</span>
        <div class="info-label">NBG:</div>
        <span class="org-badge" id="nbg">-</span>
    </div>
</div>

<input type="hidden" id="department_ids" name="department_ids" />
<input type="hidden" id="organization_ids" name="organization_ids" />


<div class="container">
        <div class="row mb-3">
            <div class="col-md-4">
                <label for="roleSelect" class="form-label">Select Role</label>
                <select id="roleSelect" class="form-select">
                    <option value="">-- Select --</option>
                    <option value="maker">Maker</option>
                    <option value="checker">Checker</option>
                </select>
            </div>
            <div class="col-md-2 d-flex align-items-end">
                <button class="btn btn-primary" onclick="searchByRole()">Search</button>
            </div>
        </div>
    </div>
<link
  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
  rel="stylesheet"
/>

<!-- <div class="container mt-4">
  <h5>User Department Table</h5>
  <table id="userDepartmentTable" class="table table-bordered table-striped">
    <thead class="table-dark">
      <tr>
        <th>User Screen Name</th>
        <th>Department ID</th>
        <th>Organization ID</th>
      </tr>
    </thead>
    <tbody>
     
    </tbody>
  </table>
</div> -->

<div class="container mt-4" id="userDepartmentTableContainer">
  <!-- Dynamic tables will be inserted here -->
</div>

<script>
document.getElementById("userSearchForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const screenName = document.getElementById("screenNameInput").value.trim();

    let baseURL = "<%= searchUserResourceURL.toString() %>";
    const delimiter = baseURL.includes('?') ? '&' : '?';

    const resourceURL = baseURL + delimiter + "<portlet:namespace />screenName=" + encodeURIComponent(screenName);

    fetch(resourceURL)
        .then(response => response.json())
        .then(async data => {
            console.log("User data fetched:", data);

            if (data.error) {
                alert(data.error);
                return;
            }

            document.getElementById("fullName").textContent = data.fullName || "-";
            document.getElementById("email").textContent = data.email || "-";
            document.getElementById("screenName").textContent = data.screenName || "-";

            const rolesContainer = document.getElementById("rolesContainer");
            rolesContainer.innerHTML = "";
            (data.roleNames || []).forEach(role => {
                const roleLower = role.toLowerCase();
                if (roleLower.includes("maker") || roleLower.includes("checker")) {
                    const badge = document.createElement("span");
                    badge.className = "role-badge";
                    badge.textContent = role;
                    rolesContainer.appendChild(badge);
                }
            });

            document.getElementById("branch").textContent = data.branch || "-";
            document.getElementById("branch").setAttribute("data-id", data.branchId || "");

            document.getElementById("zone").textContent = data.zone || "-";
            document.getElementById("zone").setAttribute("data-id", data.zoneId || "");

            document.getElementById("nbg").textContent = data.nbg || "-";
            document.getElementById("nbg").setAttribute("data-id", data.nbgId || "");
            
            document.getElementById("organization_ids").value= data.organizationId;

            document.getElementById("userImage").src = data.image || "/documents/d/boi-dms/avatar2";
            document.querySelector(".profile-card").style.display = "block";
            console.log("userScreenName :", data.screenName);
            const result = await fetchUserDepartmentData({ userScreenName: data.screenName,organizationId:data.organizationId });
            console.log("User department data:", result);
            extractOrgDeptIdsAndAppend(result);
        })
        .catch(error => {
            console.error("Error fetching user details:", error);
            alert("Failed to fetch user data.");
        });
});



async function fetchUserDepartmentData({ userScreenName, departmentId, organizationId }) {
    const filters = [];

    if (userScreenName) {
        filters.push("userScreenName eq '" + userScreenName + "'");
    }

    if (departmentId) {
        filters.push("departmentId eq '" + departmentId + "'");
    }

    if (organizationId) {
        filters.push("organizationId eq "  + organizationId);
    }
    console.log("filters :", filters);
    const filterQuery = filters.length ? "?filter=" + filters.join(" and ") : "";
    console.log("filterQuery :", filterQuery);
    const url = `/o/c/userorganizationmasters`+filterQuery;
    console.log("Fetching department data with URL:", url);

    const myHeaders = new Headers({
        "accept": "application/json",
        "x-csrf-token": Liferay.authToken,
        "Authorization": "Basic dGVzdDp0ZXN0MTIz"
    });

    try {
        const response = await fetch(url, { method: "GET", headers: myHeaders });
        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        return { items: [] };
    }
}

function extractOrgDeptIdsAndAppend(apiResponse) {
    const ids = [];

    if (Array.isArray(apiResponse.items) && apiResponse.items.length > 0) {
        apiResponse.items.forEach(item => {
            ids.push({
                organizationId: item.organizationId,
                departmentId: item.departmentId
            });
        });

        const departmentNames = ids.map(obj => obj.departmentId).join(", ");
        const organizationIds = ids.map(obj => obj.organizationId).join(", ");
        console.log("Extracted departmentIds:", departmentNames);
        console.log("Extracted organizationIds:", organizationIds);

        document.getElementById("department").textContent = departmentNames;
        const hiddenInput = document.getElementById("department_ids");
        if (hiddenInput) {
            hiddenInput.value = organizationIds;
            console.log("Set hiddenInput.value =", hiddenInput.value);
        }
    } else {
        document.getElementById("department").textContent = "-";
        document.getElementById("department_ids").value = "";
    }
}

function extractUserDepartmentList(apiResponse) {
    const result = [];

    if (apiResponse && Array.isArray(apiResponse.items)) {
        apiResponse.items.forEach(item => {
            result.push({
                userScreenName: item.userScreenName || "",
                departmentId: item.departmentId || "",
                organizationId: item.organizationId || ""
            });
        });
    }

    console.log("Extracted user department list:", result);
    return result;
}

/* function populateUserDepartmentTable(dataList) {
    const tbody = document.querySelector("#userDepartmentTable tbody");
    tbody.innerHTML = "";

    dataList.forEach(function(item) {
        const row = document.createElement("tr");
        console.log(item);
        row.innerHTML =
            "<td>" + item.userScreenName + "</td>" +
            "<td>" + item.departmentId + "</td>" +
            "<td>" + item.organizationId + "</td>";
        tbody.appendChild(row);
    });

    console.log("Populated user department table.");
} */
function populateUserDepartmentTable(dataList, headingText) {
    const container = document.getElementById("userDepartmentTableContainer");

    // Create heading
    const heading = document.createElement("h5");
    heading.className = "mt-4";
    heading.textContent = headingText;
    container.appendChild(heading);

    // Create table
    const table = document.createElement("table");
    table.className = "table table-bordered table-striped";

    // Create thead
    const thead = document.createElement("thead");
    thead.className = "table-dark";
    thead.innerHTML = `
      <tr>
        <th>User Screen Name</th>
        <th>Department ID</th>
        <th>Organization ID</th>
      </tr>
    `;
    table.appendChild(thead);

    // Create tbody
    const tbody = document.createElement("tbody");
    dataList.forEach(function(item) {
      const row = document.createElement("tr");
      row.innerHTML =
        "<td>" + item.userScreenName + "</td>" +
        "<td>" + item.departmentId + "</td>" +
        "<td>" + item.organizationId + "</td>";
      tbody.appendChild(row);
    });

    table.appendChild(tbody);
    container.appendChild(table);

    console.log("Populated user department table for:", headingText);
  }




async function searchByRole() {
    const selectedRole = document.getElementById('roleSelect').value;

    if (!selectedRole) {
        alert("Please select a role.");
        return;
    }

    const department_ids = document.getElementById("department").textContent;
    const organizationId = document.getElementById("organization_ids").value;

    console.log("department_ids:", department_ids);
    console.log("role:", selectedRole);
    console.log("organizationId:", organizationId);
    let makerOrCheckerUsers ;
    if(selectedRole.toLowerCase()==='maker'){
    	 makerOrCheckerUsers = await getUserScreenNamesByRole(organizationId, 'Branch Maker');
        console.log("User screenNames with selected role:", makerOrCheckerUsers);	
    }
    

    const dataMap = getDataIdMap();

    // Clear previous results
    const container = document.getElementById("userDepartmentTableContainer");
    container.innerHTML = "";

    for (const [key, value] of dataMap.entries()) {
        if (!value) continue;

        console.log("Key:", key, "Value:", value);

        const userDepartments = await fetchUserDepartmentData({
            organizationId: value,
            departmentId: department_ids 
        });

        const extracted = extractUserDepartmentList(userDepartments);
        console.log("extracted:", extracted);
        
        let filteredList=extracted;
        if(selectedRole.toLowerCase()==='maker'){
         filteredList = extracted.filter(item =>
            makerOrCheckerUsers.includes(item.userScreenName)
        );
        }
        console.log("Filtered user-department list for " + key + ":", filteredList);

        if (filteredList.length > 0) {
            populateUserDepartmentTable(filteredList, key);
        }
    }
}


async function getUserScreenNamesByRole(organizationId, roleName) {
	const myHeaders = new Headers({
	    "accept": "application/json",
	    "Authorization": "Basic dGVzdDp0ZXN0MTIz" // base64 for test:test123
	});


    const url = `/o/headless-admin-user/v1.0/organizations/`+organizationId+`/user-accounts`;
    console.log("Calling Headless User API:", url);

    try {
        const response = await fetch(url, { method: "GET", headers: myHeaders });
        const data = await response.json();

        const users = data.items || [];
        const filtered = users
            .filter(user => {
                const userRoles = (user.roleBriefs || []).map(role => role.name.toLowerCase());
                return userRoles.some(role => role.includes(roleName.toLowerCase()));
            })
            .map(user => user.alternateName);

        console.log("Filtered users with role:", roleName, filtered);
        return filtered;
    } catch (error) {
        console.error("Error fetching users by role:", error);
        return [];
    }
}

function getDataIdMap() {
    const idMap = new Map();

    const branchId = document.getElementById("branch")?.getAttribute("data-id") || "";
    const zoneId = document.getElementById("zone")?.getAttribute("data-id") || "";
    const nbgId = document.getElementById("nbg")?.getAttribute("data-id") || "";

    idMap.set("Branch", branchId);
    idMap.set("Zone", zoneId);
    idMap.set("NBG", nbgId);

    return idMap;
}


</script>
