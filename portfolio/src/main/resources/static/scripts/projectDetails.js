var previousEvent; // the previous event being edited by THIS user (only one can be edited at a time)
var previousEventBox; // same but for the eventbox
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
 */
function showEditEvent(eventId, eventBoxId, eventName, eventDescription, eventStartDate, eventEndDate) {
    /* Search for the edit form */
    let editForm = document.getElementById("editEventForm-" + eventBoxId);
    let previousEditForm = document.getElementById("editEventForm-" + previousEventBox);
    let delay = 0;

    /* Collapse element, send stop message, and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditEvent(eventBoxId);
        return;
    }
    /* If the previous form is open, send the stop message but let collapseall take care of it
       so that the delay still works */
    if (previousEditForm != null && previousEditForm.classList.contains("show")) {
        delay = EDIT_FORM_CLOSE_DELAY;
        if (previousEvent !== eventId) { // avoids flickering when you click on a different box for the same event
            stopEditing();
        }
    }

    /* Set the previous event values */
    previousEvent = eventId;
    previousEventBox = eventBoxId;

    /* Collapse any collapsible elements already on the page */
    const delayNew = collapseAllEditEventForms();
    delay = (delay === 0) ? delayNew : delay; // this is so that delay is still set if the previous form is open

    /* Create element if not seen before */
    if (editForm == null) {
        editForm = document.createElement("div");
        editForm.setAttribute("id", "editEventForm-" + eventBoxId);
        editForm.setAttribute("class", "editEventForm collapse");
        editForm.innerHTML = editFormTemplate;
        const formElem = editForm.querySelector("#form");
        formElem.addEventListener("submit", e => sendEditEventViaAjax(formElem, e));    // Send error via AJAX request
        document.getElementById("event-box-" + eventBoxId).appendChild(editForm);

        /* Set internal attributes of form and link cancel button */
        editForm.querySelector("#edit-event-form-header").innerHTML = "Editing " + eventName;
        editForm.querySelector("#editEventId").setAttribute("value", eventId);
        editForm.querySelector("#editEventNameInput").setAttribute("value", eventName);
        editForm.querySelector("#editEventDescriptionInput").setAttribute("value", eventDescription);
        editForm.querySelector("#editEventStartDate").setAttribute("value", eventStartDate.substring(0, 10));
        editForm.querySelector("#editEventStartTime").setAttribute("value", eventStartDate.substring(11, 16));
        editForm.querySelector("#editEventEndDate").setAttribute("value", eventEndDate.substring(0, 10));
        editForm.querySelector("#editEventEndTime").setAttribute("value", eventEndDate.substring(11, 16));
        editForm.querySelector("#cancel").onclick = function () {hideEditEvent(eventBoxId);};
    }

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    sendEditingEventMessage(eventId); // see https://www.w3schools.com/jsref/met_win_setinterval.asp
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    sendEditMessageInterval = setInterval(function() {sendEditingEventMessage(eventId)}, EVENT_EDIT_MESSAGE_FREQUENCY)

    /* Get this form to show after a delay that allows any other forms open to collapse */
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
function hideEditEvent(eventBoxId) {
    let editForm = document.getElementById("editEventForm-" + eventBoxId);
    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
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
 * Take a wild guess what this one does.
 * Hint: it has something to do with collapsing all edit event forms.
 * @return delay in ms to open form. Set to {@link EDIT_FORM_CLOSE_DELAY} if there are forms to close, else 0.
 */
function collapseAllEditEventForms() {
    let delay;
    let collapseElementList = document.getElementsByClassName("collapse show");
    delay = ((collapseElementList.length > 0) ? EDIT_FORM_CLOSE_DELAY : 0);
    for (let element of collapseElementList) {
        if (element.id.indexOf("editEventForm") !== -1) {
            new bootstrap.Collapse(element).hide();
        }
    }
    return delay;
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
