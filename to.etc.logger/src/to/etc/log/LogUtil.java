package to.etc.log;

import java.io.*;

import javax.annotation.*;

/**
 * Internal file tool related utils. It has to be inside logger project to minimize external dependencies for logger project itself.
 * Code found here is pasted from original code existed in FileTool - please don't try to make it same source again ;)
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on May 23, 2014
 */
class LogUtil {
	@Nonnull
	static final String readResourceAsString(Class< ? > base, String name, String encoding) throws Exception {
		InputStream is = base.getResourceAsStream(name);
		if(null == is)
			throw new IllegalStateException(base + ":" + name + " resource not found");
		try {
			return readStreamAsString(is, encoding);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	static String readStreamAsString(final InputStream is, final String enc) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		readStreamAsString(sb, is, enc);
		return sb.toString();
	}

	static void readStreamAsString(final Appendable o, final InputStream f, final String enc) throws Exception {
		Reader r = new InputStreamReader(f, enc);
		readStreamAsString(o, r);
	}

	static void readStreamAsString(final Appendable o, final Reader r) throws Exception {
		char[] buf = new char[4096];
		for(;;) {
			int ct = r.read(buf);
			if(ct < 0)
				break;
			o.append(new String(buf, 0, ct));
		}
	}

	static void readFileAsString(final Appendable o, final File f) throws Exception {
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		try {
			String line;
			while(null != (line = lr.readLine())) {
				o.append(line);
				o.append("\n");
			}
		} finally {
			lr.close();
		}
	}

	/**
	 * Read a file into a string using the specified encoding.
	 *
	 * @param f
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	static String readFileAsString(final File f, final String encoding) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			return readStreamAsString(is, encoding);
		} finally {
			if(is != null)
				is.close();
		}
	}
}
