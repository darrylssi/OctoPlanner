

document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');


    // Converting the given sprint's string names, start date and end date to list
    let sprintNamesList = sprintNames.split(",");
    let sprintStartDatesList = sprintStartDates.split(",");
    let sprintEndDatesList = sprintEndDates.split(",");

    // Creating one list for calendar sprints
    let sprints = [];
    let colours = ["cornflowerblue", "firebrick", "forestgreen", "blueviolet", "tomato", "darkslategrey", "darkorchid"];
    for(let i = 0; i < sprintNamesList.length; i++) {
        sprints.push( {title: sprintNamesList[i], start: sprintStartDatesList[i],
            end: sprintEndDatesList[i], backgroundColor: colours[i % colours.length]} )
    }
    let selectedSprint = null;


    let calendar = new FullCalendar.Calendar(calendarEl, {
        eventResizableFromStart:true, // when resizing sprints, can be done from start as well as end
        eventDurationEditable: false, // sprints can't be edited by default
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
        eventClick: function(info) {
            // if the user clicks on a sprint, update the selected sprint
            if (selectedSprint != null) {
                // remove editing from current sprint
                selectedSprint.setProp('durationEditable', false);
            }
            selectedSprint = info.event;
            // allow editing on new selected sprint
            selectedSprint.setProp('durationEditable', true);
        },
        // Used to show all the sprints on the calendar
        events: sprints
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});