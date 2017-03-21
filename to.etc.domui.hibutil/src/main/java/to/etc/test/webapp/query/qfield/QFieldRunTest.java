package to.etc.test.webapp.query.qfield;

import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Test;

import to.etc.webapp.query.*;

/**
 * Class that test if the light wrapper api gives the same result as the original api
 * making the same queries.
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

		//b
		relation = QTestRelation.get();
		relation.anum(12).banks().exists().bankname("23");

		q = QCriteria.create(TestRelation.class);
		bank = QTestBank.get();
		q.eq(relation.anum(), 12);
		q.exists(relation.banks()).eq(bank.bankname(), "23");

		System.out.println("6b-OLD : " + q.toString());
		System.out.println("6b-NEW : " + relation.getCriteria().toString());
		assertEquals(q.toString(), relation.getCriteria().toString());

		//c
		QTestBankAccountRoot bankAccount = QTestBankAccount.get();
		bankAccount.relation().banks().exists().bankname("23");

		QCriteria<TestBankAccount> q2 = QCriteria.create(TestBankAccount.class);

		q2.exists(bankAccount.relation().banks()).eq(bank.bankname(), "23");

		System.out.println("6c-OLD : " + q2.toString());
		System.out.println("6c-NEW : " + bankAccount.getCriteria().toString());
		assertEquals(q2.toString(), bankAccount.getCriteria().toString());

	}

	@Test
	public void test7() throws Exception {
		//(x=a and y=b) or (x=c and y=d)
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_().anum(121).logModule("dd")._$()//
			.or()//
			.$_().preferredAccount().bban("23").anum(123)._$();

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		or.and().eq(relation.anum(), 121).eq(relation.logModule(), "dd");
		or.and().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);


		System.out.println("7-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("7-NEW : " + q2.toString());
		assertEquals(q.toString(), q2.toString());

	}

	@Test
	public void test8() throws Exception {
		//(x=a or y=b) and (x=c or y=d)
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_().anum(121).or().logModule("dd")._$()//
			.$_().preferredAccount().bban("23").or().anum(123)._$()//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.anum(), 121).eq(relation.logModule(), "dd");
		q.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);


		System.out.println("8-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("8-NEW : " + q2.toString());
		assertEquals(q.toString(), q2.toString());
	}

	@Test
	public void test9() throws Exception {
		//(x=a or y=b) and (x=c or y=d) and x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_().anum(121).or().logModule("dd")._$()//
			.$_().preferredAccount().bban("23").or().anum(123)._$()//
			.anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		q.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		q.eq(relation.anum(), 1024);
		q.or().eq(relation.anum(), 121).eq(relation.logModule(), "dd");


		System.out.println("9-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("9-NEW : " + q2.toString());
		assertEquals(q.toString(), q2.toString());
	}

	@Test
	public void test10() throws Exception {
		//(x=a or y=b) and (x=c or y=d) or x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_().anum(121).or().logModule("dd")._$()//
			.$_().preferredAccount().bban("23").or().anum(123)._$()//
			.or().anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		QRestrictor<TestRelation> and = or.and();
		and.or().eq(relation.anum(), 121).eq(relation.logModule(), "dd");
		and.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		or.eq(relation.anum(), 1024);


		System.out.println("10-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("10-NEW : " + q2.toString());
		assertEquals(q.toString(), q2.toString());
	}

	@Test
	public void test11() throws Exception {
		// ((x=a and y=b) or (x=c and y=d)) and x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_()//
			.$_().anum(121).logModule("dd")._$().or().$_().preferredAccount().bban("23").anum(123)._$()//
			._$()
			.anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> and = q.and();
		QRestrictor<TestRelation> or = and.or();
		or.and().eq(relation.anum(), 121).eq(relation.logModule(), "dd");
		or.and().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		and.eq(relation.anum(), 1024);


		System.out.println("11-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("11-NEW : " + q2.toString());

		assertEquals(q.toString(), q2.toString());

	}

	@Test
	public void test12() throws Exception {
		// (x=a and y=b) and x=f or (x=c and y=d)
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_().preferredAccount().bban("23").anum(123)._$()//
			.anum(1024)//
			.or()//
			.$_().anum(121).logModule("dd")._$()//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		QRestrictor<TestRelation> and = or.and();
		and.eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		and.eq(relation.anum(), 1024);
		or.and().eq(relation.anum(), 121).eq(relation.logModule(), "dd");

		System.out.println("12-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("12-NEW : " + q2.toString());

		assertEquals(q.toString(), q2.toString());

	}

	@Test
	public void test13() throws Exception {
		//((x=a or y=b) and (x=c or y=d)) or x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_()//
			.$_().anum(121).or().logModule("dd")._$().$_().preferredAccount().bban("23").or().anum(123)._$()//
			._$()//
			.or().anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		QRestrictor<TestRelation> and = or.and();
		and.or().eq(relation.anum(), 121).eq(relation.logModule(), "dd");
		and.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		or.eq(relation.anum(), 1024);


		System.out.println("13-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("13-NEW : " + q2.toString());

		assertEquals(q.toString(), q2.toString());

	}

	@Test
	public void test14() throws Exception {
		//((x=a or x=b or y=b) and (x=c or y=d)) or x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_()//
			.$_().anum(121, 122).or().logModule("dd")._$().$_().preferredAccount().bban("23").or().anum(123)._$()//
			._$()//
			.or().anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		QRestrictor<TestRelation> and = or.and();
		and.or().eq(relation.anum(), 121).eq(relation.anum(), 122).eq(relation.logModule(), "dd");
		and.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		or.eq(relation.anum(), 1024);


		System.out.println("14-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("14-NEW : " + q2.toString());

		assertEquals(q.toString(), q2.toString());

	}

	@Test
	public void test15() throws Exception {
		//((y=b or x=a or x=b ) and (x=c or y=d)) or x=f
		QTestRelationRoot relation = QTestRelation.get();
		relation//
			.$_()//
			.$_().logModule("dd").or().anum(121, 122)._$().$_().preferredAccount().bban("23").or().anum(123)._$()//
			._$()//
			.or().anum(1024)//
		;

		QCriteria<TestRelation> q = QCriteria.create(TestRelation.class);
		QRestrictor<TestRelation> or = q.or();
		QRestrictor<TestRelation> and = or.and();
		and.or().eq(relation.logModule(), "dd").eq(relation.anum(), 121).eq(relation.anum(), 122);
		and.or().eq(relation.preferredAccount().bban(), "23").eq(relation.anum(), 123);
		or.eq(relation.anum(), 1024);


		System.out.println("15-OLD : " + q.toString());
		QCriteria<TestRelation> q2 = relation.getCriteria();
		System.out.println("15-NEW : " + q2.toString());

		assertEquals(q.toString(), q2.toString());

	}
	public static void main(String[] args) throws Exception {
		new QFieldRunTest().test15();
		//TUtilTestRunner.run(QFieldRunTest.class, QTestRelationRoot.class, QField.class);
		//	System.err.println(false & true | false & true);
	}


}
