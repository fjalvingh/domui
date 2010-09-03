package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;

/**
 * Converter for {@link NumericPresentation#MONEY_NO_SYMBOL}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public class MoneyDoubleNoSign implements IConverter<Double> {
	@Override
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;
		return MoneyUtil.render(in.doubleValue(), true, false, false);
	}

	/**
	 * Does a lax conversion of an amount to a double. The input can contain anything from
	 * currency sign to thousand separators, decimal points etc.
	 *
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Double convertStringToObject(Locale loc, String in) throws UIException {
		return MoneyUtil.parseEuroToDoubleW(in);
	}
}
