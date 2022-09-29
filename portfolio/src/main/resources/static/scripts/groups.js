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
 * Updates the page in-place based on the group update message received
 * @param groupMessage
 */
function handleGroupUpdateMessage(groupMessage) {
    if (groupLogs) {
        console.log(groupMessage);
    }

    const group_list = document.getElementsByClassName('group-block');


    if (groupMessage.shortName == null) {
    // Delete the group
    } else {

    }
}