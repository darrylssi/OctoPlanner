let stompClient = null;
const eventTimeouts = new Map(); // holds event ids and setTimeout functions in a key/value pair mapping
const EVENT_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing event messages after this many ms

// logging consts to hide certain things while developing
const editingLogs = false;
const updateLogs = true;

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
const schedulableTimeouts = new Map(); // holds schedulable box classes and setTimeout functions in a key/value pair mapping
const SCHEDULABLE_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing schedulable messages after this many ms

/**
 * Sets up a connection to a WebSocket
 * Uses the endpoint registered in WebSocketConfig.java
 */
function connect() {
    const socket = new SockJS(BASE_URL + 'ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = function (){}; // stop log spamming, from https://stackoverflow.com/questions/21767126/stompjs-javascript-client-logging-like-crazy-on-console
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/editing-event', function(message) {
            handleEventMessage(JSON.parse(message.body));
//          TODO  handleEditingSchedulableMessage(JSON.parse(message.body));
        });
        stompClient.subscribe('/topic/events', function(eventMessageOutput) {
            updateEvent(JSON.parse(eventMessageOutput.body));
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
 * Sends a message saying that a user is editing the specified schedulable.
 * Format: `schedulableType,schedulableId,userId'.
 */
function sendEditingSchedulableMessage(schedulableId, schedulableType) {
    console.log("SENDING EDITING MESSAGE FOR SCHEDULABLE: " + schedulableId)
    let user = document.getElementById('user').getAttribute('data-name');
    let userId = document.getElementById('userId').getAttribute('data-name');
    let content = `${schedulableId},${schedulableType},${userId}`;
    stompClient.send("/app/ws", {},
    JSON.stringify({'from':user, 'content':content}));
}

/**
 * Sends a websocket message saying that a user has stopped editing a schedulable.
 * Won't send anything if schedulableId is undefined
 */
function sendStopEditingMessage(schedulableId, schedulableType) {
    if (previousSchedulable !== undefined) {
        console.log('SENDING STOP MESSAGE FOR SCHEDULABLE: ' + schedulableId);
        let user = document.getElementById('user').getAttribute('data-name');
        stompClient.send("/app/ws", {},
        JSON.stringify({'from':user, 'content':`${schedulableId},${schedulableType}`}));
    }
}

/**
 * Decides whether the schedulable message means to show editing or hide editing.
 * @param editMessage JSON object received from the WebSocket
 */
function handleEditingSchedulableMessage(editMessage) {
    messageContent = editMessage.content.split(',');
    if (messageContent.length === 3) {
        console.log('GOT EDITING MESSAGE ' + editMessage.content);
        const schedulableId = messageContent[0];
        const schedulableType = messageContent[1].toLowerCase();
        const userId = messageContent[2];
        showEditingSchedulableMessage(schedulableId, schedulableType, userId);
    } else if (messageContent.length() === 2) {
        console.log('GOT STOP MESSAGE ' + editMessage.content);
        const schedulableId = messageContent[0];
        const schedulableType = messageContent[1].toLowerCase();
        hideEditingSchedulableMessage(schedulableId, schedulableType);
    }
}

/**
 * Shows editing-schedulable notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingSchedulableMessage(schedulableId, schedulableType, userId) {
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId !== docUserId) {
        // stops any existing timeouts so that the message is shown for the full length
        stopSchedulableTimeout(schedulableId);

        // locate the correct elements on the page
        const editingSchedulableBoxClass = `${schedulableType}-${schedulableId}-editing-box`;
        const editingSchedulableTextBoxClass = `${schedulableType}-${schedulableId}-editing-text`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxClass);
        const editingSchedulableTextBoxes = document.getElementsByClassName(editingSchedulableTextBoxClass);

        // update the text and make it visible
        for (const schedulableTextBox of editingSchedulableTextBoxes) {
            if (schedulableTextBox) {
                schedulableTextBox.innerHTML = `${username} is editing this ${schedulableType}`;
            }
        }
        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "visible";
            }
        }

        // Hide it after 8s
        schedulableTimeouts.set(editingSchedulableBoxClass, setTimeout(function() {hideEditingSchedulableMessage(schedulableId, schedulableType)}, SCHEDULABLE_EDIT_MESSAGE_TIMEOUT));
    }
}

/**
 * Hides the editing message for the specified schedulable and clears the timer running for it.
 * Will clear the messages from all schedulable boxes for that schedulable (such as if it spans many sprints).
 * @param message the stop message that was received
 */
function hideEditingSchedulableMessage(schedulableId, schedulableType) {
    // this check is so that if you are editing a schedulable that someone else is editing, you don't hide their message
    // when you close your form. Their message would reappear without this anyway but it avoids confusion.
    if (document.getElementById('user').getAttribute('data-name') !== message.from) {
        const editingSchedulableBoxId = `${schedulableType}-${schedulableId}-editing-box`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxId);

        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "hidden";
            }
        }
        stopEditingSchedulableTimeout(editingSchedulableBoxId);
    }
}


/**
 * Sends a message to a WebSocket endpoint using data from an HTML element
 */
function sendEditingEventMessage(eventId) {
    if (editingLogs) {
        console.log("SENDING EDITING MESSAGE FOR EVENT: " + eventId);
    }
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
        if (editingLogs) {
            console.log('SENDING STOP MESSAGE FOR EVENT: ' + eventId);
        }
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
        if (editingLogs) {
            console.log('GOT EDITING MESSAGE ' + editMessage.content);
        }
        showEditingMessage(editMessage);
    } else {
        if (editingLogs) {
            console.log('GOT STOP MESSAGE ' + editMessage.content);
        }
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

/**
* Updates all instances of an event that has been changed using information sent through websockets
* @param eventMessage the message sent through websockets with event information
*/
function updateEvent(eventMessage) {
    if (updateLogs) {
        console.log("Got update event message for event " + eventMessage.id);
    }
// get a list of event list containers
    const event_lists = document.getElementsByClassName('schedulable-list-container');

// check each event list container to see if it has the event in it / should have the event in it
    for (let eventListContainer of event_lists) {
          //check if event is there, then remove event if it exists
          event = eventListContainer.querySelector('#event-' + eventMessage.id);
          if (event !== null) {
            event.parentNode.parentNode.parentNode.remove();
          }
          // check if event list container is in the list of ids the event should be displayed in
          idIndex = eventMessage.eventListIds.indexOf(eventListContainer.id);
        if(idIndex != -1) {

            const url = BASE_URL + "event-frag/" + eventMessage.id + '/' + eventMessage.eventBoxIds[idIndex];
            const eventFragRequest = new XMLHttpRequest();
            eventFragRequest.open("GET", url, true);
            const tempIdIndex = idIndex;
            eventFragRequest.onload = () => {
                // Reload the page to get the updated list of sprints after the delete
                createEventDisplay(eventMessage, eventListContainer, tempIdIndex, eventFragRequest.response);
            }
            eventFragRequest.send();
        }
    }
}

/**
* Creates a new event display object and puts it into the correct place in the DOM
* @param eventMessage the message sent by websockets containing event info to be displayed
* @param parent the parent object for the event to be displayed in
* @param idIndex the index of this event used to access values in the id lists
* @param eventHtml the html of this event to be inserted into the page
*/
function createEventDisplay(eventMessage, parent, idIndex, eventHtml) {
    let newEvent = document.createElement("div");
    newEvent.innerHTML = eventHtml;
    if(eventMessage.nextEventIds[idIndex] === '-1') {
        parent.appendChild(newEvent);
    } else {
        parent.insertBefore(newEvent, parent.querySelector('#' + eventMessage.nextEventIds[idIndex]).parentNode.parentNode.parentNode);
    }
}