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
package to.etc.test.webapp.qsql;

import java.sql.*;
import java.util.*;

import org.junit.*;

import org.junit.experimental.categories.Category;
import to.etc.puzzler.GroupUsesDatabase;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

@Category(GroupUsesDatabase.class)
public class TestDbCompound extends TestQsqlBase {

	@Test
	public void testCompoundSQL1() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
	}

	@Test
	public void testCompoundSQL2() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).ascending("id.docnr");
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert
			.assertEquals(
				"select this_.admn_id,this_.docnr,this_.bedrag,this_.bdrg,this_.bankrekening,this_.bankrek_relatie,this_.vzop_id,this_.akst_id,this_.omschrijving,this_.betaalwijze,this_.periode,this_.jaar,this_.relatiecode,this_.relatie_naam,this_.valutadatum from v_dec_betaalopdrachten this_ order by this_.docnr asc",
				gc.getSQL());
	}

	@Test
	public void testCompoundSQL3() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).descending("id");
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert
			.assertEquals(
				"select this_.admn_id,this_.docnr,this_.bedrag,this_.bdrg,this_.bankrekening,this_.bankrek_relatie,this_.vzop_id,this_.akst_id,this_.omschrijving,this_.betaalwijze,this_.periode,this_.jaar,this_.relatiecode,this_.relatie_naam,this_.valutadatum from v_dec_betaalopdrachten this_ order by this_.admn_id desc,this_.docnr desc",
				gc.getSQL());
	}

	@Test
	public void testCompoundSQL4() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).eq("id.administrationID", "ADM1");
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);
		System.out.println(gc.getSQL());
		Assert
			.assertEquals(
				"select this_.admn_id,this_.docnr,this_.bedrag,this_.bdrg,this_.bankrekening,this_.bankrek_relatie,this_.vzop_id,this_.akst_id,this_.omschrijving,this_.betaalwijze,this_.periode,this_.jaar,this_.relatiecode,this_.relatie_naam,this_.valutadatum from v_dec_betaalopdrachten this_ where this_.admn_id=?",
				gc.getSQL());
	}

	@Test
	public void testCompoundSQL5() throws Exception {
		DecadePaymentOrderPK pk = new DecadePaymentOrderPK();
		pk.setAdministrationID("ADM1");
		pk.setDocnr(Long.valueOf(1234));

		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).eq("id", pk);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);
		System.out.println(gc.getSQL());
		Assert
			.assertEquals(
				"select this_.admn_id,this_.docnr,this_.bedrag,this_.bdrg,this_.bankrekening,this_.bankrek_relatie,this_.vzop_id,this_.akst_id,this_.omschrijving,this_.betaalwijze,this_.periode,this_.jaar,this_.relatiecode,this_.relatie_naam,this_.valutadatum from v_dec_betaalopdrachten this_ where (this_.admn_id=? and this_.docnr=?)",
				gc.getSQL());
	}

	private <T> List<T> exec(JdbcQuery<T> q) throws Exception {
		Connection dbc = getDc().getConnection();
		JdbcDataContext	jdc = new JdbcDataContext(null, dbc);
		try {
			q.dump();
			return (List<T>) q.query(jdc);
		} finally {
			try {
				dbc.close();
			} catch(Exception x) {}
		}
	}

	private <T> List<T> exec(QCriteria<T> q) throws Exception {
		JdbcQuery<T> jq = JdbcQuery.create(q);
		return exec(jq);
	}

//	@Test
	public void	testCompoundSelect1() throws Exception {
		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).limit(20);
		List<DecadePaymentOrder> res = exec(qc);

		System.out.println("Got " + res.size() + " results");
		int ix = 0;
		for(DecadePaymentOrder la : res) {
			System.out.println("id=" + la.getId() + ", desc=" + la.getPaymentDescription());
			if(ix++ > 10)
				break;
		}
	}

//	@Test
	public void testCompoundSelect2() throws Exception {
		DecadePaymentOrderPK pk = new DecadePaymentOrderPK();
		pk.setAdministrationID("ADM1");
		pk.setDocnr(Long.valueOf(4506000448l));

		QCriteria<DecadePaymentOrder> qc = QCriteria.create(DecadePaymentOrder.class).eq("id", pk);
		List<DecadePaymentOrder> res = exec(qc);

		System.out.println("Got " + res.size() + " results");
		int ix = 0;
		for(DecadePaymentOrder la : res) {
			System.out.println("id=" + la.getId() + ", desc=" + la.getPaymentDescription());
			if(ix++ > 10)
				break;
		}
//		Assert.assertEquals(1, res.size());
	}
}
