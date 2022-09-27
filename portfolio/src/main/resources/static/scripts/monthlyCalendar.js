const milestoneIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trophy" viewBox="0 0 16 16"><path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935zM3.504 1c.007.517.026 1.006.056 1.469.13 2.028.457 3.546.87 4.667C5.294 9.48 6.484 10 7 10a.5.5 0 0 1 .5.5v2.61a1 1 0 0 1-.757.97l-1.426.356a.5.5 0 0 0-.179.085L4.5 15h7l-.638-.479a.501.501 0 0 0-.18-.085l-1.425-.356a1 1 0 0 1-.757-.97V10.5A.5.5 0 0 1 9 10c.516 0 1.706-.52 2.57-2.864.413-1.12.74-2.64.87-4.667.03-.463.049-.952.056-1.469H3.504z"/></svg>';
const deadlineIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-hourglass-split" viewBox="0 0 16 16"><path d="M2.5 15a.5.5 0 1 1 0-1h1v-1a4.5 4.5 0 0 1 2.557-4.06c.29-.139.443-.377.443-.59v-.7c0-.213-.154-.451-.443-.59A4.5 4.5 0 0 1 3.5 3V2h-1a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1h-1v1a4.5 4.5 0 0 1-2.557 4.06c-.29.139-.443.377-.443.59v.7c0 .213.154.451.443.59A4.5 4.5 0 0 1 12.5 13v1h1a.5.5 0 0 1 0 1h-11zm2-13v1c0 .537.12 1.045.337 1.5h6.326c.216-.455.337-.963.337-1.5V2h-7zm3 6.35c0 .701-.478 1.236-1.011 1.492A3.5 3.5 0 0 0 4.5 13s.866-1.299 3-1.48V8.35zm1 0v3.17c2.134.181 3 1.48 3 1.48a3.5 3.5 0 0 0-1.989-3.158C8.978 9.586 8.5 9.052 8.5 8.351z"/></svg>';
const eventIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"> <path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>';


/**
 * Takes the project start and end dates from monthlyCalendar.html and returns them as JS Date objects.
 * NOTE: JS Date objects start months at 0, not 1!
 * https://stackoverflow.com/questions/15677869/how-to-convert-a-string-of-numbers-to-an-array-of-numbers
 * @param originalDateString in format 'yyyy-mm-dd [other stuff that gets ignored]'
 * @return {Date} Date object with a time of midnight (00:00:00)
 */
function getDateFromProjectDateString(originalDateString) {
    const dateString = String(originalDateString.split(" ")[0]); // remove time
    let year, month, day;
    [year, month, day] = dateString.split("-").map(function(item) {
        return parseInt(item, 10);
    });
    return new Date(year, month - 1, day);
}

/**
 * Returns a string corresponding to the given date object.
 * In the returned string, months start from 1 (unlike JS dates). Month and day fields are padded with 0s.
 * @param date JS date object
 * @return {string} format "yyyy-mm-dd"
 */
function getStringFromDate(date) {
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
}


/**
 * Creates & returns an array of schedulable icon event objects for every single day in the project.
 * Each day will have an icon for events, one for milestones, and one for deadlines.
 * Icons are created with a count of 0 and so will not be visible by default.
 * Icon event objects have an ID in the format "[type]-YYYY-MM-DD", e.g. "deadline-2022-04-23".
 * Months go from 1 to 12 (different from Date objects in JS!).
 * @return {*[]} array of icon event objects
 */
function getSchedulableIconInfo() {
    let icons = [];
    let start = getDateFromProjectDateString(projectStartDate);
    const end = getDateFromProjectDateString(projectEndDate);
    const time = "T00:00:00"; // added on to dates to hide the background

    while (start < end) {
        // date in yyyy-mm-dd format
        const date = getStringFromDate(start);
        icons.push(
            {
                id: `deadline-${date}`,
                start: `${date}${time}`,
                extendedProps: { type: 'deadline', num: 0, schedulableNames: [], description: '' }
            },
            {
                id: `milestone-${date}`,
                start: `${date}${time}`,
                extendedProps: { type: 'milestone', num: 0, schedulableNames: [], description: '' }
            },
            {
                id: `event-${date}`,
                start: `${date}${time}`,
                extendedProps: { type: 'event', num: 0, schedulableNames: [], description: '' }
            });
        let newStart = new Date(start); // on the advice of https://stackoverflow.com/a/19691491
        newStart.setDate(newStart.getDate() + 1);
        start = newStart;
    }
    return icons;
}


/**
 * Grabs all the sprint info from the HTML template and creates a list of FullCalendar events from it.
 * @return {*[]}
 */
