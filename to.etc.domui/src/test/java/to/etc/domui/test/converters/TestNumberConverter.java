package to.etc.domui.test.converters;

import java.io.*;
import java.math.*;
import java.net.*;
import java.text.*;
import java.util.*;

import org.junit.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * All kinds of conversions tests.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 18 Jul 2011
 */

public class TestNumberConverter {
	private static final String[] BAD_NUMBER = {"\u20ac", "\u20ac 1,000.00", "abc", "1,00,000", "1.00.000", "1.000.000.00", "1,000,000,00", "1,00.00", "1.00,00", "1..000", "1,,000", "1.,000", "1,.000",
		"1,000000,000.00", "1-100", "1000,-10,000.00"};

	private static final String[] BAD_MONEY = {"\u20ac", "abc", "1,00,000", "1.00.000", "1.000.000.00", "1,000,000,00", "1,00.00", "1.00,00", "1..000", "1,,000", "1.,000", "1,.000", "1,000000,000.00", "1-100",
		"1000,-10,000.00"};

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
	public <T extends Number> void check(IConverter<T> nc, String in, String out, NumericPresentation np, int scale, int minScale, boolean monetary) {
		if(monetary && !NumericPresentation.isMonetary(np)) {
			bad(nc, in, false);
		} else if(minScale > scale) {
			bad(nc, in, NumericPresentation.isMonetary(np));
		} else {
			good(nc, in, out);
		}
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	public <T extends Number> void checkT(IConverter<T> nc, String in, String out, NumericPresentation np, int scale, int minScale, boolean monetary) {
		if(monetary && !NumericPresentation.isMonetary(np)) {
			bad(nc, in, false);
		} else if(minScale != scale) {
			bad(nc, in, NumericPresentation.isMonetary(np));
		} else {
			good(nc, in, out);
		}
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	public <T extends Number> void good(IConverter<T> nc, String in, String out) {
		//System.out.print("good\t");
		//System.out.print(in + "\t");
		//System.out.println(out);
		T object = nc.convertStringToObject(NlsContext.getLocale(), in);
		String converted = nc.convertObjectToString(NlsContext.getLocale(), object);
		Assert.assertEquals(out, converted);
	}

	/**
	 * Checks a conversion which must result in ValidationException and the proper code.
	 * @param in
	 */
	public <T extends Number> void bad(IConverter<T> nc, String in, boolean monetary) {
		String badAsConverted = null;
		try {
			//System.out.print("bad \t");
			//System.out.println(in);
			T object = nc.convertStringToObject(NlsContext.getLocale(), in);
			badAsConverted = nc.convertObjectToString(NlsContext.getLocale(), object);
		} catch(ValidationException vx) {
			if(vx.getCode().equals(Msgs.V_BAD_AMOUNT) && monetary)
				return;
			if(vx.getCode().equals(Msgs.V_INVALID) && !monetary)
				return;
			Assert.fail("Unexpected ValidationException!? " + vx.getLocalizedMessage());
		}
		Assert.fail("Validated an invalid amount: '" + in + "', '" + badAsConverted + "'");
	}


	/**
	 * Test INVALID conversions.
	 */
	public void testBadConversions(NumericPresentation np, int scale, String[] badFormats) {
		IConverter<BigDecimal> nc = NumericUtil.createNumberConverter(BigDecimal.class, np, scale);
		//System.out.println("Testen van ONgeldige bedrag invoer-formaten(" + np.name() + ", scale = " + scale + "):");
		boolean monetary = NumericPresentation.isMonetary(np);
		for(String badFormat : badFormats) {
			bad(nc, badFormat, monetary); // Only euro sign is bad
		}
	}

	/**
	 * Test INVALID conversions.
	 */
	public <T extends Number> void testNumericPresentation(NumericPresentation np, int scale, Class<T> classType) {
		try {
			NumericUtil.createNumberConverter(BigDecimal.class, np, scale);
		} catch(IllegalArgumentException e) {
			if(DomUtil.isIntegerType(classType) && scale != 0) {
				return; //expected -> not possible to create NumberConverter on int types with scale other than 0.
			}
			Assert.fail("Should be possible to make instance of NumberConverter using non-monetary presentation!");
		}
	}

	/**
	 * Test INVALID conversions.
	 */
	@Test
	public void testBadConversions() {
		NumericPresentation[] npl1 = {NumericPresentation.NUMBER, NumericPresentation.NUMBER_SCALED, NumericPresentation.NUMBER_FULL, NumericPresentation.NUMBER_SCIENTIFIC};
		for(NumericPresentation np : npl1) {
			for(int i = 0; i <= 6; i++) {
				testBadConversions(np, i, BAD_NUMBER);
			}
		}
	}

	@Test
	public void testNumericPresentations() {
		for(NumericPresentation np : NumericPresentation.values()) {
			if(NumericPresentation.UNKNOWN == np) {
				continue;
			}
			for(int i = 0; i <= 6; i++) {
				testNumericPresentation(np, i, BigDecimal.class);
				testNumericPresentation(np, i, Double.class);
				testNumericPresentation(np, i, Integer.class);
				testNumericPresentation(np, i, double.class);
				testNumericPresentation(np, i, int.class);
			}
		}
	}

	private String add0(int count) {
		String res = "";
		for(int i = 0; i < count; i++) {
			res += "0";
		}
		return res;
	}

	/**
	 * Converts hardcoded comma as thousand grouping separator into locale specific.
	 * Converts hardcoded dot as decimal separator into locale specific.
	 * @param in
	 * @return
	 */
	private String f(String in) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(NlsContext.getLocale());
		char ds = dfs.getDecimalSeparator();
		char ts = dfs.getGroupingSeparator();

		in = in.replaceAll("\\.", "D");
		in = in.replace(",", "T");

		in = in.replaceAll("D", ds + "");
		in = in.replaceAll("T", ts + "");

		return in;
	}

	/**
	 * IMPORTANT: This method is actually used to generate initial content of test_number_cont_dat file. Console output is copied into file that is later used for unit testing via method
	 * {@link TestNumberConverter#testConversionsFromResourceFiles}.
	 * Tests all kinds of conversions.
	 */
	public <T extends Number> void testConversions(NumericPresentation np, int scale, Class<T> classType) {
		IConverter<T> nc = null;
		try {
			nc = NumericUtil.createNumberConverter(classType, np, scale);
		} catch(IllegalArgumentException ex) {
			if(DomUtil.isIntegerType(classType) && scale != 0) {
				return; //expected -> not possible to create NumberConverter on int types with scale other than 0.
			}
		}
		boolean truncateTrailingZero = NumericPresentation.isStripTrailingZeros(np);
		//System.out.println("SETTINGS: classType=" + classType.getName() + ", NP=" + np.name() + ", scale=" + scale);
		if(DomUtil.isIntegerType(classType)) {
			bad(nc, "", false); // Empty string is not allowed for Int types
		} else {
			good(nc, "", ""); // Empty string is allowed for decimal types
		}
		if(classType != BigDecimal.class) {
			//Money presentation is not supported for BigDecimal for now
			check(nc, "\u20ac 1000", "\u20ac 1000", np, scale, 0, true); // Leading euro with ws is allowed;

			check(nc, "  \u20ac 1000.89", f("\u20ac 1000.89"), np, scale, 2, true); // Leading euro with ws is allowed;
		}
		//-- Simple, dot is decimal point, valid
		check(nc, "1000", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 0, false);
		check(nc, "1000.", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 0, false);
		check(nc, "1000.0", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 1, false);
		check(nc, "1000.00", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 2, false);
		check(nc, "1000.99", truncateTrailingZero ? f("1000.99") : f("1000.99") + add0(scale - 2), np, scale, 2, false);
		if(scale < 3) {
			check(nc, "1000.896", "1000896" + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			check(nc, "1000.896", truncateTrailingZero ? f("1000.896") : f("1000.896") + add0(scale - 3), np, scale, 3, false);
		}
		check(nc, "1000.8965", truncateTrailingZero ? f("1000.8965") : f("1000.8965") + add0(scale - 4), np, scale, 4, false);

		//-- Simple, comma is decimal point, valid
		check(nc, "1000", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 0, false);
		check(nc, "1000,", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 0, false);
		check(nc, "1000,0", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 1, false);
		check(nc, "1000,00", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 2, false);
		check(nc, "1000,99", truncateTrailingZero ? f("1000.99") : f("1000.99") + add0(scale - 2), np, scale, 2, false);
		if(scale < 3) {
			check(nc, "1000,896", "1000896" + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			check(nc, "1000,896", truncateTrailingZero ? f("1000.896") : f("1000.896") + add0(scale - 3), np, scale, 3, false);
		}
		check(nc, "1000,8965", truncateTrailingZero ? f("1000.8965") : f("1000.8965") + add0(scale - 4), np, scale, 4, false);

		//-- Simple, dot is decimal separator, valid
		if(scale < 3) {
			check(nc, "1.000", "1000" + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			check(nc, "1.000", truncateTrailingZero ? "1" : f("1.") + add0(scale), np, scale, 3, false);
		}
		check(nc, "1.000.000", (truncateTrailingZero || scale == 0) ? "1000000" : f("1000000.") + add0(scale), np, scale, 0, false);
		check(nc, "1.000.000.000", (truncateTrailingZero || scale == 0) ? "1000000000" : f("1000000000.") + add0(scale), np, scale, 0, false);

		//-- Simple, comma is decaimal separator, valid
		if(scale < 3) {
			check(nc, "1,000", "1000" + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			check(nc, "1,000", truncateTrailingZero ? "1" : f("1.") + add0(scale), np, scale, 3, false);
		}
		check(nc, "1,000,000", (truncateTrailingZero || scale == 0) ? "1000000" : f("1000000.") + add0(scale), np, scale, 0, false);
		check(nc, "1,000,000,000", (truncateTrailingZero || scale == 0) ? "1000000000" : f("1000000000.") + add0(scale), np, scale, 0, false);

		//-- Clear distinction between thousands and decimal point using . as decimal point (>1 thousand separators)
		check(nc, "1,000,000.", (truncateTrailingZero || scale == 0) ? "1000000" : f("1000000.") + add0(scale), np, scale, 0, false);
		check(nc, "1,000,000.9", f("1000000.9") + (truncateTrailingZero ? "" : add0(scale - 1)), np, scale, 1, false);
		check(nc, "1,000,786.76", f("1000786.76") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		check(nc, "1,000,786.765", f("1000786.765") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		check(nc, "1,000,786.7654", f("1000786.7654") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
		check(nc, "1,000,786.76543", f("1000786.76543") + (truncateTrailingZero ? "" : add0(scale - 5)), np, scale, 5, false);
		check(nc, "1,000,786.765432", f("1000786.765432") + (truncateTrailingZero ? "" : add0(scale - 6)), np, scale, 6, false);

		//-- Clear distinction between thousands and decimal point using , as decimal point (>1 thousand separators)
		check(nc, "1.000.000,", (truncateTrailingZero || scale == 0) ? "1000000" : f("1000000.") + add0(scale), np, scale, 0, false);
		check(nc, "1.000.000,9", f("1000000.9") + (truncateTrailingZero ? "" : add0(scale - 1)), np, scale, 1, false);
		check(nc, "1.000.786,76", f("1000786.76") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		check(nc, "1.000.786,765", f("1000786.765") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		check(nc, "1.000.786,7654", f("1000786.7654") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
		check(nc, "1.000.786,76543", f("1000786.76543") + (truncateTrailingZero ? "" : add0(scale - 5)), np, scale, 5, false);
		check(nc, "1.000.786,765432", f("1000786.765432") + (truncateTrailingZero ? "" : add0(scale - 6)), np, scale, 6, false);

		//-- Unclear distinction (1 comma, one dot)
		check(nc, "1.000,00", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 2, false);
		check(nc, "1,000.00", (truncateTrailingZero || scale == 0) ? "1000" : f("1000.") + add0(scale), np, scale, 2, false);

		//-- Negative numbers.
		check(nc, "-1000", (truncateTrailingZero || scale == 0) ? "-1000" : f("-1000.") + add0(scale), np, scale, 0, false);
		check(nc, "-1000.99", f("-1000.99") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		check(nc, "1000-", (truncateTrailingZero || scale == 0) ? "-1000" : f("-1000.") + add0(scale), np, scale, 0, false);
		check(nc, "1000.99-", f("-1000.99") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		if(scale < 3) {
			check(nc, "1000.997-", (truncateTrailingZero || scale == 0) ? "-1000997" : f("-1000997.") + add0(scale), np, scale, 0, false);
		} else {
			check(nc, "1000.997-", f("-1000.997") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		}

		check(nc, "1000.9978-", f("-1000.9978") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
	}

	/**
	 * IMPORTANT: This method is actually used to generate initial content of test_number_cont_dat file. Console output is copied into file that is later used for unit testing via method
	 * {@link TestNumberConverter#testConversionsFromResourceFiles}.
	 * Tests all kinds of conversions that are using thousand separators.
	 */
	public <T extends Number> void testConversionsThousandSepUsed(NumericPresentation np, int scale, Class<T> classType) {
		IConverter<T> nc = null;
		try {
			nc = NumericUtil.createNumberConverter(classType, np, scale);
		} catch(IllegalArgumentException ex) {
			if(DomUtil.isIntegerType(classType) && scale != 0) {
				return; //expected -> not possible to create NumberConverter on int types with scale other than 0.
			}
		}
		boolean truncateTrailingZero = NumericPresentation.isStripTrailingZeros(np);
		//System.out.println("SETTINGS: classType=" + classType.getName() + ", NP=" + np.name() + ", scale=" + scale);
		if(DomUtil.isIntegerType(classType)) {
			bad(nc, "", false); // Empty string is not allowed for Int types
		} else {
			good(nc, "", ""); // Empty string is allowed for decimal types
		}
		if(classType != BigDecimal.class) {
			//Money presentation is not supported for BigDecimal for now
			check(nc, "\u20ac 1000", f("\u20ac 1,000"), np, scale, 0, true); // Leading euro with ws is allowed;

			check(nc, "  \u20ac 1000.89", f("\u20ac 1,000.89"), np, scale, 2, true); // Leading euro with ws is allowed;
		}
		//-- Simple, dot is decimal point, valid
		if(scale != 3) {
			checkT(nc, "1,000", f("1,000") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			//only in case of scale =3, we recognize this as valid decimal point input.
			good(nc, "1,000", f("1.000"));
		}
		checkT(nc, "1,000.", f("1,000") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		checkT(nc, "1,000.0", f("1,000") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 1, false);
		checkT(nc, "1,000.00", f("1,000") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 2, false);
		checkT(nc, "1,000.99", truncateTrailingZero ? f("1,000.99") : f("1,000.99") + add0(scale - 2), np, scale, 2, false);
		if(scale < 3) {
			checkT(nc, "10.896", f("10,896") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			checkT(nc, "10.896", truncateTrailingZero ? f("10.896") : f("10.896") + add0(scale - 3), np, scale, 3, false);
		}
		checkT(nc, "1,000.8965", truncateTrailingZero ? f("1,000.8965") : f("1,000.8965") + add0(scale - 4), np, scale, 4, false);

		//-- Simple, dot is decimal separator, valid
		if(scale != 3) {
			checkT(nc, "1.000", f("1,000") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			//only in case of scale =3, we recognize this as valid decimal point input.
			good(nc, "1.000", f("1.000"));
		}

		checkT(nc, "1.000.000", (truncateTrailingZero || scale == 0) ? f("1,000,000") : f("1,000,000.") + add0(scale), np, scale, 0, false);
		checkT(nc, "1.000.000.000", (truncateTrailingZero || scale == 0) ? f("1,000,000,000") : f("1,000,000,000.") + add0(scale), np, scale, 0, false);

		//-- Clear distinction between thousands and decimal point using . as decimal point (>1 thousand separators)
		checkT(nc, "1,000,000.", (truncateTrailingZero || scale == 0) ? f("1,000,000") : f("1,000,000.") + add0(scale), np, scale, 0, false);
		checkT(nc, "1,000,000.9", f("1,000,000.9") + (truncateTrailingZero ? "" : add0(scale - 1)), np, scale, 1, false);
		checkT(nc, "1,000,786.76", f("1,000,786.76") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		checkT(nc, "1,000,786.765", f("1,000,786.765") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		checkT(nc, "1,000,786.7654", f("1,000,786.7654") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
		checkT(nc, "1,000,786.76543", f("1,000,786.76543") + (truncateTrailingZero ? "" : add0(scale - 5)), np, scale, 5, false);
		checkT(nc, "1,000,786.765432", f("1,000,786.765432") + (truncateTrailingZero ? "" : add0(scale - 6)), np, scale, 6, false);

		//-- Clear distinction between thousands and decimal point using , as decimal point (>1 thousand separators)
		checkT(nc, "1.000.000,", (truncateTrailingZero || scale == 0) ? f("1,000,000") : f("1,000,000.") + add0(scale), np, scale, 0, false);
		checkT(nc, "1.000.000,9", f("1,000,000.9") + (truncateTrailingZero ? "" : add0(scale - 1)), np, scale, 1, false);
		checkT(nc, "1.000.786,76", f("1,000,786.76") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		checkT(nc, "1.000.786,765", f("1,000,786.765") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		checkT(nc, "1.000.786,7654", f("1,000,786.7654") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
		checkT(nc, "1.000.786,76543", f("1,000,786.76543") + (truncateTrailingZero ? "" : add0(scale - 5)), np, scale, 5, false);
		checkT(nc, "1.000.786,765432", f("1,000,786.765432") + (truncateTrailingZero ? "" : add0(scale - 6)), np, scale, 6, false);

		//-- Unclear distinction (1 comma, one dot)
		checkT(nc, "1.000,00", (truncateTrailingZero || scale == 0) ? f("1,000") : f("1,000.") + add0(scale), np, scale, 2, false);
		checkT(nc, "1,000.00", (truncateTrailingZero || scale == 0) ? f("1,000") : f("1,000.") + add0(scale), np, scale, 2, false);

		//-- Negative numbers.
		if(scale != 3) {
			checkT(nc, "-1.000", f("-1,000") + ((truncateTrailingZero || scale == 0) ? "" : add0(scale)), np, scale, 0, false);
			checkT(nc, "-1,000", f("-1,000") + ((truncateTrailingZero || scale == 0) ? "" : add0(scale)), np, scale, 0, false);
			checkT(nc, "1.000-", f("-1,000") + ((truncateTrailingZero || scale == 0) ? "" : add0(scale)), np, scale, 0, false);
		} else {
			good(nc, "-1.000", f("-1.000"));
			good(nc, "-1,000", f("-1.000"));
			good(nc, "1.000-", f("-1.000"));
		}
		checkT(nc, "-1,000.99", f("-1,000.99") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		checkT(nc, "1,000.99-", f("-1,000.99") + (truncateTrailingZero ? "" : add0(scale - 2)), np, scale, 2, false);
		if(scale < 3) {
			//check(nc, "1,000.997-", f("-1,000,997") + ((truncateTrailingZero || scale == 0) ? "" : f(".") + add0(scale)), np, scale, 0, false);
		} else {
			checkT(nc, "1,000.997-", f("-1,000.997") + (truncateTrailingZero ? "" : add0(scale - 3)), np, scale, 3, false);
		}

		checkT(nc, "1,000.9978-", f("-1,000.9978") + (truncateTrailingZero ? "" : add0(scale - 4)), np, scale, 4, false);
	}

	/**
	 * IMPORTANT: this method is used to generate console content that is used as initial content of test resurce file test_number_cont_data1.resource.
	 * This file can be reviewed by test team, and manualy extended for interesting use cases.
	 * Beside, this method aslo does test this initial set of conversions checks.
	 * Only non money NumericPresentation members are tested, since monetary members are tested by {@link TestMoneyConverter} class.
	 */
	@Test
	public void testGenerateConversionsSetResource() {
		NumericPresentation[] npl1 = {NumericPresentation.NUMBER, NumericPresentation.NUMBER_SCALED};
		for(NumericPresentation np : npl1) {
			for(int i = 0; i <= 6; i++) {
				testConversions(np, i, BigDecimal.class);
				testConversions(np, i, Double.class);
				testConversions(np, i, Integer.class);
				testConversions(np, i, double.class);
				testConversions(np, i, int.class);
			}
		}

		//with gouping separators...
		NumericPresentation[] npl2 = {NumericPresentation.NUMBER_FULL};
		for(NumericPresentation np : npl2) {
			for(int i = 0; i <= 6; i++) {
				testConversionsThousandSepUsed(np, i, BigDecimal.class);
				testConversionsThousandSepUsed(np, i, Double.class);
				testConversionsThousandSepUsed(np, i, Integer.class);
				testConversionsThousandSepUsed(np, i, double.class);
				testConversionsThousandSepUsed(np, i, int.class);
			}
		}
	}

	@Test
	public void testConversionsFromResourceFiles() throws IOException {
		InputStream is = getClass().getResourceAsStream("test_number_cont_data1.resource");
		if(null == is)
			throw new IllegalStateException("Missing resource");
		try {
			testResourceFile(is);
		} finally {
			is.close();
		}
	}

	private void testResourceFile(InputStream fi) throws IOException {
		String linebuf = null;
		BufferedReader br = null;
		try {
			//Open the file that is the first
			DataInputStream in = new DataInputStream(fi);
			br = new BufferedReader(new InputStreamReader(in));
			NumericPresentation np = NumericPresentation.NUMBER;
			int scale = 0;
			Class< ? extends Number> classType = Double.TYPE;
			IConverter< ? extends Number> nc = null;
			while((linebuf = br.readLine()) != null) {
				if(linebuf.startsWith("SETTINGS:")) {
					linebuf = linebuf.substring(9);
					String[] parts = linebuf.split(",");
					for(String part : parts) {
						String[] vals = part.split("=");
						if(vals == null || vals.length != 2) {
							throw new IOException("invalid SETTINGS part, must be key=value, but found: " + part);
						}
						if("NP".equals(vals[0].trim())) {
							np = NumericPresentation.valueOf(vals[1].trim());
						} else if("scale".equals(vals[0].trim())) {
							scale = Integer.parseInt(vals[1].trim());
						} else if("classType".equals(vals[0].trim())) {
							try {
								classType = (Class< ? extends Number>) Class.forName(vals[1].trim());
							} catch(ClassNotFoundException e) {
								if("double".equals(vals[1].trim())) {
									classType = double.class;
								} else if("int".equals(vals[1].trim())) {
									classType = int.class;
								} else {
									throw new IOException("unexpected classType value found: " + part);
								}
							}
						}
					}
					nc = NumericUtil.createNumberConverter(classType, np, scale);
				} else {
					if(nc == null) {
						throw new IOException("SETTINGS are not initialized properly!\nUse syntax as: SETTINGS: classType=java.math.BigDecimal, NP=NUMBER, scale=0");
					}
					if(linebuf.startsWith("bad")) {
						String input = linebuf.substring(3).trim();
						bad(nc, input, NumericPresentation.isMonetary(np));
					} else if(linebuf.startsWith("good\t")) {
						linebuf = linebuf.substring(5);
						if("\t".equals(linebuf)) {
							good(nc, "", "");
						} else {
							String[] splits = linebuf.split("\\t");
							if(splits == null || splits.length != 2) {
								throw new IOException("Line params with expected sucessful conversion must be delimited using TAB (\\t) character! Has to have 2 values (input and expected).");
							}
							good(nc, splits[0].trim(), splits[1].trim());
						}
					}
				}
			}
		} finally {
			//Close the input stream
			if(br != null) {
				br.close();
			}
		}
	}
}
