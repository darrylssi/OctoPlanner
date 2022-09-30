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
 * Show a pop up that says 'Changes have been saved' when adding, deleting or editing groups.
 */
function showUpdateMessage() {
    let toastLiveExample = document.getElementById('success-box');
    let toast = new bootstrap.Toast(toastLiveExample);
    toast.show();
}