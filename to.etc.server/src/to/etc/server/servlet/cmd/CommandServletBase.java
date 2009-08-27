package to.etc.server.servlet.cmd;

import java.util.*;

import javax.servlet.http.*;

import to.etc.server.servlet.*;

/**
 * This is the base for a servlet which executes commands from
 * a command repository. It has initialization handlers and
 * termination handlers that can be registered and that get
 * called every time a request is executed.
 *
 * <p>The commands are stored as classes that contain methods
 * that have the pattern execXxxx(). These functions get
 * passed a context structure to obtain their data from.
 *
 * Created on Aug 23, 2005
 * 
 * @author jal
 */
abstract public class CommandServletBase extends ContextServletBase {
	/** The registry with commands for this command handler. */
	private CommandServletRegistry	m_registry;

	protected CommandServletBase() {
		super(false);
	}

	/* (non-Javadoc)
	 * @see to.mumble.server.misc.ContextServletBase#makeContext(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean)
	 */
	@Override
	abstract public ContextServletContext makeContext(HttpServletRequest req, HttpServletResponse res, boolean ispost);

	final public CommandServletRegistry getRegistry() {
		if(m_registry == null)
			throw new IllegalStateException("Command registry not set.");
		return m_registry;
	}

	protected void setRegistry(CommandServletRegistry r) {
		m_registry = r;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Authentication code.								*/
	/*--------------------------------------------------------------*/
	/** Allow a nonce to exist for this many seconds (one minute). */
	private static final int	NONCE_LIFETIME	= 60;

	/** The map of nonces key is nonce key */
	private Hashtable			m_nonce_table	= new Hashtable();

	private long				m_ts_check;

	/** When T only authenticated requests are allowed. When false we accept authentication if supplied but we do not require it. */
	private boolean				m_mustAuthenticate;

	public final void setAuthenticationRequired(boolean ar) {
		m_mustAuthenticate = ar;
	}

	public final boolean isAuthenticationRequired() {
		return m_mustAuthenticate;
	}

	private synchronized void checkTable() {
		long cts = System.currentTimeMillis();
		if(m_ts_check > cts)
			return;

		//-- Walk the table and delete all entries that are too old
		for(Iterator it = m_nonce_table.values().iterator(); it.hasNext();) {
			NonceData nd = (NonceData) it.next();
			if(nd.getTsExpired() < cts)
				it.remove();
		}
		long iv = m_nonce_table.size() > 100 ? 2000 : 10000;
		m_ts_check = cts + iv;
	}

	/**
	 * Creates a new nonce data structure and saves it in the valid
	 * nonces hash.
	 * @return
	 */
	protected NonceData makeNonce(HttpServletRequest req) {
		//-- Create a random nonce
		long ra = (long) (Math.random() * Long.MAX_VALUE); // Calculate a long random number
		long ts = System.currentTimeMillis();
		String key = Long.toHexString(ra) + Long.toHexString(ts);
		while(key.length() < 32) {
			ra = (long) (Math.random() * Long.MAX_VALUE); // Calculate a long random number
			key = key + Long.toHexString(ra);
		}
		if(key.length() > 32)
			key = key.substring(0, 32);

		long ets = System.currentTimeMillis() + NONCE_LIFETIME * 1000;
		NonceData nd = new NonceData(key, req.getRemoteAddr(), ets);
		synchronized(this) {
			m_nonce_table.put(key, nd);
			checkTable();
		}
		return nd;
	}

	/**
	 * Gets the specified nonce's data structures. This can be called 
	 * only once: the nonce is removed by the call.
	 * @param nonce
	 * @return
	 */
	protected synchronized NonceData findNonce(String nonce) {
		checkTable();
		NonceData nd = (NonceData) m_nonce_table.remove(nonce);
		if(nd != null) {
			if(nd.getTsExpired() > System.currentTimeMillis())
				return nd;
		}
		return null;
	}

	/**
	 * FIXME Need cached shit from database.
	 * @param ident
	 * @return
	 */
	protected String findIdentPassword(String ident) throws Exception {
		return "hello";
	}
}
