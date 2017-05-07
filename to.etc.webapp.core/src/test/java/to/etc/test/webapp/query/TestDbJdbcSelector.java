package to.etc.test.webapp.query;

import java.util.*;

import javax.annotation.*;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

/**
 * Tests rendering and querying on JDBC selectors.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on May 24, 2013
 */
public class TestDbJdbcSelector extends TestQsqlBase {

	private static String render(QSelection< ? > c) throws Exception {
		QQueryRenderer r = new QQueryRenderer();
		c.visit(r);
		String s = r.toString();
		return s;
	}

	@Test
	public void testSingleNumericSelector() throws Exception {
		testSingleSelector(QSelectionFunction.COUNT, "id", Integer.class);
		testSingleSelector(QSelectionFunction.MAX, "id", Long.class);
		testSingleSelector(QSelectionFunction.MIN, "id", Long.class);
		testSingleSelector(QSelectionFunction.COUNT_DISTINCT, "id", Integer.class);
		testSingleSelector(QSelectionFunction.AVG, "id", Double.class);
		testSingleSelector(QSelectionFunction.SUM, "id", Long.class);
		testSingleSelector(QSelectionFunction.COUNT_DISTINCT, LedgerAccount.pDESCRIPTION, Integer.class);
	}

	@Test
	public void testMultiNumericSelector() throws Exception {
		testMultipleSelector(QSelectionFunction.COUNT, "id", Integer.class, QSelectionFunction.MAX, "id", Long.class);
		testMultipleSelector(QSelectionFunction.MAX, "id", Long.class, QSelectionFunction.MIN, "id", Long.class);
		testMultipleSelector(QSelectionFunction.COUNT, "id", Integer.class, QSelectionFunction.COUNT_DISTINCT, "id", Integer.class);
		testMultipleSelector(QSelectionFunction.AVG, "id", Double.class, QSelectionFunction.SUM, "id", Long.class);
	}

	@Test
	public void testDistinctSelector() throws Exception {
		List<String> selectBySql = JdbcUtil.selectSingleColumnList(getDc().getConnection(), String.class,
			"select distinct(omschrijving) from v_dec_grootboekrekeningen where omschrijving like ? order by omschrijving asc", "%a");

		QCriteria<LedgerAccount> criteria = QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, "%a");
		QSelection<LedgerAccount> selection = QSelection.create(LedgerAccount.class);
		selection.setRestrictions(criteria.getRestrictions());

		selection.distinct(LedgerAccount.pDESCRIPTION);
		selection.ascending(LedgerAccount.pDESCRIPTION);

		List<Object[]> selectResult = getDc().query(selection);

		Assert.assertEquals("FROM to.etc.test.webapp.qsql.LedgerAccount SELECT distinct(description) WHERE description like '%a' order by description ASC", render(selection));

		Assert.assertEquals(selectBySql.size(), selectResult.size());

