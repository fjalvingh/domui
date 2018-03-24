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
package to.etc.net;

import org.w3c.dom.*;
import to.etc.util.*;
import to.etc.xml.*;

import javax.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

/**
 * Utilities for net access.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 27, 2005
 */
final public class NetTools {
	private NetTools() {
	}

	/**
	 * Takes the host= parameter in the header to construct the real
	 * hostname.
	 *
	 * @param sb
	 * @param req
	 * @throws Exception
	 */
	static public void getHostURL(StringBuffer sb, HttpServletRequest req) {
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(getHostName(req));
		int port = getHostPort(req);
		if(port != 80) {
			sb.append(':');
			sb.append(port);
		}
		sb.append('/');
	}

	static public String getHostURL(HttpServletRequest req) {
		StringBuffer sb = new StringBuffer(64);
		getHostURL(sb, req);
		return sb.toString();
	}

	static public String getHostName(HttpServletRequest req) {
		//-- Proxied?
		String hdr = req.getHeader("X-Forwarded-Host");
		if(null != hdr)
			return hdr;

		String hostname = req.getHeader("Host");
		if(hostname != null) {
			int i = hostname.lastIndexOf(':');
			if(i != -1)
				hostname = hostname.substring(0, i); // Discard port,
			return hostname;
		}

		//-- No host header- use server's view..
		hostname = req.getServerName();
		if(hostname == null)
			throw new IllegalStateException("Unable to get host name from request!?");
		return hostname;
	}

	/**
	 * Return the host name/IP address of the remote side of the connection. This
	 * is proxy safe: it checks for proxy headers on the request.
	 */
	static public String getRemoteHost(HttpServletRequest req) throws Exception {
		// Only allow proxy headers from non internet addresses (10.x, 172.16..31, 192.168..)
		String remoteHost = req.getRemoteHost();
		InetAddress remote = InetAddress.getByName(remoteHost);
		if(remote.isSiteLocalAddress()) {
			//-- Proxied?
			String hdr = req.getHeader("X-Forwarded-For");
			if(null != hdr)
				return hdr;
			hdr = req.getHeader("X-Client-IP");
			if(null != hdr)
				return hdr;
		}
		return remoteHost;
	}

	public static boolean isHttps(HttpServletRequest request) throws Exception {
		String scheme = request.getScheme();
		if("https".equalsIgnoreCase(scheme))
			return true;
		String remoteHost = request.getRemoteHost();
		InetAddress remote = InetAddress.getByName(remoteHost);
		if(!remote.isSiteLocalAddress())
			return false;

		//-- Try proxy headers
		String hdr = request.getHeader("X-forwarded-proto");
		if(null == hdr)
			return false;
		return "https".equalsIgnoreCase(hdr);
	}

	static public int getHostPort(HttpServletRequest req) {
		String hostname = req.getHeader("Host");
		if(hostname != null) {
			int i = hostname.lastIndexOf(':');
			if(i != -1) {
				//-- Hostname contains port; use that
				try {
					return Integer.parseInt(hostname.substring(i + 1).trim());
				} catch(Exception x) {}
			}
		}

		//-- No host header- use server's view..
		return req.getServerPort();
	}

	/**
	 * Takes an input request and returns a proper relative path for
	 * the request. This properly handles URLEncoding and returns a
	 * string which NEVER starts with a '/', and which contains
	 * no hostname or port.
	 *
	 * @param req
	 * @return
	 */
	static public String getInputPath(HttpServletRequest req) {
		String rurl = req.getRequestURI();
		int pos = rurl.indexOf("://"); // Scheme in front?
		if(pos != -1) // Skip http
		{
			//-- Has scheme: must contain a host name.. Skip past that hostname,
			int dp = rurl.indexOf('/', pos + 3); // past http://
			if(dp == -1)
				return ""; // Root path
			rurl = rurl.substring(dp + 1);
		} else {
			//-- No http.. Does it start with /?
			if(rurl.startsWith("/"))
				rurl = rurl.substring(1);
		}
		return StringTool.decodeURLEncoded(rurl);
	}

