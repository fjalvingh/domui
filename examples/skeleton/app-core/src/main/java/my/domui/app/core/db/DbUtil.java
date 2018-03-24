package my.domui.app.core.db;

import my.domui.app.core.Constants;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.PoolManager;
import to.etc.domui.hibernate.config.HibernateConfigurator;
import to.etc.domui.hibernate.model.HibernateModelCopier;
import to.etc.domui.util.db.QCopy;
import to.etc.util.WrappedException;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.Properties;

/**
 * Helper class to initialize the database, handling updates using Flyway, and starting off Hibernate.
 */
@DefaultNonNull
public class DbUtil {
	@Nullable
	private static ConnectionPool m_pool;

	/**
	 * Constants are used to select the proper Flyway update files.
	 */
	public enum SystemDatabaseType {
		postgres, hsqldb
	}

	public static void  initialize(DataSource ds) throws Exception {
		HibernateConfiguration.configure();
		//HibernateConfigurator.enableBeforeImages(true);
		//HibernateConfigurator.enableObservableCollections(true);
		HibernateConfigurator.initialize(ds);
	}

	/**
	 * Alternate entrypoint: initialize the layer using a poolID in the default poolfile.
	 */
	public static void  _initialize(String poolname) throws Exception {
		ConnectionPool p = PoolManager.getInstance().definePool(poolname);
		initialize(p.getPooledDataSource());
	}

	/**
	 * Initialize by either a pool file or use a temp database, depending on
	 * whether
	 */
	public static void  initialize(File poolfile, Properties properties, String poolname) throws Exception {
		ConnectionPool pool;
		if(properties.getProperty(poolname + ".driver") == null) {
			pool = createTempDatabase();
		} else {
			pool = PoolManager.getInstance().definePool(poolfile, poolname);
		}
		initialize(pool);
	}

	public static void  initialize(File poolfile, String poolname) throws Exception {
		ConnectionPool pool = PoolManager.getInstance().definePool(poolfile, poolname);
		initialize(pool);
	}


	public static void initialize(ConnectionPool pool) throws Exception {
		m_pool = pool;
		updateDatabase(pool.getUnpooledDataSource(), SystemDatabaseType.postgres);
		initialize(pool.getPooledDataSource());
		QContextManager.setImplementation(QContextManager.DEFAULT, DbUtil.getContextSource()); // Prime factory with connection source
		QCopy.setImplementation(new HibernateModelCopier());
	}

	static private ConnectionPool createTempDatabase() throws Exception {
		String path = "/tmp/" + Constants.APPCODE + "DB/data";
		if(System.getProperty("maven.home") != null || System.getProperty("failsafe.test.class.path") != null) {
			File tmp = File.createTempFile("testdb", ".app");
			tmp.delete();
			tmp.mkdirs();
			path = tmp.getAbsolutePath();
		} else {
			File root = new File(path);
			root.getParentFile().mkdirs();
		}
		System.out.println("Database path is " + path);

		ConnectionPool pool = PoolManager.getInstance().definePool(
			"app", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + path, "sa", "", null
		);
		pool.initialize();
		return pool;
	}

	/**
	 * Handle updating the database. This uses <a href="">Flyway</a> as the tool
	 * to do the updates. The updates are stored as files in the app: look for the
	 * V1__create_database.sql file under resources/db/updates.
	 */
	public static void updateDatabase(DataSource ds, SystemDatabaseType dbtype) {
		Flyway fly = new Flyway();
		fly.setDataSource(ds);
		fly.setLocations("db/updates/common", "db/updates/" + dbtype.name());
		fly.setSchemas("PUBLIC");			// UPPERCASE - IMPORTANT
		fly.setCallbacks(new MyFlywayCallback());
		fly.migrate();
	}

	public static void initialize() throws Exception {
		File pool = new File(System.getProperty("user.home") + File.separator + ".dbpool.xml");
		initialize(pool, "domuiapp");
	}

	public static void main(String[] args) throws Exception {
		File pool = new File(System.getProperty("user.home") + File.separator + ".dbpool.xml");
		initialize(pool, "domuiapp");
	}

	public static QDataContextFactory getContextSource() {
		return HibernateConfigurator.getDataContextFactory();
	}

	public static QDataContext createDataContext() {
		try {
			return getContextSource().getDataContext();
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@DefaultNonNull(false)
	private static class MyFlywayCallback implements FlywayCallback {
		@Override public void beforeClean(Connection connection) {

		}

		@Override public void beforeUndo(Connection connection) {

		}

		@Override public void beforeEachUndo(Connection connection, MigrationInfo migrationInfo) {

		}

		@Override public void afterEachUndo(Connection connection, MigrationInfo migrationInfo) {

		}

		@Override public void afterUndo(Connection connection) {

		}

		@Override public void afterClean(Connection connection) {

		}

		@Override public void beforeMigrate(Connection connection) {
			System.out.println("FlyWay migration starting");
		}

		@Override public void afterMigrate(Connection connection) {
			System.out.println("FlyWay migration done");

		}

		@Override public void beforeEachMigrate(Connection connection, MigrationInfo migrationInfo) {
			System.out.println("Migrating to " + migrationInfo.getScript());
		}

		@Override public void afterEachMigrate(Connection connection, MigrationInfo migrationInfo) {

		}

		@Override public void beforeValidate(Connection connection) {

		}

		@Override public void afterValidate(Connection connection) {

		}

		@Override public void beforeBaseline(Connection connection) {

		}

		@Override public void afterBaseline(Connection connection) {

		}

		@Override public void beforeRepair(Connection connection) {

		}

		@Override public void afterRepair(Connection connection) {

		}

		@Override public void beforeInfo(Connection connection) {

		}

		@Override public void afterInfo(Connection connection) {

		}
	}
}
