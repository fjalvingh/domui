package to.etc.dbpool;

import java.io.*;
import java.util.*;

public class DbPoolUtil {
	private DbPoolUtil() {}

	static private final String[] PRESET = {"to.etc.dbpool.", "oracle.", "nl.itris.viewpoint.db.hibernate."};

	static private final String[] ENDSET = {"to.etc.dbpool.", "to.etc.domui.server.", "org.apache.tomcat"};

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
	static public void strStacktraceFiltered(final Appendable sb, final Throwable t, String[] skipbefore, String[] skipafter, int linelimit) {
		StackTraceElement[] se = t.getStackTrace();

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
				sb.append("    " + se[i].toString() + "\n");
			} catch(IOException x) {
				throw new RuntimeException(x); // Sigh
			}
		}
	}

	/**
	 * Append a filtered stack trace.
	 * @param sb
	 * @param t
	 */
	static public void getFilteredStacktrace(StringBuilder sb, Throwable t) {
		strStacktraceFiltered(sb, t, PRESET, ENDSET, 10);
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
		sb.append(new Date().toString());
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

	//	static public String strStacktrace(Throwable t) {
	//		StringWriter sw = new StringWriter(1024);
	//		PrintWriter pw = new PrintWriter(sw);
	//		t.printStackTrace(pw);
	//		pw.close();
	//		return sw.getBuffer().toString();
	//	}

	//	static public void strStacktrace(Appendable sb, Throwable t) {
	//		try {
	//			sb.append(strStacktrace(t));
	//		} catch(IOException x) // Sillyness of sillynesses
	//		{
	//			x.printStackTrace();
	//		}
	//	}

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


	static public String strMillis(long dlt) {
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

}
