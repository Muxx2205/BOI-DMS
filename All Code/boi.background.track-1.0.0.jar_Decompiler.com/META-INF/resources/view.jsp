<%@ include file="/init.jsp" %>

<style>
    .task-filter-container {
        margin-bottom: 20px;
        padding: 15px;
        background: #f5f5f5;
        border-radius: 4px;
    }
    
    .filter-group {
        display: inline-block;
        margin-right: 10px;
    }
    
    .task-table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 15px;
    }
    
    .task-table th, .task-table td {
        padding: 10px;
        text-align: left;
        border: 1px solid #ddd;
    }
    
    .task-table th {
        background-color: #f0f0f0;
        font-weight: bold;
    }
    
    .task-table tr:hover {
        background-color: #f9f9f9;
    }
    
    .context-map {
        max-width: 300px;
        overflow: auto;
        font-size: 12px;
    }
    
    .loading-spinner {
        display: none;
        text-align: center;
        padding: 20px;
    }
    
    .error-message {
        color: #d9534f;
        padding: 10px;
        margin: 10px 0;
        border: 1px solid #d9534f;
        border-radius: 4px;
        background-color: #f2dede;
    }
    
    .success-message {
        color: #5cb85c;
        padding: 10px;
        margin: 10px 0;
        border: 1px solid #5cb85c;
        border-radius: 4px;
        background-color: #dff0d8;
    }
    
    .btn {
        padding: 5px 10px;
        margin: 0 2px;
        cursor: pointer;
        border: none;
        border-radius: 3px;
    }
    
    .btn-primary {
        background-color: #007bff;
        color: white;
    }
    
    .btn-danger {
        background-color: #d9534f;
        color: white;
    }
    
    .btn-sm {
        padding: 3px 6px;
        font-size: 12px;
    }
    
    .status-badge {
        display: inline-block;
        padding: 3px 8px;
        border-radius: 3px;
        font-size: 11px;
        font-weight: bold;
    }
    
    .status-success {
        background-color: #dff0d8;
        color: #3c763d;
    }
    
    .status-failed {
        background-color: #f2dede;
        color: #a94442;
    }
    
    .status-in-progress {
        background-color: #d9edf7;
        color: #31708f;
    }
</style>

<div class="task-filter-container">
    <div class="filter-group">
        <select id="filterType" class="form-control">
            <option value="startsWith">Starts With</option>
            <option value="endsWith">Ends With</option>
            <option value="contains" selected>Contains</option>
            <option value="equalsIgnoreCase">Equals Ignore Case</option>
            <option value="exact">Exact Match</option>
        </select>
    </div>
    
    <div class="filter-group">
        <input type="text" id="filterText" class="form-control" 
               placeholder="Enter task name..." style="width: 200px;" />
    </div>
    
    <div class="filter-group">
        <button onclick="loadTasks()" class="btn btn-primary">Get Tasks</button>
        <button onclick="resetFilters()" class="btn">Reset</button>
    </div>
</div>

<div id="messageContainer"></div>
<div id="loadingSpinner" class="loading-spinner">Loading...</div>

<table class="task-table" id="taskTable">
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Status</th>
            <th>Context Map</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="5" style="text-align: center;">Click "Get Tasks" to load data</td>
        </tr>
    </tbody>
</table>

<script>
let currentRequest = null;

function showMessage(message, type) {
    const container = document.getElementById('messageContainer');
    const className = type === 'error' ? 'error-message' : 'success-message';
    container.innerHTML = `<div class="${className}">${message}</div>`;
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'block' : 'none';
}

function resetFilters() {
    document.getElementById('filterType').value = 'contains';
    document.getElementById('filterText').value = '';
    loadTasks();
}

function loadTasks() {
    // Abort previous request if exists
    if (currentRequest) {
        currentRequest.abort();
    }
    
    const type = document.getElementById("filterType").value;
    const text = document.getElementById("filterText").value;
    
    if (!text.trim()) {
        showMessage("Please enter filter text", "error");
        return;
    }
    
    showLoading(true);
    
    const url = '<portlet:resourceURL id="filterTasks" />' +
                '&filterType=' + encodeURIComponent(type) + 
                '&filterText=' + encodeURIComponent(text);
    
    currentRequest = new AbortController();
    
    fetch(url, {
        signal: currentRequest.signal,
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(res => {
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return res.json();
    })
    .then(data => {
        const tbody = document.querySelector("#taskTable tbody");
        
        if (!data.success) {
            tbody.innerHTML = `<tr><td colspan="5">Error: ${data.message || 'Unknown error'}</td></tr>`;
            showMessage(data.message || 'Failed to load tasks', 'error');
            return;
        }
        
        if (!data.tasks || data.tasks.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5">No tasks found matching your criteria</td></tr>';
            return;
        }
        
        renderTasks(data.tasks);
    })
    .catch(err => {
        if (err.name !== 'AbortError') {
            console.error('Error loading tasks:', err);
            const tbody = document.querySelector("#taskTable tbody");
            tbody.innerHTML = `<tr><td colspan="5">Failed to load tasks: ${err.message}</td></tr>`;
            showMessage('Failed to load tasks. Please try again.', 'error');
        }
    })
    .finally(() => {
        showLoading(false);
        currentRequest = null;
    });
}

function renderTasks(tasks) {
    const tbody = document.querySelector("#taskTable tbody");
    tbody.innerHTML = "";
    
    tasks.forEach(task => {
        let contextHTML = "";
        
        if (task.contextMap && typeof task.contextMap === 'object') {
            for (let key in task.contextMap) {
                if (task.contextMap.hasOwnProperty(key)) {
                    const value = task.contextMap[key];
                    contextHTML += `<strong>${escapeHtml(key)}</strong>: ${escapeHtml(String(value))}<br>`;
                }
            }
        }
        
        if (!contextHTML) {
            contextHTML = "<em>No context data</em>";
        }
        
        const statusClass = getStatusClass(task.status);
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${task.taskId}</td>
            <td><strong>${escapeHtml(task.name)}</strong></td>
            <td><span class="status-badge ${statusClass}">${escapeHtml(task.status)}</span></td>
            <td class="context-map">${contextHTML}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteTask(${task.taskId})">
                    Delete
                </button>
                <button class="btn btn-sm" onclick="viewTaskDetails(${task.taskId})">
                    Details
                </button>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}

function getStatusClass(status) {
    switch(status?.toLowerCase()) {
        case 'successful':
            return 'status-success';
        case 'failed':
            return 'status-failed';
        case 'in progress':
            return 'status-in-progress';
        default:
            return '';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function deleteTask(taskId) {
    if (!confirm(`Are you sure you want to delete task ${taskId}? This action cannot be undone.`)) {
        return;
    }
    
    showLoading(true);
    
    fetch('<portlet:resourceURL id="deleteTask" />&taskId=' + taskId, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showMessage(data.message || 'Task deleted successfully', 'success');
            loadTasks(); // Refresh the list
        } else {
            showMessage(data.message || 'Failed to delete task', 'error');
        }
    })
    .catch(err => {
        console.error('Error deleting task:', err);
        showMessage('Failed to delete task. Please try again.', 'error');
    })
    .finally(() => {
        showLoading(false);
    });
}

function viewTaskDetails(taskId) {
    alert(`Task ${taskId} details - Feature to be implemented`);
}


document.addEventListener('DOMContentLoaded', () => {
    loadTasks();
    
    
    document.getElementById('filterText').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            loadTasks();
        }
    });
});
</script>