/** InputDateCheck helper namespace 
Acceptable input: 
'/', '.' or '-' are accepted as separators; for brevity only '/' formats will be listed below:


13/3/2012, 23/02/2012 -> dd/mm/yyyy format; adapted to 13-3-2012 and 23-2-2012 leading 0 may be omitted i.e. 02/03/2012 equals 2/3/2012
13/3/13 -> dd/mm/yy format; adapted to 13-3-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may be omitted
13/3, 23/12 -> dd/mm format, adapted to 13-3-2012 and 23-12-2012, year is considered to be the current year; leading 0 may be omitted

05022013 -> ddmmyyyy format - adapted to 5-2-2013; leading 0 may NOT be omitted
050213 -> ddmmyy format, adapted to 5-2-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may NOT be omitted
0502 -> ddmmyy format, adapted to 5-2-2012, year is considered to be the current year; leading 0 may NOT be omitted
*/
var InputDateCheck = {
			
	/** *** DateInput control code *** */
	dateInputCheck : function(evt) {
		if (!evt) {
			evt = window.event;
			if (!evt) {
				return;
			}
		}
		var c = evt.target || evt.srcElement;
		InputDateCheck.dateInputRepairValueIn(c);
	},
	
	dateInputRepairValueIn : function(c) {
		if (!c)
			return;
		var val = c.value;
	
		if (!val || val.length == 0) // Nothing to see here, please move on.
			return;
		Calendar.__init();
	
		// -- Try to decode then reformat the date input
		var fmt = Calendar._TT["DEF_DATE_FORMAT"];
		try {
			var separatorsCount = InputDateCheck.countSeparators(val)
			if (separatorsCount < 2) {
				val = InputDateCheck.insertDateSeparators(val, fmt, separatorsCount);
			}
			var res = Date.parseDate(val, fmt);
			c.value = res.print(fmt);
		} catch (x) {
			alert(Calendar._TT["INVALID"]);
		}
	},
	
	/**
	 * Count of separator chars (anything else than letters and/or digits).
	 */
	countSeparators : function(str) {
		var count = 0;
		for ( var i = str.length; --i >= 0;) {
			if (InputDateCheck.isSeparator(str.charAt(i)))
				count++;
		}
		return count;
	},
	
	/**
	 * Returns T if char is anything else than letters and/or digits.
	 */
	isSeparator : function(c) {
		return !((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'));
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
			while (!InputDateCheck.isSeparator(str.charAt(index))) {
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
	}
	
}