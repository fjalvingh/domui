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
package to.etc.domui.ajax;

import java.util.*;

import to.etc.domui.annotations.*;

public class RpcClassDefinition {
	private final Class< ? > m_handlerClass;

	private boolean m_initialized;

	private Exception m_errorException;

	private String[] m_roles;

	private ResponseFormat m_responseFormat;

	private final Map<String, RpcMethodDefinition> m_methodMap = new HashMap<String, RpcMethodDefinition>();

	public RpcClassDefinition(final Class< ? > cl) {
		m_handlerClass = cl;
	}

	synchronized public void initialize() throws Exception {
		if(m_errorException != null)
			throw m_errorException;
		if(m_initialized)
			return;
		try {
			checkAnnotations();
			m_initialized = true;
		} catch(Exception x) {
			m_errorException = x;
			throw x;
		}
	}

	private void checkAnnotations() throws Exception {
		if(!m_handlerClass.isAnnotationPresent(AjaxHandler.class))
			throw new RpcException("The class " + m_handlerClass.getCanonicalName() + " is not annotated with @AjaxHandler");
		AjaxHandler ah = m_handlerClass.getAnnotation(AjaxHandler.class);
		StringTokenizer st = new StringTokenizer(ah.roles(), " \t,");
		List<String> l = new ArrayList<String>();
		while(st.hasMoreTokens()) {
			String s = st.nextToken().trim();
			if(s.length() > 0)
				l.add(s);
		}
		m_roles = l.toArray(new String[l.size()]);

		//-- Save default output format, if specified.
		m_responseFormat = ah.response();
	}

	public String[] getRoles() {
		return m_roles;
	}

	public Class< ? > getHandlerClass() {
		return m_handlerClass;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Method interface.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	public synchronized RpcMethodDefinition getMethod(final String name) throws Exception {
		RpcMethodDefinition mi = m_methodMap.get(name);
		if(mi == null) {
			mi = new RpcMethodDefinition(this, name);
			m_methodMap.put(name, mi);
		}
		mi.initialize();
		return mi;
	}

	final public ResponseFormat getResponseFormat() {
		return m_responseFormat;
	}
}
