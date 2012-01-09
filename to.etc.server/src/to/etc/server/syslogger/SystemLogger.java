package to.etc.server.syslogger;

import java.io.*;
import java.util.*;

/**
 *
 * Created on Oct 31, 2003
 * @author jal
 */
public class SystemLogger {
	/** The pending message queue for all logs. */
	static private LinkedList	m_log_ll	= new LinkedList();

	static private ArrayList	m_conv_al	= new ArrayList();

	/** The base file name for this log. */
	private String				m_log_basename;

	/** Counts the #of log messages generated. */
	private long				m_log_count;

	/** T if someone is already dumping... */
	private boolean				m_log_dumping;

	/** The broker log's DATA output file. */
	private RandomAccessFile	m_log_df;

	/** The broker log's ENTRY output file. */
	private RandomAccessFile	m_log_ef;

	File						m_log_entry_f;

	File						m_log_data_f;

	/** Zero if no problem, timestamp if log had an error: time of last open. */
	private long				m_log_fail;

	static private final long	LOGRETRY	= 1000 * (10 * 60);

	public SystemLogger(File dir, String basename) {
		//-- Make the log's paths.
		m_log_basename = basename;
		m_log_data_f = new File(dir, basename + ".df");
		m_log_entry_f = new File(dir, basename + ".ef");
	}

	public String getLogName() {
		return m_log_basename;
	}

	static public synchronized void addConverter(LogDataConverter lc) {
		if(!m_conv_al.contains(lc))
			m_conv_al.add(lc);
	}

	/// Info function.
	public String getLogState() {
		StringBuffer sb = new StringBuffer(128);
		sb.append("Log ");
		if(m_log_fail != 0)
			sb.append("FAILED");
		else {
			if(m_log_ef == null)
				sb.append("unused, not open.");
			else {
				sb.append("okay. ");
				try {
					sb.append("current size is " + (m_log_ef.length() / 1024) + "K");
				} catch(Exception x) {}
			}
		}
		return sb.toString();
	}

	private void blClose() {
		try {
			if(m_log_df != null)
				m_log_df.close();
		} catch(Exception x) {}
		try {
			if(m_log_ef != null)
				m_log_ef.close();
		} catch(Exception x) {}
		m_log_df = null;
		m_log_ef = null;
	}

	private void blFail(Exception x) {
		blClose(); // Force the log closed
		m_log_fail = System.currentTimeMillis() + LOGRETRY;
		System.out.println("SystemLogger: failure writing broker's logfile");
		x.printStackTrace();
	}

	/**
	 *	Opens the broker log if it is not already open. Returns FALSE if the
	 *  broker log had an error.
	 */
	public boolean open() {
		if(m_log_df != null) // Already open?
			return true; // Then we're OK,
		long ct = System.currentTimeMillis();

		if(m_log_fail != 0) // Log has failed?
		{
			if(ct < m_log_fail)
				return false; // Retry time not yet met?
		}


		//-- We have to open the files.
		try {
			//-- Open the files;
			m_log_df = new RandomAccessFile(m_log_data_f, "rw");
			m_log_ef = new RandomAccessFile(m_log_entry_f, "rw");

			//-- And initialize to the right position...
			long el = m_log_ef.length();
			long ep = (el / 8) * 8; // Position on a 8-byte boundary,
			m_log_ef.seek(ep); // And move there,
			m_log_df.seek(m_log_df.length()); // And more..

			//-- When here all's well!!
			m_log_fail = 0; // Not failed
			return true;
		} catch(Exception x) {
			System.out.println("SystemLogger: cannot (re)open broker logfile " + m_log_basename);
			System.out.println("log: entry file=" + m_log_entry_f);
			System.out.println("log: data  file=" + m_log_data_f);
			x.printStackTrace();
			blClose();
			m_log_fail = ct;
		}
		return false;
	}


