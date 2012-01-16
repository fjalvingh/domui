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
package to.etc.util;

import java.io.*;

/**
 * A logsink which logs into an Appendable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class MemoryLogSink implements ILogSink {
	private Appendable	m_sb;

	public MemoryLogSink(Appendable a) {
		m_sb = a;
	}

	public void exception(Throwable t, String msg) {
		try {
			m_sb.append("Exception: ");
			m_sb.append(msg);
			m_sb.append("\n");
			m_sb.append(": ");
			StringTool.strStacktrace(m_sb, t);
		} catch(IOException x) {
			//-- Never happens really but James Gosling is an Idiot.
			throw new WrappedException(x);
		}
	}

	public void log(String msg) {
		try {
			m_sb.append(msg);
			m_sb.append("\n");
		} catch(IOException x) {
			//-- Never happens really but James Gosling is an Idiot.
			throw new WrappedException(x);
		}
	}
}
