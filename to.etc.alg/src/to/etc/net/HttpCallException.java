package to.etc.net;

public class HttpCallException extends Exception {
	private int		m_code;

	private String	m_url;

	private String	m_message;

	private String	m_errorStream;

	public HttpCallException(String url, int code, String message) {
		super(message);
		m_code = code;
		m_url = url;
		m_message = message;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("HTTP error ");
		sb.append(m_code);
		sb.append(": ");
		sb.append(m_message);
		sb.append(" on ");
		sb.append(m_url);
		return sb.toString();
	}

	public String getErrorStream() {
		return m_errorStream;
	}

	public void setErrorStream(String errorStream) {
		m_errorStream = errorStream;
	}
}
