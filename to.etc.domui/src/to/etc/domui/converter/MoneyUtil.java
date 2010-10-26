package to.etc.domui.converter;

import java.math.*;
import java.text.*;
import java.util.*;

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

	/**
	 * Used for money scaling at two decimal precision on euro amounts.
	 */
	private static final int MONEY_SCALE_EURO = 2;

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
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).getMaximumFractionDigits();
		} else {
			return MONEY_SCALE_EURO;
		}
	}

	/**
	 * Use this method for money amount rounding purposes.
	 * @return returs used rounding mode.
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
	 * Parse into a double; return 0.0d for empty input.
	 * @param input
	 * @return
	 */
	@Deprecated
	static public double parseEuroToDouble(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input)) // Empty input returned as 0.0d
			return 0.0d;
		return Double.parseDouble(ms.getStringResult());
	}

	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Parse into a double; return null for empty input.
	 *
	 * @param input
	 * @return
	 */
	@Deprecated
	static public Double parseEuroToDoubleW(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input)) // Empty input returned as 0.0d
			return null;
		return Double.valueOf(ms.getStringResult());
	}

	/**
	 * Parse into a BigDecimal, return null for empty input.
	 * @param input
	 * @return
	 */
	static public BigDecimal parseEuroToBigDecimal(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input)) // Empty input returned as 0.0d
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
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}
		String s;
		if(symbol && thousands) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
			StringBuilder sb = new StringBuilder(20);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(symbol) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("##############0.00", dfs);
			StringBuilder sb = new StringBuilder(20);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(thousands) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
			s = df.format(v);
		} else {
			//-- No symbol, no thousands separators; just a #
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("##############0.00", dfs);
			s = df.format(v);
		}
		if(trunk) {
			if(s.endsWith(".00") || s.endsWith(",00"))
				return s.substring(0, s.length() - 3);
		}
		return s;
	}

	/**
	 *
	 * @param v
	 * @param thousands
	 * @param symbol
	 * @param trunk
	 * @return
	 */
	static public String render(BigDecimal v, boolean thousands, boolean symbol, boolean trunk) {
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}
		String s;
		if(symbol && thousands) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
			StringBuilder sb = new StringBuilder(20);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(symbol) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("##############0.00", dfs);
			StringBuilder sb = new StringBuilder(20);
			sb.append(NlsContext.getCurrencySymbol());
			sb.append('\u00a0');
			sb.append(df.format(v));
			s = sb.toString();
		} else if(thousands) {
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
			s = df.format(v);
		} else {
			//-- No symbol, no thousands separators; just a #
			DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
			DecimalFormat df = new DecimalFormat("##############0.00", dfs);
			s = df.format(v);
		}
		if(trunk) {
			if(s.endsWith(".00") || s.endsWith(",00"))
				return s.substring(0, s.length() - 3);
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
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}

		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
		StringBuilder sb = new StringBuilder(20);
		sb.append(NlsContext.getCurrencySymbol());
		sb.append('\u00a0');
		sb.append(df.format(v));
		String s = sb.toString();
		return s;
	}

	/**
	 * Render as a full value: [C -###,###,###.00], including currency sign, thousands separator and all, using the
	 * specified currency locale. It always renders the fraction.
	 * formatters suck.
	 * @param v
	 * @return
	 */
	static public String renderFullWithSign(BigDecimal v) {
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
		StringBuilder sb = new StringBuilder(20);
		sb.append(NlsContext.getCurrencySymbol());
		sb.append('\u00a0');
		sb.append(df.format(v));
		String s = sb.toString();
		if(s.endsWith(".00") || s.endsWith(",00"))
			return s.substring(0, s.length() - 3);
		return s;
	}

	/**
	 * Deprecated - do not use double for monetary amounts -
	 * Renders as a full value [C -###,###,###.##], but removes the fraction if it is all zeroes.
	 * @param v
	 * @return
	 */
	@Deprecated
	static public String renderTruncatedWithSign(double v) {
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}

		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
		StringBuilder sb = new StringBuilder(20);
		sb.append(NlsContext.getCurrencySymbol());
		sb.append('\u00a0');
		sb.append(df.format(v));
		String s = sb.toString();
		if(s.endsWith(".00") || s.endsWith(",00"))
			return s.substring(0, s.length() - 3);
		return s;
	}

	/**
	 * Renders as a full value [C -###,###,###.##], but removes the fraction if it is all zeroes.
	 * @param v
	 * @return
	 */
	static public String renderTruncatedWithSign(BigDecimal v) {
		if(!NlsContext.getCurrency().getCurrencyCode().equalsIgnoreCase("EUR")) {
			return NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale()).format(v);
		}
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale()); // Get numeric format symbols for the locale
		DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00", dfs);
		StringBuilder sb = new StringBuilder(20);
		sb.append(NlsContext.getCurrencySymbol());
		sb.append('\u00a0');
		sb.append(df.format(v));
		String s = sb.toString();
		if(s.endsWith(".00") || s.endsWith(",00"))
			return s.substring(0, s.length() - 3);
		return s;
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
		bdv = bdv.setScale(MoneyUtil.getMoneyScale(), getRoundingMode());
		return bdv.doubleValue();
	}

}
