package to.etc.dbpool;

import java.io.*;
import java.sql.*;
import java.util.*;

import to.etc.dbpool.stats.*;
import to.etc.dbutil.*;

/**
 * Handles database connections.
 * Created on Oct 17, 2003
 * @author jal
 */
public class PoolManager {
	/** The table of pools (ConnectionPool), identified by ID */
	private final Hashtable m_pool_ht = new Hashtable();

	private final ArrayList m_listeners = new ArrayList();

	static private Object m_connidlock = new Object();

	static private int m_nextconnid;

	private boolean m_collectStatistics;

	/**
	 * Threadlocal containing the per-thread collected statistics, per request.
	 */
	private final ThreadLocal m_threadStats = new ThreadLocal();

	/*--------------------------------------------------------------*/
	/*	CODING:	Static entries to the pool manager (obsolete).		*/
	/*--------------------------------------------------------------*/
	/** The shared global instance of a pool manager */
	static private PoolManager m_instance = new PoolManager();

	static int nextConnID() {
		synchronized(m_connidlock) {
			return ++m_nextconnid;
		}
	}

	static public PoolManager getInstance() {
		return m_instance;
	}

	static public void panic(final String shortdesc, final String body) {
		getInstance().sendPanic(shortdesc, body);
	}

	static public void logUnexpected(final String s) {
		getInstance().sendLogUnexpected(null, s);
	}

