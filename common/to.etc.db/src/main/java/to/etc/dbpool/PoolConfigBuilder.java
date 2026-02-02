package to.etc.dbpool;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import static to.etc.dbpool.PoolConfig.USERID;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-03-22.
 */
final public class PoolConfigBuilder {
	/** The max. #of connections that can be allocated before the pool blocks */
	private int m_maxConns;

	/** The #of connections to allocate when INITIALIZING */
	private int m_minConns;

	/** This pool's connection characteristics */
	private String m_url, m_driverClassName, m_uid, m_pw;

	/** If present (not null) the driver should be instantiated off this file. */
	private File m_driverPath;

	/** The SQL statement that is to be sent as a check for valid cnnections */
	private String m_checkSQL;

	private boolean m_checkConnection;

	/** Set to T if logstream logging must be enabled. */
	private boolean m_setlog;

	private boolean m_printExceptions;

	/** When T this logs to stdout every time a connection is allocated or closed */
	private boolean m_logAllocation;

	/** When T this logs to stdout a stacktrace for every allocation and close */
	private boolean m_logAllocationStack;

	/** When T this logs all statements to stdout */
	private boolean m_logStatements;

	private boolean m_ignoreUnclosed;

	private boolean m_logResultSetLocations;

	/** Forces every allocated connection to execute "alter session set sql_trace=true". */
	private boolean m_sqlTraceMode;

	private boolean m_collectStatistics;

	private ScanMode m_scanMode = ScanMode.ENABLED;

	private File m_binaryLogFile;

	private Properties m_extraProperties = new Properties();

	public PoolConfigBuilder() {}

	public PoolConfigBuilder(String id, PoolConfigSource cs) {
		try {
			m_url = cs.getProperty(id, PoolConfig.URL); // Get URL and other parameters,
			if(m_url == null)
				throw new SQLException("Undefined Pool '" + id + "' in config " + cs);
			m_driverClassName = cs.getProperty(id, PoolConfig.DRIVER);

			String s = cs.getProperty(id, PoolConfig.DRIVERPATH);
			File driverPath = null;
			if(s != null) {
				File f = new File(s);
				if(!f.exists()) {
					f = new File(System.getProperty("user.home"), s);
					if(!f.exists())
						throw new SQLException("The driver path '" + s + "' does not point to an existing file or directory");
				}
				driverPath = f;
			}
			m_driverPath = driverPath;

			m_uid = cs.getProperty(id, USERID);
			m_pw = cs.getProperty(id, PoolConfig.PASSWORD);
			m_checkSQL = cs.getProperty(id, PoolConfig.CHECKSQL);
			m_checkConnection = cs.getBool(id, PoolConfig.CHECK, false);
			m_setlog = cs.getBool(id, PoolConfig.LOGSTREAM, false);
			m_sqlTraceMode = cs.getBool(id, PoolConfig.SQLTRACE, false); // 20101102 Was 'trace'
			int maxc = cs.getInt(id, PoolConfig.MAXCONN, 20);
			int minc = cs.getInt(id, PoolConfig.MINCONN, 5);
			if(minc < 1)
				minc = 1;
			if(maxc < minc)
				maxc = minc + 5;
			m_minConns = minc;
			m_maxConns = maxc;
			m_printExceptions = cs.getBool(id, PoolConfig.PRINTEXCEPTIONS, false);

			String dp = cs.getProperty(id, PoolConfig.SCAN);
			ScanMode scanMode;
			boolean logResultSetLocations = false;
			boolean ignoreUnclosed = false;
			if(dp == null)
				scanMode = ScanMode.ENABLED;
			else if("enabled".equalsIgnoreCase(dp) || "on".equalsIgnoreCase(dp))
				scanMode = ScanMode.ENABLED;
			else if("disabled".equalsIgnoreCase(dp) || "off".equalsIgnoreCase(dp))
				scanMode = ScanMode.DISABLED;
			else if("warning".equalsIgnoreCase(dp) || "warn".equalsIgnoreCase(dp)) { // Typical development setting.
				scanMode = ScanMode.WARNING;
				logResultSetLocations = true;
				ignoreUnclosed = false;
			} else
				throw new IllegalStateException("Invalid 'scan' mode: must be enabled, disabled or warn.");
			m_scanMode = scanMode;

			m_logResultSetLocations = cs.getBool(id, PoolConfig.LOGRSLOCATIONS, logResultSetLocations); // Only override default if explicitly set.
			m_ignoreUnclosed = cs.getBool(id, PoolConfig.IGNOREUNCLOSED, ignoreUnclosed); //ditto
			m_collectStatistics = cs.getBool(id, PoolConfig.STATISTICS, false);
			m_logStatements = cs.getBool(id, PoolConfig.LOGSTATEMENTS, false);
			m_logAllocation = cs.getBool(id, PoolConfig.LOGALLOCATION, false);
			m_logAllocationStack = cs.getBool(id, PoolConfig.LOGALLOCATIONSTACK, false);

			File binaryLogFile = null;
			String bf = cs.getProperty(id, PoolConfig.BINARY_LOG);
			if(null != bf) {
				binaryLogFile = new File(bf);
			}
			m_binaryLogFile = binaryLogFile;
			m_extraProperties = cs.getExtraProperties(id);
		} catch(Exception x) {
			x.printStackTrace();
			throw new RuntimeException("Pool " + id + " parameter error: " + x, x);
		}
	}

