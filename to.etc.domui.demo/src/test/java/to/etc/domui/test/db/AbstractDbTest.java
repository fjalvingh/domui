package to.etc.domui.test.db;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-4-18.
 */
abstract public class AbstractDbTest {
	/** The dc used for each test. Created and deleted by setup fixure */
	private QDataContext m_dc;

	/**
	 * Class fixure: init database access
	 * @throws Exception
	 */
	@BeforeClass
	static public void setUp() throws Exception {
		InitTestDB.require();
	}

	@Before
	public void setUpConnection() throws Exception {
		m_dc = InitTestDB.createContext();
	}

	@After
	public void tearDownConnection() throws Exception {
		if(m_dc != null) {
			m_dc.close();
			m_dc = null;
		}
	}

	public QDataContext dc() {
		return m_dc;
	}

}
