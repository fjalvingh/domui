package to.etc.webapp.testsupport;

import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolConfigBuilder;
import to.etc.dbpool.PoolManager;
import to.etc.dbutil.DbLockKeeper;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.webapp.eventmanager.DbEventManager;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-22.
 */
public class TUtilTestDB {
	static private DataSource m_rawDS;

	private static ConnectionPool m_connectionPool;

	private TUtilTestDB() {
		// do not instantiate
	}

	/**
	 * Callable from JUnit fixures, this will "ignore" a JUnit tests when the database
	 * is unconfigured.
	 */
	static public final void assumeDatabase() {
		if(!TUtilTestProperties.hasDbConfig()) {
			Assume.assumeFalse("The database is not available", true);
			throw new AssumptionViolatedException("The database is not available");
		}
//		Assume.assumeTrue(hasDbConfig());
	}

	/**
	 * Returns a raw, unaltered datasource to the ViewPoint test database. This datasource
	 * does not alter the "current user" in red_environment.
	 */
	static synchronized public DataSource getRawDataSource() {
		assumeDatabase();
		if(m_rawDS == null) {
			String url = "jdbc:oracle:thin:@" + TUtilTestProperties.getDbConn().hostname + ":" + TUtilTestProperties.getDbConn().port + ":" + TUtilTestProperties.getDbConn().sid;
			try {
				PoolConfigBuilder t = new PoolConfigBuilder();
				t.driverClassName("oracle.jdbc.driver.OracleDriver");
				t.url(url);
				t.userId(TUtilTestProperties.getDbConn().userid);
				t.password(TUtilTestProperties.getDbConn().password);
				String s = TUtilTestProperties.getTestProperties().getProperty("driverpath");
				if(null != s)
					t.driverPath(new File(s));
				t.minConnections(2);
				t.maxConnections(50);
				m_connectionPool = PoolManager.getInstance().definePool("test", t);

//				m_connectionPool = PoolManager.getInstance().definePool("test", "oracle.jdbc.driver.OracleDriver", url, getDbConn().userid, getDbConn().password,
//					getTestProperties().getProperty("driverpath"));
				m_connectionPool.initialize();
				m_rawDS = m_connectionPool.getPooledDataSource();
			} catch(SQLException x) {
				throw new RuntimeException("cannot init pool: " + x, x);
			}

			//-- Init common infrastructure
			DbEventManager.initializeForTest();
			DbLockKeeper.init(m_rawDS);

			String defaulttimeout = DeveloperOptions.isDeveloperWorkstation() ? null : "120";
			String poolto = TUtilTestProperties.getString("pool.timeout", defaulttimeout);
			if(poolto != null && !StringTool.isBlank(poolto)) {
				int timeout = Integer.parseInt(poolto.trim());

				ConnectionPool pool = PoolManager.getPoolFrom(m_rawDS);
				if(null != pool)
					pool.setForceTimeout(timeout);
			}
		}
		return m_rawDS;
	}

	/**
	 * When set to true, all connections allocated on <b>the same thread</b> will have the "disable commit"
	 * flag set (see {@link ConnectionPool#setCommitDisabled(boolean)}. This allows changes to a test database
	 * without commiting those changes. The result of the test should be tested using the same database
	 * connection as the one altering the data.
	 */
	static public void setCommitDisabled(boolean on) {
		if(m_connectionPool == null)
			return;
		m_connectionPool.setCommitDisabled(on);
	}

	static public Connection makeRawConnection() throws Exception {
		return getRawDataSource().getConnection();
	}
}
