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
    function oddChar(obj) {
        WebUI.toClip(obj.innerHTML);
    }
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
    function popupSubmenuShow(parentId, submenu) {
        $(submenu).position({ my: 'left top', at: 'center top', of: parentId });
    }
    function registerPopinClose(id) {
        _popinCloseList.push(id);
        $(id).bind("mouseleave", popinMouseClose);
        if (_popinCloseList.length != 1)
            return;
        $(document.body).bind("keydown", popinKeyClose);
    }
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
    var _ckEditorMap = {};
    function registerCkEditorId(id, ckeInstance) {
        _ckEditorMap[id] = [ckeInstance, null];
    }
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
    function colorPickerDisable(id) {
    }
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
    function colorPickerChangeEvent(id) {
        window.clearTimeout(_colorTimer);
        _colorTimer = undefined;
        WebUI.valuechanged('eh', id);
    }
    function flare(id) {
        $('#' + id).fadeIn('fast', function () {
            $('#' + id).delay(500).fadeOut(1000, function () {
                $('#' + id).remove();
            });
        });
    }
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
    function scrollableTableReset(id, tblid) {
        var tbl = $('#' + tblid);
        var container = $('#' + id);
        tbl.floatThead('reflow');
        WebUI.doCustomUpdates();
        $.dbg('recreate');
        container.scrollTop(0);
    }
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
    })(DbPerformance || (DbPerformance = {}));
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.comp.js.map
