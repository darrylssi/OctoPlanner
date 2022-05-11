
function addRole(userId, role) {
    const url = BASE_URL + "users/" + userId + "/add-role/" + role;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}

function removeRole(userId, role) {
    const url = BASE_URL + "/users/" + userId + "/remove-role/" + role;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}