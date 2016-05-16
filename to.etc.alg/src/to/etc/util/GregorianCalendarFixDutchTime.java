
package to.etc.util;

import java.util.*;

/**
 * This class extends the gregoriancalendar to fix the time validation for certain dutch dates
 * time does not exist for may 16th 1940 between 00:00:00 and 01:40:00 and july 1st 1937 between 00:00:00 and 00:00:27 for dutch locale in the gregoriancalendar
 * for these times no error will be generated but the next valid time will be returned
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29 sep. 2015
 */
public final class GregorianCalendarFixDutchTime extends GregorianCalendar {

	public GregorianCalendarFixDutchTime(Locale aLocale) {
		super(aLocale);
	}

	public GregorianCalendarFixDutchTime(TimeZone zone, Locale aLocale) {
		super(zone, aLocale);
	}

	/**
	 * Some time values don't seem to exist
	 * time does not exist for may 16th 1940 between 00:00:00 and 01:40:00 and july 1st 1937 between 00:00:00 and 00:00:27 for dutch locale
	 * Return next lenient time value for these dates
	 * @see java.util.GregorianCalendar#computeTime()
	 */
	@Override
	protected void computeTime() {
		if(isLenient())
			super.computeTime();
		else {
			try {
				super.computeTime();
			} catch(IllegalArgumentException e) {
				setLenient(true);
				super.computeTime(); //get next lenient time
				int hour = get(Calendar.HOUR_OF_DAY);
				int seconds = get(Calendar.SECOND);
				int minutes = get(Calendar.MINUTE);
				int year = get(Calendar.YEAR);
				int month = get(Calendar.MONTH) + 1;
				int day = get(Calendar.DATE);
				if(!((year == 1937 && month == 7 && day == 1 && seconds == 28) || (year == 1940 && month == 5 && day == 16 && hour == 1 && minutes == 40)))
					throw e;
			} finally {
				setLenient(false);
			}
		}

	}

}


