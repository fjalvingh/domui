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
        _busyTimer = setTimeout("WebUI.busyIndicate()", 250);
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
//# sourceMappingURL=domui.busy.js.map