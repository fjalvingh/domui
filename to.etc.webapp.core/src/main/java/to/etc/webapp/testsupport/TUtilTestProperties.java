package to.etc.webapp.testsupport;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import javax.sql.*;

import org.junit.Assume;
import org.junit.internal.*;

import to.etc.dbpool.*;
import to.etc.dbutil.*;
import to.etc.util.*;
import to.etc.webapp.eventmanager.*;

public class TUtilTestProperties {
	/** Will contain a description of the location for the test properties used, after {@link #getTestProperties()}. */
	static private String m_propertiesLocation;

	@Nullable
	static private TestProperties m_testProperties;

	static public class DbConnectionInfo {
		public String hostname;

		public String sid;

		public String userid;

		public String password;

		public int port;
	}

	static private DbConnectionInfo m_dbconn;

	static private DataSource m_rawDS;

	static private String m_viewpointLoginName;

	static private boolean m_gotLoginName;

	private static ConnectionPool m_connectionPool;

	static {
		initLocale();
	}

	@Nonnull
	static public synchronized TestProperties getTestProperties() {
		TestProperties tp = m_testProperties;
		if(null == tp) {
			Properties p = findTestProperties();
			if(null == p) {
				System.err.println("TUtilTestProperties: no test.properties file found, using an empty one");
				p = new Properties();
			}
			m_testProperties = tp = new TestProperties(p, null != p);
		}
		return tp;
	}

