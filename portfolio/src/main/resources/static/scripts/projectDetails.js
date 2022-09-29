const currentSchedulable = {type:"", id:-1}; // the current schedulable being edited by THIS user (only one can be edited at a time)
const SCHEDULABLE_EDIT_MESSAGE_FREQUENCY = 1800; // how often editing messages are sent while someone is editing a schedulable
const SCHEDULABLE_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing schedulable messages after this many ms
const schedulableTimeouts = new Map(); // holds schedulable ids and setTimeout functions in a key/value pair mapping
let sendEditMessageInterval;
const EDIT_FORM_CLOSE_DELAY = 300;
const DATES_IN_WRONG_ORDER_MESSAGE = "Start date must always be before end date";
const EDIT_NOTIFICATIONS = ['event', 'deadline', 'milestone'];

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
        if (type in EDIT_NOTIFICATIONS) {
            stompClient.send("/app/schedulables", {}, JSON.stringify({id: id, type: type}));
            hideModal();
        } else {
            sendSprintUpdatedMessage(id);
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
 * This submits the form and shows error messages if there are any.
 * @param elem HTML Form element
 */
function saveSprint(sprintId, elem) {
    // Remove existing error messages
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            // Upon success, hide the edit project form and reload the page
            hideEditSchedulable('1', sprintId, 'sprint');
            sendSprintUpdatedMessage(sprintId);
            window.location.reload();
        } else {
            // Otherwise, show the error messages
            const errors = formRequest.responseText.split('\n');
            for (let errorMsg of errors) {
                let field = "name";
                if (errorMsg === DATES_IN_WRONG_ORDER_MESSAGE || errorMsg.indexOf('end date') !== -1) {
                    field = 'endDate';
                } else if (errorMsg.indexOf('date') !== -1 || errorMsg.indexOf('sprint') !== -1) {
                    field = 'startDate'
                } else if (errorMsg.indexOf('exceed') !== -1) {
                    field = 'description'
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
 * The function shows the selected sprint edit form. All of the other sprint edit forms are closed.
 * @param sprint Gets the selected edit sprint object
 */
function showEditSprintForm(sprint) {
    /* Search for the edit form */
    let editForm = document.getElementById("editSprintForm-" + sprint.id);
    hideErrorBoxes(editForm);
    prefillSchedulable(editForm, sprint, 'sprint');

    /* Collapse element, send stop message, and take no further action if the selected form is open */
    if (editForm != null && editForm.classList.contains("show")) {
        hideEditSchedulable('1', sprint.id, 'sprint');
        return;
    }

    /* Collapse any edit forms already on the page. If we find any, delay
       opening the new form by EDIT_FORM_CLOSE_DELAY. */
    let collapseElementList = document.getElementsByClassName("collapse show");
    let delay = collapseElementList.length > 0 ? EDIT_FORM_CLOSE_DELAY : 0;
    let different = false;
    for (let element of collapseElementList) {
        new bootstrap.Collapse(element).hide();
        /* Check whether any form is for a different form, to see whether
           we need to send a stop editing message */
        if (element.id.indexOf("editSprintForm-" + sprint.id) === -1) {
            different = true;  // Extracted to a variable to avoid sending extra messages (worst case)
        }
    }
    showRemainingChars();   // Used to update the remaining number of chars for name and description

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "editSprintForm-" + sprint.id);
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
            if (type === 'sprint'){
                sendSprintUpdatedMessage(formRequest.response);
                window.location.reload();
            } else {
                // Success
                hideForm(formRequest.response, elem.getAttribute('formBoxId'), type);
                stompClient.send("/app/schedulables", {}, JSON.stringify({id: formRequest.response, type: type}));
                if (url.indexOf("add") !== -1) {
                    resetAddForm(type);
                }
                // Update tooltips, because bootstrap needs to be told to do this
                setTimeout((schedulableId) => {
                    let schedulable = document.getElementById(`${schedulableId}`);
                    let tooltip = bootstrap.Tooltip.getInstance(schedulable);
                    if (tooltip) {
                        tooltip.update();
                    } else if (schedulable) {
                        tooltip = new bootstrap.Tooltip(schedulable, {
                            trigger: 'hover'
                        });
                    }
                }, 250, type + "-" + formRequest.response);
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
 * An attempt to make this function deal with schedulable objects... TODO JSDoc needs to be better
 * @param schedulableId the id of the schedulable object being edited
 * @param schedulableBoxId the id of the box element of the schedulable object being edited
 * @param schedulableType the type of the schedulable object (event, deadline, or milestone) as a string
 * @param schedulable the schedulable object itself
 */
function showEditSchedulable(schedulableId, schedulableBoxId, schedulableType, schedulable) {
    /* Capitalize only the first letter of the schedulableType string */
    const capitalisedType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

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

    showRemainingChars();   // Used to update the remaining number of chars for name and description

    /* Get this form to show after a delay that allows any other open forms to collapse */
    setTimeout((formId) => {
        let shownForm = document.getElementById(formId)
        new bootstrap.Collapse(shownForm).show();
        shownForm.scroll({ top: shownForm.scrollHeight, behavior: "smooth"})
    }, delay, "edit" + capitalisedType + "Form-" + schedulableBoxId);
}

/**
 * Populates the edit sprint/schedulable form with the current details of the sprint/schedulable.
 * @param editForm Edit sprint/schedulable form
 * @param schedulable Sprint/Schedulable object
 * @param type the type of the sprint/schedulable, e.g. 'sprint', 'deadline', 'milestone', or 'event'
 */
function prefillSchedulable(editForm, schedulable, type) {
    if (type === 'sprint') {
        editForm.querySelector("#sprintName").value = schedulable.sprintName;
        editForm.querySelector("#sprintDescription").value =  schedulable.sprintDescription;
        editForm.querySelector("#sprintStartDate").value = schedulable.startDay;
        editForm.querySelector("#sprintEndDate").value =  schedulable.endDay;
    } else {
        editForm.querySelector("#name").value = schedulable.name;
        editForm.querySelector("#description").value = schedulable.description;
        editForm.querySelector("#startDate").value = schedulable.startDay;

        if (type !== 'milestone'){
                editForm.querySelector("#startTime").value = schedulable.startTime;
        }
        if (type === 'event'){
            editForm.querySelector("#endDate").value = schedulable.endDay;
            editForm.querySelector("#endTime").value = schedulable.endTime;
        }
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
    const capitalisedType = schedulableType.charAt(0).toUpperCase() + schedulableType.slice(1);

    /* Search for the edit form */
    let editForm = document.getElementById("edit" + capitalisedType + "Form-" + schedulableBoxId);

    if (editForm) { // Just in case
        new bootstrap.Collapse(editForm).hide();
    }

    if (schedulableType in EDIT_NOTIFICATIONS) {
        stopEditing();
    }
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
 * Decides whether the schedulable message means to show editing or hide editing.
 * @param editMessage JSON object received from the WebSocket
 */
function handleSchedulableMessage(editMessage) {
    if (editMessage.content.split(',').length === 3) {
        if (editingLogs) {
            console.log('GOT EDITING MESSAGE ' + editMessage.content);
        }
        showEditingMessage(editMessage);
    } else {
        if (editingLogs) {
            console.log('GOT STOP MESSAGE ' + editMessage.content);
        }
        hideEditMessage(editMessage);
    }
}

/**
 * Handles an incoming sprint update message by prompting the user to refresh.
 * Should also log something if the logging variable is true.
 * @param sprintMessage the message containing information about the sprint. Name, dates etc.
 */
function handleSprintUpdateMessage(sprintMessage) {
    // logging
    if (sprintLogs) {
        console.log('GOT UPDATE SPRINT MESSAGE FOR ' + sprintMessage.name + " ID " + sprintMessage.id);
    }

    // Show the user an alert warning them that the page needs to be refreshed
    sprintProjectAlert();
}

/**
 * Responds to discovering a project has been updated (via websockets)
 */
function handleProjectUpdateMessage(projectMessage) {
    // logging
    if (projectLogs) {
        console.log('GOT UPDATE PROJECT MESSAGE FOR ' + projectMessage.name + " ID " + projectMessage.id);
    }

    // Show the user an alert warning them that the page needs to be refreshed
    sprintProjectAlert();
}

/**
 * Shows editing-schedulable notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingMessage(editMessage) {
    const schedulableId = editMessage.content.split(',')[0]; // couldn't seem to substitute these directly into the template string
    const username = editMessage.from;
    const type = editMessage.content.split(',')[1]; // Type of schedulable
    const userId = editMessage.content.split(',')[2]; // the id of the user editing the schedulable
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId !== docUserId) {
        // stops any existing timeouts so that the message is shown for the full length
        stopSchedulableTimeout(schedulableId, type);

        // locate the correct elements on the page
        const editingSchedulableBoxClass = `${type}-${schedulableId}-editing-box`;
        const editingSchedulableTextBoxClass = `${type}-${schedulableId}-editing-text`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxClass);
        const editingSchedulableTextBoxes = document.getElementsByClassName(editingSchedulableTextBoxClass);

        // update the text and make it visible
        for (const schedulableTextBox of editingSchedulableTextBoxes) {
            if (schedulableTextBox) {
                schedulableTextBox.innerHTML = `${username} is editing this ${type}`;
            }
        }
        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "visible";
            }
        }

        // Hide it after 8s
        schedulableTimeouts.set((schedulableId, type), setTimeout(function() {hideEditMessage(editMessage)}, SCHEDULABLE_EDIT_MESSAGE_TIMEOUT));
    }
}

/**
 * Hides the editing message for the specified schedulable and clears the timer running for it.
 * Will clear the messages from all schedulable boxes for that schedulable (such as if it spans many sprints).
 * @param message the stop message that was received
 */
function hideEditMessage(message) {
    // this check is so that if you are editing an schedulable that someone else is editing, you don't hide their message
    // when you close your form. Their message would reappear without this anyway but it avoids confusion.
    if (document.getElementById('user').getAttribute('data-name') !== message.from) {
        const schedulableId = message.content.split(',')[0];
        const type = message.content.split(',')[1];


        const editingSchedulableBoxId = `${type}-${schedulableId}-editing-box`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxId);
        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "hidden";
            }
        }
        stopSchedulableTimeout(schedulableId, type);
    }
}

/**
 * Stops the timeout for the specified schedulable, if it exists
 * @param schedulableId the schedulable to stop the timeout for
 * @param type the type of schedulable, e.g. event, deadline, or milestone
 */
function stopSchedulableTimeout(schedulableId, type) {
    if (schedulableTimeouts.has((schedulableId, type))) {
        clearTimeout(schedulableTimeouts.get((schedulableId, type)));
    }
}

/**
* Updates all instances of a schedulable that has been changed using information sent through websockets
* @param schedulableMessage the message sent through websockets with schedulable information
*/
function updateSchedulable(schedulableMessage) {
    if (updateLogs) {
        console.log("Got update schedulable message for " + schedulableMessage.type + " " + schedulableMessage.id);
        console.log(schedulableMessage);
    }

    if(currentSchedulable.id === schedulableMessage.id && currentSchedulable.type === schedulableMessage.type) {
        stopEditing();
    }
// get a list of schedulable list containers
    const schedulable_lists = document.getElementsByClassName('schedulable-list-container');

// check each schedulable list container to see if it has the schedulable in it / should have the schedulable in it
    for (let schedulableListContainer of schedulable_lists) {
          //check if schedulable is there, then remove schedulable if it exists
          let schedulable = schedulableListContainer.querySelector('#' + schedulableMessage.type + '-' + schedulableMessage.id);
          if (schedulable !== null) {
            schedulable.parentNode.parentNode.parentNode.remove();
          }
          // check if schedulable list container is in the list of ids the schedulable should be displayed in
          let idIndex = schedulableMessage.schedulableListIds.indexOf(schedulableListContainer.id);
        if(idIndex !== -1) {

            const url = BASE_URL + "frag/" + schedulableMessage.type + '/' + schedulableMessage.id + '/' + schedulableMessage.schedulableBoxIds[idIndex];
            const schedulableFragRequest = new XMLHttpRequest();
            schedulableFragRequest.open("GET", url, true);
            const tempIdIndex = idIndex;
            schedulableFragRequest.onload = () => {
                // Reload the page to get the updated list of sprints after the delete
                createSchedulableDisplay(schedulableMessage, schedulableListContainer, tempIdIndex, schedulableFragRequest.response);
            }
            schedulableFragRequest.send();
        }
    }
}

/**
* Creates a new schedulable display object and puts it into the correct place in the DOM
* @param schedulableMessage the message sent by websockets containing schedulable info to be displayed
* @param parent the parent object for the schedulable to be displayed in
* @param idIndex the index of this schedulable used to access values in the id lists
* @param schedulableHtml the html of this schedulable to be inserted into the page
*/
function createSchedulableDisplay(schedulableMessage, parent, idIndex, schedulableHtml) {
    let newSchedulable = document.createElement("div");
    newSchedulable.innerHTML = schedulableHtml;

    // Force tooltip to update
    setTimeout((schedulable) => {
        let tooltip = bootstrap.Tooltip.getInstance(schedulable);
        if (tooltip) {
            tooltip.update();
        } else if (schedulable) {
            tooltip = new bootstrap.Tooltip(schedulable, {
                trigger: 'hover'
            });
        }
    }, 250, newSchedulable.querySelector('.schedulable'));

    if(schedulableMessage.nextSchedulableIds[idIndex] === '-1') {
        parent.appendChild(newSchedulable);
    } else {
        parent.insertBefore(newSchedulable, parent.querySelector('#' + schedulableMessage.nextSchedulableIds[idIndex]).parentNode.parentNode.parentNode);
    }
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

/** Warn the user of changes to sprint/project dates */
function sprintProjectAlert() {
    const message = 'Sprint or Project dates have been changed. Please refresh to update the page.';
    const wrapper = document.createElement('div');
    wrapper.innerHTML = [
        '<div class="alert alert-warning alert-dismissible fade show" role="alert">',
        `   <div>${message}</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        '</div>'
    ].join('');

    let box = document.getElementById(`warning-box`);
    if(box && box.innerHTML.indexOf(message) == -1) {
        box.append(wrapper);
    }
}