package to.etc.server.syslogger;

import java.io.*;
import java.util.*;

import to.etc.util.*;

public class LogMessage {
	/// The last timestamp used
	static private long	m_last_ts;

	/// The last sequence #
	static private int	m_last_seq;


	/// The timestamp the message arrived,
	protected long		m_utc;

	/// The sequence number within the same time period,
	private int			m_seq;

	/// The thread this message arrived from
	private String		m_thrd;

	/// The stack trace, if applicable
	private String		m_stacktrace;

	/// The request parameters, if applicable
	private String		m_client;

	/// The associated message,
	private String		m_msg;

	/// The exception message.
	private String		m_x_msg;

	/// The exception name
	private String		m_x_name;

	/// The index.
	private int			m_index;

	/** The code location where the message was logged from. */
	private String		m_location;


	public LogMessage() {
		setTimeStamp(this);
		Thread t = Thread.currentThread();
		setThrd(t.getName() + " [" + t.getThreadGroup() + "]");
	}

	public LogMessage(String what) {
		this();
		setMsg(what);
	}

	public void setException(Throwable x) {
		try {
			StringWriter sw = new StringWriter(1024);
			PrintWriter pw = new PrintWriter(sw);
			x.printStackTrace(pw);
			pw.close();
			setStacktrace(sw.toString());
			setExceptionMessage(x.getMessage());
			setExceptionName(x.getClass().getName());
		} catch(Exception y) {}
	}


	/**
	 * Called to save the location of the call. This throws an exception to
	 * get a stacktrace, then it saves the trace data to the location member.
	 */
	public void setLocation() {
		try {
			throw new Exception("Where in the code am I?");
		} catch(Exception x) {
			//-- Write the exception's stack...
			StringWriter sw = new StringWriter(512);
			PrintWriter pw = new PrintWriter(sw);
			x.printStackTrace(pw);
			pw.close();
			setLocation(sw.getBuffer().toString());
		}
	}

	static synchronized private void setTimeStamp(LogMessage m) {
		m.m_utc = (new Date()).getTime();
		if(m.m_utc == m_last_ts) {
			m.setSeq(m_last_seq++);
		} else {
			m_last_ts = m.m_utc;
			m_last_seq = 0;
		}
	}

	protected void write(DataOutputStream dos) throws IOException {
		dos.writeByte(1); // Version
		dos.writeLong(m_utc);
		dos.writeInt(getSeq());
		ws(dos, getThrd());
		ws(dos, getStacktrace());
		ws(dos, m_client);
		ws(dos, m_msg);
		ws(dos, m_x_msg);
		ws(dos, m_x_name);
		ws(dos, m_location);
	}

	private static String rs(DataInputStream dis) throws IOException {
		long ss = dis.readLong(); // Read string size,
		if(ss < 0)
			return null;
		if(ss == 0l || ss > Integer.MAX_VALUE)
			return "";

		//-- Create char for string
		int len = (int) ss;
		char[] buf = new char[len];
		for(int i = 0; i < len; i++)
			buf[i] = dis.readChar();
		return new String(buf);
	}

	private static void ws(DataOutputStream dos, String s) throws IOException {
		if(s == null)
			dos.writeLong(-1);
		else {
			int sl = s.length();
			dos.writeLong(sl);
			for(int i = 0; i < sl; i++)
				dos.writeChar(s.charAt(i));
		}
	}


