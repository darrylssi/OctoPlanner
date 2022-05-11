

function toggleMenu() {
    if(document.getElementById("userMenu").className == "dropdown-content") {
        document.getElementById("userMenu").className = "dropdown-content show";
    } else {
        document.getElementById("userMenu").className = "dropdown-content";
    }
}

// Close the dropdown if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.profile-icon') && !(event.target.id == 'add-role-button')) {
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