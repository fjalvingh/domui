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

import java.math.*;
import java.text.*;
import java.util.*;

import javax.servlet.jsp.el.*;

public class CalculationUtil {
	static private final Locale				LOC			= new Locale("nl", "NL");

	static private final DateFormatSymbols	SYMS		= new DateFormatSymbols(LOC);

	static private final String[]			SHORTNAMES	= SYMS.getShortMonths();

	static private final String[]			LONGNAMES	= SYMS.getMonths();

	private CalculationUtil() {
	}

	static public final String[] getDutchMonthShorts() {
		return SHORTNAMES;
	}

	/**
	 * Calculates the age of something on a specified date.
	 * @param today
	 * @param birthdate
	 * @return
	 * @throws Exception
	 */
	static public int ageOn(Date today, Date birthdate) {
		if(today == null || birthdate == null)
			return 0;
		if(today.before(birthdate)) // Make sure parameters are in order
		{
			Date t = birthdate;
			birthdate = today;
			today = t;
		}

		GregorianCalendar ccal = new GregorianCalendar();
		ccal.setTime(today); // Set to current time,
		int cyear = ccal.get(Calendar.YEAR); // Get current year,

		GregorianCalendar bcal = new GregorianCalendar();
		bcal.setTime(birthdate); // Birthday calendar
		int byear = bcal.get(Calendar.YEAR); // Get year of birth,
		int age = cyear - byear; // Basic age is those two subtracted,
		bcal.set(Calendar.YEAR, cyear); // Both dates in same year

		if(ccal.before(bcal))
			age--;
		return age;
	}

	static public int age(Date d) {
		return ageOn(d, new Date());
	}

	/**
	 * Convert a date string in the format yyyy mm dd (with any separators)
	 * to a date value. Throw an exception on error. This uses numeric dates
	 * only.
	 * @param str
	 * @return
	 */
	static public Date date(String str) throws Exception {
		str = str.trim();
		int len = str.length();
		int ix = 0;

		int spos = ix;
		int n1 = 0;
		while(ix < len) {
			char c = str.charAt(ix);
			if(!Character.isDigit(c))
				break;
			ix++;
			n1 = n1 * 10 + (c - '0');
		}
		if(spos == ix)
			wrongDateFormatError(str);

		while(ix < len) // Skip separator
		{
			if(Character.isDigit(str.charAt(ix)))
				break;
			ix++;
		}

		//-- Get months
		int n2 = 0;
		spos = ix;
		while(ix < len) {
			char c = str.charAt(ix);
			if(!Character.isDigit(c))
				break;
			ix++;
			n2 = n2 * 10 + (c - '0');
		}
		if(spos == ix)
			wrongDateFormatError(str);

		while(ix < len) // Skip separator
		{
			if(Character.isDigit(str.charAt(ix)))
				break;
			ix++;
		}

		//-- Get days.
		int n3 = 0;
		spos = ix;
		while(ix < len) {
			char c = str.charAt(ix);
			if(!Character.isDigit(c))
				break;
			ix++;
			n3 = n3 * 10 + (c - '0');
		}
		if(spos == ix || ix != len)
			wrongDateFormatError(str);

		//-- Make a real date from parameters.
		return makeDate(n1, n2, n3);
	}

	/**
	 * Converts a string to a java.util.Date object.
	 */
	static public Date dutchDate(String s) throws Exception {
		Date d = dutchDateRAW(s);
		if(d == null)
			wrongDateFormatError(s);
		return d;
	}

	public static Date dutchDateAndTime(String s) throws ELException {
		String date = s.split(" ")[0];
		String time = s.split(" ")[1];
		Date d = dutchDateRAW(date);
		if(d == null) {
			wrongDateTimeFormatError(s);
		}
		if(!validateTime(time)) {
			wrongDateTimeFormatError(s);
		}
		addTimeToDate(d, time);

		return d;

		//TODO bojan
	}

	private static void addTimeToDate(Date d, String time) {
		int hour = Integer.parseInt(time.split(":")[0]);
		int minutes = Integer.parseInt(time.split(":")[1]);
		d.setHours(hour);
		d.setMinutes(minutes);
	}

	private static boolean validateTime(String t) {
		String time = t.trim();
		int hour = -1;
		int minutes = -1;
		try {
			hour = Integer.parseInt(time.split(":")[0]);
			minutes = Integer.parseInt(time.split(":")[1]);
		} catch(NumberFormatException e) {
			return false;
		}
		if(hour < 0 || hour > 23) {
			return false;
		}
		return minutes >= 0 && minutes <= 59;
	}

