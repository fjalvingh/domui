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
final public class BindingTut5 extends UrlPage {
	private Artist m_artist;

	private Customer m_customer;

	@Override public void createContent() throws Exception {
		LookupInput2<Artist> artistC = new LookupInput2<Artist>(QCriteria.create(Artist.class));
		add(new Div()).add(artistC);
		artistC.bind().to(this, "artist");

		LookupInput2<Customer> customerC = new LookupInput2<Customer>(QCriteria.create(Customer.class));
		add(new Div()).add(customerC);
		customerC.bind().to(this, "customer");

		DefaultButton btn = new DefaultButton("Send info", a -> {
			MsgBox.info(this, "E-mailing " + customerC.getValue() + " with info on " + artistC.getValue());
		});
		add(btn);
		btn.bind("disabled").to(this, "buttonDisabled");
	}

	private boolean isButtonDisabled() {
		return m_artist == null || m_customer == null;
	}

	private Artist getArtist() {
		return m_artist;
	}

	private void setArtist(Artist artist) {
		m_artist = artist;
	}

	private Customer getCustomer() {
		return m_customer;
	}

	private void setCustomer(Customer customer) {
		m_customer = customer;
	}
}
