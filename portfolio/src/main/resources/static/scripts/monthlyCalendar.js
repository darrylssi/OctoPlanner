


document.addEventListener('DOMContentLoaded', function() {
    let calendarEl = document.getElementById('calendar');

    let calendar = new FullCalendar.Calendar(calendarEl, {
        timeZone: 'UTC',
        defaultView: 'dayGridMonth'
    });

    calendar.render();
});