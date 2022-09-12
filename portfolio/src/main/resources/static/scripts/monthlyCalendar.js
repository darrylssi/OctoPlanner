const milestoneIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trophy" viewBox="0 0 16 16"><path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935zM3.504 1c.007.517.026 1.006.056 1.469.13 2.028.457 3.546.87 4.667C5.294 9.48 6.484 10 7 10a.5.5 0 0 1 .5.5v2.61a1 1 0 0 1-.757.97l-1.426.356a.5.5 0 0 0-.179.085L4.5 15h7l-.638-.479a.501.501 0 0 0-.18-.085l-1.425-.356a1 1 0 0 1-.757-.97V10.5A.5.5 0 0 1 9 10c.516 0 1.706-.52 2.57-2.864.413-1.12.74-2.64.87-4.667.03-.463.049-.952.056-1.469H3.504z"/></svg>';
const deadlineIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-hourglass-split" viewBox="0 0 16 16"><path d="M2.5 15a.5.5 0 1 1 0-1h1v-1a4.5 4.5 0 0 1 2.557-4.06c.29-.139.443-.377.443-.59v-.7c0-.213-.154-.451-.443-.59A4.5 4.5 0 0 1 3.5 3V2h-1a.5.5 0 0 1 0-1h11a.5.5 0 0 1 0 1h-1v1a4.5 4.5 0 0 1-2.557 4.06c-.29.139-.443.377-.443.59v.7c0 .213.154.451.443.59A4.5 4.5 0 0 1 12.5 13v1h1a.5.5 0 0 1 0 1h-11zm2-13v1c0 .537.12 1.045.337 1.5h6.326c.216-.455.337-.963.337-1.5V2h-7zm3 6.35c0 .701-.478 1.236-1.011 1.492A3.5 3.5 0 0 0 4.5 13s.866-1.299 3-1.48V8.35zm1 0v3.17c2.134.181 3 1.48 3 1.48a3.5 3.5 0 0 0-1.989-3.158C8.978 9.586 8.5 9.052 8.5 8.351z"/></svg>';
const eventIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"> <path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>';
// note that the title here doesn't matter because it is overwritten with the html
let events = [
                 {
                   id: 'b',
                   title: 'deadlines',
                   start: '2022-09-12T00:00:00',
                   extendedProps: { type: 'deadline', num: 0 }
                 },
                 {
                   id: 'a',
                   title: 'milestones',
                   start: '2022-09-12T00:00:00',
                   extendedProps: { type: 'milestone', num: 5 }
                 },
                 {
                   id: 'c',
                   title: 'events',
                   start: '2022-09-12T00:00:00',
                   extendedProps: { type: 'event', num: 1 }
                 }
             ];

// Will return black or white based on the provided colour string
// so that text shows up
// based on https://www.w3.org/TR/AERT/#color-contrast
// and https://stackoverflow.com/questions/11867545/change-text-color-based-on-brightness-of-the-covered-background-area
// (Alex Ball's answer)
function getTextColour(hexColourString) {
    let r = parseInt(hexColourString.slice(1, 3), 16);
    let g = parseInt(hexColourString.slice(3, 5), 16);
    let b = parseInt(hexColourString.slice(5, 7), 16);

    let brightness = Math.round((r * 299 + g * 587 + b * 114) / 1000);
    return (brightness > 125) ? 'black' : 'white';
}

document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');

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
    let selectedSprint = null;

    // Merges the sprints with events
    let sprintWithEvents = events.sort((a, b) => a.id.localeCompare(b.id)).reverse().concat(sprints);

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
//                selectedSprint.setProp('classNames', 'hidden');

                // allow editing on new selected sprint
                selectedSprint.setProp('durationEditable', true);

                // hides the sprint overlap error message
                document.getElementById("invalidDateRangeError").hidden = true;
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
            // let icon = document.createElement('i')
            switch(arg.event.extendedProps.type) {
                case 'event':
                    const eventContent = eventIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: eventContent }
                    break;
                case 'milestone':
                    const milestoneContent = milestoneIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: milestoneContent }
                    break;
                case 'deadline':
                    const deadlineContent = deadlineIcon + " " + arg.event.extendedProps.num.toString();
                    return { html: deadlineContent }
                    break;
                default:
                    return ;
            }
       },
       // trying to resize just the icons
       // docs: https://fullcalendar.io/docs/classname-input
       // you can add "hidden" to the class string and it will hide the icon without repositioning anything else
       eventClassNames: function(arg) {
            if (['event', 'milestone', 'deadline'].includes(arg.event.extendedProps.type)) {
                return arg.event.extendedProps.num == 0 ? [ 'schedulable-icon hidden' ] : [ 'schedulable-icon' ]
            } else {
                return;
            }
       }
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});
