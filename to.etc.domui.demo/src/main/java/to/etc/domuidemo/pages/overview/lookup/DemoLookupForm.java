package to.etc.domuidemo.pages.overview.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

public class DemoLookupForm extends UrlPage {
	private DataTable<Invoice> m_tbl;

	private ContentPanel m_cp;

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = m_cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class, "billingAddress", "billingCity", "invoiceDate");
		cp.add(lf);

		//-- Click handler gets called when search button is pressed.
		lf.setClicked((IClicked<SearchPanel<Invoice>>) clickednode -> search(clickednode));
	}

	protected void search(SearchPanel<Invoice> lf) throws Exception {
		QCriteria<Invoice> query = lf.getCriteria(); // Get query entered
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
		rr.column("billingAddress");
		rr.column("billingCity");
		rr.column("customer.lastName");
		rr.column("customer.firstName");
		rr.column("invoiceDate");
		rr.column("total");
		m_tbl = new DataTable<Invoice>(ssm, rr);
		m_cp.add(m_tbl);
		m_tbl.setPageSize(25);

		m_cp.add(new DataPager(m_tbl)); // Add a pager too, to navigate the result set.
	}


}
