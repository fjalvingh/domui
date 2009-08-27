package to.etc.nio.server;

public class ServerException extends Exception {
	private Severity m_severity;

	public ServerException(Severity s, String message, Throwable cause) {
		super(message, cause);
		m_severity = s;
	}

	public ServerException(Severity s, String message) {
		super(message);
		m_severity = s;
	}

	public ServerException(Severity s, Throwable cause) {
		super(cause);
		m_severity = s;
	}

	public Severity getSeverity() {
		return m_severity;
	}
}
