package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.webapp.query.QCriteria;

/**
 * A lookup form using properties to define what fields to lookup on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelManual3 extends AbstractSearchPage<Invoice> {
	public SearchPanelManual3() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf.getCriteria()));

		//-- Create a combobox of customers
		QCriteria<Customer> q = QCriteria.create(Customer.class)	// All customers with last names starting with A
			.ilike("lastName", "B%");
		ComboLookup2<Customer> customerC = new ComboLookup2<>(q);
		customerC.setContentRenderer((node, value) -> node.add(value.getFirstName() + " " + value.getLastName()));

		lf.add().property("customer").control(customerC);

		lf.add().property("total").control();				// Allow searching for a total
		lf.add().property("invoiceDate").control();			// And the date.
	}
}
