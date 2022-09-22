
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
function showLongProjectModal(e) {
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
        saveProject();
    }
}

// Submits the form
function saveProject() {
    const form  = document.getElementById('project');
    form.submit();
}