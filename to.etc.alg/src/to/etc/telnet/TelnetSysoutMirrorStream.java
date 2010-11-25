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
package to.etc.telnet;

import java.io.*;

/**
 * This forms a TEE for sysout and the telnet server. All data written to this
 * stream is copied both to the Telnet server AND the system.out stream.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetSysoutMirrorStream extends OutputStream {
	/// The original system.out print stream,
	protected PrintStream	m_out_ps;

	/// The Telnet server to talk to,
	protected TelnetServer	m_ts;


	public TelnetSysoutMirrorStream(TelnetServer ts, PrintStream ps) {
		m_out_ps = ps;
		m_ts = ts;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		m_ts._write(b);
		m_out_ps.write(b);
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		m_ts._write(parm1, 0, parm1.length);
		m_out_ps.write(parm1);
	}

	@Override
	public void flush() throws java.io.IOException {
		super.flush();
	}

	@Override
	public void write(byte[] parm1, int offset, int len) throws java.io.IOException {
		m_ts._write(parm1, offset, len);
		m_out_ps.write(parm1, offset, len);
	}

	@Override
	public void close() throws java.io.IOException {
		super.close();
	}


}
