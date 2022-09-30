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

    const group_list = document.getElementsByClassName('group-block');


    if (groupMessageOutput.shortName === null) { // Delete the group
        if (groupLogs) {
            console.log("Deleting group with id " + groupMessageOutput.id);
        }
        const groupBox = document.getElementById("group-" + groupMessageOutput.id);
        groupBox.parentElement.removeChild(groupBox);
    } else {
        // see if group exists, if does, update, else make new box
        const groupBox = document.getElementById("group-" + groupMessageOutput.id);
        if (groupBox === null) {
            createNewGroupDiv(groupMessageOutput);
        } else {
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

function createNewGroupDiv(groupMessageOutput) {

}

/**
 * Creates a new table row with the user's info for the specified group
 * @param groupId
 * @param user
 */
function createNewGroupMember(groupId, user) {
    console.log(groupId + user.fullName + user.id);

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