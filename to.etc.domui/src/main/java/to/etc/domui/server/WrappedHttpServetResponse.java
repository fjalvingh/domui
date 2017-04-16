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
package to.etc.domui.server;

import java.io.*;

import javax.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WrappedHttpServetResponse extends HttpServletResponseWrapper {
	/**
	 * The emulation mode that the page would want to have. When null the "default emulation mode" will
	 * be used, which is the mode for all pages that did *not* register a certain mode. This is the
	 * "default" content of the x-ua-compatible header. When null no such header will be sent.
	 */
	@Nullable
	private String m_ieEmulation;

	private boolean m_flushed;

	/** The actual outputstream. */
	private ServletOutputStream	m_sos;

	/** The wrapped stream which buffers. */
	private ServletOutputStream	m_wrappedos;

	private PrintWriter m_wr;

	private PrintWriter m_wrappedwr;

	private byte[]	m_buffer;

	private int m_bix, m_blen;

	private char[] m_chbuffer;

	final private String m_url;

	public WrappedHttpServetResponse(@Nonnull String url, @Nonnull HttpServletResponse resp, @Nonnull String defaultVersion) {
		super(resp);
		m_url = url;
		m_blen = 8192;
		m_ieEmulation = defaultVersion;
	}

	/**
	 * Set the mode you want. If you do not care set to {@link InternetExplorerVersion}.EDGE. If you do not want to send any header set to null.
	 * @param iemode
	 * @throws IOException
	 */
	public void setIeEmulationMode(@Nullable String compatible) throws IOException {
		if(m_flushed)
			throw new RuntimeException("InternetExplorer filter: output already flushed - cannot remove IE emulation header anymore! Move this call to the BEGINNING of the page.");
		m_ieEmulation = compatible;
		internalFlush();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stream-based crap.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(m_wrappedwr != null)
			throw new IllegalStateException("Attempting to allocate an output stream but a writer was allocated earlier");
		if(m_sos == null) {
			m_sos = super.getOutputStream();
			m_buffer = new byte[m_blen];
			m_wrappedos = new WrappedOS();
		}

		return m_wrappedos;
	}

	void writeByte(int b) throws IOException {
		if(m_flushed) {
			m_sos.write(b);
		} else {
			m_buffer[m_bix++] = (byte) b;
			if(m_bix >= m_blen)
				internalFlush();
		}
	}

	public void writeBuffer(byte[] b, int off, int len) throws IOException {
		if(m_flushed)
			m_sos.write(b, off, len);
		else if(m_bix + len >= m_blen) {
			internalFlush();				// Flush internal buffer
			m_sos.write(b, off, len);
		} else {
			System.arraycopy(b, off, m_buffer, m_bix, len);
			m_bix += len;
		}
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if(m_sos != null)
			throw new IllegalStateException("Attempting to allocate a Writer, but a stream was allocated earlier!");
		if(m_wr == null) {
			m_chbuffer = new char[m_blen];
			m_wr = super.getWriter();
			m_wrappedwr = new PrintWriter(new WrappedWriter());
		}
		return m_wrappedwr;
	}

	public void writeCharacters(char[] b, int off, int len) throws IOException {
		if(m_flushed)
			m_wr.write(b, off, len);
		else if(m_bix + len >= m_blen) {
			internalFlush(); // Flush internal buffer
			m_wr.write(b, off, len);
		} else {
			System.arraycopy(b, off, m_chbuffer, m_bix, len);
			m_bix += len;
		}
	}
	@Override
	public void flushBuffer() throws IOException {
		internalFlush();
		super.flushBuffer();
	}

	private void	internalFlush() throws IOException {
		if(m_flushed)
			return;

		//-- Only send a header if the emulation is set.
		if(m_ieEmulation != null) {
			addHeader("X-UA-Compatible", m_ieEmulation);
			//			System.out.println(m_url + ": Sending " + m_ieEmulation);
		}
		//		else
		//			System.out.println(m_url + ": Not sending x-ua-compatible");

		//-- Writeout the buffert
		if(m_bix > 0) {
			if(m_wr != null) {
				m_wr.write(m_chbuffer, 0, m_bix);
				m_chbuffer = null;
			} else {
				m_sos.write(m_buffer, 0, m_bix);
				m_buffer = null;
			}
			m_bix = m_blen = 0;
		}
		m_flushed = true;
	}

	private class WrappedWriter extends Writer {
		public WrappedWriter() {}

		@Override
		public void close() throws IOException {
			flushBuffer();
		}

		@Override
		public void flush() throws IOException {
		//			flushBuffer();
		}

		@Override
		public void write(char[] buf, int off, int len) throws IOException {
			writeCharacters(buf, off, len);
		}
	}

	private class WrappedOS extends ServletOutputStream {
		public WrappedOS() {
		}

		@Override
		public void write(int b) throws IOException {
			writeByte(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			writeBuffer(b, off, len);
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
		}
	}
}
