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
package to.etc.el;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public class ImplicitObjects {
	static final String sAttributeName = "to.etc.el.ImplObjs";

	private PageContext m_context;

	private Map m_page;

	private Map m_request;

	private Map m_session;

	private Map m_application;

	private Map m_param;

	private Map m_paramArray;

	private Map m_header;

	private Map m_headerArray;

	private Map m_initParam;

	private Map m_cookie;

	//-------------------------------------
	/**
	 *
	 * Constructor
	 **/
	public ImplicitObjects(PageContext pContext) {
		m_context = pContext;
	}

	/**
	 * Finds the ImplicitObjects associated with the PageContext,
	 * creating it if it doesn't yet exist.
	 */
	public static ImplicitObjects getImplicitObjects(PageContext pContext) {
		ImplicitObjects objs = (ImplicitObjects) pContext.getAttribute(sAttributeName, PageContext.PAGE_SCOPE);
		if(objs == null) {
			objs = new ImplicitObjects(pContext);
			pContext.setAttribute(sAttributeName, objs, PageContext.PAGE_SCOPE);
		}
		return objs;
	}

	/**
	 * Returns the Map that "wraps" page-scoped attributes
	 */
	public Map getPageScopeMap() {
		if(m_page == null)
			m_page = createPageScopeMap(m_context);
		return m_page;
	}

	/**
	 * Returns the Map that "wraps" request-scoped attributes
	 */
	public Map getRequestScopeMap() {
		if(m_request == null)
			m_request = createRequestScopeMap(m_context);
		return m_request;
	}

	/**
	 * Returns the Map that "wraps" session-scoped attributes
	 */
	public Map getSessionScopeMap() {
		if(m_session == null)
			m_session = createSessionScopeMap(m_context);
		return m_session;
	}

	/**
	 * Returns the Map that "wraps" application-scoped attributes
	 */
	public Map getApplicationScopeMap() {
		if(m_application == null)
			m_application = createApplicationScopeMap(m_context);
		return m_application;
	}

	/**
	 * Returns the Map that maps parameter name to a single parameter
	 * values.
	 */
	public Map getParamMap() {
		if(m_param == null)
			m_param = createParamMap(m_context);
		return m_param;
	}

	/**
	 * Returns the Map that maps parameter name to an array of parameter
	 * values.
	 */
	public Map getParamsMap() {
		if(m_paramArray == null)
			m_paramArray = createParamsMap(m_context);
		return m_paramArray;
	}

	/**
	 * Returns the Map that maps header name to a single header
	 * values.
	 */
	public Map getHeaderMap() {
		if(m_header == null)
			m_header = createHeaderMap(m_context);
		return m_header;
	}

	/**
	 * Returns the Map that maps header name to an array of header
	 * values.
	 */
	public Map getHeadersMap() {
		if(m_headerArray == null)
			m_headerArray = createHeadersMap(m_context);
		return m_headerArray;
	}

	/**
	 * Returns the Map that maps init parameter name to a single init
	 * parameter values.
	 */
	public Map getInitParamMap() {
		if(m_initParam == null)
			m_initParam = createInitParamMap(m_context);
		return m_initParam;
	}

	/**
	 * Returns the Map that maps cookie name to the first matching
	 * Cookie in request.getCookies().
	 */
	public Map getCookieMap() {
		if(m_cookie == null)
			m_cookie = createCookieMap(m_context);
		return m_cookie;
	}

	/**
	 * Creates the Map that "wraps" page-scoped attributes
	 */
	public static Map createPageScopeMap(PageContext pContext) {
		final PageContext context = pContext;
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return context.getAttributeNamesInScope(PageContext.PAGE_SCOPE);
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return context.getAttribute((String) pKey, PageContext.PAGE_SCOPE);
				return null;
			}

			@Override
			public boolean isMutable() {
				return true;
			}
		};
	}

	/**
	 * Creates the Map that "wraps" request-scoped attributes
	 */
	public static Map createRequestScopeMap(PageContext pContext) {
		final PageContext context = pContext;
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return context.getAttributeNamesInScope(PageContext.REQUEST_SCOPE);
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return context.getAttribute((String) pKey, PageContext.REQUEST_SCOPE);
				return null;
			}

			@Override
			public boolean isMutable() {
				return true;
			}
		};
	}

	/**
	 * Creates the Map that "wraps" session-scoped attributes
	 */
	public static Map createSessionScopeMap(PageContext pContext) {
		final PageContext context = pContext;
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return context.getAttributeNamesInScope(PageContext.SESSION_SCOPE);
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return context.getAttribute((String) pKey, PageContext.SESSION_SCOPE);
				return null;
			}

			@Override
			public boolean isMutable() {
				return true;
			}
		};
	}

	/**
	 * Creates the Map that "wraps" application-scoped attributes
	 */
	public static Map createApplicationScopeMap(PageContext pContext) {
		final PageContext context = pContext;
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return context.getAttributeNamesInScope(PageContext.APPLICATION_SCOPE);
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return context.getAttribute((String) pKey, PageContext.APPLICATION_SCOPE);
				return null;
			}

			@Override
			public boolean isMutable() {
				return true;
			}
		};
	}

	/**
	 * Creates the Map that maps parameter name to single parameter
	 * value.
	 */
	public static Map createParamMap(PageContext pContext) {
		final HttpServletRequest request = (HttpServletRequest) pContext.getRequest();
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return request.getParameterNames();
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return request.getParameter((String) pKey);
				return null;
			}

			@Override
			public boolean isMutable() {
				return false;
			}
		};
	}

	/**
	 * Creates the Map that maps parameter name to an array of parameter
	 * values.
	 */
	public static Map createParamsMap(PageContext pContext) {
		final HttpServletRequest request = (HttpServletRequest) pContext.getRequest();
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return request.getParameterNames();
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return request.getParameterValues((String) pKey);
				return null;
			}

			@Override
			public boolean isMutable() {
				return false;
			}
		};
	}

	/**
	 * Creates the Map that maps header name to single header
	 * value.
	 */
	public static Map createHeaderMap(PageContext pContext) {
		final HttpServletRequest request = (HttpServletRequest) pContext.getRequest();
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return request.getHeaderNames();
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return request.getHeader((String) pKey);
				return null;
			}

			@Override
			public boolean isMutable() {
				return false;
			}
		};
	}

	/**
	 * Creates the Map that maps header name to an array of header
	 * values.
	 */
	public static Map createHeadersMap(PageContext pContext) {
		final HttpServletRequest request = (HttpServletRequest) pContext.getRequest();
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return request.getHeaderNames();
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String) {
					List l = new ArrayList();
					Enumeration en = request.getHeaders((String) pKey);
					if(en != null) {
						while(en.hasMoreElements())
							l.add(en.nextElement());
					}
					String[] ret = (String[]) l.toArray(new String[l.size()]);
					return ret;
				}
				return null;
			}

			@Override
			public boolean isMutable() {
				return false;
			}
		};
	}

	/**
	 * Creates the Map that maps init parameter name to single init
	 * parameter value.
	 */
	public static Map createInitParamMap(PageContext pContext) {
		final ServletContext context = pContext.getServletContext();
		return new EnumeratedMap() {
			@Override
			public Enumeration enumerateKeys() {
				return context.getInitParameterNames();
			}

			@Override
			public Object getValue(Object pKey) {
				if(pKey instanceof String)
					return context.getInitParameter((String) pKey);
				return null;
			}

			@Override
			public boolean isMutable() {
				return false;
			}
		};
	}

	/**
	 * Creates the Map that maps cookie name to the first matching
	 * Cookie in request.getCookies().
	 */
	public static Map createCookieMap(PageContext pContext) {
		// Read all the cookies and construct the entire map
		HttpServletRequest request = (HttpServletRequest) pContext.getRequest();
		Cookie[] cookies = request.getCookies();
		Map ret = new HashMap();
		for(int i = 0; cookies != null && i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			if(cookie != null) {
				String name = cookie.getName();
				if(!ret.containsKey(name))
					ret.put(name, cookie);
			}
		}
		return ret;
	}
}
