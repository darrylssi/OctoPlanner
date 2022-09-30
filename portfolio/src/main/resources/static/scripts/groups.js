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

    if (groupMessageOutput.shortName === null) { // Delete the group if it does not exist in the database
        deleteGroupElem(groupMessageOutput.id);
    } else {
        // see if group exists, if it does, update; else make new box
        const groupBox = document.getElementById("group-" + groupMessageOutput.id);
        if (groupBox === null) {
            createNewGroup(groupMessageOutput);
        } else {
            updateGroup(groupMessageOutput);
        }
    }
}

/**
 * Updates the long and short names and user list for the specified group on the page
 * @param groupMessageOutput
 */
function updateGroup(groupMessageOutput) {
    if (groupLogs) {
        console.log("Updating group with id " + groupMessageOutput.id);
    }
    // update names
    document.getElementById("group-" + groupMessageOutput.id + "-sname").innerHTML = groupMessageOutput.shortName;
    document.getElementById("group-" + groupMessageOutput.id + "-lname").innerHTML = groupMessageOutput.longName;
    // clear all users
    document.getElementById("group-" + groupMessageOutput.id + "-table").innerHTML = `<tbody id='group-${groupMessageOutput.id}-tbody'></tbody>`;
    // add all users
    for (let user of groupMessageOutput.members) {
        createNewGroupMember(groupMessageOutput.id, user);
    }
}

/**
 * Removes a group from the page
 * @param groupId
 */
function deleteGroupElem(groupId) {
    if (groupLogs) {
        console.log("Deleting group with id " + groupId);
    }
    const groupBox = document.getElementById("group-" + groupId);
    groupBox.parentElement.removeChild(groupBox);
}

/**
 * Creates a new group on the page
 * @param groupMessageOutput the group update message with the group's details
 */
function createNewGroup(groupMessageOutput) {
    if (groupLogs) {
        console.log("Creating group with id " + groupMessageOutput.id);
    }
    const url = BASE_URL + "frag/" + groupMessageOutput.id;
    const groupFragRequest = new XMLHttpRequest();
    groupFragRequest.open("GET", url, true);

    groupFragRequest.onload = () => {
        createGroupDisplay(groupMessageOutput, groupFragRequest.response)
    }
    groupFragRequest.send();
}

/**
 * Creates the HTML element for a new group
 * @param groupMessageOutput the message with the group's information
 * @param groupHTML the HTML for the group
 */
function createGroupDisplay(groupMessageOutput, groupHTML) {
    let newGroup = document.createElement("div");
    newGroup.classList.add("card");
    newGroup.classList.add("group-block");
    newGroup.classList.add("container-auto");

    newGroup.id = "group-" + groupMessageOutput.id;

    newGroup.innerHTML = groupHTML;

    const form = document.getElementById("form");
    form.appendChild(newGroup);

    // add all users
    if (groupMessageOutput.members.length > 0) {
        document.getElementById("group-" + groupMessageOutput.id + "-table").innerHTML = `<tbody id='group-${groupMessageOutput.id}-tbody'></tbody>`;
        for (let user of groupMessageOutput.members) {
            createNewGroupMember(groupMessageOutput.id, user);
        }
    }
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

/**
 * Creates the actual HTML element for a user in a group
 * @param groupId the id of the group to add the user to
 * @param user the user object (with id and name) to add to the group
 * @param memberHTML the HTML for the user
 */
function createGroupMemberDisplay(groupId, user, memberHTML) {
    let newMember = document.createElement("tr");
    newMember.classList.add("row");
    newMember.innerHTML = memberHTML;

    // find table element and insert html response
    const table = document.getElementById("group-" + groupId + "-table");
    table.firstChild.appendChild(newMember);
}

/**
 * Submits the given group form's request in Javascript, allowing for in-place
 * updating of the page.
 * @param {HTMLFormElement} elem
 */
function sendGroupFormViaAjax(elem) {
    // Delete any pre-existing errors on the form
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[0]);
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[1]);
            sendGroupUpdatedMessage(formRequest.responseText.split(" ")[2].split("\n")[0]);
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[3]);
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[4]);
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[5]);
            // sendGroupUpdatedMessage(formRequest.responseText.split(" ")[6]);
            window.location.reload();
        } else {
            const errors = formRequest.responseText.split('\n');
            console.log(errors);

            for (let errorMsg of errors) {
                // Determine correct error field. Defaults to NameFeedback
                let field = "ShortName";
                if (errorMsg.indexOf('long name') !== -1) {
                        field = 'LongName'
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
function showDeleteModal(id, name, members) {
    const modal = document.getElementById("deleteModal");
    const deleteButton = document.getElementById("deleteButton");
    document.getElementsByClassName("modal-title")[0].textContent = "Are you sure you want to delete " + name + "?";
    document.getElementsByClassName("modal-description")[0].textContent = members+" users will be removed from this group and the group will be deleted. This cannot be undone.";
    deleteButton.onclick = () => {deleteGroup(id)}
    modal.style.display = "block";
}

/**
* Sends a delete request to delete the group with the given id
* @param id the id of the group to be deleted
*/
function deleteGroup(id) {
    const url = BASE_URL + "groups/" + id +"/remove-group";
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.open("DELETE", url, true);
    deleteRequest.onload = () => {
        hideModal();
        sendGroupUpdatedMessage(id);
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
    sendGroupUpdatedMessage(group_id);
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

/**
* Removes a single user from a group
* @param group_id the id of the group the user is being removed from
* @param user_id the id of the user being removed
*/
function removeUserFromGroup(group_id, user_id){
    let url = BASE_URL + 'groups/' + group_id + '/remove-members';
    const params = 'user_id=' + user_id;
    const removeUserRequest = new XMLHttpRequest();
    removeUserRequest.open("DELETE", url);
    removeUserRequest.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    removeUserRequest.onload = () => {
        if (removeUserRequest.status === 200) {
            window.location.reload();
        } else {
            //handle errors
        }
    }
    removeUserRequest.send(params);
}