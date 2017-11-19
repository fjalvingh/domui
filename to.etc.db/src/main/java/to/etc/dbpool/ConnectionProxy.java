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

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;

/**
 * This "implements" Connection, and is a proxy to the actual
 * connection maintained in ConnectionPoolEntry. An instance
 * is created every time a connection is allocated from the
 * pool and returned to the user. This instance is never reused
 * and becomes invalid after close() is called on it.
 * <p>This also contains all of the data associated with a connection,
 * for debugging, logging and statistics gathering. Most of the
 * data herein is locked by this.
 *
 * FIXME Must implement java.sql.PooledConnection.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class ConnectionProxy implements Connection {
	static private final int MAX_TRACEDEPTH = 20;

	/** The Pool Entry for this connection; it can NEVER be NULL!!! */
	final private PoolEntry m_pe;

	/** The ID for this proxy */
	final private int m_id;

	/** The thread who owns this connection. Always filled-in */
	final private Thread m_ownerThread;

	final private boolean m_unpooled;

	/**
	 * T if stack traces for entry must be saved. Copies {@link ConnectionPool#dbgIsStackTraceEnabled()}. A local
	 * copy is needed to prevent us from having to lock the pool.
	 */
	final private boolean m_saveTracePoints;

	/** This connection's state. Only connections in state OPEN are usable; all others abort immediately on use. In addition, only in state OPEN will this be the PoolEntry's proxy. */
	private ConnState m_state = ConnState.OPEN;

	private Tracepoint m_closeLocation;

	/*--------------- Debug and trace info ----------------*/
	/** The location etc denoting the allocation point for this connection. */
	private Tracepoint m_allocationPoint;

	/** The time that the connection was allocated in CONNTIME is true, empty otherwise */
	final private long m_allocationTS;

	/** The list of tracepoints, if usage tracing is enabled. */
	@Nullable
	private List<Tracepoint> m_tracePointList;

	/** The last time the connection was used (proxyCheck). */
	private long m_lastUsedTS;

	/** The #of times a warning has been sent that this connection is old. */
	private int m_expiryWarningCount;

	/*--------------- Non-pool related stuff ----------------*/
	/** T if this connection is in AutoCommit mode. */
	private boolean m_autocommit;

	/** If set this connection will ignore close requests. To close the connection one MUST call closeThreadConnections() on the PoolManager, or call closeForced(). */
	private boolean m_unclosable;

	/** A list of objects containing extra info on ownership and use. */
	private List<Object> m_infoObjects = Collections.EMPTY_LIST;

	/** All commit event listeners. */
	private List<IDatabaseEventListener> m_commitListenerList = Collections.EMPTY_LIST;

	/** If set this connection is marked long living and will be excluded from hanging connection checks. */
	private boolean m_longliving;

	/**
	 *	Creates new connection. This is the only way to attach one to the PoolEntry.
	 */
	ConnectionProxy(final PoolEntry pe, final int id, final Thread ownerThread, boolean tracepoints, boolean isunpooled) {
		m_pe = pe;
		m_autocommit = true;
		m_id = id;
		m_ownerThread = ownerThread;
		m_saveTracePoints = tracepoints;
		m_allocationTS = System.currentTimeMillis();
		m_lastUsedTS = m_allocationTS;
		m_allocationPoint = Tracepoint.create(null);
		m_unpooled = isunpooled;
	}

	/**
	 * Return the immutable allocation time.
	 * @return
	 */
	public long getAllocationTime() {
		return m_allocationTS;
	}

	public Tracepoint getAllocationPoint() {
		return m_allocationPoint;
	}

	public Thread getOwnerThread() {
		return m_ownerThread;
	}

	public String getPoolID() {
		return m_pe.getPool().getID();
	}

	public int getId() {
		return m_id;
	}

	@Nonnull
	protected IConnectionEventListener statsHandler() {
		return m_pe.getPool().getManager().getConnectionEventListener();
	}

	public ConnectionPool getPool() {
		return m_pe.getPool();
	}

	/**
	 * THIS MAY ONLY LOCK THIS AND MUST BE IMMUTABLE.
	 * @return
	 */
	public final boolean isUnpooled() {
		return m_unpooled;
	}

	synchronized void setLongliving(final boolean longliving) {
		m_longliving = longliving;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("ConnectionProxy[");
		sb.append(m_id);
		sb.append(':');
		m_pe.appendDesc(sb);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Adds owner info objects to the connection. These will be rendered at debug time, when needed.
	 * @param oo
	 */
	public synchronized void addOwnerInfo(Object oo) {
		if(m_infoObjects == Collections.EMPTY_LIST)
			m_infoObjects = new ArrayList<Object>();
		m_infoObjects.add(oo);
	}

	public synchronized ConnState getState() {
		return m_state;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing pool related state.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the REAL database connection (the one obtained from the JDBC driver)
	 * for this proxy. LOCKS THIS.
	 * @return	the connection
	 */
	public Connection getRealConnection() {
		return check();
	}

	/**
	 * Checks if this database connection is still open; if not it throws an
	 * exception. Logs tracepoint and updates last-used timestamp. LOCKS THIS.
	 */
	private synchronized Connection check(String sql) {
		if(m_state != ConnState.OPEN)
			throw new InvalidProxyException("Connection " + this + " is closed, it's state is " + m_state);
		saveTracepoint(sql);
		return m_pe.getConnection();
	}

	private synchronized Connection checkNoSave() {
		if(m_state != ConnState.OPEN)
			throw new InvalidProxyException("Connection " + this + " is closed, it's state is " + m_state);
		return m_pe.getConnection();
	}

	private synchronized Connection check() {
		return check(null);
	}
	private synchronized void usable() {
		if(m_state != ConnState.OPEN)
			throw new InvalidProxyException("Connection " + this + " is closed, it's state is " + m_state);
	}

	/**
	 * The "normal" close releases all resources associated with this connection by closing
	 * them, then it returns the connection to the pool. If the connection is already closed
	 * then nothing happens (multiple closes are allowed). LOCKS THIS.
	 * THIS CALL MAY ONLY BE CALLED BY USER CODE, NOT BY POOL CODE!!!
	 */
	public void close() throws SQLException {
		//		StringTool.dumpLocation("Closing connection normally using close, connection="+this+", not-closeable="+m_not_closable);
		if(!m_unclosable) {
			// jal 20101103 The code below is wrong!! this causes commit of data if autocommit WAS false and close is called because returnToPool will not rollback then!
			//			if(!m_autocommit) {
			//				try {
			//					setAutoCommit(true);
			//				} catch(Exception x) {
			//					x.printStackTrace();
			//				}
			//			}
			forceClosed();
		}
	}

	/**
	 * Actually closes a proxy, if not already done. This also sets the close
	 * reason and location. This can be called from pool code.
	 * Can be called for connections that ignore the normal close operation to
	 * force the proxy closed. The connection is returned to the pool proper.
	 *
	 * @throws SQLException
	 */
	public void forceClosed() throws SQLException {
		m_pe.getPool().logAction(this, "close()");
		synchronized(this) {
			if(m_state != ConnState.OPEN)			// 20121025 jal must check here to prevent double close from calling all listeners again.
				return;
		}

		//-- Call any listeners.
		for(IDatabaseEventListener icl : m_commitListenerList) {
			try {
				icl.onBeforeRelease(this);
			} catch(Exception x) {
				PoolManager.getInstance().logUnexpected(x, "Ignoring Exception in onBeforeRelease");
			}
		}
		m_commitListenerList = Collections.EMPTY_LIST;

		statsHandler().connectionClosed(this);

		getPool().writeSpecial(this, StatementProxy.ST_CLOSE);

		//-- Handle local chores locking THIS
		Tracepoint tp = Tracepoint.create(null);
		long duration;
		synchronized(this) {
			if(m_state != ConnState.OPEN)
				return;										// Already invalidated or closed.
			m_state = ConnState.CLOSED;
			m_closeLocation = tp;

			//-- Handle "long connection usage" stuff.
			duration = tp.getTimestamp() - m_allocationTS;	// Get #of ms used.
		}

		/*
		 * At this point the PoolEntry is still in use (so unreachable for others) but this proxy
		 * is fully invalidated. Now release the poolentry outside locks.
		 */
		m_pe.release(this);
		getPool().handleConnectionUsageTime(this, duration);
	}

	/**
	 * Called to invalidate a PoolEntry. This gets called from the expired connection
	 * scanner when this was found to be too old. Before it actually invalidates
	 * it makes sure no other process has closed this in the meantime. LOCKS THIS.
	 *
	 * @throws SQLException
	 */
	void forceInvalid() throws SQLException {
		//-- Handle local chores locking THIS
		Tracepoint tp = Tracepoint.create(null);
		synchronized(this) {
			if(m_state != ConnState.OPEN)
				return; // Already invalidated or closed.
			m_state = ConnState.INVALIDATED;
			m_closeLocation = tp;
		}

		/*
		 * At this point the PoolEntry is still in use (so unreachable for others) but this proxy
		 * is fully invalidated. Now release the poolentry outside locks.
		 */
		m_pe.invalidate(this);
		m_pe.getPool().logAction(this, "invalidate()");
	}

	/**
	 * Commit proxies to the real connection, but also handles the "disable commits" per-thread option
	 * that can be set by {@link ConnectionPool#setCommitDisabled(boolean)} and the commit-time listeners
	 * that can be added by {@link #addCommitListener(IDatabaseEventListener)}.
	 *
	 * @throws java.sql.SQLException
	 */
	@Override
	public void commit() throws java.sql.SQLException {
		m_pe.getPool().logAction(this, "commit()");

		if(!getPool().isCommitDisabled())
			check().commit();
		getPool().writeSpecial(this, StatementProxy.ST_COMMIT);

		//-- Call all listeners, abort on 1st error
		if(m_commitListenerList.size() == 0) // Fast exit if nothing is registered
			return;

		for(IDatabaseEventListener icl : m_commitListenerList) {
			try {
				icl.onAfterCommit(this);
			} catch(Exception x) {
				PoolManager.getInstance().logUnexpected(x, "Ignoring Exception in onAfterCommit");
			}
		}
		m_commitListenerList = Collections.EMPTY_LIST;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Tracepointing.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Sets the last-used timestamp and saves a tracepoint in the stack, if needed. Locks this.
	 */
	private synchronized void saveTracepoint(String sql) {
		if(!m_saveTracePoints) // Local final
			return;
		m_lastUsedTS = System.currentTimeMillis();

		if(null == m_tracePointList)
			m_tracePointList = new ArrayList<Tracepoint>(MAX_TRACEDEPTH);

		//ORDERED: remove before adding to prevent maxsize overflow.
		if(m_tracePointList.size() >= MAX_TRACEDEPTH)
			m_tracePointList.remove(0);
		m_tracePointList.add(Tracepoint.create(sql));
	}

	/**
	 * Return the list of tracepoints, from old to new. The first entry is <b>always</i> the allocation
	 * point. If stacktracing is disabled this returns the allocation point only (and the close point
	 * if known). This is valid even when the connection is closed/invalidated or whatnot.
	 * LOCKS THIS.
	 * @return
	 */
	public List<Tracepoint> getTraceList() {
		List<Tracepoint> res = new ArrayList<Tracepoint>();
		if(null != m_allocationPoint)
			res.add(m_allocationPoint);
		synchronized(this) {
			if(null != m_tracePointList)
				res.addAll(m_tracePointList);
			if(null != m_closeLocation)
				res.add(m_closeLocation);
		}
		return res;
	}

	@Nullable
	public Tracepoint getCloseLocation() {
		return m_closeLocation;
	}

	/**
	 * Return the time this was last used.
	 * @return
	 */
	public synchronized long getLastUsedTime() {
		return m_lastUsedTS;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Hanging connections checker.						*/
	/*--------------------------------------------------------------*/
	private enum LongRunState {
		/**
		 * The connection is closed or invalidated.
		 */
		CLOSED,

		/**
		 * This connection is not expired in any way
		 */
		OKAY,

		/**
		 * This connection is running for a long time, but has been recently used.
		 */
		LONGRUNNING,

		/**
		 * This connection has not been used for a long time.
		 */
		LONGUNUSED
	}

	/**
	 * List of times that warnings for old pool entries need to be given.
	 */
	static private long[] WARNINT = {5 * 60 * 1000, // 5 minutes
		15 * 60 * 1000, // 15 minutes
		60 * 60 * 1000, // 1 hour
		2 * 60 * 60 * 1000, // 2 hours
		8 * 60 * 60 * 1000 // 8 hours
	};


	/**
	 * Called without any locks to see if this connection is a "hanging" connection. This
	 * is racy by definition because no locks are maintained. It means that the connection's
	 * state can change during the process while locks are clear. This means that all
	 * parts of this process must accept a state transition from life to dead at any time.
	 * Because proxies are single-use only they cannot be resurrected, so when it is determined
	 * they are dead the process can quit immediately.
	 * <h2>Determining hang state</h2>
	 * <p>Unpooled connections have their hang state checked using a staggered time interval. They
	 * are never cleared unless we are in "urgent" mode.
	 * 
	 * <p>Longliving connections are not checked using staggered time interval. They
	 * are never cleared unless we are in "urgent" mode.
	 *
	 * @param hs
	 */
	public void checkHangState(HangCheckState hs) {
		LongRunState lrs;
		boolean destroy = false;
		synchronized(this) {
			if(m_state != ConnState.OPEN) // Already closed or invalidated?
				return;
			
			if(m_longliving) {
				return;
			}

			if(isUnpooled()) {
				//-- Count this as a hanging unpooled if it's last-used time exceeds 10 minutes. Unpooled connections are never released
				long ets = hs.getNow() - 10 * 60 * 1000;
				if(getLastUsedTime() < ets) {
					hs.incUnpooledHangCount();
					hs.addHanging(this);
				}

				//-- Unpooled connections only report a warning in staggered intervals
				long dts = m_expiryWarningCount < WARNINT.length ? WARNINT[m_expiryWarningCount] : (m_expiryWarningCount - WARNINT.length + 1) * 24 * 60 * 60 * 1000;
				ets = hs.getNow() - dts;
				if(getLastUsedTime() > ets) // No need for warning.
					return;
				hs.append("***Connection(unpooled) ").append(m_id).append(" hangs: allocated ").append(DbPoolUtil.strMilli(hs.getNow(), getAllocationTime()) + " ago, last use ") //
					.append(DbPoolUtil.strMilli(hs.getNow(), getLastUsedTime())).append(" ago\n");
				hs.append("  Allocation point:\n");
				hs.appendTracepoint(m_allocationPoint);
				m_expiryWarningCount++;
				return;
			}

			//-- Pooled connections actually expire.
			lrs = calcLongRunState(hs.getExpiryTS()); // Has it expired?
			if(lrs == LongRunState.CLOSED || lrs == LongRunState.OKAY) // cannot happen, safety
				return;

			/*
			 * The connection is in trouble. If it is UNUSED for a long time we're
			 * allowed to destroy it. If it is running for a long time we accept a
			 * grace period before it gets closed.
			 */
			if(lrs == LongRunState.LONGUNUSED)
				destroy = true;
			else {
				//-- Has the grace period expired too?
				long gts = hs.getExpiryTS() - getPool().c().getLongRunningGracePeriod() * 1000; // Extend expiry time with grace interval
				if(getAllocationTime() < gts) // Allocated before grace period?
					destroy = true;
			}

			//-- If we should destroy according to timeout rules count this as a hang
			if(destroy) {
				hs.incHang();
				if(!hs.isForced() && hs.getMode() != ScanMode.ENABLED) {
					destroy = false; // We're not allowed to destroy.
					hs.addHanging(this);
				} else {
					//-- Invalidate this proxy technically. This will fail all other requests immediately after but leaves the PoolEntry intact & pointing to it.
					hs.addReleased(this);
					m_state = ConnState.INVALIDATED;
					hs.incDestroyed();
				}
			}

			//-- Do reporting
			hs.append("***Connection(pooled) ").append(m_id).append(" " + lrs).append(": allocated ").append(DbPoolUtil.strMilli(hs.getNow(), getAllocationTime()) + " ago, last use ") //
				.append(DbPoolUtil.strMilli(hs.getNow(), getLastUsedTime())).append(" ago ");
			if(destroy)
				hs.append(" was DESTROYED\n");
			else
				hs.append(" should be destroyed\n");
			if(m_expiryWarningCount++ < 2) {
				hs.append("  Allocation point:\n");
				hs.appendTracepoint(m_allocationPoint);
			}
			if(!destroy) // If nothing happened really- exit.
				return;
		}

		/*
		 * When here this proxy has been invalidated by this call, and we must invalidate the
		 * poolentry too, outside of any lock.
		 */
		m_pe.invalidate(this);
	}

	/**
	 * Check if this connection is running too long wrt the timestamp passed. LOCKS THIS.
	 * @return
	 */
	private synchronized LongRunState calcLongRunState(long ets) {
		if(m_state != ConnState.OPEN)
			return LongRunState.CLOSED;
		if(m_lastUsedTS < ets)
			return LongRunState.LONGUNUSED; // Has not been used for a long time.
		if(m_allocationTS < ets)
			return LongRunState.LONGRUNNING; // It runs for a long time but is used regularly
		return LongRunState.OKAY; // Nothing to see here, just move on.
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stuff called from statement etc proxies.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Proxy to add resource.
	 * @param thing
	 */
	void addResource(Object thing) {
		m_pe.addResource(thing);
	}

	/**
	 * This removes the resource from the resource list because it was normally closed.
	 * @param o		the resource to remove.
	 */
	protected void removeResource(final Object o) {
		try {
			usable();
			m_pe.removeResource(this, o);
		} catch(InvalidProxyException x) {
			System.out.println("Ignored: " + x + ", at:\n" + DbPoolUtil.getLocation());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Connection related code (non-threadsafe)			*/
	/*--------------------------------------------------------------*/
	/*
	 * The "normal" connection methods are not thread-safe. Only code
	 * that can be called from other parts of the <i>pool</i> needs
	 * to be threadsafe.
	 */

	/**
	 * Forbid a connection from being closed using the normal close() method.
	 */
	public void setUncloseable(final boolean unclosable) {
		m_unclosable = unclosable;
	}

	/**
	 * Add a commit-time listener to this connection. The listener is
	 * called after the 1st commit; the list is cleared at that time
	 * (provided the commit works).
	 * @since 2011/08/12
	 * @param c
	 */
	public void addCommitListener(@Nonnull IDatabaseEventListener c) {
		if(m_commitListenerList == Collections.EMPTY_LIST)
			m_commitListenerList = new ArrayList<IDatabaseEventListener>();
		m_commitListenerList.add(c);
	}

	/**
	 * Remove an earlier registered commit listener - silly usage, questionable interface.
	 * @since 2011/08/12
	 * @param c
	 */
	public void removeCommitListener(@Nonnull IDatabaseEventListener c) {
		if(m_commitListenerList.size() == 0)
			return;
		m_commitListenerList.remove(c);
	}


	int getWarningCount() {
		return m_expiryWarningCount;
	}

	void setWarningCount(final int c) {
		m_expiryWarningCount = c;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Resource management...								*/
	/*--------------------------------------------------------------*/
	//	/**
	//	 * Called from the thread cache handler to drop the connection when
	//	 * the thread connection cache is cleared. This merely closes the
	//	 * real connection but does not do a callback to drop the connection
	//	 * from the thread cache.
	//	 */
	//	public void	_closeConnection() throws SQLException
	//	{
	//		System.out.println(this+": _closeConnection called.");
	//		m_pe.proxyClosed(this);
	//		m_closed = true;					// No: drop the connection
	//	}
	//

	/*--------------------------------------------------------------*/
	/*	CODING:	resource allocating proxies.						*/
	/*--------------------------------------------------------------*/
	public java.sql.PreparedStatement prepareStatement(java.lang.String p1) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareStatement(this, p1);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int[] p2) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareStatement(this, p1, p2);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareStatement(this, p1, p2);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2, int p3) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareStatement(this, p1, p2, p3);
	}

	public java.sql.PreparedStatement prepareStatement(java.lang.String p1, int p2, int p3, int p4) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareStatement(this, p1, p2, p3, p4);
	}

	public java.sql.CallableStatement prepareCall(String name, int a, int b, int c) throws SQLException {
		check(name);
		return m_pe.proxyPrepareCall(this, name, a, b, c);
	}

	public PreparedStatement prepareStatement(String a, String[] ar) throws SQLException {
		check(a);
		return m_pe.proxyPrepareStatement(this, a, ar);
	}

	public java.sql.Statement createStatement() throws java.sql.SQLException {
		check();
		return m_pe.proxyCreateStatement(this);
	}

	public java.sql.Statement createStatement(int p1, int p2) throws java.sql.SQLException {
		check();
		return m_pe.proxyCreateStatement(this, p1, p2);
	}

	public java.sql.Statement createStatement(int p1, int p2, int p3) throws java.sql.SQLException {
		check();
		return m_pe.proxyCreateStatement(this, p1, p2, p3);
	}

	public java.sql.CallableStatement prepareCall(java.lang.String p1, int p2, int p3) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareCall(this, p1, p2, p3);
	}

	public java.sql.CallableStatement prepareCall(java.lang.String p1) throws java.sql.SQLException {
		check(p1);
		return m_pe.proxyPrepareCall(this, p1);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Callthroughs to the original statement.				*/
	/*--------------------------------------------------------------*/
	public void setCatalog(final java.lang.String p1) throws java.sql.SQLException {
		check().setCatalog(p1);
	}

	public void rollback() throws java.sql.SQLException {
		m_pe.getPool().logAction(this, "rollback()");

		check().rollback();
		for(IDatabaseEventListener icl : m_commitListenerList) {
			try {
				icl.onAfterRollback(this);
			} catch(Exception x) {
				PoolManager.getInstance().logUnexpected(x, "Ignoring Exception in onAfterRollback");
			}
		}
		m_commitListenerList = Collections.EMPTY_LIST;

		getPool().writeSpecial(this, StatementProxy.ST_ROLLBACK);
	}


	public void clearWarnings() throws java.sql.SQLException {
		check().clearWarnings();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Map getTypeMap() throws java.sql.SQLException {
		return check().getTypeMap();
	}

	public int getTransactionIsolation() throws java.sql.SQLException {
		return check().getTransactionIsolation();
	}

	public void setTransactionIsolation(final int p1) throws java.sql.SQLException {
		check().setTransactionIsolation(p1);
	}

	public synchronized boolean isClosed() throws java.sql.SQLException {
		return m_state != ConnState.OPEN;
	}

	public void setAutoCommit(final boolean p1) throws java.sql.SQLException {
		checkNoSave().setAutoCommit(p1);
		m_autocommit = p1;
	}

	public java.lang.String getCatalog() throws java.sql.SQLException {
		return check().getCatalog();
	}

	public boolean isReadOnly() throws java.sql.SQLException {
		return check().isReadOnly();
	}


	public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException {
		return check().getMetaData();
	}


	public void setReadOnly(final boolean p1) throws java.sql.SQLException {
		check().setReadOnly(p1);
	}


	public boolean getAutoCommit() throws java.sql.SQLException {
		return check().getAutoCommit();
	}

	public java.lang.String nativeSQL(final java.lang.String p1) throws java.sql.SQLException {
		return check().nativeSQL(p1);
	}

	@SuppressWarnings("unchecked")
	public void setTypeMap(@SuppressWarnings("rawtypes") final Map p1) throws java.sql.SQLException {
		check().setTypeMap(p1);
	}


	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
		return check().getWarnings();
	}

	public void releaseSavepoint(final java.sql.Savepoint sp) throws SQLException {
		check().releaseSavepoint(sp);
	}

	public void rollback(final java.sql.Savepoint sp) throws SQLException {
		check().rollback(sp);
	}

	public java.sql.Savepoint setSavepoint(final String name) throws SQLException {
		return check().setSavepoint(name);
	}

	public java.sql.Savepoint setSavepoint() throws SQLException {
		return check().setSavepoint();
	}

	public int getHoldability() throws SQLException {
		return check().getHoldability();
	}

	public void setHoldability(final int m) throws SQLException {
		check().setHoldability(m);
	}


	/*--------------- New JDK6 garbage ------------------*/

	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return check().createArrayOf(arg0, arg1);
	}

	public Blob createBlob() throws SQLException {
		return check().createBlob();
	}

	public Clob createClob() throws SQLException {
		return check().createClob();
	}

	public NClob createNClob() throws SQLException {
		return check().createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return check().createSQLXML();
	}

	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return check().createStruct(arg0, arg1);
	}

	public Properties getClientInfo() throws SQLException {
		return check().getClientInfo();
	}

	public String getClientInfo(String arg0) throws SQLException {
		return check().getClientInfo(arg0);
	}

	public boolean isValid(int arg0) throws SQLException {
		return check().isValid(arg0);
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		if(iface.isAssignableFrom(getClass()))
			return true;
		return m_pe.getConnection().isWrapperFor(iface);
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		check().setClientInfo(arg0);
	}

	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		check().setClientInfo(arg0, arg1);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		check();
		if(iface.isAssignableFrom(getClass()))
			return (T) this;
		return m_pe.getConnection().unwrap(iface);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		check().setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return check().getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		check().abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		check().setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return check().getNetworkTimeout();
	}
}
