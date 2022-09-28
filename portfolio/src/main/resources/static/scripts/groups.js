/**
 * Swaps the visible and hidden text attributes for the specified group's show/hide users button.
 * @param button the button element to swap text for
 */
function toggleUsersButton(button) {
    let currentText = button.innerHTML;
    let hiddenText = button.getAttribute("showText")
    button.innerHTML=hiddenText;
    button.setAttribute("showText", currentText);
}