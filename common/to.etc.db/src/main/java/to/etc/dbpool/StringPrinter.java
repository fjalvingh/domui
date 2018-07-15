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

public class StringPrinter implements IPrinter {
	private StringBuilder m_sb;

	public StringPrinter() {
		m_sb = new StringBuilder(8192);
	}

	public StringPrinter(StringBuilder sb) {
		m_sb = sb;
	}

	public IPrinter header(String cssclass, String name) {
		m_sb.append("--- ").append(name).append(" ---\n");
		return this;
	}

	public IPrinter warning(String what) {
		m_sb.append("*warning: ").append(what).append("\n");
		return this;
	}

	public IPrinter nl() {
		m_sb.append("\n");
		return this;
	}

	public IPrinter pre(String css, String pre) {
		m_sb.append(pre);
		return this;
	}

	public IPrinter text(String s) {
		m_sb.append(s);
		return this;
	}

	public String getText() {
		String s = m_sb.toString();
		m_sb.setLength(0);
		return s;
	}
}
