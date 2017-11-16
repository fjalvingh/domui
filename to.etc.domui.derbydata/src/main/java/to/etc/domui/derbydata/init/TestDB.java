package to.etc.domui.derbydata.init;

import org.jetbrains.annotations.NotNull;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.util.DeveloperOptions;
import to.etc.webapp.query.QContextManager;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-6-17.
 */
final public class TestDB {
	@Nullable
	static private ConnectionPool m_pool;

	private TestDB() {
	}

	static public synchronized ConnectionPool getPool() throws Exception {
		System.getProperties().forEach((k, v) -> System.out.println("  " + k + " = " + v));

		String path = "/tmp/demoDb";
		if(System.getProperty("maven.home") != null || System.getProperty("failsafe.test.class.path") != null) {
			File tmp = File.createTempFile("testdb", ".domui");
			tmp.delete();
			tmp.mkdirs();
			path = tmp.getAbsolutePath();
		}
		System.out.println("Database path is " + path);

		return getPool(path);
	}

	@NotNull private static ConnectionPool getPool(String path) throws Exception {
		ConnectionPool pool = m_pool;
		if(null == pool) {
			String poolid = DeveloperOptions.getString("domuidemo.poolid"); // Is a poolid defined in .developer.proeprties? Then use that,
			if(poolid != null) {
				//-- Local configuration. Init using local.
				System.out.println("** WARNING: Using local database configuration, pool=" + poolid);
				pool = PoolManager.getInstance().initializePool(poolid);
			} else {
				pool = PoolManager.getInstance().definePool(
					"demo"
					, "org.hsqldb.jdbcDriver"
					, "jdbc:hsqldb:file:" + path
					, "sa"
					, ""
					, null
				);

				pool.initialize();
			}
			DBInitialize.fillDatabase(pool.getUnpooledDataSource());
			m_pool = pool;
		}
		return pool;
	}

	static public DataSource	getDataSource() throws Exception {
		return getPool().getPooledDataSource();
	}
	static public DataSource	getDataSource(String path) throws Exception {
		return getPool(path).getPooledDataSource();
	}

	static public void initialize() throws Exception {
		DbUtil.initialize(getDataSource());

		//-- Tell the generic layer how to create default DataContext's.
		QContextManager.setImplementation(QContextManager.DEFAULT, DbUtil.getContextSource()); // Prime factory with connection source
	}

	static public void main(String[] args) {
		try {
			TestDB.initialize();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