	@Nullable
	static private Properties findTestProperties() {
		InputStream is = null;
		try {
			String env = System.getenv("VPTESTCFG");
			if(env != null)
				return loadProperties(env, "VPTESTCFG");

			String sysProp = System.getProperty("VPTESTCFG");
			if(sysProp != null)
				return loadProperties(sysProp, "VPTESTCFG");

			String testFileName = System.getProperty("testProperties");
			if(testFileName != null) {
				is = TUtilTestProperties.class.getResourceAsStream("/resource/test/" + testFileName);
				if(null == is)
					throw new IllegalStateException(testFileName + ": this test.properties file, defined by the 'testProperties' java property does not exist as a resource below /resource/test/");
				Properties properties = new Properties();
				properties.load(is);
				m_propertiesLocation = "resource /resource/test/" + testFileName + " (through testProperties system property)";
				return properties;
			}

			String uh = System.getProperty("user.home");
			if(uh != null) {
				File uhf = new File(new File(uh), ".test.properties");
				if(uhf.exists()) {
					Properties properties = FileTool.loadProperties(uhf);
					m_propertiesLocation = uhf + " (from user.home property)";
					return properties;
				}
			}

			File src = new File("./test.properties");
			if(src.exists()) {
				Properties properties = FileTool.loadProperties(src);
				m_propertiesLocation = src.getAbsolutePath() + " (from current directory)";
				return properties;
			}

			//-- Try to open a resource depending on the host's name
			try {
				String name = InetAddress.getLocalHost().getCanonicalHostName();
				if(name != null) {
					int dot = name.indexOf('.');
					if(dot != -1)
						name = name.substring(0, dot);
					if(!name.equals("localhost")) {
						is = TUtilTestProperties.class.getResourceAsStream(name + ".properties");
						if(is != null) {
							Properties properties = new Properties();
							m_propertiesLocation = "resource-by-hostname: " + name + ".properties";
							properties.load(is);
							return properties;
						}
					}
				}
			} catch(Exception x) {}

			//-- Cannot find
			return null;

		} catch(Exception x) {
			x.printStackTrace();
			throw new RuntimeException(x);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	@Nonnull
	private static synchronized Properties loadProperties(@Nonnull String sysProp, @Nonnull String propNamen) throws Exception {
		File f = new File(sysProp);
		if(f.exists()) {
			Properties properties = FileTool.loadProperties(f);
			m_propertiesLocation = f + " (through environment variable " + propNamen + ")";
			return properties;
		} else
			throw new IllegalStateException(propNamen + " System property has nonexisting file " + f);
	}

	public static synchronized String getPropertiesLocation() {
		return m_propertiesLocation;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Test environment database config.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Check if a database config is present. It does not OPEN the database, but
	 * when true database tests will run. If the database is configured but a connection
	 * cannot be made this <b>will</b> fail the tests.
	 * @return
	 */
	static public boolean hasDbConfig() {
		String db = System.getenv("VPTESTDB");
		if(db != null)
			return true;
		db = System.getProperty("TESTDB");
		if(null != db)
			return true;
		TestProperties p = getTestProperties();
		db = p.getProperty("database");
		return db != null;
	}

	/**
	 * Get the database connection string. This fails hard when no connection string
	 * is present. Use {@link #hasDbConfig()} to check if a test database is configured
	 * if the test needs to be conditional.
	 * @return
	 */
	static public String getDbString() {
		String db = System.getenv("VPTESTDB");
		if(db != null)
			return db;
		db = System.getProperty("TESTDB");
		if(null != db)
			return db;
		TestProperties p = getTestProperties();
		db = p.getProperty("database");
		if(db != null)
			return db;
		System.out.println("Database = " + db);
		System.err.println("Database = " + db);
		throw new IllegalStateException("No test database specified.");
	}

	/**
	 * Callable from JUnit fixures, this will "ignore" a JUnit tests when the database
	 * is unconfigured.
	 */
	static public final void assumeDatabase() {
		if(!hasDbConfig()) {
			Assume.assumeFalse("The database is not available", true);
			throw new AssumptionViolatedException("The database is not available");
		}
//		Assume.assumeTrue(hasDbConfig());
	}

	/**
	 *
	 * @return
	 */
	static synchronized public DbConnectionInfo getDbConn() {
		if(m_dbconn != null)
			return m_dbconn;
		String db = getDbString();
		DbConnectionInfo c = new DbConnectionInfo();

		int pos = db.indexOf('@');
		if(pos != -1) {
			String a = db.substring(0, pos);
			String b = db.substring(pos + 1);

			//-- Get userid/pw
			pos = a.indexOf(':');
			if(pos != -1) {
				c.userid = a.substring(0, pos).trim();
				c.password = a.substring(pos + 1).trim();

				pos = b.indexOf('/');
				if(pos != -1) {
					c.sid = b.substring(pos + 1).trim();
					b = b.substring(0, pos);
					pos = b.indexOf(':');
					c.port = Integer.parseInt(b.substring(pos + 1).trim());
					c.hostname = b.substring(0, pos);

					m_dbconn = c;
					return c;
				}
			}
		}
		throw new IllegalStateException("Invalid database connect string: must be 'user:password@host:port/SID', not " + db);
	}

	/**
	 * Returns the SID for the test database.
	 * @return
	 */
	static public String getDbSID() {
		return getDbConn().sid;
	}

	/**
	 * This returns the ViewPoint user name to use as the 'current user' when database
	 * related tests are running. The name defaults to 'VIEWPOINT' but can be set to
	 * another value by setting the 'userid' value in the test properties file. If the
	 * userid is set to ANONYMOUS this will return NULL.
	 * @return
	 */
	static synchronized public String getViewpointLoginName() {
		if(!m_gotLoginName) {
			m_gotLoginName = true;
			m_viewpointLoginName = getTestProperties().getProperty("loginid");
			if(m_viewpointLoginName == null)
				m_viewpointLoginName = "VPC";								// jal 2014/02/11 Do not use "VIEWPOINT" since it has no rights at all and is not a real account
			else if("ANONYMOUS".equalsIgnoreCase(m_viewpointLoginName))
				m_viewpointLoginName = null;
		}
		return m_viewpointLoginName;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Database connection basics.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns a raw, unaltered datasource to the ViewPoint test database. This datasource
	 * does not alter the "current user" in red_environment.
	 *
	 * @return
	 */
	static synchronized public DataSource getRawDataSource() {
		assumeDatabase();
		if(m_rawDS == null) {
			String url = "jdbc:oracle:thin:@" + getDbConn().hostname + ":" + getDbConn().port + ":" + getDbConn().sid;
			try {
				PoolConfig.Template t = new PoolConfig.Template();
				t.setDriverClassName("oracle.jdbc.driver.OracleDriver");
				t.setUrl(url);
				t.setUid(getDbConn().userid);
				t.setPw(getDbConn().password);
				String s = getTestProperties().getProperty("driverpath");
				if(null != s)
					t.setDriverPath(new File(s));
				t.setMinConns(2);
				t.setMaxConns(50);
				m_connectionPool = PoolManager.getInstance().definePool("test", new PoolConfig(t));

//				m_connectionPool = PoolManager.getInstance().definePool("test", "oracle.jdbc.driver.OracleDriver", url, getDbConn().userid, getDbConn().password,
//					getTestProperties().getProperty("driverpath"));
				m_connectionPool.initialize();
				m_rawDS = m_connectionPool.getPooledDataSource();
			} catch(SQLException x) {
				throw new RuntimeException("cannot init pool: " + x, x);
			}

			//-- Init common infrastructure
			VpEventManager.initializeForTest();
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
	 * Important for locale specifics in tests</br>
	 * By default all tests are written for Dutch locale
	 */
	public static void initLocale() {
		Locale.setDefault(new Locale("nl", "NL"));
	}

	static public Connection makeRawConnection() throws Exception {
		return getRawDataSource().getConnection();
	}

	/**
	 * When set to true, all connections allocated on <b>the same thread</b> will have the "disable commit"
	 * flag set (see {@link ConnectionPool#setCommitDisabled(boolean)}. This allows changes to a test database
	 * without commiting those changes. The result of the test should be tested using the same database
	 * connection as the one altering the data.
	 * @param on
	 */
	static public void setCommitDisabled(boolean on) {
		if(m_connectionPool == null)
			return;
		m_connectionPool.setCommitDisabled(on);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Initialize slf4j logger.							*/
	/*--------------------------------------------------------------*/

	@GuardedBy("class")
	static private boolean m_loggingInitialized;

	static synchronized public void initLogging() {
		if(m_loggingInitialized)
			return;
		m_loggingInitialized = true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Test logging using a LogSink.						*/
	/*--------------------------------------------------------------*/

	/** If assigned the location where test log is written. */
	static private File m_testLogFile;

	/** If assigned, a LogSink logwriter that can be used to output data from tests. */
	static private PrintWriter m_logWriter;

	static private boolean m_testLogInitialized;

	static private boolean openTestLog(@Nullable String s, @Nonnull String where) {
		if(m_testLogFile != null || s == null)
			return false;
		File f = new File(s);
		try {
			tryOpenFile(f);
			return true;
		} catch(Exception x) {
			System.out.println("test: the log file " + s + " specified in " + where + " cannot be opened: " + x.getMessage());
			return false;
		}
	}

	private static void tryOpenFile(File f) throws Exception {
		m_logWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
		m_testLogFile = f;
		m_logWriter.println("**** JUnit test log created at " + new Date());
		System.out.println("test: log file created as " + f.getCanonicalPath());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					m_logWriter.flush();
				} catch(Exception x) {
					x.printStackTrace();
				}
			}

		});
	}

	static public boolean isLogging() {
		if(!m_testLogInitialized) {
			m_testLogInitialized = true;
			String s = DeveloperOptions.getString("test.logfile");
			openTestLog(s, "test.logfile in developer.properties");
			openTestLog(System.getProperty("test.logfile"), "test.logfile java property");
			openTestLog(System.getenv("TESTLOGFILE"), "TESTLOGFILE environment variable");
			if(DeveloperOptions.isDeveloperWorkstation()) {
				//-- Create a log file in the developer's home directory.
				s = System.getProperty("user.home");
				if(s == null)
					s = "/tmp";
				File f = new File(new File(s), "tests.log");
				try {
					tryOpenFile(f);
				} catch(Exception x) {
					System.out.println("test: cannot open test log output file " + f + ": " + x.getMessage());
					return false;
				}
			}
		}
		return m_testLogFile != null;
	}

	/**
	 * Returns the location for the JUnit test log file, or null if none is assigned.
	 * @return
	 */
	@Nullable
	static public File getLogFile() {
		isLogging();
		return m_testLogFile;
	}

	@Nullable
	static public PrintWriter getLogWriter() {
		isLogging();
		return m_logWriter;
	}

	/**
	 * Get a log sink if logging is actually on.
	 * @return
	 */
	@Nullable
	static public TestLogSink getLogSinkIfLogging() {
		if(!isLogging())
			return null;

		//-- Create a logger that will mirror to this output
		TestLogSink tls = new TestLogSink(m_logWriter);
		logTestName(tls);
		return tls;
	}

	static public TestLogSink getLogSink() {
		if(!isLogging())
			return new TestLogSink();
		//-- Create a logger that will mirror to this output
		TestLogSink tls = new TestLogSink(m_logWriter);
		logTestName(tls);
		return tls;
	}

	private static void logTestName(TestLogSink tls) {
		Exception x = null;
		try {
			throw new Exception();
		} catch(Exception xx) {
			x = xx;
		}

		//-- Try to find the best candidate for the JUnit test name
		StackTraceElement[] stear = x.getStackTrace();
		for(StackTraceElement ste : stear) {
			String cn = ste.getClassName();
			if(!cn.startsWith("to.etc.webapp")) {
				m_logWriter.append("\n--------------------------------------------------------------------------------------\n") //
					.append("-- test: ").append(ste.getMethodName()).append(" in class ").append(cn).append(" ----\n") //
					.append("--------------------------------------------------------------------------------------\n") //
				;
				return;
			}
		}
	}

	/**
	 * Find boolean value stored in test.properties file based on property name.
	 * If value doesn't exist return default one.
	 *
	 * @param propertyName name of property requested from properties file
	 * @param defaultValue default value returned if property can't be found
	 * @return stored or default boolean value
	 */
	public static boolean getBoolean(@Nonnull final String propertyName, final boolean defaultValue) {
		String s = getString(propertyName, defaultValue ? "true" : "false");
		return ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s));
	}

	/**
	 * Find string value stored in test.properties file based on property name.
	 * If value doesn't exist return default one.
	 *
	 * @param propertyName name of property requested from properties file
	 * @param defaultValue default value returned if property can't be found
	 * @return stored or default string value
	 */
	@Nullable
	public static String getString(@Nonnull final String propertyName, @Nullable final String defaultValue) {
		TestProperties tp = getTestProperties();
		return tp.getProperty(propertyName, defaultValue);
	}

}
