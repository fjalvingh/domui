package to.etc.domuidemo.pages.components.lookup;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.searchpanel.DefaultSearchFormBuilder;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;

/**
 * SearchPanel: mixing metadata with fields of your own, the buttons on the bar,
 * and splitting the form into two columns.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SearchPanelFormPage extends AbstractSearchPage<Invoice> {
	public SearchPanelFormPage() {
		super(Invoice.class);
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("SearchPanel: the form and its buttons");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "SearchPanel: the form and its buttons"));

		SearchPanel<Invoice> sp = new SearchPanel<>(Invoice.class);
		cp.add(sp);

		//-- Take the form builder in hand, so its addBreak() can be used below.
		DefaultSearchFormBuilder builder = new DefaultSearchFormBuilder();
		sp.setFormBuilder(builder);

		//-- One field of our own, then everything metadata has to offer.
		sp.add().property(Invoice_.billingAddress()).control();
		sp.add().action(() -> builder.addBreak());              // From here: a second column
		sp.addDefault();

		sp.setOnNew(a -> MsgBox2.on(this).info("This is where a new invoice would be made"));
		sp.setOnClear(a -> MsgBox2.on(this).info("Reset - every field is back to its default"));
		sp.setShowHideButton(Boolean.TRUE);

		cp.add(new Para().add("The billing address is ours; the three fields after the break "
			+ "are the ones Invoice's metadata asks for. addDefault() adds them behind what "
			+ "is already there, and skips any property that is already on the form."));
		cp.add(new Para().add("The action between them is executed while the form is being "
			+ "built, in the order the lines were added: this one tells the form builder to "
			+ "start a second column."));
		cp.add(new Para().add("Search and Reset are always there. Add appeared because a "
			+ "handler was set for it, and the hide button folds the whole form away - with "
			+ "the button bar staying behind so the search can still be repeated."));

		//-- The result of a search appears here.
		Div results = new Div();
		cp.add(results);
		sp.setClicked(a -> showResult(results, sp.getCriteria()));
	}
}
