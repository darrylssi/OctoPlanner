let stompClient = null;

const schedulableTimeouts = new Map(); // holds schedulable editing box class names and setTimeout functions in a key/value pair mapping
const SCHEDULABLE_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing schedulable messages after this many ms

/**
 * Sets up a connection to a WebSocket
 * Uses the endpoint registered in WebSocketConfig.java
 */
function connect() {
    const socket = new SockJS(BASE_URL + 'ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.debug = function(){}; // stop logging every single message, from https://stackoverflow.com/questions/21767126/stompjs-javascript-client-logging-like-crazy-on-console
        stompClient.subscribe('/topic/editing-event', function(message) {
            handleEditingSchedulableMessage(JSON.parse(message.body));
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
        console.log("Disconnected");
}

/**
 * Sends a message saying that a user is editing the specified schedulable.
 * Format: `schedulableType,schedulableId,userId'.
 */
function sendEditingSchedulableMessage(schedulableId, schedulableType) {
    console.log("SENDING EDITING MESSAGE FOR SCHEDULABLE: " + schedulableId + " " + schedulableType)
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
function sendStopEditingSchedulableMessage(schedulableId, schedulableType) {
    if (previousSchedulable.id !== -1) { // -1 is when there is no previousSchedulable
        console.log('SENDING STOP MESSAGE FOR SCHEDULABLE: ' + schedulableId + " " + schedulableType);
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
        showEditingSchedulableMessage(schedulableId, schedulableType, userId, editMessage.from);
    } else if (messageContent.length == 2) {
        console.log('GOT STOP MESSAGE ' + editMessage.content);
        const schedulableId = messageContent[0];
        const schedulableType = messageContent[1].toLowerCase();
        hideEditingSchedulableMessage(schedulableId, schedulableType, editMessage.from);
    } else {
        console.log('GOT BAD WEBSOCKET MESSAGE ' + editMessage.content);
    }
}

/**
 * Shows editing-schedulable notifications on the page
 * @param editMessage JSON object received from the WebSocket
 */
function showEditingSchedulableMessage(schedulableId, schedulableType, userId, username) {
    const docUserId = document.getElementById("userId").getAttribute('data-name'); // the id of the user on this page

    if (userId !== docUserId) {
        // locate the correct elements on the page
        const editingSchedulableBoxClass = `${schedulableType}-${schedulableId}-editing-box`;
        const editingSchedulableTextBoxClass = `${schedulableType}-${schedulableId}-editing-text`;
        const editingSchedulableBoxes = document.getElementsByClassName(editingSchedulableBoxClass);
        const editingSchedulableTextBoxes = document.getElementsByClassName(editingSchedulableTextBoxClass);

        // stops any existing timeouts so that the message is shown for the full length
        stopEditingSchedulableTimeout(editingSchedulableBoxClass);

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
        schedulableTimeouts.set(editingSchedulableBoxClass, setTimeout(function() {hideEditingSchedulableMessage(schedulableId, schedulableType, username)}, SCHEDULABLE_EDIT_MESSAGE_TIMEOUT));
    }
}

/**
 * Hides the editing message for the specified schedulable and clears the timer running for it.
 * Will clear the messages from all schedulable boxes for that schedulable (such as if it spans many sprints).
 * @param message the stop message that was received
 */
function hideEditingSchedulableMessage(schedulableId, schedulableType, username) {
    // this check is so that if you are editing a schedulable that someone else is editing, you don't hide their message
    // when you close your form. Their message would reappear without this anyway but it avoids confusion.
    if (document.getElementById('user').getAttribute('data-name') !== username) {
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
 * Stops the timeout for the specified schedulable, if it exists
 * @param eventId the event to stop the timeout for
 */
function stopEditingSchedulableTimeout(editingSchedulableBoxClass) {
    if (schedulableTimeouts.has(editingSchedulableBoxClass)) {
        clearTimeout(schedulableTimeouts.get(editingSchedulableBoxClass));
    }
}