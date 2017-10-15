/*
 * DomUI Java User Interface library
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
package to.etc.webapp.core;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.util.*;

/**
 *
 *
 * @author jal
 * Created on Jan 31, 2005
 */
final public class ServerTools {
	static private final String BASECODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_";

	private ServerTools() {}

	/**
	 * Tries to find the named config file. It tries the local directory first,
	 * followed by the WEB-INF path and all classpath entries.
	 *
	 * @param ctx
	 * @param basename
	 * @return
	 * @throws Exception
	 */
	static public File findConfigFileByName(ServletContext ctx, String basename) throws Exception {
		File f = new File(basename); // Try local directory 1st
		if(f.exists()) // If found we're done
			return f;

		int fix = FileTool.findFilenameExtension(basename);
		if(fix == -1) {
			basename += ".properties";
			f = new File(basename); // Try local directory 1st
			if(f.exists()) // If found we're done
				return f;
		}

		//-- Try relative to the webapps' path
		if(ctx != null) {
			String p = ctx.getRealPath("/WEB-INF/" + basename);
			f = new File(p);
			if(f.exists())
				return f;
		}

		//-- Not found still. Use the path.
		f = StringTool.findFileOnEnv(basename, "java.class.path");
		return f;
	}

	static private String m_myhostname;

