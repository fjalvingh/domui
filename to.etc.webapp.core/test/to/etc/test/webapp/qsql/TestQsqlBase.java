package to.etc.test.webapp.qsql;

import javax.annotation.*;
import javax.sql.*;

import org.junit.*;

import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;
import to.etc.webapp.testsupport.*;

public class TestQsqlBase {

	@Nonnull
	private static DataSource m_ds = TUtilTestProperties.getRawDataSource();

	@Nullable
	private QDataContext m_dc;

	@Before
	public void setUp() throws Exception {
		m_dc = new JdbcDataContext(null, m_ds.getConnection());
	}

	@After
	public void tearDown() throws Exception {
		getDc().close();
	}

	@Nonnull
	protected QDataContext getDc() {
		QDataContext dc = m_dc;
		if(dc == null) {
			throw new IllegalStateException("m_dc not initialized yet!");
		}
		return dc;
	}

}
