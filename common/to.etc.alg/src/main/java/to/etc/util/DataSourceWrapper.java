package to.etc.util;

import to.etc.function.SupplierEx;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceWrapper implements DataSource {
	final private Connection m_connection;

	final private SupplierEx<Connection> m_supplier;

	public DataSourceWrapper(Connection connection) {
		m_connection = new JdbcConnectionWrapper(connection) {
			@Override
			public void close() throws SQLException {
				//-- Do not close
			}
		};
		m_supplier = null;
	}

	public DataSourceWrapper(SupplierEx<Connection> supplier) {
		m_connection = null;
		m_supplier = supplier;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = m_connection;
		if(null != connection)
			return connection;
		SupplierEx<Connection> supplier = m_supplier;
		if(null != supplier) {
			try {
				return supplier.get();
			} catch(SQLException x) {
				throw x;
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		throw new SQLException("No connection found");
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new SQLFeatureNotSupportedException("Not implemented");
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new SQLFeatureNotSupportedException("Not implemented");
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		//-- Deliberately left empty
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		//-- Deliberately left empty
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("Not impl");
	}
}
