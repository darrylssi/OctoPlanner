

document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');

    // Converting the given sprint's string names, start date and end date to list
    let sprintIdsList = sprintIds.split(",");
    let sprintNamesList = sprintNames.split(",");
    let sprintStartDatesList = sprintStartDates.split(",");
    let sprintEndDatesList = sprintEndDates.split(",");

    // Creating one list for calendar sprints
    let sprints = [];
    let colours = ["cornflowerblue", "firebrick", "forestgreen", "blueviolet", "tomato", "darkslategrey", "darkorchid"];
    for(let i = 0; i < sprintNamesList.length; i++) {
        sprints.push( {id: sprintIdsList[i], title: sprintNamesList[i], start: sprintStartDatesList[i],
            end: sprintEndDatesList[i], backgroundColor: colours[i % colours.length]} )
    }
    let selectedSprint = null;



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
                document.getElementById("sprintStartDate").value = info.event.start;
                document.getElementById("sprintEndDate").value = info.event.end;

                // submitting the form
                form.submit();
            }
        },
        // Used to show all the sprints on the calendar
        events: sprints
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});
