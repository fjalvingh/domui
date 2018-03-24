var WebUI;
(function (WebUI) {
    export function returnKeyPress(evt, node) {
        var keyCode = WebUI.normalizeKey(evt);
        if (keyCode != 13000 && keyCode != 13)
            return true;
        try {
            evt.target.onblur(evt);
        }
        catch (err) {
        }
        WebUI.scall(evt.currentTarget ? evt.currentTarget.id : node.id, 'returnpressed');
        return false;
    }
    export function wtMouseDown(e) {
        alert(e);
    }
    export function alignTopToBottom(nodeId, alignToId, offsetY, doCallback) {
        var alignNode = $('#' + alignToId);
        var node = $('#' + nodeId);
        var myTopPos;
        if (node.css('position') == 'fixed') {
            myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY + $(alignNode).outerHeight(true);
        }
        else {
            myTopPos = $(alignNode).position().top + offsetY + $(alignNode).outerHeight(true);
        }
        $(node).css('top', myTopPos);
        if (doCallback) {
            WebUI.notifySizePositionChangedOnId(nodeId);
        }
    }
    function alignToTop(nodeId, alignToId, offsetY, doCallback) {
        var alignNode = $('#' + alignToId);
        var node = $('#' + nodeId);
        var myTopPos;
        if (node.css('position') == 'fixed') {
            myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY;
        }
        else {
            myTopPos = $(alignNode).position().top + offsetY;
        }
        var nodeHeight = $(node).outerHeight(true);
        if (myTopPos + nodeHeight > $(window).height()) {
            myTopPos = $(window).height() - nodeHeight;
        }
        $(node).css('top', myTopPos);
        if (doCallback) {
            WebUI.notifySizePositionChangedOnId(nodeId);
        }
    }
    function alignToLeft(nodeId, alignToId, offsetX, doCallback) {
        var node = $('#' + nodeId);
        var alignNode = $('#' + alignToId);
        var myLeftPos;
        if (node.css('position') == 'fixed') {
            myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX;
        }
        else {
            myLeftPos = $(alignNode).position().left + offsetX;
        }
        var nodeWidth = $(node).outerWidth(true);
        if (myLeftPos + nodeWidth > $(window).width()) {
            myLeftPos = $(window).width() - nodeWidth;
            if (myLeftPos < 1) {
                myLeftPos = 1;
            }
        }
        $(node).css('left', myLeftPos);
        if (doCallback) {
            WebUI.notifySizePositionChangedOnId(nodeId);
        }
    }
    function alignToRight(nodeId, alignToId, offsetX, doCallback) {
        var node = $('#' + nodeId);
        var alignNode = $('#' + alignToId);
        var myLeftPos;
        if (node.css('position') == 'fixed') {
            myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
        }
        else {
            myLeftPos = $(alignNode).position().left + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
        }
        if (myLeftPos < 1) {
            myLeftPos = 1;
        }
        $(node).css('left', myLeftPos);
        if (doCallback) {
            WebUI.notifySizePositionChangedOnId(nodeId);
        }
    }
    function alignToMiddle(nodeId, alignToId, offsetX, doCallback) {
        var node = $('#' + nodeId);
        var alignNode = $('#' + alignToId);
        var myLeftPos;
        if (node.css('position') == 'fixed') {
            myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
        }
        else {
            myLeftPos = $(alignNode).position().left + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
        }
        if (myLeftPos < 1) {
            myLeftPos = 1;
        }
        $(node).css('left', myLeftPos);
        if (doCallback) {
            WebUI.notifySizePositionChangedOnId(nodeId);
        }
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.computil.js.map
