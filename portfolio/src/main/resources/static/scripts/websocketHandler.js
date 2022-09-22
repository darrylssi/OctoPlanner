let stompClient = null;
const schedulableTimeouts = new Map(); // holds schedulable ids and setTimeout functions in a key/value pair mapping
const SCHEDULABLE_EDIT_MESSAGE_TIMEOUT = 4000; // hide editing schedulable messages after this many ms

// logging consts to hide certain things while developing
const editingLogs = false;
const updateLogs = false;

/**
 * Sets up a connection to a WebSocket
 * Uses the endpoint registered in WebSocketConfig.java
 */
function connect() {
    const socket = new SockJS(BASE_URL + 'ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = function (){return;}; // stop log spamming, from https://stackoverflow.com/questions/21767126/stompjs-javascript-client-logging-like-crazy-on-console
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/editing-schedulable', function(message) {
            handleSchedulableMessage(JSON.parse(message.body));
        });
        stompClient.subscribe('/topic/schedulables', function(schedulableMessageOutput) {
            updateSchedulable(JSON.parse(schedulableMessageOutput.body));
        });
        stompClient.subscribe('/topic/sprints', function(sprintMessageOutput) {
            handleSprintUpdateMessage(JSON.parse(sprintMessageOutput.body));
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
 * Sends a message saying that the specified sprint was updated.
 */
function sendSprintUpdatedMessage(sprintId) {
    if (editingLogs) {
        console.log("SENDING UPDATED SPRINT MESSAGE FOR " + sprintId);
    }
    stompClient.send("/app/sprints", {}, JSON.stringify({'id':`${sprintId}`}));
}

/**
 * Sends a message saying that a user is editing the specified schedulable.
 * Format: `schedulableType,schedulableId,userId'.
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
 * Sends a websocket message saying that a user has stopped editing a schedulable.
 * Won't send anything if schedulableId is undefined
 */
function sendStopEditingMessage(schedulableId, type) {
    if (currentSchedulable !== undefined) {
        if (editingLogs) {
            console.log('SENDING STOP MESSAGE FOR ' + type +': ' + schedulableId);
        }
        let user = document.getElementById('user').getAttribute('data-name');
        let content = `${schedulableId},${type}`
        stompClient.send("/app/ws", {},
        JSON.stringify({'from':user, 'content':content}));
    }
}


