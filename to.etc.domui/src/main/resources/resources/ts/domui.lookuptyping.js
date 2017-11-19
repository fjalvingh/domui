var WebUI;
(function (WebUI) {
    WebUI._hideExpiredMessage = false;
    var _keepAliveInterval = 0;
    function setHideExpired() {
        WebUI._hideExpiredMessage = true;
    }
    function onLookupTypingReturnKeyHandler(id, event) {
        var node = document.getElementById(id);
        if (!node || node.tagName.toLowerCase() != 'input')
            return;
        if (!event) {
            event = window.event;
            if (!event)
                return;
        }
        var keyCode = WebUI.normalizeKey(event);
        var isReturn = (keyCode == 13000 || keyCode == 13);
        if (isReturn) {
            if (scheduledOnLookupTypingTimerID) {
                window.clearTimeout(scheduledOnLookupTypingTimerID);
                scheduledOnLookupTypingTimerID = null;
            }
            event.cancelBubble = true;
            if (event.stopPropagation)
                event.stopPropagation();
            var selectedIndex = getKeywordPopupSelectedRowIndex(node);
            var trNode = selectedIndex < 0 ? null : $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
            if (trNode) {
                setKeywordPopupSelectedRowIndex(node, -1);
                $(trNode).trigger('click');
            }
            else {
                lookupTypingDone(id);
            }
        }
    }
    var scheduledOnLookupTypingTimerID = null;
    function scheduleOnLookupTypingEvent(id, event) {
        var node = document.getElementById(id);
        if (!node || node.tagName.toLowerCase() != 'input')
            return;
        if (!event) {
            event = window.event;
            if (!event)
                return;
        }
        var keyCode = WebUI.normalizeKey(event);
        var isReturn = (keyCode == 13000 || keyCode == 13);
        if (isReturn) {
            event.cancelBubble = true;
            if (event.stopPropagation)
                event.stopPropagation();
            return;
        }
        var isLeftArrowKey = (keyCode == 37000 || keyCode == 37);
        var isRightArrowKey = (keyCode == 39000 || keyCode == 39);
        if (isLeftArrowKey || isRightArrowKey) {
            return;
        }
        if (scheduledOnLookupTypingTimerID) {
            window.clearTimeout(scheduledOnLookupTypingTimerID);
            scheduledOnLookupTypingTimerID = null;
        }
        var isDownArrowKey = (keyCode == 40000 || keyCode == 40);
        var isUpArrowKey = (keyCode == 38000 || keyCode == 38);
        if (isDownArrowKey || isUpArrowKey) {
            event.cancelBubble = true;
            if (event.stopPropagation)
                event.stopPropagation();
            var selectedIndex = getKeywordPopupSelectedRowIndex(node);
            if (selectedIndex < 0)
                selectedIndex = 0;
            var trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
            if (trNode) {
                trNode.className = "ui-keyword-popup-row";
            }
            var trNodes = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr");
            if (trNodes.length > 0) {
                var divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get(0);
                if (divPopup) {
                    $(divPopup).fadeIn(300);
                    node.parentNode.style.zIndex = divPopup.style.zIndex;
                }
                if (isDownArrowKey) {
                    selectedIndex++;
                }
                else {
                    selectedIndex--;
                }
                if (selectedIndex > trNodes.length) {
                    selectedIndex = 0;
                }
                if (selectedIndex < 0) {
                    selectedIndex = trNodes.length;
                }
                trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
                if (trNode) {
                    trNode.className = "ui-keyword-popop-rowsel";
                }
            }
            else {
                selectedIndex = 0;
            }
            setKeywordPopupSelectedRowIndex(node, selectedIndex);
        }
        else {
            scheduledOnLookupTypingTimerID = window.setTimeout("WebUI.lookupTyping('" + id + "')", 500);
        }
    }
    function getKeywordPopupSelectedRowIndex(keywordInputNode) {
        var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
        if (selectedIndexInput instanceof HTMLInputElement) {
            if (selectedIndexInput.value && selectedIndexInput.value != "") {
                return parseInt(selectedIndexInput.value);
            }
		}
        return -1;
    }
    function setKeywordPopupSelectedRowIndex(keywordInputNode, intValue) {
        var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
        if (!selectedIndexInput) {
            selectedIndexInput = document.createElement("input");
            selectedIndexInput.setAttribute("type", "hidden");
            $(keywordInputNode.parentNode).append($(selectedIndexInput));
        }
        selectedIndexInput.value = intValue;
    }
    function lookupPopupClicked(id) {
        var node = document.getElementById(id);
        if (!node || node.tagName.toLowerCase() != 'input') {
            return;
        }
        var selectedIndex = getKeywordPopupSelectedRowIndex(node);
        if (selectedIndex < 0)
            selectedIndex = 0;
        var trNode = $(node.parentNode).children("div.ui-lui-keyword-popup").children("div").children("table").children("tbody").children("tr:nth-child(" + selectedIndex + ")").get(0);
        if (trNode) {
            WebUI.clicked(trNode, trNode.id, null);
        }
    }
    function lookupRowMouseOver(keywordInputId, rowNodeId) {
        var keywordInput = document.getElementById(keywordInputId);
        if (!keywordInput || keywordInput.tagName.toLowerCase() != 'input') {
            return;
        }
        var rowNode = document.getElementById(rowNodeId);
        if (!rowNode || rowNode.tagName.toLowerCase() != 'tr') {
            return;
        }
        var oldIndex = getKeywordPopupSelectedRowIndex(keywordInput);
        if (oldIndex < 0)
            oldIndex = 0;
        var trNodes = $(rowNode.parentNode).children("tr");
        var newIndex = 0;
        for (var i = 1; i <= trNodes.length; i++) {
            if (rowNode == trNodes.get(i - 1)) {
                newIndex = i;
                break;
            }
        }
        if (oldIndex != newIndex) {
            var deselectRow = $(rowNode.parentNode).children("tr:nth-child(" + oldIndex + ")").get(0);
            if (deselectRow) {
                deselectRow.className = "ui-keyword-popop-row";
            }
            rowNode.className = "ui-keyword-popop-rowsel";
            setKeywordPopupSelectedRowIndex(keywordInput, newIndex);
        }
    }
    function hideLookupTypingPopup(id) {
        var node = document.getElementById(id);
        if (!node || node.tagName.toLowerCase() != 'input')
            return;
        var divPopup = $(node.parentNode).children("div.ui-lui-keyword-popup").get();
        if (divPopup) {
            $(divPopup).fadeOut(200);
        }
        if ($.browser.msie) {
            window.setTimeout(function () {
                try {
                    node.parentNode.style.zIndex = node.style.zIndex;
                }
                catch (e) {
                }
            }, 200);
        }
        else {
            node.parentNode.style.zIndex = node.style.zIndex;
        }
    }
    function showLookupTypingPopupIfStillFocusedAndFixZIndex(id) {
        var node = document.getElementById(id);
        if (!node || node.tagName.toLowerCase() != 'input')
            return;
        var wasInFocus = node == document.activeElement;
        var qDivPopup = $(node.parentNode).children("div.ui-lui-keyword-popup");
        var divPopup;
        if (qDivPopup.length > 0) {
            divPopup = qDivPopup.get(0);
            divPopup.style.zIndex = node.style.zIndex + 1;
            node.parentNode.style.zIndex = divPopup.style.zIndex;
        }
        else {
            node.parentNode.style.zIndex = node.style.zIndex;
        }
        if (wasInFocus && divPopup) {
            $(divPopup).show();
        }
        var trNods = $(qDivPopup).children("div").children("table").children("tbody").children("tr");
        if (trNods && trNods.length > 0) {
            for (var i = 0; i < trNods.length; i++) {
                var trNod = trNods.get(i);
                $(trNod).bind("mouseover", { nodeId: id, trId: trNod.id }, function (event) {
                    lookupRowMouseOver(event.data.nodeId, event.data.trId);
                });
            }
        }
        if (divPopup) {
            $(divPopup).bind("click", { nodeId: id }, function (event) {
                lookupPopupClicked(event.data.nodeId);
            });
        }
    }
    function displayWaiting(id) {
        var node = document.getElementById(id);
        if (node) {
            for (var i = 0; i < node.childNodes.length; i++) {
                var child = node.childNodes[i];
                if (child.className == 'ui-lui-waiting') {
                    child.style.display = 'inline';
                }
            }
        }
    }
    function hideWaiting(id) {
        var node = document.getElementById(id);
        if (node) {
            for (var i = 0; i < node.childNodes.length; i++) {
                var child = node.childNodes[i];
                if (child.className == 'ui-lui-waiting') {
                    child.style.display = 'none';
                }
            }
        }
    }
    function lookupTyping(id) {
        var lookupField = document.getElementById(id);
        if (lookupField) {
            var fields = {};
            WebUI.getInputFields(fields);
            fields["webuia"] = "lookupTyping";
            fields["webuic"] = id;
            fields["$pt"] = window.DomUIpageTag;
            fields["$cid"] = window.DomUICID;
            WebUI.cancelPolling();
            var displayWaitingTimerID_1 = null;
            $.ajax({
                url: WebUI.getPostURL(),
                dataType: "*",
                data: fields,
                cache: false,
                type: "POST",
                global: false,
                beforeSend: function () {
                    var parentDiv = lookupField.parentElement;
                    if (parentDiv) {
                        displayWaitingTimerID_1 = window.setTimeout("WebUI.displayWaiting('" + parentDiv.id + "')", 500);
                    }
                },
                complete: function () {
                    if (displayWaitingTimerID_1) {
                        window.clearTimeout(displayWaitingTimerID_1);
                        displayWaitingTimerID_1 = null;
                        var parentDiv = lookupField.parentElement;
                        if (parentDiv) {
                            hideWaiting(parentDiv.id);
                        }
                    }
                    showLookupTypingPopupIfStillFocusedAndFixZIndex(id);
                    WebUI.doCustomUpdates();
                },
                success: WebUI.handleResponse,
                error: WebUI.handleError
            });
        }
    }
    function lookupTypingDone(id) {
        var fields = {};
        this.getInputFields(fields);
        fields["webuia"] = "lookupTypingDone";
        fields["webuic"] = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        WebUI.cancelPolling();
        $.ajax({
            url: WebUI.getPostURL(),
            dataType: "*",
            data: fields,
            cache: false,
            type: "POST",
            success: WebUI.handleResponse,
            error: WebUI.handleError
        });
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.lookuptyping.js.map
