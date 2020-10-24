package to.etc.domuidemo.pages.spi;

import to.etc.domui.annotations.UIPage;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.SubPage;
import to.etc.webapp.query.QCriteria;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-20.
 */
@UIPage("test")
public class TestDataPage1 extends SubPage {
	@Override public void createContent() throws Exception {
		QCriteria<Invoice> q = QCriteria.create(Invoice.class);
		SimpleSearchModel<Invoice> sm = new SimpleSearchModel<>(this, q);

		RowRenderer<Invoice> rr = new RowRenderer<>(Invoice.class);
		rr.column("invoiceDate").label("Date");
		rr.column().renderer((n, a) -> n.add(a.getCustomer().getLastName() + ", " + a.getCustomer().getFirstName())).sort("customer.lastName").width(25).label("Customer");
		rr.column("billingAddress").label("Address");
		rr.column("billingCity").label("City");
		rr.column("total").label("Amount");
		DataTable<Invoice> dt = new DataTable<>(sm, rr);
		dt.setPageSize(25);
		add(new DataPager(dt));
		add(dt);
		add(new DataPager(dt));
	}
}
