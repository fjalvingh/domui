/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import java.util.*;

/**
 * Static date/time utiility class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 28, 2007
 */
public class DateUtil {
	/** A fixed date way into the future. */
	static public final Date	FUTURE	= dateFor(9999, 11, 31);

	private DateUtil() {
	}

	/**
	 * Decraps the separate date / time found in the VP database into a normal date containing
	 * the time. This needs to be as speedy as possible.
	 *
	 * @param coredate
	 * @param timestring
	 * @return
	 */
	static public Date dateFromDateTime(Date coredate, String timestring) {
		if(coredate == null)
			return null;
		if(timestring == null)
			return coredate;
		timestring = timestring.trim();
		if(timestring.length() == 0)
			return coredate;

		//-- We need to @!#*(!^# parse 8-( Do it as fast as possible because this might be used a LOT.
		int pos = timestring.indexOf(':'); // Hours separator
		int hour = 0;
		int ix = 0;
		if(pos == -1) // Invalid time format, oh joy.
			return coredate; // Cannot fix, and throwing up would fsck up the database..
		while(ix < pos) {
			char c = timestring.charAt(ix++);
			if(!Character.isDigit(c)) // Cannot fix..
				return coredate;
			hour = hour * 10 + (c - '0');
		}

		//-- minutes.
		int len = timestring.length(); // Keep for speed
		ix = pos + 1;
		pos = timestring.indexOf(':', ix);
		int end = pos == -1 ? len : pos;
		int minutes = 0;
		while(ix < end) {
			char c = timestring.charAt(ix++);
			if(!Character.isDigit(c)) // Cannot fix..
				return coredate;
			minutes = minutes * 10 + (c - '0');
		}
		int seconds = 0;
		if(pos != -1) {
			end = len;
			ix = pos + 1;
			while(ix < len) {
				char c = timestring.charAt(ix++);
				if(!Character.isDigit(c)) // Cannot fix..
					return coredate;
				seconds = seconds * 10 + (c - '0');
			}
		}

		//-- Recalculate date object, using a calendar fgs 8-(
		Calendar cal = Calendar.getInstance();
		cal.setTime(coredate);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}



	static private final long	SECS	= 1000;

	static private final long	MINS	= 60 * 1000;

	static private final long	HOURS	= MINS * 60;

	static private final long	DAYS	= HOURS * 24;

