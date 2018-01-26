var WebUI;
(function (WebUI) {
    function getInputFields(fields) {
        var q1 = $("input").get();
        for (var i = q1.length; --i >= 0;) {
            var t = q1[i];
            if (t.type == 'file')
                continue;
            if (t.type == 'hidden' && !t.getAttribute('s'))
                continue;
            var val = undefined;
            if (t.type == 'checkbox' || t.type == 'radio') {
                val = t.checked ? "y" : "n";
            }
            else {
                val = t.value;
            }
            fields[t.id] = val;
        }
        q1 = $("select").get();
        for (var i = q1.length; --i >= 0;) {
            var sel = q1[i];
            var val = undefined;
            if (sel.selectedIndex != -1) {
                val = sel.options[sel.selectedIndex].value;
            }
            if (val != undefined)
                fields[sel.id] = val;
        }
        q1 = $("textarea").get();
        for (var i = q1.length; --i >= 0;) {
            var sel = q1[i];
            var val = void 0;
            if (sel.className == 'ui-ckeditor') {
                var editor = window.CKEDITOR.instances[sel.id];
                if (null == editor)
                    throw "Cannot locate editor with id=" + sel.id;
                val = editor.getData();
            }
            else {
                val = sel.value;
            }
            fields[sel.id] = val;
        }
        var list = _inputFieldList;
        for (var i = list.length; --i >= 0;) {
            var item = list[i];
            if (!document.getElementById(item.id)) {
                list.splice(i, 1);
            }
            else {
                var data = item.control.getInputField();
                fields[item.id] = data;
            }
        }
        return fields;
    }
    WebUI.getInputFields = getInputFields;
    var _inputFieldList;
    function registerInputControl(id, control) {
        var list = _inputFieldList;
        for (var i = list.length; --i >= 0;) {
            var item = list[i];
            if (item.id == id) {
                item.control = control;
                return;
            }
        }
        list.push({ id: id, control: control });
    }
    WebUI.registerInputControl = registerInputControl;
    function findInputControl(id) {
        var list = _inputFieldList;
        for (var i = list.length; --i >= 0;) {
            var item = list[i];
            if (item.id == id && document.getElementById(item.id)) {
                return item.control;
            }
        }
        return null;
    }
    WebUI.findInputControl = findInputControl;
    function clicked(h, id, evt) {
        $(document.body).trigger("beforeclick", [$("#" + id), evt]);
        var fields = {};
        this.getInputFields(fields);
        fields.webuia = "clicked";
        fields.webuic = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        cancelPolling();
        if (!evt)
            evt = window.event;
        if (evt) {
            evt.cancelBubble = true;
            if (evt.stopPropagation)
                evt.stopPropagation();
        }
        var e = $.event.fix(evt);
        fields._pageX = e.pageX;
        fields._pageY = e.pageY;
        fields._controlKey = e.ctrlKey == true;
        fields._shiftKey = e.shiftKey == true;
        fields._altKey = e.altKey == true;
        $.ajax({
            url: WebUI.getPostURL(),
            dataType: "*",
            data: fields,
            cache: false,
            type: "POST",
            error: handleError,
            success: handleResponse
        });
        return false;
    }
    WebUI.clicked = clicked;
    function prepareAjaxCall(id, action, fields) {
        if (!fields)
            fields = {};
        this.getInputFields(fields);
        fields.webuia = action;
        fields.webuic = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        return {
            url: WebUI.getPostURL(),
            dataType: "*",
            data: fields,
            cache: false,
            type: "POST",
            success: handleResponse,
            error: handleError
        };
    }
    WebUI.prepareAjaxCall = prepareAjaxCall;
    function scall(id, action, fields) {
        var call = prepareAjaxCall(id, action, fields);
        cancelPolling();
        $.ajax(call);
    }
    WebUI.scall = scall;
    function jsoncall(id, fields) {
        if (!fields)
            fields = {};
        fields["webuia"] = "$pagejson";
        fields["webuic"] = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        var response = "";
        $.ajax({
            url: WebUI.getPostURL(),
            dataType: "text/xml",
            data: fields,
            cache: false,
            async: false,
            type: "POST",
            success: function (data, state) {
                response = data;
            },
            error: handleError
        });
        return eval("(" + response + ")");
    }
    WebUI.jsoncall = jsoncall;
    function sendJsonAction(id, action, json) {
        var fields = {};
        fields["json"] = JSON.stringify(json);
        scall(id, action, fields);
    }
    WebUI.sendJsonAction = sendJsonAction;
    function callJsonFunction(id, action, fields) {
        if (!fields)
            fields = {};
        fields.webuia = "#" + action;
        fields.webuic = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        var response = "";
        $.ajax({
            url: WebUI.getPostURL(),
            dataType: "text/xml",
            data: fields,
            cache: false,
            async: false,
            type: "POST",
            success: function (data, state) {
                response = data;
            },
            error: handleError
        });
        return eval("(" + response + ")");
    }
    WebUI.callJsonFunction = callJsonFunction;
    function clickandchange(h, id, evt) {
        if (!evt)
            evt = window.event;
        if (evt) {
            evt.cancelBubble = true;
            if (evt.stopPropagation)
                evt.stopPropagation();
        }
        scall(id, 'clickandvchange');
    }
    WebUI.clickandchange = clickandchange;
    function valuechanged(unknown, id) {
        var item = document.getElementById(id);
        if (item && (item.tagName == "input" || item.tagName == "INPUT") && item.className == "ui-di") {
            this.dateInputRepairValueIn(item);
        }
        var fields = {};
        this.getInputFields(fields);
        fields["webuia"] = "vchange";
        fields["webuic"] = id;
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        cancelPolling();
        $.ajax({
            url: WebUI.getPostURL(),
            dataType: "*",
            data: fields,
            cache: false,
            type: "POST",
            success: handleResponse,
            error: handleError
        });
    }
    WebUI.valuechanged = valuechanged;
    function handleResponse(data, state) {
        clearErrorAsy();
        $.webui(data);
    }
    WebUI.handleResponse = handleResponse;
    function handleError(request, status, exc) {
        var txt = request.responseText;
        if (document.body)
            document.body.style.cursor = 'default';
        if (txt.length == 0) {
            if (status == "error")
                return;
            txt = "De server is niet bereikbaar 1, status=" + status + ", " + request.statusText;
        }
        document.write(txt);
        document.close();
        window.setTimeout('document.body.style.cursor="default"', 1000);
        return true;
    }
    WebUI.handleError = handleError;
    var _asyalerted = false;
    var _asyDialog = null;
    var _ignoreErrors = false;
    var _asyHider = undefined;
    function handleErrorAsy(request, status, exc) {
        if (_asyalerted) {
            startPolling(_pollInterval);
            return;
        }
        if (status === "abort")
            return;
        _asyalerted = true;
        var txt = request.responseText || "No response - status=" + status;
        if (txt.length > 512)
            txt = txt.substring(0, 512) + "...";
        if (txt.length == 0)
            txt = WebUI._T.sysPollFailMsg + status;
        setTimeout(function () {
            if (_ignoreErrors)
                return;
            document.body.style.cursor = 'default';
            var hdr = document.createElement('div');
            document.body.appendChild(hdr);
            hdr.className = 'ui-io-blk2';
            _asyHider = hdr;
            var ald = document.createElement('div');
            document.body.appendChild(ald);
            ald.className = 'ui-ioe-asy';
            _asyDialog = ald;
            var d = document.createElement('div');
            ald.appendChild(d);
            d.className = "ui-ioe-ttl";
            d.appendChild(document.createTextNode(WebUI._T.sysPollFailTitle));
            d = document.createElement('div');
            ald.appendChild(d);
            d.className = "ui-ioe-msg";
            d.appendChild(document.createTextNode(txt));
            d = document.createElement('div');
            ald.appendChild(d);
            d.className = "ui-ioe-msg2";
            var img = document.createElement('div');
            d.appendChild(img);
            img.className = "ui-ioe-img";
            d.appendChild(document.createTextNode(WebUI._T.sysPollFailCont));
            startPolling(_pollInterval);
        }, 250);
    }
    WebUI.handleErrorAsy = handleErrorAsy;
    function clearErrorAsy() {
        if (_asyDialog) {
            $(_asyDialog).remove();
        }
        if (_asyHider) {
            $(_asyHider).remove();
        }
        _asyDialog = null;
        _asyHider = null;
        _asyalerted = false;
    }
    WebUI.clearErrorAsy = clearErrorAsy;
    var _pollInterval = 2500;
    var _pollActive = false;
    var _pollTimer = undefined;
    function startPolling(interval) {
        if (interval < 100 || interval == undefined || interval == null) {
            alert("Bad interval: " + interval);
            return;
        }
        _pollInterval = interval;
        if (_pollActive)
            return;
        _pollActive = true;
        _pollTimer = setTimeout("WebUI.poll()", _pollInterval);
    }
    WebUI.startPolling = startPolling;
    function cancelPolling() {
        if (!_pollActive)
            return;
        clearTimeout(_pollTimer);
        _pollActive = false;
    }
    WebUI.cancelPolling = cancelPolling;
    function poll() {
        cancelPolling();
        var fields = {};
        fields["webuia"] = "pollasy";
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        $.ajax({
            url: window.location.href,
            dataType: "*",
            data: fields,
            cache: false,
            global: false,
            success: handleResponse,
            error: handleErrorAsy
        });
    }
    WebUI.poll = poll;
    function pingServer(timeout) {
        var url = window.DomUIappURL + "to.etc.domui.parts.PollInfo.part";
        var fields = {};
        fields["$pt"] = window.DomUIpageTag;
        fields["$cid"] = window.DomUICID;
        $.ajax({
            url: url,
            dataType: "*",
            data: fields,
            cache: false,
            global: false,
            success: function (data, state) {
                executePollCommands(data);
            },
            error: function () {
            }
        });
        startPingServer(timeout);
    }
    WebUI.pingServer = pingServer;
    function startPingServer(timeout) {
        if (timeout < 60 * 1000)
            timeout = 60 * 1000;
        setTimeout("WebUI.pingServer(" + timeout + ")", timeout);
    }
    WebUI.startPingServer = startPingServer;
    function executePollCommands(data) {
    }
    function unloaded() {
        _ignoreErrors = true;
        sendobituary();
    }
    WebUI.unloaded = unloaded;
    function beforeUnload() {
        _ignoreErrors = true;
    }
    WebUI.beforeUnload = beforeUnload;
    function sendobituary() {
        try {
            var rq = void 0;
            var w = window;
            if (w.XMLHttpRequest) {
                rq = new XMLHttpRequest();
            }
            else if (w.ActiveXObject) {
                rq = new ActiveXObject("Microsoft.XMLHTTP");
            }
            else {
                alert("Cannot send obituary (no transport)");
                return;
            }
            rq.open("GET", WebUI.getObituaryURL() + "?$cid=" + w.DomUICID + "&webuia=OBITUARY&$pt=" + w.DomUIpageTag, false);
            rq.send(null);
        }
        catch (ex) {
        }
    }
    WebUI.sendobituary = sendobituary;
    function notifyPage(command) {
        var bodyId = '_1';
        var pageBody = document.getElementById(bodyId);
        if (pageBody) {
            var fields = {};
            fields["webuia"] = "notifyPage";
            fields[bodyId + "_command"] = command;
            WebUI.scall(bodyId, "notifyPage", fields);
        }
    }
    WebUI.notifyPage = notifyPage;
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.ajax.js.map
