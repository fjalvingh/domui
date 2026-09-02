package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

/**
 * LookupInput2: what the three things it draws look like - the selected value,
 * the drop-down under the search box, and the dialog behind the button.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LookupInput2LookPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("LookupInput2: what it shows");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "LookupInput2: what it shows"));

		Customer first = getSharedContext().get(Customer.class, Long.valueOf(1));

		//-- Which columns of the value are shown when one is selected.
		LookupInput2<Customer> columns = new LookupInput2<>(Customer.class, "lastName", "city", "country");
		columns.setValue(first);

		//-- Or render the selected value yourself.
		LookupInput2<Customer> valueRenderer = new LookupInput2<>(Customer.class);
		valueRenderer.setValue(first);
		valueRenderer.setValueRenderer((node, customer) -> {
			node.add(new Span("dm-tut-hi", customer.getLastName()));
			node.add(", " + customer.getCity() + " (#" + customer.getId() + ")");
		});

		//-- The drop-down under the search box has a renderer of its own.
		LookupInput2<Customer> dropDown = new LookupInput2<>(Customer.class);
		dropDown.setKeywordSearchResultsDropDownRenderer((node, customer) ->
			node.add(customer.getLastName() + " - " + customer.getCity() + ", " + customer.getCountry()));
		dropDown.setKeySearchHint("last name");

		//-- And the dialog behind the button: which fields it searches on.
		LookupInput2<Customer> dialog = new LookupInput2<>(Customer.class);
		dialog.setSearchProperties(Customer_.lastName(), Customer_.city(), Customer_.country());
		dialog.setDefaultTitle("Pick the customer to invoice");
		dialog.setPopupSearchImmediately(true);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Value shown as three columns").control(columns);
		fb.label("Value rendered by hand").control(valueRenderer);
		fb.label("Own drop-down renderer, own hint").control(dropDown);
		fb.label("Own dialog fields and title").control(dialog);

		cp.add(new Para().add("The first two show the same customer: the constructor's column "
			+ "list and setValueRenderer() are the two ways to decide what a selected value "
			+ "looks like. Without either, the value is rendered from the @MetaObject "
			+ "display properties of the class."));
		cp.add(new Para().add("Type 'a' in the third box to see the drop-down renderer, and "
			+ "hover over the box to see the hint. Press the lookup button on the fourth to "
			+ "see a dialog with its own title, its own three search fields, and its results "
			+ "already there because of setPopupSearchImmediately(true)."));
	}
}
