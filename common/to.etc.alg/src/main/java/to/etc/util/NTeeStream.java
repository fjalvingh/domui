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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.BiConsumerEx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An outputstream which duplicates all contents written to it to N other output streams.
 */
@NonNullByDefault
public class NTeeStream extends OutputStream {

	private final OutputStream[]	m_streams;

	private final Exception[]	m_exceptionsOnStreams;

	@Nullable
	private BiConsumerEx<Integer, Exception> m_exHandler;

	public NTeeStream(OutputStream... streams) {
		m_streams = streams;
		m_exceptionsOnStreams = new Exception[streams.length];
	}

	@Override
	public void write(byte[] parm1) throws IOException {
		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					m_streams[index].write(parm1);
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				m_streams[index].close();
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	@Override
	public void flush() throws IOException {
		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					m_streams[index].flush();
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				m_streams[index].close();
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	@Override
	public void write(int b) throws IOException {
		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					m_streams[index].write(b);
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				m_streams[index].close();
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	@Override
	public void write(byte[] parm1, int parm2, int parm3) throws IOException {
		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					m_streams[index].write(parm1, parm2, parm3);
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				m_streams[index].close();
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	@Override
	public void close() throws IOException {
		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					m_streams[index].close();
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	public Exception[] getExceptionsOnStreams() {
		return m_exceptionsOnStreams;
	}

	public int getStreamsWithExceptions() {
		return (int) Arrays.stream(m_exceptionsOnStreams).filter(it -> null != it).count();
	}

	public void setOnStreamException(BiConsumerEx<Integer, Exception> consumer) {
		m_exHandler = consumer;
	}

	private void handle(int index, Exception ex) {
		BiConsumerEx<Integer, Exception> exHandler = m_exHandler;
		if(null != exHandler) {
			try {
				exHandler.accept(index, ex);
			} catch(Exception exception) {
				throw new WrappedException(exception);
			}
		}
	}
}
