
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

// Set project min date to last year
// A teacher can start a project up to a year ago (UPi AC4)
var today = new Date();
var dd = today.getDate();
var mm = today.getMonth() + 1;
var yyyy = today.getFullYear()-1;
if (dd < 10) {
    dd = '0' + dd;
}
if (mm < 10) {
    mm = '0' + mm;
}
today = yyyy + '-' + mm + '-' + dd;
document.getElementById('projectStartDate').setAttribute('min', today);

// To hide the modal when the transaction is cancelled
function hideModal() {
    const modal = document.getElementById("longProjectModal");
    modal.style.display = "none";
}

// Checks whether the project is longer than 10 years
// If it is, the confirmation modal is shown
function showLongProjectModal(e) {
    e.preventDefault();
    const startDate = new Date(document.getElementById('projectStartDate').value);
    const startString = startDate.toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'}).replace(/ /g, '/');;
    const endDate = new Date(document.getElementById('projectEndDate').value);
    const endString = endDate.toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'}).replace(/ /g, '/');;
    const modal = document.getElementById("longProjectModal");
    const title = document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you " +
            "want to create a project from " + startString + " to " + endString + "?";
    if ((endDate.getFullYear() - startDate.getFullYear()) > 10) {
        const confirmButton = document.getElementById("confirmButton");
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