function getSprintInfo() {
    // Converting the given sprint's string names, start date and end date to list
    let sprintIdsList = sprintIds.split(",");
    let sprintNamesList = sprintNames.split(",");
    let sprintStartDatesList = sprintStartDates.split(",");
    let sprintEndDatesList = sprintEndDates.split(",");
    let sprintColoursList = sprintColours.split(",");

    // Creating one list for calendar sprints
    let sprints = [];
    for(let i = 0; i < sprintNamesList.length; i++) {
        sprints.push( {id: sprintIdsList[i], title: sprintNamesList[i], start: sprintStartDatesList[i],
            end: sprintEndDatesList[i], extendedProps: { type: 'sprint' }, backgroundColor: sprintColoursList[i],
            textColor: getTextColour(sprintColoursList[i]), classNames: 'defaultEventBorder'})
    }
    return sprints;
}


/**
 * Combines gets a list of sprint events and icon events, and returns their concatenation.
 * @return {*[]} concatenation of sprint event list and icon event list
 */
function getEventList() {
    const iconEvents = getSchedulableIconInfo();
    const sprintEvents = getSprintInfo();
    return iconEvents.sort((a, b) => a.id.localeCompare(b.id)).reverse().concat(sprintEvents);
}


/**
 * Will return "black" or "white" based on the provided hex colour string. This is so that text is visible.
 * based on https://www.w3.org/TR/AERT/#color-contrast
 * and https://stackoverflow.com/questions/11867545/change-text-color-based-on-brightness-of-the-covered-background-area
 * (Alex Ball's answer)
 * @param hexColourString
 * @return {string}
 */
function getTextColour(hexColourString) {
    let r = parseInt(hexColourString.slice(1, 3), 16);
    let g = parseInt(hexColourString.slice(3, 5), 16);
    let b = parseInt(hexColourString.slice(5, 7), 16);

    let brightness = Math.round((r * 299 + g * 587 + b * 114) / 1000);
    return (brightness > 125) ? 'black' : 'white';
}


document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');
    let selectedSprint = null;
    let sprintWithEvents = getEventList();
    let mouseoverSprint = false;


    // de-select sprint when mouse is clicked if mouse not over a sprint
    document.addEventListener('mousedown', () => {
        if(!mouseoverSprint){
            if (selectedSprint != null) {
                // removing previous sprint border colour and width
                selectedSprint.setProp('classNames', 'defaultEventBorder');

                // de-select sprint
                selectedSprint.setProp('durationEditable', false);
                selectedSprint = null;
            }
        }
    })


    let calendar = new FullCalendar.Calendar(calendarEl, {
        eventResizableFromStart: (sprintsEditable === "true"), // when resizing sprints, can be done from start as well as end
        eventDurationEditable: false,                          // sprints can't be edited by default
        timeZone: 'UTC',
        themeSystem: 'bootstrap5',
        initialView: 'dayGridMonth',
        eventOrder: "-id",

        // Restricts the calendar dates based on the given project dates
        validRange: {
            start: projectStartDate,
            end: projectEndDate
        },
        buttonText: {
            today: "Today",
            prev: "<",
            next: ">"
        },

        eventDidMount: function(info) {
            if(info.event.extendedProps.type != 'sprint'){
                var tooltip = new bootstrap.Tooltip(info.el, {
                    title: info.event.extendedProps.description,
                    placement: "top",
                    trigger: "hover",
                    container: "body",
                    html: true
                });
            }
        },

        eventOverlap: function (stillEvent, movingEvent) {
            if (stillEvent.extendedProps.type === 'sprint') {
                // shows the sprint overlap error message
                document.getElementById("invalidDateRangeError").hidden = false;

                // sets the sprint overlap error message based on the selected sprint
                document.getElementById("invalidDateRangeError").innerText = "Error: " + movingEvent.title.toString()
                + " must not overlap with " + stillEvent.title.toString();
            } else {
                return true;
            }
        },
        eventClick: function(info) {
            if (sprintsEditable === "true" && info.event.extendedProps.type === 'sprint') {
                // if the user clicks on a sprint, update the selected sprint
                if (selectedSprint != null) {
                    // removing previous sprint border colour and width
                    selectedSprint.setProp('classNames', 'defaultEventBorder');

                    // remove editing from previous sprint
                    selectedSprint.setProp('durationEditable', false);
                }
                selectedSprint = info.event;

                // adding current sprint border colour and width
                selectedSprint.setProp('classNames', 'currentEventBorder');

                // allow editing on new selected sprint
                selectedSprint.setProp('durationEditable', true);

                // hides the sprint overlap error message
                document.getElementById("invalidDateRangeError").hidden = true;
            } else if(info.event.extendedProps.type != 'sprint'){
                //option to click on schedulable icons to display tooltips so that they can be viewed on mobile
                bootstrap.Tooltip.getInstance(info.el).show();
            }

        },
        eventResize: function(info) {
            // if the user clicks on a sprint, update the selected sprint
            if (sprintsEditable === "true" && info.event.extendedProps.type === 'sprint') {
                // getting the form element by its id
                const form  = document.getElementById('sprintForm');

                // update the selected sprint dates
                document.getElementById("sprintId").value = info.event.id;
                document.getElementById("sprintStartDate").value = info.event.startStr;
                document.getElementById("sprintEndDate").value = info.event.endStr;

                // submitting the form
                form.submit();
            }
        },
        // detect when mouse is over a sprint to deselect sprints when clicking outside them
        eventMouseEnter: function () {mouseoverSprint = true},
        eventMouseLeave: function() {mouseoverSprint = false},
        // Used to show all the sprints on the calendar
        events: sprintWithEvents,
        eventContent: function(arg) {
            switch(arg.event.extendedProps.type) {
                case 'event':
                    const eventContent = eventIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: eventContent }
                case 'milestone':
                    const milestoneContent = milestoneIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: milestoneContent }
                case 'deadline':
                    const deadlineContent = deadlineIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: deadlineContent }
                default:
                    return;
            }
       },
       // docs: https://fullcalendar.io/docs/classname-input
       // you can add "hidden" to the class string, and it will hide the icon without repositioning anything else
       eventClassNames: function(arg) {
            if (['event', 'milestone', 'deadline'].includes(arg.event.extendedProps.type)) {
                return arg.event.extendedProps.num === 0 ? [ 'schedulable-icon hidden' ] : [ 'schedulable-icon' ]
            }
       }
    });

    // Move calendar to project start date, update all the icons to have the right number, and render the calendar
    calendar.gotoDate(projectStartDate);
    updateIconObjectsWithSchedulables(calendar);
    calendar.render();
});


