package to.etc.server.ajax;

import java.util.*;

public class HandlerInfo {
	private Class							m_handlerClass;

	private boolean							m_initialized;

	private Exception						m_errorException;

	private String[]						m_roles;

	private ResponseFormat					m_responseFormat;

	private Map<String, HandlerMethodInfo>	m_methodMap	= new HashMap<String, HandlerMethodInfo>();

	public HandlerInfo(Class cl) {
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
			throw new AjaxHandlerException("The class " + m_handlerClass.getCanonicalName() + " is not annotated with @AjaxHandler");
		AjaxHandler ah = (AjaxHandler) m_handlerClass.getAnnotation(AjaxHandler.class);
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

	public Class getHandlerClass() {
		return m_handlerClass;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Method interface.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	public synchronized HandlerMethodInfo getMethod(String name) throws Exception {
		HandlerMethodInfo mi = m_methodMap.get(name);
		if(mi == null) {
			mi = new HandlerMethodInfo(this, name);
			m_methodMap.put(name, mi);
		}
		mi.initialize();
		return mi;
	}

	final public ResponseFormat getResponseFormat() {
		return m_responseFormat;
	}
}
