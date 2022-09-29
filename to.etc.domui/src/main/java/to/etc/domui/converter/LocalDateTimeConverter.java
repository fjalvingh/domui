package to.etc.domui.converter;

import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;
import to.etc.util.CalculationUtil;
import to.etc.util.DateUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-05-20.
 */
final public class LocalDateTimeConverter  implements IConverter<LocalDateTime> {
	static private final String DATE_PATTERN_NL = "dd-MM-yyyy HH:mm";

	static private final String DATE_PATTERN_EN = "yyyy-MM-dd HH:mm";


	@Override
	public String convertObjectToString(Locale loc, LocalDateTime in) throws UIException {
		if(in == null)
			return "";
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			DateTimeFormatter.ofPattern(DATE_PATTERN_NL).format(in);
		} else if(loc.getLanguage().equalsIgnoreCase("en")) {
			DateTimeFormatter.ofPattern(DATE_PATTERN_EN).format(in);
		}
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(in);
	}

	@Override
	public LocalDateTime convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;

		DateTimeFormatter df = null;
		String datePattern = null;
		try {
			if(loc.getLanguage().equalsIgnoreCase("nl")) {
				datePattern = DATE_PATTERN_NL;
				return DateUtil.toLocalDateTime(CalculationUtil.dutchDateAndTime(input));
			} else if(loc.getLanguage().equalsIgnoreCase("en")) {
				datePattern = DATE_PATTERN_EN;
				return LocalDateTime.parse(input, DateTimeFormatter.ofPattern(DATE_PATTERN_EN));
			} else {
				df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
				return LocalDateTime.parse(input, df);
			}
		} catch(Exception x) {
			if(datePattern == null && df != null) {
				datePattern = df.format(LocalDateTime.now());
			}
			throw new ValidationException(Msgs.vInvalidDate, datePattern);
		}
	}
}
