package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.masterchild.ChildFragment;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.InvoiceLine;
import to.etc.domui.derbydata.db.InvoiceLine_;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;

import java.math.BigDecimal;

/**
 * A sales invoice: the header with the customer and the billing address, and the
 * lines that were sold as the detail part. The lines are bound to the invoice's
 * own collection, so the table follows the record.
 */
public class InvoiceDetailPage extends UrlPage {
	private Invoice m_invoice;

	@Override
	public String getPageTitle() {
		return "Invoice " + m_invoice.getId();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Invoice " + m_invoice.getId()));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_invoice, Invoice_.customer()).readOnly().control();
		fb.property(m_invoice, Invoice_.invoiceDate()).mandatory().control();
		fb.property(m_invoice, Invoice_.billingAddress()).control();
		fb.property(m_invoice, Invoice_.billingPostalCode()).control();
		fb.property(m_invoice, Invoice_.billingCity()).control();
		fb.property(m_invoice, Invoice_.billingState()).control();
		fb.property(m_invoice, Invoice_.billingCountry()).control();
		fb.property(m_invoice, Invoice_.total()).readOnly().control();

		cp.add(new VerticalSpacer(10));
		cp.add(new HTag(2, "What was sold"));

		ChildFragment<Invoice, InvoiceLine> lines = new ChildFragment<>(m_invoice, Invoice_.invoiceLines());
		cp.add(lines);
		lines.column(InvoiceLine_.track().name());
		lines.column(InvoiceLine_.track().album().title());
		lines.column(InvoiceLine_.quantity());
		lines.column(InvoiceLine_.unitPrice());
		lines.column().label("Line total").renderer((node, line) -> node.add(lineTotal(line).toString()));
		lines.onClick(line -> UIGoto.moveSub(TrackDetails.class, "id", line.getTrack().getId()));

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addLinkButton("Show the customer", Theme.BTN_FIND, a -> UIGoto.moveSub(CustomerDetailPage.class, "id", m_invoice.getCustomer().getId()));
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	static private BigDecimal lineTotal(InvoiceLine line) {
		BigDecimal price = line.getUnitPrice();
		if(null == price)
			return BigDecimal.ZERO;
		return price.multiply(BigDecimal.valueOf(line.getQuantity()));
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			getSharedContext().save(m_invoice);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}

	@UIUrlParameter(name = "id")
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		m_invoice = invoice;
	}
}
