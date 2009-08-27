package to.etc.server.servlet.cmd;

/**
 * Saved nonce and associated client data for SOAP
 * authentication.
 * 
 * Created on Feb 8, 2005
 * @author jal
 */
public class NonceData {
	/** The actual value for the nonce string */
	private String	m_nonce;

	/** The address the client used to get the nonce. */
	private String	m_client_addr;

	/** The nonce expiration timestamp */
	private long	m_ts_expired;

	public NonceData(String nonce, String ia, long tsexpired) {
		m_nonce = nonce;
		m_client_addr = ia;
		m_ts_expired = tsexpired;
	}


	/**
	 * @return Returns the client_addr.
	 */
	public String getClientAddr() {
		return m_client_addr;
	}

	/**
	 * @return Returns the nonce.
	 */
	public String getNonce() {
		return m_nonce;
	}

	/**
	 * @return Returns the ts_created.
	 */
	public long getTsExpired() {
		return m_ts_expired;
	}
}
