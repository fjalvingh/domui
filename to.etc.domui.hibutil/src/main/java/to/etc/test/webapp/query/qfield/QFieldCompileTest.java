package to.etc.test.webapp.query.qfield;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QRestrictor;

import java.util.List;

/**
 * Not really meant to run. It checks compilation errors that should occur.
 * Commented out ofcourse
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * @ignore Test fails, and it also says in api doc that it is not really meant to be run !?...
 * Created on Feb 3, 2013
 */
@Ignore
public class QFieldCompileTest {
	@Test
	public void testPaths() throws Exception {
		QDataContext dc = null;
		QTestRelationRoot r = QTestRelation.get();
		QCriteria<TestRelation> q = r.getCriteria();

		if(q != null) {//compiler
			q.eq(r.properName(), "bla");
		}

		if(dc != null) {
			List<TestRelation> l = r.query(dc);
		}

		TestBank bank = new TestBank();
		//is same
		r.preferredAccount().bank().eq(bank);
		q.eq(r.preferredAccount().bank(), bank);
		//q.eq(QTestBankAccountEntity.get().bank(), bank);//cannot do this from other entity root
		//q.eq(QTestBankAccountEntity.get().relation().preferredAccount(), new BankAccount());//cannot do this either
		q.eq(r.preferredAccount().bban(), "123");
		q.eq(r.preferredAccount().relation(), new TestRelation());
		q.eq(r.anum(), 123.0);
		//q.eq(r.banks(), 123.0);cannot use list as eq field or get properties from it
		QRestrictor<TestBank> exists = q.exists(r.banks());
		//q.exists(QTestBankAccount.get().relation().banks());//cannot get exists from different root
		//exists.eq(r.preferredAccount().bank().bankname(), "123456");//cannot do this by mistake, will not compile
		exists.eq(QTestBank.get().bankname(), "123456");//have to use root on exists

		//is same
		QTestBankRoot b = QTestBank.get();
		r.banks().exists().bankname().eq("ing", "abn");
		q.exists(r.banks()).eq(b.bankname(), "ing").or().eq(b.bankname(), "abn");

		//new root
		Assert.assertEquals("bankname", b.bankname().toString());

//		TabularFormBuilder builder = new TabularFormBuilder(new TestBankAccount()) {
//			@Override
//			public <T> IControl<T> addProp(QField< ? , T> field) {//current application unset otherwise, this is just a compile test
//				return null;
//			}
//		};
//		builder.addProp(b.bankname());
//		builder.addProp(r.preferredAccount());
//		builder.addProp(r.preferredAccount().relation());
	}

	public static void main(String[] args) throws Exception {
		//TUtilTestRunner.run(QFieldCompileTest.class, null);
	}


}
