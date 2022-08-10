let stompClient = null;
const eventTimeouts = new Map(); // holds event ids and setTimeout functions in a key/value pair mapping
const EVENT_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing event messages after this many ms

/**
 * Sets html elements according to whether a WebSocket has been connected to or not
 * @param connected Boolean if a connection has been established to a WebSocket
 */
function setConnected(connected) {
    // Currently, does not contain any elements that need to be set according to the connected status
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
        stompClient.subscribe('/topic/editing-event', function(message) {
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
function sendEditingEventMessage(eventId) {
    console.log("SENDING EDITING MESSAGE FOR EVENT: " + eventId)
    let user = document.getElementById('user').getAttribute('data-name');
    let userId = document.getElementById('userId').getAttribute('data-name');
    let content = `${eventId},${userId}`;
    stompClient.send("/app/ws", {},
    JSON.stringify({'from':user, 'content':content}));
}

/**
 * Sends a websocket message saying that a user has stopped editing an event
 * Won't send anything if eventId is undefined
 */
function sendStopEditingMessage(eventId) {
    if (previousEvent !== undefined) {
        console.log('SENDING STOP MESSAGE FOR EVENT: ' + eventId);
        let user = document.getElementById('user').getAttribute('data-name');
        stompClient.send("/app/ws", {},
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

/**
 * Decides whether the event message means to show editing or hide editing.
 * @param editMessage JSON object received from the WebSocket
 */
function handleEventMessage(editMessage) {
    if (editMessage.content.split(',').length === 2) {
        console.log('GOT EDITING MESSAGE ' + editMessage.content);
        showEditingMessage(editMessage);
    } else {
        console.log('GOT STOP MESSAGE ' + editMessage.content);
        hideEditMessage(editMessage);
    }
}

/**
 * Shows editing-event notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingMessage(editMessage) {
    const eventId = editMessage.content.split(',')[0]; // couldn't seem to substitute these directly into the template string
    const username = editMessage.from;
    const userId = editMessage.content.split(',')[1]; // the id of the user editing the event
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId !== docUserId) {
        // stops any existing timeouts so that the message is shown for the full length
        stopEventTimeout(eventId);

        // locate the correct elements on the page
        const editingEventBoxClass = `event-${eventId}-editing-box`;
        const editingEventTextBoxClass = `event-${eventId}-editing-text`;
        const editingEventBoxes = document.getElementsByClassName(editingEventBoxClass);
        const editingEventTextBoxes = document.getElementsByClassName(editingEventTextBoxClass);

        // update the text and make it visible
        for (const eventTextBox of editingEventTextBoxes) {
            if (eventTextBox) {
                eventTextBox.innerHTML = `${username} is editing this event`;
            }
        }
        for (const eventBox of editingEventBoxes) {
            if (eventBox) {
                eventBox.style.visibility = "visible";
            }
        }

        // Hide it after 8s
        eventTimeouts.set(eventId, setTimeout(function() {hideEditMessage(editMessage)}, EVENT_EDIT_MESSAGE_TIMEOUT));
    }
}

/**
 * Hides the editing message for the specified event and clears the timer running for it.
 * Will clear the messages from all event boxes for that event (such as if it spans many sprints).
 * @param message the stop message that was received
 */
function hideEditMessage(message) {
    // this check is so that if you are editing an event that someone else is editing, you don't hide their message
    // when you close your form. Their message would reappear without this anyway but it avoids confusion.
    if (document.getElementById('user').getAttribute('data-name') !== message.from) {
        const eventId = (message.content.split(',').length === 2) ? message.content.split(',')[0] : message.content;


        const editingEventBoxId = `event-${eventId}-editing-box`;
        const editingEventBoxes = document.getElementsByClassName(editingEventBoxId);
        for (const eventBox of editingEventBoxes) {
            if (eventBox) {
                eventBox.style.visibility = "hidden";
            }
        }
        stopEventTimeout(eventId);
    }
}

/**
 * Stops the timeout for the specified event, if it exists
 * @param eventId the event to stop the timeout for
 */
function stopEventTimeout(eventId) {
    if (eventTimeouts.has(eventId)) {
        clearTimeout(eventTimeouts.get(eventId));
    }
}