let stompClient = null;
const schedulableTimeouts = new Map(); // holds schedulable ids and setTimeout functions in a key/value pair mapping
const SCHEDULABLE_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing schedulable messages after this many ms

// logging consts to hide certain things while developing
const editingLogs = true;
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

/**
 * Sets up a connection to a WebSocket
 * Uses the endpoint registered in WebSocketConfig.java
 */
function connect() {
    const socket = new SockJS(BASE_URL + 'ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = function (){return;}; // stop log spamming, from https://stackoverflow.com/questions/21767126/stompjs-javascript-client-logging-like-crazy-on-console
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/editing-schedulable', function(message) {
            handleSchedulableMessage(JSON.parse(message.body));
        });
        stompClient.subscribe('/topic/schedulable', function(schedulableMessageOutput) {
            updateSchedulable(JSON.parse(schedulableMessageOutput.body));
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
function sendEditingSchedulableMessage(schedulableId, type) {
    if (editingLogs) {
        console.log("SENDING EDITING MESSAGE FOR " + type.toUpperCase() + ": " + schedulableId);
    }
    let user = document.getElementById('user').getAttribute('data-name');
    let userId = document.getElementById('userId').getAttribute('data-name');
    let content = `${schedulableId},${type},${userId}`;
    stompClient.send("/app/ws", {},
    JSON.stringify({'from':user, 'content':content}));
}

/**
 * Sends a websocket message saying that a user has stopped editing an schedulable
 * Won't send anything if schedulableId is undefined
 */
function sendStopEditingMessage(schedulableId, type) {
    if (previousSchedulable !== undefined) {
        if (editingLogs) {
            console.log('SENDING STOP MESSAGE FOR ' + type +': ' + schedulableId);
        }
        let user = document.getElementById('user').getAttribute('data-name');
        stompClient.send("/app/ws", {},
        JSON.stringify({'from':user, 'content':{schedulableId, type}}));
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
 * Decides whether the schedulable message means to show editing or hide editing.
 * @param editMessage JSON object received from the WebSocket
 */
function handleSchedulableMessage(editMessage) {
    if (editMessage.content.split(',').length === 3) {
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
 * Shows editing-schedulable notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingMessage(editMessage) {
    const schedulableId = editMessage.content.split(',')[0]; // couldn't seem to substitute these directly into the template string
    const username = editMessage.from;
    const type = editMessage.content.split(',')[1]; // Type of schedulable
    const userId = editMessage.content.split(',')[2]; // the id of the user editing the schedulable
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId !== docUserId) {
        // stops any existing timeouts so that the message is shown for the full length
        stopSchedulableTimeout(schedulableId);

        // locate the correct elements on the page
        const editingSchedulableBoxClass = `event-${schedulableId}-editing-box`;
        const editingSchedulableTextBoxClass = `event-${schedulableId}-editing-text`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxClass);
        const editingSchedulableTextBoxes = document.getElementsByClassName(editingSchedulableTextBoxClass);

        // update the text and make it visible
        for (const schedulableTextBox of editingSchedulableTextBoxes) {
            if (schedulableTextBox) {
                schedulableTextBox.innerHTML = `${username} is editing this ${type}`;
            }
        }
        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "visible";
            }
        }

        // Hide it after 8s
        schedulableTimeouts.set(schedulableId, setTimeout(function() {hideEditMessage(editMessage)}, SCHEDULABLE_EDIT_MESSAGE_TIMEOUT));
    }
}

/**
 * Hides the editing message for the specified schedulable and clears the timer running for it.
 * Will clear the messages from all schedulable boxes for that schedulable (such as if it spans many sprints).
 * @param message the stop message that was received
 */
function hideEditMessage(message) {
    // this check is so that if you are editing an schedulable that someone else is editing, you don't hide their message
    // when you close your form. Their message would reappear without this anyway but it avoids confusion.
    if (document.getElementById('user').getAttribute('data-name') !== message.from) {
        const schedulableId = (message.content.split(',').length === 2) ? message.content.split(',')[0] : message.content;


        const editingSchedulableBoxId = `event-${schedulableId}-editing-box`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxId);
        for (const schedulableBox of editingSchedulableBoxes) {
            if (schedulableBox) {
                schedulableBox.style.visibility = "hidden";
            }
        }
        stopSchedulableTimeout(schedulableId);
    }
}

/**
 * Stops the timeout for the specified schedulable, if it exists
 * @param schedulableId the schedulable to stop the timeout for
 */
function stopSchedulableTimeout(schedulableId) {
    if (schedulableTimeouts.has(schedulableId)) {
        clearTimeout(schedulableTimeouts.get(schedulableId));
    }
}

/**
* Updates all instances of an schedulable that has been changed using information sent through websockets
* @param schedulableMessage the message sent through websockets with schedulable information
*/
function updateSchedulable(schedulableMessage) {
    if (updateLogs) {
        console.log("Got update schedulable message for schedulable " + schedulableMessage.id);
    }
// get a list of schedulable list containers
    const schedulable_lists = document.getElementsByClassName('schedulable-list-container');

// check each schedulable list container to see if it has the schedulable in it / should have the schedulable in it
    for (let schedulableListContainer of schedulable_lists) {
          //check if schedulable is there, then remove schedulable if it exists
          let schedulable = schedulableListContainer.querySelector('#event-' + schedulableMessage.id);
          if (schedulable !== null) {
            schedulable.parentNode.parentNode.parentNode.remove();
          }
          // check if schedulable list container is in the list of ids the schedulable should be displayed in
          let idIndex = schedulableMessage.schedulableListIds.indexOf(schedulableListContainer.id);
        if(idIndex != -1) {

            const url = BASE_URL + "event-frag/" + schedulableMessage.id + '/' + schedulableMessage.schedulableBoxIds[idIndex];
            const schedulableFragRequest = new XMLHttpRequest();
            schedulableFragRequest.open("GET", url, true);
            const tempIdIndex = idIndex;
            schedulableFragRequest.onload = () => {
                // Reload the page to get the updated list of sprints after the delete
                createSchedulableDisplay(schedulableMessage, schedulableListContainer, tempIdIndex, schedulableFragRequest.response);
            }
            schedulableFragRequest.send();
        }
    }
}

/**
* Creates a new schedulable display object and puts it into the correct place in the DOM
* @param schedulableMessage the message sent by websockets containing schedulable info to be displayed
* @param parent the parent object for the schedulable to be displayed in
* @param idIndex the index of this schedulable used to access values in the id lists
* @param schedulableHtml the html of this schedulable to be inserted into the page
*/
function createSchedulableDisplay(schedulableMessage, parent, idIndex, schedulableHtml) {
    let newSchedulable = document.createElement("div");
    newSchedulable.innerHTML = schedulableHtml;
    if(schedulableMessage.nextSchedulableIds[idIndex] === '-1') {
        parent.appendChild(newSchedulable);
    } else {
        parent.insertBefore(newSchedulable, parent.querySelector('#' + schedulableMessage.nextSchedulableIds[idIndex]).parentNode.parentNode.parentNode);
    }
}