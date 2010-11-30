/*
 * DomUI Java User Interface library
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
package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class DateTimeConverter implements IConverter<Date> {
	static private final ThreadLocal<DateFormat> m_format = new ThreadLocal<DateFormat>();

	static private DateFormat getFormatter() {
		DateFormat df = m_format.get();
		if(df == null) {
			df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			m_format.set(df);
		}
		return df;
	}


	@Override
	public String convertObjectToString(Locale loc, Date in) throws UIException {
		if(in == null)
			return "";
		//		if(!(in instanceof Date))
		//			throw new IllegalStateException("Type must be java.util.Date for this converter");
		Date dt = in;
		if(loc.getLanguage().equalsIgnoreCase("nl")) {
			return getFormatter().format(dt);
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
		return df.format(dt);
	}

	@Override
	public Date convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc);
		try {
			if(loc.getLanguage().equalsIgnoreCase("nl"))
				df = getFormatter();
			//				return CalculationUtil.dutchDate(input);
			return df.parse(input);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
