
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

/**
 * Binds the child elements, such that the input's remaining length is displayed.
 * 
 * Requires:
 * * A single `<input type="text">` child, with a `maxlength` attribute
 * * A single "output" child, with a `.remaining-chars-field` class
 * 
 * @param {HTMLElement} inputGroup The parent element containing a single text input,
 * and a single element with a class of 'remaining-chars-field'
 * @throws {EvalError} If any of the above requirements are broken
 */
function displayRemainingCharacters(inputGroup) {
    const inputTags = Array
                        .from(inputGroup.getElementsByTagName('input'))
                        .filter(e => e.getAttribute('type') == 'text'
                                  && e.hasAttribute('maxlength'));
    if (inputTags.length != 1) {
        throw new EvalError(
            "Expected 1 child of type <input type=\"text\" maxlength=...>, got " + inputTags.length
        );
    }
    const outputTags = Array.from(inputGroup.getElementsByClassName('remaining-chars-field'));
    if (outputTags.length != 1) {
        throw new EvalError(
            "Expected 1 child with class '.remaining-chars-field' got " + inputTags.length
        );
    }

    const input = inputTags[0];
    const output = outputTags[0];
    output.textContent = input.getAttribute('maxlength');
    input.addEventListener("input", () => {
        const remainingChars = input.getAttribute('maxlength') - input.value.length;
        output.textContent = remainingChars;
        if (remainingChars <= 0) {
            output.classList.add('text-danger');
        } else {
            output.classList.remove('text-danger');
        }
    })
}

