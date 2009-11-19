package to.etc.dbpool;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;

import javax.sql.*;

import to.etc.dbpool.stats.*;
import to.etc.dbutil.*;

/**
 * <h1>Working</h1>
 * <p>This represents an actual pool for a single database. It collects a list
 * of connections to the same database.
 * The connection pool is a generic provider for database connections of
 * several types. These can be pooled connections but the thing can also be
 * used to provide connections without a pooled set being built.</p>
 *
 * <p>To start a pool it needs to be <i>defined</i> first. Defining the
 * pool means that the definePool() call in the PoolManager has been called,
 * providing the pool's data like it's ID, the database driver to use, the
 * userid/password etc etc. Defining a pool does not allocate a pool
 * of connections.
 * After definition all of the connection calls work but when a connection
 * is closed it gets discarded as well, i.e. it is not returned to the pool.</p>
 *
 * <p>To start pooling connections you need to call initializePool() after it's
 * definition. This forces the pool to allocate it's minimum number of available
 * connections, and new connections will be retrieved from here. When connections
 * are closed they will be returned to the pool unless the max #of open connections
 * is reached in which case the connection will be discarded.</p>
 *
 * <h2>Connections from the pool</h2>
 * <p>Connections returned by this code are always wrapped by a Connection
 * wrapper. We call these connections <b>Connection proxies</b>.
 * To obtain the actual connection (for instance when you need to
 * do driver-specific calls) you need to cast the Connection to the
 * PooledConnection wrapper and call getRealConnection() on it.</p>
 *
 * <p>All of the pools' connections do resource management: all resources (statements,
 * result sets) obtained from the connection is kept in a list with the connection
 * so that they can be released/closed as soon as the connection proxy is closed. This
 * prevents reused real connections from having open statements. It also means that resource
 * leaks are minimized: as long as the connection is closed it also closes all open
 * cursors.</p>
 *
 * <h2>Main connection types</h2>
 * <p>Two main types of connection exist: <i>Pooled connections</i> and <i>Unpooled
 * connections</i>. The latter is a bit of a misnomer but the term is kept because
 * too much code uses these terms.</p>
 *
 * <h3>Pooled connections</h3>
 * <p>Pooled connections are connections that are used in server applications. These
 * are obtained from the set of free connections if possible and are subject to several
 * checks:
 * <ul>
 * 	<li>They <b>must</b> be closed again after allocation as soon as
 * 		possible. If a pooled connection is used too long (> 8 seconds) the system
 * 		will generate a warning message containing the context of the connection.
 * 		This mechanism is meant as a server optimization method; calls within the
 * 		server should never do database processing that takes that long.</li>
 * 	<li>They are forcefully closed if they are used for more than
 * 		a minute. A Janitor process scans all used connections and if it finds
 * 		a connection that's in use for more than a minute it closes both the
 * 		connection's proxy (the thing that is used by the client) and the real
 * 		connection used. This ensures that any running code on that database
 * 		is terminated. The client using the connection will get an exception
 * 		as soon as it tries to use it's proxy again. This check is done only
 * 		if the pool operates in pooled mode (initializePool() has been called).
 * </ul>
 *
 * <h3>Unpooled connections</h3>
 * <p>Unpooled connections are connections that do not have the above checks
 * executed on them. As such they can execute database code that takes longer
 * for instance for deamon-like tasks. If the pool operates in pooled mode
 * the connections are allocated from the available connections in the pool so
 * they will be provided quickly.</p>
 * <p>In addition, Unpooled connections are not counted as "used" connections
 * from the pool's connection set. This means an unlimited amount of Unpooled
 * connections can be allocated.</p>
 *
 * <h2>Closing connections</h2>
 * <p>By default these connection types are <i>closeable</i>, meaning that calling
 * <b>close()</b> on the connection will discard all resources and return the
 * real connection back to the pool. It is however possible to set a connection
 * proxy into <i>uncloseable</i> mode with a call to setCloseable(false). After
 * this call all calls to close() are silently ignored. To actually close the proxy
 * you need to call closeForced().</p>
 *
 * <p>This is used in server code where connections are cached during a request. As
 * a server executes many code sections during the handling of a request the overhead
 * of allocating connections for every database action is large. Another problem is
 * that calling code from other code can cause multiple connections to be allocated
 * to the same database, reducing the ability of the pool to provide connections.
 * To prevent this we usually want to "cache" connections once allocated during the
 * scope of a request. This caching can easily be done by setting the connection
 * to uncloseable at allocation time and closing it in a top-level code part just
 * before the request terminates. Setting the connection to uncloseable ensures that
 * if code calls close the connection stays alive for other code.</p>
 *
 * <h2>Thread connections<h2>
 * <p>Besides explicit connection caching where a connection is obtained, set to
 * uncloseable and reused by calls there is another way to cache connections during
 * the lifetime of a request: by using ThreadConnections. A thread connection can
 * be pooled or unpooled as usual. When a thread connection is allocated the connection
 * is registered to belong to the thread which allocated it. This thread becomes the
 * "owner" of that thread. The current ThreadConnection for any given pool and Thread
 * can be quickly obtained by looking in a per-thread hashtable mapped by pool.</p>
 *
 * <p>The first time a thread allocates a ThreadConnection it will not have a current
 * one so a new connection of the requested base type (pooled, unpooled) is allocated
 * and saved in the thread's map. This connection will then be set to uncloseable to
 * prevent it from being closed inadvertedly.</p>
 *
 * <p>The next time the thread requests a ThreadConnection this stored copy will be
 * returned, allowing for reuse of a connection during the time it is allocated to
 * the thread.</p>
 *
 * <p>Since a ThreadConnection is uncloseable calling close() on it will not close
 * the connection; this must be done either by calling closeForced() or by calling
 * the closeThreadConnections() call on the poolmanager. This call will walk the
 * connection list for the calling thread and discard all of the thread connections
 * allocated therein. After this call a new call to allocate a thread connection
 * will allocate a new connection from the poolset.
 *
 * <h2>Interaction between ThreadConnections and the other shit</h2>
 * <p>Unfortunately the "cache-it-myself" and the ThreadConnections method of
 * connection caching do not mix that well. It would be best to select one method
 * for a complete system and not use the other. If this is impossible (because multiple
 * code bases each use their own approach like NEMA) there are some special semantics
 * to remember.
 *
 * <p>Mixing both methods works best if the "cache-it-yourself" method uses a
 * ThreadConnection and does not forcefully close it, leaving that to the
 * closeThreadConnections() call. If the "cache-it-yourself" method allocates a pooled
 * connection (not a thread connection) and another piece of code allocates a thread
 * connection this will cause a single thread to use two connections (possibly to the
 * same database). This cannot be avoided because forcing the pooled connection to
 * check for a threadconnection first causes a shitload of trouble when that connection
 * gets closed.
 *
 * @author 	jal
 * @version $Version$
 */
public class ConnectionPool implements DbConnectorSet {
	static public final Logger MSG = Logger.getLogger("to.etc.dbpool.msg");

	static public final Logger JAN = Logger.getLogger("to.etc.dbpool.janitor");

