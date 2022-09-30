/**
 * Swaps the visible and hidden text attributes for the specified group's show/hide users button.
 * @param button the button element to swap text for
 */
function toggleUsersButton(button) {
    const currentText = button.innerHTML;
    button.innerHTML=button.getAttribute("showText");
    button.setAttribute("showText", currentText);
}

/**
* Calls toggleUsersButton with the button of the group with the given id
* @param group_id the group id of the button to be toggled
*/
function toggleById(group_id) {
    toggleUsersButton(document.getElementById("user-button-" + group_id));
}

/**
* Hides the delete group modal
*/
function hideModal() {
    const modal = document.getElementById("deleteModal");
    modal.style.display = "none";
}

/**
 * Show a modal asking the user to confirm their deletion of the object with the given id and name.
 * @param id the id of the object, e.g. 12
 * @param name the name of the object as a string
 */
function showDeleteModal(id, name) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + name + "?";
    deleteButton.onclick = () => {deleteGroup(id)}
    modal.style.display = "block";
}

/**
* Sends a delete request to delete the group with the given id
* @param id the id of the group to be deleted
*/
function deleteGroup(id) {
    const url = BASE_URL + "delete-group/" + id;
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        hideModal();
        //TODO: change this to send a message to websockets for live updating
        window.location.reload();
    }
    deleteRequest.send();
}

/**
* Shows the checkboxes so that a user can select users in the group with the given id
* Shows buttons so that user can add and remove users from groups
* @param group_id the id of the group users are being selected from
* @param button the button that was clicked to start selecting
*/
function startSelecting(group_id, button) {
    console.log('start selecting ' + group_id);
    checkboxes = document.getElementsByClassName('checkbox');
    // show checkboxes in group being edited. hide other checkboxes
    for (let checkbox of checkboxes) {
        if (checkbox.classList.contains("user-" + group_id)){
            checkbox.removeAttribute("hidden");
        } else {
            checkbox.hidden = true;
            checkbox.firstChild.firstChild.nextElementSibling.checked = false;
        }
    }
    // show an 'add selected users' button for all groups other than the one being selected
    addUsersButtons = document.getElementsByClassName('btn-add-users');
    for (let addUsersButton of addUsersButtons) {
        if(addUsersButton.parentNode.id === 'add-users-' + group_id) {
            addUsersButton.parentNode.hidden = true;
        } else {
            addUsersButton.parentNode.removeAttribute("hidden");
        }
    }
    // show a 'remove selected users' button on the group being selected. hide other remove users buttons
    removeUsersButtons = document.getElementsByClassName('btn-remove-users');
    for (let removeUsersButton of removeUsersButtons) {
        if(removeUsersButton.parentNode.id === 'remove-users-' + group_id) {
            removeUsersButton.parentNode.removeAttribute("hidden");
        } else {
            removeUsersButton.parentNode.hidden = true;
        }
    }
    resetSelectingButton();
    button.classList.add('selecting');
    button.onclick = () => stopSelecting(group_id, button);
    button.innerHTML = 'Stop Selecting Users';
}

/**
* Deselects all users and hides checkboxes. Hides all 'add selected users' and 'remove selected users' buttons
* @param group_id the id of the group being deselected
* @param button the button that was clicked
*/
function stopSelecting(group_id, button) {
    console.log('stop selecting ' + group_id);
    resetSelectingButton();
    removeButtons = document.getElementsByClassName('btn-remove-users');
    addButtons = document.getElementsByClassName('btn-add-users');
    // hide all remove buttons
    for(let removeButton of removeButtons){
        removeButton.parentNode.hidden = true
    }
    // hide all add buttons
    for(let addButton of addButtons){
        addButton.parentNode.hidden = true
    }
    // hide all checkboxes
    checkboxes = document.getElementsByClassName('checkbox');
    for (let checkbox of checkboxes) {
        checkbox.hidden = true;
        checkbox.firstChild.firstChild.nextElementSibling.checked = false;
    }
}


/**
 * Submits the given form's request in Javascript, allowing for in-place
 * updating of the page.
 * @param group_id the id of the group being modified
 * @param action the action being done. 'remove-members' or 'add-members'
 */
function sendFormViaAjax(group_id, action) {
    let url = BASE_URL + 'groups/' + group_id + '/' + action;
    const formData = new FormData(document.getElementById('form'));
    const formRequest = new XMLHttpRequest();
    if(action === 'remove-members'){
        formRequest.open("DELETE", url);
    } else {
        formRequest.open("POST", url);
    }

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            window.location.reload();
        } else {
            //handle errors
        }
    }
    formRequest.send(formData);
}

/**
* Resets the button of a group that was in selecting mode to the default mode
*/
function resetSelectingButton() {
    currentSelectingButtons = document.getElementsByClassName('selecting');
    for (let currentSelectingButton of currentSelectingButtons){
        currentSelectingButton.classList.remove('selecting');
        currentSelectingButton.focus = false;
        currentSelectingButton.innerHTML = 'Select Users';
        currentSelectingButton.onclick = () => startSelecting(currentSelectingButton.getAttribute("groupid"), currentSelectingButton);
    }
}