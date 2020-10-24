package to.etc.domuidemo.pages.spi;

import to.etc.domui.annotations.UIPage;
import to.etc.domui.annotations.UIReinject;
import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.SubPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@UIPage("invoice/{id}")
public class TestEditPage1 extends SubPage {
	@UIReinject
	private Invoice m_invoice;

	@Override public void createContent() throws Exception {
		ButtonBar2 bb = new ButtonBar2();
		add(bb);
		bb.addBackButton();

		var cp = add(new ContentPanel());

		FormBuilder fb = new FormBuilder(cp);
		fb.property(getInvoice(), Invoice_.customer()).control();
		fb.property(getInvoice(), Invoice_.billingAddress()).control();
		fb.property(getInvoice(), Invoice_.billingCity()).control();
		fb.property(getInvoice(), Invoice_.billingCountry()).control();
		fb.property(getInvoice(), Invoice_.billingPostalCode()).control();
	}

	@UIUrlParameter(name = "id")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		m_invoice = invoice;
	}
}
