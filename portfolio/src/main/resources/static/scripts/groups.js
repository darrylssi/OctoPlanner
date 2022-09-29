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
* Shows the checkboxes so that a user can select users in the group with the given id
* Shows buttons so that user can add and remove users from groups
* @param group_id the id of the group users are being selected from
* @param button the button that was clicked to start selecting
*/
function startSelecting(group_id, button) {
    button.classList.add('selecting');
    button.onclick = () => stopSelecting(group_id, button);
    button.innerHTML = 'Stop Selecting Users';
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
}

/**
* Deselects all users and hides checkboxes. Hides all 'add selected users' and 'remove selected users' buttons
* @param group_id the id of the group being deselected
* @param button the button that was clicked
*/
function stopSelecting(group_id, button) {
    console.log('stop selecting ' + group_id);
    button.classList.remove('selecting');
    button.focus = false;
    button.innerHTML = 'Select Users';
    button.onclick = () => startSelecting(group_id, button);
    removeButtons = document.getElementsByClassName('btn-remove-users');
    addButtons = document.getElementsByClassName('btn-add-users');
    for(let removeButton of removeButtons){
        removeButton.parentNode.hidden = true
    }
    for(let addButton of addButtons){
        addButton.parentNode.hidden = true
    }
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