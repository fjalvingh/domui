package to.etc.domui.test.converters;

import java.util.*;

import org.junit.*;

import to.etc.domui.converter.*;
import to.etc.webapp.nls.*;

/**
 * All kinds of conversions tests.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 18 Jul 2011
 */

public class TestTimeConvertor {
	private static final String[] BAD_TIMES = {"24:00", ":00", "abc", "10.00", "22", ":1", "111:10", ":60", "1:61", "23:60", "10::00", "10: 1", "1 :00", "1266", "12", "2401"};

	private static final String[] GOOD_TIMES = {"", "9:00", "20:00", "23:00", "0:10", "0:20", "0:03", "12:23"};

	private static final String[] GOOD_TIMES_NO_COLON = {"944", "012", "1355"};

	private static final Integer[] GOOD_TIMESRAW = {null, new Integer(32400), new Integer(72000), new Integer(82800), new Integer(600), new Integer(1200), new Integer(180), new Integer(44580)};

	private static final Integer[] GOOD_TIMESRAW_NO_COLON = {new Integer(35040), new Integer(720), new Integer(50100)};

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
	public void checkGoodTimes() {
		TimeToSecondsConverter tc = new TimeToSecondsConverter();
		Integer timeFromString = null;
		for(int j = 0; j < GOOD_TIMES.length; j++) {
			String time = GOOD_TIMES[j];
			Integer timeRaw = GOOD_TIMESRAW[j];
			String timeFromObject = tc.convertObjectToString(NlsContext.getLocale(), timeRaw);
			try {
				timeFromString = tc.convertStringToObject(NlsContext.getLocale(), time);
			} catch(Exception x) {
				Assert.fail("Time could not be converted:" + time);
			}
			Assert.assertEquals(timeFromObject, time);
			Assert.assertEquals(timeFromString, timeRaw);
		}
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	@Test
	public void checkGoodTimesNoColon() {
		TimeToSecondsConverter tc = new TimeToSecondsConverter();
		Integer timeFromString = null;
		for(int j = 0; j < GOOD_TIMES_NO_COLON.length; j++) {
			String time = GOOD_TIMES_NO_COLON[j];
			Integer timeRaw = GOOD_TIMESRAW_NO_COLON[j];
			String timeFromObject = tc.convertObjectToString(NlsContext.getLocale(), timeRaw);
			try {
				timeFromString = tc.convertStringToObject(NlsContext.getLocale(), time);
			} catch(Exception x) {
				Assert.fail("Time could not be converted:" + time);
			}
			// insert colon to compare times
			time = new StringBuffer(time).insert(time.length() - 2, ":").toString();
			Assert.assertEquals(timeFromObject, time);
			Assert.assertEquals(timeFromString, timeRaw);
		}
	}

	/**
	 * Checks a valid conversion and compares the output with the expected output.
	 * @param in
	 * @param out
	 */
	@Test
	public void checkBadTimes() {
		TimeToSecondsConverter tc = new TimeToSecondsConverter();
		for (int j = 0; j < BAD_TIMES.length; j++) {
			String time = BAD_TIMES[j];
			try {
				tc.convertStringToObject(NlsContext.getLocale(), time);
				Assert.fail("It's a good time ??? " + time);
			} catch(Exception x) {
				//-- is the expected situation
			}
		}
	}

}
