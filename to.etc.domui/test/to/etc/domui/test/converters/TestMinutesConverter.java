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
	private static final String[] BAD_VALUES = {"1..", "1..2", "1,,", "1,,2", "1.123", "1,123", "a", ".6", ",6", ".61", ",61", "2.", "2,", "1.1", "1,1"};

	private static final String[] GOOD_VALUES = {"0.02", "0.25", "1.12", "0,02", "0,25", "1,12", "2.01", "2,01", "2.09", "2,09"};

	private static final Integer[] GOOD_VALUES_RAW = {new Integer(2), new Integer(25), new Integer(72), new Integer(2), new Integer(25), new Integer(72), new Integer(121), new Integer(121), new Integer(129),  new Integer(129)};

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
		Integer minsFromString = null;
		for(int i = 0; i < GOOD_VALUES.length; i++) {
			String mins = GOOD_VALUES[i];
			Integer minsRaw = GOOD_VALUES_RAW[i];
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
			String mins = BAD_VALUES[i];
			try {
				mc.convertStringToObject(NlsContext.getLocale(), mins);
				Assert.fail("It's a good time ??? " + mins);
			} catch(Exception x) {
				//-- is the expected situation
			}
		}
	}

}
