package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;

/**
 * SearchPanel: the whole search screen, with nothing configured at all - the
 * fields come from the metadata of the entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SearchPanelPage extends AbstractSearchPage<Invoice> {
	public SearchPanelPage() {
		super(Invoice.class);
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("SearchPanel: a search screen from metadata");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "SearchPanel: a search screen from metadata"));

		SearchPanel<Invoice> sp = new SearchPanel<>(Invoice.class);
		cp.add(sp);

		cp.add(new Para().add("Three lines, and not one of them says what to search on: the "
			+ "fields, their labels and their controls all come from the searchProperties of "
			+ "Invoice's @MetaObject - the invoice date, the billing city and the customer."));
		cp.add(new Para().add("Each field got the control its type asks for: a date range for "
			+ "the date, a text box for the city, and a LookupInput2 for the customer, "
			+ "because that is a relation to another table."));
		cp.add(new Para().add("Press Search with everything empty and you get every invoice: "
			+ "an empty panel means an unrestricted query, not an error."));

		//-- The result of a search appears here.
		Div results = new Div();
		cp.add(results);
		sp.setClicked(a -> showResult(results, sp.getCriteria()));
	}
}
