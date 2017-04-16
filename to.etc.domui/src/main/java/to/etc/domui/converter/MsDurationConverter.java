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

import javax.annotation.*;
import java.util.*;

public class MsDurationConverter implements IConverter<Long> {
	static private final long DAYS = 24 * 60 * 60;

	static private final long HOURS = 60 * 60;

	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		if(in.longValue() < 0)
			return "";
		return strDurationMillis(in.longValue());
	}

	@Override
	public Long convertStringToObject(Locale loc, String in) throws UIException {
		if(null == in)
			return null;
		in = in.trim();
		if(in.length() == 0)
			return null;
		MiniScanner ms = MiniScanner.getInstance();
		ms.init(in);
		long dur = 0;
		ms.skipWs();
		long pdur = 0;
		while(!ms.eof()) {
			int nr = scanNumber(ms);
			if(nr == -1)
				throw new ValidationException(Msgs.V_BAD_DURATION);

			ms.skipWs();
			int mc = ms.LA();
			switch(mc) {
				default:
					throw new ValidationException(Msgs.V_BAD_DURATION);

				case -1:
				case 'd':
				case 'D':
					if(nr >= 100000)
						throw new ValidationException(Msgs.V_BAD_DURATION);
					dur += nr * DAYS * 1000;
					break;

				case 'H':
				case 'h':
				case 'U':
				case 'u':
					dur += nr * HOURS * 1000;
					break;

				case 'm':
				case 'M':
					if(ms.LA(1) == 's' || ms.LA(1) == 'S') {
						dur += nr;
						ms.accept();
					} else {
						dur += nr * 60 * 1000;
					}
					break;

				case 's':
				case 'S':
					dur += nr;
					break;

			}
			if(dur < pdur)
				throw new ValidationException(Msgs.V_BAD_DURATION);
			pdur = dur;

			ms.accept();
			ms.skipWs();
		}
		return Long.valueOf(dur);
	}

	private int scanNumber(@Nonnull MiniScanner ms) {
		int nr = 0;
		int ct = 0;
		for(;;) {
			int c = ms.LA();
			if(!Character.isDigit(c))
				return ct > 0 ? nr : -1;
			nr = nr * 10 + Character.digit(c, 10);
			ct++;
			ms.accept();
		}
	}

	static public String strDurationMillis(long dlt) {
		StringBuffer sb = new StringBuffer();

		int millis = (int) (dlt % 1000); // Get milliseconds,
		dlt /= 1000; // Now in seconds,

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
		if(millis != 0) {
			if(sp)
				sb.append(' ');
			sb.append(millis);
			sb.append("ms");
		}
		return sb.toString();
	}

}
