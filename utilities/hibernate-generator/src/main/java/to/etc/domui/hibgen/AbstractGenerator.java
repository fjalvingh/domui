package to.etc.domui.hibgen;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
abstract public class AbstractGenerator {
	private Connection m_dbc;

	abstract protected Connection createConnection() throws Exception;

	protected abstract void loadSchemas() throws Exception;

	protected Connection dbc() throws Exception {
		Connection dbc = m_dbc;
		if(null == dbc) {
			m_dbc = dbc = createConnection();
		}
		return dbc;
	}

	protected DataSource getFakeDatasource() {
		return new DataSource() {
			@Override public Connection getConnection() throws SQLException {
				try {
					return createConnection();
				} catch(SQLException|RuntimeException x) {
					throw x;
				} catch(Exception z) {
					throw new RuntimeException(z);				// Really useful, those idiotic checked exceptions.
				}
			}

			@Override public Connection getConnection(String username, String password) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public PrintWriter getLogWriter() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public void setLogWriter(PrintWriter out) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public void setLoginTimeout(int seconds) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public int getLoginTimeout() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				throw new IllegalStateException("Not implemented");
			}
		};
	}

	public void close() {
		if(m_dbc != null) {
			try {
				m_dbc.close();
			} catch(Exception x) {
				// who the f cares.
			}
		}
	}



}
