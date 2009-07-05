package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class DateTimeConverter implements IConverter {
	static private final ThreadLocal<DateFormat> m_format = new ThreadLocal<DateFormat>();

	static private DateFormat getFormatter() {
		DateFormat df = m_format.get();
		if(df == null) {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			m_format.set(df);
		}
		return df;
	}


	public String convertObjectToString(Locale loc, Object in) throws UIException {
		if(in == null)
			return "";
		if(!(in instanceof Date))
			throw new IllegalStateException("Type must be java.util.Date for this converter");
		Date dt = (Date) in;
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			return getFormatter().format(dt);
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
		return df.format(dt);
	}

	public Object convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
		try {
			if(loc.getLanguage().equalsIgnoreCase("nl"))
				df = getFormatter();
			//				return CalculationUtil.dutchDate(input);
			return df.parse(input);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
