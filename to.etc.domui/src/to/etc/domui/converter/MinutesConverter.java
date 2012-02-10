package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.webapp.nls.*;

/**
 * Converts a double presentation to minutes considering
 * whole part of number as number of hours, and
 * digits after decimal point as minutes.
 * 1.3-> 63; 1.30-> 90;1.70-> ValidationException
 *
 * @author <a href="mailto:jsavic@execom.eu">Jelena Savic</a>
 * Created on Feb 9, 2012
 */
public class MinutesConverter implements IConverter<Double> {

	/**
	 *
	 * @see to.etc.domui.converter.IObjectToStringConverter#convertObjectToString(java.util.Locale, java.lang.Object)
	 */
	@Override
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;

		double value;
		DecimalFormat df;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale

		int hours = in.intValue() / 60; // #of hours (digits before floating point)
		double mins = in.doubleValue() % 60; // #of minutes (digits after floating point)

		//resolve format
		if(mins < 10) {
			value = hours + mins / 10;
			df = new DecimalFormat("##0.0", dfs);
		} else {
			value = hours + mins / 100;
			df = new DecimalFormat("##0.00", dfs);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(df.format(value));
		return sb.toString();
	}

	/**
	 * Does a conversion of double number to basically integer presentation of #of minutes.
	 * Input value must have value after floating point between 0 and 0.59.
	 *
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Double convertStringToObject(Locale loc, String in) throws UIException {
		if(in == null)
			return null;

		in = in.trim();
		in = in.replace(',', '.'); // If value is entered with comma replace it with dot

		try {
			double value = Double.parseDouble(in);
			double hours = Math.floor(value);

			// resolve #of decimals
			int numDec = 0;
			final int index = in.indexOf('.');
			if(index >= 0) {
				numDec = in.length() - 1 - index;
			}
			double mins = (value - hours);
			for(int i = 0; i < numDec; i++) {
				mins *= 10;
			}
			if(mins >= 0 & mins < 60) {
				value = hours * 60 + mins;
				return Double.valueOf(value);
			}
		} catch(NumberFormatException ex) {}
		throw new ValidationException("Expected value for minutes ranges between 0 and 59!", in);
	}
}
