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
//# sourceMappingURL=domui.handlers.js.map