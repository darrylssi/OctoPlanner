const previousSchedulable = {type:"", id:-1}; // the previous schedulable being edited by THIS user (only one can be edited at a time)
let previousEvent;
const SCHEDULABLE_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing a schedulable
const EVENT_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing an event
let sendEditMessageInterval;
const EDIT_FORM_CLOSE_DELAY = 300;

/** When the delete sprint button is clicked, show a modal checking if the user is sure about deleting the sprint */
function showDeleteSprintModal(sprintId, sprintName) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + sprintName + "?";
    deleteButton.onclick = () => {deleteSprint(sprintId)}
    modal.style.display = "block";
}

/** Hides the confirm delete modal without deleting a sprint/event/deadline */
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

/** When the delete deadline button is clicked, show a modal checking if the user is sure about deleting the deadline */
function showDeleteDeadlineModal(deadlineId, deadlineName) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + deadlineName + "?";
    deleteButton.onclick = () => {deleteDeadline(deadlineId)}
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

/** sends a http request to delete the deadline with the given id */
function deleteDeadline(deadlineId) {
    const url = BASE_URL + "delete-deadline/" + deadlineId;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        // Reload the page to get the updated list of deadlines after the delete
        window.location.reload();
    }
    deleteRequest.send();
}

/**
 * Submits the `edit-event` request in Javascript, because the form doesn't
 * exist until it's spawned via Javascript, and this is the only way to convey errors.
 * @param {HTMLFormElement} elem
 * @param {Event} e
 */
function sendEditEventViaAjax(elem, e) {
    e.preventDefault();

    // Delete any pre-existing errors
    const errorListElem = elem.querySelector("#errors");
    errorListElem.innerHTML = '';
    const formData = new FormData(elem);
    const url = "./edit-event/" + elem.querySelector("#editEventId").value;
    const editRequest = new XMLHttpRequest();
    editRequest.open("POST", url);

    editRequest.onload = () => {
        if (editRequest.status == 200) {
            // Success
            window.location.reload()
        } else {
            const errors = editRequest.responseText.split('\n');
            for (const errorMsg of errors) {
                // Add one list item per error
                const errorItem = document.createElement('li');
                errorItem.classList.add("text-danger");
                errorItem.textContent = errorMsg;
                errorListElem.appendChild(errorItem);
            }
        }
    }
    editRequest.send(formData);

}

/**
 * TODO this whole function
 * An attempt to make this function deal with schedulable objects...
 * @param schedulableId the id of the schedulable object being edited
 * @param schedulableBoxId the id of the box element of the schedulable object being edited
 * @param schedulableType the type of the schedulable object (Event, Deadline, or Milestone) as a string
 */
function showEditSchedulable(schedulableId, schedulableBoxId, schedulableType, schedulable) {
    /* Capitalize only the first letter of the schedulableType string */
    schedulableType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

    /* Search for the edit form */
    let editForm = document.getElementById("edit" + schedulableType + "Form-" + schedulableBoxId);

    if (schedulableType === 'Deadline') {
        prefillDeadline(editForm, schedulable);
    }

    /* Collapse element, send stop message, and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditSchedulable(schedulableId, schedulableBoxId, schedulableType);
        return;
    }

    /* Collapse any edit schedulable forms already on the page. If we find any, delay
       opening the new form by EDIT_FORM_CLOSE_DELAY. */
    let collapseElementList = document.getElementsByClassName("collapse show");
    let delay = collapseElementList.length > 0 ? EDIT_FORM_CLOSE_DELAY : 0;
    let differentSchedulable = false;
    for (let element of collapseElementList) {
        if (element.id.indexOf("edit" + schedulableType + "Form") != -1) {
            new bootstrap.Collapse(element).hide();
            /* Check whether any form is for a different schedulable, to see whether
               we need to send a stop editing message */
            if (element.id.indexOf("edit" + schedulableType + "Form-" + schedulableBoxId) == -1) {
                differentSchedulable = true;  // Extracted to a variable to avoid sending extra messages (worst case)
                previousSchedulable.id = (element.id.split('-')[1]);  // Get schedulable id from that form
                previousSchedulable.type = schedulableType;

            }
        }
    }

    /* If a form we just closed was for a different schedulable, we need to
       send a stop editing message */
     if (differentSchedulable) {
         stopEditing();
     }

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    sendEditingSchedulableMessage(schedulableId, schedulableType); // see https://www.w3schools.com/jsref/met_win_setinterval.asp
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    sendEditMessageInterval = setInterval(function() {sendEditingSchedulableMessage(schedulableId, schedulableType)}, SCHEDULABLE_EDIT_MESSAGE_FREQUENCY)

    showRemainingChars();

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "edit" + schedulableType + "Form-" + schedulableBoxId);
}

