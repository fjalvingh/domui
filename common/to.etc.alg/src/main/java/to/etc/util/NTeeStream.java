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
import to.etc.function.ConsumerEx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An output stream which duplicates all contents written to it to N other output streams.
 * Uses parallel writes for each write command, so best is if it is wrapped inside some BufferedOutputStream.
 */
@NonNullByDefault
public class NTeeStream extends OutputStream {

	private final OutputStream[]	m_streams;

	private final Exception[]	m_exceptionsOnStreams;

	@Nullable
	private BiConsumerEx<Integer, Exception> m_exHandler;

	private ExecutorService m_executors;

	private boolean m_closed;

	public NTeeStream(OutputStream... streams) {
		m_streams = streams;
		m_exceptionsOnStreams = new Exception[streams.length];
		m_executors = Executors.newFixedThreadPool(streams.length);
	}

	@Override
	public void write(byte[] parm1) throws IOException {
		parallelAction(stream -> stream.write(parm1));
	}

	@Override
	public void flush() throws IOException {
		parallelAction(stream -> stream.flush());
	}

	@Override
	public void write(int b) throws IOException {
		parallelAction(stream -> stream.write(b));
	}

	@Override
	public void write(byte[] parm1, int parm2, int parm3) throws IOException {
		parallelAction(stream -> stream.write(parm1, parm2, parm3));
	}

	@Override
	public void close() throws IOException {
		if(m_closed) {
			return;
		}
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

		m_executors.shutdown();

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
		m_closed = true;
	}

	private void parallelAction(ConsumerEx<OutputStream> action) {
		List<Callable<Boolean>> parallelWriteTasks = new ArrayList<>();
		for(int index = 0; index < m_streams.length; index++) {

			if(null == m_exceptionsOnStreams[index]) {
				int aIndex = index;
				parallelWriteTasks.add(() -> {
					try {
						action.accept(m_streams[aIndex]);
						return Boolean.TRUE;
					} catch(IOException x) {
						m_exceptionsOnStreams[aIndex] = x;
						FileTool.closeAll(m_streams[aIndex]);
						handle(aIndex, x);
						return Boolean.FALSE;
					}
				});
			}
		}
		if(!parallelWriteTasks.isEmpty()) {
			try {
				m_executors.invokeAll(parallelWriteTasks);
			} catch(Exception e) {
				throw new RuntimeException(e);
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
