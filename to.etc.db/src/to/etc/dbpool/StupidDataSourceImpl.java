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
public class StupidDataSourceImpl implements DataSource {
	private ConnectionPool m_pool;

	private boolean m_unpooled;

	StupidDataSourceImpl(ConnectionPool p, boolean unpooled) {
		m_pool = p;
		m_unpooled = unpooled;
	}

	public Connection getConnection() throws SQLException {
		return m_pool.getConnection(m_unpooled);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		if(!m_pool.getUserID().equalsIgnoreCase(username))
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

}