	static public void logUnexpected(final Exception t, final String s) {
		getInstance().sendLogUnexpected(t, s);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Database connection pool handler.					*/
	/*--------------------------------------------------------------*/
	public void addMessageListener(final iPoolMessageHandler pmh) {
		if(!m_listeners.contains(pmh))
			m_listeners.add(pmh);
	}

	public void removeMessageListener(final iPoolMessageHandler pmh) {
		m_listeners.remove(pmh);
	}

	public void sendLogUnexpected(final Exception t, final String s) {
		for(int i = m_listeners.size(); --i >= 0;) {
			try {
				((iPoolMessageHandler) m_listeners.get(i)).sendLogUnexpected(t, s);
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendPanic(final String shortdesc, final String body) {
		for(int i = m_listeners.size(); --i >= 0;) {
			try {
				((iPoolMessageHandler) m_listeners.get(i)).sendPanic(shortdesc, body);
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public int getPoolCount() {
		return m_pool_ht.size();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Defining and initializing pools.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Defines the pool with the specified ID from the ConfigSource passed.
	 * The pool is defined but NOT initialized. The same pool can be
	 * defined more than once provided it's parameters are all the same. After
	 * this call the pool can be used but it operates in "unpooled" mode, meaning
	 * that all allocated connections will be discarded after a close.
	 */
	public ConnectionPool definePool(final PoolConfigSource cs, final String id) throws SQLException {
		//-- Create a new pool structure,
		ConnectionPool newpool = new ConnectionPool(this, id, cs);
		synchronized(m_pool_ht) {
			//-- If this pool is already there then compare else add the new one
			ConnectionPool cp = (ConnectionPool) m_pool_ht.get(id);
			if(cp != null) // Pool exists.. Must have same parameters,
			{
				if(!cp.equals(newpool))
					throw new SQLException("PoolManager: database pool with duplicate id " + id + " is being defined with DIFFERENT parameters as the original.");
				return cp; // Exists and the same: return original pool
			}
			m_pool_ht.put(id, newpool); // And define it,
			return newpool;
		}
	}

	public ConnectionPool definePool(final String id, final String driver, final String url, final String userid, final String password, final String driverpath) throws SQLException {
		ConnectionPool newpool = new ConnectionPool(this, id, driver, url, userid, password, driverpath);
		synchronized(m_pool_ht) {
			//-- If this pool is already there then compare else add the new one
			ConnectionPool cp = (ConnectionPool) m_pool_ht.get(id);
			if(cp != null) // Pool exists.. Must have same parameters,
			{
				if(!cp.equals(newpool))
					throw new SQLException("PoolManager: database pool with duplicate id " + id + " is being defined with DIFFERENT parameters as the original.");
				return cp; // Exists and the same: return original pool
			}
			m_pool_ht.put(id, newpool); // And define it,
			return newpool;
		}
	}

	/**
	 * This defines a pool, taking it's config from a properties file.
	 *
	 * @param poolfile
	 * @param id
	 * @throws Exception
	 */
	public ConnectionPool definePool(final File poolfile, final String id) throws Exception {
		PoolConfigSource cs = PoolConfigSource.create(poolfile);
		return definePool(cs, id);
	}

	/**
	 * This defines a pool using the default poolfile ".dbpool.properties" stored
	 * in the user's home directory. If no such file is found then an exception
	 * is thrown.
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public ConnectionPool definePool(final String id) throws Exception {
		String uh = System.getProperty("user.home");
		if(uh != null) {
			File pf = new File(uh, ".dbpool.properties");
			if(pf.exists()) {
				return definePool(pf, id);
			}
		}
		throw new IllegalStateException("No default '.dbpool.properties' file found in your home directory (" + uh + ")");
	}

	/**
	 * Initializes the pool defined by ID by pre-allocating the first min
	 * connections from it. If the pool has already initialized it returns
	 * immediately. If the first connections fail to be allocated properly then
	 * an exception occurs and the pool is deinitialized. After this call the
	 * pool operates in pooled mode.
	 */
	public ConnectionPool initializePool(final String id) throws Exception {
		ConnectionPool pool = definePool(id);
		pool.initialize(); // Force initialization
		return pool;
	}

	/**
	 * This combines defining and initializing a pool.
	 * @param cs	The configsource to take definitions from
	 * @param id	The ID of the pool to define.
	 * @throws Exception
	 */
	public ConnectionPool initializePool(final PoolConfigSource cs, final String id) throws SQLException {
		ConnectionPool p = definePool(cs, id); // Define the pool
		p.initialize();
		return p;
	}

	/**
	 * This combines defining and initializing a pool, taking it's config from
	 * a properties file.
	 *
	 * @param poolfile
	 * @param id
	 * @throws Exception
	 */
	public ConnectionPool initializePool(final File poolfile, final String id) throws Exception {
		PoolConfigSource cs = PoolConfigSource.create(poolfile);
		return initializePool(cs, id);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getting pools and shit from the pools.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Finds the named pool. Throws an exception if not found.
	 */
	public ConnectionPool getPool(final String id) throws SQLException {
		if(id == null)
			throw new IllegalArgumentException("The pool ID cannot be null");
		synchronized(m_pool_ht) {
			ConnectionPool pool = (ConnectionPool) m_pool_ht.get(id); // Find the pool
			if(pool == null)
				throw new SQLException("PoolManager: pool with ID " + id + " not found.");
			return pool;
		}
	}

	/**
	 * Tries to return a database type for the connection passed.
	 *
	 * @param dbc		the connection to check
	 * @return			a dbtype for the connection.
	 */
	public BaseDB getDbType(final Connection dbc) {
		if(dbc instanceof PooledConnection) // Is it a pooled thing?
			return ((PooledConnection) dbc).getDbType();
		return GenericDB.getDbType(dbc); // Try more expensive method to get the type.
	}

	//	/**
	//	 *	Returns a non-pooled, freshly allocated connection from the specified
	//	 *	pool connection. This call does NOT initialize the pool!!
	//	 */
	//	private ThreadCacheableDbConnection getUnpooledConnection(String id) throws SQLException
	//	{
	//		ConnectionPool	p = getPool(id);
	//		return p.getUnpooledConnection();			// Ask the pool to provide.
	//	}
	//	/**
	//	 *	Returns a pooled connection from the pool.
	//	 */
	//	private ThreadCacheableDbConnection getPooledConnection(String id) throws SQLException
	//	{
	//		ConnectionPool	p = getPool(id);
	//		return p.getConn();									// Ask the pool to provide.
	//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Pool management and teardown.						*/
	/*--------------------------------------------------------------*/
	public void destroyAll() {
		for(Enumeration e = m_pool_ht.elements(); e.hasMoreElements();) {
			ConnectionPool p = (ConnectionPool) e.nextElement();
			p.deinitialize();
			//			m_pool_tbl.remove(p.getId());
		}
		m_pool_ht.clear();
	}

	/**
	 * Returns a full list of all defined pools.
	 * @return
	 */
	public ConnectionPool[] getPoolList() {
		synchronized(m_pool_ht) {
			ConnectionPool[] ar = new ConnectionPool[m_pool_ht.size()];
			int i = 0;
			Enumeration e = m_pool_ht.elements();
			while(e.hasMoreElements()) {
				ConnectionPool cp = (ConnectionPool) e.nextElement();
				ar[i++] = cp;
			}
			return ar;
		}
	}

	/**
	 * The janitor task which scans for unreleased database connections.
	 */
	private void dbScanOldiesTask(final int scaninterval_in_secs) {
		//-- Righty right. Get a list of ALL pools and scan them for old shit.
		ConnectionPool[] ar = getPoolList();
		//		DBJAN.msg("Scanning for hanging connections in "+ar.length+" pools");
		for(int i = 0; i < ar.length; i++) {
			if(!ar[i].isDisableOldiesScanner())
				ar[i].scanForOldies(scaninterval_in_secs);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pool scanner.										*/
	/*--------------------------------------------------------------*/
	private final int m_dbpool_scaninterval = 120;

	private Thread m_scanthread;

	/**
	 * Starts the scanner if database locking security is requested.
	 */
	synchronized protected void dbStartJanitor() {
		if(m_dbpool_scaninterval == 0 || m_scanthread != null)
			return;

		//-- Start the task,
		try {
			m_scanthread = new Thread(new Runnable() {
				public void run() {
					runScanner();
				}
			});
			m_scanthread.setDaemon(true);
			m_scanthread.setName("PoolScanner");
			m_scanthread.start();
		} catch(Exception x) {
			x.printStackTrace();
			logUnexpected(x, "in starting dbpool scanner");
		}
	}

	void runScanner() {
		System.out.println("PoolManager: expired connection scanning thread started.");
		for(;;) {
			try {
				dbScanOldiesTask(m_dbpool_scaninterval);
				Thread.sleep(m_dbpool_scaninterval * 1000 / 2);
			} catch(Exception x) {
				logUnexpected(x, "In scanning for expired connections");
			}
		}
	}

	/*----------------------------------------------------------*/
	/*	CODING:	ThreadConnection stuff.							*/
	/*----------------------------------------------------------*/
	/**
	 * <p>The thread map. Contains a ThreadConnectionInfo per thread. We
	 * cannot use a ThreadLocal thingy because the Janitor thread must be able
	 * to remove ThreadConnections when they expire. This map is locked
	 * on 'this'; the connection maps contained herein are locked by self.
	 *
	 * <p><b>WARNING</b>: Please do NOT assume that since the connection map
	 * is a per-thread entry that locking it is unneccesary! In normal
	 * circumstances this is true, but not when a connection is forcefully
	 * released by a Janitor process for instance!
	 */
	private final WeakHashMap m_thread_ht = new WeakHashMap();

	static final private class ThreadConnectionInfo {
		/** T if connections allocated by current thread must */
		private int m_cache_connections;

		/**
		 * A map of [poolid, Connection] for connections currently allocated
		 * for this thread.
		 */
		private final Map m_pool_map = new Hashtable();

		public ThreadConnectionInfo() {}

		final public boolean isCached() {
			return m_cache_connections > 0;
		}

		final public void incCached() {
			m_cache_connections++;
			if(m_cache_connections > 20)
				throw new IllegalStateException("Calls to enableCachedConnection() and disableCachedConnection() not balanced!!");
		}

		/**
		 * Decrement the cached nesting level, and returns true if
		 * caching gets disabled. Never decrements below 0.
		 * @return
		 */
		final public boolean decCached() {
			if(m_cache_connections == 0)
				return false;
			m_cache_connections--;
			//			System.out.println("#db: cached nesting level="+m_cache_connections);
			return m_cache_connections <= 0;
		}

		final public PooledConnection get(final ConnectionPool pool) {
			return (PooledConnection) m_pool_map.get(pool);
		}

		final public void put(final ConnectionPool pool, final PooledConnection conn) {
			m_pool_map.put(pool, conn);
		}

		final public PooledConnection[] discardMap() {
			if(m_pool_map.size() == 0)
				return null;
			PooledConnection[] ar = (PooledConnection[]) m_pool_map.values().toArray(new PooledConnection[m_pool_map.size()]);
			m_pool_map.clear();
			return ar;
		}
	}

	/**
	 * Locate (or create) the thread data structure for a given thread.
	 * @return
	 */
	private ThreadConnectionInfo getThreadMap(final Thread t) {
		//-- 1. Find the thread's pool map
		synchronized(this) {
			ThreadConnectionInfo tci = (ThreadConnectionInfo) m_thread_ht.get(t);
			if(tci == null) {
				tci = new ThreadConnectionInfo();
				m_thread_ht.put(t, tci);
			}
			return tci;
		}
	}

	/**
	 * When called this enables connection caching for this thread. All calls
	 * to allocate a connection will check to see if a connection is cached
	 * for this thread. If not a connection will be allocated and cached, else
	 * the cached copy will be returned. Calls to this MUST be terminated somewhere
	 * with a call to closeThreadConnections to close all cached connections.
	 *
	 * <p>The connections allocated by this call are all made "uncloseable", meaning
	 * that calls to "close" do not work.
	 */
	public void enableThreadCaching() {
		ThreadConnectionInfo tci = getThreadMap(Thread.currentThread());
		synchronized(tci) {
			tci.incCached();
		}
	}

	/**
	 * This is the main connection allocation routine, called by all of the pool's
	 * publicly accessible connection allocation methods. It checks to see if the
	 * thread has caching enabled. If so it tries to use a cached connection. If not
	 * it returns a newly allocated connection using a protected allocation method.
	 *
	 * @param p
	 * @param unpooled
	 * @return
	 * @throws SQLException
	 */
	protected PooledConnection _getConnectionFor(final ConnectionPool p, final boolean unpooled) throws SQLException {
		ThreadConnectionInfo tci = getThreadMap(Thread.currentThread());
		synchronized(tci) // Lock against janitor access.
		{
			/*
			 * Even though synchronized I may do long actions here because usually
			 * the locked structure is ONLY accessed by this thread. The only
			 * exception is the janitor when it discards a connection; it does not
			 * matter too much if that has to wait a bit.
			 */
			if(tci.isCached()) {
				//-- Can we find a cached connection instance?
				PooledConnection pc = tci.get(p);
				if(pc != null) {
					//-- If we needed an unpooled one but got a pooled one we need to "demote" pooled to unpooled.
					if(unpooled && !pc.m_pe.isUnpooled()) // Need unpooled but got pooled?
						p.makeUnpooled(pc); // Ask to demote the connection,
					return pc;
				}
			}

			//-- We need to allocate a connection regardless.
			PooledConnection pc = p._getConnection(unpooled); // Allocate a connection (internal call)
			if(!tci.isCached())
				return pc; // Exit immediately if not cached

			//			System.out.println("dbpool: returning a new cached connection "+pc);
			//-- Cache this connection: it is a threaded one
			pc.setUncloseable(true); // These may not normally be closed.
			pc.setThreadConnection(); // Indicate it is a thread connection (for debugging)
			tci.put(p, pc); // Store as current one
			return pc;
		}
	}

	public void disableThreadCaching() {
		closeThreadConnections();
	}

	/**
	 * Closes all connections in this-thread's pool IF the cached nesting count becomes zero
	 */
	public void closeThreadConnections() {
		ThreadConnectionInfo tci = getThreadMap(Thread.currentThread());
		PooledConnection[] ar;
		synchronized(tci) // Lock against janitor access.
		{
			if(!tci.decCached()) // Has reached top level of nested calls?
				return; // Nope.
			ar = tci.discardMap();
			if(ar == null)
				return;
		}

		for(int i = ar.length; --i >= 0;) {
			//			PooledConnection	pc = ar[i];
			//			System.out.println("dbpool: closing thread connection "+pc);
			try {
				ar[i].closeForced();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	//	protected void	removeThreadConnection(Thread owning_thread, ConnectionPool p, Connection pc)
	//	{
	//		Thread	t	= Thread.currentThread();
	//		HashMap	wm	= findThreadMap(t, false);
	//		synchronized(wm)
	//		{
	//			//-- Discard the entry for this connection...
	//			Connection c2 = (Connection)wm.get(p.getID());
	//			if(c2 == null)
	//				PoolManager.logUnexpected("Connection not found in thread's connection map when connection was closed.");
	//			else if(c2 != pc)
	//			{
	//				String s = "Connection released for thread "+t+" is DIFFERENT from the CURRENT connection for this thread!";
	//				PoolManager.logUnexpected(s);
	//				throw new IllegalStateException(s);
	//			}
	//			else
	//			{
	//				if(null == wm.remove(p.getID()))
	//					throw new IllegalStateException("Failed to remove thread connection: "+pc);
	//
	//			}
	//		}
	//	}


	/**
	 * Closes the single named connection in the thread's connection
	 * list if it IS open.
	 * @param poolid
	 */
	//	public void	closeThreadConnection(String poolid) throws SQLException
	//	{
	//		Thread	t	= Thread.currentThread();
	//		HashMap	wm	= findThreadMap(t, false);		// Find the thread's connections
	//		Connection	dbc = null;
	//		synchronized(wm)
	//		{
	//			dbc = (Connection)wm.remove(poolid);
	//			if(dbc == null)
	//				return;								// No open connection.
	//		}
	//		dbc.close();
	//	}

	public ThreadData threadData() {
		return (ThreadData) m_threadStats.get();
	}

	public InfoCollector threadCollector() {
		ThreadData td = threadData();
		return td != null ? (InfoCollector) td : (InfoCollector) DummyCollector.INSTANCE;
	}

	/**
	 * Start collecting data on database usage for a single "page" or "code" part. Every call to this *must* be
	 * followed by a call to stopCollecting(). Calls to these may be nested but the data gets associated with
	 * the ident passed on the outermost call.
	 * @param ident
	 * @return
	 */
	public void startCollecting(final String ident) {
		synchronized(this) {
			if(!m_collectStatistics)
				return;
		}
		ThreadData td = (ThreadData) m_threadStats.get();
		if(td == null) {
			td = new ThreadData(ident);
			m_threadStats.set(td);
		} else
			td.increment();
	}

	public ThreadData stopCollecting(final boolean report) {
		ThreadData td = (ThreadData) m_threadStats.get();
		if(td == null)
			return null;
		if(!td.decrement())
			return null;

		//-- Last use. Report stats and discard,
		m_threadStats.set(null);

		//-- Log stats,
		if(report) {
			System.out.println("S: " + td.getIdent() + ":" + strNanoTime(td.getRequestDuration()) + " #conn=" + td.getNAllocatedConnections() + " #q=" + td.getTotalQueries() + " #u="
				+ td.getTotalUpdates() + " #qrow=" + td.getNRows() + " #urow=" + td.getNUpdatedRows() + " #errs=" + td.getNErrors());
		}
		return td;
	}

	public void stopCollecting() {
		stopCollecting(true);
	}

	static private final long MICROS = 1000;

	static private final long MILLIS = 1000 * 1000;

	static private final long SECONDS = 1000 * 1000 * 1000;

	static private final long MINUTES = 60 * SECONDS;

	static private final long NSHOURS = 60 * MINUTES;

	/**
	 * Return a nanotime timestamp with 2 thousands of precision max.
	 * @param ns
	 * @return
	 */
	static public String strNanoTime(final long ns) {
		if(ns >= NSHOURS) {
			long h = ns / NSHOURS;
			long m = (ns % NSHOURS) / MINUTES;
			return h + "h" + m + "m";
		}
		if(ns >= MINUTES) {
			long m = ns / MINUTES;
			long s = (ns % MINUTES) / SECONDS;
			return m + "m" + s + "s";
		}
		if(ns >= SECONDS) {
			long a = ns / SECONDS;
			long b = (ns % SECONDS) / MILLIS;
			return a + "." + numstr(b) + "s";
		}
		if(ns >= MILLIS) {
			long a = ns / MILLIS;
			long b = (ns % MILLIS) / MICROS;
			return a + "." + numstr(b) + "ms";
		}
		if(ns >= MICROS) {
			long a = ns / MICROS;
			long b = (ns % MICROS);
			return a + "." + numstr(b) + "us";
		}
		return ns + "ns";
	}

	static private String numstr(final long v) {
		String s = Long.toString(v);
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		while(len++ < 3)
			sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	public synchronized void setCollectStatistics(final boolean on) {
		m_collectStatistics = on;
	}
}
