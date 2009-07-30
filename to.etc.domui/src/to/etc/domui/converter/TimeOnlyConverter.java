package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class TimeOnlyConverter implements IConverter<Date> {
	public String convertObjectToString(Locale loc, Date in) throws UIException {
		Date dt = in;
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
		return df.format(dt);
	}

	public Date convertStringToObject(Locale loc, String in) throws UIException {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
		try {
			return (Date) df.parseObject(in);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
