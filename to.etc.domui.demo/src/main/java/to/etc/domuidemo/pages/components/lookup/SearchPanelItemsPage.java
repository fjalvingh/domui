package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.searchpanel.lookupcontrols.DatePeriod;
import to.etc.domui.component.searchpanel.lookupcontrols.NumberLookupValue;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QOperation;

import java.math.BigDecimal;

/**
 * SearchPanel: saying which fields to search on, how each one should look, and
 * what it starts out holding.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SearchPanelItemsPage extends AbstractSearchPage<Invoice> {
	public SearchPanelItemsPage() {
		super(Invoice.class);
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("SearchPanel: fields of your own");

		ContentPanel cp = contentPanel();
		cp.add(new HTag(1, "SearchPanel: fields of your own"));

		SearchPanel<Invoice> sp = new SearchPanel<>(Invoice.class);
		cp.add(sp);
		sp.setClicked(a -> search(sp.getCriteria()));

		//-- A default value: the control starts with it, and Clear puts it back.
		Customer defaultCustomer = getSharedContext().get(Customer.class, Long.valueOf(10));
		sp.add().property(Invoice_.customer())
			.label("Invoiced to")
			.hint("The customer the invoice was made out to")
			.defaultValue(defaultCustomer)
			.control();

		//-- The value type of a number search is not a number: it is what was typed.
		sp.add().property(Invoice_.total())
			.defaultValue(new NumberLookupValue(QOperation.GE, BigDecimal.valueOf(5.0)))
			.control();

		//-- And a date search is a period.
		sp.add().property(Invoice_.invoiceDate())
			.defaultValue(new DatePeriod(null, DateUtil.dateFor(2010, 0, 1)))
			.control();

		//-- Search on a property of the customer, not of the invoice.
		sp.add().property("customer.city")
			.label("Customer city")
			.minLength(2)
			.control();

		cp.add(new Para().add("The panel opens with three of its four fields already filled "
			+ "in, and pressing Reset puts exactly those values back - not an empty form. "
			+ "That is what a default value is."));
		cp.add(new Para().add("Note what the defaults had to be: a NumberLookupValue for the "
			+ "total and a DatePeriod for the date. The value of a search control is what "
			+ "the user may express - '>= 5' or 'up to 1 January 2010' - not the type of the "
			+ "property being searched."));
		cp.add(new Para().add("The last field searches customer.city: a search property is a "
			+ "path, so a screen can search on the record behind the record."));
	}
}
