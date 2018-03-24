/** WebUI helper namespace 
Acceptable input for dates: 
'/', '.' or '-' are accepted as separators; for brevity only '/' formats will be listed below:

Acceptable input for times:
':', '-', are accepted as separators;

For date time, only ' ' can be used for separating dates from time. 

===== DATE FORMATS =====
13/3/2012, 23/02/2012 -> dd/mm/yyyy format; adapted to 13-3-2012 and 23-2-2012 leading 0 may be omitted i.e. 02/03/2012 equals 2/3/2012
13/3/13 -> dd/mm/yy format; adapted to 13-3-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may be omitted
13/3, 23/12 -> dd/mm format, adapted to 13-3-2012 and 23-12-2012, year is considered to be the current year; leading 0 may be omitted

05022013 -> ddmmyyyy format - adapted to 5-2-2013; leading 0 may NOT be omitted
050213 -> ddmmyy format, adapted to 5-2-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may NOT be omitted
0502 -> ddmmyy format, adapted to 5-2-2012, year is considered to be the current year; leading 0 may NOT be omitted


===== TIME FORMATS =====
09:05	-> HH:MM format - standard time format. It'll not be adapted.
9:05	-> H:MM format, adapted to 09:05; you always have to specify 2 digit minutes, but leading 0-s for hours may be omitted

0905	-> HHMM format, adapted to 09:05.
905		-> HMM format, adapted to 09:05; you always have to specify 2 digit minutes, but leading 0-s for hours may be omitted
*/
var WebUI;
if(WebUI === undefined)
    WebUI = {};

$.extend(WebUI, {
	
	/** *** DateInput control code *** */
	dateInputCheck : function(evt) {
		if (!evt) {
			evt = window.event;
			if (!evt) {
				return;
			}
		}
		var c = evt.target || evt.srcElement;
		WebUI.dateInputRepairValueIn(c);
	},
	
	dateInputRepairValueIn : function(c) {
		if (!c)
			return;
		var val = c.value;
	
		if (!val || val.length == 0) // Nothing to see here, please move on.
			return;
		Calendar.__init();
	
		// -- Try to decode then reformat the date input
		try {
			val = $.trim(val);
			//Remove multiple separators
			val = val.replace(new RegExp("\\" +  Calendar._TT["DATE_TIME_SEPARATOR"] + "+"), Calendar._TT["DATE_TIME_SEPARATOR"]);
			var numbereOfSpaces = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]).length - 1;
			var res;
			if(numbereOfSpaces == 0){
				res = WebUI.dateInputRepairDateValue(val);
			} else if(numbereOfSpaces == 1){
				res = WebUI.dateInputRepairDateTimeValue(val);
			} else {
				throw "date invalid";
			}
			c.value = res;
		} catch (x) {
			alert(Calendar._TT["INVALID"]);
		}
	},
	
	
	dateInputRepairDateValue : function(val) {
		var fmt = Calendar._TT["DEF_DATE_FORMAT"];
		var separatorsCount = WebUI.countSeparators(val);
		if (separatorsCount < 2) {
			val = WebUI.insertDateSeparators(val, fmt, separatorsCount);
		}
		var res = Date.parseDate(val, fmt);
		if(!WebUI.isYearInSupportedRange(res))
			throw "date invalid - distant year";
			
		return res.print(fmt);
	},
	
	dateInputRepairTimeValue : function(val) {
		var fmt = Calendar._TT["DEF_TIME_FORMAT"];
		var tempSep = "~";
		var count = WebUI.getTimeSeparatorCount(val);
		switch(count) {
			default:
				throw "time has multiple separators";
			case 0:
				//add time separator before specified minutes
				var placeForSeparator = val.length - 2;
				val = [val.slice(0, placeForSeparator), tempSep, val.slice(placeForSeparator)].join('');
			case 1:
				var re = new RegExp("[" + Calendar._TT["TIME_SEPARATOR"] + "]","g");
				val = val.replace(re, '~');
			}
		var dummyDate = new Date();
		dummyDate.setHours(val.split(tempSep)[0], val.split(tempSep)[1], 0, 0);
		return dummyDate.print(fmt);
	},
	
	getTimeSeparatorCount : function(time){
		var supportedTimeSeparators = Calendar._TT["TIME_SEPARATOR"];
		return (time.match(new RegExp("[" + supportedTimeSeparators + "]", "g")) || []).length;
	},
	
	dateInputRepairDateTimeValue : function(val) {
		var fmt = Calendar._TT["DEF_DATETIME_FORMAT"];
		
		var parts = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]);
		var datePart = WebUI.dateInputRepairDateValue(parts[0]);
		var timePart = WebUI.dateInputRepairTimeValue(parts[1]);
		val = datePart + Calendar._TT["DATE_TIME_SEPARATOR"] + timePart;
		
		var res = Date.parseDate(val, fmt);
		return res.print(fmt);
	},
	
	/**
	 * Count of separator chars (anything else than letters and/or digits).
	 */
	countSeparators : function(str) {
		var count = 0;
		for ( var i = str.length; --i >= 0;) {
			if (WebUI.isDateSeparator(str.charAt(i)))
				count++;
		}
		return count;
	},
	
	/**
	 * Returns T if char is anything else than letters and/or digits.
	 */
	isDateSeparator : function(c) {
		return Calendar._TT["DATE_SEPARATOR"].indexOf(c) > -1;
	},
	
	insertDateSeparators : function(str, fmt, separatorsCount) {
		var b = fmt.match(/%./g); // Split format items
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
		// dd-mm dd/mm case - ignore existing separator
		if (separatorsCount == 1) {
			var index = 0;
			while (!WebUI.isDateSeparator(str.charAt(index))) {
				index++;
				if (index > len - 1) {
					throw "invalid state";
				}
			}
			str = str.substring(0, index) + '-' + str.substring(index + 1);
		}
		// -- Edit the string according to the pattern,
		var res = "";
		for ( var fix = 0; fix < b.length; fix++) {
			if (res.length != 0 && str.length != 0)
				res = res + '-'; // Just a random separator.
			switch (b[fix]) {
			default:
				throw "date invalid";
			case "%d":
			case "%e":
			case "%m":
				// Pre-existing dash separator or 2-digit day or month. Copy.
				var dashIndex = str.indexOf('-');
				var index = dashIndex == -1 ? 2 : dashIndex;
				var indexNext = dashIndex == -1 ? 2 : dashIndex + 1;
				res += str.substring(0, index);
				str = str.substring(indexNext);
				break;
			case '%y':
			case '%Y':
				// -- 2- or 4 digit year,
				res += str.substring(0, ylen);
				str = str.substring(ylen);
				break;
			}
		}
		return res;
	},
	
	isYearInSupportedRange : function(date){
		if(date.getFullYear() < Calendar._TT["MIN_YEAR"] || date.getFullYear() > Calendar._TT["MAX_YEAR"]){
			return false;
		} else {
			return true;
		}
	}
	
});
