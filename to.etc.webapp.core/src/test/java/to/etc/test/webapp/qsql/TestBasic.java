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

import javax.sql.*;

import org.junit.*;

import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;
import to.etc.webapp.testsupport.*;


public class TestBasic {
	static private DataSource m_ds;

	static private QDataContext m_dc;

//	@BeforeClass
	static public void setUp() throws Exception {
		m_ds = TUtilTestProperties.getRawDataSource();
		Connection dbc = m_ds.getConnection();
		m_dc = new JdbcDataContext(null, dbc);
	}

	static <T> List<T> exec(JdbcQuery<T> q) throws Exception {
		//		Connection dbc = m_ds.getConnection();
		//		JdbcDataContext	jdc = new JdbcDataContext(null, dbc);
		q.dump();
		return (List<T>) q.query(m_dc);
	}

	static <T> List<T> exec(QCriteria<T> q) throws Exception {
		JdbcQuery<T> jq = JdbcQuery.create(q);
		return exec(jq);
	}

	@Test
	public void testSQLGen1() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert.assertEquals("select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_", gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(0, gc.getValList().size());
	}

	@Test
	public void testSQLGen2() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class).eq("id", Long.valueOf(12)).eq("code", "BR12");

		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert.assertEquals("select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_ where this_.ID=? and this_.grbr_code=?", gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(2, gc.getValList().size());
	}

	@Test
	public void testSQLGen3() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class).eq("id", Long.valueOf(12)).eq("code", "BR12");
		qc.ascending("code");
		qc.descending("description");

		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert
			.assertEquals(
				"select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_ where this_.ID=? and this_.grbr_code=? order by this_.grbr_code asc,this_.omschrijving desc",
				gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(2, gc.getValList().size());
	}

//	@Test
	public void testExec1() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		List<LedgerAccount> res = exec(qc);

		System.out.println("Got " + res.size() + " results");
		int ix = 0;
		for(LedgerAccount la : res) {
			if(ix++ > 10)
				break;
			System.out.println("la: " + la.getCode() + ", " + la.getDescription() + ", " + la.getTypeDescription() + ", " + la.getId());
		}
		Assert.assertTrue(res.size() != 0);
	}

	//@Test
	/* Disabled because not every database contains the LedgerAccounts with code like 'E%'
	 * FIXME when there is a standard way of adding records to the database for test purposes.
	 *  */
	public void testExec2() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class).like("code", "E%");
		List<LedgerAccount> res = exec(qc);

		System.out.println("Got " + res.size() + " results");
		//		int ix = 0;
		for(LedgerAccount la : res) {
			if(!la.getCode().startsWith("E"))
				Assert.fail("Got code not starting with E: " + la.getCode());
		}
		Assert.assertTrue(res.size() != 0);
	}
	
	@Test
	public void testSQLGen5() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		qc.not().like("code", "%E7%");

		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert.assertEquals("select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_ where not this_.grbr_code like ?", gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(1, gc.getValList().size());
	}

	@Test
	public void testSQLGen6() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		QRestrictor<LedgerAccount> or = qc.or();
		QRestrictor<LedgerAccount> and = or.and();
		and.not().like("code", "%E4%");
		and.not().like("code", "%E5%"); //other variant of appending operator...
		
		or.not().like("description", "Overige%");

		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert.assertEquals("select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_ where not this_.grbr_code like ? and not this_.grbr_code like ? or not this_.omschrijving like ?", gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(3, gc.getValList().size());
	}
	
	@Test
	public void testSQLGen7() throws Exception {
		QCriteria<LedgerAccount> qc = QCriteria.create(LedgerAccount.class);
		QRestrictor<LedgerAccount> or = qc.not().or();
		or.not().like("code", "%E4%");
		or.not().like("code", "%E5%");
		or.not().like("code", "%E6%");

		JdbcSQLGenerator gc = new JdbcSQLGenerator();
		gc.visitCriteria(qc);

		System.out.println(gc.getSQL());
		Assert.assertEquals("select this_.ID,this_.grbr_code,this_.omschrijving,this_.grbr_type_omschrijving from v_dec_grootboekrekeningen this_ where not ( not this_.grbr_code like ? or not this_.grbr_code like ? or not this_.grbr_code like ?)", gc.getSQL());
		Assert.assertEquals(1, gc.getRetrieverList().size());
		Assert.assertEquals(3, gc.getValList().size());
	}
	
}
