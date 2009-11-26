package to.etc.util;

import java.io.*;

public class ProcessTools {
	private ProcessTools() {
	}

	/**
	 *	This is used to async read strout and stderr streams from a process...
	 */
	static public class StreamReaderThread extends Thread {
		/** The stream to read, */
		private Reader			m_reader;

		/** The output writer thing. */
		private final Writer	m_w;

		private final char[]	m_buf;

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
			}, name, is, encoding);
		}

		public StreamReaderThread(Writer sb, String name, InputStream is) {
			this(sb, name, is, System.getProperty("file.encoding"));
		}

		public StreamReaderThread(Writer w, String name, InputStream is, String encoding) {
			m_w = w;
			m_buf = new char[1024];
			setName("StreamReader" + name);
			try {
				m_reader = new InputStreamReader(is, encoding);
			} catch(UnsupportedEncodingException x) // Fuck James Gosling with his stupid checked exceptions crap
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
				while(0 < (szrd = m_reader.read(m_buf))) {
					//					System.out.println("dbg: writing "+szrd+" chars to the stream");
					m_w.write(m_buf, 0, szrd);
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
	 * Runs the process whose data is in the ProcessBuilder and captures the result with stdout and stderr merged.
	 * @param pb
	 * @param sb
	 * @return
	 * @throws Exception
	 */
	static public int runProcess(ProcessBuilder pb, Appendable outsb, Appendable errsb) throws Exception {
		pb.redirectErrorStream(true); // Merge stdout and stderr
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
