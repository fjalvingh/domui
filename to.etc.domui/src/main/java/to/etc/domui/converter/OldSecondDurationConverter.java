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

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import java.util.*;

public class OldSecondDurationConverter implements IConverter<Long> {
	static private final long DAYS = 24 * 60 * 60;

	static private final long HOURS = 60 * 60;

	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		//		if(!(in instanceof Number))
		//			throw new IllegalStateException("Type must extend Number for this converter");
		long dlt = in.longValue();
		//		boolean	sp = false;
		StringBuilder sb = new StringBuilder(20);
		if(dlt > DAYS) {
			sb.append(Long.toString(dlt / DAYS));
			sb.append("D ");
			dlt %= DAYS;
		}

		sb.append(Long.toString(dlt / HOURS)); // #of hours (0..23)
		dlt %= HOURS;
		sb.append(':');
		sb.append(StringTool.intToStr((int) (dlt / 60), 10, 2));
		dlt %= 60;
		if(dlt > 0) {
			sb.append(':');
			sb.append(StringTool.intToStr((int) dlt, 10, 2));
		}
		return sb.toString();
	}

	/**
	 * Convert a duration in the format: [xD] hh:mm[:ss] into a duration in seconds.
	 * @see IConverter#convertStringToObject(java.util.Locale, String)
	 */
	@Override
	public Long convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		try {
			MiniScanner s = new MiniScanner();
			return Long.valueOf(s.scanDuration(input));
		} catch(ValidationException x) {
			throw x;
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DATE);
		}
	}
}
