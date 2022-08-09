
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
 * Inserts/expands the event edit form directly below the event being edited.
 * This function adds forms into the page only as they are needed.
 */
function showEditEvent(eventBoxId, eventName, eventDescription, eventStartDate, eventEndDate) {
    /* Search for the edit form */
    let editForm = document.getElementById("editEventForm-" + eventBoxId);
    let delay = 0;

    /* Collapse element and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditEvent(eventBoxId);
        return;
    }

    /* Collapse any collapsible elements already on the page */
    let collapseElementList = document.getElementsByClassName("collapse show");
    if (collapseElementList.length > 0) {
        delay = 300;
    }
    for (let element of collapseElementList) {
        if (element.id.indexOf("editEventForm") != -1) {
            new bootstrap.Collapse(element).hide();
        }
    }

    /* Create element if not seen before */
    if (editForm == null) {
        editForm = document.createElement("div");
        editForm.setAttribute("id", "editEventForm-" + eventBoxId);
        editForm.setAttribute("class", "editEventForm collapse");
        editForm.innerHTML = editFormTemplate;
        document.getElementById("event-box-" + eventBoxId).appendChild(editForm);
        document.getElementById("form").action="/edit-event/" + eventId;

        /* Set internal attributes of form and link cancel button */
        editForm.querySelector("#edit-event-form-header").innerHTML = "Editing " + eventName;
        editForm.querySelector("#editEventNameInput").setAttribute("value", eventName);
        editForm.querySelector("#editEventDescriptionInput").setAttribute("value", eventDescription);
        editForm.querySelector("#editEventStartDate").setAttribute("value", eventStartDate.substring(0, 10));
        editForm.querySelector("#editEventStartTime").setAttribute("value", eventStartDate.substring(11, 16));
        editForm.querySelector("#editEventEndDate").setAttribute("value", eventEndDate.substring(0, 10));
        editForm.querySelector("#editEventEndTime").setAttribute("value", eventEndDate.substring(11, 16));
        editForm.querySelector("#cancel").onclick = function () {hideEditEvent(eventBoxId);};
    }

    /* Get this form to show after a delay that allows any other forms open to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "editEventForm-" + eventBoxId);
}

/** Exposes an easier access point for the cancel button */
function hideEditEvent(eventBoxId) {
    let editForm = document.getElementById("editEventForm-" + eventBoxId);
    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
}
