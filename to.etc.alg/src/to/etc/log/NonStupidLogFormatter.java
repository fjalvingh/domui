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
package to.etc.log;

import java.text.*;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NonStupidLogFormatter extends Formatter {
	static private final DateFormat	m_df	= new SimpleDateFormat("HH:mm:ss");

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_df.format(new Date(record.getMillis())));
		sb.append(' ');
		sb.append(record.getMessage());
		sb.append(" (");
		sb.append(record.getLoggerName());
		String m = record.getSourceMethodName();
		if(m != null && m.length() > 0) {
			sb.append(" @");
			sb.append(m);
		}
		sb.append(")\n");
		return sb.toString();
	}
}