	/**
	 * Converts a string to a java.util.Date object.
	 */
	static public Date dutchDateRAW(String s) {
		int len = s.length();
		int ix = 0;

		//-- Skip ws.
		while(ix < len) {
			if(!Character.isWhitespace(s.charAt(ix)))
				break;
			ix++;
		}

		//-- Get number 1
		int n1 = 0;
		int nch = 0;
		while(ix < len) {
			char c = s.charAt(ix);
			if(!Character.isDigit(c))
				break;
			nch++;
			n1 = n1 * 10 + (c - '0'); // Add in digit
			ix++;
		}

		//-- Check first number.
		if(nch == 0 || ix >= len || n1 < 1 || n1 > 31) // Base validity check for day number, and check eo$
			return null;
		ix = skippy(s, len, ix); // Skip any kind of separator

		//-- Get string 2: either a number of a month name
		int spos = ix; // Start pos of string
		int n2 = 0;
		while(ix < len) // Skip all ajacent letters and digits
		{
			char c = s.charAt(ix);
			if(Character.isDigit(c)) // Numeral?
			{
				if(n2 == -1) // Have we seen letters?
					return null;
				n2 = n2 * 10 + (c - '0'); // Count in,
			} else if(Character.isLetter(c)) {
				n2 = -1; // Indicate letters found
			} else
				break; // End of segment
			ix++;
		}
		if(ix == spos || ix >= len) // Nothing gotten [inpossible] or eo$?
			return null;
		String ms = n2 == -1 ? s.substring(spos, ix) : null; // If the month is not numeric make the string,

		//-- Collect part 3: the year.
		ix = skippy(s, len, ix); // Past any separators
		nch = 0;
		int n3 = 0;
		while(ix < len) {
			char c = s.charAt(ix);
			if(!Character.isDigit(c))
				break;
			nch++;
			n3 = n3 * 10 + (c - '0'); // Add in digit
			ix++;
		}
		// if the year consists of 2 digits, add '20' at the begining
		if(nch == 2) {
			n3 += 2000;
		}
		ix = skippy(s, len, ix); // Temove any trailing shit
		if(ix < len || n3 > 2200 || nch == 0) // Not completely consumed, year way too big, no year?
			return null;

		//-- The string was parsed. Decode the month,
		if(ms != null) {
			//-- Try to decode as a short of long month name.
			n2 = -1;
			for(int i = 0; i < SHORTNAMES.length; i++) {
				if(SHORTNAMES[i].equalsIgnoreCase(ms) || LONGNAMES[i].equalsIgnoreCase(ms)) {
					n2 = i + 1;
					break;
				}
			}
		}

		//-- At this point we must have a valid month number (1..12)
		if(n2 < 1 || n2 > 12 || n3 < 1800)
			return null;
		return makeDateRAW(n3, n2, n1);
	}

	static public Date makeDate(int year, int month, int day) throws Exception {
		Date d = makeDateRAW(year, month, day);
		if(d == null)
			throw new ELException("The date " + year + "/" + month + "/" + day + " (yyyy/mm/dd) is invalid.");
		return d;
	}

	static public Date makeDateRAW(int year, int month, int day) {
		if(year >= 1800 && year <= 2200 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
			//-- Finally- make a date :-/
			Calendar cal = new GregorianCalendar(year, month - 1, day, 0, 0, 0);

			//-- Because they're FUCKING MORONIC IDIOTS at Sun we do not know if the date is factually INVALID (feb 30 passes without any trouble)
			if(cal.get(Calendar.DAY_OF_MONTH) == day)
				return cal.getTime();
		}
		return null;
	}

	static private final void wrongDateFormatError(String str) throws ELException {
		throw new ELException("The date '" + str + "' is invalid. Use format yyyy mm dd");
	}

	static private final void wrongDateTimeFormatError(String str) throws ELException {
		throw new ELException("The date '" + str + "' is invalid. Use format dd-mm-yyyy hh-mm ");
	}

