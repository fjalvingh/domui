package to.etc.domui.test.db;

import java.util.*;

import org.junit.*;

import to.etc.domuidemo.db.*;
import to.etc.webapp.query.*;

public class TestQCriteria {
	/** The dc used for each test. Created and deleted by setup fixure */
	private QDataContext m_dc;

	/**
	 * Class fixure: init database access
	 * @throws Exception
	 */
	@BeforeClass
	static public void setUp() throws Exception {
		InitTestDB.require();
	}

	@Before
	public void setUpConnection() throws Exception {
		m_dc = InitTestDB.createContext();
	}

	@After
	public void tearDownConnection() throws Exception {
		if(m_dc != null) {
			m_dc.close();
			m_dc = null;
		}
	}

	public QDataContext dc() {
		return m_dc;
	}

	/**
	 * Simple test: load all Artists.
	 * @throws Exception
	 */
	@Test
	public void testCriteria1() throws Exception {
		QCriteria<Artist> q = QCriteria.create(Artist.class);
		List<Artist> res = dc().query(q);
		Assert.assertEquals(275, res.size());
	}

	/**
	 * Test simple query name = martha
	 * @throws Exception
	 */
	@Test
	public void testCriteria2() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("firstName", "Martha");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(1, res.size());
	}

	/**
	 * Test simple query city=Paris
	 * @throws Exception
	 */
	@Test
	public void testCriteria3() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("city", "Paris");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(2, res.size());
	}

	@Test
	public void testCriteria4() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("city", "paris");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(0, res.size());
	}

	/**
	 * test case independence in ilike.
	 * @throws Exception
	 */
	@Test
	public void testCriteria5() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).ilike("city", "paris");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(2, res.size());
	}

	/**
	 * Combinatory AND simple.
	 * @throws Exception
	 */
	@Test
	public void testAnd1() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).ilike("country", "germany").ilike("firstName", "ha%");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(1, res.size());
	}

	/**
	 * Combinatory OR simple.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOr1() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class);
		QRestrictor<Customer> or = q.or();
		or.eq("country", "Germany");
		or.eq("country", "France");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(9, res.size());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Join combinatories.									*/
	/*--------------------------------------------------------------*/

	@Test
	public void testParent1() throws Exception {
		Employee e = dc().find(Employee.class, Long.valueOf(3));
		Assert.assertNotNull(e);
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("supportRepresentative", e);
		List<Customer> res = dc().query(q);
		//		for(Customer c : res)
		//			System.out.println(c.getSupportRepresentative().getId());

		Assert.assertEquals(21, res.size());
	}

	/**
	 * Create condition on PARENT record field.
	 * @throws Exception
	 */
	@Test
	public void testParent2() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("supportRepresentative.firstName", "Margaret");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(20, res.size());
	}

	/**
	 * Create AND condition on PARENT record fields (1-up, 2x).
	 * @throws Exception
	 */
	@Test
	public void testParentAnd1() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("supportRepresentative.firstName", "Margaret").eq("supportRepresentative.lastName", "Park");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(20, res.size());
	}

	/**
	 * Create AND condition on PARENT record fields (1-up, 2x) that should not return results.
	 * @throws Exception
	 */
	@Test
	public void testParentAnd2() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class).eq("supportRepresentative.firstName", "Margaret").eq("supportRepresentative.lastName", "Krap");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(0, res.size());
	}

	/**
	 * Create AND condition on PARENT record fields (1-up, 2x).
	 * @throws Exception
	 */
	@Test
	public void testParentOr1() throws Exception {
		QCriteria<Customer> q = QCriteria.create(Customer.class);
		QRestrictor<Customer> or = q.or();
		or.eq("supportRepresentative.firstName", "Margaret");
		or.eq("supportRepresentative.firstName", "Robert");
		List<Customer> res = dc().query(q);
		Assert.assertEquals(20, res.size());
	}

	/**
	 * Do an "and" between two fields of <i>different</i> parent relations.
	 * @throws Exception
	 */
	@Test
	public void testAliases1() throws Exception {
		QCriteria<InvoiceLine> q = QCriteria.create(InvoiceLine.class);
		q.eq("invoice.billingCity", "Amsterdam");
		q.eq("track.name", "So Fine");
		List<InvoiceLine> res = dc().query(q);
//		for(InvoiceLine l : res)
//			System.out.println(">>> " + l.getTrack().getName());
		Assert.assertEquals(1, res.size());
	}

	/**
	 * Do an "or" between two fields of <i>different</i> parent relations.
	 * @throws Exception
	 */
	@Test
	public void testAliases2() throws Exception {
		QCriteria<InvoiceLine> q = QCriteria.create(InvoiceLine.class);
		QRestrictor<InvoiceLine> or = q.or();
		or.eq("invoice.billingCity", "Amsterdam");
		or.eq("track.name", "So Fine");
		List<InvoiceLine> res = dc().query(q);
//		for(InvoiceLine l : res)
//			System.out.println(">>> " + l.getTrack().getName());
		Assert.assertEquals(49, res.size());
	}


//	@Test
//	public void testAliases2() throws Exception {
//		QCriteria<Customer> q = QCriteria.create(Customer.class);
//		QRestrictor<Customer> or = q.or();
//		or.eq("supportRepresentative.firstName", "Margaret");
//		or.eq("aaaList.bbb", "ccc");
//		List<Customer> res = dc().query(q);
//		Assert.assertEquals(20, res.size());
//	}

}
