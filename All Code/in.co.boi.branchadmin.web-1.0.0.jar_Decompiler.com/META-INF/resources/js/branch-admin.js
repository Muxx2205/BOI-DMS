document.addEventListener('DOMContentLoaded', function() {

  const singleForm = document.getElementById('single-create-form');
  const singleResult = document.getElementById('single-create-result');

  singleForm.addEventListener('submit', function(e) {
    e.preventDefault();
    const formData = new FormData(singleForm);

    fetch(Liferay.PortletURL.createActionURL({
      portletId: 'branch_admin_portlet',
      mvcAction: '/branch/create_user_and_assign'
    }), {
      method: 'POST',
      body: formData
    }).then(res => res.text())
      .then(text => singleResult.innerHTML = text)
      .catch(err => singleResult.innerHTML = 'Error: ' + err);
  });

  const bulkForm = document.getElementById('bulk-upload-form');
  const bulkResult = document.getElementById('bulk-upload-result');

  bulkForm.addEventListener('submit', function(e) {
    e.preventDefault();
    const formData = new FormData(bulkForm);

    fetch(Liferay.PortletURL.createResourceURL({
      portletId: 'branch_admin_portlet',
      resourceId: '/branch/bulk_import'
    }), {
      method: 'POST',
      body: formData
    }).then(res => res.blob())
      .then(blob => {
        // if CSV error report returned
        const downloadLink = document.createElement('a');
        downloadLink.href = URL.createObjectURL(blob);
        downloadLink.download = "bulk_import_error_report.csv";
        downloadLink.click();
        bulkResult.innerHTML = "Bulk upload completed. Check error report if any.";
      })
      .catch(err => bulkResult.innerHTML = 'Error: ' + err);
  });

});
