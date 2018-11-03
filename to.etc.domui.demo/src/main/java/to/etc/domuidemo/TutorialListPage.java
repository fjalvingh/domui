package to.etc.domuidemo;

import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.binding.tut1.BindingTut3;
import to.etc.domuidemo.pages.binding.tut1.BindingTut4;
import to.etc.domuidemo.pages.binding.tut1.BindingTut5;
import to.etc.domuidemo.pages.binding.tut1.InvoiceListPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-10-18.
 */
final public class TutorialListPage extends MenuPage {
	public TutorialListPage() {
		super("Tutorial pages");
	}

	@Override public void createContent() throws Exception {
		addCaption("Binding tutorial");
		addLink(InvoiceListPage.class, "Simple binding for editing a record");
		addLink(BindingTut3.class, "Binding to other things as values");
		addLink(BindingTut4.class, "Binding disabled for a button - part 1");
		addLink(BindingTut5.class, "Binding disabled for a button - fixed");
	}
}
