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
import java.util.logging.*;

import javax.annotation.*;

/**
 *	Encapsulates a java.sql.Statement for NEMA purposes. This class implements
 *  all of the Statement interfaces, and merely routes all calls to the original
 *  statement.
 *  The close() call is used to remove this from the connection's traced
 *  statement list.
 */
public class StatementProxy implements Statement {
	static protected Logger LOG = Logger.getLogger(StatementProxy.class.getName());

	/// The original statement this connects to,
	private Statement m_st;

	/// The database connection this derived from,
	private final ConnectionProxy m_c;

	/** The current SQL statement set into this dude */
	private String m_sql_str;

	private String m_closeReason;

	@Nullable
	private Tracepoint m_closeLocation;

	private Tracepoint m_allocationLocation;

	private int m_timeout;

	/** The start timestamp of the last action on this statement. */
	long m_tsStart;

	static public final byte ST_QUERY = 0x01;

	static public final byte ST_UPDATE = 0x02;

	static public final byte ST_EXECUTE = 0x03;

	static public final byte ST_COMMIT = 0x04;

	static public final byte ST_ROLLBACK = 0x05;

	static public final byte ST_CLOSE = 0x06;

	static public final byte ST_BATCH = 0x07;

	/*--------------------------------------------------------------*/
	/*	CODING:	Changed/intercepted methods..						*/
	/*--------------------------------------------------------------*/

	StatementProxy(final ConnectionProxy c, final String sql) {
		m_sql_str = sql;
		m_c = c;
		if(c.getPool().c().isLogResultSetLocations()) {
			m_allocationLocation = Tracepoint.create(null);
		}
		m_timeout = c.getPool().getForceTimeout();
	}

	void associate(Statement st) throws SQLException {
		m_st = st;
		handleTimeout(st);
	}

	private void handleTimeout(Statement ps) throws SQLException {
		if(m_timeout <= 0)
			return;
		ps.setQueryTimeout(m_timeout);
	}


	/**
	 *  The original close is augmented with a call to remove this from the
	 *  list of traced connection resources.
	 */
	public void close() throws SQLException {
		if(m_st == null)
			return; // Was already closed!

		m_closeReason = "Normal close call";
		try {
			m_st.close();
		} finally {
			m_st = null; // Force this connection cleared
			try {
				m_c.removeResource(this);
			} catch(Exception x) {
				System.out.println("Failed to remove resource from connection: " + x + ", at:\n" + DbPoolUtil.getLocation());
			}
		}
	}

	public Connection getRealConnection() {
		getRealStatement(); // Check if already closed.
		return m_c.getRealConnection();
	}

	protected ConnectionProxy _conn() {
		return m_c;
	}

	protected ConnectionPool pool() {
		return _conn().getPool();
	}

	/**
	 * This gets called when the Connection was closed, and it closes the
	 * resources. This only closes the actual statement and does not remove
	 * the element from the connection's trace list.
	 */
	public void closedByConnection() throws SQLException {
		if(m_st != null) {
			m_closeReason = "Closed because connection was closed";
			m_closeLocation = m_c.getCloseLocation();
			StringBuilder sb = new StringBuilder(512);
			sb.append("---- Statement forced CLOSED because connection is closed ----\n");
			appendQuery(sb);
			if(m_allocationLocation != null) {
				sb.append("StatementProxy was allocated at:\n");
				DbPoolUtil.strStacktraceFiltered(sb, m_allocationLocation.getElements());
			}
			if(sb.length() > 0)
				System.out.println(sb);
			DbPoolUtil.dumpLocation("Location of close");
			try {
				m_st.close();
			} finally {
				m_st = null;
			}
		}
	}

	protected void appendQuery(StringBuilder sb) {
		if(getSQL() != null) {
			sb.append("Query: ").append(getSQL()).append("\n");
		}
	}

	protected void internalDumpInfo() {
	}

	/**
	 * Return the SQL string for this statement.
	 * @return
	 */
	public String getSQL() {
		return m_sql_str;
	}

