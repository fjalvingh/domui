package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.state.UIGoto;

/**
 * Find a sales invoice.
 */
public class InvoiceListPage extends AbstractCdShopListPage<Invoice> {
	public InvoiceListPage() {
		super(Invoice.class, "Sales invoices");
	}

	@Override
	protected void configureColumns(@NonNull RowRenderer<Invoice> rr) throws Exception {
		rr.column(Invoice_.invoiceDate()).descending().sortdefault();
		rr.column(Invoice_.customer().lastName()).ascending();
		rr.column(Invoice_.billingCity()).ascending();
		rr.column(Invoice_.total());
	}

	@Override
	protected void onRowSelected(@NonNull Invoice instance) throws Exception {
		UIGoto.moveSub(InvoiceDetailPage.class, "id", instance.getId());
	}
}
