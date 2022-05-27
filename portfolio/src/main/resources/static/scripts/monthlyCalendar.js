
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
    let sprintNamesList = sprintNames.split(",");
    let sprintStartDatesList = sprintStartDates.split(",");
    let sprintEndDatesList = sprintEndDates.split(",");
    let sprintColoursList = sprintColours.split(",");


    // Creating one list for calendar sprints
    let sprints = [];
    for(let i = 0; i < sprintNamesList.length; i++) {
        sprints.push( {title: sprintNamesList[i], start: sprintStartDatesList[i],
            end: sprintEndDatesList[i], backgroundColor: sprintColoursList[i], textColor: getTextColour(sprintColoursList[i])})
    }


    let calendar = new FullCalendar.Calendar(calendarEl, {
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
        // Used to show all the sprints on the calendar
        events: sprints
    });
    // On startup, calendar date starts from the given project start date
    calendar.gotoDate(projectStartDate);

    calendar.render();

});