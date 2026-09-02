package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * LookupInput2: the three ways of deciding which records it may find at all.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LookupInput2QueryPage extends UrlPage {
	/** The country the third control limits its search to; a field, because the page rebuilds on it. */
	private String m_country = "Brazil";

	@Override
	public void createContent() throws Exception {
		setPageTitle("LookupInput2: limiting what can be found");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "LookupInput2: limiting what can be found"));

		Div shown = new Div("dm-tut");
		shown.add("Every box below searches customers, but not the same customers.");

		//-- 1. A root criteria: everything the control finds is anded with this.
		QCriteria<Customer> usa = QCriteria.create(Customer.class).eq(Customer_.country(), "USA");
		LookupInput2<Customer> fromQuery = new LookupInput2<>(usa, "lastName", "city");

		//-- 2. A query manipulator: the same, but decided per search.
		LookupInput2<Customer> manipulated = new LookupInput2<>(Customer.class, "lastName", "country");
		manipulated.setQueryManipulator(q -> q.eq(Customer_.country(), m_country));

		ComboFixed2<String> countryC = ComboFixed2.createCombo("Brazil", "USA", "Canada", "France");
		countryC.setValue(m_country);
		countryC.setMandatory(true);
		countryC.setOnValueChanged(a -> {
			m_country = countryC.getValue();
			forceRebuild();
		});

		//-- 3. A fixed list: no database at all.
		List<Customer> five = getSharedContext().query(
			QCriteria.create(Customer.class).ascending(Customer_.lastName()).limit(5));
		LookupInput2<Customer> fromList = new LookupInput2<>(Customer.class, five);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Country to limit to").control(countryC);
		fb.label("Root criteria: country = USA").control(fromQuery);
		fb.label("Query manipulator: country = the combo").control(manipulated);
		fb.label("A fixed list of five").control(fromList);

		cp.add(new DefaultButton("Read the values", a -> {
			shown.removeAllChildren();
			shown.add("from query=" + name(fromQuery.getValue())
				+ ", manipulated=" + name(manipulated.getValue())
				+ ", from list=" + name(fromList.getValue()));
		}));
		cp.add(shown);

		cp.add(new Para().add("Search for 'a' in the second box: only American customers come "
			+ "back. Change the country above and search again in the third: the manipulator "
			+ "is asked on every search, so the same control looks somewhere else."));
		cp.add(new Para().add("The last box was given a list rather than a class, so it "
			+ "searches that list and never touches the database."));
	}

	private static String name(Customer c) {
		return c == null ? "null" : c.getLastName() + " (" + c.getCountry() + ")";
	}
}
