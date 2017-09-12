package to.etc.domui.test.db;

import java.math.*;
import java.util.*;

import org.junit.*;

import to.etc.domui.derbydata.db.Album;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.InvoiceLine;
import to.etc.webapp.query.*;

public class TestDbQCriteria {
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

	/**
	 * Test join and property/property restriction on subqueries
	 * @throws Exception
	 */
	@Test
	public void testSubCriteriaJoin() throws Exception {
		//first calculate expected outcome
		QCriteria<InvoiceLine> iq = QCriteria.create(InvoiceLine.class);
		List<InvoiceLine> l = dc().query(iq);
		int resc = 0;

		for(InvoiceLine il : l) {
			BigDecimal maxUnitPrice = null;
			for(InvoiceLine invoiceLine : il.getInvoice().getInvoiceLines()) {
				if(invoiceLine == il) {
					continue;
				}
				if(maxUnitPrice == null || invoiceLine.getUnitPrice().doubleValue() > maxUnitPrice.doubleValue()) {
					maxUnitPrice = invoiceLine.getUnitPrice();
				}
			}
			if(maxUnitPrice != null && il.getUnitPrice().doubleValue() == maxUnitPrice.doubleValue()) {
				resc++;
			}
		}

		//perform as subquery
		QCriteria<InvoiceLine> rootq = QCriteria.create(InvoiceLine.class);
		QSubQuery<InvoiceLine, InvoiceLine> subq = rootq.subquery(InvoiceLine.class);
		subq.max("unitPrice");
		subq.join("invoice");
		subq.join(rootq).ne("id", "id");

		rootq.eq("unitPrice", subq);

		System.out.println("q = " + rootq);

		List<InvoiceLine> res = dc().query(rootq);
		Assert.assertEquals(resc, res.size());

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


	/**
	 * Test to prove that the subquery path does not work in some cases
	 * @throws Exception
	 */
	@Test
	public void subQueryPathSqlOutcome() throws Exception {

		//it is not relevant for this test that it is a nonsense query
		QCriteria<Invoice> outerq = QCriteria.create(Invoice.class);
		QSelection<InvoiceLine> subq = QSelection.create(InvoiceLine.class);

		//expect join invoice customer in parent query
		outerq.eq("customer.firstName", "x");
		outerq.eq("total", new QSelectionSubquery(subq));

		//expect join invoice customer in sub query
		subq.selectProperty("unitPrice");
		subq.eq("invoice.customer.firstName", "x");

		//the sql is as expected in this query
//	    select * from
//        Invoice this_
//    inner join
//        Customer a_1x1_
//            on this_.CustomerId=a_1x1_.CustomerId
//    where
//        a_1x1_.FirstName=?
//        and this_.Total = (
//            select
//                a_2_.UnitPrice as y0_
//            from
//                InvoiceLine a_2_
//            inner join
//                Invoice a_3x1_
//                    on a_2_.InvoiceId=a_3x1_.InvoiceId
//            inner join
//                Customer a_4x2_
//                    on a_3x1_.CustomerId=a_4x2_.CustomerId
//            where
//                a_4x2_.FirstName=?
//        )
		dc().query(outerq);

		//the following that is basically the same but one level higher on invoice instead of line does not join customer in the subquery
		QCriteria<Invoice> qmain2 = QCriteria.create(Invoice.class);
		QSelection<Invoice> qsub2 = QSelection.create(Invoice.class);

		//expect join invoice customer in parent query
		qmain2.eq("customer.firstName", "x");
		qmain2.eq("total", new QSelectionSubquery(qsub2));

		//expect join invoice customer in sub query
		qsub2.selectProperty("total");
		qsub2.eq("customer.firstName", "x");

		dc().query(qmain2);

		//no expected result but sql is
//	    select * from
//        Invoice this_
//    inner join
//        Customer a_1x1_
//            on this_.CustomerId=a_1x1_.CustomerId
//    where
//        a_1x1_.FirstName=?
//        and this_.Total = (
//            select
//                a_2_.Total as y0_
//            from
//                Invoice a_2_
//            where
//                a_1x1_.FirstName=?
//        )

		//while expect this
//    select * from
//        Invoice this_
//    inner join
//        Customer a_1x1_
//            on this_.CustomerId=a_1x1_.CustomerId
//    where
//        a_1x1_.FirstName=?
//        and this_.Total = (
//            select
//                a_2_.Total as y0_
//            from
//                Invoice a_2_
//            inner join
//                Customer a_4x2_
//                     on a_2_.CustomerId=a_4x2_.CustomerId
//            where
//                a_1x1_.FirstName=?
//        )

	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Interface-based query results.						*/
	/*--------------------------------------------------------------*/

	private interface MyData {
		@QFld(0)
		double sum();

		@QFld(1)
		Customer dude();
	}

	@Test
	public void testIfQuery1() throws Exception {
		QSelection<Invoice> q = QSelection.create(Invoice.class);
		q.sum("total");
		q.selectProperty("customer");

		List<MyData> ires = dc().query(MyData.class, q);
		for(MyData md : ires) {
			System.out.println("val=" + md.sum() + ", customer=" + md.dude());
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	"in" query													*/
	/*----------------------------------------------------------------------*/

	@Test
	public void testInQuery1() throws Exception {
		QCriteria<Album> q = QCriteria.create(Album.class)
			.in("title", Arrays.asList("Led Zeppelin I", "Led Zeppelin II", "Led Zeppelin III"))
			;
		List<Album> ires = dc().query(q);
		Assert.assertEquals(3, ires.size());
	}


}
