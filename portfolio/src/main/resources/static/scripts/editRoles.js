/**
 * Note: This code will ONLY work if a BASE_URL variable is globally defined
 * on the webpage this code is linked into.
 */

if (BASE_URL === undefined) {
    throw new ReferenceError(
`Can't load editRoles.js because I can't see any BASE_URL global variable.
Elsewhere in this webpage, put the script tag:
<script th:inline="javascript">
        var BASE_URL = /*[[ \${@environment.getProperty('base-url')} ]] */ "/";
</script>`
    )
}

/**
 * Toggles the visibility of the line's associated add-role dropdown
 * @param {HTMLElement} elem The button being clicked
 * @param {PointerEvent} event The event (needed to prevent the global onclick from firing)
 */
function addRoleFunction(elem, event) {
    // Prevent the window.onclick below from firing
    event.stopPropagation();
    const userID = elem.getAttribute("data-userid");
    document.getElementById("myDropdown-" + userID).classList.toggle("show");
}

// Close the dropdown if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.add-role-button')) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        var i;
        for (i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}

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
        throw new ReferenceError("The provided element needs a 'data-userid' AND 'data-role' attribute");
    }
    removeRole(userID, role);
}

function removeRole(userId, role) {
    const url = `${BASE_URL}users/${userId}/remove-role/${role}`;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}