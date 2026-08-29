package to.etc.domuidemo.pages.binding.tut1.binding;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.UrlPage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-10-18.
 */
final public class EditInvoicePageB4 extends UrlPage {
	private Invoice m_invoice;

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		//-- Default invoice fields
		if(null == m_invoice.getInvoiceDate())
			m_invoice.setInvoiceDate(new Date());

		FormBuilder fb = new FormBuilder(cp);			// Insert the form to build into cp

		fb.property(m_invoice, Invoice_.customer()).control();
		fb.property(m_invoice, Invoice_.invoiceDate()).control();
		fb.property(m_invoice, Invoice_.billingAddress()).control();
		fb.property(m_invoice, Invoice_.total()).control();

		cp.add(new DefaultButton("Clear amount", a -> {
			m_invoice.setTotal(BigDecimal.ZERO);
		}));
	}

	@UIUrlParameter(name = "invoice")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		m_invoice = invoice;
	}
}