	/**
	 *	Writes the message to the next slot in the file.
	 */
	private void blWrite(LogMessage m) {
		//		System.out.print("..enter blWrite:");
		if(!open())
			return; // Nothing open-> exit,

		try {
			//			System.out.print(".. blWrite: start write "+m.getMsg()+": ");

			//-- 1. Write the log object to a stream,
			ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
			DataOutputStream dos = new DataOutputStream(bos);
			m.write(dos);
			dos.close();
			bos.close();

			//-- 2. Get the size of the outputstream & write it to prepare;
			int osl = bos.size(); // Get #bytes stored;
			byte[] osa = bos.toByteArray(); // Get the bytes to write,
			int dix = (int) m_log_df.getFilePointer();
			m_log_df.write(osa); // Write the block;

			//-- Now write the entry.
			m_log_ef.writeInt(dix); // Offset
			m_log_ef.writeInt(osl); // Length
			//			System.out.println(" completed.");
		} catch(Exception x) {
			blFail(x);
		}
	}

	public long logGetCount() {
		synchronized(m_log_ll) {
			return m_log_count;
		}
	}

	/**
	 *	Logs a message to the broker's log file. This file can be inspected
	 *  on-the-fly.
	 */
	public void brokerLog(LogMessage m) {
		synchronized(m_log_ll) {
			m_log_count++; // Increment total #of messages
			if(m_log_dumping) // Someone is dumping?
			{
				m_log_ll.add(m); // Then add to the end of the list
				return; // And be done,
			}

			//-- WE have to dump. Set dumping and enter the loop,
			m_log_dumping = true; // We are dumping now...
		}

		//-- When here we ARE assumed to be dumping and the first message is in m.
		try {
			//-- Add this to the list as the LAST entry,
			while(m != null) {
				blWrite(m); // Write the message to the log.

				//-- Now move to the next message,
				synchronized(m_log_ll) {
					if(m_log_ll.isEmpty())
						break;
					m = (LogMessage) m_log_ll.removeFirst();
				}
			}

			//-- End of loop- flush the log file.
			//			blFlush();							// Flush the file.
		} catch(Exception x) // If dumping dies- ignore and exit,
		{
			x.printStackTrace(); // Do stack trace though
			m_log_ll.clear();
		} finally {
			synchronized(m_log_ll) {
				m_log_dumping = false;
			}
		}
	}

	public LogReader getReader() throws Exception {
		LogReader lr = new LogReader();
		//		lr.open();
		return lr;
	}


	/**
	 * A class that can be used to read the log in some way.
	 *
	 * Created on Oct 31, 2003
	 * @author jal
	 */
	public class LogReader {
		/**
		 *	Called by NEMA when this helper class is released.
		 */
		public void close() {
			try {
				if(m_rlog_df != null)
					m_rlog_df.close();
			} catch(Exception x) {}
			try {
				if(m_rlog_ef != null)
					m_rlog_ef.close();
			} catch(Exception x) {}
			m_rlog_df = null;
			m_rlog_ef = null;
		}


		/*--------------------------------------------------------------*/
		/*	CODING:	Broker log file stuff...							*/
		/*--------------------------------------------------------------*/
		/** The broker log's DATA output file. */
		private RandomAccessFile	m_rlog_df;

		/** The broker log's ENTRY output file. */
		private RandomAccessFile	m_rlog_ef;

		/**
		 *	Returns a string detailing log file info.
		 */
		public String logState() throws Exception {
			return getLogState();
		}

		/**
		 *	Opens the logfile.
		 */
		private void open() throws Exception {
			if(m_rlog_df != null)
				return;

			try {
				//-- Open the files;
				m_rlog_df = new RandomAccessFile(m_log_data_f, "rw");
				m_rlog_ef = new RandomAccessFile(m_log_entry_f, "rw");
			} catch(Exception x) {
				close();
				throw x;
			}
		}


		/**
		 *	Returns the last logfile entry (actually the first new one).
		 */
		public int logLastEntry() throws Exception {
			open();
			long fp = ((m_rlog_ef.length() / 8));
			if(fp < 0)
				return 0;
			return (int) fp;
		}


