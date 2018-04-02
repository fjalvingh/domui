package to.etc.domuidemo.pages.binding.xxxmodel;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
public class InvoiceEditScreen extends UrlPage {
	private Invoice m_invoice;

	private InvoiceEditModel m_model;

	@Override public void createContent() throws Exception {
		add(new CaptionedHeader("Invoice for " + model().getData().getCustomer().getFirstName() + model().getData().getCustomer().getLastName()));

		FormBuilder fb = new FormBuilder(this);

		Invoice d = model().getData();

		fb.property(d, "customer").control();
		fb.property(d, "billingAddress").control();
		fb.property(d, "billingCity").control();
		fb.property(d, "billingCountry").control();

		add(new VerticalSpacer(10));

		RowRenderer<InvoiceLineModel> rr = createRowRenderer();
		DataTable<InvoiceLineModel> dt = new DataTable<>(rr);
		dt.setList(model().getLines());
		add(dt);
	}

	private RowRenderer<InvoiceLineModel> createRowRenderer() {
		RowRenderer<InvoiceLineModel> rr = new RowRenderer<>(InvoiceLineModel.class);
		rr.column("data.track.name").width(40);
		rr.column("data.unitPrice").width(10);
		rr.column("data.quantity").width(5);
		return rr;
	}

	private InvoiceEditModel model() {
		InvoiceEditModel model = m_model;
		if(null == model) {
			Invoice invoice = getInvoice();
			model = m_model = new InvoiceEditModel(invoice);
		}
		return model;
	}

	@UIUrlParameter()
	public Invoice getInvoice() {
		return m_invoice;
	}

	public void setInvoice(Invoice invoice) {
		m_invoice = invoice;
	}
}
