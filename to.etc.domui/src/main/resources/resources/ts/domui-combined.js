var WebUI;
(function (WebUI) {
    var _inputFieldList = [];
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
        WebUI.getInputFields(fields);
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
        if (status === "parsererror") {
            alert("ERROR: DomUI server returned invalid XML");
            var hr = window.location.href;
            window.location.href = hr;
            return;
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
var WebUI;
(function (WebUI) {
    function isReallyIE7() {
        var doc = document;
        return ($.browser.msie && parseInt($.browser.version) == 7 && (!doc.documentMode || doc.documentMode == 7));
    }
    WebUI.isReallyIE7 = isReallyIE7;
    function isIE8orIE8c() {
        var doc = document;
        return ($.browser.msie && (parseInt($.browser.version) == 8 || (parseInt($.browser.version) == 7 && doc.documentMode == 8)));
    }
    WebUI.isIE8orIE8c = isIE8orIE8c;
    function isNormalIE9plus() {
        var doc = document;
        return ($.browser.msie && parseInt($.browser.version) >= 9 && doc.documentMode >= 9);
    }
    WebUI.isNormalIE9plus = isNormalIE9plus;
    function isIE8orNewer() {
        var doc = document;
        return ($.browser.msie && (parseInt($.browser.version) >= 8 || (parseInt($.browser.version) == 7 && doc.documentMode >= 8)));
    }
    WebUI.isIE8orNewer = isIE8orNewer;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var SWFUpload;
    function bulkUpload(id, buttonId, url) {
        var ctl = $('#' + id);
        ctl.swfupload({
            upload_url: url,
            flash_url: window.DomUIappURL + "$js/swfupload.swf",
            file_types: '*.*',
            file_upload_limit: 1000,
            file_queue_limit: 0,
            file_size_limit: "100 MB",
            button_width: 120,
            button_height: 24,
            button_placeholder_id: buttonId,
            button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
            button_cursor: SWFUpload.CURSOR.HAND
        });
        var target = $("#" + id + " .ui-bupl-queue");
        ctl.bind('fileQueued', function (event, file) {
            var uf = new UploadFile(file, target, function () {
                $.swfupload.getInstance(ctl).cancelUpload(file.id);
            });
        });
        ctl.bind('uploadStart', function (event, file) {
            var uf = new UploadFile(file, target);
            uf.uploadStarted();
        });
        ctl.bind('uploadProgress', function (event, file, bytesdone, bytestotal) {
            var uf = new UploadFile(file, target);
            var pct = bytesdone * 100 / bytestotal;
            uf.setProgress(pct);
        });
        ctl.bind('uploadError', function (event, file, code, msg) {
            var uf = new UploadFile(file, target);
            uf.uploadError(msg);
        });
        ctl.bind('uploadSuccess', function (event, file, code, msg) {
            var uf = new UploadFile(file, target);
            uf.uploadComplete();
            WebUI.scall(id, "uploadDone", {});
        });
        ctl.bind('queueComplete', function (event, numUploaded) {
            WebUI.scall(id, "queueComplete", {});
        });
        ctl.bind('fileDialogComplete', function (nfiles) {
            if (0 == nfiles) {
                return;
            }
            ctl.swfupload('startUpload');
            WebUI.scall(id, "queueStart", {});
        });
        ctl.bind('fileQueueError', function (event, file, errorCode, message) {
            try {
                if (errorCode === SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED) {
                    alert(WebUI._T.buplTooMany);
                    return;
                }
                var uf = new UploadFile(file, target);
                switch (errorCode) {
                    case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
                        uf.uploadError(WebUI._T.buplTooBig);
                        break;
                    case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
                        uf.uploadError(WebUI._T.buplEmptyFile);
                        break;
                    case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
                        uf.uploadError(WebUI._T.buplInvalidType);
                        break;
                    default:
                        if (file !== null) {
                            uf.uploadError(WebUI._T.buplUnknownError);
                        }
                        break;
                }
            }
            catch (ex) {
                alert(ex);
            }
        });
    }
    WebUI.bulkUpload = bulkUpload;
    var UploadFile = (function () {
        function UploadFile(file, target, cancelFn) {
            this.uploadStarted = function () {
                $(".ui-bupl-stat", this._ui).html(WebUI._T.buplRunning);
                $(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").addClass("ui-bupl-running");
            };
            this.setProgress = function (pct) {
                $(".ui-bupl-perc", this._ui).width(pct + "%");
            };
            this.uploadError = function (message) {
                $(".ui-bupl-stat", this._ui).html(WebUI._T.buplError + ": " + message);
                $(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").addClass("ui-bupl-error");
                $(".ui-bupl-cancl", this._ui).remove();
                this.setProgress(0);
                this.suicide();
            };
            this.uploadComplete = function () {
                $(".ui-bupl-stat", this._ui).html(WebUI._T.buplComplete);
                $(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").removeClass("ui-bupl-error").addClass("ui-bupl-complete");
                this.setProgress(100);
                $(".ui-bupl-cancl", this._ui).remove();
                this.suicide();
            };
            this.suicide = function () {
                this._ui.delay(8000).fadeOut(500);
            };
            this._id = file.id;
            var ui = this._ui = $('#' + file.id);
            if (this._ui.length == 0) {
                target.append("<div id='" + this._id + "' class='ui-bupl-file'><div class='ui-bupl-inner ui-bupl-pending'><a href='#' class='ui-bupl-cancl'> </a><div class='ui-bupl-name'>" + file.name + "</div><div class='ui-bupl-stat'>" + WebUI._T.buplPending + "</div><div class='ui-bupl-perc'></div></div></div>");
                this._ui = $('#' + file.id);
                if (cancelFn) {
                    var me = this;
                    $(".ui-bupl-cancl", this._ui).bind("click", function () {
                        $(".ui-bupl-stat", ui).html(WebUI._T.buplCancelled);
                        me.suicide();
                        cancelFn();
                    });
                }
            }
        }
        return UploadFile;
    }());
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _busyCount = 0;
    var _busyOvl;
    var _busyTimer;
    function blockUI() {
        if (_busyCount++ > 0)
            return;
        var el = document.body;
        if (!el)
            return;
        el.style.cursor = "wait";
        var d = document.createElement('div');
        el.appendChild(d);
        d.className = 'ui-io-blk';
        _busyOvl = d;
        _busyTimer = setTimeout(function () { return busyIndicate(); }, 250);
    }
    WebUI.blockUI = blockUI;
    function isUIBlocked() {
        return _busyOvl != undefined && _busyOvl != null;
    }
    WebUI.isUIBlocked = isUIBlocked;
    function busyIndicate() {
        if (_busyTimer) {
            clearTimeout(_busyTimer);
            _busyTimer = null;
        }
        if (_busyOvl) {
            _busyOvl.className = "ui-io-blk2";
        }
    }
    function unblockUI() {
        if (_busyCount <= 0 || !_busyOvl)
            return;
        if (--_busyCount != 0)
            return;
        if (_busyTimer) {
            clearTimeout(_busyTimer);
            _busyTimer = null;
        }
        var el = document.body;
        if (!el)
            return;
        el.style.cursor = "default";
        try {
            el.removeChild(_busyOvl);
        }
        catch (x) {
        }
        _busyOvl = null;
    }
    WebUI.unblockUI = unblockUI;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _selectStart = undefined;
    var _popinCloseList = [];
    var _fckEditorIDs = [];
    var FCKeditor_fixLayout;
    function oddCharAndClickCallback(nodeId, clickId) {
        oddChar(document.getElementById(nodeId));
        document.getElementById(clickId).click();
    }
    WebUI.oddCharAndClickCallback = oddCharAndClickCallback;
    function oddChar(obj) {
        WebUI.toClip(obj.innerHTML);
    }
    WebUI.oddChar = oddChar;
    function popupMenuShow(refid, menu) {
        registerPopinClose(menu);
        var pos = $(refid).offset();
        var eWidth = $(refid).outerWidth();
        var mwidth = $(menu).outerWidth();
        var left = (pos.left);
        if (left + mwidth > screen.width)
            left = screen.width - mwidth - 10;
        var top = 3 + pos.top;
        $(menu).css({
            position: 'absolute',
            zIndex: 100,
            left: left + "px",
            top: top + "px"
        });
        $(menu).hide().fadeIn();
    }
    WebUI.popupMenuShow = popupMenuShow;
    function popupSubmenuShow(parentId, submenu) {
        $(submenu).position({ my: 'left top', at: 'center top', of: parentId });
    }
    WebUI.popupSubmenuShow = popupSubmenuShow;
    function registerPopinClose(id) {
        _popinCloseList.push(id);
        $(id).bind("mouseleave", popinMouseClose);
        if (_popinCloseList.length != 1)
            return;
        $(document.body).bind("keydown", popinKeyClose);
    }
    WebUI.registerPopinClose = registerPopinClose;
    function popinClosed(id) {
        for (var i = 0; i < _popinCloseList.length; i++) {
            if (id === _popinCloseList[i]) {
                $(id).unbind("mousedown", popinMouseClose);
                _popinCloseList.splice(i, 1);
                if (_popinCloseList.length == 0) {
                    $(document.body).unbind("keydown", popinKeyClose);
                    $(document.body).unbind("beforeclick", popinBeforeClick);
                }
                return;
            }
        }
    }
    WebUI.popinClosed = popinClosed;
    function popinBeforeClick(ee1, obj, clickevt) {
        for (var i = 0; i < _popinCloseList.length; i++) {
            var id = _popinCloseList[i];
            obj = $(obj);
            var cl = obj.closest(id);
            if (cl.size() > 0) {
                $(id).unbind("mousedown", popinMouseClose);
                _popinCloseList.splice(i, 1);
                if (_popinCloseList.length == 0) {
                    $(document.body).unbind("keydown", popinKeyClose);
                    $(document.body).unbind("beforeclick", popinBeforeClick);
                }
                return;
            }
        }
    }
    WebUI.popinBeforeClick = popinBeforeClick;
    function popinMouseClose() {
        if (WebUI.isUIBlocked())
            return;
        try {
            for (var i = 0; i < _popinCloseList.length; i++) {
                var id = _popinCloseList[i];
                var el = $(id);
                if (el) {
                    el.unbind("mousedown", popinMouseClose);
                    WebUI.scall(id.substring(1), "POPINCLOSE?", {});
                }
            }
        }
        finally {
            _popinCloseList = [];
            $(document.body).unbind("keydown", popinKeyClose);
            $(document.body).unbind("beforeclick", popinBeforeClick);
        }
    }
    WebUI.popinMouseClose = popinMouseClose;
    function popinKeyClose(evt) {
        if (!evt)
            evt = window.event;
        var kk = WebUI.normalizeKey(evt);
        if (kk == 27 || kk == 27000) {
            evt.preventDefault();
            evt.cancelBubble = true;
            if (evt.stopPropagation)
                evt.stopPropagation();
            popinMouseClose();
        }
    }
    WebUI.popinKeyClose = popinKeyClose;
    function dataTableResults(id, compId) {
        setTimeout(function (a) {
            $('#' + id).colResizable({
                postbackSafe: false,
                resizeMode: 'flex',
                onResize: function (tbl) {
                    WebUI.dataTableUpdateWidths(tbl, compId);
                }
            });
        }, 500);
    }
    WebUI.dataTableResults = dataTableResults;
    function dataTableUpdateWidths(evt, compId) {
        var tbl = evt.currentTarget;
        var hdrs = $(tbl).find(".ui-dt-th");
        var list = {};
        for (var i = 0; i < hdrs.length; i++) {
            var wid = hdrs[i].style.width;
            list["column_" + hdrs[i].id] = hdrs[i].style.width;
        }
        WebUI.scall(compId, "COLWIDTHS", list);
        console.log("Change event", tbl);
    }
    WebUI.dataTableUpdateWidths = dataTableUpdateWidths;
    var _ckEditorMap = {};
    function registerCkEditorId(id, ckeInstance) {
        _ckEditorMap[id] = [ckeInstance, null];
    }
    WebUI.registerCkEditorId = registerCkEditorId;
    function unregisterCkEditorId(id) {
        try {
            var editorBindings = _ckEditorMap[id];
            _ckEditorMap[id] = null;
            if (editorBindings && editorBindings[1]) {
                $(window).unbind('resize', editorBindings[1]);
            }
        }
        catch (ex) {
            WebUI.log('error in unregisterCkEditorId: ' + ex);
        }
    }
    WebUI.unregisterCkEditorId = unregisterCkEditorId;
    function CKeditor_OnComplete(id) {
        WebUI.doCustomUpdates();
        var elem = document.getElementById(id);
        var parentDiv = elem.parentNode;
        var editor = _ckEditorMap[id][0];
        var resizeFunction = function (ev) {
            try {
                editor.resize($(parentDiv).width() - 2, $(parentDiv).height());
            }
            catch (ex) {
                WebUI.log('error in CKeditor_OnComplete#resizeFunction: ' + ex);
            }
        };
        _ckEditorMap[id] = [editor, resizeFunction];
        $(window).bind('resize', resizeFunction);
        $(window).trigger('resize');
    }
    WebUI.CKeditor_OnComplete = CKeditor_OnComplete;
    function initAutocomplete(inputId, selectId) {
        var input = document.getElementById(inputId);
        var select = document.getElementById(selectId);
        $(input).keyup(function (event) {
            autocomplete(event, inputId, selectId);
        });
        $(select).keypress(function (event) {
            var keyCode = WebUI.normalizeKey(event);
            if (keyCode == 27 || keyCode == 27000) {
                var oldVal = input.value;
                var selectOnClick = select.click;
                var selectOnBlur = select.blur;
                select.click = null;
                select.blur = null;
                select.style.display = 'none';
                input.focus();
                input.value = oldVal;
                select.click = selectOnClick;
                select.blur = selectOnBlur;
            }
        });
    }
    WebUI.initAutocomplete = initAutocomplete;
    function autocomplete(event, inputId, selectId) {
        var select = document.getElementById(selectId);
        var cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
        if (cursorKeys.indexOf(event.keyCode + ";") == -1) {
            var input = document.getElementById(inputId);
            var found = false;
            var foundAtIndex = -1;
            for (var i = 0; i < select.options.length; i++) {
                if ((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)) {
                    foundAtIndex = i;
                    break;
                }
            }
            select.selectedIndex = foundAtIndex;
            var oldValue = input.value;
            var newValue = found ? select.options[foundAtIndex].text : oldValue;
            if (newValue != oldValue) {
                if (typeof input.selectionStart != "undefined") {
                    input.value = newValue;
                    input.selectionStart = oldValue.length;
                    input.selectionEnd = newValue.length;
                    input.focus();
                }
                var dsel = document.selection;
                if (dsel && dsel.createRange) {
                    input.value = newValue;
                    input.focus();
                    input.select();
                    var range = dsel.createRange();
                    range.collapse(true);
                    range.moveStart("character", oldValue.length);
                    range.moveEnd("character", newValue.length);
                    range.select();
                }
                else if (input.createTextRange) {
                    input.value = newValue;
                    var rNew = input.createTextRange();
                    rNew.moveStart('character', oldValue.length);
                    rNew.select();
                }
            }
        }
        else if (event.keyCode == 40) {
            select.style.display = 'inline';
            select.focus();
        }
    }
    WebUI.autocomplete = autocomplete;
    function colorPickerButton(btnid, inid, value, onchange) {
        $(btnid).ColorPicker({
            color: '#' + value,
            onShow: function (colpkr) {
                $(colpkr).fadeIn(500);
                return false;
            },
            onHide: function (colpkr) {
                $(colpkr).fadeOut(500);
                return false;
            },
            onChange: function (hsb, hex, rgb) {
                $(btnid + ' div').css('backgroundColor', '#' + hex);
                $(inid).val(hex);
                if (onchange)
                    colorPickerOnchange(btnid, hex);
            }
        });
    }
    WebUI.colorPickerButton = colorPickerButton;
    function colorPickerInput(inid, divid, value, onchange) {
        $(inid).ColorPicker({
            color: '#' + value,
            flat: false,
            onShow: function (colpkr) {
                $(colpkr).fadeIn(500);
                return false;
            },
            onHide: function (colpkr) {
                $(colpkr).fadeOut(500);
                return false;
            },
            onBeforeShow: function () {
                $(this).ColorPickerSetColor(this.value);
            },
            onChange: function (hsb, hex, rgb) {
                $(divid).css('backgroundColor', '#' + hex);
                $(inid).val(hex);
                if (onchange)
                    colorPickerOnchange(inid, hex);
            }
        });
    }
    WebUI.colorPickerInput = colorPickerInput;
    function colorPickerDisable(id) {
    }
    WebUI.colorPickerDisable = colorPickerDisable;
    var _colorLast;
    var _colorTimer;
    var _colorLastID;
    function colorPickerOnchange(id, last) {
        if (_colorLast == last && _colorLastID == id)
            return;
        if (_colorTimer) {
            window.clearTimeout(_colorTimer);
            _colorTimer = undefined;
        }
        _colorLastID = id;
        _colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
    }
    WebUI.colorPickerOnchange = colorPickerOnchange;
    function colorPickerChangeEvent(id) {
        window.clearTimeout(_colorTimer);
        _colorTimer = undefined;
        WebUI.valuechanged('eh', id);
    }
    WebUI.colorPickerChangeEvent = colorPickerChangeEvent;
    function flare(id) {
        $('#' + id).fadeIn('fast', function () {
            $('#' + id).delay(500).fadeOut(1000, function () {
                $('#' + id).remove();
            });
        });
    }
    WebUI.flare = flare;
    function flareStay(id) {
        $('#' + id).fadeIn('fast', function () {
            $('body,html').bind('mousemove.' + id, function (e) {
                $('body,html').unbind('mousemove.' + id);
                $('#' + id).delay(500).fadeOut(1000, function () {
                    $('#' + id).remove();
                });
            });
        });
    }
    WebUI.flareStay = flareStay;
    function flareStayCustom(id, delay, fadeOut) {
        $('#' + id).fadeIn('fast', function () {
            $('body,html').bind('mousemove.' + id, function (e) {
                $('body,html').unbind('mousemove.' + id);
                $('#' + id).delay(delay).fadeOut(fadeOut, function () {
                    $('#' + id).remove();
                });
            });
        });
    }
    WebUI.flareStayCustom = flareStayCustom;
    function FCKeditor_OnComplete(editorInstance) {
        if (WebUI.isIE8orNewer()) {
            var _loop_1 = function (i) {
                var fckId = _fckEditorIDs[i];
                var fckIFrame = document.getElementById(fckId + '___Frame');
                if (fckIFrame) {
                    $(fckIFrame.contentWindow.window).bind('resize', function () {
                        FCKeditor_fixLayout(fckIFrame, fckId);
                    });
                    $(fckIFrame.contentWindow.window).trigger('resize');
                }
            };
            for (var i = 0; i < _fckEditorIDs.length; i++) {
                _loop_1(i);
            }
        }
    }
    WebUI.FCKeditor_OnComplete = FCKeditor_OnComplete;
    function initScrollableTableOld(id) {
        $('#' + id + " table").fixedHeaderTable({});
        var sbody = $('#' + id + " .fht-tbody");
        sbody.scroll(function () {
            var bh = $(sbody).height();
            var st = $(sbody).scrollTop();
            var tbl = $('#' + id + " .fht-table tbody");
            var th = tbl.height();
            var left = tbl.height() - bh - st;
            if (left > 100) {
                return;
            }
            var lastRec = sbody.find("tr[lastRow]");
            if (lastRec.length != 0) {
                return;
            }
            WebUI.scall(id, "LOADMORE", {});
        });
    }
    WebUI.initScrollableTableOld = initScrollableTableOld;
    function scrollableTableReset(id, tblid) {
        var tbl = $('#' + tblid);
        var container = $('#' + id);
        tbl.floatThead('reflow');
        WebUI.doCustomUpdates();
        $.dbg('recreate');
        container.scrollTop(0);
    }
    WebUI.scrollableTableReset = scrollableTableReset;
    function initScrollableTable(id, tblid) {
        var container = $('#' + id);
        var tbl = $('#' + tblid);
        WebUI.doCustomUpdates();
        tbl.floatThead({
            scrollContainer: function () {
                return container;
            },
            getSizingRow: function ($table) {
                var rows = $table.find('tbody tr:visible').get();
                for (var i = 0; i < rows.length; i++) {
                    var cells = $(rows[i]).find('td');
                    var isInvalidRow = false;
                    for (var i_1 = 0; i_1 < cells.get().length; i_1++) {
                        if (Number($(cells[i_1]).attr('colspan')) > 1) {
                            isInvalidRow = true;
                        }
                    }
                    if (!isInvalidRow) {
                        return cells;
                    }
                }
                if (rows.length > 0) {
                    return $(rows[0]).find('td');
                }
                else {
                    return null;
                }
            }
        });
        container.scroll(function () {
            var bh = $(container).height();
            var st = $(container).scrollTop();
            var tbl = $('#' + id + " tbody");
            var th = tbl.height();
            var left = tbl.height() - bh - st;
            $.dbg("scrolling: bodyheight=" + bh + " scrolltop=" + st + " tableheight=" + th + " left=" + left);
            if (left > 100) {
                return;
            }
            var lastRec = tbl.find("tr[lastRow]");
            if (lastRec.length != 0) {
                return;
            }
            WebUI.scall(id, "LOADMORE", {});
        });
    }
    WebUI.initScrollableTable = initScrollableTable;
    var closeOnClick = (function () {
        function closeOnClick(id) {
            this._id = id;
            var clickHandler = this._clickHandler = $.proxy(this.closeMenu, this);
            $(document).click(clickHandler);
            var keyUpHandler = this._keyUpHandler = $.proxy(this.buttonHandler, this);
            $(document).keyup(keyUpHandler);
            $('#' + id).data('inst', this);
        }
        closeOnClick.prototype.closeMenu = function () {
            this.unbind();
            WebUI.scall(this._id, "CLOSEMENU?", {});
        };
        closeOnClick.prototype.unbind = function () {
            $(document).unbind("click", this._clickHandler);
            $(document).unbind("keyup", this._keyUpHandler);
        };
        closeOnClick.prototype.markClosed = function (id) {
            var inst = $('#' + id).data('inst');
            if (inst) {
                inst.unbind();
            }
        };
        closeOnClick.prototype.isInputTagEvent = function (event) {
            var src = event.srcElement;
            if (src) {
                var tn = src.tagName.toUpperCase();
                if (tn === 'INPUT' || tn == 'SELECT' || tn == "TEXTAREA")
                    return true;
            }
            return false;
        };
        closeOnClick.prototype.buttonHandler = function (event) {
            if (this.isInputTagEvent(event))
                return;
            if (event.which == 27) {
                this.closeMenu();
            }
        };
        return closeOnClick;
    }());
    var DbPerformance;
    (function (DbPerformance) {
        function post(id, sessionid) {
            $(document).ready(function () {
                setTimeout(function () {
                    $.get(window.DomUIappURL + "nl.itris.vp.parts.DbPerf.part?requestid=" + sessionid, function (data) {
                        $('#' + id).html(data);
                        $(".vp-lspf").draggable({ ghosting: false, zIndex: 100, handle: '.vp-lspf-ttl' });
                        $(".vp-lspf-close").click(function () {
                            $(".vp-lspf").hide();
                        });
                    });
                }, 500);
            });
        }
        DbPerformance.post = post;
    })(DbPerformance || (DbPerformance = {}));
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    function returnKeyPress(evt, node) {
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
    WebUI.returnKeyPress = returnKeyPress;
    function wtMouseDown(e) {
        alert(e);
    }
    WebUI.wtMouseDown = wtMouseDown;
    function alignTopToBottom(nodeId, alignToId, offsetY, doCallback) {
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
    WebUI.alignTopToBottom = alignTopToBottom;
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
    WebUI.alignToTop = alignToTop;
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
    WebUI.alignToLeft = alignToLeft;
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
    WebUI.alignToRight = alignToRight;
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
    WebUI.alignToMiddle = alignToMiddle;
    function copyTextToClipboard(text) {
        var textArea = document.createElement("textarea");
        textArea.style.position = 'fixed';
        textArea.style.top = '0';
        textArea.style.left = '0';
        textArea.style.width = '2em';
        textArea.style.height = '2em';
        textArea.style.padding = '0';
        textArea.style.border = 'none';
        textArea.style.outline = 'none';
        textArea.style.boxShadow = 'none';
        textArea.style.background = 'transparent';
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        try {
            var successful = document.execCommand('copy');
            var msg = successful ? 'successful' : 'unsuccessful';
            console.log('Copying text command was ' + msg);
        }
        catch (err) {
            console.log('Oops, unable to copy');
        }
        document.body.removeChild(textArea);
    }
    WebUI.copyTextToClipboard = copyTextToClipboard;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    function handleCalendarChanges() {
        var cals = $("div.ui-wa").get();
        for (var i = cals.length; --i >= 0;)
            handleCalendar(cals[i]);
    }
    WebUI.handleCalendarChanges = handleCalendarChanges;
    function handleCalendar(caldiv) {
        var cal = new Agenda(caldiv);
        cal.loadLayout();
        cal.reposition();
    }
    WebUI.handleCalendar = handleCalendar;
    var Agenda = (function () {
        function Agenda(div) {
            this._rootdiv = div;
            this._dragMode = 0;
            this._rounding = 15;
            var cal = this;
            div.onmousedown = function (e) {
                cal.timeDown(e);
            };
            div.onmousemove = function (e) {
                cal.timeMove(e);
            };
            div.onmouseup = function (e) {
                cal.timeUp(e);
            };
        }
        Agenda.prototype.decodeDate = function (s) {
            var ar = s.split(",");
            if (ar.length != 5)
                alert('Invalid date input: ' + s);
            var d = new Date(parseInt(ar[0]), parseInt(ar[1]) - 1, parseInt(ar[2]), parseInt(ar[3]), parseInt(ar[4]), 0);
            return d;
        };
        Agenda.prototype.loadLayout = function () {
            var caldiv = this._rootdiv;
            this._date = this.decodeDate(caldiv.getAttribute('startDate'));
            this._days = parseInt(caldiv.getAttribute('days'));
            this._startHour = parseInt(caldiv.getAttribute('hourstart'));
            this._endHour = parseInt(caldiv.getAttribute('hourend'));
            this._maxMinutes = (this._endHour - this._startHour) * 60;
            var tblheight;
            var tbl;
            if ($.browser.msie) {
                tbl = $("table.ui-wa-bgtbl", this._rootdiv).get()[0];
                tblheight = tbl.clientHeight;
                tbl = $(".ui-wa-bgtbl tbody", this._rootdiv).get()[0];
            }
            else {
                tbl = $(".ui-wa-bgtbl tbody", this._rootdiv).get()[0];
                tblheight = tbl.clientHeight;
            }
            var tr = undefined;
            for (var i = 0; i < tbl.childNodes.length; i++) {
                tr = tbl.childNodes[i];
                if (tr.tagName == 'tr' || tr.tagName == 'TR')
                    break;
            }
            var td = undefined;
            var ix = 0;
            for (var i = 0; i < tr.childNodes.length; i++) {
                td = tr.childNodes[i];
                if (td.tagName == 'td' || td.tagName == 'TD') {
                    if (ix == 0) {
                        this._headerHeight = tr.clientHeight + 1;
                        this._gutterWidth = td.clientWidth + 1;
                    }
                    else if (ix == 1) {
                        this._cellWidth = td.clientWidth + 1;
                    }
                    else
                        break;
                    ix++;
                }
            }
            this._pxPerHour = (tblheight - this._headerHeight + 1) / (this._endHour - this._startHour);
            this._endDate = new Date(this._date.getTime());
            this._endDate.setDate(this._endDate.getDate() + this._days);
            this._dayMap = [];
            this._itemdivs = $("div.ui-wa-it", this._rootdiv).get();
        };
        Agenda.prototype.reposition = function () {
            for (var i = this._itemdivs.length; --i >= 0;) {
                var idiv = this._itemdivs[i];
                var sd = this.decodeDate(idiv.getAttribute('startdate'));
                var ed = this.decodeDate(idiv.getAttribute('enddate'));
                this.assignDayAndLane(idiv, sd, ed);
            }
            var dayxo = this._gutterWidth;
            for (var i = 0; i < this._dayMap.length; i++) {
                var day = this._dayMap[i];
                if (day == undefined)
                    continue;
                var maxlanes = day.ways.length;
                if (maxlanes == 0)
                    continue;
                for (var wayix = 0; wayix < maxlanes; wayix++) {
                    var way = day.ways[wayix];
                    var wxo = dayxo + wayix * (this._cellWidth / maxlanes);
                    for (var iix = 0; iix < way.length; iix++) {
                        var item = way[iix];
                        var spanlanes = this.calcSpanLanes(day, wayix + 1, item);
                        var width = this._cellWidth / maxlanes * spanlanes;
                        var d = item.div;
                        d.style.position = "absolute";
                        d.style.top = (item.ys + this._headerHeight) + "px";
                        d.style.left = wxo + "px";
                        d.style.height = (item.ye - item.ys) + "px";
                        d.style.width = (width - 2) + "px";
                        d.style.display = 'block';
                    }
                }
                dayxo += this._cellWidth;
            }
        };
        Agenda.prototype.assignDayAndLane = function (idiv, sd, ed) {
            var so = this.calcMinuteOffset(sd, 1);
            var eo = this.calcMinuteOffset(ed, -1);
            var day = this._dayMap[so.day];
            if (day == undefined)
                day = this._dayMap[so.day] = { day: so.day, ways: [[]] };
            var ys = Math.round(so.min * this._pxPerHour / 60);
            var ye = Math.round(eo.min * this._pxPerHour / 60);
            var item = {};
            item.day = so.day;
            item.ys = ys;
            item.ye = ye;
            item.div = idiv;
            for (var i = 0; i < 4; i++) {
                var way = day.ways[i];
                if (way == undefined)
                    way = day.ways[i] = [];
                if (this.placeOnWay(way, item))
                    return;
            }
            day.ways.push(item);
        };
        Agenda.prototype.calcSpanLanes = function (day, start, item) {
            var nways = 1;
            for (var i = start; i < day.ways.length; i++) {
                var way = day.ways[i];
                for (var j = way.length; --j >= 0;) {
                    var oi = way[j];
                    if (this.calItemOverlaps(item, oi))
                        return nways;
                }
                nways++;
            }
            return nways;
        };
        Agenda.prototype.placeOnWay = function (way, item) {
            for (var i = way.length; --i >= 0;) {
                var oi = way[i];
                if (this.calItemOverlaps(item, oi))
                    return false;
            }
            way.push(item);
            return true;
        };
        Agenda.prototype.calItemOverlaps = function (i1, i2) {
            return i1.ys < i2.ye && i1.ye > i2.ys;
        };
        Agenda.prototype.calcMinuteOffset = function (d, grav) {
            var ts = d.getTime();
            if (ts <= this._date.getTime())
                return { day: 0, min: 0 };
            if (ts >= this._endDate.getTime())
                return { day: (this._days - 1), min: this._maxMinutes };
            var dayoff = Math.floor((ts - this._date.getTime()) / (86400000));
            var mins = 0;
            var h = d.getHours();
            if (h < this._startHour) {
                if (grav > 0)
                    mins = 0;
                else {
                    if (dayoff == 0)
                        mins = 0;
                    else {
                        dayoff--;
                        mins = (this._endHour - this._startHour) * 60;
                    }
                }
            }
            else if (h >= this._endHour) {
                if (grav > 0) {
                    if (dayoff + 1 >= this._days) {
                        mins = this._maxMinutes;
                    }
                    else {
                        dayoff++;
                        mins = 0;
                    }
                }
                else {
                    mins = (this._endHour - this._startHour) * 60;
                }
            }
            else {
                h -= this._startHour;
                mins = h * 60 + d.getMinutes();
            }
            return { day: dayoff, min: mins };
        };
        Agenda.prototype.destroyNode = function (x) {
            $(x).remove();
        };
        Agenda.prototype.timeDown = function (e) {
            this.timeReset();
            this._timeStart = this.fixPosition(e);
            this._timeMode = 1;
        };
        Agenda.prototype.timeReset = function () {
            if (this._timeDiv) {
                this.destroyNode(this._timeDiv);
                this._timeDiv = undefined;
            }
            this._timeMode = 0;
        };
        Agenda.prototype.timeUp = function (e) {
            if (this._dragMode && this._dragMode > 0) {
                return;
            }
            if (this._timeMode && this._timeMode > 0) {
                this.timeReset();
                var fields = {};
                fields.date = this._timeDate.getTime();
                fields.duration = this._timeDuration;
                WebUI.scall(this._rootdiv.id, 'newappt', fields);
                return;
            }
            this.timeReset();
        };
        Agenda.prototype.roundOff = function (min) {
            return Math.round(min / this._rounding) * this._rounding;
        };
        Agenda.prototype.timeMove = function (e) {
            if (this._dragMode && this._dragMode > 0) {
                return;
            }
            if (!this._timeMode || this._timeMode < 1)
                return;
            var cloc = this.fixPosition(e);
            var dy = Math.abs(cloc.y - this._timeStart.y);
            var dx = Math.abs(cloc.x - this._timeStart.x);
            if (dx < 4 && dy < 4)
                return;
            var sy = this._timeStart.y;
            var ey = cloc.y;
            if (sy > ey) {
                ey = this._timeStart.y;
                sy = cloc.y;
            }
            var dy = ey - sy;
            var day = Math.floor((cloc.x - this._gutterWidth) / this._cellWidth);
            var xo = this._gutterWidth + (day * this._cellWidth);
            var to = (sy - this._headerHeight) / this._pxPerHour * 60;
            to = this.roundOff(to);
            sy = Math.floor(to * this._pxPerHour / 60) + this._headerHeight;
            to *= 60 * 1000;
            to += ((24 * day) + this._startHour) * 60 * 60 * 1000;
            to += this._date.getTime();
            this._timeDate = new Date(to);
            var dur = dy * 60 / this._pxPerHour;
            dur = this.roundOff(dur);
            if (dur < this._rounding)
                dur = this._rounding;
            ey = sy + Math.floor(dur * this._pxPerHour / 60);
            dur *= 60 * 1000;
            this._timeDuration = dur;
            if (!this._timeDiv) {
                var d = document.createElement('div');
                this._rootdiv.appendChild(d);
                d.className = "ui-wa-nt";
                this._timeDiv = d;
                d.style.position = "absolute";
                d.style.width = (this._cellWidth - 2) + "px";
                d.style.zIndex = "90";
            }
            this._timeDiv.style.top = sy + "px";
            this._timeDiv.style.height = (ey - sy) + "px";
            this._timeDiv.style.left = xo + "px";
        };
        Agenda.prototype.fixPosition = function (e) {
            var p = WebUI.getAbsolutePosition(this._rootdiv);
            return { x: e.clientX - p.x, y: e.clientY - p.y };
        };
        return Agenda;
    }());
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    function dateInputCheckInput(evt) {
        if (!evt) {
            evt = window.event;
            if (!evt) {
                return;
            }
        }
        var c = evt.target || evt.srcElement;
        dateInputRepairValueIn(c);
    }
    WebUI.dateInputCheckInput = dateInputCheckInput;
    function dateInputRepairValueIn(c) {
        if (!c)
            return;
        var val = c.value;
        if (!val || val.length == 0)
            return;
        Calendar.__init();
        var pos = val.indexOf(' ');
        var timeval = null;
        if (pos != -1) {
            timeval = $.trim(val.substring(pos + 1));
            val = $.trim(val.substring(0, pos));
        }
        try {
            val = $.trim(val);
            val = val.replace(new RegExp("\\" + Calendar._TT["DATE_TIME_SEPARATOR"] + "+"), Calendar._TT["DATE_TIME_SEPARATOR"]);
            var numbereOfSpaces = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]).length - 1;
            var res = void 0;
            if (numbereOfSpaces == 0) {
                res = dateInputRepairDateValue(val);
            }
            else if (numbereOfSpaces == 1) {
                res = dateInputRepairDateTimeValue(val);
            }
            else {
                throw "date invalid";
            }
            c.value = res;
        }
        catch (x) {
            alert(Calendar._TT["INVALID"]);
        }
    }
    WebUI.dateInputRepairValueIn = dateInputRepairValueIn;
    function hasFieldInvalidFormat(inputValue) {
        var MAX_LENGTH = 2;
        var FORBIDDEN_CHARACTER = "0";
        return (inputValue.length === MAX_LENGTH && (inputValue.charAt(0) === FORBIDDEN_CHARACTER)) || (inputValue.length > MAX_LENGTH);
    }
    WebUI.hasFieldInvalidFormat = hasFieldInvalidFormat;
    function setDayOrMonthFormat(inputValue, result) {
        var NEEDED_CHARACTER_DAY_MONTH = "0";
        if (inputValue.length == 1) {
            result += NEEDED_CHARACTER_DAY_MONTH + inputValue;
        }
        else {
            result += inputValue;
        }
        return result;
    }
    WebUI.setDayOrMonthFormat = setDayOrMonthFormat;
    function setYearFormat(inputValue, result) {
        var NEEDED_CHARACTER_YEAR = "20";
        return result += NEEDED_CHARACTER_YEAR + inputValue;
    }
    WebUI.setYearFormat = setYearFormat;
    function showCalendar(id, withtime) {
        var inp = document.getElementById(id);
        var params = {
            inputField: inp,
            eventName: 'click',
            ifFormat: Calendar._TT[withtime ? "DEF_DATETIME_FORMAT"
                : "DEF_DATE_FORMAT"],
            daFormat: Calendar._TT["TT_DATE_FORMAT"],
            singleClick: true,
            align: 'Br',
            range: [1900, 2999],
            weekNumbers: true,
            showsTime: withtime,
            timeFormat: "24",
            electric: false,
            step: 2,
            position: null,
            cache: false
        };
        var dateFmt = params.inputField ? params.ifFormat : params.daFormat;
        params.date = Date.parseDate(inp.value, dateFmt);
        var cal = new Calendar(1, params.date, onDateSelect, function (cal) {
            cal.hide();
            cal.destroy();
        });
        cal.showsOtherMonths = false;
        cal.showsTime = withtime;
        cal.time24 = true;
        cal.params = params;
        cal.weekNumbers = true;
        cal.setRange(params.range[0], params.range[1]);
        cal.create();
        cal.refresh();
        if (!params.position)
            cal.showAtElement(params.inputField, params.align);
        else
            cal.showAt(params.position[0], params.position[1]);
    }
    WebUI.showCalendar = showCalendar;
    function onDateSelect(cal) {
        var p = cal.params;
        var update = (cal.dateClicked || p.electric);
        if (update && p.inputField) {
            p.inputField.value = cal.date.print(p.ifFormat);
            if (typeof p.inputField.onchange == "function" && cal.dateClicked)
                p.inputField.onchange();
        }
        if (update && p.displayArea)
            p.displayArea.innerHTML = cal.date.print(p.daFormat);
        if (update && typeof p.onUpdate == "function")
            p.onUpdate(cal);
        if (update && p.flat) {
            if (typeof p.flatCallback == "function")
                p.flatCallback(cal);
        }
        if (update && p.singleClick && cal.dateClicked)
            cal.callCloseHandler();
    }
    WebUI.onDateSelect = onDateSelect;
    function dateInputCheck(evt) {
        if (!evt) {
            evt = window.event;
            if (!evt) {
                return;
            }
        }
        var c = evt.target || evt.srcElement;
        dateInputRepairValueIn(c);
    }
    WebUI.dateInputCheck = dateInputCheck;
    function dateInputRepairDateValue(val) {
        var fmt = Calendar._TT["DEF_DATE_FORMAT"];
        var separatorsCount = countSeparators(val);
        if (separatorsCount < 2) {
            val = insertDateSeparators(val, fmt, separatorsCount);
        }
        var res = Date.parseDate(val, fmt);
        if (!isYearInSupportedRange(res))
            throw "date invalid - distant year";
        return res.print(fmt);
    }
    WebUI.dateInputRepairDateValue = dateInputRepairDateValue;
    function dateInputRepairTimeValue(val) {
        var fmt = Calendar._TT["DEF_TIME_FORMAT"];
        var tempSep = "~";
        var count = getTimeSeparatorCount(val);
        switch (count) {
            default:
                throw "time has multiple separators";
            case 0:
                var placeForSeparator = val.length - 2;
                val = [val.slice(0, placeForSeparator), tempSep, val.slice(placeForSeparator)].join('');
            case 1:
                var re = new RegExp("[" + Calendar._TT["TIME_SEPARATOR"] + "]", "g");
                val = val.replace(re, '~');
        }
        var dummyDate = new Date();
        dummyDate.setHours(val.split(tempSep)[0], val.split(tempSep)[1], 0, 0);
        return dummyDate.print(fmt);
    }
    WebUI.dateInputRepairTimeValue = dateInputRepairTimeValue;
    function getTimeSeparatorCount(time) {
        var supportedTimeSeparators = Calendar._TT["TIME_SEPARATOR"];
        return (time.match(new RegExp("[" + supportedTimeSeparators + "]", "g")) || []).length;
    }
    WebUI.getTimeSeparatorCount = getTimeSeparatorCount;
    function dateInputRepairDateTimeValue(val) {
        var fmt = Calendar._TT["DEF_DATETIME_FORMAT"];
        var parts = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]);
        var datePart = dateInputRepairDateValue(parts[0]);
        var timePart = dateInputRepairTimeValue(parts[1]);
        val = datePart + Calendar._TT["DATE_TIME_SEPARATOR"] + timePart;
        var res = Date.parseDate(val, fmt);
        return res.print(fmt);
    }
    WebUI.dateInputRepairDateTimeValue = dateInputRepairDateTimeValue;
    function countSeparators(str) {
        var count = 0;
        for (var i = str.length; --i >= 0;) {
            if (isDateSeparator(str.charAt(i)))
                count++;
        }
        return count;
    }
    WebUI.countSeparators = countSeparators;
    function isDateSeparator(c) {
        return Calendar._TT["DATE_SEPARATOR"].indexOf(c) > -1;
    }
    WebUI.isDateSeparator = isDateSeparator;
    function insertDateSeparators(str, fmt, separatorsCount) {
        var b = fmt.match(/%./g);
        var len = str.length;
        var ylen;
        if (len == 8)
            ylen = 4;
        else if (len == 6)
            ylen = 2;
        else if (len >= 3 && len <= 5)
            ylen = 0;
        else
            throw "date invalid";
        if (separatorsCount == 1) {
            var index = 0;
            while (!isDateSeparator(str.charAt(index))) {
                index++;
                if (index > len - 1) {
                    throw "invalid state";
                }
            }
            str = str.substring(0, index) + '-' + str.substring(index + 1);
        }
        var res = "";
        for (var fix = 0; fix < b.length; fix++) {
            if (res.length != 0 && str.length != 0)
                res = res + '-';
            switch (b[fix]) {
                default:
                    throw "date invalid";
                case "%d":
                case "%e":
                case "%m":
                    var dashIndex = str.indexOf('-');
                    var index = dashIndex == -1 ? 2 : dashIndex;
                    var indexNext = dashIndex == -1 ? 2 : dashIndex + 1;
                    res += str.substring(0, index);
                    str = str.substring(indexNext);
                    break;
                case '%y':
                case '%Y':
                    res += str.substring(0, ylen);
                    str = str.substring(ylen);
                    break;
            }
        }
        return res;
    }
    WebUI.insertDateSeparators = insertDateSeparators;
    function isYearInSupportedRange(date) {
        if (date.getFullYear() < Calendar._TT["MIN_YEAR"] || date.getFullYear() > Calendar._TT["MAX_YEAR"]) {
            return false;
        }
        else {
            return true;
        }
    }
    WebUI.isYearInSupportedRange = isYearInSupportedRange;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _dragType;
    var _dragMode;
    var _dragNode;
    var _dragCopy;
    var _dragSourceOffset;
    var _dragLastX;
    var _dragLastY;
    var _dragTimer;
    var _currentDropZone;
    var _dropRowIndex;
    var _dropRow;
    function dragMouseDown(item, evt) {
        dragReset();
        _dragType = item.getAttribute('uitype');
        if (!_dragType)
            alert("This DRAGGABLE node has no 'uitype' attribute??");
        var dragAreaId = item.getAttribute('dragarea');
        if (dragAreaId) {
            _dragNode = document.getElementById(dragAreaId);
        }
        else
            _dragNode = item;
        _dragMode = 1;
        $(document.body).bind("mousemove", dragMouseMove);
        $(document.body).bind("mouseup", dragMouseUp);
        var apos = WebUI.getAbsolutePosition(item);
        _dragSourceOffset = apos;
        apos.x = evt.clientX - apos.x;
        apos.y = evt.clientY - apos.y;
        if (evt.preventDefault)
            evt.preventDefault();
        else {
            evt.returnValue = false;
        }
        if (document.attachEvent) {
            document.attachEvent("onselectstart", WebUI.preventSelection);
        }
    }
    WebUI.dragMouseDown = dragMouseDown;
    function dragMouseUp() {
        try {
            if (_dragMode == 2) {
                dragClearTimer();
                var dz = dropTargetFind(_dragLastX, _dragLastY);
                if (dz) {
                    dropClearZone();
                    dz._drophandler.drop(dz);
                }
                else {
                    _dragNode.style.display = '';
                }
            }
        }
        finally {
            dragReset();
        }
    }
    WebUI.dragMouseUp = dragMouseUp;
    function dragMouseMove(e) {
        if (_dragMode == 0) {
            dragReset();
            return;
        }
        if (_dragMode == 1) {
            _dragCopy = dragCreateCopy(_dragNode);
            _dragNode.style.display = 'none';
            _dragMode = 2;
            document.body.appendChild(_dragCopy);
        }
        _dragCopy.style.top = (e.clientY - _dragSourceOffset.y) + "px";
        _dragCopy.style.left = (e.clientX - _dragSourceOffset.x) + "px";
        _dragLastX = e.clientX;
        _dragLastY = e.clientY;
        dragResetTimer();
    }
    WebUI.dragMouseMove = dragMouseMove;
    function dragCreateCopy(source) {
        var dv = document.createElement('div');
        if (source.tagName != "TR") {
            dv.innerHTML = source.innerHTML;
        }
        else {
            var t = document.createElement('table');
            dv.appendChild(t);
            var b = document.createElement('tbody');
            t.appendChild(b);
            b.innerHTML = source.innerHTML;
            var dad = WebUI.findParentOfTagName(source, 'TABLE');
            if (dad) {
                t.className = dad.className;
            }
        }
        dv.style.position = 'absolute';
        dv.style.width = $(source).width() + "px";
        dv.style.height = $(source).height() + "px";
        return dv;
    }
    WebUI.dragCreateCopy = dragCreateCopy;
    function dragResetTimer() {
        dragClearTimer();
        _dragTimer = setTimeout("WebUI.dragTimerFired()", 250);
    }
    WebUI.dragResetTimer = dragResetTimer;
    function dragClearTimer() {
        if (_dragTimer) {
            clearTimeout(_dragTimer);
            _dragTimer = undefined;
        }
    }
    WebUI.dragClearTimer = dragClearTimer;
    function dragTimerFired() {
        var dz = dropTargetFind(_dragLastX, _dragLastY);
        if (!dz) {
            dropClearZone();
            return;
        }
        if (dz == _currentDropZone) {
            dz._drophandler.checkRerender(dz);
            return;
        }
        dropClearZone();
        _currentDropZone = dz;
        dz._drophandler.hover(dz);
    }
    WebUI.dragTimerFired = dragTimerFired;
    function findDropZoneHandler(type) {
        if (type == "ROW")
            return _ROW_DROPZONE_HANDLER;
        return _DEFAULT_DROPZONE_HANDLER;
    }
    WebUI.findDropZoneHandler = findDropZoneHandler;
    function dropClearZone() {
        if (_currentDropZone) {
            _currentDropZone._drophandler.unmark(_currentDropZone);
            _currentDropZone = undefined;
        }
    }
    WebUI.dropClearZone = dropClearZone;
    function dragReset() {
        dragClearTimer();
        if (_dragCopy) {
            $(_dragCopy).remove();
            _dragCopy = null;
        }
        if (_dragNode) {
            $(document.body).unbind("mousemove", dragMouseMove);
            $(document.body).unbind("mouseup", dragMouseUp);
            _dragNode = null;
        }
        dropClearZone();
        _dragMode = 0;
        if (document.detachEvent) {
            document.detachEvent("onselectstart", WebUI.preventSelection);
        }
    }
    WebUI.dragReset = dragReset;
    var DropInfo = (function () {
        function DropInfo() {
        }
        return DropInfo;
    }());
    var _dropList;
    function dropGetList() {
        if (_dropList)
            return _dropList;
        var dl = $(".ui-drpbl").get();
        _dropList = [];
        for (var i = dl.length; --i >= 0;) {
            var drop = dl[i];
            var types = drop.getAttribute('uitypes');
            if (!types)
                continue;
            var def = new DropInfo();
            def._dropTarget = drop;
            def._position = WebUI.getAbsolutePosition(drop);
            def._width = drop.clientWidth;
            def._height = drop.clientHeight;
            var tar = types.split(",");
            def._types = tar;
            def._drophandler = findDropZoneHandler(drop.getAttribute('uidropmode'));
            var id = drop.getAttribute('uidropbody');
            if (id) {
                def._tbody = document.getElementById(id);
                if (!def._tbody) {
                    alert('Internal error: the TBODY ID=' + id + ' cannot be located (row dropTarget)');
                    continue;
                }
                dropRemoveNonsense(def._tbody);
            }
            _dropList.push(def);
        }
        return _dropList;
    }
    WebUI.dropGetList = dropGetList;
    function dropClearList() {
        _dropList = undefined;
    }
    WebUI.dropClearList = dropClearList;
    function dropTargetFind(x, y) {
        var dl = dropGetList();
        for (var i = dl.length; --i >= 0;) {
            var d = dl[i];
            if (x >= d._position.x && x < d._position.x + d._width
                && y >= d._position.y && y < d._position.y + d._height) {
                for (var j = d._types.length; --j >= 0;) {
                    if (d._types[j] == _dragType)
                        return d;
                }
            }
        }
        return null;
    }
    WebUI.dropTargetFind = dropTargetFind;
    function dropRemoveNonsense(body) {
        for (var i = body.childNodes.length; --i >= 0;) {
            var n = body.childNodes[i];
            if (n.nodeName == '#text')
                body.removeChild(n);
        }
    }
    WebUI.dropRemoveNonsense = dropRemoveNonsense;
    var RowDropzoneHandler = (function () {
        function RowDropzoneHandler() {
        }
        RowDropzoneHandler.prototype.locateBest = function (dz) {
            var tbody = dz._tbody;
            if (!tbody)
                throw "No TBody!";
            var mousePos = _dragLastY;
            var mouseX = _dragLastX;
            var gravity = 0;
            var lastrow = null;
            var rowindex = 0;
            var position = { top: 0, index: 0 };
            for (var i = 0; i < tbody.childNodes.length; i++) {
                var tr = tbody.childNodes[i];
                if (tr.nodeName != 'TR')
                    continue;
                lastrow = tr;
                var off = $(tr).offset();
                var prevPosition = position;
                position = { top: off.top, index: i };
                if (position) {
                    if (mousePos >= prevPosition.top && mousePos < position.top) {
                        gravity = 0;
                        if (prevPosition.top + position.top != 0) {
                            var hy = (prevPosition.top + position.top) / 2;
                            gravity = mousePos < hy ? 0 : 1;
                        }
                        var colIndex = this.getColIndex(tr, mouseX);
                        return {
                            index: rowindex - 1,
                            iindex: prevPosition.index,
                            gravity: gravity,
                            row: tr,
                            colIndex: colIndex
                        };
                    }
                }
                else {
                }
                rowindex++;
            }
            var colIndex = this.getColIndex(lastrow, mouseX);
            return {
                index: rowindex,
                iindex: position.index,
                gravity: 1,
                row: lastrow,
                colIndex: colIndex
            };
        };
        RowDropzoneHandler.prototype.getColIndex = function (tr, mouseX) {
            var left = 0;
            var right = 0;
            var j;
            for (j = 0; j < tr.childNodes.length; j++) {
                var td = tr.childNodes[j];
                if (td.nodeName != 'TD')
                    continue;
                left = right;
                right = $(td).offset().left;
                if (mouseX >= left && mouseX < right) {
                    return j - 1;
                }
            }
            return 2;
        };
        RowDropzoneHandler.prototype.checkRerender = function (dz) {
            var b = this.locateBest(dz);
            if (b.iindex == _dropRowIndex)
                return;
            this.unmark(dz);
            this.renderTween(dz, b);
        };
        RowDropzoneHandler.prototype.renderTween = function (dz, b) {
            var body = dz._tbody;
            var colCount = 0;
            if (dz._tbody.children.length > 0) {
                var temp = dz._tbody.children[0].children;
                $(temp).each(function () {
                    colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
                });
            }
            var tr = document.createElement('tr');
            var colIndex = b.colIndex;
            for (var i = 0; i < colCount; i++) {
                this.appendPlaceHolderCell(tr, colIndex == i);
            }
            if (b.iindex >= body.childNodes.length)
                body.appendChild(tr);
            else
                body.insertBefore(tr, body.childNodes[b.iindex]);
            _dropRow = tr;
            _dropRowIndex = b.iindex;
        };
        RowDropzoneHandler.prototype.appendPlaceHolderCell = function (tr, appendPlaceholder) {
            var td = document.createElement('td');
            if (appendPlaceholder) {
                td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
                td.className = 'ui-drp-ins';
            }
            tr.appendChild(td);
        };
        RowDropzoneHandler.prototype.hover = function (dz) {
            var b = this.locateBest(dz);
            this.renderTween(dz, b);
        };
        RowDropzoneHandler.prototype.unmark = function (dz) {
            if (_dropRow) {
                $(_dropRow).remove();
                _dropRow = undefined;
                _dropRowIndex = undefined;
            }
        };
        RowDropzoneHandler.prototype.drop = function (dz) {
            this.unmark(dz);
            var b = this.locateBest(dz);
            WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
                _dragid: _dragNode.id,
                _index: (b.index + b.gravity),
                _colIndex: b.colIndex
            });
            dragReset();
        };
        return RowDropzoneHandler;
    }());
    var DefaultDropzoneHandler = (function () {
        function DefaultDropzoneHandler() {
        }
        DefaultDropzoneHandler.prototype.checkRerender = function (dz) {
        };
        DefaultDropzoneHandler.prototype.hover = function (dz) {
            $(dz._dropTarget).addClass("ui-drp-hover");
        };
        DefaultDropzoneHandler.prototype.unmark = function (dz) {
            if (dz)
                $(dz._dropTarget).removeClass("ui-drp-hover");
        };
        DefaultDropzoneHandler.prototype.drop = function (dz) {
            this.unmark(dz);
            WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
                _dragid: _dragNode.id,
                _index: 0
            });
            dragReset();
        };
        return DefaultDropzoneHandler;
    }());
    var _DEFAULT_DROPZONE_HANDLER = new DefaultDropzoneHandler();
    var _ROW_DROPZONE_HANDLER = new RowDropzoneHandler();
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    function fileUploadChange(e) {
        var tgt = e.currentTarget || e.srcElement;
        var vv = tgt.value.toString();
        if (!vv) {
            return;
        }
        var allowed = tgt.getAttribute('fuallowed');
        if (allowed) {
            var ok = false;
            var spl = allowed.split(',');
            vv = vv.toLowerCase();
            for (var i = 0; i < spl.length; i++) {
                var ext = spl[i].toLowerCase();
                if (ext.substring(0, 1) != ".")
                    ext = "." + ext;
                if (ext == ".*") {
                    ok = true;
                    break;
                }
                else if (vv.indexOf(ext, vv.length - ext.length) !== -1) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                var parts = vv.split('.');
                alert(WebUI.format(WebUI._T.uploadType, (parts.length > 1) ? parts.pop() : '', allowed));
                tgt.value = "";
                return;
            }
        }
        if (typeof tgt.files[0] !== "undefined") {
            var s = tgt.getAttribute('fumaxsize');
            var maxSize = 0;
            try {
                maxSize = Number(s);
            }
            catch (x) {
            }
            if (maxSize <= 0 || maxSize === NaN)
                maxSize = 100 * 1024 * 1024;
            var size = tgt.files[0].size;
            if (size > maxSize) {
                alert(WebUI.format(WebUI._T.buplTooBig, maxSize));
                tgt.value = "";
                return;
            }
        }
        var iframe = document.getElementById('webuiif');
        if (iframe) {
            iframe.parentNode.removeChild(iframe);
            iframe = undefined;
        }
        if (!iframe) {
            if (jQuery.browser.msie && !WebUI.isNormalIE9plus()) {
                iframe = document.createElement('<iframe name="webuiif" id="webuiif" src="#" style="display:none; width:0; height:0; border:none" onload="WebUI.ieUpdateUpload(event)">');
                document.body.appendChild(iframe);
            }
            else {
                iframe = document.createElement('iframe');
                iframe.id = 'webuiif';
                iframe.name = "webuiif";
                iframe.src = "#";
                iframe.style.display = "none";
                iframe.style.width = "0px";
                iframe.style.height = "0px";
                iframe.style.border = "none";
                iframe.onload = function () {
                    updateUpload(iframe.contentDocument);
                };
                document.body.appendChild(iframe);
            }
        }
        var form = tgt.parentNode;
        var img = document.createElement('img');
        img.border = "0";
        img.src = window.DomUIProgressURL;
        form.parentNode.insertBefore(img, form);
        form.style.display = 'none';
        form.target = "webuiif";
        form.submit();
        WebUI.blockUI();
    }
    WebUI.fileUploadChange = fileUploadChange;
    function ieUpdateUpload(e) {
        var iframe = document.getElementById('webuiif');
        var xml;
        var cw = iframe.contentWindow;
        if (cw && cw.document.XMLDocument) {
            xml = cw.document.XMLDocument;
        }
        else if (iframe.contentDocument) {
            var crap = iframe.contentDocument.body.innerText;
            crap = crap.replace(/^\s+|\s+$/g, '');
            crap = crap.replace(/(\n|\r)-*/g, '');
            crap = crap.replace(/&/g, '&amp;');
            if (window.DOMParser) {
                var parser = new DOMParser();
                xml = parser.parseFromString(crap);
            }
            else if (window.ActiveXObject) {
                xml = new ActiveXObject("Microsoft.XMLDOM");
                if (!xml.loadXML(crap)) {
                    alert('ie9 in emulation mode unfixable bug: cannot parse xml');
                    window.location.href = window.location.href;
                    WebUI.unblockUI();
                    return;
                }
            }
            else {
                alert('No idea how to parse xml today.');
            }
        }
        else
            alert('IE error: something again changed in xml source structure of the iframe, sigh');
        updateUpload(xml, iframe);
    }
    WebUI.ieUpdateUpload = ieUpdateUpload;
    function updateUpload(doc, ifr) {
        try {
            $.executeXML(doc);
        }
        catch (x) {
            alert(x);
            throw x;
        }
        finally {
            WebUI.unblockUI();
        }
    }
    WebUI.updateUpload = updateUpload;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _customUpdatesContributors = $.Callbacks("unique");
    var _customUpdatesContributorsTimerID = null;
    var _browserChecked = false;
    function registerCustomUpdatesContributor(contributorFunction) {
        _customUpdatesContributors.add(contributorFunction);
    }
    WebUI.registerCustomUpdatesContributor = registerCustomUpdatesContributor;
    function unregisterCustomUpdatesContributor(contributorFunction) {
        _customUpdatesContributors.remove(contributorFunction);
    }
    WebUI.unregisterCustomUpdatesContributor = unregisterCustomUpdatesContributor;
    function doCustomUpdates() {
        $('.floatThead-wrapper').each(function (index, node) {
            $(node).attr('stretch', $(node).find('>:first-child').attr('stretch'));
        });
        $('[stretch=true]').doStretch();
        $('.ui-dt, .ui-fixovfl').fixOverflow();
        $('input[marker]').setBackgroundImageMarker();
        $("textarea[mxlength], textarea[maxbytes]")
            .unbind("input.domui")
            .unbind("propertychange.domui")
            .bind('input.domui propertychange.domui', function () {
            var maxLength = attrNumber(this, 'mxlength');
            var maxBytes = attrNumber(this, 'maxbytes');
            var val = $(this).val();
            var newlines = (val.match(/\r\n/g) || []).length;
            if (maxBytes < 0) {
                if (maxLength < 0)
                    return;
            }
            else if (maxLength < 0) {
                maxLength = maxBytes;
            }
            if (val.length + newlines > maxLength) {
                val = val.substring(0, maxLength - newlines);
                $(this).val(val);
            }
            if (maxBytes > 0) {
                var cutoff = WebUI.truncateUtfBytes(val, maxBytes);
                if (cutoff < val.length) {
                    val = val.substring(0, cutoff);
                    $(this).val(val);
                }
            }
        });
        $("textarea[mxlength], textarea[maxbytes]")
            .unbind("keypress.domui")
            .bind('keypress.domui', function (evt) {
            if (evt.which == 0 || evt.which == 8)
                return true;
            var maxLength = attrNumber(this, 'mxlength');
            var maxBytes = attrNumber(this, 'maxbytes');
            var val = $(this).val();
            var newlines = (val.match(/\r\n/g) || []).length;
            if (maxBytes < 0) {
                if (maxLength < 0)
                    return true;
            }
            else if (maxLength < 0) {
                maxLength = maxBytes;
            }
            if (val.length - newlines >= maxLength)
                return false;
            if (maxBytes > 0) {
                var bytes = WebUI.utf8Length(val);
                if (bytes >= maxBytes)
                    return false;
            }
            return true;
        });
        if (_customUpdatesContributorsTimerID) {
            window.clearTimeout(_customUpdatesContributorsTimerID);
            _customUpdatesContributorsTimerID = null;
        }
        _customUpdatesContributorsTimerID = window.setTimeout(function () {
            try {
                _customUpdatesContributors.fire();
            }
            catch (ex) {
            }
        }, 500);
    }
    WebUI.doCustomUpdates = doCustomUpdates;
    function attrNumber(elem, name) {
        var val = $(elem).attr(name);
        if (typeof val == 'undefined')
            return -1;
        return Number(val);
    }
    function onDocumentReady() {
        checkBrowser();
        WebUI.handleCalendarChanges();
        if (window.DomUIDevel)
            handleDevelopmentMode();
        doCustomUpdates();
    }
    WebUI.onDocumentReady = onDocumentReady;
    function checkBrowser() {
        if (this._browserChecked)
            return;
        this._browserChecked = true;
        if ($.browser.msie && $.browser.majorVersion < 8) {
            if ($.cookie("domuiie") == null) {
                alert(WebUI.format(WebUI._T.sysUnsupported, $.browser.majorVersion));
                $.cookie("domuiie", "true", {});
            }
        }
    }
    var _debugLastKeypress;
    var _debugMouseTarget;
    function handleDevelopmentMode() {
        $(document).bind("keydown", function (e) {
            if (e.keyCode != 192)
                return;
            var t = new Date().getTime();
            if (!_debugLastKeypress || (t - _debugLastKeypress) > 250) {
                _debugLastKeypress = t;
                return;
            }
            var id = WebUI.nearestID(_debugMouseTarget);
            if (!id) {
                id = document.body.id;
            }
            WebUI.scall(id, "DEVTREE", {});
        });
        $(document.body).bind("mousemove", function (e) {
            _debugMouseTarget = e.target;
        });
    }
    WebUI.handleDevelopmentMode = handleDevelopmentMode;
    function debug(debugId, posX, posY, debugInfoHtml) {
        if ("0123456789".indexOf(debugId.charAt(0)) > -1) {
            alert("debugId(" + debugId + ") starts with digit! Please use different one!");
        }
        var debugPanel = document.getElementById(debugId);
        if (null == debugPanel) {
            debugPanel = document.createElement(debugId);
            $(debugPanel).attr('id', debugId);
            $(debugPanel).css('position', 'absolute');
            $(debugPanel).css('marginLeft', 0);
            $(debugPanel).css('marginTop', 0);
            $(debugPanel).css('background-color', 'yellow');
            $(debugPanel).css('border', '1px');
            $(debugPanel).css('z-index', 2000);
            $(debugPanel).appendTo('body');
        }
        $(debugPanel).css('left', posX);
        $(debugPanel).css('top', posY);
        $(debugPanel).html(debugInfoHtml);
    }
    WebUI.debug = debug;
    function addPagerAccessKeys(e) {
        var KEY = {
            HOME: 36,
            END: 35,
            PAGE_UP: 33,
            PAGE_DOWN: 34
        };
        if ($('div.ui-dp-btns').size() > 0) {
            if (e.altKey) {
                if (e.keyCode == KEY.HOME) {
                    $("div.ui-dp-btns > a:nth-child(1)").click();
                }
                else if (e.keyCode == KEY.PAGE_UP) {
                    $("div.ui-dp-btns > a:nth-child(2)").click();
                }
                else if (e.keyCode == KEY.PAGE_DOWN) {
                    $("div.ui-dp-btns > a:nth-child(3)").click();
                }
                else if (e.keyCode == KEY.END) {
                    $("div.ui-dp-btns > a:nth-child(4)").click();
                }
            }
        }
    }
    WebUI.addPagerAccessKeys = addPagerAccessKeys;
})(WebUI || (WebUI = {}));
(function ($) {
    if ($.browser.msie && $.browser.majorVersion < 10) {
        $.dbg = function (a, b, c, d, e) {
            if (window.console == undefined)
                return;
            switch (arguments.length) {
                default:
                    window.console.log(a);
                    return;
                case 2:
                    window.console.log(a, b);
                    return;
                case 3:
                    window.console.log(a, b, c);
                    return;
                case 4:
                    window.console.log(a, b, c, d);
                    return;
                case 5:
                    window.console.log(a, b, c, d, e);
                    return;
            }
        };
    }
    else if (window.console != undefined) {
        if (window.console.debug != undefined) {
            $.dbg = function () {
                window.console.debug.apply(window.console, arguments);
            };
        }
        else if (window.console.log != undefined) {
            $.dbg = function () {
                window.console.log.apply(window.console, arguments);
            };
        }
    }
    else {
        $.dbg = function () { };
    }
})(jQuery);
$.fn.extend({
    center: function () {
        if (this.css("position") != "fixed") {
            this.css("position", "absolute");
            this.css("top", Math.max(0, (($(window).height() - this.outerHeight()) / 2) + $(window).scrollTop()) + "px");
            this.css("left", Math.max(0, (($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft()) + "px");
        }
        else {
            this.css("top", Math.max(0, (($(window).height() - this.outerHeight()) / 2)) + "px");
            this.css("left", Math.max(0, (($(window).width() - this.outerWidth()) / 2)) + "px");
        }
        return this;
    },
    cookie: function (name, value, options) {
        if (value !== undefined) {
            if (value === null)
                options.expires = -1;
            if (typeof options.expires === 'number') {
                var dt = new Date();
                dt.setDate(dt.getDate() + options.expires);
                options.expires = dt;
            }
            value = String(value);
            var c = [
                encodeURIComponent(name), '=', encodeURIComponent(value),
                options.expires ? '; expires=' + options.expires.toUTCString() : '',
                options.path ? '; path=' + options.path : '',
                options.domain ? '; domain=' + options.domain : '',
                options.secure ? '; secure' : ''
            ].join('');
            return (document.cookie = c);
        }
        var cookar = document.cookie.split("; ");
        for (var i = cookar.length; --i >= 0;) {
            var par = cookar[i].split('=');
            if (par.length < 2)
                continue;
            var rname = decodeURIComponent(par.shift().replace(/\+/g, ' '));
            if (rname === name) {
                return decodeURIComponent(par.join('=').replace(/\+/g, ' '));
            }
        }
        return null;
    },
    fixOverflow: function () {
        if (!$.browser.msie || $.browser.version.substring(0, 1) != "7")
            return this;
        return this.each(function () {
            if (this.scrollWidth > this.offsetWidth) {
                $(this).css({ 'padding-bottom': '20px' });
                if (this.scrollHeight <= this.offsetHeight) {
                    $(this).css({ 'overflow-y': 'hidden' });
                }
            }
            if (this.scrollHeight > this.offsetHeight) {
                $(this).css({ 'margin-right': '17px' });
                if (this.scrollWidth <= this.offsetWidth) {
                    $(this).css({ 'overflow-x': 'hidden' });
                }
            }
        });
    },
    doStretch: function () {
        return this.each(function () {
            WebUI.stretchHeightOnNode(this);
        });
    },
    setBackgroundImageMarker: function () {
        return this.each(function () {
            if ($(this).markerTransformed) {
                return;
            }
            var imageUrl = 'url(' + $(this).attr('marker') + ')';
            var value = $(this).val();
            try {
                if ((!(this == document.activeElement)) && value.length == 0) {
                    $(this).css('background-image', imageUrl);
                }
            }
            catch (e) {
            }
            $(this).css('background-repeat', 'no-repeat');
            $(this).bind('focus', function (e) {
                $(this).css('background-image', 'none');
            });
            $(this).bind('blur', function (e) {
                if (value.length == 0) {
                    $(this).css('background-image', imageUrl);
                }
                else {
                    $(this).css('background-image', 'none');
                }
            });
            $(this).markerTransformed = true;
        });
    }
});
var WebUI;
(function (WebUI) {
    WebUI._hideExpiredMessage = false;
    var _keepAliveInterval = 0;
    function setHideExpired() {
        WebUI._hideExpiredMessage = true;
    }
    WebUI.setHideExpired = setHideExpired;
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
    WebUI.onLookupTypingReturnKeyHandler = onLookupTypingReturnKeyHandler;
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
    WebUI.scheduleOnLookupTypingEvent = scheduleOnLookupTypingEvent;
    function getKeywordPopupSelectedRowIndex(keywordInputNode) {
        var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
        if (selectedIndexInput instanceof HTMLInputElement) {
            if (selectedIndexInput.value && selectedIndexInput.value != "") {
                return parseInt(selectedIndexInput.value);
            }
        }
        return -1;
    }
    WebUI.getKeywordPopupSelectedRowIndex = getKeywordPopupSelectedRowIndex;
    function setKeywordPopupSelectedRowIndex(keywordInputNode, intValue) {
        var selectedIndexInput = $(keywordInputNode.parentNode).children("input:hidden").get(0);
        if (!selectedIndexInput) {
            selectedIndexInput = document.createElement("input");
            selectedIndexInput.setAttribute("type", "hidden");
            $(keywordInputNode.parentNode).append($(selectedIndexInput));
        }
        selectedIndexInput.value = intValue;
    }
    WebUI.setKeywordPopupSelectedRowIndex = setKeywordPopupSelectedRowIndex;
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
    WebUI.lookupPopupClicked = lookupPopupClicked;
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
    WebUI.lookupRowMouseOver = lookupRowMouseOver;
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
    WebUI.hideLookupTypingPopup = hideLookupTypingPopup;
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
    WebUI.showLookupTypingPopupIfStillFocusedAndFixZIndex = showLookupTypingPopupIfStillFocusedAndFixZIndex;
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
    WebUI.displayWaiting = displayWaiting;
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
    WebUI.hideWaiting = hideWaiting;
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
    WebUI.lookupTyping = lookupTyping;
    function lookupTypingDone(id) {
        var fields = {};
        WebUI.getInputFields(fields);
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
    WebUI.lookupTypingDone = lookupTypingDone;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _frmIdCounter = 0;
    function backgroundPrint(url) {
        try {
            var frmname = priparePrintDiv(url);
            framePrint(frmname);
        }
        catch (x) {
            alert("Failed: " + x);
        }
    }
    WebUI.backgroundPrint = backgroundPrint;
    function framePrint(frmname) {
        if (jQuery.browser.msie) {
            documentPrintIE(frmname);
        }
        else {
            documentPrintNonIE(frmname);
        }
    }
    WebUI.framePrint = framePrint;
    function documentPrintIE(frmname) {
        var ex = undefined;
        try {
            var frm = window.frames[frmname];
            $("#" + frmname).on("load", function () {
                try {
                    frm.focus();
                    setTimeout(function () {
                        if (!frm.document.execCommand('print', true, null)) {
                            alert('cannot print: ' + ex);
                        }
                    }, 1000);
                }
                catch (x) {
                    ex = x;
                    alert('cannot print: ' + x);
                }
            });
        }
        catch (x) {
            ex = x;
            alert("Failed: " + x);
        }
    }
    WebUI.documentPrintIE = documentPrintIE;
    function documentPrintNonIE(frmname) {
        try {
            var frm = window.frames[frmname];
            $("#" + frmname).on("load", function () {
                try {
                    frm.focus();
                    setTimeout(function () {
                        frm.print();
                    }, 1000);
                }
                catch (x) {
                    alert('cannot print: ' + x);
                }
            });
        }
        catch (x) {
            alert("Failed: " + x);
        }
    }
    WebUI.documentPrintNonIE = documentPrintNonIE;
    function priparePrintDiv(url) {
        var div = document.getElementById('domuiprif');
        if (div)
            div.innerHTML = "";
        else {
            div = document.createElement('div');
            div.id = 'domuiprif';
            div.className = 'ui-printdiv';
            document.body.appendChild(div);
        }
        var frmname = "dmuifrm" + (_frmIdCounter++);
        if (url.trim() !== "") {
            $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" src="' + url + '">');
        }
        else {
            $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
        }
        return frmname;
    }
    WebUI.priparePrintDiv = priparePrintDiv;
    function printtext(id) {
        var item = document.getElementById(id);
        var textData;
        if (item && (item.tagName == "input" || item.tagName == "INPUT" || item.tagName == "textarea" || item.tagName == "TEXTAREA")) {
            textData = item.value;
        }
        if (textData) {
            try {
                var div = document.getElementById('domuiprif');
                if (div)
                    div.innerHTML = "";
                else {
                    div = document.createElement('div');
                    div.id = 'domuiprif';
                    div.className = 'ui-printdiv';
                    document.body.appendChild(div);
                }
                var frmname = "dmuifrm" + _frmIdCounter++;
                $(div).html('<iframe id="' + frmname + '" name="' + frmname + '" width="1000px" height="1000px"/>');
                var frm = window.frames[frmname];
                frm.document.open();
                frm.document.write('<html></head></head><body style="margin:0px;">');
                frm.document.write(textData.replace(/\n/g, '<br/>'));
                frm.document.write('</body></html>');
                frm.document.close();
                frm.focus();
                frm.print();
            }
            catch (x) {
                alert("Failed: " + x);
            }
        }
    }
    WebUI.printtext = printtext;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    function autoHeightReset(topid, flexid, bottom) {
        $(window).bind("resize", function () {
            recalculateAutoHeight(topid, flexid, bottom);
        });
        recalculateAutoHeight(topid, flexid, bottom);
    }
    WebUI.autoHeightReset = autoHeightReset;
    function recalculateAutoHeight(topid, flexid, bottom) {
        try {
            var tbot = $(topid).offset().top + $(topid).height();
            var height = $(window).height() - tbot - bottom;
            $(flexid).height(height + "px");
        }
        catch (x) {
        }
    }
    WebUI.recalculateAutoHeight = recalculateAutoHeight;
    function setThreePanelHeight(top, middle, bottom) {
        middle = "#" + middle;
        var height = $(middle).parent().height();
        var theight = 0;
        if (typeof top === "string") {
            theight = $("#" + top).height();
        }
        else if (top) {
            theight = top;
        }
        var bheight = 0;
        if (typeof bottom === "string") {
            bheight = $("#" + bottom).height();
        }
        else if (bottom) {
            bheight = $(bottom).height();
        }
        height -= theight - bheight;
        if (height < 0) {
            height = 0;
        }
        $(middle).height(height + "px");
        $(middle).css({ "overflow-y": "auto" });
    }
    WebUI.setThreePanelHeight = setThreePanelHeight;
    function autoHeight(flexid, bottom) {
        $(window).bind("resize", function () {
            autoHeightRecalc(flexid, bottom);
        });
        autoHeightRecalc(flexid, bottom);
    }
    WebUI.autoHeight = autoHeight;
    function autoHeightRecalc(flexid, bottom) {
        var tbot = $(flexid).offset().top;
        var height = $(window).height() - tbot - bottom;
        $(flexid).height(height + "px");
    }
    WebUI.autoHeightRecalc = autoHeightRecalc;
    function notifySizePositionChangedOnId(elemId) {
        var element = document.getElementById(elemId);
        if (!element) {
            return;
        }
        var fields = {};
        fields[element.id + "_rect"] = $(element).position().left + "," + $(element).position().top + "," + $(element).width() + "," + $(element).height();
        fields["window_size"] = window.innerWidth + "," + window.innerHeight;
        WebUI.scall(element.id, "notifyClientPositionAndSize", fields);
    }
    WebUI.notifySizePositionChangedOnId = notifySizePositionChangedOnId;
    function notifySizePositionChanged(event, ui) {
        var element = ui.helper.get(0);
        if (!element) {
            return;
        }
        notifySizePositionChangedOnId(element.id);
    }
    WebUI.notifySizePositionChanged = notifySizePositionChanged;
    function floatingDivResize(ev, ui) {
        $(ui.helper.get(0)).css('position', 'fixed');
        $('[stretch=true]').doStretch();
        $('.ui-dt, .ui-fixovfl').fixOverflow();
    }
    WebUI.floatingDivResize = floatingDivResize;
    function onWindowResize() {
        WebUI.doCustomUpdates();
    }
    WebUI.onWindowResize = onWindowResize;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    var _ignoreScrollClick = 0;
    function scrollLeft(bLeft) {
        if (this._ignoreScrollClick != 0 || $(bLeft).hasClass('ui-stab-dis'))
            return;
        var scrlNavig = $(bLeft.parentNode);
        var offset = -1 * parseInt($('ul', scrlNavig).css('marginLeft'));
        var diff = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-left', scrlNavig).width();
        var me = this;
        var disa = false;
        if (diff >= offset) {
            disa = true;
            diff = offset;
        }
        this._ignoreScrollClick++;
        $('ul', scrlNavig).animate({ marginLeft: '+=' + diff }, 400, 'swing', function () {
            $('.ui-stab-scrl-right', scrlNavig).removeClass('ui-stab-dis');
            if (disa) {
                $(bLeft).addClass('ui-stab-dis');
            }
            me._ignoreScrollClick--;
        });
    }
    WebUI.scrollLeft = scrollLeft;
    function scrollRight(bRight) {
        if (this._ignoreScrollClick != 0 || $(bRight).hasClass('ui-stab-dis'))
            return;
        var scrlNavig = $(bRight.parentNode);
        var tabsTotalWidth = $('li:last', scrlNavig).width() + 8 + $('li:last', scrlNavig).offset().left - $('li:first', scrlNavig).offset().left;
        var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right', scrlNavig).width();
        var maxLeftOffset = tabsTotalWidth - tabsVisibleWidth;
        var diff = tabsVisibleWidth;
        var offset = -1 * parseInt($('ul', scrlNavig).css('marginLeft'));
        var disa = false;
        if (offset >= maxLeftOffset) {
            return;
        }
        else if (diff + offset >= maxLeftOffset) {
            diff = maxLeftOffset - offset;
            disa = true;
        }
        this._ignoreScrollClick++;
        var me = this;
        $('ul', scrlNavig).animate({ marginLeft: '-=' + diff }, 400, 'swing', function () {
            $('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
            if (disa) {
                $(bRight).addClass('ui-stab-dis');
            }
            me._ignoreScrollClick--;
        });
    }
    WebUI.scrollRight = scrollRight;
    function recalculateScrollers(scrlNavigId) {
        var scrlNavig = document.getElementById(scrlNavigId);
        var tabsTotalWidth = $('li:last', scrlNavig).width() + 8 + $('li:last', scrlNavig).offset().left - $('li:first', scrlNavig).offset().left;
        var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right', scrlNavig).width();
        if (tabsTotalWidth > tabsVisibleWidth) {
            var leftM = parseInt($('ul', scrlNavig).css('marginLeft'));
            if (tabsTotalWidth + leftM > tabsVisibleWidth) {
                $('.ui-stab-scrl-right', scrlNavig).removeClass('ui-stab-dis');
            }
            else {
                $('.ui-stab-scrl-right', scrlNavig).addClass('ui-stab-dis');
            }
            if (leftM < 0) {
                $('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
            }
            else {
                $('.ui-stab-scrl-left', scrlNavig).addClass('ui-stab-dis');
            }
        }
        else {
            $('.ui-stab-scrl-left', scrlNavig).css('display', 'none');
            $('.ui-stab-scrl-right', scrlNavig).css('display', 'none');
            $('ul', scrlNavig).animate({ marginLeft: 0 }, 400, 'swing');
        }
    }
    WebUI.recalculateScrollers = recalculateScrollers;
})(WebUI || (WebUI = {}));
var WebUI;
(function (WebUI) {
    WebUI._T = {};
    function definePageName(pn) {
        $(document.body).attr("pageName", pn);
    }
    WebUI.definePageName = definePageName;
    function log() {
        var args = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args[_i] = arguments[_i];
        }
        $.dbg.apply(this, args);
    }
    WebUI.log = log;
    function curry(scope, fn) {
        scope = scope || window;
        var args = [];
        for (var i = 2, len = arguments.length; i < len; ++i) {
            args.push(arguments[i]);
        }
        return function () {
            fn.apply(scope, args);
        };
    }
    function pickle(scope, fn) {
        scope = scope || window;
        var args = [];
        for (var i = 2, len = arguments.length; i < len; ++i) {
            args.push(arguments[i]);
        }
        return function () {
            var nargs = [];
            for (var i = 0, len = args.length; i < len; i++)
                nargs.push(args[i]);
            for (var i = 0, len = arguments.length; i < len; i++)
                nargs.push(arguments[i]);
            fn.apply(scope, nargs);
        };
    }
    function normalizeKey(evt) {
        if ($.browser.mozilla) {
            if (evt.charCode > 0)
                return evt.charCode;
            return evt.keyCode * 1000;
        }
        if (evt.charCode != undefined) {
            if (evt.charCode == evt.keyCode)
                return evt.charCode;
            if (evt.keyCode > 0)
                return evt.keyCode * 1000;
            return evt.charCode;
        }
        return evt.keyCode;
    }
    WebUI.normalizeKey = normalizeKey;
    function isNumberKey(evt) {
        var keyCode = normalizeKey(evt);
        return (keyCode >= 1000 || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
    }
    WebUI.isNumberKey = isNumberKey;
    function isFloatKey(evt) {
        var keyCode = normalizeKey(evt);
        return (keyCode >= 1000 || keyCode == 0x2c || keyCode == 0x2e || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
    }
    WebUI.isFloatKey = isFloatKey;
    function delayedSetAttributes() {
        if (arguments.length < 3 || ((arguments.length & 1) != 1)) {
            alert('internal: odd call to delayedSetAttributes: ' + arguments.length);
            return;
        }
        var n = document.getElementById(arguments[0]);
        if (n == undefined)
            return;
        for (var i = 1; i < arguments.length; i += 2) {
            try {
                n[arguments[i]] = arguments[i + 1];
            }
            catch (x) {
                alert('Failed to set javascript property ' + arguments[i] + ' to ' + arguments[i + 1] + ": " + x);
            }
        }
    }
    WebUI.delayedSetAttributes = delayedSetAttributes;
    function focus(id) {
        var n = document.getElementById(id);
        if (n) {
            if ($.browser.msie) {
                setTimeout(function () {
                    try {
                        $('body').focus();
                        n.focus();
                    }
                    catch (e) {
                    }
                }, 100);
            }
            else {
                try {
                    n.focus();
                }
                catch (e) {
                }
            }
        }
    }
    WebUI.focus = focus;
    function refreshPage() {
        var url = window.location.href;
        var ix1 = url.indexOf("$cid=");
        if (ix1 > 0) {
            var ix2 = url.indexOf("&", ix1);
            if (ix2 > ix1) {
                url = url.substring(0, ix1) + url.substring(ix2 + 1);
            }
            else {
                url = url.substring(0, ix1);
            }
            window.location.href = url;
        }
    }
    WebUI.refreshPage = refreshPage;
    function getPostURL() {
        var p = window.location.href;
        var ix = p.indexOf('?');
        if (ix != -1)
            p = p.substring(0, ix);
        return p;
    }
    WebUI.getPostURL = getPostURL;
    function getObituaryURL() {
        var u = getPostURL();
        var ix = u.lastIndexOf('.');
        if (ix < 0)
            throw "INVALID PAGE URL";
        return u.substring(0, ix) + ".obit";
    }
    WebUI.getObituaryURL = getObituaryURL;
    function openWindow(url, name, par) {
        var h = undefined;
        try {
            h = window.open(url, name, par);
        }
        catch (x) {
            alert("Got popup exception: " + x);
        }
        if (!h)
            alert(WebUI._T.sysPopupBlocker);
        return false;
    }
    WebUI.openWindow = openWindow;
    function postURL(path, name, params, target) {
        var form = document.createElement("form");
        form.setAttribute("method", "post");
        form.setAttribute("action", path);
        if (null != target) {
            form.setAttribute("target", target);
        }
        for (var key in params) {
            if (params.hasOwnProperty(key)) {
                var hiddenField = document.createElement("input");
                hiddenField.setAttribute("type", "hidden");
                hiddenField.setAttribute("name", key);
                hiddenField.setAttribute("value", params[key]);
                form.appendChild(hiddenField);
            }
        }
        document.body.appendChild(form);
        form.submit();
    }
    WebUI.postURL = postURL;
    function isBrowserClosed(e) {
        try {
            if (window.event) {
                var e_1 = window.event;
                alert('wcy=' + e_1.clientY + ", wcx="
                    + e_1.clientX + ", dw="
                    + document.documentElement.clientWidth + ", screentop="
                    + self.screenTop);
                if (e_1.clientY < 0 && (e_1.clientX > (document.documentElement.clientWidth - 5) || e_1.clientX < 15))
                    return true;
            }
        }
        catch (x) {
        }
        try {
            if (window.innerWidth == 0 && window.innerHeight == 0)
                return true;
        }
        catch (x) {
        }
        return false;
    }
    WebUI.isBrowserClosed = isBrowserClosed;
    function toClip(value) {
        var w = window;
        if (w.clipboardData) {
            w.clipboardData.setData("Text", value);
        }
        else if (w.netscape) {
            if (value.createTextRange) {
                var range = value.createTextRange();
                if (range)
                    range.execCommand('Copy');
            }
            else {
                var flashcopier = 'flashcopier';
                if (!document.getElementById(flashcopier)) {
                    var divholder = document.createElement('div');
                    divholder.id = flashcopier;
                    document.body.appendChild(divholder);
                }
                document.getElementById(flashcopier).innerHTML = '';
                var divinfo = '<embed src="$js/_clipboard.swf" FlashVars="clipboard=' + encodeURIComponent(value) + '" width="0" height="0" type="application/x-shockwave-flash"></embed>';
                document.getElementById(flashcopier).innerHTML = divinfo;
            }
        }
    }
    WebUI.toClip = toClip;
    function format(message) {
        var rest = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            rest[_i - 1] = arguments[_i];
        }
        for (var i = 1; i < arguments.length; i++) {
            message = message.replace("{" + (i - 1) + "}", arguments[i]);
        }
        return message;
    }
    WebUI.format = format;
    function findParentOfTagName(node, type) {
        while (node != null) {
            node = node.parentNode;
            if (node.tagName == type)
                return node;
        }
        return null;
    }
    WebUI.findParentOfTagName = findParentOfTagName;
    function disableSelection(node) {
        node.onselectstart = function () {
            return false;
        };
        node.unselectable = "on";
        node.style.MozUserSelect = "none";
        node.style.cursor = "default";
    }
    WebUI.disableSelection = disableSelection;
    function disableSelect(id) {
        if ($.browser.msie) {
            $('#' + id).disableSelection();
        }
        else {
            $('#' + id).addClass("ui-selection-disable");
        }
    }
    WebUI.disableSelect = disableSelect;
    function enableSelect(id) {
        if ($.browser.msie) {
            $('#' + id).enableSelection();
        }
        else {
            $('#' + id).removeClass("ui-selection-disable");
        }
    }
    WebUI.enableSelect = enableSelect;
    function nearestID(elem) {
        while (elem) {
            if (elem.id)
                return elem.id;
            elem = elem.parentNode;
        }
        return undefined;
    }
    WebUI.nearestID = nearestID;
    function refreshElement(id) {
        var elem = document.getElementById(id);
        if (elem) {
            $(elem).hide();
            $(elem).show(1);
        }
    }
    WebUI.refreshElement = refreshElement;
    var Point = (function () {
        function Point(x, y) {
            this.x = x;
            this.y = y;
        }
        return Point;
    }());
    WebUI.Point = Point;
    var Rect = (function () {
        function Rect(_bx, _by, _ex, _ey) {
            this.bx = _bx;
            this.ex = _ex;
            this.by = _by;
            this.ey = _ey;
        }
        return Rect;
    }());
    WebUI.Rect = Rect;
    function getAbsolutePosition(obj) {
        var top = 0, left = 0;
        while (obj) {
            top += obj.offsetTop;
            left += obj.offsetLeft;
            obj = obj.offsetParent;
        }
        return new Point(left, top);
    }
    WebUI.getAbsolutePosition = getAbsolutePosition;
    function getAbsScrolledPosition(el) {
        var bx = el.offsetLeft || 0;
        var by = el.offsetTop || 0;
        var ex = bx + el.offsetWidth;
        var ey = by + el.offsetHeight;
        var el = el.parentNode;
        while (el != null) {
            if (el.clientHeight != null) {
                if (by < el.scrollTop)
                    by = el.scrollTop;
                if (bx < el.scrollLeft)
                    bx = el.scrollLeft;
                if (bx >= ex || by >= ey)
                    return null;
                var vey = el.scrollTop + el.clientHeight;
                var vex = el.scrollLeft + el.clientWidth;
                if (ex > vex)
                    ex = vex;
                if (ey > vey)
                    ey = vey;
                if (by >= ey || bx >= ex)
                    return null;
                by -= el.scrollTop;
                ey -= el.scrollTop;
                bx -= el.scrollLeft;
                ex -= el.scrollLeft;
                by += el.offsetTop;
                ey += el.offsetTop;
                bx += el.offsetLeft;
                ex += el.offsetLeft;
            }
            el = el.parentNode;
        }
        return new Rect(bx, by, ex, ey);
    }
    WebUI.getAbsScrolledPosition = getAbsScrolledPosition;
    function scrollMeToTop(elemId, selColor, offset) {
        var elem = document.getElementById(elemId);
        if (!elem) {
            return;
        }
        var parent = elem.parentNode;
        if (!parent) {
            return;
        }
        if (parent.scrollHeight > parent.offsetHeight) {
            var elemPos = $(elem).position().top;
            if (elemPos > 0 && elemPos < parent.offsetHeight) {
                if (selColor) {
                    var oldColor_1 = $(elem).css('background-color');
                    $(elem).animate({ backgroundColor: selColor }, "slow", function () {
                        $(elem).animate({ backgroundColor: oldColor_1 }, "fast");
                    });
                }
            }
            else {
                var newPos = $(elem).position().top + parent.scrollTop;
                if ($.browser.msie && parseInt($.browser.version) < 11) {
                    if ($(elem).height() == 0) {
                        newPos = newPos - 15;
                    }
                }
                if (offset) {
                    newPos = newPos - offset;
                }
                $(parent).animate({ scrollTop: newPos }, 'slow');
            }
        }
    }
    WebUI.scrollMeToTop = scrollMeToTop;
    function makeOptionVisible(elemId, offset) {
        if ($.browser.msie) {
            return;
        }
        var elem = document.getElementById(elemId);
        if (!elem) {
            return;
        }
        var parent = elem.parentNode;
        if (!parent) {
            return;
        }
        if (parent.scrollHeight > parent.offsetHeight) {
            var elemPos = $(elem).position().top;
            if (elemPos <= 0 || elemPos >= parent.offsetHeight) {
                var newPos = elemPos + parent.scrollTop;
                if (offset) {
                    newPos = newPos - offset;
                }
                $(parent).animate({ scrollTop: newPos }, 'slow');
            }
        }
    }
    WebUI.makeOptionVisible = makeOptionVisible;
    function truncateUtfBytes(str, nbytes) {
        var bytes = 0;
        var length = str.length;
        for (var ix = 0; ix < length; ix++) {
            var c = str.charCodeAt(ix);
            if (c < 0x80)
                bytes++;
            else if (c < 0x800)
                bytes += 2;
            else
                bytes += 3;
            if (bytes > nbytes)
                return ix;
        }
        return length;
    }
    WebUI.truncateUtfBytes = truncateUtfBytes;
    function utf8Length(str) {
        var bytes = 0;
        var length = str.length;
        for (var ix = 0; ix < length; ix++) {
            var c = str.charCodeAt(ix);
            if (c < 0x80)
                bytes++;
            else if (c < 0x800)
                bytes += 2;
            else
                bytes += 3;
        }
        return bytes;
    }
    WebUI.utf8Length = utf8Length;
    function showOverflowTextAsTitle(id, selector) {
        var root = $("#" + id);
        if (root) {
            root.find(selector).each(function () {
                if (this.offsetWidth < this.scrollWidth) {
                    var $this = $(this);
                    $this.attr("title", $this.text());
                }
            });
        }
        return true;
    }
    WebUI.showOverflowTextAsTitle = showOverflowTextAsTitle;
    function replaceBrokenImageSrc(id, alternativeImage) {
        $('img#' + id).error(function () {
            $(this).attr("src", alternativeImage);
        });
    }
    WebUI.replaceBrokenImageSrc = replaceBrokenImageSrc;
    function deactivateHiddenAccessKeys(windowId) {
        $('button').each(function (index) {
            var iButton = $(this);
            if (isButtonChildOfElement(iButton, windowId)) {
                var oldAccessKey = $(iButton).attr('accesskey');
                if (oldAccessKey != null) {
                    $(iButton).attr('accesskey', $(windowId).attr('id') + '~' + oldAccessKey);
                }
            }
        });
    }
    WebUI.deactivateHiddenAccessKeys = deactivateHiddenAccessKeys;
    function reactivateHiddenAccessKeys(windowId) {
        $("button[accesskey*='" + windowId + "~']").each(function (index) {
            var attr = $(this).attr('accesskey');
            var accessKeyArray = attr.split(windowId + '~');
            $(this).attr('accesskey', accessKeyArray[accessKeyArray.length - 1]);
        });
    }
    WebUI.reactivateHiddenAccessKeys = reactivateHiddenAccessKeys;
    function isButtonChildOfElement(buttonId, windowId) {
        return $(buttonId).parents('#' + $(windowId).attr('id')).length == 0;
    }
    WebUI.isButtonChildOfElement = isButtonChildOfElement;
    function stretchHeight(elemId) {
        var elem = document.getElementById(elemId);
        if (!elem) {
            return;
        }
        stretchHeightOnNode(elem);
    }
    WebUI.stretchHeight = stretchHeight;
    function stretchHeightOnNode(elem) {
        var elemHeight = $(elem).height();
        var totHeight = 0;
        $(elem).siblings().each(function (index, node) {
            if (node != elem && $(node).css('position') == 'static' && ($(node).css('float') == 'none' || $(node).css('width') != '100%')) {
                if (!($(node).css('visibility') == 'hidden' || $(node).css('display') == 'none')) {
                    totHeight += $(node).outerHeight(true);
                }
            }
        });
        var elemDeltaHeight = $(elem).outerHeight(true) - $(elem).height();
        if (WebUI.isIE8orIE8c()) {
            elemDeltaHeight = elemDeltaHeight + 1;
        }
        $(elem).height($(elem).parent().height() - totHeight - elemDeltaHeight);
        if ($.browser.msie && $.browser.version.substring(0, 1) == "7") {
            if (elem.scrollWidth > elem.offsetWidth) {
                $(elem).height($(elem).height() - 20);
                if ($(elem).css('overflow-y') == 'hidden') {
                    if (elem.scrollHeight > elem.offsetHeight) {
                        $(elem).css({ 'overflow-y': 'auto' });
                    }
                }
                return;
            }
        }
    }
    WebUI.stretchHeightOnNode = stretchHeightOnNode;
    function loadStylesheet(path) {
        var head = document.getElementsByTagName("head")[0];
        if (!head)
            throw "Headless document!?";
        var link = document.createElement('link');
        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = path;
        link.media = 'screen';
        head.appendChild(link);
    }
    WebUI.loadStylesheet = loadStylesheet;
    function loadJavascript(path) {
        var head = document.getElementsByTagName("head")[0];
        if (!head)
            throw "Headless document!?";
        var scp = document.createElement('script');
        scp.type = 'text/javascript';
        scp.src = path;
        head.appendChild(scp);
    }
    WebUI.loadJavascript = loadJavascript;
    function preventIE11DefaultAction(e) {
        if ((navigator.userAgent.match(/Trident\/7\./))) {
            e.preventDefault();
        }
    }
    WebUI.preventIE11DefaultAction = preventIE11DefaultAction;
    function preventSelection() {
        return false;
    }
    WebUI.preventSelection = preventSelection;
})(WebUI || (WebUI = {}));
(function ($) {
    $.webui = function (xml) {
        processDoc(xml);
    };
    $.fn.extend({
        replace: function (a) {
            return this.after(a).remove();
        },
        replaceContent: function (a) {
            return this.empty().append(a);
        }
    });
    $.expr[':'].taconiteTag = function (a) {
        return a.taconiteTag === 1;
    };
    function log() {
        var str = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            str[_i] = arguments[_i];
        }
        if (!window.console || !window.console.debug)
            return;
        window.console.debug.apply(window.console, arguments);
    }
    function processDoc(xml) {
        go(xml);
    }
    function convert(s) {
        var doc = undefined;
        try {
            if (window.ActiveXObject) {
                doc = new ActiveXObject('Microsoft.XMLDOM');
                doc.async = 'false';
                doc.loadXML(s);
            }
            else {
                var parser = new DOMParser();
                doc = parser.parseFromString(s, 'text/xml');
            }
        }
        catch (e) {
            if (window.console && window.console.debug)
                window.console.debug('ERROR parsing XML string for conversion: ' + e, e);
            throw e;
        }
        var ok = doc && doc.documentElement && doc.documentElement.tagName != 'parsererror';
        if (!ok) {
            if (doc && doc.documentElement)
                log(doc.documentElement.textContent);
            if (window.console && window.console.debug) {
                alert('Internal error: the server response could not be parsed!? Look in the console for more info');
            }
            else {
                alert('Internal error: the server response could not be parsed!? I\'ll attempt to do a full refresh now...');
                window.location.href = window.location.href;
            }
            return null;
        }
        return doc;
    }
    function go(xml) {
        if (xml === "") {
            window.location.href = window.location.href;
            return;
        }
        if (typeof xml == 'string')
            xml = convert(xml);
        if (!xml || !xml.documentElement) {
            log('Invalid document');
            return;
        }
        executeXML(xml);
    }
    function executeXML(xml) {
        var rname = xml.documentElement.tagName;
        if (rname == 'redirect') {
            WebUI.blockUI();
            log("Redirecting- ");
            var to = xml.documentElement.getAttribute('url');
            if (!$.browser.msie && !$.browser.ieedge) {
                try {
                    document.write('<html></html>');
                    document.close();
                }
                catch (xxx) {
                }
            }
            window.location.href = to;
            return;
        }
        else if (rname == 'expiredOnPollasy') {
            return;
        }
        else if (rname == 'expired') {
            var msg = WebUI._T.sysSessionExpired;
            var hr = window.location.href;
            for (var i = xml.documentElement.childNodes.length; --i >= 0;) {
                var cn = xml.documentElement.childNodes[i];
                if (cn.tagName == 'msg') {
                    if (cn.textContent)
                        msg = cn.textContent;
                    else if (cn.text)
                        msg = cn.text;
                }
                else if (cn.tagName == 'href') {
                    if (cn.textContent)
                        hr = cn.textContent;
                    else if (cn.text)
                        hr = cn.text;
                }
            }
            alert(msg);
            window.location.href = hr;
            return;
        }
        process(xml.documentElement.childNodes);
    }
    $.executeXML = function (xml) {
        executeXML(xml);
    };
    function process(commands) {
        var param = {
            postProcess: false
        };
        for (var i = 0; i < commands.length; i++) {
            if (commands[i].nodeType != 1)
                continue;
            var cmdNode = commands[i];
            if (!executeNode(cmdNode, param)) {
                return;
            }
        }
        if (param.postProcess)
            postProcess();
    }
    function executeNode(cmdNode, param) {
        var cmd = cmdNode.tagName;
        if (cmd == "parsererror") {
            alert("The server response could not be parsed: " + cmdNode.innerText);
            window.location.href = window.location.href;
            return false;
        }
        if (cmd == 'head' || cmd == 'body') {
            if (!WebUI._hideExpiredMessage) {
                alert(WebUI._T.sysSessionExpired2);
            }
            window.location.href = window.location.href;
            return false;
        }
        if (cmd == 'eval') {
            var js = void 0;
            try {
                js = (cmdNode.firstChild ? cmdNode.textContent : null);
                if (js)
                    $.globalEval(js);
            }
            catch (ex) {
                alert('eval failed: ' + ex + ", js=" + js);
                throw ex;
            }
            return true;
        }
        var q = cmdNode.getAttribute('select');
        if (!q) {
            alert('The server seems to have lost this page.. Reloading the page with fresh data');
            window.location.href = window.location.href;
            return false;
        }
        var jq = $(q);
        if (!jq[0]) {
            log('No matching targets for selector: ', q);
            return true;
        }
        if (cmd == 'changeTagAttributes') {
            executeChangeTagAttributes(cmdNode, q);
            return true;
        }
        var cdataWrap = cmdNode.getAttribute('cdataWrap') || 'div';
        var a = [];
        var trimHash = {
            wrap: 1
        };
        if (cmdNode.childNodes.length > 0) {
            param.postProcess = true;
            var els = [];
            for (var j = 0; j < cmdNode.childNodes.length; j++)
                els[j] = createNode(cmdNode.childNodes[j], cdataWrap);
            a.push(trimHash[cmd] ? cleanse(els) : els);
        }
        var n = cmdNode.getAttribute('name');
        var v = cmdNode.getAttribute('value');
        if (n !== null)
            a.push(n);
        if (v !== null)
            a.push(v);
        for (var j = 1; true; j++) {
            v = cmdNode.getAttribute('arg' + j);
            if (v === null)
                break;
            a.push(v);
        }
        try {
            jq[cmd].apply(jq, a);
        }
        catch (e) {
            console.log("exception on node " + jq + " command " + cmd);
            throw e;
        }
        return true;
    }
    function executeChangeTagAttributes(cmdNode, queryString) {
        try {
            var dest = $(queryString)[0];
            var names = [];
            for (var ai = 0; ai < dest.attributes.length; ai++) {
                names[ai] = $.trim(dest.attributes[ai].name);
            }
            var src = cmdNode;
            for (var ai = 0, attr = ''; ai < src.attributes.length; ai++) {
                var a = src.attributes[ai], n = $.trim(a.name), v_1 = $.trim(a.value);
                if (n == 'select' || n.substring(0, 2) == 'on')
                    continue;
                if (n.substring(0, 6) == 'domjs_') {
                    var s = void 0;
                    try {
                        s = "dest." + n.substring(6) + " = " + v_1;
                        eval(s);
                        continue;
                    }
                    catch (ex) {
                        alert('domjs_ eval failed: ' + ex + ", value=" + s);
                        throw ex;
                    }
                }
                if (v_1 == '---') {
                    dest.removeAttribute(n);
                    continue;
                }
                if (n == 'style') {
                    dest.style.cssText = v_1;
                    dest.setAttribute(n, v_1);
                    if ($.browser.msie && $.browser.version.substring(0, 1) == "7") {
                        if ((dest.tagName.toLowerCase() == 'div' && $(dest).height() == 0) && ((v_1.indexOf('visibility') != -1 && v_1.indexOf('hidden') == -1) || (v_1.indexOf('display') != -1 && v_1.indexOf('none') == -1))) {
                            WebUI.refreshElement(dest.id);
                        }
                    }
                }
                else {
                    if (dest.tagName.toLowerCase() == 'select' && n == 'class' && $.browser.mozilla) {
                        dest.className = v_1;
                        var ele = dest;
                        var old = ele.selectedIndex;
                        ele.selectedIndex = 1;
                        ele.selectedIndex = old;
                    }
                    else if (v_1 == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
                        $(queryString).removeAttr(n);
                        removeValueFromArray(names, n);
                    }
                    else {
                        $(queryString).attr(n, v_1);
                        removeValueFromArray(names, n);
                    }
                }
            }
            for (var ai = 0; ai < names.length; ai++) {
                var a = names[ai];
                if (a == 'checked' || a == 'disabled' || a == 'title') {
                    $(dest).removeAttr(a);
                }
            }
        }
        catch (ex) {
            alert('changeTagAttr failed: ' + ex);
            throw ex;
        }
    }
    function postProcess() {
        if (!$.browser.opera && !$.browser.msie)
            return;
        $('select:taconiteTag').each(function () {
            $('option:taconiteTag', this).each(function () {
                this.setAttribute('selected', 'selected');
                this.taconiteTag = null;
            });
            this.taconiteTag = null;
        });
    }
    function removeValueFromArray(names, n) {
        var index = names.indexOf(n);
        if (index > -1) {
            names.splice(index, 1);
        }
    }
    function cleanse(els) {
        var a = [];
        for (var i = 0; i < els.length; i++)
            if (els[i].nodeType == 1)
                a.push(els[i]);
        return a;
    }
    function createNode(node, cdataWrap) {
        var type = node.nodeType;
        if (type == 1)
            return createElement(node, cdataWrap);
        if (type == 3)
            return fixTextNode(node.nodeValue);
        if (type == 4)
            return handleCDATA(cdataWrap, node.nodeValue);
        return null;
    }
    function handleCDATA(cdataWrap, s) {
        var el = document.createElement(cdataWrap);
        el.innerHTML = s;
        return el;
    }
    function fixTextNode(s) {
        return document.createTextNode(s);
    }
    function createElement(node, cdataWrap) {
        var e, tag = node.tagName.toLowerCase();
        if ($.browser.msie && !WebUI.isNormalIE9plus()) {
            var type = node.getAttribute('type');
            if (tag == 'table'
                || type == 'radio'
                || type == 'checkbox'
                || tag == 'button'
                || (tag == 'select' && node
                    .getAttribute('multiple'))) {
                var xxa = void 0;
                try {
                    xxa = copyAttrs(null, node, true);
                    e = document.createElement('<' + tag + ' '
                        + xxa + '>');
                }
                catch (xx) {
                    alert('err= ' + xx + ', ' + tag + ", " + xxa);
                }
            }
        }
        if (!e) {
            e = document.createElement(tag);
            copyAttrs(e, node, false);
        }
        if ($.browser.msie && tag == 'td') {
            var colspan = node.getAttribute('colspan');
            if (colspan)
                e.colSpan = parseInt(colspan);
        }
        if ($.browser.msie && !e.canHaveChildren) {
            if (node.childNodes.length > 0)
                e.text = node.text;
        }
        else {
            for (var i = 0, max = node.childNodes.length; i < max; i++) {
                var child = createNode(node.childNodes[i], cdataWrap);
                if (child)
                    e.appendChild(child);
            }
        }
        if ($.browser.msie || $.browser.opera) {
            if (tag == 'select'
                || (tag == 'option' && node
                    .getAttribute('selected')))
                e.taconiteTag = 1;
        }
        return e;
    }
    function copyAttrs(dest, src, inline) {
        for (var i = 0, attr = ''; i < src.attributes.length; i++) {
            var a = src.attributes[i], n = $.trim(a.name), v = $.trim(a.value);
            if (inline) {
                if (n.substring(0, 6) == 'domjs_') {
                    alert('Unsupported domjs_ attribute in INLINE mode: ' + n);
                }
                else {
                    if ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n) {
                        if (v != "")
                            attr += (n + '="' + v + '" ');
                    }
                    else
                        attr += (n + '="' + v + '" ');
                }
            }
            else if (n.substring(0, 6) == 'domjs_') {
                var s = "dest." + n.substring(6) + " = " + v;
                try {
                    eval(s);
                }
                catch (ex) {
                    alert('domjs_ eval failed: ' + ex + ", js=" + s);
                    throw ex;
                }
            }
            else if (v != "" && dest && ($.browser.msie || $.browser.webkit || ($.browser.mozilla && $.browser.majorVersion >= 9)) && n.substring(0, 2) == 'on') {
                try {
                    if (v.indexOf("javascript:") == 0)
                        v = $.trim(v.substring(11));
                    var fntext = v.indexOf("return") >= 0 ? v : "return " + v;
                    var se = void 0;
                    if ($.browser.msie && $.browser.majorVersion < 9)
                        se = new Function(fntext);
                    else
                        se = new Function("event", fntext);
                    dest[n] = se;
                }
                catch (x) {
                    alert('DomUI: Cannot set EVENT ' + n + " as " + v + ' on ' + dest + ": " + x);
                }
            }
            else if (n == 'style') {
                dest.style.cssText = v;
                dest.setAttribute(n, v);
            }
            else {
                if (v == "" && ("checked" == n || "selected" == n || "disabled" == n || "readonly" == n)) {
                    $(dest).removeAttr(n);
                }
                else {
                    $(dest).attr(n, v);
                }
            }
        }
        return attr;
    }
})(jQuery);
$(function () {
    $.getScript("$js/domui-date-checker.js");
});
function _block() {
    WebUI.blockUI();
}
function _unblock() {
    WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function () {
    WebUI.beforeUnload();
    return undefined;
});
{
    try {
        var v = $.browser.version.split(".");
        $.browser.majorVersion = parseInt(v[0], 10);
        $.browser.minorVersion = parseInt(v[1], 10);
        if (navigator.appName == 'Netscape') {
            var ua = navigator.userAgent;
            if (ua.indexOf("Trident/") != -1)
                $.browser.msie = true;
        }
        if (/Edge/.test(navigator.userAgent)) {
            $.browser.ieedge = true;
        }
    }
    catch (x) { }
}
var DomUI = WebUI;
$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete(function () {
    WebUI.handleCalendarChanges();
    WebUI.doCustomUpdates();
});
$(document).keydown(function (e) {
    WebUI.addPagerAccessKeys(e);
});
//# sourceMappingURL=domui-combined.js.map