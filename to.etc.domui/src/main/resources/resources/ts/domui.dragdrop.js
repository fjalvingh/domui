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
    function dragResetTimer() {
        dragClearTimer();
        _dragTimer = setTimeout("WebUI.dragTimerFired()", 250);
    }
    function dragClearTimer() {
        if (_dragTimer) {
            clearTimeout(_dragTimer);
            _dragTimer = undefined;
        }
    }
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
    function findDropZoneHandler(type) {
        if (type == "ROW")
            return _ROW_DROPZONE_HANDLER;
        return _DEFAULT_DROPZONE_HANDLER;
    }
    function dropClearZone() {
        if (_currentDropZone) {
            _currentDropZone._drophandler.unmark(_currentDropZone);
            _currentDropZone = undefined;
        }
    }
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
    function dropClearList() {
        _dropList = undefined;
    }
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
    function dropRemoveNonsense(body) {
        for (var i = body.childNodes.length; --i >= 0;) {
            var n = body.childNodes[i];
            if (n.nodeName == '#text')
                body.removeChild(n);
        }
    }
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
//# sourceMappingURL=domui.dragdrop.js.map