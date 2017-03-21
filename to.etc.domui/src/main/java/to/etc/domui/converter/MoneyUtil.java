/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.converter;

import java.math.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * Utility class to handle all kinds of monetary value presentation and conversion.
 * This parses a monetary amount entered in a string in a very lax way, to allow
 * for maximal ease of use in entering currency amounts. It obeys most of the rules
 * for european (euro) amounts but has a few specials:
 * <ul>
 *	<li>Any leading currency sign (euro sign) is skipped. Only the euro sign is allowed before the amount!</li>
 *	<li>The dot and comma can both be used as either decimal point and thousands separator. If you use the
 *		one as decimal point then the other, when present, <i>must</i> represent the thousands separator and
 *		it is only allowed at the correct positions in the number.</li>
 *	<li>If a decimal point (comma or dot) is present it can be followed by no digits, one digit like .5 which will be translated
 *		as .50 or two digits. Anything else is an error.</li>
 *	<li>If thousands separators are present they must be fully valid, meaning if one exists all others must exist too, and
 *		all of them <i>must</i> be present at the correct location in the string.</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class MoneyUtil {
	//
	//	/**
	//	 * Used for money scaling at two decimal precision on euro amounts.
	//	 */
	//	private static final int MONEY_SCALE_EURO = 2;

	/**
	 * Parse a monetary value and return the proper value type, either Double or BigDecimal.
	 * FIXME Localization failure.
	 *
	 * @param <T>
	 * @param valueClass
	 * @param in
	 * @return
	 */
	static public <T> T parseMoney(Class<T> valueClass, String in) {
		if(DomUtil.isDoubleOrWrapper(valueClass))
			return (T) parseEuroToDoubleW(in);
		else if(BigDecimal.class == valueClass)
			return (T) parseEuroToBigDecimal(in);
		else
			throw new IllegalArgumentException("The value class " + valueClass + " is not a valid monetary type");
	}

	/**
	 * Use this method for money amount rounding purposes.
	 * @return Returns used maximum fraction digits value.
	 */
	public static int getMoneyScale() {
		return NlsContext.getCurrency().getDefaultFractionDigits();
	}

	/**
	 * Use this method for money amount rounding purposes.
	 * @return rounding mode to use for currency.
	 */
	public static RoundingMode getRoundingMode() {
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).getRoundingMode();
		} else {
			return RoundingMode.HALF_EVEN;
		}
	}

	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Parse into a double; return null for empty input.
	 * FIXME Localization failure!
	 *
	 * @param input
	 * @return
	 */
	@Deprecated
	static public Double parseEuroToDoubleW(String input) {
		return parseEuroToDoubleW(input, getMoneyScale(), false);
	}

	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Parse into a double; return null for empty input.
	 * FIXME Localization failure!
	 *
	 * @param input
	 * @param scale in use
	 * @param useStrictScale in case of T, if input does not have strict number (defined by scale) of decimal digits after decimal point, it would be rejected as invalid value.
	 * @return
	 */
	@Deprecated
	static public Double parseEuroToDoubleW(String input, int scale, boolean useStrictScale) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxWithCurrencySign(input, scale, useStrictScale))
			return null;
		return Double.valueOf(ms.getStringResult());
	}

	/**
	 * Parse into a BigDecimal, return null for empty input.
	 * @param input
	 * @return
	 */
	static public BigDecimal parseEuroToBigDecimal(String input) {
		return parseEuroToBigDecimal(input, getMoneyScale(), false);
	}

	/**
	 * Parse into a BigDecimal, return null for empty input.
	 * @param input
	 * @param scale
	 * @param useStrictScale in case of T, if input does not have strict number (defined by scale) of decimal digits after decimal point, it would be rejected as invalid value.
	 * @return
	 */
	static public BigDecimal parseEuroToBigDecimal(String input, int scale, boolean useStrictScale) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxWithCurrencySign(input, scale, useStrictScale)) // Empty input returned as 0.0d
			return null;
		return new BigDecimal(ms.getStringResult());
	}

	/**
	 * FIXME jal 20091221 This cannot be here- currency precision is dependent on the currency used AND the function it is used in (rounding depends on where you are rounding).
	 * Renders the value as a simple amount with the dot as a decimal point and always followed by
	 * 2 digits after the dot, rounded even (0.005 = +.01).
	 * @param v
	 * @return
	 */
	@Deprecated
	static public String renderAsSimpleDotted(double v) {
		BigDecimal bd = BigDecimal.valueOf(v);
		return bd.setScale(getMoneyScale(), getRoundingMode()).toString();
	}

	/**
	 * Deprecated - do not use double for monetary amounts.
	 * @param v
	 * @param thousands
	 * @param symbol
	 * @param trunk
	 * @return
	 */
	@Deprecated
	static public String render(double v, boolean thousands, boolean symbol, boolean trunk) {
		return render(BigDecimal.valueOf(v), thousands, symbol, trunk);
	}

	/**
	 * Convert the BigDecimal to a formatted monetary value.
	 * @param v
	 * @param thousands		Render thousand separators where needed
	 * @param symbol		Render the currency sign
	 * @param trunk			If the fraction is all zeroes, remove the fraction.
	 * @return
	 */
	static public String render(BigDecimal v, boolean thousands, boolean symbol, boolean trunk) {
		//-- Depending on the currency's default fraction size, create an "end mask"
		StringBuilder sb = new StringBuilder(20);
		Currency c = NlsContext.getCurrency();
		int nfrac = c.getDefaultFractionDigits();
		if(nfrac == -1)
			nfrac = 0; // Sigh
		String fracmask;
		if(nfrac == 0) {
			fracmask = ""; // Do not include any fraction.
		} else {
			sb.append('.');
			for(int i = nfrac; --i >= 0;)
				sb.append('0');
			fracmask = sb.toString();
			sb.setLength(0);
		}

		//-- Now convert.
		String s;
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		if(symbol && thousands) {
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0" + fracmask, dfs);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(symbol) {
			DecimalFormat df = new DecimalFormat("##############0" + fracmask, dfs);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(thousands) {
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0" + fracmask, dfs);
			s = df.format(v);
		} else {
			//-- No symbol, no thousands separators; just a #
			DecimalFormat df = new DecimalFormat("##############0" + fracmask, dfs);
			s = df.format(v);
		}
		if(trunk && nfrac > 0) {
			//-- If the fraction is rendered as .00000000 whatever we need to truncate it..
			if(dfs.getDecimalSeparator() == '.') {
				if(s.endsWith(fracmask))
					s = s.substring(0, s.length() - nfrac - 1);
			} else {
				String match = dfs.getDecimalSeparator() + fracmask.substring(1);
				if(s.endsWith(match))
					s = s.substring(0, s.length() - nfrac - 1);
			}
		}
		return s;
	}


	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Render as a full value: [C -###,###,###.00], including currency sign, thousands separator and all, using the
	 * specified currency locale. It always renders the fraction.
	 * formatters suck.
	 * @param v
	 * @return
	 */
	@Deprecated
	static public String renderFullWithSign(double v) {
		return render(BigDecimal.valueOf(v), true, true, false);
	}

	/**
	 * Render as a full value: [C -###,###,###.00], including currency sign, thousands separator and all, using the
	 * specified currency locale. It always renders the fraction.
	 * formatters suck.
	 * @param v
	 * @return
	 */
	static public String renderFullWithSign(BigDecimal v) {
		return render(v, true, true, false);
	}

	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Renders as a full value [C -###,###,###.##], but removes the fraction if it is all zeroes.
	 * @param v
	 * @return
	 */
	@Deprecated
	static public String renderTruncatedWithSign(double v) {
		return render(BigDecimal.valueOf(v), true, true, true);
	}

	/**
	 * Renders as a full value [C -###,###,###.##], but removes the fraction if it is all zeroes.
	 * @param v
	 * @return
	 */
	static public String renderTruncatedWithSign(BigDecimal v) {
		return render(v, true, true, true);
	}

	public static void main(String[] args) {
		Locale l = new Locale("nl", "NL");
		NlsContext.setLocale(l);
		NlsContext.setCurrencyLocale(l);
		String s = renderFullWithSign(new BigDecimal("123.45"));
		System.out.println(">> " + s);
	}

	/**
	 * Deprecated - do not use double for monetary amounts.
	 * @param value
	 * @return
	 */
	@Deprecated
	public static double roundValue(double value) {
		BigDecimal bdv = BigDecimal.valueOf(value);
		return roundValue(bdv).doubleValue();
	}

	@Nonnull
	public static BigDecimal roundValue(@Nonnull BigDecimal value) {
		return value.setScale(MoneyUtil.getMoneyScale(), getRoundingMode());
	}

	/**
	 * This operation compares two double values for equality.
	 * Because floating point calculations may involve rounding, calculated double values may not be accurate, so we need to compare with certain tolerance.
	 * Since we are using rounding of monetary calculations with predefined precisions, we reuse same tolerance here, so both specified double values are compared as rounded monetary values.
	 *
	 * @param value1
	 * @param value2
	 * @return
	 */
	public static boolean areRoundedValuesEqual(double value1, double value2) {
		return (roundValue(value1) == roundValue(value2));
	}

	public static boolean areRoundedValuesEqual(@Nonnull BigDecimal value1, @Nonnull BigDecimal value2) {
		return (roundValue(value1).equals(roundValue(value2)));
	}

	/**
	 * Checks if specified amount is zero value, when built-in rounding mode and scale are applied.
	 * @param value
	 * @return
	 */
	public static boolean isRoundedAsZero(@Nonnull BigDecimal value) {
		return (roundValue(BigDecimal.ZERO).equals(roundValue(value)));
	}

	@SuppressWarnings({"unchecked"})
	static public <T> void assignMonetaryConverter(final PropertyMetaModel<T> pmm, boolean editable, final IConvertable<T> node) {
		if(pmm.getConverter() != null)
			node.setConverter(pmm.getConverter());
		else {
			NumericPresentation np = null;
			if(!editable)
				np = pmm.getNumericPresentation();
			if(np == null)
				np = NumericPresentation.MONEY_NUMERIC;

			if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class) {
				node.setConverter((IConverter<T>) MoneyConverterFactory.createDoubleMoneyConverters(np));
			} else if(pmm.getActualType() == BigDecimal.class) {
				node.setConverter((IConverter<T>) MoneyConverterFactory.createBigDecimalMoneyConverters(np));
			} else
				throw new IllegalStateException("Cannot handle type=" + pmm.getActualType() + " for monetary types");
		}
	}

}
