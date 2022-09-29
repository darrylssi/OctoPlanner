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
* @param group_id the id of the group users are being selected from
*/
function startSelecting(group_id) {
    checkboxes = document.getElementsByClassName('checkbox');
    for (let checkbox of checkboxes) {
        if (checkbox.classList.contains("user-" + group_id)){
            checkbox.removeAttribute("hidden");
            checkbox.checked = false;
        } else {
                 checkbox.hidden = true;
        }
    }
}

/**
* Deselects all users and hides checkboxes
*/
function stopSelecting() {

}