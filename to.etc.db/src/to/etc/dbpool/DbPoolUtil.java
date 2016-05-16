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
package to.etc.dbpool;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

import javax.annotation.*;

public class DbPoolUtil {

	/**
	 * Host and port POJO.
	 */
	public final static class HostAndPort {
		private final int m_port;

		@Nonnull
		private final String m_host;

		private HostAndPort(@Nonnull String host, int port) {
			m_host = host;
			m_port = port;
		}

		/**
		 * Returns {@link HostAndPort} for specified host:port
		 *
		 * @param hostPort Must be host:port format
		 * @throws SQLException in case of wrong input format
		 */
		@Nonnull
		public static HostAndPort parse(@Nonnull String hostPort) throws SQLException {
			//-- Must be address:port format
			int pos = hostPort.indexOf(':');
			if(pos != -1) {
				String host = hostPort.substring(0, pos).trim();
				try {
					int port = Integer.parseInt(hostPort.substring(pos + 1).trim());
					return new HostAndPort(host, port);
				} catch(Exception x) {
					//Just keep instance undefined
				}
			}
			throw new SQLException("PL/SQL Debug handler: parameter format must be 'hostname:portnumber', it was '" + hostPort + "'. Hostname can be an ip address and is usually 127.0.0.1.");
		}

		public int getPort() {
			return m_port;
		}

		@Nonnull
		public String getHost() {
			return m_host;
		}
	}

	private DbPoolUtil() {}

	static private final String[] PRESET = {"xxxto.etc.dbpool.", "xxxxoracle.", "xxxnl.itris.viewpoint.db.hibernate."};

	static private final String[] ENDSET = {"xxxto.etc.domui.server.", "xxxorg.apache.tomcat"};

	static private boolean inSkipSet(final String[] set, final String name) {
		for(String s : set) {
			if(name.startsWith(s))
				return true;
		}
		return false;
	}

	/**
	 * Report a filtered location stack trace, where the start of the stack trace and the end can be removed.
	 * @param sb
	 * @param t
	 * @param skipbefore
	 * @param skipafter
	 */
	static public void strStacktraceFiltered(final Appendable sb, final Throwable t, String[] skipbefore, String[] skipafter, int linelimit, int indent) {
		StackTraceElement[] se = t.getStackTrace();
		strStacktraceFiltered(sb, se, skipbefore, skipafter, linelimit, indent);
	}

	static public void strStacktraceFiltered(final Appendable sb, final StackTraceElement[] se, String[] skipbefore, String[] skipafter, int linelimit, int indent) {
		//-- Find the first part to log,
		int len = se.length;
		int ix = 0;
		while(ix < len) {
			String m = se[ix].getClassName();
			if(!inSkipSet(skipbefore, m))
				break;
			ix++;
		}
		int sx = ix++; // First item not in head skipset; always logged.

		while(ix < len) {
			String m = se[ix].getClassName();
			if(inSkipSet(skipafter, m))
				break;
			ix++;
		}
		int ex = ix; // End bound, exclusive
		if(linelimit > 0) {
			if(ex - sx > linelimit)
				ex = sx + linelimit;
		}
		for(int i = sx; i < ex; i++) {
			try {
				for(int j = indent; --j >= 0;)
					sb.append(' ');
				sb.append(se[i].toString() + "\n");
			} catch(IOException x) {
				throw new RuntimeException(x); // Sigh
			}
		}
	}

	static public void strStacktraceFiltered(StringBuilder sb, final StackTraceElement[] se, int linelimit, int indent) {
		strStacktraceFiltered(sb, se, PRESET, ENDSET, linelimit, indent);
		//		strStacktraceFiltered(sb, se, new String[]{"XAXAXAXAXAXXAXX"}, ENDSET, linelimit, indent);
	}
	static public void strStacktraceFiltered(StringBuilder sb, final StackTraceElement[] se) {
		strStacktraceFiltered(sb, se, 50, 4);
	}

	/**
	 * Append a filtered stack trace.
	 * @param sb
	 * @param t
	 */
	@Deprecated
	static public void getFilteredStacktrace(StringBuilder sb, Throwable t) {
		strStacktraceFiltered(sb, t, PRESET, ENDSET, 60, 4);
	}

	static public final void dumpLocation(String msg) {
		try {
			throw new IllegalStateException("duh");
		} catch(IllegalStateException x) {
			StringBuilder sb = new StringBuilder();
			sb.append(msg);
			sb.append("\n");
			getFilteredStacktrace(sb, x);
			System.out.println(sb.toString());
		}
	}

