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

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.annotations.*;

public class RpcMethodDefinition {
	private final RpcClassDefinition m_ServiceClassDefinition;

	private Method m_method;

	private final String m_name;

	private Exception m_exc;

	private String[] m_roles;

	private boolean m_static;

	private boolean m_initialized;

	private ResponseFormat m_responseFormat;

	/**
	 * If this method has a first parameter that specifies a supported output
	 * then that class is saved herein. This indicates that the method renders
	 * it's output by itself.
	 */
	private Class< ? > m_outputClass;

	RpcMethodDefinition(final RpcClassDefinition hi, final String name) {
		m_name = name;
		m_ServiceClassDefinition = hi;
	}

	void initialize() throws Exception {
		if(m_exc != null)
			throw m_exc;
		if(m_initialized)
			return;
		try {
			m_method = findMethod(m_name);
			if(m_method == null)
				throw new RpcException(m_ServiceClassDefinition.getHandlerClass() + " does not have a method called '" + m_name + "'");
			checkReturnMethod();
			checkAnnotations(m_method);
		} catch(Exception x) {
			m_exc = x;
			throw x;
		}
	}

	private void checkReturnMethod() throws Exception {
		Class< ? > rv = m_method.getReturnType();
		if(rv != Void.TYPE) { // Any method with a return type uses the return type as result
			m_outputClass = null;
			return;
		}

		//-- Void method: the 1st parameter defines the method to render the output
		Class< ? >[] par = m_method.getParameterTypes();
		if(par.length == 0)
			throw new RpcException("The method '" + m_method + "' returns void and does not have an output parameter; it cannot be called.");
		m_outputClass = par[0]; // Output parameter type.
	}

	private Method findMethod(final String name) throws Exception {
		Class< ? > cl = m_ServiceClassDefinition.getHandlerClass();
		Method foundm = null;
		for(Method m : cl.getMethods()) {
			if(m.getName().equals(name)) {
				if(foundm != null)
					throw new RpcException("The method '" + name + "' occurs 2ce [" + foundm.toGenericString() + " and " + m.toGenericString() + "]");
				foundm = m;
			}
		}
		return foundm;
	}

	private void checkAnnotations(final Method m) throws Exception {
		m_static = Modifier.isStatic(m.getModifiers());

		//-- Check for the roles in the AjaxMethod annotation.
		AjaxMethod am = m.getAnnotation(AjaxMethod.class);
		List<String> l = new ArrayList<String>();
		if(am == null)
			throw new RpcException(m.getName() + ": The method is not annotated with @AjaxMethod");

		if(am.roles() != null) {
			StringTokenizer st = new StringTokenizer(am.roles(), " \t,");
			while(st.hasMoreTokens()) {
				String s = st.nextToken().trim();
				if(s.length() > 0)
					l.add(s);
			}
		}
		m_roles = l.toArray(new String[l.size()]);

		//-- Response format annotations.
		if(am.response() != ResponseFormat.UNDEFINED) // Is a response format defined?
			m_responseFormat = am.response(); // Then use it,
		else
			m_responseFormat = m_ServiceClassDefinition.getResponseFormat(); // Else default to class's spec
	}

	final public ResponseFormat getResponseFormat() {
		return m_responseFormat;
	}

	public String[] getRoles() {
		return m_roles;
	}

	boolean isStatic() {
		return m_static;
	}

	RpcClassDefinition getServiceClassDefinition() {
		return m_ServiceClassDefinition;
	}

	Method getMethod() {
		return m_method;
	}

	Class< ? > getOutputClass() {
		return m_outputClass;
	}

	@Override
	public String toString() {
		return m_method == null ? m_name : m_method.toGenericString();
	}
}
