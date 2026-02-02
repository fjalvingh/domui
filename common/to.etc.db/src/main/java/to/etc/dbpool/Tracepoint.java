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

/**
 * This contains a stack trace location. It is not yet pruned.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class Tracepoint {
	final private long m_timestamp;

	final private StackTraceElement[] m_elements;

	final private RuntimeException m_asException;

	final private String m_sql;

	private Tracepoint(long ts, RuntimeException x, String sql) {
		m_elements = x.getStackTrace();
		m_asException = x;
		m_timestamp = ts;
		m_sql = sql;
	}

	public RuntimeException getException() {
		return m_asException;
	}

	public String getSql() {
		return m_sql;
	}

	public StackTraceElement[] getElements() {
		return m_elements;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	/**
	 * Create a tracepoint for the current stack location and timestamp.
	 * @return
	 */
	static Tracepoint create(String sql) {
		try {
			throw new RuntimeException();
		} catch(RuntimeException x) {
			StackTraceElement[] se = x.getStackTrace();
			return new Tracepoint(System.currentTimeMillis(), x, sql);
		}
	}
}
