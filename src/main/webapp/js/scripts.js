'use strict';

var username;
var selectedRow = null;
var isEditing = false;

var createMessage = function(text) {
    return {
        username: username,
        text: text,
        time: getCurrentTime(),
        edited: false,
        deleted: false,
        id: ""
    };
};

var createNames = function(username) {
    return {
        oldName: username,
        newName: ""
    };
};

var appState = {
    mainUrl : 'messages',
    messages: {},
    token : '0'
};

function run() {
    delegateEvents();
    restoreMessages();

    onResizeDocument();
    setIconsVisible(false);
    var table = document.getElementsByClassName('table')[0];
    table.scrollTop = table.scrollHeight;

    $('#icon-edit').tooltip();
    $('#icon-remove').tooltip();
    $('#name').tooltip();
    $('#messages-number').tooltip();
    $('#message-input').popover({ delay: { "show": 1500 } });
    $('#input-name').popover();
}

function delegateEvents() {
    var nameInput = document.getElementById('input-name');
    nameInput.addEventListener('focusout', onNameInput);
    nameInput.value = username = restoreUsername() || "";

    document.getElementById('add-button').addEventListener('click', onAddButtonClick);
    document.getElementById('message-text').addEventListener('keydown', onTextInput);

    var icons = document.getElementsByClassName('icon');
    icons[0].addEventListener('click', onEditClick);
    icons[1].addEventListener('click', onRemoveClick);
}

function restoreUsername() {
    if(typeof(Storage) == "undefined") {
        alert('Local Storage is not accessible');
        return;
    }

    var name = localStorage.getItem("Chat Username");
    return name && JSON.parse(name);
}

function restoreMessages() {
    var url = appState.mainUrl + '?token=' + appState.token;

    get(url, function(responseText) {
        console.assert(responseText != null);

        var response = JSON.parse(responseText);
        appState.token = response.token;
        createAllMessages(response.messages);
        updateCounter();
        onConnectionSet();

        setTimeout(restoreMessages, 1000);
    });
}

function sendMessage(message, continueWith) {
    post(appState.mainUrl, JSON.stringify(message), function(){
        onConnectionSet();
        continueWith && continueWith();
    });
}

function addMessageToHTML(message) {
    var table = document.getElementsByClassName('table')[0];
    var bottomScroll = isScrollBottom(table);
    var row = table.insertRow(-1);

    createItem(row, message);
    updateItem(row, message);
    appState.messages[message.id] = (message);

    if(bottomScroll)
        table.scrollTop = table.scrollHeight;
}

function createItem(row, message) {
    row.innerHTML = '<td class="col-time"></td><td class="col-message">' +
        '<div class="list-group-item"><h4 class="list-group-item-heading"></h4>' +
        '<div class="wrap"><p class="list-group-item-text"></p></div></div></td>';
    row.setAttribute('id', message.id);
    row.classList.add('item');
    row.addEventListener('click', onMessageClick);

    row.lastChild.firstChild.firstChild.innerText = message.username;
    row.lastChild.firstChild.firstChild.innerHTML = message.username;
    row.firstChild.innerHTML = message.time;
}

function updateItem(row, message) {
    row.lastChild.firstChild.lastChild.firstChild.innerText = message.text;
    row.lastChild.firstChild.lastChild.firstChild.innerHTML = message.text;

    row.lastChild.firstChild.firstChild.innerText = message.username;
    row.lastChild.firstChild.firstChild.innerHTML = message.username;

    if(message.deleted) {
        row.firstChild.innerHTML =  message.time + '<br>' + '<i class="glyphicon glyphicon-trash"></i>';
        row.classList.add('deleted-message');
    } else if(message.edited) {
        row.firstChild.innerHTML = message.time + '<br>' + '<i class="glyphicon glyphicon-pencil"></i>';
    }
}

function onResizeDocument(e) {
    var all = document.getElementsByTagName('html')[0].clientHeight;
    var navbar = document.getElementsByClassName('navbar')[0].clientHeight;
    var input = document.getElementById('message-input').clientHeight;
    var height = all - navbar - input - 50;
    height = height.toString() + 'px';
    document.getElementsByClassName('table')[0].style.height = height;
}

function onTextInput(event) {
    if ((isEditing == true) && (event.currentTarget.value.length == 0)){
        isEditing = false;
        selectedRow = null;
    } else if (event.keyCode == 13) { // ENTER was pressed
        event.preventDefault();

        if (event.shiftKey) {
            var textarea = document.getElementById('message-text');
            var caretPos = getCaretPosition(textarea);
            textarea.value = textarea.value.slice(0, caretPos) + '\n' + textarea.value.slice(caretPos);
            setCaretPosition(textarea, caretPos + 1);
        }
        else {
            onAddButtonClick();
        }
    }
}

function onAddButtonClick(e) {
    var textarea = document.getElementById('message-text');

    if (isEditing) {
        var id = selectedRow.getAttribute('id');
        if (id in appState.messages)
            editMessage(appState.messages[id], textarea);
    } else if (username.length === 0) { // Empty username
        $('#input-name').popover('show');
        document.getElementById('input-name').focus();
    } else if(!/\S/.test(textarea.value)) { // Empty message
        textarea.value = '';
    } else {
        var createdMessage = createMessage(textarea.value);
        sendMessage(createdMessage, function () {
            textarea.value = '';
        });
    }
}

