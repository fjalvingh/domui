package to.etc.test.webapp.qsql;

import java.sql.*;
import java.util.*;

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

	static <T> List<T> exec(JdbcQuery<T> q) throws Exception {
		Connection dbc = m_ds.getConnection();
		JdbcDataContext	jdc = new JdbcDataContext(null, dbc);
		try {
			q.dump();
			return (List<T>) q.query(jdc);
		} finally {
			try {
				dbc.close();
			} catch(Exception x) {}
		}
	}

	static <T> List<T> exec(QCriteria<T> q) throws Exception {
		JdbcQuery<T> jq = JdbcQuery.create(q);
		return exec(jq);
	}

	@Test
	public void	testCompoundSelect1() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).limit(20);
		List<DecadePaymentOrder> res = exec(qc);

		System.out.println("Got " + res.size() + " results");
		int ix = 0;
		for(DecadePaymentOrder la : res) {
			System.out.println("id=" + la.getId() + ", desc=" + la.getPaymentDescription());
			if(ix++ > 10)
				break;
		}
	}

}