	/**
	 * Quick conversion of time to a string for presentation pps.
	 *
	 * @param millis		Time in millis.
	 * @param nfields		The number of fields to show: 1 = hours only, 2= hh:mm, 3=h:mm:ss. Warning:
	 * 						the fields that are not shown do not "round up" the shown fields! So if you
	 * 						use nfields=1 for 18:45 the hour shown would be 18, not 19!
	 * @param shortest		When set this truncates the time display when components are zero. So for
	 * 						18:00:0) this would render '18'; for 18:21:00 this would render 18:21.
	 * @return
	 */
	static public String encodeTimeInMS(long millis, int nfields, boolean shortest) {
		millis %= DAYS;
		long h = millis / HOURS;
		millis %= HOURS;
		long m = millis / MINS;
		millis %= MINS;
		long s = millis / SECS;
		millis %= SECS;

		String hs = Long.toString(h);
		if(nfields <= 1 || (shortest && m == 0 && nfields == 2) || (shortest && m == 0 && s == 0 && nfields == 3))
			return hs;
		StringBuilder sb = new StringBuilder(10);
		sb.append(h);
		sb.append(':');
		if(m < 10)
			sb.append('0');
		sb.append(m);
		if(nfields <= 2 || (shortest && s == 0))
			return sb.toString();

		sb.append(':');
		if(s < 10)
			sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	/**
	 * Decodes a time string, encoded as hh:mm:ss.mmmm where ss.mmmm are optional. If
	 * the field is invalid this returns -1, else it returns the #of ms representing
	 * the time.
	 *
	 * @param time
	 * @return
	 */
	static public long decodeTime(String timestring) {
		if(timestring == null)
			return -1;
		timestring = timestring.trim();
		int len = timestring.length(); // Keep for speed
		if(len < 5)
			return -1;

		//-- We need to @!#*(!^# parse 8-( Do it as fast as possible because this might be used a LOT.
		int pos = timestring.indexOf(':'); // Hours separator
		if(pos == -1) // Invalid time format, oh joy.
			return -1; // Cannot fix, and throwing up would fsck up the database..
		int hour = 0;
		int ix = 0;
		while(ix < pos) {
			char c = timestring.charAt(ix++);
			if(!Character.isDigit(c)) // Cannot fix..
				return -1;
			hour = hour * 10 + (c - '0');
		}

		//-- minutes.
		ix = pos + 1;
		pos = timestring.indexOf(':', ix);
		int end = pos == -1 ? len : pos;
		int minutes = 0;
		while(ix < end) {
			char c = timestring.charAt(ix++);
			if(!Character.isDigit(c)) // Cannot fix..
				return -1;
			minutes = minutes * 10 + (c - '0');
		}
		int seconds = 0;
		if(pos != -1) {
			end = len;
			ix = pos + 1;
			while(ix < len) {
				char c = timestring.charAt(ix++);
				if(!Character.isDigit(c)) // Cannot fix..
					return -1;
				seconds = seconds * 10 + (c - '0');
			}
		}

		return ((long) hour * 60 * 60 * 1000) + ((long) minutes * 60 * 1000) + ((long) seconds * 1000);
	}

	/**
	 * Extracts the dumb time field from a date.
	 * @param dt
	 * @return
	 */
	static public String extractTimeString(Date dt, int size) {
		if(size != 5 && size != 8)
			throw new IllegalStateException("Accepts only 5 or 8 char times.");
		//prevents crash when dt == null
		if(dt == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		//prevent object to become dirty because null time was read from db, in case of 00:00 return null since reading of null time would store 00:00 value into Date object
		if(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0) {
			return null;
		}
		char[] b = new char[size];
		int v = cal.get(Calendar.HOUR_OF_DAY);
		b[0] = (char) (v / 10 + '0');
		b[1] = (char) (v % 10 + '0');
		b[2] = ':';
		v = cal.get(Calendar.MINUTE);
		b[3] = (char) (v / 10 + '0');
		b[4] = (char) (v % 10 + '0');
		if(size > 5) {
			b[5] = ':';
			v = cal.get(Calendar.SECOND);
			b[6] = (char) (v / 10 + '0');
			b[7] = (char) (v % 10 + '0');
		}
		return new String(b);
	}

	static public Date truncateDate(Date dt) {
		//prevents crash when dt == null
		if(dt == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * Returns date incremented for specified value.
	 * @param dt
	 * @param field see {@link Calendar#add(int, int)}
	 * @param amount see {@link Calendar#add(int, int)}
	 * @return In case of <B>dt</B> is null returns null
	 */
	static public Date incrementDate(Date dt, int field, int amount) {
		if(dt == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(field, amount);
		return new Date(cal.getTimeInMillis());
	}
	static public void truncateDate(Date dest, Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		dest.setTime(cal.getTimeInMillis());
	}

	/**
	 * Calculates the date for 0:00 the day after (tomorrow). Works like the EndOfDay but
	 * returns one minute later.
	 * @param ts
	 * @return
	 */
	static public Date tomorrow(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		clearTime(cal);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * INTERNAL DAO USE ONLY -- This returns a date/ which ends at
	 * 23:59 of the day passed. It is used within DAO's only to
	 * allow Hibernate to query split date/time fields.
	 * @param dt
	 * @return
	 */
	static public Date dumbEndOfDayDate(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * Clears the entire time portion of a Calendar.
	 * @param cal
	 */
	static public void clearTime(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	static public Date yearStartDate(int year) {
		Calendar cal = Calendar.getInstance();
		clearTime(cal);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return new Date(cal.getTimeInMillis());
	}

	static public Date calculateDurationEndInSec(Date start, int durationInSeconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.SECOND, durationInSeconds);
		return new Date(cal.getTimeInMillis());
	}

	static public Date dateFor(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		clearTime(cal);
		return cal.getTime();
	}

	static public void setDate(Calendar cal, int year, int month, int day) {
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
	}

	static public Date dateFor(int year, int month, int day, int hour, int minute, int sec, int mils) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		setTime(cal, hour, minute, sec, mils);
		return cal.getTime();
	}

	static public void setTime(Calendar cal, int h, int m, int s, int ms) {
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		cal.set(Calendar.MILLISECOND, ms);
	}

	static public Date addMinutes(Date in, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(in);
		cal.add(Calendar.DAY_OF_YEAR, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}

	static public Date addDays(Date in, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(in);
		cal.add(Calendar.DAY_OF_YEAR, days);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}

	static public Date addYears(Date in, int years) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(in);
		cal.add(Calendar.YEAR, years);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new Date(cal.getTimeInMillis());
	}

	static public boolean between(Date start, Date end, Date v) {
		return v.getTime() >= start.getTime() && v.getTime() < end.getTime();
	}

	static public boolean overlaps(Date s1, Date e1, Date s2, Date e2) {
		return s1.getTime() < e2.getTime() && s2.getTime() < e1.getTime();
	}
	public static void main(String[] args) {
		System.out.println(encodeTimeInMS(decodeTime("13:40:21"), 3, true));
		System.out.println(encodeTimeInMS(decodeTime("13:40:21"), 2, true));
		System.out.println(encodeTimeInMS(decodeTime("13:40:21"), 1, true));

		System.out.println(encodeTimeInMS(decodeTime("13:00:00"), 3, true));
		System.out.println(encodeTimeInMS(decodeTime("13:10:00"), 3, true));
		System.out.println(encodeTimeInMS(decodeTime("13:00:20"), 3, true));

	}

	/**
	 * Updates the date part of a date only, leaving the time part unaltered.
	 * @param finisheddate
	 * @param dt
	 * @return
	 */
	public static Date setDateOnly(Date finisheddate, Date dt) {
		if(dt == null) {
			//time part without date is not valid data, so both should be null then
			return null;
		}
		Calendar cal = Calendar.getInstance();
		//prevents crash when finisheddate == null
		if(finisheddate == null) {
			cal.set(0, 0, 0, 0, 0);
		} else {
			cal.setTime(finisheddate);
		}
		int hr = cal.get(Calendar.HOUR_OF_DAY); // Retrieve time from value to set;
		int mn = cal.get(Calendar.MINUTE);
		int sc = cal.get(Calendar.SECOND);
		int ms = cal.get(Calendar.MILLISECOND);

		cal.setTime(dt);
		cal.set(Calendar.HOUR_OF_DAY, hr);
		cal.set(Calendar.MINUTE, mn);
		cal.set(Calendar.SECOND, sc);
		cal.set(Calendar.MILLISECOND, ms);
		return cal.getTime();
	}

	/**
	 * Gets the first day of the next month for the date specified in parameter.
	 * @param dt
	 * @return
	 */
	public static Date getFirstDayOfNextMonth(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * The ISO week-numbering year starts at the first day (Monday) of week 01 and ends at the Sunday before the new ISO year
	 * (hence without overlap or gap). It consists of 52 or 53 full weeks. If 1 January is on a Monday, Tuesday, Wednesday
	 * or Thursday, it is in week 01. If 1 January is on a Friday, Saturday or Sunday, it is in week 52 or 53 of the previous
	 * year (there is no week 00). 28 December is always in the last week of its year.
	 *
	 * @return
	 */
	private static Calendar createIsoCalendar() {
		Calendar cal = new GregorianCalendar();
		cal.clear();
		cal.setMinimalDaysInFirstWeek(4);
		cal.setFirstDayOfWeek(2);
		return cal;
	}

	/**
	 * Returns ISO calendar with specified year and week.
	 * @param year
	 * @param weekOfYear
	 * @return
	 */
	public static Calendar getIsoCalendarForYearAndWeek(int year, int weekOfYear) {
		Calendar cal = DateUtil.createIsoCalendar();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
		return cal;
	}

	/**
	 * Compares calendar times (hours, minutes, seconds and milliseconds) and returns
	 * a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 *
	 * They are compared by using compareTo() method. Second's calendar time is set to the copy of the first calendar
	 * to ensure that they can be different in time only.
	 *
	 * @param first
	 * @param second
	 * @return
	 */

	/**
	 * Compares calendar times (hours, minutes, seconds and milliseconds) and returns
	 * a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 *
	 * They are compared by using compareTo() method. Second's calendar time is set to the copy of the first calendar
	 * to ensure that they can be different in time only.
	 *
	 * @param first
	 * @param second
	 * @return
	 */

	public static Comparator<Calendar>	CALENDAR_TIMES_COMPARATOR	= new Comparator<Calendar>() {
																		@Override
																		public int compare(Calendar first, Calendar second) {
																			if(first.get(Calendar.HOUR_OF_DAY) < second.get(Calendar.HOUR_OF_DAY)) {
																				return -1;
																			} else if(first.get(Calendar.HOUR_OF_DAY) == second.get(Calendar.HOUR_OF_DAY)) {
																				if(first.get(Calendar.MINUTE) < second.get(Calendar.MINUTE)) {
																					return -1;
																				} else if(first.get(Calendar.MINUTE) == second.get(Calendar.MINUTE)) {
																					if(first.get(Calendar.SECOND) < second.get(Calendar.SECOND)) {
																						return -1;
																					} else if(first.get(Calendar.SECOND) == second.get(Calendar.SECOND)) {
																						if(first.get(Calendar.MILLISECOND) < second.get(Calendar.MILLISECOND)) {
																							return -1;
																						} else if(first.get(Calendar.MILLISECOND) == second.get(Calendar.MILLISECOND)) {
																							return 0;
																						}
																					}
																				}
																			}
																			return 1;
																		}
																	};
}
