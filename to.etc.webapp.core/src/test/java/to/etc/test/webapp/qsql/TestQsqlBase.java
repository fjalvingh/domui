package to.etc.test.webapp.qsql;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import to.etc.puzzler.GroupUsesDatabase;
import to.etc.webapp.qsql.JdbcDataContext;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.testsupport.TUtilTestProperties;

import javax.sql.DataSource;

@Category(GroupUsesDatabase.class)
public class TestQsqlBase {
	@Nullable
	private QDataContext m_dc;

	@Before
	public void setUp() throws Exception {
		DataSource ds = TUtilTestProperties.getRawDataSource();

		m_dc = new JdbcDataContext(null, ds.getConnection());
	}

	@After
	public void tearDown() throws Exception {
		getDc().close();
	}

	@NonNull
	protected QDataContext getDc() {
		QDataContext dc = m_dc;
		if(dc == null) {
			Assume.assumeFalse("Database is not present", true);
			throw new IllegalStateException("m_dc not initialized yet!");
		}
		return dc;
	}

}