	static private int skippy(String s, int len, int ix) {
		while(ix < len) {
			char c = s.charAt(ix);
			if(Character.isDigit(c) || Character.isLetter(c))
				return ix;
			ix++;
		}
		return ix;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Date code.											*/
	/*--------------------------------------------------------------*/
	static public boolean isFutureDate(Date a) {
		return a.after(new Date());
	}

	/**
	 * Convert to a monetary amount. This uses lenient parsing and allows
	 * for both a currency sign before or after and numeric separators. The
	 * currency indicators are just skipped; the thousand separators, when
	 * present, must be valid to prevent problems with replacing decimal point
	 * and comma.
	 *
	 * @param loc
	 * @param s
	 * @return
	 * @throws ELException
	 */
	static public BigDecimal convertToMoney(Locale loc, String s) throws ELException {
		s = s.trim();
		int ix = 0;
		DecimalFormatSymbols df = new DecimalFormatSymbols(loc); // Morons
		char dp = df.getMonetaryDecimalSeparator();
		char thou = df.getGroupingSeparator();
		StringBuilder sb = new StringBuilder(20);

		//-- Step 1: remove crud before.
		char c = 0;
		int len = s.length();
		while(ix < len) {
			c = s.charAt(ix);
			if(c == '+' || c == '-' || Character.isDigit(c))
				break;
			ix++;
		}
		if(ix >= len)
			return null; // No digits.

		//-- Handle plus and minus.
		sb.append(' '); // First char for minus
		boolean minus = false;
		if(c == '-') {
			minus = true;
			ix++;
		}
		int lastthou = 0; // Pos of last thousand separator.

		//-- We must have reached the base of the numeral: start scanning till the decimal point.
		long val = 0;
		long pval = 0;
		int opos = ix;
		while(ix < len) {
			c = s.charAt(ix);
			if(Character.isDigit(c)) {
				val = val * 10 + (c - '0'); // Include digit in running sum
				if(val < pval) // Overflowed?
					return null; // Number too big.
				pval = val;
				sb.append(c);
			} else if(c == dp) // Found decimal point?
			{
				/*
				 * If thousands separators are used then the dp must be at a
				 * thousand interval or the luser is possibly mistaking the
				 * thousands and decimal point signs.
				 */
				if(lastthou > 0) // Found thousands separator?
				{
					if(ix - lastthou != 4) // 4: 3 digits plus the separator itself
						return null; // Decimal point not at 1000 interval.
				}
				break;
			} else if(c == thou) // Is this a thousand separator?
			{
				if(lastthou == 0) // First one found?
					lastthou = ix; // Then just save the position
				else if(ix - lastthou != 4) // Has appropriate amount of digits in between?
					return null; // No - bad thousands separator
				else
					lastthou = ix; // Save location
			} else
				break; // Unexpected character.

			ix++;
		}
		if(ix == opos)
			return null; // No digits.

		/*
		 * Now we either have a .fraction or more crud.
		 */
		long frac = 0;
		int fracfac = 1; // Fractional fraction ;-)
		if(ix < len) // There's still more?
		{
			if(c == dp) // Was a decimal point?
			{
				//-- Fraction.
				ix++; // Past decimal sign
				opos = ix;
				while(ix < len) {
					c = s.charAt(ix);
					if(!Character.isDigit(c))
						break;
					frac = frac * 10 + (c - '0'); // Include thingy in fraction
					fracfac *= 10; // Fractional part
					if(ix == opos)
						sb.append('.');
					sb.append(c);
					ix++;
				}

				/*
				 * Ok: fraction parsed. Allow less than but not more than the supported
				 * number of "Cents".
				 */
				int ndec = df.getCurrency().getDefaultFractionDigits();
				if(ix - opos > ndec)
					return null; // Too many digits in fraction.
			}

			//-- Finally- skip all other crud but do not allow digits.
			while(ix < len) {
				c = s.charAt(ix);
				if(Character.isDigit(c)) // Digits!?
					return null;
				else if(c == '-')
					minus = true;
				ix++;
			}
		}
		if(minus) {
			sb.setCharAt(0, '-');
			return new BigDecimal(sb.toString());
		}
		return new BigDecimal(sb.substring(1));
	}

	static public String convertToMoneyString(Locale loc, BigDecimal n, boolean dofraction, boolean addmonetary) {
		DecimalFormatSymbols df = new DecimalFormatSymbols(loc); // Morons
		NumberFormat nf = addmonetary ? NumberFormat.getCurrencyInstance(loc) : NumberFormat.getNumberInstance(loc);
		String s = nf.format(n); // Make a currency string
		int len = s.length();
		int dpos = s.indexOf(df.getMonetaryDecimalSeparator());
		if(dpos == -1)
			dpos = len;

		StringBuilder sb = new StringBuilder(s.length() + 4);
		sb.append(s, 0, dpos); // Add all but the decimal sign
		if(!dofraction) {
			//-- Remove any fractional amount
			dpos++;
			while(dpos < len) {
				if(!Character.isDigit(s.charAt(dpos)))
					break;
				dpos++;
			}
			if(dpos < len)
				sb.append(s, dpos, len);
			return sb.toString();
		}

		//-- Make sure the #of fractional digits is correct, else add them
		sb.append(df.getMonetaryDecimalSeparator());
		dpos++;
		int nfd = 0;
		while(dpos < len) {
			char c = s.charAt(dpos);
			if(!Character.isDigit(c))
				break;
			dpos++;
			sb.append(c);
			nfd++;
		}
		while(nfd++ < df.getCurrency().getDefaultFractionDigits())
			sb.append('0');
		return sb.toString();
	}

	static public void main(String[] args) {
		BigDecimal bd = new BigDecimal("12345678.12");
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, true, true));
		bd = new BigDecimal("1000");
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, true, true));
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, true, false));
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, false, false));
		bd = new BigDecimal("12312");
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, true, true));
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, true, false));
		System.out.println("Money for " + bd + " is " + convertToMoneyString(LOC, bd, false, false));
	}

}
