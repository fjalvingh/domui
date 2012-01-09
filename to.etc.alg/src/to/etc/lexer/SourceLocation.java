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
package to.etc.lexer;

/**
 * This defines a source location for some parsed construct. It can
 * be obtained for a token from the lexer if needed.
 *
 * Created on Sep 13, 2004
 * @author jal
 */
public class SourceLocation {
	private Object	m_src;

	private int		m_line;

	private int		m_col;

	public SourceLocation(Object src, int line, int col) {
		m_src = src;
		m_line = line;
		m_col = col;
	}

	public SourceLocation(ReaderScannerBase rt) {
		m_src = rt.getSource();
		m_line = rt.getTokenLine();
		m_col = rt.getTokenColumn();
	}

	/**
	 * @return Returns the col.
	 */
	public int getCol() {
		return m_col;
	}

	/**
	 * @return Returns the line.
	 */
	public int getLine() {
		return m_line;
	}

	/**
	 * @return Returns the src.
	 */
	public Object getSrc() {
		return m_src;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(m_src != null) {
			sb.append(m_src.toString());
			sb.append('(');
		} else
			sb.append("Line ");
		sb.append(m_line);
		if(m_col >= 0) {
			sb.append(':');
			sb.append(m_col + 1);
		}
		if(m_src != null)
			sb.append(')');
		return sb.toString();
	}
}
