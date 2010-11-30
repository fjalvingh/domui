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
package to.etc.webapp.ajax.renderer;

import java.io.*;

abstract public class StructuredWriter extends Writer {
	private Writer m_w;

	private int m_size;

	public StructuredWriter(Writer w) {
		m_w = w;
	}

	@Override
	final public void write(char[] cbuf, int off, int len) throws IOException {
		m_w.write(cbuf, off, len);
		m_size += len;
	}

	final public int size() {
		return m_size;
	}

	abstract public void end() throws Exception;

	abstract public void list(String name) throws Exception;

	abstract public void record(String name) throws Exception;

	abstract public void field(String name, String value) throws Exception;

	abstract public void field(String name, boolean value) throws Exception;

	abstract public void field(String name, java.util.Date value) throws Exception;

	abstract public void field(String name, Number value) throws Exception;
}
