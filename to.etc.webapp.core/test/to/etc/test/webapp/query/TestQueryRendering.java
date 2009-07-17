package to.etc.test.webapp.query;

import org.junit.*;

import to.etc.webapp.query.*;

public class TestQueryRendering {
	private String	render(QCriteria<?> c) throws Exception {
		QQueryRenderer	r = new QQueryRenderer();
		c.visit(r);
		String s = r.toString();
		System.out.println("Q="+s);
		return s;
	}

	@Test
	public void	testRendering1() throws Exception {
		QCriteria<TestQueryRendering>	q = QCriteria.create(TestQueryRendering.class)
		.eq("organizationID", Long.valueOf(1000));
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE organizationID=1000L", render(q));
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
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE organizationID=1000L and (name='Frits' or lastname='Jalvingh' or shoeSize<43L) and isNotNull (lastname)", render(q));
	}
}
