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
//# sourceMappingURL=domui.webui.util.js.map
