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
        stompClient.subscribe(BASE_URL + 'topic/messages', function(messageOutput) {
            showMessageOutput(JSON.parse(messageOutput.body));
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
    stompClient.send(BASE_URL + "app/ws", {},
    JSON.stringify({'from':user, 'text':" was here"}));
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
        console.log(event);
        if (event !== null) {
        console.log('event found');
            //event is already displayed in this area. values can be updated
            eventName = event.querySelector('#event-name');
            eventName.innerHTML = eventMessage.name;
            eventDates = event.querySelector('#event-date');
            eventDates.innerHTML = eventMessage.startDateString + " - " + eventMessage.endDateString;
            event.setAttribute('data-bs-original-title', eventMessage.description);
        } else {
            //event is not displayed in this area and needs to be created
            //TODO
            console.log('no event');
        }
      } else {
          //event is not in this sprint and needs to be removed if it is on the page
          //check if event is there, then remove event if it exists
          console.log("remove id : " + event_lists[i].id)
          event_lists[i].remove()
      }
    }
// generate/update/delete relevant event instances
}

function testUpdateEvent(){
    eventMessage = {
        sprintIds: ['events-33-inside', 'events-33-outside',  'events-34-inside'],
        name: 'Updated Event',
        startDateString: '01/Aug/2022 00:00',
        endDateString: '04/Aug/2022 00:00',
        id: 1
    }
    console.log(eventMessage);
    updateEvent(eventMessage);
}