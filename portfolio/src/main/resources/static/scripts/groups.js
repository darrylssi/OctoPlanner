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