	static public final void getThreadAndLocation(StringBuilder sb) {
		sb.append("At ");
		sb.append(new java.util.Date().toString());
		sb.append(" in thread ");
		sb.append(Thread.currentThread().getName());
		sb.append(" (");
		sb.append(Thread.currentThread().toString());
		sb.append("), stack:\n");

		try {
			throw new Exception("Trying to get source location");
		} catch(Exception z) {
			getFilteredStacktrace(sb, z);
		}
	}

	static public final String getLocation() {
		StringBuilder sb = new StringBuilder(1024);
		getThreadAndLocation(sb);
		return sb.toString();
	}

	/**
	 * Copies the inputstream to the output stream.
	 *
	 * @param destf     the destination
	 * @param srcf      the source
	 * @throws IOException  the error
	 */
	static public void copyFile(OutputStream os, InputStream is) throws IOException {
		byte[] buf = new byte[8192];
		int sz;
		while(0 < (sz = is.read(buf)))
			os.write(buf, 0, sz);
	}

	static public void readAsString(StringBuilder sb, Reader r) throws Exception {
		char[] buf = new char[8192];
		int szread;
		while(0 < (szread = r.read(buf))) {
			sb.append(buf, 0, szread);
		}
	}

	static private final long DAYS = 24 * 60 * 60;

	static private final long HOURS = 60 * 60;


