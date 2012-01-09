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
package to.etc.dbpool;

import java.sql.*;

/**
 * Created on Jul 8, 2003
 * @author jal
 */
public class BetterSQLException extends SQLException {
	private static final long serialVersionUID = 3916681942006359065L;

	public BetterSQLException(String sql, SQLException rootcause) {
		super(format(sql, rootcause, null, 0));
		this.initCause(rootcause);
	}

	public BetterSQLException(String sql, Object[] par, int ct, SQLException rootcause) {
		super(format(sql, rootcause, par, ct));
		this.initCause(rootcause);
	}

	@Override
	public int getErrorCode() {
		if(getCause() != null) {
			return ((SQLException) getCause()).getErrorCode();
		}
		return 0;
	}

	@Override
	public String getSQLState() {
		if(getCause() != null) {
			return ((SQLException) getCause()).getSQLState();
		}
		return null;
	}

	static private String format(String sql, SQLException rootcause, Object[] par, int ct) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(rootcause.toString());
		sb.append("\n\nSQL: ");
		sb.append(sql);
		sb.append('\n');
		if(par != null && ct > 0) {
			sb.append("Parameters:\n");
			for(int i = 0; i < ct; i++) {
				sb.append("#");
				sb.append(i + 1);
				sb.append(":");
				Object o = par[i];
				if(o == null)
					sb.append("[null]\n");
				else {
					sb.append(o.getClass().getName());
					sb.append(":");
					sb.append(o.toString());
					sb.append("\n");
				}
				if(sb.length() > 8192) {
					sb.append(".... (truncated)...\n");
					break;
				}
			}
		}

		return sb.toString();
	}

	static public String format(Object[] par, int ct) {
		StringBuilder sb = new StringBuilder(128);
		if(par != null && ct > 0) {
			sb.append("Parameters:\n");
			for(int i = 0; i < ct; i++) {
				sb.append("#");
				sb.append(i + 1);
				sb.append(":");
				Object o = par[i];
				if(o == null)
					sb.append("[null]\n");
				else {
					sb.append(o.getClass().getName());
					sb.append(":");
					sb.append(o.toString());
					sb.append("\n");
				}
				if(sb.length() > 8192) {
					sb.append(".... (truncated)...\n");
					break;
				}
			}
		}

		return sb.toString();
	}
}
