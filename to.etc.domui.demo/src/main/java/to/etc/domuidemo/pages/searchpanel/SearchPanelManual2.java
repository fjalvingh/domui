package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.searchpanel.lookupcontrols.DatePeriod;
import to.etc.domui.component.searchpanel.lookupcontrols.NumberLookupValue;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QOperation;

import java.math.BigDecimal;

/**
 * Manual configuration with defaults.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelManual2 extends AbstractSearchPage<Invoice> {
	public SearchPanelManual2() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf.getCriteria()));

		//-- Find customer by ID
		Customer defaultCustomer = getSharedContext().get(Customer.class, Long.valueOf(10));
		lf.add().property("customer").defaultValue(defaultCustomer).control();	// Default customer

		//-- Default the search total to >= 5.0
		NumberLookupValue nlv = new NumberLookupValue(QOperation.GE, BigDecimal.valueOf(5.0));
		lf.add().property("total").defaultValue(nlv).control();				// Allow searching for a total

		//-- Default the date to before 2010.
		DatePeriod period = new DatePeriod(null, DateUtil.dateFor(2010, 0, 1));
		lf.add().property("invoiceDate").defaultValue(period).control();			// And the date.
	}
}
