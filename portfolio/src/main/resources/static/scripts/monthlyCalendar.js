

document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');


    // Converting the given sprint's string names, start date and end date to list
    let sprintNamesList = sprintNames.split(",");
    let sprintStartDatesList = sprintStartDates.split(",");
    let sprintEndDatesList = sprintEndDates.split(",");

    // Creating one list for calendar sprints
    let sprints = [];
    for(let i = 0; i < sprintNamesList.length; i++) {
        sprints.push( {title: sprintNamesList[i], start: sprintStartDatesList[i],
            end: sprintEndDatesList[i]} )
    };


    let calendar = new FullCalendar.Calendar(calendarEl, {
        timeZone: 'UTC',
        initialView: 'dayGridMonth',
        // Restricts the calendar dates based on the given project dates
        validRange: {
            start: projectStartDate,
            end: projectEndDate
        },
        // Used to show all the sprints on the calendar
        events: sprints
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});