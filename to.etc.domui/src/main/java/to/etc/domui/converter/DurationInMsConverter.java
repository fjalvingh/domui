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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.Msgs;

import java.util.Locale;

/**
 * Shows a remaining duration in ms into normal units up till seconds.
 */
public class DurationInMsConverter implements IConverter<Long> {
	static private final long DAYS = 24 * 60 * 60;

	static private final long HOURS = 60 * 60;

	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		long v = in.longValue() / 1000;			// v now in seconds
		if(v <= 0)
			return "0:00:00";

		StringBuilder sb = new StringBuilder();
		int days = (int) (v / DAYS);
		if(days > 0) {
			sb.append(days).append("D ");
		}
		v = v % DAYS;

		int hours = (int) (v / HOURS);
		sb.append(hours).append(':');
		v = v % HOURS;
		int mins = (int) (v / 60);
		if(mins < 10)
			sb.append('0');
		sb.append(mins).append(':');

		v = v % 60;
		if(v < 10)
			sb.append('0');
		sb.append(v);
		return sb.toString();
	}

	@Override
	public Long convertStringToObject(Locale loc, String in) throws UIException {
		if(null == in)
			return null;
		in = in.trim();
		if(in.isEmpty())
			return null;
		MiniScanner ms = MiniScanner.getInstance();
		ms.init(in);
		long dur = 0;
		ms.skipWs();
		long pdur = 0;
		while(!ms.eof()) {
			int nr = scanNumber(ms);
			if(nr == -1)
				throw new ValidationException(Msgs.vBadDuration);

			ms.skipWs();
			int mc = ms.LA();
			switch(mc) {
				default:
					throw new ValidationException(Msgs.vBadDuration);

				case -1:
				case 'd':
				case 'D':
					if(nr >= 100000)
						throw new ValidationException(Msgs.vBadDuration);
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
				throw new ValidationException(Msgs.vBadDuration);
			pdur = dur;

			ms.accept();
			ms.skipWs();
		}
		return Long.valueOf(dur);
	}

	private int scanNumber(@NonNull MiniScanner ms) {
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


}