		public LogItemResultPage logLastPage(int npp) throws Exception {
			int le = logLastEntry(); // Get last entry,
			int se = le - npp;
			if(se < 0)
				se = 0;

			return logPage(npp, se, le);
		}

		//		private int		rdint() throws IOException
		//		{
		//			byte[]	ba	= new byte[4];
		//			m_rlog_ef.readFully(ba);					// Read all 4 bytes,
		//
		//			return (ba[0] << 24) | (ba[1] << 16) | (ba[2] << 8) | ba[3];
		//		}

		private LogMessage rdLogMsg(int le) throws Exception {
			//-- Read the entry file's length and pos;
			LogMessage lm;

			try {
				m_rlog_ef.seek(le * 8);
				int off = m_rlog_ef.readInt();
				int len = m_rlog_ef.readInt();
				if(len > 500 * 1024 || len <= 0) {
					lm = new LogMessage("(invalid log message entry [size])");
					lm.setIndex(le);
					return lm;
				}

				//-- Move to the correct position and read a byte thing.
				byte[] data = new byte[len];
				m_rlog_df.seek(off);
				m_rlog_df.readFully(data);

				//-- Make an input stream and read the logmessage.
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bis);
				lm = LogMessage.read(dis);
				lm.setIndex(le);
			} catch(EOFException x) {
				return null;
			}
			return lm;
		}


		public LogItemResultPage logPage(int npp, int se, int ee) throws Exception {
			open();

			//-- Create a message array for those messages,
			int max = ee - se;
			LogMessage[] ar = new LogMessage[max];

			//-- Now start reading..
			int nr = 0;
			for(int i = 0; i < max; i++) {
				//-- Goto the entry and read it,
				ar[i] = rdLogMsg(se + i);
				if(ar[i] == null)
					break;
				nr++;
			}

			//-- Now create and return a log page.
			return new LogItemResultPage(logLastEntry(), npp, se, se + nr, ar);
		}


		public LogItemResultPage logPageByNr(int npp, int pnr) throws Exception {
			return logPage(npp, npp * pnr, npp * (pnr + 1));
		}

		public LogMessage logEntry(int ix) throws Exception {
			open();
			LogMessage lm = rdLogMsg(ix);
			if(lm == null)
				return new LogMessage("?? Past end of log message file");
			else
				return lm;
		}

		public String getLogName() {
			return SystemLogger.this.getLogName();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Main entrypoints.									*/
	/*--------------------------------------------------------------*/
	/**
	 *	This logs an unexpected exception to the broker log. This can be used
	 *  to centrally track exceptions.
	 */
	public void logUnexpected(Throwable x, String what) {
		logUnexpected(x, what, null);
	}

	/**
	 *	This logs an unexpected exception to the broker log. This can be used
	 *  to centrally track exceptions.
	 */
	public void logUnexpected(Throwable x, String what, Object oo) {
		LogMessage m = new LogMessage(what);
		if(x != null)
			m.setException(x);
		else
			m.setLocation();
		if(oo != null) {
			StringBuffer sb = new StringBuffer();
			handleObject(sb, oo);
			m.setClient(sb.toString());
		}
		brokerLog(m);
	}

	private void handleObject(StringBuffer sb, Object o) {
		for(int i = m_conv_al.size(); --i >= 0;) {
			LogDataConverter ldc = (LogDataConverter) m_conv_al.get(i);
			if(ldc.accepts(o))
				ldc.convert(sb, o);
		}
	}


	/**
	 *	This logs an unexpected exception to the broker log. This can be used
	 *  to centrally track exceptions.
	 */
	public void logUnexpected(Object oo, String what) {
		logUnexpected(null, what, oo);
	}


	/**
	 *	This logs an unexpected condition to the broker log. This can be used
	 *  to keep track of problems.
	 */
	public void logUnexpected(String what) {
		logUnexpected(null, what, null);
	}

	public void panic(String subj, String body) {
		logUnexpected(subj + "\n" + body);
		Panicker.panic(subj, body);
	}
}