	static public final Logger ALLOC = Logger.getLogger("to.etc.dbpool.alloc");

	/** The manager this pool comes from, */
	private final PoolManager m_manager;

	/** T if this pool is set to pooled mode. */
	private boolean m_is_pooledmode;

	/** All connection entries that are allocated but free for use. */
	private Stack<ConnectionPoolEntry> m_freeList = new Stack<ConnectionPoolEntry>();

	/** The connections that are currently in use (both pooled and unpooled), */
	//	private Set			m_usedSet = new HashSet();

	private Set<ConnectionPoolEntry> m_usedSet = new HashSet<ConnectionPoolEntry>();

	/** The current #of allocated and used unpooled connections. */
	private int m_n_unpooled_inuse;

	/**
	 * The current #of connections allocated for the POOL. This does NOT include
	 * the unpooled connections. The total #of connections used by the pool is
	 * the sum of this variable plus m_n_unpooled_inuse.
	 */
	private int m_n_pooledAllocated;

	/** The max. #of connections that can be allocated before the pool blocks */
	private int m_max_conns;

	/** The #of connections to allocate when INITIALIZING */
	private int m_min_conns;

	/** The current #of connections used by the clients of the pool, */
	private int m_n_pooled_inuse;

	/** The max. #of connections that was simultaneously used. */
	private int m_max_used;

	/** #of connection allocations (alloc/free) done. */
	private int m_n_connallocations;

	/** The #of times we had to wait for a pooled connection. */
	private int m_n_connectionwaits;

	/** The #of times we failed an allocation because all pooled connections were used. */
	private int m_n_connectionfails;

	/** The #of connections that were disconnected because they were assumed to be hanging. */
	private int m_n_hangdisconnects;

	/** The properties to pass to the driver's connect method. */
	private final Properties m_properties;

	/** This-pool's ID */
	private final String m_id;

	private List<ErrorEntry> m_lastErrorStack = new ArrayList<ErrorEntry>(10);

	/** The connector. */
	private final DbConnector m_pooled_connector = new DbConnector() {
		public String getID() {
			return ConnectionPool.this.getID();
		}

		public Connection makeConnection() throws SQLException {
			return getConnection(false);
		}
	};

	/** The connector. */
	private final DbConnector m_unpooled_connector = new DbConnector() {
		public String getID() {
			return ConnectionPool.this.getID();
		}

		public Connection makeConnection() throws SQLException {
			return getConnection(true);
		}
	};

	/** The unpooled datasource instance. */
	private final StupidDataSourceImpl m_unpooled_ds = new StupidDataSourceImpl(this, true);

	/** The pooled datasource instance. */
	private final StupidDataSourceImpl m_pooled_ds = new StupidDataSourceImpl(this, false);

	/** The driver instance to get connections from. */
	private Driver m_driver;

	/** This pool's connection characteristics */
	private String m_url, m_driverClassName, m_uid, m_pw;

	/** If present (not null) the driver should be instantiated off this file. */
	private File m_driverPath;

	/** T if this pool's connections need tracing. */
	private boolean m_trace;

	/** #secs for connection time warning if this pool has connection usage time calculated. */
	private final int m_conntime_warning_ms = 8000;

	/** The SQL statement that is to be sent as a check for valid cnnections */
	private String m_check;

	private boolean m_docheck;

	/** The CALCULATED SQL statement that is to be sent as a check for valid cnnections, if m_check is null. */
	private String m_check_calc;

	/** T if the pool has initialized properly */
	private Exception m_error_x;

	/// The #of statements CURRENTLY allocated by the pool
	protected int m_n_open_stmt;

	/// The #of statements MAX allocated by the pool
	protected int m_peak_open_stmt;

	/// The #of resultsets opened by the
	protected long m_n_open_rs;

	/// The #of SELECT statements executed.
	protected long m_n_exec;

	/// The #of rows returned.
	protected long m_n_rows;

	/// T if this pool has stack tracing enabled.
	protected boolean m_dbg_stacktrace = true;

	/// The database type.
	protected BaseDB m_dbtype = GenericDB.dbtypeUNKNOWN;

	/** The sequence generator for entries. */
	private int m_entryidgen;

	/** Set to T if logstream logging must be enabled. */
	private boolean m_setlog;

	private boolean m_printExceptions;

	/** When T this logs to stdout every time a connection is allocated or closed */
	private boolean m_logAllocation;

	/** When T this logs to stdout a stacktrace for every allocation and close */
	private boolean m_logAllocationStack;

	/** When T this logs all statements to stdout */
	private boolean m_logStatements;

	private boolean m_ignoreUnclosed;

	private boolean m_logResultSetLocations;

	static public enum ScanMode {
		DISABLED, ENABLED, WARNING
	}

	private ScanMode m_scanMode = ScanMode.ENABLED;

	public ConnectionPool(PoolManager pm, String id, String driver, String url, String userid, String passwd, String driverpath) throws SQLException {
		m_manager = pm;
		m_id = id;
		m_url = url;
		m_driverClassName = driver;
		m_uid = userid;
		m_pw = passwd;
		m_docheck = false;
		m_max_conns = 20;
		m_min_conns = 5;
		if(driverpath != null)
			m_driverPath = new File(driverpath);
		m_properties = new Properties();
		m_properties.setProperty("user", m_uid);
		m_properties.setProperty("password", m_pw);

		initFirst();
		if(m_min_conns < 1)
			m_min_conns = 1;
		if(m_max_conns < m_min_conns)
			m_max_conns = m_min_conns + 5;

	}

