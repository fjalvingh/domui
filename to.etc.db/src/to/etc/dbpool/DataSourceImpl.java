package to.etc.dbpool;

import java.io.*;
import java.sql.*;

import javax.sql.*;

/**
 * Pooled or unpooled datasource implementation using a connection pool. It
 * only implements the getConnection method; the rest of the silly crap is
 * ignored.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 31, 2006
 */
final public class DataSourceImpl implements DataSource {
	final private ConnectionPool m_pool;

	DataSourceImpl(ConnectionPool p) {
		m_pool = p;
	}

	public ConnectionPool getPool() {
		return m_pool;
	}

	public ConnectionPool getPool() {
		return m_pool;
	}

	public Connection getConnection() throws SQLException {
		return m_pool.getConnection(false);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		if(!m_pool.c().getUid().equalsIgnoreCase(username))
			throw new SQLException("Bad user ID or password.");
		return getConnection();
	}

	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {}

	public void setLoginTimeout(int seconds) throws SQLException {}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new IllegalStateException("Cannot unwrap to " + iface);
	}
}
