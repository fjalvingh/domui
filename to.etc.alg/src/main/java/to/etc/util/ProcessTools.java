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

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * Helper code to spawn processes and capture their output.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
final public class ProcessTools {
	public interface IFollow {
		void newData(boolean stderr, @Nonnull char[] data, int length);
	}

	@Nonnull
	final private ProcessBuilder m_builder;

	private Writer m_stdout;

	private Writer m_stderr;

	private IFollow m_follow;

	private boolean m_flush;

	public ProcessTools() {
		m_builder = new ProcessBuilder();
	}

	public ProcessTools(@Nonnull ProcessBuilder pb) {
		m_builder = pb;
	}

	@Nonnull
	public ProcessTools stdout(@Nonnull Writer stdout) {
		m_stdout = stdout;
		return this;
	}

	@Nonnull
	public ProcessTools stderr(@Nonnull Writer stderr) {
		m_stderr = stderr;
		return this;
	}

	@Nonnull
	public ProcessTools add(@Nonnull String cmd) {
		m_builder.command().add(cmd);
		return this;
	}

	@Nonnull
	public ProcessTools add(@Nonnull List<String> cmd) {
		m_builder.command().addAll(cmd);
		return this;
	}

	@Nonnull
	public ProcessTools add(String... args) {
		for(String s: args)
			m_builder.command().add(s);
		return this;
	}

	@Nonnull
	public ProcessTools setCommand(@Nonnull List<String> cmd) {
		m_builder.command(cmd);
		return this;
	}

	@Nonnull
	public ProcessTools directory(@Nonnull File cmd) {
		m_builder.directory(cmd);
		return this;
	}

	@Nonnull
	public ProcessTools follow(@Nonnull IFollow cmd) {
		m_follow = cmd;
		return this;
	}

	@Nonnull
	public ProcessTools flush() {
		m_flush = true;
		return this;
	}

	@Nonnull
	public Map<String, String> env() {
		return m_builder.environment();
	}

	@Nonnull
	public ProcessTools envStrip(@Nonnull String... names) {
		for(String name : names) {
			env().remove(name);
		}
		return this;
	}

	public int run() throws Exception {
		Writer stdout = m_stdout;
		Writer stderr = m_stderr;
		if(null == stdout)
			throw new IllegalStateException("Stdout not redirected");
		if(null == stderr) {
			m_builder.redirectErrorStream(true);
		}
		Process pr = m_builder.start();

		StreamReaderThread outr = new StreamReaderThread(stdout, "stdout", pr.getInputStream(), null, m_follow, m_flush);
		outr.start();
		StreamReaderThread errr = null;
		if(stderr != null) {
			errr = new StreamReaderThread(stderr, "stderr", pr.getErrorStream(), null, m_follow, m_flush);
			errr.start();
		}
		int rc = pr.waitFor();
		outr.join();
		if(null != errr)
			errr.join();
		return rc;


	}

	/**
	 *	This is used to async read strout and stderr streams from a process...
	 */
	static public class StreamReaderThread extends Thread {
		/** The stream to read, */
		private Reader			m_reader;

		/** The output writer thing. */
		private final Writer	m_w;

		@Nonnull
		private final char[]	m_buf;

		/** When T this flushes written output. */
		private boolean m_flush;

		private IFollow m_follow;

		public StreamReaderThread(final Appendable sb, String name, InputStream is) {
			this(sb, name, is, System.getProperty("file.encoding"));
		}

		public StreamReaderThread(final Appendable sb, String name, InputStream is, String encoding) {
			this(new Writer() {
				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
					while(len-- > 0)
						sb.append(cbuf[off++]);
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void close() throws IOException {
				}
			}, name, is, encoding, null, false);
		}

		public StreamReaderThread(Writer sb, String name, InputStream is) {
			this(sb, name, is, System.getProperty("file.encoding"), null, false);
		}

		public StreamReaderThread(Writer w, String name, InputStream is, String encoding, IFollow follow, boolean flush) {
			m_w = w;
			m_buf = new char[8192];
			m_follow = follow;
			m_flush = flush;
			setName("StreamReader" + name);
			if(null == encoding)
				encoding = System.getProperty("file.encoding");
			try {
				m_reader = new InputStreamReader(is, encoding);
			} catch(UnsupportedEncodingException x)
			{
				throw new IllegalStateException("Unsupported encoding " + encoding);
			}
		}

