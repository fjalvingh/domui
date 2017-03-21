package to.etc.domui.test.db;

import java.io.*;
import java.net.*;

import javax.sql.*;

import to.etc.dbpool.*;
import to.etc.domuidemo.db.*;
import to.etc.util.*;
import to.etc.webapp.query.*;


final public class InitTestDB {
	static private DataSource m_ds;

	static private boolean m_hasdatabase;

	static private boolean m_hashibernate;

	static private QDataContext m_currentDc;

	private InitTestDB() {}

	/**
	 * Require a database: this creates the Derby database if needed, then initializes the Hibernate
	 * layer on top of it.
	 * @throws Exception
	 */
	static public void require() throws Exception {
		checkFilled();
		initHibernate();
	}

	/**
	 * Register Hibernate objects and create factories et al if not already done.
	 * @throws Exception
	 */
	static private void initHibernate() throws Exception {
		checkFilled();
		if(!m_hashibernate) {
			DbUtil.initialize(getDataSource());
			m_hashibernate = true;
		}
	}

	/**
	 * Makes sure the test database is filled, and fills it if empty.
	 * @throws Exception
	 */
	static private void checkFilled() throws Exception {
		if(!m_hasdatabase) {
			DataSource ds = getDataSource();
			DBInitialize.fillDatabase(ds);
			m_hasdatabase = true;
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	static public DataSource getDataSource() throws Exception {
		if(m_ds != null)
			return m_ds;

		String poolid = DeveloperOptions.getString("domui.poolid"); // Is a poolid defined in .developer.properties? Then use that,
		ConnectionPool p;
		if(poolid != null) {
			//-- Local configuration. Init using local.
			System.out.println("** WARNING: Using local database configuration, pool=" + poolid);
			p = PoolManager.getInstance().initializePool(poolid);
		} else {
			URL url = InitTestDB.class.getResource("pool.xml");
			File f = new File(url.toURI());
			if(!f.exists())
				throw new IllegalStateException("Missing/inaccessible pool.xml resource");
			p = PoolManager.getInstance().initializePool(f, "demo");
		}
		m_ds = p.getUnpooledDataSource();
		return m_ds;
	}

	/**
	 * Create a new data context - YOU NEED TO CLOSE IT.
	 * @return
	 */
	static QDataContext createContext() throws Exception {
		return DbUtil.getContextSource().getDataContext();
	}

	/**
	 * Closes the previously allocated context and returns a new fresh one.
	 * @return
	 * @throws Exception
	 */
	static QDataContext getTestContext() throws Exception {
		if(m_currentDc != null) {
			try {
				m_currentDc.rollback();
			} catch(Exception x) {
				System.out.println("test: rollback failed before closing connection: " + x);
			}
			m_currentDc = null;
		}

		m_currentDc = createContext();
		return m_currentDc;
	}
}
