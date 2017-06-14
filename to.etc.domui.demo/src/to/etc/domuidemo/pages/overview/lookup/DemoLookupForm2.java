package to.etc.domuidemo.pages.overview.lookup;

import to.etc.domui.component.lookup.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public class DemoLookupForm2 extends UrlPage {
	private DataTable<Invoice> m_tbl;

	@Override
	public void createContent() throws Exception {
		LookupForm<Invoice> lf = new LookupForm<Invoice>(Invoice.class, "customer", "billingAddress", "billingCity", "invoiceDate");
		add(lf);

		//-- Click handler gets called when search button is pressed.
		lf.setClicked(new IClicked<LookupForm<Invoice>>() {
			@Override
			public void clicked(LookupForm<Invoice> clickednode) throws Exception {
				search(clickednode);
			}
		});
	}

	protected void search(LookupForm<Invoice> lf) throws Exception {
		QCriteria<Invoice> query = lf.getEnteredCriteria(); // Get query entered
		if(null == query)
			return;
		SimpleSearchModel<Invoice> ssm = new SimpleSearchModel<Invoice>(this, query);
		setQuery(ssm);
	}

	private void setQuery(SimpleSearchModel<Invoice> ssm) throws Exception {
		if(m_tbl != null) {
			//-- Table already visible. Just update it's model with the new query.
			m_tbl.setModel(ssm);
			return;
		}

		//-- We need to create a table to show the result in.
		BasicRowRenderer<Invoice> brr = new BasicRowRenderer<Invoice>(Invoice.class, "billingAddress", "billingCity", "customer.lastName", "customer.firstName", "invoiceDate", "total");
		m_tbl = new DataTable<Invoice>(ssm, brr);
		add(m_tbl);
		m_tbl.setPageSize(25);

		add(new DataPager(m_tbl)); // Add a pager too, to navigate the result set.
	}


}
