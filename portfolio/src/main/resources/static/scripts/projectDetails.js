
<!--    When the delete sprint button is clicked, show a modal checking if the user is sure about deleting the sprint   -->
function showDeleteModal(sprintId, sprintName) {
    const modal = document.getElementById("deleteSprintModal");
    const deleteButton = document.getElementById("deleteButton");
    const title = document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + sprintName + "?";
    deleteButton.onclick = () => {deleteSprint(sprintId)}
    modal.style.display = "block";
}

<!-- Hides the confirm delete sprint modal without deleting a sprint -->
function hideModal() {
    const modal = document.getElementById("deleteSprintModal");
    modal.style.display = "none";
}

<!-- sends a http request to delete the sprint with the given id -->
function deleteSprint(sprintId) {
    const url = "/delete-sprint/" + sprintId;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        <!--        Reload the page to get the updated list of sprints after the delete -->
        window.location.reload();
    }
    deleteRequest.send();
}

<!-- Monthly Calendar -->
document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');

    // // Getting the current project's start and end date
    // let projectStartDate = new Date([[${project.getProjectStartDate()}]].toString().substring(0, 10));
    // let projectEndDate = new Date([[${project.getProjectEndDate()}]].toString().substring(0, 10));
    //
    // // Adding an extra day to the project end date
    // projectEndDate.setDate(projectEndDate.getDate() + 1);

    let calendar = new FullCalendar.Calendar(calendarEl, {
        timeZone: 'UTC',
        defaultView: 'dayGridMonth',
        validRange: {
            start: [[${projectStartDate}]],
            end: [[${projectEndDate}]]
        }
    });

    calendar.render();
});