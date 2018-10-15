package to.etc.domuidemo.pages.binding.tut1;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.input.DateInput2;
import to.etc.domui.component.input.Text;
import to.etc.domui.component.input.TextStr;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-10-18.
 */
public class EditInvoicePageB1 extends UrlPage {
	private Invoice m_invoice;

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		//-- Default invoice fields
		if(null == m_invoice.getInvoiceDate())
			m_invoice.setInvoiceDate(new Date());

		//-- Manually create a form layout for educational pps - usually this is done using FormBuilder.
		TBody tb = cp.addTable();
		tb.addCssClass("ui-tbl-spaced");

		LookupInput2<Customer> custLI = new LookupInput2<Customer>(QCriteria.create(Customer.class));
		tb.addRowAndCell().add(new Label(custLI, "Customer"));
		tb.addCell().add(custLI);

		DateInput2 dateC = new DateInput2();
		tb.addRowAndCell().add(new Label(dateC, "Invoice Date"));
		tb.addCell().add(dateC);

		TextStr addrC = new TextStr();
		tb.addRowAndCell().add(new Label(addrC, "Address"));
		tb.addCell().add(addrC);

		Text<BigDecimal> amountC = new Text<>(BigDecimal.class);
		tb.addRowAndCell().add(new Label(amountC, "Amount"));
		tb.addCell().add(amountC);

		//-- Now bind the components to the m_invoice instance's properties!
		custLI.bind().to(m_invoice, "customer");
		dateC.bind().to(m_invoice, "invoiceDate");
		addrC.bind().to(m_invoice, "billingAddress");
		amountC.bind().to(m_invoice, "total");
	}

	@UIUrlParameter(name = "invoice")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		m_invoice = invoice;
	}
}
