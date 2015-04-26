'use strict';

function getCaretPosition (textarea) {
    var caretPos = 0;
    if (document.selection) {
        textarea.focus ();
        var select = document.selection.createRange ();
        select.moveStart ('character', -textarea.value.length);
        caretPos = select.text.length;
    }
    else if (textarea.selectionStart || textarea.selectionStart == '0')
        caretPos = textarea.selectionStart;
    return caretPos;
}

function setCaretPosition(textarea, pos) {
    if (textarea.setSelectionRange)	{
        textarea.focus();
        textarea.setSelectionRange(pos, pos);
    }
    else if (textarea.createTextRange) {
        var range = textarea.createTextRange();
        range.collapse(true);
        range.moveEnd('character', pos);
        range.moveStart('character', pos);
        range.select();
    }
}

function isScrollBottom(table) {
    var bottomScroll = false;
    var h1 = table.scrollHeight - table.scrollTop;

    var h2 = table.clientHeight;
    if(table.firstElementChild && table.firstElementChild.lastChild.scrollHeight)
        h2 += table.firstElementChild.lastChild.scrollHeight;

    if(h1 <= h2)
        bottomScroll = true;

    return bottomScroll;
}

function getCurrentTime() {
    var date = new Date();
    var time = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth()+1)).slice(-2) + "<br>";
    time += ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
    time += ':' + ('0' + date.getSeconds()).slice(-2);
    return time;
}

var uniqueId = function() {
    var date = Date.now();
    var random = Math.random() * Math.random();
    return Math.floor(date * random).toString();
};

function setIconsVisible(visible) {
    if(visible) {
        document.getElementsByClassName('icon')[0].style.display = "block";
        document.getElementsByClassName('icon')[1].style.display = "block";
    } else {
        document.getElementsByClassName('icon')[0].style.display = "none";
        document.getElementsByClassName('icon')[1].style.display = "none";
    }
}

function updateCounter() {
    var counter = document.getElementsByClassName('counter-holder')[0];
    var count = document.getElementsByClassName("items")[0].rows.length;
    counter.innerText = count.toString();
}