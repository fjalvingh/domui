package to.etc.test.webapp.qsql;

import org.junit.*;

import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

public class TestBasic {
	@Test
	public void testSQLGen1() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
	}

}
