package to.etc.test.webapp.query;

import java.sql.*;

import javax.annotation.*;
import javax.sql.*;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;
import to.etc.webapp.testsupport.*;

public class TestJdbcSelector {

	static private DataSource m_ds;

	static private QDataContext m_dc;

	@BeforeClass
	static public void setUp() throws Exception {
		m_ds = TUtilTestProperties.getRawDataSource();
		Connection dbc = m_ds.getConnection();
		m_dc = new JdbcDataContext(null, dbc);
	}

	@Test
	public void testSingleSelector() throws Exception {
		testSingleSelector("count (id)", Integer.class);
		testSingleSelector("max (id)", Long.class);
		testSingleSelector("min (id)", Long.class);
		testSingleSelector("count (distinct id)", Integer.class);
		testSingleSelector("avg (id)", Double.class);
		testSingleSelector("sum (id)", Long.class);
	}

	@Test
	public void testMultiSelector() throws Exception {
		testMultipleSelector("count (id)", Integer.class, "max (id)", Long.class);
		testMultipleSelector("max (id)", Long.class, "min (id)", Long.class);
		testMultipleSelector("count (id)", Integer.class, "count (distinct id)", Integer.class);
		testMultipleSelector("avg (id)", Double.class, "sum (id)", Long.class);
	}

	public <T> void testSingleSelector(@Nonnull String selectFunction, @Nonnull Class<T> type) throws Exception {
		T selectBySql = JdbcUtil.selectOne(m_dc.getConnection(), type, "select " + selectFunction + " from v_dec_grootboekrekeningen where omschrijving like ?", "%a");

		QCriteria<LedgerAccount> criteria = QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, "%a");
		QSelection<LedgerAccount> selection = QSelection.create(LedgerAccount.class);
		selection.setRestrictions(criteria.getRestrictions());

		addSelector(selectFunction, selection);

		Object[] selectResult = m_dc.queryOne(selection);

		Assert.assertEquals(selectResult.length, 1);

		if(selectBySql instanceof Number) {
			Assert.assertEquals(((Number) selectBySql).doubleValue(), ((Number) selectResult[0]).doubleValue(), 0.0001d);
		} else {
			throw new IllegalStateException("Unexpected non numerical type: " + selectBySql.getClass());
		}
	}

	private void addSelector(@Nonnull String selectFunction, @Nonnull QSelection<LedgerAccount> selection) {
		if("count (id)".equals(selectFunction)) {
			selection.count("id");
		} else if("max (id)".equals(selectFunction)) {
			selection.max("id");
		} else if("min (id)".equals(selectFunction)) {
			selection.min("id");
		} else if("count (distinct id)".equals(selectFunction)) {
			selection.countDistinct("id");
		} else if("avg (id)".equals(selectFunction)) {
			selection.avg("id");
		} else if("sum (id)".equals(selectFunction)) {
			selection.sum("id");
		}
	}

	public <T, D> void testMultipleSelector(@Nonnull String selectFunction1, @Nonnull Class<T> type1, @Nonnull String selectFunction2, @Nonnull Class<D> type2) throws Exception {
		JdbcAnyRecord rec = JdbcUtil.queryAnyOne(m_dc.getConnection(), "select " + selectFunction1 + " as val1, " + selectFunction2
			+ " as val2 from v_dec_grootboekrekeningen where omschrijving like ?", "%a");
		T selectBySql1 = rec.getValue(type1, "val1");
		D selectBySql2 = rec.getValue(type2, "val2");

		QCriteria<LedgerAccount> criteria = QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, "%a");
		QSelection<LedgerAccount> selection = QSelection.create(LedgerAccount.class);
		selection.setRestrictions(criteria.getRestrictions());

		addSelector(selectFunction1, selection);
		addSelector(selectFunction2, selection);

		Object[] selectResult = m_dc.queryOne(selection);

		Assert.assertEquals(selectResult.length, 2);

		if(selectBySql1 instanceof Number) {
			Assert.assertEquals(((Number) selectBySql1).doubleValue(), ((Number) selectResult[0]).doubleValue(), 0.0001d);
		} else {
			throw new IllegalStateException("Unexpected non numerical type: " + selectBySql1.getClass());
		}

		if(selectBySql2 instanceof Number) {
			Assert.assertEquals(((Number) selectBySql2).doubleValue(), ((Number) selectResult[1]).doubleValue(), 0.0001d);
		} else {
			throw new IllegalStateException("Unexpected non numerical type: " + selectBySql2.getClass());
		}
	}

}
