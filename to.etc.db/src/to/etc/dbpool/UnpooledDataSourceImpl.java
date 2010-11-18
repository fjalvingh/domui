package to.etc.dbpool;

import java.io.*;
import java.sql.*;

import javax.sql.*;

public class UnpooledDataSourceImpl implements DataSource {
	final private ConnectionPool m_pool;

	UnpooledDataSourceImpl(ConnectionPool p) {
		m_pool = p;
	}

	public Connection getConnection() throws SQLException {
		return m_pool.getConnection(true);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return m_pool.getUnpooledConnection(username, password);
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