	/**
	 * Returns the URL to the root of the application. This is the complete
	 * host URL including http://, host name and port number, followed by
	 * the webapp's context. The path is guaranteed to end in a slash.
	 * @param req
	 * @return
	 */
	@Nonnull
	static public String getApplicationURL(@Nonnull HttpServletRequest req) {
		String hu = getHostURL(req);
		String ctx = req.getContextPath();
		if(ctx.length() == 0) // Is this the root application?
			return hu; // Then the hostURL will suffice, thank you
		if(ctx.startsWith("/")) // This should be true always...
			return hu + ctx.substring(1) + "/"; // .. so remove one of the slashes
		return hu + ctx + "/";
	}

	/**
	 * This returns the application's context path <b>without any slashes!!</b>. So for
	 * a webapp deployed to http://www.test.nl:8080/demoapp/ this will return the string
	 * "demoapp". For a root application this returns the empty string.
	 *
	 * @param req
	 * @return
	 */
	static public String getApplicationContext(HttpServletRequest req) {
		String s = req.getContextPath();
		if(s == null || s.length() == 0)
			return ""; // Root context!
		if(s.startsWith("/") && s.endsWith("/"))
			return s.substring(1, s.length() - 1);
		if(s.startsWith("/"))
			return s.substring(1);
		if(s.endsWith("/"))
			return s.substring(0, s.length() - 1);
		return s;
	}

	/**
	 * Constructs an URL that is relative to the root of the server from an
	 * application-root based URL. So for a webapp deployed to "http://www.test.nl:8080/demoapp/"
	 * and a relative URL "general/images/button.png" this will append the string
	 * "/demoapp/general/images/button.png" to the appendable passed.
	 * @param a
	 * @throws IOException	Nonsense exception needed by stupid Java checked exception crap
	 */
	static public void appendRootRelativeURL(Appendable a, HttpServletRequest req, String rurl) throws IOException {
		String s = req.getContextPath();
		if(s == null || s.length() == 0 || s.equals("/")) { // Root application?
			a.append('/');
			s = "";
		} else {
			if(s.startsWith("/"))
				a.append(s);
			else {
				a.append('/');
				a.append(s);
			}
		}
		if(!s.endsWith("/"))
			a.append('/');
		if(rurl == null || rurl.length() == 0)
			return;
		if(rurl.startsWith("/"))
			a.append(rurl, 1, rurl.length());
		else
			a.append(rurl);
	}

