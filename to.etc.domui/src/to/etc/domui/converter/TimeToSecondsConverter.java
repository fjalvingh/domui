package to.etc.domui.converter;

import java.util.*;
import java.util.regex.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Converts time in a format [hh:]mm into an integer holding the number of seconds.
 *
 * @author unknown?
 */
public class TimeToSecondsConverter implements IConverter<Integer> {

	static private final int HOURS = 60 * 60;

	final private Pattern m_pattern;

	private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3])[:]?[0-5][0-9]";

	public TimeToSecondsConverter() {
		m_pattern = Pattern.compile(TIME24HOURS_PATTERN);
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
			return "0:" + String.format("%02d", min);
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
		Matcher matcher = m_pattern.matcher(in);
		if(!matcher.matches()) {
			throw new ValidationException(Msgs.NOT_VALID, in);
		}

		//-- Format is hh[:]mm
		int pos = in.indexOf(':');
		try {
			String hs;
			String ms;
			if(pos == -1) {
				if(in.length() == 3) {
					// format hmm
					hs = in.substring(0, 1);
					ms = in.substring(1);
				} else {
					// format hhmm
					hs = in.substring(0, 2);
					ms = in.substring(2);
				}
			} else {
				// format [h]h:mm
				hs = in.substring(0, pos);
				ms = in.substring(pos + 1);
			}
			int h = Integer.parseInt(hs.trim());
			int m = Integer.parseInt(ms.trim());
			return Integer.valueOf((h * HOURS) + (m * 60));
		} catch(Exception x) {
			throw WrappedException.wrap(x); // Should not happen apparently.
		}
	}
}