	/**
	 * Constructor: create the pool with the specified ID by retrieving the
	 * parameters from the properties file.
	 */
	public ConnectionPool(final PoolManager pm, final String id, final PoolConfigSource cs) throws SQLException {
		m_manager = pm;
		m_id = id;
		try {
			m_url = cs.getProperty(id, "url"); // Get URL and other parameters,
			m_driverClassName = cs.getProperty(id, "driver");
			m_uid = cs.getProperty(id, "userid");
			m_pw = cs.getProperty(id, "password");
			m_check = cs.getProperty("checksql", null);
			m_docheck = cs.getBool(id, "check", false);
			m_setlog = cs.getBool(id, "logstream", false);
			m_trace = cs.getBool(id, "trace", false);
			m_max_conns = cs.getInt(id, "maxconn", 20);
			m_min_conns = cs.getInt(id, "minconn", 5);
			boolean cost = cs.getBool(id, "statistics", false);
			m_printExceptions = cs.getBool(id, "printexceptions", false);

			String dp = cs.getProperty(id, "scan");
			if(dp == null)
				m_scanMode = ScanMode.ENABLED;
			else if("enabled".equalsIgnoreCase(dp) || "on".equalsIgnoreCase(dp))
				m_scanMode = ScanMode.ENABLED;
			else if("disabled".equalsIgnoreCase(dp) || "off".equalsIgnoreCase(dp))
				m_scanMode = ScanMode.DISABLED;
			else if("warning".equalsIgnoreCase(dp) || "warn".equalsIgnoreCase(dp)) { // Typical development setting.
				m_scanMode = ScanMode.WARNING;
				m_logResultSetLocations = true;
				m_ignoreUnclosed = false;
			} else
				throw new IllegalStateException("Invalid 'scan' mode: must be enabled, disabled or warn.");

			m_logResultSetLocations = cs.getBool(id, "logrslocations", m_logResultSetLocations); // Only override default if explicitly set.
			m_ignoreUnclosed = cs.getBool(id, "ignoreunclosed", m_ignoreUnclosed); //ditto

			if(cost)
				pm.setCollectStatistics(true);

			dp = cs.getProperty(id, "driverpath");
			if(dp != null) {
				File f = new File(dp);
				if(!f.exists()) {
					f = new File(System.getProperty("user.home"), dp);
					if(!f.exists())
						throw new SQLException("The driver path '" + dp + "' does not point to an existing file or directory");
				}
				m_driverPath = f;
			}
		} catch(SQLException x) {
			throw x;
		} catch(Exception x) {
			//-- We must fucking wrap. I HATE checked exceptions!
			x.printStackTrace();
			throw new RuntimeException("Pool " + id + " parameter error: " + x, x);
		}
		if(m_driverClassName == null)
			throw new SQLException("Missing jdbc driver in " + cs);
		if(m_uid == null)
			throw new SQLException("Missing uid in " + cs);
		if(m_url == null)
			throw new SQLException("Missing jdbc URL in " + cs);
		m_properties = new Properties();
		m_properties.setProperty("user", m_uid);
		m_properties.setProperty("password", m_pw);

		initFirst();
		if(m_min_conns < 1)
			m_min_conns = 1;
		if(m_max_conns < m_min_conns)
			m_max_conns = m_min_conns + 5;
	}

	private static class NoLoader extends URLClassLoader {
		NoLoader(final URL[] u) {
			super(u);
		}

		@Override
		protected synchronized Class< ? > loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			// First, check if the class has already been loaded
			Class< ? > c = findLoadedClass(name);
			//            System.out.println(name+": findLoadedClass="+c);
			if(c == null) {
				//-- Try to load by THIS loader 1st,
				try {
					c = findClass(name);
					//                    System.out.println(name+": findClass="+c);
				} catch(ClassNotFoundException x) {
					//                    System.out.println(name+": findClass exception");
				}
				if(c == null) {
					c = super.loadClass(name, resolve); // Try parent
					//                    System.out.println(name+": super.loadClass="+c);
				}
			}

