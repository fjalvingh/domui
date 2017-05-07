/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.test.webapp.query;

import java.util.*;

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
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE organizationID=1000L and (name='Frits' or lastname='Jalvingh' or shoeSize<43L) and is not null (lastname)", render(q));
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

	@Test
	public void testInWithLiterals() throws Exception {
		QCriteria<TestQueryRendering> q = QCriteria.create(TestQueryRendering.class);
		q.in("code", Arrays.asList("12", "13", "14"));
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE code in ('12','13','14')", render(q));
	}
	@Test
	public void testInWithSubquery() throws Exception {
		QSelection<TestQueryRendering> sel = QSelection.create(TestQueryRendering.class);
		sel.selectProperty("id");
		sel.gt("code", "123");

		QCriteria<TestQueryRendering> q = QCriteria.create(TestQueryRendering.class);
		q.in("code", sel);
		Assert.assertEquals("FROM to.etc.test.webapp.query.TestQueryRendering WHERE code in ((FROM to.etc.test.webapp.query.TestQueryRendering SELECT property(id) WHERE code>'123'))", render(q));
	}
}
