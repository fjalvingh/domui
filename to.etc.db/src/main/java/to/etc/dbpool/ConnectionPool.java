/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.dbpool;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import javax.sql.*;

import to.etc.dbpool.DbPoolUtil.HostAndPort;
import to.etc.dbpool.info.*;


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
final public class ConnectionPool {
	static public final Logger MSG = Logger.getLogger("to.etc.dbpool.msg");

	static public final Logger JAN = Logger.getLogger("to.etc.dbpool.janitor");

	static public final Logger ALLOC = Logger.getLogger("to.etc.dbpool.alloc");

	/** The manager this pool comes from, */
	private final PoolManager m_manager;

	/** The ID for this pool. */
	private final String m_id;

	/** The immutable pool config. */
	final private PoolConfig m_config;

	/*------------ Mutable config information -----------------*/
	/** The driver instance to get connections from. */
	private Driver m_driver;

	/** T if this pool is set to pooled mode. */
	private boolean m_isPooled;

	/** The properties to pass to the driver's connect method. */
	private final Properties m_properties = new Properties();

	/** When T this pool has been destroyed and cannot be used anymore. */
	private boolean m_destroyed;

	/** The dbtype obtained from the driver. */
	private DbType m_dbType = DbType.UNKNOWN;

	/** The CALCULATED SQL statement that is to be sent as a check for valid cnnections, if m_check is null. */
	private String m_check_calc;

	/** Per-thread configuration of connection handling for debug and JUnit test purposes. */
	private ThreadLocal<ThreadConfig> m_threadConfig = new ThreadLocal<ThreadConfig>();

	/*---------- Connection administration ---------------------------*/
	/** All connection entries that are allocated but free for use. */
	private Stack<PoolEntry> m_freeList = new Stack<PoolEntry>();

	/** The connections that are currently in use (both pooled and unpooled), */
	private Set<PoolEntry> m_usedSet = new HashSet<PoolEntry>();

	/** The current #of allocated and used unpooled connections. */
	private int m_unpooledAllocatedCount;

	private int m_unpooledMaxUsed;

	/**
	 * The current #of connections allocated for the POOL. This does NOT include
	 * the unpooled connections. The total #of connections used by the pool is
	 * the sum of this variable plus m_n_unpooled_inuse.
	 */
	private int m_pooledAllocatedCount;

	/** The current #of connections used by the clients of the pool, */
	private int m_pooledUsedCount;

	/** The max. #of connections that was simultaneously used by the pool. */
	private int m_pooledMaxUsed;

	/** #of connection allocations (alloc/free) done. */
	private int m_poolAllocationCount;

	/** #of connection allocations directly from the database.. */
	private int m_databaseAllocationCount;

	/** The #of times we had to wait for a pooled connection. */
	private int m_n_connectionwaits;

	/** The #of times we failed an allocation because all pooled connections were used. */
	private int m_n_connectionfails;

	/** The #of connections that were disconnected because they were assumed to be hanging. */
	private int m_n_hangdisconnects;

	/** The connections last found in the expiry scanner that look to be hanging. */
	private List<ConnectionProxy> m_currentlyHangingConnections = Collections.EMPTY_LIST;

	/** The connections last released by the connection scanner. */
	private List<ConnectionProxy> m_releasedConnections = Collections.EMPTY_LIST;

	/** The #of statements CURRENTLY allocated by the pool */
	protected int m_n_open_stmt;

	/** The #of statements MAX allocated by the pool */
	protected int m_peak_open_stmt;

	/** The #of resultsets opened by all statements in the pool */
	protected long m_n_open_rs;

	/** The #of prepare statements executed. */
	protected long m_statementTotalPrepareCount;

	/// The #of rows returned.
	@Deprecated
	protected long m_n_rows;

	private List<ErrorEntry> m_lastErrorStack = new ArrayList<ErrorEntry>(10);

	/** The pooled datasource instance. */
	private final DataSourceImpl m_pooled_ds = new DataSourceImpl(this);

	/** The unpooled datasource instance. */
	private final UnpooledDataSourceImpl m_unpooled_ds = new UnpooledDataSourceImpl(this);

	/** #secs for connection time warning if this pool has connection usage time calculated. */
	private final int m_conntime_warning_ms = 8000;

	/** T if this pool has stack tracing enabled. */
	protected boolean m_dbg_stacktrace = true;

	/** The sequence generator for entries. */
	private int m_entryidgen;

	private volatile int m_forceTimeout;

	/** Per-pool attributes that can be used for extensions. */
	final private Map<String, Object> m_attributeMap = new HashMap<>();

	@Nullable
	private IConnectionStatisticsFactory m_connectionStatisticsFactory;

	/**
	 * Pool event, add listeners using
	 *
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Mar 11, 2013
	 */
	public interface IPoolEvent {
		void connectionAllocated(@Nonnull Connection dbc) throws Exception;

		void connectionReleased(@Nonnull Connection dbc) throws Exception;
	}

	@Nonnull
	private List<IPoolEvent> m_poolListeners = Collections.EMPTY_LIST;

	private boolean m_hasPlSqlHandler;

	public ConnectionPool(PoolManager pm, String id, PoolConfig config) throws SQLException {
		m_manager = pm;
		m_id = id;
		m_config = config;
	}

