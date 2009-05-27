package to.etc.webapp.ajax.renderer;

import java.lang.reflect.*;

import to.etc.webapp.core.*;

public class RenderMethodException extends ServiceException {
	private final Method m_calledMethod;

	public RenderMethodException(final Method m, final String message, final Throwable cause) {
		super(message, cause);
		m_calledMethod = m;
	}

	public RenderMethodException(final Method m, final String message) {
		super(message);
		m_calledMethod = m;
	}

	public Method getCalledMethod() {
		return m_calledMethod;
	}
}
