/**
/** Request to add a given role to a user */
function addRole(userId, role) {
    const url = `${BASE_URL}users/${userId}/add-role/${role}`;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}
 * Note: This code will ONLY work if a BASE_URL variable is globally defined
 * on the webpage this code is linked into.
 */

if (BASE_URL === undefined) {
    alert(`Failed loading editRoles.js - couldn't find BASE_URL variable.
If you're a dev, see how it's done in the /users/ HTML file.
If you're not a dev, politely tell us thanks.`
    )
}

/**
 * Toggles the visibility of the line's associated add-role dropdown
 *
 * Note these should be gotten rid of when Bootstrap comes along.
 * @param {HTMLElement} elem The button being clicked
 * @param {PointerEvent} event The event (needed to prevent the global onclick from firing)
 */
//function addRoleFunction(elem, event) {
//    // Prevent the window.onclick below from firing
//    event.stopPropagation();
//    const userID = elem.getAttribute("data-userid");
//    document.getElementById("myDropdown-" + userID).classList.toggle("show");
//}

// function addRole(userId, role) {
//     const url = BASE_URL + "users/" + userId + "/add-role/" + role;
//     const patchRequest = new XMLHttpRequest();
//     patchRequest.open("PATCH", url, true);
//     patchRequest.onload = () => {
//         window.location.reload();
//     }
//     patchRequest.send();
// }

/**
 * Removes the role from a user. Provided to each button.
 * 
 * @param {HTMLElement} elem 
 */
function onclickRemoveRole(elem) {
    const userID = elem.getAttribute('data-userid');
    const role = elem.getAttribute('data-role');
    if (userID === undefined || role === undefined) {
        // The provided element needs 'data-userid' AND 'data-role' attributes
        throw new ReferenceError("Developer error: Put an onclick on the wrong element.");
    }
    removeRole(userID, role);
}

/** Request to remove a given role from a user */
function removeRole(userId, role) {
    const url = `${BASE_URL}users/${userId}/remove-role/${role}`;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}


function toggleRoleList(elementId) {
    document.getElementById(elementId).classList.toggle("show");
}