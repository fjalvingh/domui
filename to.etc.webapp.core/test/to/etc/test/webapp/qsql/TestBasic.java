package to.etc.test.webapp.qsql;

import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.junit.*;

import to.etc.dbpool.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

public class TestBasic {
	static private DataSource m_ds;

	@BeforeClass
	static public void setUp() throws Exception {
		ConnectionPool pool = PoolManager.getInstance().definePool("vpdemo");
		m_ds = pool.getUnpooledDataSource();
	}

	static List< ? > exec(JdbcQuery q) throws Exception {
		Connection dbc = m_ds.getConnection();
		try {
			return q.query(dbc);
		} finally {
			try {
				dbc.close();
			} catch(Exception x) {}
		}
	}

	@Test
	public void testSQLGen1() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
	}

	@Test
	public void testExec1() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		JdbcQuery q = gc.getQuery();
		List<LedgerAccount> res = (List<LedgerAccount>) exec(q);

		System.out.println("Got " + res.size() + " results");
		int ix = 0;
		for(LedgerAccount la : res) {
			if(ix++ > 10)
				break;
			System.out.println("la: " + la.getCode() + ", " + la.getDescription() + ", " + la.getTypeDescription() + ", " + la.getId());
		}
	}
}
