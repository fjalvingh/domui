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
import java.sql.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import javax.sql.*;

/**
 * Root of the database pool manager code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class PoolManager {
	/** The table of pools (ConnectionPool), identified by ID */
	@GuardedBy("this")
	private final Map<String, ConnectionPool> m_poolMap = new HashMap<String, ConnectionPool>();

	@GuardedBy("this")
	private List<IPoolMessageHandler> m_listenerList = new ArrayList<IPoolMessageHandler>();

	static final private Object m_connidlock = new Object();

	@GuardedBy("m_connidlock")
	static private int m_nextconnid;

	private volatile boolean m_collectStatistics;

	/** Threadlocal containing the per-thread collected statistics, per request. */
	private final ThreadLocal<IConnectionEventListener> m_connectionEventListener = new ThreadLocal<IConnectionEventListener>();

	/** The shared global instance of a pool manager */
	static final private PoolManager m_instance = new PoolManager();

	static int nextConnID() {
		synchronized(m_connidlock) {
			return ++m_nextconnid;
		}
	}

	static public PoolManager getInstance() {
		return m_instance;
	}

	public void panic(final String shortdesc, final String body) {
		sendPanic(shortdesc, body);
	}

	public void logUnexpected(final Exception t, final String s) {
		sendLogUnexpected(t, s);
	}

	public void logUnexpected(final String s) {
		sendLogUnexpected(null, s);
	}

	synchronized public void addMessageListener(final IPoolMessageHandler pmh) {
		m_listenerList = new ArrayList<IPoolMessageHandler>(m_listenerList);
		m_listenerList.add(pmh);
	}

	synchronized public void removeMessageListener(final IPoolMessageHandler pmh) {
		m_listenerList = new ArrayList<IPoolMessageHandler>(m_listenerList);
		m_listenerList.remove(pmh);
	}

	synchronized private List<IPoolMessageHandler> getListeners() {
		return m_listenerList;
	}

	public void sendLogUnexpected(final Exception t, final String s) {
		for(IPoolMessageHandler pmh : getListeners()) {
			try {
				pmh.sendLogUnexpected(t, s);
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendPanic(final String shortdesc, final String body) {
		for(IPoolMessageHandler pmh : getListeners()) {
			try {
				pmh.sendPanic(shortdesc, body);
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing pools.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the #of pools currently defined.
	 * @return
	 */
	public synchronized int getPoolCount() {
		return m_poolMap.size();
	}

	/**
	 * Finds the named pool. Throws an exception if not found.
	 */
	@Nonnull
	public ConnectionPool getPool(@Nonnull final String id) throws SQLException {
		if(id == null)
			throw new IllegalArgumentException("The pool ID cannot be null");
		synchronized(this) {
			ConnectionPool pool = m_poolMap.get(id); // Find the pool
			if(pool == null)
				throw new SQLException("PoolManager: pool with ID " + id + " not found.");
			return pool;
		}
	}

	/**
	 * Return all currently defined pools.
	 * @return
	 */
	public ConnectionPool[] getPoolList() {
		synchronized(this) {
			return m_poolMap.values().toArray(new ConnectionPool[m_poolMap.size()]);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Defining and initializing pools.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Register a new pool with the manager and allow duplicate definitions.
	 */
	private ConnectionPool addPool(ConnectionPool newpool) throws SQLException {
		synchronized(this) {
			//-- If this pool is already there then compare else add the new one
			ConnectionPool cp = m_poolMap.get(newpool.getID());
			if(cp != null) { // Pool exists.. Must have same parameters,
				if(!cp.c().equals(newpool.c()))
					throw new SQLException("PoolManager: database pool with duplicate id " + newpool.getID() + " is being defined with DIFFERENT parameters as the original.");
				return cp; // Exists and the same: return original pool
			}
			m_poolMap.put(newpool.getID(), newpool); // And define it,
			return newpool;
		}
	}

	public ConnectionPool definePool(String id, PoolConfig pc) throws SQLException {
		//-- Create a new pool structure,
		ConnectionPool newpool = new ConnectionPool(this, id, pc);
		newpool.checkParameters(); // Check all parameters outside any lock.
		return addPool(newpool);
	}

	/**
	 * Defines the pool with the specified ID from the ConfigSource passed.
	 * The pool is defined but NOT initialized. The same pool can be
	 * defined more than once provided it's parameters are all the same. After
	 * this call the pool can be used but it operates in "unpooled" mode, meaning
	 * that all allocated connections will be discarded after a close.
	 */
	public ConnectionPool definePool(final PoolConfigSource cs, final String id) throws SQLException {
		PoolConfig pc = new PoolConfig(id, cs);
		return definePool(id, pc);
	}

	public ConnectionPool definePool(final String id, final String driver, final String url, final String userid, final String password, final String driverpath) throws SQLException {
		PoolConfig pc = new PoolConfig(driver, url, userid, password, driverpath);
		return definePool(id, pc);
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
			File xf = new File(uh, ".dbpool.xml");
			if(xf.exists()) {
				return definePool(xf, id);
			}

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
	/*	CODING:	Pool management and teardown.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Destroy all current pools.
	 */
	public void destroyAll() {
		List<ConnectionPool> l;
		synchronized(this) {
			l = new ArrayList<ConnectionPool>(m_poolMap.values());
			m_poolMap.clear();
		}
		for(ConnectionPool p : l) {
			try {
				p.destroyPool();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public void destroyPool(String poolid) throws SQLException {
		ConnectionPool pool = getPool(poolid);
		pool.destroyPool();
	}

	/**
	 * Callback from pool.destroyPool to remove the pool from the manager before it is destroyed. No locks are
	 * held when called.
	 * Since we can be called for an already destroyed pool make sure the pool in the map is the
	 * one we're destroying...
	 *
	 * @param id
	 */
	@GuardedBy("this")
	synchronized boolean internalRemovePool(ConnectionPool cp) {
		ConnectionPool xp = m_poolMap.get(cp.getID());
		if(null == xp)
			return false; // Already removed
		if(xp != cp) // Different copy is active; old one was removed already
			return false;
		m_poolMap.remove(cp.getID());
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pool scanner.										*/
	/*--------------------------------------------------------------*/
	private final int m_dbpool_scaninterval = 120;

	private Thread m_scanthread;

	/**
	 * Starts the scanner if database locking security is requested.
	 */
	synchronized protected void startExpiredConnectionScanner() {
		if(m_dbpool_scaninterval == 0 || m_scanthread != null)
			return;

		//-- Start the task,
		try {
			m_scanthread = new Thread(new Runnable() {
				@Override
				public void run() {
					expiredConnectionScannerLoop();
				}
			});
			m_scanthread.setDaemon(true);
			m_scanthread.setName("DbPoolScanner");
			m_scanthread.start();
		} catch(Exception x) {
			x.printStackTrace();
			logUnexpected(x, "in starting dbpool scanner");
		}
	}

	/**
	 * FIXME Must terminate when manager is closed.
	 */
	void expiredConnectionScannerLoop() {
		//System.out.println("PoolManager: expired connection scanning thread started.");
		for(;;) {
			try {
				scanExpiredConnectionsOnce(m_dbpool_scaninterval);
				Thread.sleep(m_dbpool_scaninterval * 1000 / 2);
			} catch(Exception x) {
				logUnexpected(x, "In scanning for expired connections");
			}
		}
	}

	/**
	 * The janitor task which scans for unreleased database connections.
	 */
	private void scanExpiredConnectionsOnce(final int scaninterval_in_secs) {
		//-- Righty right. Get a list of ALL pools and scan them for old shit.
		ConnectionPool[] ar = getPoolList();
		for(ConnectionPool p : ar)
			p.scanExpiredConnections(scaninterval_in_secs, false);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Per-thread data collection.							*/
	/*--------------------------------------------------------------*/

	/**
	 * This returns the single per-thread connection event listener. If no
	 * listener has been set it returns a dummy one that does nothing.
	 */
	@Nonnull
	IConnectionEventListener getConnectionEventListener() {
		IConnectionEventListener ih = m_connectionEventListener.get();
		if(ih == null) {
			ih = DummyConnectionEventListener.INSTANCE;
			m_connectionEventListener.set(ih);
		}
		return ih;
	}

	/**
	 * Registers a statistics collection listener for the current thread. If statistics gathering
	 * is disabled the call is ignored and returns false. Else the listener is added and will
	 * receive all statement events; in this case the call returns true.
	 */
	public boolean startCollecting(String key, final IStatisticsListener collector) {
		synchronized(this) {
			if(!m_collectStatistics)
				return false;
		}
		IConnectionEventListener ih = m_connectionEventListener.get();
		if(ih == null || ih instanceof DummyConnectionEventListener) {
			ih = new CollectingConnectionEventListener(new StatisticsListenerMultiplexer());
			m_connectionEventListener.set(ih);
		}
		CollectingConnectionEventListener cih = (CollectingConnectionEventListener) ih;
		StatisticsListenerMultiplexer sink = (StatisticsListenerMultiplexer) cih.getListener();
		sink.addCollector(key, collector);
		return true;
	}

	/**
	 * Returns the collector with the specified key. If statistics collection is not enabled
	 * this returns null always; else it returns and removes the collector- if found.
	 * @param key
	 * @return
	 */
	public IStatisticsListener stopCollecting(String key) {
		IConnectionEventListener ih = m_connectionEventListener.get();
		if(ih == null || !(ih instanceof CollectingConnectionEventListener))
			return null;

		CollectingConnectionEventListener cih = (CollectingConnectionEventListener) ih;
		StatisticsListenerMultiplexer sink = (StatisticsListenerMultiplexer) cih.getListener();
		IStatisticsListener ic = sink.removeCollector(key); 	// Remove collector.
		if(null != ic)
			ic.finish();
		return ic;
	}

	public void setCollectStatistics(final boolean on) {
		m_collectStatistics = on;								// volatile
	}

	public boolean isCollectStatistics() {
		return m_collectStatistics;								// volatile
	}

	/**
	 * Add an event listener to the specified connection. The connection must be one that is allocated
	 * through this pool manager or an exception is thrown.
	 * @param dbc
	 * @param el
	 */
	static public void addListener(@Nonnull Connection dbc, @Nonnull IDatabaseEventListener el) {
		if(!(dbc instanceof ConnectionProxy))
			throw new IllegalArgumentException("The connection passed MUST have been allocated by this pool manager (it is a " + dbc + ")");
		((ConnectionProxy) dbc).addCommitListener(el);
	}

	/**
	 * Remove an event listener that was added before from the specified connection. The connection must be one that is allocated
	 * through this pool manager or an exception is thrown.
	 * @param dbc
	 * @param el
	 */
	static public void removeListener(@Nonnull Connection dbc, @Nonnull IDatabaseEventListener el) {
		if(!(dbc instanceof ConnectionProxy))
			throw new IllegalArgumentException("The connection passed MUST have been allocated by this pool manager (it is a " + dbc + ")");
		((ConnectionProxy) dbc).removeCommitListener(el);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Thread connection info (test for unclosed conns).	*/
	/*--------------------------------------------------------------*/

	private boolean m_checkCloseConnections;

	final private ThreadLocal<Set<ConnectionProxy>> m_threadConnections = new ThreadLocal<Set<ConnectionProxy>>();

	public synchronized boolean isCheckCloseConnections() {
		return m_checkCloseConnections;
	}

	public synchronized void setCheckCloseConnections(boolean checkCloseConnections) {
		m_checkCloseConnections = checkCloseConnections;
	}

	static private final boolean DEBUG = false;

	/**
	 * @param cx
	 */
	void addThreadConnection(ConnectionProxy cx) {
		if(!isCheckCloseConnections())
			return;
		Set<ConnectionProxy> cs = m_threadConnections.get();
		if(null == cs) {
			cs = new HashSet<ConnectionProxy>();
			m_threadConnections.set(cs);
		}
		cs.add(cx);
		if(DEBUG)
			System.out.println("PM: " + Thread.currentThread().getName() + " add proxy " + cx);
	}

	void removeThreadConnection(ConnectionProxy cx) {
		if(!isCheckCloseConnections())
			return;
		Set<ConnectionProxy> cs = m_threadConnections.get();
		if(DEBUG)
			System.out.println("PM: " + Thread.currentThread().getName() + " remove proxy " + cx + (cs == null ? "FAILED - NO SET" : ""));
		if(null == cs)
			return;
		cs.remove(cx);
	}

	public List<ConnectionProxy> getThreadConnections() {
		if(!isCheckCloseConnections())
			return Collections.EMPTY_LIST;
		Set<ConnectionProxy> cs = m_threadConnections.get();
		if(null == cs)
			return Collections.EMPTY_LIST;

		/*
		 * When a connection is closed by another thread (which is a bug, but which happens in special circumstances,
		 * like when logging out with another thread still running using a connection). This prevents "false alarms"
		 * when a connection is still in a thread's unclosed list but closed by another thread.
		 */
		List<ConnectionProxy> res = new ArrayList<ConnectionProxy>(cs.size());
		for(ConnectionProxy cp : cs) {
			if(cp.getState() == ConnState.OPEN)
				res.add(cp);
		}

		return res;
	}

	public void clearThreadConnections() {
		if(!isCheckCloseConnections())
			return;
		m_threadConnections.set(null);
	}

	/**
	 * Mark connection long living, for those connections which need
	 * to be opened for a long time (hanging connection check will skip
	 * this connection).
	 *
	 * @param dbc
	 * @throws SQLException
	 */
	static public void setLongLiving(@Nonnull Connection dbc) throws SQLException {
		ConnectionProxy proxy = dbc.unwrap(ConnectionProxy.class);
		proxy.setLongliving(true);
	}

	@Nullable
	static public ConnectionPool getPoolFrom(@Nonnull DataSource ds) {
		//-- Try to locate the unpooled data source for this database pool.
		if(ds instanceof DataSourceImpl) {
			return ((DataSourceImpl) ds).getPool();
		} else if(ds instanceof UnpooledDataSourceImpl) {
			UnpooledDataSourceImpl udi = (UnpooledDataSourceImpl) ds;
			return udi.getPool();
		} else {
			return null;
		}
	}

	/**
	 * Try to get the unpooled datasource associated with this datasource, if that
	 * datasource comes from the pool manager.
	 * @param ds
	 * @return
	 */
	@Nonnull
	static public DataSource	getUnpooledFrom(@Nonnull DataSource ds) {
		ConnectionPool cp = getPoolFrom(ds);
		if(null == cp)
			return ds;
		return cp.getUnpooledDataSource();
	}

}

