let stompClient = null;
const EVENT_EDIT_MESSAGE_TIMEOUT = 8000; // timeout period for event editing messages in ms
//const EVENT_EDIT_MESSAGE_FREQUENCY = 3000; // how often editing messages are sent while someone is editing an event

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
        stompClient.subscribe(BASE_URL + 'topic/editing-event', function(message) {
            handleEventMessage(JSON.parse(message.body));
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
}

/**
 * Sends a message to a WebSocket endpoint using data from an HTML element
 */
function sendEditingEventMessage(eventId) {
    let user = document.getElementById('user').getAttribute('data-name');
    let userId = document.getElementById('userId').getAttribute('data-name');
    let content = `${eventId},${userId}`;
    stompClient.send(BASE_URL + "app/ws/editing-event", {},
    JSON.stringify({'from':user, 'content':content}));
}

/**
 * Sends a websocket message saying that a user has stopped editing an event
 * Won't send anything if eventId is undefined
 */
function sendStopEditingMessage(eventId) {
    if (previousEvent != undefined) {
        console.log('sending stop message');
        let user = document.getElementById('user').getAttribute('data-name');
        stompClient.send(BASE_URL + "app/ws/editing-event", {},
        JSON.stringify({'from':user, 'content':eventId}));
    }
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

// holds event ids and setTimeout functions in a key/value pair mapping
const eventTimeouts = new Map();

/**
 * Decides whether the event message means to show editing or hide editing.
 * @param editMessage JSON object received from the WebSocket
 */
function handleEventMessage(editMessage) {
    if (editMessage.content.split(',').length == 2) {
        console.log('got show message ' + editMessage.content);
        showEditingMessage(editMessage);
    } else {
        console.log('got stop message ' + editMessage.content);
        hideEditMessage(editMessage.content);
    }
}

/**
 * Shows editing-event notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingMessage(editMessage) { // TODO make a better message template or something
    const eventId = editMessage.content.split(',')[0]; // couldn't seem to substitute these directly into the template string
    const username = editMessage.from;
    const userId = editMessage.content.split(',')[1]; // the id of the user editing the event
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId != docUserId) {
    // TODO kept for testing purposes
//        const response = document.getElementById('response');
//        const p = document.createElement('p');
//        p.style.wordWrap = 'break-word';
//        p.appendChild(document.createTextNode(editMessage.from +
//            " is editing event \"" + editMessage.content + "\""));
//        response.appendChild(p);

        /* Shows a message that the given user is editing the event with the given id, and shows the spinner*/

        // stops any existing timeouts so that the message is shown for the full length
        stopEventTimeout(eventId);

        // locate the correct editing box
        const editingEventBoxId = `event-${eventId}-editing-box`;
        const editingEventTextBoxId = `event-${eventId}-editing-text`;

        const editingEventBox = document.getElementById(editingEventBoxId);
        const editingEventTextBox = document.getElementById(editingEventTextBoxId);

        // update the text and make it visible
        editingEventTextBox.innerHTML = `${username} is editing this event`;
        editingEventBox.style.visibility = "visible";

        // Hide it after 8s
         eventTimeouts.set(eventId, setTimeout(function() {hideEditMessage(eventId)}, 8000))
    }
}

/* Hides the editing message for the specified event and clears the timer running for it */
function hideEditMessage(eventId) {
    const editingEventBoxId = `event-${eventId}-editing-box`;
    const editingEventBox = document.getElementById(editingEventBoxId);
    editingEventBox.style.visibility = "hidden";
    stopEventTimeout(eventId);
}

/* Stops the timeout for the specified event, if it exists */
function stopEventTimeout(eventId) {
    if (eventTimeouts.has(eventId)) {
        clearTimeout(eventTimeouts.get(eventId));
    }
}

// TODO ideas pad
// will need to regularly send editing messages when a user has the edit form open, and stop doing that when they close it
// also only need one regularly sent edit message, as only one event can be edited at a time

// will also need to send a STOP message when the edit thing is closed, or when one is opened
// stop message will need to include just the event id, and can then hide the editing thingy for that event

// need to store key (eventid) value (timeout) pairs in a map, with one for each event that is being edited
// no need to delete things from here, I think?
// when receive an editing message: reset the timeout
// when receive a stop message: stop the timeout and directly hide the event