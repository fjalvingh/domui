package to.etc.test.webapp.query;

import org.junit.*;

import to.etc.webapp.query.*;

public class TestQueryRendering {
	@Test
	public void	testRendering1() throws Exception {
		QCriteria<TestQueryRendering>	q = QCriteria.create(TestQueryRendering.class)
		.eq("organizationID", Long.valueOf(1000));
		System.out.println(q.toString());
	}

	@Test
	public void	testRendering2() throws Exception {
		QCriteria<TestQueryRendering>	q = QCriteria.create(TestQueryRendering.class)
		.eq("organizationID", Long.valueOf(1000))
		.add(QRestriction.or(
			QRestriction.eq("name", "Frits")
			,	QRestriction.eq("lastname", "Jalvingh")
			,	QRestriction.lt("shoeSize", 43L)
		))
		.isnotnull("lastname")
		;
		System.out.println(q.toString());
	}
}
