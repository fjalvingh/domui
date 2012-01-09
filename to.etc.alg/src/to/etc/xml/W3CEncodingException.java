package to.etc.xml;

public class W3CEncodingException extends RuntimeException {
	private String	m_reason;

	private String	m_location;

	private String	m_value;

	public W3CEncodingException(String msg) {
		m_reason = msg;
	}

	public W3CEncodingException(String msg, String value) {
		m_reason = msg;
		m_value = value;
	}

	public W3CEncodingException() {
		super("Invalid value");
	}

	public String getLocation() {
		return m_location;
	}

	public W3CEncodingException setLocation(String location) {
		m_location = location;
		return this;
	}

	public String getReason() {
		return m_reason;
	}

	public W3CEncodingException setReason(String reason) {
		m_reason = reason;
		return this;
	}

	public String getValue() {
		return m_value;
	}

	public W3CEncodingException setValue(String value) {
		m_value = value;
		return this;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(m_reason);
		if(m_value != null) {
			sb.append(" value='");
			sb.append(m_value);
			sb.append("'");
		}
		if(m_location != null) {
			sb.append(" location=");
			sb.append(m_location);
		}
		return sb.toString();
	}
}