	/**
	 * Return the config parameter class.
	 * @return
	 */
	public PoolConfig c() {
		return m_config;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pool events and listeners							*/
	/*--------------------------------------------------------------*/

	/**
	 * Add a listener for pool related events.
	 * @param l
	 */
	public synchronized void addListener(@Nonnull IPoolEvent l) {
		m_poolListeners = new ArrayList<IPoolEvent>(m_poolListeners);
		m_poolListeners.add(l);
	}

	/**
	 * Remove the specified listener.
	 * @param l
	 */
	public synchronized void removeListener(@Nonnull IPoolEvent l) {
		m_poolListeners = new ArrayList<IPoolEvent>(m_poolListeners);
		m_poolListeners.remove(l);
	}

	@Nonnull
	private synchronized List<IPoolEvent> getPoolListeners() {
		return m_poolListeners;
	}

	private void callAllocatedListeners(@Nonnull Connection dbc) {
		List<IPoolEvent> poolListeners = getPoolListeners();
		for(int i = poolListeners.size(); --i >= 0;) {
			try {
				poolListeners.get(i).connectionAllocated(dbc);
			} catch(Exception x) {
				System.out.println("Ignored exception in pool event listener " + poolListeners.get(i) + ": " + x);
				x.printStackTrace();
			}
		}
	}

	void callReleasedListeners(@Nonnull Connection dbc) {
		List<IPoolEvent> poolListeners = getPoolListeners();
		for(int i = poolListeners.size(); --i >= 0;) {
			try {
				poolListeners.get(i).connectionReleased(dbc);
			} catch(Exception x) {
				System.out.println("Ignored exception in pool event listener " + poolListeners.get(i) + ": " + x);
				x.printStackTrace();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Checking pool config and setting derived fields.	*/
	/*--------------------------------------------------------------*/
	/**
	 * Check to make s
	 * @throws SQLException
	 */
	synchronized void checkParameters() throws SQLException {
		usable();

		m_properties.setProperty("user", c().getUid());
		m_properties.setProperty("password", c().getPw());
		if(c().getUrl() == null)
			throw new SQLException("Pool " + getID() + ": missing 'url' parameter");
		if(c().getDriverClassName() == null)
			throw new SQLException("Pool " + getID() + ": missing 'driver' class name parameter");
		if(c().getUid() == null)
			throw new SQLException("Pool " + getID() + ": missing 'uid' user name parameter");
		if(c().isCollectStatistics())
			m_manager.setCollectStatistics(true);
		if(c().getDriverPath() != null) {
			if(!c().getDriverPath().exists())
				throw new SQLException("Pool " + getID() + ": driver path '" + c().getDriverPath() + "' does not exist");
		}

		if(c().getBinaryLogFile() != null)
			setFileLogging(c().getBinaryLogFile());

		String plsqldebug = DbPoolUtil.getPlSqlDebug(getID());
		if(null != plsqldebug)
			addPlSqlDebugHandler(plsqldebug);

		//-- Now initialize the rest of the parameters and try to allocate a connection for testing pps.
		Connection dbc = null;
		try {
			if(c().isSetlog())
				DriverManager.setLogWriter(new PrintWriter(System.out));

			m_driver = DbPoolUtil.loadDriver(c().getDriverPath(), c().getDriverClassName());
			System.out.println("pool(" + m_id + "): defining " + c().getDriverClassName() + ", url=" + c().getUrl() + ", uid=" + c().getUid());
			if(c().isPrintExceptions())
				System.out.println("  *warning: printExceptions is true");


			dbc = getCheckedConnection(); // Allocate a connection to see if we're OK
//			m_dbtype = GenericDB.getDbType(dbc); // Try to find the database type.
			DatabaseMetaData md = dbc.getMetaData();
			System.out.println("pool(" + getID() + "): driver version " + md.getDriverVersion() + ", " + md.getDatabaseProductName());

			//-- Get database type.
			m_dbType = DbPoolUtil.getDbTypeByDriverName(md.getDriverName());
			IConnectionStatisticsFactory connectionStatisticsFactory = null;
			switch(m_dbType) {
				default:
					break;

				case ORACLE:
					connectionStatisticsFactory = new OracleConnectionStatisticsFactory();
					break;
			}
			m_connectionStatisticsFactory = connectionStatisticsFactory;

			//-- Define a check string if needed.
			if(c().isCheckConnection()) {
				if(c().getCheckSQL() != null) {
					m_check_calc = c().getCheckSQL();
				} else {
					switch(m_dbType){
						default:
							throw new SQLException("pool(" + getID() + ")'s type is unknown, it needs a manually-configured 'check' SQL statement");
						case ORACLE:
							m_check_calc = "select 1 from dual";
							break;
						case POSTGRES:
						case MYSQL:
							m_check_calc = "select 1";
							break;
					}
				}
			}

		} catch(ClassNotFoundException x) {
			throw new SQLException("pool(" + m_id + "): driver not found " + c().getDriverClassName());
		} catch(SQLException x) {
			throw x;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x);
		} finally {
			try {
				if(dbc != null)
					dbc.close();
			} catch(Exception x) {}
		}
	}

	public void addPlSqlDebugHandler(@Nonnull String plsqldebug) throws SQLException {

		final HostAndPort hostAndPort = HostAndPort.parse(plsqldebug);

		synchronized(this) {
			if(m_hasPlSqlHandler)
				return;
			m_hasPlSqlHandler = true;
		}

		addListener(new IPoolEvent() {
			@Override
			public void connectionReleased(@Nonnull Connection dbc) throws Exception {}

			@Override
			public void connectionAllocated(@Nonnull Connection dbc) throws Exception {
				DbPoolUtil.enableRemoteDebug(dbc, hostAndPort);
			}
		});
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Primitive connection allocation.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks to see if a connection can be (re)used. If no check is configured this returns immediately.
	 */
	private SQLException checkConnection(final Connection dbc) {
		if(!c().isCheckConnection() || m_check_calc == null)
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
	 * Get a connection with the default user ID and password.
	 * @return
	 * @throws SQLException
	 */
	private Connection getCheckedConnection() throws SQLException {
		return getCheckedConnection(null, null);
	}

	/**
	 * Returns a new connection from the driver. The connection is checked for
	 * errors before it is returned (if so configured). This should prevent
	 * the !@*%% "End of file on TNS channel" oracle error... If a connection
	 * could not be obtained after 5 tries then the last exception is rethrown.
	 */
	private Connection getCheckedConnection(String user, String passwd) throws SQLException {
		usable();
		int tries = 5;
		SQLException lastx = null;

		while(tries-- > 0) {
			Connection dbc = null;
			synchronized(this) {
				m_databaseAllocationCount++;
			}

			try {
				//				ALLOC.msg(m_id+": get connection on "+m_url+", uid="+m_uid);
				Properties p = m_properties;
				if(user != null) {
					p = new Properties(m_properties);
					p.setProperty("user", user);
					p.setProperty("password", passwd);
				}

				dbc = m_driver.connect(c().getUrl(), p);
			} catch(SQLException x) {
				//x.printStackTrace();
				MSG.log(Level.WARNING, "Failed to get connection for " + getID() + ": " + x.toString(), x);
				lastx = x;
			}

			//-- If the connection was gotten OK then check it....
			if(dbc != null) {
				lastx = checkConnection(dbc); 				// Can we use the connection?
				if(lastx == null) {
					callAllocatedListeners(dbc);			// Tell the world we have a new'un
					return dbc; 							// YES-> use this!
				}

				try {
					dbc.close();
				} catch(Exception x) {}
			}
		}
		throw new SQLException("Cannot get new connection for user " + user + " from database driver: " + lastx, lastx);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization to get into pooled mode.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to put the pool in "pooled mode". If the pool already is
	 * in pooled mode we are done before we knew it ;-)
	 */
	public synchronized void initialize() throws SQLException {
		usable();
		if(m_isPooled) // Already in pooled mode?
			return;
		m_manager.startExpiredConnectionScanner(); // Start the checker thread.

		try {
			//-- Now: display that we're initing,
			System.out.print("pool(" + m_id + "): initializing to pooled mode - ");

			//-- Allocate to the min. #of connections. If it fails we die.
			for(int i = 0; i < c().getMinConns(); i++) {
				Connection c = getCheckedConnection();
				PoolEntry pe = new PoolEntry(c, this, m_entryidgen++, c().getUid());
				m_freeList.add(pe);
				if(c().isSqlTraceMode())
					pe.setSqlTraceMode(true);
				m_pooledAllocatedCount++;
			}
			m_isPooled = true;
			System.out.println(m_pooledAllocatedCount + " connections allocated, okay.");
		} catch(SQLException x) {
			System.out.println("FAILED " + x.toString());
			throw x;
		}
	}

	/**
	 * Terminate the pool. Forces all connections closed.
	 */
	@GuardedBy("this")
	void destroyPool() {
		if(!m_manager.internalRemovePool(this))
			return;

		//-- We are no longer reachable from the pool manager- destroy ourself in piece.
		Set<PoolEntry> usedset;
		Stack<PoolEntry> freelist;
		synchronized(this) {
			if(m_destroyed)
				return;
			m_destroyed = true;

			usedset = m_usedSet;
			freelist = m_freeList;
			m_usedSet = new HashSet<PoolEntry>();
			m_freeList = new Stack<PoolEntry>();
			m_statementTotalPrepareCount = 0;
			m_n_open_rs = 0;
			m_n_open_stmt = 0;
			m_pooledUsedCount = 0;
			m_pooledAllocatedCount = 0;
			m_n_rows = 0;
			m_unpooledAllocatedCount = 0;
			m_isPooled = false;
			m_pooledMaxUsed = 0;
			m_peak_open_stmt = 0;
		}

		deinitPool(freelist);
		deinitPool(usedset);
	}

	/**
	 * Releases all connections. Connections that are used are forcefully aborted. Must be
	 * called outside locks.
	 */
	private void deinitPool(final Collection<PoolEntry> s) {
		List<ConnectionProxy> all = getUsedConnections(); // Get all currently used connections
		for(ConnectionProxy px : all) {
			try {
				px.forceInvalid();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	private synchronized void usable() {
		if(m_destroyed)
			throw new IllegalStateException("This pool(" + getID() + ") has been destroyed.");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Pool allocation primitives.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param what
	 * @param dbc
	 */
	private synchronized void dbgAlloc(final String what, final Connection dbc) {
		m_poolAllocationCount++;
		if(c().isLogAllocation() || c().isLogAllocationStack()) {
			System.out.println("DEBUG: pool(" + m_id + ") ALLOCATED connection " + dbc);
			if(c().isLogAllocationStack()) {
				System.out.println(DbPoolUtil.getLocation());
			}
		}
		if(!ALLOC.isLoggable(Level.FINE))
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("ALLOCATE pool(" + m_id + ") " + what + " database[allocated for pool=" + m_pooledAllocatedCount + ", allocated unpooled=" + m_unpooledAllocatedCount + "] pool[inuse=" + m_pooledUsedCount
			+ ", free=" + m_freeList.size() + "]");
		sb.append("\nConnection: " + dbc + "\n");
		DbPoolUtil.getThreadAndLocation(sb);
		ALLOC.fine(sb.toString());
	}

	public synchronized void dbgRelease(final String what, final Connection dbc) {
		if(c().isLogAllocation() || c().isLogAllocationStack()) {
			System.out.println("DEBUG: pool(" + m_id + ") CLOSED connection " + dbc + " (back to pool set)");
			if(c().isLogAllocationStack()) {
				System.out.println(DbPoolUtil.getLocation());
			}
		}
		if(!ALLOC.isLoggable(Level.FINE))
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("RELEASED pool(" + m_id + ") " + what + " database[allocated for pool=" + m_pooledAllocatedCount + ", allocated unpooled=" + m_unpooledAllocatedCount + "] pool[inuse=" + m_pooledUsedCount
			+ ", free=" + m_freeList.size() + "]");
		if(dbc != null)
			sb.append("\nConnection: " + dbc + "\n");
		DbPoolUtil.getThreadAndLocation(sb);
		ALLOC.fine(sb.toString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pool Entry allocation and release.					*/
	/*--------------------------------------------------------------*/
	/**
	 * This allocates a new connection from the pool, and waits max. 10 seconds
	 * if no such connection becomes available. If no connection can be allocated
	 * this returns null. In all cases where a wait is needed will the wait variable
	 * be incremented.
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
	private PoolEntry allocateConnectionInner(final boolean unpooled) throws SQLException {
		int newid = 0;
		long ets = -1; // No end time yet known

		synchronized(this) {
			//-- Inner loop: check if we CAN allocate any way. This loop blocks on connection expiry.
			for(;;) {
				usable();

				//-- 1. Is a connection available in the free pool?
				while(!m_freeList.isEmpty()) {
					PoolEntry pe = m_freeList.pop();
					m_usedSet.add(pe); // Saved used entry.
					pe.setUnpooled(unpooled); // Tell the entry whether it is a pooled one or not

					if(unpooled) {
						/*
						 * Unpooled connections are no longer part of the pool. Because
						 * this connection was gotten from the pooled set we decrement
						 * the "connections allocated" for the pool.
						 */
						m_pooledAllocatedCount--; // One less allocated in the poolset.
						m_unpooledAllocatedCount++; // And one more in use
						if(m_unpooledAllocatedCount > m_unpooledMaxUsed)
							m_unpooledMaxUsed = m_unpooledAllocatedCount;
					} else {
						//-- Unpooled connections influence the "used" count.
						m_pooledUsedCount++;
						if(m_pooledUsedCount > m_pooledMaxUsed)
							m_pooledMaxUsed = m_pooledUsedCount;
					}
					return pe;
				}

				//-- 2. No free connections. Can we allocate another one?
				if(m_pooledAllocatedCount < c().getMaxConns() || unpooled) {
					/*
					 * We may allocate a new connection. Update data to show
					 * that we allocate one, then break the loop to do the
					 * actual allocation OUTSIDE the lock. This makes sure that
					 * the connection count is not exceeded while we allocate
					 * another connection.
					 */
					if(!unpooled) {
						m_pooledAllocatedCount++; // Increment actual allocation count
						m_pooledUsedCount++; // One more used,
						if(m_pooledUsedCount > m_pooledMaxUsed)
							m_pooledMaxUsed = m_pooledUsedCount;
					} else {
						m_unpooledAllocatedCount++;
						if(m_unpooledAllocatedCount > m_unpooledMaxUsed)
							m_unpooledMaxUsed = m_unpooledAllocatedCount;
					}

					//-- Save data for the creation later,
					newid = m_entryidgen++;
					break; // Enter the "allocate new" code.
				}

				//-- 3. Auch! Nothing! Wait till a connection is released!!
				m_n_connectionwaits++;
				long cts = System.currentTimeMillis();
				if(ets == -1) {
					//-- This is the 1st time through the loop: set the end time;
					ets = cts + 10 * 1000; // Delay max. 10 secs
				} else if(cts >= ets) {
					//-- We have waited too long- return failure and release lock
					return null;
				}

				//-- We're allowed to wait.
				try {
					wait(10000); // Wait max. 10 secs,
				} catch(InterruptedException e) {
					throw new SQLException("dbPool " + m_id + ": interrupted while waiting for connection to become available");
				}
			}
		}

		/*
		 * When here we're NO longer locking the pool AND we are allowed to allocate a
		 * new connection. The connection has already been counted in. This
		 * must be done outside a lock because JDBC (Oracle driver) may lock also.
		 */
		boolean ok = false;
		PoolEntry pe = null;
		try {
			//-- Allocate a connection AND A new proxydude
			Connection c = getCheckedConnection();
			pe = new PoolEntry(c, this, newid, c().getUid());
			pe.setUnpooled(unpooled);
			if(c().isSqlTraceMode())
				pe.setSqlTraceMode(true);
			ok = true;
			return pe;
		} finally {
			//-- We need to handle accounting!!
			synchronized(this) {
				if(ok) {
					m_usedSet.add(pe);
				} else {
					//-- Decrement all counters that were upped assuming the code worked.
					if(!unpooled) {
						m_pooledAllocatedCount--;
						m_pooledUsedCount--;
					} else
						m_unpooledAllocatedCount--;
				}
			}
		}
	}


	/**
	 * Allocates a connection or aborts if it is impossible to do so within
	 * reasonable time. This is the "outer" loop part which calls {@link #allocateConnectionInner(boolean)} to
	 * try to allocate a connection from the pool or by creating a connection
	 * new if allowed. This inner method waits max. 10 seconds for a connection
	 * to become available if it is out of connections.
	 *
	 * <p>This outer loop handles the case where the inner loop cannot obtain a
	 * connection in 10 seconds. It loops for max. 6 times, and reports errors while
	 * it is looping. If no connection becomes available within this 60 seconds it will
	 * run the expired connection checker with the "force" flag. This should create
	 * at least some free connections. After that it fails, mostly.</p>
	 *
	 * @return
	 * @throws SQLException
	 */
	private PoolEntry allocateConnection(final boolean unpooled) throws SQLException {
		int ctries = 0;
		while(ctries < 6) { // Outer loop, unlocked
			PoolEntry pe = allocateConnectionInner(unpooled);
			if(null != pe) // No problems, allocation was fine
				return pe;

			//-- We failed and waited 10 seconds. Report a warning on the 2nd try.
			ctries++;
			String s = "pool[" + getID() + "]: no more connections available on " + ctries + " try!?";
			System.out.println(s);
			MSG.warning(s);
			if(ctries == 2) {
				StringBuilder sb = new StringBuilder(1024 * 1024);
				dumpUsedConnections(sb);
				String msg = sb.toString();
				saveError("No more database connections for pool=" + getID() + ", try 2..", "....");
				m_manager.panic("No more database pool connections for pool " + getID(), msg);
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


		}

		//-- We tried it way too many times.... Run the expiry scanner in FORCED mode.
		scanExpiredConnections(120, true); // All pooled connections not used for > 120 seconds will be forcefully closed.

		//-- Try once more to allocate a connection....
		PoolEntry pe = allocateConnectionInner(unpooled);
		if(null != pe) // No problems, allocation was fine
			return pe;

		//-- We're dyyyyyying.....
		synchronized(this) {
			m_n_connectionfails++;
		}
		throw new SQLException("PANIC: Could not obtain a database connection - pool is exhausted (and no connections can be forcefully released)!");
	}

	private void dumpUsedConnections(StringBuilder sb) {
		List<ConnectionProxy> cpl = getUsedConnections();
		StringPrinter sp = new StringPrinter(sb);
		for(ConnectionProxy px: cpl) {
			if(px.getState() == ConnState.OPEN) {
				DbPoolUtil.printTracepoints(sp, px, true);
			}
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
	void returnToPool(PoolEntry pe, final ConnectionProxy pc) throws SQLException {
		m_manager.removeThreadConnection(pc);

		//-- Before doing anything else reset the connection outside the lock,
		boolean ok = false;
		try {
			if(!pe.getConnection().getAutoCommit())
				pe.getConnection().rollback();
			pe.getConnection().setAutoCommit(true);
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
				if(ok) {
					if(unpooled && (m_pooledAllocatedCount >= c().getMaxConns() || !pe.getUserID().equals(c().getUid()))) // Unpooled are returned only when #allocated not too big,
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
						DbPoolUtil.printTracepoints(new StringPrinter(sb), pc, true);
						String msg = sb.toString();
						saveError(subj, msg);
						m_manager.panic(subj, msg);
						throw new IllegalStateException(subj);
					}
					if(unpooled) {
						m_unpooledAllocatedCount--; // Decrement #of unpooled, because this moves to pooled.
						m_pooledAllocatedCount++; // Unpooled means another allocated one now
					} else
						m_pooledUsedCount--; // Decrement pool use count for pooled,
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
	 * Called when a PoolEntry is forced closed. This must remove all references to that entry
	 * from the pool. The entry itself is already invalid and the caller will have locked
	 * this. The database connection itself is released elsewhere.
	 * @param pe
	 */
	synchronized void removeEntryFromPool(PoolEntry pe) {
		boolean unpooled = pe.isUnpooled();
		if(unpooled)
			m_unpooledAllocatedCount--; // #of unpooled is one down
		else {
			m_pooledUsedCount--;
			m_pooledAllocatedCount--; // One less allocated because it's connection will be/is freed.
		}
		if(!m_usedSet.remove(pe)) {
			//-- cannot happen.
			//			String subj = "pool(" + m_id + "): connection not in USED pool??";
			StringBuilder sb = new StringBuilder(65536);
			sb.append("POOLERR: Connection not in used pool! Location of release is:\n");
			DbPoolUtil.getThreadAndLocation(sb);
			String msg = sb.toString();
			System.out.println(msg);
		}
	}

	/**
	 * Called when an entry is invalid or must be released back to the OS. Called when
	 * an invalid PE is found during allocation, or when a returned PE is unneeded. When
	 * called the PE should be quiescent and not connected to any proxy.
	 *
	 * @param pe
	 */
	private void discardEntry(final PoolEntry pe) {
		String subj = null;
		String msg = null;
		synchronized(this) {
			if(!pe.isUnpooled()) { // Discarding pooled means current allocation count must be decremented.
				m_pooledAllocatedCount--; // One less allocated in the pool
				m_pooledUsedCount--; // And one less used,
			} else
				m_unpooledAllocatedCount--;
			//System.out.println("DISCARD pool=" + m_id + " connection discarded to server; conns=" + m_pooledAllocatedCount + ", #unpooled=" + m_unpooledAllocatedCount);

			if(!m_usedSet.remove(pe)) {
				subj = "pool(" + m_id + "): connection not in USED pool??";
				StringBuilder sb = new StringBuilder(65536);
				sb.append("Connection not in used pool! Location of release is:\n");
				DbPoolUtil.getThreadAndLocation(sb);
				//				sb.append("\n\nConnection stack dump:\n");
				//				DbPoolUtil.printTracepoints(sb, )
				msg = sb.toString();
				saveError(subj, msg);
			}

			try {
				notify();
			} catch(Exception x) {}
		}

		/*
		 * The PE is now fully removed from all pool knowledge. Just force it's connection closed.
		 */
		pe.closeResources();
		pe.releaseConnection();

		if(subj != null) {
			m_manager.panic(subj, msg);
			throw new IllegalStateException(subj);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing all connections.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Get a list of all ConnextionProxy's currently in use. It gets that list with
	 * "this" locked but locks nothing else. The entries returned are live, so by the
	 * time you are using them they can be dead (because they are closed/invalidated
	 * at that time).
	 * <p>We return the proxies, not the entries, because the proxies remain valid for
	 * a single use context. It means they are stable in time and have a single life
	 * cycle (life, dead).</p>
	 *
	 * @return
	 */
	public List<ConnectionProxy> getUsedConnections() {
		synchronized(this) {
			List<ConnectionProxy> res = new ArrayList<ConnectionProxy>(m_usedSet.size());
			for(PoolEntry pe : m_usedSet) {
				ConnectionProxy px = pe.getProxy();
				if(px == null) // cannot happen
					throw new IllegalStateException("POOLERR: proxy is null on USED entry=" + pe);
				res.add(px);
			}
			return res;
		}
	}

	/**
	 * This is the main entry to get a connection, either pooled or unpooled.
	 * @return
	 * @throws SQLException
	 */
	ConnectionProxy getConnection(final boolean unpooled) throws SQLException {
		IConnectionEventListener d = m_manager.getConnectionEventListener();
		for(;;) {
			PoolEntry pe = allocateConnection(unpooled);
			Exception x = checkConnection(pe.getConnection()); // Is the connection still valid?
			if(x == null) {
				ConnectionProxy dbc = pe.proxyMake(); // Yes-> make the proxy and be done.
				dbgAlloc("getConnection", dbc);
				if(!unpooled)
					PoolManager.getInstance().addThreadConnection(dbc);
				d.connectionAllocated(dbc);
				return dbc;
			}

			//-- This entry is INVALID.. Loop!
			MSG.info("Pool " + m_id + ": cached connection error, " + x.toString() + "; discarded.");
			discardEntry(pe); // Delete this soonest
		}
	}

	/**
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	public Connection getUnpooledConnection(String username, String password) throws SQLException {
		IConnectionEventListener d = m_manager.getConnectionEventListener();
		int newid;
		synchronized(this) {
			newid = m_entryidgen++;
		}

		boolean ok = false;
		PoolEntry pe = null;
		try {
			//-- Allocate a connection AND A new proxydude
			Connection c = getCheckedConnection(username, password);
			pe = new PoolEntry(c, this, newid, username);
			ok = true;
			pe.setUnpooled(true);

			ConnectionProxy dbc = pe.proxyMake(); // Yes-> make the proxy and be done.
			dbgAlloc("getUnpooledConnection", dbc);
			d.connectionAllocated(dbc);
			return dbc;
		} finally {
			//-- We need to handle accounting!!
			synchronized(this) {
				if(ok) {
					m_unpooledAllocatedCount++;
					m_usedSet.add(pe);
				}
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	The scan for hanging connections handler.		 	*/
	/*--------------------------------------------------------------*/
	/**
	 * This function gets called from the broker's janitor thread, OR from
	 * the purgatory handler (the thing called when all connections are used).
	 * @returns	T if the scan found and released "hanging" connections.
	 */
	public boolean scanExpiredConnections(final int scanIntervalInSeconds, boolean forcedisconnects) {
		if(c().getScanMode() == ScanMode.DISABLED && !forcedisconnects)
			return false;

		List<ConnectionProxy> proxylist = getUsedConnections(); // Atomically get assigned ones @ this time

		//-- Discard all proxies that find themselves too old.
		long ts = System.currentTimeMillis();
		long ets = ts - scanIntervalInSeconds * 1000; // Earliest time that's still valid
		HangCheckState hs = new HangCheckState(c().getScanMode(), ts, ets, forcedisconnects);

		for(ConnectionProxy cpx : proxylist) {
			cpx.checkHangState(hs); // Check if it's hanging; this does suicide on it if hanging.
		}

		/*
		 * Report the result of the hang check. At this point all actions have already been taken.
		 */
		synchronized(this) {
			m_n_hangdisconnects += hs.getDestroyCount();
			m_currentlyHangingConnections = hs.getHangingList();
		}

		//-- Always at least log the result @ sysout.
		String report = hs.getReport();
		if(report.length() > 0) {
			System.out.println("****Database Pool " + m_id + " Hanging Connections scan *******");
			System.out.println(report);
			System.out.println("Destroyed " + hs.getDestroyCount() + ", found " + hs.getHangCount() + " hanging pooled and " + hs.getUnpooledHangCount() + " hanging unpooled connections");
		}
		return hs.getDestroyCount() > 0;
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
	void handleConnectionUsageTime(final ConnectionProxy pe, final long ut) {
		//-- Add to statistics.
		int slot = getTimeSlot(ut);
		synchronized(m_usetime_ar) {
			m_usetime_ar[slot]++;
		}
		if(ut < m_conntime_warning_ms)
			return;
		//
		//		//-- !! Connection took a shitload of time! log!
		//		StringBuilder sb = new StringBuilder(1024);
		//		sb.append("** Connection was used for more than ");
		//		sb.append(Integer.toString(m_conntime_warning_ms));
		//		sb.append("ms: it took ");
		//		sb.append(Long.toString(ut));
		//		sb.append("ms from OPEN to CLOSE!!!\n");
		//
		//		sb.append("Stack trace of the connection:\n");
		//		pe.dbgPrintStackTrace(sb, 0, 0);
		//
		//		//-- Now: log and send to admin.
		//		m_manager.logUnexpected(sb.toString());
		//		m_manager.panic("Database connection used too long", sb.toString());
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
	public synchronized void setSaveErrors(final boolean on) {
		if(on && m_lastErrorStack == null)
			m_lastErrorStack = new ArrayList<ErrorEntry>();
		else if(!on && m_lastErrorStack != null)
			m_lastErrorStack = null;
	}

	public synchronized boolean hasSavedErrors() {
		return m_lastErrorStack != null && m_lastErrorStack.size() > 0;
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

	void logExecution(final StatementProxy sp, byte stmtType) {
		logExecution(sp, false, stmtType);
	}

	/**
	 * Callback from statement pxy when a call gets executed.
	 * @param ppx
	 */
	void logExecution(final StatementProxy sp, final boolean batch, byte stmtType) {
		writeStatement(sp, stmtType);

		if(!c().isLogStatements())
			return;

		StringBuilder sb = new StringBuilder();
		sb.append("   ");
		sb.append(DbPoolUtil.strTimeOnly(new Date()));
		sb.append(batch ? " dbg batch=" : " dbg sql=");
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

	void logAction(ConnectionProxy cp, String action) {
		if(!c().isLogStatements())
			return;

		StringBuilder sb = new StringBuilder();
		sb.append("   ");
		sb.append(DbPoolUtil.strTimeOnly(new Date()));
		sb.append(' ');
		sb.append(action);
		sb.append(", connection=");
		sb.append(cp.toString());
		System.out.println(sb.toString());
	}

	void logBatch() {
		if(!c().isLogStatements())
			return;
		System.out.println("    executeBatch()");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Logfile writer.										*/
	/*--------------------------------------------------------------*/

	private boolean m_fileLogging;

	private Thread m_logWriterThread;

	private List<byte[]> m_logBufferList = new ArrayList<byte[]>();

	static private final int MAX_LOG_QUEUED = 30;

	private OutputStream m_fileLogStream;

	static public final long STMT_START_MAGIC = 0xabbacafebabedeadl;

	public boolean isFileLogging() {
		synchronized(m_logBufferList) {
			return m_fileLogging;
		}
	}

	public void setFileLogging(File target) {
		synchronized(m_logBufferList) {
			if(m_fileLogging)
				throw new IllegalArgumentException("File logging is already enabled");
			try {
				m_fileLogStream = new FileOutputStream(target, true);

			} catch(Exception x) {
				System.out.println("pool(" + getID() + ") cannot open logging file " + target + ": " + x);
				return;
			}

			m_fileLogging = true;
		}
	}

	private void writeStatement(StatementProxy ls, byte stmtType) {
		if(!isFileLogging())
			return;

		byte[] buffer;
		try {
			buffer = createLogImage(ls, stmtType);
		} catch(Exception x) {
			System.out.println("pool(" + getID() + ") failed to create statement image, statement ignored: " + x);
			return;
		}
		writeLogImage(buffer);
	}

	public void writeSpecial(ConnectionProxy cp, byte stmtType) {
		if(!isFileLogging())
			return;

		writeLogImage(createSpecialImage(cp, stmtType));
	}

	private void writeLogImage(byte[] buffer) {
		synchronized(m_logBufferList) {
			//-- Make sure writer thread is active
			if(null == m_logWriterThread) {
				m_logWriterThread = new Thread(new Runnable() {
					@Override
					public void run() {
						logWriterWriteLoop();
					}
				});
				m_logWriterThread.setName("dblgwr");
				m_logWriterThread.setDaemon(true);
//				m_logWriterThread.setPriority(Thread.MAX_PRIORITY);
				m_logWriterThread.start();
			}

			for(;;) {
				if(!m_fileLogging) // Accept disabling of log due to error
					return;

				if(m_logBufferList.size() < MAX_LOG_QUEUED) {
					m_logBufferList.add(buffer);
					m_logBufferList.notifyAll();
					return;
				}

				//-- Too many queued- wait.
				try {
					m_logBufferList.wait();
				} catch(InterruptedException x) {
					System.out.println("pool(" + getID() + ") interrupted log write- cancelled");
					return;
				}
			}
		}
	}

	private void logWriterWriteLoop() {
		try {
			for(;;) {
				byte[] buf = waitForBuffer();
				writeBuffer(buf);
			}
		} catch(Exception x) {
			System.out.println("pool(" + getID() + ") statement log write error " + x + ": logging cancelled");
			x.printStackTrace();
		} finally {
			synchronized(m_logWriterThread) {
				m_fileLogging = false; // Disable file logging.
				m_logBufferList.clear(); // Discard anything queued.
				m_logBufferList.notifyAll();
				m_logWriterThread = null;
			}
		}
	}

	private void writeBuffer(byte[] buf) throws Exception {
		m_fileLogStream.write(buf);
	}

	private byte[] waitForBuffer() throws InterruptedException {
		for(;;) {
			synchronized(m_logBufferList) {
				if(m_logBufferList.size() > 0) {
					byte[] buf = m_logBufferList.remove(0);

					if(m_logBufferList.size() == MAX_LOG_QUEUED - 1) {
						m_logBufferList.notify(); // Wake one writer
					}
					return buf;
				}
				m_logBufferList.wait();
			}
		}
	}

	/**
	 * Create a statement image record for the executed statement.
	 * @param ls
	 * @return
	 * @throws IOException
	 */
	private byte[] createLogImage(StatementProxy ls, byte stmtType) throws IOException {
		ByteArrayOutputStream baos = createImageBuilder(ls._conn(), stmtType);
		writeString(baos, ls.getSQL());
		if(ls instanceof PreparedStatementProxy) {
			PreparedStatementProxy ps = (PreparedStatementProxy) ls;
			Object[] par = ps.internalGetParameters();
			if(par.length <= 0) {
				writeInt(baos, 0);
			} else {
				writeInt(baos, par.length); // #of parameters following
				for(int i = 0; i < par.length; i++) {
					writeParameter(baos, par[i]);
				}
			}
		} else {
			writeInt(baos, 0);			// Zero parameters.
		}

		return baos.toByteArray();
	}

	private byte[] createSpecialImage(ConnectionProxy cp, byte stmtType) {
		ByteArrayOutputStream baos = createImageBuilder(cp, stmtType);
		return baos.toByteArray();
	}

	private ByteArrayOutputStream createImageBuilder(ConnectionProxy cp, byte stmtType) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
		writeLong(baos, STMT_START_MAGIC);
		baos.write(stmtType); // Indicator for execution type. Must be changed later.
		long ts = System.currentTimeMillis();
		writeLong(baos, ts);
		writeInt(baos, cp.getId());
		return baos;
	}


	private void writeParameter(ByteArrayOutputStream baos, Object object) throws IOException {
		if(null == object) {
			baos.write('0');
		} else if(object instanceof Integer) {
			baos.write('i');
			writeInt(baos, ((Integer) object).intValue());
		} else if(object instanceof Long) {
			baos.write('l');
			writeLong(baos, ((Long) object).longValue());
		} else if(object instanceof BigDecimal) {
			baos.write('B');
			writeString(baos, object.toString());
		} else if(object instanceof Double) {
			baos.write('d');
			writeString(baos, object.toString());
		} else if(object instanceof Float) {
			baos.write('f');
			writeString(baos, object.toString());
		} else if(object instanceof String) {
			baos.write('$');
			writeString(baos, (String) object);
		} else if(object instanceof Date) {
			baos.write('T');
			Date ts = (Date) object;
			writeLong(baos, ts.getTime());
		} else {
			baos.write('?');
			writeString(baos, object.getClass().getName());
		}
	}

	static private void	writeInt(ByteArrayOutputStream os, int v) {
		os.write((v >> 24) & 0xff);
		os.write((v >> 16) & 0xff);
		os.write((v >> 8) & 0xff);
		os.write(v & 0xff);
	}

	static private void writeLong(ByteArrayOutputStream os, long v) {
		writeInt(os, (int) (v >> 32));
		writeInt(os, (int) v);
	}

	static private void writeString(ByteArrayOutputStream os, String s) throws IOException {
		try {
			byte[] data = s.getBytes("utf-8");
			writeInt(os, data.length);
			os.write(data);
		} catch(UnsupportedEncodingException x) {
			throw new RuntimeException(x);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Small data access functions.						*/
	/*--------------------------------------------------------------*/
	public String getID() {
		return m_id;
	}

	/**
	 * Return the owner pool manager.
	 * @return
	 */
	public final PoolManager getManager() {
		return m_manager;
	}

	/**
	 * Called to get the "is connection OK" sql command for this
	 * pool.
	 *
	 * @return
	 * @throws SQLException
	 */
	private synchronized String getCheckString() throws SQLException {
		return m_check_calc;
	}

	synchronized public boolean isPooledMode() {
		return m_isPooled;
	}


	public DataSource getUnpooledDataSource() {
		return m_unpooled_ds;
	}

	public DataSource getPooledDataSource() {
		return m_pooled_ds;
	}

	/**
	 * When set &gt; 0, this will call setTimeout on all statements and calls.
	 * @param timeout
	 */
	public void setForceTimeout(int timeout) {
		m_forceTimeout = timeout;
	}

	public int getForceTimeout() {
		return m_forceTimeout;
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Per-thread configuration (JUnit tests et al).		*/
	/*--------------------------------------------------------------*/
	/**
	 * This enables or disables commits for connections allocated/used by <b>this</b> thread. Primary
	 * use is for JUnit tests, preventing them from changing the database.
	 * @since 2011/08/12
	 * @param on
	 */
	public void setCommitDisabled(boolean on) {
		ThreadConfig tc = m_threadConfig.get();
		if(tc == null) {
			if(!on) // Not disabled and no config -> fine already
				return;
			tc = new ThreadConfig();
			m_threadConfig.set(tc);
		}
		tc.setDisableCommit(on);
	}

	/**
	 * Returns T if commits are disabled for the current thread. This can be used
	 * to prevent JUnit tests from changing the database.
	 * @since 2011/08/12
	 * @return
	 */
	public boolean isCommitDisabled() {
		ThreadConfig tc = m_threadConfig.get();
		if(tc == null)
			return false;
		return tc.isDisableCommit();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Access to statistics.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Copies all pool data into the poolStats structure. This provides a point-in-time copy of all
	 * pool statistics in proper relation.
	 */
	public synchronized PoolStats getPoolStatistics() {
		return new PoolStats(m_unpooledAllocatedCount, m_pooledAllocatedCount, m_pooledUsedCount, //
			m_pooledMaxUsed, m_poolAllocationCount, m_n_connectionwaits, //
			m_n_connectionfails, m_n_hangdisconnects, m_n_open_stmt, //
			m_peak_open_stmt, m_n_open_rs, m_statementTotalPrepareCount, //
			m_n_rows, //
			new ArrayList<ConnectionProxy>(m_currentlyHangingConnections), //
			m_databaseAllocationCount, //
			m_unpooledMaxUsed
		);
	}

	synchronized void incOpenStmt() {
		m_n_open_stmt++;
		m_statementTotalPrepareCount++;
		if(m_n_open_stmt > m_peak_open_stmt)
			m_peak_open_stmt = m_n_open_stmt;
	}

	synchronized void decOpenStmt() {
		m_n_open_stmt--;
	}

	//	synchronized void decOpenStmt(final int count) {
	//		m_n_open_stmt -= count;
	//	}

	synchronized void incOpenRS() {
		m_n_open_rs++;
	}

	synchronized void decOpenRS() {
		m_n_open_rs--;
	}

	public synchronized void setAttribute(@Nonnull String name, @Nullable Object value) {
		m_attributeMap.put(name, value);
	}

	public synchronized Object getAttribute(@Nonnull String name) {
		return m_attributeMap.get(name);
	}

	public synchronized <T> T getOrCreateAttribute(@Nonnull String name, @Nonnull java.util.function.Supplier<T> supplier) {
		T value = (T) m_attributeMap.get(name);
		if(null == value) {
			value = supplier.get();
			m_attributeMap.put(name, value);
		}
		return value;
	}

	@Nullable public IConnectionStatisticsFactory getConnectionStatisticsFactory() {
		return m_connectionStatisticsFactory;
	}
}
