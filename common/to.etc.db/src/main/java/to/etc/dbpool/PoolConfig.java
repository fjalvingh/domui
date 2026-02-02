/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.dbpool;

import java.io.File;
import java.util.Properties;

/**
 * Contains all configurable parameters for a pool that are static after its configuration.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class PoolConfig {
	public static final String USERID = "userid";

	public static final String PASSWORD = "password";

	public static final String CHECKSQL = "checksql";

	public static final String CHECK = "check";

	public static final String MAXCONN = "maxconn";

	public static final String MINCONN = "minconn";

	public static final String LOGSTREAM = "logstream";

	public static final String SQLTRACE = "sqltrace";

	public static final String URL = "url";

	public static final String DRIVER = "driver";

	public static final String DRIVERPATH = "driverpath";

	public static final String PRINTEXCEPTIONS = "printexceptions";

	public static final String SCAN = "scan";

	public static final String LOGRSLOCATIONS = "logrslocations";

	public static final String IGNOREUNCLOSED = "ignoreunclosed";

	public static final String STATISTICS = "statistics";

	public static final String LOGSTATEMENTS = "logstatements";

	public static final String LOGALLOCATION = "logallocation";

	public static final String LOGALLOCATIONSTACK = "logallocationstack";

	public static final String BINARY_LOG = "binaryLog";

	/** The max. #of connections that can be allocated before the pool blocks */
	final private int m_maxConns;

	/** The #of connections to allocate when INITIALIZING */
	final private int m_minConns;

	/** This pool's connection characteristics */
	final private String m_url, m_driverClassName, m_uid, m_pw;

	/** If present (not null) the driver should be instantiated off this file. */
	final private File m_driverPath;

	/** The SQL statement that is to be sent as a check for valid cnnections */
	final private String m_checkSQL;

	final private boolean m_checkConnection;

	/** Set to T if logstream logging must be enabled. */
	final private boolean m_setlog;

	final private boolean m_printExceptions;

	/** When T this logs to stdout every time a connection is allocated or closed */
	final private boolean m_logAllocation;

	/** When T this logs to stdout a stacktrace for every allocation and close */
	final private boolean m_logAllocationStack;

	/** When T this logs all statements to stdout */
	final private boolean m_logStatements;

	final private boolean m_ignoreUnclosed;

	final private boolean m_logResultSetLocations;

	/** Forces every allocated connection to execute "alter session set sql_trace=true". */
	final private boolean m_sqlTraceMode;

	final private boolean m_collectStatistics;

	final private ScanMode m_scanMode;

	final private File m_binaryLogFile;

	final private Properties m_extraProperties;

	public PoolConfig(PoolConfigBuilder tpl) {
		m_binaryLogFile = tpl.getBinaryLogFile();
		m_checkConnection = tpl.isCheckConnection();
		m_checkSQL = tpl.getCheckSQL();
		m_collectStatistics = tpl.isCollectStatistics();
		m_driverClassName = tpl.getDriverClassName();
		m_driverPath = tpl.getDriverPath();
		m_extraProperties = tpl.getExtraProperties();
		m_ignoreUnclosed = tpl.isIgnoreUnclosed();
		m_logAllocation = tpl.isLogAllocation();
		m_logAllocationStack = tpl.isLogAllocationStack();
		m_logResultSetLocations = tpl.isLogResultSetLocations();
		m_logStatements = tpl.isLogStatements();
		m_maxConns = tpl.getMaxConns();
		m_minConns = tpl.getMinConns();
		m_printExceptions = tpl.isPrintExceptions();
		m_pw = tpl.getPw();
		m_scanMode = tpl.getScanMode();
		m_setlog = tpl.isSetlog();
		m_sqlTraceMode = tpl.isSqlTraceMode();
		m_uid = tpl.getUid();
		m_url = tpl.getUrl();
	}

	/**
	 * Create a pool config for pool ID from the specified source. This does not check for errors yet.
	 */
	PoolConfig(String id, PoolConfigSource cs) {
		this(new PoolConfigBuilder(id, cs));
	}

	/**
	 * Used to compare two pools if a pool is redefined.
	 */
	@Override
	public boolean equals(final Object b) {
		if(!(b instanceof PoolConfig))
			return false;
		PoolConfig p = (PoolConfig) b;
		if(!m_uid.equalsIgnoreCase(p.m_uid))
			return false;
		if(!m_url.equalsIgnoreCase(p.m_url))
			return false;
		if(!m_driverClassName.equals(p.m_driverClassName))
			return false;
		return m_pw.equals(p.m_pw);
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

	/**
	 * If checkConnection is T, this contains the SQL statement to use every time a
	 * database connection is reused. It must be configured for unknown database
	 * types but is defaulted for Oracle, MySQL and PostgreSQL
	 */
	public String getCheckSQL() {
		return m_checkSQL;
	}

	/**
	 * T if //every// time this connection is used it's validity must be checked by sending a
	 * SQL command to it.
	 */
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

	public synchronized boolean isLogStatements() {
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

	/**
	 * Returns the time, in seconds, that a LONGRUNNING connection is
	 * allowed to run past the expiry time. A LONGRUNNING connection
	 * is a connection whose allocation time is below the expiry time,
	 * but has a lastused time that is more recent than the expiry
	 * time.
	 */
	public int getLongRunningGracePeriod() {
		return 5 * 60;
	}

	public Properties getExtraProperties() {
		return m_extraProperties;
	}
}
