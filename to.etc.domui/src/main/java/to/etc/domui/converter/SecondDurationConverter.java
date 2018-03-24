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

import java.util.*;

public class SecondDurationConverter implements IConverter<Long> {
	private static final MsDurationConverter INSTANCE = new MsDurationConverter();

	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		if(in.longValue() < 0)
			return "";

		StringBuffer sb = new StringBuffer();

		long dlt = in.longValue();			// Seconds

		final long DAYS = 24 * 60 * 60;
		final long HOURS = 60 * 60;

		boolean sp = false;
		if(dlt >= DAYS) {
			sb.append(dlt / DAYS);
			sb.append("D");
			dlt %= DAYS;
			sp = true;
		}
		if(dlt >= HOURS) {
			long v = dlt / HOURS;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("u");
				sp = true;
			}
			dlt %= HOURS;
		}
		if(dlt >= 60) {
			long v = dlt / 60;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("m");
				sp = true;
			}
			dlt %= 60;
		}
		if(dlt != 0) {
			if(sp)
				sb.append(' ');
			sb.append(dlt);
			sb.append("s");
			sp = true;
		}

		if(! sp)
			return "0 s";
		return sb.toString();
	}

	/**
	 * Convert a duration in the format: [xD] hh:mm[:ss] into a duration in seconds.
	 * @see to.etc.domui.converter.IConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Long convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		Long res = INSTANCE.convertStringToObject(loc, input);
		if(res == null)
			return res;
		return Long.valueOf(res.longValue() / 1000);
	}
}
