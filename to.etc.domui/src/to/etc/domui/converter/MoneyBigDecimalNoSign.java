package to.etc.domui.converter;

import java.math.*;
import java.util.*;

import to.etc.domui.trouble.*;

public class MoneyBigDecimalNoSign implements IConverter<BigDecimal> {
	public String convertObjectToString(Locale loc, BigDecimal in) throws UIException {
		if(in == null)
			return null;
		return MoneyUtil.render(in, true, false, false);
	}

	/**
	 * Does a lax conversion of an amount to a BigDecimal. The input can contain anything from
	 * currency sign to thousand separators, decimal points etc.
	 *
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	public BigDecimal convertStringToObject(Locale loc, String in) throws UIException {
		return MoneyUtil.parseEuroToBigDecimal(in);
	}
}
