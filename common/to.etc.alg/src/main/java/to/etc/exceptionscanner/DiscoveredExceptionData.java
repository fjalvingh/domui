package to.etc.exceptionscanner;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Date;

@NonNullByDefault
final public class DiscoveredExceptionData {
	private final Thread m_thread;

	private final Date m_when;

	private final String m_exception;

	private final String m_context;

	public DiscoveredExceptionData(Thread thread, Date when, String exception, String context) {
		m_thread = thread;
		m_when = when;
		m_exception = exception;
		m_context = context;
	}

	public Thread getThread() {
		return m_thread;
	}

	public Date getWhen() {
		return m_when;
	}

	public String getException() {
		return m_exception;
	}

	public String getContext() {
		return m_context;
	}
}
