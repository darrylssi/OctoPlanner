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
        stompClient.subscribe(BASE_URL + 'topic/editing-event', function(message) {
            showEditMessage(JSON.parse(message.body));
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
 * Shows editing-event notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditMessage(editMessage) {
    const eventId = editMessage.content.split(',')[0]; // couldn't seem to substitute these directly into the template string
    const username = editMessage.from;
    const userId = editMessage.content.split(',')[1]; // TODO make a better message template or something
    const docUserId = document.getElementById("userId").getAttribute('data-name');

    console.log(eventId);
    console.log(username);
    console.log(userId);
    console.log(docUserId);

    if (userId != docUserId) {
        const response = document.getElementById('response');
        const p = document.createElement('p');
        p.style.wordWrap = 'break-word';
        p.appendChild(document.createTextNode(editMessage.from +
            " is editing event \"" + editMessage.content + "\""));
        response.appendChild(p);

        /* Shows a message that the given user is editing the event with the given id, and shows the spinner*/
        // locate the correct editing box
        const editingEventBoxId = `event-${eventId}-editing-box`;
        const editingEventTextBoxId = `event-${eventId}-editing-text`;

        const editingEventBox = document.getElementById(editingEventBoxId);
        const editingEventTextBox = document.getElementById(editingEventTextBoxId);

        // update the text and make it visible
        editingEventTextBox.innerHTML = `${username} is editing this event`;
        editingEventBox.style.visibility = "visible";
    }
}