		/**
		 * Read data from the stream until it closes line by line; add each line to
		 * the output channel.
		 */
		@Override
		public void run() {
			try {
				int szrd;
				IFollow follow = m_follow;
				while(0 <= (szrd = m_reader.read(m_buf))) {
					//					System.out.println("dbg: writing "+szrd+" chars to the stream");
					m_w.write(m_buf, 0, szrd);
					if(m_flush) {
						if(szrd > 512 || needsFlush(m_buf, szrd))
							m_w.flush();
					}
					if(null != follow) {
						try {
							m_follow.newData(false, m_buf, szrd);
						} catch(Exception x) {
							x.printStackTrace();
						}
					}
				}
				m_w.flush();
			} catch(Throwable x) {
				x.printStackTrace();
			} finally {
				try {
					if(m_reader != null)
						m_reader.close();
				} catch(Exception x) {}
			}
			//			System.out.println("Reader "+m_name+" terminated.");
		}

		static private boolean needsFlush(@Nonnull char[] buf, int szrd) {
			while(--szrd >= 0) {
				if(buf[szrd] == '\n')
					return true;
			}
			return false;
		}

	}

	/**
	 *	This is used to async read strout and stderr streams from a process into another output stream.
	 */
	static public class StreamCopyThread extends Thread {
		/** The stream to read, */
		private InputStream		m_is;

		private OutputStream	m_os;

		private final byte[]	m_buf;

		public StreamCopyThread(final OutputStream os, String name, InputStream is) {
			m_os = os;
			m_is = is;
			m_buf = new byte[1024];
			setName("StreamReader" + name);
		}

		/**
		 * Read data from the stream until it closes line by line; add each line to
		 * the output channel.
		 */
		@Override
		public void run() {
			try {
				int szrd;
				while(0 < (szrd = m_is.read(m_buf))) {
					m_os.write(m_buf, 0, szrd);
				}
				m_os.flush();
			} catch(Throwable x) {
				x.printStackTrace();
			} finally {
				try {
					if(m_is != null)
						m_is.close();
				} catch(Exception x) {}
			}
		}
	}

	/**
	 * Waits for completion of the command and collect data into the streams.
	 */
	static public int dumpStreams(Process pr, Appendable iosb) throws Exception {
		//-- Create two reader classes,
		StreamReaderThread outr = new StreamReaderThread(iosb, "stdout", pr.getInputStream());
		StreamReaderThread errr = new StreamReaderThread(iosb, "stderr", pr.getErrorStream());

		//-- Start both of 'm
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result with stdout and stderr merged.
	 * @param pb
	 * @param sb
	 * @return
	 * @throws Exception
	 */
	static public int runProcess(ProcessBuilder pb, Appendable sb) throws Exception {
		pb.redirectErrorStream(true); // Merge stdout and stderr
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(sb, "stdout", pr.getInputStream());
		outr.start();
		int rc = pr.waitFor();
		outr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result.
	 * @param pb
	 * @param sb
	 * @return
	 * @throws Exception
	 */
	static public int runProcess(ProcessBuilder pb, OutputStream stdout, Appendable stderrsb) throws Exception {
		Process pr = pb.start();
		StreamReaderThread errr = new StreamReaderThread(stderrsb, "stderr", pr.getErrorStream());
		StreamCopyThread outr = new StreamCopyThread(stdout, "stdout", pr.getInputStream());
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the result with stdout and stderr merged.
	 * @param pb
	 * @param sb
	 * @return
	 * @throws Exception
	 */
	static public int runProcess(ProcessBuilder pb, Appendable outsb, Appendable errsb) throws Exception {
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(outsb, "stdout", pr.getInputStream());
		StreamReaderThread errr = new StreamReaderThread(errsb, "stderr", pr.getErrorStream());
		outr.start();
		errr.start();
		int rc = pr.waitFor();
		outr.join();
		errr.join();
		return rc;
	}

	/**
	 * Runs the process whose data is in the ProcessBuilder and captures the
	 * result with stdout and stderr merged into a writer.
	 * @param pb
	 * @param sb
	 * @return
	 * @throws Exception
	 */
	static public int runProcess(ProcessBuilder pb, Writer out) throws Exception {
		pb.redirectErrorStream(true); // Merge stdout and stderr
		Process pr = pb.start();
		StreamReaderThread outr = new StreamReaderThread(out, "stdout", pr.getInputStream());
		outr.start();
		int rc = pr.waitFor();
		outr.join();
		return rc;
	}
}
