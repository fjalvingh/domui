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
package to.etc.template;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.util.*;

/**
 * VERY OLD - DO NOT USE
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
@Deprecated
public class ServletTplExpander extends TplExpander {
	/// The request block from a template, or NULL
	protected HttpServletRequest	m_req;

	/// The response block from a template, or null
	protected HttpServletResponse	m_res;

	public ServletTplExpander(TplCallback cb) {
		super(cb);
	}

	public ServletTplExpander(TplCallback cb, HttpServletRequest req, HttpServletResponse res) {
		this(cb);
		m_req = req;
		m_res = res;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Retrieving parameters...							*/
	/*--------------------------------------------------------------*/
	public String getParmStr(String name) {
		String s = m_req.getParameter(name);
		if(s == null)
			return "";
		return s.trim();
	}

	public String getParmStr(String name, boolean req) throws ServletException {
		String s = getParmStr(name);
		if(s == null && req)
			throw new ServletException("This request requires the parameter " + name + " to be passed.");
		return s;
	}

	public int getParmInt(String name) {
		String s = m_req.getParameter(name);
		if(s == null)
			return 0;

		try {
			int i = Integer.parseInt(s);
			return i;
		} catch(Exception x) {
			return -1;
		}
	}

	/**
	 *	Makes sure the document is not cached at the client's station.
	 */
	protected void setNoCache() {
		//		res.setContentType("text/html");				// Set HTML output,
		m_res.addHeader("expires", "Tuesday, 13-Jan-94 12:12:12 GMT");
		m_res.addHeader("pragma", "no-cache");
	}

	/**
	 *	This gets called directly after the 1st request is received. It extracts
	 *	this-servlet's URL from the request URL and saves it.
	 */
	protected void handlerInit() {
		if(m_hostname != null)
			return; // Already initialized!

		//-- Save the completed host name
		m_hostname = m_req.getServerName(); // Get hostname
		m_hostport = m_req.getServerPort(); // And the port used,
		m_hosturl = "http://" + m_hostname + (m_hostport == 80 ? "" : ":" + m_hostport) + "/";
		m_servlet_basepath = m_req.getServletPath(); // And the base path for the slet.
		m_servlet_fullpath = slconc(m_hosturl, m_servlet_basepath);
	}

	protected String getServletFullPath() {
		handlerInit();
		return m_servlet_fullpath;
	}


	/**
	 *	Expands the template from the input-stream to the output-stream, using
	 *	the specified context.
	 */
	public void expand(InputStream is) throws Exception {
		//-- Read the entire thing into a string buffer.
		try {
			String tpl = FileTool.readStreamAsString(is, "utf-8");
			setNoCache();
			m_res.setContentType("text/html");
			expand(tpl, m_res.getWriter());
		} finally {
			//			m_os	= null;
			is.close();
		}
	}

	/**
	 * If the name passed is a predefined name this function will return the
	 * result object for the name. This is better than putting all predefined
	 * names in the hash table because many names must be computed, and building
	 * such a hash table takes time and resources (that are garbage after each
	 * request!).
	 * If the name passed is not a predefined name the routine returns null.
	 */
	@Override
	protected Object findPredef(String name) {
		//-- Return predefined tings.
		if(name.equalsIgnoreCase("host")) {
			handlerInit();
			return m_hosturl;
		} else if(name.equalsIgnoreCase("appurl")) {
			//-- Return the path of the servlet
			handlerInit();
			return m_servlet_fullpath;
		} else if(name.equalsIgnoreCase("fullurl")) {
			//-- Complete URL, including query string.
			//			StringBuffer	sb	= HttpUtils.getRequestURL(m_req);
			StringBuffer sb = new StringBuffer(64);
			sb.append(m_req.getRequestURI());
			String s = m_req.getQueryString();
			if(s != null) {
				sb.append("?");
				sb.append(s);
			}
			return sb.toString();
		} else if(name.equalsIgnoreCase("request")) {
			return m_req;
		} else if(name.equalsIgnoreCase("prevurl") || name.equalsIgnoreCase("referer") || name.equalsIgnoreCase("referrer") || name.equalsIgnoreCase("referred")) {
			String s = m_req.getHeader("Referer");
			if(s == null)
				s = getServletFullPath();
			return s;
		}
		return null;
	}

	/**
	 * Locates a root name. The name is first searched in this-context's hashtable;
	 * then all user functions are tried. This allows the put method to
	 * override <b>all</b> names!!
	 */
	@Override
	protected Object locateName(String name) throws TplException {
		Object o = super.locateName(name);
		if(o != null)
			return o;
		if(m_req != null) {
			o = m_req.getParameter(name);
			if(o != null)
				return o;
		}
		return "(unknown name '" + name + "')";
	}

	/**
	 *	This method posts the "get from form" method for fields. All references
	 *	to the field specified will be resolved by asking if a form parameter
	 *	exists. So a reference to 'user.loginid' would usually refer to a result
	 *	set 'user' having a field 'loginid', but using this method it will
	 *	return the "userid" parameter from the form's request.
	 */
	public void putFormAs(String name) {
		putGetter(name, new TplFormAs(m_req));
	}


}
class TplFormAs {
	private HttpServletRequest	m_req;


	public TplFormAs(HttpServletRequest req) {
		m_req = req;
	}

	public String get(String name) {
		return m_req.getParameter(name);
	}
}
