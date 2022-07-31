
/** When the delete sprint button is clicked, show a modal checking if the user is sure about deleting the sprint */
function showDeleteModal(sprintId, sprintName) {
    const modal = document.getElementById("deleteSprintModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + sprintName + "?";
    deleteButton.onclick = () => {deleteSprint(sprintId)}
    modal.style.display = "block";
}

/** Hides the confirm delete sprint modal without deleting a sprint */
function hideModal() {
    const modal = document.getElementById("deleteSprintModal");
    modal.style.display = "none";
}

/** sends a http request to delete the sprint with the given id */
function deleteSprint(sprintId) {
    const url = BASE_URL + "delete-sprint/" + sprintId;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        // Reload the page to get the updated list of sprints after the delete
        window.location.reload();
    }
    deleteRequest.send();
}

/** Inserts the event edit form directly below the event being edited */
function showEditEvent(eventId, eventName, eventDescription, eventStartDate, eventEndDate) {
    hideEditEvent();
    const eventBox = document.getElementById("event-" + eventId);
    let editForm = document.createElement("div");
    editForm.setAttribute("id", "editEventForm");
    editForm.innerHTML = editFormTemplate;
    document.getElementById("event-box-" + eventId).appendChild(editForm);
    document.getElementById("edit-event-form-header").innerHTML = "Editing " + eventName;
    document.getElementById("editEventNameInput").setAttribute("value", eventName);
    document.getElementById("editEventDescriptionInput").setAttribute("value", eventDescription);
    document.getElementById("editEventStartTime").setAttribute("value", eventStartDate);
    document.getElementById("editEventEndTime").setAttribute("value", eventEndDate);
}

/** Remove the edit form from any event it is attached to */
function hideEditEvent() {
    const eventForm = document.getElementById("editEventForm");
    if (eventForm) {
        eventForm.parentNode.removeChild(eventForm);
    }
}

function editEvent(eventId) {
    document.getElementById("hello").style.color = "red";
    const url = BASE_URL + "edit-event/" + eventId;
    const postRequest = new XMLHttpRequest();

    postRequest.open("POST", url, true);
    postRequest.send(JSON.stringify({
        eventName: "name",
        eventDescription: "descriptionism",
    }));
    postRequest.onload = () => {
        window.location.reload();
    }
}

