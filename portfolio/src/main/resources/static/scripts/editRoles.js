
function addRoleFunction() {
    document.getElementById("myDropdown").classList.toggle("show");
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

function removeRole(userId, role) {
    const url = BASE_URL + "/users/" + userId + "/remove-role/" + role;
    const patchRequest = new XMLHttpRequest();
    patchRequest.open("PATCH", url, true);
    patchRequest.onload = () => {
        window.location.reload();
    }
    patchRequest.send();
}