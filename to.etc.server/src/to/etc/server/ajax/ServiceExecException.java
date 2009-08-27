package to.etc.server.ajax;

import java.lang.reflect.*;

/**
 * Any kind of service execution error on a defined service
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2006
 */
public class ServiceExecException extends ServiceException {
	private Class	m_serviceClass;

	private Method	m_serviceMethod;

	public ServiceExecException(Throwable t, Method m, String message) {
		super(message, t);
		m_serviceMethod = m;
		m_serviceClass = m.getDeclaringClass();
	}

	public ServiceExecException(Throwable t, Class c, String message) {
		super(message, t);
		m_serviceClass = c;
	}

	public ServiceExecException(Method m, String message) {
		super(message);
		m_serviceMethod = m;
		m_serviceClass = m.getDeclaringClass();
	}

	public ServiceExecException(Class c, String message) {
		super(message);
		m_serviceClass = c;
	}

	public Class getServiceClass() {
		return m_serviceClass;
	}

	public Method getServiceMethod() {
		return m_serviceMethod;
	}
}
