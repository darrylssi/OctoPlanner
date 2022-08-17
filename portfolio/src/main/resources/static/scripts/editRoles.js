/**
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
 * Adds a role to a user. Provided to each button in the dropdown
 *
 * @param {HTMLElement} elem The button being clicked
 */
function onclickAddRole(elem) {
    const userID = elem.getAttribute('data-userid');
    const role = elem.getAttribute('data-role');
    if (userID === undefined || role === undefined) {
        // The provided element needs 'data-userid' AND 'data-role' attributes
        throw new ReferenceError("Developer error: Put an onclick on the wrong element.");
    }
    addRole(userID, role);
}

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

/**
 * Toggles the dropdown with the associated id
 * @param elementId the id of the dropdown to be toggled
 */
function toggleRoleList(elementId) {
    hideAllRoleDropdowns();
    document.getElementById(elementId).classList.toggle("show");
}

/**
 * Does what it says on the tin - hides all add role dropdowns when called.
 */
function hideAllRoleDropdowns() {
    const dropdowns = document.getElementsByClassName("user-role-dropdown");
    for (const item of dropdowns) {
        item.classList.remove("show");
    }
}

/**
 * Returns true if there is a role in acceptableRoles that is not within targetUserRoles.
 * This would be used to check whether the add roles button should be visible, but you can't call JS from a th:if
 * statement. So, TODO remove this if it is not usable.
 * @param acceptableRoles
 * @param targetUserRoles
 * @return true if an element in acceptableRoles is not in targetRoles, else false
 */
function canAddRole(acceptableRoles, targetUserRoles) {
    for (const item of acceptableRoles) {
        if (!item in targetUserRoles) {
            return true;
        }
    }
    return false;
}