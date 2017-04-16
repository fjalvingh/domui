package to.etc.util;

import java.util.*;

import org.junit.*;

/**
 * Testing DateUtil
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Dec 13, 2012
 */
public class TestDateUtil {

	/**
	 * Test method for {@link to.etc.util.DateUtil#addMinutes(java.util.Date, int)}.
	 */
	@Test
	public final void testAddMinutes() {
		Date date = DateUtil.dateFor(2012, Calendar.JULY, 15, 11, 15, 30, 500);
		Date adjustedDate = DateUtil.addMinutes(date, 50);
		Assert.assertEquals(DateUtil.dateFor(2012, Calendar.JULY, 15, 12, 5, 30, 500), adjustedDate);

		Date dateJustBeforeMidnight = DateUtil.dateFor(2012, Calendar.JANUARY, 12, 23, 57, 0, 0);
		adjustedDate = DateUtil.addMinutes(dateJustBeforeMidnight, 4);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2012, Calendar.JANUARY, 13, 0, 1, 0, 0));

		Date dateJustAfterNewYear = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addMinutes(dateJustAfterNewYear, -59);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2011, Calendar.DECEMBER, 31, 23, 58, 50, 750));

		Date dateDoesNotChange = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addMinutes(dateDoesNotChange, 0);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750));
	}

	/**
	 * Test method for {@link to.etc.util.DateUtil#addDays(java.util.Date, int)}.
	 */
	@Test
	public final void testAddDays() {
		Date date = DateUtil.dateFor(2012, Calendar.JULY, 15, 11, 15, 30, 500);
		Date adjustedDate = DateUtil.addDays(date, 50);
		Assert.assertEquals(DateUtil.dateFor(2012, Calendar.SEPTEMBER, 3, 11, 15, 30, 500), adjustedDate);

		Date dateJustBeforeMidnight = DateUtil.dateFor(2012, Calendar.JANUARY, 12, 23, 57, 0, 0);
		adjustedDate = DateUtil.addDays(dateJustBeforeMidnight, 4);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2012, Calendar.JANUARY, 16, 23, 57, 0, 0));

		Date dateJustAfterNewYear = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addDays(dateJustAfterNewYear, -7);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2011, Calendar.DECEMBER, 25, 0, 57, 50, 750));

		Date dateDoesNotChange = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addDays(dateDoesNotChange, 0);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750));
	}

	/**
	 * Test method for {@link to.etc.util.DateUtil#addYears(java.util.Date, int)}.
	 */
	@Test
	public final void testAddYears() {
		Date date = DateUtil.dateFor(2012, Calendar.JULY, 15, 11, 15, 30, 500);
		Date adjustedDate = DateUtil.addYears(date, 50);
		Assert.assertEquals(DateUtil.dateFor(2062, Calendar.JULY, 15, 11, 15, 30, 500), adjustedDate);

		Date dateJustBeforeMidnight = DateUtil.dateFor(2012, Calendar.FEBRUARY, 29, 23, 57, 0, 0);
		adjustedDate = DateUtil.addYears(dateJustBeforeMidnight, 4);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2016, Calendar.FEBRUARY, 29, 23, 57, 0, 0));

		Date dateJustAfterNewYear = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addYears(dateJustAfterNewYear, -7);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2005, Calendar.JANUARY, 1, 0, 57, 50, 750));

		Date dateDoesNotChange = DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750);
		adjustedDate = DateUtil.addYears(dateDoesNotChange, 0);
		Assert.assertEquals(adjustedDate, DateUtil.dateFor(2012, Calendar.JANUARY, 1, 0, 57, 50, 750));
	}

	/**
	 * Should not generate errors for missing date-times in calendar but return next valid time
	 * missing date-times for dutch locale are may 16th 1940 between 00:00:00 and 01:40:00 and july 1st 1937 between 00:00:00 and 00:00:27
	 * Test method for {@link to.etc.util.DateUtil#getCalendar()}.
	 */
	@Test
	public final void testMissingTime() {
		TimeZone zone = TimeZone.getDefault();
		if(! zone.getID().equals("Europe/Amsterdam")) {
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
		}

		Calendar cal = DateUtil.getCalendar();
		DateUtil.setDate(cal, 1937, Calendar.JULY, 1);
		DateUtil.clearTime(cal);
		cal.setLenient(false);
		Assert.assertEquals(cal.getTime(), DateUtil.dateFor(1937, Calendar.JULY, 1, 00, 00, 28, 0));

		Calendar cal19370701 = DateUtil.getCalendar(Locale.forLanguageTag("NL"));
		DateUtil.setDate(cal19370701, 1937, Calendar.JULY, 1);
		DateUtil.clearTime(cal19370701);
		cal19370701.setLenient(false);
		Assert.assertEquals(cal19370701.getTime(), DateUtil.dateFor(1937, Calendar.JULY, 1, 00, 00, 28, 0));

		Calendar cal19400516 = DateUtil.getCalendar(Locale.forLanguageTag("NL"));
		DateUtil.setDate(cal19400516, 1940, Calendar.MAY, 16);
		DateUtil.clearTime(cal19400516);
		cal19400516.setLenient(false);
		Assert.assertEquals(cal19400516.getTime(), DateUtil.dateFor(1940, Calendar.MAY, 16, 1, 40, 0, 0));

		TimeZone.setDefault(zone);
	}

}
