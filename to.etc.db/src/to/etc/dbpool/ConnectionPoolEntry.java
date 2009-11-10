package to.etc.dbpool;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;


/**
 * <p>This represents an actual connection being pooled. This contains the
 * connection and a list of resources allocated from that connection
 * (statements). When this connection is reached out to a using class it will
 * create a proxy class (PooledConnection) which refers to this class for all
 * the work. Using this proxy allows us to invalidate the proxy without having
 * to invalidate the actual entry. Invalidating an entry merely means that we
 * clear the proxy's reference in this class. Since all entrypoints check whether
 * the proxy is still the "current" proxy this causes all calls made thru the
 * proxy to fail when it is invalidated.
 *
 * <h2>Tracked resource management</h2>
 * <p>This part is difficult because we need to prevent deadlocks and resource
 * loss. All calls that allocate tracked resources are made to the entry. When
 * allocating the resource the entry stays locked. The code is written in such
 * a way that never more than 1 resource is locked at one time.
 *
 * Each of the allocation proxies get called by the PooledConnection's proxies only. It
 * must check whether the pooledconnection is still valid and allocate the
 * appropriate resource. Then the resource is to be added to the entry's
 * resource list BEFORE it gets unlocked.
 * <p>The base mechanism is as follows:
 * 	<ul><li>First we only lock the PoolEntry.
 * 		<li>Then we check if the pooledconnection is still valid (current)
 *      	for this poolentry
 *      <li>Now we allocate the appropriate resource AND we add it to the
 *      	used resource list.
 *    	<li>At this point the context for the entire ConnectionPoolEntry
 *      	is valid and we UNLOCK the poolentry.
 *      <li>Finally we increment the "open statement" count on the pool. This
 *      	MUST be done out of the lock on the poolentry to prevent deadlock
 *        	(the lock route [PoolEntry, Pool] used will deadlock with the
 *          lock route [Pool, PoolEntry] in the scanner processes).
 *	</ul>
 *
 * This mechanism ensures that resources are allocated and registered in an atomic
 * operation as far as the PoolEntry is concerned. The only non-atomic part is
 * the incrementing of the pool's current open statement count. This means that
 * this counter may trail behind the actual #of statements. Since this is a
 * statistic counter only AND because preventing this would mean we had to lock
 * the pool for every action this seems a small price to pay.
 *
 *
 */
public class ConnectionPoolEntry {
	static protected final String connClosed = "CLOSED";

	static protected final String connInvalidated = "INVALIDATED";

	/**
	 * TRUE if this is an unpooled connection.
	 */
	private boolean m_is_unpooled;

	/** T while this is used as a thread-cached connection. */
	private boolean m_isthread;

	//	/** The thread that's currently owning this entry */
	//	private Thread				m_owning_thread;

	/** This-entry's number for display pps only. */
	private final int m_idnr;

	/** An exception denoting the (last) allocation point for this connection. */
	private Exception m_allocationPoint;

	/** The actual (real) database connection; null if closed. */
	protected Connection m_cx;

	/** The pool this connection is obtained from, */
	protected ConnectionPool m_pool;

	protected boolean m_trace;

	/** The list of stack traces for this connection, if enabled... */
	private final String[] m_stl_ar = new String[40];

	/** The connection proxy that currently references this connection. */
	private PooledConnection m_proxy_dbc;

	/** Connection hang protection: when was the connection last scanned. */
	private long m_scan_ts;

	/** This entry's state. Can be CLOSED or INVALIDATED or null if OKAY. */
	private String m_state;

	/** The time that the connection was allocated in CONNTIME is true, empty otherwise */
	private long m_ts_alloc;

	/** The last time the connection was used (proxyCheck). */
	private long m_ts_lastuse;

	/** The #of times a warning has been sent that this connection is inactive. */
	private int m_n_warncount;

	ConnectionPoolEntry(final Connection cx, final ConnectionPool pool, final int idnr) throws SQLException {
		m_pool = pool;
		m_cx = cx;
		m_idnr = idnr;
	}

	public int getID() {
		return m_idnr;
	}

	void setThreadCached(final boolean tc) {
		m_isthread = tc;
	}

	boolean isThreadCached() {
		return m_isthread;
	}

