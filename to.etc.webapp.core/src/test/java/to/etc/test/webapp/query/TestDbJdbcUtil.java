package to.etc.test.webapp.query;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
import to.etc.webapp.qsql.*;

public class TestDbJdbcUtil extends TestQsqlBase {

	@Test
	public void testUpdatingCallableStatement() throws Exception {
		//FIXME: currently this test depends on certain table, we have to find solution for this...
		String sql = "begin INSERT INTO RED_PARAMETERS (rpr_module, rpr_name, rpr_char1) VALUES (?,?,?) RETURNING rpr_id INTO ? ; end;";
		JdbcOutParam<Long> idParam = new JdbcOutParam<Long>(Long.class);
		if(JdbcUtil.executeUpdatingCallableStatement(getDc().getConnection(), sql, "test", "test", "test", idParam)) {
			Assert.assertNotNull(idParam.getValue());
		}
		getDc().rollback();
	}

	@Test
	public void testInvalidUpdatingCallableStatement() throws Exception {
		boolean exFound = false;
		String sql = "INSERT INTO RED_PARAMETERS (rpr_module, rpr_name, rpr_char1) VALUES (?,?,?) RETURNING rpr_id INTO ?;";
		JdbcOutParam<Long> idParam = new JdbcOutParam<Long>(Long.class);
		try {
			if(JdbcUtil.executeUpdatingCallableStatement(getDc().getConnection(), sql, "test", "test", "test", idParam)) {
				Assert.assertNotNull(idParam.getValue());
			}
		} catch(IllegalArgumentException ex) {
			exFound = true;
		}
		getDc().rollback();
		Assert.assertTrue(exFound);
	}

}
