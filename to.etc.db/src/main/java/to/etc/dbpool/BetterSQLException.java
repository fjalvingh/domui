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

import java.io.*;
import java.sql.*;
import java.util.*;

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

	private static void fillEmpty(StringBuilder b, int len, String... s) {
		int l = 0;
		for(String string : s) {
			b.append(string);
			l += string.length();

		}
		for(int i = l; i < len; i++) {
			b.append(" ");
		}
	}

	static private String oldFformat(String sql, SQLException rootcause, Object[] par, int ct) {
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

	/**
	 * This method is meant to have a better view on parameter values, see @link(BetterSQLException) for what it should do.
	 * Because it is somewhat more complex then the way it used to be it will fall back on the old method regardless of what is going on.
	 * @param sql
	 * @param rootcause
	 * @param par
	 * @param ct
	 * @return
	 */
	static private String format(String sql, SQLException rootcause, Object[] par, int ct) {
		try {
			StringBuilder sb = new StringBuilder(128);
			sb.append(rootcause == null ? "" : rootcause.toString());
			sb.append("\n\nSQL: ");
			sb.append(sql);
			sb.append('\n');
			boolean insert = sql.toLowerCase().startsWith("insert");

			StreamTokenizer st = new StreamTokenizer(new StringReader(sql)) {
				@Override
				public void parseNumbers() {
					// Ignore this
				}
			};
			st.wordChars('_', '_');
			st.wordChars('.', '.');
			st.wordChars('-', '-');
			st.wordChars('0', '9');


			List<String> names = new ArrayList<String>();
			List<String> types = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			String from = null;
			int nextToken;
			int brace = 0;
			int open = 0;
			String pt = null;
			boolean needNext = false;
			while((nextToken = st.nextToken()) != StreamTokenizer.TT_EOF) {
				String t;
				if(nextToken == StreamTokenizer.TT_NUMBER) {
					t = String.valueOf(st.nval);
				} else if(nextToken == StreamTokenizer.TT_WORD) {
					t = st.sval;
				} else if(st.sval != null) {
					t = "\'" + st.sval + "\'";
				} else {
					t = String.valueOf((char) st.ttype);
				}
				if(needNext) {
					values.add(t);
					needNext = false;
				} else if(t.equals("(")) {
					brace++;
					open++;
				} else if(t.equals(")")) {
					brace--;
				} else if(insert && !t.equals(",") && brace != 0) {
					if(open == 1) {
						names.add(t);
					} else if(open == 2) {
						values.add(t);
					}
				} else if(t.equals("=")) {
					names.add(pt);
					needNext = true;
				}
				if(pt != null && pt.equalsIgnoreCase("from")) {
					from = t;
				}
				pt = t;
			}


			int c = 0;
			for(int i = 0; i < names.size(); i++) {
				String v = values.get(i);
				if(v.trim().equals("?")) {
					if(par[c] != null) {
						values.set(i, par[c].toString());
						types.add(par[c].getClass().getName());
					} else {
						values.set(i, "[null]");
						types.add("[null]");
					}
					c++;
				} else {
					types.add(null);
				}
			}

			int maxn = 0;
			int maxt = 0;
			int maxv = 0;
			for(int i = 0; i < names.size(); i++) {
				int l = names.get(i).length() + 2;
				if(l > maxn) {
					maxn = l;
				}
			}
			for(int i = 0; i < values.size(); i++) {
				int l = values.get(i).length() + 2;
				if(l > maxv) {
					maxv = l;
				}
			}
			for(int i = 0; i < types.size(); i++) {
				String type = types.get(i);
				if(type != null) {
					int l = type.length() + 2;
					if(l > maxt) {
						maxt = l;
					}

				}
			}

			if(from != null) {
				sb.append("From : ");
				sb.append(from);
				sb.append("\n");
			}
			sb.append("Parameters:");
			int typec = 0;
			for(int i = 0; i < names.size(); i++) {
				String type = types.get(i);
				if(type == null) {
					fillEmpty(sb, 5, "\n", "");
					fillEmpty(sb, maxt, "  ", "");
					sb.append("   ");
				} else {
					typec++;
					fillEmpty(sb, 5, "\n#", String.valueOf((typec)));
					fillEmpty(sb, maxt, ": ", type);
					sb.append(" : ");
				}

				String name = names.get(i);
				fillEmpty(sb, maxn, name);

				sb.append(" = ");
				sb.append(values.get(i));

				if(sb.length() > 16384) {
					sb.append(".... (truncated)...\n");
					break;
				}
			}
			return sb.toString();
		} catch(Exception e) {
			return oldFformat(sql, rootcause, par, ct);
		}
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

