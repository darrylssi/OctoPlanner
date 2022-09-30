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

    if (groupLogs) {
        console.log("Deleting group with id " + group_id);
    }
    const groupBox = document.getElementById("group-" + group_id);
    groupBox.parentElement.removeChild(groupBox);

    sendGroupUpdatedMessage(group_id); // TODO remove
}

/**
 * Updates the page in-place based on the group update message received
 * @param groupMessageOutput
 */
function handleGroupUpdateMessage(groupMessageOutput) {
    if (groupLogs) {
        console.log(groupMessageOutput);
    }

    // groupMessageOutput.id = 10;

    if (groupMessageOutput.shortName === null) { // Delete the group
        if (groupLogs) {
            console.log("Deleting group with id " + groupMessageOutput.id);
        }
        const groupBox = document.getElementById("group-" + groupMessageOutput.id);
        groupBox.parentElement.removeChild(groupBox);
    } else {
        // see if group exists, if it does, update; else make new box
        const groupBox = document.getElementById("group-" + groupMessageOutput.id);
        if (groupBox === null) {
            if (groupLogs) {
                console.log("Creating group with id " + groupMessageOutput.id);
            }
            createNewGroupDiv(groupMessageOutput);
        } else {
            if (groupLogs) {
                console.log("Updating group with id " + groupMessageOutput.id);
            }
            // update
            document.getElementById("group-" + groupMessageOutput.id + "-sname").innerHTML = groupMessageOutput.shortName;
            document.getElementById("group-" + groupMessageOutput.id + "-lname").innerHTML = groupMessageOutput.longName;
            // clear all users
            document.getElementById("group-" + groupMessageOutput.id + "-table").innerHTML = `<tbody id='group-${groupMessageOutput.id}-tbody'></tbody>`;
            // add all users
            for (let user of groupMessageOutput.members) {
                createNewGroupMember(groupMessageOutput.id, user);
            }
        }
    }
}

/**
 * Create a div for a new group on the page
 * @param groupMessageOutput the group update message with the group's details
 */
function createNewGroupDiv(groupMessageOutput) {
    const url = BASE_URL + "frag/" + groupMessageOutput.id;
    const groupFragRequest = new XMLHttpRequest();
    groupFragRequest.open("GET", url, true);

    groupFragRequest.onload = () => {
        let newGroup = document.createElement("div");
        newGroup.classList.add("card");
        newGroup.classList.add("group-block");
        newGroup.classList.add("container-auto");

        newGroup.id = "group-" + groupMessageOutput.id;

        newGroup.innerHTML = groupFragRequest.response;

        const form = document.getElementById("form");
        form.appendChild(newGroup);
    }
    groupFragRequest.send();
}

/**
 * Creates a new table row with the user's info for the specified group
 * @param groupId
 * @param user
 */
function createNewGroupMember(groupId, user) {
    const url = BASE_URL + "frag/" + groupId + '/' + user.id;
    const groupMemberFragRequest = new XMLHttpRequest();
    groupMemberFragRequest.open("GET", url, true);

    groupMemberFragRequest.onload = () => {
        createGroupMemberDisplay(groupId, user, groupMemberFragRequest.response);
    }
    groupMemberFragRequest.send();
}

function createGroupMemberDisplay(groupId, user, memberHTML) {
    let newMember = document.createElement("tr");
    newMember.classList.add("row");
    newMember.innerHTML = memberHTML;

    // find table element and insert html response
    const table = document.getElementById("group-" + groupId + "-table");
    table.firstChild.appendChild(newMember);
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