	public static LogMessage read(DataInputStream dis) throws IOException {
		LogMessage lm = new LogMessage();
		try {
			byte v = dis.readByte();
			if(v != 0 && v != 1)
				throw new IOException("LOGMESSAGE invalid (bad version)");
			lm.m_utc = dis.readLong();
			lm.setSeq(dis.readInt());
			lm.setThrd(rs(dis));
			lm.setStacktrace(rs(dis));
			lm.setClient(rs(dis));
			lm.setMsg(rs(dis));
			lm.setExceptionMessage(rs(dis));
			lm.setExceptionName(rs(dis));
			if(v == 1)
				lm.setLocation(rs(dis));
		} catch(IOException x) {
			lm.setMsg("IO Exception while reading..");
		}
		return lm;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Info retrieval stuff...								*/
	/*--------------------------------------------------------------*/
	public String getTime() {
		//		Date	d	= new Date(m_utc);
		//		Date	cd	= new Date();

		//-- Get stuff off the date. What asshole made this interface ought to get shot!
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(m_utc));
		Calendar ct = Calendar.getInstance();
		ct.setTime(new Date());

		//-- Is the date part same?
		int day = c.get(Calendar.DAY_OF_MONTH);
		int mon = c.get(Calendar.MONTH);
		int yr = c.get(Calendar.YEAR);

		if(Calendar.JANUARY == 0)
			mon++; // Stupid assholes!


		StringBuffer sb = new StringBuffer(20);
		if(yr == ct.get(Calendar.YEAR)) {
			int ydn = c.get(Calendar.DAY_OF_YEAR);
			int cdn = ct.get(Calendar.DAY_OF_YEAR);
			if(ydn == cdn) // today?
				sb.append("Today     ");
			else if(ydn == cdn - 1) // Yesterday
				sb.append("Yesterday ");
		}
		//-- 2000/02/18
		//-- yesterday
		//-- today
		if(sb.length() == 0) {
			sb.append(Integer.toString(yr));
			sb.append("/");
			sb.append(StringTool.intToStr(mon, 10, 2));
			sb.append("/");
			sb.append(StringTool.intToStr(day, 10, 2));
			sb.append(" ");
		}

		sb.append(StringTool.intToStr(c.get(Calendar.HOUR_OF_DAY), 10, 2));
		sb.append(":");
		sb.append(StringTool.intToStr(c.get(Calendar.MINUTE), 10, 2));
		sb.append(":");
		sb.append(StringTool.intToStr(c.get(Calendar.SECOND), 10, 2));
		sb.append(".");
		sb.append(Integer.toString(getSeq()));
		return sb.toString();
	}


	public String getExceptionName() {
		return m_x_name == null ? "" : m_x_name;
	}

	public String getType() {
		if(m_x_name != null) // Was an exception?
			return m_x_name; // Return it's name,
		else
			return "message"; // Else it's just a message
	}

	private String	m_msgstr;

	private String getMsgStr() {
		if(m_msgstr != null)
			return m_msgstr;

		if(getExceptionMessage() != null) {
			if(m_msg == null)
				m_msgstr = getExceptionMessage();
			else
				m_msgstr = m_msg + ": " + getExceptionMessage();
		} else
			m_msgstr = m_msg;
		return m_msgstr;
	}


	public String getMsg() {
		String s = getMsgStr();
		if(s == null)
			return null;
		return StringTool.htmlStringize(s);
	}

	public String getMsg(int sz) {
		String s = getMsgStr();
		if(s == null)
			return null;
		if(s.length() > sz)
			s = s.substring(0, sz) + "...";
		return StringTool.htmlStringize(s);
	}

	public String getStackTrace() {
		if(getStacktrace() != null)
			return getStacktrace();

		if(getLocation() != null)
			return getLocation();
		return "";
	}

	public String getClient() {
		return m_client == null ? "" : m_client;
	}

	public void setSeq(int seq) {
		m_seq = seq;
	}

	public int getSeq() {
		return m_seq;
	}

	public void setThrd(String thrd) {
		m_thrd = thrd;
	}

	public String getThrd() {
		return m_thrd;
	}

	public void setStacktrace(String stacktrace) {
		m_stacktrace = stacktrace;
	}

	public String getStacktrace() {
		return m_stacktrace;
	}

	public void setClient(String client) {
		m_client = client;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	public void setExceptionMessage(String x_msg) {
		m_x_msg = x_msg;
	}

	public String getExceptionMessage() {
		return m_x_msg;
	}

	public void setExceptionName(String x_name) {
		m_x_name = x_name;
	}

	public void setIndex(int index) {
		m_index = index;
	}

	public int getIndex() {
		return m_index;
	}

	public void setLocation(String location) {
		m_location = location;
	}

	public String getLocation() {
		return m_location;
	}


}
