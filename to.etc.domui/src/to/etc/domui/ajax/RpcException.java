package to.etc.domui.ajax;

public class RpcException extends RuntimeException {
	/** The incoming request URL, if applicable. */
	private String m_url;

	/** The request type */
	private String m_method;

	/** The query string if this was a get */
	private String m_queryString;

	/** The remote address for this thingy. */
	private String m_remoteAddress;

	private final String m_message;

	public RpcException(final String message) {
		m_message = message;
	}

	public RpcException(final Throwable t, final String message) {
		super(t);
		m_message = message;
	}

	@Override
	public String getMessage() {
		return m_message;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(final String url) {
		m_url = url;
	}

	public String getMethod() {
		return m_method;
	}

	public void setMethod(final String method) {
		m_method = method;
	}

	public String getQueryString() {
		return m_queryString;
	}

	public void setQueryString(final String queryString) {
		m_queryString = queryString;
	}

	public String getRemoteAddress() {
		return m_remoteAddress;
	}

	public void setRemoteAddress(final String remoteAddress) {
		m_remoteAddress = remoteAddress;
	}
}
