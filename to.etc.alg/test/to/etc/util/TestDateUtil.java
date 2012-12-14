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

}