	/**
	 * Base code to wrap exceptions if needed to provide a better message.
	 * @param x
	 * @return
	 */
	protected SQLException wrap(final SQLException x) {
		if(_conn().getPool().c().isPrintExceptions()) {
			System.out.println("----- db: exception in statement -----");
			System.out.println("SQL: " + getSQL());
			x.printStackTrace();
		}
		return new BetterSQLException(getSQL(), null, 0, x);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	New methods.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the real statement, or throws an error when the statement has
	 * been closed.
	 */
	public Statement getRealStatement() {
		if(m_st == null) {
			Tracepoint closeLocation = m_closeLocation;
			throw new IllegalStateException("This statement has been CLOSED: " + m_closeReason, closeLocation == null ? null : closeLocation.getException());
		}
		return m_st;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Extended calls.                                     */
	/*--------------------------------------------------------------*/
	public ResultSet executeQuery(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_QUERY);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("executeQuery: " + sql);
		ResultSetProxy rpx = new ResultSetProxy(this);
		SQLException wx = null;
		try {
			m_c.statsHandler().executeQueryStart(this, rpx);
			rpx.associate(m_st.executeQuery(sql));
			_conn().getPool().incOpenRS();
			_conn().addResource(rpx);
			return rpx;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeQueryEnd(this, wx, rpx);
		}
	}

	public int executeUpdate(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_UPDATE);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("executeUpdate: " + sql);
		int rc = -1;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeUpdateStart(this);
			rc = getRealStatement().executeUpdate(sql);
			return rc;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeUpdateEnd(this, wx, rc);
		}
	}

	public boolean execute(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_EXECUTE);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("execute: " + sql);
		Boolean res = null;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeStart(this);
			boolean b = getRealStatement().execute(sql);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeEnd(this, wx, res);
		}
	}

	public void addBatch(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_BATCH);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("addBatch: " + sql);
		try {
			m_c.statsHandler().addBatch(this, sql);
			getRealStatement().addBatch(sql);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int[] executeBatch() throws SQLException {
		int[] res = null;
		pool().logBatch();
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("executeBatch called");
		SQLException wx = null;
		try {
			m_c.statsHandler().executeBatchStart(this);
			res = getRealStatement().executeBatch();
			return res;
		} catch(SQLException xx) {
			wx = wrap(xx);
			throw wx;
		} finally {
			m_c.statsHandler().executeBatchEnd(this, wx, res);
		}
	}

	public boolean execute(final String sql, final String ar[]) throws SQLException {
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this, ST_EXECUTE);
		SQLException wx = null;
		Boolean res = null;
		try {
			m_c.statsHandler().executeStart(this);
			boolean b = getRealStatement().execute(sql, ar);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeEnd(this, wx, res);
		}
	}

	public boolean execute(final String sql, final int[] p2) throws SQLException {
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this, ST_EXECUTE);
		Boolean res = null;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeStart(this);
			boolean b = getRealStatement().execute(sql, p2);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeEnd(this, wx, res);
		}
	}

	public boolean execute(final String sql, final int p2) throws SQLException {
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this, ST_EXECUTE);
		SQLException wx = null;
		Boolean res = null;
		try {
			m_c.statsHandler().executeStart(this);
			boolean b = getRealStatement().execute(sql, p2);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeEnd(this, wx, res);
		}
	}

	public int executeUpdate(final String sql, final String[] ar) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_UPDATE);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("executeUpdate: " + sql);
		int res = -1;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, ar);
			return res;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeUpdateEnd(this, wx, res);
		}
	}

	public int executeUpdate(final String sql, final int[] ar) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_UPDATE);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("executeUpdate: " + sql);
		int res = -1;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, ar);
			return res;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeUpdateEnd(this, wx, res);
		}
	}

	public int executeUpdate(final String sql, final int p2) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this, ST_UPDATE);
		if(LOG.isLoggable(Level.FINE))
			LOG.info("executeUpdate: " + sql);
		int res = -1;
		SQLException wx = null;
		try {
			m_c.statsHandler().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, p2);
			return res;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			m_c.statsHandler().executeUpdateEnd(this, wx, res);
		}
	}

	/*--------------------------------------------------------------*/
	/* CODING: Simple wrappers.                                     */
	/*--------------------------------------------------------------*/


	public int getMaxFieldSize() throws SQLException {
		try {
			return getRealStatement().getMaxFieldSize();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setMaxFieldSize(final int max) throws SQLException {
		try {
			getRealStatement().setMaxFieldSize(max);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getMaxRows() throws SQLException {
		try {
			return getRealStatement().getMaxRows();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setMaxRows(final int max) throws SQLException {
		try {
			getRealStatement().setMaxRows(max);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setEscapeProcessing(final boolean enable) throws SQLException {
		try {
			getRealStatement().setEscapeProcessing(enable);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getQueryTimeout() throws SQLException {
		try {
			return getRealStatement().getQueryTimeout();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setQueryTimeout(final int seconds) throws SQLException {
		try {
			getRealStatement().setQueryTimeout(seconds);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void cancel() throws SQLException {
		try {
			getRealStatement().cancel();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public SQLWarning getWarnings() throws SQLException {
		try {
			return getRealStatement().getWarnings();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void clearWarnings() throws SQLException {
		try {
			getRealStatement().clearWarnings();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setCursorName(final String name) throws SQLException {
		try {
			getRealStatement().setCursorName(name);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	/**
	 * FIXME Needs wrapping.
	 * @see java.sql.Statement#getResultSet()
	 */
	public ResultSet getResultSet() throws SQLException {
		try {
			return getRealStatement().getResultSet();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getUpdateCount() throws SQLException {
		try {
			int rc = getRealStatement().getUpdateCount();
			//			if(rc > 0)
			//				m_c.collector().incrementUpdateCount(rc);
			return rc;
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public boolean getMoreResults() throws SQLException {
		try {
			return getRealStatement().getMoreResults();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setFetchDirection(final int direction) throws SQLException {
		try {
			getRealStatement().setFetchDirection(direction);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getFetchDirection() throws SQLException {
		try {
			return getRealStatement().getFetchDirection();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setFetchSize(final int rows) throws SQLException {
		try {
			getRealStatement().setFetchSize(rows);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getFetchSize() throws SQLException {
		try {
			return getRealStatement().getFetchSize();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getResultSetConcurrency() throws SQLException {
		try {
			return getRealStatement().getResultSetConcurrency();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public int getResultSetType() throws SQLException {
		try {
			return getRealStatement().getResultSetType();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void clearBatch() throws SQLException {
		try {
			getRealStatement().clearBatch();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public Connection getConnection() throws SQLException {
		getRealStatement();
		return m_c;
	}

	public int getResultSetHoldability() throws SQLException {
		try {
			return getRealStatement().getResultSetHoldability();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		try {
			return getRealStatement().getGeneratedKeys();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public boolean getMoreResults(final int m) throws SQLException {
		try {
			return getRealStatement().getMoreResults(m);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public boolean isClosed() throws SQLException {
		try {
			return getRealStatement().isClosed();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public boolean isPoolable() throws SQLException {
		try {
			return getRealStatement().isPoolable();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		try {
			return getRealStatement().isWrapperFor(iface);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setPoolable(boolean arg0) throws SQLException {
		try {
			getRealStatement().setPoolable(arg0);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return getRealStatement().unwrap(iface);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		getRealStatement().closeOnCompletion();
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return getRealStatement().isCloseOnCompletion();
	}
}
