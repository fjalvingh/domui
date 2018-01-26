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
    function hasFieldInvalidFormat(inputValue) {
        var MAX_LENGTH = 2;
        var FORBIDDEN_CHARACTER = "0";
        return (inputValue.length === MAX_LENGTH && (inputValue.charAt(0) === FORBIDDEN_CHARACTER)) || (inputValue.length > MAX_LENGTH);
    }
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
    function setYearFormat(inputValue, result) {
        var NEEDED_CHARACTER_YEAR = "20";
        return result += NEEDED_CHARACTER_YEAR + inputValue;
    }
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
    function getTimeSeparatorCount(time) {
        var supportedTimeSeparators = Calendar._TT["TIME_SEPARATOR"];
        return (time.match(new RegExp("[" + supportedTimeSeparators + "]", "g")) || []).length;
    }
    function dateInputRepairDateTimeValue(val) {
        var fmt = Calendar._TT["DEF_DATETIME_FORMAT"];
        var parts = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]);
        var datePart = dateInputRepairDateValue(parts[0]);
        var timePart = dateInputRepairTimeValue(parts[1]);
        val = datePart + Calendar._TT["DATE_TIME_SEPARATOR"] + timePart;
        var res = Date.parseDate(val, fmt);
        return res.print(fmt);
    }
    function countSeparators(str) {
        var count = 0;
        for (var i = str.length; --i >= 0;) {
            if (isDateSeparator(str.charAt(i)))
                count++;
        }
        return count;
    }
    function isDateSeparator(c) {
        return Calendar._TT["DATE_SEPARATOR"].indexOf(c) > -1;
    }
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
    function isYearInSupportedRange(date) {
        if (date.getFullYear() < Calendar._TT["MIN_YEAR"] || date.getFullYear() > Calendar._TT["MAX_YEAR"]) {
            return false;
        }
        else {
            return true;
        }
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.dateinput.js.map