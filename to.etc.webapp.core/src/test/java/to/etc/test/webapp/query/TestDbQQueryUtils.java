package to.etc.test.webapp.query;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
import to.etc.webapp.query.*;

public class TestDbQQueryUtils extends TestQsqlBase {

	@Test
	public void testQueryCount() throws Exception {
		String descriptionLikePattern = "%a";
		Number num = TestDbJdbcSelector.testSingleSelectorStatic(getDc(), QSelectionFunction.COUNT, "id", Integer.class, descriptionLikePattern);
		int count = QQueryUtils.queryCount(getDc(), QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, descriptionLikePattern));
		Assert.assertEquals((num).doubleValue(), count, 0.0001d);

		descriptionLikePattern = "!$#impossibleToMatchThis!$#";
		count = QQueryUtils.queryCount(getDc(), QCriteria.create(LedgerAccount.class).like(LedgerAccount.pDESCRIPTION, descriptionLikePattern));
		Assert.assertEquals(0, count);
	}

}
