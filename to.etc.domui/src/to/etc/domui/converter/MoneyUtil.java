package to.etc.domui.converter;

/**
 * Utility class to handle all kinds of monetary value presentation and conversion.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class MoneyUtil {
	/**
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
	 * @param input
	 * @return
	 */
	static public double parseEuroCurrency(String input) {
		MiniScanner ms = MiniScanner.getInstance();
		ms.scanLaxEuro(input);



		return 0.0d;
	}


}
