package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Artist;
import to.etc.domui.derbydata.db.Artist_;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * Tutorial, "data binding", step 3: binding to a control property other than
 * its value. Those bindings only move one way - model to control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BindPropertyPage extends UrlPage {
	private final SendInfoModel m_model = new SendInfoModel();

	@Override
	public void createContent() throws Exception {
		setPageTitle("Binding a control property");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Binding a control property"));

		QCriteria<Artist> aq = QCriteria.create(Artist.class);
		aq.ascending(Artist_.name()).limit(20);
		ComboLookup2<Artist> artistC = new ComboLookup2<>(getSharedContext().query(aq));

		QCriteria<Customer> cq = QCriteria.create(Customer.class);
		cq.ascending(Customer_.lastName()).limit(20);
		ComboLookup2<Customer> customerC = new ComboLookup2<>(getSharedContext().query(cq));

		artistC.bind().to(m_model, SendInfoModel_.artist());
		customerC.bind().to(m_model, SendInfoModel_.customer());
		artistC.immediate();
		customerC.immediate();

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Artist").control(artistC);
		fb.label("Customer").control(customerC);

		DefaultButton send = new DefaultButton("Send info", a -> MsgBox.info(this,
			"E-mailing " + m_model.getCustomer() + " with info on " + m_model.getArtist()));
		cp.add(send);

		//-- The model decides whether the button may be pressed; the button follows.
		send.bind(IControl.DISABLED).to(m_model, SendInfoModel_.sendDisabled());
	}
}
