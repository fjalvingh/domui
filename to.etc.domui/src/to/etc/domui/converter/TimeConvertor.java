package to.etc.domui.converter;

import java.util.*;
import java.util.regex.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class TimeConvertor implements IConverter<Integer> {

	static private final int HOURS = 60 * 60;

	private Pattern pattern;

	private Matcher matcher;

	private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	public TimeConvertor() {
		pattern = Pattern.compile(TIME24HOURS_PATTERN);
	}

	/**
	 * Coverts seconds to a time with format hh:mm.
	 * @see to.etc.domui.converter.IObjectToStringConverter#convertObjectToString(java.util.Locale, java.lang.Object)
	 */
	@Override
	public String convertObjectToString(Locale loc, Integer in) {
		if(in == null)
			return "";

		int time = in.intValue();
		if(time >= HOURS) {
			int hours = Math.abs(time / HOURS);
			Integer min = new Integer((time % HOURS) / 60);
			return hours + ":" + String.format("%02d", min);
		} else {
			Integer min = new Integer(time / 60);
			return "00:" + String.format("%02d", min);
		}
	}

	/**
	 * Coverts a time with format hh:mm to number of seconds
	 * @see to.etc.domui.converter.IObjectToStringConverter#convertObjectToString(java.util.Locale, java.lang.Object)
	 */
	@Override
	public Integer convertStringToObject(Locale loc, String in) throws UIException {
		if(in == null || in.equals(""))
			return null;
		matcher = pattern.matcher(in);
		if(!matcher.matches()) {
			throw new ValidationException(Msgs.NOT_VALID, in);
		}

		//-- Format is [hh:]mm
		int pos = in.indexOf(':');
		try {
			if(pos == -1)
				return Integer.decode(in); // Time is in minutes.
			String hs = in.substring(0, pos);
			String ms = in.substring(pos + 1);
			int h = Integer.parseInt(hs.trim());
			int m = Integer.parseInt(ms.trim());
			return Integer.valueOf((h * HOURS) + (m * 60));
		} catch(Exception x) {
			return null;
		}
	}

}
