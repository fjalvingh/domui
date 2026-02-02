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

import java.io.*;
import java.net.*;
import java.util.*;

import to.etc.util.*;

/**
 * <p>This class allows posting a multipart/form-data request to a server. It uses
 * an URL as the base to connect to, and it has methods to add parameters and
 * files to the set to send.</p>
 *
 * <p>This class uses a lazy mechanism to get file data to write for posted files:
 * when data is required for a file parameter it repeatedly calls a handler for
 * a given parameter.</p>
 *
 * <p>First create an instance of this class. Then add all parameters to PUT
 * using the addParam() and addFile() calls. When all parameters to put have been
 * set then call issue() to exchange the request.</p>
 *
 * <p>The call to issue returns the connection that is used to send the request;
 * you need to read the response stream yourself (by calling getInputStream() and
 * reading it) since I have NO idea what you want to do with the answer ;-)</p>
 *
 * <p>This structure can be reused by calling clear() before use, which clears
 * all parameters and files set.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
public class MultipartPoster {
	static private final String		BOUNDARY	= "--boun-da-ry-0xababaeaGfHdNarcolethe-mumble-to-content-eNCoDer-gxixmar-rennes-le-chateau";

	/** The URL to exchange the data with */
	//	private URL			m_url;

	/** The list of params/files to send... */
	private List<AParameter>		m_param_v	= new ArrayList<AParameter>();

	//	static private	byte[]		m_boundary;

	static private String m_boundary_str;

	static private byte[] m_boundary_mark;

	static private byte[] m_boundary_mark_final;

	/** The proxy authentication string, in base64(userid:password) encoding, or null for no password */
	private String					m_proxy_auth;

	private MultipartSendListener	m_mpsl;

	static {
		m_boundary_str = BOUNDARY + Long.toHexString(System.currentTimeMillis()) + "etc";
		//		m_boundary	= m_boundary_str.getBytes();
		m_boundary_mark = ("--" + m_boundary_str + "\r\n").getBytes();
		m_boundary_mark_final = ("\r\n--" + m_boundary_str + "--\r\n").getBytes();
	}


	public MultipartPoster() {
	}

	public void setProxyAuthentication(String auth) {
		m_proxy_auth = auth;
	}

	public void setProxyAuthentication(String uid, String pw) {
		String s = uid + ":" + pw;
		m_proxy_auth = "Basic " + StringTool.encodeBase64(s);
	}

	public void clearProxyAuthentication() {
		m_proxy_auth = null;
	}

	public void setListener(MultipartSendListener mpsl) {
		m_mpsl = mpsl;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Adding parameters...								*/
	/*--------------------------------------------------------------*/
	/**
	 * Removes all parameters currently in the table. It does NOT call close
	 * for files that were added but unused!
	 */
	public void clear() {
		m_param_v.clear();
	}


	/**
	 * Adds a parameter to send to the http server. The parameter is encoded
	 * using UTF-8 encoding. You can add the same name multiple times; each
	 * parameter will be put to the server.
	 * @param name		the parameter's name
	 * @param value		the parameter's value.
	 */
	public void addParam(String name, String value) {
		AParameter ap = new AParameter(name, value);
		m_param_v.add(ap);
	}

	/**
	 * Adds a file parameter to the request. The MultipartFile class will provide
	 * the file's data as soon as it is needed when sending.
	 * @param name	the parameter's name.
	 * @param fname		the suggested filename
	 * @param mf	the handler which will provide the file's data as soon as it
	 * 				is needed.
	 */
	public void addFile(String name, String fname, MultipartFile mf) {
		AParameter ap = new AParameter(name, fname, mf);
		m_param_v.add(ap);
	}

	/**
	 * Adds a file parameter to the request. The file will be read as soon as the
	 * data is needed.
	 * @param name		the name of the parameter
	 * @param fname		the suggested filename
	 * @param f			the existing file to send
	 */
	public void addFile(String name, String fname, File f) {
		AFileToSend afts = new AFileToSend(f);
		addFile(name, fname, afts);
	}

	/**
	 * Adds a file to send to the request. The data is read from the stream
	 * provided. You must close the stream yourself!!
	 * @param name		the name of the parameter.
	 * @param fname		the suggested filename
	 * @param is		the stream providing the data.
	 */
	public void addFile(String name, String fname, InputStream is, int len) {
		AFileToSend afts = new AFileToSend(is, len);
		addFile(name, fname, afts);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Exchanging the data...								*/
	/*--------------------------------------------------------------*/
	/**
	 * Sends the constructed request to the server, and returns the reply
	 * stream (the output file generated by the server).
	 * @param conn		the URL to post the data to,
	 * @return
	 * @throws Exception	on any error.
	 */
	public HttpURLConnection exchange(URL url) throws Exception {
		HttpURLConnection hc = null;
		OutputStream os = null;
		MultipartFile mpf = null;

		//-- 1. Calculate the #bytes in files to send,
		int totsz = 0;
		for(AParameter ap : m_param_v) {
			if(ap.m_mpf != null) {
				totsz += ap.m_mpf.getSize();
			}
		}

		try {
			if(m_mpsl != null)
				m_mpsl.sending(0, totsz);

			hc = (HttpURLConnection) url.openConnection();
			if(m_proxy_auth != null)
				hc.setRequestProperty("Proxy-Authorization", m_proxy_auth);
			hc.setRequestMethod("POST");
			hc.setRequestProperty("Connection", "Keep-Alive");
			hc.setDoOutput(true);
			hc.setDoInput(true);
			hc.setUseCaches(false);
			hc.setRequestProperty("Accept-Charset", "iso-8859-1,*,utf-8");
			hc.setRequestProperty("Accept-Language", "en");
			hc.setRequestProperty("Content-type", "multipart/form-data; boundary=" + m_boundary_str);
			os = hc.getOutputStream();

			byte[] buf = null;

			//-- Initial Boundary marker
			os.write(m_boundary_mark); // Start off writing a boundary mark without crlf

			//-- Loop for all parameters,
			int szdone = 0;
			StringBuffer sb = new StringBuffer(128);
			for(AParameter ap : m_param_v) {
				//-- Content-disposition
				sb.setLength(0);
				sb.append("Content-Disposition: form-data; name=\"");
				sb.append(ap.m_name);
				sb.append("\"");
				if(ap.m_mpf != null) // File parameter??
				{
					sb.append("; filename=\"");
					sb.append(ap.m_value);
					sb.append("\"");
				}
				sb.append("\r\n");

				//-- for files we need a content-type also,
				if(ap.m_mpf != null)
					sb.append("Content-Type: application/octet-stream\r\n");
				sb.append("\r\n"); // Empty line denotes start of data

				if(ap.m_mpf == null) // Not a file?
					sb.append(ap.m_value); // Add the value
				os.write(sb.toString().getBytes()); // write it,

				//-- If this was a file then now is the time to generate data,
				if(ap.m_mpf != null) {
					mpf = ap.m_mpf;
					mpf.open();
					if(buf == null)
						buf = new byte[8192];

					//-- Keep reading and writing till eof,
					int sz;
					while(0 < (sz = mpf.getBytes(buf))) {
						if(m_mpsl != null)
							m_mpsl.sending(szdone, totsz);
						os.write(buf, 0, sz);
						szdone += sz;
					}
					mpf.close();
					mpf = null;
				}

				//-- All done! Add a CRLF and a boundary marker.
				os.write(m_boundary_mark_final);
			}
			if(m_mpsl != null)
				m_mpsl.sending(totsz, totsz);


			//-- All parameters were sent.
			//-- Now we expect the response!
			return hc;
		} finally {
			try {
				if(mpf != null)
					mpf.close();
			} catch(Exception x) {}
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}

}


/**
 * Internal class to hold parameter / file parameter information.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
@Deprecated
class AParameter {
	/** The parameter name, */
	protected String		m_name;

	/** The value IF not a file parameter, else it's null */
	protected String		m_value;

	/** The filehandler for a file parameter, or null */
	protected MultipartFile	m_mpf;

	protected AParameter(String name, String value) {
		m_name = name;
		m_value = value;
	}

	protected AParameter(String name, String filename, MultipartFile value) {
		m_name = name;
		m_value = filename;
		m_mpf = value;
	}
}


/**
 * An implementation of a MultipartFile which reads either a File or an
 * InputStream.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
class AFileToSend extends MultipartFile {
	/** The filename of the file to send, */
	private File		m_f;

	/** The inputstream when open, */
	private InputStream	m_is;

	private int			m_is_sz;

	protected AFileToSend(File f) {
		m_f = f;
	}

	protected AFileToSend(InputStream is, int sz) {
		m_is = is;
		m_is_sz = sz;
	}

	@Override
	public int getBytes(byte[] parm1) throws java.io.IOException {
		return m_is.read(parm1); // Read next block from file.
	}

	/**
	 * Open the file if no stream is provided.
	 * @throws IOException
	 */
	@Override
	public void open() throws java.io.IOException {
		if(m_is != null)
			return;
		m_is = new BufferedInputStream(new FileInputStream(m_f));
	}

	@Override
	public int getSize() {
		if(m_is != null)
			return m_is_sz;
		else
			return (int) m_f.length();
	}

	@Override
	public void close() {
		try {
			if(m_is != null && m_f != null)
				m_is.close();
		} catch(Exception x) {}
	}

}
