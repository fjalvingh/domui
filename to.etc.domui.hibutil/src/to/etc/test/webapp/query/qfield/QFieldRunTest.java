package to.etc.test.webapp.query.qfield;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Test;

import to.etc.webapp.query.*;

/**
 * Class that test if the light wrapper api gives the same result as the original api
 * making the same queries.
 *
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QFieldRunTest {

	@Test
	public void test0() throws Exception {

		QTestRelationRoot r = QTestRelation.get();

		Assert.assertEquals("properName", r.properName().toString());
		Assert.assertEquals("preferredAccount.bban", r.preferredAccount().bban().toString());
		Assert.assertEquals("preferredAccount.relation.preferredAccount", r.preferredAccount().relation().preferredAccount().toString());

	}

	@Test
	public void test1() throws Exception {
		//x = a | b
		QTestRelationRoot relation = QTestRelation.get();
		relation.anum().eq(121, 123);

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.anum(), 121).eq(relation.anum(), 123);

		System.out.println("1a-OLD : " + q.toString());
		System.out.println("1a-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());

		//same with shortcut eq, hereafter only shortcuts for eq
		relation = QTestRelation.get();
		relation.anum(121, 123);

		System.out.println("1b-OLD : " + q.toString());
		System.out.println("1b-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());
	}

	@Test
	public void test2() throws Exception {

		//x=a & y!=b
		QTestRelationRoot relation = QTestRelation.get();
		relation.anum(121).preferredAccount().bban().ne("23");

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.eq(relation.anum(), 121).ne(relation.preferredAccount().bban(), "23");

		System.out.println("2-OLD : " + q.toString());
		System.out.println("2-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());
	}

	@Test
	public void test3() throws Exception {
		//x=a | y=b
		QTestRelationRoot relation = QTestRelation.get();
		relation.anum(121).or().preferredAccount().bban("23");

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.anum(), 121).eq(relation.preferredAccount().bban(), "23");

		System.out.println("3-OLD : " + q.toString());
		System.out.println("3-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());

	}

	@Test
	public void test4() throws Exception {

		//x=a or y=b and z=c  -> x=a or (y=b and z=c)
		QTestRelationRoot relation = QTestRelation.get();
		relation.anum(121).or().preferredAccount().bban("23").anum(123);

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.anum(), 121).and().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);

		System.out.println("4-OLD : " + q.toString());
		System.out.println("4-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());

	}

	@Test
	public void test5() throws Exception {
		//(x=a or y=b) and z=c , same construction as test4 with braces added
		QTestRelationRoot relation = QTestRelation.get();
		relation.$_().anum(121).or().preferredAccount().bban("23")._$().anum(123);

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.anum(), 121).eq(relation.preferredAccount().bban(), "23");
		q.and().eq(relation.anum(), 123);

		System.out.println("5-OLD : " + q.toString());
		System.out.println("5-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());


	}

	@Test
	public void test6() throws Exception {
		//x.y=a extsist in a list of x types in z
		QTestRelationRoot relation = QTestRelation.get();
		relation.banks().exists().bankname("23");

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QTestBankRoot bank = QTestBank.get();
		q.exists(relation.banks()).eq(bank.bankname(), "23");

		System.out.println("6-OLD : " + q.toString());
		System.out.println("6-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());


	}

	public static void main(String[] args) throws Exception {

		//new QFieldRunTest().test4();
		//TUtilTestRunner.run(QFieldRunTest.class, QTestRelationRoot.class, QField.class);
	}


}
