package to.etc.domuidemo;

import to.etc.domuidemo.pages.MenuPage;
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
	}
}
