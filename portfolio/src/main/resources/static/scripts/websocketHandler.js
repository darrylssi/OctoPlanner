let stompClient = null;

/**
 * Sets html elements according to whether a WebSocket has been connected to or not
 * @param connected Boolean if a connection has been established to a WebSocket
 */
function setConnected(connected) {
    // Kept these lines in as they may be useful in future for connecting/disconnecting in a better way
    // TODO check these lines before merging to master!

    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    //document.getElementById('conversationDiv').style.visibility
    //    = connected ? 'visible' : 'hidden';

    let response = document.getElementById('response');
    if (response !== null ) {
        response.innerHTML = '';
    }
}

/**
 * Sets up a connection to a WebSocket
 * Uses the endpoint registered in WebSocketConfig.java
 */
function connect() {
    const socket = new SockJS(BASE_URL + 'ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            showMessageOutput(JSON.parse(messageOutput.body));
        });
        stompClient.subscribe('/topic/events', function(eventMessageOutput) {
            showEventMessageData(JSON.parse(eventMessageOutput.body));
        });
    });
}

/**
 * Disconnects from a WebSocket
 */
function disconnect() {
    if(stompClient != null) {
        stompClient.disconnect();
    }
        setConnected(false);
        console.log("Disconnected");
}

/**
 * Sends a message to a WebSocket endpoint using data from an HTML element
 */
function sendMessage() {
    let user = document.getElementById('user').getAttribute('data-name');
    stompClient.send("/app/ws", {},
    JSON.stringify({'from':user, 'text':" was here"}));
    sendUpdateEventData();
}

/**
 * Updates an HTML element to display a received WebSocket message
 * @param messageOutput JSON object received from the WebSocket
 */
function showMessageOutput(messageOutput) {

    const response = document.getElementById('response');
    const p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(messageOutput.from +
        messageOutput.text + " (" + messageOutput.time + ")"));
    response.appendChild(p);
}

function updateEvent(eventMessage) {
// get a list of event list containers
    const event_lists = document.getElementsByClassName('event-list-container');

// check each event list container to see if it has the event in it / should have the event in it
    for (let i = 0; i < event_lists.length; i++) {
        console.log("event list at id: " + typeof event_lists[i].id + " " + event_lists[i].id);

        if(eventMessage.sprintIds.includes(event_lists[i].id)) {
        //event is in this sprint and needs to be added or updated
        event = event_lists[i].querySelector('#event-' + eventMessage.id);
        if (event !== null) {
            //event is already displayed in this area. values can be updated
            eventName = event.querySelector('#event-name');
            eventName.innerHTML = eventMessage.name;
            eventDates = event.querySelector('#event-date');
            eventDates.innerHTML = eventMessage.startDateString + " - " + eventMessage.endDateString;
            event.setAttribute('data-bs-original-title', eventMessage.description);
        } else {
            //event is not displayed in this area and needs to be created
            createEventDisplay(eventMessage, event_lists[i]);
        }
      } else {
          //event is not in this sprint and needs to be removed if it is on the page
          //check if event is there, then remove event if it exists
          event = event_lists[i].querySelector('#event-box-' + eventMessage.id);
          if (event !== null) {
            event.remove();
          }
      }
    }
// generate/update/delete relevant event instances
}

function testUpdateEvent() {
    eventMessage = {
        sprintIds: ['events-37-inside', 'events-37-outside',  'events-39-inside', 'events-39-outside'],
        name: 'Updated Event',
        startDateString: '12/Jan/2022 00:00',
        endDateString: '26/Jan/2022 00:00',
        description: 'this event has been updated',
        startColour: "#2c2c2c2c",
        endColour: '#ff00ff4c',
        id: 1
    }
    console.log(eventMessage);
    updateEvent(eventMessage);
}

/**
 * Sends a data message to a WebSocket endpoint using attributes from the HTML elements
 */
function sendUpdateEventData() {
    eventId = 1;

    // Gets all the sprint ids that exist in the project details page
    allSprintIds = document.getElementsByClassName('event-list-container');
    sprintIdsForEvent = [];         // sprints ids for the given specific event
    for (let i = 0; i < allSprintIds.length; i++) {
        event = allSprintIds[i].querySelector('#event-' + eventId);
        // checks if the event exist in this current sprint container. If it exists, then it is added to the sprints ids list
        if (event !== null) {
            sprintIdsForEvent.push(allSprintIds[i]);
        }
    }

    //
    eventMessage = {
//        sprintIds: sprintIdsForEvent,
//        name: document.getElementById('event-name'),
//        startDateString: document.getElementById('event-date').innerText.substring(0, 17),
//        endDateString: document.getElementById('event-date').innerText.substring(20),
//        description: document.getElementById('event-description').innerText,
//        color: '#9966ff2c',
        id: eventId
    }
    stompClient.send("/app/events", {}, JSON.stringify(eventMessage));
}

/**
 * Updates an HTML element to display a received WebSocket message
 * @param eventMessage JSON object received from the WebSocket
 */
function showEventMessageData(eventMessage) {

    const eventResponse = document.getElementById('eventResponse');
    const p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(eventMessage.id + eventMessage.sprintIds + " " + eventMessage.name + " "
        + eventMessage.startDate + " - " + eventMessage.endDate));
    eventResponse.appendChild(p);
}

function createEventDisplay(eventMessage, parent) {
    console.log('creating event');
    let newEvent = document.createElement("div");
    newEvent.setAttribute("id", "event-" + eventMessage.id);
    newEvent.setAttribute("class", "event-box");
    newEvent.setAttribute("style", "background:linear-gradient(to right, " + eventMessage.startColour + ', ' + eventMessage.endColour);
    newEvent.innerHTML = eventTemplate;
    parent.appendChild(newEvent);
    newEvent.getElementsByClassName("event")[0].setAttribute('data-bs-original-title', eventMessage.description);
    newEvent.querySelector("#event-name").innerHTML = eventMessage.name;
    newEvent.querySelector("#event-date").innerHTML = eventMessage.startDateString + " - " + eventMessage.endDateString;
}