			if(resolve)
				resolveClass(c);
			return c;
		}
	}

	/**
	 * Loads the appropriate driver class.
	 * @return
	 * @throws Exception
	 */
	private Driver loadDriver() throws Exception {
		Class< ? > cl = null;
		if(m_driverPath == null) {
			//-- Default method: instantiate the driver using the normal mechanism.
			try {
				cl = Class.forName(m_driverClassName);
			} catch(Exception x) {
				throw new SQLException("The driver class '" + m_driverClassName + "' could not be loaded: " + x);
			}
		} else {
			//-- Load the driver off the classloader.
			URLClassLoader loader = new NoLoader(new URL[]{m_driverPath.toURI().toURL()}); // Sun people are idiots.
			try {
				cl = loader.loadClass(m_driverClassName);
			} catch(Exception x) {
				throw new SQLException("The driver class '" + m_driverClassName + "' could not be loaded from " + m_driverPath + ": " + x);
			}
		}

		//-- Step 2: create an instance.
		try {
			Driver d = (Driver) cl.newInstance();
			System.out.println("load: class=" + d + ", inst=" + d.getMajorVersion() + "." + d.getMinorVersion());
			return d;
		} catch(Exception x) {
			throw new SQLException("The driver class '" + m_driverClassName + "' could not be instantiated: " + x);
		}
	}

	/**
	 * Do all initialization tasks.
	 * @throws SQLException
	 */
	private final void initFirst() throws SQLException {
		Connection dbc = null;
		try {
			if(m_setlog)
				DriverManager.setLogWriter(new PrintWriter(System.out));
			m_driver = loadDriver();
			//			Class.forName(m_driverClassName);		// Load the driver or abort without it.
			System.out.println("pool(" + m_id + "): defining " + m_driverClassName + ", url=" + m_url + ", uid=" + m_uid);
			if(m_printExceptions)
				System.out.println("  *warning: printExceptions is true");
			dbc = getCheckedConnection(); // Allocate a connection to see if we're OK
			m_dbtype = GenericDB.getDbType(dbc); // Try to find the database type.
			System.out.println("pool(" + getID() + "): driver version " + dbc.getMetaData().getDriverVersion()
			//			+	", JDBC "+dbc.getMetaData().getJDBCMajorVersion()+"."+dbc.getMetaData().getJDBCMinorVersion()
				+ ", " + dbc.getMetaData().getDatabaseProductName());
		} catch(ClassNotFoundException x) {
			m_error_x = new SQLException("pool(" + m_id + "): driver not found " + m_driverClassName);
			throw (SQLException) m_error_x;
		} catch(SQLException x) {
			m_error_x = x;
			throw x;
		} catch(Exception x) {
			m_error_x = x;
			throwWrapped();
		} finally {
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	private void throwWrapped() throws SQLException {
		if(m_error_x instanceof SQLException)
			throw (SQLException) m_error_x;
		if(m_error_x instanceof RuntimeException)
			throw (RuntimeException) m_error_x;
		throw new RuntimeException("Wrapped: " + m_error_x, m_error_x);
	}

	private synchronized void dbgAlloc(final String what, final Connection dbc) {
		m_n_connallocations++;
		if(isLogAllocation() || isLogAllocationStack()) {
			System.out.println("DEBUG: pool(" + m_id + ") ALLOCATED connection " + dbc);
			if(isLogAllocationStack()) {
				System.out.println(DbPoolUtil.getLocation());
			}
		}
		if(!ALLOC.isLoggable(Level.FINE))
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("ALLOCATE pool(" + m_id + ") " + what + " database[allocated for pool=" + m_n_pooledAllocated + ", allocated unpooled=" + m_n_unpooled_inuse + "] pool[inuse=" + m_n_pooled_inuse
			+ ", free=" + m_freeList.size() + "]");
		sb.append("\nConnection: " + dbc + "\n");
		DbPoolUtil.getThreadAndLocation(sb);
		ALLOC.fine(sb.toString());
	}

	public synchronized void dbgRelease(final String what, final Connection dbc) {
		if(isLogAllocation() || isLogAllocationStack()) {
			System.out.println("DEBUG: pool(" + m_id + ") CLOSED connection " + dbc + " (back to pool set)");
			if(isLogAllocationStack()) {
				System.out.println(DbPoolUtil.getLocation());
			}
		}
		if(!ALLOC.isLoggable(Level.FINE))
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("RELEASED pool(" + m_id + ") " + what + " database[allocated for pool=" + m_n_pooledAllocated + ", allocated unpooled=" + m_n_unpooled_inuse + "] pool[inuse=" + m_n_pooled_inuse
			+ ", free=" + m_freeList.size() + "]");
		if(dbc != null)
			sb.append("\nConnection: " + dbc + "\n");
		DbPoolUtil.getThreadAndLocation(sb);
		ALLOC.fine(sb.toString());
	}

	/**
	 * Used to compare two pools if a pool is redefined.
	 */
	@Override
	public boolean equals(final Object b) {
		if(!(b instanceof ConnectionPool))
			return false;
		ConnectionPool p = (ConnectionPool) b;
		if(!m_uid.equalsIgnoreCase(p.m_uid))
			return false;
		if(!m_url.equalsIgnoreCase(p.m_url))
			return false;
		if(!m_driverClassName.equals(p.m_driverClassName))
			return false;
		if(!m_pw.equals(p.m_pw))
			return false;
		return true;
	}

	/**
	 * Called to get the "is connection OK" sql command for this
	 * pool.
	 *
	 * @return
	 * @throws SQLException
	 */
	private synchronized String getCheckString() throws SQLException {
		if(m_check_calc != null)
			return m_check_calc;
		if(m_check != null)
			m_check_calc = m_check;
		else
			m_check_calc = GenericDB.getCheckString(m_dbtype);
		return m_check_calc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization to get into pooled mode.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to put the pool in "pooled mode". If the pool already is
	 * in pooled mode we are done before we knew it ;-)
	 */
	public synchronized void initialize() throws SQLException {
		if(m_is_pooledmode) // Already in pooled mode?
			return;
		m_manager.dbStartJanitor(); // Start the checker thread.

		try {
			//-- Now: display that we're initing,
			System.out.print("pool(" + m_id + "): initializing to pooled mode - ");

			//-- Allocate to the min. #of connections. If it fails we die.
			for(int i = 0; i < m_min_conns; i++) {
				Connection c = getCheckedConnection();
				//				System.out.println(">> driver version "+c.getMetaData().getDriverVersion());
				ConnectionPoolEntry pe = new ConnectionPoolEntry(c, this, m_entryidgen++);
				m_freeList.add(pe);
				if(m_trace)
					pe.setTrace(true);
				m_n_pooledAllocated++;
			}
			m_is_pooledmode = true;
			m_error_x = null;
			System.out.println(m_n_pooledAllocated + " connections allocated, okay.");
		} catch(SQLException x) {
			m_error_x = x; // Save what went wrong;
			System.out.println("FAILED " + x.toString());
			throw x;
		}
	}

	/**
	 * Releases all connections. Connections that are used are waited for.
	 * FIXME This needs a new implementation.
	 */
	private synchronized void deinitPool(final Collection<ConnectionPoolEntry> s) {
		for(ConnectionPoolEntry pe : s)
			pe.closeRealConnection(); // Force this closed..
	}

	/**
	 * Terminate the pool. Forces all connections closed.
	 */
	public void deinitialize() {
		if(m_error_x == null)
			m_error_x = new SQLException("dbPool(" + m_id + "): pool has been de-initialized.");
		Set<ConnectionPoolEntry> usedset;
		Stack<ConnectionPoolEntry> freelist;
		synchronized(this) {
			usedset = m_usedSet;
			freelist = m_freeList;
			m_usedSet = new HashSet<ConnectionPoolEntry>();
			m_freeList = new Stack<ConnectionPoolEntry>();
			m_n_exec = 0;
			m_n_open_rs = 0;
			m_n_open_stmt = 0;
			m_n_pooled_inuse = 0;
			m_n_pooledAllocated = 0;
			m_n_rows = 0;
			m_n_unpooled_inuse = 0;
			m_error_x = null;
			m_is_pooledmode = false;
			m_max_used = 0;
			m_peak_open_stmt = 0;
		}

		deinitPool(freelist);
		deinitPool(usedset);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Pool Entry allocation and release.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This tries to allocate a connection. If a connection is in the free pool
	 * it will be returned. If the request is for an unpooled connection and the
	 * free pool was empty a new connection will be allocated and returned, without
	 * it being counted as a "used" connection.
	 *
	 * <p>If the freelist is exhausted we need to allocate a new connection, if
	 * allowed. This needs to be done outside the lock because JDBC can lock too
	 * and this would cause the pool to be locked while a connection gets allocated.
	 * Before allocating the connection we up the connection counts to ensure that
	 * the connection count is not exceeded.
	 *
	 * @return
	 * @throws SQLException
	 */
	private ConnectionPoolEntry allocateConnection(final boolean unpooled) throws SQLException {
		int ctries = 0;
		int newid = 0;

		synchronized(this) {
			//-- Inner loop: check if we CAN allocate any way.
			for(;;) {
				if(m_error_x != null)
					throwWrapped(); // On fatal pool error exit,

				//-- 1. Is a connection available in the free pool?
				while(!m_freeList.isEmpty()) {
					ConnectionPoolEntry pe = m_freeList.pop();
					m_usedSet.add(pe); // Saved used entry.
					pe.setUnpooled(unpooled); // Tell the entry whether it is a pooled one or not

					if(unpooled) {
						/*
						 * Unpooled connections are no longer part of the pool. Because
						 * this connection was gotten from the pooled set we decrement
						 * the "connections allocated" for the pool.
						 */
						m_n_pooledAllocated--; // One less allocated in the poolset.
						m_n_unpooled_inuse++; // And one more in use
					} else {
						//-- Unpooled connections influence the "used" count.
						m_n_pooled_inuse++;
						if(m_n_pooled_inuse > m_max_used)
							m_max_used = m_n_pooled_inuse;
					}
					return pe;
				}

				//-- 2. No free connections. Can we allocate another one?
				if(m_n_pooledAllocated < m_max_conns || unpooled) {
					/*
					 * We may allocate a new connection. Update data to show
					 * that we allocate one, then break the loop to do the
					 * actual allocation OUTSIDE the lock. This makes sure that
					 * the connection count is not exceeded while we allocate
					 * another connection.
					 */
					if(!unpooled) {
						m_n_pooledAllocated++; // Increment actual allocation count
						m_n_pooled_inuse++; // One more used,
						if(m_n_pooled_inuse > m_max_used)
							m_max_used = m_n_pooled_inuse;
					} else
						m_n_unpooled_inuse++;

					//-- Save data for the creation later,
					newid = m_entryidgen++;
					break; // Enter the "allocate new" code.
				}

				//-- 3. Auch! Nothing! Wait till a connection is released!!
				try {
					String s = "POOL[" + m_id + "]: no more connections available on " + ctries + " try!?";
					System.out.println(s);
					MSG.severe(s);
					ctries++;
					if(ctries == 2) {
						StringBuilder sb = new StringBuilder(1024 * 1024);
						dumpUsedConnections(sb);
						String msg = sb.toString();
						saveError("No more database connections for pool=" + getID() + ", try 2..", msg);
						PoolManager.panic("No more database pool connections for pool " + getID(), msg);
					}

					if(ctries > 5) { // If too many retries abort,
						m_n_connectionfails++;
						StringBuilder sb = new StringBuilder(1024 * 1024);
						dumpUsedConnections(sb);
						String msg = sb.toString();
						saveError("No more database connections for pool=" + getID() + " - ABORTING REQUEST", msg);
						throw new SQLException("PANIC: Could not obtain a database connection - pool is exhausted!");
					}
					m_n_connectionwaits++;
					wait(10000); // Wait max. 10 secs,
				} catch(InterruptedException e) {
					throw new SQLException("dbPool " + m_id + ": interrupted while waiting for connection to become available");
				}
			}
		}

		/*
		 * When here we're NO longer locking the pool AND we need to allocate a
		 * new connection. The connection has already been counted in. This
		 * must be done outside a lock because JDBC (Oracle driver) may lock also.
		 */
		boolean ok = false;
		ConnectionPoolEntry pe = null;
		try {
			//-- Allocate a connection AND A new proxydude
			Connection c = getCheckedConnection();
			pe = new ConnectionPoolEntry(c, this, newid);
			ok = true;
			pe.setUnpooled(unpooled);
			return pe;
		} finally {
			//-- We need to handle accounting!!
			synchronized(this) {
				if(ok) {
					m_usedSet.add(pe);
				} else {
					//-- Decrement all counters that were upped assuming the code worked.
					if(!unpooled) {
						m_n_pooledAllocated--;
						m_n_pooled_inuse--;
					} else
						m_n_unpooled_inuse--;
				}
			}
		}
	}


	/**
	 * Checks to see if a connection can be used.
	 */
	private SQLException checkConnection(final Connection dbc) {
		if(!m_docheck)
			return null;
		ResultSet rs = null;
		Statement ps = null;
		try {
			String sql = getCheckString();
			if(sql.length() == 0)
				return null;
			ps = dbc.createStatement();
			rs = ps.executeQuery(sql);
			//			MSG.msg("checkConnection: check okay");
			return null;
		} catch(SQLException ex) {
			//			MSG.msg("CheckConnection failed, "+ex.toString());
			return ex;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception xx) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception xx) {}
		}
	}

	/**
	 * Returns a new connection from the driver. The connection is checked for
	 * errors before it is returned. This should prevent the !@*%% "End of file
	 * on TNS channel" oracle error... If a connection could not be obtained
	 * after 5 tries then the last exception is rethrown.
	 */
	private Connection getCheckedConnection() throws SQLException {
		int tries = 5;
		SQLException lastx = null;

		while(tries-- > 0) {
			Connection dbc = null;

			try {
				//				ALLOC.msg(m_id+": get connection on "+m_url+", uid="+m_uid);
				dbc = m_driver.connect(m_url, m_properties);
				//				dbc	= DriverManager.getConnection(m_url, m_uid, m_pw);
			} catch(SQLException x) {
				x.printStackTrace();
				MSG.info("Failed to get connection for " + getID() + ": " + x.toString());
				lastx = x;
			}

			//-- If the connection was gotten OK then check it....
			if(dbc != null) {
				if(!m_docheck)
					return dbc;

				lastx = checkConnection(dbc); // Can we use the connection?
				if(lastx == null)
					return dbc; // YES-> use this!

				try {
					dbc.close();
				} catch(Exception x) {}
			}
		}
		throw lastx;
	}

	/**
	 * Called to demote a pooled to an unpooled connection.
	 * @param dbc
	 */
	void makeUnpooled(final PooledConnection dbc) {
		ConnectionPoolEntry pe = dbc.checkPE();
		synchronized(this) {
			if(pe.isUnpooled())
				return;

			//-- Demote the connection.
			m_n_pooled_inuse--; // One less pooled in use
			m_n_pooledAllocated--; // One less pooled allocated
			m_n_unpooled_inuse++; // One more unpooled,
			pe.setUnpooled(true); // Make sure this gets no longer checked
			dbgAlloc("pooled->unpooled migration", dbc);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Releasing connections.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Called to return a connection to the pool. This gets called only when the
	 * connection is supposed to be returned to the poolset. This gets called
	 * both for pooled and unpooled connections. If returning the connection would
	 * cause the current pooled connection count to get above the maximum this code
	 * will discard the connection.
	 */
	void returnToPool(ConnectionPoolEntry pe, final PooledConnection pc) throws SQLException {
		//-- Before doing anything else reset the connection outside the lock,
		boolean ok = false;
		pe.setThreadCached(false);
		try {
			if(!pe.m_cx.getAutoCommit())
				pe.m_cx.rollback();
			pe.m_cx.setAutoCommit(true);
			ok = true;
		} catch(SQLException ex) {
			//-- Resetting state failed!! Drop connection!!
			System.out.print("pool(" + m_id + "): failed to reinitialize connection; dropped!");
			ex.printStackTrace();
			throw ex;
		} finally {
			synchronized(this) {
				/*
				 * If the reset was okay AND the connection count does not exceed
				 * the max count we return this to the pool, else we discard the
				 * connection.
				 */
				boolean unpooled = pe.isUnpooled();
				if(unpooled)
					m_n_unpooled_inuse--; // #of unpooled is one down
				if(ok) {
					if(unpooled && m_n_pooledAllocated >= m_max_conns) // Unpooled are returned only when #allocated not too big,
						ok = false; // ok=false means do not re-use the connection
				}
				if(ok) {
					//-- We are sure that we want to put this back into the poolset's free list.
					if(!m_usedSet.remove(pe)) {
						//--cannot happen.
						String subj = "pool(" + m_id + "): connection not in USED pool??";
						StringBuilder sb = new StringBuilder(65536);
						sb.append("Connection not in used pool! Location of release is:\n");
						DbPoolUtil.getThreadAndLocation(sb);
						sb.append("\n\nConnection dump:\n");
						pe.dbgPrintStackTrace(sb, 20, 20);
						String msg = sb.toString();
						saveError(subj, msg);
						PoolManager.panic(subj, msg);
						throw new IllegalStateException(subj);
					}
					if(unpooled)
						m_n_pooledAllocated++; // Unpooled means another allocated one now
					else
						m_n_pooled_inuse--; // Decrement pool use count for pooled,
					m_freeList.push(pe);
					pe = null; // Make sure we do not use this again ;-)
					dbgRelease("returned to pool", pc);
				}

				//-- Notify any waiters that another slot is available
				//				System.out.println("POOL["+m_id+"]: connection returned, #used is "+m_n_used);
				try {
					notify();
				} catch(Exception x) {}
			}
			if(ok)
				return;

			//-- If the code above was not OK we need to discard outside of the lock
			discardEntry(pe);
		}
	}

	/**
	 * Called from checkTimeout when the connection has timed out. This removes
	 * the connection from the pool's used list, preparing it to be removed
	 * completely.
	 * Called with a locked connection entry.
	 *
	 * @param pe		the entry to remove.
	 */
	protected boolean invalidateForTimeout(final ConnectionPoolEntry pe) {
		synchronized(this) // Lock the pool
		{
			if(!m_usedSet.remove(pe))
				return false; // Remove from used pool; exit if not found
			if(!pe.isUnpooled()) // Was an unpooled connection?
			{
				m_n_pooledAllocated--;
				m_n_pooled_inuse--;
			} else
				m_n_unpooled_inuse--;
			m_dbg_stacktrace = true; // Force enable stack traces for next occurence!
			return true;
		}
	}

	/**
	 * Called when an entry is to be discarded. It removes the
	 * entry from all datasets, updates all counts and forcefully
	 * closes the entry's connection.
	 * @param pe
	 */
	private void discardEntry(final ConnectionPoolEntry pe) {
		synchronized(this) {
			if(!m_usedSet.remove(pe)) {
				String subj = "pool(" + m_id + "): connection not in USED pool??";
				StringBuilder sb = new StringBuilder(65536);
				sb.append("Connection not in used pool! Location of release is:\n");
				DbPoolUtil.getThreadAndLocation(sb);
				sb.append("\n\nConnection stack dump:\n");
				pe.dbgPrintStackTrace(sb, 20, 20);
				String msg = sb.toString();
				saveError(subj, msg);
				PoolManager.panic(subj, msg);
				pe.closeRealConnection(); // Save what can be saved
				throw new IllegalStateException("pool(" + m_id + "): connection not in USED pool??");
			}
			if(!pe.isUnpooled()) // Discarding pooled means current allocation count must be decremented.
			{
				m_n_pooledAllocated--; // One less allocated in the pool
				m_n_pooled_inuse--; // And one less used,
			} else
				m_n_unpooled_inuse--;
			System.out.println("DISCARD pool=" + m_id + " connection discarded to server; conns=" + m_n_pooledAllocated + ", #unpooled=" + m_n_unpooled_inuse);

			try {
				notify();
			} catch(Exception x) {}
		}
		pe.closeRealConnection(); // Force the thingy really closed.
	}

	/**
	 * This is the main entry to get a connection, either pooled or unpooled.
	 * @return
	 * @throws SQLException
	 */
	PooledConnection _getConnection(final boolean unpooled) throws SQLException {
		ThreadData d = m_manager.threadData();
		if(d != null)
			d.connectionAllocated();
		for(;;) {
			ConnectionPoolEntry pe = allocateConnection(unpooled);
			Exception x = checkConnection(pe.m_cx); // Is the connection still valid?
			if(x == null) {
				PooledConnection dbc = pe.proxyMake(); // Yes-> make the proxy and be done.
				dbgAlloc("getConnection", dbc);
				return dbc;
			}

			//-- This entry is INVALID.. Loop!
			MSG.info("Pool " + m_id + ": cached connection error, " + x.toString() + "; discarded.");
			discardEntry(pe); // Delete this soonest
		}
	}

	/**
	 * This is the code to be called to allocate a connection using a
	 * possible cache.
	 *
	 * @param unpooled
	 * @return
	 * @throws SQLException
	 */
	protected PooledConnection getConnection(final boolean unpooled) throws SQLException {
		return m_manager._getConnectionFor(this, unpooled);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	The scan for hanging connections handler.		 	*/
	/*--------------------------------------------------------------*/
	//	private String dumpUsedConnections() {
	//		StringBuilder sb = new StringBuilder(1024 * 1024);
	//		dumpUsedConnections(sb);
	//		return sb.toString();
	//	}

	private synchronized void dumpUsedConnections(final StringBuilder sb) {
		int maxsz = 8192;
		int i = 0;
		for(ConnectionPoolEntry pe : m_usedSet) {
			//-- Get a stack dump for this brotha
			sb.append("Dump for entry #" + i + "\n\n");
			int clen = sb.length();
			pe.dbgPrintStackTrace(sb, 10, 2); // Dump stack trace,
			if(sb.length() - clen > maxsz) // Too big?
			{
				sb.setLength(clen + maxsz); // Truncate,
				sb.append("\n--- rest truncated---\n\n");
			}
			i++;
		}
	}

	/**
	 * Returns the list of connections currently used. Only used to scan
	 * for expired/hanging connections. This may ONLY lock the pool.
	 * @return
	 */
	private ConnectionPoolEntry[] getUsedConnections() {
		synchronized(this) {
			return m_usedSet.toArray(new ConnectionPoolEntry[m_usedSet.size()]);
		}
	}

	/**
	 *	This function gets called from the broker's janitor thread, OR from
	 *  the purgatory handler (the thing called when all connections are used).
	 *  It scans
	 *  this pool for any connection that is IN USE. Then it examines the
	 *  last-scanned timestamp m_scan_ts. If it is zero then the current time
	 *  is set in there. If it is NOT zero then the connection was in use
	 *  the last time we scanned. If it is older as the db_scaninterval time
	 *  we have a stuck connection!!
	 *  The connection will be removed from the connection list and will be
	 *  closed. Then a panic message will be sent, including the connection AND
	 *  if available it's stacktrace list.
	 *  Finally, this will set the pool's STACKTRACE option to true, allowing
	 *  the software writers to find out where connections are not released next
	 *  time this occurs.
	 *  @returns	T if the scan found and released "hanging" connections.
	 */
	boolean scanForOldies(final int scaninterval_in_secs) {
		if(getScanMode() == ScanMode.DISABLED)
			return false;

		boolean logonly = getScanMode() == ScanMode.WARNING;

		/*
		 * Scan all used connections, and invalidate all connections that are
		 * too old. They will be removed from the queues and be made invalid so
		 * that all access by the failing thread is aborted. Actually closing
		 * the connection is done outside the critical region.
		 */
		//-- 1. Get the list of used connections...
		ConnectionPoolEntry[] upar = getUsedConnections();
		if(upar == null)
			return false; // No connections in use-> exit.

		//-- 2. Now: check connection by connection with lock order always entry, then pool...
		long ts = System.currentTimeMillis();
		long ets = ts - scaninterval_in_secs * 1000; // Earliest time that's still valid
		int nhanging = 0;
		StringBuilder sb = null;
		StringBuilder unsb = new StringBuilder();
		for(int i = upar.length; --i >= 0;) {
			ConnectionPoolEntry pe = upar[i]; // The connection to check.
			if(!pe.isUnpooled()) {
				if(pe.checkTimeOut(ts, ets, !logonly)) // If this has timed out and we're not logging-only it will have been removed from the queue
				{
					nhanging++;
					if(sb == null) {
						sb = new StringBuilder(8192); // Lazily create the string buffer and init it,
						if(logonly) {
							sb.append("pool(").append(m_id).append(") connection(s) used for a long time:\n");
						} else {
							sb.append("*** DATABASE CONNECTIONS WERE HANGING ***\n");
							sb.append("Releasing hanging connections:\n");
						}
					}

					//-- Purge the connection.
					if(logonly) {
						long cts = System.currentTimeMillis() - pe.getAllocationTime();
						long luts = System.currentTimeMillis() - pe.getLastUsedTime();
						sb.append("- connection ").append(pe.getID()).append(" active for ").append(DbPoolUtil.strMillis(cts));
						sb.append(", last use ").append(DbPoolUtil.strMillis(luts)).append(" ago\n");
					} else
						purgeOld(sb, pe); // Remove the connection.
				}
			} else
				pe.checkUnpooledUnused(unsb, ts);
		}
		if(unsb.length() > 0) {
			String subj = "Unpooled connection(s) possibly not freed";
			String msg = unsb.toString();
			saveError(subj, msg);
			PoolManager.panic(subj, msg);
		}

		if(nhanging == 0) {
			JAN.info(m_id + ": no hanging connections found.");
			return false; // No old stuff found.
		}
		if(!logonly) {
			synchronized(this) {
				m_n_hangdisconnects += nhanging;
			}
			String subj = nhanging + " hanging database connections were released";
			JAN.info(subj);
			if(sb != null) {
				String msg = sb.toString();
				saveError(subj, msg);
				PoolManager.panic(subj, msg);
			}
		} else {
			System.out.println(sb.toString());
		}
		return true;
	}

	/**
	 *	Called when a "hanging" connection must be purged, this closes all
	 *  associated data, and sends a problem email...
	 */
	private void purgeOld(final StringBuilder sb, final ConnectionPoolEntry pe) {
		long cts = System.currentTimeMillis() - pe.getAllocationTime();

		sb.append("Hanging database connection " + pe.getID() + " found in pool ");
		sb.append(pe.getDesc());
		sb.append(": forced closed ");
		sb.append(cts);
		sb.append(" ms after allocation\n");

		//-- Try to find a stack trace,
		try {
			String sar[] = pe.dbgGetTrace();
			if(sar != null) {
				for(int i = 0; i < sar.length; i++) {
					if(sar[i] != null) {
						sb.append("\n\n---- Stack trace entry ");
						sb.append(Integer.toString(i));
						sb.append("-----\n");
						sb.append(sar[i]);
					}
				}
			} else
				sb.append("No stack trace was available.");
		} catch(Exception ex) {
			sb.append("Exception while getting stacktraces: " + ex.toString());
		}
		pe.closeRealConnection(); // Force the thingy really closed.
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Small data access functions.						*/
	/*--------------------------------------------------------------*/
	public String getID() {
		return m_id;
	}

	public String getURL() {
		return m_url;
	}

	public int getConns() {
		return m_n_pooledAllocated;
	}

	public int getMaxConns() {
		return m_max_conns;
	}

	public int getUsed() {
		return m_n_pooled_inuse;
	}

	public int getMaxUsed() {
		return m_max_used;
	}

	/**
	 *	Returns T if stack tracking is enabled for debugging purposes.
	 */
	public synchronized boolean dbgIsStackTraceEnabled() {
		return m_dbg_stacktrace;
	}


	/**
	 *	Switches stack tracing ON. This is very expensive and should only be
	 *  used in case of trouble. Switching on stack trace (done thru the /nema/
	 *  servlet path) causes the pool to remember the last 10 stack paths that
	 *  accessed a connection.
	 */
	public synchronized void dbgSetStacktrace(final boolean on) {
		m_dbg_stacktrace = on;
	}


	/**
	 *	Returns the pool database as a constant from PooledConnection:
	 *  dbtypeORACLE, dbtypeMYSQL or dbtypeUNKNOWN.
	 */
	public BaseDB getDbType() {
		return m_dbtype;
	}


	public String dbgDumpUsedStacks() {
		return dbgDumpUsedStacks(false);
	}

	/**
	 *	Returns a string containing a stack trace list for all currently USED
	 *  connections. The dump is used for debugging when connections are not
	 *  released proper. This buffer should be displayed in a <pre> block.
	 */
	public String dbgDumpUsedStacks(final boolean firstonly) {
		if(!m_dbg_stacktrace)
			return "Stacktrace is off.";

		StringBuilder sb = new StringBuilder(10240);
		sb.append("There are ");
		sb.append(Integer.toString(m_usedSet.size()));
		sb.append(" connections in the USED pool.\n");
		int ct = 0;
		synchronized(this) {
			for(ConnectionPoolEntry pc : m_usedSet) {
				sb.append("\n\n<b>----- Pooled connection number ");
				sb.append(Integer.toString(ct));
				sb.append(" ----------</b>\n");

				//-- Dump the stack traces for this thing....
				String[] ar = pc.dbgGetTrace();
				if(ar == null)
					sb.append("No stack trace associated.\n");
				else {
					int limit = firstonly ? 1 : ar.length;
					for(int i = 0; i < ar.length && ar[i] != null; i++) {
						if(limit-- <= 0) {
							sb.append("\nRest of stack traces skipped.\n");
							break;
						}

						sb.append("Trace number ");
						sb.append(Integer.toString(i));
						sb.append("\n");
						sb.append(ar[i]);
						sb.append("\n----------------------------\n");
					}
				}

				ct++;
			}

			return sb.toString();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Connection usage time statistics.					*/
	/*--------------------------------------------------------------*/
	private final int[] m_usetime_ar = new int[10];

	static private final int[] TIMES = new int[]{10, 20, 50, 100, 250, 500, 1000, 2000, 4000};

	static private int getTimeSlot(final long ts) {
		for(int i = TIMES.length; --i >= 0;) {
			if(ts >= TIMES[i])
				return i + 1;
		}
		return 0;
	}

	/**
	 * Returns the #of seconds that a connection must have been USED before a
	 * warning and a stack dump is generated.
	 * @return
	 */
	public int getConnectionUsedTooLongWarningTimeout() {
		return m_conntime_warning_ms;
	}


	/**
	 * Adds this time to the "connection usage" timer...
	 * @param ut
	 */
	public void handleConnectionUsageTime(final ConnectionPoolEntry pe, final long ut) {
		//-- Add to statistics.
		int slot = getTimeSlot(ut);
		synchronized(m_usetime_ar) {
			m_usetime_ar[slot]++;
		}
		if(ut < m_conntime_warning_ms)
			return;

		//-- !! Connection took a shitload of time! log!
		StringBuilder sb = new StringBuilder(1024);
		sb.append("** Connection was used for more than ");
		sb.append(Integer.toString(m_conntime_warning_ms));
		sb.append("ms: it took ");
		sb.append(Long.toString(ut));
		sb.append("ms from OPEN to CLOSE!!!\n");

		sb.append("Stack trace of the connection:\n");
		pe.dbgPrintStackTrace(sb, 0, 0);

		//-- Now: log and send to admin.
		PoolManager.logUnexpected(sb.toString());
		PoolManager.panic("Database connection used too long", sb.toString());
	}

	public int[] getUseTimeTable() {
		int[] ar = new int[m_usetime_ar.length];
		synchronized(m_usetime_ar) {
			System.arraycopy(m_usetime_ar, 0, ar, 0, m_usetime_ar.length);
			return ar;
		}
	}

	//	static private final String[] COLOR = new String[]{"#660000", "#330000",};

	/**
	 * Returns a HTML-formatted table of connection usage times.
	 * @return
	 */
	public String getUseTimeTableStr() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("<table class=\"perftbl\">\n");
		sb.append("<tr>\n");
		for(int i = 0; i < TIMES.length; i++) {
			sb.append("<td class=\"");
			sb.append((i % 2) == 0 ? "perfeven" : "perfodd");
			sb.append(" perfhdr\">");
			sb.append("&lt; ");
			if(TIMES[i] >= 1000) {
				sb.append(Integer.toString(TIMES[i] / 1000));
				sb.append("s");
			} else {
				sb.append(Integer.toString(TIMES[i]));
				sb.append("ms");
			}
			sb.append("</td>");
		}
		sb.append("<td class=\"perfhdr perfmore\">More!</td>");
		sb.append("\n</tr>\n<tr>\n");
		int[] ar = getUseTimeTable();
		for(int i = 0; i < ar.length; i++) {
			sb.append("<td class=\"");
			sb.append((i % 2) == 0 ? "perfeven" : "perfodd");
			sb.append(" perfval\">");
			sb.append(Long.toString(ar[i]));
			sb.append("</td>");
		}
		sb.append("</tr>\n</table>\n");
		return sb.toString();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters and setters.								*/
	/*--------------------------------------------------------------*/
	public final PoolManager getManager() {
		return m_manager;
	}

	public synchronized void incOpenStmt() {
		m_n_open_stmt++;
		m_n_exec++;
		if(m_n_open_stmt > m_peak_open_stmt)
			m_peak_open_stmt = m_n_open_stmt;
	}

	public synchronized void decOpenStmt() {
		m_n_open_stmt--;
	}

	public synchronized void decOpenStmt(final int count) {
		m_n_open_stmt -= count;
	}

	/**
	 *	Get the current #of statements opened.
	 */
	public synchronized int getCurrOpenStmt() {
		return m_n_open_stmt;
	}

	public synchronized void incOpenRS() {
		m_n_open_rs++;
	}

	public synchronized void decOpenRS() {
		m_n_open_rs--;
	}

	public long getOpenResultSets() {
		return m_n_open_rs;
	}

	/**
	 *	Gets the peak #of concurrently open statements.
	 */
	public synchronized int getPeakOpenStmt() {
		return m_peak_open_stmt;
	}

	public synchronized long getTotalStmt() {
		return m_n_exec;
	}

	/**
	 * The #of connections allocated from the database.
	 * @return
	 */
	public synchronized int getAllocatedConnections() {
		return m_n_pooledAllocated;
	}

	/**
	 * Returns the #of times that a connection was allocated from the pool (i.e. the #of
	 * calls to getConnection()).
	 * @return
	 */
	public int getConnectionAllocationCount() {
		return m_n_connallocations;
	}


	public DbConnector getPooledConnector() {
		return m_pooled_connector;
	}

	public DbConnector getUnpooledConnector() {
		return m_unpooled_connector;
	}

	public Connection getNewPooledConnection() throws SQLException {
		return _getConnection(false);
	}

	public String getUserID() {
		return m_uid;
	}

	public DataSource getUnpooledDataSource() {
		return m_unpooled_ds;
	}

	public DataSource getPooledDataSource() {
		return m_pooled_ds;
	}

	public synchronized int getConnectionWaits() {
		return m_n_connectionwaits;
	}

	public synchronized int getConnectionFails() {
		return m_n_connectionfails;
	}

	public boolean isPooledMode() {
		return m_is_pooledmode;
	}

	public synchronized int getUnpooledInUse() {
		return m_n_unpooled_inuse;
	}

	/**
	 * Returns the #of times a connection was closed by the Janitor because it
	 * was allocated for way too long.
	 * @return
	 */
	public synchronized int getHangDisconnects() {
		return m_n_hangdisconnects;
	}

	public synchronized void setSaveErrors(final boolean on) {
		if(on && m_lastErrorStack == null)
			m_lastErrorStack = new ArrayList<ErrorEntry>();
		else if(!on && m_lastErrorStack != null)
			m_lastErrorStack = null;
	}

	public synchronized boolean isSavingErrors() {
		return m_lastErrorStack != null;
	}

	public synchronized List<ErrorEntry> getSavedErrorList() {
		if(m_lastErrorStack == null)
			return null;
		return new ArrayList<ErrorEntry>(m_lastErrorStack);
	}

	static public class ErrorEntry {
		private final Date m_ts;

		private final String m_msg;

		private final String m_subject;

		public ErrorEntry(final Date ts, final String subject, final String txt) {
			m_ts = ts;
			m_msg = txt;
			m_subject = subject;
		}

		public String getMsg() {
			return m_msg;
		}

		public String getSubject() {
			return m_subject;
		}

		public Date getTs() {
			return m_ts;
		}
	}

	public synchronized void saveError(final String subject, final String msg) {
		if(m_lastErrorStack == null)
			return;
		m_lastErrorStack.add(0, new ErrorEntry(new Date(), subject, msg));
		while(m_lastErrorStack.size() > 10) {
			m_lastErrorStack.remove(m_lastErrorStack.size() - 1);
		}
	}

	void logExecution(final StatementProxy sp) {
		logExecution(sp, false);
	}

	/**
	 * Callback from statement pxy when a call gets executed.
	 * @param ppx
	 */
	void logExecution(final StatementProxy sp, final boolean batch) {
		if(!isLogStatements())
			return;

		StringBuilder sb = new StringBuilder();
		sb.append(batch ? "    dbg batch=" : "    dbg sql=");
		sb.append(sp.getSQL());
		sb.append("\n    connection=");
		sb.append(sp._conn().toString());
		sb.append("\n");
		if(sp instanceof PreparedStatementProxy) {
			PreparedStatementProxy ppx = (PreparedStatementProxy) sp;
			Object[] par = ppx.internalGetParameters();
			if(par != null) {
				sb.append("    parameters:\n");
				for(int i = 0; i < par.length; i++) {
					sb.append("     #" + (i + 1) + ":");
					Object val = par[i];
					if(val == null)
						sb.append(" null\n");
					else {
						sb.append(val.getClass().getName());
						sb.append(":");
						sb.append(val.toString());
						sb.append("\n");
					}

					if(sb.length() >= 8192) {
						sb.append("    (rest truncated)\n");
						break;
					}
				}
			}
		}
		System.out.println(sb.toString());
	}

	void logBatch() {
		if(!isLogStatements())
			return;
		System.out.println("    executeBatch()");
	}

	public synchronized boolean hasSavedErrors() {
		return m_lastErrorStack != null && m_lastErrorStack.size() > 0;
	}

	public boolean isPrintExceptions() {
		return m_printExceptions;
	}

	public synchronized boolean isLogAllocation() {
		return m_logAllocation;
	}

	public synchronized void setLogAllocation(final boolean logAllocation) {
		m_logAllocation = logAllocation;
	}

	public synchronized boolean isLogAllocationStack() {
		return m_logAllocationStack;
	}

	public synchronized void setLogAllocationStack(final boolean logAllocationStack) {
		m_logAllocationStack = logAllocationStack;
	}

	public synchronized boolean isLogStatements() {
		return m_logStatements;
	}

	public synchronized void setLogStatements(final boolean logStatements) {
		m_logStatements = logStatements;
	}

	public boolean isIgnoreUnclosed() {
		return m_ignoreUnclosed;
	}

	public ScanMode getScanMode() {
		return m_scanMode;
	}

	public synchronized boolean isLogResultSetLocations() {
		return m_logResultSetLocations;
	}

	public synchronized void setLogResultSetLocations(boolean logResultSetLocations) {
		m_logResultSetLocations = logResultSetLocations;
	}
}
