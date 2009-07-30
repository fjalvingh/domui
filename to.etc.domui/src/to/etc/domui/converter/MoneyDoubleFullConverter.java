package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

/**
 * This converts a Double containing a monetary amount to a full representation
 * including thousands separator and valuta indicator.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class MoneyDoubleFullConverter implements IConverter<Double> {
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;
		return MoneyUtil.renderFullWithSign(in.doubleValue());
	}

	/**
	 * Does a lax conversion of an amount to a double. The input can contain anything from
	 * currency sign to thousand separators, decimal points etc.
	 *
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	public Double convertStringToObject(Locale loc, String in) throws UIException {
		return MoneyUtil.parseEuroToDoubleW(in);
	}
}