	static public synchronized String getMyHostname() {
		if(m_myhostname != null)
			return m_myhostname;

		try {
			InetAddress ad = InetAddress.getLocalHost();
			m_myhostname = ad.getHostName();
		} catch(Exception x) {
			m_myhostname = "unknown.hostname";
		}
		return m_myhostname;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	MIME types and MIME type routines...				*/
	/*--------------------------------------------------------------*/
	/// The list of file extensions vs MIME map types...
	static private Properties m_mimemap_p;

	/** The list of MIME type strings (for interning them) */
	static private HashMap<String, String> m_mimestr_ht = new HashMap<String, String>(511);

	/**
	 * This gets called with a MIME type string. It returns the same string BUT
	 * it merges all of the same strings into a single object. That is every
	 * string 'image/jpeg' passed thru this function will return a SINGLE
	 * string object of that contents. This reserves lots of storage space when
	 * many objects refer to a mime type.
	 * @param mime
	 * @return
	 */
	static synchronized public String getMimeString(String mime) {
		initMimeMap();

		mime = mime.toLowerCase();
		String r = m_mimestr_ht.get(mime);
		if(r != null)
			return r;
		m_mimestr_ht.put(mime, mime);
		return mime;
	}


	/**
	 *	Returns the mime type of a file by searching the file extension in the
	 *	MIME table. If this table has not been loaded it gets loaded.
	 */
	static public String getExtMimeType(String ext) {
		initMimeMap();
		return m_mimemap_p.getProperty(ext.toLowerCase());
	}


	/**
	 *	Returns the mime type of a file by searching the file extension in the
	 *	MIME table. If this table has not been loaded it gets loaded.
	 */
	static public String getMimeType(String fn) {
		initMimeMap();

		//-- Get the last part of the name
		int pos = fn.lastIndexOf('/');
		if(pos == -1) {
			pos = fn.lastIndexOf('\\');
		}
		if(pos != -1) {
			fn = fn.substring(pos + 1);
		}

		//-- Walk all possible indices
		int spos = 0;
		for(;;) {
			pos = fn.indexOf('.', spos);
			if(pos == -1)
				return null;
			String ext = fn.substring(pos + 1).toLowerCase();
			String mime = m_mimemap_p.getProperty(ext);
			if(null != mime)
				return mime;
			spos = pos + 1;
		}
	}

	static public String getMimeExtension(String mimetype) {
		for(Iterator<Map.Entry<Object, Object>> i = m_mimemap_p.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Object, Object> me = i.next();
			if(mimetype.equalsIgnoreCase((String) me.getValue()))
				return (String) me.getKey();
		}
		return null;
	}

	static public synchronized void addMime(String ext, String mime) {
		initMimeMap();
		m_mimemap_p.setProperty(ext, mime);
	}

	/**
	 *	Load all MIME types. First load the default ones,
	 */
	static public synchronized void initMimeMap() {
		if(m_mimemap_p != null)
			return;

		final String[] premimes = {"aif", "audio/x-aiff", "aiff", "audio/x-aiff", "aifc", "audio/x-aiff", "ai", "application/postscript", "au", "audio/basic", "asc", "text/plain", "avi",
			"video/x-msvideo",

			"bin", "application/octet-stream", "bcpio", "application/x-bcpio", "bmp", "image/bmp",

			"cab", "application/x-cabinet", "class", "application/octet-stream", "cpt", "application/mac-compactpro", "css", "text/css", "cpio", "application/x-cpio", "csh", "application/x-csh",
			"cdf", "application/x-netcdf",

			"dms", "application/octet-stream", "doc", "application/msword", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "dcr", "application/x-director", "dir",
			"application/x-director", "dxr",
			"application/x-director", "dvi", "application/x-dvi", "dwt", "application/x-dreamweaver-tpl", "dwf", "application/x-dwf",

			"exe", "application/octet-stream", "eps", "application/postscript", "etx", "text/x-setext",

			"fla", "application/x-shockwave-flash",


			"gtar", "application/x-gtar", "gif", "image/gif", "gz", "application/octet-stream",

			"hqx", "application/mac-binhex40", "html", "text/html", "htm", "text/html", "hdf", "application/x-hdf",

			"ief", "image/ief", "ice", "x-conference/x-cooltalk", "ico", "image/x-icon",

			"js", "application/x-javascript", "jpeg", "image/jpeg", "jpg", "image/jpeg", "jpe", "image/jpeg",

			"kar", "audio/midi",

			"latex", "application/x-latex", "lha", "application/octet-stream", "lhz", "application/octet-stream",

			"mid", "audio/midi", "mpeg", "video/mpeg", "mpg", "video/mpeg", "mpe", "video/mpeg", "mov", "video/quicktime", "movie", "video/x-sgi-movie", "mpga", "audio/mpeg", "mp2", "audio/mpeg",
			"mp3", "audio/mpeg", "man", "application/x-troff-man", "me", "application/x-troff-me", "ms", "application/x-troff-ms",

			"nc", "application/x-netcdf",

			"oda", "application/oda",

			"pdf", "application/pdf", "ps", "application/postscript", "ppt", "application/postscript", "pptx", "application/vnd.ms-powerpoint", "png", "image/png", "pgn", "application/x-chess-pgn",
			"pnm", "image/x-portable-anymap",
			"pbm", "image/x-portable-bitmap", "pgm", "image/x-portable-graymap", "ppm", "image/x-portable-pixmap", "properties", "application/x-properties",

			"qt", "video/quicktime",

			"rtf", "application/rtf", "ram", "audio/x-pn-realaudio", "rm", "audio/x-pn-realaudio", "rpm", "audio/x-pn-realaudio-plugin", "ra", "audio/x-realaudio", "ras", "image/x-cmu-raster", "rgb",
			"image/x-rgb", "rtx", "text/richtext", "rtf", "text/rtf",

			"smi", "application/smil", "smil", "application/smil", "skp", "application/x-koan", "skd", "application/x-koan", "skt", "application/x-koan", "skm", "application/x-koan", "src",
			"application/x-wais-source", "sh", "application/x-sh", "shar", "application/x-shar", "swf", "application/x-shockwave-flash", "sit", "application/x-stuffit", "spl",
			"application/x-futuresplash", "sv4cpio", "application/x-sv4cpio", "sv4crc", "application/x-sv4crc", "snd", "audio/basic", "sgml", "text/sgml", "sgm", "text/sgml",

			"tgz", "application/octet-stream", "tar", "application/x-tar", "tcl", "application/x-tcl", "tex", "application/x-tex", "texinfo", "application/x-texinfo", "texi", "application/x-texinfo",
			"t", "application/x-troff", "tld", "application/x-tld", "tr", "application/x-troff", "roff", "application/x-troff", "tiff", "image/tiff", "tif", "image/tiff", "txt", "text/plain", "tsv",
			"text/tab-separated-values",

			"ustar", "application/x-ustar",

			"vcd", "application/x-cdlink", "vrml", "model/vrml",

			"wav", "audio/x-wav", "wrl", "model/vrml",

			"xls", "application/vnd.ms-excel", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xbm", "image/x-xbitmap", "xpm", "image/x-xpixmax", "xwd",
			"image/x-xwindowdump", "xml", "text/xml",

			"zip", "application/zip", "z", "application/octet-stream",
			"js.map", "application/json"
			, "ts", "text/x-typescript"
		};

		m_mimemap_p = new Properties();

		//-- Now all all the rest..
		for(int i = 0; i < premimes.length; i += 2) {
			m_mimemap_p.put(premimes[i], getMimeString(premimes[i + 1]));
		}
	}

	/**
	 *	Load all MIME types. First load the default ones,
	 */
	static synchronized public void loadMimeMap(File fn) throws Exception {
		initMimeMap();

		InputStream is = new FileInputStream(fn);
		try {
			m_mimemap_p.load(is);
		} finally {
			is.close();
		}
	}


	static public Properties getMimeMap() {
		initMimeMap();
		return m_mimemap_p;
	}

	static public final String makeTimeString() {
		Calendar c = new GregorianCalendar();

		//-- Format is 18!12:17:21
		int i = c.get(Calendar.DAY_OF_MONTH);
		char[] b = new char[11];

		b[0] = (char) ((i / 10) + '0');
		b[1] = (char) ((i % 10) + '0');
		b[2] = '!';

		i = c.get(Calendar.HOUR_OF_DAY);
		b[3] = (char) ((i / 10) + '0');
		b[4] = (char) ((i % 10) + '0');
		b[5] = ':';

		i = c.get(Calendar.MINUTE);
		b[6] = (char) ((i / 10) + '0');
		b[7] = (char) ((i % 10) + '0');
		b[8] = ':';

		i = c.get(Calendar.SECOND);
		b[9] = (char) ((i / 10) + '0');
		b[10] = (char) ((i % 10) + '0');
		return new String(b);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Unique identifier generator.						*/
	/*--------------------------------------------------------------*/
	/** The current unique key generator's sequence counter */
	static private long m_us_count;

	/** The current unique key generator's timestamp. */
	static private int m_us_ts;

	static private synchronized long getUniq() {
		return ++m_us_count;
	}

	/**
	 * Returns an unique string every time it gets called. The string contains
	 * a sequence number and a time part that should be unique. The string
	 * consists of digits and letters [base64].
	 */
	static public String getUniqueString() {
		//-- Encode 4 digits (4*6 bits) of the timestamp.
		char[] cc = new char[10 + 4]; // Max. output size (64 / 6) + 4 digits

		int ix = 0;
		int v = m_us_ts;
		while(ix < 4) {
			int dv = (v & 0x3f); // Get 0..63 digit value
			cc[ix++] = BASECODES.charAt(dv); // Set basecode
			v >>= 6; // Shift out,
		}

		//-- Now do the same for the sequence (10 digits)
		long lv = getUniq();
		while(ix < 14) {
			int dv = (int) (lv & 0x3f);
			cc[ix++] = BASECODES.charAt(dv);
			lv >>= 6;
		}
		return new String(cc);
	}

	static public void generateExpiryHeader(HttpServletResponse res, int exp) {
		if(exp > 0) {
			//-- Get a calendar with the current date,
			GregorianCalendar c = new GregorianCalendar();
			//			System.out.println("Expiry: current date/time = "+c.getTime());

			c.add(Calendar.SECOND, exp); // Add in expiry,
			//			System.out.println("Expiry: after expiry "+exp+" date/time = "+c.getTime());

			//-- Now generate an HTTP date,
			res.setDateHeader("Expires", c.getTime().getTime());
		}
	}

	/**
	 * Sends headers that are needed to prevent the browser or any proxy in between from caching
	 * the page's contents.
	 * @param res
	 */
	static public void generateNoCache(HttpServletResponse response) {
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate, no-store");
		response.setHeader("Expires", "Mon, 8 Aug 2006 10:00:00 GMT");
	}

	/**
	 * Gets a 'long' parameter from the request. Throws an exception if the parameter is not
	 * available or not a valid long number.
	 * @param req
	 * @param name
	 * @return
	 * @throws ServletException
	 */
	static public long getLong(HttpServletRequest req, String name) throws ServletException {
		String s = req.getParameter(name);
		if(s != null) {
			s = s.trim();
			if(s.length() > 0) {
				try {
					return Long.parseLong(s);
				} catch(Exception x) {
					throw new ServletException("The value '" + s + "' for the request parameter '" + name + "' is not a valid long integer");
				}
			}
		}
		throw new ServletException("Missing value for request parameter '" + name + "'");
	}

	static public int getInt(HttpServletRequest req, String name) throws ServletException {
		String s = req.getParameter(name);
		if(s != null) {
			s = s.trim();
			if(s.length() > 0) {
				try {
					return Integer.parseInt(s);
				} catch(Exception x) {
					throw new ServletException("The value '" + s + "' for the request parameter '" + name + "' is not a valid integer");
				}
			}
		}
		throw new ServletException("Missing value for request parameter '" + name + "'");
	}

	static public int getInt(HttpServletRequest req, String name, int dflt) throws ServletException {
		String s = req.getParameter(name);
		if(s == null)
			return dflt;
		s = s.trim();
		if(s.length() == 0)
			return dflt;
		try {
			return Integer.parseInt(s);
		} catch(Exception x) {
			throw new ServletException("The value '" + s + "' for the request parameter '" + name + "' is not a valid integer");
		}
	}

	static {
		m_us_count = 0;
		m_us_ts = (int) (System.currentTimeMillis() % 1234567);
	}
}
