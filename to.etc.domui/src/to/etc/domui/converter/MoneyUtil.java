package to.etc.domui.converter;

import java.math.*;
import java.text.*;

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
	 * Parse into a double; return 0.0d for empty input.
	 * @param input
	 * @return
	 */
	static public double parseEuroToDouble(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		if(!ms.scanLaxEuro(input)) // Empty input returned as 0.0d
			return 0.0d;
		return Double.parseDouble(ms.getStringResult());
	}

	/**
	 * Parse into a double; return null for empty input.
	 *
	 * @param input
	 * @return
	 */
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
	 * Renders the value as a simple amount with the dot as a decimal point and always followed by
	 * 2 digits after the dot, rounded even (0.005 = +.01).
	 * @param v
	 * @return
	 */
	static public String renderAsSimpleDotted(double v) {
		BigDecimal bd = BigDecimal.valueOf(v);
		return bd.setScale(2, RoundingMode.HALF_EVEN).toString();
	}

	/**
	 * Render as a full value, including currency sign, thousands separator and all, using the
	 * specified currency locale.
	 * @param v
	 * @return
	 */
	static public String renderFullWithSign(double v) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale());
		return nf.format(v);
	}

	static public String renderFullWithSign(BigDecimal v) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(NlsContext.getCurrencyLocale());
		return nf.format(v);
	}
}
