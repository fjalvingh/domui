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

import java.io.*;
import java.sql.*;

/**
 * Contains all configurable parameters for a pool that are static after it's configuration.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class PoolConfig {
	/** The max. #of connections that can be allocated before the pool blocks */
	final private int m_max_conns;

	/** The #of connections to allocate when INITIALIZING */
	final private int m_min_conns;

	/** This pool's connection characteristics */
	final private String m_url, m_driverClassName, m_uid, m_pw;

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

	PoolConfig(String driver, String url, String userid, String passwd, String driverpath) throws SQLException {
		m_url = url;
		m_driverClassName = driver;
		m_uid = userid;
		m_pw = passwd;
		m_checkConnection = false;
		m_max_conns = 20;
		m_min_conns = 5;
		if(driverpath != null)
			m_driverPath = new File(driverpath);
	}

	/**
	 * Create a pool config for pool ID from the specified source. This does not check for errors yet.
	 * @param id
	 * @param cs
	 * @return
	 */
	PoolConfig(final String id, final PoolConfigSource cs) {
		try {
			m_url = cs.getProperty(id, "url"); // Get URL and other parameters,
			if(m_url == null)
				throw new SQLException("Undefined Pool '" + id + "' in config " + cs);
			m_driverClassName = cs.getProperty(id, "driver");
			m_uid = cs.getProperty(id, "userid");
			m_pw = cs.getProperty(id, "password");
			m_checkSQL = cs.getProperty("checksql", null);
			m_checkConnection = cs.getBool(id, "check", false);
			m_setlog = cs.getBool(id, "logstream", false);
			m_sqlTraceMode = cs.getBool(id, "sqltrace", false); // 20101102 Was 'trace'
			int maxc = cs.getInt(id, "maxconn", 20);
			int minc = cs.getInt(id, "minconn", 5);
			if(minc < 1)
				minc = 1;
			if(maxc < minc)
				maxc = minc + 5;
			m_min_conns = minc;
			m_max_conns = maxc;
			m_printExceptions = cs.getBool(id, "printexceptions", false);

			String dp = cs.getProperty(id, "scan");
			if(dp == null)
				m_scanMode = ScanMode.ENABLED;
			else if("enabled".equalsIgnoreCase(dp) || "on".equalsIgnoreCase(dp))
				m_scanMode = ScanMode.ENABLED;
			else if("disabled".equalsIgnoreCase(dp) || "off".equalsIgnoreCase(dp))
				m_scanMode = ScanMode.DISABLED;
			else if("warning".equalsIgnoreCase(dp) || "warn".equalsIgnoreCase(dp)) { // Typical development setting.
				m_scanMode = ScanMode.WARNING;
				m_logResultSetLocations = true;
				m_ignoreUnclosed = false;
			} else
				throw new IllegalStateException("Invalid 'scan' mode: must be enabled, disabled or warn.");

			m_logResultSetLocations = cs.getBool(id, "logrslocations", m_logResultSetLocations); // Only override default if explicitly set.
			m_ignoreUnclosed = cs.getBool(id, "ignoreunclosed", m_ignoreUnclosed); //ditto
			m_collectStatistics = cs.getBool(id, "statistics", false);

			dp = cs.getProperty(id, "driverpath");
			if(dp != null) {
				File f = new File(dp);
				if(!f.exists()) {
					f = new File(System.getProperty("user.home"), dp);
					if(!f.exists())
						throw new SQLException("The driver path '" + dp + "' does not point to an existing file or directory");
				}
				m_driverPath = f;
			}
			String bf = cs.getProperty(id, "binaryLog");
			if(null != bf) {
				m_binaryLogFile = new File(bf);
			}
		} catch(Exception x) {
			x.printStackTrace();
			throw new RuntimeException("Pool " + id + " parameter error: " + x, x);
		}
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
		if(!m_pw.equals(p.m_pw))
			return false;
		return true;
	}


	public int getMaxConns() {
		return m_max_conns;
	}

	public int getMinConns() {
		return m_min_conns;
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
	 * @return
	 */
	public String getCheckSQL() {
		return m_checkSQL;
	}

	/**
	 * T if //every// time this connection is used it's validity must be checked by sending a
	 * SQL command to it.
	 * @return
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

	/**
	 * Returns the time, in seconds, that a LONGRUNNING connection is
	 * allowed to run past the expiry time. A LONGRUNNING connection
	 * is a connection whose allocation time is below the expiry time,
	 * but has a lastused time that is more recent than the expiry
	 * time.
	 * @return
	 */
	public int getLongRunningGracePeriod() {
		return 5 * 60;
	}
}
