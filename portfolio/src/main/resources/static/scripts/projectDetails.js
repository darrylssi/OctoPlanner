const previousSchedulable = {type:"", id:-1}; // the previous schedulable being edited by THIS user (only one can be edited at a time)
let previousEvent;
const SCHEDULABLE_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing a schedulable
const EVENT_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing an event
let sendEditMessageInterval;
const EDIT_FORM_CLOSE_DELAY = 300;

/** Hides the confirm delete modal without deleting a sprint/event/deadline */
function hideModal() {
    const modal = document.getElementById("deleteModal");
    modal.style.display = "none";
}

/** When the delete button is clicked, show a modal checking if the user is sure about deleting the object
 * Type  */
function showDeleteModal(id, name, type) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + name + "?";
    deleteButton.onclick = () => {deleteObject(id, type)}
    modal.style.display = "block";
}

/** sends a http request to delete the object with the given id */
function deleteObject(id, type) {
    const url = BASE_URL + "delete-" + type + "/" + id;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        // Reload the page to get the updated list of events after the delete
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
 * @param schedulableType the type of the schedulable object (Event, Deadline, or Milestone)
 */
function showEditSchedulable(schedulableId, schedulableBoxId, schedulableType) {
    /* Search for the edit form */
    let editForm = document.getElementById("edit" + schedulableType + "Form-" + schedulableBoxId);

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
//     if (differentSchedulable) {
//         stopEditing(); TODO editing messages for schedulables
//     }

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    // sendEditingSchedulableMessage(schedulableId); // see https://www.w3schools.com/jsref/met_win_setinterval.asp TODO popups for schedulables
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    // sendEditMessageInterval = setInterval(function() {sendEditingSchedulableMessage(schedulableId, schedulableType)}, SCHEDULABLE_EDIT_MESSAGE_FREQUENCY)

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "edit" + schedulableType + "Form-" + schedulableBoxId);
}

/**
 * Collapse the edit form for the specified schedulable box.
 * Accessed directly by the cancel button.
 * TODO SHOULD, but DOESN'T send a stop editing message for the previous schedulable & ceases sending repeated editing messages.
 * @param schedulableId the id of the schedulable object whose edit form is being hidden
 * @param schedulableBoxId the id of the box in which the edit form will be hidden
 * @param schedulableType the type of the schedulable whose edit form is being hidden
 */
function hideEditSchedulable(schedulableId, schedulableBoxId, schedulableType) {
    let editForm = document.getElementById("edit" + schedulableType + "Form-" + schedulableBoxId);
    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
    previousSchedulable.id = schedulableId;
    previousSchedulable.type = schedulableType;
//    stopEditing();
}

/**
 * Inserts/expands the event edit form directly below the event being edited.
 * This function adds forms into the page only as they are needed.
 *
 * Also deals with the websocket part of editing events. This function will send an initial editing message and
 * set up repeating edit messages.
 * @param eventId id of the event the message should show for
 * @param eventBoxId id of the event box to display the form at
 */
function showEditEvent(eventId, eventBoxId) {
    /* Search for the edit form */
    let editForm = document.getElementById("editEventForm-" + eventBoxId);

    /* Collapse element, send stop message, and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditEvent(eventId, eventBoxId);
        return;
    }

    /* Collapse any edit event forms already on the page. If we find any, delay
       opening the new form by EDIT_FORM_CLOSE_DELAY. */
    let collapseElementList = document.getElementsByClassName("collapse show");
    let delay = collapseElementList.length > 0 ? EDIT_FORM_CLOSE_DELAY : 0;
    let differentEvent = false;
    for (let element of collapseElementList) {
        if (element.id.indexOf("editEventForm") != -1) {
            new bootstrap.Collapse(element).hide();
            /* Check whether any form is for a different event, to see whether
               we need to send a stop editing message */
            if (element.id.indexOf("editEventForm-" + eventId) == -1) {
                differentEvent = true;  // Extracted to a variable to avoid sending extra messages (worst case)
                previousEvent = (element.id.split('-')[1]);  // Get event id from that form
            }
        }
    }

    /* If a form we just closed was for a different event, we need to
       send a stop editing event */
    if (differentEvent) {
        stopEditing();
    }

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    sendEditingEventMessage(eventId); // see https://www.w3schools.com/jsref/met_win_setinterval.asp
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    sendEditMessageInterval = setInterval(function() {sendEditingEventMessage(eventId)}, EVENT_EDIT_MESSAGE_FREQUENCY)

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "editEventForm-" + eventBoxId);
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
 * Sends a stop editing message for the previously edited event, and stops any repeating edit messages from being sent.
 */
function stopEditing() {
    if (sendEditMessageInterval) {
        clearInterval(sendEditMessageInterval);
    }
    sendStopEditingMessage(previousEvent);
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