	static public String strMillisOLD(long dlt) {
		StringBuffer sb = new StringBuffer();

		int millis = (int) (dlt % 1000); // Get milliseconds,
		dlt /= 1000; // Now in seconds,

		boolean sp = false;
		if(dlt >= DAYS) {
			sb.append(dlt / DAYS);
			sb.append("D");
			dlt %= DAYS;
			sp = true;
		}
		if(dlt >= HOURS) {
			long v = dlt / HOURS;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("u");
				sp = true;
			}
			dlt %= HOURS;
		}
		if(dlt >= 60) {
			long v = dlt / 60;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("m");
				sp = true;
			}
			dlt %= 60;
		}
		if(dlt != 0) {
			if(sp)
				sb.append(' ');
			sb.append(dlt);
			sb.append("s");
			sp = true;
		}
		if(millis != 0) {
			if(sp)
				sb.append(' ');
			sb.append(millis);
			sb.append("ms");
		}
		return sb.toString();
	}

	static private final long MICROS = 1000;

	static private final long MILLIS = 1000 * 1000;

	static private final long SECONDS = 1000 * 1000 * 1000;

	static private final long MINUTES = 60 * SECONDS;

	static private final long NSHOURS = 60 * MINUTES;

	static private final long[] TIMESET = {NSHOURS, MINUTES, SECONDS, MILLIS, MICROS, 1};

	static private final String[] SUFFIXES = {"H", "m", "s", "ms", "us", "ns"};

	static private final long[] MSTIMESET = {60 * 60 * 1000, 60 * 1000, 1000, 1};

	static private final String[] MSSUFFIXES = {"H", "m", "s", "ms"};

	/**
	 * Return a nanotime timestamp with 2 thousands of precision max.
	 * @param ns
	 * @return
	 */
	static public String strNanoTime(final long ns) {
		if(ns < 1000)
			return ns + " ns";

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < TIMESET.length; i++) {
			if(ns >= TIMESET[i]) {
				long u = ns / TIMESET[i];
				sb.append(Long.toString(u));
				sb.append(SUFFIXES[i]);
				sb.append(' ');
				u = ns % TIMESET[i];
				i++;
				u = u / TIMESET[i];
				sb.append(Long.toString(u));
				sb.append(SUFFIXES[i]);
				return sb.toString();
			}
		}
		return ns + "ns";
	}

	/**
	 * Return a nanotime timestamp with 2 thousands of precision max.
	 * @param ns
	 * @return
	 */
	static public String strNanoTime2(final long ns) {
		if(ns < 1000)
			return ns + " ns";

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < TIMESET.length; i++) {
			if(ns >= TIMESET[i]) {
				double u = (double) ns / TIMESET[i];
				sb.append(String.format(Locale.US, "%.1f", Double.valueOf(u)));
				sb.append(SUFFIXES[i]);
				return sb.toString();
			}
		}
		return ns + "ns";
	}


	static public String strMillis(final long ns) {
		if(ns < 1000)
			return ns + " ms";

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < MSTIMESET.length; i++) {
			if(ns >= MSTIMESET[i]) {
				long u = ns / MSTIMESET[i];
				sb.append(Long.toString(u));
				sb.append(MSSUFFIXES[i]);
				sb.append(' ');
				u = ns % MSTIMESET[i];
				i++;
				u = u / MSTIMESET[i];
				sb.append(Long.toString(u));
				sb.append(MSSUFFIXES[i]);
				return sb.toString();
			}
		}
		return ns + "ms";
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Load driver class code...							*/
	/*--------------------------------------------------------------*/
	/**
	 * Helper class to load JDBC drivers from a given path.
	 */
	private static final class NoLoader extends URLClassLoader {
		NoLoader(final URL[] u) {
			super(u);
		}

		@Override
		protected synchronized Class< ? > loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			// First, check if the class has already been loaded
			Class< ? > c = findLoadedClass(name);
			//            System.out.println(name+": findLoadedClass="+c);
			if(c == null) {
				//-- Try to load by THIS loader 1st,
				try {
					c = findClass(name);
					//                    System.out.println(name+": findClass="+c);
				} catch(ClassNotFoundException x) {
					//                    System.out.println(name+": findClass exception");
				}
				if(c == null) {
					c = super.loadClass(name, resolve); // Try parent
					//                    System.out.println(name+": super.loadClass="+c);
				}
			}

			if(resolve)
				resolveClass(c);
			return c;
		}
	}

	/**
	 * Loads the appropriate driver class.
	 * @return
	 * @throws Exception
	 */
	static public Driver loadDriver(File driverPath, String driverClassName) throws Exception {
		Class< ? > cl = null;
		if(driverPath == null) {
			//-- Default method: instantiate the driver using the normal mechanism.
			try {
				cl = Class.forName(driverClassName);
			} catch(Exception x) {
				throw new SQLException("The driver class '" + driverClassName + "' could not be loaded: " + x);
			}
		} else {
			//-- Load the driver off the classloader.
			URLClassLoader loader = new NoLoader(new URL[]{driverPath.toURI().toURL()}); // Sun people are idiots.
			try {
				cl = loader.loadClass(driverClassName);
			} catch(Exception x) {
				throw new SQLException("The driver class '" + driverClassName + "' could not be loaded from " + driverPath + ": " + x);
			}
		}

		//-- Step 2: create an instance.
		try {
			Driver d = (Driver) cl.newInstance();
			System.out.println("load: class=" + d + ", inst=" + d.getMajorVersion() + "." + d.getMinorVersion());
			return d;
		} catch(Exception x) {
			throw new SQLException("The driver class '" + driverClassName + "' could not be instantiated: " + x);
		}
	}

	static public DbType getDbTypeByDriverName(String dn) {
		dn = dn.toLowerCase();
		//		System.out.println("Reported driver name is "+dn);
		if(dn.indexOf("oracle") != -1)
			return DbType.ORACLE;
		else if(dn.indexOf("mysql") != -1)
			return DbType.MYSQL;
		else if(dn.indexOf("postgresql") != -1)
			return DbType.POSTGRES;
		else if(dn.indexOf("orac") != -1)
			return DbType.ORACLE;
		return DbType.UNKNOWN;
	}

	static public final String	CSS_ALLOC = "h-alloc";
	static public final String	CSS_TRACEPT = "h-usepoint";
	static public final String	CSS_STACK = "h-stack";


	/**
	 * Print all tracepoints as a timestamp-since followed by a filtered stack trace.
	 * @param sb
	 * @param pc
	 */
	public static void printTracepoints(IPrinter p, ConnectionProxy pc, boolean full) {
		List<Tracepoint>	list = pc.getTraceList();
		if(list == null || list.size() == 0) {
			p.warning("Connection usage tracing has not been enabled");
			return;
		}

		//-- 1st entry is ALWAYS the allocation point and will always be displayed
		long cts = System.currentTimeMillis();
		Tracepoint	tp = list.get(0);
		long ats = tp.getTimestamp();
		p.header(CSS_ALLOC, "Connection's allocation point");
		p.text("Connection ").text(""+pc.getId()).text(" allocated at ").text(strTime(pc.getAllocationTime())+" ("+strMilli(cts, ats)+" ago)") //
		.text(", used ").text(strMilli(cts, pc.getLastUsedTime())).text(" ago") //
		.nl();
		StringBuilder	sb = new StringBuilder();
		strStacktraceFiltered(sb, tp.getElements());
		p.pre(CSS_STACK, sb.toString());
		sb.setLength(0);

		//-- Loop through the rest.
		if(full) {
			for(int i = 1; i < list.size(); i++) {
				tp = list.get(i);

				sb.setLength(0);
				sb.append("Trace ").append(i).append(", ").append(strMilli(cts, tp.getTimestamp())).append(" after allocation");
				p.header(CSS_TRACEPT, sb.toString());

				sb.setLength(0);
				strStacktraceFiltered(sb, tp.getElements());
				p.pre(CSS_STACK, sb.toString());
			}
		}
	}




	/**
	 * Report the delta time between a and b, as milli's. Order is not important.
	 * @param a
	 * @param b
	 * @return
	 */
	static public String strMilli(long a, long b) {
		long dt = (a > b) ? a - b : b - a;
		return strMillis(dt);
	}

	static public String strTime(long ts) {
		Date dt = new Date(ts);
		DateFormat	df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return df.format(dt);
	}

	static public String strTime(Date dt) {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return df.format(dt);
	}

	static private ThreadLocal<DateFormat> m_timedf = new ThreadLocal<DateFormat>();

	static public String strTimeOnly(Date dt) {
		DateFormat df = m_timedf.get();
		if(null == df) {
			df = new SimpleDateFormat("HH:mm:ss.S");
			m_timedf.set(df);
		}
		return df.format(dt);
	}

	/**
	 * Returns a properly formatted commad string for a number [english only].
	 * @param val
	 * @return
	 */
	static public String strCommad(final long val) {
		String v = Long.toString(val);
		StringBuffer sb = new StringBuffer(30);
		int pos = (v.length() % 3) + 1;
		if(pos == 0)
			pos = 3;
		for(int i = 0; i < v.length(); i++) {
			pos--;
			if(pos == 0) {
				if(i > 0)
					sb.append(',');
				pos = 3;
			}
			sb.append(v.charAt(i));
		}
		return sb.toString();
	}

	static public String strCountDur(final long count, final long nanotime) {
		if(count == 0)
			return "";
		return strCommad(count) + "/" + strNanoTime(nanotime);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	pool.jsp helper code.								*/
	/*--------------------------------------------------------------*/

	static public String q(String str) {
		if(str == null)
			return "";
		StringBuilder sb = new StringBuilder(str.length() + 30);
		int len = str.length();
		for(int i = 0; i < len; i++) {
			char c = str.charAt(i);
			switch(c){
				default:
					sb.append(c);
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
			}
		}
		return sb.toString();
	}

	static public String getStack(Tracepoint tp) {
		StringBuilder sb = new StringBuilder(8192);
		strStacktraceFiltered(sb, tp.getElements(), 50, 0);
		return sb.toString();
	}

	/**
	 *	Returns a string representing some size, in bytes. Depending on the size
	 *  it will be represented as KB, MB, GB or TB.
	 */
	public static String strSize(final long sz) {
		final long kb = 1024;
		final long mb = kb * 1024;
		final long gb = mb * 1024;
		final long tb = gb * 1024;

		long div = 1;
		String sf = "";
		if(sz >= tb) {
			div = tb;
			sf = "TB";
		} else if(sz >= gb) {
			div = gb;
			sf = "GB";
		} else if(sz >= mb) {
			div = mb;
			sf = "MB";
		} else if(sz >= kb) {
			div = kb;
			sf = "KB";
		}

		//-- Now do something,
		StringBuffer sb = new StringBuffer(15);

		if(div == 1) {
			return sz + " bytes";
		}

		long v = (sz / div);
		long r = (sz % div) / (div / 10);
		sb.append(Long.toString(v));
		if(r != 0) {
			sb.append(".");
			sb.append(Long.toString(r));
		}
		sb.append(" ");
		sb.append(sf);
		return sb.toString();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	.developer.properties interface.					*/
	/*--------------------------------------------------------------*/

	@Nullable
	static private Properties getDeveloperProperties() {
		String s = System.getProperty("user.home");
		if(s == null)
			return null;
		File f = new File(new File(s), ".developer.properties");
		if(!f.exists())
			return null;
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			Properties p = new Properties();
			p.load(is);
			return p;
		} catch(Exception x) {
			System.out.println("PoolManager: exception while reading " + f + ": " + x);
			return null;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Return the connect string if PL/SQL debugging is enabled for the specified pool.
	 * @param poolName
	 * @return
	 */
	@Nullable
	static public String getPlSqlDebug(String poolName) {
		Properties p = getDeveloperProperties();
		if(null == p)
			return null;

		//-- Generic enabled?
		String val = p.getProperty("pool.plsql.debug");
		if(val != null)
			return val;
		val = p.getProperty("pool." + poolName.toLowerCase() + ".plsql.debug");
		return val;
	}

	/**
	 * Executes dbms_debug_jdwp.connect_tcp(host, port) on specified connection. That enables remote debug.
	 *
	 * @param con
	 * @param hostAndPort
	 * @throws SQLException
	 */
	public static void enableRemoteDebug(@Nonnull Connection con, @Nonnull HostAndPort hostAndPort) throws SQLException {
		final String cmd = "begin dbms_debug_jdwp.connect_tcp('" + hostAndPort.getHost() + "'," + hostAndPort.getPort() + ");end;";
		PreparedStatement st = con.prepareStatement(cmd);
		try {
			st.execute();
		} catch(Exception x) {
			//-- Ignore any error.
		} finally {
			try {
				st.close();
			} catch(Exception x) {}
		}
	}
}
