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
//# sourceMappingURL=domui.crud.js.map
