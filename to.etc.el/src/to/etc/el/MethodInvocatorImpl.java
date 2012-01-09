package to.etc.el;

import java.lang.reflect.*;

public class MethodInvocatorImpl implements MethodInvocator {
	private Object m_bean;

	private Method m_method;

	public MethodInvocatorImpl(Object b, Method m) {
		m_bean = b;
		m_method = m;
	}

	public Object getBean() {
		return m_bean;
	}

	public Method getMethod() {
		return m_method;
	}

	public Object invoke(Object[] par) throws Exception {
		try {
			return m_method.invoke(getBean(), par);
		} catch(InvocationTargetException x) {
			if(x.getCause() instanceof Exception)
				throw (Exception) x.getCause();
			throw x;
		}
	}
}
