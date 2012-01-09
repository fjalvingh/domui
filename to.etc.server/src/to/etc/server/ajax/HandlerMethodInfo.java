package to.etc.server.ajax;

import java.lang.reflect.*;
import java.util.*;

/**
 * Packs information on a callable method of a handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 5, 2006
 */
public class HandlerMethodInfo {
	private HandlerInfo		m_handlerInfo;

	private Method			m_method;

	private String			m_name;

	private Exception		m_exc;

	private String[]		m_roles;

	private boolean			m_static;

	private boolean			m_initialized;

	private ResponseFormat	m_responseFormat;

	/**
	 * If this method has a first parameter that specifies a supported output
	 * then that class is saved herein. This indicates that the method renders
	 * it's output by itself.
	 */
	private Class			m_outputClass;

	HandlerMethodInfo(HandlerInfo hi, String name) {
		m_name = name;
		m_handlerInfo = hi;
	}

	void initialize() throws Exception {
		if(m_exc != null)
			throw m_exc;
		if(m_initialized)
			return;
		try {
			m_method = findMethod(m_name);
			if(m_method == null)
				throw new UnknownServiceMethodException(m_handlerInfo.getHandlerClass(), m_name);
			checkReturnMethod();
			checkAnnotations(m_method);
		} catch(Exception x) {
			m_exc = x;
			throw x;
		}
	}

	private void checkReturnMethod() throws Exception {
		Class rv = m_method.getReturnType();
		if(rv != Void.TYPE) { // Any method with a return type uses the return type as result
			m_outputClass = null;
			return;
		}

		//-- Void method: the 1st parameter defines the method to render the output
		Class[] par = m_method.getParameterTypes();
		if(par.length == 0)
			throw new ServiceException("The method '" + m_method + "' returns void and does not have an output parameter; it cannot be called.");
		m_outputClass = par[0]; // Output parameter type.
	}

	private Method findMethod(String name) throws Exception {
		Class cl = m_handlerInfo.getHandlerClass();
		Method foundm = null;
		for(Method m : cl.getMethods()) {
			if(m.getName().equals(name)) {
				if(foundm != null)
					throw new UnknownServiceMethodException(m_handlerInfo.getHandlerClass(), m_name, "The method '" + name + "' occurs 2ce [" + foundm.toGenericString() + " and "
						+ m.toGenericString() + "]");
				foundm = m;
			}
		}
		return foundm;
	}

	private void checkAnnotations(Method m) throws Exception {
		m_static = Modifier.isStatic(m.getModifiers());

		//-- Check for the roles in the AjaxMethod annotation.
		AjaxMethod am = m.getAnnotation(AjaxMethod.class);
		List<String> l = new ArrayList<String>();
		if(am != null) {
			StringTokenizer st = new StringTokenizer(am.roles(), " \t,");
			while(st.hasMoreTokens()) {
				String s = st.nextToken().trim();
				if(s.length() > 0)
					l.add(s);
			}
		}
		m_roles = l.toArray(new String[l.size()]);

		//-- Response format annotations.
		if(am != null && am.response() != ResponseFormat.UNDEFINED) // Is a response format defined?
			m_responseFormat = am.response(); // Then use it,
		else
			m_responseFormat = m_handlerInfo.getResponseFormat(); // Else default to class's spec
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

	HandlerInfo getHandlerInfo() {
		return m_handlerInfo;
	}

	Method getMethod() {
		return m_method;
	}

	Class getOutputClass() {
		return m_outputClass;
	}

	@Override
	public String toString() {
		return m_method == null ? m_name : m_method.toGenericString();
	}
}