function onNameInput(e) {
    var name = document.getElementById('input-name');
    if(!/\S/.test(name.value)) {
        name.value = '';
        username = '';
        storeUsername();
        $('#input-name').popover('show');
    } else {
        var usernames = createNames(username);

        username = name.value;
        usernames.newName = username;
        storeUsername();
        $('#input-name').popover('hide');

        post(appState.mainUrl + "?username=true", JSON.stringify(usernames), function(){
            onConnectionSet();
        });
    }
}

function onMessageClick(event) {
    if (appState.messages[event.currentTarget.id].username != username)
        return;

    var row = document.getElementById(event.currentTarget.id);
    if (row.classList.contains('deleted-message'))
        return;

    if (selectedRow == row) {
        setMessageActive(row, false);
        selectedRow = null;
        setIconsVisible(false);
    } else {
        if (selectedRow != null) {
            setMessageActive(selectedRow, false);
        }

        setMessageActive(row, true);
        selectedRow = row;
        setIconsVisible(true);
    }
}

function setMessageActive(row, active) {
    if (active) {
        row.classList.add('info');
        row.lastChild.firstChild.classList.add('active');
    } else {
        row.classList.remove('info');
        row.lastChild.firstChild.classList.remove('active');
    }
}

function onEditClick() {
    if(selectedRow == null)
        return;

    var text = selectedRow.getElementsByClassName('list-group-item-text')[0];
    var input = document.getElementById('message-text');
    input.value = text.innerText;
    input.focus();

    isEditing = true;
    setIconsVisible(false);

    selectedRow.classList.remove('info');
    var selectedMessage = selectedRow.getElementsByClassName('list-group-item')[0];
    selectedMessage.classList.remove('active');
}

function onRemoveClick() {
    if(selectedRow == null)
        return;

    var id = selectedRow.getAttribute('id');
    if (id in appState.messages)
        removeMessage(appState.messages[id]);
}

function editMessage(message, textarea) {
    message.edited = true;
    message.text = textarea.value;

    put(appState.mainUrl, JSON.stringify(message), function() {
        var table = document.getElementsByClassName('table')[0];
        var isBottomScroll = isScrollBottom(table);

        textarea.value = '';
        isEditing = false;
        selectedRow = null;

        if(isBottomScroll)
            table.scrollTop = table.scrollHeight;
    });
}

function removeMessage(message) {
    message.text = 'This message has been deleted.';
    message.deleted = true;

    deleteRequest(appState.mainUrl + '?id=' + message.id, function() {
        setMessageActive(selectedRow, false);
        selectedRow = null;
        setIconsVisible(false);
    });
}

function onConnectionLost() {
    var connection = document.getElementById('connection');
    connection.classList.remove('label-success');
    connection.classList.add('label-danger');
    connection.textContent = "Disconnected";
}

function onConnectionSet() {
    var connection = document.getElementById('connection');
    connection.classList.remove('label-danger');
    connection.classList.add('label-success');
    connection.textContent = "Connected";
}

function storeUsername() {
    if(typeof(Storage) == "undefined") {
        alert('localStorage is not accessible');
        return;
    }
    localStorage.setItem("Chat Username", JSON.stringify(username));
}

function createAllMessages(allMessages) {
    var messages = appState.messages;

    for (var i = 0, id; i < allMessages.length; i++) {
        id = allMessages[i].id;

        if (id in messages) {
            var row = document.getElementById(id);
            updateItem(row, allMessages[i]);
            messages[id].username = allMessages[i].username;
        } else
            addMessageToHTML(allMessages[i]);
    }
}

function defaultErrorHandler(message) {
    onConnectionLost();
    console.error(message);
    //restoreMessages();
}

function get(url, continueWith, continueWithError) {
    ajax('GET', url, null, continueWith, continueWithError);
}

function post(url, data, continueWith, continueWithError) {
    ajax('POST', url, data, continueWith, continueWithError);
}

function put(url, data, continueWith, continueWithError) {
    ajax('PUT', url, data, continueWith, continueWithError);
}

function deleteRequest(url, continueWith, continueWithError) {
    ajax('DELETE', url, null, continueWith, continueWithError);
}

function isError(text) {
    if(text == "")
        return false;

    try {
        var obj = JSON.parse(text);
    } catch(ex) {
        return true;
    }

    return !!obj.error;
}

function ajax(method, url, data, continueWith, continueWithError) {
    var xhr = new XMLHttpRequest();

    continueWithError = continueWithError || defaultErrorHandler;
    xhr.open(method || 'GET', url, true);

    xhr.onload = function () {
        if (xhr.readyState !== 4)
            return;

        if(xhr.status != 200) {
            onConnectionLost();
            continueWithError('Error on the server side, response ' + xhr.status);
            return;
        }
        else
            onConnectionSet();

        if(isError(xhr.responseText)) {
            //onConnectionLost();
            continueWithError('Error on the server side, response ' + xhr.responseText);
            return;
        }

        continueWith(xhr.responseText);
    };

    xhr.ontimeout = function () {
        onConnectionLost();
        continueWithError('Server timed out !');
    };

    xhr.onerror = function () {
        onConnectionLost();
        var errMsg = 'Server connection error !\n'+
            '\n' +
            'Check if \n'+
            '- server is active\n'+
            '- server sends header "Access-Control-Allow-Origin:*"';

        continueWithError(errMsg);
    };

    xhr.send(data);
}