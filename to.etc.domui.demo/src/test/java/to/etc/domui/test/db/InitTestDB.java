package to.etc.domui.test.db;

import to.etc.domui.derbydata.init.DbUtil;
import to.etc.domui.derbydata.init.TestDB;
import to.etc.webapp.query.QDataContext;

import javax.sql.DataSource;


final public class InitTestDB {
	static private boolean m_hashibernate;

	static private QDataContext m_currentDc;

	private InitTestDB() {
	}

	/**
	 * Require a database: this creates the Derby database if needed, then initializes the Hibernate
	 * layer on top of it.
	 * @throws Exception
	 */
	static public void require() throws Exception {
		initHibernate();
	}

	/**
	 * Register Hibernate objects and create factories et al if not already done.
	 * @throws Exception
	 */
	static private void initHibernate() throws Exception {
		if(!m_hashibernate) {
			TestDB.initialize();
			m_hashibernate = true;
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	static public DataSource getDataSource() throws Exception {
		return TestDB.getDataSource();
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
