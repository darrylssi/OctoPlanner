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

    }
}