		int index = 0;
		for(String desc : selectBySql) {
			Assert.assertEquals(desc, selectResult.get(index++)[0]);
		}
	}

	public <T> void testSingleSelector(QSelectionFunction selectFunction, @Nonnull String propertyName, @Nonnull Class<T> type) throws Exception {
		testSingleSelectorStatic(getDc(), selectFunction, propertyName, type, "%a");
	}

	@Nonnull
	public static <T> Number testSingleSelectorStatic(@Nonnull QDataContext dc, @Nonnull QSelectionFunction selectFunction, @Nonnull String propertyName, @Nonnull Class<T> type,
		@Nonnull String descriptionLikePattern) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(LedgerAccount.class);
		String columnName = cm.findProperty(propertyName).getColumnName();

		String sqlSelectFunction = toSqlSelectFunction(selectFunction, columnName);


		T selectBySql = JdbcUtil.selectOne(dc.getConnection(), type, "select " + sqlSelectFunction + " from v_dec_grootboekrekeningen where omschrijving like ?", descriptionLikePattern);

		QCriteria<LedgerAccount> criteria = QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, descriptionLikePattern);
		QSelection<LedgerAccount> selection = QSelection.create(LedgerAccount.class);
		selection.setRestrictions(criteria.getRestrictions());

		addSelector(selection, selectFunction, propertyName);

		Object[] selectResult = dc.queryOne(selection);

		Assert.assertEquals("FROM to.etc.test.webapp.qsql.LedgerAccount SELECT " + selectFunction.name().toLowerCase() + "(" + propertyName + ") WHERE description like '" + descriptionLikePattern
			+ "'", render(selection));

		if(null == selectResult) {
			throw new IllegalStateException("No result");
		}
		Assert.assertEquals(selectResult.length, 1);

		if(selectBySql instanceof Number) {
			Assert.assertEquals(((Number) selectBySql).doubleValue(), ((Number) selectResult[0]).doubleValue(), 0.0001d);
		} else {
			throw new IllegalStateException("Unexpected non numerical type: " + selectBySql.getClass());
		}
		return (Number) selectBySql;
	}

	private static String toSqlSelectFunction(@Nonnull QSelectionFunction selectFunction, @Nonnull String columnName) {
		switch(selectFunction){
			case COUNT:
				return "count(" + columnName + ")";
			case MAX:
				return "max(" + columnName + ")";
			case MIN:
				return "min(" + columnName + ")";
			case COUNT_DISTINCT:
				return "count( distinct " + columnName + ")";
			case AVG:
				return "avg(" + columnName + ")";
			case SUM:
				return "sum(" + columnName + ")";
			default:
				throw new IllegalStateException("Not supported selectFunction: " + selectFunction.name());
		}
	}

	private static void addSelector(@Nonnull QSelection<LedgerAccount> selection, @Nonnull QSelectionFunction selectFunction, @Nonnull String propertyName) {
		switch(selectFunction){
			case COUNT:
				selection.count(propertyName);
				break;
			case MAX:
				selection.max(propertyName);
				break;
			case MIN:
				selection.min(propertyName);
				break;
			case COUNT_DISTINCT:
				selection.countDistinct(propertyName);
				break;
			case AVG:
				selection.avg(propertyName);
				break;
			case SUM:
				selection.sum(propertyName);
				break;
			default:
				throw new IllegalStateException("Not supported selectFunction: " + selectFunction.name());
		}
	}

	public <T, D> void testMultipleSelector(@Nonnull QSelectionFunction selectFunction1, @Nonnull String propertyName1, @Nonnull Class<T> type1, @Nonnull QSelectionFunction selectFunction2,
		@Nonnull String propertyName2, @Nonnull Class<D> type2) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(LedgerAccount.class);
		String columnName1 = cm.findProperty(propertyName1).getColumnName();
		String sqlSelectFunction1 = toSqlSelectFunction(selectFunction1, columnName1);

		String columnName2 = cm.findProperty(propertyName2).getColumnName();
		String sqlSelectFunction2 = toSqlSelectFunction(selectFunction2, columnName2);

		JdbcAnyRecord rec = JdbcUtil.queryAnyOne(getDc().getConnection(), "select " + sqlSelectFunction1 + " as val1, " + sqlSelectFunction2
			+ " as val2 from v_dec_grootboekrekeningen where omschrijving like ?", "%a");
		T selectBySql1 = rec.getValue(type1, "val1");
		D selectBySql2 = rec.getValue(type2, "val2");

		QCriteria<LedgerAccount> criteria = QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, "%a");
		QSelection<LedgerAccount> selection = QSelection.create(LedgerAccount.class);
		selection.setRestrictions(criteria.getRestrictions());

		addSelector(selection, selectFunction1, propertyName1);
		addSelector(selection, selectFunction2, propertyName2);

		Object[] selectResult = getDc().queryOne(selection);

		Assert.assertEquals("FROM to.etc.test.webapp.qsql.LedgerAccount SELECT " + selectFunction1.name().toLowerCase() + "(" + propertyName1 + ")," + selectFunction2.name().toLowerCase() + "("
			+ propertyName2 + ") WHERE description like '%a'", render(selection));

		if(null == selectResult) {
			throw new IllegalStateException("No result");
		}
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
