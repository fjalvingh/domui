package to.etc.webapp.testsupport;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import javax.sql.*;

/**
 * Dummy implementation for DataSource used for test mode.
 *
 * @author <a href="mailto:btadic@execom.eu">Bojan Tadic</a>
 * Created on Nov 18, 2014
 */
public class TestDataSourceStub implements DataSource {

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}

}
