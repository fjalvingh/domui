package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class TimeOnlyConverter implements IConverter {
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		Date dt = (Date) in;
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
		return df.format(dt);
	}

	public Object convertStringToObject(Locale loc, String in) throws UIException {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
		try {
			return df.parseObject(in);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
