package to.etc.dbpool;

import java.io.*;
import java.util.*;

public class DbPoolUtil {
	private DbPoolUtil() {}

	static public final void dumpLocation(String msg) {
		try {
			throw new IllegalStateException("duh");
		} catch(IllegalStateException x) {
			System.out.println(msg);
			x.printStackTrace(System.out);
		}
	}

	static public final void getLocation(StringBuffer sb) {
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
			strStacktrace(sb, z);
		}
	}

	static public String strStacktrace(Throwable t) {
		StringWriter sw = new StringWriter(1024);
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.getBuffer().toString();
	}

	static public final String getLocation() {
		StringBuffer sb = new StringBuffer(512);
		getLocation(sb);
		return sb.toString();
	}

	static public void strStacktrace(Appendable sb, Throwable t) {
		try {
			sb.append(strStacktrace(t));
		} catch(IOException x) // Sillyness of sillynesses
		{
			x.printStackTrace();
		}
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

	static public void readAsString(StringBuffer sb, Reader r) throws Exception {
		char[] buf = new char[8192];
		int szread;
		while(0 < (szread = r.read(buf))) {
			sb.append(buf, 0, szread);
		}
	}

}