	public int getMaxConns() {
		return m_maxConns;
	}

	public int getMinConns() {
		return m_minConns;
	}

	public String getUrl() {
		return m_url;
	}

	public String getDriverClassName() {
		return m_driverClassName;
	}

	public String getUid() {
		return m_uid;
	}

	public String getPw() {
		return m_pw;
	}

	public File getDriverPath() {
		return m_driverPath;
	}

	public String getCheckSQL() {
		return m_checkSQL;
	}

	public boolean isCheckConnection() {
		return m_checkConnection;
	}

	public boolean isSetlog() {
		return m_setlog;
	}

	public boolean isPrintExceptions() {
		return m_printExceptions;
	}

	public boolean isLogAllocation() {
		return m_logAllocation;
	}

	public boolean isLogAllocationStack() {
		return m_logAllocationStack;
	}

	public boolean isLogStatements() {
		return m_logStatements;
	}

	public boolean isIgnoreUnclosed() {
		return m_ignoreUnclosed;
	}

	public boolean isLogResultSetLocations() {
		return m_logResultSetLocations;
	}

	public boolean isSqlTraceMode() {
		return m_sqlTraceMode;
	}

	public boolean isCollectStatistics() {
		return m_collectStatistics;
	}

	public ScanMode getScanMode() {
		return m_scanMode;
	}

	public File getBinaryLogFile() {
		return m_binaryLogFile;
	}

	public Properties getExtraProperties() {
		return m_extraProperties;
	}

	public PoolConfigBuilder extraProperties(Properties extraProperties) {
		m_extraProperties = extraProperties;
		return this;
	}

	public PoolConfigBuilder property(String name, String value) {
		m_extraProperties.setProperty(name, value);
		return this;
	}

	public PoolConfigBuilder driverPath(File driverPath) {
		m_driverPath = driverPath;
		return this;
	}

	public PoolConfigBuilder checkSQL(String checkSQL) {
		m_checkSQL = checkSQL;
		return this;
	}

	public PoolConfigBuilder checkConnection(boolean checkConnection) {
		m_checkConnection = checkConnection;
		return this;
	}

	public PoolConfigBuilder setLog(boolean setlog) {
		m_setlog = setlog;
		return this;
	}

	public PoolConfigBuilder printExceptions(boolean printExceptions) {
		m_printExceptions = printExceptions;
		return this;
	}

	public PoolConfigBuilder logAllocation(boolean logAllocation) {
		m_logAllocation = logAllocation;
		return this;
	}

	public PoolConfigBuilder logAllocationStack(boolean logAllocationStack) {
		m_logAllocationStack = logAllocationStack;
		return this;
	}

	public PoolConfigBuilder logStatements(boolean logStatements) {
		m_logStatements = logStatements;
		return this;
	}

	public PoolConfigBuilder ignoreUnclosed(boolean ignoreUnclosed) {
		m_ignoreUnclosed = ignoreUnclosed;
		return this;
	}

	public PoolConfigBuilder logResultSetLocations(boolean logResultSetLocations) {
		m_logResultSetLocations = logResultSetLocations;
		return this;
	}

	public PoolConfigBuilder sqlTraceMode(boolean sqlTraceMode) {
		m_sqlTraceMode = sqlTraceMode;
		return this;
	}

	public PoolConfigBuilder collectStatistics(boolean collectStatistics) {
		m_collectStatistics = collectStatistics;
		return this;
	}

	public PoolConfigBuilder scanMode(ScanMode scanMode) {
		m_scanMode = scanMode;
		return this;
	}

	public PoolConfigBuilder binaryLogFile(File binaryLogFile) {
		m_binaryLogFile = binaryLogFile;
		return this;
	}

	public PoolConfigBuilder url(String url) {
		m_url = url;
		return this;
	}

	public PoolConfigBuilder driverClassName(String driverClassName) {
		m_driverClassName = driverClassName;
		return this;
	}

	public PoolConfigBuilder userId(String uid) {
		m_uid = uid;
		return this;
	}

	public PoolConfigBuilder password(String pw) {
		m_pw = pw;
		return this;
	}

	public PoolConfigBuilder maxConnections(int nr) {
		m_maxConns = nr;
		return this;
	}

	public PoolConfigBuilder minConnections(int nr) {
		m_minConns = nr;
		return this;
	}
}
