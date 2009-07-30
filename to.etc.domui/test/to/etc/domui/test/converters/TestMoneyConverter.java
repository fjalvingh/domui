package to.etc.domui.test.converters;

import java.util.*;

import org.junit.*;

import to.etc.domui.converter.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class TestMoneyConverter {
	@BeforeClass
	static public void setUp() {
		Locale nl = new Locale("nl", "NL");
		NlsContext.setCurrencyLocale(nl);
		NlsContext.setLocale(nl);
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	public void check(String in, String out) {
		MiniScanner ms = MiniScanner.getInstance();
		ms.scanLaxEuro(in);
		String res = ms.getStringResult();
		System.out.println("  ... " + in + " -> " + res);
		Assert.assertEquals(out, res);
	}

	/**
	 * Checks a conversion which must result in ValidationException and the proper code.
	 * @param in
	 */
	public void bad(String in) {
		try {
			MiniScanner ms = MiniScanner.getInstance();
			System.out.println(" ... " + in + " (bad)");
			ms.scanLaxEuro(in);
		} catch(ValidationException vx) {
			if(vx.getCode().equals(Msgs.V_BAD_AMOUNT))
				return;
		}
		Assert.fail("Validated an invalid amount: '" + in + "'");
	}

	/**
	 * Test INVALID conversions.
	 */
	@Test
	public void testBadConversions() {
		System.out.println("Testen van ONgeldige bedrag invoer-formaten");
		bad("\u20ac"); // Only euro sign is bad
		bad("abc"); // Letters are bad
		bad("1,00,000"); // Bad interpunction
		bad("1.00.000");
		bad("1.000.000.00");
		bad("1,000,000,00");
		bad("1,00.00");
		bad("1.00,00");
		bad("1..000");
		bad("1,,000");
		bad("1.,000");
		bad("1,.000");
		bad("1,000000,000.00");

		bad("1-100");
		bad("1000,-10,000.00");
	}

	/**
	 * Tests all VALID conversions.
	 */
	@Test
	public void testMoneyConversions() throws Exception {
		System.out.println("Testen van geldige bedrag invoer-formaten");
		check("", ""); // Empty string is allowed,
		check("\u20ac 1000", "1000"); // Leading euro with ws is allowed;
		check("  \u20ac 1000.89", "1000.89"); // Leading euro with ws is allowed;

		//-- Simple, dot is decimal point, valid
		check("1000", "1000");
		check("1000.", "1000");
		check("1000.0", "1000.0");
		check("1000.00", "1000.00");
		check("1000.99", "1000.99");
		check("1000.89", "1000.89");
		check("1000.8", "1000.8");

		//-- Simple, comma is decimal point, valid
		check("1000,", "1000");
		check("1000,0", "1000.0");
		check("1000,00", "1000.00");
		check("1000,99", "1000.99");
		check("1000,89", "1000.89");
		check("1000,8", "1000.8");

		//-- Simple, dot is thousands separator, valid
		check("1.000", "1000");
		check("1.000.000", "1000000");
		check("1.000.000.000", "1000000000");

		//-- Simple, comma is thousands separator, valid
		check("1,000", "1000");
		check("1,000,000", "1000000");
		check("1,000,000,000", "1000000000");

		//-- Clear distinction between thousands and decimal point using . as decimal point (>1 thousand separators)
		check("1,000,000.", "1000000");
		check("1,000,000.9", "1000000.9");
		check("1,000,786.76", "1000786.76");

		//-- Clear distinction between thousands and decimal point using , as decimal point (>1 thousand separators)
		check("1.000.000,", "1000000");
		check("1.000.000,9", "1000000.9");
		check("1.000.786,76", "1000786.76");

		//-- Unclear distinction (1 comma, one dot)
		check("1.000,00", "1000.00");
		check("1,000.00", "1000.00");

		//-- Negative numbers.
		check("-1000", "-1000");
		check("-1000.99", "-1000.99");
		check("1000-", "-1000");
		check("1000.99-", "-1000.99");
	}

	private void testSimple(double v, String exp) {
		String res = MoneyUtil.renderAsSimpleDotted(v);
		System.out.println("  ... " + v + " -> " + res);
		Assert.assertEquals(exp, res);
	}

	private void testFullSign(double v, String exp) {
		String res = MoneyUtil.renderFullWithSign(v);
		System.out.println("  ... " + v + " -> " + res);
		Assert.assertEquals(exp, res);
	}

	@Test
	public void testToString() {
		System.out.println("double naar string representatie: simpel");
		testSimple(1.00, "1.00");
		testSimple(1, "1.00");
		testSimple(1.0001, "1.00");
		testSimple(1.0049999, "1.00");
		testSimple(1.00, "1.00");
		testSimple(99999999999999.875, "99999999999999.88"); // Largest precision;

		System.out.println("double naar string representatie: full format");
		testFullSign(1234567.89, "\u20ac 1.234.567,89");
		testFullSign(1234567, "\u20ac 1.234.567,00");
		testFullSign(1234567.01, "\u20ac 1.234.567,01");
		testFullSign(1234567.1, "\u20ac 1.234.567,10");

		testFullSign(-1234567.89, "\u20ac -1.234.567,89");
		testFullSign(-1234567, "\u20ac -1.234.567,00");
		testFullSign(-1234567.01, "\u20ac -1.234.567,01");
		testFullSign(-1234567.1, "\u20ac -1.234.567,10");
	}

}