	static public String getRootRelativeURL(HttpServletRequest req, String rurl) {
		StringBuilder sb = new StringBuilder();
		try {
			appendRootRelativeURL(sb, req, rurl);
		} catch(IOException x) {
			x.printStackTrace();
		}
		return sb.toString();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	URL Call Helpers.									*/
	/*--------------------------------------------------------------*/
	static public class HttpInputStream extends InputStream {
		private InputStream			m_is;

		private String				m_encoding;

		private HttpURLConnection	m_connection;

		public HttpInputStream(HttpURLConnection conn, InputStream is, String encoding) {
			m_is = is;
			m_encoding = encoding;
			m_connection = conn;
		}

		public String getEncoding() {
			return m_encoding;
		}

		@Override
		public int available() throws IOException {
			return m_is.available();
		}

		@Override
		public void close() throws IOException {
			m_is.close();
			try {
				m_connection.disconnect();
			} catch(Exception x) {}
		}

		/**
		 * Despite the warning, do not make synchronized.
		 * @see java.io.InputStream#mark(int)
		 */
		@Override
		public void mark(int readlimit) {
			m_is.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return m_is.markSupported();
		}

		@Override
		public int read() throws IOException {
			return m_is.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return m_is.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return m_is.read(b);
		}

		/**
		 * Despite the warning, do not make synchronized.
		 * @see java.io.InputStream#mark(int)
		 */
		@Override
		public void reset() throws IOException {
			m_is.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return m_is.skip(n);
		}
	}

	/**
	 * Calls an external server and tries to retrieve an XML formatted document
	 * from it. The document is then returned.
	 */
	static public Document httpGetXMLDocument(String url, int timeout, boolean namespaceaware) throws Exception {
		URL u = new URL(url);
		if(!u.getProtocol().equals("http") && !u.getProtocol().equalsIgnoreCase("https"))
			throw new IllegalStateException("This call can only accept http(s):// connections.");
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		InputStream is = null;
		Reader r = null;
		try {
			huc.setReadTimeout(timeout);
			huc.setAllowUserInteraction(false);
			huc.setDoOutput(false);
			huc.connect();

			//-- Check for a response...
			int code = huc.getResponseCode();
			if(code != HttpURLConnection.HTTP_OK) {
				throw handleHttpError(url, huc); // This throws an exception indicating the problem
			}

			//-- Create a reader.
			String encoding = huc.getContentEncoding();
			if(encoding == null || encoding.length() == 0)
				encoding = "UTF-8";
			is = huc.getInputStream();
			r = new InputStreamReader(is, encoding);
			Document doc = DomTools.getDocument(r, url, namespaceaware);
			return doc;
		} finally {
			try {
				if(r != null)
					r.close();
			} catch(Exception x) {}
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			try {
				if(huc != null)
					huc.disconnect();
			} catch(Exception x) {}
		}
	}

	/**
	 * Calls an external server and returns the response as an inputstream.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	static public InputStream httpGetStream(String url, int timeout) throws Exception {
		URL u = new URL(url);
		if(!u.getProtocol().equals("http") && !u.getProtocol().equalsIgnoreCase("https"))
			throw new IllegalStateException("This call can only accept http(s):// connections.");
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		try {
			huc.setReadTimeout(timeout);
			huc.setAllowUserInteraction(false);
			huc.setDoOutput(false);
			huc.connect();

			//-- Check for a response...
			int code = huc.getResponseCode();
			if(code != HttpURLConnection.HTTP_OK) {
				throw handleHttpError(url, huc); // This throws an exception indicating the problem
			}

			//-- Now: create the input stream..
			HttpInputStream his = new HttpInputStream(huc, huc.getInputStream(), huc.getContentEncoding());
			huc = null; // Ownership passed
			return his;
		} finally {
			try {
				if(huc != null)
					huc.disconnect();
			} catch(Exception x) {}
		}
	}

	/**
	 * Calls an external server and returns the response as a string.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	static public String httpGetString(String url, int timeout) throws Exception {
		URL u = new URL(url);
		if(!u.getProtocol().equals("http") && !u.getProtocol().equalsIgnoreCase("https"))
			throw new IllegalStateException("This call can only accept http(s):// connections.");
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		Reader r = null;
		try {
			huc.setReadTimeout(timeout);
			huc.setAllowUserInteraction(false);
			huc.setDoOutput(false);
			huc.connect();

			//-- Check for a response...
			int code = huc.getResponseCode();
			if(code != HttpURLConnection.HTTP_OK) {
				throw handleHttpError(url, huc); // This throws an exception indicating the problem
			}
			String encoding = huc.getContentEncoding();
			if(encoding == null)
				encoding = "UTF-8";
			r = new InputStreamReader(huc.getInputStream(), encoding);
			String res = FileTool.readStreamAsString(r);
			return res;
		} finally {
			try {
				if(r != null)
					r.close();
			} catch(Exception x) {}
			try {
				if(huc != null)
					huc.disconnect();
			} catch(Exception x) {}
		}
	}

	static public HttpCallException handleHttpError(String url, HttpURLConnection huc) throws Exception {
		HttpCallException hcx = new HttpCallException(url, huc.getResponseCode(), huc.getResponseMessage());
		return hcx;
	}
}
