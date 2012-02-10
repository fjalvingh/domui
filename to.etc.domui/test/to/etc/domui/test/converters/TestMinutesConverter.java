package to.etc.domui.test.converters;

import java.util.*;

import org.junit.*;

import to.etc.domui.converter.*;
import to.etc.webapp.nls.*;

/**
 * Conversion tests for {@link to.etc.domui.converter.MinutesConverter MinutesConverter}
 *
 *
 * @author <a href="mailto:jsavic@execom.eu">Jelena Savic</a>
 * Created on Feb 10, 2012
 */

public class TestMinutesConverter {
	private static final String[] BAD_VALUES = {"1..", "1..2", "1,,", "1,,2", "1.123", "1,123", "a", ".6", ",6", ".61", ",61", "2.", "2,"};

	private static final String[] GOOD_VALUES = {"0.2", "0.25", "1.12", "0,2", "0,25", "1,12"};

	private static final Double[] GOOD_VALUES_RAW = {new Double(2), new Double(25), new Double(72), new Double(2), new Double(25), new Double(72)};

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
	@Test
	public void checkGoodValues() {
		MinutesConverter mc = new MinutesConverter();
		Double minsFromString = null;
		for(int i = 0; i < GOOD_VALUES.length; i++) {
			String mins = GOOD_VALUES[i];
			Double minsRaw = GOOD_VALUES_RAW[i];
			String minsFromObject = mc.convertObjectToString(NlsContext.getLocale(), minsRaw);
			try {
				minsFromString = mc.convertStringToObject(NlsContext.getLocale(), mins);
			} catch(Exception x) {
				Assert.fail("Minutes could not be converted:" + mins);
			}
			Assert.assertEquals(minsFromObject, mins.replace(".", ","));
			Assert.assertEquals(minsFromString, minsRaw);
		}
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	@Test
	public void checkBadTimes() {
		MinutesConverter mc = new MinutesConverter();
		for(int i = 0; i < BAD_VALUES.length; i++) {
			String time = BAD_VALUES[i];
			try {
				@SuppressWarnings("unused")
				Double minsFromString = mc.convertStringToObject(NlsContext.getLocale(), time);
				Assert.fail("It's a good time ??? " + time);
			} catch(Exception x) {
				//-- is the expected situation
			}
		}
	}

}
