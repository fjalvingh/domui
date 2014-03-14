/** WebUI helper namespace 
Acceptable input: 
'/', '.' or '-' are accepted as separators; for brevity only '/' formats will be listed below:


13/3/2012, 23/02/2012 -> dd/mm/yyyy format; adapted to 13-3-2012 and 23-2-2012 leading 0 may be omitted i.e. 02/03/2012 equals 2/3/2012
13/3/13 -> dd/mm/yy format; adapted to 13-3-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may be omitted
13/3, 23/12 -> dd/mm format, adapted to 13-3-2012 and 23-12-2012, year is considered to be the current year; leading 0 may be omitted

05022013 -> ddmmyyyy format - adapted to 5-2-2013; leading 0 may NOT be omitted
050213 -> ddmmyy format, adapted to 5-2-2013, year is considered to be 19yy if yy>29 or 20yy otherwise; leading 0 may NOT be omitted
0502 -> ddmmyy format, adapted to 5-2-2012, year is considered to be the current year; leading 0 may NOT be omitted
*/
var WebUI;
if(WebUI === undefined)
    WebUI = new Object();

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
		var fmt = Calendar._TT["DEF_DATE_FORMAT"];
		try {
			var res = "";
			var separatorsCount = WebUI.countSeparators(val);
			if (separatorsCount < 2) {
				val = WebUI.insertDateSeparators(val, fmt, separatorsCount);
				res = Date.parseDate(val, fmt);
			} else {
				try {
					var resultOfConversion = WebUI.parsingOfFormat(val, fmt);
					res = Date.parseDate(resultOfConversion, fmt);
				} catch(x) {
					res = Date.parseDate(val, fmt);
					return;
				}
			}
			c.value = res.print(fmt);
		} catch (x) {
			alert(Calendar._TT["INVALID"]);
		}
	},
	
	/**
	 * Function that checks is format valid after check that input has separators.
	 */
	
	parsingOfFormat: function(inputValue, format){
		// splits to array of alphanumeric "words" from an input (separators are non-alphanumeric characters)
		var inputValueSplitted = inputValue.match(/(\w+)/g);
		var formatWithoutPercentCharSplitted = format.replace(/%/g, "").match(/(\w+)/g);
		var result = "";
		for(var i = 0; i < formatWithoutPercentCharSplitted.length; i++){			
			switch(formatWithoutPercentCharSplitted[i]){
			case "d":
				result = WebUI.formingResultForDayOrMonth(inputValueSplitted[i], result);
				break;
			case "m":
				result = WebUI.formingResultForDayOrMonth(inputValueSplitted[i], result);
				break;
			case "Y":
				result = WebUI.formingResultForYear(inputValueSplitted[i], result);
				break;
			}
		}
		result = WebUI.insertDateSeparators(result, format);
		return result;	
	},
	
	formingResultForDayOrMonth: function(inputValue, result){
		if(!WebUI.hasFieldInvalidFormat(inputValue)){
			return result = WebUI.setDayOrMonthFormat(inputValue, result);
		}
		else{
			throw "Invalid date";
		}
	},
	
	formingResultForYear: function(inputValue, result){
		var VALID_LENGTH_YEAR = 2;
		if(inputValue.length == VALID_LENGTH_YEAR){
			return result = WebUI.setYearFormat(inputValue, result);
		}
		else{
			throw "Invalid date";
		}
	},
	
	/**
	 * Function that checks is format valid of fields day and month.
	 */
	hasFieldInvalidFormat: function(inputValue){
		var MAX_LENGTH = 2;
		var FORBIDDEN_CHARACTER = "0";
		
		return (inputValue.length === MAX_LENGTH && (inputValue.charAt(0) === FORBIDDEN_CHARACTER)) || (inputValue.length > MAX_LENGTH);
	},
	
	/**
	 * Function that converts day and month parts of input string that represents date.
	 */
	setDayOrMonthFormat: function(inputValue, result){
		var NEEDED_CHARACTER_DAY_MONTH = "0";
		
		if(inputValue.length == 1){
			result += NEEDED_CHARACTER_DAY_MONTH + inputValue;
		}else {
			result += inputValue;
		}
		return result;
	},
	
	/**
	 * Function that converts year part of input string that represents date.
	 */
	setYearFormat: function(inputValue, result){
		var NEEDED_CHARACTER_YEAR = "20";
		
		return result += NEEDED_CHARACTER_YEAR + inputValue;
	},
	
	/**
	 * Count of separator chars (anything else than letters and/or digits).
	 */
	countSeparators : function(str) {
		var count = 0;
		for ( var i = str.length; --i >= 0;) {
			if (WebUI.isSeparator(str.charAt(i)))
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
			while (!WebUI.isSeparator(str.charAt(index))) {
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
	
});
