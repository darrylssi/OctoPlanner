
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
            end: sprintEndDatesList[i], backgroundColor: sprintColoursList[i], textColor: getTextColour(sprintColoursList[i])})
    }
    let selectedSprint = null;

    let mouseoverSprint = false;
    // de-select sprint when mouse is clicked if mouse not over a sprint
    document.addEventListener('mousedown', () => {
        if(!mouseoverSprint){
            if (selectedSprint != null) {
                // de-select sprint
                selectedSprint.setProp('durationEditable', false);
                selectedSprint = null;
            }
        }
    })


    let calendar = new FullCalendar.Calendar(calendarEl, {
        eventResizableFromStart: (sprintsEditable === "true"), // when resizing sprints, can be done from start as well as end
        eventDurationEditable: false,                       // sprints can't be edited by default
        timeZone: 'UTC',
        initialView: 'dayGridMonth',
        // Restricts the calendar dates based on the given project dates
        validRange: {
            start: projectStartDate,
            end: projectEndDate
        },
        buttonText: {
            today: "Today"
        },
        eventOverlap: function (stillEvent, movingEvent) {
            // shows the sprint overlap error message
            document.getElementById("invalidDateRangeError").hidden = false;

            // sets the sprint overlap error message based on the selected sprint
            document.getElementById("invalidDateRangeError").innerText = "Error: " + movingEvent.title.toString()
            + " must not overlap with " + stillEvent.title.toString();
        },
        eventClick: function(info) {
            if (sprintsEditable === "true") {
                // if the user clicks on a sprint, update the selected sprint
                if (selectedSprint != null) {
                    // remove editing from current sprint
                    selectedSprint.setProp('durationEditable', false);
                }
                selectedSprint = info.event;

                // allow editing on new selected sprint
                selectedSprint.setProp('durationEditable', true);

                // hides the sprint overlap error message
                document.getElementById("invalidDateRangeError").hidden = true;
            }

        },
        eventResize: function(info) {
            // if the user clicks on a sprint, update the selected sprint
            if (sprintsEditable === "true") {
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
        events: sprints
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});
