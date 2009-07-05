package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class DateConverter implements IConverter {
	static private final ThreadLocal<DateFormat> m_format = new ThreadLocal<DateFormat>();

	static private DateFormat getFormatter() {
		DateFormat df = m_format.get();
		if(df == null) {
			df = new SimpleDateFormat("dd-MM-yyyy");
			m_format.set(df);
		}
		return df;
	}

	public String convertObjectToString(final Locale loc, final Object in) throws UIException {
		if(in == null)
			return "";
		if(!(in instanceof Date))
			throw new IllegalStateException("Type must be java.util.Date for this converter");
		Date dt = (Date) in;
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			return getFormatter().format(dt);
		} else if(loc.getLanguage().equalsIgnoreCase("en"))
			return new SimpleDateFormat("yyyy-MM-dd").format(dt);

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, loc);
		return df.format(dt);
	}

	public Object convertStringToObject(final Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		try {
			if(loc.getLanguage().equalsIgnoreCase("nl")) // Default java date NLS code sucks utterly, it's worse than a black hole.
				return CalculationUtil.dutchDate(input);
			else if(loc.getLanguage().equalsIgnoreCase("en"))
				return new SimpleDateFormat("yyyy-MM-dd").parse(input);
			else
				return DateFormat.getDateInstance(DateFormat.SHORT, loc).parse(input);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
