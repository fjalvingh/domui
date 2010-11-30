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
package to.etc.webapp.qsql;

import java.sql.*;

public class JdbcConversionException extends RuntimeException {
	public JdbcConversionException(Throwable cause, String message) {
		super(message, cause);
	}

	public JdbcConversionException(String message) {
		super(message);
	}

	public static JdbcConversionException create(Exception x, ResultSet rs, JdbcPropertyMeta pm, int rix) {
		StringBuilder sb = new StringBuilder();
		sb.append("JDBC Data Conversion failed: property ");
		sb.append(pm);
		sb.append(" could not be set from result set value '");

		String lv = "(-)";
		try {
			lv = rs.getString(rix);
		} catch(Exception xx) {}
		sb.append(lv);
		sb.append("' @ index=");
		sb.append(rix);
		sb.append(": ");
		sb.append(x.toString());
		throw new JdbcConversionException(x, sb.toString());
	}
}
