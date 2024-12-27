package to.etc.domui.converter;

import to.etc.domui.trouble.UIException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-05-20.
 */
final public class ZonedDateTimeConverter implements IConverter<ZonedDateTime> {
	static private final String DATE_PATTERN_NL = "dd-MM-yyyy HH:mm";

	static private final String DATE_PATTERN_EN = "yyyy-MM-dd HH:mm";


	@Override
	public String convertObjectToString(Locale loc, ZonedDateTime in) throws UIException {
		if(in == null)
			return "";
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			return DateTimeFormatter.ofPattern(DATE_PATTERN_NL).format(in);
		} else if(loc.getLanguage().equalsIgnoreCase("en")) {
			return DateTimeFormatter.ofPattern(DATE_PATTERN_EN).format(in);
		}
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(in);
	}

	@Override
	public ZonedDateTime convertStringToObject(Locale loc, String input) throws UIException {
		throw new IllegalStateException("Not implemented");
		//if(input == null)
		//	return null;
		//input = input.trim();
		//if(input.length() == 0)
		//	return null;
		//
		//DateTimeFormatter df = null;
		//String datePattern = null;
		//try {
		//	if(loc.getLanguage().equalsIgnoreCase("nl")) {
		//		datePattern = DATE_PATTERN_NL;
		//		return DateUtil.toZonedDateTime(CalculationUtil.dutchDateAndTime(input));
		//	} else if(loc.getLanguage().equalsIgnoreCase("en")) {
		//		datePattern = DATE_PATTERN_EN;
		//		return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(DATE_PATTERN_EN));
		//	} else {
		//		df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
		//		return LocalDateTime.parse(input, df);
		//	}
		//} catch(Exception x) {
		//	if(datePattern == null && df != null) {
		//		datePattern = df.format(LocalDateTime.now());
		//	}
		//	throw new ValidationException(Msgs.V_INVALID_DATE, datePattern);
		//}
	}
}
