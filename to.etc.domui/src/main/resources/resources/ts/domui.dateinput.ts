/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
/**
 * Acceptable input for dates:
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
namespace WebUI {
	declare let Calendar: any;

	/***** DateInput control code ****/
	export function dateInputCheckInput(evt) {
		if(!evt) {
			evt = window.event;
			if(!evt) {
				return;
			}
		}
		let c = evt.target || evt.srcElement;
		dateInputRepairValueIn(c);
	}

	export function dateInputRepairValueIn(c) {
		if(!c)
			return;
		let val = c.value;

		if(!val || val.length == 0) // Nothing to see here, please move on.
			return;
		Calendar.__init();

		// -- 20130425 jal if this is a date+time thing the value will hold space-separated time, so make sure to split it;
		let pos = val.indexOf(' ');
		let timeval = null;
		if(pos != -1) {
			// -- Split into trimmed time part and val = only date
			timeval = $.trim(val.substring(pos + 1));
			val = $.trim(val.substring(0, pos));
		}

		// -- Try to decode then reformat the date input
		try {
			val = $.trim(val);
			//Remove multiple separators
			val = val.replace(new RegExp("\\" + Calendar._TT["DATE_TIME_SEPARATOR"] + "+"), Calendar._TT["DATE_TIME_SEPARATOR"]);
			let numbereOfSpaces = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]).length - 1;
			let res;
			if(numbereOfSpaces == 0) {
				res = dateInputRepairDateValue(val);
			} else if(numbereOfSpaces == 1) {
				res = dateInputRepairDateTimeValue(val);
			} else {
				throw "date invalid";
			}
			c.value = res;
		} catch(x) {
			alert(Calendar._TT["INVALID"]);
		}
	}

	// /**
	//  * export function that checks is format valid after check that input has separators.
	//  */
	// export function parsingOfFormat(inputValue, format) {
	// 	// splits to array of alphanumeric "words" from an input (separators are non-alphanumeric characters)
	// 	let inputValueSplitted = inputValue.match(/(\w+)/g);
	// 	let formatWithoutPercentCharSplitted = format.replace(/%/g, "").match(/(\w+)/g);
	// 	let result = "";
	// 	for(let i = 0; i < formatWithoutPercentCharSplitted.length; i++) {
	// 		switch(formatWithoutPercentCharSplitted[i]) {
	// 			case "d":
	// 				result = formingResultForDayOrMonth(inputValueSplitted[i], result);
	// 				break;
	// 			case "m":
	// 				result = formingResultForDayOrMonth(inputValueSplitted[i], result);
	// 				break;
	// 			case "Y":
	// 				result = formingResultForYear(inputValueSplitted[i], result);
	// 				break;
	// 		}
	// 	}
	// 	result = insertDateSeparators(result, format);
	// 	return result;
	// }

	// export function formingResultForDayOrMonth(inputValue, result) {
	// 	if(!hasFieldInvalidFormat(inputValue)) {
	// 		return result = setDayOrMonthFormat(inputValue, result);
	// 	}
	// 	else {
	// 		throw "Invalid date";
	// 	}
	// }
	//
	// export function formingResultForYear(inputValue, result) {
	// 	let VALID_LENGTH_YEAR = 2;
	// 	if(inputValue.length == VALID_LENGTH_YEAR) {
	// 		return result = WebUI.setYearFormat(inputValue, result);
	// 	}
	// 	else {
	// 		throw "Invalid date";
	// 	}
	// }

	/**
	 * export function that checks is format valid of fields day and month.
	 */
	export function hasFieldInvalidFormat(inputValue) {
		let MAX_LENGTH = 2;
		let FORBIDDEN_CHARACTER = "0";

		return (inputValue.length === MAX_LENGTH && (inputValue.charAt(0) === FORBIDDEN_CHARACTER)) || (inputValue.length > MAX_LENGTH);
	}

	/**
	 * export function that converts day and month parts of input string that represents date.
	 */
	export function setDayOrMonthFormat(inputValue, result) {
		let NEEDED_CHARACTER_DAY_MONTH = "0";

		if(inputValue.length == 1) {
			result += NEEDED_CHARACTER_DAY_MONTH + inputValue;
		} else {
			result += inputValue;
		}
		return result;
	}

	/**
	 * export function that converts year part of input string that represents date.
	 */
	export function setYearFormat(inputValue, result) {
		let NEEDED_CHARACTER_YEAR = "20";

		return result += NEEDED_CHARACTER_YEAR + inputValue;
	}

	/**
	 *
	 */
	export function showCalendar(id, withtime) {
		let inp = document.getElementById(id) as HTMLInputElement;
		let params : any = {
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
			electric: false, // jal 20110125 Fixes bug 885- do not update the field when moving to prevent firing the change handler.
			step: 2,
			position: null,
			cache: false
		};

		// -- Try to show the selected date from the input field.
		let dateFmt = params.inputField ? params.ifFormat : params.daFormat;
		params.date = (Date as any).parseDate(inp.value, dateFmt);

		let cal = new Calendar(1, params.date, onDateSelect, function(cal) {
			cal.hide();
			cal.destroy();
		});
		cal.showsOtherMonths = false;
		cal.showsTime = withtime;
		cal.time24 = true;
		cal.params = params;
		// cal.params = params;
		cal.weekNumbers = true;
		cal.setRange(params.range[0], params.range[1]);
		// cal.setDateStatusHandler(params.dateStatusFunc);
		// cal.getDateText = params.dateText;
		// if (params.ifFormat) {
		// cal.setDateFormat(params.ifFormat);
		// }
		// if (params.inputField && typeof params.inputField.value == "string")
		// {
		// cal.parseDate(params.inputField.value);
		// }
		cal.create();
		cal.refresh();
		if(!params.position)
			cal.showAtElement(params.inputField, params.align);
		else
			cal.showAt(params.position[0], params.position[1]);
	}

	export function onDateSelect(cal) {
		let p = cal.params;
		let update = (cal.dateClicked || p.electric);
		if(update && p.inputField) {
			p.inputField.value = cal.date.print(p.ifFormat);
			if(typeof p.inputField.onchange == "function" && cal.dateClicked)
				p.inputField.onchange();
		}
		if(update && p.displayArea)
			p.displayArea.innerHTML = cal.date.print(p.daFormat);
		if(update && typeof p.onUpdate == "function")
			p.onUpdate(cal);
		if(update && p.flat) {
			if(typeof p.flatCallback == "function")
				p.flatCallback(cal);
		}
		if(update && p.singleClick && cal.dateClicked)
			cal.callCloseHandler();
	}

	/** *** DateInput control code *** */
	export function dateInputCheck(evt) {
		if(!evt) {
			evt = window.event;
			if(!evt) {
				return;
			}
		}
		let c = evt.target || evt.srcElement;
		dateInputRepairValueIn(c);
	}

	export function dateInputRepairDateValue(val) {
		let fmt = Calendar._TT["DEF_DATE_FORMAT"];
		let separatorsCount = countSeparators(val);
		if(separatorsCount < 2) {
			val = insertDateSeparators(val, fmt, separatorsCount);
		}
		let res = (Date as any).parseDate(val, fmt);
		if(!isYearInSupportedRange(res))
			throw "date invalid - distant year";

		return res.print(fmt);
	}

	export function dateInputRepairTimeValue(val) {
		let fmt = Calendar._TT["DEF_TIME_FORMAT"];
		let tempSep = "~";
		let count = getTimeSeparatorCount(val);
		switch(count) {
			default:
				throw "time has multiple separators";
			case 0:
				//add time separator before specified minutes
				let placeForSeparator = val.length - 2;
				val = [val.slice(0, placeForSeparator), tempSep, val.slice(placeForSeparator)].join('');
			case 1:
				let re = new RegExp("[" + Calendar._TT["TIME_SEPARATOR"] + "]", "g");
				val = val.replace(re, '~');
		}
		let dummyDate = new Date();
		dummyDate.setHours(val.split(tempSep)[0], val.split(tempSep)[1], 0, 0);
		return (dummyDate as any).print(fmt);
	}

	export function getTimeSeparatorCount(time) {
		let supportedTimeSeparators = Calendar._TT["TIME_SEPARATOR"];
		return (time.match(new RegExp("[" + supportedTimeSeparators + "]", "g")) || []).length;
	}

	export function dateInputRepairDateTimeValue(val) {
		let fmt = Calendar._TT["DEF_DATETIME_FORMAT"];

		let parts = val.split(Calendar._TT["DATE_TIME_SEPARATOR"]);
		let datePart = dateInputRepairDateValue(parts[0]);
		let timePart = dateInputRepairTimeValue(parts[1]);
		val = datePart + Calendar._TT["DATE_TIME_SEPARATOR"] + timePart;

		let res = (Date as any).parseDate(val, fmt);
		return res.print(fmt);
	}

	/**
	 * Count of separator chars (anything else than letters and/or digits).
	 */
	export function countSeparators(str) {
		let count = 0;
		for(let i = str.length; --i >= 0;) {
			if(isDateSeparator(str.charAt(i)))
				count++;
		}
		return count;
	}

	/**
	 * Returns T if char is anything else than letters and/or digits.
	 */
	export function isDateSeparator(c) {
		return Calendar._TT["DATE_SEPARATOR"].indexOf(c) > -1;
	}

	export function insertDateSeparators(str, fmt, separatorsCount) {
		let b = fmt.match(/%./g); // Split format items
		let len = str.length;
		let ylen;
		if(len == 8)
			ylen = 4;
		else if(len == 6)
			ylen = 2;
		else if(len >= 3 && len <= 5)
			ylen = 0;
		else
			throw "date invalid";
		// dd-mm dd/mm case - ignore existing separator
		if(separatorsCount == 1) {
			let index = 0;
			while(!isDateSeparator(str.charAt(index))) {
				index++;
				if(index > len - 1) {
					throw "invalid state";
				}
			}
			str = str.substring(0, index) + '-' + str.substring(index + 1);
		}
		// -- Edit the string according to the pattern,
		let res = "";
		for(let fix = 0; fix < b.length; fix++) {
			if(res.length != 0 && str.length != 0)
				res = res + '-'; // Just a random separator.
			switch(b[fix]) {
				default:
					throw "date invalid";
				case "%d":
				case "%e":
				case "%m":
					// Pre-existing dash separator or 2-digit day or month. Copy.
					let dashIndex = str.indexOf('-');
					let index = dashIndex == -1 ? 2 : dashIndex;
					let indexNext = dashIndex == -1 ? 2 : dashIndex + 1;
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

	export function isYearInSupportedRange(date) {
		if(date.getFullYear() < Calendar._TT["MIN_YEAR"] || date.getFullYear() > Calendar._TT["MAX_YEAR"]) {
			return false;
		} else {
			return true;
		}
	}
}
