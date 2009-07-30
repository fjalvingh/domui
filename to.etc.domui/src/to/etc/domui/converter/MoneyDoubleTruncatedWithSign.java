package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

/**
 * Converter converting to a full money representation, including currency sign, thousands separators; this removes any zero fraction
 * so 1000.00 is rendered like E 1,000
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public class MoneyDoubleTruncatedWithSign implements IConverter<Double> {
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;
		return MoneyUtil.renderTruncatedWithSign(in.doubleValue());
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
