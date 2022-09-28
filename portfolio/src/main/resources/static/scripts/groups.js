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
 * Submits the given group form's request in Javascript, allowing for in-place
 * updating of the page.
 * @param {HTMLFormElement} elem
 * @param type the type of the schedulable, e.g. 'deadline', 'milestone', or 'event'
 */
function sendGroupFormViaAjax(elem) {
    // Delete any pre-existing errors on the form
    hideErrorBoxes(elem);

    const formData = new FormData(elem);
    const formRequest = new XMLHttpRequest();
    let url = elem.getAttribute('data-url');
    formRequest.open("POST", url);

    formRequest.onload = () => {
        if (formRequest.status === 200) {
            window.location.reload();
        } else {
            const errors = formRequest.responseText.split('\n');
            for (let errorMsg of errors) {
                // Determine correct error field. Defaults to NameFeedback
                let field = "shortName";
                // Need to write error test for long name

                field += 'Feedback';
                const errorBox = elem.querySelector(`[id*="` + field + `"]`);
                errorBox.textContent = errorMsg;
                errorBox.style.display = 'block';
            }
        }
    }
    formRequest.send(formData);
}