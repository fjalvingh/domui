package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.derbydata.db.Invoice;

/**
 * A lookup form using properties to define what fields to lookup on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelManual1 extends AbstractSearchPage<Invoice> {
	public SearchPanelManual1() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf.getCriteria()));

		lf.add().property("customer").control();			// Start with lookup by customer
		lf.add().property("total").control();				// Allow searching for a total
		lf.add().property("invoiceDate").control();			// And the date.
	}
}
