package to.etc.test.webapp.query;

import java.sql.*;

import javax.sql.*;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;
import to.etc.webapp.testsupport.*;

public class TestQQueryUtils {
	static private DataSource m_ds;

	static private QDataContext m_dc;

	@BeforeClass
	static public void setUp() throws Exception {
		m_ds = TUtilTestProperties.getRawDataSource();
		Connection dbc = m_ds.getConnection();
		m_dc = new JdbcDataContext(null, dbc);
	}

	@Test
	public void testQueryCount() throws Exception {
		String descriptionLikePattern = "%a";
		Number num = TestJdbcSelector.testSingleSelectorStatic(m_dc, QSelectionFunction.COUNT, "id", Integer.class, descriptionLikePattern);
		int count = QQueryUtils.queryCount(m_dc, QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, descriptionLikePattern));
		Assert.assertEquals((num).doubleValue(), count, 0.0001d);

		descriptionLikePattern = "!$#impossibleToMatchThis!$#";
		count = QQueryUtils.queryCount(m_dc, QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, descriptionLikePattern));
		Assert.assertEquals(0, count);
	}

	@AfterClass
	static public void tearDown() throws Exception {
		m_dc.close();
	}


}
