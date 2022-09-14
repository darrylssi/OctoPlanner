const currentSchedulable = {type:"", id:-1}; // the current schedulable being edited by THIS user (only one can be edited at a time)
const SCHEDULABLE_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing a schedulable
let sendEditMessageInterval;
const EDIT_FORM_CLOSE_DELAY = 300;
const DATES_IN_WRONG_ORDER_MESSAGE = "Start date must always be before end date";

/** Hides the deletion confirmation modal without deleting a sprint/event/deadline */
function hideModal() {
    const modal = document.getElementById("deleteModal");
    modal.style.display = "none";
}

/**
 * Show a modal asking the user to confirm their deletion of the object with the given id, name, and type.
 * @param id the id of the object, e.g. 12
 * @param name the name of the object as a string
 * @param type the type of the object, e.g. 'sprint', 'deadline', 'milestone', or 'event'
 */
function showDeleteModal(id, name, type) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + name + "?";
    deleteButton.onclick = () => {deleteObject(id, type)}
    modal.style.display = "block";
}

/**
 * Sends an HTTP request to delete the object with the given type and ID.
 * @param id the id of the object, e.g. 12
 * @param type the type of the object, e.g. 'sprint', 'deadline', 'milestone', or 'event'
 */
function deleteObject(id, type) {
    const url = BASE_URL + "delete-" + type + "/" + id;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        // Send a websocket message to update the page after the deletion
        stompClient.send("/app/schedulables", {}, JSON.stringify({id: id, type: type}));
        hideModal();
        if (type === 'sprint') {
            window.location.reload();
        }
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
 * @param type the type of the schedulable, e.g. 'deadline', 'milestone', or 'event'
 */
function sendFormViaAjax(elem, type) {
    // Delete any pre-existing errors on the form
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);
    console.log(type + ' schedulable form submitted');

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            // Success
            hideForm(formRequest.response, elem.getAttribute('formBoxId'), type);
            stompClient.send("/app/schedulables", {}, JSON.stringify({id: formRequest.response, type: type}));
            if (url.indexOf("add") != -1) {
                resetAddForm(type);
            }
        } else {
            const errors = formRequest.responseText.split('\n');
            for (let errorMsg of errors) {
                // Determine correct error field. Defaults to NameFeedback
                let field = "Name";
                if (errorMsg === DATES_IN_WRONG_ORDER_MESSAGE || errorMsg.indexOf('end date') !== -1) {
                    field = 'EndDate';
                } else if (errorMsg.indexOf('end time') !== -1) {
                    field = 'EndTime';
                } else if (errorMsg.indexOf('date') !== -1) {
                    field = 'StartDate';
                } else if (errorMsg.indexOf('time') !== -1 || errorMsg.indexOf('minute') !== -1) {
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
    showRemainingChars();
}

/**
 * An attempt to make this function deal with schedulable objects...
 * @param schedulableId the id of the schedulable object being edited
 * @param schedulableBoxId the id of the box element of the schedulable object being edited
 * @param schedulableType the type of the schedulable object (event, deadline, or milestone) as a string
 * @param schedulable the schedulable object itself
 */
function showEditSchedulable(schedulableId, schedulableBoxId, schedulableType, schedulable) {
    /* Capitalize only the first letter of the schedulableType string */
    capitalisedType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

    /* Search for the edit form */
    let editForm = document.getElementById("edit" + capitalisedType + "Form-" + schedulableBoxId);
    prefillSchedulable(editForm, schedulable, schedulableType);
    hideErrorBoxes(editForm);

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
        new bootstrap.Collapse(element).hide();
        /* Check whether any form is for a different schedulable, to see whether
           we need to send a stop editing message */
        if (element.id.indexOf("edit" + capitalisedType + "Form-" + schedulableBoxId) === -1) {
            differentSchedulable = true;  // Extracted to a variable to avoid sending extra messages (worst case)
        }
    }

    /* If a form we just closed was for a different schedulable, we need to
       send a stop editing message */
     if (differentSchedulable) {
         stopEditing();
     }

     currentSchedulable.id = schedulableId;
     currentSchedulable.type = schedulableType;

    /* Send an initial message, cancel any current repeating messages, then start sending repeating messages. */
    sendEditingSchedulableMessage(schedulableId, schedulableType); // see https://www.w3schools.com/jsref/met_win_setinterval.asp
    if (sendEditMessageInterval) { // reset interval
        clearInterval(sendEditMessageInterval);
    }
    sendEditMessageInterval = setInterval(function() {sendEditingSchedulableMessage(schedulableId, schedulableType)}, SCHEDULABLE_EDIT_MESSAGE_FREQUENCY)
    showRemainingChars();

    showRemainingChars();   // Used to update the remaining number of chars for name and description

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "edit" + capitalisedType + "Form-" + schedulableBoxId);
}

