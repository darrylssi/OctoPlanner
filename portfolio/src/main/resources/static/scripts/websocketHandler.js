let stompClient = null;

/**
 * Sets html elements according to whether a WebSocket has been connected to or not
 * @param connected Boolean if a connection has been established to a WebSocket
 */
function setConnected(connected) {
    // Kept these lines in as they may be useful in future for connecting/disconnecting in a better way
    // TODO check these lines before merging to master!

//    document.getElementById('connect').disabled = connected;
//    document.getElementById('disconnect').disabled = !connected;
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
            updateEvent(JSON.parse(eventMessageOutput.body));
        });
        sendEventUpdates();
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

/**
* Updates all instances of an event that has been changed using information sent through websockets
* @param eventMessage the message sent through websockets with event information
*/
function updateEvent(eventMessage) {
// get a list of event list containers
    const event_lists = document.getElementsByClassName('event-list-container');

// check each event list container to see if it has the event in it / should have the event in it
    for (let i = 0; i < event_lists.length; i++) {
          //check if event is there, then remove event if it exists
          event = event_lists[i].querySelector('#event-box-' + eventMessage.id);
          if (event !== null) {
            event.remove();
          }
          // check if event list container is in the list of ids the event should be displayed in
          idIndex = eventMessage.sprintIds.indexOf(event_lists[i].id);
        if(idIndex != -1) {
            createEventDisplay(eventMessage, event_lists[i], eventMessage.eventIds[idIndex]);
        }
    }
}

/**
* Creates a new event display object and puts it into the correct place in the DOM
* @param eventMessage the message sent by websockets containing event info to be displayed
* @param parent the parent object for the event to be displayed in
* @param nextEvent the id of the event that the new event should be inserted before. -1 if no following event
*/
function createEventDisplay(eventMessage, parent, nextEvent) {
    console.log('creating event');
    let newEvent = document.createElement("div");
    newEvent.setAttribute("id", "event-box-" + eventMessage.id);
    newEvent.setAttribute("class", "event-box");
    newEvent.setAttribute("style", "background:linear-gradient(to right, " + eventMessage.startColour + ', ' + eventMessage.endColour);
    newEvent.innerHTML = eventTemplate;
    if(nextEvent === '-1') {
        parent.appendChild(newEvent);
    } else {
        parent.insertBefore(newEvent, parent.querySelector("#" + nextEvent));
    }
    newEvent.getElementsByClassName("event")[0].title = eventMessage.description;
    newEvent.getElementsByClassName("event")[0].data-toggle = "tooltip";
    newEvent.getElementsByClassName("event")[0].data-placement = "top";

    newEvent.querySelector("#event-name").innerHTML = eventMessage.name;
    newEvent.querySelector("#event-date").innerHTML = eventMessage.startDate + " - " + eventMessage.endDate;

    if (canEdit == false) {
        newEvent.querySelector('.event-right').style.visibility = 'hidden';
    }

}

/**
* Checks if there is an event that has been updated and sends a websocket message if there is
*/
function sendEventUpdates() {
    console.log('sending event update for ' + eventId);
    if (eventId !== -1){
        stompClient.send("/app/events", {}, JSON.stringify({id: eventId}));
    }
}