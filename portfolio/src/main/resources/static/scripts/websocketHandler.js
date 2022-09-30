/**
 * This file contains WebSocket methods and variables that are common to all pages that use WebSockets.
 * That means it should deal with sending messages, but probably not receiving them, unless the action to be taken
 * upon receiving the message is the same across all pages.
 *
 * The JS files for each HTML template should contain functions for handling received messages. They can also call the
 * functions in this file for sending messages, and refer to the logging constants in this file.
 *
 * IMPORTANTLY, this file should be imported into HTML pages BEFORE their individual JS files.
 * Additionally, when adding websockets to a page, you'll need import statements for Stomp and SockJS.
 */

let stompClient = null;

// Show or hide console logs from various websocket functions.
const editingLogs = false;
const updateLogs = false;
const sprintLogs = false;
const projectLogs = false;

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
        stompClient.subscribe('/topic/projects', function(projectMessageOutput) {
            handleProjectUpdateMessage(JSON.parse(projectMessageOutput.body));
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
 * @param sprintId the id of the project which has updated
 */
function sendSprintUpdatedMessage(sprintId) {
    if (editingLogs) {
        console.log("SENDING UPDATED SPRINT MESSAGE FOR " + sprintId);
    }
    stompClient.send("/app/sprints", {}, JSON.stringify({'id':`${sprintId}`}));
}

/**
 * Sends a message saying that the specified project was updated.
 * @param projectId the id of the project which has updated
 */
function sendProjectUpdatedMessage(projectId) {
    if (editingLogs) {
        console.log("SENDING UPDATED PROJECT MESSAGE FOR " + projectId);
    }
    stompClient.send("/app/projects", {}, JSON.stringify({'id':`${projectId}`}));
}

/**
 * Sends a message saying that a user is editing the specified schedulable.
 * Format: `schedulableType,schedulableId,userId'.
 * @param schedulableId the id of the schedulable which has updated
 * @param type the type of the schedulable which has updated
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


