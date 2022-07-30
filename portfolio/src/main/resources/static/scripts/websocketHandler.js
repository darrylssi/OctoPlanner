var stompClient = null;

function setConnected(connected) {
    //document.getElementById('connect').disabled = connected;
    //document.getElementById('disconnect').disabled = !connected;
    //document.getElementById('conversationDiv').style.visibility
    //    = connected ? 'visible' : 'hidden';
    let response = document.getElementById('response');
    if (response !== null ) {
        response.innerHTML = '';
    }
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            showMessageOutput(JSON.parse(messageOutput.body));
        });
    });
}

function disconnect() {
    if(stompClient != null) {
        stompClient.disconnect();
    }
        setConnected(false);
        console.log("Disconnected");
}

function sendMessage() {
    let user = document.getElementById('user').getAttribute('data-name');
    stompClient.send("/app/ws", {},
    JSON.stringify({'from':user, 'text':" was here"}));
}

function showMessageOutput(messageOutput) {
    var response = document.getElementById('response');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(messageOutput.from +
        messageOutput.text + " (" + messageOutput.time + ")"));
    response.appendChild(p);
}