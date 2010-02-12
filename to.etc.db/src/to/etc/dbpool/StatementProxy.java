package to.etc.dbpool;

import java.sql.*;

import org.slf4j.*;

/**
 *	Encapsulates a java.sql.Statement for NEMA purposes. This class implements
 *  all of the Statement interfaces, and merely routes all calls to the original
 *  statement.
 *  The close() call is used to remove this from the connection's traced
 *  statement list.
 */
public class StatementProxy implements Statement {
	static protected Logger LOG = LoggerFactory.getLogger(StatementProxy.class);

	/// The original statement this connects to,
	private Statement m_st;

	/// The database connection this derived from,
	private final PooledConnection m_c;

	/** The current SQL statement set into this dude */
	private String m_sql_str;

	private String m_close_rsn;

	private Throwable m_allocationLocation;

	/*--------------------------------------------------------------*/
	/*	CODING:	Changed/intercepted methods..						*/
	/*--------------------------------------------------------------*/

	public StatementProxy(final PooledConnection c, final Statement st, final String sql) {
		m_c = c;
		m_st = st;
		m_sql_str = sql;
		if(c.m_pe.m_pool.isLogResultSetLocations()) {
			try {
				throw new RuntimeException();
			} catch(RuntimeException x) {
				m_allocationLocation = x;
			}
		}
	}

	/**
	 *  The original close is augmented with a call to remove this from the
	 *  list of traced connection resources.
	 */
	public void close() throws SQLException {
		if(m_st == null)
			return; // Was already closed!

		m_close_rsn = "Normal close call";
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

	protected PooledConnection _conn() {
		return m_c;
	}

	protected ConnectionPool pool() {
		return _conn().m_pe.getPool();
	}

	/**
	 *  This gets called when the Connection was closed, and it closes the
	 *  resources. This only closes the actual statement and does not remove
	 *  the element from the connection's trace list.
	 */
	public void closedByConnection() throws SQLException {
		if(m_st != null) {
			m_close_rsn = "Closed because connection was closed";
			StringBuilder sb = new StringBuilder(512);
			sb.append("---- Statement forced CLOSED because connection is closed ----\n");
			appendQuery(sb);
			if(m_allocationLocation != null) {
				sb.append("StatementProxy was allocated at:\n");
				DbPoolUtil.getFilteredStacktrace(sb, m_allocationLocation);
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
		if(m_c.m_pe.m_pool.isPrintExceptions()) {
			System.out.println("----- db: exception in statement -----");
			System.out.println("SQL: " + getSQL());
			//			System.out.println(BetterSQLException.format(null, 0));
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
		if(m_st == null)
			throw new IllegalStateException("This statement has been CLOSED: " + m_close_rsn);
		return m_st;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Extended calls.                                     */
	/*--------------------------------------------------------------*/
	public ResultSet executeQuery(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("executeQuery: " + sql);
		ResultSetProxy rpx = null;
		try {
			m_c.collector().executeQueryStart(this);
			rpx = new ResultSetProxy(this, m_st.executeQuery(sql));
			_conn().m_pe.m_pool.incOpenRS();
			_conn().m_pe.addResource(rpx);
			return rpx;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeQueryEnd(this, rpx);
		}
	}

	public int executeUpdate(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("executeUpdate: " + sql);
		int rc = -1;
		try {
			m_c.collector().executeUpdateStart(this);
			rc = getRealStatement().executeUpdate(sql);
			return rc;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeUpdateEnd(this, rc);
		}
	}

	public boolean execute(final String sql) throws SQLException {
		m_sql_str = sql;
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("execute: " + sql);
		Boolean res = null;
		try {
			m_c.collector().executeStart(this);
			boolean b = getRealStatement().execute(sql);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeEnd(this, res);
		}
	}

	public int[] executeBatch() throws SQLException {
		int[] res = null;
		pool().logBatch();
		if(LOG.isDebugEnabled())
			LOG.debug("executeBatch called");
		try {
			m_c.collector().executeBatchStart(this);
			res = getRealStatement().executeBatch();
			return res;
		} catch(SQLException xx) {
			SQLException wx = wrap(xx);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeBatchEnd(this, res);
		}
	}

	public boolean execute(final String sql, final String ar[]) throws SQLException {
		if(LOG.isDebugEnabled())
			LOG.debug("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this);
		Boolean res = null;
		try {
			m_c.collector().executeStart(this);
			boolean b = getRealStatement().execute(sql, ar);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeEnd(this, res);
		}
	}

	public boolean execute(final String sql, final int[] p2) throws SQLException {
		if(LOG.isDebugEnabled())
			LOG.debug("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this);
		Boolean res = null;
		try {
			m_c.collector().executeStart(this);
			boolean b = getRealStatement().execute(sql, p2);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeEnd(this, res);
		}
	}

	public boolean execute(final String sql, final int p2) throws SQLException {
		if(LOG.isDebugEnabled())
			LOG.debug("execute: " + sql);
		m_sql_str = sql;
		pool().logExecution(this);

		Boolean res = null;
		try {
			m_c.collector().executeStart(this);
			boolean b = getRealStatement().execute(sql, p2);
			res = Boolean.valueOf(b);
			return b;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeEnd(this, res);
		}
	}

	public int executeUpdate(final String sql, final String[] ar) throws SQLException {
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("executeUpdate: " + sql);
		m_sql_str = sql;
		int res = -1;
		try {
			m_c.collector().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, ar);
			return res;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeUpdateEnd(this, res);
		}
	}

	public int executeUpdate(final String sql, final int[] ar) throws SQLException {
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("executeUpdate: " + sql);
		m_sql_str = sql;
		int res = -1;
		try {
			m_c.collector().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, ar);
			return res;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeUpdateEnd(this, res);
		}
	}

	public int executeUpdate(final String sql, final int p2) throws SQLException {
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("executeUpdate: " + sql);
		m_sql_str = sql;
		int res = -1;
		try {
			m_c.collector().executeUpdateStart(this);
			res = getRealStatement().executeUpdate(sql, p2);
			return res;
		} catch(SQLException x) {
			SQLException wx = wrap(x);
			m_c.collector().executeError(this, wx);
			throw wx;
		} finally {
			m_c.collector().executeUpdateEnd(this, res);
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
			if(rc > 0)
				m_c.collector().incrementUpdateCount(rc);
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

	public void addBatch(final String sql) throws SQLException {
		pool().logExecution(this);
		if(LOG.isDebugEnabled())
			LOG.debug("addBatch: " + sql);
		try {
			m_sql_str = sql;
			m_c.collector().addBatch(sql);
			getRealStatement().addBatch(sql);
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
}