/**
 * Populates the edit deadline form with the current details of the deadline.
 * @param editForm Edit deadline form
 * @param deadline Deadline object
 */
function prefillDeadline(editForm, deadline) {
    editForm.querySelector("#name").value = deadline.name;
    editForm.querySelector("#description").value =  deadline.description;
    editForm.querySelector("#date").value = deadline.startDate.substring(0,10);
    editForm.querySelector("#time").value = deadline.startDate.substring(11, 16);
}


/**
 * Collapse the edit form for the specified schedulable box.
 * Accessed directly by the cancel button.
 * Sends a stop editing message for the previous schedulable & ceases sending repeated editing messages.
 * @param schedulableId the id of the schedulable object whose edit form is being hidden
 * @param schedulableBoxId the id of the box in which the edit form will be hidden
 * @param schedulablhideEditSchedulableeType the type of the schedulable whose edit form is being hidden
 */
function hideEditSchedulable(schedulableId, schedulableBoxId, schedulableType) {
    /* Capitalize only the first letter of the schedulableType string */
    schedulableType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

    /* Search for the edit form */
    let editForm = document.getElementById("edit" + schedulableType + "Form-" + schedulableBoxId);

    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
    previousSchedulable.id = schedulableId;
    previousSchedulable.type = schedulableType;
    stopEditing();
}

/**
 * Collapse the edit form for the specified event box.
 * Accessed directly by the cancel button.
 * Sends a stop editing message for the previous event & ceases sending repeated editing messages.
 * @param eventBoxId the ID of the event box to hide the form from
 */
function hideEditEvent(eventId, eventBoxId) {
    let editForm = document.getElementById("editEventForm-" + eventBoxId);
    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
    previousEvent = eventId;
    stopEditing();
}

/**
 * Sends a stop editing message for the previously edited schedulable, and stops any repeating edit messages from being sent.
 */
function stopEditing() {
    if (sendEditMessageInterval) {
        clearInterval(sendEditMessageInterval);
    }
    sendStopEditingSchedulableMessage(previousSchedulable.id, previousSchedulable.type);
}

/**
 * Binds an event to the input, such that the remaining length is displayed.
 *
 * @param {HTMLInputElement} input An `<input type="text" maxlength=...>` element,
 *                                  with an optional `minlength` element
 * @param {Element} display The element that'll display the output (Note: Will overwrite
 *                                  any inner HTML)
 * @throws {EvalError} If any of the above requirements are broken
 */
function displayRemainingCharacters(input, display) {
    if (
        input.tagName.toLowerCase() !== 'input'
        || input.getAttribute('type') !== 'text'
        || !input.hasAttribute('maxlength')
    ) {
        console.error(input);
        throw new EvalError(
            '`input` doesn\'t look like `<input type="text" maxlength=...>'
        );
    }
    const event = () => {
        const maxLength = input.getAttribute('maxlength');
        const minLength = input.getAttribute('minlength');
        const inputLength = input.value.length;
        const remainingChars = maxLength - inputLength;
        if (remainingChars <= 0) {
            // Too many characters
            display.classList.add('text-danger');
            display.textContent = remainingChars;
        } else if (minLength !== null && inputLength < minLength) {
            // (Optional) Not enough characters
            display.classList.add('text-danger');
            display.textContent = '< ' + (minLength - inputLength);
        } else {
            display.classList.remove('text-danger');
            display.textContent = remainingChars;
        }
    }
    // Bind the event, then give it a kick to initialise the display
    input.addEventListener("input", event);
    event();
}

/**
 * Shows the number of remaining characters on an input field with class 'limited-text-input'
 * in a span tag with class 'remaining-chars-field'.
 * This is called when the page is loaded and when the edit button is clicked.
 */
function showRemainingChars() {
    for (const parent of document.getElementsByClassName('limited-text-input')) {
        const input = parent.getElementsByTagName('input')[0];
        const display = parent.getElementsByClassName('remaining-chars-field')[0];
        displayRemainingCharacters(input, display);
    }
}
