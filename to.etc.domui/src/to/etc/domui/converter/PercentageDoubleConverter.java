package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This converts a Double containing a percentage amount to a full representation
 * including percentage sign.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Avg 11, 2009
 */
public class PercentageDoubleConverter implements IConverter<Double> {
	@Override
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		DecimalFormat df = new DecimalFormat("##0.00", dfs);
		StringBuilder sb = new StringBuilder(10);
		sb.append(df.format(in));
		sb.append('%');
		return sb.toString();
	}

	/**
	 * Does a conversion of an percentage amount to a double. The input can contain percentage value with or without trailing percentage sign.
	 * Input value must be between 0 and 100 including. Input value is rounded to two decimal spaces.
	 *
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Double convertStringToObject(Locale loc, String in) throws UIException {
		if(in == null)
			return null;

		in = in.trim();
		if(in.endsWith("%"))
			in = in.substring(0, in.length() - 1).trim();

		in = in.replace(',', '.'); // If percentage is entered with comma replace with dot

		try {
			double value = Double.parseDouble(in);
			value = Math.round(value * 100.0d) / 100.0d; // Truncate to 2 positions after the comma
			if(value <= 100.0d && value >= 0.0d)
				return Double.valueOf(value);
		} catch(NumberFormatException ex) {
		}
		throw new ValidationException(Msgs.V_BAD_PERCENTAGE, in);
	}
}
