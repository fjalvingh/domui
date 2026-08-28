package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;
import to.etc.webapp.query.QCriteria;

/**
 * Everything the shop knows about one customer: the contact and address details
 * on top, and what that customer has bought below it. The purchase history is
 * queried rather than bound, because the customer record has no invoice
 * collection - which is exactly how you would do it for a list that can grow
 * without bound.
 */
public class CustomerDetailPage extends UrlPage {
	private Customer m_customer;

	@Override
	public String getPageTitle() {
		return m_customer.getId() == null ? "New customer" : m_customer.toString();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Customer"));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_customer, Customer_.firstName()).mandatory().control();
		fb.property(m_customer, Customer_.lastName()).mandatory().control();
		fb.property(m_customer, Customer_.company()).control();
		fb.property(m_customer, Customer_.email()).mandatory().control();
		fb.property(m_customer, Customer_.phone()).control();
		fb.property(m_customer, Customer_.fax()).control();

		fb.property(m_customer, Customer_.address()).control();
		fb.property(m_customer, Customer_.postalCode()).control();
		fb.property(m_customer, Customer_.city()).control();
		fb.property(m_customer, Customer_.state()).control();
		fb.property(m_customer, Customer_.country()).control();

		//-- Who looks after this customer.
		fb.property(m_customer, Customer_.supportRepresentative())
			.control(new ComboLookup2<>(QCriteria.create(Employee.class).ascending("lastName")));

		cp.add(new VerticalSpacer(10));
		cp.add(new HTag(2, "Purchase history"));
		cp.add(createInvoiceTable());

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	private DataTable<Invoice> createInvoiceTable() throws Exception {
		QCriteria<Invoice> q = QCriteria.create(Invoice.class)
			.eq(Invoice_.customer(), m_customer)
			.descending(Invoice_.invoiceDate());

		RowRenderer<Invoice> rr = new RowRenderer<>(Invoice.class);
		rr.column(Invoice_.invoiceDate()).label("Date").descending().sortdefault();
		rr.column(Invoice_.billingCity()).label("Billed to");
		rr.column(Invoice_.total()).label("Total");
		rr.setRowClicked(invoice -> UIGoto.moveSub(InvoiceDetailPage.class, "id", invoice.getId()));

		DataTable<Invoice> dt = new DataTable<>(new SimpleSearchModel<>(this, q), rr);
		dt.setPageSize(10);
		return dt;
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			getSharedContext().save(m_customer);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}

	@UIUrlParameter(name = "id")
	public Customer getCustomer() {
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		m_customer = customer;
	}
}
