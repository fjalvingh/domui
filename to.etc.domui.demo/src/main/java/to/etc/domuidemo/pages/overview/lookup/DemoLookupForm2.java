package to.etc.domuidemo.pages.overview.lookup;

import to.etc.domui.component.lookup.LookupForm;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

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
		RowRenderer<Invoice> rr = new RowRenderer<>(Invoice.class);
		rr.column(Invoice_.customer().firstName()).ascending();
		rr.column(Invoice_.customer().lastName()).ascending();
		rr.column(Invoice_.invoiceDate()).ascending();
		rr.column(Invoice_.billingAddress()).ascending();
		rr.column(Invoice_.billingCity()).ascending();
		rr.column(Invoice_.total()).ascending();

		m_tbl = new DataTable<>(ssm, rr);
		add(m_tbl);
		m_tbl.setPageSize(25);

		add(new DataPager(m_tbl)); // Add a pager too, to navigate the result set.
	}


}
