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

import javax.annotation.concurrent.*;

/**
 * Entry in the connection pool, either used or free. Data in here is protected
 * by the POOL.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final class PoolEntry {
	/** The pool this connection is obtained from, */
	private final ConnectionPool m_pool;

	/** This-entry's number for display pps only. */
	private final int m_id;

	/** The user ID this connection was made with */
	private final String m_userID;

	/** The actual (real) database connection; null if closed. */
	private final Connection m_cx;

	/** T if this is an unpooled connection. */
	private boolean m_unpooled;

	private boolean m_sqlTrace;

	/** The connection proxy that currently references this connection. */
	private ConnectionProxy m_proxy;

	private int m_timeout;

	/** This entry's state; will change if the entry is forced closed. */
	@GuardedBy("m_pool")
	private ConnState m_state = ConnState.OPEN;

	PoolEntry(final Connection cx, final ConnectionPool pool, final int idnr, final String userid) throws SQLException {
		m_pool = pool;
		m_cx = cx;
		m_id = idnr;
		m_userID = userid;
		m_timeout = pool.getForceTimeout();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Non-locking accessor code (finals)					*/
	/*--------------------------------------------------------------*/
	/**
	 * The debug ID for this entry.
	 * @return
	 */
	public int getID() {
		return m_id;
	}

	public ConnectionPool getPool() {
		return m_pool;
	}

	public PoolManager getManager() {
		return getPool().getManager();
	}

	/**
	 * The userID this connection was made to.
	 * @return
	 */
	public String getUserID() {
		return m_userID;
	}

	/**
	 * Returns the real connection for this entry.
	 */
	public Connection getConnection() {
		return m_cx;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Simple locking accessor code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Abort if the entry has been closed. LOCKS POOL
	 */
	private void usable() {
		synchronized(m_pool) {
			if(m_state != ConnState.OPEN)
				throw new IllegalStateException("PoolEntry was " + m_state);
		}
	}

	@GuardedBy("m_pool")
	void setUnpooled(final boolean unpooled) {
		synchronized(m_pool) {
			m_unpooled = unpooled;
		}
	}

	@GuardedBy("m_pool")
	boolean isUnpooled() {
		synchronized(m_pool) {
			return m_unpooled;
		}
	}

	/**
	 * Returns a string describing the connection.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("Poolentry[");
		sb.append(m_pool.toString());
		sb.append(',');
		sb.append(m_id);
		sb.append(",type=");
		sb.append(m_unpooled ? "unpooled" : "pooled");
		sb.append("]");
		//		if(m_proxy == null)
		//			sb.append(",without proxy.");
		//		else {
		//			Thread t = m_proxy.getOwnerThread();
		//			if(t == null)
		//				sb.append(", unowned");
		//			else {
		//				sb.append(", owned by ");
		//				sb.append(t.getName());
		//			}
		//		}
		return sb.toString();
	}

	public void appendDesc(StringBuilder sb) {
		sb.append(m_pool.toString());
		sb.append(',');
		sb.append(m_id);
		sb.append(',');
		sb.append(m_unpooled ? "unpooled" : "pooled");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Proxy management.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the current proxy. Returns null if unassigned. LOCKS POOL.
	 * @return
	 */
	@GuardedBy("m_pool")
	ConnectionProxy getProxy() {
		synchronized(m_pool) {
			return m_proxy;
		}
	}

	/**
	 * Creates a new connection proxy and links it to this class. All state
	 * data in the proxy is locked by it's instance of ConnectionPoolEntry.
	 */
	ConnectionProxy proxyMake() {
		int id = PoolManager.nextConnID();
		ConnectionProxy px = new ConnectionProxy(this, id, Thread.currentThread(), m_pool.dbgIsStackTraceEnabled(), isUnpooled());
		synchronized(m_pool) {
			usable();

			if(m_proxy != null) {
				ConnectionPool.MSG.info("Attempted to create 2nd proxy?");
				throw new IllegalStateException("Attempt to create 2nd proxy for used connection");
			}
			m_proxy = px;
		}
		return px;
	}

	/**
	 * Called when a proxy is closed. When called all locks are free, and
	 * the proxy itself has already invalidated itself. It also decided it
	 * was the owning proxy- which is why it calls this. This should ONLY
	 * be called for normal release since this connection will be released
	 * to the pool for reuse.
	 * @param pc		the proxy that was closed
	 */
	void release(final ConnectionProxy pc) throws SQLException {
		String panictext = null;
		synchronized(m_pool) {
			if(pc != m_proxy) {
				panictext = "DB Proxy closed but entry is not owning it??";
			} else {
				m_proxy = null; // Currently unattached
			}
		}

		if(null != panictext) {
			StringBuilder sb = new StringBuilder(8192);
			sb.append("Current location:\n");
			DbPoolUtil.getThreadAndLocation(sb);
			sb.append("\nEntry stack\n");
			//			px.dbgPrintStackTrace(sb, 20, 20);

			getManager().panic("DB Proxy closed but entry is not owning it??", sb.toString());
			throw new IllegalStateException("kickjal: valid proxy close but entry's not owning it??");
		}

		//-- Normal close. Remove all saved resources, MUST BE OUT OF LOCK!!
		closeResources(); // Close all tracked resources
		synchronized(this) {
			if(m_proxy != null)
				throw new IllegalStateException("kickjal: proxy still active??");
		}
		m_pool.returnToPool(this, pc);
	}

	/**
	 * This should be called when this entry is to be closed and not re-used. Usually
	 * called by the proxy which should already be invalid at that time.
	 * @param pc
	 * @throws SQLException
	 */
	void invalidate(final ConnectionProxy pc) {
		String panictext = null;
		synchronized(m_pool) {
			if(m_state != ConnState.OPEN)
				return; // Already released/releasing

			if(pc != m_proxy) {
				panictext = "DB Proxy closed but entry is not owning it??";
			} else {
				m_proxy = null; // Currently unattached
				m_state = ConnState.INVALIDATED; // Make sure all normal calls fail on this afterwards.
				m_pool.removeEntryFromPool(this); // Remove all references to this entry
			}
		}
		m_pool.getManager().removeThreadConnection(pc);

		/*
		 * At this point no locks are open, this entry is invalid and the pool knows nothing of
		 * it anymore. Proceed to cleanup outside locks.
		 */

		//-- Proceed to fully release everything 1st.
		closeResources(); // Close all tracked resources (does not throw exceptions)
		releaseConnection(); // Close the connection itself

		/*
		 * If some error occured log it here.
		 */
		if(null != panictext) {
			StringBuilder sb = new StringBuilder(8192);
			sb.append("Current location:\n");
			DbPoolUtil.getThreadAndLocation(sb);
			sb.append("\nEntry stack\n");
			//			px.dbgPrintStackTrace(sb, 20, 20);

			getManager().panic("DB Proxy closed but entry is not owning it??", sb.toString());
			throw new IllegalStateException("kickjal: valid proxy close but entry's not owning it??");
		}

		synchronized(this) {
			if(m_proxy != null)
				throw new IllegalStateException("kickjal: proxy still active??");
		}
	}

	/**
	 * Force the connection closed: rollback any transaction, then close it. Accept and ignore
	 * any failure. After this the PE is fully unusable. The state should already reflect that.
	 */
	void releaseConnection() {
		try {
			m_cx.rollback();
		} catch(Exception x) {}

		try {
			m_pool.callReleasedListeners(m_cx);
			m_cx.close();
		} catch(Exception x) {}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Setting special options.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Sets the connection into Oracle SQL_TRACE mode.
	 */
	public void setSqlTraceMode(final boolean on) throws java.sql.SQLException {
		if(on == m_sqlTrace)
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
	/*	CODING:	Connection resource management...					*/
	/*--------------------------------------------------------------*/
	/** All objects allocated FROM this connection. */
	@GuardedBy("m_pool")
	private HashSet<Object> m_use_set = new HashSet<Object>();

	/**
	 * Adds a resource to this statement's tracking list, so that it will be
	 * released when the time comes. Aborts if the entry is currently closed.
	 */
	void addResource(final Object o) {
		synchronized(m_pool) {
			if(m_state == ConnState.OPEN) {
				//-- No problems: just add to the set of resources. If we are between proxy invalidate and this-invalidate the latter closes this too.
				m_use_set.add(o); // Add, do not allow duplicates.
				return;
			}
		}

		//-- The thing was allocated but we're closed now- attempt to close it immediately, ignoring errors.
		try {
			_closeResource(o);
		} catch(Exception x) {}
	}

	/**
	 * Removes a tracked resource when it was closed normally.
	 */
	void removeResource(final ConnectionProxy pc, final Object o) {
		synchronized(m_pool) {
			if(m_state != ConnState.OPEN)
				return;
			if(!m_use_set.remove(o)) { // Remove from set,
				ConnectionPool.MSG.info("to.etc.Removing element without it ever being added");
				return;
			}
		}

		//-- Ok: decrement statement count OUT OF LOCK
		if(o instanceof ResultSetProxy)
			m_pool.decOpenRS();
		else
			m_pool.decOpenStmt();
	}

	/**
	 * Called to forcefully close all tracked resources. MUST BE CALLED WITHOUT
	 * LOCK!!!!
	 */
	void closeResources() {
		Set<Object> todo;
		synchronized(m_pool) {
			todo = m_use_set;
			m_use_set = new HashSet<Object>();
		}

		//-- Release everything outside lock
		int nclosed = 0;
		for(Object o : todo) {
			_closeResource(o);
			if(o instanceof ResultSetProxy)
				m_pool.decOpenRS();
			else
				m_pool.decOpenStmt();
			nclosed++;
		}
	}

	/**
	 * Closes a specified resource and IGNORES all exceptions there. Called outside any locks.
	 */
	private void _closeResource(final Object o) {
		try {
			if(o instanceof StatementProxy)
				((StatementProxy) o).closedByConnection();
			else if(o instanceof PreparedStatementProxy)
				((PreparedStatementProxy) o).closedByConnection();
			else if(o instanceof CallableStatementProxy)
				((CallableStatementProxy) o).closedByConnection();
			else if(o instanceof ResultSetProxy)
				((ResultSetProxy) o).closedByConnection();
			else {
				System.out.println("POOLERR: Unknown element added to tracked database resources?");
				throw new RuntimeException("Unknown element added to tracked database resources?");
			}
		} catch(Exception x) { // ! Ignore all errors.
			if(!getPool().c().isIgnoreUnclosed())
				x.printStackTrace();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Tracked resource allocation routines..				*/
	/*--------------------------------------------------------------*/
	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1, final int p2, final int p3) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1, p2, p3));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1, final int[] p2) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1, p2));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1, final int p2, final int p3, final int p4) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1, p2, p3, p4));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1, final int p2) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1, p2));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.PreparedStatement proxyPrepareStatement(final ConnectionProxy pc, final java.lang.String p1, final String[] ar) throws java.sql.SQLException {
		PreparedStatementProxy ps = new PreparedStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(p1, ar));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final ConnectionProxy pc) throws java.sql.SQLException {
		StatementProxy ps = new StatementProxy(pc, null);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().createStatement());
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final ConnectionProxy pc, final int p1, final int p2) throws java.sql.SQLException {
		StatementProxy ps = new StatementProxy(pc, null);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().createStatement(p1, p2));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final ConnectionProxy pc, final String a, final String[] b) throws java.sql.SQLException {
		StatementProxy ps = new StatementProxy(pc, a);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareStatement(a, b));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.Statement proxyCreateStatement(final ConnectionProxy pc, final int p1, final int p2, final int p3) throws java.sql.SQLException {
		StatementProxy ps = new StatementProxy(pc, null);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().createStatement(p1, p2, p3));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final ConnectionProxy pc, final String p1, final int p2, final int p3) throws java.sql.SQLException {
		CallableStatementProxy ps = new CallableStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareCall(p1, p2, p3));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final ConnectionProxy pc, final String p1, final int p2, final int p3, final int p4) throws java.sql.SQLException {
		CallableStatementProxy ps = new CallableStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareCall(p1, p2, p3, p4));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}

	protected java.sql.CallableStatement proxyPrepareCall(final ConnectionProxy pc, final String p1) throws java.sql.SQLException {
		CallableStatementProxy ps = new CallableStatementProxy(pc, p1);
		try {
			pc.statsHandler().prepareStatement(ps);
			ps.associate(getConnection().prepareCall(p1));
		} finally {
			pc.statsHandler().prepareStatementEnd(ps);
		}
		addResource(ps);
		m_pool.incOpenStmt();
		return ps;
	}
}