/**
 * Updates the list icons for the calendar with the provided schedulable information.
 * For each schedulable object, every day it occurs will have its icon's counter increased,
 * and its name added to the icon's name list.
 * @param calendar the calendar object to update. Must have icons for each day in the project.
 */
function updateIconObjectsWithSchedulables(calendar) {
    const sNames = schedulableNames.split(", ");
    const sTypes = schedulableTypes.split(", ");
    const sStarts = schedulableStartDates.split(", ");
    const sEnds = schedulableEndDates.split(", ");

    const count = sNames.length; // number of schedulables
    for (let i = 0; i < count; i++) {
        let start = getDateFromProjectDateString(sStarts[i]);
        const end = getDateFromProjectDateString(sEnds[i]);

        while (start <= end) {
            const id = `${sTypes[i]}-${getStringFromDate(start)}`;
            const icon = calendar.getEventById(id);
            icon.setExtendedProp("num", icon.extendedProps.num + 1);
            icon.setExtendedProp("schedulableNames", icon.extendedProps.schedulableNames.concat([sNames[i]]));
            let schedulableTooltip = createTooltipString(icon, sNames, sStarts, sEnds, i);
            if (icon.extendedProps.description == '') {
                icon.setExtendedProp("description", schedulableTooltip);
            } else{
                icon.setExtendedProp("description", icon.extendedProps.description + '<br>' + schedulableTooltip)
            }

            let newStart = new Date(start); // on the advice of https://stackoverflow.com/a/19691491
            newStart.setDate(newStart.getDate() + 1);
            start = newStart;
        }
    }
}

/**
 * Creates schedulable tooltip contents for an icon on the monthly calendar.
 * Helper function for updateIconObjectsWithSchedulables
 * @returns {String} the contents for the tooltip, as determined by type
 */
function createTooltipString(icon, sNames, sStarts, sEnds, i) {
    let tooltip = `<strong>${sNames[i]}</strong>`;
    // Work out what to add to tooltip based on
    switch(icon.extendedProps.type) {
        case 'event':
            // Needs start and end dates and times
            tooltip += ` <small><i>${sStarts[i]} - ${sEnds[i]}</i></small>`;
            break;
        case 'deadline':
            // Needs date and time
            tooltip += ` <small><i>${sStarts[i]}</i></small>`;
            break;
        case 'milestone':
            // Needs date
            tooltip += ` <small><i>${sStarts[i].substring(0,10)}</i></small>`;
            break;
        default:
            // This shouldn't ever be reached
            break;
    }

    return tooltip;
}