	protected void setUnpooled(final boolean unpooled) {
		m_is_unpooled = unpooled;
	}

	protected boolean isUnpooled() {
		return m_is_unpooled;
	}

	protected synchronized long getAllocationTime() {
		return m_ts_alloc;
	}

	protected synchronized long getLastUsedTime() {
		return m_ts_lastuse;
	}

	int getWarningCount() {
		return m_n_warncount;
	}

	void setWarningCount(final int c) {
		m_n_warncount = c;
	}

	/**
	 *	Returns a string describing the connection.
	 */
	@Override
	public String toString() {
		return getDesc();
		//		return "PoolEntry["+m_idnr+"] of "+m_pool.getID();
	}

	public synchronized String getDesc() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("Poolentry[");
		sb.append(m_pool.toString());
		sb.append(',');
		sb.append(m_idnr);
		sb.append("] type=");
		sb.append(m_is_unpooled ? "unpooled" : "pooled");
		if(m_isthread)
			sb.append(" thread");
		if(m_proxy_dbc == null)
			sb.append(", without proxy.");
		else {
			Thread t = m_proxy_dbc.getOwnerThread();
			if(t == null)
				sb.append(", unowned");
			else {
				sb.append(", owned by ");
				sb.append(t.getName());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the real connection for this entry. DO NOT USE!
	 */
	synchronized protected Connection getConnection() {
		return m_cx;
	}

	public final ConnectionPool getPool() {
		return m_pool;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Connection proxy code...							*/
	/*--------------------------------------------------------------*/
	/**
	 * Creates a new connection proxy and links it to this class. All state
	 * data in the proxy is locked by it's instance of ConnectionPoolEntry.
	 */
	PooledConnection proxyMake() {
		int id = PoolManager.nextConnID();
		Exception loc;
		try {
			throw new Exception("Connection was allocated");
		} catch(Exception x) {
			loc = x;
		}

		synchronized(this) {
			m_ts_alloc = System.currentTimeMillis();
			m_ts_lastuse = m_ts_alloc;
			if(m_proxy_dbc != null) {
				ConnectionPool.MSG.info("Attempted to create 2nd proxy?");
				throw new IllegalStateException("Attempt to create 2nd proxy for used connection");
			}
			m_proxy_dbc = new PooledConnection(this, id);
			Thread t = Thread.currentThread();
			m_proxy_dbc.setOwnerThread(t);
			//			m_owning_thread	= t;
			m_allocationPoint = loc;
		}
		if(m_pool.dbgIsStackTraceEnabled())
			dbgSaveLocation(m_proxy_dbc, true); // Save stack for this new proxy to get INIT allocation
		return m_proxy_dbc;
	}

	/**
	 * Called when the connection proxy's close() method gets called. If the
	 * connection was already closed the close is ignored. If the connection
	 * was invalidated then a log message is written. If this is actually a
	 * valid close then - well - we close.
	 * @param pc		the proxy that was closed
	 */
	protected void proxyClosed(final PooledConnection pc) throws SQLException {
		if(m_pool.dbgIsStackTraceEnabled())
			dbgSaveLocation(pc); // Save call context if tracing

		//-- Prepare for timeout check
		int ctw = m_pool.getConnectionUsedTooLongWarningTimeout();
		long ts = 0;
		if(ctw != 0)
			ts = System.currentTimeMillis();

		boolean wasinvalid = false;
		synchronized(this) {
			if(pc.m_detach_reason == connClosed) // Already closed?
				return; // Then ignore
			if(pc.m_detach_reason == connInvalidated)
				wasinvalid = true;
			else {
				//-- This is a real close! Swap state atomically,
				if(pc != m_proxy_dbc) {
					StringBuilder sb = new StringBuilder(8192);
					sb.append("Current location:\n");
					DbPoolUtil.getThreadAndLocation(sb);
					sb.append("\nEntry stack\n");
					dbgPrintStackTrace(sb, 20, 20);

					PoolManager.panic("DB Proxy closed but entry is not owning it??", sb.toString());
					throw new IllegalStateException("kickjal: valid proxy close but entry's not owning it??");
				}
				m_proxy_dbc = null; // No longer my proxy,
				pc.m_detach_reason = connClosed; // Flag proxy as closed,
				pc.m_detach_location = DbPoolUtil.getLocation();
			}
			m_scan_ts = 0; // Reset: scan final.
			if(ctw != 0) // Need to calculate time used?
				ts -= m_ts_alloc; // Get #ms used,
		}

		//-- If this was an invalidated connection then log a message that it finally closed,
		if(wasinvalid) {
			PoolManager.logUnexpected("Invalidated connection was FINALLY closed: " + this.toString());
			return;
		}
		if(ctw != 0 && !isUnpooled())
			m_pool.handleConnectionUsageTime(this, ts);

		//-- Normal close. Remove all saved resources, MUST BE OUT OF LOCK!!
		closeResources(); // Close all tracked resources
		synchronized(this) // Return connection to pool
		{
			if(m_proxy_dbc != null)
				throw new IllegalStateException("kickjal: proxy still active??");
		}
		m_pool.returnToPool(this, pc);
	}

	/**
	 * This gets called from the proxy for every call to see if the connection
	 * is still valid. If not this throws an exception specifying that the connection
	 * was either closed or invalidated. If the connection is still valid it
	 * returns self to get the connection from.
	 * @param pc		the proxy to check
	 * @return			the database connection that can be used by the proxy.
	 */
	protected Connection proxyCheck(final PooledConnection pc, final boolean savestack) {
		RuntimeException x = null;
		StringBuilder sb = null;
		synchronized(this) {
			if(pc.m_detach_reason != null) {
				sb = new StringBuilder(8192);
				sb.append("in proxyCheck: connection was closed: " + pc.m_detach_reason);
				sb.append("\nThe close-location is:\n");
				sb.append(pc.m_detach_location == null ? "Unknown" : pc.m_detach_location);
				sb.append("\nThe connection stack is:\n");

				dbgPrintStackTrace(sb, 40, 15);
				x = new IllegalStateException(pc.toString() + ": connection was " + pc.m_detach_reason);
			} else if(pc != m_proxy_dbc) {
				sb = new StringBuilder(8192);
				sb.append("in proxyCheck: proxy refers to entry that's currently in use by someone else.\n");
				x = new IllegalStateException(pc + ": valid proxy checked but entry's not owning it");
			}
			m_ts_lastuse = System.currentTimeMillis();
		}
		if(x != null) {
			if(sb != null)
				getPool().getManager().sendPanic("The connection was found closed!?", sb.toString());
			throw x;
		}

		if(savestack && m_pool.dbgIsStackTraceEnabled())
			dbgSaveLocation(pc);
		return m_cx;
	}

	/**
	 * Called from the proxy when a resource (statement, resultset or whatever)
	 * is closed. It checks if the resource is still valid and only does all
	 * when the resource is ok. If called when the connection was invalidated
	 * this throws an exception- the original resource was freed when the
	 * connection was invalidated.
	 *
	 * @param pc		the proxy
	 * @param r			the resource that was closed
	 */
	protected void proxyRemoveResource(final PooledConnection pc, final Object r) {
		removeResource(pc, r);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Timeout code...										*/
	/*--------------------------------------------------------------*/
	/**
	 * Called to check if this entry has timed out. It flags a time each scan
	 * and invalidates the entry if the time's expired. This only invalidates
	 * the proxy and the entry; the called MUST remove the entry from the
	 * used list AND must perform cleanup by calling invalidateCleanup()
	 * sometimes.
	 * <p>MAY ONLY BE CALLED FROM THE SCAN CODE WITHOUT A LOCKED POOL</p>
	 */
	protected synchronized boolean checkTimeOut(final long currts, final long maxage, boolean removeontimeout) {
		//-- Is this connection still in use?
		if(m_state != null)
			return false; // Already closed/invalidated
		if(m_proxy_dbc == null)
			return false; // Was closed / is closing.

		//-- Was this connection marked already?
		if(m_scan_ts == 0) // Already found in a scan?
		{
			m_scan_ts = currts; // No: it's found now ;-)
			return false; // But it hasn't timed out.
		}

		//-- Is the connection OLDER than the min time?
		if(m_scan_ts > maxage)
			return false; // Not older: still not expired.

		//-- This connection has timed out. Invalidate!
		if(removeontimeout) {
			m_pool.invalidateForTimeout(this); // Remove me from the pool!
			m_state = connInvalidated;
			m_proxy_dbc.m_detach_reason = connInvalidated;
			m_proxy_dbc = null; // Disconnect proxy.
		}
		return true;
	}

	static private long[] WARNINT = {5 * 60 * 1000, // 5 minutes
		15 * 60 * 1000, // 15 minutes
		60 * 60 * 1000, // 1 hour
		2 * 60 * 60 * 1000, // 2 hours
		8 * 60 * 60 * 1000};


	/**
	 * Checks to see if it's time to report a warning for this unpooled
	 * connection..
	 * @param sb
	 * @param nowts
	 * @return
	 */
	synchronized void checkUnpooledUnused(final StringBuilder sb, final long nowts) {
		//-- Is this connection still in use?
		if(m_state != null)
			return; // Already closed/invalidated
		if(m_proxy_dbc == null)
			return; // Was closed / is closing.

		long dts = m_n_warncount < WARNINT.length ? WARNINT[m_n_warncount] : (m_n_warncount - WARNINT.length + 1) * 24 * 60 * 60 * 1000;
		if(m_ts_lastuse + dts > nowts) // Warning time not expired?
			return;

		sb.append("Unpooled connection " + this + " unused very long: last used on " + new Date(m_ts_lastuse) + "\n");
		sb.append("The connection was allocated on ");
		sb.append(new Date(m_ts_alloc));
		sb.append("; the allocation point is ");
		DbPoolUtil.getFilteredStacktrace(sb, m_allocationPoint);
		sb.append("\n");
		m_n_warncount++;
	}

	/**
	 *	Sets the connection into TRACE mode.
	 */
	public void setTrace(final boolean on) throws java.sql.SQLException {
		if(on == m_trace)
			return;

		//-- Set/reset TRACE mode.
		PreparedStatement ps = null;
		try {
			ps = m_cx.prepareStatement("alter session set sql_trace = " + (on ? "true" : "false"));
			ps.executeUpdate();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Debug stuff: stack trace info..						*/
	/*--------------------------------------------------------------*/
	/**
	 *	Saves the current stack trace in the stack trace list.
	 */
	private void dbgSaveLocation(final PooledConnection pc) {
		dbgSaveLocation(pc, false);
	}

	/**
	 * Saves the current stack trace in the stack trace list.
	 * @param pc			the connection being traced
	 * @param clearstack	If T the stack is cleared before the new entry is added
	 */
	private void dbgSaveLocation(final PooledConnection pc, final boolean clearstack) {
		//-- Ok: get a stack trace now...
		String s;
		try {
			throw new Exception("Dummy");
		} catch(Exception x) {
			StringWriter sw = new StringWriter(2048);
			PrintWriter pw = new PrintWriter(sw);
			if(pc != null) {
				pw.println(pc.toString()); // Save proxy name,
				if(m_ts_alloc != 0) // Is stamping time?
				{
					long dt = System.currentTimeMillis() - m_ts_alloc;
					pw.println("Time: " + dt + " ms after connection was allocated");
				}

				if(pc.m_owner_info != null) {
					pw.print("Associated owner context: ");
					pw.print(pc.m_owner_info.getDbConnectionOwnerInformation());
					pw.println(".");
				}
			}

			x.printStackTrace(pw);
			pw.close();
			s = sw.getBuffer().toString();
		}

		//-- Now add to the queue (ahead)
		synchronized(m_stl_ar) {
			if(clearstack) {
				for(int i = m_stl_ar.length; --i >= 1;)
					m_stl_ar[i] = null;
			} else {
				for(int i = m_stl_ar.length; --i > 0;)
					m_stl_ar[i] = m_stl_ar[i - 1];
			}
			m_stl_ar[0] = s;
		}
	}


	/**
	 *	Returns the stack trace array for this connection. It returns null
	 *  if no stack trace exists.
	 */
	public String[] dbgGetTrace() {
		String[] ar = new String[m_stl_ar.length];
		synchronized(m_stl_ar) {
			System.arraycopy(m_stl_ar, 0, ar, 0, m_stl_ar.length);
			return ar;
		}
	}


	/**
	 * This takes a dbpool stacktrace and "filters" out lines. The lines that have
	 * to do with the dumping process (dbgSaveLocation and the like) are removed,
	 * and if maxlines is > 0 only the #of lines specified is dumped. It also
	 * removes the "dummy" exception line. While each line gets copied this also
	 * checks if the line contains a generic servlet request line; if so all after
	 * that gets skipped too.
	 * @param sb
	 * @param trace
	 * @param maxlines
	 */
	static public void filterStackTrace(final StringBuilder sb, final String trace, int maxlines) {
		if(maxlines <= 0)
			maxlines = Integer.MAX_VALUE;
		int ix = 0;
		int len = trace.length();
		while(ix < len) {
			if(maxlines-- <= 0) {
				sb.append("\t(rest skipped)\n");
				return;
			}

			//-- Get a new line,
			int spos = ix;
			int le = trace.indexOf('\n', ix); // Find end of line
			if(le == -1)
				le = len; // Handle end,
			ix = le + 1;

			//-- Remove whitespace from that line,
			while(spos < le && Character.isWhitespace(trace.charAt(spos)))
				spos++;
			if(spos + 3 < le) {
				//-- Does this line contain "at"?
				if(trace.charAt(spos) == 'a' && trace.charAt(spos + 1) == 't')
					spos += 2;

				//-- Remove all ws after at to get to the actual location
				while(spos < le && Character.isWhitespace(trace.charAt(spos)))
					spos++;
			}

			String line = trace.substring(spos, le);

			//-- Filter?
			if(line.indexOf("java.lang.Exception") == -1 || line.indexOf("Dummy") == -1) {
				//-- Not the starting line. Is it an ending line?
				if(line.indexOf("javax.servlet.http.HttpServlet.service") != -1) {
					sb.append("\t(from servlet-runner)\n");
					return;
				}

				sb.append(line);
				sb.append('\n');
			}
		}
	}

	/**
	 * Dumps stacktraces for this entry. This filters data from the
	 * stacktrace to keep it's size as small as possible, and allows
	 * one to limit the #lines per stack trace.
	 * @param sb
	 * @param maxlines The max #of lines per trace; if 0 dumps all lines.
	 * @param maxtraces The max #of traces to dump. 0 or -1 dumps all traces.
	 */
	public void dbgPrintStackTrace(final StringBuilder sb, final int maxlines, int maxtraces) {
		if(maxtraces <= 0)
			maxtraces = Integer.MAX_VALUE;
		String[] ar = dbgGetTrace();
		for(int i = 0; i < ar.length; i++) {
			if(ar[i] != null) {
				if(maxtraces-- <= 0) {
					sb.append("(rest of traces skipped)\n");
					return;
				}
				sb.append("\n******* Stack entry ");
				sb.append(Integer.toString(i));
				sb.append(" *********\n");
				filterStackTrace(sb, ar[i], maxlines);
				//				sb.append(ar[i]);
			}
		}
	}

	/**
	 *	Clears the current stack trace.
	 */
	//	private void dbgClearStackTrace()
	//	{
	//		synchronized(m_stl_ar)
	//		{
	//			Arrays.fill(m_stl_ar, null);
	//		}
	//	}

	/**
	 * Called when the pool closes. Invalidates and closes immediately.
	 */
	protected void closeRealConnection() {
		Connection dbc;
		synchronized(this) {
			closeResources(); // Closes all resources and ignores any that goes bad. Also clears all counts(!)
			if(m_proxy_dbc != null) // Active connection?
			{
				m_proxy_dbc.m_detach_reason = connInvalidated;
				m_proxy_dbc.m_detach_location = DbPoolUtil.getLocation();
				m_proxy_dbc = null;
				m_state = connInvalidated;
			} else
				m_state = connClosed;
			dbc = m_cx;
			m_cx = null;
		}

		try {
			if(dbc != null)
				dbc.close();
		} catch(Exception x) {}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Connection resource management...					*/
	/*--------------------------------------------------------------*/
	/** All objects allocated FROM this connection. */
	private final HashSet<Object> m_use_set = new HashSet<Object>();

	/**
	 *	Adds a resource to this statement's tracking list, so that it will be
	 *  released when the time comes.
	 */
	synchronized void addResource(final Object o) {
		m_use_set.add(o); // Add, do not allow duplicates.
	}


	/**
	 *	Removes a tracked resource, for instance when it was closed proper. MUST
	 *  BE CALLED WITHOUT LOCK TO PREVENT DEADLOCK ON POOL OBJECT!
	 */
	void removeResource(final PooledConnection pc, final Object o) {
		synchronized(this) {
			proxyCheck(pc, false); // Still the active proxy?
			if(!m_use_set.remove(o)) // Remove from set,
				throw new RuntimeException("Removing element without it ever being added");
			//			System.out.println("        ....closing resource "+o);
		}

		//-- Ok: decrement statement count OUT OF LOCK
		if(o instanceof ResultSetProxy)
			m_pool.decOpenRS();
		else
			m_pool.decOpenStmt();
	}

	/**
	 *	Called by close() to close all tracked resources. MUST BE CALLED WITHOUT
	 *  LOCK!!!!
	 */
	private void closeResources() {
		int nclosed = 0;

		synchronized(this) // Remove and close all resources while locked,
		{
			if(!m_pool.isIgnoreUnclosed()) {
				for(Object o : m_use_set) {
					_closeResource(o);
					nclosed++;
				}
			}
			m_use_set.clear();
		}

		//-- Now decrement statement count out of lock.
		m_pool.decOpenStmt(nclosed);
	}

	/**
	 *	Closes a specified resource and IGNORES all exceptions there... CALLED
	 * WITH A LOCKED THIS SO CANNOT LOCK ANYTHING ELSE
	 */
	private void _closeResource(final Object o) {
		boolean ok = true;

		try {
			if(o instanceof StatementProxy)
				((StatementProxy) o).closedByConnection();
			else if(o instanceof PreparedStatementProxy)
				((PreparedStatementProxy) o).closedByConnection();
			else if(o instanceof CallableStatementProxy)
				((CallableStatementProxy) o).closedByConnection();
			else if(o instanceof ResultSetProxy)
				((ResultSetProxy) o).closedByConnection();
			else
				ok = false;
		} catch(Throwable x) // ! Ignore all errors.
		{
			x.printStackTrace();
		}
		if(!ok)
			throw new RuntimeException("Unknown element added to tracked database resources?");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Tracked resource allocation routines..				*/
	/*--------------------------------------------------------------*/
	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true); // Check if proxy valid
			try {
				pc.collector().prepareStatement(p1);
				ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1), p1);
			} finally {
				pc.collector().prepareStatementEnd(p1, ps);
			}
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1, final int p2, final int p3) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1, p2, p3), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1, final int[] p2) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1, p2), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1, final int p2, final int p3, final int p4) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1, p2, p3, p4), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1, final int p2) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1, p2), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final PooledConnection pc, final java.lang.String p1, final String[] ar) throws java.sql.SQLException {
		PreparedStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new PreparedStatementProxy(pc, dbc.prepareStatement(p1, ar), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final PooledConnection pc) throws java.sql.SQLException {
		StatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new StatementProxy(pc, dbc.createStatement(), "(unset)");
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final PooledConnection pc, final int p1, final int p2) throws java.sql.SQLException {
		StatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new StatementProxy(pc, dbc.createStatement(p1, p2), "(unset)");
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final PooledConnection pc, final String a, final String[] b) throws java.sql.SQLException {
		StatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new StatementProxy(pc, dbc.prepareStatement(a, b), a);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final PooledConnection pc, final int p1, final int p2, final int p3) throws java.sql.SQLException {
		StatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new StatementProxy(pc, dbc.createStatement(p1, p2, p3), "(unset)");
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final PooledConnection pc, final java.lang.String p1, final int p2, final int p3) throws java.sql.SQLException {
		CallableStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new CallableStatementProxy(pc, dbc.prepareCall(p1, p2, p3), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final PooledConnection pc, final String p1, final int p2, final int p3, final int p4) throws java.sql.SQLException {
		CallableStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new CallableStatementProxy(pc, dbc.prepareCall(p1, p2, p3, p4), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final PooledConnection pc, final String p1) throws java.sql.SQLException {
		CallableStatementProxy ps = null;
		synchronized(this) {
			Connection dbc = proxyCheck(pc, true);
			ps = new CallableStatementProxy(pc, dbc.prepareCall(p1), p1);
			addResource(ps);
		}
		m_pool.incOpenStmt();
		return ps;
	}
}
