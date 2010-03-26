package to.etc.test.webapp.qsql;

import javax.sql.*;

import org.junit.*;

import to.etc.dbpool.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

public class TestCompound {
	static private DataSource m_ds;

	@BeforeClass
	static public void setUp() throws Exception {
		ConnectionPool pool = PoolManager.getInstance().definePool("vpdemo");
		m_ds = pool.getUnpooledDataSource();
	}

	@Test
	public void testCompoundSQL() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
	}

}
