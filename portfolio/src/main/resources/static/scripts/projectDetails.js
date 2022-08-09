let previousEvent; // the previous event being edited by THIS user (only one can be edited at a time)
// had to be a var because using let gave a redefinition error TODO


/** When the delete sprint button is clicked, show a modal checking if the user is sure about deleting the sprint */
function showDeleteSprintModal(sprintId, sprintName) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + sprintName + "?";
    deleteButton.onclick = () => {deleteSprint(sprintId)}
    modal.style.display = "block";
}

/** Hides the confirm delete modal without deleting a sprint/event */
function hideModal() {
    const modal = document.getElementById("deleteModal");
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

/** When the delete event button is clicked, show a modal checking if the user is sure about deleting the event */
function showDeleteEventModal(eventId, eventName) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + eventName + "?";
    deleteButton.onclick = () => {deleteEvent(eventId)}
    modal.style.display = "block";
}

/** sends a http request to delete the event with the given id */
function deleteEvent(eventId) {
    const url = BASE_URL + "delete-event/" + eventId;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        // Reload the page to get the updated list of events after the delete
        window.location.reload();
    }
    deleteRequest.send();
}

/**
 * Inserts the event edit form directly below the event being edited.
 * Also sends an editing event websocket message.
 * @param eventId the id of the event to show the form for
 * @param eventName name of event being edited
 * @param eventDescription description of event being edited
 * @param eventStartDate start date of event being edited
 * @param eventEndDate end date of event being edited
 */
function showEditEvent(eventId, eventName, eventDescription, eventStartDate, eventEndDate) {
    hideEditEvent(eventId !== previousEvent);
    previousEvent = eventId;
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
    document.getElementById("form").action="/edit-event/" + eventId;
    sendEditingEventMessage(eventId);
}

/**
 * Remove the edit form from any event it is attached to
 * @param sendStop if true, send a stop editing websocket message also
 */
function hideEditEvent(sendStop=false) {
    const eventForm = document.getElementById("editEventForm");
    if (eventForm) {
        eventForm.parentNode.removeChild(eventForm);
    }
    if (sendStop) {
        sendStopEditingMessage(previousEvent);
    }
}

// function toggleSendingEditMessages(eventName) {
//     if (send != null) {
//         setTimeout(sendEditingEventMessage(eventName));
// }