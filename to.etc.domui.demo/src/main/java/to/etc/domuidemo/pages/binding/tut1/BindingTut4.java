package to.etc.domuidemo.pages.binding.tut1;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-10-18.
 */
final public class BindingTut4 extends UrlPage {
	@Override public void createContent() throws Exception {
		LookupInput2<Artist> artistC = new LookupInput2<Artist>(QCriteria.create(Artist.class));
		add(new Div()).add(artistC);

		LookupInput2<Customer> customerC = new LookupInput2<Customer>(QCriteria.create(Customer.class));
		add(new Div()).add(customerC);

		DefaultButton btn = new DefaultButton("Send info", a -> {
			MsgBox.info(this, "E-mailing " + customerC.getValue() + " with info on " + artistC.getValue());
		});
		add(btn);
	}
}
