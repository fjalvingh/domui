package to.etc.domuidemo;

import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.binding.tut1.binding.BindingTut3;
import to.etc.domuidemo.pages.binding.tut1.binding.BindingTut4;
import to.etc.domuidemo.pages.binding.tut1.binding.BindingTut5;
import to.etc.domuidemo.pages.binding.tut1.InvoiceListPage;
import to.etc.domuidemo.pages.tutorial.components.ComponentChangePage;
import to.etc.domuidemo.pages.tutorial.components.ComponentFormPage;
import to.etc.domuidemo.pages.tutorial.components.ComponentStatePage;
import to.etc.domuidemo.pages.tutorial.database.QueryFirstPage;
import to.etc.domuidemo.pages.tutorial.database.QueryJoinPage;
import to.etc.domuidemo.pages.tutorial.database.QueryRestrictionsPage;
import to.etc.domuidemo.pages.tutorial.first.HelloClickPage;
import to.etc.domuidemo.pages.tutorial.first.HelloPage;
import to.etc.domuidemo.pages.tutorial.first.HelloTreePage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-10-18.
 */
final public class TutorialListPage extends MenuPage {
	public TutorialListPage() {
		super("Tutorial pages");
	}

	@Override public void createContent() throws Exception {
		addCaption("Building your first page");
		addLink(HelloPage.class, "A page with a div in it");
		addLink(HelloTreePage.class, "A page is a tree of tags");
		addLink(HelloClickPage.class, "Clicking a tag");

		addCaption("Using components");
		addLink(ComponentFormPage.class, "A form of components");
		addLink(ComponentStatePage.class, "readOnly, disabled and disabledBecause");
		addLink(ComponentChangePage.class, "Reacting to a changed value");

		addCaption("Using databases");
		addLink(QueryFirstPage.class, "Your first query");
		addLink(QueryRestrictionsPage.class, "Restrictions and combinators");
		addLink(QueryJoinPage.class, "Querying over a relation");

		addCaption("Binding tutorial");
		addLink(InvoiceListPage.class, "Simple binding for editing a record");
		addLink(BindingTut3.class, "Binding to other things as values");
		addLink(BindingTut4.class, "Binding disabled for a button - part 1");
		addLink(BindingTut5.class, "Binding disabled for a button - fixed");
	}
}
