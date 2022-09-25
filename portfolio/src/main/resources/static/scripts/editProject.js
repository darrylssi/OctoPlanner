
// Sprint min date so that sprint can't end before start date
function setNewMin() {
    var newStart = document.getElementById('projectStartDate').value;
    document.getElementById('projectEndDate').setAttribute('min', newStart);
}

// Set max date so that sprint can't start after end date
function setNewMax() {
    var newEnd = document.getElementById('projectEndDate').value;
    document.getElementById('projectStartDate').setAttribute('max', newEnd);
}

// Set the min start date of the project to be the year before it was created
function setMinDate(creationDate) {
    mindate = new Date(creationDate);
    mindate.setFullYear(mindate.getFullYear()-1)
    document.getElementById('projectStartDate').setAttribute('min', mindate.toISOString().substring(0,10));
}

// Checks whether the project is longer than 10 years
// If it is, the confirmation modal is shown
// This reuses the confirmation modal for deleting sprints or schedulables
function showLongProjectModal(elem, e) {
    e.preventDefault();
    document.getElementById('deleteButton').innerText = "Confirm";
    const startDate = new Date(document.getElementById('projectStartDate').value);
    const startString = startDate.toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'}).replace(/ /g, '/');;
    const endDate = new Date(document.getElementById('projectEndDate').value);
    const endString = endDate.toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'}).replace(/ /g, '/');;
    const modal = document.getElementById("deleteModal");
    const title = document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you " +
            "want to create a project from " + startString + " to " + endString + "?";
    if ((endDate.getFullYear() - startDate.getFullYear()) > 10) {
        const confirmButton = document.getElementById("deleteButton");
        confirmButton.onclick = () => {saveProject()}
        modal.style.display = "block";
    } else {
        saveProject(elem);
    }
}

/**
 * This submits the form and shows error messages if there are any.
 * @param elem HTML Form element
 */
function saveProject(elem) {

    // Remove existing error messages
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            // Upon success, hide the edit project form and reload the page
            hideEditSchedulable('1', '1', 'project');
            window.location.reload();
        } else {
            // Otherwise, show the error messages
            const errors = formRequest.responseText.split('\n');
            for (let errorMsg of errors) {
                let field = "name";
                if (errorMsg === DATES_IN_WRONG_ORDER_MESSAGE || errorMsg.indexOf('end date') !== -1) {
                    field = 'endDate';
                } else if (errorMsg.indexOf('date') !== -1 || errorMsg.indexOf('sprint') !== -1) {
                    field = 'startDate'
                } else if (errorMsg.indexOf('exceed') !== -1) {
                    field = 'description'
                }
                field += 'Feedback';
                const errorBox = elem.querySelector(`[id*="` + field + `"]`);
                errorBox.textContent = errorMsg;
                errorBox.style.display = 'block';
            }
        }
    }
    formRequest.send(formData);
}

/**
 * Shows the edit project form.
 * @param project Project object to be edited
 */
function showEditProjectForm(project) {

    // Sets the min project start date
    setMinDate(project.projectCreationDate);

    // Pre-fills the edit project form with the details of the project being edited
    let editForm = document.getElementById("editProjectForm-1");
    hideErrorBoxes(editForm);
    editForm.querySelector("#projectName").value = project.projectName;
    editForm.querySelector("#projectDescription").value = project.projectDescription;
    editForm.querySelector("#projectStartDate").value = new Date(project.projectStartDate).toISOString().substring(0,10);
    editForm.querySelector("#projectEndDate").value = new Date(project.projectEndDate).toISOString().substring(0,10);
}