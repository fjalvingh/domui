package to.etc.test.webapp.query;

import org.junit.*;

import to.etc.test.webapp.qsql.*;
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

	@Test
	public void testNewOr1() throws Exception {
		QCriteria<TestQueryRendering>	q = QCriteria.create(TestQueryRendering.class);
		QRestrictor<TestQueryRendering> or = q.or();
		or.and().eq("lastname", "jalvingh").eq("firstname", "frits");
		or.and().eq("lastname", "mol").eq("firstname", "marc");
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE lastname='jalvingh' and firstname='frits' or lastname='mol' and firstname='marc'", render(q));
	}

	@Test
	public void testNewOr2() throws Exception {
		QCriteria<TestQueryRendering> q = QCriteria.create(TestQueryRendering.class);
		QRestrictor<TestQueryRendering> or = q.or();
		or.eq("lastname", "jalvingh");
		or.eq("lastname", "mol");

		or = q.or();
		or.eq("firstname", "frits");
		or.eq("firstname", "marc");

		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE (lastname='jalvingh' or lastname='mol') and (firstname='frits' or firstname='marc')", render(q));
	}

	@Test
	public void testSubSelect1() throws Exception {
		QCriteria<TestQueryRendering> q = QCriteria.create(TestQueryRendering.class);
		QRestrictor<LedgerAccount> r = q.exists(LedgerAccount.class, "ledgerList");
		r.eq("code", "1210012");
		r.gt("date", 12345);

		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE exists (select 1 from $[parent.ledgerList] where code='1210012' and date>12345L)", render(q));
	}


}
