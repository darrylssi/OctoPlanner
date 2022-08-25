const previousSchedulable = {type:"", id:-1}; // the previous schedulable being edited by THIS user (only one can be edited at a time)
let previousEvent;
const SCHEDULABLE_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing a schedulable
const EVENT_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing an event
let sendEditMessageInterval;
const EDIT_FORM_CLOSE_DELAY = 300;
const DATES_IN_WRONG_ORDER_MESSAGE = "Start date must always be before end date";

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
 * Hides and clears any input feedback boxes in the given element
 * @param {HTMLFormElement} elem
 */
function hideErrorBoxes(elem) {
    const errorBoxes = elem.querySelectorAll(`[id*="Feedback"]`);
    for (let feedbackBox of errorBoxes) {
        feedbackBox.innerHTML = '';
        feedbackBox.style.display = 'none';
    }
}

/**
 * Submits the given form's request in Javascript, allowing for in-place
 * updating of the page.
 * @param {HTMLFormElement} elem
 */
function sendFormViaAjax(elem) {
    // Delete any pre-existing errors on the form
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);

    formRequest.onload = () => {
        if (formRequest.status == 200) {
            // Success
            window.location.reload()
        } else {
            const errors = formRequest.responseText.split('\n');
            for (let errorMsg of errors) {
                // Determine correct error field. Defaults to NameFeedback
                let field = "Name";
                if (errorMsg === DATES_IN_WRONG_ORDER_MESSAGE || errorMsg.indexOf('end date') != -1) {
                    field = 'EndDate';
                } else if (errorMsg.indexOf('end time') != -1) {
                    field = 'EndTime';
                } else if (errorMsg.indexOf('date') != -1) {
                    field = 'StartDate';
                } else if (errorMsg.indexOf('time') != -1 || errorMsg.indexOf('minute') != -1) {
                    field = 'StartTime';
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
 * Displays the edit form for the specified schedulable object.
 * Will hide the edit forms for other schedulables (but not events TODO)
 * Should send editing messages for this schedulable so that other users can see an editing popup (TODO)
 * @param schedulableId the id of the schedulable object being edited
 * @param schedulableBoxId the id of the box element of the schedulable object being edited
 * @param schedulableType the type of the schedulable object (Event, Deadline, or Milestone - 1st letter must be capitalised!)
 */
function showEditSchedulable(schedulableId, schedulableBoxId, schedulableType) {
    schedulableType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

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
//         stopEditing(); TODO editing messages for all schedulables
//     }

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    // sendEditingSchedulableMessage(schedulableId); // see https://www.w3schools.com/jsref/met_win_setinterval.asp TODO popups for schedulables
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    // sendEditMessageInterval = setInterval(function() {sendEditingSchedulableMessage(schedulableId, schedulableType)}, SCHEDULABLE_EDIT_MESSAGE_FREQUENCY)

    showRemainingChars();

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
    schedulableType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);
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
 * @param eventName name of the edited event
 * @param eventDescription description of the edited event
 * @param eventStartDate start date of the edited event
 * @param eventEndDate end date of the edited event
 * @param projectStart start date of the project (the earliest an event can start)
 * @param projectEnd end date of the project (the latest an event can end)
 */
function showEditEvent(eventId, eventBoxId, eventName, eventDescription, eventStartDate, eventEndDate, projectStart, projectEnd) {
    /* Search for the edit form */
    let editForm = null;    // Split to account for weird behaviour
    editForm = document.getElementById("editEventForm-" + eventBoxId);

    /* Collapse element, send stop message, and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditEvent(eventId, eventBoxId);
        return;
    }

    /* Collapse any edit event forms already on the page. If we find any, delay
       opening the new form by EDIT_FORM_CLOSE_DELAY. */
    let collapseElementList = document.getElementsByClassName("collapse show");
    let delay = 0;
    let differentEvent = false;
    for (let element of collapseElementList) {
        if (element.id.indexOf("editEventForm") != -1) {
            new bootstrap.Collapse(element).hide();
            delay = EDIT_FORM_CLOSE_DELAY;
            /* Check whether any form is for a different event, to see whether
               we need to send a stop editing message */
            if (element.id.indexOf("editEventForm-" + eventId) == -1) {
                differentEvent = true;  // Extracted to a variable to avoid sending extra messages (in a worst case scenario)
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

    /* Populate this form. Doing this from javascript is not the best, but our validation leaves no choice */
    editForm.querySelector("#name").setAttribute("value", eventName);
    editForm.querySelector("#description").setAttribute("value", eventDescription);
    editForm.querySelector("#startDate").setAttribute("value", eventStartDate.substring(0, 10));
    editForm.querySelector("#startDate").setAttribute("min", projectStart);
    editForm.querySelector("#startDate").setAttribute("max", projectEnd);
    editForm.querySelector("#startTime").setAttribute("value", eventStartDate.substring(11, 16));
    editForm.querySelector("#endDate").setAttribute("value", eventEndDate.substring(0, 10));
    editForm.querySelector("#endDate").setAttribute("min", projectStart);
    editForm.querySelector("#endDate").setAttribute("max", projectEnd);
    editForm.querySelector("#endTime").setAttribute("value", eventEndDate.substring(11, 16));
    showRemainingChars();   // Used to update the remaining number of chars for name and description

    /* Set up JS to intercept the request */
    const formElem = editForm.querySelector("#form");
    if (formElem != null) {
        formElem.addEventListener("submit", e => sendEditEventViaAjax(formElem, e));    // Send error via AJAX request
        formElem.setAttribute("id", "form-js-enabled"); // Remove ability to add more listeners to this form
    }

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
