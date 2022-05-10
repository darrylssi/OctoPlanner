
// Sprint min date so that sprint can't end before start date
function setNewMin() {
    var newStart = document.getElementById('projectStartDate').value;
    document.getElementById('projectEndDate').setAttribute('min', newStart);
}

// Set max date so that sprint can't start after end date
function setNewMax() {
    var newEnd = document.getElementById('projectEndDate').value;
    document.getElementById('projectStartDate').setAttribute('max', newEnd);
}

// Set project min date to last year
// A teacher can start a project up to a year ago (UPi AC4)
var today = new Date();
var dd = today.getDate();
var mm = today.getMonth() + 1;
var yyyy = today.getFullYear()-1;
if (dd < 10) {
    dd = '0' + dd;
}
if (mm < 10) {
    mm = '0' + mm;
}
today = yyyy + '-' + mm + '-' + dd;
document.getElementById('projectStartDate').setAttribute('min', today);
