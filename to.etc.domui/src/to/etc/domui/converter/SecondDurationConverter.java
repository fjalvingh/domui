package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class SecondDurationConverter implements IConverter<Long> {
	static private final long DAYS = 24 * 60 * 60;

	static private final long HOURS = 60 * 60;

	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		//		if(!(in instanceof Number))
		//			throw new IllegalStateException("Type must extend Number for this converter");
		long dlt = ((Number) in).longValue();
		//		boolean	sp = false;
		StringBuilder sb = new StringBuilder(20);
		if(dlt > DAYS) {
			sb.append(Long.toString(dlt / DAYS));
			sb.append("D ");
			dlt %= DAYS;
		}

		sb.append(Long.toString(dlt / HOURS)); // #of hours (0..23)
		dlt %= HOURS;
		sb.append(':');
		sb.append(StringTool.intToStr((int) (dlt / 60), 10, 2));
		dlt %= 60;
		if(dlt > 0) {
			sb.append(':');
			sb.append(StringTool.intToStr((int) dlt, 10, 2));
		}
		return sb.toString();
	}

	/**
	 * Convert a duration in the format: [xD] hh:mm[:ss] into a duration in seconds.
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Long convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		try {
			MiniScanner s = new MiniScanner();
			return Long.valueOf(s.scanDuration(input));
		} catch(ValidationException x) {
			throw x;
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