/**
 * Populates the edit schedulable form with the current details of the schedulable.
 * @param editForm Edit schedulable form
 * @param schedulable Schedulable object
 * @param type the type of the schedulable, e.g. 'deadline', 'milestone', or 'event'
 */
function prefillSchedulable(editForm, schedulable, type) {
    editForm.querySelector("#name").value = schedulable.name;
    editForm.querySelector("#description").value =  schedulable.description;
    editForm.querySelector("#startDate").value = schedulable.startDay;
    if (type !== 'milestone'){
        editForm.querySelector("#startTime").value = schedulable.startTime;
    }
    if (type === 'event'){
        editForm.querySelector("#endDate").value = schedulable.endDay;
        editForm.querySelector("#endTime").value = schedulable.endTime;
    }
}

/**
 * Collapse the edit form for the specified schedulable box.
 * Accessed directly by the cancel button.
 * Sends a stop editing message for the previous schedulable & ceases sending repeated editing messages.
 * @param schedulableId the id of the schedulable object whose edit form is being hidden
 * @param schedulableBoxId the id of the box in which the edit form will be hidden
 * @param schedulableType the type of the schedulable whose edit form is being hidden
 */
function hideEditSchedulable(schedulableId, schedulableBoxId, schedulableType) {
    /* Capitalize only the first letter of the schedulableType string */
    capitalisedType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

    /* Search for the edit form */
    let editForm = document.getElementById("edit" + capitalisedType + "Form-" + schedulableBoxId);

    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
    stopEditing();
}

/**
 * Collapse the form with the specified Id.
 * Accessed directly by the cancel button.
 * Sends a stop editing message for the previous schedulable & ceases sending repeated editing messages.
 * @param schedulableId the ID of the schedulable being edited
 * @param formId the ID of the form to be closed
 * @param schedulableType the type of the editable being edited
 */
function hideForm(schedulableId, formId, schedulableType) {
    let editForm = document.getElementById(formId);
    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }
    if(currentSchedulable.id === parseInt(schedulableId) && currentSchedulable.type === schedulableType){
        stopEditing();
    }
}

/**
 * Sends a stop editing message for the previously edited schedulable, and stops any repeating edit messages from being sent.
 */
function stopEditing() {
    if (sendEditMessageInterval) {
        clearInterval(sendEditMessageInterval);
    }
    sendStopEditingMessage(currentSchedulable.id, currentSchedulable.type);
    currentSchedulable.id = -1;
    currentSchedulable.type = "";
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

/**
 * Resets the add schedulable form after submission.
 * @param type Type of schedulable to be added.
 */
function resetAddForm(type) {
    const capitalisedType = type.charAt(0).toUpperCase() + type.slice(1);
    let addForm = document.getElementById("add" + capitalisedType + "Form");
    addForm.querySelector("#addSchedulableNameInput").value = "";
    addForm.querySelector("#addSchedulableDescriptionInput").value =  "";

    // add default dates
    const today = new Date();
    addForm.querySelector("#schedulableStartDate").value = today.toISOString().substring(0,10);
    if (type !== 'milestone'){
        addForm.querySelector("#schedulableStartTime").value = today.getHours() + ":" + (today.getMinutes());
    }
    if (type === 'event'){
        addForm.querySelector("#schedulableEndDate").value = today.toISOString().substring(0,10);
        addForm.querySelector("#schedulableEndTime").value = today.getHours() + ":" + (today.getMinutes()+1);
    }

}
