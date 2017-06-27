package to.etc.domui.derbydata.init;

import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.util.DeveloperOptions;
import to.etc.webapp.query.QContextManager;

import javax.annotation.Nullable;
import javax.sql.DataSource;

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
		ConnectionPool pool = m_pool;
		if(null == pool) {
			String poolid = DeveloperOptions.getString("domuidemo.poolid"); // Is a poolid defined in .developer.proeprties? Then use that,
			if(poolid != null) {
				//-- Local configuration. Init using local.
				System.out.println("** WARNING: Using local database configuration, pool=" + poolid);
				pool = PoolManager.getInstance().initializePool(poolid);
			} else {
				if(false) {
					pool = PoolManager.getInstance().definePool(
						"demo"
						, "org.apache.derby.jdbc.EmbeddedDriver"
						, "jdbc:derby:/tmp/demoDb;create=true"
						, ""
						, ""
						, null
					);
				} else {
					pool = PoolManager.getInstance().definePool(
						"demo"
						, "org.hsqldb.jdbcDriver"
						, "jdbc:hsqldb:file:/tmp/demoDb"
						, "sa"
						, ""
						, null
					);
				}

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
