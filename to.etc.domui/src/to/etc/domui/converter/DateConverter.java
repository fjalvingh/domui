package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class DateConverter implements IConverter<Date> {
	static private final ThreadLocal<DateFormat> m_format = new ThreadLocal<DateFormat>();

	static private final String DATE_PATTERN_NL = "dd-MM-yyyy";

	static private final String DATE_PATTERN_EN = "yyyy-MM-dd";

	static private DateFormat getFormatter() {
		DateFormat df = m_format.get();
		if(df == null) {
			df = new SimpleDateFormat(DATE_PATTERN_NL);
			m_format.set(df);
		}
		return df;
	}

	public String convertObjectToString(final Locale loc, final Date in) throws UIException {
		if(in == null) {
			return "";
		}
		//		if(!(in instanceof Date))
		//			throw new IllegalStateException("Type must be java.util.Date for this converter");
		Date dt = in;
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			return getFormatter().format(dt);
		} else if(loc.getLanguage().equalsIgnoreCase("en")) {
			return new SimpleDateFormat(DATE_PATTERN_EN).format(dt);
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, loc);
		return df.format(dt);
	}

	public Date convertStringToObject(final Locale loc, String input) throws UIException {
		String datePattern = null;

		if(input == null) {
			return null;
		}
		input = input.trim();
		if(input.length() == 0) {
			return null;
		}

		try {
			if(loc.getLanguage().equalsIgnoreCase("nl")) { // Default java date NLS code sucks utterly, it's worse than a black hole.
				datePattern = DATE_PATTERN_NL;
				return CalculationUtil.dutchDate(input);
			} else if(loc.getLanguage().equalsIgnoreCase("en")) {
				datePattern = DATE_PATTERN_EN;
				SimpleDateFormat enDateFormat = new SimpleDateFormat(DATE_PATTERN_EN);
				//vmijic 20090911 Set lenient to false to prevent wierd date recaclulations by java built in code.  
				enDateFormat.setLenient(false);
				return enDateFormat.parse(input);
			} else {
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, loc);
				if(dateFormat instanceof SimpleDateFormat) {
					datePattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
				}
				//vmijic 20090911 Set lenient to false to prevent wierd date recaclulations by java built in code.  
				dateFormat.setLenient(false);
				return dateFormat.parse(input);
			}
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE, datePattern);
		}
	}
}
