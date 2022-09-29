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