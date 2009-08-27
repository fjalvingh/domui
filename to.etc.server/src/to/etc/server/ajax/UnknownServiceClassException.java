package to.etc.server.ajax;

import to.etc.server.servlet.*;

public class UnknownServiceClassException extends ServiceException {
	private String	m_requestedClass;

	public UnknownServiceClassException(RequestContext ctx, String missingClass) {
		super(ctx, "The service class '" + missingClass + "' cannot be found.");
		m_requestedClass = missingClass;
	}

	public UnknownServiceClassException(String missingClass) {
		super("The service class '" + missingClass + "' cannot be found.");
		m_requestedClass = missingClass;
	}

	public UnknownServiceClassException(String missingClass, Throwable cause) {
		super("The service class '" + missingClass + "' cannot be found.", cause);
		m_requestedClass = missingClass;
	}

	public String getRequestedClass() {
		return m_requestedClass;
	}
}
