package to.etc.server.ajax.renderer;

import java.lang.reflect.*;

import to.etc.server.ajax.*;

public class RenderMethodException extends ServiceException {
	private Method	m_calledMethod;

	public RenderMethodException(Method m, String message, Throwable cause) {
		super(message, cause);
		m_calledMethod = m;
	}

	public RenderMethodException(Method m, String message) {
		super(message);
		m_calledMethod = m;
	}

	public Method getCalledMethod() {
		return m_calledMethod;
	}
}
